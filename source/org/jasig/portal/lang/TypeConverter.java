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

/**
 * The <code>TypeConverter</code> class provides efficient and
 * consistent static type converter utilities for the basic types.
 *
 * @author <a href="mailto:jnielsen@sct.com">Jan Nielsen</a>
 * 
 * @version $Revision$
 * 
 * @invariant null != RCS_ID
 **/
public final class TypeConverter
{
    /** Canonical string representation of <code>true</code>. */
    public static final String TRUE = "true";

    /** Canonical string representation of <code>false</code>. */
    public static final String FALSE = "false";

    /**
     * Returns a canonical <code>String</code> object representing the
     * specified <code>boolean</code>. If the value is
     * <code>true</code>, the string <code>"true"</code> will be
     * returned; if the value is <code>false</code>, the string
     * <code>"false"</code>.
     *
     * @param value <code>boolean</code> value to be converted
     *
     * @return canonical string representation of the
     * <code>boolean</code> value
     *
     * @pre null != RCS_ID
     *
     * @post null != $result
     **/
    public static final String toString( boolean value )
    {
        String outValue = null;

        if( value )
        {
            outValue = TRUE;
        }
        else
        {
            outValue = FALSE;
        }

        return outValue;
    }

    /**
     * Returns the <code>boolean</code> value represented by the
     * specified string, ignoring case. If the value is neither
     * <code>"true"</code>, nor <code>"false"</code> (ignoring case),
     * an <code>IllegalArgumentException</code> is thrown.<p/>
     *
     * Note: This implementation is different than the
     * <code>java.lang.Boolean</code> implementation. The
     * <code>java.lang.Boolean</code> treats <code>null</code> and any
     * non-<code>"true"</code> value (ignoring case) as
     * <code>false</code>, i.e., there is no validation of
     * <code>false</code> values.
     *
     * @param value string representation of a boolean value
     *
     * @return <code>boolean</code> value represented by the argument
     *
     * @throws IllegalArgumentException if value is (ignoring case)
     * neither <code>"true"</code>, nor <code>"false"</code>
     *
     * @pre null != value
     *
     * @post null != RCS_ID
     **/
    public static final boolean toBoolean( String value )
    {
        try
        {
            boolean outValue = false;

            if( TRUE == value || value.equalsIgnoreCase( TRUE ) )
            {
                outValue = true;
            }
            else if( FALSE == value || value.equalsIgnoreCase( FALSE ) )
            {
                outValue = false;
            }
            else
            {
                throw new IllegalArgumentException(
                    ThrowableHelper.getInternationalizedMessage(
                        TypeConverter.class,
                        "error.argument_not_parseable",
                        new String[]{ value, "boolean" }
                        )
                    );
            }

            return outValue;
        }
        catch( NullPointerException x )
        {
            throw new IllegalArgumentException(
                ThrowableHelper.getInternationalizedMessage(
                    TypeConverter.class,
                    "error.argument_is_null"
                    )
                );
        }
    }

    /**
     * Returns a string representation of the argument of radix 10.
     *
     * @param value <code>byte</code> value to be converted
     *
     * @return string respresentation of the argument
     *
     * @pre null != RCS_ID
     *
     * @post null != $result
     **/
    public static final String toString( byte value )
    {
        return toString((int)value);
    }

    /**
     * Returns a signed decimal <code>byte</code> value. The
     * characters in the string must all be decimal digits, except
     * that the first character may be an ASCII minus sign '-'
     * ('\u002D') to indicate a negative value.
     *
     * @param value string representation of a <code>byte</code> value
     *
     * @return <code>byte</code> value represented by the argument
     *
     * @throws IllegalArgumentException if argument is not a parseable
     * <code>byte</code>
     *
     * @pre null != value
     *
     * @post null != RCS_ID
     **/
    public static final byte toByte( String value )
    {
        try
        {
            return Byte.parseByte( value );
        }
        catch( NumberFormatException x )
        {
            throw new IllegalArgumentException(
                ThrowableHelper.getInternationalizedMessage(
                    TypeConverter.class,
                    "error.argument_not_parseable",
                    new String[]{ value, "byte" }
                    )
                );
        }
        catch( NullPointerException x )
        {
            throw new IllegalArgumentException(
                ThrowableHelper.getInternationalizedMessage(
                    TypeConverter.class,
                    "error.argument_is_null"
                    )
                );
        }
    }

