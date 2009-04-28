package org.jasig.portal.portlets;

import java.io.Serializable;

import org.apache.commons.collections.Factory;

public class BooleanAttributeFactory implements Factory, Serializable {
    private static final long serialVersionUID = 1L;

    public BooleanAttribute create() {
        return new BooleanAttribute();
    }
}