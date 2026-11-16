// MVC: MODEL
// RESPONSIBLE FOR: IMAGE (img)-> VECTOR(vector)
// translates an image into a vector of numbers

// steps 1 & 2 (features on the target and on the database)
// 1. compute the features on a target
//    - relevant files: csv_util files, connection point for writing and reading files
// 2. compute the features in the database
// one extractor per task, picked by a type string

#ifndef FEATURES_H
#define FEATURES_H

#include <vector>
#include <string>
#include <opencv2/opencv.hpp>

// the 7x7 square in the middle of the image, flattened (7*7*3 = 147 numbers)
// task 1 (baseline) -> goes with ssd
std::vector<float> featureBaseline(const cv::Mat &img);

// 2d rg-chromaticity histogram, normalized (16 bins each -> 256 numbers)
// (histogram matching) -> goes with histIntersection
std::vector<float> featureHist(const cv::Mat &img);

// two rgb histograms, top half and bottom half, stacked
// multi-histogram) -> goes with histIntersection
std::vector<float> featureMultiHist(const cv::Mat &img);

// whole-image color histogram + sobel gradient-magnitude histogram, stacked
// (texture + color) -> goes with histIntersection
std::vector<float> featureTexture(const cv::Mat &img);

// histogram custom blend: center rg-chroma histogram + whole-image texture
// -> goes with histIntersection
std::vector<float> featureCustom(const cv::Mat &img);

// dispatch - hands the image to the right extractor based on the type string
std::vector<float> computeFeature(const cv::Mat &img, const std::string &type);

// which distance goes with which feature type
// returns "ssd" / "histIntersection" / "cosine"
std::string distanceFor(const std::string &type);

#endif
