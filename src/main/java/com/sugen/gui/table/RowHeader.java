package com.sugen.gui.table;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.MouseEvent;

import javax.swing.CellRendererPane;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JTable;
import javax.swing.LookAndFeel;
import javax.swing.UIManager;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.event.MouseInputListener;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableCellRenderer;

/** 
 * @author Jonathan Bingham
 */ 
public class RowHeader extends JComponent
{
	/** @serial */
    protected JTable table;
	/** @serial */
	protected CellRendererPane rendererPane;	
	/** @serial */
	protected TableCellRenderer renderer;	
	/** @serial */
	protected RowHeaderModel model;
	/** @serial */
	boolean isReorderingAllowed = true;
	
	/** @serial */
	protected int draggedRow = -1;	
	/** @serial */
	protected int draggedDistance = 0;
	
	public RowHeader(JTable table, RowHeaderModel model)
	{
		setOpaque(true);
		setLayout(new BorderLayout());
		
		renderer = createDefaultHeaderRenderer();
				rendererPane = new CellRendererPane();
        add(rendererPane);
		this.table = table;		
		this.model = model;		
		updateUI();
	}		
	
	//get/set table, renderer and model
	/** @serial */
	protected MouseInputListener mouseInputListener;
    protected class MouseInputHandler implements MouseInputListener 	
	{
        private int startDragY;
		
		public void mouseClicked(MouseEvent e) {}
		
        public void mousePressed(MouseEvent e) 		
		{
			startDragY = e.getY();			
			draggedRow = startDragY / (table.getHeight() / table.getRowCount());
			draggedDistance = 0;

            //Select row			
			Point p = e.getPoint();
			int row = p.y / (table.getHeight() / table.getRowCount());
            if(row != -1) 				
				table.setRowSelectionInterval(row, row);
        }

		public void mouseMoved(MouseEvent e) {}
		
        public void mouseDragged(MouseEvent e) 		
		{
			//Nothing to do			
			if(!isReorderingAllowed)				
				return;			
			if(draggedRow != -1) 		
			{
				int y = e.getY();				
				draggedDistance = y - startDragY;
				int hitRowIndex = y / (table.getHeight() / table.getRowCount());
				//If we're over another row
	            if(hitRowIndex != draggedRow) 		
				{
					model.swapRows(hitRowIndex, draggedRow);
					table.setRowSelectionInterval(hitRowIndex, hitRowIndex);					table.repaint();
	                startDragY = y;			
					draggedDistance = 0;		
					draggedRow = hitRowIndex;
	            }
				repaint();				
				//System.out.println(draggedRow + " " + draggedDistance);
			}
        }

        public void mouseReleased(MouseEvent e) 		
		{
			draggedRow = -1;			
			draggedDistance = 0;			
			startDragY = 0;

            // Repaint to finish cleaning up
            repaint();
            if(table != null)
                table.repaint();
        }

        public void mouseEntered(MouseEvent e) {}

        public void mouseExited(MouseEvent e) {}
    }
	
	protected Rectangle getHeaderRect(int row)
	{		
		int rowHeight = table.getHeight() / table.getRowCount();	
		return new Rectangle(0, rowHeight * row, getWidth(), rowHeight);
	}	
	
    /**
     * Initialize JTableHeader properties, e.g. font, foreground, and background.
     * The font, foreground, and background properties are only set if their
     * current value is either null or a UIResource, other properties are set
     * if the current value is null.
     */
    public void updateUI() 
	{
        LookAndFeel.installColorsAndFont(this, "TableHeader.background",
                                         "TableHeader.foreground", 										 "TableHeader.font");		installListeners();
    }

    /**
     * Attaches listeners to the JTableHeader.
     */
    protected void installListeners()	
	{
        mouseInputListener = new MouseInputHandler();
        addMouseListener(mouseInputListener);
        addMouseMotionListener(mouseInputListener);
    }

