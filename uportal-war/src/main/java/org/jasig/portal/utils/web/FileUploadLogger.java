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

package org.jasig.portal.utils.web;

import java.util.Arrays;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.fileupload.FileItemHeaders;
import org.apache.commons.fileupload.FileItemIterator;
import org.apache.commons.fileupload.FileItemStream;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.commons.fileupload.servlet.ServletFileUpload;

import com.google.common.collect.Iterators;

/**
 * @author Eric Dalquist
 * @version $Revision$
 */
public class FileUploadLogger {
    public static void logFileUploadInfo(HttpServletRequest request) {
        final boolean multipartContent = ServletFileUpload.isMultipartContent(request);
        System.out.println("multipartContent=" + multipartContent);
        if (!multipartContent) {
            return;
        }

        DiskFileItemFactory factory = new DiskFileItemFactory();
        final ServletFileUpload servletFileUpload = new ServletFileUpload(factory);
        
        try {
            final FileItemIterator itemIterator = servletFileUpload.getItemIterator(request);
            while (itemIterator.hasNext()) {
                final FileItemStream next = itemIterator.next();
                System.out.println("FileItemStream: " + next.getName());
                System.out.println("\t" + next.getContentType());
                System.out.println("\t" + next.getFieldName());
                
                final FileItemHeaders headers = next.getHeaders();
                if (headers != null) {
                    System.out.println("\t" + Arrays.toString(Iterators.toArray(headers.getHeaderNames(), String.class)));
                }
            }
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }
}
