package richtextfield;

import java.awt.Graphics;
import java.awt.Image;
import java.awt.Rectangle;
import java.awt.Shape;
import java.awt.Toolkit;
import java.util.Base64;
import java.util.logging.Level;
import javax.swing.text.AttributeSet;
import javax.swing.text.Element;
import javax.swing.text.StyleConstants;
import javax.swing.text.View;
import javax.swing.text.ViewFactory;
import javax.swing.text.html.HTML;
import javax.swing.text.html.HTMLEditorKit;
import javax.swing.text.html.ImageView;
import richtextfield.utils.CustomLogger;

public class ScaledHTMLEditorKit extends HTMLEditorKit {

    private final ViewFactory defaultFactory = new ScaledHTMLFactory();

    @Override
    public ViewFactory getViewFactory() {
        return defaultFactory;
    }

    public static class ScaledHTMLFactory extends HTMLEditorKit.HTMLFactory {

        @Override
        public View create(Element elem) {
            Object o = elem.getAttributes().getAttribute(StyleConstants.NameAttribute);
            if (o instanceof HTML.Tag && o == HTML.Tag.IMG) {
                return new ScaledImageView(elem);
            }
            return super.create(elem);
        }
    }

    public static class ScaledImageView extends ImageView {

        private Image cachedImage = null;

        public ScaledImageView(Element elem) {
            super(elem);
        }

        @Override
        public void paint(Graphics g, Shape a) {
            Rectangle alloc = (a instanceof Rectangle) ? (Rectangle) a : a.getBounds();

            if (cachedImage == null) {
                cachedImage = loadImageSafely();
            }

            if (cachedImage != null) {
                int imgWidth = cachedImage.getWidth(null);
                int imgHeight = cachedImage.getHeight(null);
                int maxWidth = getContainer().getWidth();

                if (imgWidth > maxWidth) {
                    float scale = (float) maxWidth / imgWidth;
                    int newWidth = maxWidth;
                    int newHeight = (int) (imgHeight * scale);
                    g.drawImage(cachedImage, alloc.x, alloc.y, newWidth, newHeight, null);
                } else {
                    g.drawImage(cachedImage, alloc.x, alloc.y, imgWidth, imgHeight, null);
                }
            }
        }

        @Override
        public float getPreferredSpan(int axis) {
            if (cachedImage == null) {
                cachedImage = loadImageSafely();
            }

            if (cachedImage != null) {
                int imgWidth = cachedImage.getWidth(null);
                int imgHeight = cachedImage.getHeight(null);
                int containerWidth = getContainer() != null ? getContainer().getWidth() : imgWidth;

                if (axis == View.X_AXIS && imgWidth > containerWidth) {
                    return containerWidth;
                } else if (axis == View.Y_AXIS && imgWidth > containerWidth) {
                    float scale = (float) containerWidth / imgWidth;
                    return imgHeight * scale;
                }

                return axis == View.X_AXIS ? imgWidth : imgHeight;
            }

            return super.getPreferredSpan(axis);
        }

        private Image loadImageSafely() {
            try {
                AttributeSet attrs = getElement().getAttributes();
                Object srcAttr = attrs.getAttribute(HTML.Attribute.SRC);
                if (srcAttr instanceof String src) {
                    if (src.startsWith("data:image")) {
                        int commaIndex = src.indexOf(',');
                        if (commaIndex != -1 && commaIndex + 1 < src.length()) {
                            String base64 = src.substring(commaIndex + 1);
                            byte[] imageBytes = Base64.getDecoder().decode(base64);
                            return Toolkit.getDefaultToolkit().createImage(imageBytes);
                        }
                    }
                }
            } catch (Exception e) {
                CustomLogger.print(ScaledHTMLEditorKit.class, Level.SEVERE, "Error al interactuar cargar una imagen.", e);
            }
            return null;
        }
    }
}
