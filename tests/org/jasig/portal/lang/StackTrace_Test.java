/* Copyright © 2001 The JA-SIG Collaborative.  All rights reserved.
*
* Redistribution and use in source and binary forms, with or without
* modification, are permitted provided that the following conditions
* are met:
*
* 1. Redistributions of source code must retain the above copyright
*    notice, this list of conditions and the following disclaimer.
*
* 2. Redistributions in binary form must reproduce the above copyright
*    notice, this list of conditions and the following disclaimer in
*    the documentation and/or other materials provided with the
*    distribution.
*
* 3. Redistributions of any form whatsoever must retain the following
*    acknowledgment:
*    "This product includes software developed by the JA-SIG Collaborative
*    (http://www.jasig.org/)."
*
* THIS SOFTWARE IS PROVIDED BY THE JA-SIG COLLABORATIVE "AS IS" AND ANY
* EXPRESSED OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
* IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR
* PURPOSE ARE DISCLAIMED.  IN NO EVENT SHALL THE JA-SIG COLLABORATIVE OR
* ITS CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
* SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT
* NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
* LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION)
* HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT,
* STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
* ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED
* OF THE POSSIBILITY OF SUCH DAMAGE.
*
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
        assertTrue( "Invalid line number: " + lineNumber, lineNumber == -1 || lineNumber == 148 );
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
