package com.sugen.gui.plot;

import java.util.*;

import java.awt.*;
import java.awt.event.*;
import java.awt.geom.*;
import javax.swing.*;
import javax.swing.border.*;
import javax.swing.event.*;

import com.sugen.event.*;
import com.sugen.gui.*;
import com.sugen.util.*;

/**
 * View a 2 dimensional Plot.
 *
 * @author Jonathan Bingham
 */
public class PlotView extends JComponent {
	private static final long serialVersionUID = 1L;
	public final static String PROPERTY_FOCUS_KEY = "focus_key";
    public final static String PROPERTY_DATA_MODEL = "data_model";

    protected Plot plot;

    /**
     * Always linearly transform the plot to fit it onto the screen.
     * @serial
     */
    protected LinearTransform linearTransform;

    //Support for mouse controls
    protected RotationUI rotationalUI;
    protected MouseHandler mouseHandler;

    /** Index of focused object. */
    protected int focus = -1;

    /** Radius of rendered points. */
    protected int radius = 2;

    //Renderer
    protected CellRendererPane rendererPane = new CellRendererPane();
    protected LabelRenderer renderer = new DefaultLabelRenderer();
    protected Dimension maxLabelSize = new Dimension(0, 0);

    /** When adjusting, render more roughly. */
    protected boolean isAdjusting;
    protected boolean isQuickPaint;

    /**
     * Hide or show labels.
     * @serial
     */
    protected boolean areLabelsVisible;
    private boolean hasLeftLabels;

    /**
     * Buffer to an offscreen image. Not done for the double buffering,
     * but to avoid recomputing the entire plot unnecessarily.
     */
    transient protected Image offscreenImage;
    
    /**
     * If buffered, the offscreen image will be used
     */
    protected boolean isBuffered = false;

    /**
     * If current, simply repaint from current offscreen image buffer.
     * If not, update the offscreen buffer.
     */
    transient protected boolean isImageCurrent;
    transient boolean isPrinting = false;

    /**
     * Color-code for plot objects.
     * @serial
     */
    protected Map colors = new HashMap();

    /** Cache locations for fast mouse-over response. */
    transient protected Map labelRectCache = new HashMap();

    /** @serial */
    protected SelectionModel selectionModel;

    public PlotView() {
        this(new Plot());
    }

    public PlotView(Plot plot) {
        setBorder(new EmptyBorder(10, 10, 10, 10));
        setBackground(Color.white);
        setOpaque(false);

        setLayout(new BorderLayout());
        add(rendererPane);

        mouseHandler = new MouseHandler();
        addMouseListener(mouseHandler);
        addMouseMotionListener(mouseHandler);

        setSelectionModel(new SelectionModel());

        //Scale tree to fit current size
        addComponentListener(new ComponentAdapter() {
            public void componentResized(ComponentEvent e) {
                resized();
            }
        });

        //Allow rotation of plots by shift dragging
        rotationalUI = new RotationUI(this);
        setPlot(plot);
        resized();
    }

    public Plot getPlot() {
        return plot;
    }

    /**
     * Be careful about bypassing setDataModel.
     */
    public void setPlot(Plot plot) {
        //make sure no duplicate listeners are added
        removeMouseListener(rotationalUI);
        removeMouseMotionListener(rotationalUI);

        this.plot = plot;
        rotationalUI.setPlot(plot);
        setLinearTransform();

        //Allow rotation if appropriate
        if(plot != null && plot.isRotatable()) {
            addMouseListener(rotationalUI);
            addMouseMotionListener(rotationalUI);
        }
        //System.err.println("PlotView.setPlot() " + plot);
    }

    protected void resized() {
    	if(isBuffered)
    		offscreenImage = createImage(getWidth(), getHeight());
        refresh(false);
    }

    /**
     * For overriding such as by hyperbolic view.
     */
    protected void setLinearTransform() {
        linearTransform = new LinearTransform();
        linearTransform.setScaleProportional(true);
        plot.addListener(linearTransform);
        linearTransform.setPlot(plot);
        linearTransform.addListener(plotListener);
    }

    protected LinearTransform getLinearTransform() {
        return linearTransform;
    }

