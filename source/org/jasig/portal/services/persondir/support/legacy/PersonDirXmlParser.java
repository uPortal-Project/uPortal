/* Copyright 2004 The JA-SIG Collaborative.  All rights reserved.
*  See license distributed with this file and
*  available online at http://www.uportal.org/license.html
*/

package org.jasig.portal.services.persondir.support.legacy;

import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jasig.portal.utils.XML;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.DataIntegrityViolationException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * Static function class which parses a Document such as that contained in
 * a PersonDirs.xml file to
 * obtain a List of PersonDirInfo objects.
 * 
 * @author andrew.petro@yale.edu
 * @version $Revision$ $Date$
 */
class PersonDirXmlParser {

    private static final Log log = LogFactory.getLog(PersonDirXmlParser.class);
    
    /**
     * Computes a List of PersonDirInfo objects from a Document.
     * @param personDirDoc PersonDirInfo document
     * @return a List of PersonDirInfo objects
     * @throws IllegalArgumentException if param personDirDoc is null
     * @throws DataAccessException on failure
     */
    static List getPersonDirInfos(Document personDirDoc) {
        if (personDirDoc == null)
            throw new IllegalArgumentException(
                    "Cannot get PersonDirInfos from a null document.");

        List infos = new ArrayList();

        // Each directory source is a <PersonDirInfo> (and its contents)
        NodeList list = personDirDoc.getElementsByTagName("PersonDirInfo");
        for (int i = 0; i < list.getLength(); i++) { // foreach
            // PersonDirInfo
            Element dirinfo = (Element) list.item(i);

            // Java object into which we're trying to parse the PersonDirInfo
            // element
            PersonDirInfo pdi = new PersonDirInfo();
           

                // foreach tag under the <PersonDirInfo>
                for (Node param = dirinfo.getFirstChild(); param != null; 
                    param = param.getNextSibling()) {
                    
                    if (!(param instanceof Element))
                        continue; // whitespace (typically \n) between tags
                    Element pele = (Element) param;
                    String tagname = pele.getTagName();
                    String value = XML.getElementText(pele);

                    try {
                        // each tagname corresponds to an object data field
                        if (tagname.equals("url")) {
                            pdi.setUrl(value);
                        } else if (tagname.equals("res-ref-name")) {
                            pdi.setResRefName(value);
                        } else if (tagname.equals("ldap-ref-name")) {
                            pdi.setLdapRefName(value);
                        } else if (tagname.equals("logonid")) {
                            pdi.setLogonid(value);
                        } else if (tagname.equals("driver")) {
                            pdi.setDriver(value);
                        } else if (tagname.equals("logonpassword")) {
                            pdi.setLogonpassword(value);
                        } else if (tagname.equals("uidquery")) {
                            pdi.setUidquery(value);
                        } else if (tagname.equals("usercontext")) {
                            pdi.setUsercontext(value);
                        } else if (tagname.equals("timeout")) {
                            pdi.setLdaptimelimit(Integer.parseInt(value));
                        } else if (tagname.equals("attributes")) {
                            NodeList anodes = pele
                                    .getElementsByTagName("attribute");
                            int anodecount = anodes.getLength();
                            if (anodecount != 0) {
                                String[] attributenames = new String[anodecount];
                                String[] attributealiases = new String[anodecount];
                                for (int j = 0; j < anodecount; j++) {
                                    Element anode = (Element) anodes.item(j);
                                    NodeList namenodes = anode
                                            .getElementsByTagName("name");
                                    String aname = "$$$";
                                    if (namenodes.getLength() != 0)
                                        aname = XML
                                                .getElementText((Element) namenodes
                                                        .item(0));
                                    attributenames[j] = aname;
                                    NodeList aliasnodes = anode
                                            .getElementsByTagName("alias");
                                    if (aliasnodes.getLength() == 0) {
                                        attributealiases[j] = aname;
                                    } else {
                                        attributealiases[j] = XML
                                                .getElementText((Element) aliasnodes
                                                        .item(0));
                                    }
                                }
                                pdi.setAttributenames(attributenames);
                                pdi.setAttributealiases(attributealiases);

                            } else {
                                // The <attributes> tag contains a list of names
                                // and optionally aliases each in the form
                                // name[:alias]
                                // The name is an LDAP property or database column
                                // name.
                                // The alias, if it exists, is an eduPerson property
                                // that
                                // corresponds to the previous LDAP or DBMS name.
                                // If no alias is specified, the eduPerson name is
                                // also
                                // the LDAP or DBMS column name.
                                StringTokenizer st = new StringTokenizer(value);
                                int n = st.countTokens();
                                String[] attributenames = new String[n];
                                String[] attributealiases = new String[n];
                                for (int k = 0; k < n; k++) {
                                    String tk = st.nextToken();
                                    int pos = tk.indexOf(':');
                                    if (pos > 0) { // There is an alias
                                        attributenames[k] = tk.substring(0, pos);
                                        attributealiases[k] = tk.substring(pos + 1);

                                    } else { // There is no alias
                                        attributenames[k] = tk;
                                        attributealiases[k] = tk;
                                    }
                                }
                                pdi.setAttributenames(attributenames);
                                pdi.setAttributealiases(attributealiases);
                            }
                        } else {
                            log.warn("PersonDirectory::getParameters(): Unrecognized tag ["
                                            + tagname + "] in PersonDirs.xml");
                        }
                    } catch (Throwable t) {
                        throw new DataIntegrityViolationException("Error processing tag [" + 
                                tagname + "] with value [" + value + "] in PersonDirInfo [" + 
                                dirinfo + "]", t);
                    }
                    
                   
                }

            String validationMessage = pdi.validate();
            if (validationMessage != null)
                throw new DataIntegrityViolationException("Processing PersonDirInfo " +
                        "element [" + dirinfo + "] resulted in an invalid" +
                                " PersonDirInfo object: " + validationMessage);
            infos.add(pdi); // Add one LDAP or JDBC source to the list
        }
        return infos;

    }
    
    private PersonDirXmlParser() {
        // this is a static function class.  It is stateless.  It implements no
        // interface.  This method keeps you from instantiating or extending it, since
        // it is intended to be neither instantiated nor extended.
    }
}