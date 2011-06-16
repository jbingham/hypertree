package com.sugen.io;

import java.io.*;

/**
 * Interface for writers that output one Java object at a time in
 * some arbitrary format.
 *
 * @author Jonathan Bingham
 */
public interface OutputWriter {
    /**
     * Class of Objects written by this writer. The write(Object) method
     * will write Objects of this Class type.
     */
    public Class getOutputClass();

    public Object write(Object obj) throws IOException;

    public void setOutput(Writer out);

    public void setOutput(File file) throws IOException;

    public void setOutput(File file, boolean append) throws IOException;

    public void setOutput(OutputStream out);

    public String toString();

    public void flush() throws IOException;

    public void close() throws IOException;

    public void open() throws IOException;
}
