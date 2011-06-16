package com.sugen.gui;

import javax.swing.JPanel;
import javax.swing.SwingConstants;

/**
 * A row of horizontal or vertical buttons, each of the same width and height.<P>
 *
 * Thanks to the LinearGridLayout, this has become a completely trivial class -
 * so trivial that there's little reason to even use it. It replaces ButtonRow,
 * which has been deprecated.<P>
 *
 * The main advantage is that, if you're using a FormLayout, the BasicButtonRow
 * is not resized, which is usually a good thing. If resize is recursive in that
 * layout, all advantages are gone.<P>
 *
 * The original purpose remains: lay out a row of buttons either horizontally or
 * vertically so that each button has equal width.
 *
 * @author Jonathan Bingham
 */
public class ButtonRow
    extends JPanel {
    public final static int HORIZONTAL = SwingConstants.HORIZONTAL;
    public final static int VERTICAL = SwingConstants.VERTICAL;

    public ButtonRow(int axis) {
        setLayout(new LinearGridLayout(axis));
    }
}
