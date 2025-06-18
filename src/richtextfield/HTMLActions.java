package richtextfield;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.nio.file.Files;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Base64;
import java.util.List;
import java.util.logging.Level;
import javax.imageio.ImageIO;
import javax.swing.ImageIcon;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.JTextPane;
import javax.swing.SwingUtilities;
import javax.swing.filechooser.FileFilter;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.text.AttributeSet;
import javax.swing.text.Element;
import javax.swing.text.MutableAttributeSet;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyledDocument;
import javax.swing.text.html.HTML;
import javax.swing.text.html.HTMLDocument;
import javax.swing.text.html.HTMLEditorKit;
import richtextfield.utils.CustomLogger;
import richtextfield.utils.ScreenCapture;

public class HTMLActions {

    private static final float INDENT_STEP = 20f;

    private static final JFileChooser FILE_CHOOSER = new JFileChooser() {
        @Override
        public void approveSelection() {
            FileFilter ff = getFileFilter();
            if (ff != null && !ff.accept(getSelectedFile())) {
                JOptionPane.showMessageDialog(
                        this,
                        "El fichero seleccionado no cumple con los filtros seleccionados.",
                        "ERROR",
                        JOptionPane.ERROR_MESSAGE
                );
            } else {
                super.approveSelection();
            }
        }
    };
    public static final FileNameExtensionFilter FILE_FILTER_JPEG = createFileExtensionFilter(
            "Joint Photographic Expert Group",
            "jpg", "jpeg"
    );
    public static final FileNameExtensionFilter FILE_FILTER_PNG = createFileExtensionFilter(
            "Portable Network Graphics",
            "png"
    );
    public static final FileNameExtensionFilter FILE_FILTER_IMAGES = createFileExtensionFilter(
            "Imagen",
            FILE_FILTER_JPEG, FILE_FILTER_PNG
    );
    public static final FileNameExtensionFilter FILE_FILTER_HTML = createFileExtensionFilter(
            "HyperText Markup Language Files",
            "html", "htm"
    );

    static {
        FILE_CHOOSER.setFileSelectionMode(JFileChooser.FILES_ONLY);
        FILE_CHOOSER.setMultiSelectionEnabled(false);
    }

    public static FileNameExtensionFilter createFileExtensionFilter(String name, String... fileExtensions) {
        StringBuilder sb = new StringBuilder(name);
        sb.append(" (");
        concatExtensions(fileExtensions, sb, true);
        sb.append(")");
        return new FileNameExtensionFilter(sb.toString(), fileExtensions);
    }

    public static FileNameExtensionFilter createFileExtensionFilter(String name, FileNameExtensionFilter... fileFilters) {
        StringBuilder sb = new StringBuilder(name);
        sb.append(" (");
        List<String> extensionList = new ArrayList<>();
        if (fileFilters != null && fileFilters.length > 0) {
            boolean first = true;
            for (FileNameExtensionFilter fileFilter : fileFilters) {
                String[] extensions = fileFilter.getExtensions();
                first = concatExtensions(extensions, sb, first);
                if (extensions != null && extensions.length > 0) {
                    extensionList.addAll(Arrays.asList(extensions));
                }
            }
        }
        sb.append(")");
        return new FileNameExtensionFilter(sb.toString(), (String[]) extensionList.toArray(String[]::new));
    }

    private static boolean concatExtensions(String[] extensions, StringBuilder sb, boolean first) {
        if (extensions != null && extensions.length > 0) {
            for (String extension : extensions) {
                sb.append(first ? "*." : ", *.").append(extension);
                first = false;
            }
        }
        return first;
    }

    public static void setAlignment(JTextPane textPane, int alignment) {
        SimpleAttributeSet attrs = new SimpleAttributeSet();
        StyleConstants.setAlignment(attrs, alignment);
        textPane.getStyledDocument().setParagraphAttributes(
                textPane.getSelectionStart(),
                textPane.getSelectionEnd() - textPane.getSelectionStart(),
                attrs,
                false
        );
    }

