package org.jasig.portal;

import java.util.Hashtable;
import java.util.Vector;
import java.util.Enumeration;

import java.sql.*;

import org.w3c.dom.*;
import org.apache.xerces.parsers.*;

import org.apache.xml.serialize.*;

/**
 * Reference implementation of ICoreStylesheetDescriptionDB
 * @author Peter Kharchenko
 * @version $Revision$
 */

public class CoreStylesheetDescriptionDBImpl implements ICoreStylesheetDescriptionDB {
    RdbmServices rdbmService;
    Connection con;
    public CoreStylesheetDescriptionDBImpl() {
	rdbmService = new RdbmServices();
	con=null;
    };


    // functions that allow one to browse available core stylesheets in various ways
    public Hashtable getStructureStylesheetList(String media) {
	Hashtable list=new Hashtable();

	try {
	    con=rdbmService.getConnection();
	    Statement stmt=con.createStatement();
	   
	    String sQuery = "SELECT A.STYLESHEET_NAME, A.STYLESHEET_DESCRIPTION_TEXT FROM STRUCTURE_STYLESHEET_DESCRIPTION_TABLE A, STRUCTURE_TRANSFORMATION_STYLESHEET_MEDIA_MAPPING B WHERE B.MEDIA='"+media+"' AND B.STRUCTURE_STYLESHEET_NAME=A.STYLESHEET_NAME";

	    Logger.log(Logger.DEBUG,"CoreStylesheetDescriptionDBImpl::getStructureStylesheetList() : "+sQuery);
	    ResultSet rs=stmt.executeQuery(sQuery);
	    while(rs.next()) {
		list.put(rs.getString("STYLESHEET_NAME"),rs.getString("STYLESHEET_DESCRIPTION_TEXT"));
	    }
	} catch (Exception e) { 
	    Logger.log(Logger.ERROR,e); 
	} finally {
	    rdbmService.releaseConnection (con);
	}
	return list;
    }
    public Hashtable getThemeStylesheetList(String structureStylesheetName) {
	Hashtable list=new Hashtable();

	try {
	    con=rdbmService.getConnection();
	    Statement stmt=con.createStatement();

	    String sQuery = "select a.stylesheet_name, a.stylesheet_description_text from theme_stylesheet_description_table a, theme_transformation_stylesheet_mapping b where b.structure_stylesheet_name='"+structureStylesheetName+"' and a.stylesheet_name=b.theme_stylesheet_name;";
	    Logger.log(Logger.DEBUG,"CoreStylesheetDescriptionDBImpl::getThemeStylesheetList() : "+sQuery);
	    ResultSet rs=stmt.executeQuery(sQuery);
	    while(rs.next()) {
		list.put(rs.getString("STYLESHEET_NAME"),rs.getString("STYLESHEET_DESCRIPTION_TEXT"));
	    }
	} catch (Exception e) { 
	    Logger.log(Logger.ERROR,e); 
	} finally {
	    rdbmService.releaseConnection (con);
	}
	return list;
    }
    public Hashtable getCSSStylesheetList(String themeStylesheetName) {
	Hashtable list=new Hashtable();
	try {
	    con=rdbmService.getConnection();
	    Statement stmt=con.createStatement();

	    String sQuery = "select a.stylesheet_name, a.stylesheet_description_text from css_stylesheet_description_table a, css_stylesheet_mapping b where b.theme_stylesheet_name='"+themeStylesheetName+"' and b.css_stylesheet_name=a.stylesheet_name";
	    Logger.log(Logger.DEBUG,"CoreStylesheetDescriptionDBImpl::getCSSStylesheetList() : "+sQuery);
	    ResultSet rs=stmt.executeQuery(sQuery);
	    while(rs.next()) {
		list.put(rs.getString("STYLESHEET_NAME"),rs.getString("STYLESHEET_DESCRIPTION_TEXT"));
	    }
	} catch (Exception e) { 
	    Logger.log(Logger.ERROR,e); 
	} finally {
	    rdbmService.releaseConnection (con);
	}
	return list;
    }