    public void setFocus(int key) {
        if(plot == null || key == focus)
            return;

        //Revert old node
        int oldFocus = focus;
        if(focus >= 0) {
            Object obj = plot.getDataModel().get(focus);
            boolean isSelected = selectionModel.contains(obj);
            Component comp =
                renderer.getRendererComponent(this,
                                              plot.getDataModel().get(focus),
                                              focus,
                                              isSelected, false, isAdjusting,
                                              areLabelsVisible, null);
            Rectangle rect = cellRectangle(focus, comp, isSelected, false);
            labelRectCache.put(obj, rect);
        }
        focus = key;
        //Store new node rect - maybe larger than unfocused rect
        if(focus >= 0) {
            Object obj = plot.getDataModel().get(focus);
            boolean isSelected = selectionModel.contains(obj);
            Component comp =
                renderer.getRendererComponent(this,
                                              plot.getDataModel().get(focus),
                                              focus,
                                              isSelected, true, isAdjusting,
                                              areLabelsVisible, null);
            Rectangle rect = cellRectangle(focus, comp, isSelected, true);
            labelRectCache.put(obj, rect);
        }
        repaint();
        firePropertyChange(PROPERTY_FOCUS_KEY, oldFocus, focus);
    }

    public int getFocus() {
        return focus;
    }

    protected class MouseHandler
        extends MouseInputAdapter {
        public void mouseClicked(MouseEvent e) {
            if(e.isConsumed())
                return;

            //If no meta-characters, clear selection
            if(!e.isControlDown() && !e.isShiftDown() && !e.isAltDown() &&
                e.getButton() == MouseEvent.BUTTON1)
                selectionModel.clear();

            //Something's selected/deselected
            if(focus >= 0) {
                Object dataObj = plot.getDataModel().get(focus);
                //Deselect
                boolean isSelected =
                    selectionModel.contains(dataObj);
                if(e.isControlDown() && isSelected)
                    selectionModel.remove(dataObj);
                //Select
                else if(!isSelected)
                    selectionModel.add(dataObj);
            }
        }

        public void mouseMoved(MouseEvent e) {
            try {
                mousePoint = e.getPoint();
                int hover = labelAt(mousePoint);
                if(hover != focus) {
                    setToolTipText(null);
                    setFocus(hover);
                }
                if (hover < 0)
                	setToolTipText(null);
            }
            catch(NullPointerException npe) {}
        }

        public void mousePressed(MouseEvent e) {
            setAdjusting(true);
        }

        public void mouseReleased(MouseEvent e) {
            setAdjusting(false);
        }

        public void mouseExited(MouseEvent e) {
            setFocus( -1);
        }
    }

    /**
     * So we can test if mouse has moved since last check.
     */
    transient private Point mousePoint;

    /**
     * Index of label at the specified screen location, or -1 if none.
     */
    protected int labelAt(final Point p) {
        int retval = -1;

        //First check current focus - since the focus rectangle
        //and the non-focused rectangle may not exactly overlap
        if(focus >= 0) {
            Point2D focusPoint = linearTransform.get(focus, null);
            if(focusPoint != null && focusPoint.distance(p) < radius * 2)
                return focus;
        }

        //Iterate through label rectangles
        //If mouse has moved, abort
        Iterator iter = labelRectCache.keySet().iterator();
        while(p.equals(mousePoint) && iter.hasNext() && retval == -1) {
            Object key = iter.next();
            Rectangle rect = (Rectangle)labelRectCache.get(key);
            //Select the node if user clicked within its bounds
            if(rect != null && rect.contains(p))
                retval = plot.dataModel.indexOf(key);
        }
        return retval;
    }

    public void refresh(boolean hasChanged) {
        isImageCurrent = false;
        if(plot != null && !plot.isEmpty()) {
            if(hasChanged)
                computeMaxLabelSize();
            autoSize();
        }
        repaint();
    }

    /**
     * Sets the preferredSize of the view based on the current plot.
     * Size includes insets. The plot is then mapped onto the view,
     * respecting its insets.
     */
    public void autoSize() {
        Insets insets = getInsets();
        int leftLabel = (hasLeftLabels() ? maxLabelSize.width : 0);
        Rectangle2D rect = new Rectangle2D.Double(
            insets.left + leftLabel,
            insets.top + maxLabelSize.height / 2,
            getWidth() - insets.left - insets.right - (maxLabelSize.width + leftLabel),
            getHeight() - insets.top - insets.bottom - maxLabelSize.height);
        linearTransform.mapTo(rect);
    }

