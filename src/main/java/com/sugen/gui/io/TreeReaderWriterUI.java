package com.sugen.gui.io;

import java.io.*;
import javax.swing.tree.*;

import com.sugen.io.*;
import com.sugen.util.TreeDataModel;

/**
 * Interactively read/write trees from file.
 * @see com.sugen.io.TreeReader
 * @see com.sugen.io.TreeWriter
 *
 * @author Jonathan Bingham
 */
public class TreeReaderWriterUI
    extends ReaderWriterUI {

    public TreeReaderWriterUI() {
        reader.setPrefix("reader.tree");
        writer.setPrefix("writer.tree");
        reader.setReader(null);
    }

    public Object open(Object reader, File file) throws IOException {
        TreeReader pr = (TreeReader)reader;
        pr.setInput(file);
        if(pr.hasNext()) {
            TreeModel tree = (TreeModel)pr.next();
            TreeDataModel dataModel = new TreeDataModel();
            dataModel.setRoot((DefaultMutableTreeNode)tree.getRoot());
            return dataModel;
        }
        else
            return null;
    }

    public void save(Object writer, Object data, File file) throws IOException {
        TreeWriter pw = (TreeWriter)writer;
        pw.setOutput(file);
        pw.write(data);
    }
}
