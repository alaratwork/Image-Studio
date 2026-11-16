package view;

import model.histogram.Histogram;

import javax.swing.*;
import java.awt.*;
import java.awt.geom.Path2D;

/**
 * A modular GUI component that visualizes a histogram.
 * Renders color-coded line graphs for red, green, blue,
 * This class extends JPanel a Histogram object.
 */
public class HistogramView extends JPanel {
  private Histogram histogram;


  /**
   * Sets the Histogram object to be displayed.
   * @param histogram the Histogram object
   */
  public void setHistogram(Histogram histogram) {
    this.histogram = histogram;
    // this will ensure it's repainted for each image
    repaint();
  }

  /**
   * Paints the histogram.
   * @param g Graphics object
   */
  @Override
  protected void paintComponent(Graphics g) {
    super.paintComponent(g);
    if (histogram == null) {
      return;
    }
    // from java builtin features / documentation
    Graphics2D g2d = (Graphics2D) g;
    g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
        RenderingHints.VALUE_ANTIALIAS_ON);

    int width = getWidth();
    int height = getHeight();
    int max_value = histogram.getMaximumValue();
    // methods to draw the histogram
    // with individual line charts for each color
    drawHistogram(g2d, histogram.getRedPix(),
        Color.RED, width, height, max_value);
    drawHistogram(g2d, histogram.getGreenPix(),
        Color.GREEN, width, height, max_value);
    drawHistogram(g2d, histogram.getBluePix(),
        Color.BLUE, width, height, max_value);
    drawHistogram(g2d, histogram.getIntensityValue(),
        Color.GRAY, width, height, max_value);
  }

  /**
   * Draws a line graph for a given color.
   * @param g2d Graphics2D object
   * @param histogram_data array of histogram values
   * @param color Color object
   * @param width width of the graph
   * @param height height of the graph
   * @param max_value maximum value across all channels
   */
  private void drawHistogram(Graphics2D g2d, int[] histogram_data,
                             Color color, int width,
                             int height, int max_value) {
    if (max_value == 0) {
      return;
    }
    Path2D path = new Path2D.Double();
    path.moveTo(0, height);
    for (int i = 0; i < 256; i++) {
      double x = (double) i / 255.0 * width;
      double y = height
          - ((double) histogram_data[i] / max_value * height);
      path.lineTo(x, y);
    }
    path.lineTo(width, height); // finish the line at the bottom right
    path.closePath();
    // fill the area under the curve with a translucent color
    g2d.setColor(new Color(color.getRed(),
        color.getGreen(), color.getBlue(), 64));
    g2d.fill(path);
    // draw the outline of the graph
    g2d.setColor(color);
    g2d.draw(path);
  }
}