/* Copyright © 2002 The JA-SIG Collaborative.  All rights reserved.
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
