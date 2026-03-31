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
    .uploader-queue-footer td {
        padding: 6px;
    }


</style>

<!-- Portlet -->
<section class="card portlet imp-exp view-import">

    <!-- Portlet Titlebar -->
    <header class="card-header titlebar portlet-titlebar">
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
                    <a class="button btn btn-secondary" href="<portlet:renderURL><portlet:param name="action" value="delete"/></portlet:renderURL>">
                        <spring:message code="delete"/>
                        <i class="fa fa-trash-o"></i>
                    </a>
                </li>
            </ul>
        </nav>
    </header>

    <!-- Portlet Content -->
    <div class="card-body content portlet-content">

        <h2 class="title">
            <spring:message code="upload.an.entity.to.be.imported"/>
        </h2>
        <div id="uploader-contents">

            <!-- This is the markup for the Fluid Uploader component itself. -->
            <form class="uploader-form file-uploader"
                  method="get"
                  enctype="multipart/form-data">

                <!-- The file queue -->
                <div class="uploader-queue-wrapper">
                    <!-- Top of the queue -->
                    <div class="uploader-queue-header">
                        <table  class="table" cellspacing="0" cellpadding="0" summary="Headers for the file queue." role="presentation">
                        </table>
                    </div>

                    <!-- Scrollable view -->
                    <div class="overflow-auto">
                        <div class="h-100">
                            <table cellspacing="0" class="uploader-queue table table-sm table-striped" summary="Queue of files to upload." role="presentation">
                                <thead>
                                <tr>
                                    <th scope="col" class="uploader-file-name"><spring:message code="file.name"/></th>
                                    <th scope="col" class="uploader-file-size"><spring:message code="size"/></th>
                                    <th scope="col" class="uploader-file-actions"><spring:message code="actions"/></th>
                                </tr>
                                </thead>
                                <tbody>
                                    <!-- Rows will be rendered in here. -->

                                    <!-- Template markup for the file queue rows -->
                                    <tr class="uploader-file-template uploader-file d-none">
                                        <th class="uploader-file-name" scope="row">File Name Placeholder</th>
                                        <td class="uploader-file-size">0 KB</td>
                                        <td class="uploader-file-actions">
                                            <button type="button" class="uploader-file-action btn btn-secondary" tabindex="-1">
                                                <span class="uploader-button-text"><spring:message code="remove.from.queue"/></span> <i class="fa fa-close"></i>
                                            </button>
                                        </td>
                                    </tr>

                                    <!-- Template for the file error info rows -->
                                    <tr class="uploader-file-error-template uploader-file-error d-none table-danger">
                                        <td colspan="3" class="uploader-file-error"></td>
                                    </tr>
                                </tbody>
                            </table>
                            <div class="uploader-file-progressor-template uploader-file-progress">
                                <span class="uploader-file-progress-text d-none">76%</span>
                            </div>
                        </div>
                    </div>

                    <div class="uploader-browse-instructions">
                        <p>
                            <spring:message htmlEscape="false" code="choose.files"/>
                        </p>
                    </div>

                    <!-- Foot of the queue -->
                    <div class="uploader-queue-footer">
                        <table summary="Status of file queue." role="presentation">
                            <tr>
                                <td class=".uploader-footer-buttons">
                                    <span class="uploader-button-browse"></span>
                                </td>
                                <td class="uploader-total-progress-text">
                                    <p>Total: 0 files (0 KB)</p>
                                </td>
                            </tr>
                        </table>
                        <div class="uploader-total-progress progress">&nbsp;</div>
                        <div class="uploader-errors-panel alert alert-warning d-none">
                            <div class="uploader-errors-panel-header">
                                <span class="uploader-error-panel-header">Warnings:</span>
                            </div>

                            <!-- The markup for each error section will be rendered into these containers. -->
                            <div class="uploader-error-panel-section-file-size"></div>
                            <div class="uploader-error-panel-section-num-files"></div>

                            <!-- Error section template.-->
                            <div class="uploader-error-panel-section-template d-none">
                                <div class="uploader-error-panel-section-title">
                                    <p>x files were too y and were not added to the queue.</p>
                                </div>

                                <div class="uploader-error-panel-section-details">
                                    <p>The following files were not added:</p>
                                    <p class="uploader-error-panel-section-files">file_1, file_2, file_3, file_4, file_5 </p>
                                </div>

                                <button type="button" class="uploader-error-panel-section-toggle-details btn btn-secondary">Hide this list <i class="fa fa-eye-slash"></i></button>
                                <button type="button" class="uploader-error-panel-section-remove-button btn btn-warning">
                                    <span class="uploader-errored-button-text d-none">Remove error <i class="fa fa-remove"></i></span>
                                </button>
                            </div>
                         </div>
                    </div>
                </div>

                <!-- Action buttons -->
                <div class="uploader-buttons btn-group">
                    <button type="submit" class="uploader-button-upload btn btn-success opacity-50" disabled="disabled" onclick="return false;">
                        <spring:message code="import.button.submit.upload"/>
                        <i class="fa fa-upload"></i>
                    </button>
                    <button type="button" class="uploader-button-pause d-none btn btn-danger">
                        <spring:message code="import.button.submit.stop.upload"/>
                                                <i class="fa fa-stop"></i>
                    </button>
                </div>

                <div class="uploader-status-region visually-hidden"></div>
            </form>
        </div>

    </div> <!-- end: portlet-content -->
</section> <!-- end: portlet -->

<script type="text/javascript">
    up.jQuery(document).ready(function () {
        var myUpload = up.uploader(".uploader-form", {
            queueSettings: {
                uploadURL: "<c:url value="/api/import"/>",
                fileQueueLimit: 1
            }
        });
    });
</script>
