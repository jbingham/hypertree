package com.sugen.gui.tree;

import java.awt.geom.Point2D;
import java.util.*;
import javax.swing.tree.*;

import com.sugen.util.Clade;
import com.sugen.util.TreeDataModel;

/**
 * Plot a tree "linearly", meaning that all branches extend
 * horizontally to the right.
 *
 * @author Jonathan Bingham
 */
public class LinearTreePlot
    extends AbstractTreePlot {

    public synchronized void plotTree(TreeDataModel dataModel) {
        DefaultMutableTreeNode root = (dataModel).getRoot();

        //Store everything in a hashtable while we're computing
        Map map = new HashMap(collection.size() * 4 / 3);

        //Traverse postorder to set vertical coordinates of nodes
        Enumeration postorder = root.postorderEnumeration();
        int numLeaves = 0;
        double y;
        while (postorder.hasMoreElements()) {
            TreeNode node = (TreeNode) postorder.nextElement();
            //Node's a leaf; equally space all leaves vertically
            if (node.isLeaf()) {
                y = numLeaves;
                ++numLeaves;
            }
            //Node has children; center vertically relative to them
            //(children have all been vertically positioned already)
            else {
                double firstChildY =
                    ( (Point2D) map.get(node.getChildAt(0))).getY();
                double lastChildY =
                    ( (Point2D) map.get(node.getChildAt(node.getChildCount() -
                    1))).getY();
                y = firstChildY + (lastChildY - firstChildY) / 2;
            }
            //Store node away, though we'll modify the x-coord later
            map.put(node, new Point2D.Double(0, y));
        }

        //Then traverse preorder, setting branch lengths
        Enumeration preorder = root.preorderEnumeration();
        while (preorder.hasMoreElements()) {
            TreeNode node = (TreeNode) preorder.nextElement();
            TreeNode parent = node.getParent();
            Point2D p = (Point2D) map.get(node);
            double x;
            //Root goes at x origin
            if (parent == null)
                x = 0;
            //All other nodes go to the right of their parents
            else
                x = ( (Point2D) map.get(parent)).getX();

            //Shift right by the branch length
            if (node instanceof Clade)
                x += ( (Clade) node).getBranchLength();
            else
                x += 1;
            p.setLocation(x, p.getY());
        }

        //Convert hashtable to a vector representation
        Enumeration en = root.breadthFirstEnumeration();
        while (en.hasMoreElements()) {
            Object key = en.nextElement();
            collection.add(map.get(key));
        }
    }
}
