package com.sugen.gui.tree;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.geom.AffineTransform;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.Map;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.Icon;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPopupMenu;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.undo.AbstractUndoableEdit;

import com.sugen.gui.Icons;
import com.sugen.gui.plot.PlotUI;
import com.sugen.gui.plot.PlotView;
import com.sugen.gui.plot.PlotViewUI;
import com.sugen.util.ClusterTreeNode;
import com.sugen.util.DataModel;
import com.sugen.util.TreeDataModel;

/**
 * User controls for tree views.
 * Allows user to switch between linear, radial and hyperbolic views.
 *
 * @author Jonathan Bingham
 */
public class TreeViewUI extends PlotViewUI {

    //Used by the chooseStyleAction as well as setStyle(String).
    public static final String PROPERTY_TREE_STYLE = "tree_style";
    public static final String STYLE_RADIAL = "Radial Tree";
    public static final String STYLE_LINEAR = "Linear Tree";
    public static final String STYLE_HYPERBOLIC = "Hyperbolic Tree";

    private LinearTreeView linearTreeView;
    private RadialTreeView radialTreeView;
    private HyperTreeView hyperTreeView;

    private String style = "init";

    public TreeViewUI(JFrame owner) {
        this(owner, new LinearTreeView(), new PlotUI());
    }

    public TreeViewUI(JFrame owner, PlotView view, PlotUI plotUI) {
        super(owner, view, plotUI);
        linearTreeView = new LinearTreeView();

        radialTreeView = new RadialTreeView();
        radialTreeView.setDataModel(linearTreeView.getDataModel());
        radialTreeView.setColors(linearTreeView.getColors());
        radialTreeView.setSelectionModel(linearTreeView.getSelectionModel());

        hyperTreeView = new HyperTreeView();
        hyperTreeView.setDataModel(linearTreeView.getDataModel());
        hyperTreeView.setColors(linearTreeView.getColors());
        hyperTreeView.setSelectionModel(linearTreeView.getSelectionModel());

        setStyle(STYLE_HYPERBOLIC);  // default view to display
    }

    protected java.util.List getActionList() {
        java.util.List actions = super.getActionList();

        super.getFontAction().putValue(KEY_SEPARATOR_AFTER, Boolean.TRUE);
        super.getFitToViewAction().putValue(KEY_SEPARATOR_AFTER, Boolean.TRUE);

        actions.add(chooseStyleAction);

        selectNeighborsAction.putValue(Action.NAME, "Select Children");

        actions.add(rerootAction);
        actions.add(transformAction);
        return actions;
    }

    /** @serial */
    protected Action chooseStyleAction = new AbstractAction() {
        {
            putValue(NAME, new String[] {STYLE_LINEAR,
                     STYLE_RADIAL, STYLE_HYPERBOLIC});
            Icon radialIcon = Icons.get("circularTree24.gif");
            Icon linearIcon = Icons.get("linearTree24.gif");
            Icon hyperIcon = Icons.get("hyperTree24.gif");
            putValue(Action.SMALL_ICON, new Icon[] {linearIcon, radialIcon, hyperIcon});
            putValue(KEY_ACTION_TYPE, VALUE_BUTTON_GROUP);
            putValue(KEY_SELECTED_STATE, STYLE_HYPERBOLIC);
        }

        public void actionPerformed(ActionEvent e) {
            setStyle(e.getActionCommand());
        }
    };

    public Action getChooseStyleAction() {
        return chooseStyleAction;
    }

    public void setStyle(String newStyle) {
        if(!style.equals(newStyle)) {
            // Remember selections for the new view
            ArrayList selections = new ArrayList(
                getPlotView().getSelectionModel().getCollection());

            if(newStyle.equals(STYLE_LINEAR))
                setPlotView(linearTreeView);
            else if (newStyle.equals(STYLE_RADIAL))
                setPlotView(radialTreeView);
            else if(newStyle.equals(STYLE_HYPERBOLIC)) {
                setPlotView(hyperTreeView);
                hyperTreeView.autoTransform(); // scale to fit circle
            }
            else
                throw new IllegalArgumentException(
                    "Invalid tree style: " + newStyle);
            style = newStyle;

            getPlotView().getSelectionModel().addAll(selections);

            getPlotView().setPreferredSize(new Dimension(0,0)); // reset zoom
            getPlotView().refresh(false);
        }
    }

    public String getStyle() {
        return style;
    }

    protected void updateActions() {
        if(transformAction == null || rerootAction == null) {
            return;
        }

        try {
            super.updateActions();
            boolean hasData = plotView.getDataModel() != null
                && plotView.getDataModel().size() > 0;

            transformAction.setEnabled(hasData);

            boolean hasSelection = !plotView.getSelectionModel().isEmpty();
            rerootAction.setEnabled(hasData && hasSelection);

            chooseStyleAction.setEnabled(getPlotView().getPlot() != null);
        }
        catch(NullPointerException npe) {
            npe.printStackTrace();
        }
    }

