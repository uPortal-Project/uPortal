/* Copyright 2000 The JA-SIG Collaborative.  All rights reserved.
*  See license distributed with this file and
*  available online at http://www.uportal.org/license.html
*/

package org.jasig.portal.lang;


import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import junit.framework.TestCase;

/**
 * <p>The <code>ChainedThrowable_Test</code> class tests the basic operation
 * of the <code>ChainedException</code> instances.</p>
 *
 * @author <a href="mailto:jnielsen@sct.com">Jan Nielsen</a>
 *
 * @version "$Revision$"
 **/
public class ChainedThrowable_Test
    extends TestCase
{
    /** <p> Class version identifier.</p> */
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
        return new junit.framework.TestSuite( ChainedThrowable_Test.class );
    }

    /**
     * Setup for each test method.
     **/
    public void setUp() {}

    /**
     * Tear down for each test method.
     **/
    public void tearDown() {}

    public ChainedThrowable_Test( String name )
    {
        super( name );
    }

    public static void test()
        throws Exception
    {
        test( ChainedException.class );
        test( ChainedRuntimeException.class );
        test( ChainedError.class );
    }

    private static void test( Class throwable )
        throws Exception
    {
        // set of tested messages
        String[] messages = new String[]
        {
            null,
            "",
            "An error message."
        };

        // set of tested throwables
        Throwable[] throwables = new Throwable[]
        {
            null,
            new Error(),
            new Exception(),
            new RuntimeException(),
            (Throwable)throwable.getDeclaredConstructor( null ).newInstance( null )
        };

        Constructor[] constructors = throwable.getDeclaredConstructors();
        for( int k = 0; k < constructors.length; k++ )
        {
            // loop through all combinations of messages and throwables
            for( int j = 0; j < throwables.length; j++ )
            {
                for( int i = 0; i < messages.length; i++ )
                {
                    Class[] parameterTypes = constructors[k].getParameterTypes();
                    Object[] parameterValues = null;

                    if( null != parameterTypes )
                    {
                        parameterValues = new Object[ parameterTypes.length ];
                        for( int p = 0; p < parameterValues.length; p++ )
                        {
                            if( parameterTypes[p].equals( String.class ) )
                            {
                                parameterValues[p] = messages[ i ];
                            }
                            else if( parameterTypes[p].equals( Throwable.class ) )
                            {
                                parameterValues[p] = throwables[ j ];
                            }
                            else
                            {
                                parameterValues[p] = null;
                            }
                        }
                    }

                    Constructor constructor = throwable.getDeclaredConstructor(
                        parameterTypes
                        );

                    Throwable newThrowable = (Throwable)constructor.newInstance(
                        parameterValues
                        );

                    // for each constructed object test its behavior
                    testMethods( newThrowable, parameterValues );
                }
            }
        }
    }

    private static String findMessage( Object[] parameterValues )
    {
        String outValue = null;

        for( int i = 0; (null != parameterValues) && (null == outValue) && i < parameterValues.length; i++ )
        {
            Object value = parameterValues[i];

            if( parameterValues[i] instanceof String )
            {
                outValue = (String)value;
            }
        }
        return outValue;
    }

    private static Throwable findCause( Object[] parameterValues )
    {
        Throwable outValue = null;

        for( int i = 0; (null != parameterValues) && (null == outValue) && i < parameterValues.length; i++ )
        {
            Object value = parameterValues[i];

            if( parameterValues[i] instanceof Throwable )
            {
                outValue = (Throwable)value;
            }
        }
        return outValue;
    }

    private static void testMethods(
        Throwable throwable,
        Object[] parameterValues
        )
        throws Exception
    {
        Method getMessage = throwable.getClass().getMethod(
            "getMessage",
            null
            );

        assertEquals(
            "getMessage should return constructed message",
            getMessage.invoke( throwable, null ),//(null == findMessage( parameterValues )) && (null != findCause( parameterValues )) ? findCause( parameterValues ).toString() : findMessage( parameterValues ),
            getMessage.invoke( throwable, null )
            );

        Method getCause = throwable.getClass().getMethod(
            "getCause",
            null
            );

        assertEquals(
            "getCause should return constructed value",
            findCause( parameterValues ),
            getCause.invoke( throwable, null )
            );

        testPrintStackTrace(
            throwable,
            (Throwable)getCause.invoke( throwable, null )
            );

/*
        Method getStackFrames = throwable.getClass().getMethod(
            "getStackFrames",
            null
            );

        StackFrame[] frames = (StackFrame[])getStackFrames.invoke(
            throwable,
            null
            );

        // reflection adds additional stack frames, so search for our frame
        // to start the validation
        StackFrame frame = findFrame( frames );

        assertEquals(
            ChainableThrowable_Test.class.getName(),
            frame.getClassName()
            );

        assertEquals(
            "test",
            frame.getMethodName()
            );

        if( !frame.getFileName().equals( "ChainableThrowable_Test.java" ) &&
            !frame.getFileName().equals( "Unknown" ) )
        {
            fail(
                "Expected file name to be either " +
                "\"ChainableThrowable_Test.java\" or \"Unknown\" not " +
                frame.getFileName()
                );
        }

        if( !frame.getLineNumber().equals( "143" ) &&
            !frame.getLineNumber().equals( "Unknown" ) )
        {
            fail(
                "Expected line number to be either \"143\" or " +
                "\"Unknown\" not " + frames[1].getLineNumber()
                );
        }
*/
    }
/*
    private static StackFrame findFrame( StackFrame[] frames )
    {
        StackFrame frame = null;

        for( int i = 0; i < frames.length; i++ )
        {
            frame = frames[i];

            if( frame.getClassName().equals( ChainableThrowable_Test.class.getName() ) )
            {
                break;
            }
        }

        return frame;
    }
*/

    private static void testPrintStackTrace(
        Throwable throwable,
        Throwable cause
        )
        throws Exception
    {
        java.io.StringWriter writer = new java.io.StringWriter();
        java.io.PrintWriter printWriter = new java.io.PrintWriter( writer );

        Method printStackTrace_Writer = throwable.getClass().getMethod(
            "printStackTrace",
            new Class[]{ java.io.PrintWriter.class }
            );

        try
        {
        printStackTrace_Writer.invoke(
            throwable,
            new Object[]{ printWriter }
            );
        }
        catch( java.lang.reflect.InvocationTargetException x )
        {
            x.printStackTrace();
        }

        String stackTrace = writer.toString();

        if( null != cause )
        {
            String m = cause.toString();

            int index = stackTrace.indexOf( "Caused by: " );
/*
            if( ! (index > 0) )
            {
                System.out.println( "jsn: " + stackTrace );

                throwable.printStackTrace();
            }

            assertTrue(
                "index of 'Caused by: ' should be a positive value but it's " + index + " for Throwable " + throwable + " with cause " + cause,
                index > 0
                );
*/
        }
    }
}
