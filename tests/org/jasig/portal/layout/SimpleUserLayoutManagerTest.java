/* Copyright 2002 The JA-SIG Collaborative.  All rights reserved.
*  See license distributed with this file and
*  available online at http://www.uportal.org/license.html
*/

package org.jasig.portal.layout;

import junit.framework.*;

import org.jasig.portal.SingleDocumentUserLayoutStoreMock;

import org.jasig.portal.UserProfile;
import org.jasig.portal.PortalException;
import org.jasig.portal.IUserLayoutStore;
import org.jasig.portal.security.IPerson;
import org.jasig.portal.security.PersonFactory;

import java.io.*;
import java.util.Enumeration;
import org.w3c.dom.Document;
import org.xml.sax.EntityResolver;
import org.xml.sax.InputSource;
 


public class SimpleUserLayoutManagerTest extends TestCase implements LayoutEventListener {
    Document sampleUserLayout=null;
    IUserLayoutStore uls=null;
    IPerson p=null;
    SimpleUserLayoutManager man=null;
    protected final static String SAMPLE_LAYOUT_FILENAME="userLayout.sample";

    boolean nodeAdded, nodeDeleted, nodeMoved, nodeUpdated, layoutSaved, layoutLoaded;
    LayoutEvent lastEvent;

    public SimpleUserLayoutManagerTest(String s) {
        super(s);
    }

    protected void setUp() throws Exception {
        UserLayoutDTDResolver er=new UserLayoutDTDResolver();
        // read in the layout DOM
        // note that we really do need to have a DOM structure here in order to introduce
        // persistent changes on the level of userLayout.

        //org.apache.xerces.parsers.DOMParser parser = new org.apache.xerces.parsers.DOMParser();
        javax.xml.parsers.DocumentBuilderFactory pf=javax.xml.parsers.DocumentBuilderFactory.newInstance();
        pf.setValidating(true);
        javax.xml.parsers.DocumentBuilder parser=pf.newDocumentBuilder();
        //org.apache.xerces.jaxp.DOMParser parser = new org.apache.xerces.parsers.DOMParser ();
        //parser.setFeature ("http://apache.org/xml/features/validation/dynamic", true);
        parser.setEntityResolver(er);
        parser.setErrorHandler(new org.xml.sax.helpers.DefaultHandler());
        this.sampleUserLayout=parser.parse (new org.xml.sax.InputSource(this.getClass().getResourceAsStream(SAMPLE_LAYOUT_FILENAME)));

        p = PersonFactory.createPerson();

        assertTrue(sampleUserLayout!=null);
        uls=new SingleDocumentUserLayoutStoreMock(sampleUserLayout);
        man=new SimpleUserLayoutManager(p,new UserProfile(),uls);

        // clear event-related markers
        nodeAdded=nodeDeleted=nodeMoved=nodeUpdated=layoutSaved=layoutLoaded=false;
        lastEvent=null;
        man.addLayoutEventListener(this);
        man.loadUserLayout();

    }

    protected class UserLayoutDTDResolver implements EntityResolver {
        final static String dtdName="userLayout.dtd";

        /**
         * Sets up a new input source based on the dtd specified in the xml document
         * @param publicId the public ID
         * @param systemId the system ID
         * @return an input source based on the dtd specified in the xml document
         */
        public InputSource resolveEntity (String publicId, String systemId) {
            InputStream inStream = null;
            InputSource inSrc = null;
            
            if (systemId != null) {
                if (dtdName != null && systemId.indexOf(dtdName) != -1) {
                    try {
                        Class testClass=Class.forName("org.jasig.portal.layout.SimpleUserLayoutManagerTest");
                        inStream = testClass.getResourceAsStream(dtdName);
                    } catch (Exception e) {
                        e.printStackTrace();
                        return null;
                    }
                }
                inSrc = new InputSource(inStream);
            }

            return inSrc;
        }
    }

