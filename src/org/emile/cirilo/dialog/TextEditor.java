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
import voodoosoft.jroots.dialog.*;

import org.emile.cirilo.Common;
import org.emile.cirilo.ServiceNames;
import org.emile.cirilo.User;
import org.emile.cirilo.ecm.repository.*;
import org.emile.cirilo.business.*;
import org.jdom.input.SAXBuilder;
import org.jdom.Document;
import org.apache.log4j.Logger;

import java.awt.Cursor;
import java.awt.event.*;

import javax.swing.*;

import java.text.MessageFormat;
import java.util.*;
import java.io.*;
import java.net.URL;


/**
 *  Description of the Class
 *
 * @author     Johannes Stigler
 * @created    10.3.2011
 */
public class TextEditor extends CDialog {
 
	private static Logger log = Logger.getLogger(TextEditor.class);

	private String dsid;
	private String mimetype;
	private String group;
	private String location;
	private String model;
	private String owner;
	
	/**
	 *  Constructor for the TextEditor object
	 */

	public TextEditor() {}

	public void set (String pid, String dsid, String mimetype, String group, String location, String model, String owner) {
		this.pid = pid;
		this.dsid = dsid;
		this.mimetype = mimetype;
		this.group = group;
		this.location = location;
		this.model = model;
		this.owner = owner;
	}

	/**
	 *  Description of the Method
	 *
	 * @param  e  Description of the Parameter
	 */
	public void handleCancelButton(ActionEvent e) {
		org.emile.cirilo.dialog.CBoundSerializer.save(this.getCoreDialog(), se.getTextEditorProperties(), (JTable) null);   
		close();
	}

	
	public void handlerRemoved(CEventListener aoHandler) {
	}

	/**
	 *  Description of the Method
	 */
	protected void cleaningUp() {
	}

