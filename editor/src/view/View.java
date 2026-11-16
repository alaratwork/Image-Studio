package view;

import model.histogram.Histogram;

import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.*;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;

/**
 * Represents the primary graphical user interface (GUI).
 * Extends {@code JFrame} and provides functionalities for loading,
 * displaying, and manipulating images.
 * Manages the layout and user interaction components.
 * Serves as the primary interface.
 * The class follows an MVC structure.
 * Registers a {@link ViewListener} for handling user actions.
 */
public class View extends JFrame {
  // labels display, not for interaction
  private final JLabel label;
  // this is the user-facing component
  // displays the list of items
  private final JList<String> loaded_images;
  // back end of a list that handles logic
  // stores the list of items
  private final DefaultListModel<String> loaded_images_model;
  private final HistogramView histogramPanel;
  // this controller is the view controller, new listener
  private ViewListener listener;

  /**
   * Constructs a new View instance for an Image Editor application.
   * This constructor initializes the primary GUI component.
   * The layout is designed to include:
   * 1. Left panel for operations
   * 2. Right panel for image display and a histogram panel.
   * 3. A split-pane layout separating them.
   */
  public View() {
    super("Image Editor");
    // set up window dimensions
    setSize(1200, 800);
    setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    setLayout(new BorderLayout(10, 10));
    applyLightTheme();

    // set up components within the screen
    label = new JLabel();
    label.setHorizontalAlignment(JLabel.CENTER); // Center the image
    JScrollPane image_scroller = new JScrollPane(label);

    loaded_images_model = new DefaultListModel<>();
    // populate the list display with items
    loaded_images = new JList<>(loaded_images_model);
    // selection interaction for list item
    loaded_images.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
    // event listener to handle selection
    loaded_images.addListSelectionListener(e -> {
      if (!e.getValueIsAdjusting() && listener != null) {
        listener.handleImageSelection(loaded_images.getSelectedValue());
      }
    });

    // histogram panel/container
    histogramPanel = new HistogramView();
    histogramPanel.setBorder(BorderFactory.createTitledBorder("Histogram " +
        "Values"));
    histogramPanel.setPreferredSize(new Dimension(0,
        200));

    // right side contains image and histogram
    JPanel rightSidePanel =
        new JPanel(new BorderLayout(0, 10));
    rightSidePanel.add(image_scroller, BorderLayout.CENTER);
    rightSidePanel.add(histogramPanel, BorderLayout.SOUTH);

    // left side is toolbar
    JSplitPane main_split_pane =
        new JSplitPane(JSplitPane.HORIZONTAL_SPLIT,
        createOperationsPanel(), rightSidePanel);
    main_split_pane.setDividerLocation(350);
    add(main_split_pane, BorderLayout.CENTER);
    setVisible(true);
  }

  /**
   * Sets the listener for handling view events.
   * Integrates a controller that implements the ViewListener.
   * The controller will handle user interactions such as
   * button clicks or other view-triggered events.
   * @param listener the ViewListener instance responsible for managing
   *                 events triggered by the view
   */
  public void setViewListener(ViewListener listener) {
    this.listener = listener;
  }

  /**
   * Creates and configures a JPanel.
   * Containing operational buttons
   * and controls for interacting with images.
   * It also provides a list of loaded
   * images for user interaction.
   * File Operations:
   * - Load Image
   * - Save Image
   * Brightness Operations:
   * - Brighten/Darken
   * Greyscale Operations:
   * - Red, Green, Blue, Luma, Intensity, and Value
   * Image List:
   * - Displays a list of loaded images
   *
   * @return a JPanel with all operational controls
   */
  private JPanel createOperationsPanel() {
    JPanel toolbar = new JPanel(new BorderLayout(10,
        10));
    toolbar.setBorder(BorderFactory.createEmptyBorder(10,
        10, 10, 10));
    //  all the operation buttons
    JPanel topPanel = new JPanel();
    topPanel.setLayout(new BoxLayout(topPanel,
        BoxLayout.Y_AXIS));

    // file operations
    JPanel filePanel = new JPanel(new GridLayout(1, 2,
        10, 10));
    filePanel.setBorder(BorderFactory.createTitledBorder("File " +
        "Operations"));
    JButton loadButton = new JButton("Load Image");
    loadButton.addActionListener(e -> {
      if (listener != null) listener.handleLoadImage();
    });
    JButton saveButton = new JButton("Save Image");
    saveButton.addActionListener(e -> {
      if (listener != null) listener.handleSaveImage();
    });
    filePanel.add(loadButton);
    filePanel.add(saveButton);
    topPanel.add(filePanel);
    topPanel.add(Box.createRigidArea(new Dimension(0,
        15))); // Spacer

    // brightness operations
    JPanel lighting_panel =
        new JPanel(new FlowLayout(FlowLayout.LEFT));
    lighting_panel.setBorder(BorderFactory.createTitledBorder("Brightness " +
        "Operations"));
    JTextField brighten_field = new JTextField("50", 5);
    JButton brighten_button = new JButton("Brighten");
    brighten_button.addActionListener(e -> {
      if (listener != null)
        listener.handleBrighten(brighten_field.getText());
    });
    JButton darken_button = new JButton("Darken");
    darken_button.addActionListener(e -> {
      if (listener != null)
        listener.handleDarken(brighten_field.getText());
    });
    lighting_panel.add(new JLabel("Amount: "));
    lighting_panel.add(brighten_field);
    lighting_panel.add(brighten_button);
    lighting_panel.add(darken_button);
    topPanel.add(lighting_panel);
    topPanel.add(Box.createRigidArea(new Dimension(0,
        15))); // Spacer

    // greyscale operations
    JPanel greyscalePanel = new JPanel(new GridLayout(0,
        2, 5, 5));
    greyscalePanel.setBorder(BorderFactory.createTitledBorder("Greyscale " +
        "Operations"));
    greyscalePanel.add(createOperationsButton("Red",
        e -> listener.handleRedGreyscale()));
    greyscalePanel.add(createOperationsButton("Green",
        e -> listener.handleGreenGreyscale()));
    greyscalePanel.add(createOperationsButton("Blue",
        e -> listener.handleBlueGreyscale()));
    greyscalePanel.add(createOperationsButton("Luma",
        e -> listener.handleLumaGreyscale()));
    greyscalePanel.add(createOperationsButton("Intensity",
        e -> listener.handleIntensityGreyscale()));
    greyscalePanel.add(createOperationsButton("Value",
        e -> listener.handleValueGreyscale()));
    topPanel.add(greyscalePanel);
    toolbar.add(topPanel, BorderLayout.NORTH);

    // list of loaded images to fill the remaining space
    JScrollPane list_scroller =
        new JScrollPane(loaded_images);
    list_scroller.setBorder(BorderFactory.createTitledBorder("Image " +
        "List"));
    toolbar.add(list_scroller, BorderLayout.CENTER);

    return toolbar;
  }

