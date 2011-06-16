package com.sugen.io;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.StringWriter;
import java.io.Writer;

/**
 * Basic implementation of an OutputWriter.
 * Defaults to writing to stdout.
 *
 * @author Jonathan Bingham
 */
abstract public class AbstractWriter
    implements OutputWriter {
    protected Writer writer;
    protected Object output;

    /**
     * An AbstractWriter that writes to System.out.
     */
    public AbstractWriter() {
        this(new BufferedWriter(new OutputStreamWriter(System.out)));
    }

    public AbstractWriter(Writer w) {
        writer = w;
    }

    /**
     * Class of Objects written by this writer. The write(Object) method
     * will write Objects of this Class type.
     * @return Object.class
     */
    public Class getOutputClass() {
        return Object.class;
    }

    public void setOutput(Writer out) {
        writer = out;
        output = null;
    }

    public void setOutput(File file) throws IOException {
        setOutput(file, false);
    }

    public void setOutput(File file, boolean append) throws IOException {
        if(file == null)
            writer = new BufferedWriter(new OutputStreamWriter(System.out));
        else
            writer = new BufferedWriter(new FileWriter(file.getAbsolutePath(),
                append));
        output = file;
    }

    public void setOutput(OutputStream out) {
        writer = new BufferedWriter(new OutputStreamWriter(out));
        output = null;
    }

    public String toString() {
        return(writer == null ? null : writer.toString());
    }

    public void flush() throws IOException {
        if(writer != null)
            writer.flush();
    }

    /**
     * Close the writer. Warning: if you close System.out or System.err,
     * they will be closed to all subsequent attempts to write to them
     * from anywhere in the currently running program.
     */
    public void close() throws IOException {
        if(writer != null)
            writer.close();
    }

    public void open() throws IOException {
        if(output == null)
            return;
        else if(output instanceof File)
            setOutput((File)output);
        else if(writer instanceof StringWriter)
            setOutput(new StringWriter());
    }
}
