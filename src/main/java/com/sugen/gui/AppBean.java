package com.sugen.gui;

import javax.swing.Action;
import java.beans.PropertyChangeListener;

/**
 * Interface for beans that expose their actions en masse for the benefit
 * of an AppMainWindow. Also, some property names for notifying listeners and
 * for receiving PropertyChangeEvents.
 * <p>
 * This interface makes it easy to create beans that work well together in
 * a complete applet or application. It ensures that they can both listen
 * to and dispatch PropertyChangeEvents. Also, it provides some set of
 * Actions that can be used to create GUI controls, such as menu items and
 * tool buttons. Though an AppBean may have no actions, this will probably
 * seldom be the case.
 * <p>
 * As a general rule of thumb, implement the AppBean
 * interface only for beans that are fairly 'high-level', with their own GUI
 * controls. They should be functionally or visually distinct parts of
 * an application, such as a particular type of view, a database interface,
 * a file I/O component, or the like. In the sugendk, such classes often,
 * but not always, have the suffix UI appended to their class names.
 * <p>
 * In this package, the AppMainWindow, AppMenuBar and other App* classes
 * also implement the AppBean interface. They function primarily as listeners
 * to other AppBeans. They provide much of the basic code for integrating the
 * various pieces of a complete application with the mechanics of toolbars,
 * menubar, status bar, clipboard and main window.
 *
 * @see AppMainWindow
 *
 * @author Jonathan Bingham
 */
public interface AppBean
    extends ActionConstants, PropertyChangeListener, PropertyConstants {
    /**
     * Return all actions. An AppMainWindow and sub-components will check
     * each Action's properties to determine what controls are appropriate.
     */
    public Action[] getActions();

    /** Any JComponent already contains this method. */
    public void addPropertyChangeListener(PropertyChangeListener pcl);

    /** Any JComponent already contains this method. */
    public void removePropertyChangeListener(PropertyChangeListener pcl);
}
