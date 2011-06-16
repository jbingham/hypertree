package com.sugen.gui.plot;

import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.event.ActionEvent;
import java.awt.image.BufferedImage;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Properties;

import javax.imageio.IIOImage;
import javax.imageio.ImageWriter;
import javax.imageio.stream.ImageOutputStream;
import javax.imageio.stream.MemoryCacheImageOutputStream;
import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JComponent;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.JWindow;

import com.sugen.app.HyperTree;
import com.sugen.gui.AppBean;
import com.sugen.gui.Icons;
import com.sugen.gui.SwingWorker;
import com.sugen.gui.io.ReaderUI;
import com.sugen.util.ClusterTreeNode;
import com.sugen.util.HierarchicalCluster;
import com.sun.imageio.plugins.png.PNGImageWriter;
import com.sun.imageio.plugins.png.PNGImageWriterSpi;

/**
 * Support import of a distance matrix to cluster.
 * 
 * @author Jonathan Bingham
 */
public class HierarchicalClusterer implements AppBean {
	public final Action importDistanceMatrix = new AbstractAction(
			"Import Distance Matrix...", Icons.get("emptyIcon24.gif")) {
		{
			putValue(KEY_MENU, FILE_MENU);
			putValue(KEY_LOCATION, VALUE_MENU_ONLY);
		}

		public void actionPerformed(ActionEvent e) {
			new SwingWorker() {
				public Object construct() {
					importDistanceMatrix();
					return null;
				}
			}.start();
		}
	};
	public final Action viewHeatMap = new AbstractAction(
			"View Heatmap...", Icons.get("emptyIcon24.gif")) {
		{
			putValue(KEY_MENU, VIEW_MENU);
			putValue(KEY_LOCATION, VALUE_MENU_ONLY);
		}

		public void actionPerformed(ActionEvent e) {
			new SwingWorker() {
				public Object construct() {
					viewHeatMap();
					return null;
				}
			}.start();
		}
	};
	private HyperTree hypertree;
	private Properties properties;
	private HierarchicalCluster cluster;
	private ClusterTreeNode root; // currently open

	/**
	 * Constructor.
	 * 
	 * @param hypertree
	 */
	public HierarchicalClusterer(HyperTree hypertree) {
		this(hypertree, null, null);
	}
	
	public HierarchicalClusterer(HyperTree hypertree, double[][] distances, String[] labels) {
		this.hypertree = hypertree;
		if(distances != null && labels != null)
			cluster = new HierarchicalCluster(distances, labels);
		properties = hypertree.getMainWindow().getProperties();
		updateActions();
	}

	public Action[] getActions() {
		return new Action[] { importDistanceMatrix, viewHeatMap };
	}

	/**
	 * Import a distance matrix, cluster it, and display the tree.
	 */
	private void importDistanceMatrix() {
		JFileChooser fileChooser = new JFileChooser(properties
				.getProperty(ReaderUI.PROPERTY_PATH));
		int reply = fileChooser.showOpenDialog(hypertree.getMainWindow());
		if (reply != JFileChooser.APPROVE_OPTION)
			return;

		try {
			File file = fileChooser.getSelectedFile();
			properties.setProperty(ReaderUI.PROPERTY_PATH, file
					.getAbsolutePath());

			propertySupport.firePropertyChange(PROPERTY_STATUS_MESSAGE, null,
            	"Loading file");

			BufferedReader br = new BufferedReader(new FileReader(file));
			String[] labels = null;
			String line = br.readLine();
			if (line == null)
				throw new IOException("File is empty");

			line = line.substring(1); // skip first tab
			labels = line.split("\t");
			double[][] matrix = new double[labels.length][labels.length];

			int row = 0;
			while ((line = br.readLine()) != null) {
				String[] data = line.split("\t");
				for (int col = 1; col < data.length; ++col) {
					matrix[row][col - 1] = Double.parseDouble(data[col]);
				}
				++row;
			}
			root = cluster(matrix, labels);
			hypertree.setTree(root);
			updateActions();
		} catch (Exception ex) {
			JOptionPane.showMessageDialog(null, "File parsing error: "
					+ ex.getLocalizedMessage(), "Error",
					JOptionPane.ERROR_MESSAGE);
		}
	}
	