    // functions that allow access to the entire CoreStylesheetDescription object.
    // These functions are used when working with the stylesheet, and not for browsing purposes.
    public StructureStylesheetDescription getStructureStylesheetDescription(String stylesheetName) {
	StructureStylesheetDescription fssd=null;
	try {
	    con=rdbmService.getConnection();
	    Statement stmt=con.createStatement();

	    String sQuery = "select * from structure_stylesheet_description_table where stylesheet_name='"+stylesheetName+"'";
	    Logger.log(Logger.DEBUG,"CoreStylesheetDescriptionDBImpl::getStructureStylesheetDescription() : "+sQuery);
	    ResultSet rs=stmt.executeQuery(sQuery);
	    if(rs.next()) {
		fssd=new StructureStylesheetDescription();
		// retreive database values
		String dbStylesheetName=rs.getString("STYLESHEET_NAME");
		String dbStylesheetDescriptionText=rs.getString("STYLESHEET_DESCRIPTION_TEXT");
		String dbURI=rs.getString("STYLESHEET_URI");
		String dbDescriptionURI=rs.getString("STYLESHEET_DESCRIPTION_URI");

		// obtain DOM of the description file
		DOMParser parser=new DOMParser();
		parser.parse(UtilitiesBean.fixURI(dbDescriptionURI));		
		Document stylesheetDescriptionXML=parser.getDocument();

		Logger.log(Logger.DEBUG,"CoreStylesheetDescriptionDBImpl::getStructureStylesheetDescription() : stylesheet name = "+this.getName(stylesheetDescriptionXML));
		Logger.log(Logger.DEBUG,"CoreStylesheetDescriptionDBImpl::getStructureStylesheetDescription() : stylesheet description = "+this.getDescription(stylesheetDescriptionXML));


		String xmlStylesheetName=this.getName(stylesheetDescriptionXML);
		String xmlStylesheetDescriptionText=this.getDescription(stylesheetDescriptionXML);

		if(!xmlStylesheetName.equals(dbStylesheetName)) 
		    Logger.log(Logger.ERROR,"CoreStylesheetDescriptionDBImpl::getStructureStylesheetDescription() : Structure stage stylesheet name from database (\""+dbStylesheetName+"\") differs from the name in the SDF XML (\""+xmlStylesheetName+"\")!!! Please fix.");

		if(!xmlStylesheetDescriptionText.equals(dbStylesheetDescriptionText)) 
		    Logger.log(Logger.ERROR,"CoreStylesheetDescriptionDBImpl::getStructureStylesheetDescription() : Structure stage stylesheet word description (stylesheet name=\""+dbStylesheetName+"\") from database (\""+dbStylesheetDescriptionText+"\") differs from the word description in the SDF XML (\""+xmlStylesheetDescriptionText+"\")!!! Please fix.");

		fssd.setStylesheetName(xmlStylesheetName);
		fssd.setStylesheetWordDescription(xmlStylesheetDescriptionText);
		
		// populate parameter and attriute tables
		this.populateParameterTable(stylesheetDescriptionXML,fssd);
		this.populateFolderAttributeTable(stylesheetDescriptionXML,fssd);
		this.populateChannelAttributeTable(stylesheetDescriptionXML,fssd);
		fssd.setStylesheetMediaList(this.getVectorOfSimpleTextElementValues(stylesheetDescriptionXML,"media"));

	    } else {
		Logger.log(Logger.ERROR,"CoreStylesheetDescriptionDBImpl::getStructureStylesheetDescription() : Could not find a structure stage stylesheet with a name \""+stylesheetName+"\"");
	    }
	} catch (Exception e) { 
	    Logger.log(Logger.ERROR,e); 
	} finally {
	    rdbmService.releaseConnection (con);
	}
	return fssd;
    }

