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
import voodoosoft.jroots.core.gui.CMouseListener;
import voodoosoft.jroots.dialog.*;
import voodoosoft.jroots.exception.CException;
import net.handle.hdllib.AbstractMessage;
import net.handle.hdllib.AbstractRequest;
import net.handle.hdllib.AbstractResponse;
import net.handle.hdllib.AdminRecord;
import net.handle.hdllib.AuthenticationInfo;
import net.handle.hdllib.CreateHandleRequest;
import net.handle.hdllib.DeleteHandleRequest;
import net.handle.hdllib.Encoder;
import net.handle.hdllib.HandleException;
import net.handle.hdllib.HandleValue;
import net.handle.hdllib.PublicKeyAuthenticationInfo;
import net.handle.hdllib.Resolver;
import net.handle.hdllib.Util;

import org.apache.log4j.Logger;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.io.IOUtils;
import org.emile.cirilo.Common;
import org.emile.cirilo.ServiceNames;
import org.emile.cirilo.User;
import org.emile.cirilo.ecm.templates.*;
import org.emile.cirilo.ecm.repository.*;
import org.emile.cirilo.business.*;
import org.emile.cirilo.gui.jtable.DefaultSortTableModel;
import org.emile.cirilo.ecm.repository.FedoraConnector.Relation;
import org.emile.cirilo.utils.Split;
import org.jdom.input.DOMBuilder;
import org.jdom.input.SAXBuilder;
import org.jdom.output.Format;
import org.jdom.output.XMLOutputter;
import org.jdom.transform.JDOMResult;
import org.jdom.transform.JDOMSource;
import org.jdom.xpath.XPath;
import org.jdom.filter.ElementFilter;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.Attribute;

import com.asprise.util.ui.progress.ProgressDialog;

import org.emile.cirilo.business.MDMapper;
import org.emile.cirilo.ecm.repository.Repository;
import org.emile.cirilo.business.IIIFFactory;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.ResourceBundle;
import java.awt.*;
import java.awt.event.*;

import javax.swing.*;

import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.io.*;
import java.net.URL;
import java.net.URLConnection;

import javax.xml.transform.Source;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;
import javax.xml.transform.Transformer;



/**
 *  Description of the Class
 *
 * @author     Johannes Stigler
 * @created    10.3.2011
 */
public class EditObjectDialog extends CDialog {

	private static Logger log = Logger.getLogger(EditObjectDialog.class);
	/**
	 *  Constructor for the LoginDialog object
	 */

	public EditObjectDialog() {}
	
