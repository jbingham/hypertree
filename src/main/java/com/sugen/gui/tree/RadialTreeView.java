package com.sugen.gui.tree;

import javax.swing.tree.*;
import java.awt.*;
import java.awt.geom.Point2D;

import com.sugen.gui.plot.*;

/**
 * View a radial tree plot.
 *
 * @author Jonathan Bingham
 */
public class RadialTreeView
    extends PlotView {

    public RadialTreeView() {
        this(new RadialTreePlot());
    }

    public RadialTreeView(RadialTreePlot plot) {
        super(plot);
        setLeftLabels(true);
        linearTransform.setScaleProportional(true);
    }

    /**
     * Decide whether the label goes to the left of the node rather than the
     * default right.
     */
    protected boolean isLabelLeft(int key, Point2D p) {
        TreeNode node = (TreeNode)plot.getDataModel().get(key);
        TreeNode parent = node.getParent();
        if(parent == null)
            return false;
        else {
            int parentKey = plot.getDataModel().indexOf(parent);
            Point2D q = linearTransform.get(parentKey, null);
            return p.getX() < q.getX();
        }
    }

    /** @serial */
    protected boolean isTreeLinear;

    /**
     * Actually, paint a line from the point to its parent.
     */
    protected void paintPoint(Graphics2D g, int key, Point2D p) {
        //Find parent node's location on screen
        TreeNode node = (TreeNode)plot.getDataModel().get(key);
        TreeNode parent = node.getParent();
        int parentKey = plot.getDataModel().indexOf(parent);
        if(parentKey < 0)
            return;

        Point2D q = linearTransform.get(parentKey, null);

        //Set branch color
        Color originalColor = g.getColor();
        Color branchColor = getColor(node);
        if(branchColor != null)
            g.setColor(branchColor);

        g.drawLine((int)Math.round(p.getX()), (int)Math.round(p.getY()), 
                   (int)Math.round(q.getX()), (int)Math.round(q.getY()));
        g.setColor(originalColor);
    }
}
