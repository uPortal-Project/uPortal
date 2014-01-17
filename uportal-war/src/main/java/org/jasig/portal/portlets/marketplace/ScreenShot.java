package org.jasig.portal.portlets.marketplace;

import java.util.ArrayList;
import java.util.List;

public class ScreenShot{
		private String url;
		private List<String> captions;
		
		public ScreenShot(String url){
			this.setUrl(url);
			this.setCaptions(new ArrayList<String>());
		}
		
		public ScreenShot(String url, List<String> captions){
			this.setUrl(url);
			this.setCaptions(captions);
		}
		public String getUrl() {
			return url;
		}
		public void setUrl(String url) {
			this.url = url;
		}
		/**
		 * @author vertein
		 * @return the captions for a screen shot.  Will not return null, might return empty list.
		 */
		public List<String> getCaptions() {
			if(captions==null){
				this.captions = new ArrayList<String>();
			}
			return captions;
		}
		
		private void setCaptions(List<String> captions) {
			this.captions = captions;
		}
	}