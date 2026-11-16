// PROGRAM 2/2 - QUERY 
// feature on the target, rank everyone, print the top n
// stays thin: read the args, call the controller, print what comes back
// run this per query
// two modes:
//   normal:  ./match <targetImage> <featureType> <featureCsv> <N>
//   task 5:  ./match <targetFilename> dnn <embeddingsCsv> <N> [cosine|ssd]
//            (dnn looks the target up in the csv instead of opening an image)
#include <cstdio>
#include <cstdlib>
#include <cstring>
#include <string>
#include <vector>
#include "cbir_engine.h"

// strip the folder part off a path so it matches the csv filenames
static const char *baseName(const char *path) {
    const char *slash = strrchr(path, '/');
    return slash ? slash + 1 : path;
}

int main(int argc, char *argv[]) {
    if (argc < 5) {
        printf("usage: %s <targetImage> <featureType> <featureCsv> <N> [metric]\n", argv[0]);
        printf("  featureType: baseline | hist | multihist | texture | dnn | custom\n");
        printf("  metric (dnn only): cosine (default) or ssd\n");
        return -1;
    }
    std::string target = argv[1], type = argv[2], csv = argv[3];
    int N = atoi(argv[4]);

    std::vector<Match> results;

    if (type == "dnn") {
        // the target vector lives in the csv, look it up by filename
        std::string metric = (argc >= 6) ? argv[5] : "cosine";
        std::string targetName = baseName(target.c_str());
        results = queryTopNFromCsv(targetName, csv, metric, N);
    } else {
        // everything else - compute the feature from the image
        results = queryTopN(target, type, csv, N);   // steps 1,3,4
    }
    if (results.empty()) return -1;

    // output - print the ranked filenames
    printf("target: %s   feature: %s\n", argv[1], type.c_str());
    printf("top %d matches:\n", N);
    for (size_t i = 0; i < results.size(); i++)
        printf("  %2zu.  %-16s  (dist %.4f)\n", i + 1,
               results[i].name.c_str(), results[i].distance);
    return 0;
}
