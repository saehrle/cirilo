/*
 *  -------------------------------------------------------------------------
 *  Copyright 2014 
 *  Centre for Information Modeling - Austrian Centre for Digital Humanities
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License
 *  -------------------------------------------------------------------------
 */


package org.emile.cirilo.dialog;

import voodoosoft.jroots.core.CServiceProvider;
import voodoosoft.jroots.core.CPropertyService;
import voodoosoft.jroots.core.gui.CEventListener;
import voodoosoft.jroots.core.gui.CItemListener;
import voodoosoft.jroots.core.gui.CMouseListener;
import voodoosoft.jroots.dialog.*;
import voodoosoft.jroots.exception.CException;



import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.emile.cirilo.utils.ImagePreviewPanel;
import org.emile.cirilo.utils.ImageTools;
import org.emile.cirilo.Common;
import org.emile.cirilo.ServiceNames;
import org.emile.cirilo.User;
import org.emile.cirilo.ecm.templates.*;
import org.emile.cirilo.ecm.repository.*;
import org.emile.cirilo.ecm.repository.FedoraConnector.Relation;
import org.emile.cirilo.business.*;
import org.emile.cirilo.utils.*;
import org.emile.cirilo.*;
import org.emile.cirilo.gui.jtable.DefaultSortTableModel;
import org.jdom.Element;
import org.jdom.Namespace;
import org.jdom.input.DOMBuilder;
import org.jdom.input.SAXBuilder;
import org.jdom.xpath.XPath;

import com.asprise.util.ui.progress.ProgressDialog;

import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import java.net.*;
import java.nio.charset.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.imageio.ImageIO;
import javax.imageio.stream.FileImageOutputStream;
import javax.imageio.stream.ImageOutputStream;
import javax.swing.*;
import javax.swing.event.*;

import java.util.*;
import java.util.List;
import java.io.*;
import java.net.URL;
import java.text.MessageFormat;

import org.jdom.input.SAXBuilder;
import org.jdom.xpath.XPath;
import org.jdom.output.*;
import org.jdom.input.*;
import org.jdom.*;
import org.openrdf.repository.http.HTTPRepository;
import org.openrdf.repository.manager.RemoteRepositoryManager;

import javax.xml.parsers.DocumentBuilderFactory;

import com.lowagie.text.pdf.PdfReader;
import com.sun.media.imageioimpl.plugins.tiff.TIFFImageWriter;
import com.sun.media.imageioimpl.plugins.tiff.TIFFImageWriterSpi;

import fedora.server.types.gen.RelationshipTuple;


/**
 *  Description of the Class
 *
 * @author     Johannes Stigler
 * @created    10.3.2011
 */
public class ObjectEditorDialog extends CDialog {
    private static final Log LOG = LogFactory.getLog(ObjectEditorDialog.class);

	/**
	 *  Constructor for the LoginDialog object
	 */

	public ObjectEditorDialog() {}

	
	/**
	 *  Description of the Method
	 *
	 * @param  e  Description of the Parameter
	 */
	public void handleCancelButton(ActionEvent e) {
		try {
			if ( op[2].contains("Inactive") && statusChanged) Repository.modifyObject(pid, "I", null, null);
		} catch (Exception ex) {}	
		org.emile.cirilo.dialog.CBoundSerializer.save(this.getCoreDialog(), se.getObjectDialogProperties(), (JTable) null);   
		close();
	}

   public void set (String pid, String label, String model, String owner) {
	   this.pid = pid;
	   this.model = model;
	   this.label = label;
	   this.owner = owner;
   }
	
	public void handleSaveButton(ActionEvent e) {
		  try {
				
			  getCoreDialog().setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
			  statusChanged= false;
			  Repository.modifyObject( pid, "A",
			    		this.label,
						(String)((JComboBox) getGuiComposite().getWidget("jcbUser")).getSelectedItem()); 
		  }  catch (Exception ex) {
			  ex.printStackTrace();
		   }					  
			finally {
				getCoreDialog().setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
			}
	}

	public void handleSaveRelationsButton(ActionEvent e) {
		  try {
				
			  getCoreDialog().setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
			  JList ltRels = (JList) getGuiComposite().getWidget( "jtRelations" );
			  DefaultListModel lm = (DefaultListModel) ltRels.getModel();
			  for (  Map.Entry<String, String> r : coll.entrySet()) {
				  Repository.purgeRelation("info:fedora/"+pid,Common.isMemberOf, r.getValue());
			   }
			  for (Enumeration el = lm.elements() ; el.hasMoreElements() ;) {
				  String s= el.nextElement().toString();
				  Split id = new Split(s);
				  Repository.addRelation("info:fedora/"+pid,Common.isMemberOf,id.get());
			   }

			  TEI t = new TEI(null,false,true);
       		  if (t.get(pid)) {
       			   t.createRELS_INT(null);
       		  }	   

  				
		   }  catch (Exception ex) {
		   }					  
			finally {
				getCoreDialog().setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
			}
	}
	
