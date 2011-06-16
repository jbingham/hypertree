package com.sugen.gui;

import java.awt.*;
import java.awt.event.*;
import java.beans.*;
import java.util.*;
import javax.swing.*;
import javax.swing.border.BevelBorder;
import javax.swing.border.Border;
import javax.swing.event.*;

import com.sugen.gui.form.*;

/**
 * A desktop manager that supports multiple dockable JInternalFrames.
 * Includes all the necessary menu actions for foregrounding frames,
 * closing them, and arranging them in a cascade.
 *
 * <p>Designed for integration with the other App classes, but can be used
 * independently.
 *
 * <p><b>WARNING</b>: This class is under development and has not been
 * maintained
 *
 * @author Jonathan Bingham
 */
public class AppDockableDesktop
    extends JPanel implements AppBean, PropertyChangeListener, Closeable {
    public final static int HORIZONTAL = SwingConstants.HORIZONTAL;
    public final static int VERTICAL = SwingConstants.VERTICAL;
    public final static int TOP = SwingConstants.TOP;
    public final static int LEFT = SwingConstants.LEFT;
    public final static int BOTTOM = SwingConstants.BOTTOM;
    public final static int RIGHT = SwingConstants.RIGHT;
    public final static int CENTER = SwingConstants.CENTER;

    /**
     * Mouse position within this many pixels of desktop edge signals dock.
     * @serial
     */
    protected final int MOUSE_SENSITIVITY = 10;

    /** @serial */
    protected Frame owner;

    /**
     * Where the internal frames go.
     * @serial
     */
    protected JDesktopPane appDesktop;
    /** @serial */
    protected JInternalFrame selectedFrame;
    /** @serial */
    protected Dimension defaultFrameSize = new Dimension(500, 300);

    /** Are frames by default created/foregrounded at max size?.
     * @serial
     */
    protected boolean isDefaultMaximum = false;

    //Various variables needed to manage docking behavior
    /** TOP, BOTTOM, RIGHT or LEFT.
     * @serial
     */
    protected int dockLocation;
    /** nulled out when frame is docked, restored afterward.
     * @serial
     */
    protected Border frameBorder = null;
    /** Dock frame over top of this desktop.
     * @serial */
    protected JDesktopPane desktopForDock;
    /** Rectangle to draw while moving/docking/undocking frames.
     * @serial */
    protected Rectangle frameDragRectangle;
    /** New upper left corner of moving/docking/undocking frame.
     * @serial */
    protected Point frameLocation = new Point();
    /** Needs to persist until mouseReleased.
     * @serial */
    protected Point mousePressedAt;
    /** Map of docked components to their frames.
     * @serial */
    protected Map dockedComponents = new HashMap();

    /** Last added frame's location, for cascade and autopositioning.
     * @serial */
    protected Point lastFrameLocation = new Point();

    public AppDockableDesktop() {
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

        desktopForDock = appDesktop;

        setLayout(new BorderLayout());
        add(appDesktop);
        setBorder(new BevelBorder(BevelBorder.LOWERED));
    }

    public Component add(JInternalFrame frame) {
        addListeners(frame);
        if(frame.isResizable())
            autoSize(frame);

        //If this is the first frame, enable Actions.
        if(appDesktop.getAllFrames().length == 0) {
            closeAllAction.setEnabled(true);
            cascadeAction.setEnabled(true);
            tileHorizontalAction.setEnabled(true);
            tileVerticalAction.setEnabled(true);
            windowListAction.setEnabled(true);
        }

        appDesktop.add(frame);

        if(isDefaultMaximum) {
            try {
                frame.setMaximum(true);
            }
            catch(PropertyVetoException pve) {}
        }
        else {
            positionFrame(frame);
        }
        frame.getContentPane().validate();

        windowManagerAction.update(appDesktop.getAllFrames());
        return frame;
    }

    protected void addListeners(JInternalFrame frame) {
        //Listeners: DO_NOTHING so we can handle close ourselves
        frame.setDefaultCloseOperation(JInternalFrame.DO_NOTHING_ON_CLOSE);
        frame.addInternalFrameListener(new AppFrameListener());
        frame.addComponentListener(frameResizeListener);

        Component titleBar = frame.getComponent(1);
        titleBar.addMouseListener(frameMouseListener);
        //titleBar.addMouseMotionListener(frameMouseMotionListener);
    }

    protected void setDockable(JInternalFrame frame) {
        Component titleBar = frame.getComponent(1);
        titleBar.addMouseListener(frameMouseListener);
        titleBar.addMouseMotionListener(frameMouseMotionListener);
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
    protected JInternalFrame createInternalFrame(Component c) {
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

        setDockable(frame);

        return frame;
    }

    /** @serial */
    protected ComponentListener frameResizeListener = new ComponentAdapter() {
        public void componentResized(ComponentEvent e) {
            JInternalFrame frame = (JInternalFrame)e.getSource();
            if(!isDocked(frame)) {
                if(frame.isMaximum())
                    setDefaultMaximum(true);
                else
                    setDefaultMaximum(false);
            }
        }
    };

    /**
     * Internal frame events.
     * @serial
     */
    protected class AppFrameListener
        extends InternalFrameAdapter {
        /**
         * Possibly block closing.
         */
        public void internalFrameClosing(InternalFrameEvent ife) {
            JInternalFrame frame = (JInternalFrame)ife.getSource();
            Container contents = frame.getContentPane();
            boolean isClosed = true;
            if(contents instanceof Closeable) {
                boolean canClose = ((Closeable)contents).canClose();
                if(canClose) {
                    ((Closeable)contents).close();
                }
                else
                    isClosed = false;
            }
            if(isClosed) {
                if(isDocked(frame))
                    undock(frame);
                try {
                    frame.setVisible(false);
                    frame.setClosed(true);
                }
                catch(PropertyVetoException pve) {}
                remove(frame); //remove cleanly
            }
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
    protected int indexOf(JInternalFrame frame) {
        JInternalFrame[] frames = appDesktop.getAllFrames();
        for(int i = 0; i < frames.length; i++)
            if(frames[i].equals(frame))
                return i;
        return -1;
    }

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
        //Shift by the title bar size
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

        Iterator iterator = dockedComponents.keySet().iterator();
        while(retval && iterator.hasNext()) {
            Object obj = iterator.next();
            if(obj instanceof Closeable)
                retval = ((Closeable)obj).canClose();
        }
        return retval;
    }

    public void close() {
        try {
            JInternalFrame[] frames = appDesktop.getAllFrames();
            for(int i = frames.length - 1; i >= 0; i--) {
                if(frames[i].getContentPane() instanceof Closeable)
                    ((Closeable)frames[i].getContentPane()).close();
                frames[i].setClosed(true);
            }

            Iterator iterator = dockedComponents.keySet().iterator();
            while(iterator.hasNext()) {
                Object obj = iterator.next();
                if(obj instanceof Closeable)
                    ((Closeable)obj).close();
            }
        }
        catch(PropertyVetoException pve) {
        }
        closeAllAction.setEnabled(false);
    }

    public void remove(JInternalFrame frame) {
        appDesktop.remove(frame);
        repaint();

        //No frames left
        if(appDesktop.getAllFrames().length == 0) {
            if(dockedComponents.isEmpty())
                closeAllAction.setEnabled(false);
            cascadeAction.setEnabled(false);
            tileHorizontalAction.setEnabled(false);
            tileVerticalAction.setEnabled(false);
            windowListAction.setEnabled(false);
            lastFrameLocation = new Point();
        }
        windowManagerAction.update(appDesktop.getAllFrames());
    }

    public Action[] getActions() {
        return new Action[] {
            cascadeAction, tileHorizontalAction,
            tileVerticalAction, closeAllAction, windowManagerAction,
            windowListAction};
    }

    /** @serial */
    protected Action cascadeAction = new AbstractAction("Cascade",
        new ImageIcon(getClass().getResource("images/cascadeWindows.gif"))) {
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
        new ImageIcon(getClass().getResource("images/tileHorizontal.gif"))) {
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
        new ImageIcon(getClass().getResource("images/tileVertical.gif"))) {
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

    /** @serial */
    protected Action closeAllAction = new AbstractAction("Close All",
        new ImageIcon(getClass().getResource("images/closeAllWindows.gif"))) {
        {
            putValue(KEY_MENU, "Window");
            putValue(KEY_LOCATION, VALUE_MENU_ONLY);
            putValue(KEY_SEPARATOR_AFTER, Boolean.TRUE);
            setEnabled(false);
        }

        public void actionPerformed(ActionEvent e) {
            if(canClose())
                close();
        }
    };

    public Action getCloseAllAction() {
        return closeAllAction;
    }

    /** @serial */
    protected Action windowListAction = new AbstractAction("Window List...") {
        {
            putValue(KEY_MENU, "Window");
            putValue(KEY_LOCATION, VALUE_MENU_ONLY);
            setEnabled(false);
        }

        public void actionPerformed(ActionEvent e) {
            listWindows();
        }
    };

    public Action getWindowListAction() {
        return windowListAction;
    }

    /** @serial */
    protected WindowManagerAction windowManagerAction = new WindowManagerAction();

    protected class WindowManagerAction
        extends AbstractAction {
        WindowManagerAction() {
            putValue(KEY_MENU, "Window");
            putValue(KEY_LOCATION, VALUE_MENU_ONLY);
            putValue(KEY_ACTION_TYPE, VALUE_BUTTON_GROUP);
        }

        void update(JInternalFrame[] frames) {
            int nFrames = frames.length;
            if(nFrames > 9)
                nFrames = 9;
            String[] names = new String[nFrames];
            for(int i = 0; i < nFrames; i++)
                names[i] = (i + 1) + " " + frames[i].getTitle();
            putValue(NAME, names);
            if(names.length > 0)
                putValue(KEY_SELECTED_STATE, names[0]);

            firePropertyChange(PROPERTY_ACTION_CHANGED, null,
                               WindowManagerAction.this);
        }

        public void actionPerformed(ActionEvent e) {
            String cmd = e.getActionCommand().substring(2);

            //Figure out which frame this action corresponds to
            JInternalFrame frame = null;
            int selection = 0;
            JInternalFrame[] frames = appDesktop.getAllFrames();
            while(selection < frames.length && frame == null) {
                JInternalFrame nextFrame = frames[selection++];
                if(nextFrame.getTitle().equals(cmd))
                    frame = nextFrame;
            }
            setSelectedFrame(frame);
        }
    }

    public Action getWindowManagerAction() {
        return windowManagerAction;
    }

    public void propertyChange(PropertyChangeEvent pce) {
        String propertyName = pce.getPropertyName();
        //System.out.println(propertyName);
        if(propertyName.equals(PROPERTY_TITLE)) {
            JComponent c = (JComponent)pce.getSource();
            JInternalFrame frame = getFrame(c);
            updateFrameTitle(frame, (String)pce.getOldValue(),
                             (String)pce.getNewValue());
        }
        else if(propertyName.equals(PROPERTY_CONTENTS_OPENED)) {
            JComponent newComponent = (JComponent)pce.getNewValue();
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
        else if(propertyName.equals(PROPERTY_BEAN_INSTANTIATED)) {
            JComponent newComponent = (JComponent)pce.getNewValue();
            newComponent.addPropertyChangeListener(this);
            JInternalFrame frame;
            if(newComponent instanceof JInternalFrame)
                frame = (JInternalFrame)newComponent;
            else
                frame = createInternalFrame(newComponent);
            add(frame);
        }
    }

    /**
     * The frame for the component, which is the its
     * contentPane. The frame may or may not be docked.
     */
    public JInternalFrame getFrame(Component c) {
        JInternalFrame[] frames = appDesktop.getAllFrames();
        for(int i = 0; i < frames.length; i++) {
            if(frames[i].getContentPane() == c)
                return frames[i];
        }
        return(JInternalFrame)dockedComponents.get(c);
    }

    /**
     * @param oldTitle - excluding window number
     */
    public void updateFrameTitle(JInternalFrame frame, String oldTitle,
                                 String newTitle) {
        if(frame == null)
            return;
        if(newTitle == null)
            newTitle = "";
        frame.setTitle(newTitle);
        windowManagerAction.update(appDesktop.getAllFrames());
    }

    public Dimension getDefaultFrameSize() {
        return defaultFrameSize;
    }

    public void setDefaultFrameSize(Dimension dim) {
        defaultFrameSize = dim;
    }

    /** For docking.
     * @serial */
    protected MouseListener frameMouseListener = new MouseAdapter() {
        public void mousePressed(MouseEvent e) {
            JInternalFrame frame = (JInternalFrame)e.getComponent().getParent();
            setSelectedFrame(frame);
            dockLocation = CENTER;
            desktopForDock = frame.getDesktopPane();
            mousePressedAt = e.getPoint();
        }

        public void mouseReleased(MouseEvent e) {
            if(selectedFrame == null)
                return;

            if(isDocked(selectedFrame)
               && desktopForDock != selectedFrame.getDesktopPane())
                undock(selectedFrame);
            if(dockLocation != CENTER && !isDocked(selectedFrame))
                dock(selectedFrame, dockLocation, desktopForDock);

            selectedFrame.setVisible(true);
            frameDragRectangle = null;
            repaint();
        }
    };

    /** For docking.
     * @serial */
    protected MouseMotionListener frameMouseMotionListener =
        new MouseMotionAdapter() {
        public void mouseDragged(MouseEvent e) {
            Component c = e.getComponent();
            Point p = getLocation(c);
            p.translate(e.getX(), e.getY());

            //What desktop area are we on?
            JDesktopPane newPane = getDesktopForDock(AppDockableDesktop.this, p);
            if(newPane != null)
                desktopForDock = newPane;

            //frame location on the manager
            frameLocation = new Point(p.x - mousePressedAt.x,
                                      p.y - mousePressedAt.y);

            //Determine dock location
            Point parentUpperLeft = getLocation(desktopForDock);
            Point parentLowerRight =
                new Point(parentUpperLeft.x + desktopForDock.getWidth(),
                          parentUpperLeft.y + desktopForDock.getHeight());

            //Does it need to be docked? Where?
            if(p.x < parentUpperLeft.x + MOUSE_SENSITIVITY
               && p.x > parentUpperLeft.x - MOUSE_SENSITIVITY)
                dockLocation = LEFT;
            else if(p.y < parentUpperLeft.y + MOUSE_SENSITIVITY
                    && p.y > parentUpperLeft.y - MOUSE_SENSITIVITY)
                dockLocation = TOP;
            else if(p.x > parentLowerRight.x - MOUSE_SENSITIVITY
                    && p.x < parentLowerRight.x + MOUSE_SENSITIVITY)
                dockLocation = RIGHT;
            else if(p.y > parentLowerRight.y - MOUSE_SENSITIVITY
                    && p.y < parentLowerRight.y + MOUSE_SENSITIVITY)
                dockLocation = BOTTOM;
            else
                dockLocation = CENTER;

            //Don't draw frame contents while dragging - perhaps buggy on Solaris
            if(!isDocked(selectedFrame))
                selectedFrame.setVisible(false);

            frameDragRectangle =
                getFrameRectangle(selectedFrame, dockLocation, desktopForDock);
            repaint();
        }
    };

    public boolean isDocked(JInternalFrame frame) {
        return(frame != null && frame.getDesktopPane() != appDesktop);
    }

    /**
     * @return location in window manager
     */
    protected Point getLocation(Component c) {
        Point p = c.getLocation();
        Container parent = c.getParent();
        while(parent != this) {
            p.translate(parent.getLocation().x, parent.getLocation().y);
            parent = parent.getParent();
        }
        return p;
    }

    /**
     * Recursive descent through component tree in search of the JDesktopPane
     * child to the specified Container and containing the specified Point.
     */
    protected JDesktopPane getDesktopForDock(Container frame, Point p) {
        if(frame == null || !frame.contains(p))
            return null;

        JDesktopPane retval = null;
        for(int i = 0; i < frame.getComponentCount() && retval == null; i++) {
            Component c = frame.getComponent(i);
            if(c instanceof JDesktopPane) {
                Rectangle rect = new Rectangle(getLocation(c), c.getSize());
                if(rect.contains(p))
                    retval = (JDesktopPane)c;
            }
            //Check both split pane components; not accessible via getComponent(int)
            else if(c instanceof JSplitPane) {
                Component split = ((JSplitPane)c).getLeftComponent();
                if(split instanceof JDesktopPane) {
                    Rectangle rect =
                        new Rectangle(getLocation(split), split.getSize());
                    if(rect.contains(p))
                        retval = (JDesktopPane)split;
                }
                else
                    retval = getDesktopForDock((Container)split, p);
                if(retval == null) {
                    split = ((JSplitPane)c).getRightComponent();
                    if(split instanceof JDesktopPane) {
                        Rectangle rect =
                            new Rectangle(getLocation(split), split.getSize());
                        if(rect.contains(p))
                            retval = (JDesktopPane)split;
                    }
                    else
                        retval = getDesktopForDock((Container)split, p);
                }
            }
            else if(c instanceof Container) {
                retval = getDesktopForDock((Container)c, p);
            }
        }
        return retval;
    }

    /**
     * For preview drawing before actually moving the frame.
     */
    protected Rectangle getFrameRectangle(JInternalFrame frame, int location,
                                          JDesktopPane desktop) {
        Dimension d = desktop.getSize();
        Dimension pref = frame.getPreferredSize();

        int width = d.width;
        int height = d.height;
        int x, y;
        if(location != CENTER) {
            Point p = getLocation(desktop);
            x = p.x;
            y = p.y;
        }
        else {
            x = frameLocation.x;
            y = frameLocation.y;
        }

        if(location == TOP) {
            height /= 2;
            if(height > pref.height)
                height = pref.height;
        }
        else if(location == RIGHT) {
            width /= 2;
            if(width > pref.width)
                width = pref.width;
            x += d.width - width;
        }
        else if(location == BOTTOM) {
            height /= 2;
            if(height > pref.height)
                height = pref.height;
            y += d.height - height;
        }
        else if(location == LEFT) {
            width /= 2;
            if(width > pref.width)
                width = pref.width;
        }
        //undocked or potentially undocked
        else if(!isDocked(frame)
                || (frame.getDesktopPane() == desktop && !frame.isMaximum())) {
            width = frame.getWidth();
            height = frame.getHeight();
        }
        else { //it's docked, so simply outline the docked window
            width = frame.getPreferredSize().width;
            height = frame.getPreferredSize().height;
        }
        return new Rectangle(x, y, width, height);
    }

    public void paint(Graphics graphics) {
        super.paint(graphics);
        Graphics2D g = (Graphics2D)graphics;

        //Paint outline of frame being moved
        if(frameDragRectangle != null) {
            g.setColor(appDesktop.getBackground().darker());
            g.drawRect(frameDragRectangle.x, frameDragRectangle.y,
                       frameDragRectangle.width, frameDragRectangle.height);
            g.drawRect(frameDragRectangle.x + 1, frameDragRectangle.y + 1,
                       frameDragRectangle.width - 2,
                       frameDragRectangle.height - 2);
        }
    }

    public void dock(final JInternalFrame frame, int location,
                     JDesktopPane desktop) {
        dockedComponents.put(frame.getContentPane(), frame);
        remove(frame);

        //Each docked frame is added to a new desktop pane
        final JDesktopPane dockDesktop = new JDesktopPane();

        //SplitPane orientation
        JSplitPane splitPane = new JSplitPane();
        if(location == BOTTOM || location == TOP)
            splitPane.setOrientation(JSplitPane.VERTICAL_SPLIT);
        else
            splitPane.setOrientation(JSplitPane.HORIZONTAL_SPLIT);

        //Store before we change its value
        Container desktopParent = desktop.getParent();

        //Add components to SplitPane
        if(location == BOTTOM || location == RIGHT) {
            splitPane.setTopComponent(desktop);
            splitPane.setBottomComponent(dockDesktop);
        }
        else if(location == TOP || location == LEFT) {
            splitPane.setTopComponent(dockDesktop);
            splitPane.setBottomComponent(desktop);
        }

        //Set splitter location
        Dimension size = desktop.getSize();
        splitPane.setSize(size);
        //System.out.println(appDesktop + "\n" + desktop + "\n" + splitPane + "\n" + location);
        setDividerLocation(splitPane,
                           frameDragRectangle == null ? null :
                           frameDragRectangle.getSize(),
                           location);

        //Add SplitPane
        desktopParent.remove(desktop);
        desktopParent.add(splitPane);

        frameBorder = frame.getBorder();
        frame.setBorder(null);
        dockDesktop.add(frame);

        dockDesktop.addComponentListener(new ComponentAdapter() {
            public void componentResized(ComponentEvent e) {
                frame.setSize(dockDesktop.getSize());
                revalidate();
            }
        });
        dockDesktop.setSize(dockDesktop.getPreferredSize());
        try {
            frame.setMaximum(true);
            frame.setMaximizable(false);
            frame.setIconifiable(false);
        }
        catch(PropertyVetoException pve) {
            pve.printStackTrace();
        }
    }

    /**
     * Explicitly set preferred sizes and such.
     */
    protected void setDividerLocation(JSplitPane splitPane,
                                      Dimension componentSize,
                                      int location) {
        Dimension size = splitPane.getSize();
        int widthLeft = size.width;
        int widthRight = size.width;
        int heightLeft = size.height;
        int heightRight = size.height;

        //If no component size specified, set divider at midpoint
        if(componentSize == null)
            componentSize = new Dimension(size.width / 2, size.height / 2);

        //Dimensions depend on dock location
        if(location == TOP) {
            heightLeft = componentSize.height;
            heightRight = size.height - heightLeft;
        }
        else if(location == BOTTOM) {
            heightRight = componentSize.height;
            heightLeft = size.height - heightRight;
        }
        else if(location == RIGHT) {
            widthRight = componentSize.width;
            widthLeft = size.width - widthRight;
        }
        else if(location == LEFT) {
            widthLeft = componentSize.width;
            widthRight = size.width - widthLeft;
        }

        ((JComponent)splitPane.getLeftComponent()).setPreferredSize(
            new Dimension(widthLeft, heightLeft));
        ((JComponent)splitPane.getRightComponent()).setPreferredSize(
            new Dimension(widthRight, heightRight));
    }

    public void undock(JInternalFrame frame) {
        dockedComponents.remove(frame.getContentPane());
        JDesktopPane frameDesktop = (JDesktopPane)frame.getParent();
        frameDesktop.remove(frame);

        JSplitPane splitPane = (JSplitPane)frameDesktop.getParent();

        //Is the frame the split pane's first component?
        boolean isLeftPane = splitPane.getLeftComponent() == frameDesktop;
        //Relevant only if not an empty JDesktopPane, and so needs to stay docked
        Component otherDockedComponent;
        if(isLeftPane)
            otherDockedComponent = splitPane.getRightComponent();
        else
            otherDockedComponent = splitPane.getLeftComponent();

        //Clear split pane
        splitPane.remove(frameDesktop);
        splitPane.remove(otherDockedComponent);
        frameDesktop = null;

        Dimension splitPaneSize = splitPane.getSize();
        appDesktop.setSize(splitPaneSize);
        appDesktop.setPreferredSize(splitPaneSize);

        Container splitPaneParent = splitPane.getParent();
        //Replace split pane with the still docked component,
        //added to the correct side of the parent split pane
        if(splitPaneParent instanceof JSplitPane) {
            JSplitPane parentSplitPane = (JSplitPane)splitPaneParent;

            boolean goesFirst =
                parentSplitPane.getLeftComponent() == splitPane;
            if(goesFirst)
                parentSplitPane.setLeftComponent(otherDockedComponent);
            else
                parentSplitPane.setRightComponent(otherDockedComponent);

            boolean isHorizontal =
                parentSplitPane.getOrientation() == JSplitPane.HORIZONTAL_SPLIT;

            //Determine location of remaining component in split pane
            //Location is the opposite of the component we're removing
            int location;
            if(isHorizontal && goesFirst)
                location = RIGHT;
            else if(isHorizontal)
                location = LEFT;
            else if(goesFirst)
                location = BOTTOM;
            else
                location = TOP;

            //Resize parent split pane
            setDividerLocation(parentSplitPane,
                               otherDockedComponent.getSize(),
                               location);
        }
        //Replace split pane with desktop
        else {
            splitPaneParent.remove(splitPane);
            splitPaneParent.add(appDesktop);
        }

        //BUG: title bar icons for maximize and iconify don't reappear!
        try {
            frame.setMaximizable(true);
            frame.setMaximum(false);
            frame.setIconifiable(true);
        }
        catch(PropertyVetoException pve) {}
        frame.setBorder(frameBorder);
        add(frame);
        frame.setLocation(frameLocation.x - appDesktop.getX(),
                          frameLocation.y - appDesktop.getY());
    }

    protected void setSelectedFrame(JInternalFrame frame) {
        frame.toFront(); //in case it's not already
        try {
            if(frame != selectedFrame && selectedFrame != null)
                selectedFrame.setSelected(false);
            frame.setSelected(true);
            if(isDefaultMaximum)
                frame.setMaximum(true);
        }
        catch(PropertyVetoException pve) {}

        Component oldFocusedComponent =
            selectedFrame == null ? null : selectedFrame.getContentPane();
        selectedFrame = frame;
        Component newFocusedComponent =
            selectedFrame == null ? null : selectedFrame.getContentPane();

        windowManagerAction.update(appDesktop.getAllFrames());

        //if(oldFocusedComponent != null)
        //	System.out.println("old:" + oldFocusedComponent.getClass().getName());
        //if(newFocusedComponent != null)
        //	System.out.println("new:" + newFocusedComponent.getClass().getName());
        firePropertyChange(PROPERTY_FOCUSED_BEAN,
                           oldFocusedComponent, newFocusedComponent);
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
    public void listWindows() {
        FormDialog dialog = new FormDialog(null);
        dialog.setForm(new WindowListForm(dialog));
        JInternalFrame retval = (JInternalFrame)dialog.showDialog();
        if(retval != null)
            setSelectedFrame(retval);
    }

    /**
     * A list of all open windows. User may select one. The selected JInternalFrame
     * is then returned by the showDialog() method.
     */
    protected class WindowListForm
        extends com.sugen.gui.form.Form {
        transient JList windowList;

        public WindowListForm(FormCarrier carrier) {
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

        public void applyChanges() {
            int selectNum = windowList.getSelectedIndex();
            if(selectNum != -1) {
                JInternalFrame frame = appDesktop.getAllFrames()[selectNum];
                setReturnValue(frame);
            }
        }
    }

    /**
     * Override to fix BUG with desktop background in UIManagers prior to jdk1.2rc2.
     */
    /*public void updateUI()
      {
     super.updateUI();

     UIManager uim = new UIManager();
     String name = uim.getLookAndFeel().getClass().getName();

     if(name.equals(UIManager.getCrossPlatformLookAndFeelClassName()))
     {
      if(appDesktop != null)
     appDesktop.setBackground(UIManager.getColor("List.selectionBackground"));
     }
     //Approximate other desktop colors
     else
      appDesktop.setBackground(new Color(130, 130, 130));
      }*/
}
