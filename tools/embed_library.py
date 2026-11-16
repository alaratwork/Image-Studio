"""Write ResNet-18 embeddings for a photo library, for the search engine's dnn mode.

The engine's `dnn` feature ranks images by distance between deep-network
embeddings, but it does not compute them: it reads them from a CSV with one row
per image, `<filename>,<512 numbers>`. This tool writes that CSV for any folder,
using an ImageNet-pretrained ResNet-18 with its classifier removed, so each
image is described by its 512-dimensional global-average-pooled features.

    python tools/embed_library.py samples/library build/embeddings.csv
    search-engine/match salt_IMG_5481.jpg dnn build/embeddings.csv 4 cosine

dnn mode looks the query up by file name in the CSV, so queries must be images
from the library that was embedded.
"""

import argparse
import os
import sys

import torch
import torchvision
from PIL import Image, ImageOps

# the same file types compute_features indexes (search-engine/cbir_engine.cpp, isImage)
EXTENSIONS = (".jpg", ".jpeg", ".png", ".tif", ".ppm")


def build_model():
    """ResNet-18 pretrained on ImageNet, returning features instead of class scores."""
    weights = torchvision.models.ResNet18_Weights.DEFAULT
    model = torchvision.models.resnet18(weights=weights)
    model.fc = torch.nn.Identity()
    model.eval()
    return model, weights.transforms()


def library_images(folder):
    """Image file names in a folder, sorted, skipping hidden files."""
    return sorted(name for name in os.listdir(folder)
                  if not name.startswith(".") and name.lower().endswith(EXTENSIONS))


def main():
    parser = argparse.ArgumentParser(description=__doc__.split("\n\n")[0])
    parser.add_argument("library", help="folder of images to embed")
    parser.add_argument("output", help="CSV file to write")
    args = parser.parse_args()

    if not os.path.isdir(args.library):
        sys.exit(f"not a folder: {args.library}")
    names = library_images(args.library)
    if not names:
        sys.exit(f"no images ({', '.join(EXTENSIONS)}) in {args.library}")

    model, preprocess = build_model()
    torch.manual_seed(0)
    rows = 0
    with torch.no_grad(), open(args.output, "w") as out:
        for name in names:
            path = os.path.join(args.library, name)
            try:
                # honour EXIF orientation, as OpenCV does when the engine reads the same file
                image = ImageOps.exif_transpose(Image.open(path)).convert("RGB")
            except OSError as e:
                print(f"  skip (unreadable): {name} ({e})")
                continue
            vector = model(preprocess(image).unsqueeze(0)).squeeze(0)
            out.write(name + "," + ",".join(f"{v:.6f}" for v in vector.tolist()) + "\n")
            rows += 1

    print(f"wrote {rows} embeddings ({vector.numel()} dimensions each) to {args.output}")


if __name__ == "__main__":
    main()
