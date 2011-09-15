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

package org.jasig.portlet.newsreader.mvc.portlet.reader;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.portlet.PortletPreferences;
import javax.portlet.ResourceRequest;
import javax.portlet.ResourceResponse;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jasig.portlet.newsreader.NewsConfiguration;
import org.jasig.portlet.newsreader.NewsSet;
import org.jasig.portlet.newsreader.adapter.INewsAdapter;
import org.jasig.portlet.newsreader.adapter.NewsException;
import org.jasig.portlet.newsreader.dao.NewsStore;
import org.jasig.portlet.newsreader.model.NewsFeed;
import org.jasig.portlet.newsreader.service.NewsSetResolvingService;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Controller;
import org.springframework.util.DigestUtils;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.portlet.ModelAndView;
import org.springframework.web.portlet.bind.annotation.ResourceMapping;

@Controller
@RequestMapping("VIEW")
public class AjaxNewsController {

    protected final Log log = LogFactory.getLog(getClass());
    
    private NewsStore newsStore;
    
    @Autowired(required = true)
    public void setNewsStore(NewsStore newsStore) {
        this.newsStore = newsStore;
    }
    
    private NewsSetResolvingService setCreationService;
    
    @Autowired(required = true)
    public void setSetCreationService(NewsSetResolvingService setCreationService) {
        this.setCreationService = setCreationService;
    }
    
    private ApplicationContext applicationContext;
    
    @Autowired(required = true)
    public void setApplicationContext(ApplicationContext applicationContext)
            throws BeansException {
        this.applicationContext = applicationContext;
    }
    
    @ResourceMapping
	public ModelAndView getJSONFeeds(ResourceRequest request, ResourceResponse response) throws Exception {
		log.debug("handleAjaxRequestInternal (AjaxNewsController)");
		
        Map<String, Object> model = new HashMap<String, Object>();
		
        String setName = request.getPreferences().getValue("newsSetName", "default");
        NewsSet set = setCreationService.getNewsSet(setName, request);
        List<NewsConfiguration> feeds = new ArrayList<NewsConfiguration>();
        feeds.addAll(set.getNewsConfigurations());
        Collections.sort(feeds);
        
        JSONArray jsonFeeds = new JSONArray();
        List<String> knownFeeds = new ArrayList<String>();
        for(NewsConfiguration feed : feeds) {
            if (feed.isDisplayed()) {
            	JSONObject jsonFeed = new JSONObject();
            	jsonFeed.put("id",feed.getId());
            	jsonFeed.put("name",feed.getNewsDefinition().getName());
            	jsonFeeds.add(jsonFeed);
            	knownFeeds.add(String.valueOf(feed.getId()));
            }
        }
        model.put("feeds", jsonFeeds);
       	
		PortletPreferences prefs = request.getPreferences();
		String activeateNews = request.getParameter("activeateNews");
		if (activeateNews != null) {
			prefs.setValue("activeFeed", activeateNews);
			prefs.store();
		}
		
		int maxStories = Integer.parseInt(prefs.getValue("maxStories", "10"));
		boolean showAuthor = Boolean.parseBoolean( prefs.getValue( "showAuthor", "true" ) );
		
        // only bother to fetch the active feed
        String activeFeed = request.getPreferences().getValue("activeFeed", null);
        
        // if the current active feed no longer exists in the news set, unset it
        if (!knownFeeds.contains(activeFeed)) {
            activeFeed = null;
        }
        
        // if no active feed is currently set, use the first feed in the list
        if (activeFeed == null && jsonFeeds.size() > 0) {
        	activeFeed = ((JSONObject) jsonFeeds.get(0)).getString("id");
			prefs.setValue("activeFeed", activeateNews);
			prefs.store();
        }
        
        if(activeFeed != null) {
	        NewsConfiguration feedConfig = newsStore.getNewsConfiguration(Long.valueOf(activeFeed));
	        model.put("activeFeed", feedConfig.getId());        
	        log.debug("On render Active feed is " + feedConfig.getId());
	        try {
	            // get an instance of the adapter for this feed
	            INewsAdapter adapter = (INewsAdapter) applicationContext.getBean(feedConfig.getNewsDefinition().getClassName());
	            // retrieve the feed from this adaptor
                NewsFeed sharedFeed = adapter.getSyndFeed(feedConfig, request);
                if (sharedFeed != null) {
                    if (sharedFeed.getEntries().size() > maxStories) {
                        NewsFeed limitedFeed = new NewsFeed();
                        limitedFeed.setAuthor(sharedFeed.getAuthor());
                        limitedFeed.setCopyright(sharedFeed.getCopyright());
                        limitedFeed.setLink(sharedFeed.getLink());
                        limitedFeed.setTitle(sharedFeed.getTitle());
                        limitedFeed.setEntries(sharedFeed.getEntries().subList(0, maxStories-1));
                        model.put("feed", limitedFeed);
                    } else {
                        model.put("feed", sharedFeed);
                    }
                } else {
                    log.warn("Failed to get feed from adapter.");
                    model.put("message", "The news \"" + feedConfig.getNewsDefinition().getName() + "\" is currently unavailable.");
                }
	            
	        } catch (NoSuchBeanDefinitionException ex) {
	            log.error("News class instance could not be found: " + ex.getMessage());
	            model.put("message", "The news \"" + feedConfig.getNewsDefinition().getName() + "\" is currently unavailable.");
	        } catch (NewsException ex) {
	            log.warn(ex);
	            model.put("message", "The news \"" + feedConfig.getNewsDefinition().getName() + "\" is currently unavailable.");
	        } catch (Exception ex) {
	            log.error(ex);
	            model.put("message", "The news \"" + feedConfig.getNewsDefinition().getName() + "\" is currently unavailable.");
	        }
        }
        else {
        	//display message saying "Select the news you wish to read"
        	model.put("message", "Select the news you wish to read.");
        }

		log.debug("forwarding to /ajaxFeedList");

//		String etag = String.valueOf(model.hashCode());
//        if (request.getETag() != null && etag.equals(request.getETag())) {
//            response.getCacheControl().setExpirationTime(300);
//            response.getCacheControl().setUseCachedContent(true);
//            return null;
//        }
        
        // create new content with new validation tag
//        response.getCacheControl().setETag(etag);
//        response.getCacheControl().setExpirationTime(300);
	        
		return new ModelAndView("json", model);
	}

}
