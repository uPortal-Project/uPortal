package org.jasig.portal;

/**
 * Used to store Runtime channel properties.
 * Current version gathers the following information
 * <ul>
 *  <li> willRender  - a boolean flag signaling if the channel will render at all in the current state. This flag will be checked after the ChannelRuntimeData has been passed to a channel, but prior to the renderXML() call.</li>
 * </ul>
 * @version $Revision$
 * @author Peter Kharchenko
 */

public class ChannelRuntimeProperties 
{

    private boolean bool_willRender;
    
    public ChannelRuntimeProperties() {
	// set the default values here
        bool_willRender=true;
    }

    // the set functions ...
    public void setWillRender(boolean value) { bool_willRender=value; }
    
    // the get functions ...
    public boolean willRender() { return bool_willRender; }

}
