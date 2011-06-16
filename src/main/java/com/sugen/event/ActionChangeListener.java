package com.sugen.event;

import javax.swing.*;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.Serializable;
import java.util.Enumeration;

import com.sugen.gui.ActionConstants;

/**
 * Updates menu and tool buttons when an Action undergoes a PropertyChangeEvent.
 * In Swing a class like this is created, behind the scenes, when
 * an Action is added to a JToolBar or JMenuBar; this version is designed to
 * take advantage of additional Action properties specific to the
 * ActionConstants interface.
 *
 * @see com.sugen.gui.ActionConstants
 * @author Jonathan Bingham
 */
public class ActionChangeListener
    implements PropertyChangeListener, Serializable {
    /** @serial */
    protected AbstractButton button;
    /** @serial */
    protected ButtonGroup group;

    public ActionChangeListener(AbstractButton button) {
        this.button = button;
    }

    public ActionChangeListener(ButtonGroup group) {
        this.group = group;
    }

    public void propertyChange(PropertyChangeEvent e) {
        String propertyName = e.getPropertyName();
        //System.out.println(propertyName);
        if(e.getPropertyName().equals(Action.NAME)) {
            if(button != null) {
                String text = (String)e.getNewValue();
                button.setText(text);
            }
        }
        //For toggle buttons
        else if(propertyName.equals(ActionConstants.KEY_SELECTED_STATE)) {
            Object state = e.getNewValue();
            if(button != null)
                button.setSelected(button.getText().equals(state)
                                   || Boolean.TRUE.equals(state));
            else {
                Enumeration en = group.getElements();
                while(en.hasMoreElements()) {
                    AbstractButton ab = (AbstractButton)en.nextElement();
                    if(state == null)
                        ab.setSelected(false);
                    else if(state.equals(ab.getActionCommand()))
                        ab.setSelected(!ab.isSelected());
                }
            }
        }
        else if(propertyName.equals("enabled")) {
            Boolean enabledState = (Boolean)e.getNewValue();
            if(button != null)
                button.setEnabled(enabledState.booleanValue());
            else {
                Enumeration en = group.getElements();
                while(en.hasMoreElements()) {
                    AbstractButton ab = (AbstractButton)en.nextElement();
                    ab.setEnabled(enabledState.booleanValue());
                }
            }
        }
        else if(e.getPropertyName().equals(Action.SMALL_ICON)) {
            if(button != null) {
                Icon icon = (Icon)e.getNewValue();
                button.setIcon(icon);
                button.invalidate();
                button.repaint();
            }
        }
    }
}