    /**
     * Display size depends on the size of label components. There has to be
     * enough space to fit the full label.
     */
    protected void computeMaxLabelSize() {
        try {
            maxLabelSize = new Dimension(0, 0);

            Rectangle rect;
            for(int index = 0, max = plot.size(); index < max; ++index) {
                Component label = 
                    renderer.getRendererComponent(this,
                                                  plot.getDataModel().get(index),
                                                  index,
                                                  false, false, false, true, null);
                rect = cellRectangle(index, label, false, false);
                if(rect == null)
                    continue;

                //Check if bigger than previous biggest
                if(rect.width > maxLabelSize.width)
                    maxLabelSize.width = rect.width;
                if(rect.height > maxLabelSize.height)
                    maxLabelSize.height = rect.height;
            }
        }
        catch(NullPointerException e) {}
    }

    //To avoid memory reallocation in calls to cellRectangle
    /** @serial */
    private Point2D cellRectTmp = new Point2D.Double();

    protected Rectangle cellRectangle(int key, Component label,
                                      boolean isSelected, boolean hasFocus) {
        if(label == null)
            return null;

        Point2D p = linearTransform.get(key, cellRectTmp);
        Rectangle retval = new Rectangle();

        Dimension prefSize = label.getPreferredSize();
        retval.width = prefSize.width;
        retval.height = prefSize.height;
        retval.x = (int)p.getX();

        //Invisible labels should be centered on the node
        String s = String.valueOf(plot.getDataModel().get(key));
        if((!areLabelsVisible && !isSelected && !hasFocus) ||
           s == null || "".equals(s) || "null".equals(s))
            retval.x -= prefSize.width / 2;

        //Some labels go to the left of the node
        else if(isLabelLeft(key, p)) {
            retval.x -= prefSize.width;
            retval.x -= radius;
        }
        else
            retval.x += radius;

        //Vertically center on point
        retval.y = (int)p.getY() - prefSize.height / 2;

        return retval;
    }

    /**
     * Decide whether the label goes to the left of the point rather than the
     * default right. Provided for subclasses. By default, always returns
     * false.
     */
    protected boolean isLabelLeft(int key, Point2D p) {
        return false;
    }

    protected void paintPlot(Graphics2D g) {
        g.setColor(Palette.darkSlateGray);

        //Paint each point
        labelRectCache.clear();
        for(int index = 0, size = plot.size(); index < size; index++) {
            Point2D tmp = linearTransform.get(index, null);
            paintPoint(g, index, tmp);

            //If adjusting, paint labels from the same loop
            //since it doesn't matter what goes on top of what
            if(isAdjusting && isQuickPaint && areLabelsVisible)
                paintLabel(g, index, false);
        }

        //If not adjusting, use a separate loop for the labels,
        //so we don't draw anything on top of them
        if(!(isAdjusting && isQuickPaint)) {
            for(int i = 0, size = plot.size(); i < size; ++i) {
                paintLabel(g, i, false);
            }
        }
        //Otherwise only paint selections
        else if(!areLabelsVisible && !selectionModel.isEmpty()) {
            for(int i = 0, size = plot.size(); i < size; ++i) {
                if(selectionModel.contains(plot.getDataModel().get(i)))
                    paintLabel(g, i, false);
            }
        }
    }

    protected void paintPoint(Graphics2D g, int key, Point2D p) {
        Color originalColor = g.getColor();
        Color color = getColor(plot.dataModel.get(key));
        if(color != null)
            g.setColor(color);

        g.fillOval((int)Math.round((p.getX() - radius)), 
                   (int)Math.round((p.getY() - radius)), 
        		   radius * 2, radius * 2);

        g.setColor(originalColor);
    }

    protected void paintBackground(Graphics g) {
        g.setColor(getBackground());
        g.fillRect(0, 0, getWidth(), getHeight());
        //Rectangle bounds = getBounds();
        //g.fillRect(bounds.x, bounds.y, bounds.width, bounds.height);
        paintBorder(g);
    }

    /** No need to clear first. */
    public void update(Graphics g) {
    }

    public void paintComponent(Graphics graphics) {
        Graphics2D g = (Graphics2D)graphics;
        if(!(isAdjusting && isQuickPaint) && !isPrinting) {
            ((Graphics2D)g).setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                               RenderingHints.VALUE_ANTIALIAS_ON);
        }

        //Store in an offscreen image so we only compute once
        if(!(isAdjusting && isQuickPaint) && !isPrinting && isBuffered) {
            if(!isImageCurrent) {
                if(offscreenImage == null)
                    resized();

                Graphics2D gOff = (Graphics2D)offscreenImage.getGraphics();
                gOff.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                                      RenderingHints.VALUE_ANTIALIAS_ON);
                paintBackground(gOff);
                if(plot != null && plot.getDataModel() != null)
                    paintPlot(gOff);
                isImageCurrent = true;
            }
        	g.drawImage(offscreenImage, 0, 0, this);

