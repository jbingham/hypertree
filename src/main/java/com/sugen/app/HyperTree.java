package com.sugen.app;

import java.awt.Dimension;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreeModel;

import com.sugen.event.SelectionListener;
import com.sugen.gui.AppClipboard;
import com.sugen.gui.AppMainWindow;
import com.sugen.gui.AppMenuBar;
import com.sugen.gui.Icons;
import com.sugen.gui.form.Form;
import com.sugen.gui.form.FormCarrier;
import com.sugen.gui.form.FormDialog;
import com.sugen.gui.io.TreeReaderWriterUI;
import com.sugen.gui.plot.HierarchicalClusterer;
import com.sugen.gui.tree.TreeViewUI;
import com.sugen.io.MultiTreeReader;
import com.sugen.io.PhylipReader;
import com.sugen.io.PhylipWriter;
import com.sugen.io.TreeReader;
import com.sugen.util.ClusterTreeNode;
import com.sugen.util.TreeDataModel;

/**
 * Launcher for hyperbolic tree viewer. The application in fact supports three
 * different views of a given tree structure: a hyperbolic view, a radial
 * 'unrooted' view, and a linear 'dendrogram' view. The views can be
 * manipulated, navigated, and customized in a variety of helpful ways.
 * Hopefully the application is easy enough to use that most of the
 * functionality is intuitive. No user manual currently exists.
 * <p>
 * One function worth mentioning is the capability to read and write color-code
 * files, which specify a color for each tree node. See the ColorCodeUI
 * documentation for specifics on the file format and usage.
 * <p>
 * As for file access, the application supports only Phylip treefiles,
 * currently. It could easily be modified to support any file type specified in
 * the io.properties file. This file lists available com.sugen.io.TreeReaders
 * Any standard or non-standard file format can be added by writing a new
 * TreeReader subclass and editing the properties file to include mention of the
 * new reader.
 * <p>
 * In addition to the the menu and toolbar controls, there are mouse controls
 * for manipulating the hyperbolic and radial tree views.
 * <ul>
 * <li>To drag the hyperbolic view through hyperbolic space, hold the mouse
 * button while dragging the mouse.</li>
 * <li>To rotate either the hyperbolic view or the radial view, hold the shift
 * key and the mouse button while dragging the mouse.</li>
 * <li>To zoom in or out on the hyperbolic view, hold the control key and the
 * mouse button while dragging the mouse.</li>
 * </ul>
 * <p>
 * 
 * @see com.sugen.gui.io.ColorCodeUI
 * @see com.sugen.io.TreeReader
 * @author Jonathan Bingham
 */
public class HyperTree implements Runnable {
	protected String fileName;
	private static String LICENSE = "HyperTreeLicense.txt";
	private static String PROPERTIES_FILE = ".HyperTree";
	private TreeViewUI treeViewUI;
	private AppMainWindow app;
	private HierarchicalClusterer clusterer;
	
	/**
	 * Execute with -h for full usage message.
	 */
	static public void main(String argv[]) {
		if (argv.length == 1 && argv[0].startsWith("-h")) {
			System.out.println("USAGE: java com.sugen.app.HyperTree [filename]");
			System.exit(0);
		}
		HyperTree app = new HyperTree();
		if (argv.length == 1)
			app.fileName = argv[0];
		app.run(); // No separate thread needed
	}
	
	/**
	 * Constructor.
	 */
	public HyperTree() {
		this(true);
	}

	/**
	 * Constructor.
	 * @param isRootThread if true, closing will exit the VM; if false,
	 * closing will leave the VM running
	 */
	public HyperTree(boolean isRootThread) {
		app = new AppMainWindow("HyperTree", new JLabel(Icons
				.get("HyperTreeSplash.png")));
		app.setIconImage(new ImageIcon(getClass().getClassLoader().getResource(
				"com/sugen/gui/images/hyperTree24.gif")).getImage());
		app.getAppToolBar().setRollover(Boolean.TRUE);
		try {
			app.loadProperties(PROPERTIES_FILE);
		} catch (IOException e) {
			e.printStackTrace();
		}
		app.setNativeLookAndFeel();
		app.setRootThread(isRootThread);

		// File IO
		TreeReaderWriterUI io = new TreeReaderWriterUI();
		io.getReaderUI().setReader(new MultiTreeReader());
		io.getWriterUI().setWriter(new PhylipWriter());
		app.addBean(io);

		treeViewUI = new TreeViewUI(app);
		app.getContentPane().add(treeViewUI);

		// Clipboard
		AppClipboard clipboard = new AppClipboard();
		clipboard.setActions(AppClipboard.UNDO_MASK | AppClipboard.REDO_MASK);
		app.addBean(clipboard);

		treeViewUI.addPropertyChangeListener(io);
		io.addPropertyChangeListener(treeViewUI);
		clipboard.setTarget(treeViewUI.getEditor());
		app.addBean(treeViewUI, true, true);

		// Let user import a distance matrix and cluster it
		clusterer = new HierarchicalClusterer(this);
		io.addPropertyChangeListener(clusterer);
		JMenu viewMenu = app.getAppMenuBar().getMenu(AppMenuBar.VIEW_MENU);
		viewMenu.addSeparator();
		app.addBean(clusterer);

		JMenu fileMenu = app.getAppMenuBar().getMenu(AppMenuBar.FILE_MENU);
		fileMenu.addSeparator();
		app.getAppMenuBar().add(io.getReaderUI().getRecentFilesAction());

		app.addBean(app, true, false);

		// Load file or URL specified on the command-line
		if (fileName != null)
			setTree(fileName);
	}

