/* Copyright 2002 The JA-SIG Collaborative.  All rights reserved.
*  See license distributed with this file and
*  available online at http://www.uportal.org/license.html
*/

package org.jasig.portal.layout.utils;


import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.Iterator;

import javax.xml.transform.TransformerFactory;
import javax.xml.transform.sax.SAXTransformerFactory;
import javax.xml.transform.sax.TransformerHandler;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;

import org.jasig.portal.ChannelDefinition;
import org.jasig.portal.IChannelRegistryStore;
import org.jasig.portal.ChannelRegistryStoreFactory;
import org.jasig.portal.layout.IAggregatedUserLayoutStore;
import org.jasig.portal.layout.restrictions.UserLayoutRestrictionFactory;
import org.jasig.portal.UserLayoutStoreFactory;
import org.jasig.portal.IUserLayoutStore;
import org.jasig.portal.EntityIdentifier;
import org.jasig.portal.PortalException;
import org.jasig.portal.RDBMServices;
import org.jasig.portal.groups.IEntityGroup;
import org.jasig.portal.groups.IGroupConstants;
import org.jasig.portal.services.GroupService;
import org.jasig.portal.utils.SAX2FilterImpl;
import org.xml.sax.Attributes;
import org.xml.sax.ContentHandler;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.AttributesImpl;
import org.xml.sax.helpers.XMLReaderFactory;

/**
 * A utility class to load pushed fragment configuration into the database
 * used by the pushfragment ant target.
 *
 * @author <a href="mailto:pkharchenko@interactivebusiness.com">Peter Kharchenko</a>
 * @author <a href="mailto:mvi@immagic.com">Michael Ivanov</a>
 * @version 1.0
 */
public class FragmentLoader {

    static final String configXSL="/properties/al/FragmentLoader.xsl";

    public static void main(String[] args) throws Exception {
        IUserLayoutStore layoutStoreImpl = UserLayoutStoreFactory.getUserLayoutStoreImpl();
        if ( layoutStoreImpl != null && !(layoutStoreImpl instanceof IAggregatedUserLayoutStore) )
        {
            System.out.println("Configured UserLayoutStore is: ");
            System.out.println("  " + layoutStoreImpl.getClass().getName());
            System.out.println("This step requires use of:");
            System.out.println("  " + IAggregatedUserLayoutStore.class.getName());
            System.out.println();
            System.out.println(">>>> Skipping this step.");
            System.exit(0);
        }
        String alConfigFile=args[0];
        String outputDataFile=args[1];

        HashMap rNames=new HashMap();

        // compile a table of restriction types
        /*Connection con=null;
        try {
            con=RDBMServices.getConnection();
            if(con!=null) {
                Statement stmt = con.createStatement();

                String query="SELECT RESTRICTION_TYPE,RESTRICTION_NAME FROM UP_RESTRICTIONS";
                ResultSet rs=stmt.executeQuery(query);
                while(rs.next()) {
                    rNames.put(rs.getString("RESTRICTION_NAME"),rs.getString("RESTRICTION_TYPE"));
                    System.out.println("DEBUG: restriction type mapping "+rs.getString("RESTRICTION_NAME")+" -> "+rs.getString("RESTRICTION_TYPE"));
                }

            } else {
                System.out.println("ERROR: unable to obtain database connection.");
                System.exit(1);
            }
        } catch (Exception e) {
            System.out.println("ERROR: exception raised while reading restriction type mappings:");
            e.printStackTrace();
            System.exit(1);
        } finally {
            if(con!=null) {
                RDBMServices.releaseConnection(con);
            }
        }*/
        
        rNames.putAll(UserLayoutRestrictionFactory.getAvailableRestrictions());

        // instantiate transfomer
        SAXTransformerFactory saxTFactory=(SAXTransformerFactory) TransformerFactory.newInstance();

        System.out.println("DEBUG: reading XSLT from url="+FragmentLoader.class.getResource(configXSL));

        XMLReader reader = XMLReaderFactory.createXMLReader();
        // for some weird weird reason, the following way of instantiating the parser causes all elements to dissapear ...
        // nothing like a bizzare bug like that to take up your afternoon :(
        //        XMLReader reader = SAXParserFactory.newInstance().newSAXParser().getXMLReader();
        TransformerHandler thand=saxTFactory.newTransformerHandler(new StreamSource(FragmentLoader.class.getResourceAsStream(configXSL)));

        // instantiate filter
        ConfigFilter filter=new ConfigFilter(thand,rNames);

        reader.setContentHandler(filter);
        thand.setResult(new StreamResult(outputDataFile));
        boolean isException = false;
        try {
         reader.parse(new InputSource(FragmentLoader.class.getResourceAsStream(alConfigFile)));
        } catch ( Exception e ) {
			isException = true;
			System.out.println ( "The pushed fragment file \""+alConfigFile+"\" caused the exception: " );
        	e.printStackTrace();
          }

        if ( !isException ) {
         // Cleaning the database before the DbLoader is called
         DbCleaner.cleanTables(filter.getFragmentIds());
         System.out.println("DEBUG: done");
         System.exit(0); 
        } 
        System.exit(1);
    }


