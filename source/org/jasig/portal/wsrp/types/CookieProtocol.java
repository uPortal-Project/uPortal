/**
 * CookieProtocol.java
 *
 * This file was auto-generated from WSDL
 * by the Apache Axis WSDL2Java emitter.
 */

package org.jasig.portal.wsrp.types;

/**
 * @deprecated As of uPortal 2.4, the WSRP producer in uPortal is no longer being maintained. If reintroduced, it will migrate to one based on WSRP4J.
 */
public class CookieProtocol implements java.io.Serializable {
    private java.lang.String _value_;
    private static java.util.HashMap _table_ = new java.util.HashMap();

    // Constructor
    protected CookieProtocol(java.lang.String value) {
        _value_ = value;
        _table_.put(_value_,this);
    }

    public static final java.lang.String _none = "none";
    public static final java.lang.String _perUser = "perUser";
    public static final java.lang.String _perGroup = "perGroup";
    public static final CookieProtocol none = new CookieProtocol(_none);
    public static final CookieProtocol perUser = new CookieProtocol(_perUser);
    public static final CookieProtocol perGroup = new CookieProtocol(_perGroup);
    public java.lang.String getValue() { return _value_;}
    public static CookieProtocol fromValue(java.lang.String value)
          throws java.lang.IllegalStateException {
        CookieProtocol enum = (CookieProtocol)
            _table_.get(value);
        if (enum==null) throw new java.lang.IllegalStateException();
        return enum;
    }
    public static CookieProtocol fromString(java.lang.String value)
          throws java.lang.IllegalStateException {
        return fromValue(value);
    }
    public boolean equals(java.lang.Object obj) {return (obj == this);}
    public int hashCode() { return toString().hashCode();}
    public java.lang.String toString() { return _value_;}
    public java.lang.Object readResolve() throws java.io.ObjectStreamException { return fromValue(_value_);}
}