	/**
	 * Display a heatmap of the data.
	 * Let user choose linkage method. 
	 * Complete linkage is the default,
	 * since it seems to make the best heatmap.
	 */
	public void viewHeatMap() {
		Object result = JOptionPane.showInputDialog(hypertree.getMainWindow(), "Linkage",
				"Select Linkage Method", JOptionPane.OK_CANCEL_OPTION, null,
				HierarchicalCluster.LINKAGE_METHODS, HierarchicalCluster.COMPLETE);
		if(result == null)
			return;
		
		cluster = new HierarchicalCluster(cluster.getDistanceMatrix(), cluster.getLeafLabels());
		cluster.setLinkageMethod((String)result);
		root = cluster.cluster();
		String[] labels = cluster.getReorderedLabels(root);
		double[][] distances = cluster.getReorderedMatrix(labels);
		JComponent heatmap = new HeatMap(distances, labels);
		JScrollPane scrollpane = new JScrollPane(heatmap);
		scrollpane.setPreferredSize(new Dimension(600,600));
		
		String[] options = new String[]{"Save Image...", "Save Matrix...", "OK", "Cancel"};		
		result = JOptionPane.showOptionDialog(hypertree.getMainWindow(), scrollpane, 
				"Heat Map", JOptionPane.DEFAULT_OPTION, JOptionPane.PLAIN_MESSAGE, null, 
				options, "OK");
		if(result == null)
			return;		
		
		// Save image...
		if(result.equals(new Integer(0)))
			saveHeatMap(new HeatMap(distances, labels));
		else if(result.equals(new Integer(1)))
			saveMatrix(distances, labels);
	}
	
	private void saveMatrix(double[][] distances, String[] labels) {
		JFileChooser fileChooser = new JFileChooser(properties
				.getProperty(ReaderUI.PROPERTY_PATH));
		int reply = fileChooser.showSaveDialog(hypertree.getMainWindow());
		if (reply != JFileChooser.APPROVE_OPTION)
			return;

		try {
			File file = fileChooser.getSelectedFile();
			if(!file.toString().toLowerCase().endsWith(".txt"))
				file = new File(file.toString() + ".txt");
			
			if(file.exists()) {
	            int confirm = JOptionPane.showConfirmDialog(hypertree.getMainWindow(),
	                    "File exists. Overwrite?", "Overwrite?",
	                    JOptionPane.OK_CANCEL_OPTION);
	            if (confirm != JOptionPane.OK_OPTION
	                    && confirm != JOptionPane.YES_OPTION)
	                return;
			}

			properties.setProperty(ReaderUI.PROPERTY_PATH, file
					.getAbsolutePath());
			
			propertySupport.firePropertyChange(PROPERTY_STATUS_MESSAGE, null,
        		"Saving matrix");
			
			FileWriter writer = new FileWriter(file);
			for(int i = 0; i < labels.length; ++i) {
				writer.write("\t" + labels[i]);
			}
			writer.write("\n");
			for(int row = 0; row < distances.length; ++row) {
				writer.write(labels[row]);
				for(int col = 0; col < distances.length; ++col) {
					writer.write("\t" + distances[row][col]);
				}
				writer.write("\n");
			}
			writer.flush();
			writer.close();
			
			propertySupport.firePropertyChange(PROPERTY_STATUS_MESSAGE, null,
        		"Matrix saved");
		} catch(IOException ex) {
			ex.printStackTrace();
			JOptionPane.showMessageDialog(hypertree.getMainWindow(), 
					"Error saving heatmap",
					"Error", JOptionPane.ERROR_MESSAGE);
		}
	}
	