            //Paint focused label on top of everything else
            if(focus >= 0)
                paintLabel(g, focus, true);
        }
        //Render quickly without caching or buffering
        else {
            paintBackground(g);
            if(plot != null && plot.getDataModel() != null)
                paintPlot(g);
            if(focus >= 0)
                paintLabel(g, focus, true);
        }
    }

    protected void paintLabel(Graphics2D g, int key, boolean hasFocus) {
        boolean isSelected =
            selectionModel.contains(plot.getDataModel().get(key));
        boolean isVisible = areLabelsVisible || hasFocus || isSelected;

        //Get renderer component
        Object dataObj = plot.dataModel.get(key);
        Color color = getColor(dataObj);
        Component component = renderer.getRendererComponent(
            this, dataObj, isVisible ? key : -1,
            isSelected, hasFocus, isAdjusting, areLabelsVisible, color);

        //Cache label's bounding rectangle, so mouseovers can quickly search
        Rectangle cellRect = cellRectangle(key, component, isSelected, hasFocus);
        labelRectCache.put(plot.getDataModel().get(key), cellRect);
        if(cellRect == null || !isVisible)
            return;

        rendererPane.paintComponent(g,
                                    component, this,
                                    cellRect.x, cellRect.y,
                                    cellRect.width, cellRect.height,
                                    false);
    }

    public boolean isQuickPaint() {
        return isQuickPaint;
    }

    public void setQuickPaint(boolean b) {
        isQuickPaint = b;
    }

    public SelectionModel getSelectionModel() {
        return selectionModel;
    }

    public void setSelectionModel(SelectionModel model) {
        if(selectionModel != null)
            selectionModel.removeListener(selectionListener);
        selectionModel = model;
        if(selectionModel != null)
            selectionModel.addListener(selectionListener);
    }

    public void setLabelRenderer(LabelRenderer lr) {
        renderer = lr;
    }

    public LabelRenderer getLabelRenderer() {
        return renderer;
    }

    /**
     * Allows the data model to change, with all other Plot settings
     * remaining constant.
     */
    public void setDataModel(DataModel model) {
        if(plot == null || plot.getDataModel() == model)
            return;

        Object old = plot.getDataModel();
        plot.setDataModel(model);
        plot.validate();

        //System.out.println("clear selection");
        selectionModel.clear();
        computeMaxLabelSize();
        firePropertyChange(PROPERTY_DATA_MODEL, old, model);
    }

    public DataModel getDataModel() {
        if(plot == null)
            return null;
        return plot.getDataModel();
    }

    /**
     * Refresh when plot changes.
     * @serial
     */
    protected CollectionListener plotListener = new CollectionListener() {
        public void collectionChanged(CollectionEvent e) {
            refresh(false);
        }

        public void collectionAdded(CollectionEvent e) {
            refresh(true);
        }

        public void collectionRemoved(CollectionEvent e) {
            refresh(true);
        }
    };

    /**
     * Repaint when selection changes.
     * @serial
     */
    protected SelectionListener selectionListener = new SelectionListener() {
        public void selectionChanged(CollectionEvent e) {
            refresh(true);
        }

        public void selectionAdded(CollectionEvent e) {
            refresh(true);
        }

        public void selectionRemoved(CollectionEvent e) {
            refresh(true);
        }
    };

    public boolean isAdjusting() {
        return isAdjusting;
    }

    /**
     * If quick paint is enabled, the rendering may be less complete
     * during adjustment.
     */
    public void setAdjusting(boolean b) {
        isAdjusting = b;
        if(!b)
            refresh(false);
    }

    public boolean areLabelsVisible() {
        return areLabelsVisible;
    }

    public void setLabelsVisible(boolean b) {
        areLabelsVisible = b;
        isImageCurrent = false;
        repaint();
    }

    protected Color getColor(Object key) {
        //return null;
        return(Color)colors.get(key);
    }

    protected void setColor(Object key, Color color) {
        colors.put(key, color);
    }

    public Map getColors() {
        return colors;
    }

    public boolean hasLeftLabels() {
        return hasLeftLabels;
    }

    public void setColors(Map map) {
        colors = map;
    }

    public void setLeftLabels(boolean hasLeftLabels) {
        this.hasLeftLabels = hasLeftLabels;
    }

    public int getPointRadius() {
        return radius;
    }

    public void setPointRadius(int i) {
        radius = i;
    }
}
