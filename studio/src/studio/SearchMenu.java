package studio;

import controller.ImageController;
import controller.ImageDatabase;
import java.awt.Cursor;
import java.awt.Toolkit;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.StringReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import javax.swing.ButtonGroup;
import javax.swing.JFileChooser;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JRadioButtonMenuItem;
import javax.swing.KeyStroke;
import javax.swing.SwingWorker;
import model.abstraction.InputConverter;
import model.abstraction.writer.WritePNG;
import model.image.IImage;
import view.View;

/**
 * Adds a Search menu to the editor's window: Find similar, a choice of
 * feature type, and the photo library to search. Everything goes through the
 * editor's public API (View, ImageDatabase, ImageController, the PNG writer),
 * so none of the editor's own classes change.
 */
final class SearchMenu {

  /** How many matches to show. */
  static final int RESULTS = 8;

  /** Readable names for the engine's feature types, in menu order. */
  static final Map<String, String> FEATURE_LABELS = new LinkedHashMap<>();

  static {
    FEATURE_LABELS.put("custom", "Center color + texture");
    FEATURE_LABELS.put("hist", "Color histogram");
    FEATURE_LABELS.put("multihist", "Color layout (top vs. bottom)");
    FEATURE_LABELS.put("texture", "Color + texture");
    FEATURE_LABELS.put("baseline", "Center pixels");
  }

  private final View view;
  private final ImageDatabase database;
  private final SearchEngineBridge bridge;
  private Path library;
  private String feature = SearchEngineBridge.FEATURES.get(0);
  private JMenuItem libraryItem;

  private SearchMenu(View view, ImageDatabase database, SearchEngineBridge bridge, Path library) {
    this.view = view;
    this.database = database;
    this.bridge = bridge;
    this.library = library;
  }

  /**
   * Installs the Search menu on an editor window.
   *
   * @param view     the editor's window
   * @param database the editor's image store
   * @param bridge   the connection to the search engine
   * @param library  the photo library to search until the user picks another
   */
  static void install(View view, ImageDatabase database, SearchEngineBridge bridge, Path library) {
    JMenuBar bar = new JMenuBar();
    bar.add(new SearchMenu(view, database, bridge, library).build());
    view.setJMenuBar(bar);
    view.revalidate();
  }

  private JMenu build() {
    JMenu search = new JMenu("Search");

    JMenuItem find = new JMenuItem("Find similar…");
    find.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_F,
        Toolkit.getDefaultToolkit().getMenuShortcutKeyMaskEx() | InputEvent.SHIFT_DOWN_MASK));
    find.addActionListener(e -> findSimilar());
    search.add(find);
    search.addSeparator();

    JMenu matchBy = new JMenu("Match by");
    ButtonGroup group = new ButtonGroup();
    for (Map.Entry<String, String> entry : FEATURE_LABELS.entrySet()) {
      JRadioButtonMenuItem item = new JRadioButtonMenuItem(entry.getValue(),
          entry.getKey().equals(feature));
      item.addActionListener(e -> feature = entry.getKey());
      group.add(item);
      matchBy.add(item);
    }
    search.add(matchBy);

    libraryItem = new JMenuItem();
    libraryItem.addActionListener(e -> chooseLibrary());
    updateLibraryLabel();
    search.add(libraryItem);
    return search;
  }

  private void findSimilar() {
    String name = view.getSelectedImageName();
    IImage image = name == null ? null : database.get_image(name);
    if (image == null) {
      view.displayError("Load an image (or select one in the list), then search for similar photos.");
      return;
    }
    Path query;
    try {
      // the engine reads image files, so hand it the image as currently edited
      query = Files.createTempFile("image-studio-query-", ".png");
      query.toFile().deleteOnExit();
      new WritePNG().write(image, query.toString());
    } catch (IOException | IllegalArgumentException e) {
      view.displayError("Could not prepare the image for searching: " + e.getMessage());
      return;
    }

    String chosenFeature = feature;
    Path chosenLibrary = library;
    BufferedImage preview = InputConverter.convertInput(image);
    view.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));

    // indexing a new library can take a few seconds - keep the editor responsive
    new SwingWorker<List<SearchEngineBridge.Match>, Void>() {
      @Override
      protected List<SearchEngineBridge.Match> doInBackground() throws SearchException {
        return bridge.findSimilar(query, chosenLibrary, chosenFeature, RESULTS);
      }

      @Override
      protected void done() {
        view.setCursor(Cursor.getDefaultCursor());
        try {
          new SimilarImagesWindow(view, name, preview,
              FEATURE_LABELS.get(chosenFeature).toLowerCase(Locale.ROOT),
              chosenLibrary, get(), SearchMenu.this::openInEditor).setVisible(true);
        } catch (ExecutionException e) {
          Throwable cause = e.getCause() != null ? e.getCause() : e;
          view.displayError(cause.getMessage());
        } catch (InterruptedException e) {
          Thread.currentThread().interrupt();
        } finally {
          try {
            Files.deleteIfExists(query);
          } catch (IOException ignored) {
            // deleteOnExit is the fallback
          }
        }
      }
    }.execute();
  }

  // loads a result into the editor's image list and selects it
  private void openInEditor(Path file) {
    String name = uniqueName(baseName(file));
    try {
      new ImageController(new StringReader(""), new StringBuilder(), database)
          .loadImage(file.toString(), name);
    } catch (IllegalArgumentException e) {
      view.displayError(e.getMessage());
      return;
    }
    String[] names = database.get_all_images().keySet().toArray(new String[0]);
    Arrays.sort(names);
    view.updateImageDatabase(names);
    view.setSelectedImage(name);
    view.toFront();
  }

  private void chooseLibrary() {
    JFileChooser chooser = new JFileChooser(library.toFile());
    chooser.setDialogTitle("Choose a photo library");
    chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
    if (chooser.showOpenDialog(view) == JFileChooser.APPROVE_OPTION) {
      library = chooser.getSelectedFile().toPath();
      updateLibraryLabel();
    }
  }

  private void updateLibraryLabel() {
    libraryItem.setText("Photo library: " + library.getFileName() + "…");
    libraryItem.setToolTipText(library.toAbsolutePath().toString());
  }

  private String uniqueName(String base) {
    String name = base;
    for (int n = 2; database.has_image(name); n++) {
      name = base + " (" + n + ")";
    }
    return name;
  }

  private static String baseName(Path file) {
    String name = file.getFileName().toString();
    int dot = name.lastIndexOf('.');
    return dot > 0 ? name.substring(0, dot) : name;
  }
}
