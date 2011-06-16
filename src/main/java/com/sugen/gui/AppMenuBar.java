package com.sugen.gui;

import javax.swing.*;
import java.util.*;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import com.sugen.gui.AppBean;
import com.sugen.event.ActionChangeListener;

//BUG: events on ToggleButtons aren't passed along.
/**
 * Actions are added to their preferred menu.
 * Menus may be defined as context-dependent, with contents dynamically
 * altered at runtime.
 *
 * @see AppMainWindow
 * @author Jonathan Bingham
 */
public class AppMenuBar
    extends JMenuBar implements PropertyChangeListener, ActionConstants {
    /** Keys are menu names, values are JMenus.
     * @serial  */
    protected Map menus = new HashMap();
    /** For actions that are part of button groups.
     * @serial */
    protected Map actionToGroup = new HashMap();
    /** Index of button group in menu.
     * @serial */
    protected String KEY_INDEX = "index_in_menu";
    /** For spacing.
     * @serial  */
    protected Icon defaultIcon;

    /** Keys are menu names, values are ContextDependencies.
     * @serial*/
    protected Map contextDependencies;
    //protected Collection reusableActions;

    public AppMenuBar() {
        setDefaultIcon(Icons.get("emptyIcon24.gif"));

        JMenu fileMenu = new JMenu(FILE_MENU);
        JMenu editMenu = new JMenu(EDIT_MENU);
        JMenu viewMenu = new JMenu(VIEW_MENU);
        JMenu helpMenu = new JMenu(HELP_MENU);

        add(fileMenu);
        add(editMenu);
        add(viewMenu);
        add(helpMenu);

        //Keep hash of menus/names
        menus.put(FILE_MENU, fileMenu);
        menus.put(EDIT_MENU, editMenu);
        menus.put(VIEW_MENU, viewMenu);
        menus.put(HELP_MENU, helpMenu);
    }

    /** For spacing purposes. */
    public void setDefaultIcon(Icon icon) {
        defaultIcon = icon;
    }

    /** For spacing purposes. */
    public Icon getDefaultIcon() {
        return defaultIcon;
    }

    /**
     * @param addSeparator - precede Actions for the current bean with a
     * separator in each menu in which Actions appear.
     */
    public void addBean(AppBean bean, boolean addSeparator) {
        Action[] actions = bean.getActions();

        //Stick a separator in any existing menus that are going to have items
        //from the new bean
        Iterator iterator = menus.keySet().iterator();
        while(iterator.hasNext()) {
            boolean hasMenuItem = false;
            Object menuName = iterator.next();
            JMenu menu = (JMenu)menus.get(menuName);
            if(menu.getItemCount() == 0)
                continue; //No separator needed if nothing in the menu
            for(int i = 0; i < actions.length; i++) {
                if(menuName.equals(actions[i].getValue(KEY_MENU)))
                    hasMenuItem = true;
            }
            if(hasMenuItem && addSeparator
               && (contextDependencies == null
                   || !contextDependencies.containsKey(menuName)))
                menu.addSeparator();
        }

        //Create controls for all Actions
        for(int i = 0; i < actions.length; i++) {
            //Don't add context-dependent items until needed
            Object menuName = actions[i].getValue(KEY_MENU);
            if(contextDependencies == null
               || !contextDependencies.containsKey(menuName))
                add(actions[i]);
        }
    }

    public JMenuItem add(Action action) {
        JMenuItem retval = null;
        Object actionType = action.getValue(KEY_ACTION_TYPE);
        //System.out.println(actionType + " " + action.getValue(Action.NAME));
        if(actionType == VALUE_BUTTON_GROUP) {
            addRadioMenuItems(action);
            retval = null;
        }
        else if(actionType == VALUE_TOGGLE_ACTION)
            retval = addCheckBoxMenuItem(action);
        else
            retval = addMenuItem(action);
        return retval;
    }

    /**
     * Actions are added to the appropriate menu, or into a new menu if their
     * preferred location doesn't exist. This is done by accessing the Action's
     * KEY_MENU property. By default if the property is not set, the Action goes
     * into a "View" menu.
     *
     * <P>Currently doesn't support submenus. Eventual implementation:
     * the menu property could be of the form "menu.submenu".
     */
    public JMenuItem addMenuItem(Action action) {
        Object accelerator = action.getValue(KEY_ACCELERATOR);
        if(accelerator == null || accelerator.equals(VALUE_DEFAULT))
            accelerator = null;

        String menuName = (String)action.getValue("menu");
        if(menuName == null)
            menuName = VIEW_MENU; //Default location
        JMenu menu = (JMenu)menus.get(menuName);
        JMenuItem item = null;
        if(menu != null) { //Add to existing menu
            item = menu.add(action);
            item.setAccelerator((KeyStroke)accelerator);
            if(action.getValue(KEY_SEPARATOR_AFTER) != null
               || action.getValue(KEY_MENU_SEPARATOR_AFTER) != null)
                menu.addSeparator();
        }
        else { //Create new menu - Help always goes last
            JMenu helpMenu = getMenu(HELP_MENU);
            if(helpMenu != null)
                remove(helpMenu);
            menu = new JMenu(menuName);
            item = menu.add(action);
            item.setAccelerator((KeyStroke)accelerator);
            if(action.getValue(KEY_SEPARATOR_AFTER) != null
               || action.getValue(KEY_MENU_SEPARATOR_AFTER) != null)
                menu.addSeparator();
            add(menu);
            if(helpMenu != null)
                add(helpMenu);
            menus.put(menuName, menu);
        }
        if(item.getIcon() == null)
            item.setIcon(defaultIcon);

        return item;
    }

    static public JCheckBoxMenuItem createCheckBoxMenuItem(Action action) {
        JCheckBoxMenuItem item =
            new JCheckBoxMenuItem((String)action.getValue(Action.NAME));
        item.setIcon((Icon)action.getValue(Action.SMALL_ICON));
        item.setSelected(Boolean.TRUE.equals(action.getValue(KEY_SELECTED_STATE)));

        item.addActionListener(action);
        action.addPropertyChangeListener(new ActionChangeListener(item));
        return item;
    }

    public JMenuItem addCheckBoxMenuItem(Action action) {
        Object accelerator = action.getValue(KEY_ACCELERATOR);
        if(accelerator == null || accelerator.equals(VALUE_DEFAULT))
            accelerator = null;

        String menuName = (String)action.getValue("menu");
        if(menuName == null)
            menuName = VIEW_MENU; //Default location
        JMenu menu = (JMenu)menus.get(menuName);

        JMenuItem item = createCheckBoxMenuItem(action);

        if(menu != null) { //Add to existing menu
            menu.add(item);
            item.setAccelerator((KeyStroke)accelerator);
            if(action.getValue(KEY_SEPARATOR_AFTER) != null
               || action.getValue(KEY_MENU_SEPARATOR_AFTER) != null)
                menu.addSeparator();
        }
        else { //Create new menu - Help always goes last
            JMenu helpMenu = getMenu(HELP_MENU);
            if(helpMenu != null)
                remove(helpMenu);
            menu = new JMenu(menuName);
            menu.add(item);
            item.setAccelerator((KeyStroke)accelerator);
            if(action.getValue(KEY_SEPARATOR_AFTER) != null
               || action.getValue(KEY_MENU_SEPARATOR_AFTER) != null)
                menu.addSeparator();
            add(menu);
            if(helpMenu != null)
                add(helpMenu);
            menus.put(menuName, menu);
        }
        if(item.getIcon() == null)
            item.setIcon(defaultIcon);

        return item;
    }

    /**
     * Add radio button menu items for the specified action.
     */
    public void addRadioMenuItems(Action action) {
        String menuName = (String)action.getValue(KEY_MENU);
        if(menuName == null)
            menuName = VIEW_MENU; //Default location

        JMenu menu = (JMenu)menus.get(menuName);
        JMenu helpMenu = (JMenu)menus.get(HELP_MENU);
        if(menu == null) {
            if(helpMenu != null)
                remove(helpMenu);
            menu = new JMenu(menuName);
        }
        ButtonGroup group = new ButtonGroup();
        actionToGroup.put(action, group);

        //Store where the buttons go in the menu
        JComponent menuComponent = menu.getPopupMenu();
        int index = menuComponent.getComponentCount();
        action.putValue(KEY_INDEX, new Integer(index));

        //New menu added, so store key
        if(menus.get(menuName) == null) {
            add(menu);
            if(helpMenu != null)
                add(helpMenu);
            menus.put(menuName, menu);
        }

        //Create radio button for each option
        String names[] = (String[])action.getValue(Action.NAME);
        if(names != null) {
            for(int i = 0; i < names.length; i++) {
                JRadioButtonMenuItem button = createRadioMenuItem(group, action,
                    i);
                menu.add(button);
            }
        }
        if(action.getValue(KEY_SEPARATOR_AFTER) != null
           || action.getValue(KEY_MENU_SEPARATOR_AFTER) != null)
            menu.addSeparator();
        action.addPropertyChangeListener(this);
    }

    public void setVisible(boolean b) {
        if(b) {
            JMenu editMenu = (JMenu)menus.get(EDIT_MENU);
            if(editMenu.getItemCount() == 0)
                remove(editMenu);
            JMenu viewMenu = (JMenu)menus.get(VIEW_MENU);
            if(viewMenu.getItemCount() == 0)
                remove(viewMenu);
        }
        super.setVisible(b);
    }

    public void propertyChange(PropertyChangeEvent e) {
        String property = e.getPropertyName();
        if(AppBean.PROPERTY_ACTION_CHANGED.equals(property)
            /*|| AppBean.KEY_SELECTED_STATE.equals(property)*/
            ) {
            Action action = (Action)e.getNewValue();
            //Radio buttons have changed
            if(action.getValue(KEY_ACTION_TYPE) == VALUE_BUTTON_GROUP)
                updateRadioButtons(action);
        }
        else if(AppBean.PROPERTY_FOCUSED_BEAN.equals(property)) {
            Object obj = e.getNewValue();
            Action[] actions;
            if(obj instanceof AppBean)
                actions = ((AppBean)obj).getActions();
            else
                actions = null;
            updateContextDependencies(actions);
            //updateReusableActions(actions);
        }
    }

    /**
     * Set menus to contain only items appropriate to the current context.
     * @param actions available in the present context
     */
    protected void updateContextDependencies(Action[] actions) {
        if(contextDependencies == null || contextDependencies.isEmpty())
            return;

        //Update each context-dependent menu
        Iterator iterator = contextDependencies.values().iterator();
        while(iterator.hasNext()) {
            ContextDependency dependency = (ContextDependency)iterator.next();
            updateContextDependency(dependency, actions);
        }
    }

    protected void updateContextDependency(ContextDependency dependency,
                                           Action[] actions) {
        //Remove menu items for old context
        JPopupMenu popup = dependency.menu.getPopupMenu();
        int componentCount = popup.getComponentCount();
        for(int i = componentCount - 1; i >= dependency.startIndex; i--) {
            //A Swing quirk or bug causes the occasional bout of NullPointers
            try {
                popup.remove(popup.getComponent(i));
            }
            catch(NullPointerException npe) {}
        }

        //Add Actions for new context
        if(actions != null) {
            //Begin with a separator
            if(actions.length > 0)
                popup.addSeparator();

            String menuName = dependency.menu.getText();
            for(int i = 0; i < actions.length; i++) {
                String actionMenu = (String)actions[i].getValue(KEY_MENU);
                if(menuName.equals(actionMenu))
                    add(actions[i]);
            }
        }
    }

    /**
     * @param actions available in the present context
     */
    /*protected void updateReusableActions(Action[] actions)
      {
     if(reusableActions == null || reusableActions.isEmpty())
      return;
      }*/

    /**
     * Remove all buttons and replace them with the updated list.
     * Note: If the initial list contains no items, update doesn't know where to
     * put the updated list, so it appends it to the end of the menu.
     */
    protected void updateRadioButtons(Action action) {
        String[] names = (String[])action.getValue(Action.NAME);
        if(names == null)
            return;

        ButtonGroup group = (ButtonGroup)actionToGroup.get(action);
        JMenu menu = (JMenu)menus.get(action.getValue(KEY_MENU));

        /*
           //What's the first menu item in the group?
           //Works only because items are Strings, returned in order
           Enumeration enum = group.getElements();
           JMenuItem firstItem = null;
           if(enum.hasMoreElements())
         firstItem = (JMenuItem)enum.nextElement();
//System.err.println(firstItem.getText());

           //What's the index in the menu of the first button?
           int indexInMenu = 0;
           JComponent menuComponent = menu.getPopupMenu();
           int numMenuItems = menuComponent.getComponentCount();
           while(indexInMenu < numMenuItems
         && !menuComponent.getComponent(indexInMenu).equals(firstItem))
           {
         indexInMenu++;
           }
           //If no menu items previously existed, make sure
           if(indexInMenu == numMenuItems)
         */

        //Remove all menu items and start over
        Enumeration en = group.getElements();
        while(en.hasMoreElements()) {
            JMenuItem menuItem = (JMenuItem)en.nextElement();
            try { //HACK: try block needed when LAF changed
                menu.remove(menuItem);
            }
            catch(NullPointerException e) {}
        }
        actionToGroup.remove(group);

        group = new ButtonGroup();
        actionToGroup.put(action, group);

        //Add current menuItems to the group
        int indexInMenu = ((Integer)action.getValue(KEY_INDEX)).intValue();
        for(int i = 0; i < names.length; i++) {
            menu.insert(createRadioMenuItem(group, action, i), indexInMenu++);
        }
        //System.out.println("new menu");
        revalidate();
    }

    protected JRadioButtonMenuItem createRadioMenuItem(ButtonGroup group,
        Action action, int i) {
        Icon icons[] = (Icon[])action.getValue(Action.SMALL_ICON);
        String names[] = (String[])action.getValue(Action.NAME);

        JRadioButtonMenuItem button = new JRadioButtonMenuItem(names[i]);
        button.setSelected(names[i].equals(action.getValue(KEY_SELECTED_STATE)));
        group.add(button);
        if(icons == null || icons[i] == null)
            button.setIcon(defaultIcon);
        else
            button.setIcon(icons[i]);
        button.addActionListener(action);
        action.addPropertyChangeListener(new ActionChangeListener(group));
        return button;
    }

    public JMenu getMenu(String name) {
        return(JMenu)menus.get(name);
    }

    /**
     * Menu options depend on the context. All menu items added before marking
     * the menu as context-dependent are treated as context-independent items;
     * they remain available in all contexts.
     */
    public void markContextDependent(JMenu menu) {
        if(contextDependencies == null)
            contextDependencies = new HashMap();
        contextDependencies.put(menu.getText(), new ContextDependency(menu));
    }

    public void unmarkContextDependent(JMenu menu) {
        if(contextDependencies != null) {
            Iterator iterator = contextDependencies.values().iterator();
            while(iterator.hasNext()) {
                ContextDependency dependency = (ContextDependency)
                    contextDependencies.get(iterator.next());
                if(dependency.menu == menu)
                    contextDependencies.remove(dependency.menu.getText());
            }
        }
    }

    /**
     * Info on menu whose contents are context-dependent.
     */
    protected class ContextDependency {
        JMenu menu;
        int startIndex;

        ContextDependency(JMenu menu) {
            this.menu = menu;
            startIndex = menu.getPopupMenu().getComponentCount();
        }
    }
}
