package richtextfield;

import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.util.logging.Level;
import javax.swing.JTextPane;
import javax.swing.SwingUtilities;
import javax.swing.event.CaretEvent;
import javax.swing.event.CaretListener;
import javax.swing.text.AbstractDocument;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.Element;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyledDocument;
import richtextfield.utils.CustomLogger;

public class HTMLListText {

    private static final char BULLET_CHAR = '\u2022';
    private static final String BULLET_TEXT = new String(new char[]{BULLET_CHAR, ' '});
    private static final String NUMBERS_ATTR = "NUMBERS";
    private static final String NUMBER_TEXT = "%s. ";

    public enum MODE {
        INSERT_BULLET,
        INSERT_NUMBER,
        REMOVE_BULLET,
        REMOVE_NUMBER,
    }

    public static void doList(JTextPane textPane, MODE mode) throws BadLocationException {
        StyledDocument doc = textPane.getStyledDocument();
        String selectedText = textPane.getSelectedText();
        int caretPosition = textPane.getCaretPosition();
        int selectionEnd = caretPosition;
        if (selectedText != null && !selectedText.trim().isEmpty()) {
            caretPosition = textPane.getSelectionStart();
            selectionEnd = textPane.getSelectionEnd();
        }

        Element paraEle = doc.getParagraphElement(caretPosition);
        int paraEleStart = paraEle.getStartOffset();
        int paraEleEnd;
        boolean firstPara = true;
        int n = 0;
        NUMBERS_PARA_LOOP:
        do {
            paraEle = doc.getParagraphElement(paraEleStart);
            paraEleEnd = paraEle.getEndOffset();

            if ((paraEleEnd - paraEleStart) <= 1) { // empty line

                if (firstPara) {
                    firstPara = false;
                    n = 0;
                }

                paraEleStart = paraEleEnd;
                continue NUMBERS_PARA_LOOP;
            }

            switch (mode) {
                case INSERT_BULLET:
                    if ((!isBulletedPara(textPane, paraEleStart))
                            && (!isNumberedPara(textPane, paraEleStart))) {
                        insertListText(textPane, paraEleStart, paraEleStart, null);
                    }
                    break; // switch
                case REMOVE_BULLET:
                    if (isBulletedPara(textPane, paraEleStart)) {

                        doc.remove(paraEleStart, BULLET_TEXT.length());
                    }
                    break; // switch
                case INSERT_NUMBER:
                    if (isBulletedPara(textPane, paraEleStart)) {
                        break; // switch
                    }

                    if (firstPara) {
                        firstPara = false;
                        n = 0;
                    }

                    if (isNumberedPara(textPane, paraEleStart)) {

                        // remove any existing number
                        doc.remove(paraEleStart, getNumberLength(textPane, paraEleStart));
                    }

                    if (!isNumberedPara(textPane, paraEleStart)) {
                        insertListText(textPane, paraEleStart, paraEleStart, ++n);
                    }

                    break; // switch
                case REMOVE_NUMBER:
                    if (isNumberedPara(textPane, paraEleStart)) {
                        doc.remove(paraEleStart, getNumberLength(textPane, paraEleStart));
                    }
                    break; // switch
            }
            // Get the updated para element details after numbering
            paraEle = doc.getParagraphElement(paraEleStart);
            paraEleEnd = paraEle.getEndOffset();

            paraEleStart = paraEleEnd;

        } while (paraEleEnd <= selectionEnd);
        // NUMBERS_PARA_LOOP

        textPane.requestFocusInWindow();
    }

    private static void insertListText(JTextPane textPane, int insertPos, int attributesPos, Integer number)
            throws BadLocationException {
        String listText = BULLET_TEXT;
        AttributeSet attrs1 = getParaStartAttributes(textPane, attributesPos);
        if (number != null) {
            listText = getNumberString(number);
            SimpleAttributeSet attrs2 = new SimpleAttributeSet(attrs1);
            attrs2.addAttribute(NUMBERS_ATTR, number);
            attrs1 = attrs2;
        }
        StyledDocument doc = textPane.getStyledDocument();
        doc.insertString(insertPos, listText, attrs1);
    }

