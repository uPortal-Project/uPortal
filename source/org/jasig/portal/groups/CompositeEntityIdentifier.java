/* Copyright 2002 The JA-SIG Collaborative.  All rights reserved.
*  See license distributed with this file and
*  available online at http://www.uportal.org/license.html
*/

package org.jasig.portal.groups;

import java.util.Properties;

import javax.naming.CompoundName;
import javax.naming.InvalidNameException;
import javax.naming.Name;
import javax.naming.NameParser;
import javax.naming.NamingException;
/**
 * A composite key and type that uniquely identify a portal entity.  The composite
 * key contains a service name, which may be compound, and a native key, which is
 * the key that identifies the entity in the local service.
 *
 * @author Dan Ellentuck
 * @version $Revision$
 */
public class CompositeEntityIdentifier extends org.jasig.portal.EntityIdentifier 
implements IGroupConstants
{
    // static vars:
    protected static Properties props;
    protected static String separator;
    static {
        props = new Properties();
        try 
            { separator = GroupServiceConfiguration.getConfiguration().getNodeSeparator(); }
        catch (Exception ex) 
            { separator = IGroupConstants.NODE_SEPARATOR; }
        props.put("jndi.syntax.separator", separator);
        props.put("jndi.syntax.direction", "left_to_right");
    }
    
    // instance key:
	protected Name compositeKey;
/**
 * @param entityKey java.lang.String
 * @param entityType java.lang.Class
 */
public CompositeEntityIdentifier(String entityKey, Class entityType) 
throws GroupsException
{
    super(entityKey, entityType);
    try
        { compositeKey = getParser().parse(entityKey); }
    catch (NamingException ne)
        { throw new GroupsException("Error in group key: " + ne.getMessage());}
}
/**
 * @return javax.naming.Name
 */
protected Name getCompositeKey() 
{
    return compositeKey;
}
/**
 * @return java.lang.String
 */
public String getKey() {
    return getCompositeKey().toString();
}
/**
 * @return java.lang.String
 */
public String getLocalKey() {
    return getCompositeKey().get(size() - 1).toString();
}
/**
 * @return javax.naming.NameParser
 */
protected static NameParser getParser() 
{
    return new NameParser() 
    {
        public Name parse(String s) throws InvalidNameException
        {
            int start = 0;
            int separatorLength = separator.length();
            int end = s.indexOf(separator, start);
            Name name = newCompoundName();
            while (end != -1)
            {
                name.add(s.substring(start,end));
	            start = end + separatorLength;
	            end = s.indexOf(separator, start);
            }
            return name.add(s.substring(start));
        }
    };
}
/**
 * If the composite key is either empty or has a single node, there is
 * no service name.
 * @return javax.naming.Name
 */
public Name getServiceName() 
{
    return ( size() < 2 ) ? null : getCompositeKey().getPrefix(size() - 1);
} 
/**
 * Returns a new empty CompoundName
 */
public static Name newCompoundName() throws InvalidNameException
{
    return new CompoundName("", props);
}
/**
 * @return String - the removed component
 */
public String popNode() throws InvalidNameException
{
    return (String)getCompositeKey().remove(0);
}
/**
 * @return javax.naming.Name
 */
public Name pushNode(String newNode) throws InvalidNameException
{
    return getCompositeKey().add(0,newNode);
}
/**
 * @param newCompositeKey javax.naming.Name
 */
public void setCompositeKey(Name newCompositeKey)
{
    compositeKey = newCompositeKey;
}
/**
 * @param newServiceName javax.naming.Name
 */
public void setServiceName(Name newServiceName) throws InvalidNameException
{
    Name newKey = newCompoundName().addAll(newServiceName).add(getLocalKey());
    setCompositeKey(newKey);
}
/**
 * @return int
 */
protected int size() 
{
    return getCompositeKey().size();
}
/**
 * Returns a String that represents the value of this object.
 * @return java.lang.String
 */
public String toString() {
    return "CompositeEntityIdentifier (" + type + "(" + getKey() + "))";

}

/**
 * Returns a CompoundName parsed from key
 */
public static Name parseCompoundKey(String key) throws NamingException
{
    return getParser().parse(key);
}
}
