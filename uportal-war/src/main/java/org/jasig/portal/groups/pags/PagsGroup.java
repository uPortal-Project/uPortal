/**
 * Licensed to Apereo under one or more contributor license
 * agreements. See the NOTICE file distributed with this work
 * for additional information regarding copyright ownership.
 * Apereo licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file
 * except in compliance with the License.  You may obtain a
 * copy of the License at the following location:
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.jasig.portal.groups.pags;

import java.util.Iterator;
import java.util.List;
import java.util.Vector;

import org.jasig.portal.security.IPerson;

public class PagsGroup {

    private String key;
    private String name;
    private String description;
    private List<String> members;
    private List<TestGroup> testGroups;
    
    public PagsGroup() {
        members = new Vector<String>();
        testGroups = new Vector<TestGroup>();
    }
    
    public void setKey(String key) {
        this.key = key;
    }
    public String getKey() {
        return key;
    }
    
    public void setName(String name) {
        this.name = name;
    }
    public String getName() {
        return name;
    }
    
    public void setDescription(String description) {
        this.description = description;
    }
    public String getDescription() {
        return description;
    }
    public void addMember(String key) {
        members.add(key);
    }
    public boolean hasMember(String key) {
        return members.contains(key);
    }
    public void addTestGroup(TestGroup testGroup) {
        testGroups.add(testGroup);
    }
    public boolean contains(IPerson person) {
        return ( testGroups.isEmpty() ) ? false : test(person);
    }
    public boolean test(IPerson person) {
        if (testGroups.isEmpty())
             return true;
        for (Iterator<TestGroup> i = testGroups.iterator(); i.hasNext(); ) {
            TestGroup testGroup = i.next();
            if (testGroup.test(person)) {
                return true;
            }
        }
        return false;
    }
    public String toString() {
         return "GroupDefinition " + key + " (" + name + ")";
    }

    public List<String> getMembers() {
        return members;
    }

    public List<TestGroup> getTestGroups() {
        return testGroups;
    }
}
