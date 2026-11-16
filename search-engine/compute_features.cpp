// PROGRAM 1/1: OFFLINE
// drives @match- builds the feature csv for the whole database
// read the args, call the controller, print the count
// run this once per feature type 
// usage: ./compute_features <imageDir> <featureType> <outCsv>
#include <cstdio>
#include <string>
#include "cbir_engine.h"

int main(int argc, char *argv[]) {
    if (argc < 4) {
        printf("usage: %s <imageDir> <featureType> <outCsv>\n", argv[0]);
        printf("  featureType: baseline\n");
        return -1;
    }
    int n = indexDirectory(argv[1], argv[2], argv[3]);   // step 2
    if (n < 0) return -1;
    printf("done. wrote %d feature vectors to %s\n", n, argv[3]);
    return 0;
}
