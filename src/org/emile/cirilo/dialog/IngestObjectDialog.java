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
import voodoosoft.jroots.dialog.*;

import org.emile.cirilo.Common;
import org.emile.cirilo.ServiceNames;
import org.emile.cirilo.ecm.templates.*;
import org.emile.cirilo.ecm.repository.*;
import org.emile.cirilo.utils.Split;
import org.emile.cirilo.business.*;
import org.emile.cirilo.*;

import com.asprise.util.ui.progress.ProgressDialog;

import org.apache.log4j.Logger;

import java.awt.Cursor;
import java.awt.event.*;

import javax.swing.*;

import org.jdom.Document;
import org.jdom.Element;
import org.jdom.Attribute; 
import org.jdom.Namespace;
import org.jdom.input.SAXBuilder;
import org.jdom.output.Format;
import org.jdom.output.XMLOutputter;
import org.jdom.xpath.XPath;

import java.text.MessageFormat;
import java.util.*;
import java.io.*;
import java.awt.Color;

import org.xmldb.api.base.*;
import org.xmldb.api.*;



/**
 *  Description of the Class
 *
 * @author     Johannes Stigler
 * @created    10.3.2011
 */
public class IngestObjectDialog extends CDialog {

	private static Logger log = Logger.getLogger(IngestObjectDialog.class);
	
	/**
	 *  Constructor for the LoginDialog object
	 */

	public IngestObjectDialog() {}

	
	/**
	 *  Description of the Method
	 *
	 * @param  e  Description of the Parameter
	 */
	public void handleCancelButton(ActionEvent e) {
		org.emile.cirilo.dialog.CBoundSerializer.save(this.getCoreDialog(), se.getIngestDialogProperties(), (JTable) null);   
		close();
	}
	
	/**
	 *  Description of the Method
	 *
	 * @param  e  Description of the Parameter
	 */
	public void handleResetButton(ActionEvent e) {
		try {
			JCheckBox jcbGenerated = ((JCheckBox) getGuiComposite().getWidget("jcbGenerated"));
			jcbGenerated.setSelected(false);
			JComboBox jcbContentModel = ((JComboBox) getGuiComposite().getWidget("jcbContentModel"));
			jcbContentModel.setSelectedIndex(0);
			set();
			
			new DCMI().reset(moGA);
			
		} catch (Exception ex) {			
		}
			
	}

	/**
	 *  Description of the Method
	 *
	 * @param  e  Description of the Parameter
	 */
	 public void handleCBGenerated(ActionEvent e) {
		try {
			set();
		} catch (Exception ex) {			
		}
	}

	 
	private void set() {
		try {
			JCheckBox jcbGenerated = ((JCheckBox) getGuiComposite().getWidget("jcbGenerated"));
			JTextField jtfPID = ((JTextField) getGuiComposite().getWidget("jtfPID"));
			JTextField jtfTitle = ((JTextField) getGuiComposite().getWidget("jtfTitle"));
			JComboBox jcbNamespace = ((JComboBox) getGuiComposite().getWidget("jcbNamespace"));
			JComboBox jcbUser = ((JComboBox) getGuiComposite().getWidget("jcbUser"));
			JComboBox jcbContentModel = ((JComboBox) getGuiComposite().getWidget("jcbContentModel"));			
			
			jcbUser.setSelectedItem(user.getUser());
			jcbNamespace.setEnabled(false);			
		
			String cm = jcbContentModel.getSelectedItem().toString().toLowerCase();
			
			JButton jbEXCEL = ((JButton) getGuiComposite().getWidget("jbEXCEL"));
			jbEXCEL.setEnabled(!(cm.contains("cirilo:mei") || cm.contains("cirilo:ontology")));
			
			if (cm.contains("context")) {
				jcbNamespace.setSelectedIndex(1);
			} else {
				jcbNamespace.setSelectedIndex(0);				
			}
			
			if (!jcbGenerated.isSelected()) {
				jtfPID.setBackground( new Color (238,238,238) );
				jtfPID.setText("");
				jtfPID.setEnabled(false);
				jtfTitle.requestFocus();
			} else {
				jtfPID.setBackground( Color.YELLOW );
				jtfPID.setEnabled(true);
				jtfPID.requestFocus();
			}
		} catch (Exception ex) {		
		}
		
	}
	
	public void handleCMComboBox(ItemEvent e)
	throws Exception {

		if (e.getStateChange() == 1) {
			set(); 
		}
	}

