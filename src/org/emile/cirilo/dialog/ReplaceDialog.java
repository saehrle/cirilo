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


import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.emile.cirilo.ServiceNames;
import org.emile.cirilo.User;
import org.emile.cirilo.ecm.templates.*;
import org.emile.cirilo.ecm.repository.*;
import org.emile.cirilo.utils.Split;
import org.emile.cirilo.business.*;
import org.emile.cirilo.*;

import com.asprise.util.ui.progress.ProgressDialog;

import org.jdom.input.SAXBuilder;
import org.jdom.xpath.XPath;
import org.jdom.output.*;
import org.jdom.input.*;
import org.jdom.*;

import java.awt.Cursor;
import java.awt.event.*;

import javax.swing.*;

import java.util.*;
import java.io.*;
import java.awt.Color;



/**
 *  Description of the Class
 *
 * @author     Johannes Stigler
 * @created    10.3.2011
 */
public class ReplaceDialog extends CDialog {
    private static final Log LOG = LogFactory.getLog(ReplaceDialog.class);

	/**
	 *  Constructor for the LoginDialog object
	 */

	public ReplaceDialog() {}

	
	public void handleStylesheetButton( ActionEvent ev ) {
		try {
	
			SelectLayoutDialog dlg = (SelectLayoutDialog) CServiceProvider.getService(DialogNames.SELECTLAYOUT_DIALOG);
			dlg.set(null, "STYLESHEET", null, "yoda");
			dlg.open();
			if (!dlg.getLocation().isEmpty()) ((JTextField) getGuiComposite().getWidget("jtfStylesheet")).setText(dlg.getStylesheet());
			((JTextField) getGuiComposite().getWidget("jtfStylesheet")).setCaretPosition(0);
		}
		catch ( Exception e ) {
		}
	}

	public void handleFO_StylesheetButton( ActionEvent ev ) {
		try {
	
			SelectLayoutDialog dlg = (SelectLayoutDialog) CServiceProvider.getService(DialogNames.SELECTLAYOUT_DIALOG);
			dlg.set(null, "FO_STYLESHEET", null, "yoda");
			dlg.open();
			if (!dlg.getLocation().isEmpty()) ((JTextField) getGuiComposite().getWidget("jtfFO_Stylesheet")).setText(dlg.getStylesheet());
			((JTextField) getGuiComposite().getWidget("jtfFO_Stylesheet")).setCaretPosition(0);
		}
		catch ( Exception e ) {
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
			getGuiComposite().getWidget( "jbSaveRelations" ).setEnabled( !lma.isEmpty() );
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
		}
		catch ( Exception e ) {
		}
	}

