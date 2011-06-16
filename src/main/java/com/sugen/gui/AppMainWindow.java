package com.sugen.gui;

import javax.swing.*;
import javax.swing.border.*;

import java.awt.*;
import java.awt.event.*;
import java.beans.*;
import java.io.*;
import java.util.*;

/**
 * An Applet/Application, with many niceties built in.
 * <P>
 * Simply add beans to it using addBean(AppBean), and each
 * will be given the appropriate menu and
 * toolbar controls, plus integrated with the status bar! This can be customized
 * some to control the grouping of controls. Beyond that,
 * you can customize by calling addMenuItem(Action) or addToolButton(Action)
 * directly instead of using addBean(AppBean).
 * <P>
 * The app is itself an AppBean, meaning that it has its own controls and,
 * to use these, you must either call addBean(beanApp) or add the Actions
 * individually. Typically you will want to
 * add the app to itself <b>last</b>, after you have added the other beans,
 * because, by convention, Exit is the last option in the File menu.
 * <P>
 * To create a splash page/bootup screen, simply specify the label to use.
 *
 * @see AppMenuBar
 * @see AppStatusBar
 * @see AppToolBar
 * @see AppBean
 * @see AppDesktop
 * @see AppClipboard
 *
 * @author Jonathan Bingham
 */
