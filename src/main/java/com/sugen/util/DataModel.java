package com.sugen.util;

import java.util.Iterator;

/**
 * Provides a uniform way of accessing
 * data from various structures, given that the underlying
 * indexing must be an array for efficiency.
 *
 * <p>
 * Implementations provide array indexing for some possibly non-array data
 * structure. So, eg, a TreeModel might support indexing according
 * to its breadth-first order. This can be statically computed
 * in advance.
 *
 * @author Jonathan Bingham
 * @see com.sugen.gui.plot.Plot
 */
public interface DataModel {
    /**
     * @return number of plottable items in the data model
     */
    public int size();

    /**
     * @return a label, key or other user representation of a plottable Object.
     */
    public Object get(int i);

    /**
     * @return index of a plottable item in the data model
     */
    public int indexOf(Object obj);

    public Iterator iterator();
}
