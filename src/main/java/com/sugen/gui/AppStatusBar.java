package com.sugen.gui;

import javax.swing.*;
import javax.swing.border.*;
import java.awt.*;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

//BUG: does not update borders consistent with current L&F.
//TODO: Create status bar animation.
/**
 * A simple status bar with three message areas. These are: general,
 * progress, and coordinates. The names are arbitrary.
 *
 * @author Jonathan Bingham
 */
public class AppStatusBar
    extends JPanel implements PropertyChangeListener {
    /** @serial */
    protected JPanel statusBar;
    /** @serial */
    protected JLabel generalStatusLabel = new JLabel(" "); //To fix height
    /** @serial */
    protected JLabel progressLabel = new JLabel();
    /** @serial */
    protected JLabel coordinateLabel = new JLabel();
    //protected Border border = new BevelBorder(BevelBorder.LOWERED);
    /** @serial */
    protected Border border = new CompoundBorder(new EmptyBorder(1, 1, 1, 1),
                                                 new LineBorder(Color.gray));

    public AppStatusBar() {
        setLayout(new BorderLayout());
        generalStatusLabel.setBorder(border);
        add(generalStatusLabel);

        JPanel formatting = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        add(formatting, BorderLayout.EAST);

        int height = generalStatusLabel.getPreferredSize().height;
        progressLabel.setPreferredSize(new Dimension(150, height));
        progressLabel.setBorder(border);
        coordinateLabel.setPreferredSize(new Dimension(200, height));
        coordinateLabel.setBorder(border);
        formatting.add(progressLabel);
        formatting.add(coordinateLabel);
    }

    /**
     * Set borders.
     */
    /*
      public void updateUI()
      {
     super.updateUI();
     progressLabel.setBorder(border);
     coordinateLabel.setBorder(border);
     generalStatusLabel.setBorder(border);
      }
     */

    public void setGeneralStatusMessage(String message) {
        if("Ready".equals(message))
            message = "";
        generalStatusLabel.setText(" " + message);
        generalStatusLabel.repaint();
    }

    public String getGeneralStatusMessage() {
        String message = generalStatusLabel.getText();
        return message.substring(1);
    }

    public void setCoordinateMessage(String message) {
        coordinateLabel.setText(" " + message);
        coordinateLabel.repaint();
    }

    public String getCoordinateMessage() {
        String message = coordinateLabel.getText();
        return message.substring(1);
    }

    public void propertyChange(PropertyChangeEvent e) {
        String name = e.getPropertyName();
        if(name.equals(AppBean.PROPERTY_STATUS_MESSAGE))
            setGeneralStatusMessage((String)e.getNewValue());
        else if(name.equals(AppBean.PROPERTY_COORDINATE_MESSAGE))
            setCoordinateMessage((String)e.getNewValue());
    }
}
