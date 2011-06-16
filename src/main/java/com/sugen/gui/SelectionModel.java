package com.sugen.gui;

import java.util.Collection;
import java.util.ArrayList;

import com.sugen.event.CollectionEvent;
import com.sugen.event.SelectionListener;
import com.sugen.event.UtilListener;
import com.sugen.util.CollectionModel;

/**
 * Calls SelectionListener methods on registered SelectionListeners and
 * CollectionListener methods on registered CollectionListeners.
 *
 * @author Jonathan Bingham
 */
public class SelectionModel
    extends CollectionModel {
    public SelectionModel() {
        super(new ArrayList());
    }

    public SelectionModel(Collection c) {
        super(c);
    }

    protected void fireAdded(UtilListener l, CollectionEvent e) {
        if(l instanceof SelectionListener)
            ((SelectionListener)l).selectionAdded(e);
        else
            super.fireAdded(l, e);
    }

    protected void fireRemoved(UtilListener l, CollectionEvent e) {
        if(l instanceof SelectionListener)
            ((SelectionListener)l).selectionRemoved(e);
        else
            super.fireRemoved(l, e);
    }

    protected void fireChanged(UtilListener l, CollectionEvent e) {
        if(l instanceof SelectionListener)
            ((SelectionListener)l).selectionChanged(e);
        else
            super.fireChanged(l, e);
    }
}
