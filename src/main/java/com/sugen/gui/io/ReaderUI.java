package com.sugen.gui.io;

import java.awt.Frame;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.beans.Beans;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.MissingResourceException;
import java.util.Properties;
import java.util.ResourceBundle;
import java.util.StringTokenizer;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.KeyStroke;

import com.sugen.event.UtilListener;
import com.sugen.gui.AppBean;
import com.sugen.gui.Closeable;
import com.sugen.gui.Icons;
import com.sugen.util.CollectionModel;

/**
 * Interactively read from file.
 * Remembers recently accessed files, current directory, etc.
 * <p>
 * A default reader class may be specified, in which case the ReaderUI
 * will only call <pre>open(Object, File)</pre> using that reader as a parameter.
 * If no default class is specified,
 * the ReaderUI will interactively prompt users to pick from the allowable
 * readers defined in the io.properties file. A prefix must be specified
 * to indicate which reader names are relevant to this ReaderUI. See the
 * io.properties file for examples and more information. Typically, the
 * default reader and readers in the properties file are instances of
 * InputReader.
 * <p>
 * The reader stores recent files and the reader parameters used to open
 * them in the application properties file.
 *
 * @see com.sugen.io.InputReader
 * @author Jonathan Bingham
 */
public class ReaderUI implements AppBean, Closeable, Serializable {
    public final static String PROPERTY_PATH = "path";

    /** @serial */
    protected Frame owner;
    /** @serial */
    protected Object reader = Boolean.TRUE;
    /** @serial */
    protected ResourceBundle resources;
    /** @serial */
    protected Properties properties;
    /** @serial */
    protected String currentPath;

    /**
     * For notifying listeners of newly read data.
     * @serial
     */
    protected CollectionModel collectionModel = new CollectionModel();

    /**
     * Prefix of keys read from properties file. Must be set in subclasses.
     * @serial
     */
    protected String prefix = "reader";
    /** @serial */
    protected int filesRemembered = 4;
    /** @serial */
    protected List recentFiles = new ArrayList(filesRemembered);
    /** @serial */
    protected Map recentReaders = new HashMap(filesRemembered + 1);

    public ReaderUI() {
        try {
            resources = ResourceBundle.getBundle("io");
        }
        catch(MissingResourceException mre) {
            System.err.println("io.properties not found");
        }
    }

    public String getPrefix() {
        return prefix;
    }

    public void setPrefix(String prefix) {
        this.prefix = prefix;
    }

    public int getFilesRemembered() {
        return filesRemembered;
    }

    public void setFilesRemembered(int i) {
        filesRemembered = i;
    }

    /**
     * Get available reader presentation names from the properties file.
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
    protected Action openAction = new AbstractAction("Open...",
        Icons.get("open24.gif")) {
        {
            putValue(KEY_MENU, "File");
            putValue(KEY_ACCELERATOR,
                     KeyStroke.getKeyStroke(KeyEvent.VK_O, KeyEvent.CTRL_MASK));
        }

        public void actionPerformed(ActionEvent e) {
            if(canOpen())
                open();
        }
    };

    /** @serial */
    protected RecentFilesAction recentFilesAction = new RecentFilesAction();

    protected class RecentFilesAction
        extends AbstractAction {
        RecentFilesAction() {
            putValue(KEY_MENU, FILE_MENU);
            putValue(KEY_LOCATION, VALUE_MENU_ONLY);
            putValue(KEY_ACTION_TYPE, VALUE_BUTTON_GROUP);
        }

        void update(Object[] files) {
            int nFiles = files.length;
            String[] names = new String[nFiles];
            for(int i = 0; i < nFiles; i++)
                names[i] = (i + 1) + " " + files[i].toString();
            putValue(NAME, names);
            if(names.length > 0)
                putValue(KEY_SELECTED_STATE, null);

            firePropertyChange(PROPERTY_ACTION_CHANGED, null, RecentFilesAction.this);
        }

        public void actionPerformed(ActionEvent e) {
            if(!canOpen())
                return;

            String file = e.getActionCommand().substring(2);
            try {
                recentFiles.remove(file);
                Object recentReader = recentReaders.get(file);
                Object fileContents = null;
                if(recentReader != null) {
                    recentReader = Beans.instantiate(getClass().getClassLoader(),
                        (String)recentReader);
                    fileContents = open(recentReader, new File(file));
                }
                else if(reader != null)
                    fileContents = open(reader, new File(file));
                else
                    throw new IOException();
                recentFiles.add(0, file);

                // First clear any old data
                propertySupport.firePropertyChange(PROPERTY_DATA, null, null);

                // Then notify of new data
                propertySupport.firePropertyChange(PROPERTY_FILE, null, file);
                propertySupport.firePropertyChange(PROPERTY_PATH, null, currentPath);
                propertySupport.firePropertyChange(PROPERTY_DATA, null,
                    fileContents);
            }
            catch(Exception ioe) {
                JOptionPane.showMessageDialog(owner,
                                              "Unable to open file " + file
                                              + "!", "Error",
                                              JOptionPane.ERROR_MESSAGE);
                ioe.printStackTrace();
            }
            update(recentFiles.toArray());
        }
    }

    public Action getRecentFilesAction() {
        return recentFilesAction;
    }

