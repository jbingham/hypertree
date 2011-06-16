package com.sugen.gui.form;


/**
 * A model used by FormDialogs if they will have more than one form.
 * It must be implemented whenever you want to create a Wizard.
 * 
 * @see FormDialog
 * @author Jonathan Bingham
 */
public interface WizardModel
{
	public int getFormCount();
	public Form getFormAt(int i);
}
