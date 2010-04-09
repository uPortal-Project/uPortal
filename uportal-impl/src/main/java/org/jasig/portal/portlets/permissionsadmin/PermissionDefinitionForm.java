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

package org.jasig.portal.portlets.permissionsadmin;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

import org.jasig.portal.permission.IPermissionActivity;
import org.jasig.portal.permission.IPermissionOwner;
import org.jasig.portal.permission.target.IPermissionTarget;
import org.jasig.portal.portlets.Attribute;

/**
 * PermissionDefinitionForm represents important editing information for 
 * editing a set of permission activities on a specific target.
 * 
 * @author Drew Wills
 * @author Jen Bourey, jbourey@unicon.net
 * @version $Revision$
 */
public class PermissionDefinitionForm implements Serializable {

    private static final long serialVersionUID = 1L;

    // Instance Members.
    private IPermissionOwner owner;
    private IPermissionActivity activity;
    private IPermissionTarget target;

    private Map<String, String> permissions = new HashMap<String, String>();

    /*
     * Public API.
     */

    /**
     * Default constructor
     */
    public PermissionDefinitionForm() {
    }

    public IPermissionOwner getOwner() {
        return owner;
    }

    public void setOwner(IPermissionOwner owner) {
        this.owner = owner;
    }

    public IPermissionActivity getActivity() {
        return activity;
    }

    public void setActivity(IPermissionActivity activity) {
        this.activity = activity;
    }

    public IPermissionTarget getTarget() {
        return target;
    }

    public void setTarget(IPermissionTarget target) {
        this.target = target;
    }

    public Map<String, String> getPermissions() {
        return permissions;
    }

    public void setPermissions(Map<String, String> permissions) {
        this.permissions = permissions;
    }

}
