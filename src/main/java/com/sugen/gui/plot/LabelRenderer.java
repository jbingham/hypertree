package com.sugen.gui.plot;

import java.awt.Color;
import java.awt.Component;

/**
 * Returns a Component for rendering labels. Similar to the Swing
 * TableCellRenderer and ListRenderer.
 *
 * @author Jonathan Bingham
 */
public interface LabelRenderer {
    public Component getRendererComponent(Component view,
          Object dataObject, int key,
          boolean isSelected, boolean hasFocus,
          boolean isAdjusting,
          boolean isVisible, Color color);
}
