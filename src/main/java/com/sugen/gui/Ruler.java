package com.sugen.gui;

import javax.swing.JLabel;
import javax.swing.SwingConstants;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Rectangle;
import java.awt.Dimension;
import java.awt.Insets;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;

import java.text.NumberFormat;

/**
 * A resizeable and customizable ruler.
 *
 * <p>It's amazing how complicated such a seemingly simple thing can be.
 * It displays sensible units at readable intervals, and optionally marks
 * some particular measurement. Currently the only orientation supported is
 * horizontal, with the tick marks along the bottom. Ideally, the Ruler
 * should be generalizable, so that it can be oriented vertically, with
 * tick marks along the top, left, bottom or right. Also, it should be
 * possible to apply transformations to the ruler scale to support log,
 * inverse transform, etc. But for the moment, these are pipe dreams.
 *
 * @author Jonathan Bingham
 */
public class Ruler
    extends JLabel {
    public static final int TOP = SwingConstants.TOP;
    public static final int LEFT = SwingConstants.LEFT;
    public static final int BOTTOM = SwingConstants.BOTTOM;
    public static final int RIGHT = SwingConstants.RIGHT;

    //public static final int IDENTITY_TRANSFORM = 1;
    //public static final int LOG_TRANSFORM = 2;
    //public static final int INVERSE_TRANSFORM = 4;
    //public static final int INVERSE_LOG_TRANSFORM = 8;

    /**
     * Controls the factor of the numbers that appear as labels
     * at each major tick.
     * @serial
     */
    protected int rate = 5;

    //TODO: implement ruler position.
    /**
     * Either LEFT, RIGHT, TOP or BOTTOM.
     * Where the ruler goes relative to what it's measuring. Determines
     * text placement.
     */
    //protected int position = TOP;
    /** @serial */
    protected boolean areLabelsVisible = true;
    /** @serial */
    protected boolean isSliderVisible = false;

    //TODO: implement transform
    /**
     * Transform used for the ruler units.
     */
    //protected int transform = 0; //IDENTITY_TRANSFORM;

    /** The maximum value on the ruler.
     * @serial
     */
    protected double max = 0;
    /** The minimum value on the ruler.
     * @serial
     */
    protected double min = 0;
    /**
     * Number of units per minor tick. For an integer ruler,
     * always an integer unless <1, in which case
     * 1/unitsPerTick is always an integer.
     * @serial
     */
    protected double unitsPerTick = 1;
    /** Number of pixels per minor tick.
     * @serial
     */
    protected double pixelsPerTick = 1;
    /** Scale in units per pixel.
     * @serial
     */
    protected double unitsPerPixel = 1;
    /** The number of minor ticks per major tick on the ruler.
     * @serial
     */
    protected int minorTicks = 10;
    /**
     * Minimum pixels per minor tick, chosen to allow space for labels.
     * Adjusted on the basis of the screen font.
     * @serial
     */
    protected double minPixelsPerTick = 1;
    /** Metric for the font used to label major tick intervals.
     * @serial
     */
    protected FontMetrics fontMetrics;
    /** Used to format labels at major ticks.
     * @serial
     */
    protected NumberFormat numberFormat = NumberFormat.getNumberInstance();
    /** Are labels shown? .
     * @serial
     */
    protected boolean isLabeled;

    /** Base of vertical ticks.
     * @serial
     */
    protected int tickBase;
    /** Top of vertical ticks.
     * @serial
     */
    protected int tickEnd;
    /** Minor ticks are one third the height of major ticks.
     * @serial
     */
    protected int minorTickEnd;
    /** Middle ticks are two thirds the height of major ticks.
     * @serial
     */
    protected int middleTickEnd;

    public Ruler() {
        Font font = getFont();
        setFont(new Font(font.getName(), font.getStyle(), 9));
        setOpaque(true);

        //On resize, recalibrate the ruler
        addComponentListener(new ComponentAdapter() {
            public void componentResized(ComponentEvent e) {
                Ruler rule = (Ruler)e.getSource();
                rule.computeParameters();
                repaint();
            }
        });
        //Mark the point where mouse is clicked
        addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent e) {
                if(isSliderVisible) {
                    int x = e.getX();
                    setMark((x - getInsets().left) * getScale() + min);
                    repaint();
                }
            }
        });
        //Mark the point where the mouse is dragging
        addMouseMotionListener(new MouseMotionAdapter() {
            public void mouseDragged(MouseEvent e) {
                if(isSliderVisible) {
                    int x = e.getX();
                    setMark((x - getInsets().left) * getScale() + min);
                    repaint();
                }
            }
        });
    }

    public void setSliderVisible(boolean b) {
        isSliderVisible = b;
    }

    public void setLabelsVisible(boolean b) {
        isSliderVisible = b;
    }

    /**
     * Not implemented.
     * Either LEFT, RIGHT, TOP or BOTTOM.
     * Where the ruler goes relative to what it's measuring. Determines
     * text placement.
     */
    /*public void setPosition(int i)
      {
     position = i;
      }*/

    public boolean isSliderVisible() {
        return isSliderVisible;
    }

    public boolean areLabelsVisible() {
        return areLabelsVisible;
    }

    /**
     * Not implemented.
     * Either LEFT, RIGHT, TOP or BOTTOM.
     * Where the ruler goes relative to what it's measuring. Determines
     * text placement.
     */
    /*public int getPosition()
      {
     return position;
      }*/

    public void computeParameters() {
        Dimension size = getSize();
        Insets insets = getInsets();

        //Compute the minimum tick spacing to accommodate the new max value.
        //Could further refine based on the range in view...
        //HACK: the 8 at the end is a workaround
        int widthMax = fontMetrics.stringWidth(numberFormat.format(max)) + 8;
        int widthMin = fontMetrics.stringWidth(numberFormat.format(min)) + 8;
        minPixelsPerTick = Math.max(widthMin, widthMax) / minorTicks;

        //Compute coordinates needed to draw ticks.
        tickBase = size.height - insets.bottom;
        //2: gap btwn label & tick
        tickEnd = insets.top + fontMetrics.getHeight() + 2;
        minorTickEnd = (int)(tickEnd + (tickBase - tickEnd) * 2 / 3);
        middleTickEnd = (int)(tickEnd + (tickBase - tickEnd) / 3);

        //System.err.println("max:" + max + ",min:" + min + ",width:" + size.width);

        unitsPerPixel = (max - min) / (size.width - insets.left - insets.right);
        if(unitsPerPixel <= 0)
            return;

        //Ruler shouldn't be too fine-grained to fit the numbers.
        while((int)((double)unitsPerTick / unitsPerPixel) < minPixelsPerTick)
            unitsPerTick *= rate;
        //Ruler shouldn't be any more coarse-grained than it has to be.
        while((int)((double)unitsPerTick / unitsPerPixel) >=
              minPixelsPerTick * rate)
            unitsPerTick /= rate;
        pixelsPerTick = (double)unitsPerTick / unitsPerPixel;

        if(pixelMark < insets.left)
            pixelMark = insets.left;
        setMark(mark);

        //System.err.println("unitsPerPixel:" + unitsPerPixel + ",unitsPerTick:" + unitsPerTick + ",pixelsPerTick:" + pixelsPerTick);
        //System.err.println("pixelsPerTick:" + pixelsPerTick + ",tickBase:" + tickBase + ",tickEnd:" + tickEnd);
        //System.err.println("max:" + max + ",min:" + min + ",mark:" + mark);
    }

    public void setFont(Font font) {
        //Overridden to store current font metrics.
        super.setFont(font);
        fontMetrics = getFontMetrics(getFont());
        setPreferredSize(
            new Dimension(getPreferredSize().width, fontMetrics.getAscent() * 3));
    }

    public void paint(Graphics g) {
        super.paint(g);
        if(max == min)
            return;

        //Only paint the viewable area - otherwise performance suffers
        //on large rulers
        Rectangle rect = getVisibleRect();
        Dimension size = getSize();
        Insets insets = getInsets();

        Color fg = getForeground();
        g.setColor(fg);

        if(isSliderVisible) {
            g.setColor(glassColor);
            g.fillRect(markRectangle.x, markRectangle.y,
                       markRectangle.width, markRectangle.height);
            g.setColor(fg);
        }

        //Number of whole ticks so far
        int numTicks = (int)(
            (((double)(rect.x - insets.left)
              / (double)(size.width - insets.left - insets.right)) //% not shown on left
             * (max - min)) //times range = ruler value in units from left end
            / unitsPerTick); //convert to ticks
        if(numTicks < 0)
            numTicks = 0; //may be at far left, so insets screwed up calculation

        //Starting point to paint
        double x = numTicks * pixelsPerTick + insets.left;

        //May need to start with a fractional tick
        //BUG: only works for units/tick > 1
        double fractionalTicks = (min % unitsPerTick) / unitsPerTick;
        if(fractionalTicks != 0d) {
            x += (1 - fractionalTicks) * pixelsPerTick;
            numTicks++;
        }

        //Ruler may start with minor ticks rather than major, for the sake of
        //labeling. Ticks off from the last major tick.
        int ticksOff = (int)(min % (unitsPerTick * minorTicks));
        //System.err.println("unitsPerTick:" + unitsPerTick + ",min:" + min + ",minorTicks:" + minorTicks);

        //Ending point to paint
        int xend = rect.x + rect.width;
        if(xend > size.width - insets.right)
            xend = size.width - insets.right;

        //System.err.println("numTicks:" + numTicks + ",x:" + x + ",xend:" + xend + ",pixerlsPerTick:" + pixelsPerTick);
        //System.err.println("pixelMark:" + pixelMark + ",mark:" + mark);

        int labelBase = fontMetrics.getAscent() + insets.top;
        int intx = (int)Math.round(x);
        while(intx <= xend) {
            //Draw major tick with label
            if((numTicks + ticksOff) % minorTicks == 0) {
                g.drawLine(intx, tickBase, intx, tickEnd);

                //If there's already a tick with the same label, don't repeat it
                if(areLabelsVisible && (unitsPerTick * numTicks) % 1 == 0) {
                    String tickLabel = numberFormat.format(
                        unitsPerTick * (numTicks - fractionalTicks) + min);
                    g.drawString(tickLabel,
                                 intx - fontMetrics.stringWidth(tickLabel) / 2,
                                 labelBase);
                }
            }
            //Draw middle tick
            else if((numTicks + ticksOff) % (minorTicks / 2) == 0) {
                g.drawLine(intx, tickBase, intx, middleTickEnd);
            }
            //Draw minor tick
            else
                g.drawLine(intx, tickBase, intx, minorTickEnd);
            x += pixelsPerTick;
            intx = (int)Math.round(x);
            numTicks++;
        }

        //Paint glass slider with crosshair
        if(isSliderVisible) {
            g.setColor(getBackground());
            g.draw3DRect(markRectangle.x, markRectangle.y,
                         markRectangle.width, markRectangle.height, true);
            int mark = (int)Math.round(pixelMark);
            g.setColor(Color.red);
            g.drawLine(mark, 0, mark, size.height);
            g.setColor(fg);
        }
    }

    public void setMax(double x) {
        max = x;
        computeParameters(); //They depend on max
    }

    public void setMin(double x) {
        min = x;
        computeParameters(); //They depend on min
    }

    public double getMax() {
        return max;
    }

    public double getMin() {
        return min;
    }

    /** Used to format labels at major ticks. */
    /*public void setNumberFormat(NumberFormat format)
      {
     numberFormat = format;
     computeParameters();
      }*/

    /** Used to format labels at major ticks. */
    /*public NumberFormat getNumberFormat()
      {
     return numberFormat;
      }*/

    public int getRate() {
        return rate;
    }

    public void setRate(int i) {
        rate = i;
    }

    /*public int getTransform()
      {
     return transform;
      }


      public void setTransform(int i)
      {
     transform = i;
      }*/

    /**
     * Scale in units per pixel. Useful for converting other pixel values into
     * the corresponding unit measurement.
     */
    public double getScale() {
        return unitsPerPixel;
    }

    public int getMinorTicks() {
        return minorTicks;
    }

    public void setMinorTicks(int i) {
        minorTicks = i;
    }

    /**
     *  The pixel coordinate needed equivalent of the mark.
     * @serial
     */
    protected double pixelMark = Integer.MIN_VALUE;

    /**
     * A measurement point along the ruler in the ruler's units.
     * @serial
     */
    protected double mark = 0;
    /** @serial */
    protected Rectangle markRectangle = new Rectangle();
    /** @serial */
    protected Color glassColor;

    /**
     * Mark a measurement point along the ruler at the given unit value.
     */
    public void setMark(double d) {
        //System.err.print("mark request:" + f);
        if(d < min)
            d = min;
        else if(d > max)
            d = max;
        mark = d;
        pixelMark = ((d - min) / unitsPerPixel) + getInsets().left;
        //System.err.println("mark set:" + pixelMark + " out of " + getSize().width);
        int h = getSize().height;
        int half = h / 2; //Guard against even/odd rounding stuff
        markRectangle.setBounds((int)Math.round(pixelMark) - half, 0,
                                half * 2 + 1, h);
    }

    /**
     * A measurement point along the ruler.
     */
    public double getMark() {
        return mark;
    }

    /**
     * Set pixel length of ruler.
     */
    public void setLength(int i) {
        Dimension dim = new Dimension(i, getPreferredSize().height);
        setPreferredSize(dim);
        setSize(dim);
        invalidate();
    }

    public void validate() {
        super.validate();
        computeParameters();
    }

    public int toPixel(double measurement) {
        return(int)(unitsPerPixel / measurement) + getInsets().left;
    }

    public double valueOf(int pixel) {
        return unitsPerPixel * (pixel - getInsets().left);
    }

    public void updateUI() {
        super.updateUI();
        glassColor = new Color(getBackground().getRed() + 15,
                               getBackground().getGreen() + 15,
                               getBackground().getBlue() + 15);
    }
}