    private static char getParaFirstCharacter(JTextPane textPane, int paraEleStart) throws BadLocationException {
        return textPane.getText(paraEleStart, 1).charAt(0);
    }

    private static boolean isBulletedPara(JTextPane textPane, int paraEleStart) throws BadLocationException {
        return getParaFirstCharacter(textPane, paraEleStart) == BULLET_CHAR;
    }

    private static boolean isFirstCharNumber(JTextPane textPane, int paraEleStart) throws BadLocationException {
        return Character.isDigit(getParaFirstCharacter(textPane, paraEleStart));
    }

    private static AttributeSet getParaStartAttributes(JTextPane textPane, int pos) {

        StyledDocument doc = textPane.getStyledDocument();
        Element charEle = doc.getCharacterElement(pos);
        return charEle.getAttributes();
    }

    private static AttributeSet getNumbersAttributes(AttributeSet attrs1, Integer number) {
        SimpleAttributeSet attrs2 = new SimpleAttributeSet(attrs1);
        attrs2.addAttribute(NUMBERS_ATTR, number);
        return attrs2;
    }

    private static boolean isNumberedPara(JTextPane textPane, int paraEleStart) throws BadLocationException {

        AttributeSet attrSet = getParaStartAttributes(textPane, paraEleStart);
        Integer paraNum = (Integer) attrSet.getAttribute(NUMBERS_ATTR);

        return !((paraNum == null) || (!isFirstCharNumber(textPane, paraEleStart)));
    }

    private static String getNumberString(Integer number) {
        return String.format(NUMBER_TEXT, number);
    }

    /*
	 * Returns the numbered para's number length. This length includes
	 * the number + dot + space. For example, the text "12. A Numbered para..."
	 * has the number length of 4.
     */
    private static int getNumberLength(JTextPane textPane, int paraEleStart) {
        return getNumberString(getParaNumber(textPane, paraEleStart)).length();
    }

    private static Integer getParaNumber(JTextPane textPane, int paraEleStart) {
        AttributeSet attrSet = getParaStartAttributes(textPane, paraEleStart);
        Integer paraNum = (Integer) attrSet.getAttribute(NUMBERS_ATTR);
        return paraNum;
    }

    public static class ListParaKeyListener implements KeyListener, CaretListener {

        private final JTextPane textPane;
        // These two variables are derived in the keyPressed and are used in
        // keyReleased method.
        private String prevParaText_;
        private int prevParaEleStart_;

        // Identifies if a key is pressed in a bulleted para. 
        // This is required to distinguish from the numbered para.
        private boolean bulletedPara_;

        // Identifies if a key is pressed in a numbered para.
        // This is required to distinguish from the bulleted para.
        private boolean numberedPara_;

        // This flag checks true if the caret position within a bulleted para
        // is at the first text position after the bullet (bullet char + space).
        // Also see EditorCaretListener and BulletParaKeyListener.
        private boolean startPosPlusBullet__;

        // This flag checks true if the caret position within a numbered para
        // is at the first text position after the number (number + dot + space).
        // Alse see EditorCaretListener and NumbersParaKeyListener.		
        private boolean startPosPlusNum__;

        public ListParaKeyListener(JTextPane textPane) {
            this.textPane = textPane;
        }

        @Override
        public void keyTyped(KeyEvent e) {

        }

