package com.sugen.io;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.InputStream;

public class MultiTreeReader extends TreeReader {
	private TreeReader treeReader = new PhylipReader();
	
	public MultiTreeReader() {}

	public boolean hasNext() {
		return treeReader.hasNext();
	}

	public Object next() {
		return treeReader.next();
	}
	
	public void setInput(InputStream is) {
		treeReader.setInput(is);
	}
	
	public void setInput(String s) {
		if (s.startsWith("<"))
			treeReader = new PhyloXmlReader();		
		treeReader.setInput(s);
	}

	public void setInput(File file) throws FileNotFoundException {
		if (file.getName().toLowerCase().endsWith("xml"))
			treeReader = new PhyloXmlReader();
		treeReader.setInput(file);
	}
}
