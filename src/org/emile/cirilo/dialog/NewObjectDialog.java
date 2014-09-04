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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.emile.cirilo.Common;
import org.emile.cirilo.ServiceNames;
import org.emile.cirilo.ecm.templates.*;
import org.emile.cirilo.ecm.repository.*;
import org.emile.cirilo.business.*;
import org.emile.cirilo.utils.*;
import org.emile.cirilo.*;

import com.asprise.util.ui.progress.ProgressDialog;

import org.jdom.input.DOMBuilder;
import org.jdom.input.SAXBuilder;
import org.jdom.xpath.XPath;
import org.jdom.output.*;
import org.jdom.input.*;
import org.jdom.*;
import org.jdom.transform.*;
import org.openrdf.repository.config.RepositoryConfig;
import org.openrdf.repository.config.RepositoryImplConfig;
import org.openrdf.repository.manager.RemoteRepositoryManager;
import org.openrdf.repository.sail.config.SailRepositoryConfig;
import org.openrdf.sail.nativerdf.config.NativeStoreConfig;

import javax.xml.transform.stream.*;

import java.awt.Cursor;
import java.awt.event.*;

import javax.swing.*;
import javax.xml.transform.*;

import java.text.MessageFormat;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.io.*;
import java.awt.Color;

import net.glxn.qrgen.QRCode;
import net.glxn.qrgen.image.ImageType;



/**
 *  Description of the Class
 *
 * @author     Johannes Stigler
 * @created    10.3.2011
 */
public class NewObjectDialog extends CDialog {
    private static final Log LOG = LogFactory.getLog(NewObjectDialog.class);

	/**
	 *  Constructor for the LoginDialog object
	 */

	public NewObjectDialog() {}

	
	/**
	 *  Description of the Method
	 *
	 * @param  e  Description of the Parameter
	 */
	public void handleCancelButton(ActionEvent e) {
		org.emile.cirilo.dialog.CBoundSerializer.save(this.getCoreDialog(), se.getNewDialogProperties(), (JTable) null);   
		close();
	}
		    
