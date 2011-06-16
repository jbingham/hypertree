package com.sugen.gui.plot;

import java.awt.geom.*;

/**
 * Static methods for rotating a plot.
 *
 * @author Jonathan Bingham
 */
public class Rotation { //extends PlotTransform
    /**
     * Rotate a point about a center point.
     */
    static public Point2D rotate(Point2D p, Point2D center, double radians) {
        if(radians == 0d)
            return p;
        Point2D q = new Point2D.Double(p.getX() - center.getX(),
                                       p.getY() - center.getY());
        q = rotate(q, radians, null);
        q = new Point2D.Double(q.getX() + center.getX(),
                               q.getY() + center.getY());
        return q;
    }

    /**
     * Rotate a point about the origin.
     */
    static public Point2D rotate(Point2D p, double radians, Point2D retval) {
        double x, y;
        if(radians == 0d) {
            x = p.getX();
            y = p.getY();
        }
        else {
            x = p.getX() * Math.cos(radians) - p.getY() * Math.sin(radians);
            y = p.getY() * Math.cos(radians) + p.getX() * Math.sin(radians);
        }
        if(retval == null)
            retval = new Point2D.Double(x, y);
        else
            retval.setLocation(x, y);
        return retval;
    }

    /**
     * Angle in radians of the arc from point p to point q relative to a center point.
     */
    static public double angle(Point2D center, Point2D p, Point2D q) {
        double angle1 = angle(p.getX() - center.getX(), p.getY() - center.getY());
        double angle2 = angle(q.getX() - center.getX(), q.getY() - center.getY());
        return angle2 - angle1;
    }

    /**
     * Angle in radians of the arc from point p to point q relative
     * to the origin.
     */
    static public double angle(Point2D p, Point2D q) {
        double angle1 = angle(p.getX(), p.getY());
        double angle2 = angle(q.getX(), q.getY());
        return angle2 - angle1;
    }

    /**
     * Return the angle from (1,0) to (x,y), always in the range of -pi to +pi.
     */
    static public double angle(double x, double y) {
        if(x == 0 && y == 0)
            return 0;

        double hypotenuse = Math.sqrt(x * x + y * y);
        double angle = 0;
        //Quadrants 1 and 4
        if(x >= 0)
            angle = Math.asin(y / hypotenuse);
        //Quadrant 2
        else if(y >= 0)
            angle = Math.acos(x / hypotenuse);
        //Quadrant 3
        else
            angle = (3 / 2) * Math.PI - Math.asin(y / hypotenuse);
        if(angle < -Math.PI)
            angle += 2 * Math.PI;
        else if(angle > Math.PI)
            angle -= 2 * Math.PI;
        return angle;
    }

    /**
     * The angle of (x,y) if p is the origin, always in the range of -pi to +pi.
     */
    static public double angle(Point2D p, double x, double y) {
        return angle(x - p.getX(), y - p.getY());
    }
}