  /**
   * Creates a JButton, attaches an ActionListener to it.
   * Used internally for creating buttons in the toolbar
   * @param text the text to display on the button
   * @param action_listener the ActionListener
   * @return the created JButton
   */
  private JButton createOperationsButton(String text,
                                         ActionListener action_listener) {
    JButton button = new JButton(text);
    button.addActionListener(action_listener);
    return button;
  }

  /**
   * Applies a clean, light theme to the UI.
   */
  private void applyLightTheme() {
    try {
      UIManager.setLookAndFeel(UIManager.
          getSystemLookAndFeelClassName());
    } catch (Exception e) {
      System.err.println("System look and feel " +
          "not found. Using default.");
    }
  }

  /**
   * Updates the displayed image in the view.
   * @param image_name the BufferedImage to be set
   */
  public void updateImage(BufferedImage image_name) {
    label.setIcon(image_name != null ?
        new ImageIcon(image_name) : null);
  }

  /**
   * Updates the list of image names displayed.
   * @param image_names an array of image names
   */
  public void updateImageDatabase(String[] image_names) {
    String selected = loaded_images.getSelectedValue();
    loaded_images_model.clear();
    for (String image_name : image_names) {
      loaded_images_model.addElement(image_name);
    }
    if (selected != null) {
      loaded_images.setSelectedValue(selected,
          false);
    }
  }

  /**
   * Sets the selected image from the list of loaded images.
   * This method updates the view to reflect the
   * currently selected image.
   * @param image_name the name of the image to be selected
   */
  public void setSelectedImage(String image_name) {
    loaded_images.setSelectedValue(image_name,
        true);
  }

  /**
   * Updates the histogram panel with the given histogram.
   * @param histogram the histogram to be displayed
   */
  public void updateHistogram(Histogram histogram) {
    histogramPanel.setHistogram(histogram);
  }

  /**
   * Clears the histogram panel.
   */
  public void clearHistogram() {
    histogramPanel.setHistogram(null);
  }

  /**
   * Displays a pop-up message to the user.
   * @param message the message to be displayed
   */
  public void displayMessage(String message) {
    JOptionPane.showMessageDialog(this, message);
  }

  /**
   * Displays a save-as dialog to the user.
   * @return the path to the selected file
   */
  public String exportSaveWindow() {
    JFileChooser file_chooser =
        new JFileChooser(".");
    if (file_chooser.showSaveDialog(this)
        == JFileChooser.APPROVE_OPTION) {
      return file_chooser.getSelectedFile().getAbsolutePath();
    }
    return null;
  }

  /**
   * Displays a load-from dialog to the user.
   * @return the path to the selected file
   */
  public String importLoadWindow() {
    JFileChooser file_chooser = new JFileChooser(".");
    FileNameExtensionFilter extension =
        new FileNameExtensionFilter("Image Files: ",
            "bmp", "jpg", "jpeg", "png", "ppm");
    file_chooser.setFileFilter(extension);
    if (file_chooser.showOpenDialog(this)
        == JFileChooser.APPROVE_OPTION) {
      return file_chooser.getSelectedFile().getAbsolutePath();
    }
    return null;
  }

  /**
   * Displays a pop-up input window to the user.
   * @param message the message to be displayed
   * @param value the default value to be displayed
   * @return the user-entered value
   */
  public String popUpInputWindow(String message,
                                 String value) {
    return JOptionPane.showInputDialog(this,
        message, value);
  }

  /**
   * Displays an error message to the user.
   * @param message the message to be displayed
   */
  public void displayError(String message) {
    JOptionPane.showMessageDialog(this, message,
        "Error", JOptionPane.ERROR_MESSAGE);
  }

  /**
   * Returns the selected image name from the list.
   * @return the selected image name
   */
  public String getSelectedImageName() {
    return loaded_images.getSelectedValue();
  }
}