    public static void toggleStyle(JTextPane textPane, Object styleAttribute) {
        int start = textPane.getSelectionStart();
        int end = textPane.getSelectionEnd();

        if (start == end) {
            return;
        }
        MutableAttributeSet attrs = new SimpleAttributeSet(textPane.getCharacterAttributes());

        if (styleAttribute == StyleConstants.CharacterConstants.Bold) {
            StyleConstants.setBold(attrs, !StyleConstants.isBold(attrs));
        } else if (styleAttribute == StyleConstants.CharacterConstants.Italic) {
            StyleConstants.setItalic(attrs, !StyleConstants.isItalic(attrs));
        } else if (styleAttribute == StyleConstants.CharacterConstants.Underline) {
            StyleConstants.setUnderline(attrs, !StyleConstants.isUnderline(attrs));
        }

        textPane.getStyledDocument().setCharacterAttributes(start, end - start, attrs, false);
    }

    public static void setTextBackground(JTextPane textPane, Color background) {
        int start = textPane.getSelectionStart();
        int end = textPane.getSelectionEnd();

        if (start == end) {
            return;
        }
        MutableAttributeSet attrs = new SimpleAttributeSet(textPane.getCharacterAttributes());
        if (Color.BLACK.equals(background)) {
            StyleConstants.setBackground(attrs, new Color(background.getRed(), background.getGreen(), background.getBlue(), 0));
        } else {
            StyleConstants.setBackground(attrs, background);
        }

        textPane.getStyledDocument().setCharacterAttributes(start, end - start, attrs, true);
    }

    public static void addIndent(JTextPane textPane) {
        int start = textPane.getSelectionStart();
        int end = textPane.getSelectionEnd();
        StyledDocument doc = textPane.getStyledDocument();

        Element paragraph = doc.getParagraphElement(start);
        AttributeSet attr = paragraph.getAttributes();

        float currentIndent = StyleConstants.getLeftIndent(attr);
        SimpleAttributeSet sas = new SimpleAttributeSet();
        StyleConstants.setLeftIndent(sas, currentIndent + INDENT_STEP);
        StyleConstants.setFirstLineIndent(sas, currentIndent + INDENT_STEP);
        doc.setParagraphAttributes(start, end - start, sas, false);
    }

    public static void removeIndent(JTextPane textPane) {
        int start = textPane.getSelectionStart();
        int end = textPane.getSelectionEnd();
        StyledDocument doc = textPane.getStyledDocument();

        Element paragraph = doc.getParagraphElement(start);
        AttributeSet attr = paragraph.getAttributes();

        float currentIndent = StyleConstants.getLeftIndent(attr);
        float newIndent = Math.max(0f, currentIndent - INDENT_STEP);

        SimpleAttributeSet sas = new SimpleAttributeSet();
        StyleConstants.setLeftIndent(sas, newIndent);
        StyleConstants.setFirstLineIndent(sas, newIndent);
        doc.setParagraphAttributes(start, end - start, sas, false);
    }

    public static File getFile(JTextPane textPane, int mode, FileFilter... fileFilters) {
        int result = JFileChooser.CANCEL_OPTION;
        FILE_CHOOSER.resetChoosableFileFilters();
        if (fileFilters != null && fileFilters.length > 0) {
            FILE_CHOOSER.setAcceptAllFileFilterUsed(false);
            for (FileFilter fileFilter : fileFilters) {
                FILE_CHOOSER.addChoosableFileFilter(fileFilter);
            }
        } else {
            FILE_CHOOSER.setAcceptAllFileFilterUsed(true);
        }
        switch (mode) {
            case JFileChooser.OPEN_DIALOG:
                result = FILE_CHOOSER.showOpenDialog(textPane);
                break;
            case JFileChooser.SAVE_DIALOG:
                result = FILE_CHOOSER.showSaveDialog(textPane);
                break;
        }
        if (result == JFileChooser.APPROVE_OPTION) {
            return FILE_CHOOSER.getSelectedFile();
        }
        return null;
    }

    public static void addImage(JTextPane textPane) {
        File file = getFile(textPane, JFileChooser.OPEN_DIALOG, FILE_FILTER_IMAGES,
                FILE_FILTER_JPEG, FILE_FILTER_PNG);
        if (file != null && file.exists()) {
            insertImage(textPane, new ImageIcon(file.getAbsolutePath()), file.getName());
        }
    }

    public static void captureScreenShot(JTextPane textPane) {
        Color selectionColor = Color.RED;
        final ScreenCapture screenCapture = new ScreenCapture(selectionColor);
        Thread thread = new Thread(() -> {
            screenCapture.captureImage();
            if (screenCapture.isImageCaptured()) {
                BufferedImage img = screenCapture.getImage();
                insertImage(textPane, new ImageIcon(img), "ScreenShot_%s.png".formatted(System.currentTimeMillis()));
            }
        });
        thread.start();
    }

