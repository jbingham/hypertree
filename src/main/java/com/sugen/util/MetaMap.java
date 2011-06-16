package com.sugen.util;

import java.util.Map;
import java.util.HashMap;

/**
 * A HashMap with a parent HashMap of defaults, as with the Properties class.
 * Note that the only MetaMap method that refers to the parent Map is
 * currently get(Object). All other methods, such as iterator() and isEmpty(),
 * do not take into account any values stored in the parent Map. 
 * 
 * @author Jonathan Bingham
 */
public class MetaMap extends HashMap
{
	/**
	 * @serial
	 */
	protected Map defaults;
	
	public MetaMap(Map defaults)
	{
		setDefaults(defaults);
	}
	
	public Object get(Object key)
	{
		//System.err.println("Metakey request:" + key);
		Object retval = super.get(key);
		if(retval != null || defaults == null)
			return retval;
		else 
			return defaults.get(key);
	}
	
	public Map getDefaults()
	{
		return defaults;
	}
	
	public void setDefaults(Map defaults)
	{
		this.defaults = defaults;
	}
}
