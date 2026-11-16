// USER INTERFACE
// a third view that shows the target and the top n matches in one opencv window
// reuses the exact same controller call (queryTopN / queryTopNFromCsv),
// no feature or distance logic lives here
//
// usage: ./gui <targetImage> <featureType> <featureCsv> <N> [metric] [imageDir]
//   imageDir defaults to the folder the target lives in, so we can load the
//   match images to draw them
// USER INTERACTION: 
// keys:  any key or the window close button quits
#include <cstdio>
#include <cstdlib>
#include <cstring>
#include <string>
#include <vector>
#include <opencv2/opencv.hpp>
#include "cbir_engine.h"

// strip the folder part off a path
static std::string baseName(const std::string &path) {
    size_t slash = path.find_last_of('/');
    return slash == std::string::npos ? path : path.substr(slash + 1);
}

// grab the folder part of a path (so we know where match images live)
static std::string dirName(const std::string &path) {
    size_t slash = path.find_last_of('/');
    return slash == std::string::npos ? "." : path.substr(0, slash);
}

// resize an image to fit inside a tile while keeping its aspect ratio,
// then center it on a fixed-size gray tile so the grid lines up neatly
static cv::Mat makeTile(const cv::Mat &img, int tileW, int tileH) {
    cv::Mat tile(tileH, tileW, CV_8UC3, cv::Scalar(40, 40, 40));
    if (img.empty()) return tile;

    float scale = std::min((float)tileW / img.cols, (float)tileH / img.rows);
    int w = (int)(img.cols * scale), h = (int)(img.rows * scale);
    cv::Mat resized;
    cv::resize(img, resized, cv::Size(w, h));

    int ox = (tileW - w) / 2, oy = (tileH - h) / 2;
    resized.copyTo(tile(cv::Rect(ox, oy, w, h)));
    return tile;
}

// write a label onto a tile (filename / rank / distance)
static void label(cv::Mat &tile, const std::string &text) {
    int base = 0;
    cv::Size ts = cv::getTextSize(text, cv::FONT_HERSHEY_SIMPLEX, 0.45, 1, &base);
    // dark strip behind the text so it stays readable
    cv::rectangle(tile, cv::Rect(0, tile.rows - ts.height - 8, ts.width + 10, ts.height + 8),
                  cv::Scalar(0, 0, 0), cv::FILLED);
    cv::putText(tile, text, cv::Point(5, tile.rows - 6),
                cv::FONT_HERSHEY_SIMPLEX, 0.45, cv::Scalar(230, 230, 230), 1, cv::LINE_AA);
}

int main(int argc, char *argv[]) {
    if (argc < 5) {
        printf("usage: %s <targetImage> <featureType> <featureCsv> <N> [metric] [imageDir]\n", argv[0]);
        printf("  featureType: baseline | hist | multihist | texture | dnn | custom\n");
        return -1;
    }
    std::string target = argv[1], type = argv[2], csv = argv[3];
    int N = atoi(argv[4]);
    std::string metric = (argc >= 6) ? argv[5] : "cosine";
    std::string imageDir = (argc >= 7) ? argv[6] : dirName(target);

    // step 1,3,4 - ask the controller for the ranked matches (same as match.cpp)
    std::vector<Match> results;
    if (type == "dnn")
        results = queryTopNFromCsv(baseName(target), csv, metric, N);
    else
        results = queryTopN(target, type, csv, N);

    if (results.empty()) {
        printf("no results (check paths / feature type)\n");
        return -1;
    }

    // build the display: target on the left, matches in a row after it
    const int tileW = 220, tileH = 220, pad = 10;
    int cols = (int)results.size() + 1;            // +1 for the target tile
    int canvasW = cols * tileW + (cols + 1) * pad;
    int canvasH = tileH + 2 * pad + 26;            // room for a title strip
    cv::Mat canvas(canvasH, canvasW, CV_8UC3, cv::Scalar(25, 25, 25));

    // title strip
    cv::putText(canvas, "CBIR  -  feature: " + type + "   target: " + baseName(target),
                cv::Point(pad, 18), cv::FONT_HERSHEY_SIMPLEX, 0.5,
                cv::Scalar(200, 200, 200), 1, cv::LINE_AA);

    int top = 26 + pad;

    // target tile (load from the path we were given)
    cv::Mat tImg = cv::imread(target);
    cv::Mat tTile = makeTile(tImg, tileW, tileH);
    label(tTile, "TARGET");
    tTile.copyTo(canvas(cv::Rect(pad, top, tileW, tileH)));

    // match tiles
    for (size_t i = 0; i < results.size(); i++) {
        std::string path = imageDir + "/" + results[i].name;
        cv::Mat mImg = cv::imread(path);
        cv::Mat tile = makeTile(mImg, tileW, tileH);

        char cap[128];
        snprintf(cap, sizeof(cap), "%zu. %s  (%.3f)", i + 1,
                 results[i].name.c_str(), results[i].distance);
        label(tile, cap);

        int x = pad + (int)(i + 1) * (tileW + pad);
        tile.copyTo(canvas(cv::Rect(x, top, tileW, tileH)));
    }

    cv::namedWindow("CBIR results", cv::WINDOW_AUTOSIZE);
    cv::imshow("CBIR results", canvas);
    printf("showing %zu matches - press any key in the window to quit\n", results.size());
    cv::waitKey(0);
    return 0;
}
