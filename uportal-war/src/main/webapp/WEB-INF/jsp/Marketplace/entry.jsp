<%--
    Licensed to Jasig under one or more contributor license
    agreements. See the NOTICE file distributed with this work
    for additional information regarding copyright ownership.
    Jasig licenses this file to you under the Apache License,
    Version 2.0 (the "License"); you may not use this file
    except in compliance with the License. You may obtain a
    copy of the License at:

    http://www.apache.org/licenses/LICENSE-2.0

    Unless required by applicable law or agreed to in writing,
    software distributed under the License is distributed on
    an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
    KIND, either express or implied. See the License for the
    specific language governing permissions and limitations
    under the License.

--%>

<%@ include file="/WEB-INF/jsp/include.jsp"%>
<script type="text/javascript" src="<rs:resourceURL value="/rs/jquery/1.6.1/jquery-1.6.1.min.js"/>"></script>

<style>
	.marketplace_wrapper{
		min-height:250px;
	}
	.marketplace_description_title h2{
		font-family:'Arial Bold', 'Arial';
		font-weight:700;
		font-size:13px;
		color:#000000;
		text-align:left;
	}
	.marketplace_portlet_title{
		font-family: 'Arial Bold', 'Arial';
		font-weight:700;
		font-size:18px;
		color:#26507D;
		text-align:left;
	}
	.marketplace_description_body{
		font-family:'Arial';
		font-weight:400;
		font-size:12px;
		color:#000000;
		text-align:left;
	}
	.marketplace_dropdown_menu li a{
		font-size:14px;
		text-align:left;
		color:#000000;
	}
	.marketplace_dropdown_button{
		background-color:#666666;
	}
	.marketplace_dropdown_button:first-child{
		color:#FFFFFF
	}
	.marketplace_button_group>.btn-group:last-child>.btn:first-child{
		border-bottom-left-radius:5px;
		border-top-left-radius:5px;
	}
	.marketplace_button_group>.btn-group:first-child>.dropdown-toggle{
		border-top-right-radius:5px;
		border-bottom-right-radius:5px;
	}
	.marketplace_carousel_inner div img{
		margin:auto;
	}
	.marketplace_section_header{
		font-family:'Arial Bold', 'Arial';
		font-weight:700;
		font-size: 16px;
		color:#000000;
		text-align:left;
		font-style: normal;
	}
	.marketplace_release_date{
		font-family:'Arial';
		font-weight:400;
		font-size:14px;
		color: #000000;
		text-align:left;
		font-style:normal;
	}
	.marketplace_release_notes{
		padding-left:20px;
	}
	
	.marketplace_release_note{
		font-family:'Arial';
		font-weight:400;
		font-size:14px;
		color:#000000;
		text-align:left;
		font-style:normal;
		list-style:none;
		display:none;
	}
	
	.marketplace_show{
		display:block;
	}
	
	#marketplace_show_more_less_link{
		font-family: 'Arial';
		font-weight: 400;
		font-size: 14px;
		color: #26507D;
		float: right;
	}
	
</style>

