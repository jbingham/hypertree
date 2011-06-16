package com.sugen.gui;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Frame;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.awt.event.MouseEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyVetoException;
import java.util.Properties;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JComponent;
import javax.swing.JDesktopPane;
import javax.swing.JInternalFrame;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import javax.swing.border.BevelBorder;
import javax.swing.event.InternalFrameAdapter;
import javax.swing.event.InternalFrameEvent;
import javax.swing.event.MouseInputAdapter;
import javax.swing.event.MouseInputListener;

/**
 * A desktop manager that supports multiple dockable JInternalFrames.
 * Includes all the necessary menu actions for foregrounding frames,
 * closing them, and arranging them in a cascade.
 *
 * <p>Designed for integration with the other App classes, but can be used
 * independently.
 *
 * @author Jonathan Bingham
 */
public class AppDesktop
    extends JPanel implements AppBean, PropertyChangeListener, Closeable {
    public final static int HORIZONTAL = SwingConstants.HORIZONTAL;
    public final static int VERTICAL = SwingConstants.VERTICAL;

    /** @serial */
    protected Frame owner;
    /** @serial */
    protected Properties properties;

    /**
     * Where the internal frames go.
     * @serial
     */
    protected JDesktopPane appDesktop;
    /** @serial */
    protected JInternalFrame selectedFrame;
    /** @serial */
    protected Dimension defaultFrameSize = new Dimension(500, 300);

    /**
     * Are frames by default created/foregrounded at max size?.
     * @serial
     */
    protected boolean isDefaultMaximum = false;

    /**
     * Last added frame's location, for cascade and autopositioning.
     * @serial
     */
    protected Point lastFrameLocation = new Point();

    public AppDesktop() {
        appDesktop = new JDesktopPane();

        //HACK workaround until first bug fix release of Java 2
        //When resized, make sure maximized frames are resized too
        appDesktop.addComponentListener(new ComponentAdapter() {
            public void componentResized(ComponentEvent e) {
                JInternalFrame[] frames = appDesktop.getAllFrames();
                for(int i = 0; i < frames.length; i++)
                    if(frames[i].isMaximum())
                        frames[i].setSize(appDesktop.getSize());
                revalidate();
            }
        });

        setLayout(new BorderLayout());
        add(appDesktop);
        setBorder(new BevelBorder(BevelBorder.LOWERED));
    }

    public Component add(JInternalFrame frame) {
        addListeners(frame);

        //set size and location from properties file
        String prefix = "desktop." + frame.getContentPane().getClass().getName() +
            ".";
        if(properties != null && properties.containsKey(prefix + "width")) {
            try {
                int width = Integer.parseInt(properties.getProperty(prefix +
                    "width"));
                int height = Integer.parseInt(properties.getProperty(prefix +
                    "height"));
                frame.setSize(width, height);
                appDesktop.add(frame);
                int x = Integer.parseInt(properties.getProperty(prefix + "x"));
                int y = Integer.parseInt(properties.getProperty(prefix + "y"));
                frame.setLocation(x, y);
            }
            catch(NumberFormatException e) {}
        }
        //set size and location to defaults
        else {
            if(frame.isResizable())
                autoSize(frame);
            appDesktop.add(frame);
            if(isDefaultMaximum)
                try {
                    frame.setMaximum(true);
                }
                catch(PropertyVetoException pve) {}
            else
                positionFrame(frame);
        }

        frame.getContentPane().validate();
        setSelectedFrame(frame);
        updateActions();
        return frame;
    }

    protected void remove(JInternalFrame frame) {
        if(properties != null) {
            String prefix = "desktop." +
                frame.getContentPane().getClass().getName() + ".";
            properties.setProperty(prefix + "x",
                                   String.valueOf(frame.getLocation().x));
            properties.setProperty(prefix + "y",
                                   String.valueOf(frame.getLocation().y));
            properties.setProperty(prefix + "width",
                                   String.valueOf(frame.getWidth()));
            properties.setProperty(prefix + "height",
                                   String.valueOf(frame.getHeight()));
        }

        appDesktop.remove(frame);
        repaint();

        JInternalFrame[] frames = appDesktop.getAllFrames();
        //windowManagerAction.update(frames);
        if(frames != null && frames.length > 0)
            setSelectedFrame(frames[0]);
        else
            setSelectedFrame(null);
        updateActions();
    }

    protected void updateActions() {
        boolean hasFrames = appDesktop.getAllFrames().length > 0;

        //closeAllAction.setEnabled(hasFrames);
        cascadeAction.setEnabled(hasFrames);
        tileHorizontalAction.setEnabled(hasFrames);
        tileVerticalAction.setEnabled(hasFrames);
        //windowListAction.setEnabled(true);
        //windowManagerAction.update(appDesktop.getAllFrames());
    }

    protected void addListeners(JInternalFrame frame) {
        //Listeners: DO_NOTHING so we can handle close ourselves
        frame.setDefaultCloseOperation(JInternalFrame.DO_NOTHING_ON_CLOSE);
        frame.addInternalFrameListener(appFrameListener);
        frame.addComponentListener(frameResizeListener);
        frame.addMouseListener(mouseInputListener);
        frame.addMouseMotionListener(mouseInputListener);

        Component titleBar = frame.getComponent(1); //HACK JInternalFrame titlebar
        titleBar.addMouseListener(mouseInputListener);
        titleBar.addMouseMotionListener(mouseInputListener);
    }

    protected void autoSize(JInternalFrame frame) {
        Dimension cSize = frame.getContentPane().getPreferredSize();
        if(cSize.height == 0 || cSize.width == 0)
            frame.setSize(defaultFrameSize);
        else
            frame.pack();

        //Frame can't be bigger than viewable area.
        Dimension frameSize = frame.getSize();
        if(frameSize.width > appDesktop.getWidth())
            frameSize.width = appDesktop.getWidth();
        if(frameSize.height > appDesktop.getHeight())
            frameSize.height = appDesktop.getHeight();
        frame.setSize(frameSize);
        frame.setPreferredSize(frameSize);
    }

    /**
     * Create internal frame for the specified component, setting default
     * properties and adding appropriate listeners.
     */
    public JInternalFrame createInternalFrame(Component c) {
        JInternalFrame frame = new JInternalFrame("");
        frame.setClosable(true);
        frame.setResizable(true);
        frame.setMaximizable(true);
        frame.setIconifiable(true);
        frame.setOpaque(true);

        if(c instanceof Container)
            frame.setContentPane((Container)c);
        else
            frame.getContentPane().add((Component)c);
        //addListeners(frame);
        return frame;
    }

    /** @serial */
    protected ComponentListener frameResizeListener = new ComponentAdapter() {
        public void componentResized(ComponentEvent e) {
            JInternalFrame frame = (JInternalFrame)e.getSource();
            if(frame.isMaximum())
                setDefaultMaximum(true);
            else
                setDefaultMaximum(false);
        }
    };

    /** @serial */
    protected AppFrameListener appFrameListener = new AppFrameListener();

    /**
     * Internal frame events.
     */
    protected class AppFrameListener
        extends InternalFrameAdapter {
        /**
         * Possibly block closing.
         */
        public void internalFrameClosing(InternalFrameEvent ife) {
            JInternalFrame frame = (JInternalFrame)ife.getSource();
            close(frame);
        }

        /**
         * Set the selection state to the currently activated frame.
         */
        public void internalFrameActivated(InternalFrameEvent ife) {
            JInternalFrame frame = (JInternalFrame)ife.getSource();
            setSelectedFrame(frame);
        }
    }

    /** Index of frame among children of window manager; activated frame is first. */
    /*protected int indexOf(JInternalFrame frame)
      {
     JInternalFrame[] frames = appDesktop.getAllFrames();
     for(int i = 0; i < frames.length; i++)
      if(frames[i].equals(frame))
       return i;
     return -1;
      }*/

    public void cascade() {
        lastFrameLocation.x = 0;
        lastFrameLocation.y = 0;

        JInternalFrame frames[] = appDesktop.getAllFrames();
        for(int i = 0; i < frames.length; i++) {
            positionFrame(frames[i]);
        }
    }

    /**
     * HORIZONTAL or VERTICAL
     */
    public void tile(int axis) {
        //Divide up space among open frames
        JInternalFrame frames[] = appDesktop.getAllFrames();
        Dimension frameSize = new Dimension(appDesktop.getSize());
        int xShift = 0, yShift = 0;
        if(frames.length > 0) {
            if(axis == HORIZONTAL) {
                frameSize.height /= frames.length;
                yShift = frameSize.height;
            }
            else {
                frameSize.width /= frames.length;
                xShift = frameSize.width;
            }
        }

        //Set size of each frame
        int x = 0, y = 0;
        for(int i = 0; i < frames.length; i++) {
            if(frames[i].isMaximum()) {
                try {
                    frames[i].setMaximum(false);
                }
                catch(PropertyVetoException pve) {}
            }
            frames[i].setSize(frameSize);
            frames[i].setLocation(x, y);
            x += xShift;
            y += yShift;
        }
    }

    protected void positionFrame(JInternalFrame frame) {
        if(!frame.isVisible())
            return;
        if(frame.isMaximum()) {
            try {
                frame.setMaximum(false);
            }
            catch(PropertyVetoException pve) {}
        }
        int shift = frame.getComponent(1).getPreferredSize().height
            + frame.getInsets().top;

        frame.setLocation(lastFrameLocation);
        lastFrameLocation.x += shift;
        lastFrameLocation.y += shift;

        if(lastFrameLocation.x > getSize().width / 2
           || lastFrameLocation.y > getSize().height / 2)
            lastFrameLocation = new Point();

        setSelectedFrame(frame);
    }

    public boolean canClose() {
        boolean retval = true;
        JInternalFrame[] frames = appDesktop.getAllFrames();
        for(int i = frames.length - 1; i >= 0 && retval; i--) {
            if(frames[i].getContentPane() instanceof Closeable)
                retval = ((Closeable)frames[i].getContentPane()).canClose();
        }
        return retval;
    }

    public void close() {
        JInternalFrame[] frames = appDesktop.getAllFrames();
        for(int i = frames.length - 1; i >= 0; i--) {
            if(frames[i].getContentPane() instanceof Closeable)
                ((Closeable)frames[i].getContentPane()).close();
            close(frames[i]);
        }
        //updateActions();
    }

    public void close(JInternalFrame frame) {
        Container contents = frame.getContentPane();
        if(contents instanceof Closeable) {
            boolean canClose = ((Closeable)contents).canClose();
            if(canClose) {
                ((Closeable)contents).close();
                frame.setVisible(false);
                remove(frame); //remove cleanly
            }
        }
        updateActions();
    }

    public Action[] getActions() {
        return new Action[] {
            cascadeAction, tileHorizontalAction,
            tileVerticalAction, /*closeAllAction, windowManagerAction,
                windowListAction*/};
    }

    /** @serial */
    protected Action cascadeAction = new AbstractAction("Cascade",
        Icons.get("cascadeWindows24.gif")) {
        {
            putValue(KEY_MENU, "Window");
            putValue(KEY_LOCATION, VALUE_MENU_ONLY);
            setEnabled(false);
        }

        public void actionPerformed(ActionEvent e) {
            cascade();
        }
    };

    public Action getCascadeAction() {
        return cascadeAction;
    }

    /** @serial */
    protected Action tileHorizontalAction = new AbstractAction(
        "Tile Horizontally",
        Icons.get("tileHorizontal24.gif")) {
        {
            putValue(KEY_MENU, "Window");
            putValue(KEY_LOCATION, VALUE_MENU_ONLY);
            setEnabled(false);
        }

        public void actionPerformed(ActionEvent e) {
            tile(HORIZONTAL);
        }
    };

    public Action getTileHorizontalAction() {
        return tileHorizontalAction;
    }

    /** @serial */
    protected Action tileVerticalAction = new AbstractAction("Tile Vertically",
        Icons.get("tileVertical24.gif")) {
        {
            putValue(KEY_MENU, "Window");
            putValue(KEY_LOCATION, VALUE_MENU_ONLY);
            setEnabled(false);
        }

        public void actionPerformed(ActionEvent e) {
            tile(VERTICAL);
        }
    };

    public Action getTileVerticalAction() {
        return tileVerticalAction;
    }

    /*protected Action closeAllAction = new AbstractAction("Close All",
     new ImageIcon(getClass().getResource("images/closeAllWindows.gif")))
      {
     {
      putValue(KEY_MENU, "Window");
      putValue(KEY_LOCATION, VALUE_MENU_ONLY);
      putValue(KEY_SEPARATOR_AFTER, Boolean.TRUE);
      setEnabled(false);
     }
     public void actionPerformed(ActionEvent e)
     {
      if(canClose())
       close();
     }
      };

      public Action getCloseAllAction()
      {
     return closeAllAction;
      }*/

    /*protected Action windowListAction = new AbstractAction("Window List...")
      {
     {
      putValue(KEY_MENU, "Window");
      putValue(KEY_LOCATION, VALUE_MENU_ONLY);
      setEnabled(false);
     }
     public void actionPerformed(ActionEvent e)
     {
      listWindows();
     }
      };

      public Action getWindowListAction()
      {
     return windowListAction;
      }

     protected WindowManagerAction windowManagerAction = new WindowManagerAction();

      protected class WindowManagerAction extends AbstractAction
      {
     WindowManagerAction()
     {
      putValue(KEY_MENU, "Window");
      putValue(KEY_LOCATION, VALUE_MENU_ONLY);
      putValue(KEY_ACTION_TYPE, VALUE_BUTTON_GROUP);
     }

     void update(JInternalFrame[] frames)
     {
      int nFrames = frames.length;
      if(nFrames > 9)
       nFrames = 9;
      String[] names = new String[nFrames];
      for(int i = 0; i < nFrames; i++)
       names[i] = (i + 1) + " " + frames[i].getTitle();
      putValue(NAME, names);
      if(names.length > 0)
       putValue(KEY_SELECTED_STATE, names[0]);

     firePropertyChange(PROPERTY_ACTION_CHANGED, null, WindowManagerAction.this);
     }

     public void actionPerformed(ActionEvent e)
     {
      String cmd = e.getActionCommand().substring(2);

      //Figure out which frame this action corresponds to
      JInternalFrame frame = null;
      int selection = 0;
      JInternalFrame[] frames = appDesktop.getAllFrames();
      while(selection < frames.length && frame == null)
      {
       JInternalFrame nextFrame = frames[selection++];
       if(nextFrame.getTitle().equals(cmd))
        frame = nextFrame;
      }
      setSelectedFrame(frame);
     }
      }

      public Action getWindowManagerAction()
      {
     return windowManagerAction;
      }*/

    public void propertyChange(PropertyChangeEvent e) {
        String name = e.getPropertyName();
        //System.out.println(propertyName);
        if(name.equals(PROPERTY_TITLE)) {
            JComponent c = (JComponent)e.getSource();
            JInternalFrame frame = getFrame(c);
            updateFrameTitle(frame, (String)e.getOldValue(),
                             (String)e.getNewValue());
        }
        else if(name.equals(PROPERTY_CONTENTS_OPENED)) {
            JComponent newComponent = (JComponent)e.getNewValue();
            newComponent.updateUI(); //In case changed while component wasn't visible
            if(getFrame(newComponent) == null) {
                JInternalFrame frame;
                if(newComponent instanceof JInternalFrame)
                    frame = (JInternalFrame)newComponent;
                else
                    frame = createInternalFrame(newComponent);
                add(frame);
            }
            //System.out.println("opened" + " " + getFrame(c));
        }
        else if(name.equals(PROPERTY_BEAN_INSTANTIATED)) {
            JComponent newComponent = (JComponent)e.getNewValue();
            newComponent.addPropertyChangeListener(this);
            JInternalFrame frame;
            if(newComponent instanceof JInternalFrame)
                frame = (JInternalFrame)newComponent;
            else
                frame = createInternalFrame(newComponent);
            add(frame);
        }
        else if(name.equals(PROPERTY_PROPERTIES)) {
            properties = (Properties)e.getNewValue();
        }
    }

    /**
     * The frame having the specified Component as its
     * contentPane.
     */
    public JInternalFrame getFrame(Component c) {
        JInternalFrame[] frames = appDesktop.getAllFrames();
        for(int i = 0; i < frames.length; i++) {
            if(frames[i].getContentPane() == c)
                return frames[i];
        }
        return null;
    }

    public void updateFrameTitle(JInternalFrame frame, String oldTitle,
                                 String newTitle) {
        if(frame == null)
            return;
        if(newTitle == null)
            newTitle = "";
        frame.setTitle(newTitle);
        //windowManagerAction.update(appDesktop.getAllFrames());
    }

    public Dimension getDefaultFrameSize() {
        return defaultFrameSize;
    }

    public void setDefaultFrameSize(Dimension dim) {
        defaultFrameSize = dim;
    }

    /**
     * For updating actions and hiding contents while dragging/resizing.
     * @serial
     */
    protected MouseInputListener mouseInputListener = new MouseInputAdapter() {
        public void mouseDragged(MouseEvent e) {
            JInternalFrame frame;
            if(e.getSource() instanceof JInternalFrame)
                frame = (JInternalFrame)e.getSource();
            else
                frame = (JInternalFrame)e.getComponent().getParent();
            setSelectedFrame(frame);

            frame.getContentPane().setVisible(false);
            frame.setOpaque(false);
        }

        public void mouseReleased(MouseEvent e) {
            JInternalFrame frame;
            if(e.getSource() instanceof JInternalFrame)
                frame = (JInternalFrame)e.getSource();
            else
                frame = (JInternalFrame)e.getComponent().getParent();
            frame.getContentPane().setVisible(true);
            frame.setOpaque(true);
        }
    };

    protected void setSelectedFrame(JInternalFrame frame) {
        if(frame != null)
            frame.toFront(); //in case it's not already
        //windowManagerAction.update(appDesktop.getAllFrames());
        try {
            if(frame != selectedFrame && selectedFrame != null)
                selectedFrame.setSelected(false);
            if(frame != null) {
                frame.setSelected(true);
                if(isDefaultMaximum)
                    frame.setMaximum(true);
            }
        }
        catch(PropertyVetoException pve) {}

        //Manage focus
        Component oldFocusedComponent =
            selectedFrame == null ? null : selectedFrame.getContentPane();
        selectedFrame = frame;
        Component newFocusedComponent =
            selectedFrame == null ? null : selectedFrame.getContentPane();
        firePropertyChange(PROPERTY_FOCUSED_BEAN,
                           oldFocusedComponent, newFocusedComponent);

        //In case frame didn't clean up after itself
        firePropertyChange(PROPERTY_STATUS_MESSAGE, null, "Ready");

        //If nothing open, clear file property
        if(frame == null)
            firePropertyChange(PROPERTY_FILE, "", null);
    }

    public boolean isDefaultMaximum() {
        return isDefaultMaximum;
    }

    public void setDefaultMaximum(boolean b) {
        isDefaultMaximum = b;
    }

    /**
     * Pop up a scrollable list of all windows, in case there are more than nine.
     * Let user select one. Selected frame moves toFront().
     */
    /*public void listWindows()
      {
     FormDialog dialog = new FormDialog(null);
     dialog.setForm(new WindowListForm(dialog));
     JInternalFrame retval = (JInternalFrame)dialog.showDialog();
     if(retval != null)
      setSelectedFrame(retval);
      }*/

    /**
     * A list of all open windows. User may select one. The selected JInternalFrame
     * is then returned by the showDialog() method.
     */
    /*protected class WindowListForm extends com.sugen.gui.form.Form
      {
     JList windowList;

     public WindowListForm(FormCarrier carrier)
     {
      super(carrier);
      setTitle("Window List");
      setPreferredSize(new Dimension(400, 300));

      //Get titles of all open frames...
      JInternalFrame[] frames = appDesktop.getAllFrames();
      String[] frameTitles = new String[frames.length];
      for(int i = 0; i < frames.length; i++)
       frameTitles[i] = frames[i].getTitle();

      windowList = new JList(frameTitles);
      windowList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

      JScrollPane scrollPane = new JScrollPane(windowList);
      setLayout(new BorderLayout());
      WindowListForm.this.add(scrollPane);
     }

     public void applyChanges()
     {
      int selectNum = windowList.getSelectedIndex();
      if(selectNum != -1)
      {
       JInternalFrame frame = appDesktop.getAllFrames()[selectNum];
       setReturnValue(frame);
      }
     }
      }*/
}
