package com.sugen.util;

import java.util.*;

/**
 * Simple implementation of a DataModel based on an ArrayList.
 * @author Jonathan Bingham
 */
public class DefaultDataModel
    implements DataModel {
    protected List data = new ArrayList();

    public void add(Object item) {
        data.add(item);
    }

    public int size() {
        return data.size();
    }

    public Object get(int i) {
        return data.get(i);
    }

    public int indexOf(Object obj) {
        return data.indexOf(obj);
    }

    public Iterator iterator() {
        return data.iterator();
    }
}
