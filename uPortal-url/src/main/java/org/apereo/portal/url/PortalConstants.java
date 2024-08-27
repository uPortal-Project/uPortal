package org.apereo.portal.url;

public class PortalConstants {
    static final String SEPARATOR = "_";
    static final String PORTAL_PARAM_PREFIX = "u" + SEPARATOR;

    static final String PORTLET_CONTROL_PREFIX = "pC";
    static final String PORTLET_PARAM_PREFIX = "pP" + SEPARATOR;
    static final String PORTLET_PUBLIC_RENDER_PARAM_PREFIX = "pG" + SEPARATOR;
    static final String PARAM_TARGET_PORTLET = PORTLET_CONTROL_PREFIX + "t";
    static final String PARAM_ADDITIONAL_PORTLET = PORTLET_CONTROL_PREFIX + "a";
    static final String PARAM_DELEGATE_PARENT = PORTLET_CONTROL_PREFIX + "d";
    static final String PARAM_RESOURCE_ID = PORTLET_CONTROL_PREFIX + "r";
    static final String PARAM_CACHEABILITY = PORTLET_CONTROL_PREFIX + "c";
    static final String PARAM_WINDOW_STATE = PORTLET_CONTROL_PREFIX + "s";
    static final String PARAM_PORTLET_MODE = PORTLET_CONTROL_PREFIX + "m";
    static final String PARAM_COPY_PARAMETERS = PORTLET_CONTROL_PREFIX + "p";
    static final String LEGACY_PARAM_PORTLET_FNAME = "uP_fname";
    static final String LEGACY_PARAM_PORTLET_REQUEST_TYPE = "pltc_type";
    static final String LEGACY_PARAM_PORTLET_STATE = "pltc_state";
    static final String LEGACY_PARAM_PORTLET_MODE = "pltc_mode";
    static final String LEGACY_PARAM_PORTLET_PARAM_PREFX = "pltp_";
    static final String LEGACY_PARAM_LAYOUT_ROOT = "root";
    static final String LEGACY_PARAM_LAYOUT_ROOT_VALUE = "uP_root";
    static final String LEGACY_PARAM_LAYOUT_STRUCT_PARAM = "uP_sparam";
    static final String LEGACY_PARAM_LAYOUT_TAB_ID = "activeTab";

    static final String SLASH = "/";
    static final String PORTLET_PATH_PREFIX = "p";
    static final String FOLDER_PATH_PREFIX = "f";
    static final String REQUEST_TYPE_SUFFIX = ".uP";
}
