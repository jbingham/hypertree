package com.sugen.event;

import java.util.Collection;
import com.sugen.util.CollectionModel;

/**
 * For changes to a Collection.
 *
 * @author Jonathan Bingham
 */
public class CollectionEvent
    extends java.util.EventObject {
    /** @serial */
    protected Object object;
    /** @serial */
    protected Collection collection;

    /**
     * @param obj The single element in the source that has changed
     */
    public CollectionEvent(Collection source, Object obj) {
        super(source);
        object = obj;
    }

    /**
     * @param changes All elements in the source that have changed
     */
    public CollectionEvent(Collection source, Collection changes) {
        super(source);
        collection = changes;
    }

    /**
     * The single element that has changed.
     */
    public Object getChanged() {
        return object;
    }

    /**
     * All elements that have changed.
     */
    public Collection getAllChanged() {
        return collection;
    }

    /**
     * The source Collection. If the event source is a CollectionModel,
     * returns the CollectionModel's underlying Collection.
     */
    public Collection getCollection() {
        if (getSource() instanceof CollectionModel)
            return ( (CollectionModel) getSource()).getCollection();
        else
            return (Collection) getSource();
    }

    /**
     * The event source as a CollectionModel.
     */
    public CollectionModel getModel() {
        return (CollectionModel) getSource();
    }

    /**
     * Were multiple elements changed?.
     */
    public boolean isMultiple() {
        return (collection != null);
    }
}
