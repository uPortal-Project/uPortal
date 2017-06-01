<%--

    Licensed to Apereo under one or more contributor license
    agreements. See the NOTICE file distributed with this work
    for additional information regarding copyright ownership.
    Apereo licenses this file to you under the Apache License,
    Version 2.0 (the "License"); you may not use this file
    except in compliance with the License.  You may obtain a
    copy of the License at the following location:

      http://www.apache.org/licenses/LICENSE-2.0

    Unless required by applicable law or agreed to in writing,
    software distributed under the License is distributed on an
    "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
    KIND, either express or implied.  See the License for the
    specific language governing permissions and limitations
    under the License.

--%>
<%@ include file="/WEB-INF/jsp/include.jsp" %>

<div class="up-gallery">
    <div class="fl-fix fl-col-mixed fl-col-mixed2 gallery-inner" style="display:none">
        <div class="fl-col-side fl-force-left menu-wrapper">
            <ul class="menu" role="menu">
                <li class="add-content-link active" role="tab">
                    <a href="#"><span><spring:message code="add.stuff"/></span></a>
                </li>
                <li class="use-content-link" role="tab">
                    <a href="#"><span><spring:message code="use.it"/></span></a>
                </li>
                <li class="skin-link" role="tab">
                    <a href="#"><span><spring:message code="colors"/></span></a>
                </li>
                <li class="layout-link last" role="tab">
                    <a href="#"><span><spring:message code="layouts"/></span></a>
                </li>
            </ul>
            <!--div class="close-button">
                <a class="button"><span><spring:message code="im.done"/></span></a>
            </div-->
        </div>
        <div class="fl-col-main content-wrapper" role="tabpanel">
            <div class="fl-fix content">
                <!--xsl:call-template name="gallery-add-content-pane"/-->
                <div class="fl-fix fl-col-mixed fl-col-mixed2 pane add-content">
                    <div class="fl-col-fixed fl-force-left content-filters-wrapper">
                        <div class="categories-column active">
                            <h3 class="portlet-list-link"><span><spring:message code="stuff"/></span></h3>
                            <div class="categories-wrapper active">
                               <div class="portlet-search-view">
                                    <form class="portlet-search-form">
                                        <label for="portletSearch"><spring:message code="search.stuff.add"/></label>
                                        <input id="portletSearch" name="portletSearch" class="portlet-search-input" type="text" value="<spring:message code="search"/>" />
                                        <input type="submit" value="<spring:message code="search"/>" class="portlet-search-submit"/>
                                    </form>
                                </div>
                                <div class="categories">
                                    <h4><spring:message code="categories"/></h4>
                                        <div class="category-choice-container">
                                            <ul>
                                                <li class="category-choice">
                                                    <a href="#" class="category-choice-link">
                                                        <span class="category-choice-name"></span>
                                                    </a>
                                                </li>
                                            </ul>
                                        </div>
                                    <div class="clear-float"></div>
                                </div>
                            </div>
                        </div>
                        <div class="packages-column">
                            <h3 class="package-list-link"><span><spring:message code="packaged.stuff"/></span></h3>
                            <div class="packages-wrapper">
                                <div class="packages">
                                    <p><spring:message code="add.package.instruction"/></p>
                                </div>
                            </div>
                        </div>
                    </div>
                    <div class="content-results-wrapper fl-col-main">
                        <div class="column-inner">
                            <div class="results-wrapper fl-col-mixed2">
                                <div class="pager-column fl-col-side fl-force-right">
                                    <div class="column-inner">
                                    </div>
                                </div>
                                <div class="results-column fl-col-main fl-fix">
                                    <!--xsl:call-template name="gallery-add-content-pane-portlet-list">
                                        <xsl:with-param name="CONTEXT" select="'addContent'"/>
                                    </xsl:call-template-->
                                    <div class="results-wrapper portlet-results fl-col-mixed2">
                                        <div class="pager-column fl-col-side fl-force-right">
                                            <div class="column-inner">
                                                <div class="pager flc-pager-top">
                                                    <!--Previous-->
                                                    <div class="pager-button-up flc-pager-previous">
                                                        <a class="pager-button-up-inner" href="#">
                                                            <span><spring:message code="up"/></span>
                                                        </a>
                                                    </div>
                                                    <!--Pager Links-->
                                                    <div style="display:none">
                                                        <ul class="fl-pager-links flc-pager-links" style="margin:0; display:inline">
                                                            <li class="flc-pager-pageLink"><a href="#">1</a></li>
                                                            <li class="flc-pager-pageLink-disabled">2</li>
                                                            <li class="flc-pager-pageLink"><a href="#">3</a></li>
                                                        </ul>
                                                    </div>
                                                    <!--Pagination-->
                                                    <div class="pager-pagination"></div>
                                                    <!--Pager Summary-->
                                                    <div style="display:none">
                                                        <span class="flc-pager-summary"><spring:message code="show"/></span>
                                                        <span><select class="pager-page-size flc-pager-page-size"></select></span>
                                                    </div>
                                                    <!--Next-->
                                                    <div class="pager-button-down flc-pager-next">
                                                        <a class="pager-button-down-inner" href="#">
                                                            <span><spring:message code="down"/></span>
                                                        </a>
                                                    </div>
                                                </div>
                                            </div>
                                        </div>
                                        <div class="results-column fl-col-main ui-helper-clearfix">
                                            <ul id="addContentPortletList" class="results-list portlet-list">
                                                <li class="result-item portlet">
                                                    <div class="ri-wrapper portlet-wrapper">
                                                        <a class="ri-utility portlet-thumb-gripper" href="#" title="<spring:message code="drag.to.add.content"/>"><span>Drag Handle</span></a>
                                                        <a href="#" class="ri-link portlet-thumb-link">
                                                            <span>
                                                                <spring:message code="add"/>
                                                            </span>
                                                        </a>
                                                        <div class="ri-content portlet-thumb-content ui-helper-clearfix">
                                                            <div class="ri-titlebar portlet-thumb-titlebar"></div>
                                                            <div class="ri-icon portlet-thumb-icon"><span>Thumbnail</span></div>
                                                            <div class="ri-description portlet-thumb-description">Description</div>
                                                        </div>
                                                    </div>
                                                </li>
                                            </ul>
                                        </div>
                                    </div>
                                    <!--xsl:call-template name="gallery-add-content-pane-fragment-list"/-->
                                    <div class="results-wrapper package-results fl-col-mixed2" style="display:none">
                                        <div class="pager-column fl-col-side fl-force-right">
                                            <div class="column-inner">
                                                <div class="pager flc-pager-top">
                                                    <!--Previous-->
                                                    <div class="pager-button-up flc-pager-previous">
                                                        <a class="pager-button-up-inner" href="#">
                                                            <span><spring:message code="up"/></span>
                                                        </a>
                                                    </div>
                                                    <!--Pager Links-->
                                                    <div style="display:none">
                                                        <ul class="fl-pager-links flc-pager-links" style="margin:0; display:inline">
                                                            <li class="flc-pager-pageLink"><a href="#">1</a></li>
                                                            <li class="flc-pager-pageLink-disabled">2</li>
                                                            <li class="flc-pager-pageLink"><a href="#">3</a></li>
                                                        </ul>
                                                    </div>
                                                    <!--Pagination-->
                                                    <div class="pager-pagination"></div>
                                                    <!--Pager Summary-->
                                                    <div style="display:none">
                                                        <span class="flc-pager-summary"><spring:message code="show"/></span>
                                                        <span><select class="pager-page-size flc-pager-page-size"></select></span>
                                                    </div>
                                                    <!--Next-->
                                                    <div class="pager-button-down flc-pager-next">
                                                        <a class="pager-button-down-inner" href="#">
                                                            <span><spring:message code="down"/></span>
                                                        </a>
                                                    </div>
                                                </div>
                                            </div>
                                        </div>
                                        <div class="results-column fl-col-main">
                                            <ul class="results-list package-list ui-helper-clearfix">
                                                <li class="results-item package">
                                                   <div class="ri-wrapper package-wrapper">
                                                       <div class="ri-utility"></div>
                                                       <a href="#" class="ri-link package-link"><span><spring:message code="subscribe"/></span></a>
                                                       <div class="ri-content package-content ui-helper-clearfix">
                                                           <div class="ri-titlebar package-titlebar">Titlebar</div>
                                                           <div class="ri-icon package-icon"><span>Thumbnail</span></div>
                                                           <div class="ri-description package-description">Description</div>
                                                       </div>
                                                   </div>
                                                </li>
                                            </ul>
                                        </div>
                                    </div>
                                </div>
                                <div class="clear-float"></div>
                            </div>
                        </div>
                    </div>
                    <div class="content-modal content-loading"></div>
                </div>

                <!--xsl:call-template name="gallery-use-content-pane"/-->
                <div class="fl-fix fl-col-mixed fl-col-mixed2 use-content" style="display:none">
                    <div class="fl-col-fixed fl-force-left content-filters-wrapper">
                        <div class="categories-column active">
                            <h3 class="portlet-list-link"><span><spring:message code="stuff"/></span></h3>
                            <div class="categories-wrapper active">
                                <div class="portlet-search-view">
                                    <form class="portlet-search-form">
                                        <div class="search">
                                            <input class="portlet-search-input" type="text"/>
                                        </div>
                                    </form>
                                </div>
                                <div class="categories">
                                    <h4><spring:message code="categories"/></h4>
                                    <div class="category-choice-container">
                                        <ul>
                                            <li class="category-choice">
                                                <a href="#" class="category-choice-link">
                                                    <span class="category-choice-name"></span>
                                                </a>
                                            </li>
                                        </ul>
                                    </div>
                                    <div class="clear-float"></div>
                                </div>
                            </div>
                        </div>
                    </div>
                    <div class="content-results-wrapper fl-col-main">
                        <div class="column-inner">
                            <div class="results-wrapper fl-col-mixed2">
                                <div class="pager-column fl-col-side fl-force-right">
                                    <div class="column-inner">
                                    </div>
                                </div>
                                <div class="results-column fl-col-main fl-fix">
                                    <!--xsl:call-template name="gallery-add-content-pane-portlet-list">
                                        <xsl:with-param name="CONTEXT" select="'useContent'"/>
                                    </xsl:call-template-->
                                    <div class="results-wrapper portlet-results fl-col-mixed2">
                                        <div class="pager-column fl-col-side fl-force-right">
                                            <div class="column-inner">
                                                <div class="pager flc-pager-top">
                                                    <!--Previous-->
                                                    <div class="pager-button-up flc-pager-previous">
                                                        <a class="pager-button-up-inner" href="#">
                                                            <span><spring:message code="up"/></span>
                                                        </a>
                                                    </div>
                                                    <!--Pager Links-->
                                                    <div style="display:none">
                                                        <ul class="fl-pager-links flc-pager-links" style="margin:0; display:inline">
                                                            <li class="flc-pager-pageLink"><a href="#">1</a></li>
                                                            <li class="flc-pager-pageLink-disabled">2</li>
                                                            <li class="flc-pager-pageLink"><a href="#">3</a></li>
                                                        </ul>
                                                    </div>
                                                    <!--Pagination-->
                                                    <div class="pager-pagination"></div>
                                                    <!--Pager Summary-->
                                                    <div style="display:none">
                                                        <span class="flc-pager-summary"><spring:message code="show"/></span>
                                                        <span><select class="pager-page-size flc-pager-page-size"></select></span>
                                                    </div>
                                                    <!--Next-->
                                                    <div class="pager-button-down flc-pager-next">
                                                        <a class="pager-button-down-inner" href="#">
                                                            <span><spring:message code="down"/></span>
                                                        </a>
                                                    </div>
                                                </div>
                                            </div>
                                        </div>
                                        <div class="results-column fl-col-main ui-helper-clearfix">
                                            <ul id="useContentPortletList" class="results-list portlet-list">
                                                <li class="result-item portlet">
                                                    <div class="ri-wrapper portlet-wrapper">
                                                        <div class="ri-utility"></div>
                                                        <a href="#" class="ri-link portlet-thumb-link">
                                                            <span>
                                                                <spring:message code="use"/>
                                                            </span>
                                                        </a>
                                                        <div class="ri-content portlet-thumb-content ui-helper-clearfix">
                                                            <div class="ri-titlebar portlet-thumb-titlebar"></div>
                                                            <div class="ri-icon portlet-thumb-icon"><span>Thumbnail</span></div>
                                                            <div class="ri-description portlet-thumb-description">Description</div>
                                                        </div>
                                                    </div>
                                                </li>
                                            </ul>
                                        </div>
                                    </div>
                                </div>
                                <div class="clear-float"></div>
                            </div>
                        </div>
                    </div>
                </div>
                <!--xsl:call-template name="gallery-skin-pane"/-->
                <div class="skins" style="display:none">
                    <div class="content-results-wrapper">
                        <div class="column-inner">
                            <div class="results-wrapper">
                                <ul class="results-list skins-list">
                                    <li class="results-item skin">
                                        <div class="ri-wrapper skins-wrapper">
                                            <a class="ri-link skin-link" href="#">
                                                <div class="ri-titlebar skin-titlebar"></div>
                                                <div class="ri-content">
                                                    <div class="ri-icon skin-thumb">
                                                        <span>Thumbnail</span>
                                                    </div>
                                                </div>
                                            </a>
                                        </div>
                                    </li>
                                </ul>
                            </div>
                        </div>
                    </div>
                </div>
                <!--xsl:call-template name="gallery-layout-pane"/-->
                <div class="layouts" style="display:none">
                    <div class="content-results-wrapper">
                        <div class="column-inner">
                            <div class="results-wrapper">
                                <ul class="results-list layouts-list">
                                    <li class="results-item layout">
                                        <div class="ri-wrapper layout-wrapper">
                                            <a class="ri-link layout-link" href="#">
                                                <div class="ri-titlebar layout-titlebar"></div>
                                                <div class="ri-content">
                                                    <div class="ri-icon layout-thumb">
                                                        <span>Thumbnail</span>
                                                    </div>
                                                    <div class="ri-description layout-description"></div>
                                                </div>
                                            </a>
                                        </div>
                                    </li>
                                </ul>
                            </div>
                        </div>
                    </div>
                </div>

            </div>
        </div>
        <div id="galleryLoader" class="gallery-loader"><span><spring:message code="loading"/></span></div>
    </div>
</div>
