package org.jasig.portal.channels.version;

import java.util.HashMap;
import java.util.Map;

import org.jasig.portal.ChannelStaticData;
import org.jasig.portal.PortalEvent;
import org.jasig.portal.channels.CAbstractXslt;
import org.jasig.portal.tools.versioning.Version;
import org.jasig.portal.tools.versioning.VersionsManager;
import org.jasig.portal.utils.DocumentFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * CVersion displays the version number of some uPortal component, identified by
 * functional name via a ChannelStaticData parameter. CVersion requires the
 * parameter "componentFunctionalName", which should name the functional name
 * of some component the version of which the channel will display.
 * CVersion accepts the optional parameter "xsltUri", specifying the URI to
 * the XSLT to use for rendering the version information.
 */
public class CVersion extends CAbstractXslt {

    private final VersionsManager versionsManager = VersionsManager
            .getInstance();

    protected Map getStylesheetParams() throws Exception {

        ChannelStaticData staticData = getStaticData();
        String componentFunctionalName = (String) staticData
                .get("componentFunctionalName");

        Version componentVersion = versionsManager
                .getVersion(componentFunctionalName);

        String version = "unknown";

        if (componentVersion != null) {
            int majorVersion = componentVersion.getMajor();
            int minorVersion = componentVersion.getMinor();
            int microVersion = componentVersion.getMicro();

            StringBuffer versionStringBuffer = new StringBuffer();
            versionStringBuffer.append(majorVersion);
            versionStringBuffer.append(".");
            versionStringBuffer.append(minorVersion);
            versionStringBuffer.append(".");
            versionStringBuffer.append(microVersion);
            version = versionStringBuffer.toString();
        }

        HashMap paramMap = new HashMap();
        paramMap.put("version", version);
        return paramMap;
    }

    protected Document getXml() throws Exception {
        Document document = DocumentFactory.getNewDocument();

        Element versionElement = document.createElement("version");
        document.appendChild(versionElement);

        return document;
    }

    protected String getXsltUri() throws Exception {
        ChannelStaticData staticData = getStaticData();
        String xsltUri = (String) staticData.get("xsltUri");
        if (xsltUri == null) {
            xsltUri = "cversion.xsl";
        }
        return xsltUri;
    }

    public void receiveEvent(PortalEvent ev) {
        // do nothing
    }

}
