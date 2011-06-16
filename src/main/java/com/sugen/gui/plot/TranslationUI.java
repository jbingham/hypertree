package com.sugen.gui.plot;

import java.awt.*;
import java.awt.event.MouseEvent;
import java.awt.geom.*;

/**
 * Translate a plot interactively, via mouse and other controls.
 * Drag to translate.
 *
 * @author Jonathan Bingham
 */
public class TranslationUI
    extends PlotUI {
    /** @serial */
    protected Plot view;

    public TranslationUI(Plot view) {
        this(view, null);
    }

    public TranslationUI(Plot view, Plot target) {
        this.view = view;
        setModifiers(MouseEvent.BUTTON1_MASK);
        setPlot(target);
    }

    /**
     * Start mouse-driven translation from this point.
     */
    transient protected Point2D pStartTranslation = new Point();

    /**
     * Translate from point where mouse was pressed to current position
     * relative to the model's origin.
     */
    public void mousePressed(MouseEvent e) {
        if(e.getModifiers() == modifiers) {
            try {
                pStartTranslation = view.inverseTransform(e.getPoint(), null);
            }
            catch(NoninvertibleTransformException nte) {
                nte.printStackTrace();
            }
        }
    }

    public void mouseReleased(MouseEvent e) {
        pStartTranslation = null;
    }

    /**
     * Translate from point where mouse was pressed to current position
     * relative to the model's origin.
     */
    synchronized public void mouseDragged(MouseEvent e) {
        if(pStartTranslation != null) {
            AffineTransform transform = super.plot.getTransform();
            if(transform == null)
                transform = new AffineTransform();

            Point2D pEndTranslation = e.getPoint();
            try {
                pEndTranslation = view.inverseTransform(e.getPoint(), null);
            }
            catch(NoninvertibleTransformException nte) {
                nte.printStackTrace();
            }
            double deltaX = pEndTranslation.getX() - pStartTranslation.getX();
            double deltaY = pEndTranslation.getY() - pStartTranslation.getY();

            transform.setTransform(transform.getScaleX(),
                                   transform.getShearY(),
                                   transform.getShearX(),
                                   transform.getScaleY(),
                                   transform.getTranslateX() + deltaX,
                                   transform.getTranslateY() + deltaY);

            pStartTranslation = pEndTranslation;
            super.plot.setTransform(transform);
        }
    }
}