	/**
	 * Save heatmap image to file.
	 */
	private void saveHeatMap(JComponent heatmap) {
		JFileChooser fileChooser = new JFileChooser(properties
				.getProperty(ReaderUI.PROPERTY_PATH));
		int reply = fileChooser.showSaveDialog(hypertree.getMainWindow());
		if (reply != JFileChooser.APPROVE_OPTION)
			return;

		try {
			File file = fileChooser.getSelectedFile();
			if(!file.toString().toLowerCase().endsWith(".png"))
				file = new File(file.toString() + ".png");
			
			if(file.exists()) {
	            int confirm = JOptionPane.showConfirmDialog(hypertree.getMainWindow(),
	                    "File exists. Overwrite?", "Overwrite?",
	                    JOptionPane.OK_CANCEL_OPTION);
	            if (confirm != JOptionPane.OK_OPTION
	                    && confirm != JOptionPane.YES_OPTION)
	                return;
			}

			properties.setProperty(ReaderUI.PROPERTY_PATH, file
					.getAbsolutePath());
			
			propertySupport.firePropertyChange(PROPERTY_STATUS_MESSAGE, null,
        		"Saving heatmap");

			// Make image
			JWindow window = new JWindow(hypertree.getMainWindow());
			window.getContentPane().add(heatmap);
			heatmap.setSize(heatmap.getPreferredSize());
			heatmap.validate();
			window.pack();
			window.setVisible(true);
	        BufferedImage bi =
	              new BufferedImage(heatmap.getWidth(), heatmap.getHeight(),
	                                BufferedImage.TYPE_INT_ARGB);
	        Graphics g = bi.getGraphics();
	        heatmap.paint(g);
	        window.setVisible(false);

	        // Save
	        FileOutputStream out = new FileOutputStream(file);
	        IIOImage image = new IIOImage(bi.getRaster(), null, null);
	        image.setRenderedImage(bi);
	        ImageWriter iw = new PNGImageWriter(new PNGImageWriterSpi());
            ImageOutputStream ios = new MemoryCacheImageOutputStream(out);
			iw.setOutput(ios);
			iw.write(image);
			ios.close();
			out.close();
	        
			propertySupport.firePropertyChange(PROPERTY_STATUS_MESSAGE, null,
        		"Heatmap saved");
		} catch(IOException ex) {
			ex.printStackTrace();
			JOptionPane.showMessageDialog(hypertree.getMainWindow(), 
					"Error saving heatmap",
					"Error", JOptionPane.ERROR_MESSAGE);
		}
	}

	/**
	 * Display a tree for the distance matrix. Prompt the user to choose a
	 * linkage method.
	 * 
	 * @param distances square matrix
	 * @param labels a label for each row/column in the distance matrix
	 */
	public ClusterTreeNode cluster(double[][] distances, String[] labels) {
		Object result = JOptionPane.showInputDialog(hypertree.getMainWindow(), "Linkage",
				"Select Linkage Method", JOptionPane.OK_CANCEL_OPTION, null,
				HierarchicalCluster.LINKAGE_METHODS, HierarchicalCluster.AVERAGE);
		if (result != null)
			return clusterAndDisplay(distances, labels, (String) result);
		else
			return null;
	}

	/**
	 * Display a tree for the distance matrix.
	 * 
	 * @param distances square matrix
	 * @param labels a label for each row/column in the distance matrix
	 * @param linkage AVERAGE, COMPLETE or SINGLE
	 */
	private ClusterTreeNode clusterAndDisplay(double[][] distances,
			String[] labels, String linkage) {
		return cluster(distances, labels, linkage);
	}

	/**
	 * Set the distance matrix and cluster it.
	 * 
	 * @param distances square matrix
	 * @param labels a label for each row/column in the distance matrix
	 * @param linkage AVERAGE, COMPLETE or SINGLE
	 */
	public ClusterTreeNode cluster(double[][] distances, 
			String[] labels, String linkage) {
		propertySupport.firePropertyChange(PROPERTY_STATUS_MESSAGE, null,
        		"Clustering");

		cluster = new HierarchicalCluster(distances, labels, linkage);
		try {
			root = cluster.cluster();
			updateActions();
			return root; 
		} finally {
			propertySupport.firePropertyChange(PROPERTY_STATUS_MESSAGE, null,
    			"Ready");
		}
	}

	/**
	 * Convert a similarity matrix to a distance matrix.
	 * 
	 * @param similarities all similarities must have values between 0 and 1.
	 */
	public static double[][] similarityToDistance(double[][] similarities) {
		double[][] distances = new double[similarities.length][similarities.length];
		for (int i = 0; i < similarities.length; i++) {
			for (int j = 0; j < similarities[0].length; j++) {
				distances[i][j] = 1.0 - similarities[i][j];
			}
		}
		return distances;
	}

	/** @serial */
	protected PropertyChangeSupport propertySupport = new PropertyChangeSupport(this);

	public synchronized void addPropertyChangeListener(
			PropertyChangeListener pcl) {
		propertySupport.addPropertyChangeListener(pcl);
	}

	public synchronized void removePropertyChangeListener(
			PropertyChangeListener pcl) {
		propertySupport.removePropertyChangeListener(pcl);
	}

	public void propertyChange(PropertyChangeEvent e) {
        String name = e.getPropertyName();
        //As fired by, eg, PlotReaderWriterUI
        if(PROPERTY_DATA.equals(name)) {
        	root = null;
        	cluster = null;
            updateActions();
        }
	}
	
    protected void updateActions() {
    	importDistanceMatrix.setEnabled(true);
    	viewHeatMap.setEnabled(root != null);
    }
}
