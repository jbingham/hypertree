package com.sugen.gui;

import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Insets;
import java.awt.LayoutManager;

import javax.swing.SwingConstants;

/**
 * A layout manager designed for creating a grid with either one row or one
 * column. You don't have to know in advance how many rows or columns. Just add
 * components. Also, there are no insets unless you explicitly set them on
 * the container.
 * <P>
 * @author Jonathan Bingham
 */
public class LinearGridLayout
    implements LayoutManager, java.io.Serializable {
    public final static int HORIZONTAL = SwingConstants.HORIZONTAL;
    public final static int VERTICAL = SwingConstants.VERTICAL;

    /** @serial */
    protected int gap;
    /** @serial */
    protected int axis;

    /**
     * By default, a HORIZONTAL layout with gap of 10
     */
    public LinearGridLayout() {
        this(HORIZONTAL);
    }

    public LinearGridLayout(int axis) {
        this(axis, 10);
    }

    public LinearGridLayout(int axis, int gap) {
        this.axis = axis;
        this.gap = gap;
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
    public void setSpacing(int gap) {
        this.gap = gap;
    }

    public int getSpacing() {
        return gap;
    }

    /**
     * The components can be laid out HORIZONTAL-ly or VERTICAL-ly, similar to
     * a BoxLayout.
     */
    public void setAxis(int axis) {
        this.axis = axis;
    }

    public int getAxis() {
        return axis;
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
    public Dimension preferredLayoutSize(Container target) {
        Insets insets = target.getInsets();
        int w = 0;
        int h = 0;

        int maxw = 0;
        int maxh = 0;
        int nvisible = 0;

        for(int i = 0, nmembers = target.getComponentCount(); i < nmembers; i++) {
            Component comp = target.getComponent(i);
            if(comp.isVisible()) {
                ++nvisible;
                Dimension d = comp.getPreferredSize();
                if(d.height > maxh - insets.top - insets.bottom)
                    maxh = d.height + insets.top + insets.bottom;
                if(d.width > maxw - insets.left - insets.right)
                    maxw = d.width + insets.left + insets.right;
            }
        }
        //If there's anything at all in the layout, calculate stuff
        if(nvisible > 0) {
            if(axis == HORIZONTAL) {
                h = maxh;
                w = maxw * nvisible + (nvisible - 1) * gap;
            }
            else { //VERTICAL
                w = maxw;
                h = maxh * nvisible + (nvisible - 1) * gap;
            }
            h += insets.top + insets.bottom;
            w += insets.left + insets.right;
        }
        return new Dimension(w, h);
    }

    public Dimension maximumLayoutSize(Container target) {
        return target.getMaximumSize();
    }

    /**
     * Returns the preferred size.
     */
    public Dimension minimumLayoutSize(Container target) {
        return preferredLayoutSize(target);
    }

    /**
     * Lays out the container. Gets the current size of its target, and uses up all
     * of the space, making component size equal along the axis.
     */
    public void layoutContainer(Container target) {
        Insets insets = target.getInsets();
        int nmembers = target.getComponentCount();
        int maxh = 0;
        int maxw = 0;
        int nvisible = 0;

        for(int i = 0; i < nmembers; i++) {
            Component comp = target.getComponent(i);
            if(comp.isVisible()) {
                ++nvisible;
                Dimension d = comp.getPreferredSize();
                if(d.width > maxw)
                    maxw = d.width;
                if(d.height > maxh)
                    maxh = d.height;
            }
        }
        if(nvisible == 0)return;

        int x = insets.left;
        int y = insets.top;
        for(int i = 0; i < nmembers; i++) {
            Component comp = target.getComponent(i);
            if(comp.isVisible()) {
                comp.setBounds(x, y, maxw, maxh);
                if(axis == HORIZONTAL)
                    x += maxw + gap;
                else
                    y += maxh + gap;
            }
        }
    }

    public String toString() {
        return getClass().getName() + "(axis:" + axis + ",gap:" + gap + ")";
    }
}