	/**
	 * Threadable way to launch the app.
	 */
	public void run() {
		checkLicense(LICENSE);
		app.setVisible(true);
	}

	/**
	 * Read tree from stream using PhylipReader.
	 */
	protected TreeModel readTree(InputStream input) {
		TreeReader reader = new PhylipReader();
		reader.setInput(input);
		if (reader.hasNext())
			return (TreeModel) reader.next();
		else
			return null;
	}

	/**
	 * Read tree from stream using PhylipReader.
	 */
	protected TreeModel readTree(String phylipString) {
		TreeReader reader = new PhylipReader();
		reader.setInput(phylipString);
		if (reader.hasNext())
			return (TreeModel) reader.next();
		else
			return null;
	}

	private void checkLicense(String path) {
		File propertyFile = new File(System.getProperty("user.home")
				+ File.separator + PROPERTIES_FILE);
		if (propertyFile.exists())
			return;

		FormCarrier formCarrier = new FormDialog(null, true);
		formCarrier.setForm(new LicenseForm(formCarrier, path));
		formCarrier.getCancelButton().setText("I Do Not Accept");
		formCarrier.getCancelButton().requestFocus();
		formCarrier.getOkButton().setText("I Accept");
		Object retval = formCarrier.showDialog();
		if (retval == null)
			System.exit(0);
	}

	private class LicenseForm extends Form {
		LicenseForm(FormCarrier formCarrier, String license) {
			super(formCarrier);
			setTitle("HYPERTREE License Agreement");
			// FormFactory factory = new FormFactory(this);
			JTextArea textArea = new JTextArea(readLicense(license));
			textArea.setEditable(false);
			JScrollPane scrollPane = new JScrollPane();
			scrollPane.setPreferredSize(new Dimension(textArea
					.getPreferredSize().width + 40, 200));
			scrollPane.getViewport().setView(textArea);
			add(scrollPane);
			add(new JLabel(
					"To use this software, you must accept the terms of the license agreement."));
		}

		private String readLicense(String path) {
			String license = null;
			try {
				InputStream is = getClass().getClassLoader()
						.getResourceAsStream(path);
				BufferedReader br = new BufferedReader(
						new InputStreamReader(is));
				StringBuffer sb = new StringBuffer();
				String line;
				while ((line = br.readLine()) != null) {
					sb.append(line + "\n");
				}
				license = sb.toString();
				br.close();
			} catch (IOException ioe) {
				ioe.printStackTrace();
				System.exit(0);
			}
			return license;
		}
	}

	// Hooks for programmatic launching of the application.

	/**
	 * Open tree from file or URL, if not null.
	 */
	public void setTree(String fileName) {
		if (fileName == null)
			throw new IllegalArgumentException("File cannot be null");

		TreeDataModel model = new TreeDataModel();
		InputStream input = null;
		try {
			input = new FileInputStream(fileName);
		} catch (IOException e) {
			e.printStackTrace();
		}
		TreeModel treeModel = readTree(input);
		model.setRoot((DefaultMutableTreeNode) treeModel.getRoot());
		treeViewUI.setDataModel(model);
	}

	/**
	 * The root of the tree to display.
	 */
	public void setTree(ClusterTreeNode root) {
		TreeDataModel model = new TreeDataModel();
		model.setRoot(root);
		treeViewUI.setDataModel(model);
	}

	/**
	 * Display a tree starting from a distance matrix.
	 */
	public void setTree(double[][] distances, String[] labels) {
		setTree(clusterer.cluster(distances, labels));
	}

	/**
	 * Display a tree starting from a distance matrix.
	 */
	public void setTree(double[][] distances, String[] labels, String linkage) {
		setTree(clusterer.cluster(distances, labels, linkage));
	}

	/**
	 * Listen for selections of tree nodes or leaves.
	 */
	public void addSelectionListener(SelectionListener sl) {
		treeViewUI.getPlotView().getSelectionModel().addListener(sl);
	}

	/**
	 * Listen for selections of tree nodes or leaves.
	 */
	public void removeSelectionListener(SelectionListener sl) {
		treeViewUI.getPlotView().getSelectionModel().removeListener(sl);
	}
	
	public AppMainWindow getMainWindow() {
		return app;
	}
}
