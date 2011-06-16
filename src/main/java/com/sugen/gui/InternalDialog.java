package com.sugen.gui;

import javax.swing.*;
import javax.swing.event.*;

import java.awt.*;
import java.awt.event.*;
import java.beans.*;

/**
 * Implementation of DialogModel for internal dialogs.
 * @author Jonathan Bingham
 */
public class InternalDialog
    extends javax.swing.JInternalFrame implements DialogModel {
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
     * @serial
     */
    public JButton okButton = new JButton("OK");

    /**
     * Cancel button. Automatically has action listener to call cancelButtonPressed.
     * Escape accomplishes the same.
     * @serial
     */
    public JButton cancelButton = new JButton("Cancel");

    /** @serial */
    protected JPanel buttonPanel;

    public InternalDialog() {
        initialize(new JButton[] {okButton, cancelButton}, SOUTH);
    }

    public InternalDialog(JButton buttons[]) {
        initialize(buttons, SOUTH);
    }

    /**
     * @param buttonPosition either BorderLayout.SOUTH or BorderLayout.EAST
     */
    public InternalDialog(JButton buttons[], String buttonPosition) {
        initialize(buttons, buttonPosition);
    }

    protected void initialize(JButton buttons[], String buttonPosition) {
        setClosable(true);
        setLayer(JLayeredPane.MODAL_LAYER);
        setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
        addInternalFrameListener(new InternalFrameAdapter() {
            public void internalFrameClosing(InternalFrameEvent e) {
                cancelButtonPressed();
            }
        });

        //Not quite standard: if another button has focus, it should get clicked
        okButton.registerKeyboardAction(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                okButton.doClick();
            }
        }, KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0),
            JComponent.WHEN_IN_FOCUSED_WINDOW);
        okButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                okButtonPressed();
            }
        });

        cancelButton.registerKeyboardAction(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                cancelButton.doClick();
            }
        }, KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0),
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
        JPanel buttonRow = new JPanel(new LinearGridLayout(axis));
        buttonPanel.add(buttonRow);
        for(int i = 0; i < buttons.length; i++)
            buttonRow.add(buttons[i]);
    }

    public JButton[] getButtons() {
        JButton buttons[] = new JButton[buttonPanel.getComponentCount()];
        for(int i = 0, j = buttonPanel.getComponentCount(); i < j; i++)
            buttons[i] = (JButton)buttonPanel.getComponents()[i];
        return buttons;
    }

    public JButton getOkButton() {
        return okButton;
    }

    public JButton getCancelButton() {
        return cancelButton;
    }

    /**
     * By default, closes the dialog and returns ACCEPT_OPTION.
     * Override to do anything in particular.
     */
    public void okButtonPressed() {
        if(getReturnValue() == CANCEL_OPTION)
            setReturnValue(ACCEPT_OPTION);
        close();
    }

    public void close() {
        firePropertyChange(PROPERTY_CLOSED, null, Boolean.TRUE);
        try {
            setVisible(false);
            setClosed(true);
        }
        catch(PropertyVetoException pve) {}
    }

    /**
     * Sets return value to CANCEL_OPTION and closes the dialog.
     */
    public void cancelButtonPressed() {
        returnValue = CANCEL_OPTION;
        close();
    }

    /**
     * Show the dialog and return a value from it.
     * @see #setReturnValue(Object)
     * @return CANCEL_OPTION, ACCEPT_OPTION, or some other Object
     */
    public Object showDialog() {
        addPropertyChangeListener(new PropertyChangeListener() {
            public void propertyChange(PropertyChangeEvent pce) {
                String name = pce.getPropertyName();
                if(name.equals(PROPERTY_CLOSED));
                stopModal2();
            }
        });
        show();
        startModal2();
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

    /*
     * Creates a new EventDispatchThread to dispatch events from. This
     * method returns when stopModal is invoked.
     */
    private synchronized void startModal2() {
        /* Since all input will be blocked until this dialog is dismissed,
         * make sure its parent containers are visible first (this component
         * is tested below).  This is necessary for JApplets, because
         * because an applet normally isn't made visible until after its
         * start() method returns -- if this method is called from start(),
         * the applet will appear to hang while an invisible modal frame
         * waits for input.
         */
        if(isVisible() && !isShowing()) {
            Container parent = this.getParent();
            while(parent != null) {
                if(parent.isVisible() == false)
                    parent.setVisible(true);
                parent = parent.getParent();
            }
        }

        try {
            if(SwingUtilities.isEventDispatchThread()) {
                System.out.println(
                    "eventDispatchThread - maybe this is the problem.");
                EventQueue theQueue = getToolkit().getSystemEventQueue();
                while(isVisible()) {
                    // This is essentially the body of EventDispatchThread
                    AWTEvent event = theQueue.getNextEvent();
                    Object src = event.getSource();
                    // can't call theQueue.dispatchEvent, so I pasted it's body here
                    //if (event instanceof ActiveEvent) {
                    //((ActiveEvent) event).dispatch();
                    //} else
                    if(src instanceof Component)
                        ((Component)src).dispatchEvent(event);
                    else if(src instanceof MenuComponent)
                        ((MenuComponent)src).dispatchEvent(event);
                    else
                        System.err.println("unable to dispatch event: " + event);
                }
            }
            else
                while(isVisible())
                    wait();
        }
        catch(InterruptedException e) {}
    }

    /*
     * Stops the event dispatching loop created by a previous call to
     * <code>startModal</code>.
     */
    private synchronized void stopModal2() {
        notifyAll();
    }
}
