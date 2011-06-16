package com.sugen.gui;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

/**
 * Implementation of DialogModel for dialogs in separate windows.
 *
 * @author Jonathan Bingham
 */
public class BasicDialog
    extends javax.swing.JDialog implements DialogModel {
    /**
     * This can be set by derived classes so that a call to showDialog()
     * will return a value. By default, if user clicks OK, the returnValue
     * has some non-null value. If the user cancels, the returnValue is null.
     * @serial
     */
    protected Object returnValue = ACCEPT_OPTION;

    /**
     * okButton. Automatically has action listener to call okButtonPressed.
     * Carriage return accomplishes the same.
     */
    public final JButton okButton = new JButton("OK");

    /**
     * Cancel button. Automatically has action listener to call cancelButtonPressed.
     * Escape accomplishes the same.
     */
    public final JButton cancelButton = new JButton("Cancel");

    /** @serial */
    protected JPanel buttonPanel;
    /** @serial */
    protected JPanel buttonRow;

    /**
     * Necessary for compilation under jdk1.1.x, but superfluous in 1.2 due
     * to changes in awt.
     * @serial
     */
    //protected Window owner;

    public BasicDialog(Frame owner, boolean isModal) {
        super(owner, isModal);
        initialize(owner, isModal,
                   new JButton[] {okButton, cancelButton}, SOUTH);
    }

    public BasicDialog(Frame owner, boolean isModal, JButton buttons[]) {
        super(owner, isModal);        
        initialize(owner, isModal, buttons, SOUTH);
    }

    /**
     * @param buttonPosition either BorderLayout.SOUTH or BorderLayout.EAST
     */
    public BasicDialog(Frame owner, boolean isModal, JButton buttons[],
                       String buttonPosition) {
        super(owner, isModal);
        initialize(owner, isModal, buttons, buttonPosition);
    }

    protected void initialize(Frame owner, boolean isModal, JButton buttons[],
                              String buttonPosition) {
        //this.owner = owner;

        addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent e) {
                cancelButtonPressed();
            }
        });

        //Not quite standard: if another button has focus, it should get clicked
        /*okButton.registerKeyboardAction(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                //System.out.println(e.getSource());
                okButton.doClick();
            }
        },
            KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0),
            JComponent.WHEN_IN_FOCUSED_WINDOW);*/
        okButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                okButtonPressed();
            }
        });
        okButton.requestFocus();

        cancelButton.registerKeyboardAction(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                cancelButton.doClick();
            }
        },
            KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0),
            JComponent.WHEN_IN_FOCUSED_WINDOW);
        cancelButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                cancelButtonPressed();
            }
        });

        setButtons(buttons, buttonPosition);
    }

    /**
     * Set the buttons for the bottom or side of the form.
     * You will need to add listeners for any buttons except ok and cancel,
     * since these automatically have listeners that call okButtonPressed and
     * cancelButtonPressed.
     */
    public void setButtons(JButton buttons[]) {
        setButtons(buttons, SOUTH);
    }

    /**
     * @param buttons an array of buttons, perhaps simply OK and Cancel.
     * @param buttonPosition either SOUTH or EAST
     */
    public void setButtons(JButton buttons[], String buttonPosition) {
        if(buttonPanel != null)
            getContentPane().remove(buttonPanel);
        buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 10));
        getContentPane().add(buttonPanel, buttonPosition);

        int axis = buttonPosition == SOUTH ?
            ButtonRow.HORIZONTAL : ButtonRow.VERTICAL;
        buttonRow = new JPanel(new LinearGridLayout(axis));
        buttonPanel.add(buttonRow);
        for(int i = 0; i < buttons.length; i++)
            buttonRow.add(buttons[i]);
//System.out.println(buttons.length);
    }

    public JButton[] getButtons() {
        return(JButton[])buttonRow.getComponents();
    }

    public JButton getOkButton() {
        return okButton;
    }

    public JButton getCancelButton() {
        return cancelButton;
    }

    /**
     * Needed for jdk1.1.x; in 1.2 a method with the same name, return
     * type, and purpose is available.
     */
    /*public Window getOwner()
         {
        return owner;
         }*/

    /**
     * By default, closes the dialog and returns ACCEPT_OPTION.
     * Override to do anything in particular.
     */
    public void okButtonPressed() {
        if(getReturnValue() == CANCEL_OPTION)
            setReturnValue(ACCEPT_OPTION);
        setVisible(false);
        //dispose();
    }

    /**
     * Sets return value to CANCEL_OPTION and closes the dialog.
     */
    public void cancelButtonPressed() {
        returnValue = CANCEL_OPTION;
        setVisible(false);
        //dispose();
    }

    /**
     * Show the dialog and return a value from it.
     * @see #setReturnValue(Object)
     * @return CANCEL_OPTION, ACCEPT_OPTION, or some other Object
     */
    public Object showDialog() {
        setVisible(true);
        return returnValue;
    }

    /**
     * Set the return value of this dialog, which is returned if the dialog
     * was shown using showDialog().
     */
    public void setReturnValue(Object value) {
        returnValue = value;
    }

    /**
     * Get the return value of this dialog.
     */
    public Object getReturnValue() {
        return returnValue;
    }

    /**
     * Overloaded to auto-size and auto-position.
     */
    public void setVisible(boolean b) {
        if(b) {
            pack();
            setLocationRelativeTo(getOwner());
        }
        super.setVisible(b);
    }
}
