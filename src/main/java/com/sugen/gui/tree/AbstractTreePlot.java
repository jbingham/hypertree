package com.sugen.gui.tree;

import com.sugen.gui.plot.Plot;
import com.sugen.util.TreeDataModel;

/**
 * Abstract base class for plotting trees in two-dimensional space.
 *
 * <p>
 * Use a TreeView to display the plot. This is just a store for the
 * coordinates and layout of nodes and leaves.
 * </p>
 *
 * @author Jonathan Bingham
 */
abstract public class AbstractTreePlot
    extends Plot {

    public AbstractTreePlot() {
        setDataModel(null);
    }

    public void validate() {
        getCollection().clear();  // method from ListModel
        if (getTransform() != null)
            getTransform().setToIdentity();
        if (getDataModel() != null) {
            plotTree((TreeDataModel)getDataModel());
        }
    }

    abstract public void plotTree(TreeDataModel tree);
}
