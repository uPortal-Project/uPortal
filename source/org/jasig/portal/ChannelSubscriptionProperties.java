package org.jasig.portal;

import java.util.*;


/** 
 * Used to store static properties of the channel.
 * Current version gathers the following information:
 * <ul> 
 *  <li> Name </li>
 *  <li> isMinimizable </li>
 *  <li> isDetachable </li>
 *  <li> isRemovable </li>
 *  <li> isEditable </li>
 *  <li> hasHelp </li>
 *  <li> DefaultDetachWidth </li>
 *  <li> DefaultDetachHeight </li>
 * </ul>
 * @version  0.1
 * @author Peter Kharchenko
 */

public class ChannelSubscriptionProperties 
{
    private String str_name;

    // I leave those as strings, just in case we'll want to use some weird format
    private String str_defaultDetachWidth;
    private String str_defaultDetachHeight;
    
    private boolean bool_isMinimizable;
    private boolean bool_isDetachable;
    private boolean bool_isRemovable;
    private boolean bool_isEditable;
    private boolean bool_hasHelp;

    public ChannelSubscriptionProperties() {
	// set the default values here ... I've chosen something safe for now
	str_name="";
	str_defaultDetachWidth="200";
	str_defaultDetachHeight="100";
	bool_isMinimizable=true;
	bool_isDetachable=true;
	bool_isRemovable=true;
	bool_isEditable=false;
	bool_hasHelp=false;
    }

    // the set functions
    public void setName(String n) { str_name=n; }
    public void setDefaultDetachWidth(String ddw) { str_defaultDetachWidth=ddw;}
    public void setDefaultDetachHeight(String ddh) { str_defaultDetachHeight=ddh;}
    public void setMinimizable(boolean value) { bool_isMinimizable=value; }
    public void setDetachable(boolean value) { bool_isDetachable=value; }
    public void setRemovable(boolean value) { bool_isRemovable=value; }
    public void setEditable(boolean value) { bool_isEditable=value; }
    public void setHasHelp(boolean value) { bool_hasHelp=value; }

    // the get functions
    public String getName() { return str_name; }
    public String getDefaultDetachWidth() { return str_defaultDetachWidth; }
    public String getDefaultDetachHeight() { return str_defaultDetachHeight; }
    public boolean isMinimizable() {return bool_isMinimizable; } 
    public boolean isDetachable() { return bool_isDetachable; }
    public boolean isRemovable() { return bool_isRemovable; }
    public boolean isEditable() { return bool_isEditable; }
    public boolean hasHelp() { return bool_hasHelp; }

}
