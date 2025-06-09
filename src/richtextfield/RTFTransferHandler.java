/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package richtextfield;

import java.awt.Toolkit;
import javax.swing.*;
import javax.swing.text.*;
import javax.swing.text.rtf.RTFEditorKit;
import java.awt.datatransfer.*;
import java.io.*;
import java.util.logging.Level;
import richtextfield.utils.CustomLogger;

public class RTFTransferHandler extends TransferHandler {

    private final DataFlavor rtfFlavor = new DataFlavor("text/rtf;class=java.io.InputStream", "RTF");

    @Override
    protected Transferable createTransferable(JComponent c) {
        if (!(c instanceof JTextPane)) {
            return null;
        }

        try {

            JTextPane textPane = (JTextPane) c;
            int start = textPane.getSelectionStart();
            int end = textPane.getSelectionEnd();
            if (start == end) {
                return null;
            }

            StyledDocument sourceDoc = textPane.getStyledDocument();
            DefaultStyledDocument newDoc = new DefaultStyledDocument();

            String text = sourceDoc.getText(start, end - start);
            newDoc.insertString(0, text, null);

            for (int i = 0; i < text.length(); i++) {
                Element el = sourceDoc.getCharacterElement(start + i);
                AttributeSet as = el.getAttributes();
                newDoc.setCharacterAttributes(i, 1, as, true);
            }
            try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
//                textPane.getEditorKit().write(baos, newDoc, start, end);
                textPane.getEditorKit().write(baos, newDoc, 0, newDoc.getLength());

                byte[] rtfBytes = baos.toByteArray();
                Transferable t = new Transferable() {
                    @Override
                    public DataFlavor[] getTransferDataFlavors() {
                        return new DataFlavor[]{rtfFlavor, DataFlavor.stringFlavor};
                    }

                    @Override
                    public boolean isDataFlavorSupported(DataFlavor df) {
                        return df.equals(rtfFlavor) || df.equals(DataFlavor.stringFlavor);
                    }

                    @Override
                    public Object getTransferData(DataFlavor df) {
                        if (df.equals(rtfFlavor)) {
                            return new ByteArrayInputStream(rtfBytes);
                        } else {
                            return text;
                        }
                    }
                };
                return t;
            }
        } catch (Exception e) {
            CustomLogger.print(this.getClass(), Level.SEVERE, "Error en el copiado", e);
        }
        return null;
    }

    @Override
    public boolean canImport(TransferSupport support) {
        return support.isDataFlavorSupported(rtfFlavor)
                || support.isDataFlavorSupported(DataFlavor.stringFlavor);
    }

    @Override
    public boolean importData(TransferSupport support) {
        if (!canImport(support)) {
            return false;
        }

        try {
            JTextPane target = (JTextPane) support.getComponent();
            int start = target.getSelectionStart();
            int end = target.getSelectionEnd();

            Document mainDoc = target.getDocument();
            if (start != end) {
                mainDoc.remove(start, end - start);
            }
            Transferable transferable = Toolkit.getDefaultToolkit().getSystemClipboard().getContents(null);

            if (transferable.isDataFlavorSupported(rtfFlavor)) {
                // Leer el contenido en un documento temporal
                DefaultStyledDocument tempDoc = new DefaultStyledDocument();
                InputStream rtfStream = (InputStream) transferable.getTransferData(rtfFlavor);
//            InputStream rtfStream = (InputStream) Toolkit.getDefaultToolkit()
//                    .getSystemClipboard().getData(rtfFlavor);
                new RTFEditorKit().read(rtfStream, tempDoc, 0);

                // Leer texto desde documento temporal
                String text = tempDoc.getText(0, tempDoc.getLength());

                // Eliminar salto de línea final si no se desea
                if (text.endsWith("\n")) {
                    text = text.substring(0, text.length() - 1);
                }

                // Insertar con estilos
                mainDoc.insertString(start, text, null);
                for (int i = 0; i < text.length(); i++) {
                    Element el = tempDoc.getCharacterElement(i);
                    AttributeSet attr = el.getAttributes();
                    ((StyledDocument) mainDoc).setCharacterAttributes(start + i, 1, attr, true);
                }

                return true;
            } else if (transferable.isDataFlavorSupported(DataFlavor.stringFlavor)) {
                String plainText = (String) transferable.getTransferData(DataFlavor.stringFlavor);
                mainDoc.insertString(start, plainText, null);
                return true;
            }

        } catch (Exception e) {
            CustomLogger.print(this.getClass(), Level.SEVERE, "Error en el pegado", e);
        }

        return false;
    }

    @Override
    public int getSourceActions(JComponent c) {
        return COPY_OR_MOVE;
    }
}
