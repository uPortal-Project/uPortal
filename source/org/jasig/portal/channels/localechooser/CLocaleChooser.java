package org.jasig.portal.channels.localechooser;

import  java.io.*;
import  java.util.*;

import org.jasig.portal.IChannel;
import org.jasig.portal.ChannelStaticData;
import org.jasig.portal.ChannelRuntimeData;
import org.jasig.portal.ChannelRuntimeProperties;
import org.jasig.portal.PortalEvent;
import org.jasig.portal.PortalException;
import org.jasig.portal.utils.DocumentFactory;
import org.xml.sax.ContentHandler;
import  org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.jasig.portal.services.LogService;

import org.jasig.portal.i18n.LocaleAwareXSLT;


public class CLocaleChooser implements IChannel
{

    private ChannelRuntimeData runtimeData;
    private ChannelStaticData staticData;
    private final String channelName = "Locale Chooser";
    protected final String sslLocation = "CLocaleChooser.ssl";

    public CLocaleChooser()
    {
    }

    public void setRuntimeData (ChannelRuntimeData rd)
    {
	this.runtimeData = rd;
    }

    public void setStaticData (ChannelStaticData sd)
    {
	this.staticData = sd;
    }

    public ChannelRuntimeProperties getRuntimeProperties()
    {
	return new ChannelRuntimeProperties();
    }

    public void receiveEvent(PortalEvent ev)
    {
    }

    public void renderXML (ContentHandler out) throws PortalException
    {
	Document doc = DocumentFactory.getNewDocument();
	String locale = runtimeData.getParameter("locale");


	// Create <locale-status> element
	Element localeStatusElement = doc.createElement("locale-status");

	if (locale != null)
	    {
		// Create <locale> element
		Element localeElement = doc.createElement("locale");
		localeElement.appendChild(doc.createTextNode(locale));
		localeStatusElement.appendChild(localeElement);
	    }

	doc.appendChild(localeStatusElement);

	LogService.instance().log(LogService.DEBUG, "LocaleChooser - render XML");
	LogService.instance().log(LogService.DEBUG, "LocaleChooser -  locale: " + locale);
	LogService.instance().log(LogService.DEBUG, "LocaleChooser -  sslLocation: " + sslLocation);
	LogService.instance().log(LogService.DEBUG, "LocaleChooser -  XSL= " + locale + "/" + sslLocation);

	LocaleAwareXSLT xslt = new LocaleAwareXSLT(this, runtimeData.getLocales());
	xslt.setXML(doc);
	xslt.setXSL(sslLocation, runtimeData.getBrowserInfo());
	xslt.setTarget(out);
	xslt.setStylesheetParameter("baseActionURL", runtimeData.getBaseActionURL());
	xslt.transform();
  }

}