    /**
     * Set it to null to force user to choose a reader specified in the
     * io.properties file.
     */
    public void setReader(Object reader) {
        this.reader = reader;
    }

    public Object getReader() {
        return reader;
    }

    /**
     * User will pick a file and a reader, if none has been programmatically set.
     */
    public Object open() {
        Object oldReader = reader;
        if(reader == null)
            chooseReader();
        if(reader == null)
            return null;

        try {
            JFileChooser chooser = new JFileChooser(getPath());
            if(chooser.showOpenDialog(owner) != JFileChooser.APPROVE_OPTION)
                return null;
            File file = chooser.getSelectedFile();
            currentPath = file.getAbsolutePath();

            propertySupport.firePropertyChange(PROPERTY_STATUS_MESSAGE, null,
                                               "Loading");
            Object data = open(reader, file);

            setCurrentFile(file);

            propertySupport.firePropertyChange(PROPERTY_STATUS_MESSAGE, null,
                                               "Ready");
            propertySupport.firePropertyChange(PROPERTY_DATA, null, data);
            propertySupport.firePropertyChange(PROPERTY_FILE, null, file);
            propertySupport.firePropertyChange(PROPERTY_PATH, null, currentPath);
            return data;
        }
        catch(Exception e) {
            propertySupport.firePropertyChange(PROPERTY_STATUS_MESSAGE, null,
                                               e.getClass().getName() + " " +
                                               e.getMessage());
            e.printStackTrace();
            return null;
        }
        finally {
            reader = oldReader;
        }
    }

    public String getCurrentFile() {
        return currentPath;
    }

    protected void setCurrentFile(File file) {
        if(file == null)
            return;

        currentPath = file.getAbsolutePath();
        if(properties != null)
            properties.put(PROPERTY_PATH, currentPath);

        recentFiles.remove(file.toString());
        recentFiles.add(0, file.toString());
        recentReaders.put(file.toString(), reader.getClass().getName());
        if(recentFiles.size() > filesRemembered) {
            String lastFile = (String)recentFiles.get(filesRemembered - 1);
            recentReaders.remove(lastFile);
            recentFiles.remove(lastFile);
        }
        recentFilesAction.update(recentFiles.toArray());
    }

    /**
     * By default, open all files as unparsed ASCII.
     * Override to parse and return some other data object.
     * @return a string version of the contents of the file.
     */
    protected Object open(Object reader, File file) throws IOException {
        BufferedReader br =
            new BufferedReader(new InputStreamReader(new FileInputStream(file)));
        StringBuffer buffer = new StringBuffer();
        String line;
        while((line = br.readLine()) != null)
            buffer.append(line);
        return buffer.toString();
    }

    /**
     * Allow user to interactively select a TreeParser.
     */
    public Object chooseReader() {
        String[] readers = getPresentationNames();
        Object retval = JOptionPane.showInputDialog(owner,
            "Choose file format:",
            "Choose File Format",
            JOptionPane.INFORMATION_MESSAGE,
            null,
            readers, readers[0]);
        if(retval == null)
            return null;

        String readerClassName = getClassName((String)retval);
        ClassLoader cl = ReaderUI.class.getClassLoader();
        Object reader = null;
        try {
            reader = Beans.instantiate(cl, readerClassName);
        }
        catch(Exception e) {}
        setReader(reader);
        return reader;
    }

    public Action[] getActions() {
        return new Action[] {
            openAction};
    }

    public Action getOpenAction() {
        return openAction;
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
        else if(PROPERTY_PATH.equals(name))
            currentPath = String.valueOf(e.getNewValue());
    }

    protected void setProperties(Properties props) {
        properties = props;
        if(props == null)
            return;

        currentPath = props.getProperty(PROPERTY_PATH);
        for(int i = 1; i <= filesRemembered; i++) {
            String file = props.getProperty("file." + i);
            if(file == null)
                break;
            recentFiles.add(file);
            String reader = props.getProperty("file." + i + ".reader");
            if(reader != null)
                recentReaders.put(file, reader);
        }
        recentFilesAction.update(recentFiles.toArray());
    }

    public boolean canClose() {
        return true;
    }

    public void close() {
        if(properties == null)
            return;
        for(int i = 0; i < recentFiles.size(); i++) {
            String file = (String)recentFiles.get(i);
            properties.put("file." + (i + 1), file);

            String reader = (String)recentReaders.get(file);
            if(reader != null)
                properties.put("file." + (i + 1) + ".reader", reader);
        }
    }

    /**
     * Override if checks need to be performed before opening. Eg, if
     * only one file can be open at a time, and the first needs to be
     * closed.
     */
    public boolean canOpen() {
        return true;
    }

    public void setPath(String path) {
        if(properties != null)
            properties.put(PROPERTY_PATH, path);
        else
            currentPath = path;
    }

    public String getPath() {
        if(properties != null)
            return properties.getProperty(PROPERTY_PATH);
        else
            return currentPath;
    }

    public synchronized void addCollectionListener(UtilListener listener) {
        collectionModel.addListener(listener);
    }

    public synchronized void removeCollectionListener(UtilListener listener) {
        collectionModel.removeListener(listener);
    }

    public Collection getData() {
        return collectionModel;
    }
}