    public ThemeStylesheetDescription getThemeStylesheetDescription(String stylesheetName) {
	ThemeStylesheetDescription sssd=null;
	try {
	    con=rdbmService.getConnection();
	    Statement stmt=con.createStatement();

	    String sQuery = "select * from theme_stylesheet_description_table where stylesheet_name='"+stylesheetName+"'";
	    Logger.log(Logger.DEBUG,"CoreStylesheetDescriptionDBImpl::getThemeStylesheetDescription() : "+sQuery);
	    ResultSet rs=stmt.executeQuery(sQuery);
	    if(rs.next()) {
		sssd=new ThemeStylesheetDescription();
		// retreive database values
		String dbStylesheetName=rs.getString("STYLESHEET_NAME");
		String dbStylesheetDescriptionText=rs.getString("STYLESHEET_DESCRIPTION_TEXT");
		String dbURI=rs.getString("STYLESHEET_URI");
		String dbDescriptionURI=rs.getString("STYLESHEET_DESCRIPTION_URI");

		// obtain DOM of the description file
		DOMParser parser=new DOMParser();
		parser.parse(UtilitiesBean.fixURI(dbDescriptionURI));		
		Document stylesheetDescriptionXML=parser.getDocument();

		Logger.log(Logger.DEBUG,"CoreStylesheetDescriptionDBImpl::getThemeStylesheetDescription() : stylesheet name = "+this.getName(stylesheetDescriptionXML));
		Logger.log(Logger.DEBUG,"CoreStylesheetDescriptionDBImpl::getThemeStylesheetDescription() : stylesheet description = "+this.getDescription(stylesheetDescriptionXML));


		String xmlStylesheetName=this.getName(stylesheetDescriptionXML);
		String xmlStylesheetDescriptionText=this.getDescription(stylesheetDescriptionXML);

		if(!xmlStylesheetName.equals(dbStylesheetName)) 
		    Logger.log(Logger.ERROR,"CoreStylesheetDescriptionDBImpl::getThemeStylesheetDescription() : Theme stage stylesheet name from database (\""+dbStylesheetName+"\") differs from the name in the SDF XML (\""+xmlStylesheetName+"\")!!! Please fix.");

		if(!xmlStylesheetDescriptionText.equals(dbStylesheetDescriptionText)) 
		    Logger.log(Logger.ERROR,"CoreStylesheetDescriptionDBImpl::getThemeStylesheetDescription() : Theme stage stylesheet word description (stylesheet name=\""+dbStylesheetName+"\") from database (\""+dbStylesheetDescriptionText+"\") differs from the word description in the SDF XML (\""+xmlStylesheetDescriptionText+"\")!!! Please fix.");

		sssd.setStylesheetName(xmlStylesheetName);
		sssd.setStylesheetWordDescription(xmlStylesheetDescriptionText);
		
		// populate parameter and attriute tables
		this.populateParameterTable(stylesheetDescriptionXML,sssd);
		this.populateChannelAttributeTable(stylesheetDescriptionXML,sssd);
		sssd.setStructureStylesheetList(this.getVectorOfSimpleTextElementValues(stylesheetDescriptionXML,"parentstylesheet"));

	    } else {
		Logger.log(Logger.ERROR,"CoreStylesheetDescriptionDBImpl::getThemeStylesheetDescription() : Could not find a theme stage stylesheet with a name \""+stylesheetName+"\"");
	    }
	} catch (Exception e) { 
	    Logger.log(Logger.ERROR,e); 
	} finally {
	    rdbmService.releaseConnection (con);
	}
	return sssd;
    }