    /**
     * Keep the three tree views in synch.
     */
    public void setDataModel(DataModel data) {
        linearTreeView.setDataModel(data);
        linearTreeView.getPlot().setDataModel(data);
        radialTreeView.setDataModel(data);
        radialTreeView.getPlot().setDataModel(data);
        hyperTreeView.setDataModel(data);
        hyperTreeView.getPlot().setDataModel(data);
        updateActions();
    }


    /** @serial */
    protected Action transformAction = new AbstractAction("Log Transform...",
        Icons.get("emptyIcon24.gif")) {
        {
            putValue(KEY_MENU, EDIT_MENU);
            putValue(KEY_LOCATION, VALUE_MENU_ONLY);
            this.setEnabled(false);
        }

        public void actionPerformed(ActionEvent e) {
            int retval = JOptionPane.showConfirmDialog(owner,
                "A log transform will be applied to all branch lengths. Continue?");
            if(retval == JOptionPane.CANCEL_OPTION)
                return;
            logTransform();
        }
    };

    public Action getTransformAction() {
        return transformAction;
    }

    /** @serial */
    protected Action rerootAction = new RerootAction();
    protected class RerootAction
        extends AbstractAction {
        public RerootAction() {
            super("Reroot...", Icons.get("emptyIcon24.gif"));
            putValue(KEY_MENU, EDIT_MENU);
            putValue(KEY_LOCATION, VALUE_MENU_ONLY);
        }

        public void actionPerformed(ActionEvent e) {
            if(getPlotView().getSelectionModel().isEmpty())
                return;
            Iterator iter = getPlotView().getSelectionModel().iterator();
            if(!iter.hasNext())
                return;

            int retval = JOptionPane.showConfirmDialog(owner,
                "Reroot the tree?");
            if(retval == JOptionPane.CANCEL_OPTION)
                return;

            DefaultMutableTreeNode newRoot = (DefaultMutableTreeNode)iter.next();
            reroot(newRoot);

            JOptionPane.showMessageDialog(owner, "Rerooted.");
        }
    }

    public Action getRerootAction() {
        return rerootAction;
    }

    public void reroot(DefaultMutableTreeNode newRoot) {
        if(newRoot == null)
            return;
        TreeDataModel tree = (TreeDataModel)plotView.getDataModel();
        DefaultMutableTreeNode oldRoot = tree.getRoot();
        editor.addEdit(new UndoableReroot(oldRoot, newRoot));
        firePropertyChange(PROPERTY_DATA, null, plotView.getDataModel());
    }

    protected class UndoableReroot
        extends AbstractUndoableEdit {
        private DefaultMutableTreeNode oldRootNode;
        private DefaultMutableTreeNode newRootNode;

        UndoableReroot(DefaultMutableTreeNode oldRoot,
                       DefaultMutableTreeNode newRoot) {
            oldRootNode = oldRoot;
            newRootNode = newRoot;
            reroot(newRoot);
        }

        private void reroot(DefaultMutableTreeNode node) {
            //Ascend the tree from the new root to the old, making each successive
            //parent node a child of the previous
            TreeDataModel tree = (TreeDataModel)plotView.getDataModel();
            Object root = tree.getRoot();
            ClusterTreeNode currentNode = (ClusterTreeNode)node;
            ClusterTreeNode parent = (ClusterTreeNode)node.getParent();
            ClusterTreeNode child;
            while(currentNode != root) {
                child = currentNode;
                currentNode = parent;
                if(parent != null)
                    parent = (ClusterTreeNode)parent.getParent();

                if(currentNode != null) {
                    if(currentNode.isNodeChild(child))
                        currentNode.remove(child);
                    child.setBranchLength(currentNode.getBranchLength());
                    child.add(currentNode);
                }
            }
            tree.setRoot(node);

            validatePlots();
            getPlotView().validate();
            fitToView();
        }

        public void undo() {
            super.undo();
            reroot(oldRootNode);
        }

        public void redo() {
            super.redo();
            reroot(newRootNode);
        }
    }

    public void logTransform() {
        editor.addEdit(new UndoableTransform());
        firePropertyChange(PROPERTY_DATA, null, plotView.getDataModel());
    }