<c:set var="n"><portlet:namespace/></c:set>
<div id="${n}" class="marketplace_wrapper">
	<div>
	 	<div class="row">
			<div class="col-md-3 col-xs-6 marketplace_portlet_title">${Portlet.title}</div>
			<div class="col-md-3 col-xs-6 col-md-push-3" class="${n}go_button">
				<div class="btn-group marketplace_button_group" style="float:right">
					<a href="${renderRequest.contextPath}/p/${Portlet.FName}/render.uP" id="marketplace_go_button" class="btn btn-default marketplace_dropdown_button">Go</a>
					<button type="button" class="btn btn-default dropdown-toggle marketplace_dropdown_button" data-toggle="dropdown">
						<span class="caret"></span>
						<span class="sr-only"></span>
					</button>
					<ul class="dropdown-menu marketplace_dropdown_menu" role="menu">
						<li><a href="${renderRequest.contextPath}/p/${Portlet.FName}/render.uP">Go</a></li>
						<li><a href="#">Add to Favorites</a></li>
						<li class="divider"></li>
						<li><a href="#">Share on Twitter</a></li>
						<li><a href="#">Share on Facebook</a></li>
						<li><a href="#">Copy link</a></li>
					</ul>
				</div>
			</div>
			<div class="col-md-3 col-xs-6 col-md-pull-3" id="${n}portlet_rating">Place Holder for Rating</div>
	 	</div>
	 	<div class="row">
	 		<div class="col-sm-12 marketplace_description_title"><h2>${Portlet.title}</h2></div>
	 		<div class="col-sm-12 marketplace_description_body">${Portlet.description}</div>
	 	</div>
	 	<br>
	 	<!-- Now let's add some Preferences -->
	 	<!-- Start with Screen shots and what not -->
	 	
	 	<c:if test="${not empty Portlet.screenShots}">
	 		<div class="row">
			 	<div class="col-xs-12 col-md-4">
			 		<p>
			 			<span class="marketplace_section_header">SCREENSHOTS/VIDEOS</span>
			 		</p>
			 		<div id="marketplace_screenshots_and_videos" class="carousel slide" data-ride="carousel" data-interval="9000" data-wrap="true">
				 		<ol class="carousel-indicators marketplace_carousel_indicators">
				 			<c:forEach var="screenShot" items="${Portlet.screenShots}" varStatus="loopStatus">
								<li data-target="#marketplace_screenshots_and_videos" data-slide-to="${loopStatus.index}"></li>
							</c:forEach>
				  		</ol>
			 			<div class="carousel-inner marketplace_carousel_inner">
			 				<c:forEach var="screenShot" items="${Portlet.screenShots}">
			 					<div class="item marketplace_screen_shots">
			 						<img src="${screenShot.url}" alt="screenshot for portlet">
			 						<c:if test="${not empty screenShot.captions}">
			 							<div class="carousel-caption">
			 								<c:forEach var="portletCaption" items="${screenShot.captions}">
			 									<h3>${portletCaption}</h3>
			 								</c:forEach>
			 							</div>
			 						</c:if>
			 					</div>
			 				</c:forEach>
			 			</div>
			 			<a class="left carousel-control" href="#marketplace_screenshots_and_videos" data-slide="prev">
					    	<span class="glyphicon glyphicon-chevron-left"></span>
					  	</a>
					  	<a class="right carousel-control" href="#marketplace_screenshots_and_videos" data-slide="next">
					    	<span class="glyphicon glyphicon-chevron-right"></span>
			  			</a>
			 		</div>
			 	</div>
		 	</div>
	 	</c:if>
	 	<br>
	 	<c:if test="${not empty Portlet.portletReleaseNotes}">
	 		<div class="row">
		 		<div class = "col-xs-12 col-md-4">
		 			<p>
		 				<span class="marketplace_section_header">What's New</span>
                        <c:if test="${not empty Portlet.portletReleaseNotes.releaseDate}">
                            <span class="marketplace_release_date">&nbsp;(Released ${Portlet.portletReleaseNotes.releaseDate})</span>
                        </c:if>
                        
		 			</p>
		 			<p>
		 				<br>
		 				<c:if test="${not empty Portlet.portletReleaseNotes.releaseNotes}">
		 					<ul class="marketplace_release_notes">
		 						<c:forEach var="releaseNote" items="${Portlet.portletReleaseNotes.releaseNotes}">
									<li class="marketplace_release_note">- &nbsp;${releaseNote}</li>
								</c:forEach>
							</ul>
		 				</c:if>
		 			</p>
		 			<c:if test="${fn:length(Portlet.portletReleaseNotes.releaseNotes) gt 3}">
		 				<span><a id="marketplace_show_more_less_link">More</a></span>
		 			</c:if>
		 		</div>
	 		</div>
	 	</c:if>
	 	<br>
	 	<br>
	 	<div class="row col-xs-12" style="clear:both;">
	 		<portlet:renderURL var="entryURL" windowState="MAXIMIZED" >
	 		</portlet:renderURL>
	 		<div class="col-xs-4">
	 		</div>
	 		<div class="col-xs-4">
	 		</div>
	 		<div class="col-xs-4" style="float:left">
				<a href="${entryURL}">Back to List</a>
			</div>
	 	</div>
	</div>						
</div>
<script type="text/javascript">
	$(document).ready( function () {
		$(".marketplace_screen_shots:first").addClass("active");
		$(".marketplace_carousel_indicators>li:first-child").addClass("active");
		$(".marketplace_release_notes>li:nth-child(-n+3)").addClass("marketplace_show");
		$('carousel').carousel();
	});
	var lengthLink = document.getElementById('marketplace_show_more_less_link');
	lengthLink.onclick = toggleNotesDisplayLength;
	function toggleNotesDisplayLength(){
		var currentText = lengthLink.innerHTML;
		if($.trim(currentText) == 'More'){
			$(".marketplace_release_notes>li").addClass("marketplace_show");
			lengthLink.innerHTML="Less";
		}else{
			$(".marketplace_release_notes>li:not(:nth-child(-n+3))").removeClass("marketplace_show");
			lengthLink.innerHTML="More";
		}
	}
</script>
