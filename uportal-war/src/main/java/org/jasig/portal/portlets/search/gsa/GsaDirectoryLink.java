package org.jasig.portal.portlets.search.gsa;

import javax.xml.bind.annotation.XmlElement;

public class GsaDirectoryLink {
    
    private String name;
    
    private String link;
    
    @XmlElement(name="GD")
    public String getName() {
        return name;
    }
    
    public void setName(String name) {
        this.name = name;
    }
    
    @XmlElement(name="GL")
    public String getLink() {
        return link;
    }
    
    public void setLink(String link) {
        this.link = link;
    }
    
}
