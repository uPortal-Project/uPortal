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

package org.jasig.portal.layout.dlm.processing;

/**
 * An interface implemented along with IParameterProcessor to convey if a call
 * to IParameterProcessor's processParameters() method has resulted in a
 * processor completing its functionality and hence can be removed from being a
 * currently selected optional processor. This can alleviate a requirement of
 * sending a specific URL solely to remove a processor that has completed its
 * task. Once selected, an optional processor remains as the currently selected
 * processor for all future HTTP requests until it conveys that it is finished
 * via implementing this interface or it is replaced by a subsequent HTTP
 * request including the uP_dlmPrc parameter.
 *
 * @author Mark Boyd
 *
 */
public interface IOptionalParameterProcessor
{
    /**
     * Answers whether a currently selected optional parameter processor has
     * completed its work and can be removed from being the selected optional
     * processor.
     *
     * @return boolean
     */
    public boolean isFinished();
}
