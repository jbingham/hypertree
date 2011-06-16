package com.sugen.gui.form;

import com.sugen.gui.*;

/**
 * A generalized base class for dialog windows that contain data entry forms.
 * Supports multiple forms in the same window.
 * <P>
 * The controller in a Model/View/Control triad.<P>
 *
 * A typical use of this class for one form:<BR>
<PRE>
FormCarrier fd = new FormDialog(owner, true);
fd.setForm(new MyForm(fd));
fd.showDialog();
</PRE>
 * <P>
 * To use this as a 'Wizard', set its model to a WizardModel. This gives
 * next/back/finish buttons instead of ok/cancel, and handles navigation from one
 * form to the next. A typical use:<BR>
<PRE>
FormCarrier fd = new InternalFormDialog(owner, true);
fd.setModel(new WizardModel()
{
   public int getFormCount() { return 2; }
   public Form getFormAt(int i)
   {
      if(i == 0) return new MyForm0(fd);
      else return new MyForm1(fd);
   }
});
fd.showDialog();
</PRE>
 *
 * @see Form
 * @author Jonathan Bingham
 **/
public interface FormCarrier extends DialogModel {
	// The okButton is reused for 'Next >' and 'Finish'.
	public final static String OK = "OK";
	public final static String NEXT = "Next >";
	public final static String FINISH = "Finish";

    public void setForm(Form form);
    public Form getForm();

	public void setModel(WizardModel model);
	public WizardModel getModel();

	public SwingWorker getWorker();
	public void setWorker(SwingWorker worker);

	public FormPane getFormPane();

	public void setWaiting(boolean isWaiting);
	public void close();

	public java.awt.Component getCarrierComponent();
}
