/* Copyright 2004 The JA-SIG Collaborative.  All rights reserved.
*  See license distributed with this file and
*  available online at http://www.uportal.org/license.html
*/

package org.jasig.portal.services.persondir.support;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import junit.framework.TestCase;

import org.jasig.portal.services.persondir.IPersonAttributeDao;
import org.jasig.portal.services.persondir.support.merger.MultivaluedAttributeMerger;
import org.jasig.portal.services.persondir.support.merger.NoncollidingAttributeAdder;

/**
 * MergingPersonAttributeDaoImpl testcase.
 * @author andrew.petro@yale.edu
 * @version $Revision$ $Date$
 */
public class MergingPersonAttributeDaoImplTest extends TestCase {
    private MockPersonAttributeDao sourceOne;
    private MockPersonAttributeDao sourceTwo;
    private MockPersonAttributeDao collidesWithOne;
    private Map oneAndTwo;
    private Map oneAndTwoAndThree;
    private final String queryAttr = "ThisDoesntMatterForMockDaos";
        
    protected void setUp() {
        this.sourceOne = new MockPersonAttributeDao();
        Map sourceOneMap = new HashMap();
        sourceOneMap.put("shirtColor", "blue");
        sourceOneMap.put("favoriteColor", "purple");
        this.sourceOne.setBackingMap(sourceOneMap);
        
        this.sourceTwo = new MockPersonAttributeDao();
        Map sourceTwoMap = new HashMap();
        sourceTwoMap.put("tieColor", "black");
        sourceTwoMap.put("shoeType", "closed-toe");
        this.sourceTwo.setBackingMap(sourceTwoMap);
        
        this.oneAndTwo = new HashMap();
        this.oneAndTwo.putAll(sourceOneMap);
        this.oneAndTwo.putAll(sourceTwoMap);
        
        this.collidesWithOne = new MockPersonAttributeDao();
        Map collidingMap = new HashMap();
        collidingMap.put("shirtColor", "white");
        collidingMap.put("favoriteColor", "red");
        this.collidesWithOne.setBackingMap(collidingMap);
        
        this.oneAndTwoAndThree = new HashMap();
        MultivaluedAttributeMerger merger = new MultivaluedAttributeMerger();
        this.oneAndTwoAndThree = merger.mergeAttributes(this.oneAndTwoAndThree, sourceOneMap);
        this.oneAndTwoAndThree = merger.mergeAttributes(this.oneAndTwoAndThree, sourceTwoMap);
        this.oneAndTwoAndThree = merger.mergeAttributes(this.oneAndTwoAndThree, collidingMap);
    }
    
    /**
     * Test basic usage to merge attributes from a couple of sources.
     */
    public void testBasics() {
        
        List attributeSources = new ArrayList();
        
        attributeSources.add(this.sourceOne);
        attributeSources.add(this.sourceTwo);
        
        MergingPersonAttributeDaoImpl impl = new MergingPersonAttributeDaoImpl();
        impl.setPersonAttributeDaos(attributeSources);
        
        Map queryMap = new HashMap();
        queryMap.put(queryAttr, "awp9");
        
        Map result = impl.getUserAttributes(queryMap);
        assertEquals(this.oneAndTwo, result);
    }
    
    /**
     * Test basic merging of attribute names.
     */
    public void testAttributeNames() {
        
        List attributeSources = new ArrayList();
        
        attributeSources.add(this.sourceOne);
        attributeSources.add(this.sourceTwo);
        
        MergingPersonAttributeDaoImpl impl = new MergingPersonAttributeDaoImpl();
        impl.setPersonAttributeDaos(attributeSources);
        
        Set attributeNames = impl.getPossibleUserAttributeNames();
        
        assertEquals(this.oneAndTwo.keySet(), attributeNames);
    }
    
    /**
     * Test default exception handling behavior of recovering from failures
     * of individual attribute sources on the merge list.
     */
    public void testExceptionHandling() {
        List attributeSources = new ArrayList();
        
        attributeSources.add(this.sourceOne);
        attributeSources.add(this.sourceTwo);
        attributeSources.add(new ThrowingPersonAttributeDao());
        attributeSources.add(this.collidesWithOne);
        
        MergingPersonAttributeDaoImpl impl = new MergingPersonAttributeDaoImpl();
        impl.setPersonAttributeDaos(attributeSources);
        
        Map queryMap = new HashMap();
        queryMap.put(queryAttr, "awp9");
        
        Map result = impl.getUserAttributes(queryMap);
        assertEquals(this.oneAndTwoAndThree, result);
    }
    
