package richtextfield.images;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import javax.swing.ImageIcon;

public class ImageLoader {

    public static final String PATH_ICON_OPEN = "/richtextfield/images/icons8-open-file-16.png";
    public static final String PATH_ICON_SAVE = "/richtextfield/images/icons8-save-16.png";
    public static final String PATH_ICON_UNDO = "/richtextfield/images/icons8-undo-16.png";
    public static final String PATH_ICON_REDO = "/richtextfield/images/icons8-redo-16.png";
    public static final String PATH_ICON_CUT = "/richtextfield/images/icons8-cut-16.png";
    public static final String PATH_ICON_COPY = "/richtextfield/images/icons8-copy-16.png";
    public static final String PATH_ICON_PASTE = "/richtextfield/images/icons8-paste-16.png";
    public static final String PATH_ICON_BOLD = "/richtextfield/images/icons8-bold-16.png";
    public static final String PATH_ICON_UNDERLINE = "/richtextfield/images/icons8-underline-16.png";
    public static final String PATH_ICON_ITALIC = "/richtextfield/images/icons8-italic-16.png";
    public static final String PATH_ICON_ADD_INDENT = "/richtextfield/images/icons8-indent-16.png";
    public static final String PATH_ICON_REMOVE_INDENT = "/richtextfield/images/icons8-outdent-16.png";
    public static final String PATH_ICON_LIST = "/richtextfield/images/icons8-list-16.png";
    public static final String PATH_ICON_NUMERED_LIST = "/richtextfield/images/icons8-numbered-list-16.png";    
    public static final String PATH_ICON_ALIGN_LEFT = "/richtextfield/images/icons8-align-left-16.png";
    public static final String PATH_ICON_ALIGN_CENTER = "/richtextfield/images/icons8-align-center-16.png";
    public static final String PATH_ICON_ALIGN_RIGHT = "/richtextfield/images/icons8-align-right-16.png";
    public static final String PATH_ICON_ALIGN_JUSTIFIED = "/richtextfield/images/icons8-align-justify-16.png";
    public static final String PATH_ICON_ADD_PICTURE = "/richtextfield/images/icons8-picture-16.png";
    
    public static ImageIcon get(String imagePath){
        return new ImageIcon(ImageLoader.class.getResource(imagePath));
    }
    
    public static ImageIcon generateRectangleColorIcon( int width, int height, Color color){
        BufferedImage bi = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = (Graphics2D)bi.getGraphics();
        g.setColor(color);
        g.fillRect(0, 0, width, height);
        g.dispose();
        return new ImageIcon(bi);
    }
}
