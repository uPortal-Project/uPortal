/* Copyright 2002 The JA-SIG Collaborative.  All rights reserved.
*  See license distributed with this file and
*  available online at http://www.uportal.org/license.html
*/

package org.jasig.portal;

import junit.framework.*;
import org.jasig.portal.UPFileSpec;
import org.jasig.portal.PortalException;

public class UPFileSpecTest extends TestCase {

    String tagValue;
    String methodValue;
    String methodNodeIdValue;
    String targetNodeIdValue;
    String manual_uPFile;

    public UPFileSpecTest(String name) {
        super(name);
    }

    protected void setUp() {
        tagValue="someTagValue";
        methodValue=UPFileSpec.RENDER_URL_ELEMENT;
        methodNodeIdValue="renderNodeIdValue";
        targetNodeIdValue="someTargetValue";
    
        manual_uPFile=UPFileSpec.TAG_URL_ELEMENT+UPFileSpec.PORTAL_URL_SEPARATOR+tagValue+UPFileSpec.PORTAL_URL_SEPARATOR+methodValue+UPFileSpec.PORTAL_URL_SEPARATOR+methodNodeIdValue+UPFileSpec.PORTAL_URL_SEPARATOR+UPFileSpec.TARGET_URL_ELEMENT+UPFileSpec.PORTAL_URL_SEPARATOR+targetNodeIdValue+UPFileSpec.PORTAL_URL_SEPARATOR+UPFileSpec.PORTAL_URL_SUFFIX;

    }

    public void testCombinedBuild() throws PortalException {
        String uPFile=UPFileSpec.buildUPFile(tagValue,UPFileSpec.RENDER_METHOD,methodNodeIdValue,targetNodeIdValue,null);
        assertEquals(uPFile,manual_uPFile);        
    }

    public void testIncrementalBuild() throws PortalException {
        UPFileSpec upf=new UPFileSpec();
        upf.setTagId(tagValue);
        upf.setMethod(UPFileSpec.RENDER_METHOD);
        upf.setMethodNodeId(methodNodeIdValue);
        upf.setTargetNodeId(targetNodeIdValue);
        String uPFile=upf.getUPFile();
        assertEquals(uPFile,manual_uPFile);        
    }

    public void testParsing() {
        UPFileSpec fs=new UPFileSpec(manual_uPFile);
        assertEquals(tagValue,fs.getTagId());
        assertEquals(methodValue,fs.getMethod());
        assertEquals(methodNodeIdValue,fs.getMethodNodeId());
        assertEquals(targetNodeIdValue,fs.getTargetNodeId());
    }
    

}
