// MCV: CONTROLLER
// the two programs only call these, they never touch the model directly
// owns the 4 step order: 1 feature on target, 2 features on database,
// 3 distance to each, 4 sort ascending and return top n
// owns the order of the 4 steps but none of the actual feature/distance math
#include <vector>
#include <string>
#include <algorithm>
#include <cstring>
#include <cstdlib>
#include <dirent.h>
#include <opencv2/opencv.hpp>
#include "cbir_engine.h"
#include "features.h"
#include "distances.h"
#include "csv_util.h"

// section start:------------ HELPERS

// check: is this filename one of the image types we read?
// relevant to: features on database
// helper, filters the directory walk
static bool isImage(const char *name) {
    const char *dot = strrchr(name, '.');
    if (!dot) return false;
    return strcasecmp(dot, ".jpg") == 0 || strcasecmp(dot, ".jpeg") == 0 ||
           strcasecmp(dot, ".png") == 0 || strcasecmp(dot, ".tif") == 0 ||
           strcasecmp(dot, ".ppm") == 0;
}

// strip the folder part off a path
// relevant to: sorting in ascending order and returning top m
// csv stores bare filenames so it can strip to match
static const char *baseName(const char *path) {
    const char *slash = strrchr(path, '/');
    return slash ? slash + 1 : path;
}

// send the two vectors to the right metric by name
// keeps the metric choice in one place
static float dispatchDistance(const std::string &metric,
                              const std::vector<float> &a,
                              const std::vector<float> &b) {
    if (metric == "ssd") return ssd(a, b);
    if (metric == "histIntersection") return histIntersection(a, b);
    if (metric == "cosine") return cosineDist(a, b);
    return ssd(a, b);
}

// section end:------------ HELPERS

// section start:------------ DATABASE BUILD

// walk the folder, get each image's feature, write it to the csv
// (offline database build) | task: all
int indexDirectory(const std::string &imageDir, const std::string &featureType,
                   const std::string &outCsv) {
    DIR *dirp = opendir(imageDir.c_str());
    if (!dirp) {
        printf("cannot open dir %s\n", imageDir.c_str());
        return -1;
    }
    // append_image_data_csv wants a char*, so copy the path into a buffer
    char outBuf[1024];
    strncpy(outBuf, outCsv.c_str(), sizeof(outBuf) - 1);
    outBuf[sizeof(outBuf) - 1] = '\0';

    struct dirent *dp;
    int count = 0, reset = 1;          // reset=1 wipes the csv on the first image
    while ((dp = readdir(dirp)) != nullptr) {
        if (!isImage(dp->d_name)) continue;

        char path[1024];
        snprintf(path, sizeof(path), "%s/%s", imageDir.c_str(), dp->d_name);
        cv::Mat img = cv::imread(path);
        if (img.empty()) {
            printf("  skip (unreadable): %s\n", dp->d_name);
            continue;
        }

        std::vector<float> feat = computeFeature(img, featureType);  // step 2
        if (feat.empty()) {
            printf("unknown feature type '%s'\n", featureType.c_str());
            closedir(dirp);
            return -1;
        }

        append_image_data_csv(outBuf, dp->d_name, feat, reset);            // write the row
        reset = 0;                                                   // append from here
        count++;
        if (count % 100 == 0) printf("  processed %d...\n", count);
    }
    closedir(dirp);
    return count;
}

// section end:------------ DATABASE BUILD

// section start:------------ QUERY

// feature on the target, distance to everyone, sort, hand back the top n
std::vector<Match> queryTopN(const std::string &targetPath,
                             const std::string &featureType,
                             const std::string &featureCsv, int N) {
    std::vector<Match> out;

    // step 1 - feature on the target image (computed live from the pixels)
    cv::Mat target = cv::imread(targetPath);
    if (target.empty()) {
        printf("cannot read target %s\n", targetPath.c_str());
        return out;
    }
    std::vector<float> tfeat = computeFeature(target, featureType);
    if (tfeat.empty()) {
        printf("unknown feature type '%s'\n", featureType.c_str());
        return out;
    }

    std::string metric = distanceFor(featureType);   // which metric this feature uses

    // load the precomputed database (step 3 prep)
    char csvBuf[1024];
    strncpy(csvBuf, featureCsv.c_str(), sizeof(csvBuf) - 1);
    csvBuf[sizeof(csvBuf) - 1] = '\0';
    std::vector<char *> names;
    std::vector<std::vector<float>> feats;
    if (read_image_data_csv(csvBuf, names, feats, 0) != 0) return out;

    // step 3 - distance from the target to every database image
    const char *targetName = baseName(targetPath.c_str());
    std::vector<Match> all;
    all.reserve(feats.size());
    for (size_t i = 0; i < feats.size(); i++) {
        if (strcmp(names[i], targetName) == 0) continue;   // skip the target itself
        all.push_back({dispatchDistance(metric, tfeat, feats[i]),
                       std::string(names[i])});
    }
    for (char *p : names) free(p);

    // step 4 - sort smallest first and keep the top n
    std::sort(all.begin(), all.end(),
              [](const Match &a, const Match &b) { return a.distance < b.distance; });
    int lim = (N < (int)all.size()) ? N : (int)all.size();
    out.assign(all.begin(), all.begin() + lim);
    return out;
}

// look the target vector up in the embeddings csv, then rank
// (resnet18 embeddings)
// unlike the others we dont open an image - the 512 numbers are already in
// the csv, so step 1 is finding the target's row by filename
std::vector<Match> queryTopNFromCsv(const std::string &targetName,
                                    const std::string &featureCsv,
                                    const std::string &metric, int N) {
    std::vector<Match> out;

    char csvBuf[1024];
    strncpy(csvBuf, featureCsv.c_str(), sizeof(csvBuf) - 1);
    csvBuf[sizeof(csvBuf) - 1] = '\0';
    std::vector<char *> names;
    std::vector<std::vector<float>> feats;
    if (read_image_data_csv(csvBuf, names, feats, 0) != 0) return out;

    // step 1 - find the target's row by filename (lookup, not compute)
    int tIdx = -1;
    for (size_t i = 0; i < names.size(); i++) {
        if (strcmp(names[i], targetName.c_str()) == 0) { tIdx = (int)i; break; }
    }
    if (tIdx < 0) {
        printf("target %s not found in %s\n", targetName.c_str(), featureCsv.c_str());
        for (char *p : names) free(p);
        return out;
    }
    std::vector<float> tfeat = feats[tIdx];

    // step 3 - distance from target to every other row
    std::vector<Match> all;
    all.reserve(feats.size());
    for (size_t i = 0; i < feats.size(); i++) {
        if ((int)i == tIdx) continue;   // skip the target itself
        all.push_back({dispatchDistance(metric, tfeat, feats[i]),
                       std::string(names[i])});
    }
    for (char *p : names) free(p);

    // step 4 - sort smallest first and keep the top n
    std::sort(all.begin(), all.end(),
              [](const Match &a, const Match &b) { return a.distance < b.distance; });
    int lim = (N < (int)all.size()) ? N : (int)all.size();
    out.assign(all.begin(), all.begin() + lim);
    return out;
}

// section end:------------ QUERY