	public void show()
	 throws CShowFailedException {
		try {
		  org.emile.cirilo.dialog.CBoundSerializer.load(this.getCoreDialog(), se.getTextEditorProperties(), (JTable) null);
          
	      JScrollPane scrPane = (JScrollPane) getGuiComposite().getWidget("scrPane");
		  scrPane.setVisible(false);

		  JEditorPane jebEditorPane = (JEditorPane) getGuiComposite().getWidget("jebEditorPane");
		  if (mimetype.contains("xml")) {
			  	jebEditorPane.setContentType("text/xml");
			    if (group.equals("R")) {
				  	URL url = new URL (location);
			    	jebEditorPane.setPage(url);
			    } else {
			    	jebEditorPane.setText(new String(Repository.getDatastream(pid, dsid,""),"UTF-8"));
			    	
			    }
		   } else if (Common.TEXT_MIMETYPES.contains(mimetype)) {
			  	byte[] buf = Repository.getDatastream(pid, dsid, "");
			  	jebEditorPane.setContentType(mimetype);
			  	jebEditorPane.setText(new String(buf));
	      } else if (mimetype.contains("text/log")) {
		  	   jebEditorPane.setContentType("text/plain");			  	
			   jebEditorPane.setText(new String(readFile(pid)));      	  
         }
  	     scrPane.setVisible(true);
 	     
		 } catch (Exception e){
			 log.error(e.getLocalizedMessage(),e);	
		 }
		 finally {
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
			  
		  se = (Session) CServiceProvider.getService( ServiceNames.SESSIONCLASS );						
	      user = (User) CServiceProvider.getService(ServiceNames.CURRENT_USER);
	      
	      props = (CPropertyService) CServiceProvider.getService(ServiceNames.PROPERTIES);

	      
		  CDialogTools.createButtonListener(this, "jbClose", "handleCancelButton");
		  	  
		  JMenuItem jmiSave = (JMenuItem) getGuiComposite().getWidget("jmiSave");
		  jmiSave.setEnabled(!group.equals("R"));
	      jmiSave.addActionListener(new ActionListener() {
	    	  public void actionPerformed(ActionEvent e) {
    			  JMenuItem jmiSave = null; 
	    		  try {
	    			  jmiSave = (JMenuItem) getGuiComposite().getWidget("jmiSave");
	    			  jmiSave.setEnabled(false);
	    			  getCoreDialog().setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
	    			  if (mimetype.contains("xml")) {
	    				  JEditorPane jebEditorPane = (JEditorPane) getGuiComposite().getWidget("jebEditorPane");
	    	              try  {		
	    	            	   if (dsid.equals("TEI_SOURCE") && !pid.startsWith("cirilo:")) {
	    	            		   TEI t = new TEI(null,false,false);
	    	            		   t.setUser(owner != null ? owner : user.getUser());
	    	            		   t.set(new String(jebEditorPane.getText().getBytes("UTF-8"),"UTF-8"));
	    	            		   t.setPID(pid);
	    	            		   
	    	            		   if (t.isValid()) {
	    	            			   t.validate(null, null);
  	    				  		       jebEditorPane.setText( t.toString());

  	    				  		   	   SAXBuilder builder = new SAXBuilder();
  	    				  		   	   try {
  	    				  		   		   Document doc = builder.build(new StringReader(t.toString()));
  	    				  		   		   Repository.modifyDatastreamByValue(pid, dsid, mimetype, new String(t.toString().getBytes("UTF-8"),"UTF-8"));
  	    				  		   	   } catch (Exception ex) {
  		  	 								JOptionPane.showMessageDialog(  getCoreDialog(),  res.getString("xmlformat") , Common.WINDOW_HEADER, JOptionPane.INFORMATION_MESSAGE); 				    				    	            			   
  	    				  		   	   }
	    	            		   } else {
	    	    		    			MessageFormat msgFmt = new MessageFormat(res.getString("parsererror"));
	    	    		    			Object[] args = {"TEI_SOURCE"}; 		    		
	    	    		    			JOptionPane.showMessageDialog(  getCoreDialog(), msgFmt.format(args), Common.WINDOW_HEADER, JOptionPane.INFORMATION_MESSAGE);
	    	            		   }		
	    	            	   } else if (dsid.equals("MEI_SOURCE") && !pid.startsWith("cirilo:")) {
		    	            		   MEI m = new MEI(null,false,false);
		    	            		   m.set(new String(jebEditorPane.getText().getBytes("UTF-8"),"UTF-8"));
		    	            		   m.setUser(owner != null ? owner : user.getUser());
		    	            		   m.setPID(pid);
		    	            		   
		    	            		   if (m.isValid()) {
		    	            			   m.validate(null, null);
	  	    				  		       jebEditorPane.setText( m.toString());

	  	    				  		   	   SAXBuilder builder = new SAXBuilder();
	  	    				  		   	   try {
	  	    				  		   		   Document doc = builder.build(new StringReader(m.toString()));
	  	    				  		   		   Repository.modifyDatastreamByValue(pid, dsid, mimetype, new String(m.toString().getBytes("UTF-8"),"UTF-8"));
	  	    				  		   	   } catch (Exception ex) {
	  		  	 								JOptionPane.showMessageDialog(  getCoreDialog(),  res.getString("xmlformat") , Common.WINDOW_HEADER, JOptionPane.INFORMATION_MESSAGE); 				    				    	            			   
	  	    				  		   	   }
		    	            		   } else {
		    	    		    			MessageFormat msgFmt = new MessageFormat(res.getString("parsererror"));
		    	    		    			Object[] args = {"MEI_SOURCE"}; 		    		
		    	    		    			JOptionPane.showMessageDialog(  getCoreDialog(), msgFmt.format(args), Common.WINDOW_HEADER, JOptionPane.INFORMATION_MESSAGE);
		    	            		   }		
	    	            	   } else if (dsid.equals("STORY") && !pid.startsWith("cirilo:")) {
		    	            		   STORY s = new STORY(null,false,false);
		    	            		   s.setUser(owner != null ? owner : user.getUser());
		    	            		   s.set(new String(jebEditorPane.getText().getBytes("UTF-8"),"UTF-8"));    	            		   
		    	            		   if (s.isValid()) {
		    	            			   s.validate(null, null);
		    	            			   s.setPID(pid);
	  	    				  		       jebEditorPane.setText(s.toString());	  	    				  		       
	  	    				  		   	   try {
	  	    				  		   		   Repository.modifyDatastreamByValue(pid, dsid, mimetype, new String(s.toString().getBytes("UTF-8"),"UTF-8"));
	  	    				  		   	   } catch (Exception ex) {
	  		  	 								JOptionPane.showMessageDialog(  getCoreDialog(),  res.getString("xmlformat") , Common.WINDOW_HEADER, JOptionPane.INFORMATION_MESSAGE); 				    				    	            			   
	  	    				  		   	   }
		    	            		   } else {
		    	    		    			MessageFormat msgFmt = new MessageFormat(res.getString("parsererror"));
		    	    		    			Object[] args = {"STORY"}; 		    		
		    	    		    			JOptionPane.showMessageDialog(  getCoreDialog(), msgFmt.format(args), Common.WINDOW_HEADER, JOptionPane.INFORMATION_MESSAGE);
		    	            		   }
	    	            		   
	    	            	   } else if (dsid.equals("LIDO_SOURCE") && !pid.startsWith("cirilo:")) {
		    	            		   LIDO l = new LIDO(null,false,false);
		    	            		   l.setUser(owner != null ? owner : user.getUser());
		    	            		   l.set(new String(jebEditorPane.getText().getBytes("UTF-8"),"UTF-8"));
		    	            		   l.setPID(pid);
		    	            		   
		    	            		   if (l.isValid()) {
		    	            			   l.validate(null, null);
	  	    				  		       jebEditorPane.setText( l.toString());

	  	    				  		   	   SAXBuilder builder = new SAXBuilder();
	  	    				  		   	   try {
	  	    				  		   		   Document doc = builder.build(new StringReader(l.toString()));
	  	    				  		   		   Repository.modifyDatastreamByValue(pid, dsid, mimetype, new String(l.toString().getBytes("UTF-8"),"UTF-8"));
	  	    				  		   	   } catch (Exception ex) {
	  		  	 								JOptionPane.showMessageDialog(  getCoreDialog(),  res.getString("xmlformat") , Common.WINDOW_HEADER, JOptionPane.INFORMATION_MESSAGE); 				    				    	            			   
	  	    				  		   	   }
		    	            		   } else {
		    	    		    			MessageFormat msgFmt = new MessageFormat(res.getString("parsererror"));
		    	    		    			Object[] args = {"LIDO_SOURCE"}; 		    		
		    	    		    			JOptionPane.showMessageDialog(  getCoreDialog(), msgFmt.format(args), Common.WINDOW_HEADER, JOptionPane.INFORMATION_MESSAGE);
		    	            		   }
	    	             	   } else if (dsid.equals("EDM_STREAM") && !pid.startsWith("cirilo:")) {
	    	             		    EDM edm = new EDM (user, jebEditorPane.getText());
	    	    		  		    jebEditorPane.setText(edm.toString());	  	
	    	    		  		    edm.save();
  	    				  		    try {
  	    				  		   	   Repository.modifyDatastreamByValue(pid, dsid, mimetype, edm.toString());
  	    				  		   	} catch (Exception ex) {
  		  	 							MessageFormat msgFmt = new MessageFormat(res.getString("parsererror"));
	    	    		    			Object[] args = {"EDM_STREAM"}; 		    		
	    	    		    			JOptionPane.showMessageDialog(  getCoreDialog(), msgFmt.format(args), Common.WINDOW_HEADER, JOptionPane.INFORMATION_MESSAGE);
	    	            		   }
    	       		       } else if (dsid.equals("DC") && !pid.startsWith("cirilo:")) {
	    	            		   DC d = new DC(null,false,true);
	    	            		   d.set(new String(jebEditorPane.getText().getBytes("UTF-8"),"UTF-8"));
  				  		   		   Repository.modifyDatastreamByValue(pid, dsid, mimetype, new String(d.toString().getBytes("UTF-8"),"UTF-8"));	    	            		   
	    	            	   } else {   
	    	            		   SAXBuilder builder = new SAXBuilder();
	    	            		   try {
	    	            			   Document doc = builder.build(new StringReader(jebEditorPane.getText()));
		  	    				  		    	            		   	    	            		   		  	    				  	
		  	    				  	   if (dsid.equals("METS_SOURCE") && !pid.startsWith("cirilo:")) {
		  	    				  		   METS m = new METS (null, false, true);
		  	    				  		   m.set(new String(jebEditorPane.getText().getBytes("UTF-8"),"UTF-8"));
		  	    				  		   m.setPID(pid);
		  	    				  		   m.ingestImages();
		  	    				  		   m.createMapping(null,null);
		  	    				  		   Repository.modifyDatastreamByValue(pid, dsid, mimetype, m.toString());	
		  	    				  		   jebEditorPane.setText( m.toString());
		  	    				  	   } else {
		  	    				  		   Repository.modifyDatastreamByValue(pid, dsid, mimetype, new String(jebEditorPane.getText().getBytes("UTF-8"),"UTF-8"));
		  	    				  	   }
		  	    				  	   
		  	    				  	   if (dsid.equals("ONTOLOGY")) {

		  	 	 				    		try {
		  	 	 				    			
		  	 	 				    			String pr = props.getProperty("user", "SKOS.IFY"); 
			  	 				    			if (model != null && model.contains("SKOS") && pr != null && pr.equals("1") ) {
			  	 				    				SkosifyFactory skosify = (SkosifyFactory) CServiceProvider.getService(ServiceNames.SKOSIFY_SERVICE);
			  	 				    				String skos = skosify.skosify(new String(jebEditorPane.getText().getBytes("UTF-8")));
			  	 				    				if (!skos.isEmpty()) jebEditorPane.setText(skos);
			  	 				    			} 
			  	 				    			

		  	 	 				    			File temp = File.createTempFile("tmp","xml");
		  	 					           		FileOutputStream fos = new FileOutputStream(temp);
		  	 					           		fos.write(jebEditorPane.getText().getBytes("UTF-8"));
		  	 					           		fos.close();

		  	 									TripleStoreFactory tf = new TripleStoreFactory();
		  	 									if (tf.getStatus()) {
		  	 										tf.update(temp, pid);
		  	 									}	
		  	 									tf.close();
		  	 					           		temp.delete();
		  	 				    		} catch (Exception ex) {
		  	 				    			log.error(ex.getLocalizedMessage(),ex);	
		  	 								JOptionPane.showMessageDialog(  getCoreDialog(), ex.getMessage(), Common.WINDOW_HEADER, JOptionPane.INFORMATION_MESSAGE); 				    			
		  	 				    		}
		  	    				  	  }		
	    	            		   } catch (Exception ex) {
	  	 								JOptionPane.showMessageDialog(  getCoreDialog(),  res.getString("xmlformat") , Common.WINDOW_HEADER, JOptionPane.INFORMATION_MESSAGE); 				    				    	            			   
	    	            		   }
		  	    				  	
	    	            	   }
	    	                } catch (Exception q) {
	    	                	log.error(q.getLocalizedMessage(),q);	
	    	                }

	    			  }
	    			  if ((Common.TEXT_MIMETYPES.contains(mimetype))) {
	    				  JEditorPane jebEditorPane = (JEditorPane) getGuiComposite().getWidget("jebEditorPane");
	    				  Repository.modifyDatastream(pid, dsid, mimetype, jebEditorPane.getText().getBytes("UTF-8"));
	    				  
	    			  }
	    	     } catch (Exception ex) {
	    	    	 log.error(ex.getLocalizedMessage(),ex);	
	    	     }
	 	  		 finally {
	 	  			if (jmiSave!= null) jmiSave.setEnabled(!group.equals("R"));
	 				getCoreDialog().setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
	 			}
	    	  }
	      });	      
	      JMenuItem jmiQuit = (JMenuItem) getGuiComposite().getWidget("jmiQuit");
	      jmiQuit.addActionListener(new ActionListener() {
	    	  public void actionPerformed(ActionEvent e) {
	    	     handleCancelButton(null);
	    	  }
	      });
		} catch (Exception ex) {
			throw new COpenFailedException(ex);
		}
	}

	private String readFile( String file ) throws IOException {
	    BufferedReader reader = new BufferedReader( new FileReader (file));
	    String line  = null;
	    StringBuilder stringBuilder = new StringBuilder();
	    String ls = System.getProperty("line.separator");
	    while( ( line = reader.readLine() ) != null ) {
	        stringBuilder.append( line );
	        stringBuilder.append( ls );
	    }
	    return stringBuilder.toString();
	 }

	private User user;
	private CPropertyService props;
	private ArrayList<String> groups;
	private ResourceBundle res;
	private Session se;
	private String pid;

}

