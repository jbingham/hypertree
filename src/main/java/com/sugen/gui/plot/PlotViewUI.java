package com.sugen.gui.plot;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Event;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Frame;
import java.awt.Graphics;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.geom.Point2D;
import java.awt.print.Book;
import java.awt.print.PageFormat;
import java.awt.print.Printable;
import java.awt.print.PrinterException;
import java.awt.print.PrinterJob;
import java.beans.PropertyChangeEvent;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Properties;
import java.util.SortedSet;
import java.util.TreeSet;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JButton;
import javax.swing.JColorChooser;
import javax.swing.JFrame;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.KeyStroke;
import javax.swing.border.Border;
import javax.swing.border.EmptyBorder;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.undo.AbstractUndoableEdit;

import com.sugen.event.CollectionEvent;
import com.sugen.event.SelectionListener;
import com.sugen.gui.AbstractEditor;
import com.sugen.gui.AppBean;
import com.sugen.gui.Closeable;
import com.sugen.gui.Editable;
import com.sugen.gui.Editor;
import com.sugen.gui.FontChooser;
import com.sugen.gui.Icons;
import com.sugen.gui.SwingWorker;
import com.sugen.gui.form.Form;
import com.sugen.gui.form.FormDialog;
import com.sugen.gui.form.FormException;
import com.sugen.gui.io.ColorCodeUI;
import com.sugen.util.DataModel;
import com.sugen.util.TreeDataModel;

/**
 * UI controls for various plot views.
 *
 * @author Jonathan Bingham
 */
