package com.sugen.gui.form;

import java.awt.Component;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;

/**
 * A generalized base class for data entry/editing forms.<P>
 *
 * This is the View in a Model/View/Control triad. The Controller is the
 * FormCarrier, and the Model is up to the programmer to supply in a specific
 * instance: it's the data that goes into the form.<P>
 *
 * You must put a Form in a FormCarrier. Form isn't itself derived from
 * JDialog because you might want to use either a JDialog or a JInternalDialog,
 * or place multiple forms in the same carrier
 * window (either at once, or sequentially, as in a 'Wizard').<P>
 *
 * A simple example:<BR>
 * <PRE>
 	class LoginForm extends Form {
		private JTextField userField;
		private JPasswordField passwordField;

		public LoginForm(FormCarrier carrier) {
			super(carrier);
			setTitle("Enter User Login");

			FormFactory factory = new FormFactory(this);
			userField = factory.createTextField();
			passwordField = factory.createPasswordField();
			add(factory.label(userField, "User Name"));
			add(factory.label(passwordField, "Password"));
		}
	}
</PRE>
 *
 * @see FormCarrier
 * @see FormFactory
 * @see FormLayout
 *
 * @author Jonathan Bingham
 */
public class Form
    extends JPanel {
    /** @serial */
    protected boolean isModified;
    /** @serial */
    protected boolean isEditable;

    /**
     * The JDialog in which this form appears. It may be null.
     * @serial
     */
    protected FormCarrier carrier;
    /** @serial */
    protected String title;

    public Form() {
        this(null);
    }

    public Form(FormCarrier carrier) {
        this.carrier = carrier;
        isEditable = true;
        setBorder(new EmptyBorder(20, 20, 10, 20));
        FormLayout layout = new FormLayout(FormLayout.VERTICAL, FormLayout.LEFT);
        layout.setResizeRecursive(true);
        setLayout(layout);
    }

    public FormCarrier getCarrier() {
        return carrier;
    }

    public void setCarrier(FormCarrier carrier) {
        this.carrier = carrier;
    }

    /**
     * Get the Model used by this Form. For polymorphism. In many cases, there may
     * be no single Object that encapsulates the Model so conveniently.
     */
    public Object getModel() {
        return null;
    }

    /**
     * Set the Model used by this Form. For polymorphism. In many cases, there may
     * be no single Object that encapsulates the Model so conveniently.
     */
    public void setModel(Object model) {}

    public boolean isModified() {
        return isModified;
    }

    public void setModified(boolean b) {
        isModified = b;
    }

    /**
     * In Wizards, data validation is done before moving on to the next form,
     * without actually calling applyChanges. To catch data validation problems,
     * either throw an exception, which will be handled in a default way, or
     * handle the exception internally, and return false if the problem is not
     * corrected.
     */
    public boolean isDataValid() throws FormException {
        return true;
    }

    /**
     * On OK or Finish, apply the changes on the form. This could mean altering the
     * appearance of the application, saving to a preferences file, committing to the
     * database, or something else. This method is called automatically on OK or
     * Finish and need only be overridden for most purposes. If a form will have
     * an Apply button, the method must be called explicitly. Throw an exception
     * if the data is not valid or if some other error occurs. Any thrown
     * FormException will automatically be displayed in a JOptionPane, and the
     * Form will remain open.
     */
    public void applyChanges() throws FormException {}

    /**
     * Get the data out of the GUI components and put it into the data
     * structures where it needs to go. Throw an exception
     * or handle errors in the method if there are any problems.
     */
    public void setModelFromForm() throws FormException {}

    /**
     * Get the data from the Model and copy it into the widgets on the form.
     */
    public void setFormFromModel() {}

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public boolean isEditable() {
        return isEditable;
    }

    /**
     * Override this if you want to enable/disable or otherwise alter components
     * on the form.
     */
    public void setEditable(boolean b) {
        isEditable = b;
    }

    public Object getReturnValue() {
        return carrier == null ? null : carrier.getReturnValue();
    }

    public void setReturnValue(Object value) {
        if(carrier != null)
            carrier.setReturnValue(value);
    }

    public Component getCarrierComponent() {
        return carrier == null ? null : carrier.getCarrierComponent();
    }
}
