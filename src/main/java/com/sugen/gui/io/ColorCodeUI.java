package com.sugen.gui.io;

import java.awt.Color;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.Serializable;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Properties;
import java.util.StringTokenizer;

import javax.swing.Action;

import com.sugen.gui.AppBean;
import com.sugen.gui.Icons;
import com.sugen.gui.Palette;

/**
 * GUI support for color-code files. A color-code file contains a set of
 * Strings as keys, followed by whitespace and then a Color. Here's an
 * example of a valid color-code file: <br><pre>
 apple        256,0,0<br>
 orange       orange<br>
 banana       yellow<br>
 weirdFruit   whoKnows?<br>
 </pre>
 * Colors can be specified in more than one way, as you can see. A color
 * can be specified as an RGB triplet, with commas. Pure red is 256,0,0.
 * Alternatively, a color can be specified using any of the color names
 * defined the Palette class. Finally, a color can be defined using any
 * other name at all, in which case a color will be automatically assigned
 * using a stock of preselected colors.
 *
 * @see com.sugen.gui.Palette
 * @author Jonathan Bingham
 */
public class ColorCodeUI
    extends ReaderWriterUI implements Serializable {
    /**
     * @serial
     */
    protected Properties colorCode = new Properties();

    public ColorCodeUI() {
        reader.openAction.putValue(Action.NAME, "Import Colors...");
        reader.openAction.putValue(Action.SMALL_ICON,
                                   Icons.get("emptyIcon24.gif"));
        reader.openAction.putValue(AppBean.KEY_LOCATION,
                                   AppBean.VALUE_MENU_ONLY);
        reader.openAction.putValue(AppBean.KEY_ACCELERATOR,
                                   AppBean.VALUE_DEFAULT);
        reader.setPrefix("reader.colorcode");

        writer.saveAsAction.putValue(Action.NAME, "Export Colors...");
        writer.saveAsAction.putValue(AppBean.KEY_LOCATION,
                                     AppBean.VALUE_MENU_ONLY);
        writer.saveQuery = "Export colors first?";
        writer.setPrefix("writer.colorcode");
    }

    public Object open(Object parser, File file) throws IOException {
        super.open(parser, file);
        colorCode = new Properties();
        colorCode.clear();
        colorCode.load(new FileInputStream(file));
        checkColors(); // sets needsSave = true
        writer.needsSave = false;
        writer.updateActions();
        return colorCode;
    }

    protected Collection getDefaultColors() {
        Collection colors = new ArrayList();
        colors.add(Palette.blue);
        colors.add(Palette.red);
        colors.add(Palette.green);
        colors.add(Palette.violet);
        colors.add(Palette.purple);
        colors.add(Palette.orange);
        colors.add(Palette.lightBlue);
        colors.add(Palette.lightGreen);
        colors.add(Palette.pink);
        colors.add(Palette.cyan);
        colors.add(Palette.gray);
        colors.add(Palette.darkCyan);
        colors.add(Palette.darkGreen);
        colors.add(Palette.darkRed);
        colors.add(Palette.darkBlue);
        colors.add(Palette.greenYellow);
        colors.add(Palette.salmon);
        return colors;
    }

    /**
     * Check that the color entries are valid. Auto-assign default colors
     * if not.
     */
    protected void checkColors() {
        Properties props = (Properties)colorCode.clone();
        Enumeration en = props.keys();
        Map categories = new HashMap();
        Iterator colors = getDefaultColors().iterator();
        while(en.hasMoreElements()) {
            String key = (String)en.nextElement();
            if(getColor(key) != null) {
                //System.err.println(key + " " + properties.get(key));
                continue;
            }
            Object category = props.get(key);
            if(categories.get(category) != null) {
                setColor(key, (Color)categories.get(category));
            }
            else if(colors.hasNext()) {
                Color color = (Color)colors.next();
                categories.put(category, color);
                setColor(key, color);
            }
            else
                colors = getDefaultColors().iterator(); // repeat colors
            //System.err.println(key + " " + properties.get(key));
        }
    }

    public void save(Object writer, Object data, File file) throws IOException {
        //super.save(writer, data, file); // NA
        FileOutputStream out = new FileOutputStream(file);
        colorCode.store(out, null);
    }

    public Color getColor(String key) {
        Color retval = null;

        String color = colorCode.getProperty(key);
        if(color == null)
            retval = null;
        //Parse RGB color
        else if(color.indexOf(",") != -1) {
            StringTokenizer tokenizer = new StringTokenizer(color, ",");
            try {
                int red = Integer.parseInt(tokenizer.nextToken());
                int green = Integer.parseInt(tokenizer.nextToken());
                int blue = Integer.parseInt(tokenizer.nextToken());
                retval = new Color(red, green, blue);
            }
            catch(NumberFormatException e) {}
        }
        //Parse color name from Palette class
        else {
            try {
                Field field = Palette.class.getField(color);
                retval = (Color)field.get(null);
            }
            catch(NoSuchFieldException e) {}
            catch(IllegalAccessException e) {}
        }
        return retval;
    }

    public void setColor(String key, Color color) {
        if(color != null && key != null && !color.equals(getColor(key)))
            colorCode.put(key, color.getRed() + ","
                           + color.getGreen() + ","
                           + color.getBlue());
        writer.needsSave = true;
        //writer.propertyChange(new PropertyChangeEvent(this, PROPERTY_DATA,
        //    null, properties));
    }

    public void setAllColors(Map map) {
        if(map == null)
            return;
        Iterator iter = map.keySet().iterator();
        while(iter.hasNext()) {
            String key = iter.next().toString();
            Color color = (Color)map.get(key);
            if(color != null && !color.equals(getColor(key)))
                colorCode.put(key, color.getRed() + ","
                               + color.getGreen() + ","
                               + color.getBlue());
        }
        writer.needsSave = false;
        //writer.propertyChange(new PropertyChangeEvent(this, PROPERTY_DATA,
        //    null, properties));
    }

    public Action getImportAction() {
        return reader.openAction;
    }

    public Action getExportAction() {
        return writer.saveAsAction;
    }

    public void clear() {
        colorCode.clear();
    }

    public Enumeration keys() {
        return colorCode.keys();
    }

    /**
     * Always allow user to close, even without exporting colors first.
     */
    public boolean canClose() {
        return true;
    }

    public void close() {
        //don't call reader method - we don't want recent files saved
        //reader.close();
        writer.close();
    }
}