    public CoreCSSStylesheetDescription getCSSStylesheetDescription(String stylesheetName){
	CoreCSSStylesheetDescription ccd=null;
	try {
	    con=rdbmService.getConnection();
	    Statement stmt=con.createStatement();

	    String sQuery = "select * from css_stylesheet_description_table where stylesheet_name='"+stylesheetName+"'";
	    Logger.log(Logger.DEBUG,"CoreStylesheetDescriptionDBImpl::getCSSStylesheetDescription() : "+sQuery);
	    ResultSet rs=stmt.executeQuery(sQuery);
	    if(rs.next()) {
		ccd=new CoreCSSStylesheetDescription();
		// retreive database values
		String dbStylesheetName=rs.getString("STYLESHEET_NAME");
		String dbStylesheetDescriptionText=rs.getString("STYLESHEET_DESCRIPTION_TEXT");
		String dbURI=rs.getString("STYLESHEET_URI");
		String dbDescriptionURI=rs.getString("STYLESHEET_DESCRIPTION_URI");

		// obtain DOM of the description file
		DOMParser parser=new DOMParser();
		parser.parse(UtilitiesBean.fixURI(dbDescriptionURI));		
		Document stylesheetDescriptionXML=parser.getDocument();

		Logger.log(Logger.DEBUG,"CoreStylesheetDescriptionDBImpl::getCSSStylesheetDescription() : stylesheet name = "+this.getName(stylesheetDescriptionXML));
		Logger.log(Logger.DEBUG,"CoreStylesheetDescriptionDBImpl::getCSSStylesheetDescription() : stylesheet description = "+this.getDescription(stylesheetDescriptionXML));


		String xmlStylesheetName=this.getName(stylesheetDescriptionXML);
		String xmlStylesheetDescriptionText=this.getDescription(stylesheetDescriptionXML);

		if(!xmlStylesheetName.equals(dbStylesheetName)) 
		    Logger.log(Logger.ERROR,"CoreStylesheetDescriptionDBImpl::getCSSStylesheetDescription() : CSS stylesheet name from database (\""+dbStylesheetName+"\") differs from the name in the SDF XML (\""+xmlStylesheetName+"\")!!! Please fix.");

		if(!xmlStylesheetDescriptionText.equals(dbStylesheetDescriptionText)) 
		    Logger.log(Logger.ERROR,"CoreStylesheetDescriptionDBImpl::getCSSStylesheetDescription() : CSS stylesheet word description (stylesheet name=\""+dbStylesheetName+"\") from database (\""+dbStylesheetDescriptionText+"\") differs from the word description in the SDF XML (\""+xmlStylesheetDescriptionText+"\")!!! Please fix.");

		ccd.setStylesheetName(xmlStylesheetName);
		ccd.setStylesheetWordDescription(xmlStylesheetDescriptionText);
		
		// populate parameter and attriute tables
		this.populateParameterTable(stylesheetDescriptionXML,ccd);
		ccd.setThemeStylesheetList(this.getVectorOfSimpleTextElementValues(stylesheetDescriptionXML,"parentstylesheet"));

	    } else {
		Logger.log(Logger.ERROR,"CoreStylesheetDescriptionDBImpl::getCSSStylesheetDescription() : Could not find a CSS stylesheet with a name \""+stylesheetName+"\"");
	    }
	} catch (Exception e) { 
	    Logger.log(Logger.ERROR,e); 
	} finally {
	    rdbmService.releaseConnection (con);
	}
	return ccd;
    }



    public void removeStructureStylesheetDescription(String stylesheetName){
	try {
	    con=rdbmService.getConnection();
	    Statement stmt=con.createStatement();
	    
	    // note that we don't delete from THEME_TRANSFORMATION_STYLESHEET_MAPPING table. 
	    // Information contained in that table belongs to theme-stage stylesheet. Let them fix it.
	    String sQuery = "delete from structure_stylesheet_description_table where stylesheet_name='"+stylesheetName+"'; delete from STRUCTURE_TRANSFORMATION_STYLESHEET_MEDIA_MAPPING where structure_stylesheet_name='"+stylesheetName+"';";

	    Logger.log(Logger.DEBUG,"CoreStylesheetDescriptionDBImpl::removeStructureStylesheetDescription() : "+sQuery);
	    ResultSet rs=stmt.executeQuery(sQuery);
	    
	} catch (Exception e) { 
	    Logger.log(Logger.ERROR,e); 
	} finally {
	    rdbmService.releaseConnection (con);
	}

    }
    public void removeThemeStylesheetDescription(String stylesheetName){
	try {
	    con=rdbmService.getConnection();
	    Statement stmt=con.createStatement();

	    // note that we don't delete from CSS_STYLESHEET_MAPPING table
	    String sQuery = "delete from theme_stylesheet_description_table where stylesheet_name='"+stylesheetName+"'; delete from THEME_TRANSFORMATION_STYLESHEET_MAPPING where theme_stylesheet_name='"+stylesheetName+"';";

	    Logger.log(Logger.DEBUG,"CoreStylesheetDescriptionDBImpl::removeThemeStylesheetDescription() : "+sQuery);
	    ResultSet rs=stmt.executeQuery(sQuery);
	    
	} catch (Exception e) { 
	    Logger.log(Logger.ERROR,e); 
	} finally {
	    rdbmService.releaseConnection (con);
	}
    }
    public void removeCSSStylesheetDescription(String stylesheetName){
	try {
	    con=rdbmService.getConnection();
	    Statement stmt=con.createStatement();

	    String sQuery = "delete from css_stylesheet_description_table where stylesheet_name='"+stylesheetName+"'; delete from CSS_STYLESHEET_MAPPING where css_stylesheet_name='"+stylesheetName+"'";

	    Logger.log(Logger.DEBUG,"CoreStylesheetDescriptionDBImpl::removeCSSStylesheetDescription() : "+sQuery);
	    ResultSet rs=stmt.executeQuery(sQuery);
	    
	} catch (Exception e) { 
	    Logger.log(Logger.ERROR,e); 
	} finally {
	    rdbmService.releaseConnection (con);
	}
    }

