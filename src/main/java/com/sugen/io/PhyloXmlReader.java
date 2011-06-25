package com.sugen.io;

import java.awt.Color;
import java.util.List;

import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreeModel;
import javax.xml.parsers.SAXParserFactory;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import com.sugen.util.Clade;
import com.sugen.util.Clade.Confidence;
import com.sugen.util.Clade.Distribution;
import com.sugen.util.Clade.Reference;
import com.sugen.util.Clade.Sequence;
import com.sugen.util.Clade.Sequence.Annotation;
import com.sugen.util.Clade.Sequence.DomainArchitecture;
import com.sugen.util.Clade.Sequence.DomainArchitecture.Domain;
import com.sugen.util.Clade.Taxonomy;

/** 
 * Read phyloxml format from http://www.phyloxml.org.
 */
public class PhyloXmlReader extends TreeReader {
	private TreeModel tree;
	private boolean isRead;
	
	public PhyloXmlReader() {
//		System.err.println("PhyloXmlReader");
	}

	public boolean hasNext() {
		if (tree == null && !isRead)
			init();		
		return tree != null;
	}

	public Object next() {
		if (tree == null && !isRead)
			init();	
		TreeModel next = tree;
		tree = null;
		return next;
	}
	
	private void init() {
		isRead = true;
		try {
			SAXParserFactory.newInstance().newSAXParser()
				.parse(inputStream, new PhyloHandler());
		} catch (Exception e) {
			e.printStackTrace();
			throw new RuntimeException(e);
		}
	}
	
	class PhyloHandler extends DefaultHandler {
		private int depth = 0;
		private Clade parent;
		private Clade node;
		
		private boolean isClade;
		
		private boolean isName;
		private boolean isLength;
		private boolean isConfidence;
		
		private boolean isTaxonomy;
		private boolean isId;
		private boolean isCode;
		private boolean isScientificName;
		private boolean isCommonName;
		private boolean isRank;
		private boolean isUri;
		
		private boolean isDistribution;
		private boolean isDescription;
		
		private boolean isSequence;
		private boolean isSymbol;
		private boolean isAccession;
		private boolean isLocation;
		private boolean isDomain;
		
		private boolean isEvents;
		private boolean isAnnotation;
		
		private boolean isRed;
		private boolean isGreen;
		private boolean isBlue;

