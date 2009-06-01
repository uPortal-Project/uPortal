/**
 * Copyright (c) 2000-2009, Jasig, Inc.
 * See license distributed with this file and available online at
 * https://www.ja-sig.org/svn/jasig-parent/tags/rel-10/license-header.txt
 */

package org.jasig.portal.channel;

import java.io.Serializable;
import java.util.Iterator;
import java.util.Set;

import org.jasig.portal.IBasicEntity;
import org.jasig.portal.channels.error.CError;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * Implements legacy channel def to XML conversion methods. These should eventually be moved to 
 * a place like the layout code that actually cares about what a channel looks like as XML
 * 
 * @author Eric Dalquist
 * @version $Revision$
 */
@SuppressWarnings("deprecation")
@Deprecated
public abstract class XmlGeneratingBaseChannelDefinition implements IChannelDefinition, IBasicEntity, Serializable {
    private static final long serialVersionUID = 1L;

    /**
     * Get an Element expressing the minimum attributes necessary to represent a
     * channel.
     * 
     * @param doc
     *            Document that will be the owner of the Element returned
     * @param idTag
     *            Value of the identifier for the channel
     * @param chanClassArg
     *            fully qualified class name of the channel
     * @param editable
     *            true if the channel handles the Edit event
     * @param hasHelp
     *            true if the channel handles the Help event
     * @param hasAbout
     *            true if the channel handles the About event
     * @return Element representing the channel
     */
    @Deprecated
    private Element getBase(Document doc, String idTag, String chanClassArg,
            boolean editable, boolean hasHelp, boolean hasAbout) {
        Element channel = doc.createElement("channel");

        // the ID attribute is the identifier for the Channel element
        channel.setAttribute("ID", idTag);
        channel.setIdAttribute("ID", true);

        channel.setAttribute("chanID", this.getId() + "");
        channel.setAttribute("timeout", this.getTimeout() + "");
        final String locale = this.getLocale();
        if (locale != null) {
            channel.setAttribute("name", getName(locale));
            channel.setAttribute("title", getTitle(locale));
            channel.setAttribute("locale", locale);
        } else {
            channel.setAttribute("name", this.getName());
            channel.setAttribute("title", this.getTitle());
        }
        channel.setAttribute("fname", this.getFName());

        // chanClassArg is so named to highlight that we are using the argument
        // to the method rather than the instance variable chanClass
        channel.setAttribute("class", chanClassArg);
        channel.setAttribute("typeID", String.valueOf(this.getTypeId()));
        channel.setAttribute("editable", Boolean.toString(editable));
        channel.setAttribute("hasHelp", Boolean.toString(hasHelp));
        channel.setAttribute("hasAbout", Boolean.toString(hasAbout));
        channel.setAttribute("secure", Boolean.toString(this.isSecure()));
        channel.setAttribute("isPortlet", Boolean.toString(this.isPortlet()));

        return channel;
    }

    @Deprecated
    private final Element nodeParameter(Document doc, String name, int value) {
        return nodeParameter(doc, name, Integer.toString(value));
    }

    @Deprecated
    private final Element nodeParameter(Document doc, String name, String value) {
        Element parameter = doc.createElement("parameter");
        parameter.setAttribute("name", name);
        parameter.setAttribute("value", value);
        return parameter;
    }

    @Deprecated
    private final void addParameters(Document doc, Element channel) {
        final Set<IChannelParameter> parameters = this.getParameters();
        if (parameters != null) {
            Iterator<IChannelParameter> iter = parameters.iterator();
            while (iter.hasNext()) {
                IChannelParameter cp = iter.next();

                Element parameter = nodeParameter(doc, cp.getName(), cp
                        .getValue());
                if (cp.getOverride()) {
                    parameter.setAttribute("override", "yes");
                } else {
                    parameter.setAttribute("override", "no");
                }
                channel.appendChild(parameter);
            }
        }
    }

    /**
     * Display a message where this channel should be
     * @deprecated
     */
    @Deprecated
    public Element getDocument(Document doc, String idTag, String statusMsg,
            int errorId) {
        Element channel = getBase(doc, idTag, CError.class.getName(), false,
                false, false);
        addParameters(doc, channel);
        channel.appendChild(nodeParameter(doc, "CErrorMessage", statusMsg));
        channel.appendChild(nodeParameter(doc, "CErrorChanId", idTag));
        channel.appendChild(nodeParameter(doc, "CErrorErrorId", errorId));
        return channel;
    }

    /**
     * return an xml representation of this channel
     */
    @Deprecated
    public Element getDocument(Document doc, String idTag) {
        Element channel = getBase(doc, idTag, this.getJavaClass(), this.isEditable(),
                this.hasHelp(), this.hasAbout());
        channel.setAttribute("description", this.getDescription());
        addParameters(doc, channel);
        return channel;
    }
}