        @Override
        public void keyPressed(KeyEvent e) {
            try {
                String selectedText = textPane.getSelectedText();

                if ((selectedText == null) || (selectedText.trim().isEmpty())) {
                    // continue, processing key press without any selected text
                } else {
                    // text is selected within numbered para and a key is pressed
                    doReplaceSelectionRoutine();
                    return;
                }

                numberedPara_ = false;
                bulletedPara_ = false;
                int pos = textPane.getCaretPosition();

                boolean isNumbered = isNumberedParaForPos(pos);
                boolean isBulleted = isBulletedParaForPos(pos);

                if (!isNumbered && !isBulleted) {
                    return;
                }

                Element paraEle = textPane.getStyledDocument().getParagraphElement(pos);
                int paraEleStart = paraEle.getStartOffset();

                switch (e.getKeyCode()) {

                    case KeyEvent.VK_LEFT: // same as that of VK_KP_LEFT
                    case KeyEvent.VK_KP_LEFT:
                        if (isNumbered) {
                            int newPos = pos - (getNumberLength(textPane, paraEleStart) + 1);
                            doLeftArrowKeyRoutine(newPos, startPosPlusNum__);
                        } else if (isBulleted) {
                            int newPos = pos - (BULLET_TEXT.length() + 1);
                            doLeftArrowKeyRoutine(newPos, startPosPlusBullet__);
                        }
                        break;
                    case KeyEvent.VK_DELETE:
                        doDeleteKeyRoutine(paraEle, pos);
                        break;
                    case KeyEvent.VK_BACK_SPACE:
                        doBackspaceKeyRoutine(paraEle);
                        break;
                    case KeyEvent.VK_ENTER:
                        getPrevParaDetails(pos);
                        break;
                }
            } catch (Exception ex) {
                CustomLogger.print(HTMLListText.class, Level.SEVERE, "Error al interactuar con un Bullet/Number List.", ex);
            }
        }

        @Override
        public void keyReleased(KeyEvent e) {
            try {
                if (!numberedPara_ && !bulletedPara_) {
                    return;
                }

                switch (e.getKeyCode()) {
                    case KeyEvent.VK_ENTER:
                        doEnterKeyRoutine();
                        break;
                }
            } catch (Exception ex) {
                CustomLogger.print(HTMLListText.class, Level.SEVERE, "Error al interactuar con un Bullet/Number List.", ex);
            }
        }

        @Override
        public void caretUpdate(CaretEvent e) {
            try {
                startPosPlusBullet__ = false;
                startPosPlusNum__ = false;
                Element paraEle = textPane.getStyledDocument().getParagraphElement(textPane.getCaretPosition());
                int paraEleStart = paraEle.getStartOffset();

                if (isBulletedPara(textPane, paraEleStart)) {
                    int bulletLength = BULLET_TEXT.length();
                    if (e.getDot() == (paraEleStart + bulletLength)) {

                        startPosPlusBullet__ = true;
                    } else if (e.getDot() < (paraEleStart + bulletLength)) {

                        textPane.setCaretPosition(paraEleStart + bulletLength);
                    } else {
                        // continue
                    }
                } else if (isNumberedPara(textPane, paraEleStart)) {

                    int numLen = getNumberLength(textPane, paraEleStart);

                    if (e.getDot() < (paraEleStart + numLen)) {

                        textPane.setCaretPosition(paraEleStart + numLen);
                    } else if (e.getDot() == (paraEleStart + numLen)) {

                        startPosPlusNum__ = true;
                    } else {
                        // continue
                    }
                } else {
                    // not a bulleted or numbered para
                }
            } catch (Exception ex) {
                CustomLogger.print(HTMLListText.class, Level.SEVERE, "Error al interactuar con un Bullet/Number List.", ex);
            }
        }