    public void addStructureStylesheetDescription(String stylesheetDescriptionURI,String stylesheetURI){
	// need to read in the description file to obtain information such as name, word description and media list
	try {
	    DOMParser parser=new DOMParser();
	    parser.parse(UtilitiesBean.fixURI(stylesheetDescriptionURI));		
	    Document stylesheetDescriptionXML=parser.getDocument();
	    
	    Logger.log(Logger.DEBUG,"CoreStylesheetDescriptionDBImpl::addStructureStylesheetDescription() : stylesheet name = "+this.getName(stylesheetDescriptionXML));
	    Logger.log(Logger.DEBUG,"CoreStylesheetDescriptionDBImpl::addStructureStylesheetDescription() : stylesheet description = "+this.getDescription(stylesheetDescriptionXML));
	    
	    StructureStylesheetDescription fssd=new StructureStylesheetDescription();	    
	    String xmlStylesheetName=this.getName(stylesheetDescriptionXML);
	    String xmlStylesheetDescriptionText=this.getDescription(stylesheetDescriptionXML);
	    
	    fssd.setStylesheetName(xmlStylesheetName);
	    fssd.setStylesheetWordDescription(xmlStylesheetDescriptionText);
	    
	    // populate parameter and attriute tables
	    this.populateParameterTable(stylesheetDescriptionXML,fssd);
	    this.populateFolderAttributeTable(stylesheetDescriptionXML,fssd);
	    this.populateChannelAttributeTable(stylesheetDescriptionXML,fssd);
	    fssd.setStylesheetMediaList(this.getVectorOfSimpleTextElementValues(stylesheetDescriptionXML,"media"));


	    // now write out the database record
	    
	    // first the basic record
	    con=rdbmService.getConnection();
	    Statement stmt=con.createStatement();

	    String sQuery = "INSERT into STRUCTURE_STYLESHEET_DESCRIPTION_TABLE (STYLESHEET_NAME,STYLESHEET_URI,STYLESHEET_DESCRIPTION_URI,STYLESHEET_DESCRIPTION_TEXT) values ('"+xmlStylesheetName+"','"+stylesheetURI+"','"+stylesheetDescriptionURI+"','"+xmlStylesheetDescriptionText+"')";

	    Logger.log(Logger.DEBUG,"CoreStylesheetDescriptionDBImpl::addStructureStylesheetDescription() : "+sQuery);
	    ResultSet rs=stmt.executeQuery(sQuery);
	    
	    for (Enumeration e = fssd.getStylesheetMediaList().elements() ; e.hasMoreElements() ;) {
		sQuery = "INSERT into STRUCTURE_TRANSFORMATION_STYLESHEET_MEDIA_MAPPING (MEDIA,STRUCTURE_STYLESHEET_NAME) values ('"+((String) e.nextElement())+"','"+xmlStylesheetName+"');";
		Logger.log(Logger.DEBUG,"CoreStylesheetDescriptionDBImpl::addStructureStylesheetDescription() : "+sQuery);
		rs=stmt.executeQuery(sQuery);
	    }

	} catch (Exception e) {
	    Logger.log(Logger.DEBUG,e);
	}
    }
    public void addThemeStylesheetDescription(String stylesheetDescriptionURI,String stylesheetURI){
	// need to read in the description file to obtain information such as name, word description and media list
	try {
	    DOMParser parser=new DOMParser();
	    parser.parse(UtilitiesBean.fixURI(stylesheetDescriptionURI));		
	    Document stylesheetDescriptionXML=parser.getDocument();
	    
	    Logger.log(Logger.DEBUG,"CoreStylesheetDescriptionDBImpl::addThemeStylesheetDescription() : stylesheet name = "+this.getName(stylesheetDescriptionXML));
	    Logger.log(Logger.DEBUG,"CoreStylesheetDescriptionDBImpl::addThemeStylesheetDescription() : stylesheet description = "+this.getDescription(stylesheetDescriptionXML));
	    
	    ThemeStylesheetDescription sssd=new ThemeStylesheetDescription();	    
	    String xmlStylesheetName=this.getName(stylesheetDescriptionXML);
	    String xmlStylesheetDescriptionText=this.getDescription(stylesheetDescriptionXML);
	    
	    sssd.setStylesheetName(xmlStylesheetName);
	    sssd.setStylesheetWordDescription(xmlStylesheetDescriptionText);
	    
	    // populate parameter and attriute tables
	    this.populateParameterTable(stylesheetDescriptionXML,sssd);
	    this.populateChannelAttributeTable(stylesheetDescriptionXML,sssd);
	    sssd.setStructureStylesheetList(this.getVectorOfSimpleTextElementValues(stylesheetDescriptionXML,"parentstylesheet"));

	    con=rdbmService.getConnection();
	    Statement stmt=con.createStatement();

	    String sQuery = "INSERT into THEME_STYLESHEET_DESCRIPTION_TABLE (STYLESHEET_NAME,STYLESHEET_URI,STYLESHEET_DESCRIPTION_URI,STYLESHEET_DESCRIPTION_TEXT) values ('"+xmlStylesheetName+"','"+stylesheetURI+"','"+stylesheetDescriptionURI+"','"+xmlStylesheetDescriptionText+"')";

	    Logger.log(Logger.DEBUG,"CoreStylesheetDescriptionDBImpl::addThemeStylesheetDescription() : "+sQuery);
	    ResultSet rs=stmt.executeQuery(sQuery);

	    
	    for (Enumeration e = sssd.getStructureStylesheetList().elements() ; e.hasMoreElements() ;) {
		sQuery = "INSERT into THEME_TRANSFORMATION_STYLESHEET_MAPPING (THEME_STYLESHEET_NAME,STRUCTURE_STYLESHEET_NAME) values ('"+xmlStylesheetName+"','"+((String) e.nextElement())+"');";
		Logger.log(Logger.DEBUG,"CoreStylesheetDescriptionDBImpl::addThemeStylesheetDescription() : "+sQuery);
		rs=stmt.executeQuery(sQuery);
	    }

	} catch (Exception e) {
	    Logger.log(Logger.DEBUG,e);
	}
    }
    public void addCSSStylesheetDescription(String stylesheetDescriptionURI,String stylesheetURI){

	try {
	    DOMParser parser=new DOMParser();
	    parser.parse(UtilitiesBean.fixURI(stylesheetDescriptionURI));		
	    Document stylesheetDescriptionXML=parser.getDocument();
	    
	    Logger.log(Logger.DEBUG,"CoreStylesheetDescriptionDBImpl::addCSSStylesheetDescription() : stylesheet name = "+this.getName(stylesheetDescriptionXML));
	    Logger.log(Logger.DEBUG,"CoreStylesheetDescriptionDBImpl::addCSSStylesheetDescription() : stylesheet description = "+this.getDescription(stylesheetDescriptionXML));
	    

	    CoreCSSStylesheetDescription ccd=new CoreCSSStylesheetDescription();
	    String xmlStylesheetName=this.getName(stylesheetDescriptionXML);
	    String xmlStylesheetDescriptionText=this.getDescription(stylesheetDescriptionXML);
	    
	    ccd.setStylesheetName(xmlStylesheetName);
	    ccd.setStylesheetWordDescription(xmlStylesheetDescriptionText);
	    
	    // populate parameter and attriute tables
	    this.populateParameterTable(stylesheetDescriptionXML,ccd);
	    ccd.setThemeStylesheetList(this.getVectorOfSimpleTextElementValues(stylesheetDescriptionXML,"parentstylesheet"));


	    con=rdbmService.getConnection();
	    Statement stmt=con.createStatement();

	    String sQuery = "INSERT into CSS_STYLESHEET_DESCRIPTION_TABLE (STYLESHEET_NAME,STYLESHEET_URI,STYLESHEET_DESCRIPTION_URI,STYLESHEET_DESCRIPTION_TEXT) values ('"+xmlStylesheetName+"','"+stylesheetURI+"','"+stylesheetDescriptionURI+"','"+xmlStylesheetDescriptionText+"')";

	    Logger.log(Logger.DEBUG,"CoreStylesheetDescriptionDBImpl::addCSSStylesheetDescription() : "+sQuery);
	    ResultSet rs=stmt.executeQuery(sQuery);

	    
	    for (Enumeration e = ccd.getThemeStylesheetList().elements() ; e.hasMoreElements() ;) {
		sQuery = "INSERT into CSS_STYLESHEET_MAPPING (CSS_STYLESHEET_NAME,THEME_STYLESHEET_NAME) values ('"+xmlStylesheetName+"','"+((String) e.nextElement())+"');";
		Logger.log(Logger.DEBUG,"CoreStylesheetDescriptionDBImpl::addCSSStylesheetDescription() : "+sQuery);
		rs=stmt.executeQuery(sQuery);
	    }

	} catch (Exception e) {
	    Logger.log(Logger.DEBUG,e);
	}

    }





