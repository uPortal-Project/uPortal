
package org.jasig.portal.layout.dlm;


    /** Constants used in DLM.
     */
public class Constants
{
    public static final String RCS_ID = "@(#) $Header$";

    // define the namespace prefixe on dlm elements and attributes.
    public static final String NS = "cp:";
    public static final String NS_DECL = "xmlns:cp";
    public static final String NS_URI ="http://www.campuspipeline.com";
    
    // define the names of attributes that hold layout adjustment permissions
       
    public static final String ATT_DELETE_ALLOWED     = NS+"deleteAllowed";
    public static final String ATT_MOVE_ALLOWED       = NS+"moveAllowed";
    public static final String ATT_EDIT_ALLOWED       = NS+"editAllowed";
    public static final String ATT_ADD_CHILD_ALLOWED  = NS+"addChildAllowed";

    public static final String ATT_ID                  = "ID";
    public static final String ATT_TYPE                = "type";
    public static final String ATT_CHANNEL_ID          = "chanID";
    public static final String ATT_HIDDEN              = "hidden";
    public static final String ATT_NAME                = "name";

    public static final String ATT_PLF_ID              = NS+"plfID";
    public static final String ATT_ORIGIN              = NS+"origin";
    public static final String ATT_PRECEDENCE          = NS+"precedence";
    public static final String ATT_FRAGMENT            = NS+"fragment";
    public static final String LCL_FRAGMENT_NAME       =    "fragmentName";
    public static final String ATT_FRAGMENT_NAME       = NS+LCL_FRAGMENT_NAME;
    public static final String LCL_IS_TEMPLATE_USER    =    "isTemplateUser";
    public static final String ATT_IS_TEMPLATE_USER    = NS+LCL_IS_TEMPLATE_USER;
    public static final String ATT_TEMPLATE_LOGIN_ID   = NS+"templateLoginID";
    public static final String ATT_DEBUG_PRINT         = NS+"debugPrint";

    public static final String ELM_POSITION_SET        = NS+"positionSet";
    public static final String ELM_POSITION            = NS+"position";
    public static final String ELM_DELETE_SET          = NS+"deleteSet";
    public static final String ELM_DELETE              = NS+"delete";
    public static final String ELM_EDIT_SET            = NS+"editSet";
    public static final String ELM_EDIT                = NS+"edit";
    public static final String ELM_PREF                = NS+"pref";
    
    public static final String PLF = "RDBMDistributedLayoutStore.PLF";
    public static final String STRUCTURE_PREFS
                                    = "StructureStylesheetUserPreferences";
    public static final String THEME_PREFS = "ThemeStylesheetUserPreferences";

    public static final String DIRECTIVE_PREFIX = "d";
    
}