	public void handleRelsMouseDoubleClick(MouseEvent e, int type) {

		try {

			CEventListener.setBlocked(true);
			// no events while handling

			if (type == MouseEvent.MOUSE_CLICKED) {
				if (e.getClickCount() >= 2) {
					handleRemoveRelationButton(null);	
				}
			}
		} catch (Exception ex) {
		} finally {
			CEventListener.setBlocked(false);
		}

	}	

	public void handleNonRelsMouseDoubleClick(MouseEvent e, int type) {

		try {

			CEventListener.setBlocked(true);
			// no events while handling

			if (type == MouseEvent.MOUSE_CLICKED) {
				if (e.getClickCount() >= 2) {
					handleAddRelationButton(null);	
				}
			}
		} catch (Exception ex) {
		} finally {
			CEventListener.setBlocked(false);
		}

	}	

	public void handleAddRelationButton( ActionEvent ev ) {
		try {
			JList ltRels = (JList) getGuiComposite().getWidget( "jtRelations" );
			DefaultListModel lma = (DefaultListModel) ltRels.getModel();
			JList ltNonRels = (JList) getGuiComposite().getWidget( "jtNonRelations" );
			DefaultListModel lmb = (DefaultListModel) ltNonRels.getModel();

			int[] sel = ltNonRels.getSelectedIndices();
			ArrayList rm = new ArrayList( 16 );

			for ( int i = 0; i < sel.length; i++ ) {
				lma.addElement( lmb.getElementAt( sel[i] ) );
				rm.add( lmb.getElementAt( sel[i] ) );
			}
			for ( int i = 0; i < rm.size(); i++ ) {
				lmb.removeElement( rm.get( i ) );
			}
			ltRels.setSelectedIndex( ltRels.getLastVisibleIndex() );
			ltNonRels.setSelectedIndex( ltNonRels.getLastVisibleIndex() );
			getGuiComposite().getWidget( "jbRemoveRelation" ).setEnabled( !lma.isEmpty() );
			getGuiComposite().getWidget( "jbAddRelation" ).setEnabled( !lmb.isEmpty() );
			getGuiComposite().getWidget( "jbSaveRelations" ).setEnabled(true);
		}
		catch ( Exception e ) {
		}
	}


