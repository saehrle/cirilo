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

package org.emile.cirilo;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JLabel;

public class CiriloResources_en extends java.util.ListResourceBundle {

    public Object[][] getContents() { return contents; }

    static final Object[][] contents = 
        {
    	/* gui*/
           { "user"          , "User"         },
           { "passwd"      , "Password"        },
           { "login"          , "Login"          },
           { "cancel"      , "Cancel"       },
           { "pool"         , "Datapool"         },
           { "yes"      , "Yes"       },
           { "no"         , "No"         },
           { "close"         , "Close"       },
           { "open"          , "Open"          },
           { "save"         , "Save"         },
           { "saveas"         , "Save as"         },
           { "saveds"         , "Save datastream"         },
           { "errsave"         , "Error while saving file" },
           { "saveok"         ,  "Datastream {0} of object {1} successfully written to file {2}." },           
           { "submit"         , "Submit"         },
           { "edit"         , "Edit"         },
           { "replace"          , "Replace"          },
           { "export"          , "Export"          },
           { "delete"          , "Delete"          },
           { "new"          , "New"          },
           { "search"          , "Search"          },
           { "updatereg"          , "Update index"          },
           { "geo"          , "Aggregate"          },
           { "checkoai"          , "Released for OAI-Harvester "          },
           { "active"          , "Active"          },
           { "inactive"          , "Inactive"          },
           { "ingestsim"          , "Simulate ingest"          },
           { "ingestdir"          , "From filesystem"          },
           { "ingestex"          , "From eXist"          },
           { "ingestexcel"          , "From Excel"          },
           { "validate"          , "Validate"          },
           { "showlog"          , "Show log"          },
           { "reset"          , "Reset"          },
           { "apply"          , "Apply"          },
           { "create"          , "Create"          },
           { "add"          , "Add"          },
           { "prop"          , "Properties"          },
           { "streams"          , "Content datastreams"          },
           { "sysdata"          , "System datastreams"          },
           { "rels"          , "Relations"          },
           { "appear"          , "Appears in:"          },
           { "sysprop"          , "System properties"          },
           { "dcfromtei"          , "Refresh object from source"          },
           { "transfs"          , "Transformations"          },
           { "streamid"          , "Datastream-ID"          },
           { "file"          , "File"          },
           { "transform"          , "Transform"          },
           { "simulate"          , "Simulate"          },
           { "unmod"          , "-"          },
           { "sourcedir"          , "Source directory"          },
           { "targetdir"          , "Target directory"          },
           { "noconn", "Connection to repository could not be established."},
           { "server", "Fedora-Server"},
           { "imageserver", "Imageserver"},
           { "context", "Context"},
           { "protocol", "Protocol"},
           { "url", "URL"},
           { "home", "Home directory"},
           { "maketemplate", "Add as template"},
           { "extras.createenvironment", "Create environment ..."},
           { "extras.upgrade", "Upgrade repository ..."},
           { "extras.reorganize", "Reorganize triplestore ..."},
           { "english", "English"},
           { "german", "German"},
           { "language", "Language"},
           { "harvest", "Collect"},
           { "general", "General"},
           { "teiupload", "Upload of TEI documents"},
           { "meiupload", "Upload of MEI documents"},
           { "metsupload", "Upload of METS documents"},
           { "skosupload", "Upload of SKOS documents"},
           { "lidoupload", "Upload of LIDO documents"},
           { "defaultcm", "Default Content Model"},
           { "preferences", "Preferences"},
           { "texteditor", "Texteditor"},
           { "login", "Login"},
           { "loginiips", "Imageserver Login"},
               
           { "dcmapping", "Extract Dublin Core metadata"},
           { "semextraction", "Apply policy for extracting semantic constructs"},
           { "removeempties", "Remove empty elements without attributes"},
           { "createcontexts", "Create context objects"},
           { "resolveregex", "Resolve regular expressions"},
           { "resolvegeoids", "Resolve placeName elements against geonames.org"},
           { "ingestimages", "Upload images"},
           { "resolveskos", "Resolve SKOS concepts"},
           { "customization", "Execute TEI customization"},
           { "refreshsource", "Overwrite source document with expanded content"},
           { "skosify", "Normalize SKOS datastreams via Skosify"},
           { "createfromjpeg", "Create METS documents from image directories without XML metadata "},
           { "geonameslogin", "Geonames webservice login name"},
           { "onlygeonameids", "Accept elements with geonames.org ID exclusively"},
   
          
           /* dialog */
           { "import"          , "Import"          },
           { "noedit"          , "No edit options for this content model available."          },
           { "choosefile"          , "Choose file"          },
           { "choose"          , "Choose"          },
           { "errimport"          , "Error while importing {0}. File could not be validated correctly."          },
           { "show"          , "Show"          },           
           { "update"      , "Datastream {0} from object {1} has been successfully updated with the content of {2}."   },
           { "objmodsuc"          , "{0} object(s) modified successfully."          },
           { "chooseedir"          , "Choose export directory"          }, 
           { "objexsuc"          , "{0} object(s) exported successfully."          },
           { "addcont"          , "Adding..."          },
           { "geosuc"          , "{0} context object(s) updated successfully."          },
           { "regsuc"          , "Index of {0} object(s) updated successfully."          },
           { "refresh"          , "Refresh"          },
           { "replaceobjc"          , "Replace object content"          },
           { "objmod"          , "This process modifies {0} object(s). Are you sure you want to continue?"          },
           { "objdel"          , "This process deletes {0} object(s) from the repository. Are you sure you want to continue?" },
           { "objdelsuc"          , "{0} object(s) deleted from repository."          },
           { "objex"          , "This process exports {0} object(s) from the repository. Are you sure you want to continue?" },
           { "file.export"          , "Export objects"          },
           { "refreshcont"          , "Refreshing..."          },
           { "chooseidir"          , "Choose ingest directory"          },
           { "chooseimdir"          , "Choose import directory"          },
           { "objcrea"          , "This process creates {0} object(s) based on Content Model {1} from source {2} in the repository. Are you sure you want to continue?" },
           { "objimp"          , "This process creates {0} object(s) from source {1} in the repository. Are you sure you want to continue?" },
           { "askharv"          , "This process collects metadata from {0} providers.  Are you sure you want to continue?" },
           { "ingestcont"          , "Ingesting ..."          },
           { "harvcont"          , "Harvesting ..."          },
           { "start"          , " Start"          },
           { "end"          , " End"          },
           { "ofsim"          , " of simulation"          },
           { "ofingest"          , " of ingest: "          },
           { "ofimport"          , " of import: "          },
           { "novalidrtei"          ,"\n{0}. Template does not generate valid TEI-markup for record {1}. "},
           { "novalidtei"          ,"\n{0}. File {1} does not contain valid TEI-markup. "},
           { "createfail"          ,"\n{0}. Object {1} couldn't created. "},
           { "objing"          , "\n{0}. Object {1} based on file {2} has been created. "   },
           { "objingrefr"          , "\n{0}. Object {1} based on file {2} has been refreshed. "   },
           { "objingr"          , "\n{0}. Object {1} based on record {2} has been created. "   },
           { "objingrrefr"          , "\n{0}. Object {1} based on record {2} has been refreshed. "   },
           { "denied"          , "\n{0}. Access of user {1} to object {2} has been denied. "   },
           { "novalidrmets"          ,"\n{0}. Template does not generate valid METS-markup for record {1}. "},
           { "novalidmets"          ,"\n{0}. File {1} does not contain valid METS-markup. "},
           { "ingested"          , " object(s) ingested, "          },
           { "imported"          , " object(s) imported, "          },
           { "existed"          , " object(s) already exist(s) and "          },
           { "failed"          , " file(s) do(es) not have a valid FEDORA object schema."          },
           { "refreshed"          , " refreshed. "          },
           { "details"          , "For details on the harvesting process please see the log at "          },
           { "invalauthor"          , "Invalid authorisation!"          },
           { "invalauthent"          , "Invalid authentication!"          },
           { "double"          , "A data object with the persistent identifier {0} is already existing."          },
           { "objowner"          , "A data object with the content model {0} and owner rights for {1} has been created with the persistent identifier {2}."          },
           { "errcrea"          , "Error while creating a new data object with the content model {0}."          },
           { "nopid"          , "No persistent identifier declared."          },
           { "choosesdir"          , "Choose source directory"          }, 
           { "choosetdir"          , "Choose target directory"          }, 
           { "choosedirstyle"          , " Please choose a source and target directory and a XSL-stylesheet."          }, 
           { "object"          , "Object"          },
           { "oftrans"          , " of transformation"          },
           { "transcont"          , "Transforming..."          },
           { "objmodf3"          , "This process modifies {0} object(s) ({1} in {2}.*) in the repository. Are you sure you want to continue?"          },
           { "objmode"          , "This process creates {0} file(s) in directory {1}. Are you sure you want to continue?"          },
           { "log"          , "{0} file(s) created, {1} resulted in error."          },
           { "addrel"          , "Adding of datastream RELS-EXT ..."         },
           { "relsuc"          , "{0} object(s) updated with datastream RELS-EXT."         },
           { "envok"          , "System environment for user {0} has been created"         },
           { "choosetemp"          , "Choose template"         },
           { "choosesource"          , "Choose source"         },
           { "ingest"          , "Ingest"         },
           { "import"          , "Import"         },
           { "evaluate"          , "Evaluate"         },
           { "showtrip"          , "Show triples"         },
           { "results"          , "Results"         },
           { "exceltable"          , "Excel spreadsheet"         },
           { "teitemp"          , "Template"         },
           { "provider",            "Provider" },
           { "baseurl",            "Base URL" },
           { "prefix",            "Metadataprefix" },
           { "updated",            "Last update" },
           { "relsintsuc",            "Datastream RELS-INT of object {0} updated successfully." },
           { "valerror",            "Validation error" },
           { "creatingobject",            " Creating object " },
           { "objectnotfound",            "Couldn't found object {0}." },
           { "imagenotfound",            "Couldn't found image datastream {0} for object {1}." },                
           { "updatingobject",            " Updating object " },
           { "excelformat",            " File format of file {0} not supported, please use  [Excel OOXML]." },
           { "xmlformat", 		"No valid XML document. Datastream could not be saved." },
           { "selectformat", 		"Cirilo supports import of FEDORA object files in the following formats:" },
           { "ingfail", 		"ingest failed" },
           { "alrexist", 		"already exists" },
           { "attingest",        "Importing file {0} with format {1} and PID {2}" },         
           { "novalidobj",       "File {0} does not have a valid FEDORA object schema."},
           { "ingestof",       "Ingesting file "},
           { "tei2mets",       "Create METS datastream"},
           { "nologdir",       "Log directory {0} couldn't found."},

           { "novalidkey", "File {0} contains no valid handle key."},			  
           { "keyok" , "Key from file {0} is permanently available for authentication on handle server."},			  
		   { "selectkey", "Select key file"},
           
		   { "oaiok"         ,  "For {0} of total {1} entries from OAI-PMH source {2} harvesting objects could be created."},
           { "exportcontext",   "Export context"},


           
           /* cirilo */
           { "loadprop"          , "Load application properties"          },
           { "sam"          , "Setup access manager"          },
           { "dam"          , "Setup dialogue manager"          },
           { "notitle"          , "Untitled"          },
           
           /* menu */
           { "file.edit"          , "Edit objects"          },
           { "file.ingest"          , "Ingest objects"          },
           { "file.import"          , "Import objects"          },
                      { "Extras"          , "Extras"          },
           { "file.transform"          , "Transform files"          },
           { "resetdeskt"          , "Reset desktop"          },
           { "exit"          , "Exit"          },
           { "changeuser"          , "Change repository"          },
           { "Infos"          , "Infos"          },
           { "about"          , "About Cirilo"          },
           { "File"          , "File"          },          
        
           /* setup */
           { "createobj"          , "Create object"          },
           { "editdc"          , "Edit Dublin Core"          },
           { "editobjsing"          , "Edit object"          },
           { "choosestyle"          , "Choose XSL-stylesheet"          },
           { "extras.harvest"          , "Harvest metadata"          },
           { "extras.templater"          , "RDF template tester"          },
           { "extras.preferences"          , "Preferences ..."          },
           { "ingestexcel"          , "From Excel spreadsheet"          },
           { "existlogin"          , "eXist-database login"          },
           
           /* table header */
           { "pid"          , "PID"          },
           { "title"          , "Title"          },
           { "contentmodel"          , "Content Model"          },
           { "lastupdate"          , "Last update"          },
           { "owner"          , "Owner"          },

           { "id"          , "Datastream ID"          },
           { "label"          , "Label"          },
           { "versionable"          , "Versionable"          },
           { "mimetype"          , "Mime Type"          },
           { "replaceversion"          , "Update will replace most recent version"          },
           { "createversion"          , "Update will create new version"          },
           { "createstream"          , "Create Datastream"          },
           { "nonvalidid",            "Error while creating datastream {0}. This is a reserved identifier." },
           { "nonvaliddel",            "Error while deleting datastream {0}. Default datastreams can not be deleted." },
           { "delstream",            "This process deletes {0} datastream(s) . Are you sure you want to continue?" },
           { "userdef"          , "User-defined stylesheet"          },
           { "nonsysadm"          , "Deleting system objects is permitted for administrators only. Object {0} could not be deleted."          },           
           { "parsererror"          , "Processing of file {0} resulted in XML parsing error."          },

           { "datalocations"          , "Datastream locations"          },
           
           { "hdlprefix"          , "Handle prefix:"          },
           { "proprefix"          , "Project prefix:"          },
           { "numcons"          , "Number consecutively:"          },
           { "startw"          , "Beginning with:"          },
           { "getkey"          , "Get key"          },
	       { "hdlmanage"          , "Manage handles"          },
	       	{ "hdlcreate"          , "Creating handles ..."          },
           { "hdldel"          , "Deleting handles ..."          },
           { "hdlauthfailed"          , "Authentification on Handle service failed! Couldn't found a valid public key file."          },
           { "hdlsel"          , "Select key file"          },
           { "hdlvalid"        ,  "Successful authentification on Handle service." },
           { "hdlcreated"  , "Handle(s) for {0} object(s) created."          },
           { "hdldeleted"  , "Handle(s) of  {0} deleted."          },
           { "shownat"  , "Shown At"          },
           { "cmodel"  , "Content Model"          },
           
           { "nonvalidrdf"  , "\n{0}. RDF stream from source {1} could not be validated correctly."          },
           { "triplestoreerror"  , "Error while sending data to {0} triplestore."          },
           { "sysprops"  , "System properties"          },
         

        };    


}
