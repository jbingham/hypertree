package com.sugen.gui.form;

import java.awt.Component;
import java.awt.Cursor;
import java.awt.event.MouseAdapter;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import javax.swing.JButton;

import com.sugen.gui.InternalDialog;
import com.sugen.gui.SwingWorker;

/**
 * Implementation of a FormCarrier as an internal dialog.
 * 
 * @author Jonathan Bingham
**/
public class InternalFormDialog extends InternalDialog 
	implements FormCarrier, PropertyChangeListener
{
	/** @serial */
	protected FormPane formPane = new FormPane();
    
	public InternalFormDialog()
	{
 		getContentPane().add(formPane);
		formPane.addPropertyChangeListener(this);
		
		getGlassPane().addMouseListener(new MouseAdapter(){}); //for cursor
		getGlassPane().setCursor(new Cursor(Cursor.WAIT_CURSOR));
    }
	
	/**
	 * In threaded form processing, interrupt the thread before closing.
	 */
	public void cancelButtonPressed()
	{
		formPane.cancel();
		super.cancelButtonPressed();
	}
	
    public void setForm(Form form)
    { 
		formPane.setForm(form);
		pack();
    }

    public Form getForm()
    { 
		return formPane.getForm();
    }
	
	/**
	 * If there will be more than one Form, a non-null model is required.
	 */
	public void setModel(WizardModel model)
	{
		formPane.setModel(model);
		pack();
	}
	
	public WizardModel getModel()
	{
		return formPane.getModel();
	}
    
	/**
	 * Process OK, Next and Finish in a separate, interruptable thread.
	 */
	public void okButtonPressed()
	{
		formPane.ok(okButton.getText());
	}
	
	/**
	 * Show/hide glass pane and waiting cursor.
	 */
	public void setWaiting(boolean isWaiting)
	{
		getGlassPane().setVisible(isWaiting);
	}
	
	public Component getCarrierComponent()
	{
		return this;
	}
	
	public void propertyChange(PropertyChangeEvent pce)
	{
		String name = pce.getPropertyName();
		if(name.equals(FormPane.PROPERTY_BACK_BUTTON))
		{
			if(pce.getNewValue() != null)
			{
				JButton backButton = (JButton)pce.getNewValue();
				JButton[] buttons = getButtons();
				JButton[] newButtons = new JButton[buttons.length + 1];
				newButtons[0] = backButton;
				for(int i = 0; i < buttons.length; i++)
				{
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
	
	public SwingWorker getWorker()
	{
		return formPane.getWorker();
	}
	
	public void setWorker(SwingWorker worker)
	{
		formPane.setWorker(worker);
	}
	
	public FormPane getFormPane()
	{
		return formPane;
	}
}
