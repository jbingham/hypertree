package com.sugen.io;

import java.io.*;

/**
 *
 * @author Jonathan Bingham
 */
public class TextWriter
    extends AbstractWriter {
    public TextWriter() {
    }

    public TextWriter(Writer w) {
        super(w);
    }

    public Class getOutputClass() {
        return String.class;
    }

    /**
     * Calls Object.toString.
     * @param obj Object to write
     */
    public Object write(Object obj) throws IOException {
        if (obj != null && writer != null)
            writer.write(obj.toString() + "\n");
        return writer;
    }
}
