package richtextfield;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.GraphicsEnvironment;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JColorChooser;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextPane;
import javax.swing.JToggleButton;
import javax.swing.JToolBar;
import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;
import javax.swing.text.AttributeSet;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyledEditorKit;
import javax.swing.text.html.HTMLEditorKit;
import javax.swing.undo.UndoManager;
import richtextfield.images.ImageLoader;

public class RichTextField extends JPanel {

    public static final ImageIcon ICON_OPEN = ImageLoader.get(ImageLoader.PATH_ICON_OPEN);
    public static final ImageIcon ICON_SAVE = ImageLoader.get(ImageLoader.PATH_ICON_SAVE);
    public static final ImageIcon ICON_UNDO = ImageLoader.get(ImageLoader.PATH_ICON_UNDO);
    public static final ImageIcon ICON_REDO = ImageLoader.get(ImageLoader.PATH_ICON_REDO);
    public static final ImageIcon ICON_CUT = ImageLoader.get(ImageLoader.PATH_ICON_CUT);
    public static final ImageIcon ICON_COPY = ImageLoader.get(ImageLoader.PATH_ICON_COPY);
    public static final ImageIcon ICON_PASTE = ImageLoader.get(ImageLoader.PATH_ICON_PASTE);
    public static final ImageIcon ICON_BOLD = ImageLoader.get(ImageLoader.PATH_ICON_BOLD);
    public static final ImageIcon ICON_UNDERLINE = ImageLoader.get(ImageLoader.PATH_ICON_UNDERLINE);
    public static final ImageIcon ICON_ITALIC = ImageLoader.get(ImageLoader.PATH_ICON_ITALIC);
    public static final ImageIcon ICON_ADD_INDENT = ImageLoader.get(ImageLoader.PATH_ICON_ADD_INDENT);
    public static final ImageIcon ICON_REMOVE_INDENT = ImageLoader.get(ImageLoader.PATH_ICON_REMOVE_INDENT);
    public static final ImageIcon ICON_LIST = ImageLoader.get(ImageLoader.PATH_ICON_LIST);
    public static final ImageIcon ICON_NUMERED_LIST = ImageLoader.get(ImageLoader.PATH_ICON_NUMERED_LIST);
    public static final ImageIcon ICON_ALIGN_LEFT = ImageLoader.get(ImageLoader.PATH_ICON_ALIGN_LEFT);
    public static final ImageIcon ICON_ALIGN_CENTER = ImageLoader.get(ImageLoader.PATH_ICON_ALIGN_CENTER);
    public static final ImageIcon ICON_ALIGN_RIGHT = ImageLoader.get(ImageLoader.PATH_ICON_ALIGN_RIGHT);
    public static final ImageIcon ICON_ALIGN_JUSTIFIED = ImageLoader.get(ImageLoader.PATH_ICON_ALIGN_JUSTIFIED);
    public static final ImageIcon ICON_ADD_PICTURE = ImageLoader.get(ImageLoader.PATH_ICON_ADD_PICTURE);
    public static final ImageIcon ICON_SCREENSHOT = ImageLoader.get(ImageLoader.PATH_ICON_SCREENSHOT);