        /*
		 * Routine for processing selected text with numbered paras
		 * after pressing Enter, Backspace or Delete keys, and the
		 * paste insert replacing the selected text.
         */
        private void doReplaceSelectionRoutine() throws BadLocationException {

            // Get selection start and end para details.
            // Check if there are numbered paras at top and bottom
            // of the selection. Re-number if needed i.e., when selection
            // is replaced in the middle of numbered paras or at the top
            // items of the numbered paras.
            StyledDocument doc = textPane.getStyledDocument();;
            Element topParaEle = doc.getParagraphElement(textPane.getSelectionStart());
            Element bottomParaEle = doc.getParagraphElement(textPane.getSelectionEnd());

            int bottomParaEleStart = bottomParaEle.getStartOffset();
            int bottomParaEleEnd = bottomParaEle.getEndOffset();

            // No numbered text at bottom, no processing required -or-
            // no next para after selection end (end of document text).
            if ((!isNumberedPara(textPane, bottomParaEleStart))
                    || (bottomParaEleEnd > doc.getLength())) {
                return;
            }

            // Check if para following the selection end is numbered or not.
            Element paraEle = doc.getParagraphElement(bottomParaEleEnd + 1);
            int paraEleStart = paraEle.getStartOffset();

            if (!isNumberedPara(textPane, paraEleStart)) {
                return;
            }

            // Process re-numbering
            Integer numTop = getParaNumber(textPane, topParaEle.getStartOffset());

            if (numTop != null) {

                // There are numbered items above the removed para, and
                // there are numbered items following the removed para;
                // bottom numbers start from numTop + 1.
                doNewNumbers(paraEleStart, numTop);
            } else {
                // numTop == null
                // There are no numbered items above the removed para, and
                // there are numbered items following the removed para;
                // bottom numbers start from 1.
                doNewNumbers(paraEleStart, 0);
            }

        } // doReplaceSelectionRoutine()

        /*
		 * Common routine to arrive at new numbers and replace the previous
		 * ones after the following actions within numbered para list:
		 * - Enter, Delete, Backspace key press.
		 * - Delete, Backspace and paste-insert selected text.
         */
        private void doNewNumbers(int nextParaEleStart, Integer newNum) throws BadLocationException {

            StyledDocument doc = textPane.getStyledDocument();
            Element nextParaEle = doc.getParagraphElement(nextParaEleStart);
            boolean nextParaIsNumbered = true;

            NUMBERED_PARA_LOOP:
            while (nextParaIsNumbered) {

                Integer oldNum = getParaNumber(textPane, nextParaEleStart);
                newNum++;
                replaceNumbers(nextParaEleStart, oldNum, newNum);

                nextParaIsNumbered = false;

                // Get following para details after number is replaced for a para
                int nextParaEleEnd = nextParaEle.getEndOffset();
                int nextParaPos = nextParaEleEnd + 1;

                if (nextParaPos > doc.getLength()) {
                    break NUMBERED_PARA_LOOP; // no next para, end of document text
                }

                nextParaEle = doc.getParagraphElement(nextParaPos);
                nextParaEleStart = nextParaEle.getStartOffset();
                nextParaIsNumbered = isNumberedPara(textPane, nextParaEleStart);
            }
            // NUMBERED_PARA_LOOP

        } // doNewNumbers()

        private void replaceNumbers(int nextParaEleStart, Integer prevNum, Integer newNum) throws BadLocationException {
            ((AbstractDocument) textPane.getStyledDocument()).replace(
                    nextParaEleStart,
                    getNumberString(prevNum).length(),
                    getNumberString(newNum),
                    getNumbersAttributes(getParaStartAttributes(textPane, nextParaEleStart), newNum));
        }

        private boolean isNumberedParaForPos(int caretPos) throws BadLocationException {
            Element paraEle = textPane.getStyledDocument().getParagraphElement(caretPos);
            return isNumberedPara(textPane, paraEle.getStartOffset());
        }

        private boolean isBulletedParaForPos(int caretPos) throws BadLocationException {
            Element paraEle = textPane.getStyledDocument().getParagraphElement(caretPos);
            return isBulletedPara(textPane, paraEle.getStartOffset());
        }

