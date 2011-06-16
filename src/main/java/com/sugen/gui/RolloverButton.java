package com.sugen.gui;

import javax.swing.AbstractButton;
import javax.swing.JToggleButton;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

/**
 * A button that has a border only on mouseover.
 * <P>
 * The static method makeRollover(AbstractButton)
 * allows other AbstractButtons to have the rollover listener added.
 * <P>
 * On JToolBars, the rollover property can supposedly be set for whatever buttons are
 * on it; on other components, it's necessary to use some special code,
 * which is what RolloverButton provides.
 *
 * @author Jonathan Bingham
 */
public class RolloverButton {
    /** Listener to make buttons have borders only on mouseover. */
    static protected MouseAdapter rolloverMouseListener = new MouseAdapter() {
        public void mouseEntered(MouseEvent e) {
            AbstractButton button = (AbstractButton)e.getSource();
            if(button.isEnabled())
                button.setBorderPainted(true);
        }

        public void mouseExited(MouseEvent e) {
            AbstractButton button = (AbstractButton)e.getSource();
            if(!(button.isSelected() && button instanceof JToggleButton))
                button.setBorderPainted(false);
        }
    };
    static void makeRollover(AbstractButton button) {
        button.addMouseListener(rolloverMouseListener);
        button.setBorderPainted(false);
    }

    /**
     * Set/remove rollover property.
     */
     static void makeRollover(AbstractButton button, boolean b) {
        if(b)makeRollover(button);
        else {
            button.removeMouseListener(rolloverMouseListener);
            button.setBorderPainted(true);
        }
    }
}