    public static final String ID_ACTION_OPEN = "openAction";
    public static final String ID_ACTION_SAVE = "saveAction";
    public static final String ID_ACTION_UNDO = "undoAction";
    public static final String ID_ACTION_REDO = "redoAction";
    public static final String ID_ACTION_CUT = "cutAction";
    public static final String ID_ACTION_COPY = "copyAction";
    public static final String ID_ACTION_PASTE = "pasteAction";
    public static final String ID_ACTION_FONT_FAMILY = "fontFamilyAction";
    public static final String ID_ACTION_FONT_SIZE = "fontSizeAction";
    public static final String ID_ACTION_FONT_COLOR = "fontColorAction";
    public static final String ID_ACTION_FONT_BACKGROUND = "fontBackgroundAction";
    public static final String ID_ACTION_BOLD = "boldAction";
    public static final String ID_ACTION_UNDERLINE = "underlineAction";
    public static final String ID_ACTION_ITALIC = "italicAction";
    public static final String ID_ACTION_ADD_INDENT = "addIndentAction";
    public static final String ID_ACTION_REMOVE_INDENT = "removeIndentAction";
    public static final String ID_ACTION_LIST = "listAction";
    public static final String ID_ACTION_NUMERED_LIST = "numeredListAction";
    public static final String ID_ACTION_ALIGN_LEFT = "alignLeftAction";
    public static final String ID_ACTION_ALIGN_CENTER = "alignCenterAction";
    public static final String ID_ACTION_ALIGN_RIGHT = "alignRightAction";
    public static final String ID_ACTION_ALIGN_JUSTIFIED = "alignJustifiedAction";
    public static final String ID_ACTION_ADD_PICTURE = "addPictureAction";
    public static final String ID_ACTION_SCREENSHOT = "screenshot";

    private final JToolBar toolbar;
    private final JScrollPane scrollpane;
    private final JTextPane textPane;
    private final UndoManager undoManager;

    public RichTextField() {
        super();

        toolbar = new JToolBar();
        scrollpane = new JScrollPane();
        textPane = new JTextPane();
        undoManager = new UndoManager();

        configureComponents();
    }

    private void configureComponents() {
        configureLayout();
        configureToolBar();
        configureTextPane();

        this.validate();
        this.repaint();
    }

    private void configureLayout() {
        this.setLayout(new BorderLayout());

        scrollpane.setViewportView(textPane);
        this.add(toolbar, BorderLayout.NORTH);
        this.add(scrollpane, BorderLayout.CENTER);
    }

