/* Copyright 2001 The JA-SIG Collaborative.  All rights reserved.
*  See license distributed with this file and
*  available online at http://www.uportal.org/license.html
*/

package org.jasig.portal.lang;

import junit.framework.TestCase;

/**
 * <p>The <code>StackTraceElement_Test</code> </p>
 *
 * @author <a href="mailto:jnielsen@sct.com">Jan Nielsen</a>
 *
 * @version "$Revision$"
 **/
public class StackTrace_Test
    extends TestCase
{

    /** Class version identifier. */
    public static final String RCS_ID = "@(#) $Header$";

    /**
     * Run all the test cases defined in the class.
     *
     * @param args not used
     **/
    public static void main( String[] args )
    {
        junit.textui.TestRunner.run( suite() );
    }

    /**
     * Build a test suite using reflection.
     *
     * @return test suite for the class
     **/
    public static junit.framework.TestSuite suite()
    {
        return new junit.framework.TestSuite( 
            StackTrace_Test.class
            );
    }

    /**
     * Setup for each test method.
     **/
    public void setUp() {}

    /**
     * Tear down for each test method.
     **/
    public void tearDown() {}

    public StackTrace_Test( String name )
    {
        super( name );
    }

    public void test_getStackTraceElement()
    {
        StackTrace[] frames = StackTrace.getStackTrace();
        assertNotNull( frames );
    }

    public void test_getStackTraceElement_0()
    {
        StackTrace frame = StackTrace.getStackTrace( 0 );
        assertNotNull( frame );
    }

    public void test_getStackTraceElement_1()
    {
        StackTrace frame = StackTrace.getStackTrace( 1 );
        assertNotNull( frame );
    }

    public void test_getStackTraceElement_minus_1()
    {
        StackTrace frame = StackTrace.getStackTrace( -1 );
        assertNull( frame );
    }

    public void test_getStackTraceElement_Integer_MAX_VALUE()
    {
        StackTrace frame = StackTrace.getStackTrace( Integer.MAX_VALUE );
        assertNull( frame );
    }

    public void test_getStackTraceElement_Integer_MIN_VALUE()
    {
        StackTrace frame = StackTrace.getStackTrace( Integer.MIN_VALUE );
        assertNull( frame );
    }

    public void test_toString()
    {
        StackTrace[] frames = StackTrace.getStackTrace();
        String trace = StackTrace.toString( frames );
        assertNotNull( trace );
    }

    public void test_getFileName()
    {
        StackTrace frame = StackTrace.getStackTrace( 0 );
        assertNotNull( "Stack trace element should not be null.", frame );
        String fileName = frame.getFileName();
        assertNotNull( "File name should not be null.", fileName );
        assertTrue(
            "Invalid file name:" + fileName,
            "StackTrace_Test.java".equals( fileName ) ||
            "Unknown Source".equals( fileName )
            );
    }

    public void test_getClassName()
    {
        StackTrace frame = StackTrace.getStackTrace( 0 );
        assertNotNull( frame );
        assertEquals( getClass().getName(), frame.getClassName() );
    }

    public void test_getMethodName()
    {
        StackTrace frame = StackTrace.getStackTrace( 0 );
        assertNotNull( frame );
        assertEquals( "test_getMethodName", frame.getMethodName() );
    }

    public void test_getLineNumber()
    {
        StackTrace frame = StackTrace.getStackTrace( 0 );
        assertNotNull( frame );
        int lineNumber = frame.getLineNumber();
        assertTrue( "Invalid line number: " + lineNumber, lineNumber == -1 || lineNumber == 133 );
    }
    
    public void test_StackTrace()
    {
        StackTrace[] trace = null;
        
        try
        {
            // trigger an exception which contains "Unknown Source",
            // "Native Method", and source code lines, and validate
            // each frame.
            Class.forName( "someUnknownClass" ).newInstance();
        }
        catch( Exception x )
        {
            trace = StackTrace.getStackTrace( x );
        }
        
        for( int i = 0; i < trace.length; i++ )
        {
            if( "java.net.URLClassLoader$1".equals( trace[i].getClassName() ) )
            {
                //TestCase.assertEquals( "Unknown Source", trace[i].getFileName() );
                //TestCase.assertEquals( -1, trace[i].getLineNumber() );
                TestCase.assertEquals( "run", trace[i].getMethodName() );
                TestCase.assertEquals( false, trace[i].isNativeMethod() );
            }
            else if( "java.security.AccessController".equals( trace[i].getClassName() ) )
            {
                //TestCase.assertEquals( "Unknown Source", trace[i].getFileName() );
                //TestCase.assertEquals( -2, trace[i].getLineNumber() );
                TestCase.assertEquals( "doPrivileged", trace[i].getMethodName() );
                TestCase.assertEquals( true, trace[i].isNativeMethod() );
            }
        }
    }
}
