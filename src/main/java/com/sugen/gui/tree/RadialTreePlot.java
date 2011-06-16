package com.sugen.gui.tree;

import java.awt.geom.Point2D;
import java.util.*;
import javax.swing.tree.*;

import com.sugen.util.ClusterTreeNode;
import com.sugen.util.TreeDataModel;
import com.sugen.gui.plot.*;

/**
 * Plot a tree "radially", meaning that branches radiate outward
 * from the root spanning the arc of a circle.
 *
 * @author Jonathan Bingham
 */
public class RadialTreePlot
    extends AbstractTreePlot {

    /**
     * Plot nodes at each successive depth. Bound the tree plot by a
     * circle with radius equal to the maximum depth of the tree.
     * Then divide up the tree like a pie chart, creating
     * wedges according to the relative number of leaves below each node.
     */
    public synchronized void plotTree(TreeDataModel tree) {
        DefaultMutableTreeNode root = tree.getRoot();

        //HACK: multiply by a magic number, chosen to keep trees with great
        //recursive depth from curling back in on themselves. Otherwise,
        //it would be fine to use the results of getDepth(root) directly.
        double depth = getDepth(root) * 1000;

        Map map = new HashMap();
        map.put(root, new Point2D.Double(0, 0)); //root goes at origin

        //Make sure initial arc available is 2*pi, by adding a fudge factor
        Point2D boundingArcStart = new Point2D.Double(depth, 0);
        Point2D boundingArcEnd = new Point2D.Double(depth, -0.0001);
        plotNodesCircularly(root, boundingArcStart, boundingArcEnd, map);
        //System.out.println("plotCircularTree " + root.hashCode());

        //Convert hash to a vector representation
        Enumeration en = root.breadthFirstEnumeration();
        while (en.hasMoreElements()) {
            Object key = en.nextElement();
            collection.add(map.get(key));
        }
    }

    /**
     * Recursively called during breadth-first traversal of tree.
     */
    protected void plotNodesCircularly(DefaultMutableTreeNode node,
                                       Point2D boundingArcStart,
                                       Point2D boundingArcEnd, Map map) {
        Point2D nodePt = (Point2D) map.get(node);
        //Total arc angle available for this node and its children
        double arcSpan =
            Rotation.angle(nodePt, boundingArcStart, boundingArcEnd);
        if (arcSpan < 0)
            arcSpan += 2 * Math.PI;

        //Angle from node to boundingArcStart
        double startAngle =
            Rotation.angle(nodePt, boundingArcStart.getX(),
                           boundingArcStart.getY());
        if (startAngle < 0)
            startAngle += 2 * Math.PI; //No effect, in practice?

        int leafCount = node.getLeafCount();

        //Bounds for current child; a portion of the total arcSpan
        Point2D childArcStart = boundingArcStart;
        Point2D childArcEnd;

        //Descend tree depth-first, divvying up the bounding
        //arc proportionally to the number of leaves each child has
        for (int i = 0; i < node.getChildCount(); i++) {
            DefaultMutableTreeNode child =
                (DefaultMutableTreeNode) node.getChildAt(i);

            //Proportion of arc to allocate to this child
            int childLeafCount = child.getLeafCount();

            //Child branch length
            double branchLength = 1;
            if (child instanceof ClusterTreeNode)
                branchLength = ( (ClusterTreeNode) child).getBranchLength();

            //Arc for the child node
            double childArcSpan = arcSpan * childLeafCount / leafCount;
            double childStartAngle = startAngle + (childArcSpan / 2);

            //Plot child node based on branch length and angle
            Point2D childPt =
                Plot.toCartesian(branchLength, childStartAngle, null);
            //translate based on parent node
            childPt.setLocation(childPt.getX() + nodePt.getX(),
                                childPt.getY() + nodePt.getY());
            map.put(child, childPt);

            //Plot child's children
            //HACK: Rotating like this gradually curls the tree back in on itself
            childArcEnd = Rotation.rotate(childArcStart, nodePt, childArcSpan);
            plotNodesCircularly(child, childArcStart, childArcEnd, map);

            //Set bounds for next child of original node
            startAngle = startAngle + childArcSpan;
            childArcStart = childArcEnd;
        }
    }

    /**
     * Maximum depth in the tree. Determined
     * by summing branch lengths along each tree path, and
     * taking the max.
     */
    protected double getDepth(DefaultMutableTreeNode root) {
        if (root == null)
            return 0;
        //If no branch lengths, depth is accurately given by root.getDepth()
        if (! (root instanceof ClusterTreeNode))
            return root.getDepth();

        //Find the maximum depth by checking all tree paths.
        double retval = 0;
        Map nodeDepth = new HashMap();
        Enumeration depthFirst = root.depthFirstEnumeration();
        while (depthFirst.hasMoreElements()) {
            ClusterTreeNode node = (ClusterTreeNode) depthFirst.nextElement();
            double currentNodeDepth = node.getBranchLength();

            //Add parent's depth
            ClusterTreeNode parent = (ClusterTreeNode) node.getParent();
            Double depth = null;
            if (parent != null)
                depth = (Double) nodeDepth.get(parent);
            if (depth != null)
                currentNodeDepth += depth.doubleValue();

            //Deepest so far
            if (currentNodeDepth > retval)
                retval = currentNodeDepth;

            nodeDepth.put(node, new Double(currentNodeDepth));
        }
        //System.out.println(retval);
        return retval;
    }
}
