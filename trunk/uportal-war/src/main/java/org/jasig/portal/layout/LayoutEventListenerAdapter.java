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

package org.jasig.portal.layout;

/**
 * Basic impl of LayoutEventListener which does nothing.
 * 
 * @author Eric Dalquist
 * @version $Revision$
 */
public class LayoutEventListenerAdapter implements LayoutEventListener {

    /* (non-Javadoc)
     * @see org.jasig.portal.layout.LayoutEventListener#channelAdded(org.jasig.portal.layout.LayoutEvent)
     */
    @Override
    public void channelAdded(LayoutEvent ev) {
    }

    /* (non-Javadoc)
     * @see org.jasig.portal.layout.LayoutEventListener#channelUpdated(org.jasig.portal.layout.LayoutEvent)
     */
    @Override
    public void channelUpdated(LayoutEvent ev) {
    }

    /* (non-Javadoc)
     * @see org.jasig.portal.layout.LayoutEventListener#channelMoved(org.jasig.portal.layout.LayoutMoveEvent)
     */
    @Override
    public void channelMoved(LayoutMoveEvent ev) {
        
    }

    /* (non-Javadoc)
     * @see org.jasig.portal.layout.LayoutEventListener#channelDeleted(org.jasig.portal.layout.LayoutMoveEvent)
     */
    @Override
    public void channelDeleted(LayoutMoveEvent ev) {
        
    }

    /* (non-Javadoc)
     * @see org.jasig.portal.layout.LayoutEventListener#folderAdded(org.jasig.portal.layout.LayoutEvent)
     */
    @Override
    public void folderAdded(LayoutEvent ev) {
        
    }

    /* (non-Javadoc)
     * @see org.jasig.portal.layout.LayoutEventListener#folderUpdated(org.jasig.portal.layout.LayoutEvent)
     */
    @Override
    public void folderUpdated(LayoutEvent ev) {
        
    }

    /* (non-Javadoc)
     * @see org.jasig.portal.layout.LayoutEventListener#folderMoved(org.jasig.portal.layout.LayoutMoveEvent)
     */
    @Override
    public void folderMoved(LayoutMoveEvent ev) {
        
    }

    /* (non-Javadoc)
     * @see org.jasig.portal.layout.LayoutEventListener#folderDeleted(org.jasig.portal.layout.LayoutMoveEvent)
     */
    @Override
    public void folderDeleted(LayoutMoveEvent ev) {
        
    }

    /* (non-Javadoc)
     * @see org.jasig.portal.layout.LayoutEventListener#layoutLoaded()
     */
    @Override
    public void layoutLoaded() {
        
    }

    /* (non-Javadoc)
     * @see org.jasig.portal.layout.LayoutEventListener#layoutSaved()
     */
    @Override
    public void layoutSaved() {
        
    }

}
