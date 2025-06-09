package richtextfield;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.logging.Level;
import javax.swing.ImageIcon;
import javax.swing.JFileChooser;
import javax.swing.JTextPane;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.Element;
import javax.swing.text.MutableAttributeSet;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyledDocument;
import javax.swing.text.html.HTMLDocument;
import javax.swing.text.html.HTMLEditorKit;
import javax.swing.text.rtf.RTFEditorKit;
import richtextfield.utils.CustomLogger;

public class RTFActions {

    private static final float INDENT_STEP = 20f;

    private static final JFileChooser FILE_CHOOSER = new JFileChooser();

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

    public static File getFile(JTextPane textPane, int mode) {
        int result = JFileChooser.CANCEL_OPTION;
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
        File file = getFile(textPane, JFileChooser.OPEN_DIALOG);
//        if (file != null && file.exists()) {
//            ImageIcon icon = new ImageIcon(file.getAbsolutePath());
//            textPane.setCaretPosition(textPane.getCaretPosition());
//            textPane.insertIcon(icon);
//        }
        
        if (file != null && file.exists()) {
            ImageIcon icon = new ImageIcon(file.getAbsolutePath());
            if (icon.getIconWidth() <= 0) {
                CustomLogger.print(RTFActions.class, Level.SEVERE, "Imagen no válida o no cargada.n", null);
                return;
            }

            SimpleAttributeSet attrs = new SimpleAttributeSet();
            StyleConstants.setIcon(attrs, icon);

            try {
                StyledDocument doc = textPane.getStyledDocument();
                int pos = textPane.getCaretPosition();
                doc.insertString(pos, " ", attrs); // espacio necesario
            } catch (BadLocationException e) {
                CustomLogger.print(RTFActions.class, Level.SEVERE, "Error al añadir la imagen.", e);
            }
        }
    }

    public static void saveAsRTF(JTextPane textPane, File file) {
        RTFEditorKit rtfKit = new RTFEditorKit();
        try (FileOutputStream fos = new FileOutputStream(file)) {
            rtfKit.write(fos, textPane.getDocument(), 0, textPane.getDocument().getLength());
        }catch(Exception e){
            CustomLogger.print(RTFActions.class, Level.SEVERE, "Error al guardar fichero RTF.", e);
        }
    }

    public static void loadFromRTF(JTextPane textPane, File file){
        RTFEditorKit rtfKit = new RTFEditorKit();
        StyledDocument doc = (StyledDocument) rtfKit.createDefaultDocument();
        try (FileInputStream fis = new FileInputStream(file)) {
            rtfKit.read(fis, doc, 0);
            textPane.setDocument(doc);
        }catch(Exception e){
            CustomLogger.print(RTFActions.class, Level.SEVERE, "Error al cargar fichero RTF.", e);
        }
    }

    public static void saveAsHTML(JTextPane textPane, File file){
        HTMLEditorKit htmlKit = new HTMLEditorKit();
        try (FileWriter writer = new FileWriter(file)) {
            htmlKit.write(writer, textPane.getDocument(), 0, textPane.getDocument().getLength());
        }catch(Exception e){
            CustomLogger.print(RTFActions.class, Level.SEVERE, "Error al guardar fichero HTML.", e);
        }
    }

    public static void loadFromHTML(JTextPane textPane, File file) {
        HTMLEditorKit htmlKit = new HTMLEditorKit();
        HTMLDocument htmlDoc = (HTMLDocument) htmlKit.createDefaultDocument();
        try (FileReader reader = new FileReader(file)) {
            htmlKit.read(reader, htmlDoc, 0);
            textPane.setDocument(htmlDoc);
        }catch(Exception e){
            CustomLogger.print(RTFActions.class, Level.SEVERE, "Error al guardar fichero HTML.", e);
        }
    }
}