	public void handleObjectReplace(ArrayList sb)
	throws Exception {
		
	   final ArrayList substitutions = sb;
	    new Thread() {
			public void run() {

				FileWriter	logger = null;
				try {
					  JTable loTable = (JTable) getGuiComposite().getWidget(jtData);
					  Boolean hasRelations = false;
					  
					  Format format = Format.getRawFormat();
					  format.setEncoding("UTF-8");
 					  XMLOutputter outputter = new XMLOutputter(format);		   			   														

 					  EDM edm = new EDM(user);
 					  
  					  SAXBuilder builder = new SAXBuilder(); 						 						
					  builder = new SAXBuilder();				    						

					  int[] selected = loTable.getSelectedRows();
					  ProgressDialog pd = new ProgressDialog(getCoreDialog(), Common.WINDOW_HEADER);
					  pd.displayPercentageInProgressBar = true;
					  pd.beginTask(res.getString("replace"), selected.length, true);
					  
					  logger = new FileWriter( new File(System.getProperty("user.home")).getAbsolutePath()+System.getProperty( "file.separator" )+"replace.log" );
					  
					  int deleted = 0;
					  
					  MessageFormat msgFmt = new MessageFormat(res.getString("objmod"));
	 				  Object[] args = {new Integer(selected.length).toString()};
	 					
				      int liChoice = JOptionPane.showConfirmDialog(null, msgFmt.format(args), res.getString("replaceobjc"), JOptionPane.YES_NO_OPTION,
				    		  						JOptionPane.QUESTION_MESSAGE);
					 
//					  System.setProperty("javax.xml.transform.TransformerFactory",  "net.sf.saxon.TransformerFactoryImpl");			
                      TransformerFactory transFact = TransformerFactory.newInstance();
                      ByteArrayOutputStream os;
				      Transformer trans = null;
				      Source xsltSource = null;
				      
				      byte[] url = null;
				      org.jdom.Document mapping = null;;
				      MDMapper m = null;;
				      
			    	  int fi=0;
				      if (liChoice == 0) {
				    	  for (int i=0; i<selected.length; i++) {
				    		  if(pd.isCanceled()) {break;}		
				    		  String pid =(String)loTable.getValueAt(selected[i],0);
				    		  String model =(String)loTable.getValueAt(selected[i],2);
				    		  String xuser =(String)loTable.getValueAt(selected[i],4);

							  logger.write( new java.util.Date()  +" " + pid + " ");									                                    	
				    		  
				    		  DOMBuilder db = new DOMBuilder();
				    		  try {

				    		  for (int j = 0; j < substitutions.size(); j++) {
				    			  String p = (String) substitutions.get(j);
				    			  if (p.substring(0,1).equals(Common.DCMAPPING)) {
				    				  if (p.substring(1,2).equals(Common.REPLACE)) {
					    					 if (!pid.contains("cirilo:") && model.contains("cm:TEI")) { 
					    						org.jdom.Document tei = db.build (Repository.getDatastream(pid, "TEI_SOURCE"));
	  			    	            		   	TEI t = new TEI(null,false,false);
	  			    	            		   	t.set(outputter.outputString(tei));
	  			    	            		   	t.setPID(pid);
	  			    	            		   	t.validate(null, null);
	   	    				  		   		   	Repository.modifyDatastreamByValue(pid, "TEI_SOURCE", "text/xml", new String(t.toString().getBytes("UTF-8"),"UTF-8"));
	  				    					 }   
					    					 if (!pid.contains("cirilo:") && model.contains("cm:MEI")) { 
					    						org.jdom.Document mei = db.build (Repository.getDatastream(pid, "MEI_SOURCE"));
	  			    	            		   	MEI n = new MEI(null,false,false);
	  			    	            		   	n.set(outputter.outputString(mei));
	  			    	            		   	n.setPID(pid);
	  			    	            		   	n.validate(null, null);
	   	    				  		   		   	Repository.modifyDatastreamByValue(pid, "MEI_SOURCE", "text/xml", new String(n.toString().getBytes("UTF-8"),"UTF-8"));
	  				    					 }   
							    			 if (!pid.contains("cirilo:") && model.contains("cm:OAIRecord")) {
							    					byte[] stylesheet = null;
							    		        	try {
							    			        	stylesheet =  Repository.getDatastream("cirilo:"+xuser, "RECORDtoEDM" , "");
							    			        } catch (Exception ex) {
						    			           		try { 
						    			           			stylesheet =  Repository.getDatastream("cirilo:Backbone", "RECORDtoEDM" , "");
						    			        		} catch (Exception q) {
														 log.error(q.getLocalizedMessage(),q);	
					    			        			 continue;
						    			        		}
							    		          	}

							    		        	try {					    		        	
							    		        		JDOMSource in = new JDOMSource(builder.build(new StringReader(new String(Repository.getDatastream(pid, "RECORD","")))));
							    		        		JDOMResult out = new JDOMResult();
													
							    		        		System.setProperty("javax.xml.transform.TransformerFactory",  "net.sf.saxon.TransformerFactoryImpl");  
							    		        		Transformer transformer = TransformerFactory.newInstance().newTransformer(new StreamSource(new StringReader(new String(stylesheet))));
							    		        		transformer.transform(in, out);
							    		        		System.setProperty("javax.xml.transform.TransformerFactory",  "org.apache.xalan.processor.TransformerFactoryImpl");	    					  
							    		        		edm.set(outputter.outputString(out.getResult()));
							    		        		Repository.modifyDatastreamByValue(pid, "EDM_STREAM", "text/xml", edm.toString());
							    		        		edm.refresh(pid);
							    		        	} catch (Exception e) {
							    		        		log.error(e.getLocalizedMessage(),e);	
							    		        		continue;
							    		        	}	
							    			 }					    					 
					    					 if (!pid.contains("cirilo:") && model.contains("cm:LIDO")) { 
				    						    org.jdom.Document lido = db.build (Repository.getDatastream(pid, "LIDO_SOURCE"));
	  			    	            		   	LIDO l = new LIDO(null,false,false);
	  			    	            		   	l.set(outputter.outputString(lido));
	  			    	            		   	l.setPID(pid);
	  			    	            		   	l.validate(null, null);
	   	    				  		   		   	Repository.modifyDatastreamByValue(pid, "LIDO_SOURCE", "text/xml", new String(l.toString().getBytes("UTF-8"),"UTF-8"));
	  				    					 }   
				    				  }	  
				    			  }
				    		  }

				    		  Document doc = db.build (Repository.getDatastream(pid, "DC"));
				    		  Element root = doc.getRootElement();
				    			  				    			  
				    			  
				    		  for (int j = 0; j < substitutions.size(); j++) {
				    			  String p = (String) substitutions.get(j);
				    			  if (p.substring(0,1).equals(Common.DUBLIN_CORE)) {
				    				  if (p.substring(1,2).equals(Common.REPLACE)) {
				    					  root.removeContent(new ElementFilter(Common.DCMI[j].toLowerCase()));
				    				  }
				    			  }
				    		  }
				    		  
				    		  for (int j = 0; j < substitutions.size(); j++) {
				    			  String p = (String) substitutions.get(j);
				    			  if (p.substring(0,1).equals(Common.DUBLIN_CORE)) {
				    				  if (!p.substring(1,2).equals(Common.UNALTERED) && !p.substring(2).isEmpty()) {
				    					  java.util.List children = root.getChildren(Common.DCMI[j].toLowerCase(), Common.xmlns_dc);
				    					  boolean found = false;
				    					  for ( Object e : children) {				    						  
				    						  if (((Element) e).getText().equals(p.substring(2))) found=true;;
				    					  }
				    					  if (!found) {
				    						  Element dc = new Element(Common.DCMI[j].toLowerCase(), Common.xmlns_dc);
				    						  dc.addContent(p.substring(2));
				    						  root.addContent(dc);
				    					  }
				    				  }
				    			  }
				    		  }
				    		  doc = Common.validate(doc);
				    		  Repository.modifyDatastreamByValue(pid, "DC", "text/xml", outputter.outputString(doc));
				    		  
				    		  
				    		  for (int j = 0; j < substitutions.size(); j++) {
				    			  String p = (String) substitutions.get(j);
				    			  if (p.substring(0,1).equals(Common.OAIPROVIDER)) {
				    				  if (p.substring(1,2).equals(Common.REPLACE)) {
							    		  doc = db.build (Repository.getDatastream(pid, "RELS-EXT"));
			                              Element rdf = doc.getRootElement().getChild("Description", Common.xmlns_rdf);
				    					  if(rdf.getChild("itemID", Common.xmlns_oai) == null && p.substring(2).equals("true")) {
				    						  Element oai = new Element("itemID", Common.xmlns_oai);
				    						  oai.addContent(Common.OAIPHM()+pid);
				    						  rdf.addContent(oai);
								    		  Repository.modifyDatastreamByValue(pid, "RELS-EXT", "text/xml", outputter.outputString(doc));
				    					  }
				    				  }	  
				    			  }
				    		  }
				    		  
	
				    		  for (int j = 0; j < substitutions.size(); j++) {
				    			  String p = (String) substitutions.get(j);
					    		  if (p.substring(0,1).equals(Common.HSSF_LAYOUT)) {
				    				  addReference(pid, "HSSF_STYLESHEET", p);
				    		      }
				    		  }
				    		  
				    		  for (int j = 0; j < substitutions.size(); j++) {
				    			  String p = (String) substitutions.get(j);
					    		  if (p.substring(0,1).equals(Common.HTML_LAYOUT)) {
				    				  addReference(pid, "STYLESHEET", p);
				    		      }
				    		  }
 
				    		  for (int j = 0; j < substitutions.size(); j++) {
				    			  String p = (String) substitutions.get(j);
					    		  if (p.substring(0,1).equals(Common.DC_MAPPING)) {
				    				  addReference(pid, "DC_MAPPING", p);
				    		      }
				    		  }
 
				    		  for (int j = 0; j < substitutions.size(); j++) {
				    			  String p = (String) substitutions.get(j);
					    		  if (p.substring(0,1).equals(Common.RDF_MAPPING)) {
				    				  addReference(pid, "RDF_MAPPING", p);
				    		      }
				    		  }
				    		  for (int j = 0; j < substitutions.size(); j++) {
				    			  String p = (String) substitutions.get(j);
					    		  if (p.substring(0,1).equals(Common.BIBTEX_MAPPING)) {
				    				  addReference(pid, "BIBTEX_MAPPING", p);
					    		  }	  
				    		  }
				    		  for (int j = 0; j < substitutions.size(); j++) {
				    			  String p = (String) substitutions.get(j);
					    		  if (p.substring(0,1).equals(Common.KML_TEMPLATE)) {
				    				  addReference(pid, "KML_TEMPLATE", p);
					    		  }	  			    			  
				    		  }
				    		  for (int j = 0; j < substitutions.size(); j++) {
				    			  String p = (String) substitutions.get(j);
					    		  if (p.substring(0,1).equals(Common.REPLACEMENT_RULESET)) {
				    				  addReference(pid, "REPLACEMENT_RULESET", p);
				    			  }
				    		  }
				    		  for (int j = 0; j < substitutions.size(); j++) {
				    			  String p = (String) substitutions.get(j);
					    		  if (p.substring(0,1).equals(Common.TORDF)) {
				    				  addReference(pid, "TORDF", p);
				    			  }
				    		  }
				    		  for (int j = 0; j < substitutions.size(); j++) {
				    			  String p = (String) substitutions.get(j);
					    		  if (p.substring(0,1).equals(Common.TOMETS)) {
				    				  addReference(pid, "TOMETS", p);
				    			  }
				    			  
				    		  }
 				    		  
				    		  for (int j = 0; j < substitutions.size(); j++) {
				    			  String p = (String) substitutions.get(j);
				    			  if (p.substring(0,1).equals(Common.XSLT)) {
				    				  if (p.substring(1,2).equals(Common.ADD)){	
				    				      if (p.contains("system:add.latex")) {
					    				    	 try { 
					    						  Repository.addDatastream(pid, "LATEX_STYLESHEET",  "Reference to TEI2LaTeX Stylesheet", "text/xml", "http://gams.uni-graz.at/tei/latex/latex.xsl");
					    			  			  } catch (Exception ex) {}
									    				  
				    				      } else if (p.contains("system:add.source")) {
							    				    	 try { 
							    						  Repository.addDatastream(pid, "SOURCE_REF",  "", "text/xml", "http://gams.uni-graz.at/archive/get/"+pid+"/LIDO_SOURCE");
							    			  			  } catch (Exception ex) {}
											    				  
				    					  } else if (p.contains("system:add.handles")) {
				    				    	  try {
				    				    		  
				    				    		  Document lido = db.build (Repository.getDatastream(pid, "LIDO_SOURCE"));
				    				    		  XPath xpath = XPath.newInstance("//lido:recordID[@lido:type='HANDLE']");
				    				    		  xpath.addNamespace(Common.xmlns_lido);
				    				    		  Element handle = (Element) xpath.selectSingleNode( lido );
				    				    		  if (handle != null) {
   			    				    			  try {
				    				    		
				    		 		    		      Handles hdl = (Handles) CServiceProvider.getService( ServiceNames.HANDLESCLASS );
				    								  byte buf[] = new byte[256];
				    								  buf = hdl.getHandleKey();
				    								  
				    								  Resolver resolver = new Resolver();
				    								  AuthenticationInfo   auth = new PublicKeyAuthenticationInfo(Util.encodeString(Common.HANDLE_PREFIX+"11471"), 300, Util.getPrivateKeyFromBytes(buf, 0));					

				    								  if (resolver.checkAuthentication(auth)) {			    			  
				    									  HandleValue object   =  new HandleValue(1  , Util.encodeString("URL"), Util.encodeString("http://gams.uni-graz.at/"+pid));
				    									  HandleValue dc       =  new HandleValue(2  , Util.encodeString("URL.METADATA"), Util.encodeString("http://gams.uni-graz.at/"+pid+"/DC"));
				    									  AdminRecord admin =	new AdminRecord(Util.encodeString(Common.HANDLE_PREFIX+"11471"), 200,
				    				                           false, true, false, false, true, true,			                      
				    				                           true, true, true, true, true, false);
				    									  HandleValue hs_admin =  new HandleValue(100, Util.encodeString("HS_ADMIN"), Encoder.encodeAdminRecord(admin));
				    									  HandleValue[] values = { object, hs_admin, dc };
				    									  AbstractRequest request = new CreateHandleRequest(Util.encodeString(handle.getText().substring(4)), values, auth);
				    									  AbstractResponse response = resolver.getResolver().processRequest(request);
				    									 
				    									  
				    							          if (response.responseCode == AbstractMessage.RC_SUCCESS) { 
				    										  Document rdf = db.build (Repository.getDatastream(pid, "RELS-EXT"));
				    										  Element desc = rdf.getRootElement().getChild("Description", Common.xmlns_rdf);
				    										  Element oai =desc.getChild("itemID", Common.xmlns_oai);
				    										  oai.setText(handle.getText());
				    										  desc.addContent(oai);
				    										  Repository.modifyDatastreamByValue(pid, "RELS-EXT", "text/xml", outputter.outputString(rdf));
				    									  }
				    					     	        }
				    				    			  } catch (Exception ex) {
				    				    				  ex.printStackTrace();
				    				    			  }
				    				    			 }
				    				    	  	} catch (Exception q) {				    				    		  
				    				    	  	}

						    				  } else if (p.contains("system:export.images")) {
						    					  						    					  
						    					  DefaultSortTableModel dm = Repository.listDatastreams(pid,false);					    					  
						    					  for (int k = 0 ; k < dm.getRowCount(); k++) {
						    						  String ID = (String) dm.getValueAt(k, 0);
						    						  String mimetype = (String) dm.getValueAt(k, 2);			
						    						  if (mimetype.contains("image/jpeg") && !ID.equals("THUMBNAIL")) {
						    							  byte [] datastream = Repository.getDatastream(pid, ID, "");
						    							  FileOutputStream out = new FileOutputStream(new File(props.getProperty("user", "import.path")+File.separator+pid.replaceAll("o:","")+"_"+ID+".jpg"));
						    							  IOUtils.write(datastream, out);
						    						  }					    						  					    						  
		  
				    				    	  }
				    				    		  
				    				    		  
/*									    		  doc = db.build (Repository.getDatastream(pid, "RELS-EXT"));
					                              List list = doc.getRootElement().getChild("Description",Common.xmlns_rdf).getChildren("isMemberOf", Common.xmlns_rel);
					                              for (Iterator iter = list.iterator(); iter.hasNext();) {
					            					  Element em = (Element) iter.next();
		                                              String context = em.getAttributeValue("resource", Common.xmlns_rdf);
					                                  if (context.startsWith("info:fedora/context:vase") && !context.startsWith("info:fedora/context:vase.ocm.")  && !context.startsWith("info:fedora/context:vase.pn.")  && !context.startsWith("info:fedora/context:vase.gn.")) {
							    						  Repository.addRelation("info:fedora/"+pid,Common.isMemberOf,"info:fedora/context:vase.ocm."+context.substring(25));
					                                  }	  
					                              }
				    				    	 	  
				    				              Repository.purgeDatastream(pid, "KML_TEMPLATE")  ;			    				    		  
    			    					     	  Repository.modifyDatastream (pid, "", null, "R","http://gams.uni-graz.at/archive/objects/"+pid+"/methods/sdef:Object/getMetadata");
	*/			

						    		      } else if (p.contains("system:fix.mws.bug")) {
		  
						    		    	  			byte[] _stream = Repository.getDatastream(pid,"TEI_SOURCE", "");	  
				    				            		SAXBuilder _builder = new SAXBuilder(); 		
						    				            Document tei = _builder.build(new ByteArrayInputStream(_stream));
						    				
						    				            XPath xpath = XPath.newInstance("//t:biblScope[@type]");
						    				            xpath.addNamespace(Common.xmlns_tei_p5);
						    				            
						    				            List old = xpath.selectNodes(tei);
						    				            
						    				            if (!old.isEmpty()) {
						    				            	try {
						    				            		byte[] _map =  Repository.getDatastream("cirilo:mws", "DC_MAPPING_OLD" , "");
						    								
						    				            		Document _mapping = _builder.build(new ByteArrayInputStream(_map));
						    				            		MDMapper _m = new MDMapper(pid,outputter.outputString(_mapping));
						    									org.jdom.Document _dc = builder.build( new StringReader (_m.transform(tei) ) );							
						    				            		Repository.modifyDatastreamByValue(pid, "DC", "text/xml", outputter.outputString(_dc));
						    				            		log.info("mws bug fix: "+pid+" ... ok");
						    				            	} catch (Exception q) {
						    				            		log.info(q.getMessage());
						    				            	}
						    				            	
						    				            }
						    				            
				    				      } else {
				    					  
				    				    	  try {
				    				    		  Split param = new Split(p.substring(2));
				    				    		  os = new ByteArrayOutputStream();
				    				    		  if (i == 0 ) {
				    				    			  xsltSource = new StreamSource(new File(param.get(0)));
				    				    			  trans = transFact.newTransformer(xsltSource);
				    				    			  trans.setParameter("pid", pid);
				    				    		  }					    			
				    				    		  
				    				    		  DOMSource domSource=new DOMSource(Repository.getDatastream(pid, param.get(1)));
			 	    				    		  trans.transform(domSource, new StreamResult(os));
				    				    		  if (p.substring(1,2).equals(Common.SIMULATE)) {
				    				    			  logger.write("\n"+new String(os.toByteArray(),"UTF-8")+"\n" );
				    				    		  }	  
				    				    		  else if (p.substring(1,2).equals(Common.ADD)) {
				    				    			  Repository.modifyDatastreamByValue(pid, param.get(1), "text/xml", new String(os.toByteArray(),"UTF-8") );
				    				    		  }	  
				    				    	  } catch (Exception q) {}
				    				      }  
				    				  }  
				    			  }  
				    		  }
				    		  				    		  
/*				    		  
  			    		  for (int j = 0; j < substitutions.size(); j++) {
				    			  String p = (String) substitutions.get(j);
				    			  if (p.substring(0,1).equals(Common.XSLT)) {
				    				 
				    				  if (!p.substring(0,1).equals("0")){
				    					  try {
				    					  if (!p.substring(1,2).equals(Common.VOYANT)) { 
				    					    Split param = new Split(p.substring(2));
				    					  	os = new ByteArrayOutputStream();
				    					  	if (i == 0 ) {
				    						  	xsltSource = new StreamSource(new File(param.get(0)));
				    						  	trans = transFact.newTransformer(xsltSource);
				    		                    trans.setParameter("pid", pid);
				    					  	}					    				  
			    						  	DOMSource domSource=new DOMSource(Repository.getDatastream(pid, param.get(1)));
			    						  
			    						  	trans.transform(domSource, new StreamResult(os));
				    					  	if (p.substring(1,2).equals(Common.SIMULATE)) {
				    						  	logger.write("\n"+new String(os.toByteArray(),"UTF-8")+"\n" );
				    					  	}	  
				    					  	else if (p.substring(1,2).equals(Common.ADD)) {
				    						  	Repository.modifyDatastreamByValue(pid, param.get(1), "text/xml", new String(os.toByteArray(),"UTF-8") );
				    					  	}	  
				    					  }
				    					  if ( p.substring(1,2).equals(Common.VOYANT)) {
				    						  Repository.addDatastream(pid, "VOYANT",  "Reference to Voyant Tools", "text/xml", "http://voyant-tools.org?input=http://gams.uni-graz.at/archive/objects/"+pid+"/datastreams/TEI_SOURCE/content");
				    					  }
				    					 } catch (Exception q) {
				    						 log.error(q.getLocalizedMessage(),q);	

				    					 }  
				    				  }
				    			  }
				    			  
				    		  } */
				    		  
				    		  				    		  
				    		  for (int j = 0; j < substitutions.size(); j++) {
				    			  String p = (String) substitutions.get(j);
					    		  if (p.substring(0,1).equals(Common.FO_LAYOUT)) {
				    				  addReference(pid, "FO_STYLESHEET", p);
				    			  }				    			  
				    		  }
				    		  
				    		  for (int j = 0; j < substitutions.size(); j++) {
				    			  String p = (String) substitutions.get(j);
				    			  if (p.substring(0,1).equals(Common.QUERY)) {
				    				  if (p.substring(1,2).equals(Common.REPLACE)) {
				    					 try { 
				    				      File query = File.createTempFile( "tmp", "~" );		
				    				      BufferedWriter out = new BufferedWriter(new FileWriter(query));
				    				      out.write(p.substring(2).replaceAll("[$]self","fedora:"+pid));
				    				      out.close();
			    				    	  Repository.modifyDatastream(pid, "QUERY", "application/sparql-query", "M", query);
				    				      query.delete();
				    					 } catch (Exception q) {}  
				    				      
				    				  }	  
				    			  }
				    		  }

				    		  for (int j = 0; j < substitutions.size(); j++) {
				    			  String p = (String) substitutions.get(j);
				    			  if (p.substring(0,1).equals(Common.OWNER)) {
				    				  if (p.substring(1,2).equals(Common.REPLACE)) {
				    					  try {
				    					  Repository.modifyObject( pid, null, null, p.substring(2));
				    					 } catch (Exception q) {}  
				    				  }	  
				    			  }				    		  
				    		  }
				    		  
                              boolean first=true;
                              hasRelations = false;
                              
				    		  for (int j = 0; j < substitutions.size(); j++) {
				    			  String p = (String) substitutions.get(j);
		    					  try {
				    			  if (p.substring(0,1).equals(Common.RELATIONS)) {
				    				  if (p.substring(1,2).equals(Common.REPLACE) && first) {
			    						  first=false;
							  			  java.util.List  <org.emile.cirilo.ecm.repository.FedoraConnector.Relation>relations = Repository.getRelations(pid,Common.isMemberOf);
							  			  for (Relation r: relations) {
							  				 Repository.purgeRelation("info:fedora/"+pid,Common.isMemberOf, r.getTo());
							  			  }
				    				  }	  
				    				  Split id = new Split(p.substring(2));				    				  
		    						  Repository.addRelation("info:fedora/"+pid,Common.isMemberOf,"info:fedora/"+id.get());
		    						  hasRelations = true;
				    			  }
		    					 } catch (Exception q) {}  
				    		  }
				    		  
				    		  if (hasRelations && !pid.contains("cirilo:")) {
				    			  if (!pid.contains("cirilo:") && model.contains("cm:TEI")) { 
				    				  TEI t = new TEI(null,false,true);
				    				  if (t.get(pid)) {
				    					  t.createRELS_INT(null);
				    				  }	   
				    			  }
				    			  if (!pid.contains("cirilo:") && model.contains("cm:LIDO")) { 
				    				  LIDO l = new LIDO(null,false,true);
				    				  if (l.get(pid)) {
				    					  l.createRELS_INT(null);
				    				  }	   
				    			  }
				    		  }	  
				    		  
			    			  fi++;
							  logger.write( "... ok\n");									                                    	

				    		  try {Thread.sleep(5);} catch (Exception e) {}	
				    		  
				    		  } catch (Exception eq) {
				    		  }
				    		  finally {
								  logger.write( "... error\n");									                                    	
					    		  pd.worked(1);
				    		  }
				    	}	
				    	msgFmt = new MessageFormat(res.getString("objmodsuc"));
		 				Object[] args0 = {new Integer(fi).toString()}; 		    		
		 					JOptionPane.showMessageDialog(  getCoreDialog(), msgFmt.format(args0), Common.WINDOW_HEADER, JOptionPane.INFORMATION_MESSAGE);
						
					}

				    edm.save();

 					
				} catch (Exception ex) {
					log.error(ex.getLocalizedMessage(),ex);	
				}
				finally {
					getCoreDialog().setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
					try {
						logger.close();
					} catch (Exception fe) {
					}
				}
		
			}
		}.start();
		
	}
	
