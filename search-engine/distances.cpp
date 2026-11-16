// MVC: MODEL
// compares the distance between 2 vectors
// vector(distance) vs vector(distance)
// compute and compare target to each database image
// smaller distance means more similar

#include <vector>
#include <cmath>
#include "distances.h"

// sum of squared differences
float ssd(const std::vector<float> &a, const std::vector<float> &b) {
    float sum = 0.0f;
    size_t n = a.size() < b.size() ? a.size() : b.size();
    for (size_t i = 0; i < n; i++) {
        float d = a[i] - b[i];
        sum += d * d;
    }
    return sum;
}

// histogram intersection as a distance
// assumes the inputs are already normalized
float histIntersection(const std::vector<float> &a, const std::vector<float> &b) {
    float inter = 0.0f;
    size_t n = a.size() < b.size() ? a.size() : b.size();
    for (size_t i = 0; i < n; i++)
        inter += std::min(a[i], b[i]);   // how much the two histograms overlap
    return 1.0f - inter;                 // flip it so smaller means more similar
}

// cosine distance
// normalize each vector by its length then dot them
float cosineDist(const std::vector<float> &a, const std::vector<float> &b) {
    float dot = 0.0f, na = 0.0f, nb = 0.0f;
    size_t n = a.size() < b.size() ? a.size() : b.size();
    for (size_t i = 0; i < n; i++) {
        dot += a[i] * b[i];
        na += a[i] * a[i];               // length of a, squared
        nb += b[i] * b[i];               // length of b, squared
    }
    if (na == 0.0f || nb == 0.0f) return 1.0f;   // dont divide by zero
    return 1.0f - (dot / (std::sqrt(na) * std::sqrt(nb)));
}
