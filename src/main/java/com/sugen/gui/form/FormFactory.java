package com.sugen.gui.form;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.border.Border;

/**
 * Creates components for a Form with convenient stuff set/added. These include
 * default sizes and preferences for certain Swing components as well as
 * listeners to detect modification.<P>
 *
 * Eventually, more components may need to be added. These are the most common
 * ones, or at least the ones used so far.<P>
 *
 * @see Form
 * @author Jonathan Bingham
 */
public class FormFactory {
    protected int defaultColumns;
    protected int defaultHeight;

    protected Form form;

    /**
     * A listener to detect modification of non-text form components.
     */
    protected ActionListener modificationListener = new ActionListener() {
        public void actionPerformed(ActionEvent e) {
            form.setModified(true);
        }
    };

    /**
     * A listener to detect modification of text form components.
     */
    protected KeyListener keyModificationListener = new KeyAdapter() {
        public void keyPressed(KeyEvent e) {
            if (! (e.getSource() instanceof JTextArea)) {
                if (e.getKeyCode() == KeyEvent.VK_ENTER)
                    form.getCarrier().getOkButton().doClick();
                else if (e.getKeyCode() == KeyEvent.VK_ESCAPE)
                    form.getCarrier().getCancelButton().doClick();
            }
            else
                form.setModified(true);
        }
    };

    public FormFactory(Form form) {
        this(form, 20, 125);
    }

    /**
     * @param cols The default number of columns.
     * @param height The preferred height of variable height components.
     */
    public FormFactory(Form form, int cols, int height) {
        this.form = form;
        defaultColumns = cols;
        defaultHeight = height;
    }

    public JComboBox createComboBox() {
        JComboBox box = new JComboBox();
        box.addItem(" "); //to set preferred height
        int width = form.getFontMetrics(box.getFont()).charWidth('X')
            * defaultColumns;
        //System.err.println("Combobox: " + width);
        int height = box.getPreferredSize().height;
        box.removeAllItems();
        box.setPreferredSize(new Dimension(width, height));
        box.addActionListener(modificationListener);
        return box;
    }

    public JTextField createTextField() {
        JTextField field = new JTextField();
        field.setColumns(defaultColumns);
        field.addKeyListener(keyModificationListener);
        return field;
    }

    public JPasswordField createPasswordField() {
        JPasswordField field = new JPasswordField(" ");
        field.setColumns(defaultColumns);
        field.setText(null);
        field.addKeyListener(keyModificationListener);
        return field;
    }

    public JTextArea createTextArea() {
        JTextArea area = new JTextArea();
        area.setColumns(defaultColumns);
        area.setLineWrap(true);
        area.addKeyListener(keyModificationListener);
        return area;
    }

    /**
     * Wraps the component in a JPanel along with a JLabel with text at top left.
     */
    public JComponent label(JComponent c, String title) {
        return label(c, title, FormLayout.LEFT, FormLayout.TOP);
    }

    /**
     * @param position SwingConstants.LEFT, RIGHT, TOP or BOTTOM.
     */
    public JComponent label(JComponent c, String title, int position) {
        int align = position == FormLayout.LEFT || position == FormLayout.RIGHT ?
            FormLayout.TOP : FormLayout.LEFT;
        return label(c, title, position, align);
    }

    /**
     * Wraps the component with a label.
     * @param position FormLayout.LEFT, RIGHT, TOP, BOTTOM
     * @param alignment FormLayout.LEFT, RIGHT, CENTER, TOP, BOTTOM
     * @see FormLayout Some restrictions apply to the combinations of parameters.
     */
    public JComponent label(final JComponent c, String title,
                            int position, int alignment) {
        int axis = position == SwingConstants.LEFT ||
            position == SwingConstants.RIGHT ?
            FormLayout.HORIZONTAL : FormLayout.VERTICAL;
        boolean isLabelFirst = position == FormLayout.LEFT ||
            position == FormLayout.TOP ?
            true : false;
        JLabel label = new JLabel(title);
        JPanel panel = new JPanel(new FormLayout(axis, alignment));
        if (isLabelFirst) {
            panel.add(label);
            panel.add(c);
        }
        else {
            panel.add(c);
            panel.add(label);
        }
        return panel;
    }

    public int getDefaultHeight() {
        return defaultHeight;
    }

    public int getDefaultColumns() {
        return defaultColumns;
    }

    public void setDefaultHeight(int h) {
        defaultHeight = h;
    }

    public void setDefaultColumns(int w) {
        defaultColumns = w;
    }

    /**
     * Increase the preferred size of a JComponent by its borders' insets.
     * Eg, if you have a component set to a particular size, setting its border is
     * going to cut into its space. That can be a bad thing. You might want to
     * increase the size so the border goes around it, rather than taking up space
     * inside the original area. That's what this method does.
     */
    static public JComponent setBorderAndResize(JComponent c, Border b) {
        Dimension dim = c.getPreferredSize();
        c.setBorder(b);
        Insets ins = c.getInsets();
        dim.width += ins.left + ins.right;
        dim.height += ins.top + ins.bottom;
        c.setPreferredSize(dim);
        return c;
    }
}