    /**
     * Returns a string representation of the argument. The result
     * string of length 1 consisting solely of the specified char.
     *
     * @param value <code>char</code> value to be converted
     *
     * @return string representation of the argument
     *
     * @pre null != RCS_ID
     *
     * @post null != $result
     **/
    public static final String toString( char value )
    {
        return String.valueOf( new char[]{ value } );
    }

    /**
     * Returns the first character of the string.
     *
     * @param value string representation of the <code>char</code>
     *
     * @return <code>char</code> representation of the argument
     *
     * @throws IllegalArgumentException if <code>value</code> is
     * <code>null</code> or of zero length
     *
     * @pre null != value
     *
     * @post null != RCS_ID
     **/
    public static final char toChar( String value )
    {
        try
        {
            return value.charAt( 0 );
        }
        catch( IndexOutOfBoundsException x )
        {
            throw new IllegalArgumentException(
                ThrowableHelper.getInternationalizedMessage(
                    TypeConverter.class,
                    "error.argument_not_parseable",
                    new String[]{ value, "char" }
                    )
                );
        }
        catch( NullPointerException x )
        {
            throw new IllegalArgumentException(
                ThrowableHelper.getInternationalizedMessage(
                    TypeConverter.class,
                    "error.argument_is_null"
                    )
                );
        }
    }

    /**
     * Returns a string representation of the specified
     * <code>short</code> value of radix 10.
     *
     * @param value <code>short</code> value to be converted
     *
     * @return string representation of the argument
     *
     * @pre null != RCS_ID
     *
     * @post null != $result
     **/
    public static final String toString( short value )
    {
        return toString( (int)value );
    }

    /**
     * Returns the <code>short</code> representation of the specified
     * string. The characters in the string must all be decimal
     * digits, except that the first character may be an ASCII minus
     * sign '-' ('\u002D') to indicate a negative value.
     *
     * @param value string representation of the <code>short</code>
     *
     * @return <code>short</code> representation of the argument
     *
     * @throws IllegalArgumentException if argument is not a parseable
     * <code>short</code>
     *
     * @pre null != value
     *
     * @post null != RCS_ID
     **/
    public static final short toShort( String value )
    {
        try
        {
            return Short.parseShort( value );
        }
        catch( NumberFormatException x )
        {
            throw new IllegalArgumentException(
                ThrowableHelper.getInternationalizedMessage(
                    TypeConverter.class,
                    "error.argument_not_parseable",
                    new String[] { value, "short" }
                    )
                );
        }
        catch( NullPointerException x )
        {
            throw new IllegalArgumentException(
                ThrowableHelper.getInternationalizedMessage(
                    TypeConverter.class,
                    "error.argument_is_null"
                    )
                );
        }
    }

    /**
     * Returns a string representation of the specified
     * <code>int</code>.
     *
     * @param value <code>int</code> >value to be converted
     *
     * @return string representation of the argument
     *
     * @pre null != RCS_ID
     *
     * @post null != $result
     **/
    public static final String toString( int value )
    {
        return Integer.toString( value );
    }

