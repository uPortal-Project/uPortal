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
package org.apereo.portal.i18n.dao.jpa;

import java.io.Serializable;
import java.util.Locale;
import javax.persistence.Cacheable;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;
import javax.persistence.TableGenerator;
import org.apereo.portal.i18n.Message;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.hibernate.annotations.Index;
import org.hibernate.annotations.NaturalId;
import org.springframework.util.Assert;

@Entity
@Table(name = "UP_MESSAGE")
@SequenceGenerator(name = "UP_MESSAGE_GEN", sequenceName = "UP_MESSAGE_SEQ", allocationSize = 5)
@TableGenerator(name = "UP_MESSAGE_GEN", pkColumnValue = "UP_MESSAGE", allocationSize = 5)
@Cacheable
@Cache(usage = CacheConcurrencyStrategy.NONSTRICT_READ_WRITE)
class MessageImpl implements Message, Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(generator = "UP_MESSAGE_GEN")
    @Column(name = "ID")
    private final long id = -1;

    @NaturalId
    @Column(name = "CODE", length = 128, nullable = false)
    @Index(name = "IDX_MESSAGE__CODE")
    private final String code;

    @NaturalId
    @Column(name = "LOCALE", length = 64, nullable = false)
    @Index(name = "IDX_MESSAGE__LOCALE")
    private final Locale locale;

    @Column(name = "VALUE", length = 1024, nullable = false)
    private String value;

    /** Used via reflection */
    @SuppressWarnings("unused")
    private MessageImpl() {
        this.code = null;
        this.locale = null;
    }

    MessageImpl(String code, Locale locale, String value) {
        Assert.notNull(code);
        Assert.notNull(locale);
        Assert.notNull(value);

        this.code = code;
        this.locale = locale;
        this.value = value;
    }

    @Override
    public String getCode() {
        return code;
    }

    @Override
    public String getValue() {
        return value;
    }

    @Override
    public void setValue(String value) {
        this.value = value;
    }

    @Override
    public long getId() {
        return id;
    }

    @Override
    public Locale getLocale() {
        return locale;
    }

    /* (non-Javadoc)
     * @see java.lang.Object#hashCode()
     */
    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((this.code == null) ? 0 : this.code.hashCode());
        result = prime * result + ((this.locale == null) ? 0 : this.locale.hashCode());
        return result;
    }

    /* (non-Javadoc)
     * @see java.lang.Object#equals(java.lang.Object)
     */
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null) return false;
        if (obj instanceof Message) return false;
        Message other = (Message) obj;
        if (this.code == null) {
            if (other.getCode() != null) return false;
        } else if (!this.code.equals(other.getCode())) return false;
        if (this.locale == null) {
            if (other.getLocale() != null) return false;
        } else if (!this.locale.equals(other.getLocale())) return false;
        return true;
    }

    /* (non-Javadoc)
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        return "MessageImpl [id="
                + this.id
                + ", locale="
                + this.locale
                + ", code="
                + this.code
                + ", value="
                + this.value
                + "]";
    }
}