    public static void insertImage(JTextPane textPane, ImageIcon icon, String iconName) {
        new Thread(() -> {
            HTMLEditorKit kit = (HTMLEditorKit) textPane.getEditorKit();
            HTMLDocument doc = (HTMLDocument) textPane.getDocument();
            try {
                String base64 = encodeToBase64(icon);
                String imgTag = "<p><img src=\"data:image/png;base64," + base64 + "\" alt=\"" + iconName + "\"></p>";
                kit.insertHTML(doc, textPane.getCaretPosition(), imgTag, 0, 0, HTML.Tag.P);
                SwingUtilities.invokeLater(() -> {
                textPane.revalidate();
                textPane.repaint();
                });
            } catch (Exception e) {
                String errorMsg = "Error al intentar añadir una imagen:\n%s".formatted(iconName);
                CustomLogger.print(HTMLActions.class, Level.SEVERE, errorMsg, e);
                SwingUtilities.invokeLater(() -> JOptionPane.showMessageDialog(
                        textPane,
                        errorMsg,
                        "ERROR",
                        JOptionPane.ERROR_MESSAGE
                ));
        }
        }).start();
    }

    private static String encodeToBase64(ImageIcon icon) throws IOException {
        BufferedImage image = new BufferedImage(
                icon.getIconWidth(), icon.getIconHeight(), BufferedImage.TYPE_INT_ARGB);
        Graphics g = image.createGraphics();
        icon.paintIcon(null, g, 0, 0);
        g.dispose();
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ImageIO.write(image, "png", baos);
        return Base64.getEncoder().encodeToString(baos.toByteArray());
    }

    public static void setAsList(JTextPane textPane, boolean isNumber) {
        try {
            HTMLListText.doList(textPane,
                    isNumber ? HTMLListText.MODE.INSERT_NUMBER : HTMLListText.MODE.INSERT_BULLET);
        } catch (Exception ex) {
            CustomLogger.print(HTMLActions.class, Level.SEVERE, "Error al intentar añadir Bullet/Number List.", ex);
        }
    }

    public static void saveAsHTML(JTextPane textPane) {
        File file = getFile(textPane, JFileChooser.SAVE_DIALOG, FILE_FILTER_HTML);
        if (file != null) {
            new Thread(() -> {
            try {
                HTMLEditorKit kit = (HTMLEditorKit) textPane.getEditorKit();
                StringWriter writer = new StringWriter();
                kit.write(writer, textPane.getDocument(), 0, textPane.getDocument().getLength());
                Files.writeString(file.toPath(), writer.toString(), StandardOpenOption.CREATE);
                    SwingUtilities.invokeLater(() -> JOptionPane.showMessageDialog(
                            textPane,
                            "Documento guardado correctamente en:\n%s".formatted(file)
                    ));
            } catch (Exception e) {
                    String errorMsg = "Error al guardar el documento en:\n%s".formatted(file);
                    CustomLogger.print(HTMLActions.class, Level.SEVERE, errorMsg, e);
                    SwingUtilities.invokeLater(() -> JOptionPane.showMessageDialog(
                            textPane,
                            errorMsg,
                            "ERROR",
                            JOptionPane.ERROR_MESSAGE
                    ));
            }
            }).start();
        }
    }

    public static void loadFromHTML(JTextPane textPane) {
        File file = getFile(textPane, JFileChooser.OPEN_DIALOG, FILE_FILTER_HTML);
        if (file != null && file.exists() && file.isFile()) {
            new Thread(() -> {
            try {
                List<String> stringLines = Files.readAllLines(file.toPath());
                HTMLEditorKit kit = (HTMLEditorKit) textPane.getEditorKit();
                textPane.setText("");
                kit.read(new StringReader(String.join("", stringLines)), textPane.getDocument(), 0);
                    SwingUtilities.invokeLater(() -> {
                        textPane.validate();
                        textPane.repaint();
                    });
            } catch (Exception e) {
                    String errorMsg = "Error al cargar el documento:\n%s".formatted(file);
                    CustomLogger.print(HTMLActions.class, Level.SEVERE, errorMsg, e);
                    SwingUtilities.invokeLater(() -> JOptionPane.showMessageDialog(
                            textPane,
                            errorMsg,
                            "ERROR",
                            JOptionPane.ERROR_MESSAGE
                    ));
            }
            }).start();
        }
    }
}
