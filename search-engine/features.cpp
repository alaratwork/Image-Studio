// MVC: MODEL
// RESPONSIBLE FOR: IMAGE (img)-> VECTOR(vector)
// translates an image into a vector of numbers
// steps 1 & 2 (features on the target and on the database)
// 1. compute the features on a target
// 2. compute the features in the database
// -- where an image becomes numbers
// -- adds a new feature() function per task, then wires it into computeFeature and distanceFor
#include <vector>
#include <string>
#include <cmath>
#include <opencv2/opencv.hpp>
#include "features.h"

// section start:------------ HISTOGRAM HELPER FUNCTIONS

// FEATURE: HISTOGRAM, helper 1/3
// normalizes a histogram in place so all of the bins add up to 1
// helper function to compute the features on a target and database - intersection needs normalized inputs
static void normalize(std::vector<float> &h) {
    float total = 0.0f;
    for (float v : h) total += v;
    if (total <= 0.0f) return;          // empty
    for (float &v : h) v /= total;
}

// FEATURE: HISTOGRAM, helper 2/3
// 2di rg-chromaticity histogram over a region of interest on an image area
// helper function to compute the features on a target and database - intersection needs normalized inputs
// r = R/(R+G+B), g = G/(R+G+B), bins x bins grid
static std::vector<float> rgHist(const cv::Mat &img, const cv::Rect &roi, int bins) {
    std::vector<float> h(bins * bins, 0.0f);
    for (int y = roi.y; y < roi.y + roi.height; y++) {
        for (int x = roi.x; x < roi.x + roi.width; x++) {
            cv::Vec3b px = img.at<cv::Vec3b>(y, x);   // bgr
            float B = px[0], G = px[1], R = px[2];
            float sum = R + G + B;
            if (sum <= 0.0f) sum = 1.0f;              // dont divide by zero
            float r = R / sum, g = G / sum;
            int ri = (int)(r * bins); if (ri >= bins) ri = bins - 1;
            int gi = (int)(g * bins); if (gi >= bins) gi = bins - 1;
            h[ri * bins + gi] += 1.0f;
        }
    }
    normalize(h);
    return h;
}

// FEATURE: HISTOGRAM, helper 3/3
// 3d rgb histogram over a region of interest on an image area
// helper function to compute the features on a target and database - intersection needs normalized inputs
// bins per channel -> bins^3 numbers
static std::vector<float> rgbHist(const cv::Mat &img, const cv::Rect &roi, int bins) {
    std::vector<float> h(bins * bins * bins, 0.0f);
    int div = 256 / bins;
    for (int y = roi.y; y < roi.y + roi.height; y++) {
        for (int x = roi.x; x < roi.x + roi.width; x++) {
            cv::Vec3b px = img.at<cv::Vec3b>(y, x);   // bgr
            int bi = px[0] / div; if (bi >= bins) bi = bins - 1;
            int gi = px[1] / div; if (gi >= bins) gi = bins - 1;
            int ri = px[2] / div; if (ri >= bins) ri = bins - 1;
            h[(ri * bins + gi) * bins + bi] += 1.0f;
        }
    }
    normalize(h);
    return h;
}

// section end:------------ HISTOGRAM HELPER FUNCTIONS

// section start:------------ FEATURE COMPUTATION

// baseline matching: use the 7x7 square in the middle of the image as a feature vector->
// use sum-of-squared-difference as the distance metric, comparing the image with itself results in a distance of 0.
// grab the 7x7 block in the center and flatten it (bgr per pixel)
// 147 numbers, distance is ssd, self compare has to be 0
std::vector<float> featureBaseline(const cv::Mat &img) {
    const int S = 7;
    int cy = img.rows / 2;        // center row
    int cx = img.cols / 2;        // center col
    int half = S / 2;             // 3, so the window runs center-3 to center+3

    std::vector<float> feat;
    feat.reserve(S * S * 3);
    for (int dy = -half; dy <= half; dy++) {
        for (int dx = -half; dx <= half; dx++) {
            int y = cy + dy, x = cx + dx;
            // clamp so we dont fall off the edge on a tiny image
            if (y < 0) y = 0;
            if (y >= img.rows) y = img.rows - 1;
            if (x < 0) x = 0;
            if (x >= img.cols) x = img.cols - 1;
            cv::Vec3b px = img.at<cv::Vec3b>(y, x);   // bgr
            feat.push_back((float)px[0]);
            feat.push_back((float)px[1]);
            feat.push_back((float)px[2]);
        }
    }
    return feat;
}

// color histogram: calculates the histograms from images and implements a histogram intersection
// whole-image 2d rg-chromaticity histogram, 16 bins each
// drops brightness, keeps color ratio -> 16*16 = 256 normalized numbers
std::vector<float> featureHist(const cv::Mat &img) {
    const int bins = 16;
    return rgHist(img, cv::Rect(0, 0, img.cols, img.rows), bins);
}

