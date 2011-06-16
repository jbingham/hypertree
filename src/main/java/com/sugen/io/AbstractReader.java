package com.sugen.io;

import java.io.*;
import java.util.*;

/**
 * Basic implementation of an InputReader designed to work with ASCII files.
 * Defaults to reading stdin.
 *
 * @author Jonathan Bingham
 */
abstract public class AbstractReader
    implements InputReader {
    protected BufferedReader reader;
    protected Object input;

    /**
     * Last line unread from the input stream.
     */
    protected String lineUnread;
    protected String lastLineRead;

    /**
     *
     */
    public AbstractReader() {
        reader = new BufferedReader(new InputStreamReader(System.in));
    }

    /**
     * Class of Objects created by this reader. The Iterator.next() method
     * will return Objects of this Class type. The readAll() method will
     * return a Collection of Objects of this Class type.
     * @return Object.class
     */
    public Class getInputClass() {
        return Object.class;
    }

    /**
     * New parser on an input stream.
     */
    public AbstractReader(InputStream is) throws IOException {
        setInput(is);
    }

    public void setInput(InputStream is) {
        reader = new BufferedReader(new InputStreamReader(is));
        input = null;
    }

    /**
     * New parser on an input stream.
     */
    public AbstractReader(File file) throws FileNotFoundException {
        setInput(file);
    }

    public void setInput(File file) throws FileNotFoundException {
        if(file != null)
            reader = new BufferedReader(new FileReader(file));
        else
            reader = new BufferedReader(new InputStreamReader(System.in));
        input = file;
    }

    /**
     * New parser on a String.
     */
    public AbstractReader(String in) {
        setInput(in);
    }

    public void setInput(String in) {
        reader = new BufferedReader(new StringReader(in));
        input = in;
    }

    public String readLine() {
        if(lineUnread != null) {
            String retval = lineUnread;
            lineUnread = null;
            lastLineRead = retval;
            return retval;
        }
        try {
            lastLineRead = reader.readLine();
            return lastLineRead;
        }
        catch(IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    public void unreadLine() {
        lineUnread = lastLineRead;
    }

    /**
     * False if readLine returns null.
     */
    //public boolean hasNext()
    //{
    //	boolean hasNext = (readLine() != null);
    //	unreadLine();
    //	return hasNext;
    //}

    /**
     * By default, returns readLine(). You typically want to override.
     */
    //public Object next()
    //{
    //	return readLine();
    //}

    public void remove() {}

    public Collection readAll() {
        return null;
    }

    public void close() throws IOException {
        if(reader != null)
            reader.close();
    }

    public void open() throws IOException {
        if(input == null)
            return;
        else if(input instanceof String)
            setInput((String)input);
        else if(input instanceof File)
            setInput((File)input);
    }
}