    /**
     * Parses the string argument as a signed decimal integer. The
     * characters in the string must all be decimal digits, except
     * that the first character may be an ASCII minus sign '-'
     * ('\u002D') to indicate a negative value.
     *
     * @param value string representation of the <code>int</code>
     *
     * @return <code>int</code> representation of the argument
     *
     * @throws IllegalArgumentException if string does not contain a
     * parseable <code>int</code>
     *
     * @pre null != value
     *
     * @post null != RCS_ID
     **/
    public static final int toInt( String value )
    {
        try
        {
            return Integer.parseInt( value );
        }
        catch( NumberFormatException x )
        {
            throw new IllegalArgumentException(
                ThrowableHelper.getInternationalizedMessage(
                    TypeConverter.class,
                    "error.argument_not_parseable",
                    new String[] { value, "int" }
                    )
                );
        }
        catch( NullPointerException x )
        {
            throw new IllegalArgumentException(
                ThrowableHelper.getInternationalizedMessage(
                    TypeConverter.class,
                    "error.argument_is_null"
                    )
                );
        }
    }

    /**
     * Returns <code>true</code> if the value can be parsed to an
     * <code>int</code>.
     *
     * @param value string representation of the <code>int</code>
     *
     * @return <code>true</code> if argument is an <code>int</code>;
     * otherwise <code>false</code>
     *
     * @pre null != value
     *
     * @post null != RCS_ID
     **/
    public static final boolean isInt( String value )
    {
        boolean isInt = true;
        
        try
        {
            TypeConverter.toInt( value );
        }
        catch( IllegalArgumentException x )
        {
            isInt = false;
        }

        return isInt;
    }

    /**
     * Returns a string representation of the specified
     * <code>long</code>.
     *
     * @param value <code>long</code> value to be converted
     *
     * @return string representation of the argument
     *
     * @pre null != RCS_ID
     *
     * @post null != $result
     **/
    public static final String toString( long value )
    {
        return Long.toString( value );
    }

    /**
     * Parses the string argument as a signed long in the radix
     * specified by the second argument. The characters in the string
     * must all be digits of the specified radix (as determined by
     * whether Character.digit returns a nonnegative value), except
     * that the first character may be an ASCII minus sign '-'
     * ('\u002d' to indicate a negative value. The resulting long
     * value is returned. Note that neither L nor l is permitted to
     * appear at the end of the string as a type indicator, as would
     * be permitted in Java programming language source code.
     *
     * @param value string representation of the <code>long</code>
     *
     * @return <code>long</code> representation of the argument
     *
     * @throws IllegalArgumentException if string does not contain a
     * parseable <code>long</code>
     *
     * @pre null != value
     *
     * @post null != RCS_ID
     **/
    public static final long toLong( String value )
    {
        try
        {
            return Long.parseLong( value );
        }
        catch( NumberFormatException x )
        {
            throw new IllegalArgumentException(
                ThrowableHelper.getInternationalizedMessage(
                    TypeConverter.class,
                    "error.argument_not_parseable",
                    new String[] { value, "long" }
                    )
                );
        }
        catch( NullPointerException x )
        {
            throw new IllegalArgumentException(
                ThrowableHelper.getInternationalizedMessage(
                    TypeConverter.class,
                    "error.argument_is_null"
                    )
                );
        }
    }

    /**
     * Returns a string representation of the specified
     * <code>float</code>.
     *
     * @param value <code>float</code> value to be converted
     *
     * @return string representation of the argument
     *
     * @pre null != RCS_ID
     *
     * @post null != $result
     **/
    public static final String toString( float value )
    {
        return Float.toString( value );
    }

