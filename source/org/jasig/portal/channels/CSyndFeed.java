/* Copyright 2001, 2006 The JA-SIG Collaborative.  All rights reserved.
*  See license distributed with this file and
*  available online at http://www.uportal.org/license.html
*/

package org.jasig.portal.channels;

import java.util.Iterator;
import java.util.List;
import java.io.IOException;
import java.io.StringReader;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;

import org.ccil.cowan.tagsoup.Parser;
import org.jasig.portal.ChannelCacheKey;
import org.jasig.portal.ChannelRuntimeData;
import org.jasig.portal.ChannelStaticData;
import org.jasig.portal.ICacheable;
import org.jasig.portal.PortalException;
import org.jasig.portal.ResourceMissingException;
import org.jasig.portal.utils.DocumentFactory;
import org.jasig.portal.utils.ResourceLoader;
import org.jasig.portal.utils.XSLT;
import org.jasig.portal.utils.uri.BlockedUriException;
import org.jasig.portal.utils.uri.IUriScrutinizer;
import org.jasig.portal.utils.uri.PrefixUriScrutinizer;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.xml.sax.ContentHandler;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import com.sun.syndication.feed.synd.SyndContent;
import com.sun.syndication.feed.synd.SyndEntry;
import com.sun.syndication.feed.synd.SyndFeed;
import com.sun.syndication.feed.synd.SyndImage;
import com.sun.syndication.io.FeedException;
import com.sun.syndication.io.SyndFeedInput;
import com.sun.syndication.io.XmlReader;

/**
 * <p>A channel which renders news feeds in the portal.</p>
 *
 * <p>Static channel parameters to be supplied:
 *
 *  1) "xmlUri" -   URI representing the news feed. RSS and	Atom are supported. 
 *  				(See Rome's documentation for specific version information.) 
 *  2) "viewNum" -  Maximum number of news articles to list.
 *  3) "cacheTimeout" - the amount of time (in seconds) that the contents of the
 *                  channel should be cached (optional).  If this parameter is left
 *                  out, a default timeout value will be used.
 *  4) "upc_localConnContext" - The class name of the ILocalConnectionContext 
 *                  implementation.
 *                  <i>Use when local data needs to be sent with the
 *                  request for the URL.</i>
 *  5) "upc_allow_xmlUri_prefixes" - Optional parameter specifying as a whitespace
 *                  delimited String the allowable xmlUri prefixes.  
 *                  <i>Defaults to "http:// https://"</i>
 *  6) "upc_deny_xmlUri_prefixes" - Optional parameter specifying as a whitespace
 *                  delimited String URI prefixes that should block a URI 
 *                  as xmlUri even if it matched one of the allow prefixes.
 *                  <i>Defaults to ""</i>
 *  7) "restrict_xmlUri_inStaticData" - Optional parameter specifying whether 
 *                  the xmlUri should be restricted according to the allow and
 *                  deny prefix rules above as presented in ChannelStaticData
 *                  or just as presented in ChannelRuntimeData.  "true" means
 *                  both ChannelStaticData and ChannelRuntimeData will be restricted.
 *                  Any other value or the parameter not being present means 
 *                  only ChannelRuntimeData will be restricted.  It is important
 *                  to set this value to true when using subscribe-time
 *                  channel parameter configuration of the xmlUri.
 * </p>
 * <p>
 * As of uPortal 2.5.1, the xmlUri must match an allowed URI prefix.
 * By default http:// and https:// URIs are allowed.  If you are using the 
 * empty document or another XML file from the classpath or from the filesystem,
 * you will need to allow a prefix to or the full path of that resource.
 * </p>
 */

public class CSyndFeed extends BaseChannel implements ICacheable{

	
	private static final String SSL_LOCATION = "CSyndFeed/CSyndFeed.ssl";	
    
    private SyndFeed feed = null;
	private int cacheTimeout = 500;
	private IUriScrutinizer uriScrutinizer;


    private int limit = 15;
	private String xmlUri = null;
	
