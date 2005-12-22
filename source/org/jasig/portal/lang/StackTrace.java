/**
 * Copyright © 2004,2005 The JA-SIG Collaborative.  All rights reserved.
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

import java.lang.reflect.Method;
import java.io.PrintWriter;
import java.io.StringWriter;

/**
 * The <code>StackTrace</code> object represents a single stack frame
 * of information. The entire set of stack frames is represented as an
 * array of <code>StackTrace</code> object.<p/>
 *
 * Note: The stack frame information is not always available because
 * some JVMs strip some or all the required information.
 *
 * @author <a href="mailto:jnielsen@sct.com">Jan Nielsen</a>
 *
 * @version "$Revision$"
 **/
public final class StackTrace
{
    /** Default size of the stack trace array buffer. */
    private static final int DEFAULT_STACK_TRACE_ARRAY_SIZE =
        TypeConverter.toInt(
            Resources.getString(
                StackTrace.class,
                "getStackTraceElement.default_stack_trace_array_size"
                )
            );

    /** Moniker for native method. */
    private static final int NATIVE_METHOD = -2;

    /** Prime number used to calculate hash code. */
    private static final int PRIME = 31;

    /**
     * Returns the name of the source file. If the parser failed to
     * find the source file name the value <code>"Unknown"</code> will
     * be returned.
     *
     * @return name of the source file
     **/
    public final String getFileName()
    {
        String fileName = mFileName;

        if( null == fileName )
        {
            fileName = Resources.getString(
                StackTrace.class,
                "unknown_source"
                );
        }

        return fileName;
    }

    /**
     * Returns the name of the class. The parser should always be
     * able to identify the calling class.
     *
     * @return name of the class
     **/
    public final String getClassName()
    {
        return mClassName;
    }

    /**
     * Returns the name of the method. The parser should alwas be
     * able to identify the calling method name.
     *
     * @return name of the method
     **/
    public final String getMethodName()
    {
        return mMethodName;
    }

    /**
     * Returns the line number in the source file. If the parser is
     * unable to determine the line number the value <code>-1</code>
     * will be returned. If the parser determines the method is a
     * native, a -2 is returned.
     *
     * @return source code line number, or -1 or -2
     **/
    public final int getLineNumber()
    {
        return mLineNumber;
    }

    /**
     * Returns <code>true</code> if method is implemented
     * natively.
     *
     * @return <code>true</code> is native method; otherwise
     * <code>false</code>
     **/
    public final boolean isNativeMethod()
    {
        return NATIVE_METHOD == mLineNumber;
    }