     /**
     * Cleans up the tables contained the old data of the fragments to be reloaded
     * It is used before the DbLoader utility is called
     *
     */
  private static class DbCleaner {


   public static void cleanTables( Map fragmentIds ) {

    Connection con = RDBMServices.getConnection();

    if ( fragmentIds != null && !fragmentIds.isEmpty() ) {

     try {

      PreparedStatement fragmentIdstmt = con.prepareStatement("SELECT FRAGMENT_ID FROM UP_OWNER_FRAGMENT WHERE FRAGMENT_NAME = ?");
      Map oldFragmentIds = new HashMap();
      for ( Iterator i = fragmentIds.keySet().iterator(); i.hasNext(); ) {
       String name = (String)i.next();
       fragmentIdstmt.setString(1,name);
       ResultSet rs = fragmentIdstmt.executeQuery();
       if ( rs.next() ) oldFragmentIds.put(rs.getString(1),name);
       if ( rs != null ) rs.close();
      }
      if ( fragmentIdstmt != null ) fragmentIdstmt.close();

      if ( oldFragmentIds.size() > 0 ) {

        System.out.println("DEBUG: cleaning tables...");

         con.setAutoCommit(false);

         //PreparedStatement deleteLayoutStruct = con.prepareStatement("DELETE FROM UP_LAYOUT_STRUCT_AGGR WHERE FRAGMENT_ID = ?");
         PreparedStatement updateLayoutStruct = con.prepareStatement("UPDATE UP_LAYOUT_STRUCT_AGGR SET FRAGMENT_ID = ? WHERE FRAGMENT_ID = ?");
         PreparedStatement deleteFragments = con.prepareStatement("DELETE FROM UP_FRAGMENTS WHERE FRAGMENT_ID = ?");
         PreparedStatement deleteFragmentRestrictions = con.prepareStatement("DELETE FROM UP_FRAGMENT_RESTRICTIONS WHERE FRAGMENT_ID = ?");
         PreparedStatement deleteFragmentParams = con.prepareStatement("DELETE FROM UP_FRAGMENT_PARAM WHERE FRAGMENT_ID = ?");
         PreparedStatement deleteOwnerFragment = con.prepareStatement("DELETE FROM UP_OWNER_FRAGMENT WHERE FRAGMENT_ID = ?");
         PreparedStatement deleteGroupFragment = con.prepareStatement("DELETE FROM UP_GROUP_FRAGMENT WHERE FRAGMENT_ID = ?");

         try {
          for ( Iterator i = oldFragmentIds.keySet().iterator(); i.hasNext(); ) {
           String oldId = (String) i.next(); 
           int fragmentId = Integer.parseInt(oldId);
           // Setting the parameter - fragment id
           String newId = (String) fragmentIds.get(oldFragmentIds.get(oldId));
           updateLayoutStruct.setInt(1,Integer.parseInt(newId));
           updateLayoutStruct.setInt(2,fragmentId);
           deleteFragments.setInt(1,fragmentId);
           deleteFragmentRestrictions.setInt(1,fragmentId);
           deleteFragmentParams.setInt(1,fragmentId);
           deleteOwnerFragment.setInt(1,fragmentId);
           deleteGroupFragment.setInt(1,fragmentId);

           // Executing statements
           updateLayoutStruct.executeUpdate();
           deleteFragments.executeUpdate();
           deleteFragmentRestrictions.executeUpdate();
           deleteFragmentParams.executeUpdate();
           deleteOwnerFragment.executeUpdate();
           deleteGroupFragment.executeUpdate();
          }
          // Commit
          con.commit();
         } catch ( Exception sqle ) {
            con.rollback();
            System.out.println ( "DEBUG: " + sqle.getMessage() );
            sqle.printStackTrace();
           }

         if ( updateLayoutStruct != null ) updateLayoutStruct.close();
         if ( deleteFragments != null ) deleteFragments.close();
         if ( deleteFragmentRestrictions != null ) deleteFragmentRestrictions.close();
         if ( deleteFragmentParams != null ) deleteFragmentParams.close();
         if ( deleteOwnerFragment != null ) deleteOwnerFragment.close();
         if ( deleteGroupFragment != null ) deleteGroupFragment.close();

         if ( con != null ) con.close();

         System.out.println("DEBUG: cleaning done...");
       } // if end
      } catch ( Exception e ) {
            System.out.println ( "DEBUG: " + e.getMessage() );
            e.printStackTrace();
        }
     } // if end
    }

  }



