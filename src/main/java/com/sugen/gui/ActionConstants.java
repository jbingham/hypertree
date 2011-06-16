package com.sugen.gui;

/**
 * Keys used in Actions.
 *
 * @author Jonathan Bingham
 */
public interface ActionConstants {
    public static final String VALUE_DEFAULT = "default";

    public static final String KEY_MENU = "menu";
    public final static String FILE_MENU = "File";
    public final static String EDIT_MENU = "Edit";
    public final static String VIEW_MENU = "View";
    public final static String HELP_MENU = "Help";
    public final static String OPTION_MENU = "Option";

    /** Value should be a Keystroke. */
    public static final String KEY_ACCELERATOR = "shortcut";
    public static final String KEY_SEPARATOR_AFTER = "separator";
    public static final String KEY_MENU_SEPARATOR_AFTER = "menuSeparator";
    public static final String KEY_TOOLBAR_SEPARATOR_AFTER = "toolbarSeparator";

    public static final String KEY_ACTION_TYPE = "actionType";
    /**
     * Signals that an Action should be given radio buttons in
     * the menubar and toggle buttons on the tool bar (provided that MENU_ONLY
     * is not specified). The NAME property should then contain a String
     * array of names for each of the buttons in the group. Likewise, the
     * SMALL_ICON property should contain an Icon array.
     */
    public static final String VALUE_BUTTON_GROUP = "buttonGroup";

    /** Like a BUTTON_GROUP, but with only one member. No need for arrays. */
    public static final String VALUE_TOGGLE_ACTION = "toggle";

    /**
     * In a BUTTON_GROUP, the SELECTED_STATE is the selected button's name.
     * In a TOGGLE_ACTION, the SELECTED_STATE is the Boolean selected state
     */
    public static final String KEY_SELECTED_STATE = "selectedState";

    public static final String KEY_LOCATION = "location";
    public static final String VALUE_MENU_ONLY = "menuOnly";
}