    // private helper modules that retreive information from the DOM structure of the description files
    private String getName(Document descr) {
	NodeList names=descr.getElementsByTagName("name");
	Node name=null;
	for(int i=names.getLength()-1;i>=0;i--) {
	    name=names.item(i);
	    if(name.getParentNode().getLocalName().equals("stylesheetdescription")) break;
	    else name=null;
	}
	if(name!=null) {
	    return this.getTextChildNodeValue(name);;
	} else { Logger.log(Logger.DEBUG,"CoreStylesheetDescriptionDBImpl::getName() : no \"name\" element was found udner the \"stylesheetdescription\" node!"); return null; }
    }

    private String getDescription(Document descr) {
	NodeList descriptions=descr.getElementsByTagName("description");
	Node description=null;
	for(int i=descriptions.getLength()-1;i>=0;i--) {
	    description=descriptions.item(i);
	    if(description.getParentNode().getLocalName().equals("stylesheetdescription")) break;
	    else description=null;
	}
	if(description!=null) {
	    return this.getTextChildNodeValue(description);
	} else { Logger.log(Logger.DEBUG,"CoreStylesheetDescriptionDBImpl::getName() : no \"description\" element was found udner the \"stylesheetdescription\" node!"); return null; }
    }

    private void populateParameterTable(Document descr,CoreStylesheetDescription csd) {
	NodeList parametersNodes=descr.getElementsByTagName("parameters");
	Node parametersNode=null;
	for(int i=parametersNodes.getLength()-1;i>=0;i--) {
	    parametersNode=parametersNodes.item(i);
	    if(parametersNode.getParentNode().getLocalName().equals("stylesheetdescription")) break;
	    else parametersNode=null;
	}
	if(parametersNode!=null) {
	    NodeList children=parametersNode.getChildNodes();
	    for(int i=children.getLength()-1;i>=0;i--) {
		Node child=children.item(i);
		if(child.getNodeType()==Node.ELEMENT_NODE && child.getLocalName().equals("parameter")) {
		    Element parameter=(Element) children.item(i);
		    // process a <parameter> node
		    String name=parameter.getAttribute("name");
		    String description=null; String defaultvalue=null;
		    NodeList pchildren=parameter.getChildNodes();
		    for(int j=pchildren.getLength()-1;j>=0;j--) {
			Node pchild=pchildren.item(j);
			if(pchild.getNodeType()==Node.ELEMENT_NODE) {
			    if(pchild.getLocalName().equals("defaultvalue")) {
				defaultvalue=this.getTextChildNodeValue(pchild);
			    } else if(pchild.getLocalName().equals("description")) {
				description=this.getTextChildNodeValue(pchild);
			    }
			}
		    }
		    Logger.log(Logger.DEBUG,"CoreStylesheetDescriptionDBImpl::populateParameterTable() : adding a stylesheet parameter : (\""+name+"\",\""+defaultvalue+"\",\""+description+"\")");
		    csd.addStylesheetParameter(name,defaultvalue,description);
		} 
	    }
	}
    }