	private void addReference (String pid, String dsid, String p) 
	{
        try {
      	  if (p.substring(1,2).equals(Common.REPLACE)) {
      		  Repository.modifyDatastream (pid, dsid , null, "R", p.substring(2));
      	  }
		} catch (Exception q) {	
			log.error(q.getLocalizedMessage(),q);	

		}
        
       try {
      	  if (p.substring(1,2).equals(Common.ADD)) {
			  Repository.addDatastream(pid, dsid,  "Reference to "+dsid, "text/xml",  p.substring(2));
      	  }
	   } catch (Exception q) {			
			 try {
	      		  Repository.modifyDatastream (pid, dsid , null, "R", p.substring(2));
			 } catch (Exception e) {
				 log.error(e.getLocalizedMessage(),e);	
			 }  
	   }
	}      
	
	public void handleManageHandle(boolean op, String handle, String project, String start, boolean mode)
	throws Exception {
	    final boolean _op = op;
        final String _handle = handle;
        final String _project = project;
        final String _start = start;
	    final boolean _mode = mode;
	    new Thread() {
			public void run() {

				FileWriter	logger = null;
				try {
					  JTable loTable = (JTable) getGuiComposite().getWidget(jtData);
					  
					  int[] selected = loTable.getSelectedRows();
					  
					  int p = _mode ? Integer.parseInt(_start) : 0;
					  
					  ProgressDialog pd = new ProgressDialog(getCoreDialog(), Common.WINDOW_HEADER);
					  pd.displayPercentageInProgressBar = true;
					  pd.beginTask((_op ? res.getString("hdlcreate") :  res.getString("hdldel") ) , selected.length, true);
					  
					  logger = new FileWriter( new File(System.getProperty("user.home")).getAbsolutePath()+System.getProperty( "file.separator" )+"handles.log" );
					 
					  MessageFormat msgFmt = new MessageFormat(res.getString("objmod"));
	 				  Object[] args = {new Integer(selected.length).toString()};
	 					
				      int liChoice = JOptionPane.showConfirmDialog(null, msgFmt.format(args), (_op ? res.getString("hdlcreate") :  res.getString("hdldel") ) , JOptionPane.YES_NO_OPTION,
				    		  						JOptionPane.QUESTION_MESSAGE);

					  					 
				      
			    	  int fi=0;
			    	  int fe=0;
			    	  
				      if (liChoice == 0) {

				    	  DOMBuilder db = new DOMBuilder();
						  Format format = Format.getRawFormat();
						  format.setEncoding("UTF-8");
	 					  XMLOutputter outputter = new XMLOutputter(format);		   			   														
				    	  
 		    		      Handles hdl = (Handles) CServiceProvider.getService( ServiceNames.HANDLESCLASS );
						  byte buf[] = new byte[256];
						  buf = hdl.getHandleKey();
							
						  Resolver resolver = new Resolver();
						  AuthenticationInfo   auth = new PublicKeyAuthenticationInfo(Util.encodeString(Common.HANDLE_PREFIX+_handle), 300, Util.getPrivateKeyFromBytes(buf, 0));					

						  if (!resolver.checkAuthentication(auth)) {
						        throw new HandleException(HandleException.UNABLE_TO_AUTHENTICATE, "Invalid credentials");
						  }
						  
 						  AdminRecord admin =	new AdminRecord(Util.encodeString(Common.HANDLE_PREFIX+_handle), 200,
			                           false, true, false, false, true, true,			                      
			                           true, true, true, true, true, false);
 						  
 						  HandleValue hs_admin = null;
 						  HandleValue object = null;   
 						  HandleValue dc = null;
 						  
 						  if (_op) hs_admin =  new HandleValue(100, Util.encodeString("HS_ADMIN"), Encoder.encodeAdminRecord(admin));
				    	  
 						  String fedora = user.getUrl();						
 						  String host = fedora.substring(0,fedora.lastIndexOf("/"));

 						  
				    	  for (int i=0; i<selected.length; i++) {

				    		  if(pd.isCanceled()) {break;}		

				    		  String pid =(String)loTable.getValueAt(selected[i],0);

				    		  if (!pid.startsWith("o:")) continue;

				    		  try {    						  

								if (_op) {

					    			String id = null;
					    			
									if (_mode) { 
										id = new Integer(p++).toString();
									} else {
										String[] pattern = new String[10];
										Pattern s;
										Matcher m;

										pattern[0] = "o:[a-z]*[.](\\d*)([a-z])$";
										pattern[1] = "o:[a-z]*[.](\\d*)$";
										pattern[2] = "o:[a-z]*[-][A-Z0-9]*[-](\\d*)$";
										pattern[3] = "o:[a-z]*[.][a-z]*[.](\\d*)([a-z])$";
										pattern[4] = "o:[a-z]*[.][a-z]*[.](\\d*)$";
										
										s = Pattern.compile(pattern[0]);
										m = s.matcher(pid);

										if (m.find()) {
											char c = m.group(2).charAt(0);
											id = m.group(1) + "."+ (new Integer(c-96)).toString();
										} else {
											s = Pattern.compile(pattern[1]);
											m = s.matcher(pid);
											if (m.find()) {
												id = m.group(1);
											} else {
												s = Pattern.compile(pattern[2]);
												m = s.matcher(pid);
												if (m.find()) {
													id = m.group(1);
												} else {
													s = Pattern.compile(pattern[3]);
													m = s.matcher(pid);
													if (m.find()) {
														char c = m.group(2).charAt(0);
														id = m.group(1) + "."+ (new Integer(c-96)).toString();
													} else {
														s = Pattern.compile(pattern[4]);
														m = s.matcher(pid);
														if (m.find()) {
															id = m.group(1);
														}
													}	
												}
											}
										}

									}

									Document doc = db.build (Repository.getDatastream(pid, "RELS-EXT"));
		                            Element rdf = doc.getRootElement().getChild("Description", Common.xmlns_rdf);
                                    Element itemID = rdf.getChild("itemID", Common.xmlns_oai);

                                    if (itemID != null && itemID.getText().startsWith("hdl:")) continue;
                                    
									String h = _handle+"/"+_project+"."+id;
									
									PidList list = Repository.query("select $object from <#ri> where $object <http://www.openarchives.org/OAI/2.0/itemID> 'hdl:"+h+"'");        

									if (list.isEmpty()) {
										
										object   =  new HandleValue(1  , Util.encodeString("URL"), Util.encodeString(host+"/"+pid));
										dc       =  new HandleValue(2  , Util.encodeString("URL.METADATA"), Util.encodeString(host+"/"+pid+"/DC"));

										HandleValue[] values = { object, hs_admin, dc };
								
										AbstractRequest request = new CreateHandleRequest(Util.encodeString(h), values, auth);
										AbstractResponse response = resolver.getResolver().processRequest(request);

										if (response.responseCode == AbstractMessage.RC_SUCCESS) {
										
											rdf.removeContent(new ElementFilter("itemID"));			                              
											Element oai = new Element("itemID", Common.xmlns_oai);
											oai.addContent("hdl:"+h);
											rdf.addContent(oai);
											logger.write(new java.util.Date()+ " "  + pid + " :: insert hdl:" + h + "\n");									                                    	
											Repository.modifyDatastreamByValue(pid, "RELS-EXT", "text/xml", outputter.outputString(doc));
											fi++;
										} else {
											logger.write(new java.util.Date() + " "  + pid + " ... no connection to handle server.\n");									                                    	
											fe++;
										}
									} else {
										logger.write(new java.util.Date() + " "  + pid + " :: hdl:" + h + " pre-assigned to "+ list.get(0)+"\n");									                                    	
									}
								} else {
									Document doc = db.build (Repository.getDatastream(pid, "RELS-EXT"));
		                            Element rdf = doc.getRootElement().getChild("Description", Common.xmlns_rdf);
		                            
                                    Element itemID = rdf.getChild("itemID", Common.xmlns_oai);
                                    if (itemID != null) {
                                    	String h = itemID.getText();
                                    	AbstractRequest request = new DeleteHandleRequest(Util.encodeString(itemID.getText().substring(4)), auth);
                                    	AbstractResponse response = resolver.getResolver().processRequest(request);                                    
                                    	if (response.responseCode == AbstractMessage.RC_SUCCESS) {			                            
                                    		rdf.removeContent(new ElementFilter("itemID"));			                              
                                    		Repository.modifyDatastreamByValue(pid, "RELS-EXT", "text/xml", outputter.outputString(doc));
                                    		fi++;
                                    		logger.write(new java.util.Date() + " " + pid + " :: delete " + h+"\n");									                                    	
                                    	} else {
                                    		logger.write(new java.util.Date() + " " + pid + " ... no connection to handle server.\n");									                                    	
                                    		fe++;
                                    	}
                                    }	
								}
				    			  

				    		  try {Thread.sleep(5);} catch (Exception e) {}	
				    		  
				    		  } catch (Exception q) {
				    			 log.error(q.getLocalizedMessage(),q);	

				    		  }
				    		  finally {
					    		  pd.worked(1);
				    		  }
				    	}						
						logger.close();
						
						msgFmt = new MessageFormat(res.getString(_op? "hdlcreated" : "hdldeleted"));
		 				Object[] args0 = {new Integer(fi).toString()};

						JOptionPane.showMessageDialog(  getCoreDialog(), msgFmt.format(args0) );
					}

 					
				} catch (Exception ex) {
					JOptionPane.showMessageDialog(  getCoreDialog(), res.getString("hdlauthfailed") );
				}
				finally {
					getCoreDialog().setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
					try {
						logger.close();
					} catch (Exception fe) {}
				}
		
			}
		}.start();
		
	}
	
	
	/**
	 *  Description of the Method
	 *
	 * @param  e  Description of the Parameter
	 */
	public void handleCancelButton(ActionEvent e) {
		try {
			org.emile.cirilo.dialog.CBoundSerializer.save(this.getCoreDialog(), se.getEditDialogProperties(),(JTable) getGuiComposite().getWidget(jtData));
		} catch (Exception ex) {}		
		close();
	}

