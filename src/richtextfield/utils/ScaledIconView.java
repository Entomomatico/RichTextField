
package richtextfield.utils;

import java.awt.Container;
import java.awt.Graphics;
import java.awt.Rectangle;
import java.awt.Shape;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.text.Element;
import javax.swing.text.IconView;
import javax.swing.text.StyleConstants;
import javax.swing.text.View;

public class ScaledIconView extends IconView {
    private final Icon icon;
    private int scaledWidth;
    private int scaledHeight;

    public ScaledIconView(Element elem) {
        super(elem);
        icon = (Icon) elem.getAttributes().getAttribute(StyleConstants.IconAttribute);
        scaledWidth = icon != null ? icon.getIconWidth() : 0;
        scaledHeight = icon != null ? icon.getIconHeight() : 0;
    }

    @Override
    public float getPreferredSpan(int axis) {
        if (icon == null) return 0;

        Container container = getContainer();
        int containerWidth = container != null ? container.getWidth() : Integer.MAX_VALUE;
        int imgWidth = icon.getIconWidth();
        int imgHeight = icon.getIconHeight();

        if (imgWidth > containerWidth) {
            float scale = (float) containerWidth / imgWidth;
            scaledWidth = containerWidth;
            scaledHeight = (int) (imgHeight * scale);
        } else {
            scaledWidth = imgWidth;
            scaledHeight = imgHeight;
        }

        return axis == View.X_AXIS ? scaledWidth : scaledHeight;
    }

    @Override
    public void paint(Graphics g, Shape a) {
        if (icon == null) return;

        Rectangle alloc = (a instanceof Rectangle) ? (Rectangle) a : a.getBounds();

        if (icon instanceof ImageIcon) {
            g.drawImage(((ImageIcon) icon).getImage(), alloc.x, alloc.y, scaledWidth, scaledHeight, null);
        } else {
            icon.paintIcon(getContainer(), g, alloc.x, alloc.y);
        }
    }
}

