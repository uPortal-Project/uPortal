/**
 * Copyright © 2001 The JA-SIG Collaborative.  All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 *
 * 1. Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 *
 * 2. Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in
 *    the documentation and/or other materials provided with the
 *    distribution.
 *
 * 3. Redistributions of any form whatsoever must retain the following
 *    acknowledgment:
 *    "This product includes software developed by the JA-SIG Collaborative
 *    (http://www.jasig.org/)."
 *
 * THIS SOFTWARE IS PROVIDED BY THE JA-SIG COLLABORATIVE "AS IS" AND ANY
 * EXPRESSED OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR
 * PURPOSE ARE DISCLAIMED.  IN NO EVENT SHALL THE JA-SIG COLLABORATIVE OR
 * ITS CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT
 * NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION)
 * HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT,
 * STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED
 * OF THE POSSIBILITY OF SUCH DAMAGE.
 *
 */

package org.jasig.portal;

import java.util.Hashtable;
import java.util.Vector;
import java.util.Enumeration;

import org.w3c.dom.*;
import org.apache.xerces.parsers.*;

import org.apache.xml.serialize.*;

/**
 * Reference implementation of ICoreStylesheetDescriptionDB
 * @author Peter Kharchenko, peterk@interactivebusiness.com
 * @version $Revision$
 */

public class CoreStylesheetDescriptionDBImpl implements ICoreStylesheetDescriptionDB {
    public CoreStylesheetDescriptionDBImpl() {
    };


    public Hashtable getMimeTypeList() {
        Hashtable list=new Hashtable();
        try {
          GenericPortalBean.getDbImplObject().getMimeTypeList(list);
        } catch (Exception e) {
            Logger.log(Logger.ERROR,e);
        }
        return list;
    }

    // functions that allow one to browse available core stylesheets in various ways
    public Hashtable getStructureStylesheetList(String mimeType) {
        Hashtable list=new Hashtable();

        try {
          GenericPortalBean.getDbImplObject().getStructureStylesheetList(mimeType, list);
        } catch (Exception e) {
            Logger.log(Logger.ERROR,e);
        }
        return list;
    }

    public Hashtable getThemeStylesheetList(String structureStylesheetName) {
        Hashtable list=new Hashtable();

        try {
          GenericPortalBean.getDbImplObject().getThemeStylesheetList(structureStylesheetName, list);
        } catch (Exception e) {
          Logger.log(Logger.ERROR,e);
        }
        return list;
    }

    // functions that allow access to the entire CoreStylesheetDescription object.
    // These functions are used when working with the stylesheet, and not for browsing purposes.
    public StructureStylesheetDescription getStructureStylesheetDescription(String stylesheetName) {
        StructureStylesheetDescription fssd=null;
        try {
          String[] db = GenericPortalBean.getDbImplObject().getStructureStylesheetDescription(stylesheetName);
          String dbStylesheetName=db[0];
          String dbStylesheetDescriptionText=db[1];
          String dbURI=db[2];
          String dbDescriptionURI=db[3];
                fssd=new StructureStylesheetDescription();

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
                fssd.setStylesheetURI(dbURI);
                fssd.setStylesheetDescriptionURI(dbDescriptionURI);
                fssd.setStylesheetWordDescription(xmlStylesheetDescriptionText);

                // populate parameter and attriute tables
                this.populateParameterTable(stylesheetDescriptionXML,fssd);
                this.populateFolderAttributeTable(stylesheetDescriptionXML,fssd);
                this.populateChannelAttributeTable(stylesheetDescriptionXML,fssd);

        } catch (Exception e) {
            Logger.log(Logger.ERROR,e);
        }
        return fssd;
    }

