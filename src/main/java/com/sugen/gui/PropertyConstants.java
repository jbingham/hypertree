package com.sugen.gui;

/**
 * Properties that GUI Beans use to communicate with each other.
 * Some additional properties of use to only a single bean or tightly
 * coupled group of beans may be specified as final static Strings inside
 * those bean class definitions rather than here. This interface contains
 * only the most common properties used by the various AppBeans and especially
 * the AppMainWindow and related classes.
 *
 * @author Jonathan Bingham
 */
public interface PropertyConstants {
    public static final String PROPERTY_STATUS_MESSAGE = "status";
    public static final String PROPERTY_COORDINATE_MESSAGE =
        "status_coordinate";
    public static final String PROPERTY_TITLE = "title";
    public static final String PROPERTY_ACTION_CHANGED = "action_changed";
    public static final String PROPERTY_CONTENTS_OPENED = "contents_opened";
    public static final String PROPERTY_CONTENTS_CLOSED = "contents_closed";
    public static final String PROPERTY_BEAN_INSTANTIATED = "bean_instantiated";
    public static final String PROPERTY_FOCUSED_BEAN = "focused_bean";
    public static final String PROPERTY_OWNER = "owner";
    public static final String PROPERTY_PROPERTIES = "properties";
    public static final String PROPERTY_CANCELED = "canceled";
    public static final String PROPERTY_WAITING = "waiting";
    public static final String PROPERTY_DATA = "data";
    public static final String PROPERTY_FILE = "file";
    public final static String PROPERTY_DATABASE = "database";
}