	public void renderXML(ContentHandler out) throws PortalException {
		Document doc;

		feed = getFeed(xmlUri);
		
		if (feed == null){
			doc = buildErrorDocument();
		}else{
		    doc = buildNewsDocument(feed, limit);
		}
		
		if (log.isDebugEnabled()){
			log.debug("XML: "+org.jasig.portal.utils.XML.serializeNode(doc));
		}
		
	    // Now perform the transformation
	    XSLT xslt = XSLT.getTransformer(this);
		xslt.setXML(doc);
	    xslt.setXSL(SSL_LOCATION, runtimeData.getBrowserInfo());
	    xslt.setStylesheetParameter("baseActionURL", runtimeData.getBaseActionURL());
	    xslt.setTarget(out);
	    xslt.transform();
	}

	private static Document buildErrorDocument() {
		Document doc;
		doc = DocumentFactory.getNewDocument();
		// TODO: display something if there was an error with the feed
		Node newsNode = doc.createElement("error");
		newsNode.setTextContent("There was an error retrieving the news source. Please try back later.");
		doc.appendChild(newsNode);
		return doc;
	}

	private static Document buildNewsDocument(SyndFeed feed, int limit) {
		Document doc = DocumentFactory.getNewDocument();
		Node newsNode = doc.createElement("news");
		doc.appendChild(newsNode);
		
		Node temp = doc.createElement("desc");
		temp.setTextContent(feed.getDescription());
		newsNode.appendChild(temp);
		
		temp = doc.createElement("link");
		temp.setTextContent(feed.getLink());
		newsNode.appendChild(temp);
		
		SyndImage image = feed.getImage();
		if (image != null){
			Node imageNode = doc.createElement("image");
			newsNode.appendChild(imageNode);
			
			temp = doc.createElement("url");
			temp.setTextContent(image.getUrl());
			imageNode.appendChild(temp);
			
			temp = doc.createElement("title");
			temp.setTextContent(image.getTitle());
			imageNode.appendChild(temp);
	
			temp = doc.createElement("description");
			temp.setTextContent(image.getDescription());
			imageNode.appendChild(temp);
			
			temp = doc.createElement("link");
			temp.setTextContent(image.getLink());
			imageNode.appendChild(temp);
		}
		
		Node itemsNode = doc.createElement("items");
		newsNode.appendChild(itemsNode);
		
		List entries = feed.getEntries();
		int count = 0;
		for (Iterator i = entries.iterator();i.hasNext() && count < limit;count ++){
			SyndEntry item = (SyndEntry) i.next();
			
			Node itemNode = doc.createElement("item");
			itemsNode.appendChild(itemNode);
			Node n;
			
			n = doc.createElement("title");
			itemNode.appendChild(n);
			n.setTextContent(item.getTitle());
			
			n = doc.createElement("link");
			itemNode.appendChild(n);
			n.setTextContent(item.getLink());

			SyndContent sc = item.getDescription();
			if (sc != null){
				String text = sc.getValue();
				n = doc.createElement("description");
				itemNode.appendChild(n);
		
				// for now we always assume html: see Rome bug #26
	//			if (sc.getType().equals("text/html")){
					Parser p = new Parser();
					try {
						
						SaferHTMLHandler c = new SaferHTMLHandler(doc,n);
						p.setContentHandler(c);
						p.parse(new InputSource(new StringReader(text)));
						
					} catch (IOException e) {
						throw new RuntimeException(e);
					} catch (SAXException e) {
						throw new RuntimeException(e);
					}
	//			}
			}
		}
		return doc;
	}
	