    protected class UndoableTransform
        extends AbstractUndoableEdit {
        private int MULTIPLIER = 10000;
        private int CONSTANT = 2;

        UndoableTransform() {
            applyTransform();
        }

        private void applyTransform() {
            DataModel dataModel = getPlotView().getPlot().getDataModel();
            Iterator iter = dataModel.iterator();
            while(iter.hasNext()) {
                Object obj = iter.next();
                if(!(obj instanceof ClusterTreeNode))
                    continue;
                ClusterTreeNode node = (ClusterTreeNode)obj;
                double oldValue = node.getBranchLength();
                if(oldValue >= 0)
                    node.setBranchLength(
                        Math.log(oldValue * MULTIPLIER + CONSTANT));
            }

            validatePlots();
            getPlotView().validate();
            fitToView();
        }

        public void undo() {
            super.undo();
            DataModel dataModel = getPlotView().getPlot().getDataModel();
            Iterator iter = dataModel.iterator();
            while(iter.hasNext()) {
                Object obj = iter.next();
                if(!(obj instanceof ClusterTreeNode))
                    continue;
                ClusterTreeNode node = (ClusterTreeNode)obj;
                double oldValue = node.getBranchLength();
                if(oldValue >= 0)
                    node.setBranchLength(
                        (Math.exp(oldValue) - CONSTANT) / MULTIPLIER);
            }
            validatePlots();
            getPlotView().validate();
            fitToView();
        }

        public void redo() {
            super.redo();
            applyTransform();
        }
    }

    /**
     * Overridden to select children.
     */
    public void selectNeighbors() {
        Collection newSelections = new ArrayList();
        Iterator iter = plotView.getSelectionModel().iterator();
        while(iter.hasNext()) {
            DefaultMutableTreeNode node = (DefaultMutableTreeNode)iter.next();
            if(!node.isLeaf()) {
                Enumeration en = node.breadthFirstEnumeration();
                while(en.hasMoreElements())
                    newSelections.add(en.nextElement());
            }
        }
        plotView.getSelectionModel().addAll(newSelections);
    }

    protected JPopupMenu getPopup() {
        JPopupMenu popup = super.getPopup();
        popup.add(transformAction);
        return popup;
    }

    protected JPopupMenu getLabelPopup() {
        JPopupMenu popup = super.getLabelPopup();
        popup.add(rerootAction);
        return popup;
    }

    public void zoomIn() {
        if (hyperTreeView == getPlotView()) {
            AffineTransform transform = plotUI.getPlot().getTransform();
            if (transform == null)
                transform = new AffineTransform();
            transform.setTransform(transform.getScaleX() * zoomIncrement,
                                   transform.getShearY() * zoomIncrement,
                                   transform.getShearX() * zoomIncrement,
                                   transform.getScaleY() * zoomIncrement,
                                   transform.getTranslateX() * zoomIncrement,
                                   transform.getTranslateY() * zoomIncrement);
            getPlotUI().getPlot().setTransform(transform);
        }
        else if(linearTreeView == getPlotView()) {
            super.setZoomX(false);
            super.zoomIn();
        }
        else {
            super.setZoomX(true);
            super.zoomIn();
        }
    }

    public void zoomOut() {
        if (hyperTreeView == getPlotView()) {
            AffineTransform transform = plotUI.getPlot().getTransform();
            if (transform == null)
                transform = new AffineTransform();
            transform.setTransform(transform.getScaleX() / zoomIncrement,
                                   transform.getShearY() / zoomIncrement,
                                   transform.getShearX() / zoomIncrement,
                                   transform.getScaleY() / zoomIncrement,
                                   transform.getTranslateX() / zoomIncrement,
                                   transform.getTranslateY() / zoomIncrement);
            getPlotUI().getPlot().setTransform(transform);
        }
        else if(linearTreeView == getPlotView()) {
            super.setZoomX(false);
            super.zoomOut();
        }
        else {
            super.setZoomX(true);
            super.zoomOut();
        }
    }

    public void setLabelFont(Font font) {
        super.setLabelFont(font);
        ( (Component) linearTreeView.getLabelRenderer()).setFont(font);
        ( (Component) radialTreeView.getLabelRenderer()).setFont(font);
        ( (Component) hyperTreeView.getLabelRenderer()).setFont(font);
    }

    protected void validatePlots() {
        linearTreeView.getPlot().validate();
        radialTreeView.getPlot().validate();
        hyperTreeView.getPlot().validate();
        hyperTreeView.autoTransform();
    }

    protected void setColors(Map nodesToColors) {
        super.setColors(nodesToColors);
        linearTreeView.setColors(nodesToColors);
        radialTreeView.setColors(nodesToColors);
        hyperTreeView.setColors(nodesToColors);
    }

    public void setLabelsVisible(boolean b) {
        super.setLabelsVisible(b);
        linearTreeView.setLabelsVisible(b);
        radialTreeView.setLabelsVisible(b);
        hyperTreeView.setLabelsVisible(b);
    }
}