    private void populateFolderAttributeTable(Document descr,StructureStylesheetDescription cxsd) {NodeList parametersNodes=descr.getElementsByTagName("parameters");
	NodeList folderattributesNodes=descr.getElementsByTagName("folderattributes");
	Node folderattributesNode=null;
	for(int i=folderattributesNodes.getLength()-1;i>=0;i--) {
	    folderattributesNode=folderattributesNodes.item(i);
	    if(folderattributesNode.getParentNode().getLocalName().equals("stylesheetdescription")) break;
	    else folderattributesNode=null;
	}
	if(folderattributesNode!=null) {
	    NodeList children=folderattributesNode.getChildNodes();
	    for(int i=children.getLength()-1;i>=0;i--) {
		Node child=children.item(i);
		if(child.getNodeType()==Node.ELEMENT_NODE && child.getLocalName().equals("attribute")) {
		    Element attribute=(Element) children.item(i);
		    // process a <attribute> node
		    String name=attribute.getAttribute("name");
		    String description=null; String defaultvalue=null;
		    NodeList pchildren=attribute.getChildNodes();
		    for(int j=pchildren.getLength()-1;j>=0;j--) {
			Node pchild=pchildren.item(j);
			if(pchild.getNodeType()==Node.ELEMENT_NODE) {
			    if(pchild.getLocalName().equals("defaultvalue")) {
				defaultvalue=this.getTextChildNodeValue(pchild);
			    } else if(pchild.getLocalName().equals("description")) {
				description=this.getTextChildNodeValue(pchild);
			    }
			}
		    }
		    Logger.log(Logger.DEBUG,"CoreStylesheetDescriptionDBImpl::populateFolderAttributeTable() : adding a stylesheet folder attribute : (\""+name+"\",\""+defaultvalue+"\",\""+description+"\")");
		    cxsd.addFolderAttribute(name,defaultvalue,description);
		} 
	    }
	}
    }

