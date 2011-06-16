package com.sugen.event;

/**
 * For listening to changes to a Collection.
 * 
 * @author Jonathan Bingham
 */
public interface CollectionListener extends UtilListener {
	public void collectionChanged(CollectionEvent e);
	public void collectionAdded(CollectionEvent e);
	public void collectionRemoved(CollectionEvent e);
}
