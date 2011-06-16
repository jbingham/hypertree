package com.sugen.io;

import javax.swing.tree.TreeModel;

/**
 * Parse a tree.
 *
 * @author Jonathan Bingham
 */
public abstract class TreeReader extends AbstractReader {
    public TreeReader() {}

    /**
     * Class of Objects created by this reader. The Iterator.next() method
     * will return Objects of this Class type. The readAll() method will
     * return a Collection of Objects of this Class type.
     * @return javax.swing.tree.TreeModel.class
     */
    public Class getInputClass() {
        return TreeModel.class;
    }
}
