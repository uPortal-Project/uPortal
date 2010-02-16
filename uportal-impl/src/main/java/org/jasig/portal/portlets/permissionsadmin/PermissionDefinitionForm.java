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
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jasig.portal.layout.dlm.remoting.JsonEntityBean;
import org.jasig.portal.security.IPermission;
import org.jasig.portal.security.IPermissionStore;
import org.jasig.portal.security.provider.RDBMPermissionImpl;
import org.jasig.portal.security.provider.RDBMPermissionImpl.PrincipalType;
import org.springframework.binding.message.MessageBuilder;
import org.springframework.binding.message.MessageContext;
import org.springframework.webflow.core.collection.ParameterMap;

public class PermissionDefinitionForm implements Serializable {
    
    // Static Members.
    private static final String ENTITY_OTHER = "other";
    private static final long serialVersionUID = 1L;

    // Instance Members.
    private String owner;
    private Map<JsonEntityBean,Type> principalsMap = Collections.emptyMap();
    private String activity;
    private JsonEntityBean target;
    private static final Log log = LogFactory.getLog(RDBMPermissionImpl.class);
    
    /*
     * Public API.
     */
    
    public enum Type {
        
        INHERIT,
        
        GRANT,
        
        DENY
        
    }
    
    public String getOwner() {
        return owner;
    }
    
    public void setOwner(String owner) {

        if (owner == null || owner.trim().length() == 0) {
            // This is a no-op;  let's keep null if that's what we have.
            return;
        }

        this.owner = owner;

    }

    public List<JsonEntityBean> getPrincipals() {
        return new ArrayList<JsonEntityBean>(principalsMap.keySet());
    }

    public void setPrincipals(List<JsonEntityBean> principals) {
        this.principalsMap = mergePrincipalsMap(this.principalsMap, principals);
    }
    
    public Map<JsonEntityBean,Type> getPrincipalsMap() {
        return new HashMap<JsonEntityBean,Type>(principalsMap);
    }

    public String getActivity() {
        return activity;
    }

    public void setActivity(String activity) {

        if (activity == null || activity.trim().length() == 0) {
            // This is a no-op;  let's keep null if that's what we have.
            return;
        }

        this.activity = activity;

    }

    public String getTarget() {
        return target != null ? target.getName() : null;
    }

    public void setTargetAsStringIfDifferent(String target) {
        
        if (target == null || target.trim().length() == 0) {
            // This is a no-op;  let's keep null if that's what we have.
            return;
        }
        
        if (this.target != null && target.equals(this.target.getName())) {
            // The UI is merely re-setting the previous choice;  we need to keep 
            // what we have b/c our existing version may be 'better' due to 
            // selection of group/channel/category in a sub-flow.
            return;
        }
        
        // This is a legitimate, string-based target selection.
        this.target = new JsonEntityBean();
        this.target.setEntityType(ENTITY_OTHER);
        this.target.setName(target);
        this.target.setId(target);

    }
    
    public void setTarget(List<JsonEntityBean> target) {

        // Assertions.
        if (target == null) {
            String msg = "Argument 'target' cannot be null";
            throw new IllegalArgumentException(msg);
        }

        this.target = target.size() > 0 ? target.get(0) : null;

    }
    
    public void setTypes(ParameterMap requestParameters) {
        
        // Assertions.
        if (requestParameters == null) {
            String msg = "Argument 'requestParameters' cannot be null";
            throw new IllegalArgumentException(msg);
        }
        
        for (Map.Entry<JsonEntityBean,Type> y : principalsMap.entrySet()) {
            JsonEntityBean principal = y.getKey();
            String val = requestParameters.get(principal.getId() + "_type");
            if (val != null) {
                Type type = Type.valueOf(val);
                y.setValue(type);
            } else {
                if (log.isWarnEnabled()) {
                    log.warn("No type parameter specified for the following principal:  name=" 
                                    + principal.getName() + ", Id=" + principal.getId());
                }
            }
        }

    }
    
    public boolean validateEditPermission(MessageContext msgs) {
        
        /*
         * All fields must be entered.
         */

        // owner
        if (owner == null) {
            msgs.addMessage(new MessageBuilder().error().source("owner")
                            .defaultText("Owner is required").build());
        }
        
        // principals
        if (principalsMap.isEmpty()) {
            msgs.addMessage(new MessageBuilder().error().source("principal")
                .defaultText("Specify one or more principals").build());
        }
        
        // activity
        if (activity == null) {
            msgs.addMessage(new MessageBuilder().error().source("activity")
                            .defaultText("Activity is required").build());
        }
        
        // target
        if (target == null) {
            msgs.addMessage(new MessageBuilder().error().source("target")
                            .defaultText("Target is required").build());
        }
        
        return msgs.getAllMessages().length == 0;
        
    }
    
    public boolean save(IPermissionStore store, MessageContext msgs) {

        List<IPermission> list = new ArrayList<IPermission>();
        for (Map.Entry<JsonEntityBean,Type> y : principalsMap.entrySet()) {
            Type type = y.getValue();
            if (Type.INHERIT.equals(type)) {
                // We don't persist INHERIT records (it's the default)
                continue;
            }
            JsonEntityBean principal = y.getKey();
            IPermission permission = store.newInstance(owner);
            permission.setPrincipal(PrincipalType.byEntityTypeName(principal.getEntityType()).toInt() 
                                        + RDBMPermissionImpl.PRINCIPAL_SEPARATOR 
                                        + principal.getId());
            permission.setType(type.name());
            permission.setActivity(activity);
            permission.setTarget(target.getId());
            list.add(permission);
        }
        
        boolean rslt = true;  // default
        try {
            store.add(list.toArray(new IPermission[0]));
        } catch (Throwable t) {
            log.error(t);
            msgs.addMessage(new MessageBuilder().error().source(null)
                    .defaultText("PermissionStore was unable to save changes").build());
            rslt = false;
        }
        
        return rslt;

    }

    /*
     * Private Stuff.
     */
    
    private Map<JsonEntityBean, Type> mergePrincipalsMap(Map<JsonEntityBean, Type> currentMap, List<JsonEntityBean> selections) {

        // Assertions.
        if (currentMap == null) {
            String msg = "Argument 'currentMap' cannot be null";
            throw new IllegalArgumentException(msg);
        }
        if (selections == null) {
            String msg = "Argument 'selections' cannot be null";
            throw new IllegalArgumentException(msg);
        }
        
        final Map<JsonEntityBean, Type> rslt = new HashMap<JsonEntityBean, Type>();
        
        for (JsonEntityBean principal : selections) {
            Type y = currentMap.containsKey(principal) 
                                    ? currentMap.get(principal) 
                                    : Type.GRANT;  // assume GRANT until told otherwise...
            rslt.put(principal, y);
        }
        
        return rslt;

    }

}
