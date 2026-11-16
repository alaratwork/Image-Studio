// MCV: CONTROLLER
// the two programs only call these, they never touch the model directly
// owns the 4 step order: 1 feature on target, 2 features on database,
// 3 distance to each, 4 sort ascending and return top n
#ifndef CBIR_ENGINE_H
#define CBIR_ENGINE_H

#include <vector>
#include <string>

// one ranked result - a distance and the filename it belongs to
// flow: step 4 output, comes back sorted closest first
struct Match {
    float distance;
    std::string name;
};

// build the feature csv for a whole folder
// (features on the database, done offline)
// featureType is baseline / hist / multihist / texture / custom (not dnn)
// returns how many images it indexed, or -1 if something went wrong
int indexDirectory(const std::string &imageDir, const std::string &featureType,
                   const std::string &outCsv);

// rank the database against one target image
// (feature on target -> distances -> sort top n)
// computes the target feature live, target itself is left out, empty on error
std::vector<Match> queryTopN(const std::string &targetPath,
                             const std::string &featureType,
                             const std::string &featureCsv, int N);

// task 5 variant - the dnn embeddings come precomputed in a csv, so step 1
// is a LOOKUP by filename instead of computing a feature from the image
// metric is cosine or ssd
// targetName is just the filename
std::vector<Match> queryTopNFromCsv(const std::string &targetName,
                                    const std::string &featureCsv,
                                    const std::string &metric, int N);

#endif
