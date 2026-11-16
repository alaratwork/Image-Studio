# Makefile - CS5330 Project 2 (CBIR), MVC layout
# Apple Silicon Mac, OpenCV via Homebrew (/opt/homebrew)

CXX      = clang++
CXXFLAGS = -std=c++17 -Wall -O2 -I/opt/homebrew/include/opencv4
LDFLAGS  = -L/opt/homebrew/lib \
           -lopencv_core -lopencv_imgproc -lopencv_imgcodecs -lopencv_highgui
# if libs arent found, swap the line above for:
# LDFLAGS = $(shell pkg-config --libs opencv4)

# model (features, distances, csv_util) + controller (cbir_engine)
COMMON = features.o distances.o csv_util.o cbir_engine.o

all: compute_features match gui

compute_features: compute_features.o $(COMMON)
	$(CXX) $(CXXFLAGS) -o $@ $^ $(LDFLAGS)

match: match.o $(COMMON)
	$(CXX) $(CXXFLAGS) -o $@ $^ $(LDFLAGS)

# gui is an extension - a third view, same controller
gui: gui.o $(COMMON)
	$(CXX) $(CXXFLAGS) -o $@ $^ $(LDFLAGS)

%.o: %.cpp
	$(CXX) $(CXXFLAGS) -c $< -o $@

clean:
	rm -f *.o compute_features match gui

.PHONY: all clean