public class AppMainWindow
    extends JFrame implements AppBean, PropertyChangeListener, Closeable {
    public final static String WINDOWS_LAF =
        "com.sun.java.swing.plaf.windows.WindowsLookAndFeel";
    public final static String MOTIF_LAF =
        "com.sun.java.swing.plaf.motif.MotifLookAndFeel";
    public final static String JAVA_LAF =
        "javax.swing.plaf.metal.MetalLookAndFeel";
    public final static String MAC_LAF =
        "com.sun.java.swing.plaf.mac.MacLookAndFeel";

    //Properties stored in the application properties file
    public final static String PROPERTY_LOOK_AND_FEEL = "lookAndFeel";
    public static final String PROPERTY_WIDTH = "width";
    public static final String PROPERTY_HEIGHT = "height";
    public static final String PROPERTY_X = "x";
    public static final String PROPERTY_Y = "y";

    /** @serial */
    protected String titlePrefix;

    /** @serial */
    protected JWindow splashWindow;
    /** @serial */
    protected JLabel splashLabel = null;
    /** @serial */
    protected int beanProgressMax = 0;
    /** @serial */
    protected JProgressBar beanProgressBar;

    /** @serial */
    protected java.util.List beans = new ArrayList();
    /** @serial */
    protected boolean isRootThread = true;
    /** @serial */
    protected JApplet applet = null;

    /** @serial */
    protected AppMenuBar menuBar;

    /** @serial */
    protected AppToolBar toolBar;
    /** @serial */
    protected AppStatusBar statusBar;
    /** @serial */
    protected Properties properties;
    /** @serial */
    protected File propertiesFile;

    public AppMainWindow(String title) {
        this(title, null);
    }

    public AppMainWindow(String title, JLabel splashLabel) {
        this(title, splashLabel, 0);
    }

    /**
     * @param nbeans number of beans to be added; if specified, the initial
     * splash screen will have a progress bar incremented each time a bean is
     * added
     */
    public AppMainWindow(String title, JLabel splashLabel, int nbeans) {
        super(title);

        titlePrefix = title;

        beanProgressMax = nbeans;
        setSplashLabel(splashLabel);
        if(splashLabel != null)
            about();

        setSize(720, 600);
        Dimension screenSize = getToolkit().getScreenSize();
        setLocation(0 + (screenSize.width - getWidth()) / 2,
                    0 + (screenSize.height - getHeight()) / 2);

        menuBar = new AppMenuBar();
        setAppMenuBar(menuBar);
        toolBar = new AppToolBar();
        setAppToolBar(toolBar);

        statusBar = new AppStatusBar();
        setAppStatusBar(statusBar);

        //Detect escape and pass on to anyone interested
        //BUG: in jdk1.2, focus never gained, so key action never fired
        menuBar.registerKeyboardAction(
            new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                cancel();
            }
        },
            KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0),
            JComponent.WHEN_IN_FOCUSED_WINDOW);

        //Perform check before closing
        setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent e) {
                if(canClose()) {
                    close();
                    exit();
                }
            }
        });

        //Waiting cursor when glasspane is visible
        getGlassPane().addMouseListener(new MouseAdapter() {});
        getGlassPane().setCursor(new Cursor(Cursor.WAIT_CURSOR));
    }

    /**
     * Called on ESC.
     */
    protected void cancel() {
        firePropertyChange(PROPERTY_CANCELED, null, Boolean.TRUE);
    }

    public void setAppMenuBar(AppMenuBar menubar) {
        this.menuBar = menubar;
        setJMenuBar(menuBar);
    }

    public AppMenuBar getAppMenuBar() {
        return menuBar;
    }

    public void setAppToolBar(AppToolBar toolBar) {
        this.toolBar = toolBar;
        getContentPane().add(toolBar, BorderLayout.NORTH);
    }

    public AppToolBar getAppToolBar() {
        return toolBar;
    }

    public void setAppStatusBar(AppStatusBar statusBar) {
        this.statusBar = statusBar;
        getContentPane().add(statusBar, BorderLayout.SOUTH);
    }

    public AppStatusBar getAppStatusBar() {
        return statusBar;
    }

    /**
     * Load application properties from the specified (relative) file name.
     * The current directory will be searched first, followed by all
     * directories defined in the class path, in order.
     */
    public void loadProperties(String fileName) throws IOException {
        //Could implement all of this using a PropertyResourceBundle, right?

        properties = new Properties();

        //Check current directory first
        File file = new File(fileName);

        //Check user home
        if(!file.exists())
            file = new File(System.getProperty("user.home")
                            + File.separator + fileName);
        propertiesFile = file;

        //Check in classpath, in case it's in an install directory
        if(!file.exists()) {
            String classpath = System.getProperty("java.class.path");
            StringTokenizer tokenizer =
                new StringTokenizer(classpath, File.pathSeparator);
            while(tokenizer.hasMoreTokens())
                file = new File(tokenizer.nextToken() + File.separator +
                                fileName);
            if(file.exists())
                propertiesFile = file;
        }
        //Create new file in user home
        if(!file.exists())
            propertiesFile.createNewFile();
        //Load
        else
            properties.load(new FileInputStream(propertiesFile));
        setProperties(properties);

        propertySupport.firePropertyChange(PROPERTY_PROPERTIES, null,
                                           properties);
    }

    protected void setProperties(Properties props) {
        if(props == null)
            return;
        //Set properties from the file

        //Size
        String width = props.getProperty(PROPERTY_WIDTH);
        String height = props.getProperty(PROPERTY_HEIGHT);
        try {
            if(width != null && height != null)
                setSize(Integer.parseInt(width), Integer.parseInt(height));
        }
        catch(NumberFormatException e) {
            e.printStackTrace();
        }

        //Location
        String x = props.getProperty(PROPERTY_X);
        String y = props.getProperty(PROPERTY_Y);
        try {
            if(x != null && y != null)
                setLocation(Integer.parseInt(x), Integer.parseInt(y));
        }
        catch(NumberFormatException e) {
            e.printStackTrace();
        }

        String laf = props.getProperty(PROPERTY_LOOK_AND_FEEL);
        if(laf != null)
            setLookAndFeel(laf);
    }

    protected void saveProperties() throws IOException {
        if(properties == null || propertiesFile == null)
            return;

        properties.setProperty(PROPERTY_WIDTH, Integer.toString(this.getWidth()));
        properties.setProperty(PROPERTY_HEIGHT, Integer.toString(getHeight()));
        properties.setProperty(PROPERTY_X, Integer.toString(getX()));
        properties.setProperty(PROPERTY_Y, Integer.toString(getY()));
        properties.setProperty(PROPERTY_LOOK_AND_FEEL,
                               UIManager.getLookAndFeel().getClass().getName());

        properties.store(new FileOutputStream(propertiesFile), null);
    }

    public Properties getProperties() {
        return properties;
    }

    public void setTitle(String newTitle) {
        //System.out.println("setTitle " + newTitle);
        String oldTitle = getTitle();
        if(newTitle == null)
            newTitle = "";
        super.setTitle(titlePrefix + " - " + newTitle);
        propertySupport.firePropertyChange(PROPERTY_TITLE, oldTitle, getTitle());
    }

    public void setSplashLabel(JLabel label) {
        splashLabel = label;
    }

    public JLabel getSplashLabel() {
        return splashLabel;
    }

    /**
     * Add a bean to the app. Set menu and tool commands based on the bean's
     * Actions. Listen for property changes to the bean.
     */
    public void addBean(AppBean bean) {
        addBean(bean, false, false);
    }

    /**
     * Add a bean to the app. Set menu and tool commands based on the bean's
     * Actions. Listen for property changes to the bean.
     */
    public void addBean(AppBean bean, boolean addMenuSeparator,
                        boolean newToolBar) {
        bean.addPropertyChangeListener(this);

        //Tell listeners that an owner is available
        bean.propertyChange(
            new PropertyChangeEvent(this, PROPERTY_OWNER, null, this));

        //Notify of current properties
        if(properties != null) {
            PropertyChangeEvent pce =
                new PropertyChangeEvent(this, PROPERTY_PROPERTIES,
                                        null, properties);
            bean.propertyChange(pce);
        }

        incrementBeanProgress();
        beans.add(bean);

        Action[] actions = bean.getActions();
        if(actions == null)
            return;

        menuBar.addBean(bean, addMenuSeparator);
        toolBar.addBean(bean, newToolBar);
    }

    public void setApplet(JApplet applet) {
        this.applet = applet;
    }

    public JApplet getApplet() {
        return applet;
    }

    public void incrementBeanProgress() {
        if(beanProgressMax > 0)
            beanProgressBar.setValue(beanProgressBar.getValue() + 1);
    }

    /**
     * If not the root thread, exiting this app will not exit the system, only
     * dispose of the app window, terminating its thread.
     */
    public void setRootThread(boolean b) {
        isRootThread = b;
    }

    /**
     * If not the root thread, exiting this app will not exit the system, only
     * dispose of the app window, terminating its thread.
     */
    public boolean isRootThread() {
        return isRootThread;
    }

    /**
     * Overridden to close the splash page.
     */
    public void setVisible(boolean b) {
        if(splashWindow != null) {
            splashWindow.setVisible(false);
            splashWindow.dispose();
        }
        //if(b)
        //	centerOnScreen();
        menuBar.setVisible(b); //HACK, so method will be called
        super.setVisible(b);
    }

    /**
     * Show splash screen using the SplashIcon.
     */
    public void about() {
        splashWindow = new JWindow(this);
        final JPanel panel = new JPanel(new BorderLayout());
        splashWindow.setContentPane(panel);

        if(splashLabel == null) {
            splashLabel =
                new JLabel("Assembled from the Sugen Component Library");
            Dimension size = splashLabel.getPreferredSize();
            splashLabel.setPreferredSize(new Dimension(size.width,
                size.height * 4));
        }

        if(isVisible()) //it's clickable, so should have an etched border
            panel.setBorder(new EtchedBorder());

        //If it's opened by the user rather than when the program is
        //launching, the user needs to be able to close it
        if(isVisible()) {
            getGlassPane().setVisible(true);
            splashLabel.addMouseListener(new MouseAdapter() {
                public void mouseClicked(MouseEvent e) {
                    if(e.isAltDown() && e.isControlDown()) {
                        JLabel eeLabel = new JLabel();
                        splashLabel.setIcon(null);
                        java.awt.Color c = splashLabel.getBackground();
                        eeLabel.setOpaque(true);
                        eeLabel.setBackground(splashLabel.getForeground());
                        eeLabel.setForeground(c);
                        eeLabel.setBorder(new CompoundBorder(
                            new BevelBorder(BevelBorder.RAISED),
                            new BevelBorder(BevelBorder.LOWERED)));
                        eeLabel.setFont(new Font("sansserif", Font.BOLD, 14));
                        eeLabel.setText(
                            "Components by Jonathan Bingham, 1998-9");
                        eeLabel.addMouseListener(new MouseAdapter() {
                            public void mouseClicked(MouseEvent e) {
                                getGlassPane().setVisible(false);
                                splashWindow.setVisible(false);
                                splashWindow.dispose();
                            }
                        });
                        panel.add(eeLabel);
                        splashWindow.pack();
                        panel.revalidate();
                        panel.repaint();
                        return;
                    }
                    getGlassPane().setVisible(false);
                    splashWindow.setVisible(false);
                    splashWindow.dispose();
                }
            });
        }
        panel.add(splashLabel);

        //Add a progress bar below the splash label
        if(!isVisible() && beanProgressMax > 0) {
            beanProgressBar = new JProgressBar();
            beanProgressBar.setMaximum(beanProgressMax + 1);
            JPanel beanPanel = new JPanel(new BorderLayout());
            beanPanel.setBorder(new EmptyBorder(2, 40, 2, 40));
            beanPanel.add(beanProgressBar);
            panel.add(beanPanel, BorderLayout.SOUTH);
            beanProgressBar.setValue(1); //Let user think something's already happened.
        }

        Dimension dim = panel.getPreferredSize();
        Point loc; //owner's location
        Dimension size; //owner's size

        //Center on top of application window
        if(isVisible()) {
            loc = getLocation();
            size = getSize();
        }
        //Or center on the screen if application hasn't been launched yet.
        else {
            loc = new Point();
            size = getToolkit().getScreenSize();
        }
        splashWindow.setLocation(loc.x + (size.width - dim.width) / 2,
                                 loc.y + (size.height - dim.height) / 2);
        splashWindow.setSize(dim);
        splashWindow.setVisible(true);
    }

    public Action[] getActions() {
        return new Action[] {
            exitAction, aboutAction, lookAndFeelAction};
    }

    /** @serial */
    protected Action aboutAction = new AbstractAction("About...",
        Icons.get("about24.gif")) {
        {
            putValue(KEY_MENU, "Help");
            putValue(KEY_LOCATION, VALUE_MENU_ONLY);
        }

        public void actionPerformed(ActionEvent e) {
            about();
        }
    };

    public Action getAboutAction() {
        return aboutAction;
    }

    /** @serial */
    protected Action closeAction = new AbstractAction("Close",
        Icons.get("close24.gif")) {
        {
            putValue(KEY_MENU, "File");
            putValue(KEY_LOCATION, VALUE_MENU_ONLY);
            putValue(KEY_ACCELERATOR,
                     KeyStroke.getKeyStroke(KeyEvent.VK_W, KeyEvent.CTRL_MASK));
        }

        public void actionPerformed(ActionEvent e) {
            if(canClose())
                close();
        }
    };

    public Action getCloseAction() {
        return closeAction;
    }

    /** @serial */
    protected Action exitAction = new AbstractAction("Exit",
        Icons.get("exit24.gif")) {
        {
            putValue(KEY_MENU, "File");
            putValue(KEY_LOCATION, VALUE_MENU_ONLY);
        }

        public void actionPerformed(ActionEvent e) {
            if(canClose()) {
                close();
                exit();
            }
        }
    };

    public Action getExitAction() {
        return exitAction;
    }

    public boolean canClose() {
        boolean retval = true;
        Iterator iterator = beans.iterator();
        while(iterator.hasNext() && retval) {
            Object bean = iterator.next();
            if(bean instanceof Closeable && !bean.equals(this))
                retval = ((Closeable)bean).canClose();
        }
        return retval;
    }

    public void close() {
        Iterator iterator = beans.iterator();
        while(iterator.hasNext()) {
            Object bean = iterator.next();
            if(bean instanceof Closeable && !bean.equals(this))
                ((Closeable)bean).close();
        }
        setTitle(null);
    }

    /**
     * Exits without notifying other beans. To notify, first call
     * canClose() and close().
     */
    public void exit() {
        setVisible(false);
        if(applet == null) {
            try {
                saveProperties();
            }
            catch(IOException e) {
                e.printStackTrace();
            }
            dispose();
        }
        else
            applet.stop();
        if(isRootThread)
            System.exit(0);
    }

    public void setWaiting(boolean b) {
        getGlassPane().setVisible(b);
    }

    /** @serial */
    protected Action lookAndFeelAction = new AbstractAction("Look and Feel...") {
        {
            putValue(KEY_MENU, VIEW_MENU);
            putValue(KEY_LOCATION, VALUE_MENU_ONLY);
        }

        public void actionPerformed(ActionEvent e) {
            setLookAndFeel();
        }
    };

    public Action getLookAndFeelAction() {
        return lookAndFeelAction;
    }

    public String getLookAndFeel() {
        return UIManager.getLookAndFeel().getName();
    }

    /**
     * Interactively set it.
     * Currently lets user choose only Java, Windows, Mac and Motif L&F.
     * Override this method to support non-standard looks and feels.
     */
    public void setLookAndFeel() {
        final String JAVA = "Java";
        final String MOTIF = "Motif";
        final String MAC = "Mac";
        final String WINDOWS = "Windows";

        //Figure out permissible L&F on user's machine
        Object[] choices;
        String osName = System.getProperty("os.name");
        if(osName.startsWith(WINDOWS))
            choices = new String[] {
                JAVA, MOTIF, WINDOWS};
        else if(osName.startsWith(MAC))
            choices = new String[] {
                JAVA, MAC, MOTIF};
        else
            choices = new String[] {
                JAVA, MOTIF};

        //Let user pick L&F
        Object selection = JOptionPane.showInputDialog(this,
            "Choose look and feel:", "Look and Feel",
            JOptionPane.INFORMATION_MESSAGE, null, choices, choices[0]);
        if(selection == null)
            return;

        //Get corresponding class name
        String lnf = null;
        if(selection.equals(JAVA))
            lnf = JAVA_LAF;
        else if(selection.equals(MOTIF))
            lnf = MOTIF_LAF;
        else if(selection.equals(MAC))
            lnf = MAC_LAF;
        else if(selection.equals(WINDOWS))
            lnf = WINDOWS_LAF;
        setLookAndFeel(lnf);
    }

    public void setNativeLookAndFeel() {
        setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
    }

    public void setLookAndFeel(String lnf) {
        try {
            UIManager.setLookAndFeel(lnf);
            SwingUtilities.updateComponentTreeUI(this);
        }
        catch(Exception e) {
            propertySupport.firePropertyChange(PROPERTY_STATUS_MESSAGE, null,
                                               "Error: Unsupported Look and Feel: " +
                                               lnf);
        }
    }

    /** @serial */
    protected PropertyChangeSupport propertySupport = new PropertyChangeSupport(this);

    public synchronized void addPropertyChangeListener(PropertyChangeListener
        pcl) {
        propertySupport.addPropertyChangeListener(pcl);
    }

    public synchronized void removePropertyChangeListener(
        PropertyChangeListener pcl) {
        propertySupport.removePropertyChangeListener(pcl);
    }

    /**
     * One of the beans has changed a property.
     */
    public void propertyChange(PropertyChangeEvent e) {
        //System.out.println(e.getPropertyName());
        String name = e.getPropertyName();
        if(PROPERTY_WAITING.equals(name))
            setWaiting(Boolean.TRUE.equals(e.getNewValue()));
        else if(PROPERTY_FILE.equals(name))
            setTitle(e.getNewValue() == null ? null : e.getNewValue().toString());
        else if(PROPERTY_DATABASE.equals(name))
            setTitle(e.getNewValue() == null ? null : e.getNewValue().toString());
        //else if(PROPERTY_TITLE.equals(name) && e.getSource() != this)
        //	setTitle((String)e.getNewValue());
        else
            statusBar.propertyChange(e);
    }
}
