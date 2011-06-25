package com.sugen.gui.plot;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Insets;

import javax.swing.JComponent;
import javax.swing.border.Border;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;

import com.sugen.gui.ToolTippable;

/**
 * Default implementation of a LabelRenderer.
 *
 * @author Jonathan Bingham
 */
public class DefaultLabelRenderer extends JComponent implements LabelRenderer {
	private static final long serialVersionUID = 1L;
	
    protected String text;
    protected Object dataObject;
    protected boolean isAdjusting;
    protected boolean hasFocus;
    protected boolean isSelected;
    protected Component view;
    protected int key = -1;
    protected boolean isVisible;

    //Colors
    /** @serial */
    protected Color defaultColor = Color.black;
    /** @serial */
    protected Color backgroundColor = com.sugen.gui.Palette.lightYellow; //new Color(185, 240, 205);
    /** @serial */
    protected Color emptyColor = new Color(0x7B68EE);
    /** @serial */
    protected Color focusColor = com.sugen.gui.Palette.lightYellow; //UIManager.getColor("List.selectionBackground");
    protected Color selectionColor = com.sugen.gui.Palette.lightBlue;

    //Borders
    /** @serial */
    protected Border border = new LineBorder(backgroundColor.darker().darker().darker());
    /** @serial */
    protected Border focusBorder = new LineBorder(Color.darkGray);
    /** @serial */
    protected Border emptyBorder = new EmptyBorder(4, 4, 4, 4);
    /** @serial */
    protected Border selectionBorder = new LineBorder(focusColor.darker().darker().darker());

    //Measurements - stored to avoid recomputing
    /** @serial */
    protected FontMetrics metrics;
    /** @serial */
    protected int baseline;
    /** @serial */
    protected Dimension hiddenSize = new Dimension(8, 8);
    /** @serial */
    protected Dimension dimension = new Dimension();

    public DefaultLabelRenderer() {
        setFont(new Font("SansSerif", Font.PLAIN, 12));
    }

    public Component getRendererComponent(Component view,
          Object dataObject, 
          int key,
          boolean isSelected, 
          boolean hasFocus,
          boolean isAdjusting,
          boolean isVisible, 
          Color color) {
    	this.dataObject = dataObject;
        this.isAdjusting = isAdjusting;
        this.hasFocus = hasFocus;
        this.isSelected = isSelected;
        this.view = view;
        this.key = key;
        this.isVisible = isVisible;
        if(color == null)
            color = defaultColor;

        setOpaque(isSelected || hasFocus);

        if(hasFocus) {
            setBorder(focusBorder);
            setBackground(focusColor);
            setForeground(defaultColor);
        }
        else if(isSelected) {
            setBorder(selectionBorder);
            setBackground(selectionColor);
            setForeground(defaultColor);
        }
        else {
            setBorder((text == null || text.equals("")) ? emptyBorder : border);
            //setBackground(backgroundColor); //doesn't matter - it won't be opaque
            setForeground(color);
        }
        setText(dataObject);
        
        if (dataObject instanceof ToolTippable) {
        	((JComponent)view).setToolTipText(
        			((ToolTippable)dataObject).getToolTipText());
        }
        return this;
    }

    public void setText(Object dataObject) {
        this.text = (dataObject == null || dataObject.equals("")) ?
            null : dataObject.toString();
        
        //No size if adjusting and invisible
        if(isAdjusting && !isSelected && !hasFocus && !isVisible) {
            dimension.setSize(0, 0);
            setPreferredSize(dimension);
            return;
        }
        //Minimal size otherwise
        if(!isVisible && !hasFocus && !isSelected) {
            setPreferredSize(hiddenSize);
            return;
        }

        //Preferred size and text baseline
        Insets insets = getInsets();
        if(text != null) {
            int w = insets.left + insets.right + metrics.stringWidth(text);
            int h = insets.top + insets.bottom + metrics.getHeight();
            dimension.setSize(w, h);
            baseline = metrics.getAscent() + insets.top;
        }
        else if(isAdjusting)
            dimension.setSize(0, 0);
        else // if(hasFocus)
            dimension.setSize(hiddenSize);
        setPreferredSize(dimension);
    }

    public void paint(Graphics g) {
        Insets insets = getInsets();
        if(hasFocus || isSelected) {
            if(text != null && getBorder() != null) {
                getBorder().paintBorder(this, g, 0, 0, getWidth(), getHeight());
                g.setColor(getBackground());
                g.fillRect(insets.left, insets.top,
                           getWidth() - insets.left - insets.right,
                           getHeight() - insets.top - insets.bottom);
            } else if(text == null) {
                g.setColor(getBackground());
                g.fillOval(0, 0, getWidth() - 1, getHeight() - 1);
                g.setColor(getForeground());
                g.drawOval(0, 0, getWidth() - 1, getHeight() - 1);
            }
        }
        if(text != null) {
            g.setColor(getForeground());
            g.setFont(super.getFont());
            g.drawString(text, insets.left, baseline);
        }
    }

    public void setFont(Font f) {
        super.setFont(f);
        metrics = getFontMetrics(f);
    }

    public boolean isShowing() {
        return true;
    }
}
