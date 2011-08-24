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

<!-- Portlet -->
<div class="fl-widget portlet imp-exp view-import" role="section">
    
    <!-- Portlet Titlebar -->
    <div class="fl-widget-titlebar titlebar portlet-titlebar" role="sectionhead">
        <h2 class="title" role="heading"><spring:message code="import.portlet.entities"/></h2>
        <div class="toolbar" role="toolbar">
            <ul>
                <li><a class="button" href="<portlet:renderURL><portlet:param name="action" value="export"/></portlet:renderURL>"><spring:message code="export"/></a></li>
                <li><a class="button" href="<portlet:renderURL><portlet:param name="action" value="delete"/></portlet:renderURL>"><spring:message code="delete"/></a></li>
            </ul>
        </div>
    </div>
    
    <!-- Portlet Content -->
    <div class="fl-widget-content content portlet-content" role="main">
        
        <!-- Note -->
        <div class="portlet-note" role="note">
            <p><spring:message code="upload.an.entity.to.be.imported"/></p>
        </div>
        
        <div id="uploader-contents">
            
            <!-- This is the markup for the Fluid Uploader component itself. -->
            <form class="flc-uploader fl-uploader" 
                  method="get" 
                  enctype="multipart/form-data">
                      
                <!-- The file queue -->
                <div class="fl-uploader-queue-wrapper">
                    <!-- Top of the queue -->
                    <div class="fl-uploader-queue-header">
                        <table cellspacing="0" cellpadding="0" summary="Headers for the file queue." role="presentation">
                            <caption><spring:message code="file.upload.queue"/>:</caption>
                            <tr>
                                <th scope="col" class="fl-uploader-file-name"><spring:message code="file.name"/></th>
                                <th scope="col" class="fl-uploader-file-size"><spring:message code="size"/></th>
                                <th scope="col" class="fl-uploader-file-actions">&nbsp;</th>
                            </tr>
                        </table>
                    </div>
                    
                    <!-- Scrollable view -->
                    <div class="flc-scroller fl-scroller">
                        <div class="fl-scroller-inner">
                            <table cellspacing="0" class="flc-uploader-queue fl-uploader-queue" summary="Queue of files to upload." role="presentation">
                                <tbody>
                                    <!-- Rows will be rendered in here. -->
                                    
                                    <!-- Template markup for the file queue rows -->
                                    <tr class="flc-uploader-file-tmplt flc-uploader-file fl-uploader-hidden-templates">
                                        <th class="flc-uploader-file-name fl-uploader-file-name" scope="row">File Name Placeholder</th>
                                        <td class="flc-uploader-file-size fl-uploader-file-size">0 KB</td>
                                        <td class="fl-uploader-file-actions">
                                            <button type="button" class="flc-uploader-file-action fl-uploader-file-action" tabindex="-1">
                                                <span class="fl-uploader-button-text fl-uploader-hidden">Remove file from queue</span>
                                            </button>
                                        </td>
                                    </tr>
                                    
                                    <!-- Template for the file error info rows -->
                                    <tr class="flc-uploader-file-error-tmplt fl-uploader-file-error fl-uploader-hidden-templates">
                                        <td colspan="3" class="flc-uploader-file-error"></td>
                                    </tr>
                                </tbody>
                            </table>
                            <div class="flc-uploader-file-progressor-tmplt fl-uploader-file-progress">
                                <span class="fl-uploader-file-progress-text fl-uploader-hidden">76%</span>
                            </div>
                        </div>
                    </div>
                    
                    <div class="flc-uploader-browse-instructions fl-uploader-browse-instructions">
                        Choose <em>Browse files</em> to add files to the queue 
                    </div>
        
                    <!-- Foot of the queue -->
                    <div class="flc-uploader-queue-footer fl-uploader-queue-footer">
                        <table cellspacing="0" cellpadding="0" summary="Status of file queue." role="presentation">
                            <tr>
                                <td class="flc-uploader-total-progress-text">
                                    Total: 0 files (0 KB)
                                </td>
                                <td class=".fl-uploader-footer-buttons" align="right" >
                                    <span class="flc-uploader-button-browse fl-uploader-browse">
                                        <span class="flc-uploader-button-browse-text">Browse files</span>
                                    </span>
                                </td>
                            </tr>
                        </table>
                        <div class="flc-uploader-total-progress fl-uploader-total-progress-okay">&nbsp;</div>
                        <div class="flc-uploader-errorsPanel fl-uploader-errorsPanel">
                             <div class="fl-uploader-errorsPanel-header"><span class="flc-uploader-errorPanel-header">Warnings:</span></div>
            
                             <!-- The markup for each error section will be rendered into these containers. -->
                             <div class="flc-uploader-errorPanel-section-fileSize"></div>
                             <div class="flc-uploader-errorPanel-section-numFiles"></div>
                             
                             <!-- Error section template.-->
                             <div class="flc-uploader-errorPanel-section-tmplt fl-uploader-hidden-templates">
                                 <div class="flc-uploader-errorPanel-section-title fl-uploader-errorPanel-section-title">
                                     x files were too y and were not added to the queue.
                                 </div>
                                 
                                 <div class="flc-uploader-errorPanel-section-details fl-uploader-errorPanel-section-details">
                                     <p>The following files were not added:</p>
                                     <p class="flc-uploader-errorPanel-section-files">file_1, file_2, file_3, file_4, file_5 </p>
                                 </div>
                                 
                                 <button type="button" class="flc-uploader-errorPanel-section-toggleDetails fl-uploader-errorPanel-section-toggleDetails">Hide this list</button>
                                 <button type="button" class="flc-uploader-errorPanel-section-removeButton fl-uploader-errorPanel-section-removeButton">
                                     <span class="flc-uploader-erroredButton-text fl-uploader-hidden">Remove error</span>
                                 </button>
                             </div>
                         </div> 
                    </div>
                </div>
                
                <!-- Action buttons -->
                <div class="fl-uploader-btns">
                    <button type="button" class="flc-uploader-button-pause fl-uploader-pause fl-uploader-hidden">Stop Upload</button>
                    <button type="button" class="flc-uploader-button-upload fl-uploader-upload fl-uploader-button-default fl-uploader-dim" disabled="disabled">Upload</button>
                </div>
                
                <div class="flc-uploader-status-region fl-offScreen-hidden"></div>
            </form>        
        </div>
        
    </div> <!-- end: portlet-content -->
</div> <!-- end: portlet -->

<script type="text/javascript">
    up.jQuery(document).ready(function () {
        var myUpload = up.fluid.uploader(".flc-uploader", {
            strategy: {
                type: "fluid.uploader.progressiveStrategy",
                options: {
                    // Special options for the Flash version of Uploader.
                    flashSettings: {
                        // This option points to the location of the SWFUpload Flash object that ships with Fluid Infusion.
                        flashURL: "/ResourceServingWebapp/rs/fluid/1.3/lib/swfupload.swf",
                        
                        // This option points to the location of the Browse Files button used with Flash 10 clients.
                        flashButtonImageURL: "infusion/components/uploader/images/browse.png"
                    }
                }
            },
            queueSettings: {
                // Set the uploadURL to the URL for posting files to your server.
                uploadURL: "<c:url value="/api/import"/>",
                fileQueueLimit: 1
            }
        });
    });
</script> 
