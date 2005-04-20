package org.jasig.portal.layout.dlm.providers;

import org.jasig.portal.layout.dlm.Constants;
import org.jasig.portal.services.LogService;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;



public class ExtensionClass
{
    public static final String RCS_ID = "@(#) $Header$";

    private static ThreadLocal cThreadedCpInfo = null;

    public static void setThreadedCpInfo(Object cpInfo)
    {
        if (cThreadedCpInfo == null) 
        {
            cThreadedCpInfo = new ThreadLocal();
        }
        cThreadedCpInfo.set(cpInfo);
    }

    /**
     * Return the cpInfo Node tree for the current channel.  The current thread is checked
     * via ThreadLocal to determine the correct cpInfo structure to return.  This method is
     * intended for use directly by a channel by calling this as an extension function from
     * the stylesheet.
     *
     * @return a <code>Node</code> object that should be of type <code>Element</code>
     */
    public static Node getCpInfo()
    {
        if (cThreadedCpInfo == null)
        {
            LogService.log( LogService.DEBUG, "cThreadedCpInfo == null" );
            throw new IllegalStateException("cThreadedCpInfo == null");
        }
        else if (cThreadedCpInfo.get() == null)
        {
            LogService.log( LogService.DEBUG, "cThreadedCpInfo.get() == null" );
            throw new IllegalStateException("cThreadedCpInfo.get() == null");
        }
        
        return (Node)cThreadedCpInfo.get();
    }

    /**
     * Return an array of attribute values for the given attribute name.  If there are no
     * values found a zero length array is returned.  These values correspond to the ldap
     * attributes specified in the cpinfo.properties file.
     *
     * @param LDAPAttributeName String values of the LDAP attribute ie "pdsRole"
     * @return an array of (<code>String</code>)
     */
    public static String[] getLDAPAttributes(String LDAPAttributeName)
    {
        LogService.log( LogService.DEBUG, "In getLDAPAttributes.  Param is: " + LDAPAttributeName );
        // return the LDAP Attributes for the given name
        // This will be stored as a child element named cp:cpProperty in the cpInfo Node
        // return null if none present or if the LDAPAttributeName is null
        if (LDAPAttributeName == null) 
        {
            return new String[0];
        }
        Element cpInfo = (Element)cThreadedCpInfo.get();
        // Sanity check - cpInfo Node should be an Element Node
        if (cpInfo.getNodeType() != Node.ELEMENT_NODE) 
        {
            LogService.log( LogService.ERROR, "cpInfo Node in ExtensionClass is the wrong type: " + cpInfo.getNodeType() );
            return new String[0];
        }
        if (!cpInfo.hasChildNodes()) 
        {
            LogService.log( LogService.DEBUG, "cpInfo has no child nodes" );
            return new String[0];
        }

        // Look for a cp:cpProperty child element node with name==LDAPAttributeName
        NodeList TempList = cpInfo.getElementsByTagNameNS(Constants.NS_URI,"cpProperty");
        LogService.log( LogService.DEBUG, "TempList obtained OK" );
        Element TempElement = null;

        for (int i = 0; i < TempList.getLength(); i++) 
        {
            TempElement = (Element)TempList.item(i);
            if (TempElement.getAttribute("name").equals(LDAPAttributeName))
            {
                // We've found the right node, now get the children
                LogService.log( LogService.DEBUG, "We have the correct node at: " + i );
                TempList = TempElement.getElementsByTagNameNS(Constants.NS_URI,"cpValue");
                int TempLength = TempList.getLength();
                if (TempLength == 0) 
                {
                    // No text Nodes are found, so return
                    return new String[0];
                }
                String[] ReturnValues = new String[TempLength];
                for (int j = 0; j < TempLength; j++)
                {
                    LogService.log( LogService.DEBUG, "About to set ReturnValues[" + j + "] to: " + TempList.item(j).getFirstChild().getNodeValue() );
                    ReturnValues[j] = TempList.item(j).getFirstChild().getNodeValue();
                    LogService.log( LogService.DEBUG, "Just set ReturnValues[" + j + "]" );
                }
                return ReturnValues;
            }
        }

        // We didn't find any values for this LDAP property
        LogService.log( LogService.DEBUG, "We didn't find any values for this LDAP property.  About to return array of length 0." );
        return new String[0];
    }

    /**
     * Return the property value for the given property name.  If there is no
     * value found a blank String is returned.  This value corresponds to the 
     * properties specified in the cpinfo.properties file.
     *
     * @param DirectoryPropertyName String values of the directory property ie "calendar.enabled"
     * @return a (<code>String</code>)
     */
    public static String getDirectoryProperty(String DirectoryPropertyName)
    {
        // return the DirectoryProperty value for the given name
        // This will be stored as an attribute in the cpInfo Node
        // return null if none present or if the DirectoryPropertyName is null
        if (DirectoryPropertyName == null) 
        {
            return "";
        }
        Element cpInfo = (Element)cThreadedCpInfo.get();
        // Sanity check - cpInfo Node should be an Element Node
        if (cpInfo.getNodeType() != Node.ELEMENT_NODE) 
        {
            LogService.log( LogService.ERROR, "cpInfo Node in ExtensionClass is the wrong type: " + cpInfo.getNodeType() );
            return "";
        }
        return cpInfo.getAttribute(DirectoryPropertyName);
    }

}

