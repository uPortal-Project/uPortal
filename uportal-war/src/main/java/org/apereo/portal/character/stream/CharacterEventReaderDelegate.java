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

import org.apereo.portal.character.stream.events.CharacterEvent;

/**
 * This is the base class for deriving an CharacterEventReader filter.
 *
 * <p>This class is designed to sit between an CharacterEventReader and an application's
 * CharacterEventReader. By default each method does nothing but call the corresponding method on
 * the parent interface.
 *
 */
public class CharacterEventReaderDelegate implements CharacterEventReader {
    private final CharacterEventReader delegate;

    public CharacterEventReaderDelegate(CharacterEventReader delegate) {
        this.delegate = delegate;
    }

    /* (non-Javadoc)
     * @see java.util.Iterator#hasNext()
     */
    @Override
    public boolean hasNext() {
        return this.delegate.hasNext();
    }

    /* (non-Javadoc)
     * @see java.util.Iterator#next()
     */
    @Override
    public CharacterEvent next() {
        return this.delegate.next();
    }

    /* (non-Javadoc)
     * @see java.util.Iterator#remove()
     */
    @Override
    public void remove() {
        this.delegate.remove();
    }

    /* (non-Javadoc)
     * @see org.apereo.portal.character.stream.CharacterEventReader#peek()
     */
    @Override
    public CharacterEvent peek() {
        return this.delegate.peek();
    }

    /* (non-Javadoc)
     * @see org.apereo.portal.character.stream.CharacterEventReader#close()
     */
    @Override
    public void close() {
        this.delegate.close();
    }

    public CharacterEventReader getParent() {
        return delegate;
    }
}
