package com.sugen.gui.plot;

import java.beans.*;
import java.io.Serializable;
import javax.swing.Action;
import javax.swing.event.MouseInputAdapter;

import com.sugen.event.*;
import com.sugen.gui.AppBean;

/**
 * A GUI interface providing mouse controls for a Plot.
 *
 * @author Jonathan Bingham
 */
public class PlotUI
    extends MouseInputAdapter implements AppBean, CollectionListener,
    Serializable {
    /** @serial */
    protected Plot plot;

    public PlotUI() {
    }

    public PlotUI(Plot pl) {
        setPlot(pl);
    }

    public void setPlot(Plot pl) {
        if(plot != null)
            plot.removeListener(this);
        plot = pl;
        if(plot != null)
            plot.addListener(this);
        updateActions();
    }

    public Plot getPlot() {
        return plot;
    }

    protected void updateActions() {}

    public Action[] getActions() {
        return null;
    }

    /**
     * Modifiers for the initiation of PlotMouseEvents.
     * @serial
     */
    protected int modifiers = 0;

    /**
     * Modifiers for the initiation of PlotMouseEvents.
     */
    public void setModifiers(int i) {
        modifiers = i;
    }

    /**
     * Modifiers for the initiation of PlotMouseEvents.
     */
    public int getModifiers() {
        return modifiers;
    }

    /** @serial */
    protected PropertyChangeSupport propertySupport =
        new PropertyChangeSupport(this);

    public synchronized void addPropertyChangeListener(PropertyChangeListener
        pcl) {
        propertySupport.addPropertyChangeListener(pcl);
    }

    public synchronized void removePropertyChangeListener(
        PropertyChangeListener pcl) {
        propertySupport.removePropertyChangeListener(pcl);
    }

    public void propertyChange(PropertyChangeEvent pce) {}

    public void collectionChanged(CollectionEvent e) {}

    public void collectionAdded(CollectionEvent e) {}

    public void collectionRemoved(CollectionEvent e) {}
}