	public void setStaticData(ChannelStaticData sd) throws PortalException {
		staticData = sd;
		
		String allowXmlUriPrefixesParam = 
          sd.getParameter("upc_allow_xmlUri_prefixes");
		String denyXmlUriPrefixesParam = 
          sd.getParameter("upc_deny_xmlUri_prefixes");
      
		uriScrutinizer = 
          PrefixUriScrutinizer.instanceFromParameters(allowXmlUriPrefixesParam, denyXmlUriPrefixesParam);
	      		
	    // determine whether we should restrict what URIs we accept as the xmlUri from
	    // ChannelStaticData
	    String scrutinizeXmlUriAsStaticDataString = sd.getParameter("restrict_xmlUri_inStaticData");
	    boolean scrutinizeXmlUriAsStaticData = "true".equals(scrutinizeXmlUriAsStaticDataString);
	    
	    String xmlUriParam = sd.getParameter("xmlUri");
	    if (scrutinizeXmlUriAsStaticData) {
	        // apply configured xmlUri restrictions
	        setXmlUri(xmlUriParam);
	    } else {
	        // set the field directly to avoid applying xmlUri restrictions
	        xmlUri = xmlUriParam;
	    }		
		
		String param = sd.getParameter("cacheTimeout");
		try{
			cacheTimeout = Integer.parseInt(param);
		}
    	catch(NumberFormatException e){
    		cacheTimeout = 500;
    	}
		param = sd.getParameter("viewNum");
		try{
			limit = Integer.parseInt(param);
		}
    	catch(NumberFormatException e){
    		limit = 15;
    	}
	}

	public void setRuntimeData(ChannelRuntimeData rd) throws PortalException {
		runtimeData = rd;
	}

	private static SyndFeed getFeed(String xmlUri) throws PortalException {
		SyndFeed feed;
		try {
			URL feedUrl;
			feedUrl = new URL(xmlUri);
			SyndFeedInput input = new SyndFeedInput();
			feed = input.build(new XmlReader(feedUrl));
		} catch (MalformedURLException e) {
			throw new PortalException(e);
		} catch (IllegalArgumentException e) {
			throw new PortalException(e);
		} catch (FeedException e) {
			throw new PortalException(e);
		} catch (IOException e) {
			throw new PortalException(e);
		} catch (BlockedUriException e) {
			throw new PortalException(e);
		}
		return feed;
	}
/*
	public final ChannelRuntimeProperties getRuntimeProperties() {
		// this channel returns ChannelRuntimeProperties that specify the
		// dynamic channel title to be the title of the feed.
		
		String title = null;
		if (feed != null)
			title = feed.getTitle();
		return new TitledChannelRuntimeProperties(title);
	}
*/

	public ChannelCacheKey generateKey() {
		ChannelCacheKey k = null;
		k = new ChannelCacheKey();
		k.setKeyScope(ChannelCacheKey.SYSTEM_KEY_SCOPE);
		k.setKey("RSS:xmlUri:"+xmlUri + ",limit:"+  limit);
		k.setKeyValidity(new Long(System.currentTimeMillis()));
		return k;
	}

	public boolean isCacheValid(Object validity) {

	    if (!(validity instanceof Long))
	        return false;
	    return (System.currentTimeMillis() - ((Long)validity).longValue() < cacheTimeout*1000);
	}

    /**
     * Set the URI or resource-relative-path of the XML this channel should
     * render.
     * @param xmlUriArg URI or local resource path to the XML this channel should render.
     * @throws IllegalArgumentException if xmlUriArg specifies a missing resource
     * or if the URI has bad syntax
     * @throws BlockedUriException if the xmlUriArg is blocked for policy reasons
     */
    private void setXmlUri(String xmlUriArg) {
        URL url = null;
        try {
            url = ResourceLoader.getResourceAsURL(this.getClass(), xmlUriArg);
        } catch (ResourceMissingException e) {
            IllegalArgumentException iae = new IllegalArgumentException("Resource [" + xmlUriArg + "] missing.");
            iae.initCause(e);
            throw iae;
        }
        
        String urlString = url.toExternalForm();
        try {
            this.uriScrutinizer.scrutinize(new URI(urlString));
        }catch (URISyntaxException e1) {
            throw new IllegalArgumentException("xmlUri [" + xmlUriArg + "] resolved to a URI with bad syntax.");
        }
        
        this.xmlUri = xmlUriArg;
    }
    
}