        /*
	 * Left arrow key press routine within a bulleted and numbered paras.
	 * Moves the cursor when caret is at position startPosPlusBullet__ or at
	 * startPosPlusNum__ for bullets or numbers respectively.
	 * Also see EditorCaretListener.
	 *
	 * The parameter startTextPos indicates if startPosPlusBullet__ or
	 * startPosPlusNum__. pos is the present caret postion.
         */
        private void doLeftArrowKeyRoutine(int pos, boolean startTextPos) {
            if (!startTextPos) {
                return;
            }

            // Check if this is start of document
            Element paraEle = textPane.getStyledDocument().getParagraphElement(textPane.getCaretPosition());
            int newPos = (paraEle.getStartOffset() == 0) ? 0 : pos;

            // Position the caret in an EDT, otherwise the caret is
            // positioned at one less position than intended.
            SwingUtilities.invokeLater(() -> textPane.setCaretPosition(newPos));
        }

        private void doDeleteKeyRoutine(Element paraEle, int pos) throws BadLocationException {
            int paraEleEnd = paraEle.getEndOffset();
            StyledDocument doc = textPane.getStyledDocument();
            if (paraEleEnd > doc.getLength()) {
                return; // no next para, end of document text
            }

            if (pos == (paraEleEnd - 1)) { // last char of para; -1 is for CR

                Element nextParaEle = doc.getParagraphElement(paraEleEnd + 1);
                int nextParaEleStart = nextParaEle.getStartOffset();

                if (isNumberedParaForPos(paraEleEnd + 1)) {
                    doc.remove(pos, getNumberLength(textPane, nextParaEleStart));
                    doReNumberingForDeleteKey(paraEleEnd + 1);
                } else if (isBulletedParaForPos(paraEleEnd + 1)) {
                    // following para is bulleted, remove
                    doc.remove(pos, BULLET_TEXT.length());
                }
                // else, not a numbered or bulledted para
                // delete happens normally (one char)
            }
        }

        private void doReNumberingForDeleteKey(int delParaPos) throws BadLocationException {
            // Get para element details where delete key is pressed
            StyledDocument doc = textPane.getStyledDocument();
            Element paraEle = doc.getParagraphElement(delParaPos);
            int paraEleStart = paraEle.getStartOffset();
            int paraEleEnd = paraEle.getEndOffset();

            // Get bottom para element details
            Element bottomParaEle = doc.getParagraphElement(paraEleEnd + 1);
            int bottomParaEleStart = bottomParaEle.getStartOffset();

            // In case bottom para is not numbered or end of document,
            // no re-numbering is required.
            if ((paraEleEnd > doc.getLength())
                    || (!isNumberedPara(textPane, bottomParaEleStart))) {
                return;
            }

            Integer n = getParaNumber(textPane, paraEleStart);
            doNewNumbers(bottomParaEleStart, n);
        }

        // Backspace key press routine within a numbered para.
        // Also, see EditorCaretListener.
        private void doBackspaceKeyRoutine(Element paraEle) throws BadLocationException {
            StyledDocument doc = textPane.getStyledDocument();
            // In case the position of cursor at the backspace is just after
            // the number: remove the number and re-number the following ones.
            if (startPosPlusNum__) {

                int startOffset = paraEle.getStartOffset();
                doc.remove(startOffset, getNumberLength(textPane, startOffset));
                doReNumberingForBackspaceKey(paraEle, startOffset);
                startPosPlusNum__ = false;
            }

            // In case the position of cursor at the backspace is just 
            // before the bullet (that is BULLET_LENGTH).
            if (startPosPlusBullet__) {

                doc.remove(paraEle.getStartOffset(), BULLET_TEXT.length());
                startPosPlusBullet__ = false;
            }
        }

