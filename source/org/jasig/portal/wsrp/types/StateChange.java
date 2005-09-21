/**
 * StateChange.java
 *
 * This file was auto-generated from WSDL
 * by the Apache Axis WSDL2Java emitter.
 */

package org.jasig.portal.wsrp.types;

/**
 * @deprecated As of uPortal 2.4, the WSRP producer in uPortal is no longer being maintained. If reintroduced, it will migrate to one based on WSRP4J.
 */
public class StateChange implements java.io.Serializable {
    private java.lang.String _value_;
    private static java.util.HashMap _table_ = new java.util.HashMap();

    // Constructor
    protected StateChange(java.lang.String value) {
        _value_ = value;
        _table_.put(_value_,this);
    }

    public static final java.lang.String _readWrite = "readWrite";
    public static final java.lang.String _cloneBeforeWrite = "cloneBeforeWrite";
    public static final java.lang.String _readOnly = "readOnly";
    public static final StateChange readWrite = new StateChange(_readWrite);
    public static final StateChange cloneBeforeWrite = new StateChange(_cloneBeforeWrite);
    public static final StateChange readOnly = new StateChange(_readOnly);
    public java.lang.String getValue() { return _value_;}
    public static StateChange fromValue(java.lang.String value)
          throws java.lang.IllegalStateException {
        StateChange enumeration = (StateChange)
            _table_.get(value);
        if (enumeration==null) throw new java.lang.IllegalStateException();
        return enumeration;
    }
    public static StateChange fromString(java.lang.String value)
          throws java.lang.IllegalStateException {
        return fromValue(value);
    }
    public boolean equals(java.lang.Object obj) {return (obj == this);}
    public int hashCode() { return toString().hashCode();}
    public java.lang.String toString() { return _value_;}
    public java.lang.Object readResolve() throws java.io.ObjectStreamException { return fromValue(_value_);}
}