	public void handleShowLogfileButton(ActionEvent e) 
	throws Exception {
		TextEditor dlg = (TextEditor) CServiceProvider.getService(DialogNames.TEXTEDITOR);
		dlg.set(logfile, null, "text/log", "R", null, null, null);
		dlg.open();
	}	


	
	public void handleIngestFromFilesystemButton(ActionEvent e) 
		throws Exception {
		new Thread() {
			public void run() {
				
				FileWriter logger = null;
				String model = "";
				
				try {
					CPropertyService props = (CPropertyService) CServiceProvider.getService(ServiceNames.PROPERTIES);
					
					TemplateSubsystem temps = (TemplateSubsystem) CServiceProvider.getService(ServiceNames.TEMPLATESUBSYSTEM);
					JComboBox jcbContentModel = ((JComboBox) getGuiComposite().getWidget("jcbContentModel"));            
					JComboBox jcbNamespace = ((JComboBox) getGuiComposite().getWidget("jcbNamespace"));
					JComboBox jcbUser = ((JComboBox) getGuiComposite().getWidget("jcbUser"));
					model = (String)jcbContentModel.getSelectedItem();
										
					JFileChooser chooser = new JFileChooser(props.getProperty("user", "ingest.import.path"));

					chooser.setDialogTitle(res.getString("chooseidir"));
					chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
			
					if (chooser.showDialog(getCoreDialog(), res.getString("choose")) != JFileChooser.APPROVE_OPTION) {
						return;
					}

					props.setProperty("user", "ingest.import.path", chooser.getCurrentDirectory().getAbsolutePath());
					props.saveProperties("user");
					
					File fp = chooser.getSelectedFile();

					logger = new FileWriter( fp.getAbsolutePath()+System.getProperty( "file.separator" )+"ingest.log" );
					
					getCoreDialog().setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
					
					rdfs = new RDF();
					
					files = new ArrayList<String>();
					pids = new ArrayList<String>();

					treeWalk(fp,true, model);
					if (model.contains("Ontology")) {
						pids = rdfs.getPidList();
					}	
					
					MessageFormat msgFmt = new MessageFormat(res.getString("objcrea"));
					Object[] args = {new Integer( pids.size() == 0 ? files.size() : pids.size() ).toString(), model, fp.getAbsolutePath()};
		            
					int liChoice = JOptionPane.showConfirmDialog(null, msgFmt.format(args) ,
							Common.WINDOW_HEADER, JOptionPane.YES_NO_OPTION,
							JOptionPane.QUESTION_MESSAGE);

					if (liChoice == 0) {
						
					    props.setProperty("user", "Ingest.ContentModel", jcbContentModel.getSelectedItem().toString());

						ProgressDialog progressDialog = new ProgressDialog( getCoreDialog(), Common.WINDOW_HEADER);
						progressDialog.displayPercentageInProgressBar = true;
						progressDialog.millisToDecideToPopup = 1;
						progressDialog.millisToPopup = 1;

			   		    progressDialog.beginTask(res.getString("ingestcont"), pids.size() == 0 ? files.size() : pids.size(), true);
						
						
			   		    int fa = 0;
						fi = 0;
						

						progressDialog.worked(1);
						
						boolean onlyValidate = ((JCheckBox) getGuiComposite().getWidget("jcbSimulate")).isSelected(); 
                        String simulate = onlyValidate ? res.getString("ofsim") : "";                        						
						logger.write( res.getString("start")+ simulate+res.getString("ofingest") +new java.util.Date() );	
						

						JCheckBox jcbGenerated = ((JCheckBox) getGuiComposite().getWidget("jcbGenerated"));
						JTextField jtfPID = ((JTextField) getGuiComposite().getWidget("jtfPID"));
						
						for (int i = 0; i < (pids.size() == 0 ? files.size() : pids.size()); i++) {

							String pid = (String)jcbNamespace.getSelectedItem()+(String)jcbUser.getSelectedItem();

							progressDialog.setTaskName(res.getString("ingestof")+ (pids.size() == 0 ? files.get(i) : pids.get(i)) +" ...");
							if (jcbGenerated.isSelected() && !jtfPID.getText().isEmpty()) {
							   pid = "o:"+jtfPID.getText();
							   if (files.size() == 1 || pids.size()==1) {
								   if (Repository.exist(pid)) {
                                   	msgFmt = new MessageFormat(res.getString("double"));
   									Object[] args9 = {pid};
   									JOptionPane.showMessageDialog(  getCoreDialog(), msgFmt.format(args9) , Common.WINDOW_HEADER, JOptionPane.INFORMATION_MESSAGE);									   
   									break;
								   }
								   pid = "$"+pid;
							   }
							}
							
							if(progressDialog.isCanceled()) {
								break;
							}				

							progressDialog.worked(1);
 															
							try {
								Thread.sleep(50); 
							} catch (InterruptedException e) {
							}
								
							if (model.contains("TEI")) {

								TEI t = new TEI(logger, onlyValidate, true);
								t.setUser((String)jcbUser.getSelectedItem());
								t.set((String) files.get(i), false);
								
								if (!t.isValid()) {	
									
									msgFmt = new MessageFormat(res.getString("novalidtei"));
									Object[] args0 = {new java.util.Date(), (String) files.get(i)}; 
					  	  		    logger.write( msgFmt.format(args0)  ); 
					 				continue;
								}

								Split pcm = new Split(model);
												
								if (t.getPID().isEmpty()) {		
									if (!onlyValidate) {
										pid = temps.cloneTemplate("info:fedora/"+ pcm.get(), (String)jcbUser.getSelectedItem(), pid.trim(), res.getString("notitle"));
										t.setPID(pid);
										
										if (!Common.exist(pid)) {
											msgFmt = new MessageFormat(res.getString("createfail"));
											Object[] args0 = {new java.util.Date(), pid}; 
							  	  		    logger.write( msgFmt.format(args0)  ); 
											continue;
										}
									} else {t.setPID("obj:generated");}
									fi++;
									msgFmt = new MessageFormat(res.getString("objing"));
									Object[] args1 = {new java.util.Date(), pid, t.getName() };
									
									logger.write(msgFmt.format(args1) );									
								} else {
                                    if (!Repository.exist(t.getPID())) {
    									if (!onlyValidate) {
    										pid = temps.cloneTemplate("info:fedora/"+ pcm.get(), (String)jcbUser.getSelectedItem(), "$"+ t.getPID(), res.getString("notitle"));
    										if (!Common.exist(t.getPID())) {
    											msgFmt = new MessageFormat(res.getString("createfail"));
    											Object[] args0 = {new java.util.Date(), t.getPID()}; 
    							  	  		    logger.write( msgFmt.format(args0)  ); 
    											continue;
    										}
    									}
    									fi++;
    									msgFmt = new MessageFormat(res.getString("objing"));
    									Object[] args2 = {new java.util.Date(), t.getPID(), t.getName() };
    									
    									logger.write(msgFmt.format(args2));									                                    	
                                    } else {
                                    	if (t.getPID().contains(":"+(String)jcbUser.getSelectedItem()) || groups.contains("administrator")) {
                                    		
                                    		msgFmt = new MessageFormat(res.getString("objingrefr"));
        									Object[] args3 = {new java.util.Date(), t.getPID(), t.getName() };
                                    		logger.write( msgFmt.format(args3));
                                    		fa++;
                                    	} else {	
                                    		msgFmt = new MessageFormat(res.getString("denied"));
        									Object[] args4 = {new java.util.Date(), (String)jcbUser.getSelectedItem(), t.getPID() };
                                    		
                                    		logger.write( msgFmt.format(args4));
                                    		continue;
                                    	}	
                                    }
							    }
								
															    
 							    t.validate(pcm.get(), moGA);
 							    Common.genQR(user, t.getPID());
										
 							    try { 							    
 							    	if (!onlyValidate) Repository.modifyDatastreamByValue(t.getPID(), "TEI_SOURCE", "text/xml", t.toString()); 							    	
 							    } catch (Exception q) {}	

							}


							if (model.contains("MEI")) {

								MEI m = new MEI(logger, onlyValidate, true);
								m.setUser((String)jcbUser.getSelectedItem());
								m.set((String) files.get(i), false);
								
								if (!m.isValid()) {	
									
									msgFmt = new MessageFormat(res.getString("novalidtei"));
									Object[] args0 = {new java.util.Date(), (String) files.get(i)}; 
					  	  		    logger.write( msgFmt.format(args0)  ); 
					 				continue;
								}

								Split pcm = new Split(model);
												
								if (m.getPID().isEmpty()) {		
									if (!onlyValidate) {
										pid = temps.cloneTemplate("info:fedora/"+ pcm.get(), (String)jcbUser.getSelectedItem(), pid.trim(), res.getString("notitle"));
										m.setPID(pid);
										if (!Common.exist(pid)) {
											msgFmt = new MessageFormat(res.getString("createfail"));
											Object[] args0 = {new java.util.Date(), pid}; 
							  	  		    logger.write( msgFmt.format(args0)  ); 
											continue;
										}
									} else {m.setPID("obj:generated");}
									fi++;
									msgFmt = new MessageFormat(res.getString("objing"));
									Object[] args1 = {new java.util.Date(), pid, m.getName() };
									
									logger.write(msgFmt.format(args1) );									
								} else {
                                    if (!Repository.exist(m.getPID())) {
    									if (!onlyValidate) {
    										pid = temps.cloneTemplate("info:fedora/"+ pcm.get(), (String)jcbUser.getSelectedItem(), "$"+ m.getPID(), res.getString("notitle"));
    										if (!Common.exist(m.getPID())) {
    											msgFmt = new MessageFormat(res.getString("createfail"));
    											Object[] args0 = {new java.util.Date(), m.getPID()}; 
    							  	  		    logger.write( msgFmt.format(args0)  ); 
    											continue;
    										}
    									}
    									fi++;
    									msgFmt = new MessageFormat(res.getString("objing"));
    									Object[] args2 = {new java.util.Date(), m.getPID(), m.getName() };
    									
    									logger.write(msgFmt.format(args2));									                                    	
                                    } else {
                                    	if (m.getPID().contains(":"+(String)jcbUser.getSelectedItem()+".") || groups.contains("administrator")) {
                                    		
                                    		msgFmt = new MessageFormat(res.getString("objingrefr"));
        									Object[] args3 = {new java.util.Date(), m.getPID(), m.getName() };
                                    		logger.write( msgFmt.format(args3));
                                    		fa++;
                                    	} else {	
                                    		msgFmt = new MessageFormat(res.getString("denied"));
        									Object[] args4 = {new java.util.Date(), (String)jcbUser.getSelectedItem(), m.getPID() };
                                    		
                                    		logger.write( msgFmt.format(args4));
                                    		continue;
                                    	}	
                                    }
							    }
								
															    
 							    m.validate(pcm.get(), moGA);
 							    Common.genQR(user, m.getPID());
										
 							    try { 							    
 							    	if (!onlyValidate) Repository.modifyDatastreamByValue(m.getPID(), "MEI_SOURCE", "text/xml", m.toString()); 							    	
 							    } catch (Exception q) {}	

							}

							
							if (model.contains("METS") ) {

								METS m = new METS(logger, onlyValidate, true);
								m.setUser((String)jcbUser.getSelectedItem());
								m.set((String) files.get(i), false);
								
								if (!m.isValid()) {	
									msgFmt = new MessageFormat(res.getString("novalidmets"));
									Object[] args5 = {new java.util.Date(), (String) files.get(i)};
									
					  	  		    logger.write( msgFmt.format(args5)); 
					 				continue;
								}

								Split pcm = new Split(model);
												
								if (m.getPID().isEmpty()) {		
									if (!onlyValidate) {
										pid = temps.cloneTemplate("info:fedora/"+ pcm.get(), (String)jcbUser.getSelectedItem(), pid.trim(), res.getString("notitle"));
										m.setPID(pid);
										if (!Common.exist(pid)) {
											msgFmt = new MessageFormat(res.getString("createfail"));
											Object[] args0 = {new java.util.Date(), pid}; 
							  	  		    logger.write( msgFmt.format(args0)  ); 
											continue;
										}
									} else {m.setPID("obj:generated");}
									fi++;
									msgFmt = new MessageFormat(res.getString("objing"));
									Object[] args6 = {new java.util.Date(), pid, m.getName() };
									
									logger.write( msgFmt.format(args6));									
								} else {
                                    if (!Repository.exist(m.getPID())) {
    									if (!onlyValidate) {
    										pid = temps.cloneTemplate("info:fedora/"+ pcm.get(), (String)jcbUser.getSelectedItem(), "$"+ m.getPID(), res.getString("notitle"));
    										if (!Common.exist(m.getPID())) {
    											msgFmt = new MessageFormat(res.getString("createfail"));
    											Object[] args0 = {new java.util.Date(), m.getPID()}; 
    							  	  		    logger.write( msgFmt.format(args0)  ); 
    											continue;
    										}
    									}
    									fi++;
    									msgFmt = new MessageFormat(res.getString("objing"));
    									Object[] args7 = {new java.util.Date(), m.getPID(), m.getName() };
    									
    									logger.write( msgFmt.format(args7));									                                    	
                                    } else {
                                    	
                                    	msgFmt = new MessageFormat(res.getString("objingrefr"));
    									Object[] args8 = {new java.util.Date(), m.getPID(), m.getName() };
    									logger.write(msgFmt.format(args8));
    									fa++;
                                    }
							    }
															    
							    m.ingestImages();
							    m.write();
 							    m.createMapping(pcm.get(), moGA);
                                Common.genQR(user, m.getPID());
                                
 							    try {														    
 							    	if (!onlyValidate) Repository.modifyDatastreamByValue(m.getPID(), "METS_SOURCE", "text/xml", m.toString());
 							    } catch (Exception q) {
 							      log.error(q.getLocalizedMessage(),q);	

 							    }	

							}

							if (model.contains("LIDO")) {

								LIDO l = new LIDO(logger, onlyValidate, true);
								l.setUser((String)jcbUser.getSelectedItem());
								l.set((String) files.get(i), false);
								
								if (!l.isValid()) {	
									
									msgFmt = new MessageFormat(res.getString("novalidtei"));
									Object[] args0 = {new java.util.Date(), (String) files.get(i)}; 
					  	  		    logger.write( msgFmt.format(args0)  ); 
					 				continue;
								}

								Split pcm = new Split(model);
												
								if (l.getPID().isEmpty()) {		
									if (!onlyValidate) {
										pid = temps.cloneTemplate("info:fedora/"+ pcm.get(), (String)jcbUser.getSelectedItem(), pid.trim(), res.getString("notitle"));
										l.setPID(pid);
										if (!Common.exist(pid)) {
											msgFmt = new MessageFormat(res.getString("createfail"));
											Object[] args0 = {new java.util.Date(), pid}; 
							  	  		    logger.write( msgFmt.format(args0)  ); 
											continue;
										}
									} else {l.setPID("obj:generated");}
									fi++;
									msgFmt = new MessageFormat(res.getString("objing"));
									Object[] args1 = {new java.util.Date(), pid, l.getName() };
									
									logger.write(msgFmt.format(args1) );									
								} else {
                                    if (!Repository.exist(l.getPID())) {
    									if (!onlyValidate) {
    										pid = temps.cloneTemplate("info:fedora/"+ pcm.get(), (String)jcbUser.getSelectedItem(), "$"+ l.getPID(), res.getString("notitle"));
    										if (!Common.exist(l.getPID())) {
    											msgFmt = new MessageFormat(res.getString("createfail"));
    											Object[] args0 = {new java.util.Date(), l.getPID()}; 
    							  	  		    logger.write( msgFmt.format(args0)  ); 
    											continue;
    										}
    									}
    									fi++;
    									msgFmt = new MessageFormat(res.getString("objing"));
    									Object[] args2 = {new java.util.Date(), l.getPID(), l.getName() };
    									
    									logger.write(msgFmt.format(args2));									                                    	
                                    } else {
                                    	if (l.getPID().contains(":"+(String)jcbUser.getSelectedItem()+".") || groups.contains("administrator")) {
                                    		
                                    		msgFmt = new MessageFormat(res.getString("objingrefr"));
        									Object[] args3 = {new java.util.Date(), l.getPID(), l.getName() };
                                    		logger.write( msgFmt.format(args3));
                                    		fa++;
                                    	} else {	
                                    		msgFmt = new MessageFormat(res.getString("denied"));
        									Object[] args4 = {new java.util.Date(), (String)jcbUser.getSelectedItem(), l.getPID() };
                                    		
                                    		logger.write( msgFmt.format(args4));
                                    		continue;
                                    	}	
                                    }
							    }	
								
 							    l.validate(pcm.get(), moGA);
 							    
 							    Common.genQR(user, l.getPID());
										
 							    try { 							    
 							    	if (!onlyValidate) Repository.modifyDatastreamByValue(l.getPID(), "LIDO_SOURCE", "text/xml", l.toString()); 							    	
 							    } catch (Exception q) {}	

							}
							
							
							if (model.contains("Ontology")) {	
								String s;
								if (pids.size() > 0) {
									pid = pids.get(i);
									s = rdfs.get(pid);
								} else {
									pid = "o:"+user.getUser();
									rdfs.set(files.get(i));
									s = rdfs.toString();
								}
								
								if (!onlyValidate) {
								  try{	
 	 	 				    		File temp = File.createTempFile("tmp","xml");
  	 					           	FileOutputStream fos = new FileOutputStream(temp);
  	 					           	fos.write(s.getBytes("UTF-8"));
 	 					           	fos.close();

 	 					           	Split pcm = new Split(model);
									if (!Repository.exist(pid)) {
										pid = temps.cloneTemplate("info:fedora/"+ pcm.get(), (String)jcbUser.getSelectedItem(), (pids.size()>0 ? "$":"")+pid.trim(), res.getString("notitle"));
										if (!Common.exist(pid)) {
											msgFmt = new MessageFormat(res.getString("createfail"));
											Object[] args0 = {new java.util.Date(), pid}; 
							  	  		    logger.write( msgFmt.format(args0)  ); 
											continue;
										}
										fi++;
 										msgFmt = new MessageFormat(res.getString("objing"));
 										Object[] args6 = {new java.util.Date(), pid, (pids.size()>0 ? pids.get(i) : files.get(i)) };
 	 									logger.write( msgFmt.format(args6));				
									} else {
										fa++;
 										msgFmt = new MessageFormat(res.getString("objingrrefr"));
 										Object[] args6 = {new java.util.Date(), pid, (pids.size()>0 ? pids.get(i) : files.get(i)) };
 	 									logger.write( msgFmt.format(args6));				
									}
 	 					           	  	 					           	
  	 								TripleStoreFactory tf = new TripleStoreFactory();
  	 								if (tf.getStatus()) {
  	 									if (tf.update(temp, pid)) {
  	 										if (pids.size() >0) rdfs.mapDC(pid);
  	 										Repository.modifyDatastreamByValue(pid, "ONTOLOGY", "text/xml", rdfs.toString()); 	
  	 										Common.genQR(user, pid);
  	 									} else {
  	 										msgFmt = new MessageFormat(res.getString("nonvalidrdf"));
  	 										Object[] args6 = {new java.util.Date(), pid };
  	 	 									logger.write( msgFmt.format(args6));
  	 									}
  	 								}	
  	 								tf.close();
                                    temp.delete();
 									
								  } catch (Exception q) {}
								}  

								
							}

						}
						
						logger.write("\n" +res.getString("end")+simulate+res.getString("ofingest") + new java.util.Date() + ". " + new Integer(fi).toString().trim() + res.getString("ingested")+ new Integer(fa).toString().trim() + res.getString("refreshed"));									
						logger.close();
						
						JOptionPane.showMessageDialog(  getCoreDialog(), new Integer(fi).toString()+ res.getString("ingested")+ new Integer(fa).toString().trim() + res.getString("refreshed") + res.getString("details")+fp.getAbsolutePath()+System.getProperty( "file.separator" )+"ingest.log" , Common.WINDOW_HEADER, JOptionPane.INFORMATION_MESSAGE);
			        	logfile =fp.getAbsolutePath()+System.getProperty( "file.separator" )+"ingest.log"; 
						getGuiComposite().getWidget("jbShowLogfile").setEnabled(true);

					}

					
				} catch (Exception ex) {
					try {
						logger.write(new java.util.Date()  +" "+ex.getLocalizedMessage()+"\n");
						logger.close();
					} catch (Exception ez) {}
				}
				finally {
					getCoreDialog().setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
				}
		
			}
		}.start();
		
	}

	
	public void handleIngestFromExistButton(ActionEvent e) 
			throws Exception {
			new Thread() {
				public void run() {
					final String driver = "org.exist.xmldb.DatabaseImpl";
					String model = "";
					
					try {
						CPropertyService props = (CPropertyService) CServiceProvider.getService(ServiceNames.PROPERTIES);
						
						TemplateSubsystem temps = (TemplateSubsystem) CServiceProvider.getService(ServiceNames.TEMPLATESUBSYSTEM);
						JComboBox jcbContentModel = ((JComboBox) getGuiComposite().getWidget("jcbContentModel"));            
						JComboBox jcbNamespace = ((JComboBox) getGuiComposite().getWidget("jcbNamespace"));
						JComboBox jcbUser = ((JComboBox) getGuiComposite().getWidget("jcbUser"));
						model = (String)jcbContentModel.getSelectedItem();
						
						Class cl = Class.forName( driver );
						Database database = (Database) cl.newInstance();
						database.setProperty( "create-database", "true" );
						DatabaseManager.registerDatabase( database );
          			
						getCoreDialog().setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));

						int fa=0;
						
						
						LoginExistDialog loLogin;
						
						loLogin = (LoginExistDialog) CServiceProvider.getService(DialogNames.LOGIN_EXISTDIALOG);
						loLogin.open();

						if (!loLogin.isConnected()) {
							return;
						}

						
						files = new ArrayList<String>();						
				        treeWalk(user.getExistUrl() , user.getExistHome(), user.getExistUser(), user.getExistPasswd());
		            
				        
				        MessageFormat msgFmt = new MessageFormat(res.getString("objcrea"));
						Object[] args = {new Integer(files.size()).toString(), model, (user.getExistUrl()+user.getExistHome()) };
						
						int liChoice = JOptionPane.showConfirmDialog(null, msgFmt.format(args) ,
								Common.WINDOW_HEADER, JOptionPane.YES_NO_OPTION,
								JOptionPane.QUESTION_MESSAGE);

						FileWriter logger = new FileWriter(new File(System.getProperty("java.io.tmpdir")).getAbsolutePath()+System.getProperty("file.separator")+ "ingest.log" );

						if (liChoice == 0) {
							
						    props.setProperty("user", "Ingest.ContentModel", jcbContentModel.getSelectedItem().toString());
						
							ProgressDialog progressDialog = new ProgressDialog( getCoreDialog(), Common.WINDOW_HEADER);
							progressDialog.displayPercentageInProgressBar = true;
							progressDialog.millisToDecideToPopup = 1;
							progressDialog.millisToPopup = 1;

				   		    progressDialog.beginTask(res.getString("ingestcont"), files.size(), true);
							
							fi = 0;
							
									
							progressDialog.worked(1);

							boolean onlyValidate = ((JCheckBox) getGuiComposite().getWidget("jcbSimulate")).isSelected(); 
	                        String simulate = onlyValidate ? res.getString("ofsim") : "";                        						
							logger.write( new java.util.Date()  +res.getString("start")+ simulate+res.getString("ofingest"));	

							JCheckBox jcbGenerated = ((JCheckBox) getGuiComposite().getWidget("jcbGenerated"));
							JTextField jtfPID = ((JTextField) getGuiComposite().getWidget("jtfPID"));
														
							for (int i = 0; i<files.size(); i++) {
											    
								String pid = (String)jcbNamespace.getSelectedItem()+(String)jcbUser.getSelectedItem();

								progressDialog.setTaskName(res.getString("ingestof")+files.get(i)+" ...");
								if (jcbGenerated.isSelected() && !jtfPID.getText().isEmpty()) {
									   pid = "o:"+jtfPID.getText();
									   if (files.size() == 1) {
										   if (Repository.exist(pid)) {
		                                   	msgFmt = new MessageFormat(res.getString("double"));
		   									Object[] args9 = {pid};
		   									JOptionPane.showMessageDialog(  getCoreDialog(), msgFmt.format(args9) , Common.WINDOW_HEADER, JOptionPane.INFORMATION_MESSAGE);									   
		   									break;
										   }
										   pid = "$"+pid;
									   }
									}
				    
								if(progressDialog.isCanceled()) {
									break;
								}				

								progressDialog.worked(1);
																	
								
								try {
									Thread.sleep(50); 
								} catch (InterruptedException e) {
								}

								if (model.contains("TEI")) {

									TEI t = new TEI(logger, onlyValidate, true);
									t.setUser((String)jcbUser.getSelectedItem());
									t.set((String) files.get(i), true);
									
									if (!t.isValid()) {	
										 msgFmt = new MessageFormat(res.getString("novalidtei"));
										Object[] args1 = {new java.util.Date(), (String) files.get(i)};
										
						  	  		    logger.write( msgFmt.format(args1)); 
						 				continue;
									}

									Split pcm = new Split(model);
									
									if (t.getPID().isEmpty()) {	
										if (!onlyValidate) {
											pid = temps.cloneTemplate("info:fedora/"+ pcm.get(), (String)jcbUser.getSelectedItem(), pid.trim(), res.getString("notitle"));
											t.setPID(pid);
											if (!Common.exist(pid)) {
												msgFmt = new MessageFormat(res.getString("createfail"));
												Object[] args0 = {new java.util.Date(), pid}; 
								  	  		    logger.write( msgFmt.format(args0)  ); 
												continue;
											}
										} else {t.setPID("obj:generated");}
										fi++;
										msgFmt = new MessageFormat(res.getString("objing"));
										Object[] args2 = {new java.util.Date(), pid, t.getName()};
										
										logger.write( msgFmt.format(args2));									
									} else {
	                                    if (!Repository.exist(t.getPID())) {
	    									if (!onlyValidate) {
	    										pid = temps.cloneTemplate("info:fedora/"+ pcm.get(), (String)jcbUser.getSelectedItem(), "$"+ t.getPID(), res.getString("notitle"));
	    										if (!Common.exist(t.getPID())) {
	    											msgFmt = new MessageFormat(res.getString("createfail"));
	    											Object[] args0 = {new java.util.Date(), t.getPID()}; 
	    							  	  		    logger.write( msgFmt.format(args0)  ); 
	    											continue;
	    										}
	    									}
	    									fi++;
	    									
	    									msgFmt = new MessageFormat(res.getString("objing"));
											Object[] args3 = {new java.util.Date(), t.getPID(), t.getName()};
	    									logger.write( msgFmt.format(args3));									                                    	
	                                    } else {
	                                    	if (t.getPID().contains(":"+(String)jcbUser.getSelectedItem()+".") || groups.contains("administrator")) {
	                                    		msgFmt = new MessageFormat(res.getString("objingrefr"));
												Object[] args4 = {new java.util.Date(), t.getPID(), t.getName()};
	                                    		
	                                    		logger.write( msgFmt.format(args4));
	                                    		fa++;
	                                    	} else {
	                                    		msgFmt = new MessageFormat(res.getString("denied"));
												Object[] args5 = {new java.util.Date(), (String)jcbUser.getSelectedItem(), t.getPID()};
	                                    		
	                                    		logger.write( msgFmt.format(args5));
	                                    		continue;
	                                    	}	
	                                    }
								    }
									
	 							    t.validate(pcm.get(), moGA);
	 							    Common.genQR(user, t.getPID());
	 							    
	 							    try {														    
	 	 							   if (!onlyValidate) Repository.modifyDatastreamByValue(t.getPID(), "TEI_SOURCE", "text/xml", t.toString());
	 							    } catch (Exception q) {
	 							    	log.error(q.getLocalizedMessage(),q);	
	 							    }	
								

								}


								if (model.contains("MEI")) {

									MEI m = new MEI(logger, onlyValidate, true);
									m.setUser((String)jcbUser.getSelectedItem());
									m.set((String) files.get(i), true);
									
									if (!m.isValid()) {	
										 msgFmt = new MessageFormat(res.getString("novalidtei"));
										Object[] args1 = {new java.util.Date(), (String) files.get(i)};
										
						  	  		    logger.write( msgFmt.format(args1)); 
						 				continue;
									}

									Split pcm = new Split(model);
									
									if (m.getPID().isEmpty()) {	
										if (!onlyValidate) {
											pid = temps.cloneTemplate("info:fedora/"+ pcm.get(), (String)jcbUser.getSelectedItem(), pid.trim(), res.getString("notitle"));
											m.setPID(pid);
											if (!Common.exist(pid)) {
												msgFmt = new MessageFormat(res.getString("createfail"));
												Object[] args0 = {new java.util.Date(), pid}; 
								  	  		    logger.write( msgFmt.format(args0)  ); 
												continue;
											}
										} else {m.setPID("obj:generated");}
										fi++;
										msgFmt = new MessageFormat(res.getString("objing"));
										Object[] args2 = {new java.util.Date(), pid, m.getName()};
										
										logger.write( msgFmt.format(args2));									
									} else {
	                                    if (!Repository.exist(m.getPID())) {
	    									if (!onlyValidate) {
	    										pid = temps.cloneTemplate("info:fedora/"+ pcm.get(), (String)jcbUser.getSelectedItem(), "$"+ m.getPID(), res.getString("notitle"));
												if (!Common.exist(m.getPID())) {
													msgFmt = new MessageFormat(res.getString("createfail"));
													Object[] args0 = {new java.util.Date(), m.getPID()}; 
									  	  		    logger.write( msgFmt.format(args0)  ); 
													continue;
												}
	    									}
	    									fi++;
	    									
	    									msgFmt = new MessageFormat(res.getString("objing"));
											Object[] args3 = {new java.util.Date(), m.getPID(), m.getName()};
	    									logger.write( msgFmt.format(args3));									                                    	
	                                    } else {
	                                    	if (m.getPID().contains(":"+(String)jcbUser.getSelectedItem()+".") || groups.contains("administrator")) {
	                                    		msgFmt = new MessageFormat(res.getString("objingrefr"));
												Object[] args4 = {new java.util.Date(), m.getPID(), m.getName()};
	                                    		
	                                    		logger.write( msgFmt.format(args4));
	                                    		fa++;
	                                    	} else {
	                                    		msgFmt = new MessageFormat(res.getString("denied"));
												Object[] args5 = {new java.util.Date(), (String)jcbUser.getSelectedItem(), m.getPID()};
	                                    		
	                                    		logger.write( msgFmt.format(args5));
	                                    		continue;
	                                    	}	
	                                    }
								    }
									
	 							    m.validate(pcm.get(), moGA);
	 							    Common.genQR(user, m.getPID());
	 							    
	 							    try {														    
	 	 							   if (!onlyValidate) Repository.modifyDatastreamByValue(m.getPID(), "MEI_SOURCE", "text/xml", m.toString());
	 							    } catch (Exception q) {
	 							    	log.error(q.getLocalizedMessage(),q);	
	 							    }	
								

								}
								
								
								if (model.contains("METS") ) {

									METS m = new METS(logger, onlyValidate, true);
									m.setUser((String)jcbUser.getSelectedItem());
									m.set((String) files.get(i), true);
								
									if (!m.isValid()) {	
										
										msgFmt = new MessageFormat(res.getString("novalidmets"));
										Object[] args6 = {new java.util.Date(), (String) files.get(i)};
										
					  	  		    	logger.write( msgFmt.format(args6)); 
					  	  		    	continue;
									}

									Split pcm = new Split(model);
								
									if (m.getPID().isEmpty()) {
										if (!onlyValidate) {
											pid = temps.cloneTemplate("info:fedora/"+ pcm.get(), (String)jcbUser.getSelectedItem(), pid.trim(), res.getString("notitle"));
											m.setPID(pid);
											if (!Common.exist(pid)) {
												msgFmt = new MessageFormat(res.getString("createfail"));
												Object[] args0 = {new java.util.Date(), pid}; 
								  	  		    logger.write( msgFmt.format(args0)  ); 
												continue;
											}
										} else {m.setPID("obj:generated");}
										fi++;
										msgFmt = new MessageFormat(res.getString("objing"));
										Object[] args7 = {new java.util.Date(), pid, m.getName()};
										
										logger.write(  msgFmt.format(args7));									
									} else {
										if (!Repository.exist(m.getPID())) {
											if (!onlyValidate) {
												pid = temps.cloneTemplate("info:fedora/"+ pcm.get(), (String)jcbUser.getSelectedItem(), "$"+ m.getPID(), res.getString("notitle"));
												if (!Common.exist(m.getPID())) {
													msgFmt = new MessageFormat(res.getString("createfail"));
													Object[] args0 = {new java.util.Date(), m.getPID()}; 
									  	  		    logger.write( msgFmt.format(args0)  ); 
													continue;
												}
											}	
											fi++;
											msgFmt = new MessageFormat(res.getString("objing"));
											Object[] args8 = {new java.util.Date(), m.getPID(), m.getName()};
											
											logger.write( msgFmt.format(args8));									                                    	
										} else {
											
											msgFmt = new MessageFormat(res.getString("objingrefr"));
											Object[] args9 = {new java.util.Date(), m.getPID(), m.getName()};
											logger.write( msgFmt.format(args9));
											fa++;
										}
									}
								
									m.ingestImages();
									m.write();
									m.createMapping(pcm.get(), moGA);
	 							    Common.genQR(user, m.getPID());

	 							    try {														    
										if (!onlyValidate) Repository.modifyDatastreamByValue(m.getPID(), "METS_SOURCE", "text/xml", m.toString());
		 							} catch (Exception q) {}	
							
								}
								
								if (model.contains("LIDO")) {

									LIDO l = new LIDO(logger, onlyValidate, true);
									l.setUser((String)jcbUser.getSelectedItem());
									l.set((String) files.get(i), true);
									
									if (!l.isValid()) {	
										 msgFmt = new MessageFormat(res.getString("novalidtei"));
										Object[] args1 = {new java.util.Date(), (String) files.get(i)};
										
						  	  		    logger.write( msgFmt.format(args1)); 
						 				continue;
									}

									Split pcm = new Split(model);
									
									if (l.getPID().isEmpty()) {	
										if (!onlyValidate) {
											pid = temps.cloneTemplate("info:fedora/"+ pcm.get(), (String)jcbUser.getSelectedItem(), pid.trim(), res.getString("notitle"));
											l.setPID(pid);
											if (!Common.exist(pid)) {
												msgFmt = new MessageFormat(res.getString("createfail"));
												Object[] args0 = {new java.util.Date(), pid}; 
								  	  		    logger.write( msgFmt.format(args0)  ); 
												continue;
											}
										} else {l.setPID("obj:generated");}
										fi++;
										msgFmt = new MessageFormat(res.getString("objing"));
										Object[] args2 = {new java.util.Date(), pid, l.getName()};
										
										logger.write( msgFmt.format(args2));									
									} else {
	                                    if (!Repository.exist(l.getPID())) {
	    									if (!onlyValidate) {
	    										pid = temps.cloneTemplate("info:fedora/"+ pcm.get(), (String)jcbUser.getSelectedItem(), "$"+ l.getPID(), res.getString("notitle"));
												if (!Common.exist(l.getPID())) {
													msgFmt = new MessageFormat(res.getString("createfail"));
													Object[] args0 = {new java.util.Date(), l.getPID()}; 
									  	  		    logger.write( msgFmt.format(args0)  ); 
													continue;
												}
	    									}
	    									fi++;
	    									
	    									msgFmt = new MessageFormat(res.getString("objing"));
											Object[] args3 = {new java.util.Date(), l.getPID(), l.getName()};
	    									logger.write( msgFmt.format(args3));									                                    	
	                                    } else {
	                                    	if (l.getPID().contains(":"+(String)jcbUser.getSelectedItem()+".") || groups.contains("administrator")) {
	                                    		msgFmt = new MessageFormat(res.getString("objingrefr"));
												Object[] args4 = {new java.util.Date(), l.getPID(), l.getName()};
	                                    		
	                                    		logger.write( msgFmt.format(args4));
	                                    		fa++;
	                                    	} else {
	                                    		msgFmt = new MessageFormat(res.getString("denied"));
												Object[] args5 = {new java.util.Date(), (String)jcbUser.getSelectedItem(), l.getPID()};
	                                    		
	                                    		logger.write( msgFmt.format(args5));
	                                    		continue;
	                                    	}	
	                                    }
								    }
									
	 							    l.validate(pcm.get(), moGA);
	 							    Common.genQR(user, l.getPID());
	 							    
	 							    try {														    
	 	 							   if (!onlyValidate) Repository.modifyDatastreamByValue(l.getPID(), "LIDO_SOURCE", "text/xml", l.toString());
	 							    } catch (Exception q) {
	 							    	log.error(q.getLocalizedMessage(),q);	
	 							    }	
								

								}


							
							}
							                           		
							
							logger.write("\n"+ new java.util.Date()  +res.getString("end")+simulate+res.getString("ofingest")+new Integer(fi).toString().trim() + res.getString("ingested")+ new Integer(fa).toString().trim() + res.getString("refreshed"));									
							logger.close();
							logfile = new File(System.getProperty("java.io.tmpdir")).getAbsolutePath()+System.getProperty("file.separator")+"ingest.log"; 
							JOptionPane.showMessageDialog(  getCoreDialog(), new Integer(fi).toString()+ res.getString("ingested") + new Integer(fa).toString().trim() + res.getString("refreshed") +res.getString("details")+logfile, Common.WINDOW_HEADER, JOptionPane.INFORMATION_MESSAGE );
							getGuiComposite().getWidget("jbShowLogfile").setEnabled(true);
							
						}
						
					} catch (Exception ex) {
					}
					finally {
						getCoreDialog().setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
					}
			
				}
			}.start();
			
		}
	
	
	public void handleIngestFromExcelButton(ActionEvent e) 
			throws Exception {
			new Thread() {
				public void run() {
					FileWriter logger = null;
					String model = "";
					EXCEL excel = null;;
										
					try {
						CPropertyService props = (CPropertyService) CServiceProvider.getService(ServiceNames.PROPERTIES);
						
						TemplateSubsystem temps = (TemplateSubsystem) CServiceProvider.getService(ServiceNames.TEMPLATESUBSYSTEM);
						JComboBox jcbContentModel = ((JComboBox) getGuiComposite().getWidget("jcbContentModel"));            
						JComboBox jcbNamespace = ((JComboBox) getGuiComposite().getWidget("jcbNamespace"));
						JComboBox jcbUser = ((JComboBox) getGuiComposite().getWidget("jcbUser"));
						model = (String)jcbContentModel.getSelectedItem();
											

						getCoreDialog().setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));

						int fa=0;
						
						IngestExcelDialog loExcel;
						
						loExcel = (IngestExcelDialog) CServiceProvider.getService(DialogNames.INGESTEXCEL_DIALOG);
						loExcel.open();

						if (!loExcel.isSubmit() || loExcel.getTemplate().isEmpty() || loExcel.getTable().isEmpty()) {
							return;
						}
												
						excel = new EXCEL(loExcel.getTable(), loExcel.getTemplate());						
						
						if (excel.init(0)) {
						
						MessageFormat msgFmt = new MessageFormat(res.getString("objcrea"));
						Object[] args = {new Integer(excel.getRowCount()).toString(), model, ""};

						logger = new FileWriter(new File(System.getProperty("java.io.tmpdir")).getAbsolutePath()+System.getProperty("file.separator")+ "ingest.log" );
       
						int liChoice = JOptionPane.showConfirmDialog(null, msgFmt.format(args) ,
								Common.WINDOW_HEADER, JOptionPane.YES_NO_OPTION,
								JOptionPane.QUESTION_MESSAGE);

						if (liChoice == 0) {

							props.setProperty("user", "Ingest.ContentModel", jcbContentModel.getSelectedItem().toString());

							ProgressDialog progressDialog = new ProgressDialog( getCoreDialog(), Common.WINDOW_HEADER);
							progressDialog.displayPercentageInProgressBar = true;
							progressDialog.millisToDecideToPopup = 1;
							progressDialog.millisToPopup = 1;

				   		    progressDialog.beginTask(res.getString("ingestcont"), excel.getRowCount(), true);
							
							
							fi = 0;
							

							progressDialog.worked(1);
							
							boolean onlyValidate = ((JCheckBox) getGuiComposite().getWidget("jcbSimulate")).isSelected(); 
	                        String simulate = onlyValidate ? res.getString("ofsim") : "";                        						
							logger.write( res.getString("start")+ simulate+res.getString("ofingest") +new java.util.Date() );	
						
							int i = 0;
							
							
							while (excel.hasNext()) {

								String pid = (String)jcbNamespace.getSelectedItem()+(String)jcbUser.getSelectedItem();
                                i++;
                                
								if(progressDialog.isCanceled()) {
									break;
								}				

								progressDialog.worked(1);
	 															
								try {
									Thread.sleep(50); 
								} catch (InterruptedException e) {
								}
									
								if (model.indexOf("TEI") > -1) {
																	
									TEI t = new TEI(logger, onlyValidate, false);
									t.setUser((String)jcbUser.getSelectedItem());
	                                t.set(excel.toString());                                                                 
										                                
									if (!t.isValid()) {									
										msgFmt = new MessageFormat(res.getString("novalidrtei"));
										Object[] args0 = {new java.util.Date(),new Integer(i).toString()}; 
						  	  		    logger.write( msgFmt.format(args0)  ); 
						 				continue;
									}

									Split pcm = new Split(model);
													
									if (t.getPID().isEmpty()) {		
										if (!onlyValidate) {
											pid = temps.cloneTemplate("info:fedora/"+ pcm.get(), (String)jcbUser.getSelectedItem(), pid.trim(), res.getString("notitle"));
											t.setPID(pid);
											if (!Common.exist(pid)) {
												msgFmt = new MessageFormat(res.getString("createfail"));
												Object[] args0 = {new java.util.Date(), pid}; 
								  	  		    logger.write( msgFmt.format(args0)  ); 
												continue;
											}
										} else {t.setPID("obj:generated");}
										fi++;
										msgFmt = new MessageFormat(res.getString("objingr"));
										Object[] args1 = {new java.util.Date(), pid,new Integer(i).toString() };
										
										logger.write(msgFmt.format(args1) );									
									} else {
	                                    if (!Repository.exist(t.getPID())) {
	    									if (!onlyValidate) {
	    										pid = temps.cloneTemplate("info:fedora/"+ pcm.get(), (String)jcbUser.getSelectedItem(), "$"+ t.getPID(), res.getString("notitle"));
												if (!Common.exist( t.getPID())) {
													msgFmt = new MessageFormat(res.getString("createfail"));
													Object[] args0 = {new java.util.Date(),  t.getPID()}; 
									  	  		    logger.write( msgFmt.format(args0)  ); 
													continue;
												}
	    									}
	    									fi++;
	    									msgFmt = new MessageFormat(res.getString("objingr"));
	    									Object[] args2 = {new java.util.Date(), t.getPID(),new Integer(i).toString()};
	    									
	    									logger.write(msgFmt.format(args2));									                                    	
	                                    } else {
	                                    	if (t.getPID().contains(":"+(String)jcbUser.getSelectedItem()+".") || groups.contains("administrator")) {	                                    		
	                                    		msgFmt = new MessageFormat(res.getString("objingrrefr"));
	        									Object[] args3 = {new java.util.Date(), t.getPID(), new Integer(i).toString() };
	                                    		logger.write( msgFmt.format(args3));
	                                    		fa++;
	                                    	} else {	
	                                    		msgFmt = new MessageFormat(res.getString("denied"));
	        									Object[] args4 = {new java.util.Date(), (String)jcbUser.getSelectedItem(), t.getPID() };	                                    		
	                                    		logger.write( msgFmt.format(args4));
	                                    		continue;
	                                    	}	
	                                    }
								    }
									
	 							    t.validate(pcm.get(), moGA);
	 							    Common.genQR(user, t.getPID());
											
	 							    try { 							    
	 							    	if (!onlyValidate) Repository.modifyDatastreamByValue(t.getPID(), "TEI_SOURCE", "text/xml", t.toString()); 							    	
	 							    } catch (Exception q) {}	

								}

								
								if (model.contains("METS") ) {

									METS m = new METS(logger, onlyValidate, false);
									m.setUser((String)jcbUser.getSelectedItem());
                                    m.set(excel.toString());                                                                 
									
									if (!m.isValid()) {	
										msgFmt = new MessageFormat(res.getString("novalidrmets"));
										Object[] args5 = {new java.util.Date(), new Integer(i+1).toString()};
										
						  	  		    logger.write( msgFmt.format(args5)); 
						 				continue;
									}

									Split pcm = new Split(model);
													
									if (m.getPID().isEmpty()) {		
										if (!onlyValidate) {
											pid = temps.cloneTemplate("info:fedora/"+ pcm.get(), (String)jcbUser.getSelectedItem(), pid.trim(), res.getString("notitle"));
											m.setPID(pid);
											if (!Common.exist(pid)) {
												msgFmt = new MessageFormat(res.getString("createfail"));
												Object[] args0 = {new java.util.Date(), pid}; 
								  	  		    logger.write( msgFmt.format(args0)  ); 
												continue;
											}
										} else {m.setPID("obj:generated");}
										fi++;
										msgFmt = new MessageFormat(res.getString("objingr"));
										Object[] args6 = {new java.util.Date(), pid, new Integer(i+1).toString()};
										
										logger.write( msgFmt.format(args6));									
									} else {
	                                    if (!Repository.exist(m.getPID())) {
	    									if (!onlyValidate) {
	    										pid = temps.cloneTemplate("info:fedora/"+ pcm.get(), (String)jcbUser.getSelectedItem(), "$"+ m.getPID(), res.getString("notitle"));
												if (!Common.exist(m.getPID())) {
													msgFmt = new MessageFormat(res.getString("createfail"));
													Object[] args0 = {new java.util.Date(), m.getPID()}; 
									  	  		    logger.write( msgFmt.format(args0)  ); 
													continue;
												}
	    									}
	    									fi++;
	    									msgFmt = new MessageFormat(res.getString("objingr"));
	    									Object[] args7 = {new java.util.Date(), m.getPID(), new Integer(i+1).toString()};
	    									
	    									logger.write( msgFmt.format(args7));									                                    	
	                                    } else {
	                                    	if (m.getPID().contains(":"+(String)jcbUser.getSelectedItem()+".") || groups.contains("administrator")) {	                                    		
	                                    		msgFmt = new MessageFormat(res.getString("objingrrefr"));
	        									Object[] args3 = {new java.util.Date(), m.getPID(), new Integer(i).toString() };
	                                    		logger.write( msgFmt.format(args3));
	                                    		fa++;
	                                    	} else {	
	                                    		msgFmt = new MessageFormat(res.getString("denied"));
	        									Object[] args4 = {new java.util.Date(), (String)jcbUser.getSelectedItem(), m.getPID() };	                                    		
	                                    		logger.write( msgFmt.format(args4));
	                                    		continue;
	                                    	}	
	                                    }
								    }
																    
								    m.ingestImages();
								    m.write();
	 							    m.createMapping(pcm.get(),moGA);
	 							    Common.genQR(user, m.getPID());

	 							    try {														    
	 							    	if (!onlyValidate) Repository.modifyDatastreamByValue(m.getPID(), "METS_SOURCE", "text/xml", m.toString());
	 							    } catch (Exception q) {}	

								}
								
								if (model.indexOf("LIDO") > -1) {
									
									LIDO l = new LIDO(logger, onlyValidate, false);
									l.setUser((String)jcbUser.getSelectedItem());
	                                l.set(excel.toString());                                                                 
										                                
									if (!l.isValid()) {									
										msgFmt = new MessageFormat(res.getString("novalidrtei"));
										Object[] args0 = {new java.util.Date(),new Integer(i).toString()}; 
						  	  		    logger.write( msgFmt.format(args0)  ); 
						 				continue;
									}

									Split pcm = new Split(model);
													
									if (l.getPID().isEmpty()) {		
									    if (!onlyValidate) {
											pid = temps.cloneTemplate("info:fedora/"+ pcm.get(), (String)jcbUser.getSelectedItem(), pid.trim(), res.getString("notitle"));
											l.setPID(pid);
											if (!Common.exist(pid)) {
												msgFmt = new MessageFormat(res.getString("createfail"));
												Object[] args0 = {new java.util.Date(), pid}; 
								  	  		    logger.write( msgFmt.format(args0)  ); 
												continue;
											}	
										} else {l.setPID("obj:generated");}
										fi++;
										msgFmt = new MessageFormat(res.getString("objingr"));
										Object[] args1 = {new java.util.Date(), pid,new Integer(i).toString() };
										
										logger.write(msgFmt.format(args1) );									
									} else {
	                                    if (!Repository.exist(l.getPID())) {
	    									if (!onlyValidate) {
	    										pid = temps.cloneTemplate("info:fedora/"+ pcm.get(), (String)jcbUser.getSelectedItem(), "$"+ l.getPID(), res.getString("notitle"));
												if (!Common.exist(l.getPID())) {
													msgFmt = new MessageFormat(res.getString("createfail"));
													Object[] args0 = {new java.util.Date(), l.getPID()}; 
									  	  		    logger.write( msgFmt.format(args0)  ); 
													continue;
												}	
	    									}
	    									fi++;
	    									msgFmt = new MessageFormat(res.getString("objingr"));
	    									Object[] args2 = {new java.util.Date(), l.getPID(),new Integer(i).toString()};
	    									
	    									logger.write(msgFmt.format(args2));									                                    	
	                                    } else {
	                                    	if (l.getPID().contains(":"+(String)jcbUser.getSelectedItem()+".") || groups.contains("administrator")) {
	                                    		
	                                    		msgFmt = new MessageFormat(res.getString("objingrrefr"));
	        									Object[] args3 = {new java.util.Date(), l.getPID(), new Integer(i).toString() };
	                                    		logger.write( msgFmt.format(args3));
	                                    		fa++;
	                                    	} else {	
	                                    		msgFmt = new MessageFormat(res.getString("denied"));
	        									Object[] args4 = {new java.util.Date(), (String)jcbUser.getSelectedItem(), l.getPID() };
	                                    		
	                                    		logger.write( msgFmt.format(args4));
	                                    		continue;
	                                    	}	
	                                    }
								    }
									
	 							    l.validate(pcm.get(), moGA);
	 							    Common.genQR(user, l.getPID());
											
	 							    try { 							    
	 							    	if (!onlyValidate) Repository.modifyDatastreamByValue(l.getPID(), "LIDO_SOURCE", "text/xml", l.toString()); 							    	
	 							    } catch (Exception q) {}	

								}
								
								if (model.contains("Resource") ) {

									DC d = new DC(logger, onlyValidate, false);
				                    d.set(excel.toString());                                                                 
									
									if (!d.isValid()) {	
										msgFmt = new MessageFormat(res.getString("novalidrmets"));
										Object[] args5 = {new java.util.Date(), new Integer(i+1).toString()};
									
						  	  		    logger.write( msgFmt.format(args5)); 
						 				continue;
									}

									Split pcm = new Split(model);
													
									if (d.getPID().isEmpty()) {		
										if (!onlyValidate) {
											pid = temps.cloneTemplate("info:fedora/"+ pcm.get(), (String)jcbUser.getSelectedItem(), pid.trim(), res.getString("notitle"));
											d.setPID(pid);
											if (!Common.exist(pid)) {
												msgFmt = new MessageFormat(res.getString("createfail"));
												Object[] args0 = {new java.util.Date(), pid}; 
								  	  		    logger.write( msgFmt.format(args0)  ); 
												continue;
											}	
										} else {d.setPID("obj:generated");}
										fi++;
										msgFmt = new MessageFormat(res.getString("objingr"));
										Object[] args6 = {new java.util.Date(), pid, new Integer(i+1).toString()};
										
										logger.write( msgFmt.format(args6));									
									} else {
	                                    if (!Repository.exist(d.getPID())) {
	    									if (!onlyValidate) {
	    										pid = temps.cloneTemplate("info:fedora/"+ pcm.get(), (String)jcbUser.getSelectedItem(), "$"+ d.getPID(), res.getString("notitle"));
												if (!Common.exist( d.getPID())) {
													msgFmt = new MessageFormat(res.getString("createfail"));
													Object[] args0 = {new java.util.Date(),  d.getPID()}; 
									  	  		    logger.write( msgFmt.format(args0)  ); 
													continue;
												}	
	    									}
	    									fi++;
	    									msgFmt = new MessageFormat(res.getString("objingr"));
	    									Object[] args7 = {new java.util.Date(), d.getPID(), new Integer(i+1).toString()};
	    									
	    									logger.write( msgFmt.format(args7));									                                    	
	                                    } else {
	                                    	if (d.getPID().contains(":"+(String)jcbUser.getSelectedItem()+".") || groups.contains("administrator")) {	                                    		
	                                    		msgFmt = new MessageFormat(res.getString("objingrrefr"));
	        									Object[] args3 = {new java.util.Date(), d.getPID(), new Integer(i).toString() };
	                                    		logger.write( msgFmt.format(args3));
	                                    		fa++;
	                                    	} else {	
	                                    		msgFmt = new MessageFormat(res.getString("denied"));
	        									Object[] args4 = {new java.util.Date(), (String)jcbUser.getSelectedItem(), d.getPID() };	                                    		
	                                    		logger.write( msgFmt.format(args4));
	                                    		continue;
	                                    	}	
	                                    }
								    }
								    Common.genQR(user, d.getPID());

	 							    try {														    
	 							    	if (!onlyValidate) {
	 							    		Repository.modifyDatastreamByValue(d.getPID(), "DC", "text/xml", d.toString());
	 							    		String url = d.getURL();
	 							    		if (url != null) {
	 							    			Repository.modifyDatastream(d.getPID(), "URL", null, "R", url);
	 							    		}   		
	 							    	}
	 							    } catch (Exception q) {}	

								}

								
							}
							
							progressDialog.setCanceled(true);
							
							logger.write("\n" +res.getString("end")+simulate+res.getString("ofingest") + new java.util.Date() + ". " + new Integer(fi).toString().trim() + res.getString("ingested")+ new Integer(fa).toString().trim() + res.getString("refreshed"));									
							logger.close();
							
							
							JOptionPane.showMessageDialog(  getCoreDialog(), new Integer(fi).toString()+ res.getString("ingested")+ new Integer(fa).toString().trim() + res.getString("refreshed") + res.getString("details")+""+System.getProperty( "file.separator" )+"ingest.log" , Common.WINDOW_HEADER, JOptionPane.INFORMATION_MESSAGE);
							logfile = new File(System.getProperty("java.io.tmpdir")).getAbsolutePath()+System.getProperty("file.separator")+"ingest.log"; 
							getGuiComposite().getWidget("jbShowLogfile").setEnabled(true);
							excel.destroy();

						}

						} else {
							MessageFormat msgFmt = new MessageFormat(res.getString("excelformat"));
							Object[] args = {loExcel.getTable()};
							JOptionPane.showMessageDialog(  getCoreDialog(), msgFmt.format(args), Common.WINDOW_HEADER, JOptionPane.INFORMATION_MESSAGE);
						}
						
					} catch (Exception ex) {
						try {
							logger.write(new java.util.Date()  +" "+ex.getLocalizedMessage()+"\n");
							logger.close();
						} catch (Exception ez) {
						}
					}
					finally {
						getCoreDialog().setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));					
					}
			
				}
			}.start();
			
		}
	

			
