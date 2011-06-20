package com.sugen.io;

import java.util.Collection;

import javax.swing.tree.DefaultTreeModel;

import com.sugen.util.ClusterTreeNode;

/**
 * Parse Phylip treefiles.
 *
 * @author Jonathan Bingham
 */
public class PhylipReader extends TreeReader {
    public PhylipReader() {}

    public boolean hasNext() {
        String line = readLine();
        if(line != null)
            unreadLine();
        return(line != null);
    }

    /**
     * @return a TreeModel
     */
    public Object next() {
        //Read a line at a time, appending nodes
        StringBuffer completeTree = new StringBuffer();
        String line;
        while((line = readLine()) != null)
            completeTree.append(line);

        ClusterTreeNode root = new ClusterTreeNode();
        root.setBranchLength(0);
        parseTree(completeTree.toString(), 0, root);

        //No tree...
        if(root.isLeaf() && root.getUserObject() == null
           && root.getBranchLength() == 0)
            return null;
        else
            return new DefaultTreeModel(root);
    }

    /**
     * Parses the subtree starting at the specified node.
     *
     * @param root - parse all children, or all info if the node is a leaf
     * @param input - input String to parse
     * @param index - current index in the input String
     * @return index of character after the end of the subtree
     */
    protected int parseTree(String input, int index, ClusterTreeNode root) {
        //root has children
        if(input.charAt(index) == '(') {
            //parse children of node, recursively parsing each child's subtree
            int retval = index;
            do {
                //add child node to root
                ClusterTreeNode node = new ClusterTreeNode();
                root.insert(node, root.getChildCount());
                retval = parseTree(input, retval + 1, node);
            }
            //recursion terminates when there are no more children
            while(input.charAt(retval) == ',');
            
            //determine branch length of node
            if (input.charAt(retval) == ')' 
            	  && Character.isDigit(input.charAt(retval + 1))) {
            	retval = parseNodeLabel(input, retval + 1, root);
                retval = parseBranchLength(input, retval + 1, root);
            } else if(input.charAt(retval + 1) == ':') {
                retval = parseBranchLength(input, retval + 2, root);
            } else {
                ++retval;
            }

            return retval;
        }
        //root is a leaf
        else {
            return parseLeaf(input, index, root);
        }
    }

    /**
     * @param leaf - Object to be customized with label and branch length
     * @return index of character after leaf
     */
    protected int parseLeaf(String input, int leafLabelStart,
                            ClusterTreeNode leaf) {
        int branchLengthStart = parseLeafLabel(input, leafLabelStart, leaf);
        int end = branchLengthStart;
//        System.err.println("leaf: " + leaf.getUserObject());
        
        //Parse branch length
        if(input.charAt(branchLengthStart) == ':')
            end = parseBranchLength(input, ++branchLengthStart, leaf);
            
        return end;
    }

    /**
     * The leaf label is terminated by a colon, usually.
     * A close parenthesis or comma will also terminate it, so 
     * no colon, comma or parenthesis characters can be in the label.    
     * @param leaf - its userObject is set to the parsed label String
     * @return index of character after leaf
     */
    protected int parseLeafLabel(String input, int start,
                                 ClusterTreeNode leaf) {
        int nextColon = input.indexOf(':', start);
        int nextComma = input.indexOf(',', start);
        int nextParen = input.indexOf(')', start);
        int end = nextColon;
        if(end == -1) {
            if(nextComma != -1 && nextParen != -1)
                end = Math.min(nextComma, nextParen);
            else
                end = Math.max(nextComma, nextParen);
        }
        if(end == -1) {
            System.err.println("eof");
            return input.length();
        }
        String label = input.substring(start, end);
        leaf.setUserObject(label);
        
        return end;
    }

    /**
     * @param leaf - its branchLength is set from parsed input
     * @return index of character after leaf
     */
    protected int parseBranchLength(
    		String input, 
    		int start,
            ClusterTreeNode leaf) {
        int nextComma = input.indexOf(',', start);
        int closeParen = input.indexOf(')', start);
        int end;

        //If both characters were found, use the first occurring
        if(nextComma != -1 && closeParen != -1)
            end = Math.min(nextComma, closeParen);
        //otherwise use the one that was found
        else
            end = Math.max(nextComma, closeParen);
        
        // For bootstrap replicates, skip past the replicate count
        int openBracket = input.indexOf('[', start);
        int closeBracket = input.indexOf(']', start);
        if(openBracket != -1 && openBracket < end && closeBracket != -1) {
            String replicates = input.substring(openBracket + 1, closeBracket);
            leaf.setBootstrapReplicates(Double.parseDouble(replicates));
//            System.err.println("Replicates: " + replicates);
            end = openBracket;
        }

        String lengthString = input.substring(start, end);
        double length = Double.valueOf(lengthString).doubleValue();
        if(length < 0)
        	length = 0;
        leaf.setBranchLength(length);
//        System.err.println("length=" + length);

        if(end == openBracket && closeBracket != -1)
            end = closeBracket + 1;
        
        return end;
    }

    /**
    * @param leaf - its branchLength is set from parsed input
    * @return index of character after leaf
    */
    private int parseNodeLabel(String input, int end, ClusterTreeNode leaf) {
//    	System.err.println("checkBootstrap: "
//    			+ input.substring(Math.max(0, end-40), end)
//    			+ input.charAt(end));
    	
    	int colon = input.indexOf(':', end);
    	if (colon < 0 || colon > input.length())
    		return end;
    	
    	String nodeLabel = input.substring(end, colon);
//    	System.err.println("bootstrap=" + bootstrap);
    	
    	try {
    		leaf.setUserObject(nodeLabel);
    	} catch(NumberFormatException e) {
    		e.printStackTrace();
    	}
    	
		return colon + 1;
	}

	public Collection<?> readAll() {
        throw new UnsupportedOperationException();
    }
}