	/**
	 *  Description of the Method
	 *
	 * @param  e  Description of the Parameter
	 */
	public void handleDelButton(ActionEvent e) {
			new Thread() {
				public void run() {
				  String last;	
				  try {

					  JTable loTable = (JTable) getGuiComposite().getWidget(jtData);
					  int[] selected = loTable.getSelectedRows();
					  ProgressDialog pd = new ProgressDialog(getCoreDialog(), Common.WINDOW_HEADER);
					  pd.displayPercentageInProgressBar = true;
					  pd.beginTask(res.getString("delete"), selected.length, true);
					  int deleted = 0;
					  
					  MessageFormat msgFmt = new MessageFormat(res.getString("objdel"));
		 			  Object[] args = {new Integer(selected.length).toString()};
				      int liChoice = JOptionPane.showConfirmDialog(null, msgFmt.format(args) ,
				    		  						Common.WINDOW_HEADER, JOptionPane.YES_NO_OPTION,
				    		  						JOptionPane.QUESTION_MESSAGE);
					 
				      if (liChoice == 0) {
				    	  TripleStoreFactory tf = new TripleStoreFactory();
					   	  IIIFFactory i3f = (IIIFFactory) CServiceProvider.getService(ServiceNames.I3F_SERVICE);
				    	  for (int i=0; i<selected.length; i++) {

				    		  if(pd.isCanceled()) {break;}		
				    		  String pid =(String)loTable.getValueAt(selected[i],0);
					    	  if ( pid.startsWith("cirilo:") && !groups.contains("administrator") ) {
					    		  	msgFmt = new MessageFormat(res.getString("nonsysadm"));
					    		  	Object[] argo = {pid};
									JOptionPane.showMessageDialog(  getCoreDialog(),msgFmt.format(argo), Common.WINDOW_HEADER, JOptionPane.INFORMATION_MESSAGE);
						    		continue;
						      }
				    		  String model =(String)loTable.getValueAt(selected[i],2);				    		  
				  			  if (model.contains("Ontology") || model.contains("SKOS") || model.contains("TEI")) {	
				  			    try {
									if (tf.getStatus()) {
										tf.remove(pid);
									}	
			  				     } catch (Exception u) {}

			  				  }	
				  			  i3f.delete(pid);
				    		  if (Repository.purgeObject(pid)) deleted++;
				    		  pd.worked(1);
				    		  try {Thread.sleep(5);} catch (Exception e) {}						    				    
				      		}
							tf.close();

							msgFmt = new MessageFormat(res.getString("objdelsuc"));
			 				Object[] args0 = {new Integer(deleted).toString()};
					       	JOptionPane.showMessageDialog( null, msgFmt.format(args0), Common.WINDOW_HEADER, JOptionPane.INFORMATION_MESSAGE);
				   		}
				    } catch (Exception ex) {log.error(ex.getLocalizedMessage(),ex);	}					  
				}
			}.start();	 
	}

