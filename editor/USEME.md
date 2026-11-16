# Image Manipulation and Enhancement (IME)

## Summary

This is a GUI-based image processing application built in Java Swing.  
It is designed with a Model-View-Controller (MVC) architecture.

---

## Key Features

- Multi-format image support (PPM, PNG, JPEG, BMP)  
- Transformation operations (brightness, greyscale)  
- Live-updating color histogram

---

## How to Run the Program

To launch the application, run the `IMEProgram.java` file.

---

## Features & How to Use

The application window is divided into the **Operations Panel** on the left and the **Image Preview Area** on the right.

### File Operations

Use the **"Load Image"** and **"Save Image"** buttons to manage your files.  
You will be prompted to name new images, which will appear in the **Image List**.

### Brightness Operations

Use the textbox to set an amount, then click **"Brighten"** or **"Darken"** to apply the effect to the selected image.

### Greyscale Operations

Click any of the six greyscale buttons (e.g., **"Red"**, **"Luma"**) to apply that filter to the selected image.

### Image List

This list displays all loaded and created images.  
Click a name to preview the image and update its histogram.

### Image Preview and Histogram

The right side of the application displays the selected image on top and a live histogram of its color values below.  
The histogram includes a key to identify the **Red**, **Green**, **Blue**, and **Intensity** channels.

