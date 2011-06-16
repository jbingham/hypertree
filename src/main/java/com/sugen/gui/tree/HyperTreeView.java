package com.sugen.gui.tree;

import java.awt.*;
import java.awt.geom.*;
import javax.swing.tree.*;

import com.sugen.gui.plot.*;

/**
 * Hyperbolically view a TreePlot.
 *
 * @author Jonathan Bingham
 */
public class HyperTreeView extends HyperbolicView {
    //protected QuadCurve2D quadCurve = new QuadCurve2D.Double();

    /**
     * Curves with distanceSq less than this value will be rendered as lines
     */
    protected int FLATNESS = 100;

    public HyperTreeView() {
        this(new HyperTreePlot());
    }

    public HyperTreeView(HyperTreePlot plot) {
        super(plot);
    }

    protected void paintPoint(Graphics2D g, int key, Point2D p) {
        //Find parent node's location on screen
        TreeNode node = (TreeNode) plot.getDataModel().get(key);
        TreeNode parent = node.getParent();
        int parentKey = plot.getDataModel().indexOf(parent);
        if (parentKey < 0)
            return;

        Point2D q = linearTransform.get(parentKey, null);

        //Set branch color
        Color originalColor = g.getColor();
        Color branchColor = getColor(node);
        if (branchColor != null)
            g.setColor(branchColor);

        if (isAdjusting || p.distanceSq(q) < FLATNESS) {
            g.drawLine((int)Math.round(p.getX()), (int)Math.round(p.getY()),
                       (int)Math.round(q.getX()), (int)Math.round(q.getY()));
        }
        else {
            drawArc(g, p, q, key, parentKey);
        }
        g.setColor(originalColor);
    }

    /**
     * Tangent to origin.
     * @param key1 - first node key
     * @param key2 - second node key
     * @return - tangent
     */
    private Point2D tangent(int key1, int key2) {
        //Solve intersection of two linear equations:
        //1 - the line defined by the two keys' points
        //2 - the perpendicular line passing through the origin

        Point2D p = plot.get(key1, null);
        Point2D q = plot.get(key2, null);
        double slope = (p.getY() - q.getY()) / (p.getX() - q.getX());
        double intercept = p.getY() - slope * p.getX();

        double perpSlope = -1 / slope;
        double perpIntercept = modelOrigin.getY()
            - perpSlope * modelOrigin.getX();

        double x = (perpIntercept - intercept) / (slope - perpSlope);
        double y = slope * x + intercept;
        //System.err.println(p + " " + slope + " " + intercept + "\n" + q + "\n" + modelOrigin + "\n" + new Point2D.Double(x,y));
        return linearTransform.transform(new Point2D.Double(x, y), null);
    }

    /**
     * Draw a better approximation of a hyperbolic arc.
     */
    private void drawArc(Graphics2D g, Point2D p, Point2D q,
                         int key1, int key2) {
        Point2D tangent = tangent(key1, key2);

        double tangentToP = tangent.distanceSq(p);
        double tangentToQ = tangent.distanceSq(q);
        double pToQ = p.distanceSq(q);

        //System.out.println(key1 + " to " + key2);

        //Check if tangent lies along the curve between p and q
        if ( (tangentToP < pToQ) && (tangentToQ < pToQ)) {
            Point2D control1 = getControlPoint(tangent, tangent, p);
            QuadCurve2D quadCurve = new QuadCurve2D.Double();
            quadCurve.setCurve(tangent, control1, p);
            g.draw(quadCurve);
            //g.fillRect((int)control.getX(), (int)control.getY(), 2, 2);

            Point2D control2 = getControlPoint(tangent, tangent, q);
            quadCurve.setCurve(tangent, control2, q);
            g.draw(quadCurve);
            //g.fillRect((int)control.getX(), (int)control.getY(), 2, 2);
            //g.drawString("t", (int)tangent.getX(), (int)tangent.getY());
        }
        else {
            //g.drawString("t", (int)tangent.getX(), (int)tangent.getY());
            //Point2D control = p; //HACK: produces a simple line
            Point2D control1 = getControlPoint(tangent, p, q);
            QuadCurve2D quadCurve = new QuadCurve2D.Double();
            quadCurve.setCurve(p, control1, q);
            g.draw(quadCurve);
            //g.fillRect((int)control.getX(), (int)control.getY(), 2, 2);
        }
    }

    /**
     * Crude, insufficiently curvy approximation. The midpoint of the two
     * parameter points.
     */
    private Point2D getMidpoint(Point2D p, Point2D q) {
        try {
            p = linearTransform.inverseTransform(p, null);
            q = linearTransform.inverseTransform(q, null);
        }
        catch (NoninvertibleTransformException nte) {}

        Point2D pControl =
            new Point2D.Double((p.getX() + q.getX()) / 2,
                               (p.getY() + q.getY()) / 2);
        return linearTransform.transform(pControl, pControl);
    }

    /**
     * Parameters p and q must both lie to the same side of the tangent point,
     * since this is the control point for a quadratic, not a cubic, curve.
     * @param tangent - the tangent to the origin of the line defined by p and q
     */
    private Point2D getControlPoint(Point2D tangent, Point2D p, Point2D q) {
        if (p.distance(tangent) / radius > 0.3)
            return getMidpoint(p, q);

        //Rotate p and q into a canonical orientation, such that
        //p.y and q.y are always both positive, and p.x and q.x always
        //have the same sign
        double tangentAngle = Rotation.angle(origin, tangent.getX(),
                                             tangent.getY()) - Math.PI / 2;
        //System.out.println(angle); //correct value
        Point2D rotateP = Rotation.rotate(p, origin, -tangentAngle);
        Point2D rotateQ = Rotation.rotate(q, origin, -tangentAngle);

        double x = (rotateP.getX() + rotateQ.getX()) / 2;
        double maxY = Math.max(rotateP.getY(), rotateQ.getY());
        Point2D retval = new Point2D.Double(x, maxY);

        //System.out.println("slopeP=" + slopeP + " slopeQ=" + slopeQ);

        //Rotate back from canonical orientation to actual
        retval = Rotation.rotate(retval, origin, tangentAngle);
        return retval;
    }
}