		public void startElement(String uri, String localName, 
				String qName, Attributes attributes) {
			
			// Abort after parsing the first tree
			if (depth < 0)
				return;
			
			if (qName.equals("phylogeny")) {
//				System.err.println("phylogeny: ");
				parent = new Clade();
	        	parent.setBranchLength(0.0f); // not a real clade
				tree = new DefaultTreeModel(parent);
				
			} else if (qName.equals("clade")) {
				isClade = true;
				++depth;
//				System.err.println("clade: " + depth);
				if (node != null) {
					parent = node;
				}				
				
		        node = new Clade();
		        if (depth == 1)
		        	node.setBranchLength(0);
		        parent.add(node);
		        
		        if (attributes != null && attributes.getLength() > 0) {
		        	for (int i = 0; i < attributes.getLength(); ++i) {
		        		if (attributes.getQName(i).equals("branch_length")) {
		        			node.setBranchLength(
		        					Double.parseDouble(attributes.getValue(i)));
		        		}
		        	}
		        }
		        
			} else if (qName.equals("name")) {
				isName = true;				
			} else if (qName.equals("branch_length")) {
				isLength = true;				
			} else if (qName.equals("confidence")) {
				isConfidence = true;

				if (isClade && !isEvents && !isAnnotation) {
					Clade c = node == null ? parent : node;
					Confidence conf = new Confidence();
	    			c.getConfidences().add(conf);
					
			        if (attributes != null && attributes.getLength() > 0) {
			        	for (int i = 0; i < attributes.getLength(); ++i) {
			        		if (attributes.getQName(i).equals("type")) {
			        			conf.setType(attributes.getValue(i));
			        		}
			        	}
			        }
				}
		        				
			} else if (qName.equals("taxonomy")) {
				isTaxonomy = true;
				Clade c = node == null ? parent : node;
				c.setTaxonomy(new Taxonomy());
				
			} else if (qName.equals("id") && isTaxonomy) {
				isId = true;
				
				Clade c = node == null ? parent : node;				
		        if (attributes != null && attributes.getLength() > 0) {
		        	for (int i = 0; i < attributes.getLength(); ++i) {
		        		if (attributes.getQName(i).equals("provider")) {
		        			c.getTaxonomy().setProvider(attributes.getValue(i));
		        		}
		        	}
		        }
				
			} else if (qName.equals("code")) {
				isCode = true;
			} else if (qName.equals("scientific_name")) {
				isScientificName = true;
			} else if (qName.equals("common_name")) {
				isCommonName = true;
			} else if (qName.equals("rank")) {
				isRank = true;
			} else if (qName.equals("uri")) {
				isUri = true;
				
			} else if (qName.equals("sequence")) {
				isSequence = true;
				Clade c = node == null ? parent : node;
				c.setSequence(new Sequence());

			} else if (qName.equals("accession")) {
				isAccession = true;
			} else if (qName.equals("location")) {
				isLocation = true;
			} else if (qName.equals("domain_architecture")) {
				Clade c = node == null ? parent : node;
				DomainArchitecture da = new DomainArchitecture();
				c.getSequence().setDomainArchitecture(da);

				if (attributes != null && attributes.getLength() > 0) {
		        	for (int i = 0; i < attributes.getLength(); ++i) {
		        		if (attributes.getQName(i).equals("length")) {
		        			da.setLength(Integer.parseInt(attributes.getValue(i)));
		        		}
		        	}
				}
				
			} else if (qName.equals("domain")) {
				isDomain = true;
				Clade c = node == null ? parent : node;
				Domain d = new Domain();
				c.getSequence().getDomainArchitecture().getDomains().add(d);

				if (attributes != null && attributes.getLength() > 0) {
		        	for (int i = 0; i < attributes.getLength(); ++i) {
		        		if (attributes.getQName(i).equals("from")) {
		        			d.setFrom(Integer.parseInt(attributes.getValue(i)));
		        		} else if (attributes.getQName(i).equals("to")) {
			        		d.setTo(Integer.parseInt(attributes.getValue(i)));
		        		} else if (attributes.getQName(i).equals("confidence")) {
			        		d.setConfidence(Double.parseDouble(attributes.getValue(i)));
		        		}
		        	}
		        }
				
			} else if (qName.equals("events")) {
				isEvents = true;

			} else if (qName.equals("annotation")) {
				isAnnotation = true;
				Clade c = node == null ? parent : node;
				
				if (isSequence) {
					c.getSequence().getAnnotations().add(new Annotation());
				} 
				
			} else if (qName.equals("distribution")) {
				isDistribution = true;
				Clade c = node == null ? parent : node;
				Distribution d = new Distribution();
				c.getDistributions().add(d);

			} else if (qName.equals("desc")) {
				isDescription = true;

			} else if (qName.equals("reference")) {
				Clade c = node == null ? parent : node;
				Reference ref = new Reference();
				c.getReferences().add(ref);
		        if (attributes != null && attributes.getLength() > 0) {
		        	for (int i = 0; i < attributes.getLength(); ++i) {
		        		if (attributes.getQName(i).equals("doi")) {
		        			ref.setDoi(attributes.getValue(i));
		        		}
		        	}
		        }

			} else if (qName.equals("red")) {
				isRed = true;
			} else if (qName.equals("green")) {
				isGreen = true;
			} else if (qName.equals("blue")) {
				isBlue = true;
			}
		}
		
