package com.sugen.gui.io;

import java.awt.Frame;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.beans.Beans;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Serializable;
import java.util.MissingResourceException;
import java.util.Properties;
import java.util.ResourceBundle;
import java.util.StringTokenizer;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.KeyStroke;

import com.sugen.event.CollectionEvent;
import com.sugen.event.CollectionListener;
import com.sugen.gui.AppBean;
import com.sugen.gui.Closeable;
import com.sugen.gui.Icons;

/**
 * Interactively save to file. Uses io.properties.
 *
 * <p>
 * A default writer class may be specified, in which case the WriterUI
 * will pass an instance of that writer with every call to write(Object).
 * If no default class is specified,
 * the WriterUI will interactively prompt users to pick from the allowable
 * writers defined in the io.properties file. Currently, a prefix must be specified
 * to indicate which writer names are relevant to this WriterUI. See the
 * io.properties file for examples and more information. Also, the writers
 * are typically instances of OutputWriter.
 * <p>
 * The WriterUI stores the last accessed directory
 * in the application properties file.
 *
 * @see com.sugen.io.OutputWriter
 * @author Jonathan Bingham
 */
public class WriterUI
    implements AppBean, Closeable, Serializable, CollectionListener {
    public final static String PROPERTY_PATH = "path";

    /** @serial */
    protected Frame owner;
    /** @serial */
    protected boolean needsSave;
    /** @serial */
    protected Object writer = Boolean.TRUE;
    /** @serial */
    protected Object data;
    /** @serial */
    protected ResourceBundle resources;
    /** @serial */
    protected Properties properties;
    /** @serial */
    private File currentFile;
    /** @serial */
    protected String currentPath;
    /** @serial */
    protected String saveQuery = "Save changes before closing?";

    /**
     * Prefix of keys to read from properties file. Must be set in subclasses.
     * @serial
     */
    protected String prefix = "writer";

    public WriterUI() {
        try {
            resources = ResourceBundle.getBundle("io");
        }
        catch(MissingResourceException mre) {
            System.err.println("io.properties not found");
        }
        updateActions();
    }

    public String getPrefix() {
        return prefix;
    }

    public void setPrefix(String prefix) {
        this.prefix = prefix;
    }

    /**
     * Get available parser presentation names from the properties file.
     */
    public String[] getPresentationNames() {
        String list = resources.getString(prefix + ".list");
        StringTokenizer tokenizer = new StringTokenizer(list, ";");
        String[] retval = new String[tokenizer.countTokens()];
        int i = 0;
        while(tokenizer.hasMoreElements())
            retval[i++] = (String)tokenizer.nextElement();
        return retval;
    }

    /**
     * Given the human presentation name, return the java class name.
     */
    public String getClassName(String presentationName) {
        try {
            return resources.getString(prefix + "." + presentationName);
        }
        catch(MissingResourceException mre) {
            return presentationName;
        }
    }

    /** @serial */
    protected Action saveAction = new AbstractAction("Save",
        Icons.get("save24.gif")) {
        {
            putValue(KEY_MENU, "File");
            putValue(KEY_ACCELERATOR,
                     KeyStroke.getKeyStroke(KeyEvent.VK_S, KeyEvent.CTRL_MASK));
        }

        public void actionPerformed(ActionEvent e) {
            save(currentFile);
        }
    };

    /** @serial */
    protected Action saveAsAction = new AbstractAction("Save As...") {
        {
            putValue(KEY_MENU, "File");
            putValue(KEY_LOCATION, VALUE_MENU_ONLY);
        }

        public void actionPerformed(ActionEvent e) {
            save(null);
        }
    };

    public Action getCloseAction() {
        return closeAction;
    }

    /** @serial */
    protected Action closeAction = new AbstractAction("Close",
        Icons.get("close24.gif")) {
        {
            putValue(KEY_MENU, "File");
            putValue(KEY_ACCELERATOR,
                     KeyStroke.getKeyStroke(KeyEvent.VK_W, KeyEvent.CTRL_MASK));
            putValue(KEY_LOCATION, VALUE_MENU_ONLY);
            putValue(KEY_SEPARATOR_AFTER, Boolean.TRUE);
        }

        public void actionPerformed(ActionEvent e) {
            if(canClose())
                close();
        }
    };

    /**
     * Set data to save.
     */
    public void setData(Object obj) {
        needsSave = (obj != null) && (data != null);
        //System.out.println(getClass().getName() + " " + needsSave);
        data = obj;
        updateActions();
    }

    public Object getData() {
        return data;
    }

    /**
     * Set to null to force the user to choose a writer as specified in
     * io.properties.
     */
    public void setWriter(Object writer) {
        this.writer = writer;
    }

    public Object getWriter() {
        return writer;
    }

    /**
     * User will pick a file and a parser, if none has been programmatically set.
     */
    public void save(File file) {
        Object oldWriter = writer;
        if(writer == null)
            chooseWriter();
        if(writer == null)
            return;

        try {
            if(file == null) {
                JFileChooser chooser = new JFileChooser(getPath());
                if(chooser.showSaveDialog(owner) != JFileChooser.APPROVE_OPTION)
                    return;
                file = chooser.getSelectedFile();
                currentFile = file;
                currentPath = file.getAbsolutePath();
            }

            propertySupport.firePropertyChange(PROPERTY_STATUS_MESSAGE, null,
                                               "Saving");
            save(writer, data, file);
            propertySupport.firePropertyChange(PROPERTY_STATUS_MESSAGE, null,
                                               "Ready");
            propertySupport.firePropertyChange(PROPERTY_FILE, null, currentFile);
            propertySupport.firePropertyChange(PROPERTY_PATH, null, currentPath);

            needsSave = false;
        }
        catch(Exception e) {
            propertySupport.firePropertyChange(PROPERTY_STATUS_MESSAGE, null,
                                               e.getClass().getName() + " " +
                                               e.getMessage());
            e.printStackTrace();
        }
        finally {
            writer = oldWriter;
        }
    }

    public File getCurrentFile() {
        return currentFile;
    }

    /**
     * Write the data to file.
     * By default, the data's toString() method is written, ignoring the
     * writer parameter.
     */
    protected void save(Object writer, Object data, File file) throws
        IOException {
        BufferedWriter out = new BufferedWriter(new FileWriter(file));
        if(data != null)
            out.write(data.toString());
        out.flush();
        out.close();
    }

    /**
     * Allow user to interactively select a TreeParser.
     */
    public Object chooseWriter() {
        updateActions();
        String[] writers = getPresentationNames();
        Object retval = JOptionPane.showInputDialog(owner,
            "Choose file format:",
            "Choose File Format",
            JOptionPane.INFORMATION_MESSAGE,
            null,
            writers, writers[0]);
        if(retval == null)
            return null;

        String writerClassName = getClassName((String)retval);
        ClassLoader cl = WriterUI.class.getClassLoader();
        Object writer = null;
        try {
            writer = Beans.instantiate(cl, writerClassName);
        }
        catch(Exception e) {}
        setWriter(writer);
        return writer;
    }

    public Action[] getActions() {
        return new Action[] {
            closeAction, saveAction, saveAsAction};
    }

    public Action getSaveAction() {
        return saveAction;
    }

    public Action getSaveAsAction() {
        return saveAsAction;
    }

    /** @serial */
    protected PropertyChangeSupport propertySupport = new PropertyChangeSupport(this);

    public synchronized void addPropertyChangeListener(
        PropertyChangeListener pcl) {
        propertySupport.addPropertyChangeListener(pcl);
    }

    public synchronized void removePropertyChangeListener(
        PropertyChangeListener pcl) {
        propertySupport.removePropertyChangeListener(pcl);
    }

    public void propertyChange(PropertyChangeEvent e) {
        String name = e.getPropertyName();
        if(PROPERTY_OWNER.equals(name))
            owner = (Frame)e.getNewValue();
        else if(PROPERTY_PROPERTIES.equals(name))
            setProperties((Properties)e.getNewValue());
        else if(PROPERTY_DATA.equals(name))
            setData(e.getNewValue());
        else if(PROPERTY_FILE.equals(name))
            setCurrentFile(new File(String.valueOf(e.getNewValue())));
        else if(PROPERTY_PATH.equals(name))
            setPath(String.valueOf(e.getNewValue()));
    }

    protected void updateActions() {
        boolean hasData = (data != null);
        saveAction.setEnabled(hasData && needsSave);
        saveAsAction.setEnabled(hasData);
        closeAction.setEnabled(hasData);
    }

    public boolean canClose() {
        if(!needsSave)
            return true;

        //Prompt user to save first
        int retval =
            JOptionPane.showConfirmDialog(owner, saveQuery);
        if(retval == JOptionPane.CANCEL_OPTION)
            return false;
        else if(retval == JOptionPane.NO_OPTION)
            return true;
        else {
            save(currentFile); // allow user to save
            return !needsSave; // value may be updated by call to save(file)
        }
    }

    public void close() {
        setData(null);
        propertySupport.firePropertyChange(PROPERTY_STATUS_MESSAGE, null,
                                           "Ready");
        propertySupport.firePropertyChange(PROPERTY_FILE, null, null);
        propertySupport.firePropertyChange(PROPERTY_DATA, null, null);
    }

    protected void setProperties(Properties props) {
        properties = props;
        currentPath = props.getProperty(PROPERTY_PATH);
    }

    public void setPath(String path) {
        if(properties != null)
            properties.setProperty(PROPERTY_PATH, path);
        else
            currentPath = path;
    }

    public void setCurrentFile(File file) {
        currentFile = file;
    }

    public String getPath() {
        if(properties != null)
            return properties.getProperty(PROPERTY_PATH);
        else
            return currentPath;
    }

    public void collectionAdded(CollectionEvent e) {
        collectionChanged(e);
    }

    public void collectionRemoved(CollectionEvent e) {
        collectionChanged(e);
    }

    public void collectionChanged(CollectionEvent e) {
        setData(e.getCollection());
    }
}