	/**
	 *  Description of the Method
	 *
	 * @param  ev  Description of the Parameter
	 */
	public void handleReplaceButton( ActionEvent ev ) {
		try {
			JList ltRels = (JList) getGuiComposite().getWidget( "jtRelations" );
			DefaultListModel lm = (DefaultListModel) ltRels.getModel();
			
			ArrayList<String> substitutions = new ArrayList<String>();
			for (int i=0; i<Common.DCMI.length;i++) {
				substitutions.add(Common.DUBLIN_CORE + new Integer(((JComboBox)getGuiComposite().getWidget( "jcb"+Common.DCMI[i])).getSelectedIndex()).toString() + ((JTextField) getGuiComposite().getWidget("jtf"+Common.DCMI[i])).getText());
			}
			substitutions.add(Common.OAIPROVIDER + new Integer(((JComboBox)getGuiComposite().getWidget( "jcbOAI")).getSelectedIndex()).toString() + (((JCheckBox) getGuiComposite().getWidget("jcbOAIProvider")).isSelected() ? "true":"false"));
			substitutions.add(Common.HTML_LAYOUT + new Integer(((JComboBox)getGuiComposite().getWidget( "jcbStylesheet")).getSelectedIndex()).toString() + ((JTextField) getGuiComposite().getWidget("jtfStylesheet")).getText());
			substitutions.add(Common.FO_LAYOUT + new Integer(((JComboBox)getGuiComposite().getWidget( "jcbFO_Stylesheet")).getSelectedIndex()).toString() + ((JTextField) getGuiComposite().getWidget("jtfFO_Stylesheet")).getText());
			substitutions.add(Common.QUERY + new Integer(((JComboBox)getGuiComposite().getWidget( "jcbQueries")).getSelectedIndex()).toString() + ((JTextArea) getGuiComposite().getWidget("jtaQueries")).getText());
			substitutions.add(Common.OWNER + new Integer(((JComboBox)getGuiComposite().getWidget( "jcbOwner")).getSelectedIndex()).toString() + ((JComboBox) getGuiComposite().getWidget("jcbUser")).getSelectedItem().toString());
			substitutions.add(Common.XSLT + new Integer(((JComboBox)getGuiComposite().getWidget( "jcbXSLStylesheet")).getSelectedIndex()).toString() +((JTextField) getGuiComposite().getWidget("jtfXSLStylesheet")).getText()+Common.SEPERATOR+((JComboBox) getGuiComposite().getWidget("jcbDatastreams")).getSelectedItem().toString());
			substitutions.add(Common.DCMAPPING + new Integer(((JComboBox)getGuiComposite().getWidget( "jcbDCMapping")).getSelectedIndex()).toString() + (((JCheckBox) getGuiComposite().getWidget("jcbDCMIMapping")).isSelected() ? "true":"false"));
			substitutions.add(Common.DC_MAPPING + new Integer(((JComboBox)getGuiComposite().getWidget( "jcbDC_MAPPING")).getSelectedIndex()).toString() + ((JTextField) getGuiComposite().getWidget("jtfDC_MAPPING")).getText());
			substitutions.add(Common.RDF_MAPPING + new Integer(((JComboBox)getGuiComposite().getWidget( "jcbRDF_MAPPING")).getSelectedIndex()).toString() + ((JTextField) getGuiComposite().getWidget("jtfRDF_MAPPING")).getText());
			substitutions.add(Common.BIBTEX_MAPPING + new Integer(((JComboBox)getGuiComposite().getWidget( "jcbBIBTEX_MAPPING")).getSelectedIndex()).toString() + ((JTextField) getGuiComposite().getWidget("jtfBIBTEX_MAPPING")).getText());
			substitutions.add(Common.KML_TEMPLATE + new Integer(((JComboBox)getGuiComposite().getWidget( "jcbKML_TEMPLATE")).getSelectedIndex()).toString() + ((JTextField) getGuiComposite().getWidget("jtfKML_TEMPLATE")).getText());
			substitutions.add(Common.REPLACEMENT_RULESET + new Integer(((JComboBox)getGuiComposite().getWidget( "jcbREPLACEMENT_RULESET")).getSelectedIndex()).toString() + ((JTextField) getGuiComposite().getWidget("jtfREPLACEMENT_RULESET")).getText());
			substitutions.add(Common.TORDF + new Integer(((JComboBox)getGuiComposite().getWidget( "jcbTORDF")).getSelectedIndex()).toString() + ((JTextField) getGuiComposite().getWidget("jtfTORDF")).getText());
			substitutions.add(Common.TOMETS + new Integer(((JComboBox)getGuiComposite().getWidget( "jcbTOMETS")).getSelectedIndex()).toString() + ((JTextField) getGuiComposite().getWidget("jtfTOMETS")).getText());

			for(int i=0; i < lm.size(); i++) {
				substitutions.add(Common.RELATIONS + new Integer(((JComboBox)getGuiComposite().getWidget( "jcbRels")).getSelectedIndex()).toString() + (String)lm.getElementAt(i));
			}
			
			oParent.handleObjectReplace(substitutions);
		}
		catch ( Exception e ) {
			e.printStackTrace();
		}
	}
	
	
	public void handleXSLStylesheetButton(ActionEvent e) {
		try {
			CPropertyService props = (CPropertyService) CServiceProvider.getService(ServiceNames.PROPERTIES);
			JFileChooser chooser = new JFileChooser(props.getProperty("user", "ingest.stylesheet.path"));

			chooser.setDialogTitle(res.getString("choosestyle"));
			chooser.addChoosableFileFilter(new FileFilter(".xsl"));

			if (chooser.showDialog(getCoreDialog(), res.getString("choose")) != JFileChooser.APPROVE_OPTION) {
				return;
			}

			props.setProperty("user", "ingest.stylesheet.path",chooser.getSelectedFile().getAbsolutePath());
			props.saveProperties("user");			
			((JTextField) getGuiComposite().getWidget("jtfXSLStylesheet")).setText(chooser.getSelectedFile().getAbsolutePath());

		} catch (Exception ex) {		
		}
	}

	
	/**
	 *  Description of the Method
	 *
	 * @param  e  Description of the Parameter
	 */
	public void handleCancelButton(ActionEvent e) {
		org.emile.cirilo.dialog.CBoundSerializer.save(this.getCoreDialog(), se.getReplaceDialogProperties(), (JTable) null);   
		close();
	}
		
	public void handleSeekButton(ActionEvent e) {
		  try {
 			    seekString =  (String) ((JTextField) getGuiComposite().getWidget("jtfSeek")).getText().trim();
			    props.setProperty("user", "relations.seekterm", seekString);
				props.saveProperties("user");
			    refresh();		   
			   
		    } catch (Exception ex) {}					  
	}

	public void setParent(EditObjectDialog dlg) {
		this.oParent = dlg;
	}
	
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