@SuppressWarnings({"unchecked", "rawtypes"})
public class PlotViewUI extends JScrollPane
    implements AppBean, SelectionListener, Editable,
        Printable, Closeable, Serializable {
	private static final long serialVersionUID = 1L;
	public final static String PROPERTY_FONT = "label.font";
    public final static String PROPERTY_FONT_SIZE = "label.font.size";
    public final static String PROPERTY_FONT_STYLE = "label.font.style";
    public final static String PROPERTY_LABELS_VISIBLE = "label.visible";

    /** @serial */
    protected Frame owner;
    /** @serial */
    protected JPanel boundingPanel = new JPanel(new BorderLayout());
    /** @serial */
    protected PlotView plotView;
    /** @serial */
    protected PlotUI plotUI;
    /** @serial */
    protected AbstractEditor editor = new AbstractEditor();
    /** @serial */
    protected Properties properties;

    /** @serial */
    protected ColorCodeUI colorUI;

    /**
     * Change in size of plot when zooming in or out.
     * @serial
     */
    protected float zoomIncrement = 1.5f;

    private boolean isZoomX = true;
    private boolean isZoomY = true;

    public PlotViewUI(JFrame owner) {
        this(owner, new PlotView(), new PlotUI());
    }

    public PlotViewUI(JFrame owner, PlotView plotView, PlotUI plotUI) {
        setPlotUI(plotUI);
        setPlotView(plotView);

        this.owner = owner;
        colorUI = new ColorCodeUI();
        colorUI.addPropertyChangeListener(this);

        boundingPanel.setDoubleBuffered(false);
        setViewportView(boundingPanel);

        setCorner(JScrollPane.LOWER_RIGHT_CORNER, new JPanel());
        getHorizontalScrollBar().setUnitIncrement(20);
        getVerticalScrollBar().setUnitIncrement(20);
    }

    /** @serial */
    protected MouseListener popupListener = new PopupListener();
    protected class PopupListener
        extends MouseAdapter {
        public void mouseClicked(MouseEvent e) {
            if(e.isPopupTrigger()
               || e.getModifiers() == MouseEvent.BUTTON3_MASK) {
                if(plotView.getFocus() >= 0) {
                    JPopupMenu popup = getLabelPopup();
                    popup.show(plotView, e.getX(), e.getY());
                }
                else {
                    JPopupMenu popup = getPopup();
                    popup.show(plotView, e.getX(), e.getY());
                }
                e.consume();
                plotView.repaint();
            }
        }
    }

    public PlotUI getPlotUI() {
        return plotUI;
    }

    public void setPlotUI(PlotUI ui) {
        if(plotUI != null)
            plotUI.removePropertyChangeListener(this);
        plotUI = ui;
        if(plotUI != null)
            plotUI.addPropertyChangeListener(this);
    }

    public PlotView getPlotView() {
        return plotView;
    }

    public void setPlotView(PlotView view) {
        boundingPanel.removeAll();

        if(view.getPlot() != null)
            view.getPlot().setTransform(null);

        //Listen to the new plot, not the old
        if(plotView != null) {
            plotView.removeMouseListener(popupListener);
            plotView.removePropertyChangeListener(this);
            plotView.getSelectionModel().removeListener(this);
        }
        if(view != null) {
            view.addMouseListener(popupListener);
            view.addPropertyChangeListener(this);
            view.getSelectionModel().addListener(this);
            boundingPanel.add(view);
        }
        plotView = view;
        plotUI.setPlot(view.getPlot()); // use new plot
        revalidate();
    }

    public void setDataModel(DataModel data) {
        plotView.setDataModel(data);
        updateActions();
    }

    public Editor getEditor() {
        return editor;
    }

    public void setEditor(AbstractEditor editor) {
        if(editor == null)
            throw new IllegalArgumentException("Editor cannot be null");
        this.editor = editor;
    }

    public void selectionAdded(CollectionEvent e) {
        selectionChanged(e);
    }

    public void selectionRemoved(CollectionEvent e) {
        selectionChanged(e);
    }

    public void selectionChanged(CollectionEvent e) {
        updateActions();
        plotView.refresh(false); //Since cached node label rectangles may be invalid
    }

    protected java.util.List getActionList() {
        java.util.List actions = new ArrayList();
        actions.add(copyAction);
        copyAction.putValue(AppBean.KEY_SEPARATOR_AFTER, Boolean.TRUE);
        copyAction.putValue(AppBean.KEY_TOOLBAR_SEPARATOR_AFTER, Boolean.TRUE);

        actions.add(printAction);
        printAction.putValue(AppBean.KEY_TOOLBAR_SEPARATOR_AFTER, Boolean.TRUE);
        actions.add(printPreviewAction);
        printPreviewAction.putValue(AppBean.KEY_SEPARATOR_AFTER, Boolean.TRUE);

        actions.add(colorUI.getImportAction());
        actions.add(colorUI.getExportAction());
        colorUI.getExportAction().putValue(AppBean.KEY_SEPARATOR_AFTER,
                                           Boolean.TRUE);

        actions.add(selectAction);
        actions.add(selectNeighborsAction);
        selectNeighborsAction.putValue(AppBean.KEY_SEPARATOR_AFTER,
                                       Boolean.TRUE);

        actions.add(zoomInAction);
        actions.add(zoomOutAction);
        actions.add(fitToViewAction);
        fitToViewAction.putValue(AppBean.KEY_SEPARATOR_AFTER, Boolean.TRUE);

        actions.add(viewLabelsAction);
        actions.add(quickPaintAction);
        quickPaintAction.putValue(AppBean.KEY_SEPARATOR_AFTER, Boolean.TRUE);

        actions.add(rotateAction);
        actions.add(distanceAction);
        distanceAction.putValue(AppBean.KEY_SEPARATOR_AFTER, Boolean.TRUE);

        actions.add(labelAction);
        actions.add(colorAction);
        actions.add(fontAction);

        return actions;
    }

    public Action[] getActions() {
        java.util.List actions = getActionList();

        Action[] retval = new Action[actions.size()];
        for(int i = 0; i < retval.length; i++)
            retval[i] = (Action)actions.get(i);
        return retval;
    }

    protected void updateActions() {
        boolean hasData = plotView.getDataModel() != null
            && plotView.getDataModel().size() != 0;

        if(colorUI != null) {
            colorUI.getImportAction().setEnabled(hasData);
            colorUI.getExportAction().setEnabled(hasData);
        }

        printAction.setEnabled(hasData);
        printPreviewAction.setEnabled(hasData);
        fontAction.setEnabled(hasData);
        zoomInAction.setEnabled(hasData);
        zoomOutAction.setEnabled(hasData);
        fitToViewAction.setEnabled(hasData);
        selectAction.setEnabled(hasData);

        viewLabelsAction.putValue(KEY_SELECTED_STATE,
                                  plotView.areLabelsVisible() ? Boolean.TRUE :
                                  Boolean.FALSE);
        viewLabelsAction.setEnabled(hasData);
        quickPaintAction.putValue(KEY_SELECTED_STATE,
                                  plotView.isQuickPaint() ? Boolean.TRUE :
                                  Boolean.FALSE);
        quickPaintAction.setEnabled(hasData);

        rotateAction.setEnabled(hasData && plotView.getPlot().isRotatable());

        boolean hasSelection = !plotView.getSelectionModel().isEmpty();

        copyAction.setEnabled(hasData && hasSelection);
        distanceAction.setEnabled(hasData &&
                                  plotView.getSelectionModel().size() == 2);
        selectNeighborsAction.setEnabled(hasData && hasSelection);
        labelAction.setEnabled(hasData && hasSelection);
        colorAction.setEnabled(hasData && hasSelection);
    }

    /**
     * Each incremental zoom in or out changes the width by this much.
     */
    public void setZoomIncrement(float f) {
        zoomIncrement = f;
    }

    /**
     * Each incremental zoom in or out changes the width by this much.
     */
    public float getZoomIncrement() {
        return zoomIncrement;
    }

    public void propertyChange(PropertyChangeEvent e) {
        String name = e.getPropertyName();
        if(AppBean.PROPERTY_OWNER.equals(name))
            owner = (Frame)e.getNewValue();
        else if(PlotView.PROPERTY_FOCUS_KEY.equals(name)) {
            int key = ((Integer)e.getNewValue()).intValue();
            if(key < 0)
                firePropertyChange(PROPERTY_STATUS_MESSAGE, null, "Ready");
            else
                firePropertyChange(PROPERTY_STATUS_MESSAGE, null,
                                   plotView.getDataModel().get(key).toString());
        }
        else if(PlotView.PROPERTY_DATA_MODEL.equals(name)) {
            if(e.getOldValue() != e.getNewValue()) {
                editor.discardAllEdits();
                colorUI.clear();
            }
        }
        else if(PROPERTY_DATA.equals(name) && e.getSource() != colorUI) {
            //As fired by, eg, PlotReaderWriterUI
            if(e.getNewValue() == null) {
                setDataModel(null);
                plotView.getPlot().clear();
                plotView.refresh(true);
            }
            else if(e.getNewValue() instanceof Plot) {
                Plot plot = (Plot)e.getNewValue();
                setDataModel(plot.getDataModel());
                plotView.getPlot().clear();
                plotView.getPlot().addAll(plot);
                plotView.refresh(true);
            }
            //As fired by, eg, TreeReaderWriterUI
            else if(e.getNewValue() instanceof DataModel) {
//                System.err.println("PlotViewUI.propertyChange(): " + e.getNewValue());
                DataModel model = (DataModel)e.getNewValue();
                if (model instanceof TreeDataModel)
                	setColors(((TreeDataModel)model).getColors());
                setDataModel(model);
            }
        }
        else if(PROPERTY_PROPERTIES.equals(name)) {
            setProperties((Properties)e.getNewValue());
        }

        //handle events from ColorCodeUI
        if(colorUI == e.getSource() &&
              PROPERTY_DATA.equals(name) &&
              e.getNewValue() != null) {
            editor.addEdit(new UndoableColorCode(true));
        }
        //forward events to ColorCodeUI
        else if(colorUI != e.getSource())
            colorUI.propertyChange(e);
    }

    protected Action printAction = new AbstractAction("Print...",
        Icons.get("print24.gif")) {
		private static final long serialVersionUID = 1L;
        {
            putValue(KEY_MENU, FILE_MENU);
            putValue(KEY_ACCELERATOR,
                     KeyStroke.getKeyStroke(KeyEvent.VK_P, Event.CTRL_MASK));
        }

        public void actionPerformed(ActionEvent e) {
        	new SwingWorker() {
        		public Object construct() {
        			printPage();
        			return null;
        		}
        	}.start();         
        }
    };

    public Action getPrintAction() {
        return printAction;
    }

    protected Action printPreviewAction = new AbstractAction("Print Preview") {
		private static final long serialVersionUID = 1L;
        {
            putValue(KEY_MENU, FILE_MENU);
            putValue(KEY_LOCATION, VALUE_MENU_ONLY);
        }

        public void actionPerformed(ActionEvent e) {
        	new SwingWorker() {
        		public Object construct() {
		            print(true, null, null);
        			return null;
        		}
        	}.start();         
        }
    };

    public Action getPrintPreviewAction() {
        return printPreviewAction;
    }

    protected Action fontAction = new AbstractAction("Font...",
        Icons.get("font24.gif")) {
		private static final long serialVersionUID = 1L;
        {
            putValue(KEY_MENU, EDIT_MENU);
            putValue(KEY_LOCATION, VALUE_MENU_ONLY);
        }

        public void actionPerformed(ActionEvent e) {
        	new SwingWorker() {
        		public Object construct() {
            		changeFont();
					return null;
				}
			}.start();         
        }
    };

    public Action getFontAction() {
        return fontAction;
    }

    protected Action distanceAction = new AbstractAction("Distance...",
        Icons.get("emptyIcon24.gif")) {
		private static final long serialVersionUID = 1L;
        {
            putValue(KEY_MENU, EDIT_MENU);
            putValue(KEY_LOCATION, VALUE_MENU_ONLY);
        }

        public void actionPerformed(ActionEvent e) {
        	new SwingWorker() {
        		public Object construct() {
        			displayDistance();
					return null;
				}
			}.start();         
        }
    };

    public Action getDistanceAction() {
        return distanceAction;
    }

    protected Action zoomInAction = new AbstractAction("Zoom In",
        Icons.get("zoomIn24.gif")) {
		private static final long serialVersionUID = 1L;
        {
            putValue(KEY_MENU, VIEW_MENU);
            putValue(KEY_ACCELERATOR,
                     KeyStroke.getKeyStroke('=', Event.CTRL_MASK));
        }

        public void actionPerformed(ActionEvent e) {
            zoomIn();
        }
    };

    public Action getZoomInAction() {
        return zoomInAction;
    }

    protected Action zoomOutAction = new AbstractAction("Zoom Out",
        Icons.get("zoomOut24.gif")) {
		private static final long serialVersionUID = 1L;
        {
            putValue(KEY_MENU, VIEW_MENU);
            putValue(KEY_ACCELERATOR,
                     KeyStroke.getKeyStroke('-', Event.CTRL_MASK));
        }

        public void actionPerformed(ActionEvent e) {
            zoomOut();
        }
    };

    public Action getZoomOutAction() {
        return zoomOutAction;
    }

    protected Action fitToViewAction = new AbstractAction("Fit to View",
        Icons.get("emptyIcon24.gif")) {
		private static final long serialVersionUID = 1L;
		{
            putValue(KEY_MENU, VIEW_MENU);
            putValue(KEY_LOCATION, VALUE_MENU_ONLY);
        }

        public void actionPerformed(ActionEvent e) {
            fitToView();
        }
    };

    public Action getFitToViewAction() {
        return fitToViewAction;
    }

    protected Action selectAction = new AbstractAction("Find...",
        Icons.get("find24.gif")) {
		private static final long serialVersionUID = 1L;
        {
            putValue(KEY_MENU, EDIT_MENU);
        }

        public void actionPerformed(ActionEvent e) {
        	new SwingWorker() {
        		public Object construct() {
        			select();
					return null;
				}
			}.start();         
        }
    };

    public Action getSelectAction() {
        return selectAction;
    }

    protected Action selectNeighborsAction =
        new AbstractAction("Select Neighbors...", Icons.get("emptyIcon24.gif")) {
		private static final long serialVersionUID = 1L;
        {
            putValue(KEY_MENU, EDIT_MENU);
            putValue(KEY_LOCATION, VALUE_MENU_ONLY);
        }

        public void actionPerformed(ActionEvent e) {
        	new SwingWorker() {
        		public Object construct() {
		            selectNeighbors();
					return null;
				}
			}.start();         
        }
    };

    public Action getSelectNeighborsAction() {
        return selectNeighborsAction;
    }
    
    protected Action copyAction = new AbstractAction(
    		"Copy", Icons.get("copy24.gif")) {
		private static final long serialVersionUID = 1L;
        {
            putValue(KEY_MENU, EDIT_MENU);
            putValue(KEY_ACCELERATOR,
                    KeyStroke.getKeyStroke('C', Event.CTRL_MASK));
        }
    	public void actionPerformed(ActionEvent e) {
	       Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
	       StringBuffer buffer = new StringBuffer();
	       for(Iterator iter = plotView.getSelectionModel().getCollection().iterator(); 
	       		 iter.hasNext(); ) {
	    	   Object o = iter.next();
	    	   if(o != null)
	    		   buffer.append(String.valueOf(o) + "\n");
	       }
	       clipboard.setContents(new StringSelection(buffer.toString()), null);
    	}
    };

    protected Action colorAction = new AbstractAction("Color...",
        Icons.get("color24.gif")) {
		private static final long serialVersionUID = 1L;
        {
            putValue(KEY_MENU, EDIT_MENU);
            //putValue(KEY_LOCATION, VALUE_MENU_ONLY);
        }

        public void actionPerformed(ActionEvent e) {
        	new SwingWorker() {
        		public Object construct() {
        			changeColor();
					return null;
				}
			}.start();         
        }
    };

    public Action getColorAction() {
        return colorAction;
    }

    public ColorCodeUI getColorCodeUI() {
        return colorUI;
    }

    protected Action viewLabelsAction = new AbstractAction("Labels") {
		private static final long serialVersionUID = 1L;
        {
            putValue(KEY_MENU, VIEW_MENU);
            putValue(KEY_LOCATION, VALUE_MENU_ONLY);
            putValue(KEY_ACTION_TYPE, VALUE_TOGGLE_ACTION);
            putValue(KEY_SELECTED_STATE, Boolean.TRUE);
        }

        public void actionPerformed(ActionEvent e) {
            setLabelsVisible(!plotView.areLabelsVisible());
        }
    };

    public void setLabelsVisible(boolean b) {
        plotView.setLabelsVisible(b);
    }

    public Action getViewLabelsAction() {
        return viewLabelsAction;
    }

    protected Action quickPaintAction = new AbstractAction("Quick Draw") {
		private static final long serialVersionUID = 1L;
		{
            putValue(KEY_MENU, VIEW_MENU);
            putValue(KEY_LOCATION, VALUE_MENU_ONLY);
            putValue(KEY_ACTION_TYPE, VALUE_TOGGLE_ACTION);
            putValue(KEY_SELECTED_STATE, Boolean.FALSE);
        }

        public void actionPerformed(ActionEvent e) {
            plotView.setQuickPaint(!plotView.isQuickPaint());
        }
    };

    protected Action labelAction = new AbstractAction("Rename...",
        Icons.get("emptyIcon24.gif")) {
		private static final long serialVersionUID = 1L;
        {
            putValue(KEY_MENU, EDIT_MENU);
            putValue(KEY_LOCATION, VALUE_MENU_ONLY);
        }

        public void actionPerformed(ActionEvent e) {
            Iterator iter = plotView.getSelectionModel().iterator();
            if(iter.hasNext())
                rename(iter.next());
        }
    };

    public Action getLabelAction() {
        return labelAction;
    }

    protected Action rotateAction = new AbstractAction("Rotate...",
        Icons.get("emptyIcon24.gif")) {
		private static final long serialVersionUID = 1L;
        {
            putValue(KEY_MENU, EDIT_MENU);
            putValue(KEY_LOCATION, VALUE_MENU_ONLY);
        }

        public void actionPerformed(ActionEvent e) {
            String rotate = JOptionPane.showInputDialog(owner,
                "Degrees to rotate:",
                "Rotate Plot", JOptionPane.PLAIN_MESSAGE);
            if(rotate == null)
                return;
            int rotation;
            try {
                rotation = Integer.parseInt(rotate);
                plotView.rotationalUI.rotateAboutOrigin(Math.PI * 2 * rotation /
                    360);
            }
            catch(NumberFormatException nte) {
                JOptionPane.showMessageDialog(owner,
                                              "Value must be an integer",
                                              "Error",
                                              JOptionPane.ERROR_MESSAGE);
            }
        }
    };

    public Action getRotateAction() {
        return rotateAction;
    }

    public void fitToView() {
        updateActions();
        Dimension fit = new Dimension(getViewport().getSize());
        plotView.setPreferredSize(fit);
        plotView.setSize(fit);
        revalidate();
        plotView.refresh(false);
    }

    public void select() {
        FormDialog dialog = new FormDialog(owner);
        dialog.setForm(new SelectForm(dialog));
        dialog.showDialog();
    }

    protected class SelectForm extends Form {
		private static final long serialVersionUID = 1L;
        transient private JList list;

        SelectForm(FormDialog dialog) {
            super(dialog);
            setTitle("Select");

            Collection listItems = nonNullLabels();
            list = new JList(listItems.toArray());
            JScrollPane scrollPane = new JScrollPane(list);
            scrollPane.setPreferredSize(new Dimension(200, 300));
            this.add(scrollPane);
        }

        public void applyChanges() throws FormException {
            Object[] selections = list.getSelectedValues();
            if(selections != null) {
                Collection collection = new ArrayList();
                for(int i = 0; i < selections.length; i++)
                    collection.add(selections[i]);
                plotView.getSelectionModel().addAll(collection);
            }
            super.applyChanges();
        }
    }

    /**
     * Show distance between two selected keys.
     */
    protected void displayDistance() {
        Iterator iter = plotView.getSelectionModel().iterator();
        Object from = iter.next();
        Object to = iter.next();
        int fromIndex = plotView.getDataModel().indexOf(from);
        int toIndex = plotView.getDataModel().indexOf(to);
        Point2D fromPoint = (Point2D)plotView.getPlot().get(fromIndex);
        Point2D toPoint = (Point2D)plotView.getPlot().get(toIndex);
        double distance = fromPoint.distance(toPoint);

        java.text.NumberFormat nf = java.text.NumberFormat.getInstance();
        JOptionPane.showMessageDialog(owner,
                                      "Distance equals " + nf.format(distance),
                                      "Distance", JOptionPane.PLAIN_MESSAGE);
    }

    protected SortedSet nonNullLabels() {
        SortedSet nonNull = new TreeSet(new Comparator() {
            //Sort
            public int compare(Object a, Object b) {
                return a.toString().compareTo(b.toString());
            }
        });

        Iterator iter = plotUI.plot.dataModel.iterator();
        while(iter.hasNext()) {
            Object next = iter.next();
            if(next.toString() != null && !"".equals(next.toString()))
                nonNull.add(next);
        }
        return nonNull;
    }

    /**
     * Set color code as read from ColorCodeUI Properties file.
     */
    protected class UndoableColorCode extends AbstractUndoableEdit {
		private static final long serialVersionUID = 1L;
        transient private Map colors = new HashMap();

        UndoableColorCode(boolean propagateColors) {
            SortedSet nonNullLabels = nonNullLabels();

            Enumeration keys = colorUI.keys();
            while(keys.hasMoreElements()) {
                Object key = keys.nextElement();
                if(nonNullLabels.contains(key)) {
                    //HACK: to retrieve the item matching the key
                    Object dataObj = nonNullLabels.tailSet(key).first();
                    colors.put(dataObj, colorUI.getColor(key.toString()));
                }
            }
            toggle();
        }

        private void toggle() {
            Map oldColors = getColors();
            setColors(colors);
            colors = oldColors;
            plotView.refresh(true);
        }

        public void undo() {
            super.undo();
            toggle();
        }

        public void redo() {
            super.redo();
            toggle();
        }
    }

    public void selectNeighbors() {
        //Prompt user for max distance that defines neighbors
        String option = JOptionPane.showInputDialog(owner,
            "Enter maximum distance of neighbors",
            "Select Neighbors",
            JOptionPane.INFORMATION_MESSAGE);
        double maxDistance;
        try {
            maxDistance = Double.parseDouble(option);
            if(maxDistance <= 0)
                throw new NumberFormatException();
        }
        catch(NumberFormatException e) {
            JOptionPane.showMessageDialog(owner, "Invalid distance",
                                          "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        Collection newSelections = new ArrayList();
        Iterator iter = plotView.getSelectionModel().iterator();
        while(iter.hasNext()) {
            Object dataObj = iter.next();
            int index = plotView.getDataModel().indexOf(dataObj);
            Point2D center = (Point2D)plotView.getPlot().get(index);

            //Get all untransformed points
            java.util.List points = (java.util.List)plotView.getPlot().
                getCollection();
            for(int i = 0; i < points.size(); i++) {
                Point2D p = (Point2D)points.get(i);

                //Add any points within the max distance from the center
                if(center.distance(p) <= maxDistance) {
                    Object key = plotView.getDataModel().get(i);
                    newSelections.add(key);
                }
            }
        }
        plotView.getSelectionModel().addAll(newSelections);
    }

    public void changeColor() {
        Color color = JColorChooser.showDialog(owner, "Choose Color", null);
        if(color == null)
            return;

        Collection selections = plotView.getSelectionModel().getCollection();
        editor.addEdit(new UndoableColor(selections, color));
    }

    protected class UndoableColor extends AbstractUndoableEdit {
		private static final long serialVersionUID = 1L;
        transient private Collection selections;
        transient private Map oldColors;
        transient private Color color;

        UndoableColor(Collection selections, Color color) {
            this.selections = new ArrayList(selections);
            this.color = color;
            oldColors = new HashMap(selections.size() * 4 / 3);
            toggle();
        }

        public void undo() {
            super.undo();
            Iterator iter = selections.iterator();
            while(iter.hasNext()) {
                Object node = iter.next();
                setColor(node, (Color)oldColors.get(node));
            }
            oldColors.clear();
            plotView.refresh(true);
        }

        public void redo() {
            super.redo();
            toggle();
        }

        private void toggle() {
            Iterator iter = selections.iterator();
            while(iter.hasNext()) {
                Object key = iter.next();
                oldColors.put(key, plotView.getColor(key));
                setColor(key, color);
            }
            plotView.refresh(true);
        }
    }

    protected Map getColors() {
        return plotView.colors;
    }

    protected void setColors(Map colors) {
        plotView.colors = colors;
        colorUI.setAllColors(colors);
        plotView.refresh(true);
    }

    protected void setColor(Object dataObj, Color color) {
        plotView.setColor(dataObj, color);
        if(dataObj != null) {
            colorUI.setColor(dataObj.toString(), color);
        }
    }

    public void rename(Object dataObj) {
        Object label =
            JOptionPane.showInputDialog(this, "Edit Label", "New Label",
                                        JOptionPane.PLAIN_MESSAGE, null, null,
                                        dataObj.toString());
        if(label == null)
            return;
        editor.addEdit(new UndoableLabel(dataObj, label));
        firePropertyChange(PROPERTY_DATA, null, plotView.getDataModel());
    }

    protected class UndoableLabel extends AbstractUndoableEdit {
		private static final long serialVersionUID = 1L;
        transient private Object dataObj;
        transient private Object oldLabel;

        UndoableLabel(Object dataObj, Object label) {
            this.dataObj = dataObj;
            oldLabel = label;
            toggle();
        }

        protected void toggle() {
            if(dataObj instanceof StringBuffer) {
                StringBuffer sb = (StringBuffer)dataObj;
                Object label = new StringBuffer(dataObj.toString());
                sb.setLength(0);
                sb.append(oldLabel);
                oldLabel = label;
            }
            else if(dataObj instanceof DefaultMutableTreeNode) {
                DefaultMutableTreeNode node = (DefaultMutableTreeNode)dataObj;
                Object label = node.getUserObject();
                node.setUserObject(oldLabel);
                oldLabel = label;
            }
            plotView.refresh(true);
        }

        public void undo() {
            super.undo();
            toggle();
        }

        public void redo() {
            super.redo();
            toggle();
        }
    }

    public int print(Graphics graphics, PageFormat pageFormat, int pageIndex) {
        //RepaintManager repaintMngr = RepaintManager.currentManager(this);
        //repaintMngr.setDoubleBufferingEnabled(false);
        plotView.isPrinting = true;
        try {
            this.print(false, graphics, pageFormat);
        } finally {
            plotView.isPrinting = false;
        }
        //repaintMngr.setDoubleBufferingEnabled(true);
        return Printable.PAGE_EXISTS;
    }

    /**
     * Create a PrinterJob and print the view.
     * TODO: allow multiple pages instead of resizing to fit on one page
     */
    protected void printPage() {
        PrinterJob printJob = PrinterJob.getPrinterJob();
        Book book = new Book();
        PageFormat pageFormat = printJob.defaultPage();
        book.append(this, pageFormat);
        printJob.setPageable(book);
        if (printJob.printDialog()) {
            try {
                printJob.print();
            }
            catch(PrinterException pe) {
                pe.printStackTrace();
                JOptionPane.showMessageDialog(null, pe.getMessage(),
                                              "Failure: Could not print.",
                                              JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    /**
     * Print or preview the page.
     * Automatically resize to fit a single standard page.
     */
    public void print(boolean previewOnly, Graphics graphics,
                      PageFormat pageFormat) {
        //Scale the tree to fit the page
        Dimension oldSize = plotView.getSize();
        if(pageFormat == null) {
            PrinterJob printJob = PrinterJob.getPrinterJob();
            pageFormat = printJob.defaultPage();
        }
        
        // Change the label font size
        Dimension oldLabelSize = plotView.maxLabelSize;
        if(!plotView.areLabelsVisible)
        	plotView.maxLabelSize = new Dimension(0, 0);
        Font oldFont = ((Component)plotView.getLabelRenderer()).getFont();
        float fontSize = (float)(oldFont.getSize2D() *
       		(float)pageFormat.getImageableWidth() / 
       			(float)plotView.getSize().width);
        ((Component)plotView.getLabelRenderer()).setFont(
        		oldFont.deriveFont(fontSize));        

        Border oldBorder = plotView.getBorder();
        plotView.setBorder(new EmptyBorder(
        		(int)pageFormat.getImageableY(), (int)pageFormat.getImageableX(), 
        		(int)pageFormat.getImageableY(), (int)pageFormat.getImageableX()));

        plotView.setPreferredSize(new Dimension(
            (int)pageFormat.getWidth(), (int)pageFormat.getHeight()));
        plotView.setSize(plotView.getPreferredSize());
        
        boundingPanel.remove(plotView);
        boundingPanel.setLayout(new FlowLayout(FlowLayout.CENTER, 0, 0));
        boundingPanel.add(plotView);   
        
        plotView.refresh(true);        
        
        if(previewOnly)
            JOptionPane.showMessageDialog(owner, "Click OK to end preview.",
                                          "Preview", JOptionPane.PLAIN_MESSAGE);
        else
            plotView.print(graphics);

        //Restore size to whatever it was before printing
        plotView.setPreferredSize(oldSize);
        plotView.setSize(oldSize);

        boundingPanel.remove(plotView);
        boundingPanel.setLayout(new BorderLayout());
        boundingPanel.add(plotView);

        ((Component)plotView.getLabelRenderer()).setFont(oldFont);
        plotView.setBorder(oldBorder);
        if(plotView.areLabelsVisible)
        	plotView.maxLabelSize = oldLabelSize;
        plotView.autoSize();
        plotView.refresh(false);
    }

    /**
     * When zooming in or out, zoom the x axis.
     */
    public void setZoomX(boolean b) {
        isZoomX = b;
    }

    /**
     * When zooming in or out, zoom the x axis.
     */
    public boolean isZoomX() {
        return isZoomX;
    }

    /**
     * When zooming in or out, zoom the y axis.
     */
    public void setZoomY(boolean b) {
        isZoomY = b;
    }

    /**
     * When zooming in or out, zoom the y axis.
     */
    public boolean isZoomY() {
        return isZoomY;
    }

    public void zoomIn() {
        zoom(zoomIncrement);
    }

    public void zoomOut() {
        zoom(1 / zoomIncrement);
    }

    protected void zoom(float zoomIncrement) {
        Dimension dim = new Dimension(plotView.getSize());
        Point p = new Point(getViewport().getViewPosition());
        Rectangle rect = getViewport().getViewRect();

        if(isZoomX()) {
            p.x = (int)((p.x + rect.width / 2) * zoomIncrement - rect.width / 2);
            dim.width *= zoomIncrement;
        }
        if(isZoomY()) {
            p.y = (int)((p.y + rect.height / 2) * zoomIncrement - rect.height / 2);
            dim.height *= zoomIncrement;
        }
        plotView.setPreferredSize(dim);
        plotView.setSize(dim);
        plotView.refresh(false);
        revalidate(); //To adjust scrollbar size
        getViewport().setViewPosition(p);
    }

    /** @serial */
    protected FormDialog dialog;
    /** @serial */
    protected FontChooser fontChooser;

    public void changeFont() {
        if(dialog == null)
            dialog = new FormDialog(owner);
        if(fontChooser == null) {
            fontChooser = new FontChooser(dialog, getLabelFont());
            dialog.setForm(fontChooser);
        }
        else
            fontChooser.setCurrentFont(getLabelFont());

        JButton applyButton = new JButton("Apply");
        dialog.setButtons(new JButton[] {applyButton, dialog.okButton,
                          dialog.cancelButton});
        applyButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                //try
                {
                    fontChooser.applyChanges();
                    setLabelFont((Font)fontChooser.getReturnValue());
                }
                //catch(FormException fe) {}
            }
        });
        Font oldFont = getLabelFont();
        Font newFont = (Font)dialog.showDialog();

        //If user clicked apply along the way, make sure it's undoable.
        if(newFont == null && oldFont != getLabelFont())
            newFont = getLabelFont();
        if(newFont != null)
            editor.addEdit(new UndoableFontChange(oldFont, newFont));
    }

    public void setLabelFont(Font font) {
        if(plotView.getLabelRenderer() instanceof Component)
            ((Component)plotView.getLabelRenderer()).setFont(font);
        else
            plotView.setFont(font);
        plotView.refresh(true);
    }

    protected class UndoableFontChange extends AbstractUndoableEdit {
		private static final long serialVersionUID = 1L;
        transient private Font newFont;
        transient private Font oldFont;

        public UndoableFontChange(Font oldFont, Font newFont) {
            this.oldFont = oldFont;
            this.newFont = newFont;
            setLabelFont(newFont);
        }

        public void undo() {
            super.undo();
            setLabelFont(oldFont);
        }

        public void redo() {
            super.redo();
            setLabelFont(newFont);
        }
    }

    public Font getLabelFont() {
        if(plotView.getLabelRenderer() instanceof Component)
            return((Component)plotView.getLabelRenderer()).getFont();
        else
            return plotView.getFont();
    }

    protected JPopupMenu getPopup() {
        JPopupMenu popup = new JPopupMenu();
        popup.add(zoomInAction);
        popup.add(zoomOutAction);
        popup.add(rotateAction);
        popup.addSeparator();

        popup.add(selectAction);
        popup.add(fontAction);
        popup.addSeparator();

        popup.add(fitToViewAction);
        return popup;
    }

    protected JPopupMenu getLabelPopup() {
        JPopupMenu popup = new JPopupMenu();
        popup.add(selectNeighborsAction);
        popup.add(colorAction);
        popup.add(labelAction);
        popup.add(copyAction);
        return popup;
    }

    protected void setProperties(Properties props) {
        properties = props;

        //Set label font
        Font font = getLabelFont();
        String name = props.getProperty(PROPERTY_FONT);
        if(name != null)
            font = new Font(name, font.getStyle(), font.getSize());

        try {
            String style = props.getProperty(PROPERTY_FONT_STYLE);
            if(style != null)
                font.deriveFont(Integer.parseInt(style));
        }
        catch(NumberFormatException e) {
            e.printStackTrace();
        }
        try {
            String size = props.getProperty(PROPERTY_FONT_SIZE);
            if(size != null)
                font = font.deriveFont(Float.parseFloat(size));
        }
        catch(NumberFormatException e) {
            e.printStackTrace();
        }
        setLabelFont(font);

        //Labels visible
        String visible = props.getProperty(PROPERTY_LABELS_VISIBLE);
        if(visible != null)
            setLabelsVisible(Boolean.valueOf(visible).booleanValue());
        updateActions();
    }

    public boolean canClose() {
        return colorUI.canClose();
    }

    public void close() {
        plotView.setDataModel(null);
        updateActions();
        colorUI.close();

        if(properties == null)
            return;

        Font font = getLabelFont();
        properties.put(PROPERTY_FONT, font.getName());
        properties.put(PROPERTY_FONT_STYLE, String.valueOf(font.getStyle()));
        properties.put(PROPERTY_FONT_SIZE, String.valueOf(font.getSize()));
        properties.put(PROPERTY_LABELS_VISIBLE,
                       plotView.areLabelsVisible() ? "true" : "false");
    }
}
