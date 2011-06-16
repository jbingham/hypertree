package com.sugen.gui.form;

import java.awt.*;
import javax.swing.*;

import com.sugen.gui.*;

/**
 * A layout manager designed to be of special use to dialog boxes and data entry
 * forms.
 * <P>
 * Lays out a container in unevenly sized rows or columns. If horizontally laid out,
 * they can be center, top, or bottom justified. If vertically, left, right, 
 * or center. Each row can be 'wrapped' with a label, whose placement depends on
 * the alignment and axis. 
 * <P>
 * The container respects preferred sizes. It resizes 'smartly', 
 * depending on the types of components it contains.
 *
 * @author Jonathan Bingham
 */
public class FormLayout implements LayoutManager, java.io.Serializable
{
	public final static int DEFAULT_GAP = 10;
	public final static int HORIZONTAL = SwingConstants.HORIZONTAL;
	public final static int VERTICAL = SwingConstants.VERTICAL;
	public final static int LEFT = SwingConstants.LEFT;
	public final static int RIGHT = SwingConstants.RIGHT;
	public final static int CENTER = SwingConstants.CENTER;
	public final static int TOP = SwingConstants.TOP;
	public final static int BOTTOM = SwingConstants.BOTTOM;
	
	/** @serial */
	protected int alignment;
	/** @serial */
	protected int gap;
	/** @serial */
	protected int axis;
	
	/**
	 * When the components have extra space in the direction perpendicular 
	 * to the axis, do they each grow by a constant factor, 
	 * or do the resizeable components take up all space available to them?
	 * Default: false.
	 * <P>
	 * E.g., imagine a bunch of components laid out horizontally. Some are shorter
	 * than others (in the 'perpendicular' direction). If these are resizeable,
	 * they might all take up the maximum space, thus achieving the same
	 * height. This is the case of isResizeConstant is false.
	 * Alternatively, they might retain the constant differences in height;
	 * the tallest would remain tallest, but would occupy all space. The shorter
	 * components would remain shorter by the same constant factor. This is the
	 * case if isResizeConstant is true. 
	 * @serial 
	 */
	protected boolean isResizeConstant;
	
	/**
	 * Should the component be resizeable only if at least one child is?
	 * Default false.
	 * @serial 
	 */
	protected boolean isResizeRecursive;
	/** @serial */
	protected boolean isResizeable;
	
	/**
	 * By default, a HORIZONTAL layout with vertically CENTERed components 
	 * 10 pixels apart and perpendicular constant resize FALSE.
	 */
	public FormLayout()
	{
		this(HORIZONTAL);
	}
	
	
	public FormLayout(int axis)
	{
		this(axis, CENTER);
	}
	
	
	public FormLayout(int axis, int align)
	{
		this.axis = axis;
		setSpacing(FormLayout.DEFAULT_GAP);
		setAlignment(align);
		isResizeConstant = false;
		isResizeRecursive = true;
		isResizeable = true;
	}
	

