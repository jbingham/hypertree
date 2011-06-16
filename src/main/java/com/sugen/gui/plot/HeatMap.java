package com.sugen.gui.plot;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Insets;

import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;

/** 
 * A heatmap.
 * @author Jonathan Bingham
 */
public class HeatMap extends JPanel {
	private int squareSize = 12;
	private int labelLength;
	private int labelPadding = 6;
	private double max;
	private double[][] distances;
	private String[] labels;
	private Color hotColor = Color.GREEN;
	private Color coldColor = Color.BLACK;

	/**
	 * Constructor.
	 * @param distances square matrix
	 * @param labels row and column labels
	 * @throws IllegalArgumentException if distances or labels not null,
	 * or matrix is not square, or labels array is of wrong length 
	 */
	public HeatMap(double[][] distances, String[] labels) {
		this(distances, labels, 0d);
	}
	
	/**
	 * Constructor.
	 * @param distances square matrix
	 * @param labels row and column labels
	 * @throws IllegalArgumentException if distances or labels not null,
	 * or matrix is not square, or labels array is of wrong length 
	 */
	public HeatMap(double[][] distances, String[] labels, double max) {
		if(labels == null || labels.length == 0)
			throw new IllegalArgumentException("Labels cannot be null");
		if(distances == null || distances.length == 0)
			throw new IllegalArgumentException("Distance matrix cannot be null");
		if(labels.length != distances.length)
			throw new IllegalArgumentException(
					"Distance matrix and labels must have same number of elements");
		if(distances[0].length != distances.length)
			throw new IllegalArgumentException(
					"Distance matrix is not square");
		
		this.distances = distances;
		this.labels = labels;	
		this.max = max;
		
		if(this.max == 0) {
			for(int row = 0; row < distances.length; ++row) {
				for(int col = 0; col < distances.length; ++col) {
					if(distances[row][col] > this.max)
						this.max = distances[row][col];
				}
			}
		}
		int w = 10;
		setBorder(new EmptyBorder(w, w, w, w));
		setBackground(Color.WHITE);
		setOpaque(true);
	}
	
	/**
	 * Size of heatmap plus labels plus margins/borders.
	 */
	public Dimension getPreferredSize() {
		if(labelLength == 0 && getFont() != null)
			setFont(getFont());
		
		Dimension dim = super.getPreferredSize();
		dim.height += squareSize * distances.length + labelLength + labelPadding;
		dim.width  += squareSize * distances.length + labelLength + labelPadding;
		return dim;
	}
	
	/**
	 * Compute maximum label legnth.
	 */
	public void setFont(Font font) {
		super.setFont(font);
		FontMetrics metrics = getFontMetrics(font); 
		labelLength = 0;
		if(labels != null)
			for(int i = 0; i < labels.length; ++i) {
				int w = metrics.stringWidth(labels[i]);
				if(w > labelLength)
					labelLength = w;
			}
	}
	
	protected void paintComponent(Graphics g) {
		super.paintComponent(g);	
		Graphics2D g2d = (Graphics2D)g;		
		paintLabels(g2d);
		paintHeatMap(g2d);
	}
	
	/**
	 * Color for maximum heat.
	 */
	public Color getHotColor() {
		return hotColor;
	}

	/**
	 * Color for maximum heat.
	 */
	public void setHotColor(Color hotColor) {
		this.hotColor = hotColor;
	}

	private void paintLabels(Graphics2D g) {
		Insets insets = getInsets();
		FontMetrics metrics = getFontMetrics(getFont()); 

		// Paint each label
		for(int i = 0; i < labels.length; ++i) {
			// Paint labels along left edge, right-aligned
			g.drawString(labels[i], 
					insets.left + labelLength - metrics.stringWidth(labels[i]), 
					insets.top + labelLength + labelPadding + (i + 1) * squareSize);
			
			// Paint labels along top, rotated vertically
			int x = insets.left + labelLength + labelPadding + (i + 1) * squareSize;
			int y = insets.top + labelLength;
			g.translate(x,y);
			g.rotate(-Math.PI / 2);
			g.drawString(labels[i],	0, 0); 
			g.rotate(Math.PI / 2);
			g.translate(-x, -y);
		}
	}
	
	private void paintHeatMap(Graphics2D g) {
		Insets insets = getInsets();
		int x = insets.left + labelLength + labelPadding;
		int y = x;
		Color originalColor = g.getColor();
		g.setColor(coldColor);
		g.fillRect(x, y, squareSize * distances.length, squareSize * distances.length);
		
		for(int row = 0; row < distances.length; ++row) {
			for(int col = 0; col < distances.length; ++col) {
				g.setColor(color(distances[row][col]));
				g.fillRect(x + col * squareSize, y + row * squareSize, 
						squareSize, squareSize);
			}
		}
		g.setColor(originalColor);
	}
	
	/**
	 * Make heatmap color.
	 * @param d in [0,1]
	 * @return alpha-adjusted color, derived from hotColor.
	 */
	private Color color(double d) {
		if(d > max)
			d = max;
		if(d < 0)
			d = 0;
		int alpha = (int)(255d * ((max - d) / max));
		return new Color(hotColor.getRed(), hotColor.getGreen(), hotColor.getBlue(), alpha);
	}

	/**
	 * Color of zero value. White or black looks best.
	 */
	public void setColdColor(Color coldColor) {
		this.coldColor = coldColor;
	}

	/**
	 * Color of zero value. White or black looks best.
	 */
	public Color getColdColor() {
		return coldColor;
	}
}