    /**
     * Returns a new float initialized to the value represented by the
     * specified String, as performed by the valueOf method of class
     * Double.
     *
     * @param value string representation of the <code>long</code>
     *
     * @return <code>long</code> representation of the argument
     *
     * @throws IllegalArgumentException if string does not contain a
     * parseable <code>long</code>
     *
     * @pre null != value
     *
     * @post null != RCS_ID
     **/
    public static final float toFloat( String value )
    {
        float floatValue = Float.NaN;

        try
        {
            floatValue = Float.parseFloat( value );
        }
        catch( NumberFormatException x )
        {
            // In JRE 1.3 and JRE 1.2, the values "Infinity" and
            // "-Infinity" are not parsed to the appropriate typed
            // "value".
            if( "Infinity".equals( value ) )
            {
                floatValue = Float.POSITIVE_INFINITY;
            }
            else if( "-Infinity".equals( value ) )
            {
                floatValue = Float.NEGATIVE_INFINITY;
            }
            else
            {
                throw new IllegalArgumentException(
                    ThrowableHelper.getInternationalizedMessage(
                        TypeConverter.class,
                        "error.argument_not_parseable",
                        new String[] { value, "float" }
                        )
                    );
            }
        }
        catch( NullPointerException x )
        {
            throw new IllegalArgumentException(
                ThrowableHelper.getInternationalizedMessage(
                    TypeConverter.class,
                    "error.argument_is_null"
                    )
                );
        }

        return floatValue;
    }

    /**
     * Returns a string representation of the specified
     * <code>double</code>.
     *
     * @param value <code>double</code> value to be converted
     *
     * @return string representation of the argument
     *
     * @pre null != RCS_ID
     *
     * @post null != $result
     **/
    public static final String toString( double value )
    {
        return Double.toString( value );
    }

    /**
     * Returns a new double initialized to the value represented by
     * the specified String, as performed by the valueOf method of
     * class Double.
     *
     * @param value string representation of the <code>long</code>
     *
     * @return <code>long</code> representation of the argument
     *
     * @throws IllegalArgumentException if string does not contain a
     * parseable <code>long</code>
     *
     * @pre null != value
     *
     * @post null != RCS_ID
     **/
    public static final double toDouble( String value )
    {
        double doubleValue = Double.NaN;

        try
        {
            doubleValue = Double.parseDouble( value );
        }
        catch( NumberFormatException x )
        {
            // In JRE 1.3 and JRE 1.2, the values "Infinity" and
            // "-Infinity" are not parsed to the appropriate typed
            // "value".
            if( "Infinity".equals( value ) )
            {
                doubleValue = Double.POSITIVE_INFINITY;
            }
            else if( "-Infinity".equals( value ) )
            {
                doubleValue = Double.NEGATIVE_INFINITY;
            }
            else
            {
                throw new IllegalArgumentException(
                    ThrowableHelper.getInternationalizedMessage(
                        TypeConverter.class,
                        "error.argument_not_parseable",
                        new String[] { value, "double" }
                        )
                    );
            }
        }
        catch( NullPointerException x )
        {
            throw new IllegalArgumentException(
                ThrowableHelper.getInternationalizedMessage(
                    TypeConverter.class,
                    "error.argument_is_null"
                    )
                );
        }

        return doubleValue;
    }

    /**
     * Returns a hexidecimal string representation of the specified
     * byte array.
     *
     * @param values <code>byte</code> array to be converted
     *
     * @return hexidecimal string representation of argument
     *
     * @pre null != values
     *
     * @post null != $result
     **/
    public static final String toHexString( byte[] values )
    {
        char[] buffer = new char[ 2 * values.length ];

        for( int i = 0; i < values.length; i++ )
        {
            buffer[ ( i << 1 ) ] = DIGITS[ ( values[ i ] & 0xf0 ) >> 4 ];
            buffer[ ( i << 1 ) + 1 ] = DIGITS[ ( values[ i ] & 0x0f ) ];
        }

        return new String( buffer );
    }

    /**
     * Converts a long value to a hexidecimal string.
     *
     * @param value long to be converted
     *
     * @return string representation of the long value
     *
     * @pre null != RCS_ID
     *
     * @post null != $result
     **/
    public static final String toHexString( long value )
    {
        return Long.toHexString( value );
    }

    /** Hexidecimal digits. */
    private static final char[] DIGITS =
    {
        '0', '1', '2', '3', '4', '5', '6', '7', 
        '8', '9', 'a', 'b', 'c', 'd', 'e', 'f'
    };
    
    /**
     * Private constructor.
     **/
    private TypeConverter() {}
}
