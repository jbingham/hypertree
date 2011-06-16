package com.sugen.util;

import java.util.ResourceBundle;
import java.util.MissingResourceException;
import java.util.StringTokenizer;
import java.util.*;

/**
 * More convenient for the db package. Automatically handles exceptions
 * by returning nulls instead. Also checks for default values in a systematic
 * way.
 * 
 * @author Jonathan Bingham
 */
public class PropertyBundle
{
	public final static String VALUE_DELIMITER = ";";
	public final static String KEY_DELIMITER = ".";
	
	protected ResourceBundle resources;
	
	public PropertyBundle(String name)
	{
		try
		{
			resources = ResourceBundle.getBundle(name);
		}
		catch(MissingResourceException e)
		{
			System.err.println("Unable to load ResourceBundle " 
							   + name + ".properties");
		}
	}
	
	public PropertyBundle(ResourceBundle bundle)
	{
		resources = bundle;
	}

	/**
	 * Get a string from the resource bundle.
	 * @return null if the value is null or the resource is missing
	 */
	public String getString(String key)
	{
		return getDefault(key);
		/*
		try
		{
			return resources.getString(key);
		}
		catch(MissingResourceException e) {}
		return null;
		*/
	}
	
	/**
	 * Get a string from the resource bundle.
	 * If the resource is missing, check for defaults. 
	 * E.g., if the key "MyDB.MyTable.id" is missing, getString will check
	 * for "MyDB.id" and then for "id".
	 */
	public String getDefault(String key)
	{
		do
		{
			try
			{
				return resources.getString(key);
			}
			catch(MissingResourceException e) {}
			key = removePenultimateToken(key);
			//System.err.println("getDefault " + key);
		} 
		while(key != null);
		return null;
	}
	
	/**
	 * Return the key with the second to last token removed, or null if there
	 * was only one token. Tokens are delimited by KEY_DELIMITER.
	 */
	protected String removePenultimateToken(String key)
	{
		StringTokenizer tokenizer = new StringTokenizer(key, KEY_DELIMITER);
		int numTokens = tokenizer.countTokens();
		if(numTokens < 2)
			return null;
		
		StringBuffer retval = new StringBuffer();
		int i = 1;
		while(tokenizer.hasMoreTokens())
		{
			String token = tokenizer.nextToken();
			if(i != numTokens - 1)
				retval.append(token);
			if(i < numTokens - 1)
				retval.append(KEY_DELIMITER);
			++i;
		}
		return retval.toString();
	}
	
	/**
	 * Get a semicolon delimited list and parse it.
	 */
	public String[] getStringArray(String key)
	{
		return getStringArray(key, null, null);
	}
	
	/**
	 * Get a semicolon delimited list and parse it, limiting the results.
	 * Limit the list from getStringArray(String) to those values which
	 * pass a certain limiting criterion.
	 * <p>
	 * Much simpler than it sounds. Consider a properties file containing
	 * the following:
	 * <pre>
	 * db.table.list=MyDB.table1;MyDB.table2;MyDB.table3
	 * 
	 * MyDB.table1.myLimiter=someValue
	 * MyDB.table2.myLimiter=someValue
	 * MyDB.table3.myLimiter=someDifferentValue
	 * </pre>
	 * If you call <pre>getStringArray("db.table.list", "myLimiter", "someValue")</pre>
	 * you'll get back an array containing {"MyDB.table1", "MyDB.table2"}.
	 * A simple call to </pre>getStringArray("db.table.list")</pre> will return 
	 * all three tables. 
	 * 
	 * @param value a String or Class
	 */
	public String[] getStringArray(String key, String limiter, String value)
	{
		String listString = null;
		try
		{
			listString = resources.getString(key);
		}
		catch(MissingResourceException e) 
		{
			return null;
		}

		//Tokenize out all of the list elements
		StringTokenizer tokenizer =
			new StringTokenizer(listString, VALUE_DELIMITER);
		Collection values = new ArrayList();
		while(tokenizer.hasMoreTokens())
		{
			String token = tokenizer.nextToken();
			if(limiter == null || value == null)
			{
				values.add(token);
			}
			//Possibly screen out list elements based on the limiter criterion
			else
			{
				String limiterKey = token + KEY_DELIMITER + limiter;
				String limiterValue = getString(limiterKey);
				
				//If they limiter value equals the criterion, keep the item
				if(value.equals(limiterValue))
					values.add(token);
			}
		}
			
		//Copy into a String array
		Iterator iter = values.iterator();
		String[] retval = new String[values.size()];
		int i = 0;
		while(iter.hasNext())
			retval[i++] = (String)iter.next();
		return retval;
	}
}
