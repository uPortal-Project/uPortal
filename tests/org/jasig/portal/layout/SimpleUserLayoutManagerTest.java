package org.jasig.portal.layout;

import junit.framework.*;

import org.jasig.portal.SingleDocumentUserLayoutStoreMock;

import org.jasig.portal.UserProfile;
import org.jasig.portal.PortalException;
import org.jasig.portal.IUserLayoutStore;
import org.jasig.portal.security.IPerson;
import org.jasig.portal.layout.*;

import java.io.*;
import java.util.Iterator;
import java.util.List;
import org.w3c.dom.Document;
import org.xml.sax.EntityResolver;
import org.xml.sax.InputSource;

import org.jasig.portal.utils.XML;


public class SimpleUserLayoutManagerTest extends TestCase {
    Document sampleUserLayout=null;
    IUserLayoutStore uls=null;
    IPerson p=null;
    SimpleUserLayoutManager man=null;
    protected final static String SAMPLE_LAYOUT_FILENAME="userLayout.sample";

    public SimpleUserLayoutManagerTest(String s) {
        super(s);
    }

    protected void setUp() throws Exception {
        UserLayoutDTDResolver er=new UserLayoutDTDResolver();
        // read in the layout DOM
        // note that we really do need to have a DOM structure here in order to introduce
        // persistent changes on the level of userLayout.
        //org.apache.xerces.parsers.DOMParser parser = new org.apache.xerces.parsers.DOMParser();
        org.apache.xerces.parsers.DOMParser parser = new org.apache.xerces.parsers.DOMParser ();
        parser.setEntityResolver(er);
        // set parser features
        parser.setFeature ("http://apache.org/xml/features/validation/dynamic", true);
            
        parser.parse (new org.xml.sax.InputSource(this.getClass().getResourceAsStream(SAMPLE_LAYOUT_FILENAME)));
        this.sampleUserLayout=parser.getDocument();

        p=new org.jasig.portal.security.provider.PersonImpl();

        assertTrue(sampleUserLayout!=null);
        uls=new SingleDocumentUserLayoutStoreMock(sampleUserLayout);
        man=new SimpleUserLayoutManager(p,new UserProfile(),uls);
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
                    inStream = this.getClass().getResourceAsStream(dtdName);
                }
                inSrc = new InputSource(inStream);
            }

            return inSrc;
        }
    }

    public void testGetNode() throws Exception {

        // get a folder
        {
            String id="s4";
            UserLayoutNodeDescription node=man.getNode(id);
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
            UserLayoutNodeDescription node=man.getNode(id);
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
        UserLayoutNodeDescription gnode=man.getNode(nchan.getId());
        assertEquals("comparing the original and getNode() result: ",nchan,gnode);
        assertEquals("parentId is the specified attachment point",man.getParentId(nchan.getId()),parentId);
        assertEquals("siblingId is the specified next sibling",man.getNextSiblingId(nchan.getId()),siblingId);

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
        UserLayoutNodeDescription gnode=man.getNode(nfold.getId());
        assertEquals("comparing the original and getNode() result: ",nfold,gnode);
        assertEquals("parentId is the specified attachment point",man.getParentId(nfold.getId()),parentId);
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
        man.deleteNode(nodeId);

        boolean exception=false;
        try {
            UserLayoutNodeDescription node=man.getNode(nodeId);
        } catch (PortalException e) {
            exception=true;
        }
        
        assertTrue("Thrown an exception when looking for a deleted node or returned a node with no parent.",exception || man.getParentId(nodeId)==null);

    }
    
    public void testMoveChannel() throws Exception {
        // try moving a channel
        String nodeId="n8";
        String targetId="s10";
        assertTrue("Can channel \""+nodeId+"\" be moved to folder \""+targetId+"\"",man.canMoveNode(nodeId,targetId,null));
        man.moveNode(nodeId,targetId,null);
        assertEquals("New channel attachment point",man.getParentId(nodeId),targetId);
        assertEquals("Next siblingId",man.getNextSiblingId(nodeId),null);
    }

    public void testMoveUnderRootNode() throws Exception {
        // try moving a channel
        String nodeId="n3";
        String targetId="root";
        UserLayoutNodeDescription rootNode=man.getNode(targetId);
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

    }

    public void testUpdateFolder() throws Exception {
        String folderId="s1";
        UserLayoutFolderDescription fold=(UserLayoutFolderDescription) man.getNode(folderId);
        fold.setName("New name");
        fold.setHidden(true);
        fold.setFolderType(UserLayoutFolderDescription.REGULAR_TYPE);
        // get child list
        List ochildren=man.getChildIds(folderId);

        man.updateNode(fold);
        //        System.out.println("Layout\n"+XML.serializeNode(man.getUserLayoutDOM()));

        UserLayoutFolderDescription rfold=(UserLayoutFolderDescription) man.getNode(folderId);
        assertEquals("Comparing node used to update with the update result: ",fold,rfold);

        List nchildren=man.getChildIds(folderId);
        assertEquals("Comparing child Ids of an updated folder: ",ochildren,nchildren);

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
    
    protected void assertEquals(UserLayoutNodeDescription one,UserLayoutNodeDescription two) {
        assertEquals("",one,two);
    }

    protected void assertEquals(String message,UserLayoutNodeDescription one,UserLayoutNodeDescription two) {
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
        for(Iterator i=one.getParameterNames().iterator();i.hasNext();) {
            String pName=(String)i.next();
            assertEquals("channel parameter \""+pName+"\"",one.getParameterValue(pName),two.getParameterValue(pName));
        }
        // other way around
        for(Iterator i=two.getParameterNames().iterator();i.hasNext();) {
            assertTrue("contains parameter",one.containsParameter((String)i.next()));
        }

    }

    private void assertEquals(String message,UserLayoutFolderDescription one,UserLayoutFolderDescription two) {
        assertEquals(message+"channel folder type",one.getFolderType(),two.getFolderType());
    }

    public void testsGetFolderDescription() throws Exception {
        
    }

}
