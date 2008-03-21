@org.hibernate.annotations.TypeDefs( {
    @org.hibernate.annotations.TypeDef(name="nullSafeText", typeClass=org.jasig.portal.dao.usertype.EscapedTextType.class),
    @org.hibernate.annotations.TypeDef(name="nullSafeString", typeClass=org.jasig.portal.dao.usertype.EscapedStringType.class)
} ) 
package org.jasig.portal.portlet.dao.jpa;
