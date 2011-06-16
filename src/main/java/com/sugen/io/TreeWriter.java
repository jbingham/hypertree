package com.sugen.io;

import javax.swing.tree.TreeModel;

/**
 * Write a Tree.
 *
 * @author Jonathan Bingham
 */
abstract public class TreeWriter
    extends AbstractWriter {
    /**
     * Class of Objects written by this writer. The write(Object) method
     * will write Objects of this Class type.
     * @return javax.swing.tree.TreeModel.class
     */
    public Class getOutputClass() {
        return TreeModel.class;
    }
}
