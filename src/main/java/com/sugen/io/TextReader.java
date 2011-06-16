package com.sugen.io;

import java.io.*;

/**
 * Basic implementation of an InputReader designed to work with ASCII files.
 * Defaults to reading stdin.
 *
 * @author Jonathan Bingham
 */
public class TextReader
    extends AbstractReader {

    public TextReader() {
        super();
    }

    /**
     * Class of Objects created by this reader. The Iterator.next() method
     * will return Objects of this Class type. The readAll() method will
     * return a Collection of Objects of this Class type.
     * @return String.class
     */
    public Class getInputClass() {
        return String.class;
    }

    /**
     * New parser on an input stream.
     */
    public TextReader(InputStream is) throws IOException {
        setInput(is);
    }

    /**
     * New parser on an input stream.
     */
    public TextReader(File file) throws FileNotFoundException {
        setInput(file);
    }

    /**
     * New parser on a String.
     */
    public TextReader(String in) {
        setInput(in);
    }

    /**
     * False if readLine returns null.
     */
    public boolean hasNext() {
        boolean hasNext = (super.readLine() != null);
        super.unreadLine();
        return hasNext;
    }

    /**
     * By default, returns readLine(). You typically want to override.
     */
    public Object next() {
        return super.readLine();
    }

    public void remove() {}
}