    /**
     * Attempts to determine group key based on a group name.
     * If the group key can not be determined in a unique way, the method will terminate!
     *
     * @param groupName a <code>String</code> value
     * @return a group key
     */
    static String getGroupKey(String groupName) throws Exception {
        EntityIdentifier[] mg=GroupService.searchForGroups(groupName,IGroupConstants.IS,Class.forName("org.jasig.portal.security.IPerson"));
        if(mg!=null && mg.length>0) {
            if(mg.length>1) {
                // multiple matches
                System.out.println("ERROR: group name \""+groupName+"\" matches several existing groups: [Key\tName\tDescription]");
                for(int i=0;i<mg.length;i++) {
                    IEntityGroup g=GroupService.findGroup(mg[i].getKey());
                    System.out.print("\t\""+g.getKey()+"\"\t"+g.getName()+"\"\t"+g.getDescription());
                }
                System.out.println("Please modify config file to specify group key directly (i.e. <group key=\"keyValue\">...)");
                System.exit(1);
            } else {
                System.out.println("DEBUG: group \""+groupName+"\", key=\""+mg[0].getKey()+"\"");
                return mg[0].getKey();
            }
        } else {
            // didnt' match
            System.out.println("ERROR: can not find user group with name \""+groupName+"\" in the database !");
            // try nonexact match
            EntityIdentifier[] mg2=GroupService.searchForGroups(groupName,IGroupConstants.CONTAINS,Class.forName("org.jasig.portal.security.IPerson"));
            if(mg2!=null && mg2.length>0) {
                System.out.print("Possible matches are: [");
                for(int i=0;i<mg2.length;i++) {
                    IEntityGroup g=GroupService.findGroup(mg2[i].getKey());
                    System.out.print("\""+g.getName()+"\" ");
                }
                System.out.println("]");
            }
             throw new PortalException("ERROR: can not find user group with name \""+groupName+"\" in the database !");
        }
        return null;
    }

    /**
     * A filter that will perform the following functions:
     * - intercept and verify restriction names, writing out ids
     * - intercept and verify user group names, writing out ids
     *
     */
    private static class ConfigFilter extends SAX2FilterImpl {
        private Map rMap;
        private boolean groupMode=false;
        private AttributesImpl groupAtts;
        private String groupLocalName;
        private String groupUri;
        private String groupData=null;
        private Map fragmentIds;
        private static IAggregatedUserLayoutStore layoutStore = null;
        private static IChannelRegistryStore channelStore = null;
        private static String adminId = null;

        public ConfigFilter(ContentHandler ch,Map rMap) throws PortalException {
            super(ch);
            this.rMap=rMap;
            fragmentIds = new HashMap();
            if ( layoutStore == null ) {
             IUserLayoutStore layoutStoreImpl = UserLayoutStoreFactory.getUserLayoutStoreImpl();
             if ( layoutStoreImpl == null || !(layoutStoreImpl instanceof IAggregatedUserLayoutStore) )
              throw new PortalException ( "The user layout store is NULL or must implement IAggregatedUserLayoutStore!" );
             layoutStore =  (IAggregatedUserLayoutStore) layoutStoreImpl;
            }
            if ( channelStore == null )
             channelStore = ChannelRegistryStoreFactory.getChannelRegistryStoreImpl();
        }

        private String getAdminId() throws Exception {
           if ( adminId == null ) {
           	Connection con = RDBMServices.getConnection();
           	ResultSet rs = con.createStatement().executeQuery("SELECT USER_ID FROM UP_USER WHERE USER_NAME='admin'");
           	if ( rs.next() )
           	  adminId = rs.getString(1);
           	rs.close();
           	con.close();
           }
           return adminId;
        }
        
        
        public Set getFragmentNames() {
           return fragmentIds.keySet();
        }
        
        public Map getFragmentIds() {
           return fragmentIds;
        }

        public void characters (char ch[], int start, int length) throws SAXException   {
            if(groupMode) {
                // accumulate character data
                String ds=new String(ch,start,length);
                if(groupData==null) {
                    groupData=ds;
                } else {
                    groupData=groupData+ds;
                }
            } else {
                super.characters(ch,start,length);
            }
        }

