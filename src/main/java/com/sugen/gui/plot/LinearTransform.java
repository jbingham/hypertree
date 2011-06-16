package com.sugen.gui.plot;

import java.awt.geom.*;

import com.sugen.event.*;

//TODO: make it idiot-proof; override any harmful methods.
/**
 * Translate and scale a plot, perhaps for screen display.
 * Modification of the AffineTransform must be done through the
 * methods provided here, not via superclass methods, to avoid side effects.
 *
 * @author Jonathan Bingham
 */
public class LinearTransform
    extends Transform {
    /** Are x and y scales equal?.
     * @serial
     */
    protected boolean isScaleProportional = false;

    /** A fixed rectangle into which to map all model points.
     * @serial
     */
    protected Rectangle2D fixedRectangle;

    public LinearTransform() {
        transform = new AffineTransform();
    }

    /**
     * Set the scale to xScale, yScale.
     */
    public void setScale(double xScale, double yScale) {
        if(fixedRectangle != null)
            return;

        transform.scale(xScale / transform.getScaleX(),
                        yScale / transform.getScaleY());
        fireChanged((Object)this, (String)null);
    }

    /**
     * Lock the aspect ratio.
     */
    public void setScaleProportional(boolean b) {
        isScaleProportional = b;
    }

    /**
     * Lock the aspect ratio.
     */
    public boolean isScaleProportional() {
        return isScaleProportional;
    }

    /**
     * Set scale and translation to map all PlotModel points into the
     * specified rectangle's area. Use it to map from the model to a screen area.
     */
    public void mapTo(Rectangle2D rect) {
        fixedRectangle = rect;
        if(super.plot == null)
            return;

        Rectangle2D modelRect = modelBoundingRect(null);
        //System.err.println("map from " + modelRect);

        double translateX = rect.getX();
        double translateY = rect.getY();
        double scaleX = rect.getWidth() / modelRect.getWidth();
        double scaleY = rect.getHeight() / modelRect.getHeight();

        //Set scale
        if(isScaleProportional) {
            //Center inside rectangle
            if(scaleX > scaleY) {
                translateX += (rect.getWidth() - scaleY * modelRect.getWidth()) /
                    2;
                scaleX = scaleY;
            }
            else {
                translateY += (rect.getHeight() - scaleX * modelRect.getHeight()) /
                    2;
                scaleY = scaleX;
            }
        }
        transform.setTransform(scaleX, 0, 0, scaleY, translateX, translateY);
        transform.translate( -modelRect.getX(), -modelRect.getY());

        //System.err.println(transform);
    }

    protected Rectangle2D modelBoundingRect(Rectangle2D retval) {
        return super.plot.boundingRect(retval);
    }

    public void collectionChanged(CollectionEvent e) {
        if(fixedRectangle != null)
            mapTo(fixedRectangle);
        super.collectionChanged(e);
    }
}
