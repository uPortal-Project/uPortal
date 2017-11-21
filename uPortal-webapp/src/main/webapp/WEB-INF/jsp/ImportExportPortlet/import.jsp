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
<%@ include file="/WEB-INF/jsp/include.jsp"%>

<style type="text/css">
    td {
        padding: 6px;
    }
</style>

<!-- Portlet -->
<section class="portlet imp-exp view-import">

    <!-- Portlet Titlebar -->
    <header class="titlebar portlet-titlebar">
        <h2 class="title">
            <spring:message code="manage.portlet.entities"/>
        </h2>
        <nav class="toolbar">
            <ul class="btn-group">
                <li class="btn">
                    <a class="button btn btn-primary" href="<portlet:renderURL><portlet:param name="action" value="export"/></portlet:renderURL>">
                        <spring:message code="export"/>
                        <i class="fa fa-download"></i>
                    </a>
                </li>
                <li class="btn">
                    <a class="button btn btn-default" href="<portlet:renderURL><portlet:param name="action" value="delete"/></portlet:renderURL>">
                        <spring:message code="delete"/>
                        <i class="fa fa-trash-o"></i>
                    </a>
                </li>
            </ul>
        </nav>
    </header>

    <!-- Portlet Content -->
    <div class="content portlet-content">

        <h2 class="title">
            <spring:message code="upload.an.entity.to.be.imported"/>
        </h2>
        <div id="uploader-contents">

            <!-- This is the markup for the Fluid Uploader component itself. -->
            <form class="flc-uploader "
                  method="get"
                  enctype="multipart/form-data">

                <!-- The file queue -->
                <div class="">
                    <!-- Top of the queue -->
                    <div class="">
                        <table  class="table" cellspacing="0" cellpadding="0" summary="Headers for the file queue." role="presentation">
                        </table>
                    </div>

                    <!-- Scrollable view -->
                    <div class="flc-scroller ">
                        <div class="">
                            <table cellspacing="0" class="flc-uploader-queue table table-condensed table-striped" summary="Queue of files to upload." role="presentation">
                                <thead>
                                <tr>
                                    <th scope="col" class=""><spring:message code="file.name"/></th>
                                    <th scope="col" class=""><spring:message code="size"/></th>
                                    <th scope="col" class=""><spring:message code="actions"/></th>
                                </tr>
                                </thead>
                                <tbody>
                                    <!-- Rows will be rendered in here. -->

                                    <!-- Template markup for the file queue rows -->
                                    <tr class="flc-uploader-file-tmplt flc-uploader-file ">
                                        <th class="flc-uploader-file-name " scope="row">File Name Placeholder</th>
                                        <td class="flc-uploader-file-size ">0 KB</td>
                                        <td class="">
                                            <button type="button" class="flc-uploader-file-action btn btn-default" tabindex="-1">
                                                <span class=""><spring:message code="remove.from.queue"/></span> <i class="fa fa-close"></i>
                                            </button>
                                        </td>
                                    </tr>

                                    <!-- Template for the file error info rows -->
                                    <tr class="flc-uploader-file-error-tmplt bg-danger">
                                        <td colspan="3" class="flc-uploader-file-error"></td>
                                    </tr>
                                </tbody>
                            </table>
                            <div class="flc-uploader-file-progressor-tmplt ">
                                <span class="">76%</span>
                            </div>
                        </div>
                    </div>

                    <div class="flc-uploader-browse-instructions ">
                        <p>
                            <spring:message htmlEscape="false" code="choose.files"/>
                        </p>
                    </div>

                    <!-- Foot of the queue -->
                    <div class="flc-uploader-queue-footer ">
                        <table summary="Status of file queue." role="presentation">
                            <tr>
                                <td class="">
                                    <span class="flc-uploader-button-browse "></span>
                                </td>
                                <td class="flc-uploader-total-progress-text">
                                    <p>Total: 0 files (0 KB)</p>
                                </td>
                            </tr>
                        </table>
                        <div class="flc-uploader-total-progress ">&nbsp;</div>
                        <div class="flc-uploader-errorsPanel ">
                            <div class="">
                                <span class="flc-uploader-errorPanel-header">Warnings:</span>
                            </div>

                            <!-- The markup for each error section will be rendered into these containers. -->
                            <div class="flc-uploader-errorPanel-section-fileSize"></div>
                            <div class="flc-uploader-errorPanel-section-numFiles"></div>

                            <!-- Error section template.-->
                            <div class="flc-uploader-errorPanel-section-tmplt ">
                                <div class="flc-uploader-errorPanel-section-title ">
                                    <p>x files were too y and were not added to the queue.</p>
                                </div>

                                <div class="flc-uploader-errorPanel-section-details ">
                                    <p>The following files were not added:</p>
                                    <p class="flc-uploader-errorPanel-section-files">file_1, file_2, file_3, file_4, file_5 </p>
                                </div>

                                <button type="button" class="flc-uploader-errorPanel-section-toggleDetails btn btn-default">Hide this list <i class="fa fa-eye-slash"></i></button>
                                <button type="button" class="flc-uploader-errorPanel-section-removeButton btn btn-warning">
                                    <span class="flc-uploader-erroredButton-text ">Remove error <i class="fa fa-remove"></i></span>
                                </button>
                            </div>
                         </div>
                    </div>
                </div>

                <!-- Action buttons -->
                <div class="btn-inline-group">
                    <button type="submit" class="flc-uploader-button-upload btn btn-success" disabled="disabled">
                        <spring:message code="import.button.submit.upload"/>
                        <i class="fa fa-upload"></i>
                    </button>
                    <button type="button" class="flc-uploader-button-pause btn btn-danger">
                        <spring:message code="import.button.submit.stop.upload"/>
                        <i class="fa fa-stop"></i>
                    </button>
                </div>

                <div class="flc-uploader-status-region "></div>
            </form>
        </div>

    </div> <!-- end: portlet-content -->
</section> <!-- end: portlet -->

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
