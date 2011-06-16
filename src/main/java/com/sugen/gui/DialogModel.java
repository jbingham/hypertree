package com.sugen.gui;

import javax.swing.JButton;
import java.awt.BorderLayout;

/**
 * Interface common to internal dialogs and dialogs in separate windows.
 * It automatically sizes to its preferred size when set visible, and
 * automatically places itself on-screen in a suitable place, either centered
 * on top of its owner or centered on screen if its owner is null.<P>
 *
 * DialogModel includes an ok and a cancel button, which call okButtonPressed
 * and
 * cancelButtonPressed. Equivalent keyboard actions are carriage return for
 * OK and escape for cancel. The methods can be overridden in subclasses to
 * do something before closing the dialog. <P>
 *
 * A DialogModel returns a value. When the
 * user clicks OK, a non-null value is returned. When the user clicks Cancel,
 * the DialogModel returns null. The return value on OK can be set to a
 * specific
 * value if desired.
 * To get a return value:<BR>
 <PRE>
 DialogModel dialog = new MyDialogImplementation(null, false);<br>
 Object retval = dialog.showDialog();<br>
 if(retval == DialogModel.CANCEL_OPTION)<br>
 ;//user clicked cancel<br>
 else if(retval == DialogModel.ACCEPT_OPTION)<br>
 ;//user clicked OK<br>
 else <br>
 ;//user clicked OK and some other useful value was returned<br>
 </PRE>
 * If you're not interested in the return value, you can call setVisible(true)
 * instead of showDialog():<BR>
 <PRE>
 DialogModel dialog = new MyDialogImplementation(null, false);<br>
 dialog.setVisible(true); //user may click OK or Cancel to close the dialog<br>
 </PRE>
 *
 * @author Jonathan Bingham
 */
public interface DialogModel {
    public final static String PROPERTY_CLOSED = "dialogClosed";

    /** Value returned if user cancels. */
    public final static Object CANCEL_OPTION = null;
    /** Value returned if user clicks the ok button. */
    public final static Object ACCEPT_OPTION = "acceptOption";
    public final static String EAST = BorderLayout.EAST;
    public final static String SOUTH = BorderLayout.SOUTH;

    public void setButtons(JButton buttons[]);

    public void setButtons(JButton buttons[], String buttonPosition);

    public JButton[] getButtons();

    public JButton getOkButton();

    public JButton getCancelButton();

    public void okButtonPressed();

    public void cancelButtonPressed();

    public Object showDialog();

    public void setReturnValue(Object value);

    public Object getReturnValue();
}
