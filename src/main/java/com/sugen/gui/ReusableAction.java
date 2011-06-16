package com.sugen.gui;

import java.awt.event.ActionEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.Icon;

/**
 * One Action acts as a switch to multiple Actions. This allows the
 * implementation of context-sensitive Actions. It dispatches events
 * based on the current context, in which the "target" action is the
 * relevant one.
 *
 * @see AppToolBar
 * @see AppMenuBar
 *
 * @author Jonathan Bingham
 */
public class ReusableAction
    extends AbstractAction implements PropertyChangeListener {
    /** @serial */
    protected Action targetAction;

    public ReusableAction(String name) {
        super(name);
    }

    public ReusableAction(String name, Icon smallIcon) {
        super(name, smallIcon);
    }

    /**
     * The Action to which to dispatch events.
     */
    public void setTargetAction(Action action) {
        if(targetAction != null)
            targetAction.removePropertyChangeListener(ReusableAction.this);
        targetAction = action;
        if(targetAction != null) {
            setEnabled(targetAction.isEnabled());
            targetAction.addPropertyChangeListener(ReusableAction.this);
        }
        else
            setEnabled(false);
    }

    public void actionPerformed(ActionEvent e) {
        if(targetAction != null)
            targetAction.actionPerformed(e);
    }

    public void propertyChange(PropertyChangeEvent pce) {
        if(pce.getPropertyName().equals("enabled"))
            setEnabled(pce.getNewValue().equals(Boolean.TRUE));
    }
}