	public void refresh() {
		  try {
			   getCoreDialog().setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
				JList ltRels = (JList) getGuiComposite().getWidget( "jtRelations" );
				DefaultListModel lma = (DefaultListModel) ltRels.getModel();
				JList ltNonRels = (JList) getGuiComposite().getWidget( "jtNonRelations" );
				DefaultListModel lmb = (DefaultListModel) ltNonRels.getModel();
                lmb.removeAllElements();

                java.util.List<String> containers = Repository.getContainers(user.getUser(), groups.contains( "administrator") );
         			        
  	        
				HashMap coll = new HashMap();
				for (int i = 0; i <lma.getSize();i++) {
						Split id = new Split(lma.get(i).toString());
	                	coll.put(id.get(),"");
				}
					
		        SortedSet<String> sortedset= new TreeSet<String>(containers);
		        
				HashMap<String,String> hm = new HashMap();
		        SortedSet<String> hma= new TreeSet<String>();
			
				for (String s: sortedset) {
					  Split id = new Split(s);
	                  hm.put(id.get(), s);
	                  hma.add(id.get());
				}
				
				for (  String s: hma) {
					if (coll.get(s) == null ) {
						 if (hm.get(s).contains(seekString.replace("*",""))) {
							 lmb.addElement(hm.get(s));
						 }						 
					}
				}
	
							
				ltRels.setSelectedIndex( 0 );
	            ltNonRels.setSelectedIndex( 0 );
	
				moGA.setData("jtfSeek", seekString);

		    } catch (Exception ex) {
		    }
			finally {
				getCoreDialog().setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
			}

	}

	
	public void show()
	 throws CShowFailedException {
		try {
			se = (Session) CServiceProvider.getService( ServiceNames.SESSIONCLASS );						
			org.emile.cirilo.dialog.CBoundSerializer.load(this.getCoreDialog(), se.getReplaceDialogProperties(), (JTable) null);
			
			seekString = props.getProperty("user", "relations.seekterm");
			moGA.setData("jtfSeek", seekString);
		    refresh();
	
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

			groups = (ArrayList) CServiceProvider.getService( ServiceNames.MEMBER_LIST );
			user = (User) CServiceProvider.getService(ServiceNames.CURRENT_USER);
			
			CDialogTools.createButtonListener(this, "jbClose", "handleCancelButton");
			CDialogTools.createButtonListener(this, "jbAddRelation", "handleAddRelationButton");
			CDialogTools.createButtonListener(this, "jbRemoveRelation", "handleRemoveRelationButton");
			CDialogTools.createButtonListener(this, "jbReplace", "handleReplaceButton");
			CDialogTools.createButtonListener(this, "jbSeek", "handleSeekButton");			
			CDialogTools.createButtonListener(this, "jbStylesheet", "handleStylesheetButton");
			CDialogTools.createButtonListener(this, "jbFO_Stylesheet", "handleFO_StylesheetButton");
			CDialogTools.createButtonListener(this, "jbXSLStylesheet", "handleXSLStylesheetButton");

			props = (CPropertyService) CServiceProvider.getService(ServiceNames.PROPERTIES);			
			((JTextField) getGuiComposite().getWidget("jtfXSLStylesheet")).setText(props.getProperty("user", "ingest.stylesheet.path"));							

			
			JList ltRels = (JList) getGuiComposite().getWidget( "jtRelations" );
			JList ltNonRels = (JList) getGuiComposite().getWidget( "jtNonRelations" );
/*			
	        List<String> containers = Repository.getContainers("yoda", true);

	        SortedSet<String> sortedset= new TreeSet<String>(containers);

			HashMap<String,String> hm = new HashMap();
	        SortedSet<String> hma= new TreeSet<String>();
		
			for (String s: sortedset) {
				  Split id = new Split(s);
                  hm.put(id.get(), s);
                  hma.add(id.get());
			}
			        
			DefaultListModel lm = (DefaultListModel) ltNonRels.getModel();
			for (String s: hma) {
                  lm.addElement(hm.get(s));
			}
			ltRels.setSelectedIndex( 0 );
            ltNonRels.setSelectedIndex( 0 );
*/

            JComboBox jcbUser = ((JComboBox) getGuiComposite().getWidget("jcbUser"));
	        List<String> users = Repository.getUsers();
	        for (String s : users) {
	        	    if (!s.isEmpty()) jcbUser.addItem(s);
	         }

	        
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
			ex.printStackTrace();
			throw new COpenFailedException(ex);
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
	

	private CDefaultGuiAdapter moGA;
	private CPropertyService props;
	private User user;
	private ArrayList<String> groups;
	private Session se;
	private EditObjectDialog oParent;
    private String seekString;
    private ResourceBundle res;
	
}
