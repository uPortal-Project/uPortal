/**
 * Copyright (c) 2000-2009, Jasig, Inc.
 * See license distributed with this file and available online at
 * https://www.ja-sig.org/svn/jasig-parent/tags/rel-10/license-header.txt
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