    public void testGetNode() throws Exception {

        // test load event
        {
            assertTrue("layoutLoad event receive",layoutLoaded);
        }

        // get a folder
        {
            String id="s4";
            IUserLayoutNodeDescription node=man.getNode(id);
            assertTrue(node!=null);
            if(node!=null) {
                // make up a fake node, with the same id
                UserLayoutFolderDescription ts=new UserLayoutFolderDescription();
                ts.setId(node.getId());
                ts.setName("Main");
                ts.setHidden(false);
                ts.setImmutable(true);
                ts.setUnremovable(true);
                ts.setFolderType(UserLayoutFolderDescription.REGULAR_TYPE);
                assertEquals(ts,node);
            }
        }

        // get a channel
        {
            String id="n6";
            IUserLayoutNodeDescription node=man.getNode(id);
            assertTrue(node!=null);
            if(node!=null) {
                // make up a fake node, with the same id
                UserLayoutChannelDescription ts=new UserLayoutChannelDescription();
                ts.setId(node.getId());
                ts.setName("uPortal-Powered Sites");
                ts.setTitle("uPortal-Powered Sites");
                ts.setHidden(false);
                ts.setImmutable(true);
                ts.setUnremovable(true);
                ts.setClassName("org.jasig.portal.channels.CGenericXSLT");
                ts.setFunctionalName("");
                ts.setChannelPublishId("5");
                ts.setChannelTypeId("0");
                ts.setDescription("uPortal Demos channel");
                ts.setEditable(false);
                ts.setHasAbout(false);
                ts.setHasHelp(false);
                ts.setTimeout(10000);

                ts.setParameterValue("xmlUri","http://www.interactivebusiness.com/publish/jasigPortalDemos.rss");
                ts.setParameterValue("sslUri","CGenericXSLT/RSS/RSS-0_9x.ssl");

                assertEquals(ts,node);
            }
        }
    }

    public void testAddChannel() throws Exception {
        // testing by doing add and subsequent get

        // add a channel
        UserLayoutChannelDescription nchan=new UserLayoutChannelDescription();
        nchan.setName("Newly added channel");
        nchan.setTitle("Newly added channel");
        nchan.setHidden(false);
        nchan.setImmutable(true);
        nchan.setUnremovable(true);
        nchan.setClassName("org.jasig.portal.channels.CGenericXSLT");
        nchan.setFunctionalName("");
        nchan.setChannelPublishId("5");
        nchan.setChannelTypeId("0");
        nchan.setDescription("some example channel");
        nchan.setEditable(false);
        nchan.setHasAbout(false);
        nchan.setHasHelp(false);
        nchan.setTimeout(10000);

        nchan.setParameterValue("xmlUri","http://www.interactivebusiness.com/publish/jasigPortalDemos.rss");
        nchan.setParameterValue("sslUri","CGenericXSLT/RSS/RSS-0_9x.ssl");
        
        String parentId="s7";
        String siblingId="n8";
        assertTrue("can a node be added ?",man.canAddNode(nchan,parentId,siblingId));

        UserLayoutChannelDescription rchan=(UserLayoutChannelDescription) man.addNode(nchan,parentId,siblingId);
        // System.out.println("Layout\n"+XML.serializeNode(man.getUserLayoutDOM()));
        assertTrue("addNode() returned null",rchan!=null);
        nchan.setId(rchan.getId());
        assertEquals("comparing the original and addNode() result: ",nchan,rchan);

        // do a get
        IUserLayoutNodeDescription gnode=man.getNode(nchan.getId());
        assertEquals("comparing the original and getNode() result: ",nchan,gnode);
        assertEquals("parentId is the specified attachment point",parentId,man.getParentId(nchan.getId()));
        assertEquals("siblingId is the specified next sibling",siblingId,man.getNextSiblingId(nchan.getId()));

        assertTrue("nodeAdded event received",nodeAdded);
        assertTrue("LayoutEvent is not null",lastEvent!=null);
        assertEquals("LayoutEvent nodeId",nchan.getId(),lastEvent.getNodeDescription().getId());

    }