    public ThemeStylesheetDescription getThemeStylesheetDescription(String stylesheetName) {
        ThemeStylesheetDescription sssd=null;
        try {
          String[] db = GenericPortalBean.getDbImplObject().getThemeStylesheetDescription(stylesheetName);
          String dbStylesheetName=db[0];
          String dbStylesheetDescriptionText=db[1];
          String dbURI=db[2];
          String dbDescriptionURI=db[3];
                sssd=new ThemeStylesheetDescription();

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
                sssd.setStylesheetURI(dbURI);
                sssd.setStylesheetDescriptionURI(dbDescriptionURI);
                sssd.setStylesheetWordDescription(xmlStylesheetDescriptionText);
                sssd.setMimeType(this.getRootElementTextValue(stylesheetDescriptionXML,"mimeType"));
                Logger.log(Logger.DEBUG,"CoreStylesheetDescriptionDBImpl::getThemeStylesheetDescription() : setting mimetype=\""+sssd.getMimeType()+"\"");
                sssd.setSerializerName(this.getRootElementTextValue(stylesheetDescriptionXML,"serializer"));
                Logger.log(Logger.DEBUG,"CoreStylesheetDescriptionDBImpl::getThemeStylesheetDescription() : setting serializerName=\""+sssd.getSerializerName()+"\"");
                sssd.setCustomUserPreferencesManager(this.getRootElementTextValue(stylesheetDescriptionXML,"upmanager"));

                // populate parameter and attriute tables
                this.populateParameterTable(stylesheetDescriptionXML,sssd);
                this.populateChannelAttributeTable(stylesheetDescriptionXML,sssd);
                sssd.setStructureStylesheetList(this.getVectorOfSimpleTextElementValues(stylesheetDescriptionXML,"parentstylesheet"));


        } catch (Exception e) {
            Logger.log(Logger.ERROR,e);
        }
        return sssd;
    }


    public void removeStructureStylesheetDescription(String stylesheetName){
        try {
          GenericPortalBean.getDbImplObject().removeStructureStylesheetDescription(stylesheetName);

        } catch (Exception e) {
            Logger.log(Logger.ERROR,e);
        }
    }
    public void removeThemeStylesheetDescription(String stylesheetName){
        try {
          GenericPortalBean.getDbImplObject().removeThemeStylesheetDescription(stylesheetName);
        } catch (Exception e) {
            Logger.log(Logger.ERROR,e);
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
            //            this.populateParameterTable(stylesheetDescriptionXML,fssd);
            //            this.populateFolderAttributeTable(stylesheetDescriptionXML,fssd);
            //            this.populateChannelAttributeTable(stylesheetDescriptionXML,fssd);
            //            fssd.setStylesheetMediaList(this.getVectorOfSimpleTextElementValues(stylesheetDescriptionXML,"media"));


            // now write out the database record

            // first the basic record
            GenericPortalBean.getDbImplObject().addStructureStylesheetDescription(xmlStylesheetName, stylesheetURI, stylesheetDescriptionURI, xmlStylesheetDescriptionText);

        } catch (Exception e) {
            Logger.log(Logger.DEBUG,e);
        }
    }

    public void addThemeStylesheetDescription(String stylesheetDescriptionURI,String stylesheetURI){
        // need to read in the description file to obtain information such as name, word description and mime type list
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
            sssd.setMimeType(this.getRootElementTextValue(stylesheetDescriptionXML,"mimetype"));

            // populate parameter and attribute tables
            //this.populateParameterTable(stylesheetDescriptionXML,sssd);
            //this.populateChannelAttributeTable(stylesheetDescriptionXML,sssd);
            //sssd.setStructureStylesheetList(this.getVectorOfSimpleTextElementValues(stylesheetDescriptionXML,"parentstylesheet"));
            //sssd.setMimeTypeList(this.getVectorOfSimpleTextElementValues(stylesheetDescriptionXML,"mimetype"));

            GenericPortalBean.getDbImplObject().addThemeStylesheetDescription(xmlStylesheetName, stylesheetURI, stylesheetDescriptionURI,
                xmlStylesheetDescriptionText, sssd.getMimeType(), sssd.getStructureStylesheetList().elements());
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
        } else { Logger.log(Logger.DEBUG,"CoreStylesheetDescriptionDBImpl::getName() : no \"name\" element was found under the \"stylesheetdescription\" node!"); return null; }
    }

    private String getRootElementTextValue(Document descr,String elementName) {
        NodeList names=descr.getElementsByTagName(elementName);
        Node name=null;
        for(int i=names.getLength()-1;i>=0;i--) {
            name=names.item(i);
            if(name.getParentNode().getLocalName().equals("stylesheetdescription")) break;
            else name=null;
        }
        if(name!=null) {
            return this.getTextChildNodeValue(name);;
        } else { Logger.log(Logger.DEBUG,"CoreStylesheetDescriptionDBImpl::getRootElementTextValue() : no \""+elementName+"\" element was found under the \"stylesheetdescription\" node!"); return null; }
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
        } else { Logger.log(Logger.DEBUG,"CoreStylesheetDescriptionDBImpl::getName() : no \"description\" element was found under the \"stylesheetdescription\" node!"); return null; }
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

}
