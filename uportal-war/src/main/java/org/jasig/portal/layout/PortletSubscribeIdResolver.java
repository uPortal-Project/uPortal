package org.jasig.portal.layout;

import org.apache.commons.lang.StringUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

/**
 * Returns the subscribe ID of a channel having the passed in functional name or null if it can't find such a channel in
 * the layout.
 */
public class PortletSubscribeIdResolver implements INodeIdResolver {
	private final String fname;

	/**
	 * @param fname Functional name of portlet whose subscribe id to search for.
	 */
	public PortletSubscribeIdResolver(String fname) {
		this.fname = fname;
	}

	@Override
	public String traverseDocument(Document document) {
		final NodeList channels = document.getElementsByTagName("channel");
		for (int i = 0; i < channels.getLength(); i++) {
			final Element e = (Element) channels.item(i);
			if (fname.equals(e.getAttribute("fname"))) {
				String ID = e.getAttribute("ID");
				return StringUtils.isEmpty(ID) ? null : ID;
			}
		}
		return null;
	}
}