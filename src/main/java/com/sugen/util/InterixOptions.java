package com.sugen.util;

import java.beans.Beans;
import java.io.File;
import java.io.IOException;

/**
 * Parse command-line parameters according to Interix specifications. This
 * supports single character flags of the form "-f", long parameter names of the
 * form "--flag", values of the forms "-f=3", "-f 3", "--flag=3" or "--flag 3",
 * and an arbitrary number of non-flagged arguments at the end of the command
 * line. (It also supports unflagged arguments interspersed with flagged ones,
 * but that's usually bad style, so I haven't provided an example.)
 * <p>
 * Here's some sample code that might appear inside a main(String[] argv)
 * method:
 * 
 * <pre>
 * InterixOptions opts = new InterixOptions(argv);
 * while(opts.next() &amp;&amp; opts.isFlag())
 * {
 * 	//option is &quot;-h&quot; or &quot;--help&quot;
 * 	if(opts.isFlag(&quot;h&quot;, &quot;help&quot;)) 
 * 		showHelpMessage();
 * 
 * 	//option is &quot;-s=string&quot; or &quot;-s string&quot;	
 * 	else if(opts.isFlag(&quot;s&quot;))    
 * 		String s = opts.getString();
 * 
 * 	//option is &quot;-i=n&quot; or &quot;-i n&quot;, where n is an integer
 * 	else if(opts.isFlag(&quot;i&quot;))    
 * 		int i = opts.getInt();
 * 
 * 	//option is &quot;-f=n&quot; or &quot;-f n&quot;, where n is a floating point
 * 	else if(opts.isFlag(&quot;f&quot;&quot;))   
 * 		File file = opts.getFile();
 * 
 * 	//option is &quot;--bool&quot;
 * 	else if(opts.isFlag(&quot;bool&quot;)) 
 * 		boolean isBool = true;
 * 
 * 	//option is &quot;--object=com.MyCompany.MyClass&quot; 
 * 	//or &quot;--object com.MyCompany.MyClass&quot;, 	
 * 	//where com.MyCompany.MyClass is a Java class name
 * 	else if(opts.isFlag(&quot;object&quot;))
 * 		MyClass name = (MyClass)opts.getObject();
 * 
 * 	else 
 * 		throw new IllegalArgumentException(&quot;Unknown parameter &quot; + opts.getFlag());
 * }
 * 
 * //Process remaining (unflagged) parameters, if any, one at a time
 * do
 * {
 * 	BufferedReader reader;
 * 	if(opts.getOption() != null)
 * 		reader = new BufferedReader(new FileReader(opts.getOption()));
 * 	//Default to reading STDIN
 * 	else 
 * 		reader = new BufferedReader(new InputStreamReader(System.in));
 * 
 * 	//Do something with the input...
 * } 
 * while(opts.next());
 * </pre>
 * 
 * @author Jonathan Bingham
 */
public class InterixOptions {
	protected String option;
	protected int index = 0;
	protected String[] argv;

	public InterixOptions(String[] argv) {
		this.argv = argv;
	}

	/**
	 * Move on to the next option.
	 * 
	 * @return true if there are more options
	 */
	public boolean next() {
		boolean hasNext = index < argv.length;
		if (hasNext) {
			option = argv[index];
			++index;
		} else {
			option = null;
		}
		return hasNext;
	}

	/**
	 * Is the current option a flagged option of the form "-f" or "--flag"?
	 */
	public boolean isFlag() {
		if (option == null)
			return false;
		return (option.startsWith("--") && option.length() > 2)
				|| (option.startsWith("-") && option.length() > 1);
	}

	/**
	 * Is the current option a flag with the specified value?
	 * 
	 * @param flag
	 *            the flag excluding the leading hyphen or hyphens
	 */
	public boolean isFlag(String flag) {
		if (!isFlag())
			return false;

		String s = getFlag();
		return flag.equals(s);
	}

	/**
	 * Is the current option a flag with either of the specified values? This is
	 * a convenience for when multiple flags have the same meaning; ie, if "-f"
	 * and "--flag" are equivalent. In that case, isFlag("f", "flag") will
	 * return true if the current flag equals either of those options.
	 * 
	 * @param flag
	 *            the flag excluding the leading hyphen or hyphens
	 * @param alt
	 *            an alternate flag excluding the leading hyphen or hyphens
	 */
	public boolean isFlag(String flag, String alt) {
		return isFlag(flag) || isFlag(alt);
	}

	/**
	 * Parse out the flag portion of a command, eliminating the leading hyphen
	 * or hyphens and also any assigned value after an equals sign.
	 * 
	 * @throws IllegalArgumentException
	 *             for badly formatted flags
	 */
	public String getFlag() {
		if (option == null)
			return null;

		int start = 1;
		int end = option.indexOf("=");
		// long name
		if (option.length() > 2 && option.charAt(1) == '-')
			++start;
		// illegal name of the form: "-xx"
		else if ((end != 2 && end != -1) || (end == -1 && option.length() > 2))
			throw new IllegalArgumentException(option);

		if (end == -1)
			return option.substring(start);
		else
			return option.substring(start, end);
	}

	/**
	 * Get the current option, exactly as it appeared on the command line.
	 */
	public String getOption() {
		return option;
	}

	/**
	 * Parse out the value of a String specifying "-s=value" or "--str=value".
	 */
	protected String getValue() {
		if (option == null || !isFlag())
			return option;

		int start = option.indexOf("=");
		if (start == -1)
			option = argv[index++];
		return option.substring(start + 1);
	}

	/**
	 * Get the option's value as a File.
	 */
	public File getFile() {
		String s = getValue();
		if (s == null)
			return null;
		return new File(s);
	}

	/**
	 * Get the option's value as a String.
	 * 
	 * @throws IllegalArgumentException
	 *             if the option's value is null
	 */
	public String getString() {
		String s = getValue();
		if (s == null)
			throw new IllegalArgumentException(
					"Missing value specification for option " + s);
		return s;
	}

	/**
	 * Get the option's value as an int.
	 */
	public int getInt() {
		return Integer.parseInt(getValue());
	}

	/**
	 * Get the option's value as a float.
	 */
	public float getFloat() {
		return new Float(getValue()).floatValue();
	}

	/**
	 * Get the option's value as a double.
	 */
	public double getDouble() {
		return new Double(getValue()).doubleValue();
	}

	/**
	 * Instantiate a Java Object for the specified option's value.
	 */
	public Object getObject() throws ClassNotFoundException, IOException {
		String s = getValue();
		ClassLoader loader = Class.class.getClassLoader();
		return Beans.instantiate(loader, s);
	}
}
