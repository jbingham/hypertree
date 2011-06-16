package com.sugen.gui.table;

import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Types;
import java.util.ArrayList;
import java.util.List;

import javax.swing.table.AbstractTableModel;

/**
 * Modified from Sun's JDBCAdapter.
 * 
 * @author Jonathan Bingham
 */
public class JdbcTableModel extends AbstractTableModel 
{
	/** @serial */
    protected String[] columnNames = {};
	/** @serial */
    protected List rows = new ArrayList();
	/** @serial */
    protected ResultSetMetaData metaData;

	public JdbcTableModel()
	{
		try
		{
			setResultSet(null, null);
		}
		catch(SQLException e) {}
	}
	
	public void setResultSet(ResultSet results, Statement statement)
		throws SQLException
	{
		if(results == null)
		{
			rows.clear();
			columnNames = new String[]{};
			fireTableChanged(null);
			return;
		}
		
        metaData = results.getMetaData();

        int numberOfColumns =  metaData.getColumnCount();
        columnNames = new String[numberOfColumns];
		
        // Get the column names and cache them.
        // Then we can close the connection.
        for(int column = 0; column < numberOfColumns; column++)
            columnNames[column] = metaData.getColumnLabel(column+1);

        // Get all rows.
        rows.clear();
        while(results.next())
		{
            List newRow = new ArrayList();
            for(int i = 1; i <= getColumnCount(); i++) 				
				newRow.add(results.getObject(i));
            rows.add(newRow);
        }       
		//  close(); Need to copy the metaData, bug in jdbc:odbc driver.
        fireTableChanged(null); // Tell the listeners a new table has arrived.
		
		if(statement != null)
			statement.close();
	}	

    public String getColumnName(int column) 
	{
		return columnNames[column] == null ? "" : columnNames[column];
    }

    public Class getColumnClass(int column) 
	{
        int type;
        try 
		{
            type = metaData.getColumnType(column+1);
        }
		catch(SQLException e) 
		{
            return super.getColumnClass(column);
        }

        switch(type) 
		{
        case Types.CHAR:
        case Types.VARCHAR:
        case Types.LONGVARCHAR:
            return String.class;

        case Types.BIT:
            return Boolean.class;

        case Types.TINYINT:
        case Types.SMALLINT:
        case Types.INTEGER:
            return Integer.class;

        case Types.BIGINT:
            return Long.class;

        case Types.FLOAT:
        case Types.DOUBLE:
            return Double.class;

        case Types.DATE:
            return java.sql.Date.class;

        default:
            return Object.class;
        }
    }

    public boolean isCellEditable(int row, int column) 
	{
        try 
		{
            return metaData.isWritable(column+1);
        }
        catch (SQLException e) 
		{
            return false;
        }
    }

    public int getColumnCount() 
	{
        return columnNames.length;
    }

    // Data methods

    public int getRowCount() 
	{
        return rows.size();
    }

    public Object getValueAt(int aRow, int aColumn) 
	{
        List row = (List)rows.get(aRow);
        return row.get(aColumn);
    }

    public String dbRepresentation(int column, Object value) 
	{
        if(value == null)
            return "null";

        int type;
        try 
		{
            type = metaData.getColumnType(column+1);
        }
        catch (SQLException e) 
		{
            return value.toString();
        }

		switch(type) 
		{
        case Types.INTEGER:
        case Types.DOUBLE:
        case Types.FLOAT:
            return value.toString();
        case Types.BIT:
            return ((Boolean)value).booleanValue() ? "1" : "0";
        case Types.DATE:
            return value.toString(); // This will need some conversion.
        default:
            return "\""+value.toString()+"\"";
        }
    }

    public void setValueAt(Object value, int row, int column) 
	{
        try 
		{
            String tableName = metaData.getTableName(column+1);
            // Some of the drivers seem buggy, tableName should not be null.
            if (tableName == null) {
                System.out.println("Table name returned null.");
            }
            String columnName = getColumnName(column);
            String query =
                "update "+tableName+
                " set "+columnName+" = "+dbRepresentation(column, value)+
                " where ";
            // We don't have a model of the schema so we don't know the
            // primary keys or which columns to lock on. To demonstrate
            // that editing is possible, we'll just lock on everything.
            for(int col = 0; col<getColumnCount(); col++) 
			{
                String colName = getColumnName(col);
                if (colName.equals(""))
                    continue;
                if (col != 0)
                    query = query + " and ";
                query = query + colName +" = "+
                    dbRepresentation(col, getValueAt(row, col));
            }
            System.out.println(query);
            System.out.println("Not sending update to database");
            // statement.executeQuery(query);
        }
        catch (SQLException e) 
		{
            //e.printStackTrace();
            System.err.println("Update failed");
        }
        List dataRow = (List)rows.get(row);
        dataRow.set(column, value);
    }
}
