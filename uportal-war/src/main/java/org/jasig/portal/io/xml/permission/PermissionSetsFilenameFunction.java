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

package org.jasig.portal.io.xml.permission;

import java.util.regex.Pattern;

import org.dom4j.Node;
import org.jasig.portal.utils.Tuple;

import com.google.common.base.Function;

/**
 * Generates the correct file name for a Permission Set
 * 
 * @author Eric Dalquist
 * @version $Revision$
 */
public class PermissionSetsFilenameFunction implements Function<Tuple<String, Node>, String> {
    public static final Pattern SPLIT = Pattern.compile("\\|");
    
    public static String[] splitKey(String key) {
        final String[] keyParts = SPLIT.split(key);
        if (keyParts.length != 5) {
            throw new IllegalArgumentException("PermissionSets key must have five parts");
        }
        
        return keyParts;
    }
    
    @Override
    public String apply(Tuple<String, Node> data) {
        final String[] keyParts = splitKey(data.first);
        
        final Node node = data.second.selectSingleNode("/permission-set/principal/child::node()/child::text()");
        final String principal = node.getText();
        return principal + "__" + keyParts[3] + "__" + keyParts[0];
    }
}
