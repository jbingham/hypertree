package com.sugen.gui.plot;

import java.awt.Component;
import java.awt.Rectangle;
import java.awt.event.MouseEvent;
import java.awt.geom.*;

/**
 * Rotate a plot around the center of some specified Component.
 * Shift drag to rotate.
 *
 * @author Jonathan Bingham
 */
public class RotationUI
    extends PlotUI {
    /** @serial */
    protected Component view;

    /**
     * @param centerOn - component whose center defines the origin for rotation.
     */
    public RotationUI(Component centerOn) {
        this(centerOn, null);
    }

    /**
     * @param centerOn - component whose center defines the origin for rotation
     * @param plot - the PlotModel containing the AffineTransform to rotate
     */
    public RotationUI(Component centerOn, Plot plot) {
        modifiers = MouseEvent.BUTTON1_MASK | MouseEvent.SHIFT_MASK;
        setPlot(plot);
        view = centerOn;
    }

    /*public void propertyChange(PropertyChangeEvent pce)
      {
     if(PROPERTY_OWNER.equals(pce.getPropertyName()))
      view = (Frame)pce.getNewValue();
      }*/

    /**
     * Start mouse-driven rotation from this point.
     */
    transient protected Point2D pStartRotation;

    /** @serial */
    protected Point2D viewOrigin;
    /** @serial */
    protected Point2D origin;

    /**
     * For undo.
     */
    transient protected double oldRotation;

    /**
     * Rotate from point where mouse was pressed to current position
     * relative to the model's origin.
     */
    public void mousePressed(MouseEvent e) {
        if(e.getModifiers() == modifiers) {
            pStartRotation = e.getPoint();
            try {
                Rectangle rect = view.getBounds();
                viewOrigin = new Point2D.Double(rect.x + rect.width / 2d,
                                                rect.y + rect.height / 2d);

                AffineTransform transform = plot.getTransform();
                if(transform == null)
                    transform = new AffineTransform();
                origin =
                    transform.inverseTransform(new Point2D.Double(0, 0), origin);
                //System.out.println(origin);
            }
            catch(NoninvertibleTransformException nte) {}
        }
    }

    public void mouseReleased(MouseEvent e) {
        pStartRotation = null;
    }

    /**
     * Rotate from point where mouse was pressed to current position
     * relative to the model's origin.
     */
    public synchronized void mouseDragged(MouseEvent e) {
        if(e.getModifiers() == modifiers) {
            Point2D pEndRotation = e.getPoint();

            double mouseRotation =
                Rotation.angle(viewOrigin, pStartRotation, pEndRotation);

            AffineTransform transform = plot.getTransform();
            if(transform == null)
                transform = new AffineTransform();
            transform.rotate(mouseRotation, origin.getX(), origin.getY());

            pStartRotation = pEndRotation;
            plot.setTransform(transform);
        }
    }

    /*
      public Action[] getActions()
      {
     return new Action[]{ rotateAction };
      }

      protected void updateActions()
      {
     boolean hasPlot = plotModel != null;
     rotateAction.setEnabled(hasPlot);
      }

      protected Action rotateAction = new AbstractAction("Rotate...")
      {
     {
      putValue(KEY_MENU, EDIT_MENU);
      putValue(KEY_LOCATION, VALUE_MENU_ONLY);
      this.setEnabled(false);
     }

     public void actionPerformed(ActionEvent e)
     {
      String rotate = JOptionPane.showInputDialog(owner,
          "Degrees to rotate:",
          "Rotate Plot", JOptionPane.PLAIN_MESSAGE);
      if(rotate == null)
       return;
      int rotation = 0;
      try
      {
       rotation = Integer.parseInt(rotate);
       rotateAboutOrigin(Math.PI * 2 * rotation / 360);
      }
      catch(NumberFormatException nte)
      {
       JOptionPane.showMessageDialog(owner,
        "Value must be an integer", "Error",
        JOptionPane.ERROR_MESSAGE);
      }
     }
      };

      public Action getRotateAction()
      {
     return rotateAction;
      }
     */

    public void rotateAboutOrigin(double radians) {
        AffineTransform transform = plot.getTransform();
        if(transform == null)
            transform = new AffineTransform();
        try {
            origin =
                transform.inverseTransform(new Point2D.Double(0, 0), origin);
        }
        catch(NoninvertibleTransformException nte) {}
        transform.rotate(radians, origin.getX(), origin.getY());
        plot.setTransform(transform);
    }
}
