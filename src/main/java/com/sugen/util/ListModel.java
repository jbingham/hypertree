package com.sugen.util;

import java.util.*;

/**
 * This class implements the java util List API and notifies registered
 * listeners of changes to its contents.
 *
 * @author Jonathan Bingham
 */
public class ListModel
    extends CollectionModel implements List {
    public ListModel() {
        super(new ArrayList());
    }

    public ListModel(List list) {
        super(list);
    }

    /**
     * Throws IllegalArgumentException if the Collection isn't a List.
     */
    public void setCollection(Collection c) {
        if(c instanceof List || c == null)
            super.setCollection(c);
        else
            throw new IllegalArgumentException(
                "ListModel's Collection must be a List");
    }

    public int indexOf(Object obj) {
        return((List)collection).indexOf(obj);
    }

    public int lastIndexOf(Object obj) {
        return((List)collection).lastIndexOf(obj);
    }

    public ListIterator listIterator() {
        return((List)collection).listIterator();
    }

    public ListIterator listIterator(int i) {
        return((List)collection).listIterator(i);
    }

    public List subList(int i, int j) {
        return((List)collection).subList(i, j);
    }

    public Object get(int i) {
        return((List)collection).get(i);
    }

    public Object set(int index, Object element) {
        Object rv = get(index);
        ((List)collection).set(index, element);
        fireChanged(this, element);
        return rv;
    }

    public void add(int index, Object element) {
        ((List)collection).add(index, element);
        fireAdded(this, element);
    }

    public boolean addAll(int index, Collection c) {
        boolean retval = ((List)collection).addAll(index, c);
        if(retval)
            fireAdded(this, c);
        return retval;
    }

    public Object remove(int index) {
        Object retval = ((List)collection).remove(index);
        fireRemoved(this, retval);
        return retval;
    }
}
