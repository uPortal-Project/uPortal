package org.jasig.portal.groups;

/**
 * Copyright (c) 2001 The JA-SIG Collaborative.  All rights reserved.
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
 
 /**
 * An interface for retrieving <code>IEntities</code>.
 * @author Dan Ellentuck
 * @version 1.0, 11/29/01
 */
public interface IEntityStore {
/**
 * Returns an <code>Iterator</code> over the <code>IEntities</code> that are members of 
 * the <code>IEntityGroup</code>.
 * @return java.util.Iterator
 * @param group org.jasig.portal.groups.IEntityGroup
 * @deprecated
 * replaced by <code>IEntityGroupStore.findEntitiesForGroup(IEntityGroup)</code>.
 */
java.util.Iterator findEntitiesForGroup(IEntityGroup group) throws GroupsException;
/**
 * @return org.jasig.portal.groups.IEntity
 * @param key java.lang.String
 */
IEntity newInstance(String key) throws GroupsException;
/**
 * @return org.jasig.portal.groups.IEntity
 * @param key java.lang.String - the entity's key
 * @param type java.lang.Class - the entity's Type
 */
IEntity newInstance(String key, Class type) throws GroupsException;
}