    /**
     * Test handling of underlying sources which return null on 
     * getPossibleUserAttributeNames().
     */
    public void testNullAttribNames() {
        List attributeSources = new ArrayList();
        
        attributeSources.add(this.sourceOne);
        attributeSources.add(this.sourceTwo);
        attributeSources.add(new NullAttribNamesPersonAttributeDao());
        attributeSources.add(this.collidesWithOne);
        
        MergingPersonAttributeDaoImpl impl = new MergingPersonAttributeDaoImpl();
        impl.setPersonAttributeDaos(attributeSources);
        
        Set attribNames = impl.getPossibleUserAttributeNames();
        
        Set expectedAttribNames = new HashSet();
        expectedAttribNames.addAll(this.sourceOne.getPossibleUserAttributeNames());
        expectedAttribNames.addAll(this.sourceTwo.getPossibleUserAttributeNames());
        expectedAttribNames.addAll(this.collidesWithOne.getPossibleUserAttributeNames());
        
        assertEquals(expectedAttribNames, attribNames);
    }
    
    /**
     * Test that, when configured to do so, MergingPersonAttributeDaoImpl
     * propogates RuntimeExceptions generated by its attribute sources.
     */
    public void testExceptionThrowing() {
        List attributeSources = new ArrayList();
        
        attributeSources.add(this.sourceOne);
        attributeSources.add(this.sourceTwo);
        attributeSources.add(new ThrowingPersonAttributeDao());
        attributeSources.add(this.collidesWithOne);
        
        MergingPersonAttributeDaoImpl impl = new MergingPersonAttributeDaoImpl();
        impl.setPersonAttributeDaos(attributeSources);
        impl.setRecoverExceptions(false);
        
        try {
            Map queryMap = new HashMap();
            queryMap.put(queryAttr, "awp9");
            
            impl.getUserAttributes(queryMap);
        } catch (RuntimeException rte) {
            // good, was propogated
            return;
        }
        fail("MergingPersonAttributeDao should have propogated RTE");
    }
    
    /**
     * Test ability to override the default merging strategy.
     *
     */
    public void testAlternativeMerging() {
        List attributeSources = new ArrayList();
        
        attributeSources.add(this.sourceOne);
        attributeSources.add(this.sourceTwo);
        attributeSources.add(this.collidesWithOne);
        
        MergingPersonAttributeDaoImpl impl = new MergingPersonAttributeDaoImpl();
        impl.setPersonAttributeDaos(attributeSources);
        impl.setMerger(new NoncollidingAttributeAdder());
        
        Map queryMap = new HashMap();
        queryMap.put(queryAttr, "awp9");
        
        Map result = impl.getUserAttributes(queryMap);
        assertEquals(this.oneAndTwo, result);
    }
    
    /**
     * A mock, test implementation of ThrowingPersonAttributeDao which always
     * throws a RuntimeException.
     */
    private class ThrowingPersonAttributeDao implements IPersonAttributeDao {

    	/**
    	 * @throws RuntimeException always
    	 */
        public Map getUserAttributes(String uid) {
            throw new RuntimeException("ThrowingPersonAttributeDao always throws");
        }
        
        /**
         * @throws RuntimeException always
         */
        public Map getUserAttributes(Map queryMap) {
            throw new RuntimeException("ThrowingPersonAttributeDao always throws");
        }

        /**
         * @throws RuntimeException always
         */
        public Set getPossibleUserAttributeNames() {
            throw new RuntimeException("ThrowingPersonAttributeDao always throws");
        }
    }
    
    /**
     * A mock, test implementation of IPersonAttributeDao which throws a 
     * RuntimeExcedption for the attribute getting methods and returns null
     * for the getPossibleUserAttributeNames() method.
     */
    private class NullAttribNamesPersonAttributeDao implements IPersonAttributeDao {

    	/**
    	 * @throws RuntimeException Always.
    	 */
        public Map getUserAttributes(String uid) {
            throw new RuntimeException("NullAttribNamesPersonAttributeDao always throws");
        }
        
        /**
         * @throws RuntimeException always
         */
        public Map getUserAttributes(Map queryMap) {
            throw new RuntimeException("NullAttribNamesPersonAttributeDao always throws");
        }
        
        /**
         * @return null
         */
        public Set getPossibleUserAttributeNames() {
            return null;
        }
    }
    
}
