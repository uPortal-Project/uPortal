package org.jasig.portal.layout.dlm;

import org.jasig.portal.PortalException;
import org.jasig.portal.layout.node.IUserLayoutChannelDescription;
import org.jasig.portal.layout.node.UserLayoutChannelDescription;
import org.jasig.portal.layout.node.UserLayoutNodeDescription;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * DLM specific Channel description to protect DLM artifacts of channels.
 * 
 * @author mboyd@sungardct.com
 */
public class ChannelDescription extends UserLayoutChannelDescription
{
    private String plfId = null;
    private String origin = null;
    private FragmentChannelInfo fragmentChannelInfo = null;
    
    public FragmentChannelInfo getFragmentChannelInfo()
    {
        return fragmentChannelInfo;
    }
    public void resetParameter(String parameterName) throws PortalException
    {
        /*
         * If this channel is not from a fragment then let the parent handle
         * resetting to potentially drop back to the publish time parameter
         * value if not ad-hoc. If from a fragment then we drop back to the
         * value held by the fragment or if ad-hoc remove it completely.
         */
        if (this.fragmentChannelInfo != null)
        {
            String value = fragmentChannelInfo.getParameterValue(parameterName);
            if (value == null) // user, ad-hoc parm so delete
            {
                super.remove(parameterName);
            }
            else if (fragmentChannelInfo.canOverrideParameter(parameterName))
                super.setParameterValue(parameterName, value);
        }
        else
            super.resetParameter(parameterName);
    }
    public void setFragmentChannelInfo(FragmentChannelInfo fragmentChannelInfo)
    {
        this.fragmentChannelInfo = fragmentChannelInfo;
    }
    public String getOrigin()
    {
        return origin;
    }
    public void setOrigin(String origin)
    {
        if (origin != null && origin.equals(""))
            origin = null;
        this.origin = origin;
    }
    public String getPlfId()
    {
        return plfId;
    }
    public void setPlfId(String plfId)
    {
        if (plfId != null && plfId.equals(""))
            plfId = null;
        this.plfId = plfId;
    }
    /**
     * For DLM parameters starting with the dlm namespace are reserved for use
     * by DLM and hence can't be overridden. The channel published definition
     * may restrict updating. And finally, a fragment may restrict updating.
     */
    public boolean getParameterOverrideValue(String parameterName)
    {
        if (parameterName.startsWith(Constants.NS)
                || !super.getParameterOverrideValue(parameterName)
                || (fragmentChannelInfo != null && !fragmentChannelInfo
                        .canOverrideParameter(parameterName)))
            return false;
        return true;
    }
    
    /**
     * Shadowing version of the same method in UserLayoutNodeDescription to
     * produce DLM specific instances for channels.
     *
     * @param xmlNode a user layout DTD folder/channel <code>Element</code> value
     * @return an <code>UserLayoutNodeDescription</code> value
     * @exception PortalException if the xml passed is somehow invalid.
     */
    public static UserLayoutNodeDescription createUserLayoutNodeDescription(
            Element xmlNode) throws PortalException
    {
        // is this a channel ?
        String nodeName = xmlNode.getNodeName();
        if (nodeName.equals("channel"))
            return new ChannelDescription(xmlNode);
        else
            return UserLayoutNodeDescription
                    .createUserLayoutNodeDescription(xmlNode);
    }
    
    /**
     * Overridden constructor of super class.
     */
    public ChannelDescription()
    {
        super();
    }
    /**
     * Overridden constructor of super class.
     * 
     * @param xmlNode the Element to be represented
     * @throws PortalException
     */
    public ChannelDescription(Element xmlNode) throws PortalException
    {
        super(xmlNode);
        // dlm-specific attributes
        this.setPlfId(xmlNode.getAttributeNS(Constants.NS_URI, Constants.LCL_PLF_ID));
        this.setOrigin(xmlNode.getAttributeNS(Constants.NS_URI, Constants.LCL_ORIGIN));
    }
    /**
     * Overridden constructor of super class.
     * 
     * @param d an IUserLayoutChannelDescription
     */
    public ChannelDescription(IUserLayoutChannelDescription d)
    {
        super(d);
        
        if (d instanceof ChannelDescription)
        {
            ChannelDescription cd = (ChannelDescription)d;
            this.setPlfId(cd.getPlfId());
            this.setOrigin(cd.getOrigin());
        }
    }
    public Element getXML(Document root)
    {
        Element node = super.getXML(root);
        
        // now add in DLM specific attributes if found
        if (getPlfId() != null)
            node.setAttributeNS(Constants.NS_URI, Constants.ATT_PLF_ID,
                    getPlfId());

        if (getOrigin() != null)
            node.setAttributeNS(Constants.NS_URI, Constants.ATT_ORIGIN,
                    getOrigin());
        return node;
    }
}