/*  			  			  		
		    	
	  			pid = "obj:bag."+idno.getText().substring(17).replace(":", ".");	  				  			
	  			
	            bibtex = "<file xmlns=\"http://bibtexml.sf.net/\">"+
	                        "<entry id=\""+pid+"\">"+  			
	                        "<book>"+
	                        "<author>"+author+"</author>"+
	                        "<title>"+title+"</title>"+
	                        "<type>"+issuance+"</type>"+
	                        "<publisher>"+publisher+"</publisher>"+
	                        "<owner>"+owner+"</owner>"+
	                        "<year>"+year+"</year>"+
	                        "<address>"+place+"</address>"+
	                        "</book>"+		
	                        "</entry>"+		
	                      "</file>";
	            
	*/
						
	
	/**
	 *  Description of the Method
	 *
	 * @param  aoHandler  Description of the Parameter
	 */
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
			se = (Session) CServiceProvider.getService( ServiceNames.SESSIONCLASS );						
			org.emile.cirilo.dialog.CBoundSerializer.load(this.getCoreDialog(), se.getIngestDialogProperties(), (JTable) null);
			CPropertyService props = (CPropertyService) CServiceProvider.getService( ServiceNames.PROPERTIES );
		    String p = props.getProperty("user", "METS.CreateFromJPEG"); 
		    createFromJPEG =  p != null && p.equals("1");		    
		    p = props.getProperty("user", "Ingest.ContentModel");
		    if (p != null) {
		    	JComboBox jcbContentModel = ((JComboBox) getGuiComposite().getWidget("jcbContentModel"));
		    	jcbContentModel.setSelectedItem(p);
		    	set();
		    }

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
			new DCMI().preallocate(moGA);
			
			CDialogTools.createButtonListener(this, "jbClose", "handleCancelButton");
			CDialogTools.createButtonListener(this, "jbReset", "handleResetButton");
			CDialogTools.createButtonListener(this, "jcbGenerated", "handleCBGenerated");
			CDialogTools.createButtonListener(this, "jbIngest", "handleIngestFromFilesystemButton");			
			CDialogTools.createButtonListener(this, "jbImport", "handleIngestFromExistButton");			
			CDialogTools.createButtonListener(this, "jbEXCEL", "handleIngestFromExcelButton");			
			CDialogTools.createButtonListener(this, "jbShowLogfile", "handleShowLogfileButton");			
			
			JComboBox jcbContentModel = ((JComboBox) getGuiComposite().getWidget("jcbContentModel"));			
			JComboBox jcbUser = ((JComboBox) getGuiComposite().getWidget("jcbUser"));
			JCheckBox jcbGenerated = ((JCheckBox) getGuiComposite().getWidget("jcbGenerated"));
			JTextField jtfPID = ((JTextField) getGuiComposite().getWidget("jtfPID"));

			
			groups = (ArrayList) CServiceProvider.getService(ServiceNames.MEMBER_LIST);
			user = (User) CServiceProvider.getService(ServiceNames.CURRENT_USER);
	
	        List<String> ds = Repository.getTemplates(user.getUser(),groups.contains("administrator"));                
	        for (String s: ds) {
	              if (!s.isEmpty() && (s.contains("TEI") || s.contains("MEI")  || s.contains("METS") || s.contains("LIDO")  || s.contains("Resource") || s.contains("cirilo:Ontology") )) jcbContentModel.addItem(s);            	
	         }
	           
            boolean contains = false;
	        List<String> users = Repository.getUsers();
	        for (String s : users) {
	        	    if (!s.isEmpty()) {
	        	    	jcbUser.addItem(s);
		        	    if (!contains) if (s.equals(user.getUser())) contains = true;
	        	    }
	         }
                   
	        if (!contains) jcbUser.addItem(user.getUser());
	        jcbUser.setSelectedItem(user.getUser());
	        
			jcbUser.setEnabled(groups.contains("administrator"));
			jtfPID.setEnabled(groups.contains("administrator"));
			jcbGenerated.setEnabled(groups.contains("administrator"));
			 
			 getGuiComposite().getWidget("jbShowLogfile").setEnabled(false);
	
			 new CItemListener((JComboBox) getGuiComposite().getWidget("jcbContentModel"), this, "handleCMComboBox");
			 set();
			
		} catch (Exception ex) {
			log.error(ex.getLocalizedMessage(),ex);	
			throw new COpenFailedException(ex);
		}
	}


	private void treeWalk(File file, boolean mode, String model) {
		   
		  try {
	 		if (file.isDirectory()) {
		 		File[] xml = file.listFiles(new XMLFilter());
		 		if (mode) createFromJPEG = (xml.length == 0);
			    if (xml.length == 0 && createFromJPEG) {
		 		    File[] images = file.listFiles(new JPGFilter());
		 		 	if (images.length > 0) {
		    	        FileOutputStream fos = new FileOutputStream( file.getAbsolutePath()+File.separator+"mets.xml" );
		    			BufferedWriter os = new BufferedWriter(new OutputStreamWriter( fos, "UTF-8" ) );
		    			os.write(book);
		    			os.close();
		 		 	}    
	 			}
		 		File[] children = file.listFiles();
		 		for (int i = 0; i < children.length; i++) {
		 			treeWalk(children[i],false, model);
		 		}
	     	} else if (file.getAbsolutePath().toLowerCase().endsWith(".xml") || file.getAbsolutePath().toLowerCase().endsWith(".docx")|| file.getAbsolutePath().toLowerCase().endsWith(".odt")) {
	     		files.add(file.getAbsolutePath());
	     		if (model.contains("Ontology")) {
					rdfs.set(file.getAbsolutePath());				
	     		}
	     		
	     	}
		  } catch (Exception e) {log.error(e.getLocalizedMessage(),e);	}	
	}	


	private void treeWalk(String URI, String path, String user, String passwd) {
       try {
		org.xmldb.api.base.Collection collection = DatabaseManager.getCollection( URI + path , user, passwd);
			try {
				String[] collections = collection.listChildCollections();			
				for (int i = 0; i < collections.length; i++) {
					treeWalk(URI, path+"/"+collections[i], user, passwd);
				}
			} catch (Exception ex) {
			}
			try {
				String[] members = collection.listResources();
				for (int i = 0; i < members.length; i++) {
					if (members[i].toLowerCase().contains(".xml"))
						files.add(path+"/"+members[i]);
				}
			} catch (Exception ex) {}
            			
			collection.close();			
		} catch (Exception  e){}
     }	
	
	 class JPGFilter implements FilenameFilter
	 {
	  public boolean accept( File f, String s )
	  {
			s=s.toLowerCase();
		    return (s.endsWith( ".jpg" ) || s.endsWith( ".jpeg" ) || s.endsWith( ".tif" ) || s.endsWith( ".tiff" )) && !s.startsWith(".");
	  }
	}

	class XMLFilter implements FilenameFilter
	 {
	  public boolean accept( File f, String s )
	  {
	    return s.toLowerCase().endsWith( ".xml" ) && !s.startsWith(".");
	  }
	}
	
	private static String book = "<book xmlns=\"http://gams.uni-graz.at/viewer\" xmlns:xlink=\"http://www.w3.org/1999/xlink\"><title>unknown</title><author>unknown</author><structure/></book>";
	 
	private static String importpath; 
	private static ArrayList<String> files;
	private static ArrayList<String> pids;
	private RDF rdfs;
	private String logfile;
	private static int fi;
	private CDefaultGuiAdapter moGA;
	private User user;
	private ArrayList<String> groups;
	private Session se;
	private String bibtex;
	private ResourceBundle res;
	private boolean createFromJPEG;
	
}

