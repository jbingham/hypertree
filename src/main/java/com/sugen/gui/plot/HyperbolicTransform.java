package com.sugen.gui.plot;

import java.awt.geom.*;

/**
 * Hyperbolically maps an infinite Cartesian space onto a unit circle.
 * As points approach the edge of the unit circle, their Cartesian coordinates
 * go to infinity, so that no point can ever lie along the circumference
 * of the circle (which represents infinity).
 *
 * @author Jonathan Bingham
 */
public class HyperbolicTransform
    extends LinearTransform {

    public HyperbolicTransform() {
        super();
    }

    protected Point2D applyTransform(Point2D point, Point2D retval) {
        double modelX = point.getX();
        double modelY = point.getY();

        //No hyperbolic transform made to (0,0)
        if(modelX == 0 && modelY == 0)
            return super.applyTransform(point, retval);

        double modelDistFromOrigin = Math.sqrt(modelX * modelX +
                                               modelY * modelY);
        double conversionFactor = (1 - 1 / (modelDistFromOrigin + 1))
            / modelDistFromOrigin;

        double viewX = modelX * conversionFactor;
        double viewY = modelY * conversionFactor;

        if(retval == null)
            retval = new Point2D.Double(viewX, viewY);
        else
            retval.setLocation(viewX, viewY);

        return super.applyTransform(retval, retval);
    }

    protected Point2D applyInverseTransform(Point2D point, Point2D retval) throws
        NoninvertibleTransformException {
        retval = super.applyInverseTransform(point, retval);

        double viewX = retval.getX();
        double viewY = retval.getY();

        //No hyperbolic transform made to (0,0)
        if(viewX == 0 && viewY == 0)
            return retval;

        double viewDistFromOrigin = Math.sqrt(viewX * viewX + viewY * viewY);
        double conversionFactor = (1 / (1 - viewDistFromOrigin) - 1)
            / viewDistFromOrigin;

        double modelX = viewX * conversionFactor;
        double modelY = viewY * conversionFactor;

        //if(retval == null)
        //	retval = new Point2D.Double(modelX, modelY);
        //else
        retval.setLocation(modelX, modelY);

        return retval;
    }

    /**
     * Always return a unit rectangle.
     */
    protected Rectangle2D modelBoundingRect(Rectangle2D retval) {
        if(retval == null)
            retval = new Rectangle2D.Double( -1, -1, 2, 2);
        else
            retval.setFrame( -1, -1, 2, 2);
        return retval;
    }

    //Print out some tangents
    /*public static void main(String args[])
      {
     double y = 0.5;
     if(args.length > 0)
      y = Double.parseDouble(args[0]);

     HyperbolicTransform ht = new HyperbolicTransform();

     Point2D p = new Point2D.Double(0, y);
     Point2D q = new Point2D.Double(0, y);
     System.out.println(y + " " + ht.transform(q, null).distance(0, 0)); //radius
     System.out.println("x\tslope\tpRadius");
     for(double x = -8; x < 8; x += 0.5)
     {
      q.setLocation(p);
      p.setLocation(x, y);
      Point2D hyperP = ht.transform(p, null);
      Point2D hyperQ = ht.transform(q, null);
      System.out.println(x + "\t" + (hyperP.getX() - hyperQ.getX() / hyperP.getY() - hyperQ.getY())
             + "\t" + hyperP.distance(0, 0));
     }
      }*/
}
