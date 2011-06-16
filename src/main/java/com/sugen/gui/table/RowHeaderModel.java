package com.sugen.gui.table;

/**
 * A model used by RowHeaders to appropriately label and rearrange table rows. 
 * @author Jonathan Bingham
 */ 
public interface RowHeaderModel
{	
	public String getRowName(int row);
	
	/**
	 * Optional; used if RowHeader.isReorderingAllowed().
	 */
	public void swapRows(int a, int b);
}  

