/**
 * Copyright © 2004 The JA-SIG Collaborative.  All rights reserved.
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

import java.io.PrintStream;
import java.io.PrintWriter;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/**
 * The <code>ChainedException</code> class has facilities for exception
 * chaining. Exceptions which extend this class inherit these facilities by
 * implementing appropriate constructors.
 *
 * @author <a href="mailto:jnielsen@sct.com">Jan Nielsen</a>
 *
 * @version "$Revision$"
 **/
public class ChainedException
    extends Exception
    implements ChainedThrowable
{
    /** The chained exception which is the cause of this exception.*/
    private Throwable mCause = null;

    /** Cache of the stack trace for the exception. */
    private StackTrace[] mStackTrace = null;

    /**
     * Constructs an exception with no message and no cause.
     **/
    public ChainedException()
    {
        initCause();
    }

    /**
     * Constructs an exception with a message but no cause. The
     * message should be an internationalized message.
     *
     * @param message exception message
     **/
    public ChainedException( String message )
    {
        super( message );
        initCause();
    }

    /**
     * Constructs a new exception instance with no message but a
     * cause.
     *
     * @param cause underlying cause of the exception
     **/
    public ChainedException( Throwable cause )
    {
        super( null == cause ? null : cause.getMessage() );
        initCause();
        mCause = cause;
    }

    /**
     * Constructs a new exception instance with a message and a
     * cause. The message should be an internationalized message.
     *
     * @param message exception message
     *
     * @param cause underlying cause of the exception
     **/
    public ChainedException( String message, Throwable cause )
    {
        super( message );
        initCause();
        mCause = cause;
    }

    /**
     * Returns the cause for this <code>Throwable</code> or
     * <code>null</code>. The cause is mean for diagnostic purposes
     * only; Clients should not try to use the cause for additional
     * error handling since that would build an inappropriate
     * subsystem dependency.
     *
     * @return throwable associated with this object, or
     * <code>null</code> if not set
     **/
    public Throwable getCause()
    {
        return mCause;
    }

    /**
     * Translate the error message to a locale specific error
     * message.
     *
     * @return localized error message
     **/
    public String getLocalizedMessage()
    {
        return ThrowableHelper.getLocalizedMessage(
            getMessage()
            );
    }

    /**
     * Prints this throwable and its backtrace to the standard
     * error stream.  This method prints a stack trace for this
     * <code>Throwable</code> object on the error output stream that
     * is the value of the field <code>System.err</code>. The first
     * line of output contains the result of the {@link #toString()}
     * method for this object.  Remaining lines represent data
     * previously recorded by the method {@link
     * #fillInStackTrace()}. The format of this information depends on
     * the implementation, but the following example may be regarded
     * as typical:<p/>
     *
     * <blockquote><pre>
     * java.lang.NullPointerException
     *         at MyClass.mash(MyClass.java:9)
     *         at MyClass.crunch(MyClass.java:6)
     *         at MyClass.main(MyClass.java:3)
     * </pre></blockquote>
     *
     * This example was produced by running the program:<p/>
     *
     * <pre>
     * class MyClass {
     *     public static void main(String[] args) {
     *         crunch(null);
     *     }
     *     static void crunch(int[] a) {
     *         mash(a);
     *     }
     *     static void mash(int[] b) {
     *         System.out.println(b[0]);
     *     }
     * }
     * </pre>
     *
     * The backtrace for a throwable with an initialized, non-null
     * cause should generally include the backtrace for the cause.
     * The format of this information depends on the implementation,
     * but the following example may be regarded as typical:<p/>
     *
     * <pre>
     * HighLevelException: MidLevelException: LowLevelException
     *         at Junk.a(Junk.java:13)
     *         at Junk.main(Junk.java:4)
     * Caused by: MidLevelException: LowLevelException
     *         at Junk.c(Junk.java:23)
     *         at Junk.b(Junk.java:17)
     *         at Junk.a(Junk.java:11)
     *         ... 1 more
     * Caused by: LowLevelException
     *         at Junk.e(Junk.java:30)
     *         at Junk.d(Junk.java:27)
     *         at Junk.c(Junk.java:21)
     *         ... 3 more
     * </pre>
     *
     * Note the presence of lines containing the characters
     * <tt>"..."</tt>.  These lines indicate that the remainder of the
     * stack trace for this exception matches the indicated number of
     * frames from the bottom of the stack trace of the exception that
     * was caused by this exception (the "enclosing" exception).  This
     * shorthand can greatly reduce the length of the output in the
     * common case where a wrapped exception is thrown from same
     * method as the "causative exception" is caught.  The above
     * example was produced by running the program:<p/>
     *
     * <pre>
     * public class Junk {
     *     public static void main(String args[]) {
     *         try {
     *             a();
     *         } catch(HighLevelException e) {
     *             e.printStackTrace();
     *         }
     *     }
     *     static void a() throws HighLevelException {
     *         try {
     *             b();
     *         } catch(MidLevelException e) {
     *             throw new HighLevelException(e);
     *         }
     *     }
     *     static void b() throws MidLevelException {
     *         c();
     *     }
     *     static void c() throws MidLevelException {
     *         try {
     *             d();
     *         } catch(LowLevelException e) {
     *             throw new MidLevelException(e);
     *         }
     *     }
     *     static void d() throws LowLevelException {
     *        e();
     *     }
     *     static void e() throws LowLevelException {
     *         throw new LowLevelException();
     *     }
     * }
     *
     * class HighLevelException extends Exception {
     *     HighLevelException(Throwable cause) { super(cause); }
     * }
     *
     * class MidLevelException extends Exception {
     *     MidLevelException(Throwable cause)  { super(cause); }
     * }
     *
     * class LowLevelException extends Exception {
     * }
     * </pre>
     **/
    public void printStackTrace()
    {
        printStackTrace( System.err );
    }

    /**
     * Prints this throwable and its backtrace to the specified
     * print stream.
     *
     * @param stream <code>PrintStream</code> to use for output
     *
     * @throws NullPointerException if stream is <code>null</code>
     **/
    public void printStackTrace(
        PrintStream stream
        )
    {
        synchronized( stream )
        {
            stream.println( this );

            StackTrace[] trace =
                getOurStackTrace0();

            for( int i=0; i < trace.length; i++ )
            {
                stream.println(
                    Resources.getString( 
                        ChainedException.class,
                        "at",
                        new String[]{ trace[i].toString() }
                        )
                    );
            }

            Throwable ourCause = getCause();

            if( null != ourCause )
            {
                if( ourCause instanceof ChainedException )
                {
                    ((ChainedException)ourCause).printStackTraceAsCause(
                        stream,
                        trace
                        );
                }
                else
                {
                    stream.println( 
                        Resources.getString( 
                            ChainedException.class,
                            "caused_by",
                            new String[]{ ourCause.toString() }
                            )
                        );

                    ourCause.printStackTrace(
                        stream
                        );
                }
            }
        }
    }

    /**
     * Prints this throwable and its backtrace to the specified
     * print writer.
     *
     * @param stream <code>PrintWriter</code> to use for output
     *
     * @throws NullPointerException if stream is <code>null</code>
     **/
    public void printStackTrace(
        PrintWriter stream
        )
    {
        // In a pre-1.4 JVM
        synchronized( stream )
        {
            stream.println( this );

            StackTrace[] trace =
                getOurStackTrace0();

            for (int i=0; i < trace.length; i++)
            {
                stream.println( 
                    Resources.getString( 
                        ChainedException.class,
                        "at",
                        new String[]{ trace[i].toString() }
                        )
                    );
            }

            Throwable ourCause = getCause();

            if( null != ourCause )
            {
                if( ourCause instanceof ChainedException )
                {
                    ((ChainedException)ourCause).printStackTraceAsCause(
                        stream,
                        trace
                        );
                }
                else
                {
                    stream.println( 
                        Resources.getString( 
                            ChainedException.class,
                            "caused_by",
                            new String[]{ ourCause.toString() }
                            )
                        );

                    ourCause.printStackTrace(
                        stream
                        );
                }
            }
        }
    }

    /**
     * Returns an array of stack trace objects representing the current
     * stack.
     * 
     * @return array of stack trace elements
     **/
    private StackTrace[] getOurStackTrace0()
    {
        if( null == mStackTrace )
        {
            try
            {
                Method getStackTrace = getClass().getMethod(
                    "getStackTrace",
                    (Class[])null
                    );

                mStackTrace = StackTrace.convertStackTrace(
                    (Object[])getStackTrace.invoke( this, (Object[])null )
                    );
            }
            catch( InvocationTargetException x )
            {
                // In a 1.4 JVM but a problem occurred during invocation, so
                // generate a pre 1.4 trace. The trace will not be correct.
                mStackTrace = getOurStackTraceInPre1_4();
            }
            catch( NoSuchMethodException x )
            {
                // In a pre-1.4 JVM
                mStackTrace = getOurStackTraceInPre1_4();
            }
            catch( IllegalAccessException x )
            {
                // In a pre-1.4 JVM.
                mStackTrace = getOurStackTraceInPre1_4();
            }
        }
        return mStackTrace;
    }
    
   
    /**
     * In a pre-1.4 JVM, create the stack trace elements by first
     * constructing the normal error message and then removing the
     * error message from the stream.
     * 
     * @return array of stack trace elements 
     **/
    private StackTrace[] getOurStackTraceInPre1_4()
    {
        // In a pre-1.4 JVM, create the stack trace as one string
        // object by using the super class mechanism.
        java.io.StringWriter stringWriter = new java.io.StringWriter();
        java.io.PrintWriter printWriter = null;

        StackTrace[] elements = null;
        
        try
        {
            printWriter = new java.io.PrintWriter(
                stringWriter
                );

            super.printStackTrace(
                printWriter
                );

            elements = StackTrace.getStackTrace(
                this,
                stringWriter.toString()
                );
        }
        finally
        {
            if( null != printWriter )
            {
                printWriter.close();
            }
        }
        return elements;
    }

    /**
     * Always <code>null</code> out the 1.4 cause to ensure serialized
     * exceptions are properly reconstructed.
     **/
    private void initCause()
    {
        try
        {
            Method initCause = this.getClass().getMethod(
                "initCause", new Class[]{ Throwable.class }
                );

            initCause.invoke( this, new Object[]{ null } );
        }
        catch( NoSuchMethodException x )
        {
            ;
            // This is not a JDK 1.4 environment.
        }
        catch( InvocationTargetException x )
        {
            ;
            // Something bad happened when initCause was executed.
        }
        catch( IllegalAccessException x )
        {
            ;
            // This is not a JDK 1.4 environment, but the object has defined
            // an initCause method which is not accesssible.
        }
    }

    /**
     * Print our stack trace as a cause for the specified stack
     * trace.
     * 
     * @param stream stream to which stack trace should be written
     * 
     * @param causedTrace stack trace to write to stream
     **/
    private void printStackTraceAsCause(
        PrintStream stream,
        StackTrace[] causedTrace
        )
    {
        StackTrace[] trace = getOurStackTrace0();

        int m = trace.length - 1;
        int n = causedTrace.length - 1;

        while( 0 <= m && 0 <= n && trace[m].equals( causedTrace[n] ) )
        {
            m--;
            n--;
        }

        stream.println( 
            Resources.getString( 
                ChainedException.class,
                "caused_by",
                new String[]{ this.toString() }
                )
            );

        for( int i = 0; i <= m; i++ )
        {
            stream.println( 
                Resources.getString( 
                    ChainedException.class,
                    "at",
                    new String[]{ trace[i].toString() }
                    )
                );
        }

        int framesInCommon = trace.length - 1 - m;

        if( 0 != framesInCommon )
        {
            stream.println( 
                Resources.getString( 
                    ChainedException.class,
                    "more",
                    new String[]{ TypeConverter.toString( framesInCommon ) }
                    )
                );
        }

        Throwable ourCause = getCause();

        if( null != ourCause )
        {
            if( ourCause instanceof ChainedException )
            {
                ((ChainedException)ourCause).printStackTraceAsCause(
                    stream,
                    trace
                    );
            }
        }
    }

    /**
     * Print our stack trace as a cause for the specified stack trace.
     * 
     * @param stream stream to which stack trace should be written
     * 
     * @param causedTrace stack trace to write to stream
     **/
    private void printStackTraceAsCause(
        PrintWriter stream,
        StackTrace[] causedTrace
        )
    {
        StackTrace[] trace = getOurStackTrace0();

        int m = trace.length - 1;
        int n = causedTrace.length - 1;

        while( 0 <= m && 0 <= n && trace[m].equals( causedTrace[n] ) )
        {
            m--;
            n--;
        }

        stream.println( 
            Resources.getString( 
                ChainedException.class,
                "caused_by",
                new String[]{ this.toString() }
                )
            );

        for( int i=0; i <= m; i++ )
        {
            stream.println( 
                Resources.getString( 
                    ChainedException.class,
                    "at",
                    new String[]{ trace[i].toString() }
                    )
                );
        }

        int framesInCommon = trace.length - 1 - m;

        if( 0 != framesInCommon )
        {
            stream.println( 
                Resources.getString( 
                    ChainedException.class,
                    "more",
                    new String[]{ TypeConverter.toString( framesInCommon ) }
                    )
                );
        }

        Throwable ourCause = getCause();

        if( null != ourCause )
        {
            if( ourCause instanceof ChainedException )
            {
                ((ChainedException)ourCause).printStackTraceAsCause(
                    stream,
                    trace
                    );
            }
        }
    }
}
