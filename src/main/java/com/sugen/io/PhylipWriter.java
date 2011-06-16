package com.sugen.io;

import java.io.IOException;
import java.io.StringWriter;

import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreeModel;

import com.sugen.util.ClusterTreeNode;
import com.sugen.util.TreeDataModel;

/**
 * Write Phylip treefiles.
 *
 * @author Jonathan Bingham
 */
public class PhylipWriter
    extends TreeWriter {
    public PhylipWriter() {}

    public Object write(Object obj) throws IOException {
        try {
            DefaultMutableTreeNode root;
            if(obj instanceof TreeModel)
                root = (DefaultMutableTreeNode)((TreeModel)obj).getRoot();
            else if(obj instanceof TreeDataModel)
                root = ((TreeDataModel)obj).getRoot();
            else
                throw new ClassCastException("Tree must be TreeModel or TreeDataModel: "
                    + (obj == null ? "null" : String.valueOf(obj.getClass())));
            writeTree(root);
            writer.write(";\n");
        }
        finally {
            writer.flush();
            writer.close();
        }
        if(writer instanceof StringWriter)
            return writer.toString();
        else
            return null;
    }

    /**
     * Recursively write tree nodes.
     */
    protected void writeTree(DefaultMutableTreeNode node) throws IOException {
        if(!node.isLeaf())
            writer.write("(\n");

        //Children
        for(int i = 0, numChildren = node.getChildCount(); i < numChildren; i++) {
            writeTree((ClusterTreeNode)node.getChildAt(i));
            if(i < numChildren - 1)
                writer.write(",\n");
        }
        if(!node.isLeaf())
            writer.write(")\n");

        //Label
        if(!node.isRoot() && node.getUserObject() != null)
            writer.write(node.toString());

        //Branch length
        if(!node.isRoot() && node instanceof ClusterTreeNode) {
            writer.write(":");
            ClusterTreeNode branch = (ClusterTreeNode)node;
            double length = branch.getBranchLength();
            writer.write(String.valueOf(length));
            if(branch.getBootstrapReplicates() != 0)
                writer.write("[" + branch.getBootstrapReplicates() + "]");
        }
    }
}
