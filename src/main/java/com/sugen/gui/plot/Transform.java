package com.sugen.gui.plot;

import java.awt.geom.*;
import java.util.*;

import com.sugen.event.*;

/**
 * Any class that accesses a Plot can do so via a Transform.
 * This leaves the underlying model in place but creates a transformed view of it,
 * through which the Plot can be accessed and changed. Points are
 * transformed from model to view, and inverse transformed back from view to
 * model. An entire series
 * of transforms can be strung together. For visualization, this will
 * eventually lead to a transform that maps to a screen area.
 *
 * @author Jonathan Bingham
 */
public class Transform
    extends Plot implements CollectionListener {
    /** @serial */
    protected Plot plot;

    public Transform() {
    }

    public void setPlot(Plot plot) {
        this.plot = plot;
    }

    public Plot getPlot() {
        return plot;
    }

    /**
     * Map a point from the model's coordinate system into the view's.
     */
    protected Point2D applyTransform(Point2D from, Point2D into) {
        if(transform != null)
            return(Point2D)transform.transform(from, into);

        //No transform to apply
        if(into == null)
            return new Point2D.Double(from.getX(), from.getY());
        else {
            into.setLocation(from);
            return into;
        }
    }

    /**
     * Map a point from the view's coordinate system back into the model's.
     */
    protected Point2D applyInverseTransform(Point2D from, Point2D into) throws
        NoninvertibleTransformException {
        if(transform != null)
            return(Point2D)transform.inverseTransform(from, into);

        //No transform to apply
        if(into == null)
            return new Point2D.Double(from.getX(), from.getY());
        else {
            into.setLocation(from);
            return into;
        }
    }

    public Point2D get(int key, Point2D into) {
        if(plot == null)
            throw new IllegalStateException("Transform.get - plot==null");
        return applyTransform(plot.get(key, into), into);
    }

    public Iterator iterator() {
        final Iterator iter = super.iterator();
        return new Iterator() {
            public boolean hasNext() {
                return iter.hasNext();
            }

            public Object next() {
                return applyTransform((Point2D)iter.next(), null);
            }

            public void remove() {
                iter.remove();
            }
        };
    }

    public void add(int key, Point2D p) {
        try {
            super.add(key, applyInverseTransform(p, null));
        }
        catch(NoninvertibleTransformException nte) {}
        catch(NullPointerException npe) {}
    }

    public void add(Point2D p) {
        try {
            super.add(applyInverseTransform(p, null));
        }
        catch(NoninvertibleTransformException nte) {}
        catch(NullPointerException npe) {}
    }

    public Object remove(int key) {
        try {
            return plot.remove(key);
        }
        catch(NullPointerException npe) {
            return null;
        }
    }

    public void clear() {
        try {
            plot.clear();
        }
        catch(NullPointerException npe) {}
    }

    /**
     * Get untransformed point in the model.
     */
    public Point2D getModelPoint(int key, Point2D retval) {
        try {
            if(plot instanceof Transform)
                return((Transform)plot).getModelPoint(key, retval);
            else
                return plot.get(key, null);
        }
        catch(NullPointerException npe) {
            return null;
        }
    }

    /**
     * Given a point in this plot, returns the corresponding point in the
     * original untransformed model.
     */
    public Point2D inverseTransform(Point2D transformedPoint, Point2D retval) throws
        NoninvertibleTransformException {
        if(plot == null || !(plot instanceof Transform))
            return applyInverseTransform(transformedPoint, retval);
        retval = applyInverseTransform(transformedPoint, retval);
        return((Transform)plot).inverseTransform(retval, retval);
    }

    /**
     * Given a point in the original untransformed model, returns the
     * corresponding transformed point in this plot.
     */
    public Point2D transform(Point2D modelPoint, Point2D into) {
        if(plot == null || !(plot instanceof Transform))
            return applyTransform(modelPoint, into);

        ((Transform)plot).transform(modelPoint, into);
        return applyTransform(into, into);
    }

    public void collectionChanged(CollectionEvent e) {
        Plot model = (Plot)e.getSource();
        if(plot != model)
            setPlot(model);
        fireChanged(this, e.isMultiple() ? e.getAllChanged() : e.getChanged());
    }

    public void collectionAdded(CollectionEvent e) {
        Plot model = (Plot)e.getSource();
        if(plot != model)
            setPlot(model);
        fireAdded(this, e.isMultiple() ? e.getAllChanged() : e.getChanged());
    }

    public void collectionRemoved(CollectionEvent e) {
        Plot model = (Plot)e.getSource();
        if(plot != model)
            setPlot(model);
        fireRemoved(this, e.isMultiple() ? e.getAllChanged() : e.getChanged());
    }
}
