package studio;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Graphics2D;
import java.awt.GridLayout;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.nio.file.Path;
import java.util.List;
import java.util.Set;
import java.util.function.Consumer;
import javax.imageio.ImageIO;
import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.SwingConstants;

/**
 * Shows the query image and its closest matches as a grid of thumbnails, each
 * with its rank, file name and distance, and a button to open it in the editor.
 */
final class SimilarImagesWindow extends JFrame {

  private static final int THUMBNAIL = 180;
  private static final int COLUMNS = 4;
  private static final int MAX_CAPTION = 22;

  /** File types the editor's ReaderFactory can open. */
  private static final Set<String> EDITOR_FORMATS = Set.of("jpg", "jpeg", "png", "ppm", "bmp");

  /**
   * @param owner        the editor window to position this one over
   * @param queryName    the editor's name for the query image
   * @param query        the query image's pixels
   * @param featureLabel a readable name for the feature type used
   * @param library      the folder that was searched
   * @param matches      the engine's results, closest first
   * @param openInEditor called with a result's file when "Open in editor" is pressed
   */
  SimilarImagesWindow(Component owner, String queryName, BufferedImage query, String featureLabel,
                      Path library, List<SearchEngineBridge.Match> matches,
                      Consumer<Path> openInEditor) {
    super("Similar to “" + queryName + "”");
    setDefaultCloseOperation(DISPOSE_ON_CLOSE);

    JPanel root = new JPanel(new BorderLayout(12, 12));
    root.setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));

    JPanel header = new JPanel(new FlowLayout(FlowLayout.LEFT, 12, 0));
    header.add(new JLabel(new ImageIcon(scale(query, 110))));
    header.add(new JLabel("<html><b>Top " + matches.size() + " matches</b> in "
        + escape(String.valueOf(library.getFileName())) + "<br>matched by " + escape(featureLabel)
        + " &middot; a lower distance means more similar</html>"));
    root.add(header, BorderLayout.NORTH);

    JPanel grid = new JPanel(new GridLayout(0, COLUMNS, 10, 10));
    for (SearchEngineBridge.Match match : matches) {
      grid.add(card(match, library.resolve(match.fileName()), openInEditor));
    }
    JScrollPane scroll = new JScrollPane(grid);
    scroll.setBorder(BorderFactory.createEmptyBorder());
    scroll.getVerticalScrollBar().setUnitIncrement(16);
    root.add(scroll, BorderLayout.CENTER);

    setContentPane(root);
    pack();
    Dimension size = getSize();
    setSize(Math.min(size.width, 900), Math.min(size.height, 720));
    setLocationRelativeTo(owner);
  }

  private static JPanel card(SearchEngineBridge.Match match, Path file, Consumer<Path> openInEditor) {
    JPanel card = new JPanel();
    card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));

    BufferedImage image = read(file);
    JLabel thumbnail = image != null
        ? new JLabel(new ImageIcon(scale(image, THUMBNAIL)))
        : new JLabel("(no preview)", SwingConstants.CENTER);
    thumbnail.setAlignmentX(Component.CENTER_ALIGNMENT);

    String detail = match.identical() ? "same photo" : String.format("distance %.4f", match.distance());
    JLabel caption = new JLabel("<html><center>" + match.rank() + ". " + escape(shorten(match.fileName()))
        + "<br>" + detail + "</center></html>", SwingConstants.CENTER);
    caption.setToolTipText(match.fileName());
    caption.setAlignmentX(Component.CENTER_ALIGNMENT);

    JButton open = new JButton("Open in editor");
    open.setAlignmentX(Component.CENTER_ALIGNMENT);
    open.setEnabled(EDITOR_FORMATS.contains(SearchEngineBridge.extension(file)));
    open.addActionListener(e -> openInEditor.accept(file));

    card.add(thumbnail);
    card.add(caption);
    card.add(open);
    return card;
  }

  private static BufferedImage read(Path file) {
    try {
      return ImageIO.read(file.toFile());   // null for formats ImageIO cannot decode, e.g. PPM
    } catch (IOException e) {
      return null;
    }
  }

  /**
   * @param source an image
   * @param max    the longest side of the result, in pixels
   * @return a copy scaled to fit within max x max, keeping its aspect ratio
   */
  static BufferedImage scale(BufferedImage source, int max) {
    double factor = Math.min(1.0, (double) max / Math.max(source.getWidth(), source.getHeight()));
    int w = Math.max(1, (int) Math.round(source.getWidth() * factor));
    int h = Math.max(1, (int) Math.round(source.getHeight() * factor));
    BufferedImage scaled = new BufferedImage(w, h, BufferedImage.TYPE_INT_RGB);
    Graphics2D g = scaled.createGraphics();
    g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
    g.drawImage(source, 0, 0, w, h, null);
    g.dispose();
    return scaled;
  }

  // long names would widen every card in the grid; the full name is in the tooltip
  private static String shorten(String fileName) {
    return fileName.length() <= MAX_CAPTION ? fileName
        : fileName.substring(0, MAX_CAPTION - 1) + "…";
  }

  private static String escape(String text) {
    return text.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;");
  }
}
