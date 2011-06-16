package com.sugen.gui.plot;

import java.awt.geom.AffineTransform;

import javax.swing.JFrame;

/**
 * UI controls for a HyperbolicView.
 *
 * @author Jonathan Bingham
 */
public class HyperbolicViewUI
    extends PlotViewUI {
    public HyperbolicViewUI(JFrame owner) {
        this(owner, new HyperbolicView(), new PlotUI());
    }

    public HyperbolicViewUI(JFrame owner, HyperbolicView view, PlotUI plotUI) {
        super(owner, view, plotUI);
    }

    public void zoomIn() {
        AffineTransform transform = plotUI.getPlot().getTransform();
        if(transform == null)
            transform = new AffineTransform();
        transform.setTransform(transform.getScaleX() * zoomIncrement,
                               transform.getShearY() * zoomIncrement,
                               transform.getShearX() * zoomIncrement,
                               transform.getScaleY() * zoomIncrement,
                               transform.getTranslateX() * zoomIncrement,
                               transform.getTranslateY() * zoomIncrement);
        plotUI.getPlot().setTransform(transform);
    }

    public void zoomOut() {
        AffineTransform transform = plotUI.getPlot().getTransform();
        if(transform == null)
            transform = new AffineTransform();
        transform.setTransform(transform.getScaleX() / zoomIncrement,
                               transform.getShearY() / zoomIncrement,
                               transform.getShearX() / zoomIncrement,
                               transform.getScaleY() / zoomIncrement,
                               transform.getTranslateX() / zoomIncrement,
                               transform.getTranslateY() / zoomIncrement);
        plotUI.getPlot().setTransform(transform);
    }
}
