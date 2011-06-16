package com.sugen.gui;

import java.awt.datatransfer.Transferable;
import java.beans.*;
import java.io.Serializable;

import javax.swing.Action;
import javax.swing.undo.UndoManager;
import javax.swing.undo.UndoableEdit;

/**
 * Trivial implementation of all methods for an Editor that
 * supports Undo and Redo only.
 *
 * @author Jonathan Bingham
 */
public class AbstractEditor
    implements Editor, Serializable {
    /**
     * Note that UndoableEdits may choose not to support Serialization.
     * In such cases, the UndoManager should be reinitialized upon
     * deserialization.
     *
     * @serial
     */
    protected UndoManager undoManager = new UndoManager();
    /** @serial */
    protected Action[] actions;
    /** @serial */
    protected Action[] popupActions;
    /** @serial */
    protected int availableEdits = 0;

    /**
     * Must be overridden if add'l editing is supported.
     */
    public int getEdits() {
        return availableEdits;
    }

    public void propertyChange(PropertyChangeEvent pce) {
        String name = pce.getPropertyName();
        if(name.equals(AppClipboard.PROPERTY_EDITOR_ACTIONS))
            actions = (Action[])pce.getNewValue();
        else if(name.equals(AppClipboard.PROPERTY_EDITOR_POPUP_ACTIONS))
            popupActions = (Action[])pce.getNewValue();
    }

    public Action[] getActions() {
        return actions;
    }

    public Action[] getPopupActions() {
        return popupActions;
    }

    /*public UndoManager getUndoManager()
      {
     return undoManager;
      }*/

    public void addEdit(UndoableEdit edit) {
        undoManager.addEdit(edit);
        updateAvailableEdits();
    }

    public void discardAllEdits() {
        undoManager.discardAllEdits();
        updateAvailableEdits();
    }

    protected void updateAvailableEdits() {
        //Determine what editor operations are available
        int edits = 0;
        if(undoManager.canUndo())
            edits = edits | UNDO_MASK;
        if(undoManager.canRedo())
            edits = edits | REDO_MASK;

        int oldMask = availableEdits;
        availableEdits = edits;
        if(oldMask != availableEdits)
            propertySupport.firePropertyChange(PROPERTY_ACTIVE_EDITS,
                                               null, //new Integer(oldMask),
                                               new Integer(availableEdits));
    }

    /** Probably no need to override. */
    public void undo() {
        undoManager.undo();
        updateAvailableEdits();
    }

    /** Probably no need to override. */
    public void redo() {
        undoManager.redo();
        updateAvailableEdits();
    }

    /** @return reference to former selected Object. */
    public Transferable cut() {
        return null;
    }

    /** @return reference to currently selected Object. */
    public Transferable copy() {
        return null;
    }

    public void paste(Transferable obj) {
    }

    public void delete() {
    }

    /** @serial */
    protected PropertyChangeSupport propertySupport =
        new PropertyChangeSupport(this);

    public synchronized void addPropertyChangeListener(PropertyChangeListener
        pcl) {
        propertySupport.addPropertyChangeListener(pcl);
    }

    public synchronized void removePropertyChangeListener(
        PropertyChangeListener pcl) {
        propertySupport.removePropertyChangeListener(pcl);
    }
}
