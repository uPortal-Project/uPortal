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

package org.jasig.portal.security.xslt;

/**
 * Interface for retrievieng localized messages with parameters that can be used in messages (in
 * format '{0}' or '{1,name}'). Unfortunately, since JAXP is used, we lose some of the Xalan API for
 * extention functions, hence it's hard to create acutal multi-argument extention functions,
 * therefore we're using seperate methods for functions with different arities. Currently up to 3
 * parameters are supported.
 * 
 * @author Eric Dalquist
 * @author Arvids Grabovskis
 * @version $Revision$
 */
public interface IXalanMessageHelper {
    
    public String getMessage(String code, String language);
    
    public String getMessage(String code, String language, String arg1);
    
    public String getMessage(String code, String language, String arg1, String arg2);
    
    public String getMessage(String code, String language, String arg1, String arg2, String arg3);
}
