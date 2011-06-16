package com.sugen.gui.form;

import java.awt.CardLayout;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Vector;

import javax.swing.JButton;
import javax.swing.JPanel;

import com.sugen.gui.DialogModel;
import com.sugen.gui.DialogUtilities;
import com.sugen.gui.SwingWorker;

/**
 * A content pane for forms.
 * 
 * @see FormDialog
 * @see InternalFormDialog
 * 
 * @author Jonathan Bingham
 **/
public class FormPane extends JPanel {
	// The okButton is reused for 'Next >' and 'Finish'.
	public final static String OK = "OK";
	public final static String NEXT = "Next >";
	public final static String FINISH = "Finish";

	public final static String PROPERTY_FORM_TITLE = "formTitle";
	public final static String PROPERTY_FORMS_CLOSED = "formsClosed";
	public final static String PROPERTY_OK_TEXT = "okText";
	public final static String PROPERTY_WAITING = "waiting";
	public final static String PROPERTY_BACK_BUTTON = "backButtons";

	/** @serial */
	public JButton backButton;
	/** @serial */
	protected CardLayout cardLayout;
	/** @serial */
	protected Vector forms;
	/** @serial */
	protected int currentForm;

	/** @serial */
	protected WizardModel wizardModel;

	/**
	 * For threading that will be interrupted whenever the user cancels. Used
	 * when processing forms on ok/finish/next. May be used for other purposes
	 * too.
	 * 
	 * @serial
	 */
	protected SwingWorker worker;

	public FormPane() {
		cardLayout = new CardLayout();
		setLayout(cardLayout);

		forms = new Vector();
		currentForm = 0;
	}

	/**
	 * For threading that will be interrupted whenever the user cancels. Used
	 * when processing forms on ok/finish/next. May be used for other purposes
	 * too.
	 */
	public SwingWorker getWorker() {
		return worker;
	}

	/**
	 * For threading that will be interrupted whenever the user cancels. Used
	 * when processing forms on ok/finish/next. May be used for other purposes
	 * too.
	 */
	public void setWorker(SwingWorker worker) {
		this.worker = worker;
	}

	public void setForm(Form form) {
		setModel(null);
		forms.addElement(form);
		add(form, String.valueOf(currentForm));
		firePropertyChange(PROPERTY_FORM_TITLE, null, form.getTitle());
		firePropertyChange(PROPERTY_OK_TEXT, null, OK);
		firePropertyChange(PROPERTY_BACK_BUTTON, backButton, null);
		backButton = null;
	}

	public Form getForm() {
		return (Form) forms.elementAt(currentForm);
	}

	/**
	 * If there will be more than one Form, a non-null model is required.
	 */
	public void setModel(WizardModel model) {
		wizardModel = model;
		forms.removeAllElements();
		removeAll();
		if (model == null)
			return;

		Form form = model.getFormAt(0);
		forms.addElement(form);
		add(form, String.valueOf(currentForm));
		firePropertyChange(PROPERTY_FORM_TITLE, null, form.getTitle());

		if (model.getFormCount() > 1) {
			firePropertyChange(PROPERTY_OK_TEXT, null, NEXT);

			backButton = new JButton("< Back");
			backButton.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					showPreviousForm();
				}
			});
			backButton.setEnabled(false);
			firePropertyChange(PROPERTY_BACK_BUTTON, null, backButton);
		} else if (model.getFormCount() == 1)
			setForm(model.getFormAt(0));
	}

	public WizardModel getModel() {
		return wizardModel;
	}

	public void showNextForm() {
		Form form = wizardModel.getFormAt(++currentForm);
		forms.addElement(form);
		add(form, String.valueOf(currentForm));
		cardLayout.next(this);
		firePropertyChange(PROPERTY_FORM_TITLE, null, form.getTitle());

		if (currentForm > 0)
			backButton.setEnabled(true);
		if (currentForm == wizardModel.getFormCount() - 1)
			firePropertyChange(PROPERTY_OK_TEXT, null, FINISH);

		// Wizard can have problems if the next form is bigger than this one.
		// So we auto-resize to the preferred size or the current size,
		// whichever is bigger.
		Dimension psize = getPreferredSize();
		Dimension size = getSize();
		if (psize.width > size.width)
			size.width = psize.width;
		if (psize.height > size.height)
			size.height = psize.height;
		setSize(size);
		setPreferredSize(size);
		validate();
	}

	public void showPreviousForm() {
		if (currentForm == wizardModel.getFormCount() - 1)
			firePropertyChange(PROPERTY_OK_TEXT, null, NEXT);

		cardLayout.previous(this);
		remove((Form) forms.elementAt(currentForm));
		--currentForm;

		firePropertyChange(PROPERTY_FORM_TITLE, null, getForm().getTitle());

		if (currentForm == 0)
			backButton.setEnabled(false);

		// Auto-resize to the preferred size or the current size,
		// whichever is bigger.
		Dimension psize = getPreferredSize();
		Dimension size = getSize();
		if (psize.width > size.width)
			size.width = psize.width;
		if (psize.height > size.height)
			size.height = psize.height;
		setSize(size);
		setPreferredSize(size);
		validate();
	}

	protected class Worker extends SwingWorker {
		private String name;

		Worker(String name) {
			this.name = name;
		}

		public Object construct() {
			try {
				if (name == NEXT)
					next();
				else if (name == FINISH)
					finish();
				else // OK
				{
					Form form = getForm();
					if (form.isDataValid()) {
						form.setModelFromForm();
						form.applyChanges();
						if (form.getReturnValue() == DialogModel.CANCEL_OPTION)
							form.setReturnValue(DialogModel.ACCEPT_OPTION);
						close();
					}
				}
			} catch (FormException fe) {
				if (fe.getMessage() != null)
					DialogUtilities.showErrorDialog(FormPane.this, fe);
			}
			return null;
		}

		public void finished() {
			setWaiting(false);
			worker = null;
		}
	}

	/**
	 * In threaded form processing, interrupt the thread before closing.
	 */
	public void cancel() {
		if (worker != null) {
			worker.interrupt();
			worker = null;
		}
	}

	/**
	 * Process OK, Next and Finish in a separate, interruptable thread.
	 */
	public void ok(final String name) {
		setWaiting(true);
		worker = new Worker(name);
		worker.start();
	}

	/**
	 * If data is valid, show the next form.
	 */
	public void next() throws FormException {
		if (getForm().isDataValid())
			showNextForm();
	}

	/**
	 * Validate data and apply changes on all forms in a sequence (a Wizard).
	 * (In a Wizard, the last button is Finish.) If successful, close. If
	 * unsuccessful, throw a FormException.
	 */
	public void finish() throws FormException {
		// Apply changes on all forms in the series.
		if (getForm().isDataValid()) {
			for (int i = 0, size = forms.size(); i < size; i++) {
				Form aForm = (Form) forms.elementAt(i);
				aForm.setModelFromForm();
				aForm.applyChanges();
			}
			close();
		}
	}

	/**
	 * Calls showPreviousForm.
	 */
	public void back() {
		showPreviousForm();
	}

	public void close() {
		if (worker != null) {
			worker.interrupt();
			worker = null;
		}
		firePropertyChange(PROPERTY_FORMS_CLOSED, null, Boolean.TRUE);
	}

	public void setWaiting(boolean b) {
		firePropertyChange(PROPERTY_WAITING, null, new Boolean(b));
	}
}
