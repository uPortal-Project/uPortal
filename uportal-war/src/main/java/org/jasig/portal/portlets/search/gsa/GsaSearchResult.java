package org.jasig.portal.portlets.search.gsa;

import javax.xml.bind.annotation.XmlElement;

public class GsaSearchResult {

    private String title;

    private String link;

    private String snippet;

    private String visibleUrl;

    private String shortVisibleUrl;

    @XmlElement(name="T")
    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    @XmlElement(name="U")
    public String getLink() {
        return link;
    }

    public void setLink(String link) {
        this.link = link;
    }

    @XmlElement(name="S")
    public String getSnippet() {
        return snippet;
    }

    public void setSnippet(String snippet) {
        this.snippet = snippet;
    }

    @XmlElement(name="UD")
    public String getVisibleUrl() {
        return visibleUrl;
    }

    public void setVisibleUrl(String visibleUrl) {
        this.visibleUrl = visibleUrl;
    }

    @XmlElement(name="UE")
    public String getShortVisibleUrl() {
        return shortVisibleUrl;
    }

    public void setShortVisibleUrl(String shortVisibleUrl) {
        this.shortVisibleUrl = shortVisibleUrl;
    }

}
