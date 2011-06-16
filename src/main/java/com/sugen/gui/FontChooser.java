package com.sugen.gui;

import java.awt.Font;
import java.awt.GraphicsEnvironment;
import javax.swing.*;

import com.sugen.gui.form.*;

/**
 * A simple GUI Form for choosing Fonts. Allows specification of font name,
 * loaded from the system graphics environment, of the style, and of the size.
 * <p>
 * The FontChooser must be embedded in a FormCarrier. Here's a typical use:</br>
 <pre>
 FormCarrier carrier = new FormDialog();<br>
 carrier.setForm(new FontChooser(carrier, currentFont));<br>
 Font newFont = carrier.showDialog();<br>
 </pre>
 The newFont equals null if the user canceled, or the new Font otherwise.
 *
 * @author Jonathan Bingham
 */
public class FontChooser
    extends Form {
    /** @serial */
    protected JComboBox fontBox;
    /** @serial */
    protected JTextField sizeField;
    /** @serial */
    protected JComboBox styleBox;

    public FontChooser(FormDialog dialog, Font font) {
        super(dialog);
        setTitle("Choose Font");

        setLayout(new FormLayout(FormLayout.VERTICAL, FormLayout.CENTER));
        ((FormLayout)getLayout()).setResizeConstant(true);
        fontBox =
            new JComboBox(GraphicsEnvironment.getLocalGraphicsEnvironment().
                          getAvailableFontFamilyNames());

        sizeField = new JTextField();
        sizeField.setColumns(2);

        styleBox = new JComboBox();
        styleBox.addItem("Plain");
        styleBox.addItem("Bold");
        styleBox.addItem("Italic");

        setCurrentFont(font);

        FormFactory factory = new FormFactory(this);
        this.add(factory.label(fontBox, "Font"));
        this.add(factory.label(sizeField, "Size"));
        this.add(factory.label(styleBox, "Style"));
    }

    public void applyChanges() {
        Font font = new Font((String)fontBox.getSelectedItem(),
                             styleBox.getSelectedIndex(),
                             Integer.parseInt(sizeField.getText()));
        setReturnValue(font);
    }

    public void setCurrentFont(Font font) {
        sizeField.setText(String.valueOf(font.getSize()));
        fontBox.setSelectedItem(font.getName());
        styleBox.setSelectedIndex(font.getStyle());
    }
}
