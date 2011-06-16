package com.sugen.gui.form;

import javax.swing.JButton;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.beans.*;

import com.sugen.gui.*;

/**
 * Implementation of a FormCarrier as a standard, separate dialog window.
 *
 * @author Jonathan Bingham
 */
public class FormDialog
    extends BasicDialog
    implements FormCarrier, PropertyChangeListener {
    /** @serial */
    protected FormPane formPane = new FormPane();

    /**
     * A modal FormDialog.
     * @param owner May be null.
     */
    public FormDialog(Frame owner) {
        this(owner, true);
    }

    /**
     * @param owner May be null.
     */
    public FormDialog(Frame owner, boolean isModal) {
        super(owner, isModal);

        getContentPane().add(formPane);
        formPane.addPropertyChangeListener(this);

        getGlassPane().addMouseListener(new MouseAdapter() {}); //for cursor
        getGlassPane().setCursor(new Cursor(Cursor.WAIT_CURSOR));
    }

    public void cancelButtonPressed() {
        formPane.cancel();
        super.cancelButtonPressed();
    }

    public void setForm(Form form) {
        formPane.setForm(form);
    }

    public Form getForm() {
        return formPane.getForm();
    }

    /**
     * If there will be more than one Form, a non-null model is required.
     */
    public void setModel(WizardModel model) {
        formPane.setModel(model);
    }

    public WizardModel getModel() {
        return formPane.getModel();
        //return model;
    }

    /**
     * Process OK, Next and Finish in a separate, interruptable thread.
     */
    public void okButtonPressed() {
        formPane.ok(okButton.getText());
    }

    /**
     * If data is valid, show the next form.
     */
    public void nextButtonPressed() throws FormException {
        formPane.next();
        /*if(getForm().isDataValid())
         showNextForm();*/
    }

    /**
     * Show/hide glass pane and waiting cursor.
     */
    public void setWaiting(boolean isWaiting) {
        getGlassPane().setVisible(isWaiting);
    }

    public void close() {
        setVisible(false);
        //dispose();
    }

    public Component getCarrierComponent() {
        return this;
    }

    public SwingWorker getWorker() {
        return formPane.getWorker();
    }

    public void setWorker(SwingWorker worker) {
        formPane.setWorker(worker);
    }

    public FormPane getFormPane() {
        return formPane;
    }

    public void propertyChange(PropertyChangeEvent pce) {
        String name = pce.getPropertyName();
        if(name.equals(FormPane.PROPERTY_BACK_BUTTON)) {
            if(pce.getNewValue() != null) {
                JButton backButton = (JButton)pce.getNewValue();
                JButton[] buttons = getButtons();
                JButton[] newButtons = new JButton[buttons.length + 1];
                newButtons[0] = backButton;
                for(int i = 0; i < buttons.length; i++) {
                    newButtons[i + 1] = buttons[i];
                }
                setButtons(newButtons);
            }
        }
        else if(name.equals(FormPane.PROPERTY_FORM_TITLE))
            setTitle((String)pce.getNewValue());
        else if(name.equals(FormPane.PROPERTY_FORMS_CLOSED))
            close();
        else if(name.equals(FormPane.PROPERTY_OK_TEXT))
            okButton.setText((String)pce.getNewValue());
        else if(name.equals(FormPane.PROPERTY_WAITING))
            setWaiting(((Boolean)pce.getNewValue()).booleanValue());
    }
}