        private void doReNumberingForBackspaceKey(Element paraEle, int paraEleStart) throws BadLocationException {

            // Get bottom para element and check if numbered.
            StyledDocument doc = textPane.getStyledDocument();
            Element bottomParaEle = doc.getParagraphElement(paraEle.getEndOffset() + 1);
            int bottomParaEleStart = bottomParaEle.getStartOffset();

            if (!isNumberedPara(textPane, bottomParaEleStart)) {
                return; // there are no numbers following this para, and
                // no re-numbering required.
            }

            // Get top para element and number
            Integer numTop = null;

            if (paraEleStart == 0) {

                // beginning of document, no top para exists
                // before the document start; numTop = null
            } else {
                Element topParaEle = doc.getParagraphElement(paraEleStart - 1);
                numTop = getParaNumber(textPane, topParaEle.getStartOffset());
            }

            if (numTop == null) {

                // There are no numbered items above the removed para, and
                // there are numbered items following the removed para;
                // bottom numbers start from 1.
                doNewNumbers(bottomParaEleStart, 0);
            } else {
                // numTop != null
                // There are numbered items above the removed para, and
                // there are numbered items following the removed para;
                // bottom numbers start from numTop + 1.
                doNewNumbers(bottomParaEleStart, numTop);
            }
        }

        // This method is used with Enter key press routine.
        // Two instance variable values are derived here and are used
        // in the keyReleased() method: prevParaEleStart_ and prevParaText_
        private void getPrevParaDetails(int pos) throws BadLocationException {

            pos = pos - 1;
            boolean doThis = false;
            if (isBulletedParaForPos(pos)) {
                bulletedPara_ = true;
                doThis = true;
            }
            if (isNumberedParaForPos(pos)) {
                numberedPara_ = true;
                doThis = true;
            }
            if (doThis) {
                Element paraEle = textPane.getStyledDocument().getParagraphElement(pos);
                prevParaEleStart_ = paraEle.getStartOffset();
                prevParaText_ = getPrevParaText(prevParaEleStart_, paraEle.getEndOffset());
            }
        }

        private String getPrevParaText(int prevParaEleStart, int prevParaEleEnd) throws BadLocationException {
            return textPane.getStyledDocument().getText(prevParaEleStart, (prevParaEleEnd - prevParaEleStart));
        }

        // Enter key press routine within a numbered para.
        // Also, see keyPressed().
        private void doEnterKeyRoutine() throws BadLocationException {

            StyledDocument doc = textPane.getStyledDocument();
            String prevParaText = prevParaText_;
            int prevParaEleStart = prevParaEleStart_;
            if (numberedPara_) {
                int len = getNumberLength(textPane, prevParaEleStart) + 1; // +1 for CR

                // Check if prev para with numbers has text					
                if (prevParaText.length() == len) {
                    // Para has numbers and no text, remove number from para
                    doc.remove(prevParaEleStart, len);
                    textPane.setCaretPosition(prevParaEleStart);
                    return;
                }
                // Prev para with number and text,			
                // insert number for new para (current position)
                Integer num = getParaNumber(textPane, prevParaEleStart);
                num++;
                insertListText(textPane, textPane.getCaretPosition(), prevParaEleStart, num);

                // After insert, check for numbered paras following the newly
                // inserted numberd para; and re-number those paras.
                // Get newly inserted number para details
                Element newParaEle = doc.getParagraphElement(textPane.getCaretPosition());
                int newParaEleEnd = newParaEle.getEndOffset();

                if (newParaEleEnd > doc.getLength()) {
                    return; // no next para, end of document text
                }

                // Get next para (following the newly inserted one) and
                // re-number para only if already numered.
                Element nextParaEle = doc.getParagraphElement(newParaEleEnd + 1);
                int nextParaEleStart = nextParaEle.getStartOffset();

                if (isNumberedPara(textPane, nextParaEleStart)) {
                    doNewNumbers(nextParaEleStart, num);
                }
            } else if (bulletedPara_) {
                // Check if prev para with bullet has text					
                if (prevParaText.length() < 4) {
                    // Para has bullet and no text, remove bullet+CR from para
                    doc.remove(prevParaEleStart, (BULLET_TEXT.length() + 1));
                    textPane.setCaretPosition(prevParaEleStart);
                    return;
                }
                // Prev para with bullet and text

                // Insert bullet for next para (current position), and
                // prev para attributes are used for this bullet.	
                insertListText(textPane, textPane.getCaretPosition(), prevParaEleStart, null);
            }

        } // doEnterKeyRoutine()

    }

}
