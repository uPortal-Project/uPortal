/**
 * Licensed to Apereo under one or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information regarding copyright ownership. Apereo
 * licenses this file to you under the Apache License, Version 2.0 (the "License"); you may not use
 * this file except in compliance with the License. You may obtain a copy of the License at the
 * following location:
 *
 * <p>http://www.apache.org/licenses/LICENSE-2.0
 *
 * <p>Unless required by applicable law or agreed to in writing, software distributed under the
 * License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either
 * express or implied. See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apereo.portal.layout.dlm;

import org.w3c.dom.Document;

/**
 * Represents a ready-to-use DLM fragment and supports pluggable strategies for layout content.  The
 * {@link UserView} class has existed since the dawn of DLM, but this interface is a recent
 * addition.  Originally DLM supported only one strategy for layout content:  the layout owner's
 * persisted layout.  Refactoring the <code>UserView</code> into an interface-based abstraction
 * opens the door to pluggable strategies, such as metadata matching.
 *
 * @since 5.1
 */
public interface IUserView {

    int getUserId();

    Document getLayout();

    void setLayout(Document layout);

    int getLayoutId();

    void setLayoutId(int layoutId);

    int getProfileId();

    void setProfileId(int profileId);

}
