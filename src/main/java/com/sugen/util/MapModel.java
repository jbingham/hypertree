package com.sugen.util;

import java.io.Serializable;
import java.util.*;
import javax.swing.event.EventListenerList;

import com.sugen.event.MapEvent;
import com.sugen.event.MapListener;
import com.sugen.event.UtilListener;

/**
 * A general Map model with listeners.
 *
 * <p>By default, a MapModel is backed by a HashMap, but you can use
 * any Map, such as a TreeMap, depending on the behavior
 * desired.
 *
 * @author Jonathan Bingham
 * @see CollectionModel
 */
public class MapModel
    implements Map, Serializable {
    /** @serial */
    protected Map map;

    public MapModel() {
        this(new HashMap());
    }

    public MapModel(Map m) {
        map = m;
    }

    /**
     * If the returned map is modified, no events will be fired.
     */
    public Map getMap() {
        return map;
    }

    public void setMap(Map c) {
        if(c == null)
            throw new IllegalArgumentException("Map cannot be null");
        map = c;
        fireChanged(c, null);
    }

    public boolean isEmpty() {
        return map.isEmpty();
    }

    public Set keySet() {
        return map.keySet();
    }

    public int size() {
        return map.size();
    }

    public boolean containsKey(Object obj) {
        return map.containsKey(obj);
    }

    public boolean containsValue(Object obj) {
        return map.containsValue(obj);
    }

    public void clear() {
        map.clear();
        fireChanged(this, null);
    }

    public Set entrySet() {
        return map.entrySet();
    }

    public Object get(Object key) {
        return map.get(key);
    }

    public Object put(Object key, Object value) {
        Object retval = map.put(key, value);
        firePut(this, key);
        return retval;
    }

    public void putAll(Map m) {
        if(m != null) {
            map.putAll(m);
            firePut(this, m);
        }
    }

    public Object remove(Object key) {
        Object retval = map.remove(key);
        fireRemoved(this, key);
        return retval;
    }

    public Collection values() {
        return map.values();
    }

//******************* Event dispatching **********************//

    /** @serial */
    protected EventListenerList listenerList = new EventListenerList();

    public void addListener(UtilListener l) {
        listenerList.add(UtilListener.class, l);
    }

    public void removeListener(UtilListener l) {
        listenerList.remove(UtilListener.class, l);
    }

    protected void firePut(Object source, Object key) {
        Object[] listeners = listenerList.getListenerList();
        MapEvent e = null;

        for(int i = listeners.length - 2; i >= 0; i -= 2) {
            if(listeners[i] == UtilListener.class) {
                if(e == null)
                    e = new MapEvent(this, key);
                firePut((UtilListener)listeners[i + 1], e);
            }
        }
    }

    protected void fireRemoved(Object source, Object key) {
        Object[] listeners = listenerList.getListenerList();
        MapEvent e = null;

        for(int i = listeners.length - 2; i >= 0; i -= 2) {
            if(listeners[i] == UtilListener.class) {
                if(e == null)
                    e = new MapEvent(this, key);
                fireRemoved((UtilListener)listeners[i + 1], e);
            }
        }
    }

    protected void fireChanged(Object source, Object key) {
        Object[] listeners = listenerList.getListenerList();
        MapEvent e = null;

        for(int i = listeners.length - 2; i >= 0; i -= 2) {
            if(listeners[i] == UtilListener.class) {
                if(e == null)
                    e = new MapEvent(this, null);
                fireChanged((UtilListener)listeners[i + 1], e);
            }
        }
    }

    /** Overload to make the model call any method you like. */
    protected void firePut(UtilListener l, MapEvent e) {
        if(l instanceof MapListener)
            ((MapListener)l).mapPut(e);
    }

    /** Overload to make the model call any method you like. */
    protected void fireRemoved(UtilListener l, MapEvent e) {
        if(l instanceof MapListener)
            ((MapListener)l).mapRemoved(e);
    }

    /** Overload to make the model call any method you like. */
    protected void fireChanged(UtilListener l, MapEvent e) {
        if(l instanceof MapListener)
            ((MapListener)l).mapChanged(e);
    }
}
