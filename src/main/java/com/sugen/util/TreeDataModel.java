package com.sugen.util;

import java.util.*;
import javax.swing.tree.*;

/**
 * Stores a tree in an array in breadth first order, for efficient access.
 *
 * @author Jonathan Bingham
 */
public class TreeDataModel extends DefaultDataModel {
    protected DefaultMutableTreeNode root;

    public DefaultMutableTreeNode getRoot() {
        return root;
    }

    public void setRoot(DefaultMutableTreeNode root) { //, boolean isBreadthFirst)
        this.root = root;
        data.clear();

        Enumeration en = root.breadthFirstEnumeration();
        //Enumeration enum = isBreadthFirst ? root.breadthFirstEnumeration()
        //                   : root.depthFirstEnumeration();
        while(en.hasMoreElements()) {
            DefaultMutableTreeNode node =
                (DefaultMutableTreeNode)en.nextElement();
            data.add(node);
        }
    }
}