        public void startElement (String uri, String localName, String qName, Attributes atts) throws SAXException {

            AttributesImpl ai=new AttributesImpl(atts);

             // Adding the fragment name to the vector
            if ( qName.equals("fragment") ) {
             String name = atts.getValue("name");
             try {
              String id = layoutStore.getNextFragmentId();
              if ( !fragmentIds.containsKey(name) )
                fragmentIds.put(name,id);
              ai.addAttribute(uri,"id","id","CDATA",id);
              ai.addAttribute(uri,"owner","owner","CDATA",getAdminId());
             } catch ( Exception pe ) {
                 throw new SAXException(pe);
               }
            } // Getting the channel ID by the fname
            else if ( qName.equals("channel") ) {
             String fname = atts.getValue("fname");
             ChannelDefinition chanDef;
             try {
              chanDef = channelStore.getChannelDefinition(fname);
             } catch ( Exception e ) {
                 // We have to catch Exception because getChannelDefinition() throws Exception.
                 throw new SAXException(e);
             }
             if (chanDef == null){
                 throw new SAXException("Unable to find definition for channel fname: "+ fname);
             }               
             ai.addAttribute(uri,"id","id","CDATA",chanDef.getId()+"");
            }

            if(qName.equals("group")) { // this could be made more robust by adding another mode for "groups" element
                groupMode=true;
                groupUri=uri; groupLocalName=localName; groupAtts=new AttributesImpl(atts);

            } else if(qName.equals("restriction")) { // this can also be made more robust by adding another mode for "restrictions" element
                // look up restriction name in the DB
                if(ai.getIndex("type")!=-1) {
                        // restriction type was specified
                        if(ai.getIndex("name")!=-1 && rMap.containsKey(ai.getValue("name")) && (!ai.getValue("type").equals(ai.getValue("name")))) {
                            System.out.println("ERROR: specified restriction type \""+ai.getValue("type")+"\" does not match the specified name \""+ai.getValue("name")+"\" in the database. name \""+ai.getValue("name")+"\" matches restriction type \""+(String)rMap.get(ai.getValue("name"))+"\"");
                            System.exit(1);
                        } else {
                            super.startElement(uri,localName,qName,ai);
                        }
                } else {
                    String restrName=ai.getValue("name");
                    if(restrName!=null) {
                        ai.addAttribute(uri,"type","type","CDATA",restrName);
                    } else {
                        System.out.println("ERROR: config file specifies a restriction name \""+restrName+"\" which is not registered with the database!");
                        System.exit(1);
                    }
                    super.startElement(uri,localName,qName,ai);
                }
            } else {
                super.startElement(uri,localName,qName,ai);
            }

        }

        public void endElement (String uri, String localName, String qName)	throws SAXException {

            if(groupMode) {
                if(qName.equals("group")) {
                    if(groupAtts.getIndex("key")==-1) {
                        if(groupData!=null) {
                            String groupKey=null;
                            try {
                                groupKey=getGroupKey(groupData);
                            } catch (Exception e) {
                                System.out.println("ERROR: encountered exception while trying to determine group key for a group name \""+groupData+"\"");
                                e.printStackTrace();
                                System.exit(1);
                            }
                            groupAtts.addAttribute(groupUri,"key","key","CDATA",groupKey);
                            // output group element
                            super.startElement(groupUri,groupLocalName,"group",groupAtts);
                            super.characters(groupData.toCharArray(), 0, groupData.length());
                            super.endElement(groupUri,groupLocalName,"group");
                        } else {
                            System.out.println("ERROR: one of the group elements is empty and no group key has been specified !");
                            System.exit(1);
                        }
                    } else {
                        // check specified group key
                        try {
                            IEntityGroup g=GroupService.findGroup(groupAtts.getValue("key"));
                            if(g!=null) {
                                if(groupData!=null) {
                                    if(g.getName().equals(groupData)) {
                                        System.out.println("DEBUG: group key=\""+groupAtts.getValue("key")+"\" checked out with the name \""+groupData+"\".");
                                        // output group element
                                        super.startElement(groupUri,groupLocalName,"group",groupAtts);
                                        if(groupData!=null) {
                                            super.characters(groupData.toCharArray(), 0, groupData.length());
                                        }
                                        super.endElement(groupUri,groupLocalName,"group");
                                    } else {
                                        System.out.println("ERROR: group key \""+groupAtts.getValue("key")+"\" belongs to a group with a name \""+g.getName()+"\", where the name specified by the config file is \""+groupData+"\". Please fix the config file.");
                                        System.exit(1);
                                    }
                                }
                            } else {
                                System.out.println("ERROR: unable to find a group with a key \""+groupAtts.getValue("key")+"\"! Either correct the key, or consider matching the group by name.");
                                System.exit(1);
                            }
                        } catch (Exception e) {
                            System.out.println("ERROR: exception raised while trying to look up group by a key=\""+groupAtts.getValue("key")+"\"!");
                            e.printStackTrace();
                            System.exit(1);
                        }
                    }
                } else {
                    System.out.println("WARNING: <group/> contains other elements, which it shouldn't! Please check config validity.");
                }
                groupMode=false;
                groupData=null;
                groupAtts=null;
                groupUri=null;
                groupLocalName=null;
            } else {
                super.endElement(uri,localName,qName);
            }
        }
    }
}
