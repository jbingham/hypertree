package com.sugen.io;

import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreeModel;
import javax.xml.parsers.SAXParserFactory;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import com.sugen.util.ClusterTreeNode;

/** 
 * Read phyloxml format from http://www.phyloxml.org.
 */
public class PhyloXmlReader extends TreeReader {
	private TreeModel tree;
	private boolean isRead;
	
	public PhyloXmlReader() {
//		System.err.println("PhyloXmlReader");
	}

	public boolean hasNext() {
		if (tree == null && !isRead)
			init();		
		return tree != null;
	}

	public Object next() {
		if (tree == null && !isRead)
			init();	
		TreeModel next = tree;
		tree = null;
		return next;
	}
	
	private void init() {
		isRead = true;
		try {
			SAXParserFactory.newInstance().newSAXParser()
				.parse(inputStream, new PhyloHandler());
		} catch (Exception e) {
			e.printStackTrace();
			throw new RuntimeException(e);
		}
	}
	
	class PhyloHandler extends DefaultHandler {
		private int depth = 0;
		private ClusterTreeNode parent;
		private ClusterTreeNode node;
		private boolean isName;
		private boolean isLength;
		private boolean isConfidence;

		public void startElement(String uri, String localName, 
				String qName, Attributes attributes) {
			
			isName = false;
			isLength = false;
			isConfidence = false;
			
			if (qName.equals("phylogeny")) {			
//				System.err.println("phylogeny: ");
				parent = new ClusterTreeNode();
	        	parent.setBranchLength(0);
				tree = new DefaultTreeModel(parent);

			} else if (qName.equals("clade")) {
				++depth;
				if (node != null)
					parent = node;
				
//				System.err.println("clade: " + depth);
				
		        node = new ClusterTreeNode();
		        parent.add(node);
		        
			} else if (qName.equals("name")) {
				isName = true;
				
			} else if (qName.equals("branch_length")) {
				isLength = true;
				
			} else if (qName.equals("confidence")) {
				isConfidence = true;
			}
		}
		
		public void endElement(String uri, String localName, String qName) {	
			isName = false;
			isLength = false;
			isConfidence = false;

			if (qName.equals("clade")) {
				--depth;
				node = parent;
				parent = (ClusterTreeNode)node.getParent();
			} 
		}
		
		public void characters(char ch[], int start, int length)
        	  throws SAXException {
			if (isName) {
				String s = new String(ch, start, length).trim();
//				System.err.println("name=" + s);
				node.setUserObject(s);
			} else if (isLength) {
				String s = new String(ch, start, length).trim();
//				System.err.println("branch_length=" + s);
				if (s.length() > 0)
					node.setBranchLength(Double.parseDouble(s));
			} else if (isConfidence) {
				String s = new String(ch, start, length).trim();
//				System.err.println("confidence=" + s);
				if (s.length() > 0)
					node.setBootstrapReplicates(Double.parseDouble(s));
			}
		}
	}
}
