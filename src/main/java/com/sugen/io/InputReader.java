package com.sugen.io;

import java.io.*;
import java.util.*;

/**
 * Interface for readers that iterate through a String, File
 * or InputStream, extracting data one Java object at a time.
 * Using an InputReader is easy. If you want to read Objects one at a
 * time, do this:
 <p><pre>
 InputReader reader = new SomeReader(aFile);
 while(reader.hasNext())
 {
  Object obj = reader.next();
  //Do something with the Object.
 }
 </pre>
 Or if you simply want to read in all of the Objects before doing anything
 with them:
 <pre>
 InputReader reader = new SomeReader(aFile);
 Collection objects = reader.readAll();
 </pre>
 *
 * @author Jonathan Bingham
 */
public interface InputReader
    extends Iterator {
    /**
     * Class of Objects created by this reader. The Iterator.next() method
     * will return Objects of this Class type. The readAll() method will
     * return a Collection of Objects of this Class type.
     */
    public Class getInputClass();

    public void setInput(InputStream is);

    public void setInput(File file) throws FileNotFoundException;

    public void setInput(String in);

    public Collection readAll();

    public void close() throws IOException;

    public void open() throws IOException;
}
