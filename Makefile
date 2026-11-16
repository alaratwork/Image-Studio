# Image Studio - one build for both components.
#
#   make              build the editor and the search engine
#   make editor       compile the Java editor into build/image-editor.jar
#   make engine       build the C++ search engine (compute_features, match, gui)
#   make demo         index samples/library and run an example visual search
#   make run-editor   launch the editor
#   make clean
#
# Neither component's own build is modified: the engine's makefile is driven
# with OpenCV flags from pkg-config, so it builds wherever OpenCV is installed.

JAVAC ?= javac
JAR   ?= jar
JAVA  ?= java
BUILD := build

# Ubuntu ships OpenCV 4, current Homebrew ships OpenCV 5 - use whichever pkg-config knows.
OPENCV_PKG    := $(shell pkg-config --exists opencv4 2>/dev/null && echo opencv4 || echo opencv5)
OPENCV_CFLAGS := $(shell pkg-config --cflags $(OPENCV_PKG) 2>/dev/null)
OPENCV_LIBS   := $(shell pkg-config --libs-only-L $(OPENCV_PKG) 2>/dev/null) \
                 -lopencv_core -lopencv_imgproc -lopencv_imgcodecs -lopencv_highgui

EDITOR_SRC := $(shell find editor/src -name '*.java')

# demo defaults - override on the command line, e.g. make demo FEATURE=hist
LIBRARY ?= samples/library
FEATURE ?= custom
QUERY   ?= $(LIBRARY)/salt_IMG_5481.jpg
TOP     ?= 4

.PHONY: all editor engine demo run-editor clean

all: editor engine

editor: $(BUILD)/image-editor.jar

$(BUILD)/image-editor.jar: $(EDITOR_SRC)
	@mkdir -p $(BUILD)/editor-classes
	$(JAVAC) --release 17 -d $(BUILD)/editor-classes $(EDITOR_SRC)
	$(JAR) --create --file $@ --main-class IMEProgram -C $(BUILD)/editor-classes .

engine:
	$(MAKE) -C search-engine CXXFLAGS="-std=c++17 -Wall -O2 $(OPENCV_CFLAGS)" LDFLAGS="$(OPENCV_LIBS)"

demo: engine
	@mkdir -p $(BUILD)
	search-engine/compute_features $(LIBRARY) $(FEATURE) $(BUILD)/feat_$(FEATURE).csv
	search-engine/match $(QUERY) $(FEATURE) $(BUILD)/feat_$(FEATURE).csv $(TOP)

run-editor: editor
	$(JAVA) -jar $(BUILD)/image-editor.jar

clean:
	$(MAKE) -C search-engine clean
	rm -rf $(BUILD)
