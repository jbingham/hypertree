package com.sugen.gui.io;

import java.beans.*;
import java.io.File;
import java.io.IOException;

import javax.swing.Action;

import com.sugen.event.UtilListener;
import com.sugen.gui.*;
import java.io.FileWriter;
import java.io.BufferedWriter;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.FileInputStream;

/**
 * Combines functionality of ReaderUI and WriterUI into a single class.
 *
 * @author Jonathan Bingham
 */
public class ReaderWriterUI implements AppBean, Closeable {
    public ReaderWriterUI() {
        reader.addPropertyChangeListener(this);
        writer.addPropertyChangeListener(this);
    }

    protected ReaderUI reader = new ReaderUI() {
        public Object open(Object parser, File file) throws IOException {
            return ReaderWriterUI.this.open(parser, file);
        }

        public boolean canOpen() {
            return ReaderWriterUI.this.canOpen();
        }
    };

    protected WriterUI writer = new WriterUI() {
        public void save(Object writer, Object data, File file) throws
            IOException {
            ReaderWriterUI.this.save(writer, data, file);
        }
    };

    /**
     * @return Actions from reader and writer.
     */
    public Action[] getActions() {
        return new Action[] {
            reader.getOpenAction(),
            writer.getCloseAction(),
            writer.getSaveAction(),
            writer.getSaveAsAction()};
    }

    protected PropertyChangeSupport propertySupport = new PropertyChangeSupport(this);

    public synchronized void addPropertyChangeListener(
        PropertyChangeListener pcl) {
        propertySupport.addPropertyChangeListener(pcl);
    }

    public synchronized void removePropertyChangeListener(
        PropertyChangeListener pcl) {
        propertySupport.removePropertyChangeListener(pcl);
    }

    /**
     * Forward events to and from reader/writer
     */
    public void propertyChange(PropertyChangeEvent e) {
        if(e.getSource() == reader || e.getSource() == writer) {
            propertySupport.firePropertyChange(
                new PropertyChangeEvent(this, e.getPropertyName(),
                                        e.getOldValue(), e.getNewValue()));
            //System.out.println("ReaderWriterUI.propertyChange " + e.getPropertyName());
            if(e.getSource() == reader)
                writer.propertyChange(e);
            else if(e.getSource() == writer)
                reader.propertyChange(e);
        }
        else {
            reader.propertyChange(e);
            writer.propertyChange(e);
        }
    }

    /**
     * When overriding in subclasses, first call super.open.
     * Do not call reader.open(reader, file) or an infinite loop will result.
     */
    public Object open(Object reader, File file) throws IOException {
        BufferedReader br =
            new BufferedReader(new InputStreamReader(new FileInputStream(file)));
        StringBuffer buffer = new StringBuffer();
        String line;
        while((line = br.readLine()) != null)
            buffer.append(line);
        return buffer.toString();
    }

    /**
     * When overriding in subclasses, first call super.write.
     * Do not call writer.save(writer, data, file) or an infinite loop will result.
     */
    public void save(Object writer, Object data, File file) throws IOException {
        BufferedWriter out = new BufferedWriter(new FileWriter(file));
        if(data != null)
            out.write(data.toString());
        out.flush();
        out.close();
    }

    public ReaderUI getReaderUI() {
        return reader;
    }

    public WriterUI getWriterUI() {
        return writer;
    }

    public boolean canClose() {
        return (reader.canClose() && writer.canClose());
    }

    public void close() {
        reader.close();
        writer.close();
    }

    public boolean canOpen() {
        return writer.canClose();
    }

    public synchronized void addCollectionListener(UtilListener listener) {
        reader.collectionModel.addListener(listener);
    }

    public synchronized void removeCollectionListener(UtilListener listener) {
        reader.collectionModel.removeListener(listener);
    }
}
