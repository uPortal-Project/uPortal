/* Copyright 2009 The JA-SIG Collaborative.  All rights reserved.
 *  See license distributed with this file and
 *  available online at http://www.uportal.org/license.html
 */

package org.jasig.portal.layout.dlm.providers;

import junit.framework.TestCase;

import org.dom4j.Element;
import org.jasig.portal.layout.dlm.Evaluator;
import org.jasig.portal.layout.dlm.EvaluatorFactory;
import org.jasig.portal.security.IPerson;
import org.jasig.portal.security.provider.PersonImpl;

public class ParenTest extends TestCase {

    public void testIsApplicable() {
        
        IPerson p = new PersonImpl();
        
        // Paren.Type.OR...
        Paren orParen = new Paren(Paren.Type.OR);
        orParen.addEvaluator(new AllUsersEvaluatorFactory());
        assertTrue("true should make true", orParen.isApplicable(p));
        orParen.addEvaluator(new NoUsersEvaluatorFactory());
        assertTrue("true + false should make true", orParen.isApplicable(p));
        orParen = new Paren(Paren.Type.OR);
        orParen.addEvaluator(new NoUsersEvaluatorFactory());
        assertFalse("false should make false", orParen.isApplicable(p));
        
        // Paren.Type.AND...
        Paren andParen = new Paren(Paren.Type.AND);
        andParen.addEvaluator(new AllUsersEvaluatorFactory());
        assertTrue("true should make true", andParen.isApplicable(p));
        andParen.addEvaluator(new NoUsersEvaluatorFactory());
        assertFalse("true + false should make false", andParen.isApplicable(p));

        // Paren.Type.NOT...
        Paren notParen = new Paren(Paren.Type.NOT);
        notParen.addEvaluator(new AllUsersEvaluatorFactory());
        assertFalse("true should make false", notParen.isApplicable(p));
        notParen = new Paren(Paren.Type.NOT);
        notParen.addEvaluator(new NoUsersEvaluatorFactory());
        assertTrue("false should make true", notParen.isApplicable(p));

    }
    
    private static class NoUsersEvaluatorFactory extends Evaluator {

        @Override
        public Class<? extends EvaluatorFactory> getFactoryClass() {
            throw new UnsupportedOperationException();
        }

        @Override
        public boolean isApplicable(IPerson person) {
            return false;
        }

        @Override
        public void toElement(Element parent) {
            throw new UnsupportedOperationException();
        }
        
    }

}
