package com.sugen.gui;

import javax.swing.*;
import java.awt.Event;
import java.awt.event.KeyEvent;
import java.awt.Panel;
import java.awt.event.ActionEvent;
import java.awt.datatransfer.*;
import java.beans.*;
import java.util.*;

/**
 * A general application interface to the system clipboard,
 * permitting Editable Objects to cut and copy to it and to
 * paste from it. The clipboard listens for the PROPERTY_FOCUSED_COMPONENT,
 * and if fired, treats that Editor (if an Editor)
 * as the one to which to dispatch all calls for
 * editing. So the AppClipboard class is a central event spot. Add it as a
 * PropertyChangeListener to all Editable objects in your application, let it know
 * which Editable object has focus or is active (by firing PROPERTY_FOCUSED_COMPONENT
 * on an Editable object),
 * and off you go!
 * <p>
 * <pre>
 AppClipboard clipboard = new AppClipboard();<br>
 Editable editableComponent1 = new MyEditableComponent1();<br>
 editableComponent1.addPropertyChangeListener(clipboard);<br>
 Editable editableComponent2 = new MyEditableComponent2();<br>
 editableComponent2.addPropertyChangeListener(clipboard);<br>
//Now the clipboard will dispatch events to whichever editable<br>
//component has most recently fired PROPERTY_FOCUSED_COMPONENT<br>
 </pre>
 *
 * @author Jonathan Bingham
 */