	public void handleExportButton(ActionEvent e) {
		new Thread() {
			public void run() {
			  try {
				  XMLOutputter outputter = new XMLOutputter();
				  
				  JTable loTable = (JTable) getGuiComposite().getWidget(jtData);
				  int[] selected = loTable.getSelectedRows();
				  ProgressDialog pd = new ProgressDialog(getCoreDialog(), Common.WINDOW_HEADER);
				  pd.displayPercentageInProgressBar = true;
				  pd.beginTask(res.getString("export"), selected.length, true);
				  int exported = 0;
				  SAXBuilder builder = new SAXBuilder();

				  MessageFormat msgFmt = new MessageFormat(res.getString("objex"));
	 			  Object[] args = {new Integer(selected.length).toString()};
				  
			      int liChoice = JOptionPane.showConfirmDialog(null, msgFmt.format(args) ,
			    		  res.getString("file.export"), JOptionPane.YES_NO_OPTION,
			    		  						JOptionPane.QUESTION_MESSAGE);
				 
			      if (liChoice == 0) {
					  CPropertyService props = (CPropertyService) CServiceProvider.getService(ServiceNames.PROPERTIES);
					  JFileChooser chooser = new JFileChooser(props.getProperty("user", "export.path"));
					  chooser.setDialogTitle(res.getString("chooseedir"));
					  chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);

					  if (chooser.showDialog(getCoreDialog(), res.getString("choose")) != JFileChooser.APPROVE_OPTION) {
						  return;
					  }
					  props.setProperty("user", "export.path", chooser.getSelectedFile().getAbsolutePath());
					  props.saveProperties("user");
					  File fp = chooser.getSelectedFile();
	    	  
					  String fedora = user.getUrl();						
					  String host = fedora.substring(0,fedora.lastIndexOf("/"));
					  String cocoon = host+"/cocoon";
			
					  
			    	  for (int i=0; i<selected.length; i++) {
			    		  if(pd.isCanceled()) {break;}		
			    		  String pid =(String)loTable.getValueAt(selected[i],0);	
			    		  try {
			    	          FileOutputStream fos = new FileOutputStream(fp.getAbsolutePath()+System.getProperty( "file.separator" )+pid.replace(":",".")+".xml" );
							  BufferedWriter out = new BufferedWriter(new OutputStreamWriter( fos, "UTF-8" ) );
							  String ec =  props.getProperty("user", "General.ExportContext");
							  ec = (ec == null || ec.isEmpty() ? "Archive" : ec); 
  				    	      Document foxml = builder.build(new StringReader(new String(Repository.get2ObjectXml(pid,ec))));
							  
			  	    		  try {				  	    			
			  	    			  	List datastreams;									
			  	    				XPath xpath = XPath.newInstance("//foxml:datastream/foxml:datastreamVersion[contains('"+Common.TEXT_MIMETYPES+"',@MIMETYPE)]");
			  	    				xpath.addNamespace( Common.xmlns_foxml );
			  	    				datastreams = (List) xpath.selectNodes(foxml);
			  	    				for (Iterator iter = datastreams.iterator(); iter.hasNext();) {
			  	    					org.jdom.Element e = (org.jdom.Element) iter.next();
			  	    					Element bc = e.getChild("binaryContent",Common.xmlns_foxml);
			  	    					byte[] byteArray = Base64.decodeBase64(bc.getValue().getBytes());
			  	    					String buf = new String(byteArray).replaceAll(fedora, "http://fedora.host/fedora")
										       .replaceAll(cocoon, "http://fedora.host/cocoon")
										       .replaceAll(host, "http://fedora.host")
											   .replaceAll("http://fedora.host#", "http://gams.uni-graz.at#")
											   .replaceAll("http://fedora.host/viewer", "http://gams.uni-graz.at/viewer")
											    .replaceAll("http://fedora.host/ontology","http://gams.uni-graz.at/ontology");
			  	    					
			  	    					byteArray = Base64.encodeBase64(buf.getBytes());
			  	    					bc.setText(new String(byteArray));
			  	    					e.setAttribute("SIZE",new Integer(byteArray.length).toString());
			  	    				}
			  	    		  } catch (Exception eq) {}

							  String buf = outputter.outputString(foxml);			  	    		  
							  out.write(buf.replaceAll(fedora, "http://fedora.host/fedora")
							        .replaceAll(cocoon, "http://fedora.host/cocoon")
							        .replaceAll(host, "http://fedora.host")
									.replaceAll("http://fedora.host#", "http://gams.uni-graz.at#")
								    .replaceAll("http://fedora.host/viewer", "http://gams.uni-graz.at/viewer")
									.replaceAll("http://fedora.host/ontology","http://gams.uni-graz.at/ontology"));
							  out.close();
			    			  
			    			  exported++;
			    		  } catch (Exception ex) {
					       	  JOptionPane.showMessageDialog( null,  ex.getLocalizedMessage(), Common.WINDOW_HEADER, JOptionPane.INFORMATION_MESSAGE);
			    		  }	  
			    		  pd.worked(1);
			    		  try {Thread.sleep(5);} catch (Exception eq) {			    			 
			    		  }						    				    
			      		}
			    	   msgFmt = new MessageFormat(res.getString("objexsuc"));
	 					Object[] args0 = {new Integer(exported).toString()}; 		    		
	 					JOptionPane.showMessageDialog( getCoreDialog(), msgFmt.format(args0), Common.WINDOW_HEADER, JOptionPane.INFORMATION_MESSAGE);
				       	
			   		}
			    } catch (Exception ex) {
			    	log.error(ex.getLocalizedMessage(),ex);	
			    }					  
			}
		}.start();	 
	}

	public void handleEditButton(ActionEvent e) {
		  try {
				
			  getCoreDialog().setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));


			  JTable loTable = (JTable) getGuiComposite().getWidget(jtData);
			  int[] selected = loTable.getSelectedRows();
			  String pid =(String)loTable.getValueAt(selected[0],0);	
			  String label =(String)loTable.getValueAt(selected[0],1);	
			  String model =(String)loTable.getValueAt(selected[0],2);	
			  String owner =(String)loTable.getValueAt(selected[0],4);	
      	  
			  ObjectEditorDialog dlg = (ObjectEditorDialog) CServiceProvider.getService(DialogNames.OBJECTEDITOR_DIALOG);
			  dlg.set(pid, label, model, owner);
			  dlg.open();
			  
		    } catch (Exception ex) {}					  
			finally {
				getCoreDialog().setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
			}
	}
	
	public void handleHandleButton(ActionEvent e) {
		
		try {
			HandleDialog dlg;

			dlg = (HandleDialog) CServiceProvider.getService(DialogNames.HANDLE_DIALOG);
			dlg.setParent(this);
			dlg.open();
		} catch (Exception ex) {
		}

	}
	

	
	public void handleMouseDoubleClick(MouseEvent e, int type) {

		try {

			CEventListener.setBlocked(true);
			// no events while handling

			if (type == MouseEvent.MOUSE_CLICKED) {
				if (e.getClickCount() >= 2) {
					handleEditButton(null);
				}
			}
		} catch (Exception ex) {
		} finally {
			CEventListener.setBlocked(false);
		}

	}	
	
	public void handleNewButton(ActionEvent e) {
		
		try {
			NewObjectDialog dlg;

			dlg = (NewObjectDialog) CServiceProvider.getService(DialogNames.NEWOBJECT_DIALOG);
			dlg.set(this,null,user.getUser());
			dlg.open();
		} catch (Exception ex) {
		}

	}
	
	public void handleReplaceButton(ActionEvent e) {
		
		try {
			ReplaceDialog dlg;

			dlg = (ReplaceDialog) CServiceProvider.getService(DialogNames.REPLACE_DIALOG);
			dlg.setParent(this);
			dlg.open();
		} catch (Exception ex) {
		}

	}			

	public void handleRefreshButton(ActionEvent e) {
		
		try {
			refresh();
		} catch (Exception ex) {
		}

	}			
	

	public void handleModelButton(ActionEvent e) {
		
		try {	
		  JTable loTable = (JTable) getGuiComposite().getWidget(jtData);
		  int[] selected = loTable.getSelectedRows();
		  String pid =(String)loTable.getValueAt(selected[0],0);	
		  Repository.addRelation("info:fedora/"+pid,"http://ecm.sourceforge.net/relations/0/2/#isTemplateFor","info:fedora/cm:DefaultContentModel");
		} catch (Exception ex) {
			log.error(ex.getLocalizedMessage(),ex);	
		}

	}			
	
	
	public void handleValidateButton(ActionEvent e) 
			throws Exception {
			new Thread() {
				public void run() {

					int fi = 0;

					try {
						JTable loTable = (JTable) getGuiComposite().getWidget(jtData);
						int[] selected = loTable.getSelectedRows();
						ProgressDialog progressDialog = new ProgressDialog(getCoreDialog(), Common.WINDOW_HEADER);
						progressDialog.displayPercentageInProgressBar = true;
						progressDialog.beginTask(res.getString("addcont"), selected.length, true);
						
						MessageFormat msgFmt = new MessageFormat(res.getString("objmod"));
	 					Object[] args = {new Integer(selected.length).toString()};
	 					
  				        int liChoice = JOptionPane.showConfirmDialog(null, msgFmt.format(args) ,
  				        		res.getString("geo"), JOptionPane.YES_NO_OPTION,
					    		  						JOptionPane.QUESTION_MESSAGE);

 				       	Document pelagios;
 				       	Document cmif;
 				       	Document kml;

 				       	DOMBuilder db = new DOMBuilder();
 				       	

						if (liChoice == 0) {

							getCoreDialog().setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));

							progressDialog.worked(1);
							
							AggregatorFactory af = new AggregatorFactory();
							
 				    	    for (int i=0; i<selected.length; i++) 
 				    	    {
					    		String pid =(String)loTable.getValueAt(selected[i],0);
					    		String title =(String)loTable.getValueAt(selected[i],1);
					    		String cm =(String)loTable.getValueAt(selected[i],2);
					    		String owner = (String)loTable.getValueAt(selected[i],4);
					    		
					    		try {
					    			
					    			if (cm.equals("cm:Context")) {
					    				if (Repository.exists(pid, "KML")) {					    					
					  						byte[] stylesheet = null;
					 				       	try {
					 				       		stylesheet =  Repository.getDatastream("cirilo:"+owner, "KML_STYLESHEET" , "");
					  				       	} catch (Exception ex) {
					  				       		stylesheet =  Repository.getDatastream("cirilo:Backbone", "KML_STYLESHEET" , "");
					  				        }
					  				        Transformer transformer = TransformerFactory.newInstance().newTransformer(new StreamSource(new StringReader(new String(stylesheet, "UTF-8"))));					    					
							    			try {
					 							kml = db.build (Repository.getDatastream("cirilo:"+owner, "KML_TEMPLATE"));
					 						} catch (Exception ex) {
					 							kml = db.build (Repository.getDatastream("cirilo:Backbone", "KML_TEMPLATE"));
					 						}
					    					transformer.setParameter("context", pid);
					    					af.aggregateKML(pid, title, kml, transformer);
					    				}
					    				if (Repository.exists(pid, "PELAGIOS")) {					    					
					  						byte[] stylesheet = null;
					 				       	try {
					 				       		stylesheet =  Repository.getDatastream("cirilo:"+owner, "PELAGIOS_STYLESHEET" , "");
					  				       	} catch (Exception ex) {
					  				       		stylesheet =  Repository.getDatastream("cirilo:Backbone", "PELAGIOS_STYLESHEET" , "");
					  				        }
					  				        Transformer transformer = TransformerFactory.newInstance().newTransformer(new StreamSource(new StringReader(new String(stylesheet, "UTF-8"))));					    					
							    			try {
					 							pelagios = db.build (Repository.getDatastream("cirilo:"+owner, "PELAGIOS_TEMPLATE"));
					 						} catch (Exception ex) {
					 							pelagios = db.build (Repository.getDatastream("cirilo:Backbone", "PELAGIOS_TEMPLATE"));
					 						}
					    					transformer.setParameter("context", pid);
					    					af.aggregatePELAGIOS(pid, title, pelagios, transformer);
					    				}
					    				if (Repository.exists(pid, "CMIF")) {					    					
					  						byte[] stylesheet = null;
					 				       	try {
					 				       		stylesheet =  Repository.getDatastream("cirilo:"+owner, "CMIF_STYLESHEET" , "");
					  				       	} catch (Exception ex) {
					  				       		stylesheet =  Repository.getDatastream("cirilo:Backbone", "CMIF_STYLESHEET" , "");
					  				        }
					  				        Transformer transformer = TransformerFactory.newInstance().newTransformer(new StreamSource(new StringReader(new String(stylesheet, "UTF-8"))));					    					
							    			try {
					 							cmif = db.build (Repository.getDatastream("cirilo:"+owner, "CMIF_TEMPLATE"));
					 						} catch (Exception ex) {
					 							cmif = db.build (Repository.getDatastream("cirilo:Backbone", "CMIF_TEMPLATE"));
					 						}
					    					transformer.setParameter("context", pid);
					    					af.aggregateCMIF(pid, title, cmif, transformer);
					    				}
					    				fi++;
					    			}
					    		} catch (Exception r) {}

								if(progressDialog.isCanceled()) {
									// break;
								}				

								progressDialog.worked(1);
	 															
								try {
									Thread.sleep(50); 
								} catch (InterruptedException e) {
								}
									
 				    	    }
 				    	   msgFmt = new MessageFormat(res.getString("geosuc"));
 		 				   Object[] args1 = {new Integer(fi).toString()}; 		    		
 		 				   JOptionPane.showMessageDialog(  getCoreDialog(), msgFmt.format(args1), Common.WINDOW_HEADER, JOptionPane.INFORMATION_MESSAGE);
 							
						}
						
					} catch (Exception ex) {
						try {
						} catch (Exception ez) {}
					}
					finally {
						getCoreDialog().setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
					}
			
				}
			}.start();
			
		}
	
		
	
	public void handleSeekButton(ActionEvent e) {

		try {
			    seekString =  (String) ((JTextField) getGuiComposite().getWidget("jtfSeek")).getText().trim();
			    props.setProperty("user", "objects.seekterm", seekString);
				props.saveProperties("user");
			    refresh();		   
			   
		    } catch (Exception ex) {}					  
	}			
	
	public void handleMakeEnvironmentButton(ActionEvent e) {

		try {
			MakeEnvironmentDialog loDlg;


			loDlg = (MakeEnvironmentDialog) CServiceProvider.getService(DialogNames.MAKEENVIRONMENT_DIALOG);
			loDlg.open();
			
			if (loDlg.isOK()) {
				try {
						getCoreDialog().setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
					
						TemplateSubsystem temps = (TemplateSubsystem) CServiceProvider.getService(ServiceNames.TEMPLATESUBSYSTEM);
						temps.makeTemplate("cirilo:TEI", loDlg.getUser(), "$cirilo:TEI."+loDlg.getUser(), "Untitled", "info:fedora/cm:TEI");
						temps.makeTemplate("cirilo:LIDO", loDlg.getUser(), "$cirilo:LIDO."+loDlg.getUser(), "Untitled", "info:fedora/cm:LIDO");
						temps.makeTemplate("cirilo:Context", loDlg.getUser(), "$cirilo:Context."+loDlg.getUser(), "Untitled", "info:fedora/cm:Content");
						temps.makeTemplate("cirilo:OAIRecord", loDlg.getUser(), "$cirilo:OAIRecord."+loDlg.getUser(), "Untitled", "info:fedora/cm:OAIRecord");
						temps.makeTemplate("cirilo:Environment", loDlg.getUser(), "$cirilo:"+loDlg.getUser(), "Untitled", "");
				} catch (Exception ex) {
					log.error(ex.getLocalizedMessage(),ex);	
				}
				finally {
					getCoreDialog().setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
				    MessageFormat msgFmt = new MessageFormat(res.getString("envok"));
				    Object[] args = {loDlg.getUser()};
					JOptionPane.showMessageDialog( getCoreDialog(), msgFmt.format(args) , Common.WINDOW_HEADER, JOptionPane.INFORMATION_MESSAGE );

				}

			}

		}
		catch (Exception ex) {
			CException.record(ex, this);
		}
	}

	
	public void handleAddModel() throws Exception  {

		new Thread() {
			public void run() {

				try {
						
						 String qu ="select distinct ?pid ?title ?model ?lastModifiedDate ?user "+
							       "where { ?object  <dc:title> ?title ."+
							                  " ?object  <dc:identifier> ?pid ."+
							                   "?object <fedora-view:lastModifiedDate> ?lastModifiedDate ."+
							                   "?object <info:fedora/fedora-system:def/model#ownerId> ?user ."+
							                   "optional {?object <info:fedora/fedora-system:def/model#hasModel> ?model .  filter (!regex(str(?model), '^info:fedora/fedora-system:') && regex(str(?model), '^null') && !regex(str(?pid), '^fedora-system:') ) } . " +
							         "} order by ?pid";

			 			 String[] columnNames = {res.getString("pid"),
				                   res.getString("title"),
				                   res.getString("contentmodel"),
				                   res.getString("lastupdate"),
				                   res.getString("owner") };
			 			 
				         DefaultSortTableModel dm= Repository.getObjects(qu,columnNames);

				         DOMBuilder db = new DOMBuilder();
						 Format	format = Format.getRawFormat();
						 format.setEncoding("UTF-8");
						 XMLOutputter 	outputter = new XMLOutputter(format);

				         int objects = 0;
				         
						ProgressDialog progressDialog = new ProgressDialog( getCoreDialog(), Common.WINDOW_HEADER);
						progressDialog.displayPercentageInProgressBar = true;
						progressDialog.millisToDecideToPopup = 1;
						progressDialog.millisToPopup = 1;
						progressDialog.beginTask(res.getString("addrel"),dm.getRowCount(), true);
											
						getCoreDialog().setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
		         
				         Element rdf = new Element("RDF",Common.xmlns_rdf);
				         Element description = new Element("Description",Common.xmlns_rdf);
				         Attribute about = new Attribute("about", "", Attribute.CDATA_TYPE, Common.xmlns_rdf);
				         Element hasmodel = new Element("hasModel", Common.xmlns_model);
				         Attribute resource = new Attribute("resource", "info:fedora/cm:DefaultContentModel", Attribute.CDATA_TYPE, Common.xmlns_rdf);
				         hasmodel.setAttribute(resource);
				         description.setAttribute(about);
				         description.addContent(hasmodel);
				         rdf.addContent(description);
				    	 
			       	     File tmp = File.createTempFile("temp", ".tmp");	    					
						 
 		       	         progressDialog.worked(1);
				        			       	     			       	     
				         for (int i=0; i<dm.getRowCount(); i++) {
				 				String pid = (String) dm.getValueAt(i,0);

				 				if(progressDialog.isCanceled()) {
									break;
								}
								
				       	       progressDialog.worked(1);
								
								try {
									Thread.sleep(50); 
								} catch (InterruptedException e) {
								}
								
				 				try {
				 					Document doc = db.build (Repository.getDatastream(pid, "RELS-EXT"));
				 				} catch (Exception ex) {
				 					about.setValue("info:fedora/"+pid);
							    	try {				    		
				    					byte[] data = (byte[]) new String (outputter.outputString(rdf)).getBytes("UTF-8");   					
				    					FileOutputStream fos = new FileOutputStream(tmp);
				    					fos.write(data);
				    					fos.flush();
				    					fos.close();	    					
							    		Repository.addDatastream(pid, "RELS-EXT", "", "X", "text/xml", tmp);
							    	} catch (Exception q ){
							    	}	
							    	objects++;	 					
				 				}
				 			}
						    tmp.delete();
						    
						    MessageFormat msgFmt = new MessageFormat(res.getString("relsuc"));
						    Object[] args = {new Integer(objects).toString() };
							JOptionPane.showMessageDialog( getCoreDialog(), msgFmt.format(args) , Common.WINDOW_HEADER, JOptionPane.INFORMATION_MESSAGE );
				         
				} catch (Exception ex) {
				}
				finally {
					getCoreDialog().setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
				}
			}
		}.start();
	}


	
	
	public void refresh() {
		try {			
			getCoreDialog().setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));


			user = (User) CServiceProvider.getService(ServiceNames.CURRENT_USER);
			groups = (ArrayList) CServiceProvider.getService(ServiceNames.MEMBER_LIST);
            if (seekString.startsWith("add:RELS-EXT")) {
            	handleAddModel();
            } else {	             
            	 String seek = seekString.replace("*","");
 			     String qu ="select distinct ?pid ?title ?model ?lastModifiedDate ?user ?hdl "+
        				       "where { ?object  <dc:title> ?title ."+
          				                  " ?object  <dc:identifier> ?pid ."+
          				                   "?object <fedora-view:lastModifiedDate> ?lastModifiedDate ."+
          				                   "?object <info:fedora/fedora-system:def/model#ownerId> ?user ."+
          				                   "optional {?object <info:fedora/fedora-system:def/model#hasModel> ?model } . " +
         				                   "optional {?object <http://www.openarchives.org/OAI/2.0/itemID> ?hdl } . " +
          				                 "filter (" +
          				                 (!groups.contains("administrator") ? " (?user = '"+user.getUser()+"' || ?user='public')  && "  : "") +	
          				                 "(!regex(str(?model), '^info:fedora/fedora-system:') )" +
        				                   (!seek.isEmpty() ?
        		 	          				       "&& ( regex(?title,'"+seek+"','i') || regex(str(?model),'"+seek+"','i')  || regex(?pid,'"+seek+"','i')  || regex(?user,'"+seek+"','i') )" : "")+
        		 	          				       (!groups.contains("administrator") ?  "&&  (!regex(str(?pid), '^cirilo:') || ?user = '"+user.getUser()+"')  && !regex(str(?pid), '^ini:') && !regex(str(?pid), '^tcm:')"  : "")+
          				                 "  )"+
		                "} order by ?pid";
		

			
			JTable tb = (JTable) getGuiComposite().getWidget(jtData);
			int row = tb.getSelectedRow();
 	 	    String[] columnNames = {res.getString("pid"),
	                   res.getString("title"),
	                   res.getString("contentmodel"),
	                   res.getString("lastupdate"),
	                   res.getString("owner"),
	                   "Handles",
	                   };
			
			DefaultSortTableModel dm= Repository.getObjects(qu, columnNames);
			tb.setModel(dm);
			tb. setShowHorizontalLines(false);
			if (dm.getRowCount() > 0) {
				tb.setRowSelectionInterval(0,0);
			}
            }
		} catch (Exception e) {			
		}
		finally {
			getCoreDialog().setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
		}
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
			seekString = props.getProperty("user", "objects.seekterm");
			seekString = seekString == null ? "*" : seekString;
			moGA.setData("jtfSeek", seekString);
			refresh();
			JTable loTable = (JTable) getGuiComposite().getWidget(jtData);
			se = (Session) CServiceProvider.getService( ServiceNames.SESSIONCLASS );						
			org.emile.cirilo.dialog.CBoundSerializer.load(this.getCoreDialog(), se.getEditDialogProperties(), loTable);

		} catch (Exception e){}
	}		
	/**
	 *  Description of the Method
	 *
	 * @exception  COpenFailedException  Description of the Exception
	 */
	protected void opened() throws COpenFailedException {

		try {
			
			 res =(ResourceBundle) CServiceProvider.getService(ServiceNames.RESOURCES);
   			 props = (CPropertyService) CServiceProvider.getService(ServiceNames.PROPERTIES);			
		
			 moGA = (CDefaultGuiAdapter)getGuiAdapter();						
			 CDialogTools.createButtonListener(this, "jbClose", "handleCancelButton");
			 CDialogTools.createButtonListener(this, "jbDel", "handleDelButton");
			 CDialogTools.createButtonListener(this, "jbExport", "handleExportButton");
			 CDialogTools.createButtonListener(this, "jbReplace", "handleReplaceButton");
			 CDialogTools.createButtonListener(this, "jbAddGeo", "handleValidateButton");
			 CDialogTools.createButtonListener(this, "jbRefresh", "handleRefreshButton");
			 CDialogTools.createButtonListener(this, "jbEdit", "handleEditButton");
			 CDialogTools.createButtonListener(this, "jbNew", "handleNewButton");
			 CDialogTools.createButtonListener(this, "jbSeek", "handleSeekButton");
			 CDialogTools.createButtonListener(this, "jbManage", "handleHandleButton");
			 
			 user = (User) CServiceProvider.getService(ServiceNames.CURRENT_USER);
			 groups = (ArrayList) CServiceProvider.getService(ServiceNames.MEMBER_LIST);

  			 ((JButton) getGuiComposite().getWidget("jbManage")).setEnabled(groups.contains("administrator"));
  			           
 			 popup = new JPopupMenu();
		     JMenuItem mi;
			 mi = new JMenuItem(res.getString("refresh"));
		     mi.addActionListener(new ActionListener() {
		    	  public void actionPerformed(ActionEvent e) {
		    		  handleRefreshButton(null);
		    	  }
		      });	      
		     popup.add(mi);
		     popup.add(new JSeparator());
 			 mi = new JMenuItem(res.getString("edit"));
		     mi.addActionListener(new ActionListener() {
		    	  public void actionPerformed(ActionEvent e) {
		    		  handleEditButton(null);
		    	  }
		     });	      
		     
		     popup.add(mi);
			 mi = new JMenuItem(res.getString("delete"));
		     mi.addActionListener(new ActionListener() {
		    	  public void actionPerformed(ActionEvent e) {
		    		  handleDelButton(null);
		    	  }
		     });	      
		     popup.add(mi);
		     popup.add(new JSeparator());
			 mi = new JMenuItem(res.getString("new"));
		     mi.addActionListener(new ActionListener() {
		    	  public void actionPerformed(ActionEvent e) {
		    		  handleNewButton(null);
		    	  }
		      });	      
		     popup.add(mi);
		     popup.add(new JSeparator());
			 mi = new JMenuItem(res.getString("replace"));
		     mi.addActionListener(new ActionListener() {
		    	  public void actionPerformed(ActionEvent e) {
		    		  handleReplaceButton(null);
		    	  }
		      });	      
		     popup.add(mi);
			 mi = new JMenuItem(res.getString("export"));
		     mi.addActionListener(new ActionListener() {
		    	  public void actionPerformed(ActionEvent e) {
		    		  handleExportButton(null);
		    	  }
		      });	      
		     
		     popup.add(mi);
/*		     
		     popup.add(new JSeparator());
			 mi = new JMenuItem(res.getString("maketemplate"));
		     mi.addActionListener(new ActionListener() {
		    	  public void actionPerformed(ActionEvent e) {
		    		  handleModelButton(null);
		    	  }
		      });	      
		     popup.add(mi);
*/		     
/*		     
 			 mi = new JMenuItem(res.getString("makeenvironment"));
			    mi.addActionListener(new ActionListener() {
			    	  public void actionPerformed(ActionEvent e) {
			    		  handleMakeEnvironmentButton(null);
			    	  }
			      });	      
			    popup.add(mi);
*/
		    
		     PopupListener popupListener = new PopupListener();
 			 JTable tb = (JTable) getGuiComposite().getWidget(jtData);
	         new CMouseListener(tb, this, "handleMouseDoubleClick");
		     tb.addMouseListener(popupListener);
		     
			tb.addKeyListener(new KeyAdapter() {
				public void keyPressed(KeyEvent e) {
			         if (e.getKeyCode() == 82 &&  KeyEvent.getKeyModifiersText(e.getModifiers()).equals("Strg")) { 
			        	 refresh();
						handleEditButton(null);
			         }	
			    }
			 });

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

             
		} catch (Exception ex) {
			throw new COpenFailedException(ex);
		}
	}
	
	  
	  class PopupListener extends MouseAdapter {
		    public void mousePressed(MouseEvent e) {
		      showPopup(e);
		    }
		    public void mouseReleased(MouseEvent e) {
		      showPopup(e);
		    }
		    private void showPopup(MouseEvent e) {
		      if (e.isPopupTrigger()) {
		        popup.show(e.getComponent(), e.getX(), e.getY());
		      }
		    }
		  }

	  
	private JPopupMenu popup;	
	private CDefaultGuiAdapter moGA;
	private CPropertyService props;
	private User user;
	private ArrayList<String> groups;
	private Session se;
	private final String jtData ="jtData";
	private ResourceBundle res;
	private String seekString;

	
}

