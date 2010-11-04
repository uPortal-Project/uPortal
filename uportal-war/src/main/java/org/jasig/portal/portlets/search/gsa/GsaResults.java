/**
 * Licensed to Jasig under one or more contributor license
 * agreements. See the NOTICE file distributed with this work
 * for additional information regarding copyright ownership.
 * Jasig licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file
 * except in compliance with the License. You may obtain a
 * copy of the License at:
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on
 * an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

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
