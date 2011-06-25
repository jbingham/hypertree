package com.sugen.util;

import java.awt.Color;
import java.util.*;
import javax.swing.tree.*;

import com.sugen.gui.Colorable;

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
        //Enumeration en = isBreadthFirst ? root.breadthFirstEnumeration()
        //                   : root.depthFirstEnumeration();
        while(en.hasMoreElements()) {
            data.add(en.nextElement());
        }
    }
    
    /**
     * Get map of colors from the clades.
     */
    public Map<TreeNode, Color> getColors() {
    	Map<TreeNode, Color> colors = new HashMap<TreeNode, Color>();
    	for (Object o : data) {
    		if (o instanceof Colorable)
    			colors.put((TreeNode)o,((Colorable)o).getColor());
    	}
    	return colors;
    }
    
    /**
     * Set clade colors from the map.
     */
    public void setColors(Map<TreeNode, Color> colors) {
    	if (colors == null || colors.isEmpty())
    		return;
    	
    	for (Object o : data) {
    		if (o instanceof Colorable) {
    			((Colorable)o).setColor(colors.get(o));
    		}
    	}
    }
}
