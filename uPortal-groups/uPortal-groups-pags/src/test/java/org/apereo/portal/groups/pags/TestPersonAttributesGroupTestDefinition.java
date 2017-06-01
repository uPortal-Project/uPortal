package org.apereo.portal.groups.pags;

import org.apereo.portal.groups.pags.dao.IPersonAttributesGroupTestDefinition;
import org.apereo.portal.groups.pags.dao.IPersonAttributesGroupTestGroupDefinition;
import org.dom4j.Element;

/**
 * Supports instantiating {@link IPersonTester} objects in unit tests.
 */
public final class TestPersonAttributesGroupTestDefinition
        implements IPersonAttributesGroupTestDefinition {

    private final String attributeName;
    private final String testValue;

    public TestPersonAttributesGroupTestDefinition(String attributeName, String testValue) {
        this.attributeName = attributeName;
        this.testValue = testValue;
    }

    @Override
    public long getId() {
        throw new UnsupportedOperationException();
    }

    @Override
    public String getAttributeName() {
        return attributeName;
    }

    @Override
    public void setAttributeName(String attributeName) {
        throw new UnsupportedOperationException();
    }

    @Override
    public String getTesterClassName() {
        throw new UnsupportedOperationException();
    }

    @Override
    public void setTesterClassName(String className) {
        throw new UnsupportedOperationException();
    }

    @Override
    public String getTestValue() {
        return testValue;
    }

    @Override
    public void setTestValue(String testValue) {
        throw new UnsupportedOperationException();
    }

    @Override
    public IPersonAttributesGroupTestGroupDefinition getTestGroup() {
        throw new UnsupportedOperationException();
    }

    @Override
    public void setTestGroup(IPersonAttributesGroupTestGroupDefinition testGroup) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void toElement(Element parent) {
        throw new UnsupportedOperationException();
    }

}
