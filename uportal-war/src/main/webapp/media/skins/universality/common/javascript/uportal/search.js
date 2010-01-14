/*
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

// Revision: 2007-8-24 gthompson

var up = up || {};

// Add an instance of the research object to it as a singleton
up.ResearchObject = function () {
    // Private methods
    var buildUrlDefault = function(searchType,searchString)
    {
        return searchType.url+escape(searchString);
    }

    var buildUrlA9 = function(searchType,searchString)
    {
        return searchType.url+escape(searchString);
    }

    var buildUrlAmazon = function(searchType,searchString)
    {
        return searchType.url+escape(searchString)+"?a=obooks&a=cweb";
    }
    
    // Private Data Structure
    var searchTypes = 
    {
        "web":
        {
            "label":"Web Search",
            "options":
            {
                "google":{
                    "title":"Google",
                    "buildUrl":buildUrlDefault,
                    "url":"http://www.google.com/search?q="
                },
                "yahoo":{
                    "title":"Yahoo",
                    "buildUrl":buildUrlDefault,
                    "url":"http://search.yahoo.com/search?p="
                },
                "a9":{
                    "title":"A9.com",
                    "url":"http://a9.com/",
                    "buildUrl":buildUrlA9
                },
                "askjeeves":{
                    "title":"AskJeeves",
                    "buildUrl":buildUrlDefault,
                    "url":"http://web.ask.com/web?q="
                },
                "msn":{
                    "title":"MSN",
                    "buildUrl":buildUrlDefault,
                    "url":"http://search.msn.com/results.aspx?q="
                },
                "snap":{
                    "title":"Snap",
                    "buildUrl":buildUrlDefault,
                    "url":"http://www.snap.com/search.php?query="
                }
            }
        },
        "references":
        {
            "label":"References",
            "options":
            {
                "answers":{
                    "title":"Answers.com",
                    "buildUrl":buildUrlDefault,
                    "url":"http://answers.com/main/ntquery?s="
                },
                "dictionary":{
                    "title":"Dictionary.com",
                    "buildUrl":buildUrlDefault,
                    "url":"http://dictionary.reference.com/search?q="
                },
                "infoplease":{
                    "title":"Infoplease",
                    "buildUrl":buildUrlDefault,
                    "url":"http://www.infoplease.com/search?in=all&query="
                },
                "wikipedia":{
                    "title":"Wikipedia ",
                    "buildUrl":buildUrlDefault,
                    "url":"http://www.google.com/search?domains=en.wikipedia.org&num=20&sitesearch=en.wikipedia.org&q="
                },
                "onelook":{
                    "title":"OneLook",
                    "buildUrl":buildUrlDefault,
                    "url":"http://www.onelook.com/?w="
                },
                "bartleby":{
                    "title":"Bartleby",
                    "buildUrl":buildUrlDefault,
                    "url":"http://www.bartleby.com/cgi-bin/texis/webinator/sitesearch?FILTER=&x=13&y=9&query="
                }
            }
        },
        "news":
        {
            "label":"News",
            "options":
            {
                "googlenews":{
                    "title":"Google News",
                    "buildUrl":buildUrlDefault,
                    "url":"http://news.google.com/news?q="
                },
                "yahoonews":{
                    "title":"Yahoo News",
                    "buildUrl":buildUrlDefault,
                    "url":"http://news.search.yahoo.com/news/search?p="
                }/*,
                "azrepublic":{
                    "title":"Arizona Republic",
                    "buildUrl":buildUrlDefault,
                    "url":"http://search.atomz.com/search/?sp-x-1=&sp-date-range=-1&sp-a=sp10021ba9&sp-f=ISO-8859-1&sp-g=&sp-s=0&sp-p=all&sp-q="
                },
                "asustatepress":{
                    "title":"ASU State Press",
                    "buildUrl":buildUrlDefault,
                    "url":"http://www.statepress.com/advresults.php?searchKeyword="
                } */       
            }
        },
        "books":
        {
            "label":"Books",
            "options":
            {
                "gutenberg":{
                    "title":"Project Gutenberg ",
                    "buildUrl":buildUrlDefault,
                    "url":"http://www.google.com/search?num=20&hl=en&lr=&domains=gutenberg.org&sitesearch=gutenberg.org&q="
                },            
                "amazon":{
                    "title":"Amazon (A9.com)",
                    "url":"http://a9.com/",
                    "buildUrl":buildUrlAmazon
                }        
            }
        }
                    
    }

    // Public methods
    this.openSearchWindow = function (formRef,searchId)
    {
        //Split apart searchId to get the option group id, and the option id
        var parts = searchId.split('|');
        var searchType = searchTypes[parts[0]].options[parts[1]];
        if (searchType && formRef && formRef.search)
        {
            var searchString = formRef.search.value;
            window.open(searchType.buildUrl(searchType,searchString));
        }
        return false;
    }

    // Write Search as HTML into the Document (as document is being read or written)
    this.writeSearchInDocument = function ()
    {

        var defaultSearch = up.jQuery.cookie("searchDefault") || "google";

        var HTMLText = "";
        HTMLText += '<form onsubmit="return up.research.openSearchWindow(this,this.searchType.options[this.searchType.selectedIndex].value)" action="#" id="webSearchForm">';
        HTMLText += '<input type="text" name="search" value="" id="webSearchInput" title="Enter text to search for" />';
        HTMLText += '<select name="searchType" onchange="up.jQuery.cookie(\'searchDefault\',this.options[this.selectedIndex].value.split(\'|\')[1], { path:\'/\' })" title="Select Search Type">';

        for (var i in searchTypes)
        {
            HTMLText += '<optgroup label="'+searchTypes[i].label+'">';
            for (var ii in searchTypes[i].options)
            {
                HTMLText += '<option value="'+i+'|'+ii+'"'+((ii==defaultSearch)?' selected="selected" ':'')+'>'+searchTypes[i].options[ii].title+'</option>';
            }
            HTMLText += '</optgroup>';
        }
        HTMLText += '</select>';
        HTMLText += '<input type="Submit" name="submit" value="Search" id="webSearchSubmit" />';
        HTMLText += '</form>';

        document.writeln(HTMLText);
    }
};