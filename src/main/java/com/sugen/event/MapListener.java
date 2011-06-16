package com.sugen.event;

/**
 * For listening to changes to a Collection.
 *
 * @author Jonathan Bingham
 */
public interface MapListener
    extends UtilListener { //java.util.EventListener
    public void mapChanged(MapEvent e);

    public void mapPut(MapEvent e);

    public void mapRemoved(MapEvent e);
}
