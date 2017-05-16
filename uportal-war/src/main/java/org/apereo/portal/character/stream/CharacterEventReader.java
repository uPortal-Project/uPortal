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
package org.apereo.portal.character.stream;

import java.util.Iterator;
import org.apereo.portal.character.stream.events.CharacterEvent;

/**
 */
public interface CharacterEventReader extends Iterator<CharacterEvent> {
    /**
     * Check the next XMLEvent without reading it from the stream. Returns null if the stream is at
     * EOF or has no more XMLEvents. A call to peek() will be equal to the next return of next().
     */
    public CharacterEvent peek();

    /**
     * Frees any resources associated with this Reader. This method does not close the underlying
     * input source.
     */
    public void close();
}
