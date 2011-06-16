package com.sugen.gui;

import java.awt.datatransfer.Transferable;
import java.beans.PropertyChangeListener;

/**
 * Operations defining editable classes. It is up to implementing classes to use
 * UndoableEdits or some other mechanism.
 * Must fire PROPERTY_ACTIVE_EDITS when the various editing options (undo,
 * redo, cut, copy, paste, delete) become available/unavailable.
 *
 * @author Jonathan Bingham
 */
public interface Editor
    extends AppBean {
    public final static String PROPERTY_ACTIVE_EDITS = "active_edits";

    public final static int UNDO_MASK = 1;
    public final static int REDO_MASK = 2;
    public final static int CUT_MASK = 4;
    public final static int COPY_MASK = 8;
    public final static int PASTE_MASK = 16;
    public final static int DELETE_MASK = 32;

    /** Bit mask describing which edit operations are supported by this editor. */
    public int getEdits();

    public void undo();

    public void redo();

    /** Cut currently selected Object and return it. */
    public Transferable cut();

    /** @return reference to currently selected Object. */
    public Transferable copy();

    public void paste(Transferable obj);

    /** Delete current selection. */
    public void delete();

    public void addPropertyChangeListener(PropertyChangeListener listener);

    public void removePropertyChangeListener(PropertyChangeListener listener);
}