	/**
	 * How many pixels of space separate components?
	 * <P>
	 * Note: around the perimeter of the container, there will be NO GAP at all.
	 * This is different from FlowLayout, for example, where the gap applies
	 * both between components and around their borders. The reason for having
	 * no insets is simple: you can add an EmptyBorder if you want insets.
	 * If there were insets by default and you didn't want them, they would
	 * not be so simple to take away.
	 */
	public void setSpacing(int gap)
	{
		this.gap = gap;
	}
	
	
	public int getSpacing()
	{
		return gap;
	}
	
	
	/**
	 * When the components have extra space in the direction perpendicular 
	 * to the axis, do they each grow by a constant factor, 
	 * or do the resizeable components take up all space available to them?
	 * Default: false.
	 * <P>
	 * E.g., imagine a bunch of components laid out horizontally. Some are shorter
	 * than others (in the 'perpendicular' direction). If these are resizeable,
	 * they might all take up the maximum space, thus achieving the same
	 * height. This is the case of isResizeConstant is false.
	 * Alternatively, they might retain the constant differences in height;
	 * the tallest would remain tallest, but would occupy all space. The shorter
	 * components would remain shorter by the same constant factor. This is the
	 * case if isResizeConstant is true. 
	 */
	public void setResizeConstant(boolean b)
	{
		isResizeConstant = b;
	}
	
	
	public boolean isResizeConstant()
	{
		return isResizeConstant();
	}
	
	
	public void setResizeRecursive(boolean b)
	{
		isResizeRecursive = b;
	}
	
	
	public boolean isResizeRecursive()
	{
		return isResizeRecursive;
	}
	
	
	public void setResizeable(boolean b)
	{
		isResizeable = b;
	}
	
	
	public boolean isResizeable()
	{
		return isResizeable;
	}
	
	
	/**
	 * The components can be laid out HORIZONTAL-ly or VERTICAL-ly, similar to
	 * a BoxLayout.
	 */
	public void setAxis(int axis)
	{
		this.axis = axis;
	}
	
	
	public int getAxis()
	{
		return axis;
	}
	
	
	/**
	 * HORIZONTAL or VERTICAL.
	 * HORIZONTAL layouts can have their components aligned TOP, BOTTOM or CENTER;
	 * VERTICAL layouts can have their components aligned LEFT, RIGHT or CENTER.
	 */
	public void setAlignment(int align)
	{
		this.alignment = align;
	}

	
	public int getAlignment()
	{
		return alignment;
	}
	
	
	/** 
	 * Not used; from superclass. 
	 */
    public void addLayoutComponent(String name, Component comp) {}
	
	
    /**
     * Not used.  
     */
    public void removeLayoutComponent(Component comp) {}

	
    /**
     * Returns the preferred size of the target by looking at the preferred
	 * sizes of all components in the layout.
     */
    public Dimension preferredLayoutSize(Container target) 
	{
		Insets insets = target.getInsets();
		int w = insets.left + insets.right;
		int h = insets.top + insets.bottom;
		
		int maxw = 0;
		int maxh = 0;
		int nvisible = 0;

		for(int i = 0, nmembers = target.getComponentCount(); i < nmembers; i++)
		{
		    Component comp = target.getComponent(i);
		    if(comp.isVisible()) 
			{
				++nvisible;
				Dimension d = comp.getPreferredSize();
				w += d.width + gap;
				h += d.height + gap;
				if(d.height > maxh - insets.top - insets.bottom)
					maxh = d.height + insets.top + insets.bottom;
				if(d.width > maxw - insets.left - insets.right)
					maxw = d.width + insets.left + insets.right;
			}
		}
		//If there's anything at all in the layout, we overshot a little
		if(nvisible > 0)
		{
			if(axis == HORIZONTAL)
			{
				h = maxh;
				w -= gap;
			}
			else //VERTICAL 
			{
				w = maxw;
				h -= gap;
			}
		}
		//If there's nothing, the layout requires no space at all.
		else
		{
			w = 0;
			h = 0;
		}
		return new Dimension(w, h);
    }

	
	public Dimension maximumLayoutSize(Container target) 
	{
		return target.getMaximumSize();
    }

	
    /**
	 * Returns the preferred size.
     */
    public Dimension minimumLayoutSize(Container target) 
	{
		return preferredLayoutSize(target);
    }