// multi-histogram (spatial): histograms that represent diferent spatial parts of the image
// rgb histogram of the top half + rgb histogram of the bottom half, stacked
// 8 bins each -> two 512-length histograms -> 1024 numbers
// stacking them means histIntersection compares the halves and adds up,
// which equally weights top vs bottom
std::vector<float> featureMultiHist(const cv::Mat &img) {
    const int bins = 8;
    int halfH = img.rows / 2;
    cv::Rect top(0, 0, img.cols, halfH);
    cv::Rect bottom(0, halfH, img.cols, img.rows - halfH);

    std::vector<float> ht = rgbHist(img, top, bins);
    std::vector<float> hb = rgbHist(img, bottom, bins);

    // each half is already normalized, halve them so the stacked vector
    // still sums to 1 and the two regions count equally
    for (float &v : ht) v *= 0.5f;
    for (float &v : hb) v *= 0.5f;

    std::vector<float> feat;
    feat.reserve(ht.size() + hb.size());
    feat.insert(feat.end(), ht.begin(), ht.end());
    feat.insert(feat.end(), hb.begin(), hb.end());
    return feat;
}

// texture + color: uses a whole image color histogram and a whole image texture histogram as the feature vector
// sobel gradient-magnitude histogram over the whole image
// build a grayscale gradient magnitude, then histogram it into `bins`
static std::vector<float> textureHist(const cv::Mat &img, int bins) {
    cv::Mat gray;
    cv::cvtColor(img, gray, cv::COLOR_BGR2GRAY);

    // sobel in x and y, then magnitude (opencv is allowed for task 4)
    cv::Mat gx, gy;
    cv::Sobel(gray, gx, CV_32F, 1, 0);
    cv::Sobel(gray, gy, CV_32F, 0, 1);
    cv::Mat mag;
    cv::magnitude(gx, gy, mag);   // sqrt(gx^2 + gy^2) per pixel

    std::vector<float> h(bins, 0.0f);
    // magnitudes can run past 255, so cap the range we bin into
    const float maxMag = 360.0f;
    for (int y = 0; y < mag.rows; y++) {
        for (int x = 0; x < mag.cols; x++) {
            float m = mag.at<float>(y, x);
            int b = (int)(m / maxMag * bins);
            if (b < 0) b = 0;
            if (b >= bins) b = bins - 1;
            h[b] += 1.0f;
        }
    }
    normalize(h);
    return h;
}

// whole-image color histogram + whole-image texture histogram, stacked equally
// rg-chroma color hist (256) + sobel magnitude hist (256), each weighted half
std::vector<float> featureTexture(const cv::Mat &img) {
    std::vector<float> color = rgHist(img, cv::Rect(0, 0, img.cols, img.rows), 16);
    std::vector<float> tex = textureHist(img, 256);

    for (float &v : color) v *= 0.5f;   // weight the two histograms equally
    for (float &v : tex) v *= 0.5f;

    std::vector<float> feat;
    feat.reserve(color.size() + tex.size());
    feat.insert(feat.end(), color.begin(), color.end());
    feat.insert(feat.end(), tex.begin(), tex.end());
    return feat;
}

// histogram custom design layer
// design: center-weighted color + whole-image texture
// idea - the center region usually holds the subject, so a center color
// histogram + a whole-image texture histogram
std::vector<float> featureCustom(const cv::Mat &img) {
    // center crop = middle half of width and height
    int cw = img.cols / 2, ch = img.rows / 2;
    int cx = img.cols / 4, cy = img.rows / 4;
    cv::Rect center(cx, cy, cw, ch);

    std::vector<float> color = rgHist(img, center, 16);
    std::vector<float> tex = textureHist(img, 256);

    // weight color a bit heavier (0.6) than texture (0.4) for this design
    for (float &v : color) v *= 0.6f;
    for (float &v : tex) v *= 0.4f;

    std::vector<float> feat;
    feat.reserve(color.size() + tex.size());
    feat.insert(feat.end(), color.begin(), color.end());
    feat.insert(feat.end(), tex.begin(), tex.end());
    return feat;
}

// section end:------------ FEATURE COMPUTATION

// section start:------------ DISPATCH

// dispatch by type string
std::vector<float> computeFeature(const cv::Mat &img, const std::string &type) {
    if (type == "baseline")  return featureBaseline(img);
    if (type == "hist")      return featureHist(img);
    if (type == "multihist") return featureMultiHist(img);
    if (type == "texture")   return featureTexture(img);
    if (type == "custom")    return featureCustom(img);
    return std::vector<float>();   // dont know this type
}

// pairs each feature type with its distance metric
std::string distanceFor(const std::string &type) {
    if (type == "baseline") return "ssd";
    if (type == "hist" || type == "multihist" ||
        type == "texture" || type == "custom") return "histIntersection";
    if (type == "dnn") return "cosine";
    return "ssd";
}
// section end:------------ DISPATCH