    private void populateChannelAttributeTable(Document descr,CoreXSLTStylesheetDescription cxsd) {
	NodeList channelattributesNodes=descr.getElementsByTagName("channelattributes");
	Node channelattributesNode=null;
	for(int i=channelattributesNodes.getLength()-1;i>=0;i--) {
	    channelattributesNode=channelattributesNodes.item(i);
	    if(channelattributesNode.getParentNode().getLocalName().equals("stylesheetdescription")) break;
	    else channelattributesNode=null;
	}
	if(channelattributesNode!=null) {
	    NodeList children=channelattributesNode.getChildNodes();
	    for(int i=children.getLength()-1;i>=0;i--) {
		Node child=children.item(i);
		if(child.getNodeType()==Node.ELEMENT_NODE && child.getLocalName().equals("attribute")) {
		    Element attribute=(Element) children.item(i);
		    // process a <attribute> node
		    String name=attribute.getAttribute("name");
		    String description=null; String defaultvalue=null;
		    NodeList pchildren=attribute.getChildNodes();
		    for(int j=pchildren.getLength()-1;j>=0;j--) {
			Node pchild=pchildren.item(j);
			if(pchild.getNodeType()==Node.ELEMENT_NODE) {
			    if(pchild.getLocalName().equals("defaultvalue")) {
				defaultvalue=this.getTextChildNodeValue(pchild);
			    } else if(pchild.getLocalName().equals("description")) {
				description=this.getTextChildNodeValue(pchild);
			    }
			}
		    }
		    Logger.log(Logger.DEBUG,"CoreStylesheetDescriptionDBImpl::populateChannelAttributeTable() : adding a stylesheet channel attribute : (\""+name+"\",\""+defaultvalue+"\",\""+description+"\")");
		    cxsd.addChannelAttribute(name,defaultvalue,description);
		} 
	    }
	}
    }

    private Vector getVectorOfSimpleTextElementValues(Document descr,String elementName) {
	Vector v=new Vector();
	
	// find "stylesheetdescription" node, take the first one
	Element stylesheetdescriptionElement=(Element) (descr.getElementsByTagName("stylesheetdescription")).item(0);
	if(stylesheetdescriptionElement==null) { Logger.log(Logger.ERROR,"Could not obtain <stylesheetdescription> element"); return null; }
	NodeList elements=stylesheetdescriptionElement.getElementsByTagName(elementName);
	for(int i=elements.getLength()-1;i>=0;i--) {
	    v.add(this.getTextChildNodeValue(elements.item(i)));
	    //	    Logger.log(Logger.DEBUG,"adding "+this.getTextChildNodeValue(elements.item(i))+" to the \""+elementName+"\" vector.");
	}
	return v;
    }

    private String getTextChildNodeValue(Node node) {
	if(node==null) return null;
	NodeList children=node.getChildNodes();
	for(int i=children.getLength()-1;i>=0;i--) {
	    Node child=children.item(i);
	    if(child.getNodeType()==Node.TEXT_NODE) return child.getNodeValue();
	}
	return null;
    }

};