		public void endElement(String uri, String localName, String qName) {
			// Abort after parsing the first tree
			if (depth < 0)
				return;

			if (qName.equals("clade")) {
				isClade = false;
				--depth;
				
				// Abort after parsing the first tree
				if (depth == 0)
					depth = -1; 
				
				node = parent;
				parent = (Clade)node.getParent();
				
			} else if (qName.equals("name")) {
				isName = false;				
			} else if (qName.equals("branch_length")) {
				isLength = false;				
			} else if (qName.equals("confidence")) {
				isConfidence = false;
				
			} else if (qName.equals("taxonomy")) {
				isTaxonomy = false;
			} else if (qName.equals("id")) {
				isId = false;
			} else if (qName.equals("code")) {
				isCode = false;
			} else if (qName.equals("scientific_name")) {
				isScientificName = false;
			} else if (qName.equals("common_name")) {
				isCommonName = false;
			} else if (qName.equals("rank")) {
				isRank = false;
			} else if (qName.equals("uri")) {
				isUri = false;
				
			} else if (qName.equals("sequence")) {
				isSequence = false;
			} else if (qName.equals("symbol")) {
				isSymbol = false;
			} else if (qName.equals("accession")) {
				isAccession = false;
			} else if (qName.equals("location")) {
				isLocation = false;
			} else if (qName.equals("domain")) {
				isDomain = false;
				
			} else if (qName.equals("events")) {
				isEvents = false;

			} else if (qName.equals("annotation")) {
				isAnnotation = false;

			} else if (qName.equals("distribution")) {
				isDistribution = false;
			} else if (qName.equals("desc")) {
				isDescription = false;

			} else if (qName.equals("red")) {
				isRed = false;
			} else if (qName.equals("green")) {
				isGreen = false;
			} else if (qName.equals("blue")) {
				isBlue = false;
			}
		}
		
		public void characters(char ch[], int start, int length)
        	  throws SAXException {
			String s = new String(ch, start, length).trim();
			if (s.length() <= 0)
				return;
						
			if (isName && !isSequence) {
				Clade clade = node == null ? parent : node;
				clade.setUserObject(s);
			
			} else if (isLength) {
				node.setBranchLength(Double.parseDouble(s));
				
			} else if (isConfidence && isClade && !isEvents && !isAnnotation) {
//				System.err.println("confidence: " + s);
				
				Clade c = node == null ? parent : node;
				c.getConfidences().get(c.getConfidences().size() - 1)
					.setValue(Double.parseDouble(s));

			} else if (isTaxonomy) {
				if (isId) {
					node.getTaxonomy().setId(s);
				} else if (isCode) {
					node.getTaxonomy().setCode(s);							
				} else if (isScientificName) {
					node.getTaxonomy().setScientificName(s);				
				} else if (isCommonName) {
					node.getTaxonomy().setCommonName(s);				
				} else if (isRank) {
					node.getTaxonomy().setRank(s);				
				} else if (isUri) {
					node.getTaxonomy().setUri(s);
				}
				
			} else if (isSequence) {
				if (isSymbol) {
					node.getSequence().setSymbol(s);
				} else if (isAccession) {
					node.getSequence().setAccession(s);
				} else if (isName) {
					node.getSequence().setName(s);
				} else if (isLocation) {
					node.getSequence().setLocation(s);					
				} else if (isUri) {
					node.getSequence().setUri(s);
				} else if (isDomain) {
					List<Domain> dlist = node.getSequence().getDomainArchitecture().getDomains();
					Domain d = dlist.get(dlist.size() - 1);
					d.setValue(s);				
				} else if (isAnnotation) {
					if (isDescription) {
						Annotation annot = node.getSequence().getAnnotations().get(
								node.getSequence().getAnnotations().size()-1);
						annot.setDescription(s);
					}
				}
			} else if (isDistribution) {
				if (isDescription) {
					Distribution d = node.getDistributions().get(
							node.getDistributions().size() - 1);
					d.setDescription(s);
				}
				
			} else if (isRed) {
				Color c = node.getColor() == null ? new Color(0,0,0) : node.getColor();
				node.setColor(new Color(Integer.parseInt(s), c.getGreen(), c.getBlue()));				
			} else if (isGreen) {
				Color c = node.getColor() == null ? new Color(0,0,0) : node.getColor();
				node.setColor(new Color(c.getRed(), Integer.parseInt(s),  c.getBlue()));				
			} else if (isBlue) {
				Color c = node.getColor() == null ? new Color(0,0,0) : node.getColor();
				node.setColor(new Color(c.getRed(), c.getGreen(), Integer.parseInt(s)));
			}
		}
	}
}