    public void paint(Graphics g) 
	{
        Rectangle visibleRect = getVisibleRect();
		g.clearRect(visibleRect.x, visibleRect.y, 
					visibleRect.width, visibleRect.height);
		
		int numRows = table.getRowCount();		
		if(numRows == 0)
			return;
		
	    Dimension size = getSize();
		int rowHeight = table.getHeight() / numRows;
		int firstRow = (int)(visibleRect.y / rowHeight);
		int firstRowY = rowHeight * firstRow;
		int lastRow = firstRow + visibleRect.height / rowHeight + 2;
		if(lastRow > numRows)
			lastRow = numRows;

		Rectangle cellRect = 		
			new Rectangle(0, firstRowY, size.width, rowHeight);
		Rectangle draggedCellRect = null;		
		for(int row = firstRow; row < lastRow; row++)	
		{
            if(row != draggedRow) 			
			{
                paintCell(g, cellRect, row);
			}
			else 			
			{
			    //Draw a well in place of the moving column
			    g.setColor(getParent().getBackground());
			    g.fillRect(cellRect.x, cellRect.y,
			               cellRect.width, cellRect.height);				
			    draggedCellRect = new Rectangle(cellRect);
			}
            cellRect.y += cellRect.height;
        }

		// draw the dragged cell if we are dragging
		if(draggedRow != -1 && draggedCellRect != null)
		{
		    draggedCellRect.y += draggedDistance;
		    paintCell(g, draggedCellRect, draggedRow);
		}
    }

    protected void paintCell(Graphics g, Rectangle cellRect, int row)	
	{     
		Component component = renderer.getTableCellRendererComponent(
                  table, model.getRowName(row),
                  false, false, row, -1);
		rendererPane.add(component);
        rendererPane.paintComponent(g, 
			component, this, 			
			cellRect.x, cellRect.y,
			cellRect.width, cellRect.height, 
			true);    
	}

    protected TableCellRenderer createDefaultHeaderRenderer() 
	{
		DefaultTableCellRenderer label = new DefaultTableCellRenderer() 
		{
		    public Component getTableCellRendererComponent(JTable table, 
				Object value, boolean isSelected, boolean hasFocus, 
				int row, int column) 
			{
		        if (table != null) 
				{
	                setForeground(getForeground());
	                setBackground(getBackground());
	                setFont(getFont());
		        }
		        setText((value == null) ? "" : value.toString());
				setBorder(new CompoundBorder(
					UIManager.getBorder("TableHeader.cellBorder"),
					new EmptyBorder(0, 4, 0, 4)));
				return this;
			}
		};
		label.setHorizontalAlignment(JLabel.CENTER);
		return label;
    }
	
    /**
     * Return the minimum size of the header. The minimum width is the sum 
     * of the minimum widths of each column (plus inter-cell spacing).
     */
    public Dimension getMinimumSize() 
	{
		int width = 0;
		int height = table.getHeight();
		for(int row = 0; row < table.getRowCount(); row++)
		{
			Component c = renderer.getTableCellRendererComponent(table,
				model.getRowName(row), false, false, row, -1);
			width = Math.max(width, c.getMinimumSize().width);
		}
        return new Dimension(width, height);
    }

    /**
     * Return the preferred size of the header. The preferred width is the 
     * maximum of the preferred widths of all of the components provided 
     * by the header renderers. 
     */
    public Dimension getPreferredSize() 
	{
		//System.out.println(model + "\n" + table);
		int width = 0;
		int height = table.getHeight();
		for(int row = 0; row < table.getRowCount(); row++)
		{
			Component c = renderer.getTableCellRendererComponent(table,
				model.getRowName(row), false, false, row, -1);
			width = Math.max(width, c.getPreferredSize().width);
		}
        return new Dimension(width, height);
    }

    /**
     * Return the maximum size of the header. The maximum width is the sum 
     * of the maximum widths of each column (plus inter-cell spacing).
     */
    public Dimension getMaximumSize()
	{
		int width = Integer.MAX_VALUE;
		int height = table.getHeight();
		for(int row = 0; row < table.getRowCount(); row++)
		{
			Component c = renderer.getTableCellRendererComponent(table,
				model.getRowName(row), false, false, row, -1);
			width = Math.min(width, c.getMaximumSize().width);
		}
        return new Dimension(width, height);
    }	
	
	public boolean isReorderingAllowed()	
	{
		return isReorderingAllowed;
	}	
	
	public void setReorderingAllowed(boolean b)	
	{
		isReorderingAllowed = b;	
	}
}  

