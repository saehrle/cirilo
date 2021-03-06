package org.emile.cirilo.business;

import java.io.StringReader;
import java.net.URL;
import java.util.Iterator;
import java.util.List;
import java.net.URLConnection;

import org.apache.log4j.Logger;
import org.emile.cirilo.Common;
import org.emile.cirilo.ecm.repository.Repository;
import org.jdom.Element;
import org.jdom.input.DOMBuilder;
import org.jdom.input.SAXBuilder;
import org.jdom.output.Format;
import org.jdom.output.XMLOutputter;
import org.jdom.xpath.XPath;

public class ContextRefresher {

    private static Logger log = Logger.getLogger(ContextRefresher.class);

	public ContextRefresher() {};
	
	
	public boolean refreshKML(String pid, String tit) {
		try {
	        DOMBuilder db = new DOMBuilder();
	        org.jdom.Document doc = db.build (Repository.getDatastream(pid, "METADATA", new Integer(0)));

			XPath xpath = XPath.newInstance("/s:sparql/s:results/s:result[contains(s:model/@uri,'cm:TEI') or contains(s:model/@uri,'cm:LIDO')]");
			xpath.addNamespace( Common.xmlns_sparql );

									
			List nodes = (List) xpath.selectNodes( doc );
		   	if (nodes.size() > 0) {

				Format format = Format.getRawFormat();
				format.setEncoding("UTF-8");
				XMLOutputter outputter = new XMLOutputter(format);
		   		
				byte[] url =  Repository.getDatastream(pid, "KML_TEMPLATE" , "");

				SAXBuilder builder = new SAXBuilder(); 				
				URLConnection con = new URL (new String(url)).openConnection();
				con.setUseCaches(false);
		    	org.jdom.Document kml_template =builder.build(con.getInputStream());
		   														
				XPath qPath =  XPath.newInstance("//mm:metadata-mapping");
				qPath.addNamespace(Common.xmlns_mm);
				Element Placemark_template = (Element) qPath.selectSingleNode( kml_template );
				
				qPath =  XPath.newInstance("//k:Folder");
				qPath.addNamespace(Common.xmlns_kml);
				Element Folder = (Element) qPath.selectSingleNode( kml_template );
				
				Folder.getChild("name", Common.xmlns_kml).setText(tit);
   												
				for (Iterator iter = nodes.iterator(); iter.hasNext();) 
	    		{
	    			try {	
	    				Element e = (Element) iter.next();
	    				
	    				String uri = e.getChild("pid", Common.xmlns_sparql ).getAttributeValue("uri").substring(Common.INFO_FEDORA.length());
	    				String model = e.getChild("model", Common.xmlns_sparql ).getAttributeValue("uri").substring(Common.INFO_FEDORA.length());
	    				String title = e.getChildText("title", Common.xmlns_sparql);
	    				
	    				org.jdom.Document data = null;
	    				XPath oPath = null;
	    				
	    				if (model.contains("cm:TEI")) { 
	    					data= db.build (Repository.getDatastream(uri, "TEI_SOURCE"));
		    				oPath = XPath.newInstance("//t:placeName[contains(@xml:id,'GID.')]");
		    				oPath.addNamespace( Common.xmlns_tei_p5 );
		    				oPath.addNamespace( Common.xmlns_xml );
	    				}
	    				if (model.contains("cm:LIDO")) { 
	    					data= db.build (Repository.getDatastream(uri, "LIDO_SOURCE"));
		    				oPath = XPath.newInstance("//t:item[contains(@id,'GID.')]");
		    				oPath.addNamespace(Common.xmlns_tei_p5);
		    				oPath.addNamespace(Common.xmlns_lido);
		    				oPath.addNamespace(Common.xmlns_xml );
	    				}
	    					 
	    				List placeNames = (List) oPath.selectNodes( data );

	    				if (placeNames.size() > 0) {
	    					int i=0;
	    					for (Iterator jter = placeNames.iterator(); jter.hasNext();) 
	    		    		{
	    		    			try {	
	    		    				Element place = (Element) jter.next();
    		    					String p =  outputter.outputString(Placemark_template);
    		    					i++;
    		    					MDMapper m = new MDMapper (uri, p);	
    		    					
    		    					Element o = (Element) place.clone();    		    					
    		    					Element q = new Element("xmlContent");
    		    					q.addContent((Element)data.getRootElement().clone());
    		    					o.addContent(q);
    		    					
    		    					String s = m.transform(new org.jdom.Document(o));
     		    				    org.jdom.Document Placemark = builder.build(new StringReader(s));
     		    				    Element name =  Placemark.getRootElement().getChild("name",Common.xmlns_kml);
     		    				    if (name == null) {
     		    				    	name = new Element ("name", Common.xmlns_nkml);
     		    				    	name.setText(title);
     		    				    	Placemark.getRootElement().addContent(0,name);
     		    				    } else {
     		    				    	name.setText(title);
     		    				    }
    		    					Folder.addContent((Element)Placemark.getRootElement().clone());
	    		    			} catch (Exception r) {
	    						  	log.error(r.getLocalizedMessage(),r);		  
	    		    			}
	    		    		}	
	    				}
	 
	    				
					} catch (Exception q) {
					  	log.error(q.getLocalizedMessage(),q);		  
					}
	                
	    		}		
								
				Placemark_template.getParent().removeContent(Placemark_template);
		   		Repository.modifyDatastreamByValue(pid, "KML", "text/xml",outputter.outputString(kml_template).replace("<k:", "<").replace("</k:", "</").replace("xmlns:k", "xmlns"));
		        return true;
		   	}
		   	
		   	return false;
		   	
		} catch (Exception e) {
		  	log.error(e.getLocalizedMessage(),e);		  
			return false;
		}
		
		
	}


}