	/**
	 *  Description of the Method
	 *
	 * @param  ev  Description of the Parameter
	 */
	public void handleRemoveRelationButton( ActionEvent ev ) {
		try {
			JList ltRels = (JList) getGuiComposite().getWidget( "jtRelations" );
			DefaultListModel lma = (DefaultListModel) ltRels.getModel();
			JList ltNonRels = (JList) getGuiComposite().getWidget( "jtNonRelations" );
			DefaultListModel lmb = (DefaultListModel) ltNonRels.getModel();

			int[] sel = ltRels.getSelectedIndices();
			ArrayList rm = new ArrayList( 16 );

			for ( int i = 0; i < sel.length; i++ ) {
				lmb.addElement( lma.getElementAt( sel[i] ) );
				rm.add( lma.getElementAt( sel[i] ) );
			}
			for ( int i = 0; i < rm.size(); i++ ) {
				lma.removeElement( rm.get( i ) );
			}
			ltRels.setSelectedIndex( ltRels.getLastVisibleIndex() );
			ltNonRels.setSelectedIndex( ltNonRels.getLastVisibleIndex() );
			getGuiComposite().getWidget( "jbRemoveRelation" ).setEnabled( !lma.isEmpty() );
			getGuiComposite().getWidget( "jbAddRelation" ).setEnabled( !lmb.isEmpty() );
			getGuiComposite().getWidget( "jbSaveRelations" ).setEnabled( true );
		}
		catch ( Exception e ) {
		}
	}
	

	
	public void handleNewButton(ActionEvent e) {
		try {
			CreateDatastreamDialog loD;
			
			loD = (CreateDatastreamDialog) CServiceProvider.getService(DialogNames.CREATEDATASTREAM_DIALOG);
			loD.open();

			if (!loD.getID().isEmpty()) {
				File fp =  File.createTempFile( "temp", ".tmp");
				if (loD.getMimetype().equals("text/xml")) {
		            FileOutputStream fop = new FileOutputStream(fp);  
		            byte[] contentInBytes = "<content/>".getBytes("UTF-8");  
		            fop.write(contentInBytes);  
		            fop.flush();  
		            fop.close();  
				}
				if (loD.getMimetype().equals("image/tiff")) {
					String ref ="http://"+user.getIIPSUrl()+"/iipsrv?FIF=&hei=900&cvt=jpeg";
					Repository.addDatastream(pid, loD.getID(), loD.getLabel(), loD.getMimetype(), ref);
				} else {
					Repository.addDatastream(pid, loD.getID(), loD.getLabel(), loD.getMimetype().equals("text/xml") ? "X" : "M", loD.getMimetype(), fp );
				}	
				fp.delete();
		    	JTable ds = (JTable) getGuiComposite().getWidget(jtDatastreams);
		    	ds.setModel(Repository.listDatastreams(pid,false));
		    	ds.setRowSelectionInterval(0,0);		
			}

		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}

	public void handleDelButton(ActionEvent e) {
		try {
	    	JTable tb = (JTable) getGuiComposite().getWidget("jtDatastreams");
  		  	int[] selected = tb.getSelectedRows();
  		  	
    		MessageFormat msgFmt = new MessageFormat(res.getString("delstream"));
    		Object[] args = {selected.length};
    		int liChoice = JOptionPane.showConfirmDialog(null, msgFmt.format(args) ,
	    		  						Common.WINDOW_HEADER, JOptionPane.YES_NO_OPTION,
	    		  						JOptionPane.QUESTION_MESSAGE);

    		if (liChoice == 0) {
      	    for (int i=selected.length-1; i>-1; i--) {
    	    	String dsid = (String) tb.getValueAt(selected[i], 0);

    	    	if (Common.SYSTEM_DATASTREAMS.contains("|"+dsid+"|")) {
    	    		msgFmt = new MessageFormat(res.getString("nonvaliddel"));
    	    		Object[] args0 = {dsid};
    	    		JOptionPane.showMessageDialog( null, msgFmt.format(args0), Common.WINDOW_HEADER, JOptionPane.INFORMATION_MESSAGE);
    	    	} else {		
				  			 
    	    		if (liChoice == 0) {
			    	  	Repository.purgeDatastream(pid, dsid);
			    	  	JTable ds = (JTable) getGuiComposite().getWidget(jtDatastreams);
			    	  	ds.setModel(Repository.listDatastreams(pid,false));
			    	  	ds.setRowSelectionInterval(0,0);
    	    		}
    	    	}
      	    }
			}		

		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}

	
	public void handleoUploadButton(ActionEvent e) {
	      handleUpload(false);	  
	}

	public void handlemUploadButton(ActionEvent e) {
	       handleUpload(true);	  		  
	}
	
	public void handlemEditButton(ActionEvent e) {
		try {
		    getCoreDialog().setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));				

		    JTable tb = (JTable) getGuiComposite().getWidget("jtMetadata");
	    	int rs = tb.getSelectedRow();

	    	String dsid = (String) tb.getValueAt(rs, 0);
	    	String mimetype = (String) tb.getValueAt(rs, 2);
	    	String group = (String) tb.getValueAt(rs, 3);
	    	String location = (String) tb.getValueAt(rs, 5);
	
			if (dsid.equals("DC")) {
				NewObjectDialog dlg;
				dlg = (NewObjectDialog) CServiceProvider.getService(DialogNames.NEWOBJECT_DIALOG);
				dlg.set((EditObjectDialog) null,pid,owner);
				dlg.open();
			}  else  if (isText(mimetype)) {
	        	TextEditor dlg = (TextEditor) CServiceProvider.getService(DialogNames.TEXTEDITOR);
	        	dlg.set(pid, dsid, mimetype, group, location);
	        	dlg.open();
			}  else if (isImage(mimetype)) {
		        try {
		        	byte[] stream = null; 
					ByteArrayInputStream byteArrayInputStream = null;
					java.awt.image.BufferedImage image = null;
					if (group.equals("R")) {
						URL url = new URL(location);
						image = javax.imageio.ImageIO.read(url);
					} else {
						stream =  Repository.getDatastream(pid, dsid, "");
						byteArrayInputStream = new ByteArrayInputStream(stream);
						image = javax.imageio.ImageIO.read(byteArrayInputStream);
						byteArrayInputStream.close();
					}
	  			  	ij.ImagePlus ip = new ij.ImagePlus(dsid, image);
					ip.show("Statuszeile");
		        } catch (IOException eq) {
		        	eq.printStackTrace();
		        }
		    } else {
				JOptionPane.showMessageDialog(  getCoreDialog(), res.getString("noedit"), Common.WINDOW_HEADER, JOptionPane.INFORMATION_MESSAGE );	        	
	        }
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		finally {
		    getCoreDialog().setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));				
		}
	}


	
	public void handleoEditButton(ActionEvent e) {
		try {
		    getCoreDialog().setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));				

		    JTable tb = (JTable) getGuiComposite().getWidget("jtDatastreams");
	    	int rs = tb.getSelectedRow();

	    	String dsid = (String) tb.getValueAt(rs, 0);
	    	String title = (String) tb.getValueAt(rs, 1);
	    	String mimetype = (String) tb.getValueAt(rs, 2);
	    	String group = (String) tb.getValueAt(rs, 3);
	    	String location = (String) tb.getValueAt(rs, 5);
	    	
			if (dsid.equals("DC")) {
				EditDCDialog dlg;
				dlg = (EditDCDialog) CServiceProvider.getService(DialogNames.EDITDC_DIALOG);
				dlg.set(pid,owner);
				dlg.open();			
			}  else if  (isText(mimetype)) {
	        	TextEditor dlg = (TextEditor) CServiceProvider.getService(DialogNames.TEXTEDITOR);
	        	dlg.set(pid, dsid, mimetype, group, location);
	        	dlg.open();
			}  else if (isImage(mimetype)) {
		        try {
		        	byte[] stream = null; 
					ByteArrayInputStream byteArrayInputStream = null;
					java.awt.image.BufferedImage image = null;
					if (group.equals("R")) {
						URL url = new URL(location);
						image = javax.imageio.ImageIO.read(url);
					} else {
						stream =  Repository.getDatastream(pid, dsid, "");
						byteArrayInputStream = new ByteArrayInputStream(stream);
						image = javax.imageio.ImageIO.read(byteArrayInputStream);
						byteArrayInputStream.close();
					}
	  			  	ij.ImagePlus ip = new ij.ImagePlus(dsid, image);
					ip.show("Statuszeile");
		        } catch (IOException eq) {
		        	eq.printStackTrace();
		        }
	        } else {
				JOptionPane.showMessageDialog(  getCoreDialog(), res.getString("noedit"), Common.WINDOW_HEADER, JOptionPane.INFORMATION_MESSAGE );	        	
	        }
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		finally {
			    getCoreDialog().setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));				
		}
	}

	public void handlemMouseDoubleClick(MouseEvent e, int type) {

		try {

			CEventListener.setBlocked(true);
			// no events while handling

			if (type == MouseEvent.MOUSE_CLICKED) {
				if (e.getClickCount() >= 2) {
					handleUpload(true);	
				}
			}
		} catch (Exception ex) {
		} finally {
			CEventListener.setBlocked(false);
		}

	}	

	public void handleoMouseDoubleClick(MouseEvent e, int type) {

		try {

			CEventListener.setBlocked(true);
			// no events while handling

			if (type == MouseEvent.MOUSE_CLICKED) {
				if (e.getClickCount() >= 2) {
					handleUpload(false);	
				}
			}
		} catch (Exception ex) {
		} finally {
			CEventListener.setBlocked(false);
		}

	}	
	
	public void handleUpload(boolean mode) {
		JFileChooser chooser = null;
		Boolean ret;
	    try {
	    	ret = true; 
			getCoreDialog().setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
		    JTable tb = (JTable) getGuiComposite().getWidget(mode? "jtMetadata" : "jtDatastreams");
	    	int rs = tb.getSelectedRow();

	    	String dsid = (String) tb.getValueAt(rs, 0);
	    	String mimetype = (String) tb.getValueAt(rs, 2);
	    	String controlgroup = (String) tb.getValueAt(rs, 3);
	    	String location = (String) tb.getValueAt(rs, 5);
	    	
 		    CPropertyService props = (CPropertyService) CServiceProvider.getService(ServiceNames.PROPERTIES);
			
 		    if ( controlgroup.contains("X") || controlgroup.contains("M") || mimetype.toLowerCase().equals("image/tiff") ) {
				chooser = new JFileChooser(props.getProperty("user", "import.path"));
				chooser.setDialogTitle(res.getString("choosefile"));
			
				if (mimetype.toLowerCase().contains("xml")) chooser.addChoosableFileFilter(new FileFilter(".xml"));
				if (dsid.toLowerCase().contains("ontology")) chooser.addChoosableFileFilter(new FileFilter(".rdf"));
				if (mimetype.toLowerCase().contains("jpeg")) {
					ImagePreviewPanel preview = new ImagePreviewPanel();
					chooser.setAccessory(preview);
					chooser.addPropertyChangeListener(preview);
					chooser.addChoosableFileFilter(new FileFilter(".jpg"));
				}
				if (mimetype.toLowerCase().contains("tiff")) {
					ImagePreviewPanel preview = new ImagePreviewPanel();
					chooser.setAccessory(preview);
					chooser.addPropertyChangeListener(preview);
					chooser.addChoosableFileFilter(new FileFilter(".tif"));
				}

				if (mimetype.toLowerCase().contains("plain")) chooser.addChoosableFileFilter(new FileFilter(".txt"));
				if (mimetype.toLowerCase().contains("pdf")) chooser.addChoosableFileFilter(new FileFilter(".pdf"));

				if (chooser.showDialog(getCoreDialog(), res.getString("choose")) != JFileChooser.APPROVE_OPTION) {
					return;
				} 
				props.setProperty("user", "import.path", chooser.getCurrentDirectory().getAbsolutePath());
				props.saveProperties("user");
			    getCoreDialog().setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
			    
			    if (dsid.equals("THUMBNAIL")) {
			    	File thumbnail = File.createTempFile( "temp", ".tmp" );			    
			    	ImageTools.createThumbnail( chooser.getSelectedFile(), thumbnail, 100, 80, Color.lightGray );
			    	Repository.modifyDatastream(pid, dsid, mimetype, controlgroup, thumbnail);
			    	thumbnail.delete();
			    } else if (mimetype.toLowerCase().equals("image/tiff") && !user.getIIPSUser().isEmpty()) {
					TIFFImageWriterSpi imageWriterSpi = new TIFFImageWriterSpi();
					TIFFImageWriter imageWriter = (TIFFImageWriter)imageWriterSpi.createWriterInstance();

					File tp = File.createTempFile("temp", ".tmp");
					ImageOutputStream out = new FileImageOutputStream(tp);
					imageWriter.setOutput(out);
						
					BufferedImage img = ImageIO.read(chooser.getSelectedFile());			      				                		
					org.emile.cirilo.business.PTIFConverter.pyramidGenerator(imageWriter, img, 256, 256);
					out.close();
					String ref = null;
					Scp s = new Scp();
					if (s.connect()) {
						ref = s.put(tp, pid, dsid);
						s.disconnect();
						ref = "http://"+user.getIIPSUrl()+"/iipsrv?FIF="+ref+"&hei=900&cvt=jpeg";
						try {
							Repository.modifyDatastream(pid, dsid, mimetype, "M", ref);
						} catch (Exception ep) {}          											
					}
					tp.delete();
			    	
 		    	} else {
 		    		if (mimetype.toLowerCase().contains("pdf")) {
/* 		    			try {
 		    				 byte [] passwd = null;
 		    				 PdfReader reader = new PdfReader(chooser.getSelectedFile().getAbsolutePath(), passwd);
 		    			} catch(Exception ex) {
 	    					 JOptionPane.showMessageDialog(getCoreDialog(), "Hinzufügen von Datei "+chooser.getSelectedFile()+" ist nicht möglich. Validierung des Dateinhaltes ist fehlgeschlagen.\nDie Datei enthält kein Dokument in einem gültigen PDF-Format. ");
 		    				 return;
 		    			} */
 		    		}
 		    		if (mimetype.toLowerCase().equals("image/jpeg") || mimetype.toLowerCase().equals("image/tiff")) {
						File thumb = File.createTempFile( "temp", ".tmp" );
						ImageTools.createThumbnail( chooser.getSelectedFile(), thumb, 100, 80, Color.lightGray );
				    	Repository.modifyDatastream(pid, "THUMBNAIL", "image/jpeg", "M", thumb);
				    	thumb.delete();
 		    		} 	

 		    		String path = chooser.getSelectedFile().getAbsolutePath();
 		    		File selected = new File(path);

 		    		
 		    		if (dsid.equals("TEI_SOURCE")) {
						TEI t = new TEI(null,false,true);
						t.set(chooser.getSelectedFile().getAbsolutePath(), false);
						if (t.isValid()) {
						    t.setPID(pid);
						    t.validate(null, null);
							Repository.modifyDatastreamByValue(pid, "TEI_SOURCE", "text/xml", t.toString());
						    refresh(true);
						} else { ret = false; }
 				    } else if (dsid.equals("METS_SOURCE")) {
						METS m = new METS(null,false,true);
						m.set(selected.getAbsolutePath(), false);
						if (m.isValid()) {
						    m.setPID(pid);
						    m.ingestImages();
						    m.write();
						    m.createMapping(null, null);
							Repository.modifyDatastreamByValue(pid, "METS_SOURCE", "text/xml", m.toString());
						    refresh(true);
						} else { ret = false; }
						
 				    } else if (dsid.equals("BIBTEX") && !selected.getAbsolutePath().contains(".xml")) { 				    	
 				    	File bibtex = File.createTempFile( "temp", ".tmp" );			    
						net.sourceforge.bibtexml.BibTeXConverter bc = new net.sourceforge.bibtexml.BibTeXConverter();
						bc.bibTexToXml( selected, bibtex );
 				    	Repository.modifyDatastream(pid, dsid,"text/xml", controlgroup, bibtex);
 				    	bibtex.delete();

 				    } else {
 				    	
 				    	Repository.modifyDatastream(pid, dsid, mimetype, controlgroup, selected);
 				    	
 				    	if (dsid.equals("ONTOLOGY")) {

 				    		try {
 				    			String ses = (String) props.getProperty("user", "sesame.server");
					        	RemoteRepositoryManager repositoryManager = new RemoteRepositoryManager(ses == null ? Common.SESAME_SERVER : ses);
					        	repositoryManager.setUsernameAndPassword(user.getUser(), user.getPasswd());
					        	repositoryManager.initialize();	 				           	
					        	org.openrdf.repository.Repository repo = repositoryManager.getRepository(user.getUrl().substring(7).replace("/",".")); 	
					        	repo.initialize(); 				    			
					        	org.openrdf.repository.RepositoryConnection con = repo.getConnection();	 				    			
					        	con.clear(new org.openrdf.model.impl.URIImpl(pid)); 							 							  				
 					        	con.add(chooser.getSelectedFile(), null, org.openrdf.rio.RDFFormat.RDFXML, new org.openrdf.model.impl.URIImpl(pid)); 					           		
 				    		} catch (Exception e) {
 				    			e.printStackTrace();
 								JOptionPane.showMessageDialog(  getCoreDialog(), e.getMessage(), Common.WINDOW_HEADER, JOptionPane.INFORMATION_MESSAGE); 				    			
 				    		}
 				    	}
 				    	
 				    }	

 		    		if (ret) 
 		    		{
 		    			MessageFormat msgFmt = new MessageFormat(res.getString("update"));
 		    			Object[] args = {dsid, pid,chooser.getSelectedFile()}; 		    		
 		    			JOptionPane.showMessageDialog(  getCoreDialog(), msgFmt.format(args), Common.WINDOW_HEADER, JOptionPane.INFORMATION_MESSAGE);
 		    		} else {
 		    			MessageFormat msgFmt = new MessageFormat(res.getString("parsererror"));
		    			Object[] args = {chooser.getSelectedFile()}; 		    		
		    			JOptionPane.showMessageDialog(  getCoreDialog(), msgFmt.format(args), Common.WINDOW_HEADER, JOptionPane.INFORMATION_MESSAGE);
 		    		}	
		    	}
				if ( mode) {
					JTable md = (JTable) getGuiComposite().getWidget(jtMetadata);
					md.setModel(Repository.listDatastreams(pid,true));
					md.setRowSelectionInterval(rs,rs);			
					md. setShowHorizontalLines(false);
				} else {
					JTable ds = (JTable) getGuiComposite().getWidget(jtDatastreams);
					ds.setModel(Repository.listDatastreams(pid,false));
					ds.setRowSelectionInterval(rs,rs);		
					ds. setShowHorizontalLines(false);
				}
			}
 		    if ( controlgroup.contains("R") && !mimetype.toLowerCase().contains("tiff") ) {
 		    	if (dsid.contains("STYLESHEET")) {
 		    		SelectLayoutDialog dlg = (SelectLayoutDialog) CServiceProvider.getService(DialogNames.SELECTLAYOUT_DIALOG);
 		    		dlg.set(pid, dsid, model, owner);
 		    		dlg.open();
 					if (!dlg.getStylesheet().isEmpty()) tb.setValueAt(dlg.getStylesheet(), rs, 5 );
 		    	} else {
 		    		LocationDialog dlg = (LocationDialog) CServiceProvider.getService(DialogNames.LOCATION_DIALOG);
 		    		dlg.set(pid, dsid, location);
 					dlg.open();
 					if (!dlg.get().isEmpty()) tb.setValueAt(dlg.get(), rs, 5 );
 		    	}	
 		    }
	    	
	    } catch (Exception e) {
			 
				MessageFormat msgFmt = new MessageFormat(res.getString("errimport"));
				Object[] args = {chooser.getSelectedFile()}; 		    		
				JOptionPane.showMessageDialog(  getCoreDialog(), msgFmt.format(args), Common.WINDOW_HEADER, JOptionPane.INFORMATION_MESSAGE);
					
	    }	
		finally {
			getCoreDialog().setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
		}

	}
	
	class FileFilter extends javax.swing.filechooser.FileFilter {
		  private String filter;
		  public FileFilter(String f) {
			  this.filter = f;
		  }
		  public boolean accept(File file) {
			    if (file.isDirectory()) {
			    	return true;
			        }
		    String filename = file.getName();
		    return filename.endsWith(this.filter);
		  }
		  public String getDescription() {
			    return "*"+this.filter;
		  }
	}      
	
	public void setMetadata() {
		
	}

	public void setDatastreams() {
		
	}
	
	public void handleEditButton(ActionEvent e) {
		  try {
		  
		    } catch (Exception ex) {}					  
	}
	
	public void handlerRemoved(CEventListener aoHandler) {
	}

	public void handleSeekButton(ActionEvent e) {
		  try {
			    seekString =  (String) ((JTextField) getGuiComposite().getWidget("jtfSeek")).getText().trim();
			    props.setProperty("user", "relations.seekterm", seekString);
				props.saveProperties("user");

			   refresh(true);
		    } catch (Exception ex) {}					  
	}

	public void refresh(boolean mode) {
		  try {
			   getCoreDialog().setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));

			    JList ltRels = (JList) getGuiComposite().getWidget( "jtRelations" );
				DefaultListModel lma = (DefaultListModel) ltRels.getModel();
				lma.removeAllElements();
				JList ltNonRels = (JList) getGuiComposite().getWidget( "jtNonRelations" );
				DefaultListModel lmb = (DefaultListModel) ltNonRels.getModel();
				lmb.removeAllElements();
				java.util.List  <org.emile.cirilo.ecm.repository.FedoraConnector.Relation>relations = Repository.getRelations(pid,Common.isMemberOf);	
				coll = new HashMap();
		        for (Relation r : relations) {
		        	   String s=r.getTo().substring(12); 
		        	   coll.put(s, s);
		        }
		        java.util.List<String> containers = Repository.getContainers(user.getUser(), groups.contains( "administrator") );
             			        
		        SortedSet<String> sortedset= new TreeSet<String>(containers);
		        
				HashMap<String,String> hm = new HashMap();
		        SortedSet<String> hma = new TreeSet<String>();
			
				for (String s: sortedset) {
					  Split id = new Split(s);
	                  hm.put(id.get(), s);
	                  hma.add(id.get());
				}
				
				for (  String s: hma) {
					 if (coll.get(s) != null) lma.addElement(hm.get(s));
					 else if (mode) {
						 if (hm.get(s).contains(seekString.replace("*", ""))) {
							 lmb.addElement(hm.get(s));
						 }						 
					 } else {
						 lmb.addElement(hm.get(s));
					 }	 
				}
	
							
				ltRels.setSelectedIndex( 0 );
	            ltNonRels.setSelectedIndex( 0 );
	            
				new CMouseListener(ltRels, this, "handleRelsMouseDoubleClick");
				new CMouseListener(ltNonRels, this, "handleNonRelsMouseDoubleClick");
		  
		    } catch (Exception ex) {}		
			finally {
				getCoreDialog().setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
			}

	}

	
	
	
	/**
	 *  Description of the Method
	 */
	protected void cleaningUp() {
	}

	public void show()
	 throws CShowFailedException {
		try {
			se = (Session) CServiceProvider.getService( ServiceNames.SESSIONCLASS );						
			org.emile.cirilo.dialog.CBoundSerializer.load(this.getCoreDialog(), se.getObjectDialogProperties(), (JTable) null);
			
			seekString = props.getProperty("user", "relations.seekterm");
			moGA.setData("jtfSeek", seekString);
		    refresh(true);			
			
		} catch (Exception e) {		
		}
	}
	/**
	 *  Description of the Method
	 *
	 * @exception  COpenFailedException  Description of the Exception
	 */
	protected void opened() throws COpenFailedException {

		try {

			res =(ResourceBundle) CServiceProvider.getService(ServiceNames.RESOURCES);

			moGA = (CDefaultGuiAdapter)getGuiAdapter();						
			CDialogTools.createButtonListener(this, "jbClose", "handleCancelButton");
			CDialogTools.createButtonListener(this, "jbSave", "handleSaveButton");
			CDialogTools.createButtonListener(this, "jbSaveRelations", "handleSaveRelationsButton");
			CDialogTools.createButtonListener(this, "jbSeek", "handleSeekButton");
			CDialogTools.createButtonListener(this, "jboUpload", "handleoUploadButton");
			CDialogTools.createButtonListener(this, "jbmUpload", "handlemUploadButton");
			CDialogTools.createButtonListener(this, "jboEdit", "handleoEditButton");
			CDialogTools.createButtonListener(this, "jbmEdit", "handlemEditButton");
			CDialogTools.createButtonListener(this, "jbAddRelation", "handleAddRelationButton");
			CDialogTools.createButtonListener(this, "jbRemoveRelation", "handleRemoveRelationButton");

			CDialogTools.createButtonListener(this, "jbNew", "handleNewButton");
			CDialogTools.createButtonListener(this, "jbDel", "handleDelButton");
			
			
			String[] op = Repository.getObjectProfile(pid);
			((JTextField) getGuiComposite().getWidget("jtfIdentifier")).setText(pid);
			((JTextField) getGuiComposite().getWidget("jtfIdentifier")).setEnabled(false);
//			((JComboBox) getGuiComposite().getWidget("jcbState")).setSelectedIndex(op[2].contains("Inactive")? 1: 0);
			
			popupMetadata = new JPopupMenu();
 		    JMenuItem mi;
			mi = new JMenuItem(res.getString("import"));
		    mi.addActionListener(new ActionListener() {
		    	  public void actionPerformed(ActionEvent e) {
		    		  handlemUploadButton(null);
		    	  }
		      });	      
		    popupMetadata.add(mi);
		    popupMetadata.add(new JSeparator());
			mi = new JMenuItem(res.getString("edit"));
		    mi.addActionListener(new ActionListener() {
		    	  public void actionPerformed(ActionEvent e) {
		    		  handlemEditButton(null);
		    	  }
		      });	      
		    popupMetadata.add(mi);

		    popupDatastreams = new JPopupMenu();
			mi = new JMenuItem(res.getString("import"));
		    mi.addActionListener(new ActionListener() {
		    	  public void actionPerformed(ActionEvent e) {
		    		  handleoUploadButton(null);
		    	  }
		      });	      
		    popupDatastreams.add(mi);			
		    popupDatastreams.add(new JSeparator());
			mi = new JMenuItem(res.getString("edit"));
		    mi.addActionListener(new ActionListener() {
		    	  public void actionPerformed(ActionEvent e) {
		    		  handleoEditButton(null);
		    	  }
		      });	      
		    popupDatastreams.add(mi);
			
			user = (User) CServiceProvider.getService(ServiceNames.CURRENT_USER);
			groups = (ArrayList) CServiceProvider.getService( ServiceNames.MEMBER_LIST );

			JTable md = (JTable) getGuiComposite().getWidget(jtMetadata);
			md.getSelectionModel().addListSelectionListener(new MySelectionListener(md,(JButton) getGuiComposite().getWidget("jbmEdit")));			
			
/*
 			statusChanged = false;
			if ( op[2].contains("Inactive") ) {
				statusChanged = true;
				Repository.modifyObject(pid, "A", null, null);
			}
*/
			
			md.setModel(Repository.listDatastreams(pid,true));
			md.setRowSelectionInterval(0,0);			
			md. setShowHorizontalLines(false);
			new CMouseListener(md, this, "handlemMouseDoubleClick");			
		    MouseListener popupListener = new PopupmListener();
		    md.addMouseListener(popupListener);

		    if (!pid.equals("cirilo:Backbone"))  {
		    	JTable ds = (JTable) getGuiComposite().getWidget(jtDatastreams);
		    	ds.getSelectionModel().addListSelectionListener(new MySelectionListener(ds,(JButton) getGuiComposite().getWidget("jboEdit")));			

		    	ds.setModel(Repository.listDatastreams(pid,false));
		    	ds.setRowSelectionInterval(0,0);		
		    	ds. setShowHorizontalLines(false);
		    	new CMouseListener(ds, this, "handleoMouseDoubleClick");
		    	popupListener = new PopupoListener();
		    	ds.addMouseListener(popupListener);
		    }
		    
			JComboBox jcbUser = ((JComboBox) getGuiComposite().getWidget("jcbUser"));
	        java.util.List<String> users = Repository.getUsers();
	        for (String s : users) {
	        	    if (!s.isEmpty()) jcbUser.addItem(s);
	        	    
	         }
	        
	        jcbUser.setSelectedItem(owner);
			((JComboBox) getGuiComposite().getWidget("jcbUser")).setSelectedItem(op[0].replace("\"",""));
			((JComboBox) getGuiComposite().getWidget("jcbUser")).setEnabled(groups.contains("administrator"));
			getGuiComposite().getWidget( "jbSaveRelations" ).setEnabled( false );

			props = (CPropertyService) CServiceProvider.getService(ServiceNames.PROPERTIES);

			JTextField tf = (JTextField) getGuiComposite().getWidget("jtfSeek");
			tf.addKeyListener(
				new KeyAdapter() {
					public void keyPressed(KeyEvent ev) {
						if (ev.getKeyCode() == KeyEvent.VK_ENTER) {
							ev.consume();
							handleSeekButton(null);
						}
					}
				});

			JTabbedPane tpPane = ((JTabbedPane) getGuiComposite().getWidget("tpPane"));
			tpPane.setSelectedIndex(2);
			
		} catch (Exception ex) {
			ex.printStackTrace();
			throw new COpenFailedException(ex);
		}
	}

	
    private boolean isText(String mimetype) {
    	if (mimetype.contains("xml") || mimetype.contains("text/plain")) return true;
    	return false;
    }

    private boolean isImage(String mimetype) {
    	if (mimetype.contains("jpeg") || mimetype.contains("gif") | mimetype.contains("tif") | mimetype.contains("png")) return true;
    	return false;
    }

    private boolean isRelation(String group) {
    	if (group.contains("R")) return true;
    	return false;
    }
	
	 class MySelectionListener implements ListSelectionListener{

		JTable table;
		JButton button;
		
		public MySelectionListener(JTable table, JButton button) {
			this.table = table;
			this.button = button;
		}
		@Override
		public void valueChanged(ListSelectionEvent e) {
			 int rs = this.table.getSelectedRow() > -1 ? this.table.getSelectedRow() : 0;
			 String mimetype = (String) this.table.getValueAt(rs, 2);
			 String group = (String) this.table.getValueAt(rs, 3);
			 String id = (String) this.table.getValueAt(rs, 0);
			 button.setText(isImage(mimetype) || isRelation(group) ? res.getString("show") : res.getString("edit"));
		}

	}	
	 
	  class PopupmListener extends MouseAdapter {
		    public void mousePressed(MouseEvent e) {
		      showPopup(e);
		    }
		    public void mouseReleased(MouseEvent e) {
		      showPopup(e);
		    }
		    private void showPopup(MouseEvent e) {
		      if (e.isPopupTrigger()) {
		    	  popupMetadata.show(e.getComponent(), e.getX(), e.getY());
		      }
		    }
		  }
	  
	  class PopupoListener extends MouseAdapter {
		    public void mousePressed(MouseEvent e) {
		      showPopup(e);
		    }
		    public void mouseReleased(MouseEvent e) {
		      showPopup(e);
		    }
		    private void showPopup(MouseEvent e) {
		      if (e.isPopupTrigger()) {
		        popupDatastreams.show(e.getComponent(), e.getX(), e.getY());
		      }
		    }
		  }
	
	private CDefaultGuiAdapter moGA;
	private User user;
	private ArrayList<String> groups;
	private Session se;
	private String pid;
	private String  model;
	private String  label;
	private String owner;
	private HashMap<String,String> coll;
	private JPopupMenu popupMetadata;
	private JPopupMenu popupDatastreams;
	private ResourceBundle res;
	private CPropertyService props;
	private String seekString;
	private String[] op;
	private final String jtMetadata ="jtMetadata";
	private final String jtDatastreams ="jtDatastreams";
	private Boolean statusChanged;


}