    /**
     * Returns all <code>StackTrace</code>s in an array of the
     * specified throwable.
     *
     *
     * @param throwable throwable being parsed
     *
     * @return array of <code>StackTrace</code>s
     *
     * @throws NullPointerException if throwable is <code>null</code>
     *
     * @throws NullPointerException if stackTrace is <code>null</code>
     *
     * @throws IllegalArgumentException if stackTrace is an empty
     * string
     **/
    public static final StackTrace[] getStackTrace(
        Throwable throwable
        )
    {
        StringWriter stringWriter = new StringWriter();
        PrintWriter printWriter = null;

        StackTrace[] elements = null;
        
        try
        {
            printWriter = new PrintWriter(
                stringWriter
                );

            throwable.printStackTrace(
                printWriter
                );

            elements = StackTrace.getStackTrace(
                throwable,
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
     * Returns all <code>StackTrace</code>s in an array. This method
     * is used by the <code>Chained*</code> concrete classes to avoid
     * a circular dependency. The throwable, and its string output
     * must both be specified. Both are required because the throwable
     * could be a <code>com.sct.pipeline.lang.ChainedException</code>
     * which would result in a circular dependency if a
     * <code>printStackTrace</code> was used in this method to
     * retrieve the trace information. Invoke this method in the
     * following fashion:
     *
     * <code><pre>
     *    StringWriter stringWriter = new StringWriter();
     *
     *    PrintWriter printWriter = new PrintWriter(
     *        stringWriter
     *        );
     *
     *    Throwable t = new Throwable();
     *
     *    t.printStackTrace(
     *        printWriter
     *        );
     *
     *    printWriter.close();
     *
     *    StackTrace[] frames = getStackTraceElements(
     *        t,
     *        stringWriter.toString()
     *        );
     * </pre></code>
     *
     * @param throwable throwable being parsed
     *
     * @param stackTrace string output of
     * <code>throwable.printStackTrace</code>
     *
     * @return array of <code>StackTrace</code>s
     *
     * @throws NullPointerException if throwable is <code>null</code>
     *
     * @throws NullPointerException if stackTrace is <code>null</code>
     *
     * @throws IllegalArgumentException if stackTrace is an empty string
     **/
    static final StackTrace[] getStackTrace(
        Throwable throwable,
        String stackTrace
        )
    {
        if( null == throwable )
        {
            throw new NullPointerException(
                ThrowableHelper.getInternationalizedMessage(
                    StackTrace.class,
                    "getStackTraceElement.error.throwable_is_null"
                    )
                );
        }

        if( null == stackTrace )
        {
            throw new NullPointerException(
                ThrowableHelper.getInternationalizedMessage(
                    StackTrace.class,
                    "getStackTraceElement.error.stack_trace_is_null"
                    )
                );
        }

        if( stackTrace.equals("") )
        {
            throw new IllegalArgumentException(
                ThrowableHelper.getInternationalizedMessage(
                    StackTrace.class,
                    "getStackTraceElement.error.stack_trace_is_empty"
                    )
                );
        }

        return parseStackTrace(
            throwable,
            stackTrace
            );
    }

    /**
     * Returns the stack trace elements of the client. The array of
     * stack trace elements start with the client's invoking frame in
     * the zeroth element.
     *
     * @return array of stack trace elements of the client
     **/
    public static final StackTrace[] getStackTrace()
    {
        StringWriter stringWriter = new StringWriter();

        PrintWriter printWriter = null;
        StackTrace[] clientFrames = null;

        try
        {
        
            printWriter = new PrintWriter(
                stringWriter
                );

            Throwable t = new Throwable();
        
            t.printStackTrace(
                printWriter
                );
        
            printWriter.close();
        
            StackTrace[] frames = getStackTrace(
                t,
                stringWriter.toString()
                );
        
            clientFrames = new StackTrace[
                frames.length - 1
            ];
        
            // Skip the first frame (this method).
            System.arraycopy(
                frames,
                1,
                clientFrames,
                0,
                clientFrames.length
                );
        }
        finally
        {
            if( null != printWriter )
            {
                printWriter.close();
            }
        }

        return clientFrames;
    }

    /**
     * Returns the specified stack trace element if it exists. If an
     * invalid, either too large or too small, index is specified
     * <code>null</code> is returned.
     *
     * @param index index of the stack trace element to return
     *
     * @return stack trace element at the index or <code>null</code>
     **/
    public static final StackTrace getStackTrace(
        int index
        )
    {
        StackTrace element = null;
        PrintWriter printWriter = null;
        
        try
        {
        
            if( !(0 > index) )
            {
                String moniker = "dummy moniker";

                StringWriter stringWriter = new StringWriter();

                printWriter = new PrintWriter(
                    stringWriter
                    );

                Throwable throwable = new Throwable( moniker );

                throwable.printStackTrace(
                    printWriter
                    );

                StackTrace[] elements = getStackTrace(
                    throwable,
                    stringWriter.toString()
                    );

                if( (0 < index + 1 ) && (elements.length > index + 1) )
                {    
                    element = elements[ index + 1];
                }
            }
        }
        finally
        {
            if( null != printWriter )
            {
                printWriter.close();
            }
        }

        return element;
    }
    
    /**
     * Converts a JDK 1.4 stack trace to this object.
     * 
     * @param stackTrace array of JDK 1.4 stack trace objects
     * 
     * @return array of <code>StackTrace</code> objects
     **/
    static final StackTrace[] convertStackTrace(
        Object[] stackTrace
        )
    {
        int size = stackTrace.length;
        
        StackTrace[] elements = new StackTrace[ size ];
        
        for( int i = 0; i < size; i++ )
        {
            elements[i] = convertElement( stackTrace[i] );
        }
        return elements;
    }

    /**
     * Converts a J2SDK 1.4 <code>StackTraceElement</code> object
     * to a <code>StackTrace</code> object.
     * 
     * @param element <code>StackTraceElement</code> object
     * 
     * @return corresponding <code>StackTrace</code> object
     **/
    private static final StackTrace convertElement(
        Object element
        )
    {
        StackTrace stackTraceElement = null;

        try
        {
            Method getClassName =
                element.getClass().getMethod( "getClassName", (Class[])null ); 
            
            Method getMethodName = 
                element.getClass().getMethod( "getMethodName", (Class[])null );
            
            Method getFileName = 
                element.getClass().getMethod( "getFileName", (Class[])null );

            Method getLineNumber = 
                element.getClass().getMethod( "getLineNumber", (Class[])null );

            stackTraceElement = new StackTrace(
                (String)getClassName.invoke( element, (Object[])null ),
                (String)getMethodName.invoke( element, (Object[])null ),
                (String)getFileName.invoke( element, (Object[])null ),
                ((Integer)getLineNumber.invoke( element, (Object[])null )).intValue()
                );
        }
        catch( Exception x )
        {
            ThrowableHelper.handle( x );
        }
        return stackTraceElement;
    }

    /**
     * Returns an array of <code>StackTrace</code> objects representing
     * the <code>Throwable</code> argument.
     * 
     * @param throwable 
     * 
     * @param stackTrace 
     * 
     * @return array of <code>StackTrace</code> objects
     **/    
    private static final StackTrace[] parseStackTrace(
        Throwable throwable,
        String stackTrace
        )
    {
        // Remove the standard header information in the stack trace.
        try
        {
            stackTrace = stackTrace.substring(
                null != throwable.getLocalizedMessage() ?
                ((throwable.getClass().getName() +
                  ":" +
                  throwable.getLocalizedMessage()).length() + 1) :
                (throwable.getClass().getName().length() + 1)
                );
        }
        catch( IndexOutOfBoundsException x )
        {
            ThrowableHelper.handle( x );
        }

        Object[] stackTraceObjects = parseStackTrace(
            stackTrace
            );

        StackTrace[] stackTraceArray =
            new StackTrace[ stackTraceObjects.length ];

        if( null != stackTraceObjects )
        {
            System.arraycopy(
                stackTraceObjects,
                0,
                stackTraceArray,
                0,
                stackTraceArray.length
                );
        }

        return stackTraceArray;
    }

    /**
     * Parses a stack trace into individual stack frames.
     *  
     * @param stackTrace stack trace to parse
     * 
     * @return array of stack trace objects
     **/
    private static final Object[] parseStackTrace(
        String stackTrace
        )
    {
        final ParseState state = new ParseState(
            stackTrace
            );

        java.util.ArrayList stackTraceElements = new java.util.ArrayList(
            DEFAULT_STACK_TRACE_ARRAY_SIZE
            );

        while( (state.mIndex+3) < state.mSize )
        {
            StackTrace frame = parseStackTrace( state );

            stackTraceElements.add( frame );
        }

        return stackTraceElements.toArray();
    }

    /**
     * Returns a representation of the stack frame element. The
     * format of the information may vary but the following examples
     * are typical:
     *
     * <ul>
     * <li><tt>mycompany.MyClass.snap(MyClass.java:9)</tt></li>
     * <li><tt>mycompany.MyClass.snap(MyClass.java)</tt></li>
     * <li><tt>mycompany.MyClass.snap(Unknown Source)</tt></li>
     * <li><tt>mycompany.MyClass.snap(Native Method)</tt></li>
     * </ul>
     *
     * @return string presentation of the stack frame.
     **/
    public final String toString()
    {
        StringBuffer buffer = new StringBuffer( mClassName ).
            append( "." ).
            append( mMethodName );

        if( isNativeMethod() )
        {
            buffer.append( "(" + 
                           Resources.getString(
                               StackTrace.class,
                               "native_method"
                               ) + ")" );
        }
        else if( null != mFileName && 0 <= mLineNumber )
        {
            buffer.append( "(" ).
            append( mFileName ).
            append( ":" ).
            append( mLineNumber ).
            append( ")" );
        }
        else if( null != mFileName )
        {
            buffer.append( "(" ).
            append( mFileName ).
            append( ")" );
        }
        else
        {
            buffer.append( "(" + 
                           Resources.getString(
                               StackTrace.class,
                               "unknown_source"
                               ) + ")" );
        }

        return buffer.toString();
    }

    /**
     * Returns a string representation of the stack trace elements.
     *
     * @param elements stack trace elements to stringify
     *
     * @return string representation of the stack trace array 
     **/
    public static final String toString(
        StackTrace[] elements
        )
    {
        String sep = System.getProperty( "line.separator" );
        StringBuffer buffer = new StringBuffer();

        for( int i = 0; i < elements.length; i++ )
        {
            buffer.append(
                elements[i].toString()
                ).append( sep );
        }

        return buffer.toString();
    }

    /**
     * Returns <code>true<code> if the specified object is another
     * <tt>StackTrace</tt> instance representing the same
     * execution point as this instance.  Two stack trace elements
     * <tt>a</tt> and <tt>b</tt> are equal if and only if:
     *
     * <pre><code>
     *     equals(a.getFileName(), b.getFileName()) &&
     *     a.getLineNumber() == b.getLineNumber()) &&
     *     equals(a.getClassName(), b.getClassName()) &&
     *     equals(a.getMethodName(), b.getMethodName())
     * </code></pre>
     *
     * where <tt>equals</tt> is defined as:
     *
     * <pre><code>
     *     static boolean equals(Object a, Object b) {
     *         return a==b || (a != null && a.equals(b));
     *     }
     * </code></pre>
     *
     * @param obj the object to be compared with this stack trace
     * element
     *
     * @return <code>true</code> if the specified object is another
     * <tt>StackTrace</tt> instance representing the same
     * execution point as this instance; otherwise <code>false</code>
     **/
    public boolean equals( Object obj )
    {
        boolean isEqual = false;

        if( this == obj )
        {
            isEqual = true;
        }
        else if( obj instanceof StackTrace )
        {
            StackTrace e = (StackTrace)obj;

            isEqual = e.mClassName.equals(mClassName) &&
                e.mLineNumber == mLineNumber &&
                eq(mMethodName, e.mMethodName) &&
                eq(mFileName, e.mFileName);
        }

        return isEqual;
    }

    /**
     * Returns <code>true</code> if the two objects are
     * equal. Equality is defined as either object identity or if
     * <code>a.equals(b)</code> if <code>a</code> is not
     * <code>null</code>.
     * 
     * @param a left hand side argument
     * 
     * @param b right hand side argument
     * 
     * @return <code>true</code> if the two are equal; otherwise
     * <code>false</code>
     **/
    private static final boolean eq(Object a, Object b)
    {
        return a==b || (null != a && a.equals(b));
    }

    /**
     * Returns a hash code value for this stack trace element.
     *
     * @return hash code value
     **/
    public int hashCode()
    {
        int result = PRIME*mClassName.hashCode() + mMethodName.hashCode();
        result = PRIME*result + (null == mFileName ? 0 : mFileName.hashCode());
        result = PRIME*result + mLineNumber;

        return result;
    }

    /**
     * Hidden constructor for the <code>StackTrace</code> object.
     * 
     * @param className class name in the stack frame
     * 
     * @param methodName method name in the stack frame
     * 
     * @param fileName file name in the stack frame
     * 
     * @param lineNumber line number in the stack frame 
     **/
    private StackTrace(
        String className,
        String methodName,
        String fileName,
        int lineNumber
        )
    {
        mClassName = className;
        mMethodName = methodName;
        mFileName = fileName;
        mLineNumber = lineNumber;
    }

    /**
     * Parse the first class name from the specified stack trace
     * starting at the specified index.
     *
     * @param state parsing state
     *
     * @return parsed stack trace element
     **/
    private static final StackTrace parseStackTrace(
        ParseState state
        )
    {
        String className = null;
        String methodName = null;
        String fileName = null;
        int lineNumber = -1;

        int classNameStart;
        int classNameEnd;
        int methodNameStart = 0;
        int methodNameEnd = 0;
        int fileNameStart;
        int fileNameEnd;
        int lineNumberStart;
        int lineNumberEnd;

        // Move to beginning of class name.
        while( state.mIndex + 2 < state.mSize  )
        {
            if( 'a' == state.mStackTraceCharacters[ state.mIndex     ] &&
                't' == state.mStackTraceCharacters[ state.mIndex + 1 ] &&
                ' ' == state.mStackTraceCharacters[ state.mIndex + 2 ] )
            {
                state.mIndex += 3;
                break;
            }
            state.mIndex++;
        }
        classNameStart = state.mIndex;
        classNameEnd = classNameStart;

        // Move the end of method name, saving the class name end and
        // method name start along the way.
        while( state.mIndex < state.mSize )
        {
            if( '.' == state.mStackTraceCharacters[ state.mIndex ] )
            {
                classNameEnd = state.mIndex;
                methodNameStart = classNameEnd+1;
            }
            else if( '(' == state.mStackTraceCharacters[ state.mIndex ] )
            {
                methodNameEnd = state.mIndex;
                break;
            }
            state.mIndex++;
        }

        // Define the class name.
        try
        {
            className = state.mStackTrace.substring(
                classNameStart,
                classNameEnd
                );
        }
        catch( IndexOutOfBoundsException x )
        {
            ThrowableHelper.handle( x );
        }

        try
        {
            // Define the method name.
            methodName = state.mStackTrace.substring(
                methodNameStart,
                methodNameEnd
                );
        }
        catch( IndexOutOfBoundsException x )
        {
            ThrowableHelper.handle( x );
        }

        // Move to after the parenthesis.
        fileNameStart = state.mIndex + 1;

        if( state.mIndex + 14 < state.mSize &&
            'U' == state.mStackTraceCharacters[ state.mIndex + 1 ] &&
            'n' == state.mStackTraceCharacters[ state.mIndex + 2 ] &&
            'k' == state.mStackTraceCharacters[ state.mIndex + 3 ] &&
            'n' == state.mStackTraceCharacters[ state.mIndex + 4 ] &&
            'o' == state.mStackTraceCharacters[ state.mIndex + 5 ] &&
            'w' == state.mStackTraceCharacters[ state.mIndex + 6 ] &&
            'n' == state.mStackTraceCharacters[ state.mIndex + 7 ] &&
            ' ' == state.mStackTraceCharacters[ state.mIndex + 8 ] &&
            'S' == state.mStackTraceCharacters[ state.mIndex + 9 ] &&
            'o' == state.mStackTraceCharacters[ state.mIndex + 10] &&
            'u' == state.mStackTraceCharacters[ state.mIndex + 11] &&
            'r' == state.mStackTraceCharacters[ state.mIndex + 12] &&
            'c' == state.mStackTraceCharacters[ state.mIndex + 13] &&
            'e' == state.mStackTraceCharacters[ state.mIndex + 14] )
        {
            fileName = null;
            lineNumber = -1;
        }
        else if(
            state.mIndex + 13 < state.mSize &&
            'N' == state.mStackTraceCharacters[ state.mIndex + 1 ] &&
            'a' == state.mStackTraceCharacters[ state.mIndex + 2 ] &&
            't' == state.mStackTraceCharacters[ state.mIndex + 3 ] &&
            'i' == state.mStackTraceCharacters[ state.mIndex + 4 ] &&
            'v' == state.mStackTraceCharacters[ state.mIndex + 5 ] &&
            'e' == state.mStackTraceCharacters[ state.mIndex + 6 ] &&
            ' ' == state.mStackTraceCharacters[ state.mIndex + 7 ] &&
            'M' == state.mStackTraceCharacters[ state.mIndex + 8 ] &&
            'e' == state.mStackTraceCharacters[ state.mIndex + 9 ] &&
            't' == state.mStackTraceCharacters[ state.mIndex + 10] &&
            'h' == state.mStackTraceCharacters[ state.mIndex + 11] &&
            'o' == state.mStackTraceCharacters[ state.mIndex + 12] &&
            'd' == state.mStackTraceCharacters[ state.mIndex + 13] )
        {
            lineNumber = -2;
        }
        else
        {
            // Current character.
            char ch;

            // Move to end of source file name by looking for the ':'.
            while( state.mIndex < state.mSize )
            {
                ch = state.mStackTraceCharacters[ state.mIndex ];

                if( '\n' == ch || '\r' == ch )
                {
                    // Failed to find ':' so leave the file name and line
                    // number as unknown.

                    // In case this is a '\r\n' platform.
                    if( (state.mIndex+1) < state.mSize &&
                        '\n' == state.mStackTraceCharacters[state.mIndex+1] )
                    {
                        state.mIndex++;
                    }
                    break;
                }
                else if( '.' == ch )
                {
                    // Move past the ".java".
                    fileNameEnd = state.mIndex + 5;

                    // Move past the ":".
                    lineNumberStart = fileNameEnd + 1;

                    // Define the file name.
                    fileName = state.mStackTrace.substring(
                        fileNameStart,
                        fileNameEnd
                        );

                    // Move to the start of the line number.
                    state.mIndex = lineNumberStart;

                    while( state.mIndex < state.mSize )
                    {
                        ch = state.mStackTraceCharacters[ state.mIndex ];
                        if( '\n' == ch || '\r' == ch )
                        {
                            // Failed to find the terminator, so leave
                            // line number unknown.

                            // In case this is a '\r\n' platform.
                            if( (state.mIndex+1) < state.mSize &&
                                '\n' == state.mStackTraceCharacters[
                                    state.mIndex+1 ] )
                            {
                                state.mIndex++;
                            }
                            break;
                        }
                        else if(
                            ')'==state.mStackTraceCharacters[state.mIndex]
                            )
                        {
                            lineNumberEnd = state.mIndex;

                            // Define the line number.
                            lineNumber = TypeConverter.toInt(
                                state.mStackTrace.substring(
                                    lineNumberStart,
                                    lineNumberEnd
                                    )
                                );
                            // Don't break here in case there are spaces at
                            // the end of the stack trace.
                        }
                        state.mIndex++;
                    }
                    break;
                }
                state.mIndex++;
            }
        }

        StackTrace frame = new StackTrace(
            className,
            methodName,
            fileName,
            lineNumber
            );

        return frame;
    }

    /**
     * The <code>ParseState</code> class holds the current parsing
     * state.
     * 
     * @author <a href="mailto:jnielsen@sct.com">Jan Nielsen</a>
     * 
     * @version "$Revision$"
     *
     * @invariant null != mStackTrace
     **/
    private static class ParseState
    {
        /**
         * Construct a parsing state object with the specified
         * stack trace.
         *
         * @param stackTrace stack trace to be parsed
         **/
        private ParseState( String stackTrace )
        {
            mStackTrace = stackTrace;
            mStackTraceCharacters = mStackTrace.toCharArray();
            mSize = mStackTraceCharacters.length;
            mIndex = 0;
        }

        /** Entire stack trace. */
        private String mStackTrace;

        /** Entire stack trace in character array form. */
        private char [] mStackTraceCharacters;

        /** Size of the entire stack trace. */
        private int mSize;

        /** Current index in the stack trace array. */
        private int mIndex;
    }

    /**
     * Holder for the source file name.
     **/
    private String mFileName;

    /**
     * Holder for the class name.
     **/
    private String mClassName;

    /**
     * Holder for the method name.
     **/
    private String mMethodName;

    /**
     * Holder for the line number. The value (-2) is used to denote a
     * native method.
     **/
    private int mLineNumber = -1;
}
