package com.sugen.event;

/**
 * For listening to changes to a Collection of selected items.
 * 
 * @author Jonathan Bingham
 */
public interface SelectionListener extends UtilListener { 
	public void selectionChanged(CollectionEvent e);
	public void selectionAdded(CollectionEvent e);
	public void selectionRemoved(CollectionEvent e);
}
