/**
 * Licensed to Jasig under one or more contributor license
 * agreements. See the NOTICE file distributed with this work
 * for additional information regarding copyright ownership.
 * Jasig licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file
 * except in compliance with the License. You may obtain a
 * copy of the License at:
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on
 * an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

/*
 * Created on Oct 25, 2005
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package org.jasig.portal.channels.jsp.tree;

/**
 * If a tree is to support expanding and collapsing portions of the tree then
 * an implementation of this interface must be provided to the tree to translate
 * user actions into URLs suitable for the domain in which the tree is being
 * used and which will cause the appropriate methods on the appropriate tree
 * nodes to be called.
 * 
 * @author Mark Boyd
 * @deprecated All IChannel implementations should be migrated to portlets
 */
@Deprecated
public interface ITreeActionUrlResolver
{
    public static final int SHOW_CHILDREN = 0;
    public static final int HIDE_CHILDREN = 1;
    public static final int SHOW_ASPECTS = 2;
    public static final int HIDE_ASPECTS = 3;
    
    public String getTreeActionUrl(int type, String nodeId);
}
