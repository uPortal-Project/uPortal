package org.jasig.portal.portlets.search.gsa;

import java.util.List;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name="GSP")
public class GsaResults {
    
    private List<String> spellingSuggestion;
    
    private List<GsaDirectoryLink> directoryLinks;
    
    private List<GsaSearchResult> searchResults;

    @XmlElement(name="Suggestion")
    @XmlElementWrapper(name="Spelling")
    public List<String> getSpellingSuggestion() {
        return spellingSuggestion;
    }

    public void setSpellingSuggestion(List<String> spellingSuggestion) {
        this.spellingSuggestion = spellingSuggestion;
    }

    @XmlElement(name="GM")
    public List<GsaDirectoryLink> getDirectoryLinks() {
        return directoryLinks;
    }

    public void setDirectoryLinks(List<GsaDirectoryLink> directoryLinks) {
        this.directoryLinks = directoryLinks;
    }

    @XmlElement(name="R")
    @XmlElementWrapper(name="RES")
    public List<GsaSearchResult> getSearchResults() {
        return searchResults;
    }

    public void setSearchResults(List<GsaSearchResult> searchResults) {
        this.searchResults = searchResults;
    }

}