    public void testAddFolder() throws Exception {
        // testing by doing add and subsequent get

        // add a channel
        UserLayoutFolderDescription nfold=new UserLayoutFolderDescription();
        nfold.setName("Newly added folder");
        nfold.setHidden(false);
        nfold.setImmutable(true);
        nfold.setUnremovable(true);
        nfold.setFolderType(UserLayoutFolderDescription.REGULAR_TYPE);

        String parentId="s7";
        assertTrue("can a node be added ?",man.canAddNode(nfold,parentId,null));

        UserLayoutFolderDescription rfold=(UserLayoutFolderDescription) man.addNode(nfold,parentId,null);
        //        System.out.println("Layout\n"+XML.serializeNode(man.getUserLayoutDOM()));
        assertTrue("addNode() returned null",rfold!=null);
        nfold.setId(rfold.getId());
        assertEquals("comparing the original and addNode() result: ",nfold,rfold);

        // do a get
        IUserLayoutNodeDescription gnode=man.getNode(nfold.getId());
        assertEquals("comparing the original and getNode() result: ",nfold,gnode);
        assertEquals("parentId is the specified attachment point",parentId,man.getParentId(nfold.getId()));

        assertTrue("nodeAdded event received",nodeAdded);
        assertTrue("LayoutEvent is not null",lastEvent!=null);
        assertEquals("LayoutEvent nodeId",nfold.getId(),lastEvent.getNodeDescription().getId());
    }

    public void testCanMove() throws Exception {
        // try moving a channel
        String nodeId="n8";
        String targetId="s10";
        assertTrue("Can channel \""+nodeId+"\" be moved to folder \""+targetId+"\"",man.canMoveNode(nodeId,targetId,null));

        // try moving into an immutable folder
        targetId="s4";
        assertTrue("Moving channel \""+nodeId+"\" be moved to an immutable folder \""+targetId+"\"",!man.canMoveNode(nodeId,targetId,null));

        // try moving from an immutable folder
        nodeId="n6"; targetId="s10";
        assertTrue("Can channel \""+nodeId+"\" (from the immutable folder) be moved to folder \""+targetId+"\"",!man.canMoveNode(nodeId,targetId,null));



    }

    public void testCanDelete() throws Exception {
        String nodeId="s1";
        assertTrue("Can delete removable node=\""+nodeId+"\"",man.canDeleteNode(nodeId));
        nodeId="n3";
        assertTrue("Can delete unremovable node=\""+nodeId+"\"",!man.canDeleteNode(nodeId));

    }

    public void testDelete() throws Exception {
        String nodeId="s1";
        String parentId="root";
        man.deleteNode(nodeId);

        boolean exception=false;
        try {
            IUserLayoutNodeDescription node=man.getNode(nodeId);
        } catch (PortalException e) {
            exception=true;
        }
        
        assertTrue("Thrown an exception when looking for a deleted node or returned a node with no parent.",exception || man.getParentId(nodeId)==null);

        assertTrue("nodeDeleted event received",nodeDeleted);
        assertTrue("LayoutEvent is not null",lastEvent!=null);
        assertTrue("LayoutMoveEvent was received",lastEvent instanceof LayoutMoveEvent);
        LayoutMoveEvent lme=(LayoutMoveEvent) lastEvent;
        assertEquals("LayoutMoveEvent nodeId",nodeId,lme.getNodeDescription().getId());
        assertEquals("LayoutMoveEvent oldParentNodeId",parentId,lme.getOldParentNodeId());

    }
    