    private void configureToolBar() {
        toolbar.setRollover(true);
        toolbar.setFloatable(false);

        addButtonToToolbar(ID_ACTION_OPEN, "Abrir", ICON_OPEN,
                createToolbarActionListener(e -> HTMLActions.loadFromHTML(textPane)));
        addButtonToToolbar(ID_ACTION_SAVE, "Guardar", ICON_SAVE,
                createToolbarActionListener(e -> HTMLActions.saveAsHTML(textPane)));

        toolbar.addSeparator();

        addButtonToToolbar(ID_ACTION_UNDO, "Deshacer", ICON_UNDO, e -> {
            if (undoManager.canUndo()) {
                undoManager.undo();
            }
        });
        addButtonToToolbar(ID_ACTION_REDO, "Rehacer", ICON_REDO, e -> {
            if (undoManager.canRedo()) {
                undoManager.redo();
            }
        });

        toolbar.addSeparator();

        addButtonToToolbar(ID_ACTION_CUT, "Cortar", ICON_CUT,
                new HTMLEditorKit.CutAction());
        addButtonToToolbar(ID_ACTION_COPY, "Copiar", ICON_COPY,
                new HTMLEditorKit.CopyAction());
        addButtonToToolbar(ID_ACTION_PASTE, "Pegar", ICON_PASTE,
                new HTMLEditorKit.PasteAction());

        toolbar.addSeparator();

        toolbar.add(new JLabel(" Fuente: "));

        JComboBox fontCombo = new JComboBox(GraphicsEnvironment.getLocalGraphicsEnvironment().getAvailableFontFamilyNames());
        fontCombo.setName(ID_ACTION_FONT_FAMILY);
        fontCombo.addItemListener((e) -> {
            SwingUtilities.invokeLater(() -> {
                new HTMLEditorKit.FontFamilyAction(ID_ACTION_FONT_FAMILY, (String) fontCombo.getSelectedItem())
                        .actionPerformed(new ActionEvent(e.getSource(), 0, ID_ACTION_FONT_FAMILY));
                updateStyleButtons();
            });
        });
        toolbar.add(fontCombo);

        JComboBox sizeComboBox = new JComboBox(new String[]{"8", "10", "12", "14", "16", "18", "20", "24", "32", "40", "60", "80"});
        sizeComboBox.setName(ID_ACTION_FONT_SIZE);
        sizeComboBox.setEditable(true);
        sizeComboBox.addItemListener((e) -> {
            SwingUtilities.invokeLater(() -> {
                new HTMLEditorKit.FontSizeAction(ID_ACTION_FONT_SIZE, Integer.parseInt(sizeComboBox.getSelectedItem().toString()))
                        .actionPerformed(new ActionEvent(textPane, 0, ID_ACTION_FONT_SIZE));
            });
        });
        toolbar.add(sizeComboBox);

        addButtonToToolbar(ID_ACTION_FONT_COLOR, "Color de la Fuente", generateColorIcon(Color.BLACK), (e) -> {
            SwingUtilities.invokeLater(() -> {
                AttributeSet attrs = textPane.getCharacterAttributes();
                Color newColor = JColorChooser.showDialog((Component) e.getSource(), "Color", StyleConstants.getForeground(attrs));
                if (newColor != null) {
                    new HTMLEditorKit.ForegroundAction(ID_ACTION_FONT_COLOR, newColor)
                            .actionPerformed(new ActionEvent(textPane, 0, ID_ACTION_FONT_COLOR));
                    updateStyleButtons();
                }
            });
        });

        toolbar.addSeparator();

        toolbar.add(new JLabel(" Fondo: "));

        addButtonToToolbar(ID_ACTION_FONT_BACKGROUND, "Color de fondo", generateColorIcon(Color.BLACK), (e) -> {
            SwingUtilities.invokeLater(() -> {
                AttributeSet attrs = textPane.getCharacterAttributes();
                Color newColor = JColorChooser.showDialog((Component) e.getSource(), "Color", StyleConstants.getBackground(attrs));
                if (newColor != null) {
                    HTMLActions.setTextBackground(textPane, newColor);
                    updateStyleButtons();
                }
            });
        });

        toolbar.addSeparator();

        addToggleButtonToToolbar(ID_ACTION_BOLD, "Negrita", ICON_BOLD,
                new HTMLEditorKit.BoldAction());
        addToggleButtonToToolbar(ID_ACTION_UNDERLINE, "Subrayado", ICON_UNDERLINE,
                new HTMLEditorKit.UnderlineAction());
        addToggleButtonToToolbar(ID_ACTION_ITALIC, "Cursiva", ICON_ITALIC,
                new HTMLEditorKit.ItalicAction());

        toolbar.addSeparator();

        addButtonToToolbar(ID_ACTION_REMOVE_INDENT, "Quitar indentado", ICON_REMOVE_INDENT,
                (e) -> HTMLActions.removeIndent(textPane));
        addButtonToToolbar(ID_ACTION_ADD_INDENT, "Insertar indentado", ICON_ADD_INDENT,
                (e) -> HTMLActions.addIndent(textPane));

        toolbar.addSeparator();

        addButtonToToolbar(ID_ACTION_LIST, "Lista", ICON_LIST,
                e -> HTMLActions.setAsList(textPane, false));
        addButtonToToolbar(ID_ACTION_NUMERED_LIST, "Lista Numerada", ICON_NUMERED_LIST,
                e -> HTMLActions.setAsList(textPane, true));

        toolbar.addSeparator();

        addToggleButtonToToolbar(ID_ACTION_ALIGN_LEFT, "Alineación Izquierda", ICON_ALIGN_LEFT,
                new HTMLEditorKit.AlignmentAction(ID_ACTION_ALIGN_LEFT, StyleConstants.ALIGN_LEFT));
        addToggleButtonToToolbar(ID_ACTION_ALIGN_CENTER, "Alineación Centrada", ICON_ALIGN_CENTER,
                new HTMLEditorKit.AlignmentAction(ID_ACTION_ALIGN_CENTER, StyleConstants.ALIGN_CENTER));
        addToggleButtonToToolbar(ID_ACTION_ALIGN_RIGHT, "Alineación Derecha", ICON_ALIGN_RIGHT,
                new HTMLEditorKit.AlignmentAction(ID_ACTION_ALIGN_RIGHT, StyleConstants.ALIGN_RIGHT));
        addToggleButtonToToolbar(ID_ACTION_ALIGN_JUSTIFIED, "Alineación Justificada", ICON_ALIGN_JUSTIFIED,
                new HTMLEditorKit.AlignmentAction(ID_ACTION_ALIGN_JUSTIFIED, StyleConstants.ALIGN_JUSTIFIED));

        toolbar.addSeparator();

        addButtonToToolbar(ID_ACTION_ADD_PICTURE, "Insertar imagen", ICON_ADD_PICTURE,
                (e) -> HTMLActions.addImage(textPane));
        addButtonToToolbar(ID_ACTION_SCREENSHOT, "Capturar pantalla", ICON_SCREENSHOT,
                (e) -> HTMLActions.captureScreenShot(textPane));
    }

