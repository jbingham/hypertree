package com.sugen.gui.plot;

import java.awt.geom.*;
import java.util.*;

import com.sugen.util.*;

/**
 * A generic implementation of a plot containing a set of Points in 2D space.
 * These should be added using the superclass methods, <pre>add(Object obj)</pre>
 * or <pre>add(int index, Object obj)</pre>. They will
 * be retrieved in their list order.
 * <p>
 * Supports AffineTransforms.
 *
 * @author Jonathan Bingham
 */
public class Plot
    extends ListModel {
    /** @serial */
    protected DataModel dataModel;
    /** @serial */
    protected AffineTransform transform = null;

    /**
     * Get copy of the point, transformed if the affine transform is not null.
     */
    public Point2D get(int key, Point2D retval) {
        Point2D plotPoint = (Point2D)get(key);
        if(transform != null)
            return transform.transform(plotPoint, retval);
        else if(retval == null)
            return new Point2D.Double(plotPoint.getX(), plotPoint.getY());
        else {
            retval.setLocation(plotPoint.getX(), plotPoint.getY());
            return retval;
        }
    }

    public Point2D transform(Point2D p, Point2D retval) {
        if(transform != null)
            return transform.transform(p, retval);

        if(retval == null)
            retval = new Point2D.Double(p.getX(), p.getY());
        else
            p.setLocation(p);
        return retval;
    }

    public Point2D inverseTransform(Point2D p, Point2D retval)
        throws NoninvertibleTransformException {
        if(transform != null)
            return transform.inverseTransform(p, retval);

        if(retval == null)
            retval = new Point2D.Double(p.getX(), p.getY());
        else
            p.setLocation(p);
        return retval;
    }

    /**
     * Get the minimum bounding rectangle needed to contain all of the points
     * in the plot.
     */
    public Rectangle2D boundingRect(Rectangle2D rect) {
        double minX = Double.MAX_VALUE,
            minY = Double.MAX_VALUE,
            maxX = Double.MIN_VALUE,
            maxY = Double.MIN_VALUE;

        Point2D p = new Point2D.Double();
        Iterator iter = iterator();
        while(iter.hasNext()) {
            p = (Point2D)iter.next();
            double x = p.getX();
            double y = p.getY();
            if(x < minX)
                minX = x;
            if(y < minY)
                minY = y;
            if(x > maxX)
                maxX = x;
            if(y > maxY)
                maxY = y;
        }
        if(rect == null)
            rect = new Rectangle2D.Double(minX, minY, maxX - minX, maxY - minY);
        else
            rect.setFrame(minX, minY, maxX - minX, maxY - minY);
        return rect;
    }

    /**
     * @return an iterator of affine transformed points.
     */
    public Iterator iterator() {
        final Iterator iter = super.iterator();
        return new Iterator() {
            public boolean hasNext() {
                return iter.hasNext();
            }

            public Object next() {
                return transform((Point2D)iter.next(), null);
            }

            public void remove() {
                iter.remove();
            }
        };
    }

    /**
     * Passed along to listeners. Useful if the keys have some structured
     * relationship, such as a TreeModel, which isn't accessible via
     * the key alone. By default the model is null; it's a convenience.
     */
    public void setDataModel(DataModel model) {
        dataModel = model;
    }

    public DataModel getDataModel() {
        return dataModel;
    }

    /**
     * Convert from polar to Cartesian coordinates.
     */
    static public Point2D toCartesian(double radius, double theta, Point2D p) {
        double x = Math.cos(theta);
        double y = Math.sin(theta);
        double scalar = radius / Math.sqrt(x * x + y * y);
        x *= scalar;
        y *= scalar;
        if(p == null)
            p = new Point2D.Double(x, y);
        else
            p.setLocation(x, y);
        return p;
    }

    public void setTransform(AffineTransform at) {
        //System.out.println("PlotModel.setTransform()");
        transform = at;
        fireChanged(this, null);
    }

    public AffineTransform getTransform() {
        return transform;
    }

    public void validate() {}

    /**
     * Can the plot be rotated?.
     */
    public boolean isRotatable() {
        return true;
    }
}
