package com.sugen.gui;

import javax.swing.*;
import javax.swing.Action;
import javax.swing.JToolBar;
import java.awt.FlowLayout;
import java.awt.Insets;
import java.awt.Component;
import com.sugen.event.ActionChangeListener;

//BUG: events on ToggleButtons aren't passed along.
/**
 * A toolbar integrated with the other App classes.
 *
 * @author Jonathan Bingham
 */
public class AppToolBar
    extends JPanel implements ActionConstants {
    /** @serial */
    protected JToolBar toolBar = new JToolBar();

    /**
     * There's been talk of providing a way of natively doing this in Swing.
     * @serial
     */
    protected Boolean isRollover = null;

    public AppToolBar() {
        setLayout(new FlowLayout(FlowLayout.LEFT, 0, 0));
        add(toolBar);
    }

    /**
     * @param newToolBar if true, create a new JToolBar for all of this
     * bean's actions (and possibly other beans as well)
     */
    public void addBean(AppBean bean, boolean newToolBar) {
        Action[] actions = bean.getActions();

        //toolBar = newToolBar ? new JToolBar() : this.toolBar;
        if(newToolBar)
            toolBar.addSeparator();

        //Add tool buttons
        for(int i = 0; i < actions.length; i++) {
            if(actions[i].getValue(KEY_LOCATION) == VALUE_MENU_ONLY)
                continue;
            addToolButton(actions[i]);
            if(actions[i].getValue(KEY_SEPARATOR_AFTER) == Boolean.TRUE
               ||
               actions[i].getValue(KEY_TOOLBAR_SEPARATOR_AFTER) == Boolean.TRUE)
                toolBar.addSeparator();
        }
        if(toolBar.getComponentCount() > 0)
            add(toolBar);
    }

    /**
     * Adds a button if the action has an icon associated. The button contains
     * no text, only an image with tooltip. This could be changed later, for greater
     * flexibility.
     */
    public AbstractButton addToolButton(Action act) {
        if(act.getValue(Action.SMALL_ICON) == null)
            return null;
        if(act.getValue(KEY_ACTION_TYPE) == VALUE_BUTTON_GROUP) {
            addToolButtonGroup(act);
            return null;
        }
        if(act.getValue(KEY_ACTION_TYPE) == VALUE_TOGGLE_ACTION)
            return addToolToggleButton(act);

        JButton button = toolBar.add(act);
        button.setMargin(new Insets(0, 0, 0, 0));
        if(isRollover != null && isRollover.booleanValue())
            RolloverButton.makeRollover(button);
        button.setIcon((Icon)act.getValue(Action.SMALL_ICON));
        button.setToolTipText((String)act.getValue(Action.NAME));
        button.setText(null); //Icons only
        button.setFocusPainted(false); //Ignored by l&f
        return button;
    }

    public void addSeparator() {
        toolBar.addSeparator();
    }

    public AbstractButton addToolToggleButton(Action act) {
        JToggleButton button = new JToggleButton();
        button.addPropertyChangeListener(new ActionChangeListener(button));
        toolBar.add(button);

        if(isRollover != null && isRollover.booleanValue())
            RolloverButton.makeRollover(button);

        button.setIcon((Icon)act.getValue(Action.SMALL_ICON));
        button.setToolTipText((String)act.getValue(Action.NAME));
        button.setText(null); //Icons only
        button.setFocusPainted(false); //Ignored by l&f
        button.setSelected(act.getValue(KEY_SELECTED_STATE) == Boolean.TRUE);

        return button;
    }

    /**
     * Add a group of mutually exclusive toggle buttons.
     */
    public ButtonGroup addToolButtonGroup(Action act) {
        Icon icons[] = (Icon[])act.getValue(Action.SMALL_ICON);
        String names[] = (String[])act.getValue(Action.NAME);
        ButtonGroup group = new ButtonGroup();
        for(int i = 0; i < names.length; i++) {
            JToggleButton button = new JToggleButton(names[i]);
            if(icons == null || icons[i] == null)
                continue;
            else
                button.setIcon(icons[i]);
            if(isRollover != null && isRollover.booleanValue())
                RolloverButton.makeRollover(button);
            group.add(button);
            button.setSelected(names[i].equals(act.getValue(KEY_SELECTED_STATE)));
            button.setToolTipText(names[i]);
            button.setText(null); //Icons only
            button.setMargin(new Insets(0, 0, 0, 0));
            button.setActionCommand(names[i]);
            button.setFocusPainted(false); //Ignored by l&f
            button.addActionListener(act);
            toolBar.add(button);
        }
        act.addPropertyChangeListener(new ActionChangeListener(group));
        return group;
    }

    /**
     * Override UI default for toolbar button margins.
     */
    public void updateUI() {
        super.updateUI();

        String name = UIManager.getLookAndFeel().getClass().getName();

        Component[] toolbars = getComponents();
        for(int i = 0; i < toolbars.length; i++) {
            if(!(toolbars[i] instanceof JToolBar))
                continue;

            Component[] buttons = ((JToolBar)toolbars[i]).getComponents();
            for(int j = 0; j < buttons.length; j++) {
                if(buttons[j] instanceof AbstractButton) {
                    if(!name.equals(AppMainWindow.MOTIF_LAF))
                        ((AbstractButton)buttons[j]).setMargin(new Insets(1, 1,
                            1, 1));
                    //if(isRollover == null)
                    //	RolloverButton.makeRollover(((AbstractButton)buttons[j]),
                    //		name.equals(AppMainWindow.WINDOWS_LAF));
                }
            }
        }
    }

    public Boolean getRollover() {
        return isRollover;
    }

    public void setRollover(Boolean b) {
        isRollover = b;

        Component[] toolbars = getComponents();
        for(int i = 0; i < toolbars.length; i++) {
            if(!(toolbars[i] instanceof JToolBar))
                continue;

            Component[] buttons = ((JToolBar)toolbars[i]).getComponents();
            for(int j = 0; j < buttons.length; j++)
                if(buttons[j] instanceof AbstractButton)
                    RolloverButton.makeRollover((AbstractButton)buttons[j],
                                                b != null && b.booleanValue());
        }
    }
}
