/* Copyright 2005 The JA-SIG Collaborative.  All rights reserved.
*  See license distributed with this file and
*  available online at http://www.uportal.org/license.html
*/

package org.jasig.portal.layout.dlm;

import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.List;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jasig.portal.StructureStylesheetUserPreferences;
import org.jasig.portal.ThemeStylesheetUserPreferences;

/**
 * Distributed layout Extension to user preferences object for stylesheets
 * performing structure or theme transformation. This extension adds support
 * for user preferences set by layout owneres in incorporated elements.
 * @author Mark Boyd <a href="mailto:">mboyd@campuspipeline.com</a>
 * @version $Revision$ $Date$
 * @since uPortal 2.5
 */

public class DistributedUserPreferences
    extends StructureStylesheetUserPreferences
{
    public static final String RCS_ID = "@(#) $Header$";
    private static Log LOG = LogFactory.getLog(DistributedUserPreferences.class);

    protected Hashtable incorporatedChannelAttributeValues;
    protected Hashtable incorporatedFolderAttributeValues;

    /**
     * Creates a new DistributedUserPreferences object with empty tables.
     *
     */
    public DistributedUserPreferences()
    {
        super();
        this.incorporatedChannelAttributeValues=new Hashtable();
        this.incorporatedFolderAttributeValues=new Hashtable();
    }

    /**
     * Creates a DistributedUserPreferences with values for super classes 
     * derived from those of the passed in StructureStylesheetUserPreferences 
     * object.
     * 
     * @param ssup
     */
    public DistributedUserPreferences
        ( StructureStylesheetUserPreferences ssup )
    {
        super(ssup);
        this.incorporatedChannelAttributeValues=new Hashtable();
        this.incorporatedFolderAttributeValues = new Hashtable();
    }

    /**
     * If instantiated with a theme stylesheet preferences then this object
     * should only be used in place of a theme stylesheet user prefs since
     * structure stylesheet oriented variables will not be initialized.
     */
    public DistributedUserPreferences
        ( ThemeStylesheetUserPreferences tsup )
    {
        super(tsup);
        this.incorporatedChannelAttributeValues=new Hashtable();
    }

    /**
     * Creates a new DistributedUserPreferences object populated with all values
     * from the passed-in instance.
     * 
     * @param dup
     */
    public DistributedUserPreferences(DistributedUserPreferences dup)
    {
        super((StructureStylesheetUserPreferences) dup);
        if (dup.incorporatedChannelAttributeValues != null)
            this.incorporatedChannelAttributeValues = new Hashtable(
                    dup.incorporatedChannelAttributeValues);
        if (dup.incorporatedFolderAttributeValues != null)
            this.incorporatedFolderAttributeValues = new Hashtable(
                    dup.incorporatedFolderAttributeValues);
    }
    
    /**
     * Provides a copy of this object with all fields instantiated to reflect 
     * the values of this object. This allows subclasses to override to add
     * correct copying behavior for their added fields.
     * 
     * @return a copy of this object
     */
    public Object newInstance()
    {
        return new DistributedUserPreferences(this);
    }

    //////////// extensions for structure super class
    
    public Enumeration getFolders()
    {
        Enumeration userOwned = folderAttributeValues.keys();
        Enumeration incorporated = incorporatedFolderAttributeValues.keys();
        return new CompositeEnumeration( userOwned, incorporated );
    }

    public boolean hasFolder( String folderId )
    {
        return folderAttributeValues.containsKey( folderId ) ||
        incorporatedFolderAttributeValues.containsKey( folderId );
    }
    
    public String getFolderAttributeValue(String folderId,
                                          String attributeName)
    {
        Integer attributeNumber = (Integer) folderAttributeNumbers
                                                .get(attributeName);

        if(attributeNumber==null)
        {
            LOG.error("Attempting to obtain a non-existing attribute \""
                    + attributeName + "\".");
            return null;
        }
        String value=null;
        List l=(List) folderAttributeValues.get(folderId);
        if(l==null)
        {
            // user attribute changes not found, any incorporated?
            l=(List) incorporatedFolderAttributeValues.get(folderId);
            // non incorporated, use default
            if(l==null)
            {
                return (String) defaultFolderAttributeValues
                .get(attributeNumber.intValue());
            }
            else
            {
                // inc'd list found, is it long enough?
                if(attributeNumber.intValue()<l.size())
                {
                    value=(String) l.get(attributeNumber.intValue());
                }
                // if not long enough, use default
                if(value==null)
                {
                    try
                    {
                        value=(String) defaultFolderAttributeValues
                        .get(attributeNumber.intValue());
                    }
                    catch (IndexOutOfBoundsException e)
                    {
                        LOG.error("Internal Error - attribute name is " +
                                "registered, but no default value is provided.");
                        return null;
                    }
                }
            }
        }
        else // user attribute changes list found for this channel
        {
            // is list long enough for my attribute?
            if(attributeNumber.intValue()<l.size())
            {
                value=(String) l.get(attributeNumber.intValue());
            }
            // if not then delegate to inc'd change if it exists
            if(value==null)
            {
                l=(List) incorporatedFolderAttributeValues.get(folderId);
                if(l==null)
                {
                    // no changes recorded in inc'd values, use default
                    try
                    {
                        value=(String) defaultFolderAttributeValues
                        .get(attributeNumber.intValue());
                    }
                    catch (IndexOutOfBoundsException e)
                    {
                        // no default specified, should never occur.
                        LOG.error("Internal Error - attribute name is " +
                                "registered, but no default value is provided.");
                        return null;
                    }
                }
                else // inc'd list found, is it long enough?
                {
                    if(attributeNumber.intValue()<l.size())
                    {
                        value=(String) l.get(attributeNumber.intValue());
                    }
                    // if not long enough then use default
                    if(value==null)
                    {
                        try
                        {
                            value=(String) defaultFolderAttributeValues
                            .get(attributeNumber.intValue());
                        }
                        catch (IndexOutOfBoundsException e)
                        {
                            LOG.error("Internal Error - attribute name is " +
                            "registered, but no default value is provided.");
                            return null;
                        }
                    }
                }
            }
        }
        return value;
    }

    public void setIncorporatedFolderAttributeValue(String folderSubscribeId,
                                                    String attributeName,
                                                    String attributeValue)
    {
        Integer attributeNumber=(Integer)folderAttributeNumbers
        .get(attributeName);
        
        if(attributeNumber==null)
        {
            LOG.error("Attempting to set a non-existing folder attribute \"" +
                    attributeName + "\".");
            return;
        }
        List l=(List) incorporatedFolderAttributeValues.get(folderSubscribeId);
        if(l==null)
            l=this.createIncorporatedFolder(folderSubscribeId);
        try
        {
            l.set(attributeNumber.intValue(), attributeValue);
        }
        catch (IndexOutOfBoundsException e)
        {
            // bring up the array to the right size
            for(int i=l.size();i<attributeNumber.intValue();i++)
            {
                l.add((String)null);
            }
            l.add(attributeValue);
        }
    }

    public void removeFolder(String folderID) {
        if(folderAttributeValues.remove(folderID)==null &&
           incorporatedFolderAttributeValues.remove( folderID ) == null )
        {
            LOG.error("Attempting to remove an non-existing folder " +
                    "(folderID=\"" + folderID + "\") ");
        }
    }

    public void removeChannel(String channelSubscribeId) {
        if(channelAttributeValues.remove(channelSubscribeId)==null &&
           incorporatedChannelAttributeValues.remove(channelSubscribeId)==null)
            LOG.error("Attempting to remove an non-existing channel " +
                    "(channelSubscribeId=\""+channelSubscribeId+"\").");
    }


    public void removeDefinedFolderAttributeValue( String folderID,
                                                   String attributeName )
    {
        Integer attributeNumber=(Integer)folderAttributeNumbers
        .get(attributeName);

        // if that attribute isn't defined then we are done
        if(attributeNumber==null)
            return;
        
        List l=(List) folderAttributeValues.get(folderID);

        // if no atts found for folder then it doesn't have to be removed
        if(l==null)
            return;
        try
        {
            l.remove( attributeNumber.intValue() );
        }
        catch( Exception e )
        {
            // if index out of bounds then the value aint' there
        }
    }

    private ArrayList createIncorporatedFolder(String folderID)
    {
        ArrayList l=new ArrayList(defaultFolderAttributeValues.size());
        incorporatedFolderAttributeValues.put(folderID,l);
        return l;
    }
    
    public void changeFolderId( String oldFolderId, String newFolderId )
    {
        List l = (List) folderAttributeValues.remove( oldFolderId );
        if ( l != null )
            folderAttributeValues.put( newFolderId, l );
    }

    ////////// extensions to theme stylesheet super class for channels

    public Enumeration getChannels()
    {
        Enumeration userOwned = channelAttributeValues.keys();
        Enumeration incorporated = incorporatedChannelAttributeValues.keys();
        return new CompositeEnumeration( userOwned, incorporated );
    }

    public boolean hasChannel( String chanId )
    {
        return channelAttributeValues.containsKey( chanId ) ||
        incorporatedChannelAttributeValues.containsKey( chanId );
    }
    
    public String getChannelAttributeValue(String channelSubscribeId,
                                           String attributeName)
    {
        Integer attributeNumber=(Integer)channelAttributeNumbers
        .get(attributeName);
        
        if(attributeNumber==null)
        {
            LOG.error("Attempting to obtain a non-existing attribute \"" + 
                    attributeName + "\".");
            return null;
        }
        String value=null;
        List l=(List) channelAttributeValues.get(channelSubscribeId);
        if(l==null)
        {
            // user attribute changes not found, any incorporated?
            l=(List) incorporatedChannelAttributeValues.get(channelSubscribeId);
            // non incorporated, use default
            if(l==null)
            {
                return (String) defaultChannelAttributeValues
                .get(attributeNumber.intValue());
            }
            else
            {
                // inc'd list found, is it long enough?
                if(attributeNumber.intValue()<l.size())
                {
                    value=(String) l.get(attributeNumber.intValue());
                }
                // if not long enough, use default
                if(value==null)
                {
                    try
                    {
                        value=(String) defaultChannelAttributeValues
                        .get(attributeNumber.intValue());
                    }
                    catch (IndexOutOfBoundsException e)
                    {
                        LOG.error("Internal Error - attribute name is " +
                                "registered, but no default value is provided.");
                        return null;
                    }
                }
            }
        }
        else // user attribute changes list found for this channel
        {
            // is list long enough for my attribute?
            if(attributeNumber.intValue()<l.size())
            {
                value=(String) l.get(attributeNumber.intValue());
            }
            // if not then delegate to inc'd change if it exists
            if(value==null)
            {
                l=(List) incorporatedChannelAttributeValues.get(channelSubscribeId);
                if(l==null)
                {
                    // no changes recorded in inc'd values, use default
                    try
                    {
                        value=(String) defaultChannelAttributeValues
                        .get(attributeNumber.intValue());
                    }
                    catch (IndexOutOfBoundsException e)
                    {
                        // no default specified, should never occur.
                        LOG.error("Internal Error - attribute name is " +
                                "registered, but no default value is provided.");
                        return null;
                    }
                }
                else // inc'd list found, is it long enough?
                {
                    if(attributeNumber.intValue()<l.size())
                    {
                        value=(String) l.get(attributeNumber.intValue());
                    }
                    // if not long enough then use default
                    if(value==null)
                    {
                        try
                        {
                            value=(String) defaultChannelAttributeValues
                            .get(attributeNumber.intValue());
                        }
                        catch (IndexOutOfBoundsException e)
                        {
                            LOG.error("Internal Error - attribute name is " +
                                    "registered, but no default value is provided.");
                            return null;
                        }
                    }
                }
            }
        }
        return value;
    }

    public void setIncorporatedChannelAttributeValue(String channelSubscribeId,
                                                     String attributeName,
                                                     String attributeValue)
    {
        Integer attributeNumber=(Integer)channelAttributeNumbers
        .get(attributeName);
        
        if(attributeNumber==null)
        {
            LOG.error("Attempting to set a non-existing channel attribute \""+
                    attributeName+"\".");
            return;
        }
        List l=(List) incorporatedChannelAttributeValues.get(channelSubscribeId);
        if(l==null)
            l=this.createIncorporatedChannel(channelSubscribeId);
        try
        {
            l.set(attributeNumber.intValue(), attributeValue);
        }
        catch (IndexOutOfBoundsException e)
        {
            // bring up the array to the right size
            for(int i=l.size();i<attributeNumber.intValue();i++)
            {
                l.add((String)null);
            }
            l.add(attributeValue);
        }
    }

    public void removeDefinedChannelAttributeValue( String channelID,
                                                    String attributeName )
    {
        Integer attributeNumber=(Integer)channelAttributeNumbers
        .get(attributeName);

        // if that attribute isn't defined then we are done
        if(attributeNumber==null)
            return;
        
        List l=(List) channelAttributeValues.get(channelID);

        // if no atts found for channel then it doesn't have to be removed
        if(l==null)
            return;
        try
        {
            l.remove( attributeNumber.intValue() );
        }
        catch( Exception e )
        {
            // if index out of bounds then the value aint' there
        }
        return;
    }

    private ArrayList createIncorporatedChannel( String channelSubscribeId )
    {
        ArrayList l=new ArrayList(defaultChannelAttributeValues.size());
        incorporatedChannelAttributeValues.put(channelSubscribeId,l);
        return l;
    }
    
    public void changeChannelId( String oldChannelId, String newChannelId )
    {
        List l = (List) channelAttributeValues.remove( oldChannelId );
        if ( l != null )
            channelAttributeValues.put( newChannelId, l );
    }
}
