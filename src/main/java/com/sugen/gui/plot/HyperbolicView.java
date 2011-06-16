package com.sugen.gui.plot;

import java.awt.*;
import java.awt.geom.*;

import com.sugen.gui.Palette;
import com.sugen.util.DataModel;

/**
 * Hyperbolically view a Plot.
 *
 * @author Jonathan Bingham
 **/
public class HyperbolicView
    extends PlotView {
    /** @serial */
    protected Point2D origin = new Point2D.Double();
    /** @serial */
    protected Point2D modelOrigin;
    /** @serial */
    protected int radius;

    public HyperbolicView() {
        this(new Plot());
    }

    public HyperbolicView(Plot plot) {
        super(plot);

        setLabelRenderer(new HyperbolicLabelRenderer());

        //Mouse events
        //Allow rotation by shift dragging
        addMouseListener(rotationalUI);
        addMouseMotionListener(rotationalUI);

        //Allow translation by dragging
        TranslationUI translationUI = new TranslationUI(linearTransform, plot);
        addMouseListener(translationUI);
        addMouseMotionListener(translationUI);

        //Allow scaling by control dragging
        ScaleUI scaleUI = new ScaleUI(this, plot);
        addMouseListener(scaleUI);
        addMouseMotionListener(scaleUI);
    }

    /**
     * Use a HyperbolicTransform instead of an ordinary LinearTransform.
     */
    protected void setLinearTransform() {
        linearTransform = new HyperbolicTransform();
        plot.addListener(linearTransform);
        linearTransform.setPlot(plot);
        linearTransform.addListener(plotListener);
    }

    public void autoSize() {

        Insets insets = getInsets();
        linearTransform.setScaleProportional(true);

        Rectangle rect =
            new Rectangle(insets.left, insets.top,
                          getWidth() - insets.left - insets.right,
                          getHeight() - insets.top - insets.bottom);
        linearTransform.mapTo(rect);

        AffineTransform transform = linearTransform.getTransform();
        origin = transform.transform(new Point2D.Double(0, 0), null);

        //Model origin changes with the translation... so do this elsewhere?
        try {
            modelOrigin = linearTransform.inverseTransform(origin, null);
        }
        catch(NoninvertibleTransformException nte) {}

        radius = (int)(transform.transform(new Point2D.Double(0, 1), null).getY()
                       - origin.getY());
    }

    protected boolean isLabelLeft(int key, Point2D p) {
        return p.getX() < origin.getX();
    }

    protected void paintBackground(Graphics g) {
        super.paintBackground(g);
        int diameter = 2 * radius;
        double x = origin.getX() - radius;
        double y = origin.getY() - radius;

        //Draw unit circle
        g.setColor(Palette.lightSkyBlue);
        g.drawOval((int)x, (int)y, diameter, diameter);
    }

    /**
     * Move the given node to the center of the hyperbolic plot, rotated
     * into a canonical orientation with children to the right.
     */
    public void centerOn(int key) {
        AffineTransform transform = plot.getTransform();
        if(transform == null)
            transform = new AffineTransform();

        Point2D p = plot.get(key, null);

        //Translate so the point's at the center
        transform.setTransform(transform.getScaleX(), transform.getShearY(),
                               transform.getShearX(), transform.getScaleY(),
                               transform.getTranslateX() - p.getX(),
                               transform.getTranslateY() - p.getY());
        plot.setTransform(transform);
    }

    /**
     * Overridden to call autoTransform().
     */
    public void setDataModel(DataModel dm) {
        super.setDataModel(dm);
        autoTransform();
    }

    /**
     * Adjust the transform to fit the hyperbolic unit circle.
     */
    public void autoTransform() {

        //Reset the transform
        AffineTransform at = plot.getTransform();
        if(at == null) {
            at = new AffineTransform();
            plot.setTransform(at);
        }
        at.setToIdentity();

        //Set to reasonable first size
        Rectangle2D boundingRect = plot.boundingRect(null);
        double min = Math.min(boundingRect.getWidth(), boundingRect.getHeight());
        at.scale(10 / min, 10 / min);
    }
}
