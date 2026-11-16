// MVC: MODEL
// compares the distance between 2 vectors
// vector(distance) vs vector(distance)
// compute and compare target to each database image
// smaller distance means more similar

#ifndef DISTANCES_H
#define DISTANCES_H

#include <vector>

// sum of squared differences
// comparing a vector with itself has to come out to 0
float ssd(const std::vector<float> &a, const std::vector<float> &b);

// histogram intersection turned into a distance
// intersection is the overlap of the two histograms,  return 1 - overlap
// inputs need to be normalized first
float histIntersection(const std::vector<float> &a, const std::vector<float> &b);

// cosine distance(resnet18 embeddings)
// d = 1 - cos(angle between the two vectors)
float cosineDist(const std::vector<float> &a, const std::vector<float> &b);

#endif
