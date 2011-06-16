package com.sugen.gui;

/**
 *
 * @author Jonathan Bingham
 */
public interface Closeable {
    /**
     * Allows implementing class to veto closing.
     * @return boolean
     */
    public boolean canClose();

    public void close();
}
