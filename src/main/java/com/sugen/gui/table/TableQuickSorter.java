package com.sugen.gui.table;

/*
 * @(#)TableSorter.java	1.5 97/12/17
 *
 * Copyright (c) 1997 Sun Microsystems, Inc. All Rights Reserved.
 *
 * This software is the confidential and proprietary information of Sun
 * Microsystems, Inc. ("Confidential Information").  You shall not
 * disclose such Confidential Information and shall use it only in
 * accordance with the terms of the license agreement you entered into
 * with Sun.
 *
 * SUN MAKES NO REPRESENTATIONS OR WARRANTIES ABOUT THE SUITABILITY OF THE
 * SOFTWARE, EITHER EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR
 * PURPOSE, OR NON-INFRINGEMENT. SUN SHALL NOT BE LIABLE FOR ANY DAMAGES
 * SUFFERED BY LICENSEE AS A RESULT OF USING, MODIFYING OR DISTRIBUTING
 * THIS SOFTWARE OR ITS DERIVATIVES.
 *
 */

/**
* An abstract sorter for JTables which uses quicksort.<P>
* 
* To use: extend and implement the methods form AbstractTableModel. You
* will typically want to retain a reference to the sorter, so that you
* can call its methods later.<P>
*
* While being based on Sun's original demo version, this table sorter <b>modifies* the underlying data</b>. Viz, sorting alters the underlying data
* order rather than maintaining a mapping, as in the original version. This 
* allows the use of quickSort. To make the class concrete, you <b>must</b>
* also override one additional method: 
* <pre>public void swap(int row1, int row2)</pre>** @author Jonathan Bingham
 * 
 * @author Jonathan Bingham
*/

import java.awt.event.InputEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.Date;
import java.util.Vector;

import javax.swing.JTable;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableColumnModel;


public abstract class TableQuickSorter extends AbstractTableModel
{
	/** 
	 * Supports comparisons on multiple columns, though the current implementation
	 * only ever uses a single column.
	 * @serial 
	 */
	protected Vector sortingColumns = new Vector();
	/** @serial */
	protected JTable table;	
	/**
	 * Sort in ascending order?
	 * @serial 
	 */
    protected boolean ascending = true;
	
	public TableQuickSorter(JTable table)
	{
		super();		
		setTable(table);	
	}
	
	public TableQuickSorter()
	{		
		super();
	}
	
	public void setTable(JTable table)
	{		
		this.table = table;		
		addMouseListenerToHeader();
	}	
	
	public JTable getTable()	
	{		
		return table;	
	}
	
	public int compareRowsByColumn(int row1, int row2, int column)
    {
        Class type = getColumnClass(column);

		// Check for nulls
        Object o1 = getValueAt(row1, column);
        Object o2 = getValueAt(row2, column); 

        // If both values are null return 0
        if(o1 == null && o2 == null)
            return 0; 
        else if(o1 == null) // Define null less than everything. 
            return -1; 
        else if(o2 == null)
            return 1; 
		
		//Allow for subclasses of Number
        if(o1 instanceof java.lang.Number)
        {
            double d1 = ((Number)o1).doubleValue();
            double d2 = ((Number)o2).doubleValue();

            if(d1 < d2)
                return -1;
            else if(d1 > d2)
                return 1;
            else
                return 0;
        }
        else if(type == java.util.Date.class)
        {
            long n1 = ((Date)o1).getTime();
            long n2 = ((Date)o2).getTime();

            if(n1 < n2)
                return -1;
            else if(n1 > n2)
                return 1;
            else return 0;
        }
        else if(type == String.class)
        {
            int result = ((String)o1).compareTo((String)o2);

            if(result < 0)
                return -1;
            else if(result > 0)
                return 1;
            else return 0;
        }
        else if(type == Boolean.class)
        {
            boolean b1 = ((Boolean)o1).booleanValue();
            boolean b2 = ((Boolean)o2).booleanValue();
			
			// Define false < true
            if(b1 == b2)
                return 0;
            else if(b1) 
                return 1;
            else
                return -1;
        }
        else
        {
            String s1 = o1.toString();
            String s2 = o2.toString();
            int result = s1.compareTo(s2);

            if(result < 0)
                return -1;
            else if(result > 0)
                return 1;
            else return 0;
        }
    }
	
	/**
	 * Compare row1 to row2 using the sortingColumns.
	 */
    public int compare(int row1, int row2)
    {
        for(int level = 0; level < sortingColumns.size(); level++)
        {
            Integer column = (Integer)sortingColumns.elementAt(level);
            int result = compareRowsByColumn(row1, row2, column.intValue());
            if(result != 0)
                return ascending ? result : -result;
        }
        return 0;
    }

   /**
    * QuickSort the vector referenced by this TableSorter.
    */
	public synchronized void sort() 
	{
		// Empty vectors are already sorted
		if(table.getRowCount() > 0)
    		quickSort(0, (table.getRowCount() - 1));
	}
  
	/**
	 *  Do not call this directly! (it's unsynchronized, to allow recursion)
	 */
	protected void quickSort(int first, int last)
	{
		if(first < last) 
		{
			int low = first; 
			int high = last; 
			int mid = (high + low)/2;

			while(low <= high) 
			{
				while(compare(mid, low) > 0) 
				    low++;
				while(compare(mid, high) < 0)
				    high--;
				if(low <= high) 
				{
					swap(low, high);
					low = low + 1;
					high = high - 1; 
				}
			}
			if(first < high) 
				quickSort(first, high);
			if(low < last)
				quickSort(low, last);
		}
	}

	abstract public void swap(int row1, int row2);

    public void sortByColumn(int column) 
	{
        sortByColumn(column, true);
    }

    public void sortByColumn(int column, boolean ascending) 
	{
        this.ascending = ascending;
        sortingColumns.removeAllElements();
        sortingColumns.addElement(new Integer(column));
        sort();
    }

    // There is nowhere else to put this. 
    // Add a mouse listener to the Table to trigger a table sort 
    // when a column heading is clicked in the JTable. 
    protected void addMouseListenerToHeader() 
	{ 
        final TableQuickSorter sorter = this; 
        final JTable tableView = table; 
        tableView.setColumnSelectionAllowed(false); 
        MouseAdapter listMouseListener = new MouseAdapter() 
		{
            public void mouseClicked(MouseEvent e) 
			{
                TableColumnModel columnModel = tableView.getColumnModel();
                int viewColumn = columnModel.getColumnIndexAtX(e.getX()); 
                int column = tableView.convertColumnIndexToModel(viewColumn); 
                if(e.getClickCount() == 1 && column != -1) 
				{
                    //System.out.println("Sorting ..."); 
                    int shiftPressed = e.getModifiers()&InputEvent.SHIFT_MASK; 
                    boolean ascending = (shiftPressed == 0); 
                    sorter.sortByColumn(column, ascending); 
                }
             }
         };
        JTableHeader th = tableView.getTableHeader(); 
        th.addMouseListener(listMouseListener); 
    }
}