    public void testMoveChannel() throws Exception {
        // try moving a channel
        String nodeId="n8";
        String parentId="s7";
        String targetId="s10";
        assertTrue("Can channel \""+nodeId+"\" be moved to folder \""+targetId+"\"",man.canMoveNode(nodeId,targetId,null));
        man.moveNode(nodeId,targetId,null);
        assertEquals("New channel attachment point",man.getParentId(nodeId),targetId);
        assertEquals("Next siblingId",man.getNextSiblingId(nodeId),null);

        assertTrue("nodeMoved event received",nodeMoved);
        assertTrue("LayoutEvent is not null",lastEvent!=null);
        assertTrue("LayoutMoveEvent was received",lastEvent instanceof LayoutMoveEvent);
        LayoutMoveEvent lme=(LayoutMoveEvent) lastEvent;
        assertEquals("LayoutMoveEvent nodeId",nodeId,lme.getNodeDescription().getId());
        assertEquals("LayoutMoveEvent oldParentNodeId",parentId,lme.getOldParentNodeId());

    }

    public void testMoveUnderRootNode() throws Exception {
        // try moving a channel
        String nodeId="n3";
        String targetId="root";
        IUserLayoutNodeDescription rootNode=man.getNode(targetId);
        assertTrue("Can channel \""+nodeId+"\" be moved to folder \""+targetId+"\"",man.canMoveNode(nodeId,targetId,null));
        man.moveNode(nodeId,targetId,null);
        assertEquals("New channel attachment point",man.getParentId(nodeId),targetId);
        assertEquals("Next siblingId",man.getNextSiblingId(nodeId),null);
        //  System.out.println("Layout\n"+XML.serializeNode(man.getUserLayoutDOM()));
    }

    public void testUpdateChannel() throws Exception {
        String channelId="n8";
        UserLayoutChannelDescription chan=(UserLayoutChannelDescription) man.getNode(channelId);
        chan.setName("uPortal other page");
        chan.setTimeout(10);
        chan.setTitle("uPortal other title");
        chan.setHidden(true);
        chan.setParameterValue("newParameter","newValue");
        man.updateNode(chan);
        //        System.out.println("Layout\n"+XML.serializeNode(man.getUserLayoutDOM()));

        UserLayoutChannelDescription rchan=(UserLayoutChannelDescription) man.getNode(channelId);
        assertEquals("Comparing node used to update with the update result: ",chan,rchan);

        assertTrue("nodeUpdated event received",nodeUpdated);
        assertTrue("LayoutEvent is not null",lastEvent!=null);
        assertEquals("LayoutEvent nodeId",channelId,lastEvent.getNodeDescription().getId());


    }

    public void testUpdateFolder() throws Exception {
        String folderId="s1";
        UserLayoutFolderDescription fold=(UserLayoutFolderDescription) man.getNode(folderId);
        fold.setName("New name");
        fold.setHidden(true);
        fold.setFolderType(UserLayoutFolderDescription.REGULAR_TYPE);
        // get child list
        Enumeration ochildren=man.getChildIds(folderId);

        man.updateNode(fold);
        //        System.out.println("Layout\n"+XML.serializeNode(man.getUserLayoutDOM()));

        UserLayoutFolderDescription rfold=(UserLayoutFolderDescription) man.getNode(folderId);
        assertEquals("Comparing node used to update with the update result: ",fold,rfold);

        Enumeration nchildren=man.getChildIds(folderId);
        assertEquals("Comparing child Ids of an updated folder: ",ochildren,nchildren);

        assertTrue("nodeUpdated event received",nodeUpdated);
        assertTrue("LayoutEvent is not null",lastEvent!=null);
        assertEquals("LayoutEvent nodeId",folderId,lastEvent.getNodeDescription().getId());

    }

    public void testMoveFolder() throws Exception {
        // try moving a channel
        String nodeId="s10";
        String targetId="s7";
        String nextSiblingId="n8";
        man.moveNode(nodeId,targetId,nextSiblingId);
        assertEquals("New channel attachment point",man.getParentId(nodeId),targetId);
        assertEquals("Next siblingId",man.getNextSiblingId(nodeId),nextSiblingId);
        // System.out.println("Layout\n"+XML.serializeNode(man.getUserLayoutDOM()));
    }
    
