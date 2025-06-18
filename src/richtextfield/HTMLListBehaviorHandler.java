package richtextfield;

import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.util.logging.Level;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.swing.JOptionPane;
import javax.swing.JTextPane;
import javax.swing.SwingUtilities;
import javax.swing.text.BadLocationException;
import javax.swing.text.Element;
import javax.swing.text.html.HTML;
import javax.swing.text.html.HTMLDocument;
import javax.swing.text.html.HTMLEditorKit;
import richtextfield.utils.CustomLogger;

public class HTMLListBehaviorHandler {

    private final JTextPane textPane;

    public HTMLListBehaviorHandler(JTextPane textPane) {
        this.textPane = textPane;
        setupListeners();
    }

    private void setupListeners() {
        textPane.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                try {
                    if (e.getKeyCode() == KeyEvent.VK_ENTER) {
                        handleEnter(e);
                    } else if (e.getKeyCode() == KeyEvent.VK_BACK_SPACE) {
                        handleBackspace(e);
                    }
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
        });
    }

    private void handleEnter(KeyEvent e) throws Exception {
        HTMLDocument doc = (HTMLDocument) textPane.getDocument();
        int pos = textPane.getCaretPosition();

        Element elem = doc.getCharacterElement(pos);
        Element parent = elem.getParentElement();

        if (parent.getName().equalsIgnoreCase("li")) {
            String text = doc.getText(parent.getStartOffset(), parent.getEndOffset() - parent.getStartOffset()).trim();
            if (text.isEmpty()) {
                doc.remove(parent.getStartOffset(), parent.getEndOffset() - parent.getStartOffset());
                e.consume();
                return;
            }

            HTMLEditorKit kit = (HTMLEditorKit) textPane.getEditorKit();
            kit.insertHTML(doc, parent.getEndOffset(), "<li></li>", 0, 0, HTML.Tag.LI);
            textPane.setCaretPosition(parent.getEndOffset() + 5);
            e.consume();
        } else {
            String lineText = getCurrentLineText();
            if (lineText.matches("^\\s*\\u2022\\s+.*")) {
                insertListFromTextPane(false);
                e.consume();
            } else if (lineText.matches("^\\s*\\d+\\.\\s+.*")) {
                insertListFromTextPane(true);
                e.consume();
            }
        }
    }

    private void handleBackspace(KeyEvent e) throws Exception {
        HTMLDocument doc = (HTMLDocument) textPane.getDocument();
        int pos = textPane.getCaretPosition();

        Element elem = doc.getCharacterElement(pos);
        Element parent = elem.getParentElement();

        if (parent.getName().equalsIgnoreCase("li")) {
            if (pos == parent.getStartOffset()) {
                doc.remove(parent.getStartOffset(), parent.getEndOffset() - parent.getStartOffset());
                e.consume();
            }
        }
    }

    private String getCurrentLineText() throws BadLocationException {
        int caretPos = textPane.getCaretPosition();
        Element root = textPane.getDocument().getDefaultRootElement();
        int line = root.getElementIndex(caretPos);
        Element lineElem = root.getElement(line);
        int start = lineElem.getStartOffset();
        int end = lineElem.getEndOffset();
        return textPane.getDocument().getText(start, end - start).trim();
    }

    public void insertListFromTextPane(boolean ordered) {

        try {
            HTMLEditorKit kit = (HTMLEditorKit) textPane.getEditorKit();
            HTMLDocument doc = (HTMLDocument) textPane.getDocument();

            int start = textPane.getSelectionStart();
            int end = textPane.getSelectionEnd();
            if (start == end) {
                return;
            }

            String selectedText = textPane.getSelectedText();
            if (selectedText == null || selectedText.trim().isEmpty()) {
                return;
            }

            String[] lines = selectedText.split("\\r?\\n");

            StringBuilder html = new StringBuilder();
            html.append(ordered ? "<ol>" : "<ul>");

            Pattern bulletPattern = Pattern.compile("^\\s*(\\u2022|\\d+\\.)\\s+(.*)");

            for (String line : lines) {
                Matcher matcher = bulletPattern.matcher(line);
                if (matcher.matches()) {
                    html.append("<li>").append(matcher.group(2).trim()).append("</li>");
                } else {
                    html.append("<li>").append(line.trim()).append("</li>");
                }
            }

            html.append(ordered ? "</ol>" : "</ul>");

            doc.remove(start, end - start);
            kit.insertHTML(doc, start, html.toString(), 0, 0, ordered ? HTML.Tag.OL : HTML.Tag.UL);

        } catch (Exception ex) {
            String errorMsg = "Error al crear el listado.";
            CustomLogger.print(HTMLActions.class, Level.SEVERE, errorMsg, ex);
            SwingUtilities.invokeLater(() -> JOptionPane.showMessageDialog(
                    textPane,
                    errorMsg,
                    "ERROR",
                    JOptionPane.ERROR_MESSAGE
            ));
        }
    }
}
