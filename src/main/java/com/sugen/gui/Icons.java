package com.sugen.gui;

import javax.swing.ImageIcon;
import javax.swing.Icon;

/**
 * Provides a single static method for loading icons as resources.
 * The URL of all icons is relative to a directory called "images"
 * beneath this class's URL. This is useful because of the caveats in
 * using relative URLs from inside of a jar file.
 *
 * @author Jonathan Bingham
 */
public class Icons {
    /**
     * Load an icon as a resource from the "images" directory.
     */
    public static Icon get(String name) {
        return new ImageIcon(Icons.class.getResource("images/" + name));
    }
}