    protected void assertEquals(IUserLayoutNodeDescription one,IUserLayoutNodeDescription two) {
        assertEquals("",one,two);
    }

    protected void assertEquals(String message,IUserLayoutNodeDescription one,IUserLayoutNodeDescription two) {
        if(one instanceof UserLayoutFolderDescription) {
            assertTrue(message+"nodes are of a different type!",two instanceof UserLayoutFolderDescription);
        } else {
            assertTrue(message+"nodes are of a different type!",two instanceof UserLayoutChannelDescription);
        }
        assertEquals(message+"node attribute \"name\"",one.getName(),two.getName());
        assertEquals(message+"node attribute \"unremovable\"",one.isUnremovable(),two.isUnremovable());
        assertEquals(message+"node attribute \"mmutable\"",one.isImmutable(),two.isImmutable());
        assertEquals(message+"node attribute \"hidden\"",one.isHidden(),two.isHidden());
    }
    
    private void assertEquals(String message, UserLayoutChannelDescription one,UserLayoutChannelDescription two) {
        assertEquals(message+"channel attribute \"hasAbout\"",one.hasAbout(),two.hasAbout());
        assertEquals(message+"channel attribute \"hasHelp\"",one.hasHelp(),two.hasHelp());
        assertEquals(message+"channel attribute \"isEditable\"",one.isEditable(),two.isEditable());
        assertEquals(message+"channel attribute \"timeout\"",one.getTimeout(),two.getTimeout());
        assertEquals(message+"channel functional name",one.getFunctionalName(),two.getFunctionalName());
        assertEquals(message+"channel subscribe id",one.getChannelSubscribeId(),two.getChannelSubscribeId());
        assertEquals(message+"channel type id",one.getChannelTypeId(),two.getChannelTypeId());
        assertEquals(message+"channel public id",one.getChannelPublishId(),two.getChannelPublishId());
        assertEquals(message+"channel class name",one.getClassName(),two.getClassName());
        assertEquals(message+"channel title",one.getTitle(),two.getTitle());
        assertEquals(message+"channel description",one.getDescription(),two.getDescription());

        // compare parameter content
        for(Enumeration e=one.getParameterNames();e.hasMoreElements();) {
            String pName=(String)e.nextElement();
            assertEquals("channel parameter \""+pName+"\"",one.getParameterValue(pName),two.getParameterValue(pName));
        }
        // other way around
        for(Enumeration e=two.getParameterNames();e.hasMoreElements();) {
            assertTrue("contains parameter",one.containsParameter((String)e.nextElement()));
        }

    }

    private void assertEquals(String message,UserLayoutFolderDescription one,UserLayoutFolderDescription two) {
        assertEquals(message+"channel folder type",one.getFolderType(),two.getFolderType());
    }

    public void testsGetFolderDescription() throws Exception {
        
    }

    public void channelAdded(LayoutEvent ev) {
        lastEvent=ev;
        nodeAdded=true;
    }

    public void channelUpdated(LayoutEvent ev) {
        lastEvent=ev;
        nodeUpdated=true;
    }

    public void channelMoved(LayoutMoveEvent ev) {
        lastEvent=ev;
        nodeMoved=true;
    }

    public void channelDeleted(LayoutMoveEvent ev) {
        lastEvent=ev;
        nodeDeleted=true;
    }

    public void folderAdded(LayoutEvent ev){
        lastEvent=ev;
        nodeAdded=true;
    }
    public void folderUpdated(LayoutEvent ev) {
        lastEvent=ev;
        nodeUpdated=true;
    }
    public void folderMoved(LayoutMoveEvent ev){
        lastEvent=ev;
        nodeMoved=true;
    }
    public void folderDeleted(LayoutMoveEvent ev){
        lastEvent=ev;
        nodeDeleted=true;
    }

    public void layoutLoaded() {
        layoutLoaded=true;
    }
    public void layoutSaved() {
        layoutSaved=true;
    }

}
