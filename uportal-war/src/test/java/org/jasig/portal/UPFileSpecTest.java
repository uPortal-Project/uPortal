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

package org.jasig.portal;

import junit.framework.TestCase;

public class UPFileSpecTest extends TestCase {

    String methodValue;
    String methodNodeIdValue;
    String targetNodeIdValue;
    String manual_uPFile;

    public UPFileSpecTest(String name) {
        super(name);
    }

    protected void setUp() {
        methodValue=UPFileSpec.RENDER_URL_ELEMENT;
        methodNodeIdValue="renderNodeIdValue";
        targetNodeIdValue="someTargetValue";
    
        manual_uPFile=methodValue+UPFileSpec.PORTAL_URL_SEPARATOR+methodNodeIdValue+UPFileSpec.PORTAL_URL_SEPARATOR+UPFileSpec.TARGET_URL_ELEMENT+UPFileSpec.PORTAL_URL_SEPARATOR+targetNodeIdValue+UPFileSpec.PORTAL_URL_SEPARATOR+UPFileSpec.PORTAL_URL_SUFFIX;

    }

    public void testCombinedBuild() throws PortalException {
        String uPFile=UPFileSpec.buildUPFile(UPFileSpec.RENDER_METHOD,methodNodeIdValue,targetNodeIdValue,null);
        assertEquals(uPFile,manual_uPFile);        
    }

    public void testIncrementalBuild() throws PortalException {
        UPFileSpec upf=new UPFileSpec();
        upf.setMethod(UPFileSpec.RENDER_METHOD);
        upf.setMethodNodeId(methodNodeIdValue);
        upf.setTargetNodeId(targetNodeIdValue);
        String uPFile=upf.getUPFile();
        assertEquals(uPFile,manual_uPFile);        
    }

    public void testParsing() {
        UPFileSpec fs=new UPFileSpec(manual_uPFile);
        assertEquals(methodValue,fs.getMethod());
        assertEquals(methodNodeIdValue,fs.getMethodNodeId());
        assertEquals(targetNodeIdValue,fs.getTargetNodeId());
    }
    

}
