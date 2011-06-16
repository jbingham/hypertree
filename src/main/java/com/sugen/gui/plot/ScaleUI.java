package com.sugen.gui.plot;

import java.awt.*;
import java.awt.event.MouseEvent;
import java.awt.geom.*;

/**
 * Scale a plot interactively, via mouse and other controls.
 * Ctrl drag to resize.
 *
 * @author Jonathan Bingham
 */
public class ScaleUI
    extends PlotUI {
    /** @serial */
    protected Component view;

    /** Increment unit when interactively setting scale. */
    //protected double scaleIncrement = 1;

    public ScaleUI(Component comp) {
        this(comp, null);
    }

    public ScaleUI(Component comp, Plot plot) {
        view = comp;
        setModifiers(MouseEvent.BUTTON1_MASK | MouseEvent.CTRL_MASK);
        setPlot(plot);
    }

    /*public Action[] getActions()
      {
     return new Action[]{adjustScaleAction};
      }*/

    /*protected AbstractAction adjustScaleAction =
     new AbstractAction("Adjust Scale...",
      new ImageIcon(getClass().getResource("images/adjustTreeScale24.gif")))
      {
     public void actionPerformed(ActionEvent e)
     {
      adjustScale();
     }
      };

      public Action getAdjustScaleAction()
      {
     return adjustScaleAction;
      }*/

    /**
     * Let user adjust the horizontal and vertical scale of the display.
     */
    /*public void adjustScale()
      {
     final AffineTransform transform = plotModel.getTransform();

     final double oldXScale = transform.getScaleX();
     final double oldYScale = transform.getScaleY();

     FormDialog dialog = new FormDialog(null);
     final Form form = new Form(dialog);

     //Use sliders to adjust scale, with current value centered
     int intX = (int)(oldXScale / scaleIncrement);
     final JSlider xSlider =
      new JSlider(JSlider.HORIZONTAL, 1, 1000, intX);
     xSlider.setPaintTicks(true);

     int intY = (int)(oldYScale / scaleIncrement);
     final JSlider ySlider =
      new JSlider(JSlider.HORIZONTAL, 1, 1000, intY);
     ySlider.setPaintTicks(true);

     final JCheckBox previewBox = new JCheckBox("Preview", true);
     previewBox.addChangeListener(new ChangeListener()
     {
      public void stateChanged(ChangeEvent e)
      {
       linearTransform.setAdjusting(!previewBox.isSelected());
      }
     });

     final JCheckBox synchBox = new JCheckBox("Synchronize", true);
     synchBox.setEnabled(linearTransform.isScaleProportional());

     ChangeListener sliderListener = new ChangeListener()
     {
      double newXScale;
      double newYScale;

      public void stateChanged(ChangeEvent e)
      {
       if(e.getSource() == xSlider)
       {
        newXScale = xSlider.getValue() * scaleIncrement;
        //if(synchBox.isSelected() && ySlider.getValue() != newXScale)
        //	ySlider.setValue(xSlider.getValue());
       }
       else
       {
        newYScale = ySlider.getValue() * scaleIncrement;
        //if(synchBox.isSelected() && xSlider.getValue() != newYScale)
        //	xSlider.setValue(ySlider.getValue());
       }
       transform.setToScale(newXScale, newYScale);
       plotModel.firePlotTransformed(null);
      }
     };
     xSlider.addChangeListener(sliderListener);
     ySlider.addChangeListener(sliderListener);

     //Layout
     FormFactory factory = new FormFactory(form);
     form.add(factory.label(xSlider, "Horizontal Scale", FormLayout.TOP));
     form.add(factory.label(ySlider, "Vertical Scale", FormLayout.TOP));

     JPanel panel = new JPanel(new FlowLayout(FlowLayout.CENTER, 0, 10));
     panel.add(previewBox);
     panel.add(synchBox);
     form.add(panel);
     dialog.setForm(form);

     JButton resetButton = new JButton("Reset");
     resetButton.addActionListener(new ActionListener()
     {
      public void actionPerformed(ActionEvent e)
      {
       xSlider.setValue((int)(oldXScale / scaleIncrement));
       ySlider.setValue((int)(oldYScale / scaleIncrement));
       form.revalidate();
       form.repaint();
       transform.setToScale(oldXScale, oldYScale);
       plotModel.firePlotTransformed(null);
      }
     });
     dialog.setButtons(new JButton[]
      {resetButton, dialog.okButton, dialog.cancelButton});

     Object retval = dialog.showDialog();
     //If user canceled, reset
     if(retval == null)
     {
      transform.setToScale(oldXScale, oldYScale);
      plotModel.firePlotTransformed(null);
     }
      }*/

    /** Increment unit when interactively setting scale. */
    /*public void setScaleIncrement(double d)
      {
     scaleIncrement = d;
      }*/

    /** Increment unit when interactively setting scale. */
    /*public double getScaleIncrement()
      {
     return scaleIncrement;
      }*/

    /**
     * Start mouse-driven resize/scale from this radius.
     */
    transient protected Point2D pStartScale = null;

    //protected Point2D origin = null;
    /** @serial */
    protected Point2D viewOrigin = null;

    public void mousePressed(MouseEvent e) {
        if(e.getModifiers() == modifiers) {
            Rectangle rect = view.getBounds();
            viewOrigin = new Point2D.Double(rect.x + rect.width / 2d,
                                            rect.y + rect.height / 2d);

            pStartScale = e.getPoint();
        }
    }

    public void mouseDragged(MouseEvent e) {
        if(e.getModifiers() == modifiers) {
            Point2D pEndScale = e.getPoint();

            double scale = pEndScale.distance(viewOrigin)
                / pStartScale.distance(viewOrigin);

            AffineTransform transform = super.plot.getTransform();
            transform.setTransform(transform.getScaleX() * scale,
                                   transform.getShearY(),
                                   transform.getShearX(),
                                   transform.getScaleY() * scale,
                                   transform.getTranslateX() * scale,
                                   transform.getTranslateY() * scale);

            pStartScale = pEndScale;
            super.plot.setTransform(transform);
        }
    }
}
