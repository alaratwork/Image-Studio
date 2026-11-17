# Image Studio - one build for both components.
#
#   make              build the editor and the search engine
#   make editor       compile the Java editor into build/image-editor.jar
#   make engine       build the C++ search engine (compute_features, match, gui)
#   make demo         index samples/library and run an example visual search
#   make run-editor   launch the editor
#   make studio       compile Image Studio (the editor plus Search) into build/image-studio.jar
#   make run-studio   launch Image Studio
#   make test         run the bridge checks, including end-to-end searches
#   make embeddings   write ResNet-18 embeddings for the library (needs tools/requirements.txt)
#   make demo-dnn     search by those embeddings
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
STUDIO_SRC := $(shell find studio/src -name '*.java')
TEST_SRC   := $(shell find studio/test -name '*.java')
PYTHON     ?= python3

# demo defaults - override on the command line, e.g. make demo FEATURE=hist
LIBRARY ?= samples/library
FEATURE ?= custom
QUERY   ?= $(LIBRARY)/salt_IMG_5481.jpg
TOP     ?= 4

.PHONY: all editor engine studio demo demo-dnn embeddings run-editor run-studio test clean

all: editor engine studio

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

studio: $(BUILD)/image-studio.jar

# the editor's sources compile unchanged alongside the new studio package
$(BUILD)/image-studio.jar: $(EDITOR_SRC) $(STUDIO_SRC)
	@mkdir -p $(BUILD)/studio-classes
	$(JAVAC) --release 17 -d $(BUILD)/studio-classes $(EDITOR_SRC) $(STUDIO_SRC)
	$(JAR) --create --file $@ --main-class studio.ImageStudio -C $(BUILD)/studio-classes .

test: studio engine
	@mkdir -p $(BUILD)/test-classes
	$(JAVAC) --release 17 -cp $(BUILD)/studio-classes -d $(BUILD)/test-classes $(TEST_SRC)
	$(JAVA) -Djava.awt.headless=true -cp $(BUILD)/studio-classes:$(BUILD)/test-classes studio.BridgeTest

embeddings:
	@mkdir -p $(BUILD)
	$(PYTHON) tools/embed_library.py $(LIBRARY) $(BUILD)/embeddings.csv

demo-dnn: engine
	search-engine/match $(notdir $(QUERY)) dnn $(BUILD)/embeddings.csv $(TOP) cosine

run-editor: editor
	$(JAVA) -jar $(BUILD)/image-editor.jar

run-studio: studio engine
	$(JAVA) -jar $(BUILD)/image-studio.jar

clean:
	$(MAKE) -C search-engine clean
	rm -rf $(BUILD)
