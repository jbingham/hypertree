package com.sugen.gui;

import java.awt.*;
import javax.swing.*;

/**
 * A set of static convenience methods useful in creating simple dialogs.
 *
 * @author Jonathan Bingham
 */
public class DialogUtilities {
    /**
     * Create a generic error dialog for an exception, centered on owner.
     * @param owner may be null
     */
    static public void showErrorDialog(Component owner, Exception e) {
        JOptionPane.showMessageDialog(owner,
            lineWrapString(e.getMessage(), 400),
            "Error", JOptionPane.ERROR_MESSAGE);
        e.printStackTrace();
    }

    /**
     * Create a generic error dialog for an exception, centered on owner.
     * @param owner may be null
     */
    static public void showInternalErrorDialog(Component owner, Exception e) {
        JOptionPane.showInternalMessageDialog(
            owner, lineWrapString(e.getMessage(), 400),
            "Error", JOptionPane.ERROR_MESSAGE);
        e.printStackTrace();
    }

    /**
     * Returns a JPanel with a string line-wrapped at the specified number
     * of pixels.
     *
     * @return a JPanel containing JLabels.
     */
    static public JPanel lineWrapString(String s, int width) {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        JLabel label = new JLabel(); //because JLabels have a different default font
        FontMetrics fm = panel.getFontMetrics(label.getFont());

        //Create multi-line display with breaks between words
        int lineStart = 0, //Start current line at this index
            breakChar = 0; //Break current line at this index

        s = s.replace('\n', ' ');
        for(int i = s.indexOf(' ', lineStart), //try extending line to next space
            nchars = s.length();
            i < nchars && i != -1; //not past end of string
            i = s.indexOf(' ', breakChar + 1)) { //find next space char
            if(fm.stringWidth(s.substring(lineStart, i)) > width) {
                JLabel line = new JLabel(s.substring(lineStart, breakChar));
                panel.add(line);
                lineStart = breakChar + 1;
            }
            breakChar = i;
        }
        panel.add(new JLabel(s.substring(lineStart, s.length())));
        return panel;
    }
}
