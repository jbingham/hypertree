package com.sugen.util;

import javax.swing.tree.*;

/**
 * TreeNode that adds the concept of branch length. 
 * Useful in clustering algorithms like phylogenetic reconstruction.
 *
 * @author Jonathan Bingham
 */
public class ClusterTreeNode extends DefaultMutableTreeNode {
    private double branchLength;
    private int bootstrapReplicates;

    /**
     * Default constructor. Null label, branch length equals one.
     */
    public ClusterTreeNode() {
        this(null, 1, 0);
    }

    /**
     * Constructor. 
     * @param nodeLabel optional label for the node
     * @param branchLength non-negative distance
     */
    public ClusterTreeNode(String nodeLabel, double branchLength) {
        this(nodeLabel, branchLength, 0);
    }

    /**
     * Constructor.
     * @param nodeLabel optional label for the node
     * @param branchLength non-negative distance
     * @param bootstrapReplicates non-negative number of replicates
     */
    public ClusterTreeNode(String nodeLabel, double branchLength, int bootstrapReplicates) {
        super(nodeLabel);
        this.branchLength = branchLength;
        this.bootstrapReplicates = bootstrapReplicates;
    }

	/**
     * Branch length.
     * @return non-negative distance
     */
    public double getBranchLength() {
        return branchLength;
    }

    /**
     * Bootstrap replicates, or zero (0) if there aren't any.
     * @return number of replicates; non-negative
     */
    public int getBootstrapReplicates() {
        return bootstrapReplicates;
    }

    /**
     * Branch length.
     * @param distance non-negative length
     */
    public void setBranchLength(double distance) {
    	if(distance < 0)
    		throw new IllegalArgumentException("Branch length cannot be negative.");
        branchLength = distance;
    }

    /**
     * Bootstrap replicates, or zero (0) if there aren't any.
     * @param replicates non-negative value
     */
    public void setBootstrapReplicates(int replicates) {
    	if(replicates < 0)
    		throw new IllegalArgumentException("Bootstrap replicates cannot be negative.");
        bootstrapReplicates = replicates;
    }

    /**
     * Overridden to display bootstrap replicates for non-leaf nodes.
     */
    public String toString() {
        if(super.toString() == null && bootstrapReplicates != 0)
            return String.valueOf(bootstrapReplicates);
        else
            return super.toString();
    }
}
