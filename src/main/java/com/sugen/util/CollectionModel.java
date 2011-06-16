package com.sugen.util;

import java.io.Serializable;
import java.util.*;
import javax.swing.event.EventListenerList;

import com.sugen.event.CollectionEvent;
import com.sugen.event.CollectionListener;
import com.sugen.event.UtilListener;

/**
 * A general model with listeners in the Collections framework. Use it, for
 * example, as an efficient selection model with better performance and greater
 * flexibility than the models built into Swing.
 * 
 * <p>
 * By default, a CollectionModel is backed by a HashSet, but you can use any
 * Collection, such as a SortedSet, depending on the behavior desired.
 * 
 * @see MapModel
 * 
 * @author Jonathan Bingham
 */
public class CollectionModel implements Collection, Serializable {
	/**
	 * @serial
	 */
	protected Collection collection;

	public CollectionModel() {
		this(new HashSet());
	}

	public CollectionModel(Collection c) {
		collection = c;
	}

	/**
	 * If the returned collection is modified, no events will be fired.
	 */
	public Collection getCollection() {
		return collection;
	}

	public void setCollection(Collection c) {
		if (c == this)
			throw new IllegalArgumentException(
					"CollectionModel cannot reference itself");
		collection = c;
		fireChanged(this, null);
	}

	public boolean containsAll(Collection c) {
		return collection.containsAll(c);
	}

	public boolean isEmpty() {
		return collection == null || collection.isEmpty();
	}

	public Iterator iterator() {
		return collection.iterator();
	}

	public int size() {
		return collection.size();
	}

	public Object[] toArray() {
		return collection.toArray();
	}

	public Object[] toArray(Object[] obj) {
		return collection.toArray(obj);
	}

	public boolean contains(Object obj) {
		return collection.contains(obj);
	}

	public void clear() {
		collection.clear();
		fireChanged(this, null);
	}

	public boolean add(Object obj) {
		// if(obj instanceof Collection)
		// throw new Error("Added Collection as an Object");
		boolean retval = collection.add(obj);
		fireAdded(this, obj);
		return retval;
	}

	public boolean addAll(Collection c) {
		boolean retval = collection.addAll(c);
		if (retval)
			fireAdded(this, c);
		return retval;
	}

	public boolean remove(Object obj) {
		boolean retval = collection.remove(obj);
		fireRemoved(this, obj);
		return retval;
	}

	public boolean removeAll(Collection c) {
		boolean retval = collection.removeAll(c);
		if (retval)
			fireRemoved(this, c);
		return retval;
	}

	public boolean retainAll(Collection c) {
		boolean retval = collection.retainAll(c);
		if (retval)
			fireChanged(this, null);
		return retval;
	}

	// ******************* Event dispatching **********************//

	/** @serial */
	protected EventListenerList listenerList = new EventListenerList();

	public void addListener(UtilListener l) {
		listenerList.add(UtilListener.class, l);
	}

	public void removeListener(UtilListener l) {
		listenerList.remove(UtilListener.class, l);
	}

	protected void fireAdded(Object source, Collection c) {
		Object[] listeners = listenerList.getListenerList();
		CollectionEvent e = null;

		for (int i = listeners.length - 2; i >= 0; i -= 2) {
			if (listeners[i] == UtilListener.class) {
				if (e == null)
					e = new CollectionEvent(this, c);
				fireAdded((UtilListener) listeners[i + 1], e);
			}
		}
	}

	protected void fireAdded(Object source, Object key) {
		Object[] listeners = listenerList.getListenerList();
		CollectionEvent e = null;

		for (int i = listeners.length - 2; i >= 0; i -= 2) {
			if (listeners[i] == UtilListener.class) {
				if (e == null)
					e = new CollectionEvent(this, key);
				fireAdded((UtilListener) listeners[i + 1], e);
			}
		}
	}

	protected void fireRemoved(Object source, Object key) {
		Object[] listeners = listenerList.getListenerList();
		CollectionEvent e = null;

		for (int i = listeners.length - 2; i >= 0; i -= 2) {
			if (listeners[i] == UtilListener.class) {
				if (e == null)
					e = new CollectionEvent(this, key);
				fireRemoved((UtilListener) listeners[i + 1], e);
			}
		}
	}

	protected void fireRemoved(Object source, Collection c) {
		Object[] listeners = listenerList.getListenerList();
		CollectionEvent e = null;

		for (int i = listeners.length - 2; i >= 0; i -= 2) {
			if (listeners[i] == UtilListener.class) {
				if (e == null)
					e = new CollectionEvent(this, c);
				fireRemoved((UtilListener) listeners[i + 1], e);
			}
		}
	}

	protected void fireChanged(Object source, Object key) {
		Object[] listeners = listenerList.getListenerList();
		CollectionEvent e = null;

		for (int i = listeners.length - 2; i >= 0; i -= 2) {
			if (listeners[i] == UtilListener.class) {
				if (e == null)
					e = new CollectionEvent(this, key);
				fireChanged((UtilListener) listeners[i + 1], e);
			}
		}
	}

	/** Overload to make the model call any method you like. */
	protected void fireAdded(UtilListener l, CollectionEvent e) {
		if (l instanceof CollectionListener)
			((CollectionListener) l).collectionAdded(e);
	}

	/** Overload to make the model call any method you like. */
	protected void fireRemoved(UtilListener l, CollectionEvent e) {
		if (l instanceof CollectionListener)
			((CollectionListener) l).collectionRemoved(e);
	}

	/** Overload to make the model call any method you like. */
	protected void fireChanged(UtilListener l, CollectionEvent e) {
		if (l instanceof CollectionListener)
			((CollectionListener) l).collectionChanged(e);
	}
}