public class AppClipboard
    implements Editor, AppBean, ClipboardOwner {
    public final static String PROPERTY_CLIPBOARD_CONTENTS =
        "clipboard_contents";
    public final static String PROPERTY_EDITOR_ACTIONS = "editorActions";
    public final static String PROPERTY_EDITOR_POPUP_ACTIONS =
        "editorPopupActions";

    /** From which stuff is copied, cut and deleted, and to which it is pasted. */
    protected Editor target;
    protected Clipboard clipboard;
    protected boolean hasOwnership;

    protected int mask = UNDO_MASK | REDO_MASK | CUT_MASK
        | COPY_MASK | PASTE_MASK | DELETE_MASK;
    protected Collection actions;

    public AppClipboard() {
        Panel panel = new Panel();
        clipboard = panel.getToolkit().getSystemClipboard();
        setActions(mask);
    }

    public int getEdits() {
        return mask;
    }

    /**
     * Describes what Editable operations are currently available.
     */
    public void setEdits(int i) {
        if(mask == i)
            return;

        //int oldMask = mask;
        mask = i;
        undoAction.setEnabled((mask & UNDO_MASK) == UNDO_MASK
                              && target != null);
        redoAction.setEnabled((mask & REDO_MASK) == REDO_MASK
                              && target != null);
        cutAction.setEnabled((mask & CUT_MASK) == CUT_MASK
                             && target != null);
        copyAction.setEnabled((mask & COPY_MASK) == COPY_MASK
                              && target != null);
        pasteAction.setEnabled((mask & PASTE_MASK) == PASTE_MASK
                               && target != null && hasOwnership);
        deleteAction.setEnabled((mask & DELETE_MASK) == DELETE_MASK
                                && target != null);

        //propertySupport.firePropertyChange(PROPERTY_ACTIVE_EDITS,
        //								   new Integer(oldMask), new Integer(mask));
    }

    public void lostOwnership(Clipboard clipboard, Transferable obj) {
        hasOwnership = false;
        pasteAction.setEnabled(false);

        propertySupport.firePropertyChange(PROPERTY_CLIPBOARD_CONTENTS, "", null);
    }

    public Transferable getClipboardContents() {
        if(hasOwnership)
            return clipboard.getContents(this);
        else
            return null;
    }

    /**
     * Called by copy and cut.
     */
    public void setClipboardContents(Transferable obj) {
        if(obj == null)
            return;

        Object oldObj = clipboard.getContents(this);

        hasOwnership = true;
        clipboard.setContents(obj, this);
        pasteAction.setEnabled((mask & PASTE_MASK) == PASTE_MASK && target != null);

        propertySupport.firePropertyChange(PROPERTY_CLIPBOARD_CONTENTS,
                                           oldObj, obj);
    }

    /** From which stuff is copied, cut and deleted, and to which it is pasted. */
    public void setTarget(Editor editor) {
        boolean targetChanged = editor != target;
        if(targetChanged) {
            if(target != null)
                target.removePropertyChangeListener(this);
            if(editor != null) {
                editor.addPropertyChangeListener(this);

                //tell editor what popup Actions are here, less undo/redo
                PropertyChangeEvent pce =
                    new PropertyChangeEvent(this, PROPERTY_EDITOR_POPUP_ACTIONS,
                                            null, getPopupActions());
                editor.propertyChange(pce);

                //tell editor what Actions are here, less undo/redo
                pce = new PropertyChangeEvent(this, PROPERTY_EDITOR_ACTIONS,
                                              null, getActions());
                editor.propertyChange(pce);
            }
        }

        //Editor oldTarget = target;
        target = editor;

        if(target != null)
            setEdits(target.getEdits());
        else
            setEdits(0);

        //propertySupport.firePropertyChange(PROPERTY_TARGET_EDITOR, oldTarget, target);
    }

    /** From which stuff is copied, cut and deleted, and to which it is pasted. */
    public Editor getTarget() {
        return target;
    }

    /*
     * Set available Actions based on the given edits mask.
     */
    public void setActions(int mask) {
        actions = new ArrayList(6);
        if((mask & UNDO_MASK) == UNDO_MASK)
            actions.add(undoAction);
        if((mask & REDO_MASK) == REDO_MASK)
            actions.add(redoAction);
        if((mask & CUT_MASK) == CUT_MASK)
            actions.add(cutAction);
        if((mask & COPY_MASK) == COPY_MASK)
            actions.add(copyAction);
        if((mask & PASTE_MASK) == PASTE_MASK)
            actions.add(pasteAction);
        if((mask & DELETE_MASK) == DELETE_MASK)
            actions.add(deleteAction);
    }

    /**
     * Get Actions for all supported Editable operations.
     */
    public Action[] getActions() {
        //Add a separator after undo/redo if there are more actions
        boolean needsSeparator = actions.size() > 2
            && (actions.contains(undoAction) || actions.contains(redoAction));

        if(needsSeparator) {
            if(actions.contains(undoAction) && !actions.contains(redoAction))
                undoAction.putValue(KEY_SEPARATOR_AFTER, Boolean.TRUE);
            else
                redoAction.putValue(KEY_SEPARATOR_AFTER, Boolean.TRUE);
        }
        Action[] retval = new Action[actions.size()];
        Iterator iterator = actions.iterator();
        int i = 0;
        while(iterator.hasNext())
            retval[i++] = (Action)iterator.next();
        //System.out.println(retval.length);
        return retval;
    }

    /**
     * By convention, Undo and Redo don't go in popup menus.
     */
    public Action[] getPopupActions() {
        int numActions = actions.size();
        if(actions.contains(undoAction))
            --numActions;
        if(actions.contains(redoAction))
            --numActions;
        Action[] retval = new Action[numActions];
        Iterator iterator = actions.iterator();
        int i = 0;
        while(iterator.hasNext()) {
            Action action = (Action)iterator.next();
            if(action != undoAction && action != redoAction)
                retval[i++] = action;
        }
        return retval;
    }

    protected Action undoAction = new AbstractAction("Undo",
        Icons.get("undo24.gif")) {
        {
            putValue(KEY_MENU, EDIT_MENU);
            putValue(KEY_ACCELERATOR,
                     KeyStroke.getKeyStroke('Z', Event.CTRL_MASK));
            setEnabled(false);
        }

        public void actionPerformed(ActionEvent e) {
            undo();
        }
    };

    public Action getUndoAction() {
        return undoAction;
    }

    protected Action redoAction = new AbstractAction("Redo",
        Icons.get("redo24.gif")) {
        {
            putValue(KEY_MENU, EDIT_MENU);
            putValue(KEY_ACCELERATOR,
                     KeyStroke.getKeyStroke('Z',
                                            Event.CTRL_MASK | Event.SHIFT_MASK));
            setEnabled(false);
        }

        public void actionPerformed(ActionEvent e) {
            redo();
        }
    };

    public Action getRedoAction() {
        return redoAction;
    }

    protected Action cutAction = new AbstractAction("Cut",
        Icons.get("cut24.gif")) {
        {
            putValue(KEY_MENU, EDIT_MENU);
            putValue(KEY_ACCELERATOR,
                     KeyStroke.getKeyStroke('X', Event.CTRL_MASK));
            setEnabled(false);
        }

        public void actionPerformed(ActionEvent e) {
            cut();
        }
    };

    public Action getCutAction() {
        return cutAction;
    }

    protected Action copyAction = new AbstractAction("Copy",
        Icons.get("copy24.gif")) {
        {
            putValue(KEY_MENU, EDIT_MENU);
            putValue(KEY_ACCELERATOR,
                     KeyStroke.getKeyStroke('C', Event.CTRL_MASK));
            setEnabled(false);
        }

        public void actionPerformed(ActionEvent e) {
            copy();
        }
    };

    public Action getCopyAction() {
        return copyAction;
    }

    protected Action pasteAction = new AbstractAction("Paste",
        Icons.get("paste24.gif")) {
        {
            putValue(KEY_MENU, EDIT_MENU);
            putValue(KEY_ACCELERATOR,
                     KeyStroke.getKeyStroke('V', Event.CTRL_MASK));
            setEnabled(false);
        }

        public void actionPerformed(ActionEvent e) {
            paste(getClipboardContents());
        }
    };

    public Action getPasteAction() {
        return pasteAction;
    }

    protected Action deleteAction = new AbstractAction("Delete",
        Icons.get("delete24.gif")) {
        {
            putValue(KEY_MENU, EDIT_MENU);
            putValue(KEY_ACCELERATOR,
                     KeyStroke.getKeyStroke(KeyEvent.VK_DELETE, 0));
            setEnabled(false);
        }

        public void actionPerformed(ActionEvent e) {
            delete();
        }
    };

    public Action getDeleteAction() {
        return deleteAction;
    }

    public void undo() {
        if(target != null)
            target.undo();
    }

    public void redo() {
        if(target != null)
            target.redo();
    }

    /** Cut currently selected Object and return it. */
    public Transferable cut() {
        if(target != null)
            setClipboardContents(target.cut());
        return getClipboardContents();
    }

    /** @return reference to currently selected Object. */
    public Transferable copy() {
        if(target != null)
            setClipboardContents(target.copy());
        return getClipboardContents();
    }

    public void paste(Transferable obj) {
        if(target != null && obj != null)
            target.paste(obj);
    }

    /** Delete current selection. */
    public void delete() {
        if(target != null)
            target.delete();
    }

    protected PropertyChangeSupport propertySupport = new PropertyChangeSupport(this);

    public synchronized void addPropertyChangeListener(PropertyChangeListener
        pcl) {
        propertySupport.addPropertyChangeListener(pcl);
    }

    public synchronized void removePropertyChangeListener(
        PropertyChangeListener pcl) {
        propertySupport.removePropertyChangeListener(pcl);
    }

    public void propertyChange(PropertyChangeEvent e) {
        String property = e.getPropertyName();
        if(property.equals(PROPERTY_FOCUSED_BEAN)) {
            Object focused = e.getNewValue();
            if(focused instanceof Editable)
                setTarget(((Editable)focused).getEditor());
            else
                setTarget(null);
            //if(focusedComponent != null)
            //System.err.println(property + focused.getClass().getName());
        }
        else if(property.equals(PROPERTY_ACTIVE_EDITS)) {
            Integer mask = (Integer)e.getNewValue();
            if(mask != null)
                setEdits(mask.intValue());
            //System.err.println(property + this.target.getClass().getName());
        }
    }

    public boolean close() {
        return true;
    }

    public Object selection() {
        if(target != null)
            return target.copy();
        else
            return null;
    }
}
