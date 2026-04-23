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
    <div class="clearfix row row2 gallery-inner" style="display:none">
        <div class="col-auto float-start menu-wrapper">
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
        <div class="col content-wrapper" role="tabpanel">
            <div class="clearfix content">
                <div class="clearfix row row2 pane add-content">
                    <div class="col-auto float-start content-filters-wrapper">
                        <div class="categories-column active">
                            <h3 class="portlet-list-link"><span><spring:message code="stuff"/></span></h3>
                            <div class="categories-wrapper active">
                               <div class="portlet-search-view">
                                    <form class="portlet-search-form">
                                        <input id="portletSearch" name="portletSearch" class="portlet-search-input" type="text" placeholder="Search for stuff" />
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
                    <div class="content-results-wrapper col">
                        <div class="column-inner">
                            <div class="results-wrapper row2">
                                <div class="pager-column col-auto float-end">
                                    <div class="column-inner">
                                    </div>
                                </div>
                                <div class="results-column col clearfix">
                                    <div class="results-wrapper portlet-results row2">
                                        <div class="pager-column col-auto float-end">
                                            <div class="column-inner">
                                                <div class="pager pager-top">
                                                    <!--Previous-->
                                                    <div class="pager-button-up pager-previous">
                                                        <a class="pager-button-up-inner" href="#">
                                                            <span><spring:message code="up"/></span>
                                                        </a>
                                                    </div>
                                                    <!--Pager Links-->
                                                    <div style="display:none">
                                                        <ul class="pagination-links pager-links" style="margin:0; display:inline">
                                                            <li class="pager-pageLink"><a href="#">1</a></li>
                                                            <li class="pager-pageLink-disabled">2</li>
                                                            <li class="pager-pageLink"><a href="#">3</a></li>
                                                        </ul>
                                                    </div>
                                                    <!--Pagination-->
                                                    <div class="pager-pagination"></div>
                                                    <!--Pager Summary-->
                                                    <div style="display:none">
                                                        <span class="pager-summary"><spring:message code="show"/></span>
                                                        <span><select class="pager-page-size pager-page-size"></select></span>
                                                    </div>
                                                    <!--Next-->
                                                    <div class="pager-button-down pager-next">
                                                        <a class="pager-button-down-inner" href="#">
                                                            <span><spring:message code="down"/></span>
                                                        </a>
                                                    </div>
                                                </div>
                                            </div>
                                        </div>
                                        <div class="results-column col ui-helper-clearfix">
                                            <ul id="addContentPortletList" class="results-list portlet-list modern-portlet-container">
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
                                </div>
                                <div class="clear-float"></div>
                            </div>
                        </div>
                    </div>
                    <div class="content-modal content-loading"></div>
                </div>

                <div class="clearfix row row2 use-content" style="display:none">
                    <div class="col-auto float-start content-filters-wrapper">
                        <div class="categories-column active">
                            <h3 class="portlet-list-link"><span><spring:message code="stuff"/></span></h3>
                            <div class="categories-wrapper active">
                                <div class="portlet-search-view">
                                    <form class="portlet-search-form">
                                        <input class="portlet-search-input" type="text" placeholder="Search for stuff" />
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
                    <div class="content-results-wrapper col">
                        <div class="column-inner">
                            <div class="results-wrapper row2">
                                <div class="pager-column col-auto float-end">
                                    <div class="column-inner">
                                    </div>
                                </div>
                                <div class="results-column col clearfix">
                                    <div class="results-wrapper portlet-results row2">
                                        <div class="pager-column col-auto float-end">
                                            <div class="column-inner">
                                                <div class="pager pager-top">
                                                    <!--Previous-->
                                                    <div class="pager-button-up pager-previous">
                                                        <a class="pager-button-up-inner" href="#">
                                                            <span><spring:message code="up"/></span>
                                                        </a>
                                                    </div>
                                                    <!--Pager Links-->
                                                    <div style="display:none">
                                                        <ul class="pagination-links pager-links" style="margin:0; display:inline">
                                                            <li class="pager-pageLink"><a href="#">1</a></li>
                                                            <li class="pager-pageLink-disabled">2</li>
                                                            <li class="pager-pageLink"><a href="#">3</a></li>
                                                        </ul>
                                                    </div>
                                                    <!--Pagination-->
                                                    <div class="pager-pagination"></div>
                                                    <!--Pager Summary-->
                                                    <div style="display:none">
                                                        <span class="pager-summary"><spring:message code="show"/></span>
                                                        <span><select class="pager-page-size pager-page-size"></select></span>
                                                    </div>
                                                    <!--Next-->
                                                    <div class="pager-button-down pager-next">
                                                        <a class="pager-button-down-inner" href="#">
                                                            <span><spring:message code="down"/></span>
                                                        </a>
                                                    </div>
                                                </div>
                                            </div>
                                        </div>
                                        <div class="results-column col ui-helper-clearfix">
                                            <ul id="useContentPortletList" class="results-list portlet-list modern-portlet-container">
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
