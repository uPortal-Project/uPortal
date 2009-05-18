package org.jasig.portal.portlets;

import java.io.Serializable;

import org.apache.commons.collections.Factory;

public class StringListAttributeFactory implements Factory, Serializable {
    private static final long serialVersionUID = 1L;

    public StringListAttribute create() {
        return new StringListAttribute();
    }
}