    /**
     * Lays out the container. Gets the current size of its target, and uses up all
	 * of the space, distributed among those components that are horizontally or
	 * vertically resizeable.
     */
    public void layoutContainer(Container target) 
	{
		Insets insets = target.getInsets();
		Dimension size = target.getSize();
		int maxw = size.width - (insets.left + insets.right);
		int maxh = size.height - (insets.top + insets.bottom);
		int nmembers = target.getComponentCount();
		
		//See if there's any extra space to be distributed among resizable components
		int minw = 0;
		int minh = 0;
		int nhresize = 0; //How many components take up horiz slack?
		int nvresize = 0; //How many components take up vert slack?
		int nvisible = 0;
		
		for(int i = 0; i < nmembers; i++)
		{
		    Component comp = target.getComponent(i);
		    if(comp.isVisible()) 
			{
				++nvisible;
				Dimension d = comp.getPreferredSize();
				if(axis == HORIZONTAL)
				{
					if(d.height > minh) 
						minh = d.height;
					minw += d.width + gap; 
					if(isHorizontallyResizeable(comp) && 
						(!isResizeRecursive ||
						!(comp instanceof Container) || 
						isChildResizeable((Container)comp, HORIZONTAL)))
						++nhresize;
				}
				else //VERTICAL
				{
					if(d.width > minw) 
						minw = d.width;
					minh += d.height + gap;
					if(isVerticallyResizeable(comp) && 
						(!isResizeRecursive ||
						!(comp instanceof Container) || 
						isChildResizeable((Container)comp, VERTICAL)))
						++nvresize;
				}
			}
		}
		//If there's anything at all in the layout, we overshot a little
		if(nvisible > 0)
		{
			if(axis == HORIZONTAL) 
				minw -= gap;
			else //VERTICAL 
				minh -= gap;
		}
		else return; //nothing to layout

		int extraw = (maxw > minw ? maxw - minw : 0);
		int extrawper = 0; //Extra pixels width to be added to each 
		int extrah = (maxh > minh ? maxh - minh : 0);
		int extrahper = 0; //Extra pixels height to be added to each
		if(nhresize != 0)
			extrawper = extraw / nhresize;
		if(nvresize != 0)
			extrahper = extrah / nvresize;

		//Layout each component
		int x = insets.left; //Left edge of row or column
		int y = insets.top; //Top edge of row or column
		
		int compx = 0; //Left edge of component in that row or column
		int compy = 0; //Top edge of component in that row or column
		
		for(int i = 0 ; i < nmembers ; i++) 
		{
			compx = x;
			compy = y;
			
		    Component comp = target.getComponent(i);
		    if(comp.isVisible()) 
			{
				//Set to preferred size plus any extra room, if the component
				//will accept it
				Dimension d = comp.getPreferredSize();
				int w = d.width;
				int h = d.height;
				
				//Set location depending on alignment
				if(axis == HORIZONTAL)
				{
					if(extrawper != 0 && isHorizontallyResizeable(comp) && 
						(!isResizeRecursive ||
						!(comp instanceof Container) || 
						isChildResizeable((Container)comp, HORIZONTAL)))
						w += extrawper;
					if(isVerticallyResizeable(comp) && 
						(!isResizeRecursive ||
						!(comp instanceof Container) || 
						isChildResizeable((Container)comp, VERTICAL)))
					{
						if(isResizeConstant)
							h += extrah;
						else
							h = maxh;
					}
					comp.setSize(w, h);
					if(h < maxh && alignment != TOP)
					{
						if(alignment == BOTTOM)
							compy += maxh - h;
						else //CENTER 
							compy += (maxh - h)/2;
					}
					x += w + gap; //Only x changes in a horizontal layout
				}
				else if(axis == VERTICAL)
				{
					if(extrahper != 0 && isVerticallyResizeable(comp) && 
						(!isResizeRecursive ||
						!(comp instanceof Container) || 
						isChildResizeable((Container)comp, VERTICAL)))
						h += extrahper;
					if(isHorizontallyResizeable(comp) && 
						(!isResizeRecursive ||
						!(comp instanceof Container) || 
						isChildResizeable((Container)comp, HORIZONTAL)))
					{
						if(isResizeConstant)
							w += extraw;
						else 
							w = maxw;
					}
					comp.setSize(w, h);
					if(w < maxw && alignment != LEFT)
					{
						if(alignment == RIGHT)
							compx += maxw - w;
						else //CENTER 
							compx += (maxw - w)/2;
					}
					y += h + gap; //Only y changes in a vertical layout
				}
				comp.setLocation(compx, compy);
			}
		}
	}
    
	/**
	 * Anything except an AbstractButton, a JTextField, a JLabel, a 
	 * HORIZONTAL JSeparator, and a
	 * JComboBox is vertically resizeable.
	 * You can overload this to control which components are vertically stretched to
	 * occupy available space.
	 */
	public boolean isVerticallyResizeable(Component c)
	{
		if(!isResizeable) 
			return false;
		if(c instanceof AbstractButton || c instanceof JTextField ||
			c instanceof JLabel || c instanceof JComboBox)
			return false;
		if(c instanceof JSeparator && 
			((JSeparator)c).getOrientation() == JSeparator.HORIZONTAL)
			return false;
		return true;
	}
    
	
	/**
	 * Anything except a JLabel, AbstractButton, ButtonRow,
	 * or VERTICAL JSeparator is horizontally resizeable.
	 * Overload this to control which components are horizontally stretched to
	 * occupy available space. 
	 */
	public boolean isHorizontallyResizeable(Component c)
	{
		if(!isResizeable) 
			return false;
		if(c instanceof JLabel || c instanceof AbstractButton 
			|| c instanceof ButtonRow)
			return false;
		if(c instanceof JSeparator && 
			((JSeparator)c).getOrientation() == JSeparator.VERTICAL)
			return false;
		return true;
	}
	
	/**
	 * A container is not resizeable unless at least one child is, 
	 * if isResizeRecursive is true.
	 */ 
	protected boolean isChildResizeable(Container ct, int axis)
	{
		if(!isResizeable) 
			return false;
		LayoutManager lay = ct.getLayout();
		if(lay != null && lay instanceof FormLayout)
		{
			boolean isNoChild = true;
			FormLayout flay = (FormLayout)lay;
			for(int i = 0, j = ct.getComponentCount(); i < j && isNoChild; i++)
			{
				Component subc = ct.getComponent(i);
				if(subc.isVisible())
				{
					if(axis == HORIZONTAL)
						isNoChild = isNoChild && !flay.isHorizontallyResizeable(subc);
					else //VERTICAL
						isNoChild = isNoChild && !flay.isVerticallyResizeable(subc);
				}
			}
			return !isNoChild;
		}
		return true;
	}
}
