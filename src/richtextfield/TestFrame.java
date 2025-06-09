package richtextfield;

import java.awt.Dimension;
import java.awt.EventQueue;
import java.util.logging.Level;
import javax.swing.JFrame;
import javax.swing.UIManager;
import javax.swing.WindowConstants;
import richtextfield.utils.CustomLogger;

public class TestFrame extends JFrame {

    public TestFrame() {
        super();
        this.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        this.setContentPane(new RichTextField());
        this.setPreferredSize(new Dimension(400, 400));
        
        this.pack();
    }

    public static void main(String args[]) {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception ex) {
            CustomLogger.print(TestFrame.class, Level.SEVERE, "", ex);
        }

        EventQueue.invokeLater(() -> new TestFrame().setVisible(true));
    }
}
