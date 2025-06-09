/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package richtextfield;

import javax.swing.Icon;
import javax.swing.text.Element;
import javax.swing.text.StyleConstants;
import javax.swing.text.View;
import javax.swing.text.ViewFactory;
import richtextfield.utils.ScaledIconView;

/**
 *
 * @author Entomomatico
 */
public class RTFScaledViewFactory implements ViewFactory {

    private final ViewFactory originalViewFactory;

    public RTFScaledViewFactory(ViewFactory originalViewFactory) {
        this.originalViewFactory = originalViewFactory;
    }

    @Override
    public View create(Element elem) {
        Object kind = elem.getAttributes().getAttribute(StyleConstants.NameAttribute);

        if (StyleConstants.IconElementName.equals(kind)) {
            return new ScaledIconView(elem);
        }

        Object iconAttr = elem.getAttributes().getAttribute(StyleConstants.IconAttribute);
        if (iconAttr instanceof Icon) {
            return new ScaledIconView(elem);
        }

        return originalViewFactory.create(elem);
    }
}
