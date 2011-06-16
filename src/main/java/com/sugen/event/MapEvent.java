package com.sugen.event;

import java.util.Map;
import com.sugen.util.MapModel;

/**
 * For changes to a Collection.
 *
 * @author Jonathan Bingham
 */
public class MapEvent
    extends java.util.EventObject {
    /** @serial */
    protected Object key;
    /** @serial */
    protected Map map;

    /**
     * @param key The single element in the source that has changed
     */
    public MapEvent(Map source, Object key) {
        super(source);
        this.key = key;
    }

    /**
     * @param changes All elements in the source that have changed
     */
    public MapEvent(Map source, Map changes) {
        super(source);
        this.map = changes;
    }

    /**
     * The single element that has changed.
     */
    public Object getChanged() {
        return key;
    }

    /**
     * All elements that have changed.
     */
    public Map getAllChanged() {
        return map;
    }

    /**
     * Were multiple elements changed?.
     */
    public boolean isMultiple() {
        return (map != null);
    }

    /**
     * The source Map. If the event source is a MapModel,
     * returns the MapModel's underlying Map.
     */
    public Map getMap() {
        if (getSource() instanceof MapModel)
            return ( (MapModel) getSource()).getMap();
        else
            return (Map) getSource();
    }

    /**
     * The event source as a MapModel.
     */
    public MapModel getModel() {
        return (MapModel) getSource();
    }
}