	public void set(EditObjectDialog dlg, String pid, String owner) {
		this.dlg = dlg;
		this.pid = pid;
		this.owner = owner;
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
			reset();
			
			new DCMI().reset(moGA);
		} catch (Exception ex) {			
		}
			
	}


	 
	private void reset() {
		try {
			JTextField jtfPID = ((JTextField) getGuiComposite().getWidget("jtfPID"));
			JTextField jtfTitle = ((JTextField) getGuiComposite().getWidget("jtfTitle"));
			JComboBox jcbNamespace = ((JComboBox) getGuiComposite().getWidget("jcbNamespace"));
			JComboBox jcbUser = ((JComboBox) getGuiComposite().getWidget("jcbUser"));
			JComboBox jcbContentModel = ((JComboBox) getGuiComposite().getWidget("jcbContentModel"));			
			jcbNamespace.setEnabled(false);			
			
			jcbUser.setSelectedItem(user.getUser());
		
            String cm = jcbContentModel.getSelectedItem().toString().toLowerCase();
			if (cm.contains("context") || cm.contains("query")) {
				jcbNamespace.setSelectedIndex(cm.contains("context") ? 1 : 2);
				jcbNamespace.setEnabled(true);
				jtfPID.setBackground( Color.YELLOW );
				jtfPID.setEnabled(true);
				jtfPID.requestFocus();
			} else  {
				jcbNamespace.setSelectedIndex(0);
				if (!groups.contains("administrator")) {
					jtfPID.setBackground( new Color (238,238,238) );
					jtfPID.setText("");
					jcbNamespace.setEnabled(false);
					jtfPID.setEnabled(false);
					jtfTitle.requestFocus();
				}
			}
			
		} catch (Exception ex) {		
		}
		
	}

	/**
	 *  Description of the Method
	 *
	 * @param  e  Description of the Parameter
	 */
	public void handleCreateButton(ActionEvent e) {
		
		String model = "";
		String x = "";
		try {
			TemplateSubsystem temps = (TemplateSubsystem) CServiceProvider.getService(ServiceNames.TEMPLATESUBSYSTEM);
			JComboBox jcbContentModel = ((JComboBox) getGuiComposite().getWidget("jcbContentModel"));            
			JTextField jtfPID = ((JTextField) getGuiComposite().getWidget("jtfPID"));
			JComboBox jcbNamespace = ((JComboBox) getGuiComposite().getWidget("jcbNamespace"));
			JComboBox jcbUser = ((JComboBox) getGuiComposite().getWidget("jcbUser"));
			model = (String)jcbContentModel.getSelectedItem();

			
			String pid = ""; 
					
			 if (((String)jcbNamespace.getSelectedItem()).contains("context")||((String)jcbNamespace.getSelectedItem()).contains("container")||((String)jcbNamespace.getSelectedItem()).contains("query")) {
					pid= (String)jcbNamespace.getSelectedItem()+(String)jtfPID.getText().trim(); 					
			 } else if (((String)jtfPID.getText()).startsWith("$")) {
					pid= "$"+(String)jcbNamespace.getSelectedItem()+(String)jtfPID.getText().substring(1);
			} else {
				pid = ((String)jcbNamespace.getSelectedItem()+(String)jcbUser.getSelectedItem()).trim();
			}
			 
			 
			if (Repository.exist(pid.substring(1))) {
				
				MessageFormat msgFmt = new MessageFormat(res.getString("double"));
				Object[] args = {pid.substring(1)}; 
				JOptionPane.showMessageDialog (getCoreDialog(), msgFmt.format(args), Common.WINDOW_HEADER, JOptionPane.INFORMATION_MESSAGE);
				return;
			}
			getCoreDialog().setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
			
			Split pcm = new Split(model);
			pid = temps.cloneTemplate("info:fedora/"+pcm.get(),(String)jcbUser.getSelectedItem(), pid, (String) null);
			
			DCMI dc = new DCMI();
			dc.save(moGA);
			while (!Repository.exist(pid));
			
			Common.genQR(user, pid);
			dc.write(pid, moGA, ((JCheckBox) getGuiComposite().getWidget("jcbOAIProvider")).isSelected());

/*			
			if (model.contains("Ontology") || model.contains("SKOS")) {                
			   	String seseameServer = new String( Repository.getDatastream(pid, "REPOSITORY", "") );				   
			   	Pattern pattern = Pattern.compile("(.*)/(.*)/(.*)"); 
			   	Matcher m = pattern.matcher(seseameServer); 
			   	if (m.find()) {
			   		RemoteRepositoryManager manager = new RemoteRepositoryManager(m.group(1));
			   		manager.setUsernameAndPassword("fedora", "");
			   		manager.initialize();
			   		RepositoryImplConfig repositoryTypeSpec = new SailRepositoryConfig(new NativeStoreConfig());								
			   		RepositoryConfig config = new RepositoryConfig(pid.substring(2), repositoryTypeSpec);
			   		manager.addRepositoryConfig(config);
			   	}
		   }		
			
*/			if (dlg != null) dlg.refresh();
			
			MessageFormat msgFmt = new MessageFormat(res.getString("objowner"));
			Object[] args = {model, (String)jcbUser.getSelectedItem(), pid}; 
					  			
			JOptionPane.showMessageDialog (getCoreDialog(), msgFmt.format(args), Common.WINDOW_HEADER, JOptionPane.INFORMATION_MESSAGE);

			
		} catch (Exception ex) {
			if (model.contains("Context") || model.contains("Query")) {
				
				MessageFormat msgFmt = new MessageFormat(res.getString("errcrea"));
			    Object[] args = {model};
				
				
				JOptionPane.showMessageDialog (getCoreDialog(), msgFmt.format(args)+res.getString("nopid"), Common.WINDOW_HEADER, JOptionPane.INFORMATION_MESSAGE); }
			
				else {
					MessageFormat msgFmt = new MessageFormat(res.getString("errcrea"));
				    Object[] args = {model};
					
					JOptionPane.showMessageDialog (getCoreDialog(),msgFmt.format(args), Common.WINDOW_HEADER, JOptionPane.INFORMATION_MESSAGE); /*   !"+x+pid+ex.getMessage());	*/		
		}}
		finally {
			getCoreDialog().setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
		}
	}

	public void handleCMComboBox(ItemEvent e)
	throws Exception {

		if (e.getStateChange() == 1) {
			reset(); 
		}
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

	public void show()
	 throws CShowFailedException {
		try {
			se = (Session) CServiceProvider.getService( ServiceNames.SESSIONCLASS );						
			org.emile.cirilo.dialog.CBoundSerializer.load(this.getCoreDialog(), se.getNewDialogProperties(), (JTable) null);
//          DCMI x = new DCMI("/Users/yoda/xo/quetzal/src/org/emile/quetzal/ruleset.xml");
//			SAXBuilder builder = new SAXBuilder();
//			x.map(builder.build("/Users/yoda/xo/quetzal/src/org/emile/quetzal/tei.xml"));
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
			props = (CPropertyService) CServiceProvider.getService( ServiceNames.PROPERTIES );
	
			if (pid==null) new DCMI().preallocate(moGA);
			
			CDialogTools.createButtonListener(this, "jbClose", "handleCancelButton");
			CDialogTools.createButtonListener(this, "jbReset", "handleResetButton");
			CDialogTools.createButtonListener(this, "jbSave", "handleCreateButton");			
			
			JComboBox jcbContentModel = ((JComboBox) getGuiComposite().getWidget("jcbContentModel"));			
			JComboBox jcbUser = ((JComboBox) getGuiComposite().getWidget("jcbUser"));
			JTextField jtfPID = ((JTextField) getGuiComposite().getWidget("jtfPID"));
			
			groups = (ArrayList) CServiceProvider.getService(ServiceNames.MEMBER_LIST);
			user = (User) CServiceProvider.getService(ServiceNames.CURRENT_USER);
			
            List<String> ds = Repository.getTemplates(user.getUser(),groups.contains("administrator"));                
            for (String s: ds) {
                if (!s.isEmpty()) jcbContentModel.addItem(s);            	
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
	        
	        
            String cm =  props.getProperty("user", "General.DefaultContentModel");
            jcbContentModel.setSelectedItem(cm); 
            if (jcbContentModel.getSelectedIndex() == -1) jcbContentModel.setSelectedIndex(0); 
            
			jcbUser.setEnabled(groups.contains("administrator"));
			jtfPID.setEnabled(groups.contains("administrator"));
	
			new CItemListener((JComboBox) getGuiComposite().getWidget("jcbContentModel"), this, "handleCMComboBox");
			reset();
			
			jcbUser.setSelectedItem(user);
			
		} catch (Exception ex) {
			ex.printStackTrace();
			throw new COpenFailedException(ex);
		}
	}

	
	private CDefaultGuiAdapter moGA;
	private User user;
	private EditObjectDialog dlg;
	private ArrayList<String> groups;
	private Session se;
    private String pid;	
    private String owner;	
    private ResourceBundle res;
    private CPropertyService props;
	
}