    private ImageIcon generateColorIcon(Color color) {
        return ImageLoader.generateRectangleColorIcon(16, 16, color);
    }

    private void configureTextPane() {
        textPane.setContentType("text/html");
        textPane.setEditorKit(new ScaledHTMLEditorKit());
        textPane.addCaretListener(e -> updateStyleButtons());
        HTMLListText.ListParaKeyListener textWithListListener = new HTMLListText.ListParaKeyListener(textPane);
        textPane.addKeyListener(textWithListListener);
        textPane.addCaretListener(textWithListListener);
        textPane.getStyledDocument().addUndoableEditListener(e -> undoManager.addEdit(e.getEdit()));
        textPane.addComponentListener(new ComponentAdapter() {
            @Override
            public void componentResized(ComponentEvent e) {
                // Forzamos recalculo de layout y repaint
                textPane.revalidate();
                textPane.repaint();
            }
        });

        textPane.getInputMap().put(KeyStroke.getKeyStroke(KeyEvent.VK_S, InputEvent.CTRL_DOWN_MASK), ID_ACTION_SAVE);
        textPane.getActionMap().put(ID_ACTION_SAVE, createToolbarActionListener(e -> HTMLActions.saveAsHTML(textPane)));
        textPane.getInputMap().put(KeyStroke.getKeyStroke(KeyEvent.VK_B, InputEvent.CTRL_DOWN_MASK), ID_ACTION_BOLD);
        textPane.getActionMap().put(ID_ACTION_BOLD, createToolbarActionListener(new StyledEditorKit.BoldAction()));
        textPane.getInputMap().put(KeyStroke.getKeyStroke(KeyEvent.VK_U, InputEvent.CTRL_DOWN_MASK), ID_ACTION_UNDERLINE);
        textPane.getActionMap().put(ID_ACTION_UNDERLINE, createToolbarActionListener(new StyledEditorKit.UnderlineAction()));
        textPane.getInputMap().put(KeyStroke.getKeyStroke(KeyEvent.VK_I, InputEvent.CTRL_DOWN_MASK), ID_ACTION_ITALIC);
        textPane.getActionMap().put(ID_ACTION_ITALIC, createToolbarActionListener(new StyledEditorKit.ItalicAction()));
        textPane.getInputMap().put(KeyStroke.getKeyStroke(KeyEvent.VK_Z, InputEvent.CTRL_DOWN_MASK), ID_ACTION_UNDO);
        textPane.getActionMap().put(ID_ACTION_UNDO, createToolbarActionListener(e -> {
            if (undoManager.canUndo()) {
                undoManager.undo();
            }
        }));
        textPane.getInputMap().put(KeyStroke.getKeyStroke(KeyEvent.VK_Y, InputEvent.CTRL_DOWN_MASK), ID_ACTION_REDO);
        textPane.getActionMap().put(ID_ACTION_REDO, createToolbarActionListener(e -> {
            if (undoManager.canRedo()) {
                undoManager.redo();
            }
        }));
    }

    private void updateStyleButtons() {
        AttributeSet attrs = textPane.getCharacterAttributes();

        for (Component c : toolbar.getComponents()) {
            if (isAction(c, ID_ACTION_FONT_FAMILY)) {
                ((JComboBox) c).setSelectedItem(String.valueOf(StyleConstants.getFontFamily(attrs)));
            } else if (isAction(c, ID_ACTION_FONT_SIZE)) {
                ((JComboBox) c).setSelectedItem(String.valueOf(StyleConstants.getFontSize(attrs)));
            } else if (isAction(c, ID_ACTION_FONT_COLOR)) {
                ((JButton) c).setIcon(generateColorIcon(StyleConstants.getForeground(attrs)));
            } else if (isAction(c, ID_ACTION_FONT_BACKGROUND)) {
                ((JButton) c).setIcon(generateColorIcon(StyleConstants.getBackground(attrs)));
            } else if (isAction(c, ID_ACTION_UNDO)) {
                ((JButton) c).setEnabled(undoManager.canUndo());
            } else if (isAction(c, ID_ACTION_REDO)) {
                ((JButton) c).setEnabled(undoManager.canRedo());
            } else if (isAction(c, ID_ACTION_BOLD)) {
                ((JToggleButton) c).setSelected(StyleConstants.isBold(attrs));
            } else if (isAction(c, ID_ACTION_UNDERLINE)) {
                ((JToggleButton) c).setSelected(StyleConstants.isUnderline(attrs));
            } else if (isAction(c, ID_ACTION_ITALIC)) {
                ((JToggleButton) c).setSelected(StyleConstants.isItalic(attrs));
            } else if (isAction(c, ID_ACTION_ALIGN_LEFT)) {
                ((JToggleButton) c).setSelected(StyleConstants.getAlignment(attrs) == StyleConstants.ALIGN_LEFT);
            } else if (isAction(c, ID_ACTION_ALIGN_CENTER)) {
                ((JToggleButton) c).setSelected(StyleConstants.getAlignment(attrs) == StyleConstants.ALIGN_CENTER);
            } else if (isAction(c, ID_ACTION_ALIGN_RIGHT)) {
                ((JToggleButton) c).setSelected(StyleConstants.getAlignment(attrs) == StyleConstants.ALIGN_RIGHT);
            } else if (isAction(c, ID_ACTION_ALIGN_JUSTIFIED)) {
                ((JToggleButton) c).setSelected(StyleConstants.getAlignment(attrs) == StyleConstants.ALIGN_JUSTIFIED);
            }
        }
    }

    private boolean isAction(Component c, String actionId) {
        return actionId.equals(c.getName());
    }

    private Action createToolbarActionListener(ActionListener functionalityAction) {
        return new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                SwingUtilities.invokeLater(() -> {
                    e.setSource(textPane);
                    functionalityAction.actionPerformed(e);
                    updateStyleButtons();
                });
            }
        };
    }

    private void addButtonToToolbar(String id, String tooltip, ImageIcon icon, ActionListener action) {
        JButton tButton = new JButton(icon);
        tButton.setName(id);
        tButton.setToolTipText(tooltip);
        tButton.addActionListener(createToolbarActionListener(action));

        toolbar.add(tButton);
    }

    private void addToggleButtonToToolbar(String id, String tooltip, ImageIcon icon, ActionListener action) {
        JToggleButton tButton = new JToggleButton(icon);
        tButton.setName(id);
        tButton.setToolTipText(tooltip);
        tButton.addActionListener(createToolbarActionListener(action));

        toolbar.add(tButton);
    }

}
