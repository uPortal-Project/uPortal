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
package org.apereo.portal.version.dao.jpa;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;
import javax.persistence.TableGenerator;
import org.apereo.portal.version.AbstractVersion;
import org.apereo.portal.version.om.Version;
import org.hibernate.annotations.NaturalId;
import org.hibernate.annotations.NaturalIdCache;
import org.hibernate.annotations.Type;

@Entity
@Table(name = "UP_VERSION")
@SequenceGenerator(name = "UP_VERSION_GEN", sequenceName = "UP_VERSION_SEQ", allocationSize = 1)
@TableGenerator(name = "UP_VERSION_GEN", pkColumnValue = "UP_VERSION", allocationSize = 1)
@NaturalIdCache(region = "org.apereo.portal.version.dao.jpa.VersionImpl-NaturalId")
class VersionImpl extends AbstractVersion {
    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(generator = "UP_VERSION_GEN")
    @Column(name = "VERSION_ID")
    private final long id;

    @javax.persistence.Version
    @Column(name = "ENTITY_VERSION")
    private final long entityVersion;

    @NaturalId
    @Column(name = "PRODUCT", length = 128, nullable = false)
    @Type(type = "fname")
    private final String product;

    @Column(name = "MAJOR_VER", nullable = false)
    private int major;

    @Column(name = "MINOR_VER", nullable = false)
    private int minor;

    @Column(name = "PATCH_VER", nullable = false)
    private int patch;

    @Column(name = "LOCAL_VER")
    private Integer local;

    @SuppressWarnings("unused")
    private VersionImpl() {
        this.id = -1;
        this.entityVersion = -1;
        this.product = null;
    }

    public VersionImpl(String product, int major, int minor, int patch) {
        this.id = -1;
        this.entityVersion = -1;
        this.product = product;
        this.major = major;
        this.minor = minor;
        this.patch = patch;
    }

    public VersionImpl(String product, int major, int minor, int patch, Integer local) {
        this.id = -1;
        this.entityVersion = -1;
        this.product = product;
        this.major = major;
        this.minor = minor;
        this.patch = patch;
        this.local = local;
    }

    public VersionImpl(String product, Version version) {
        this.id = -1;
        this.entityVersion = -1;
        this.product = product;
        this.major = version.getMajor();
        this.minor = version.getMinor();
        this.patch = version.getPatch();
        this.local = version.getLocal();
    }

    @Override
    public int getMajor() {
        return this.major;
    }

    @Override
    public int getMinor() {
        return this.minor;
    }

    @Override
    public int getPatch() {
        return this.patch;
    }

    @Override
    public Integer getLocal() {
        return this.local;
    }

    public void setMajor(int major) {
        this.major = major;
    }

    public void setMinor(int minor) {
        this.minor = minor;
    }

    public void setPatch(int patch) {
        this.patch = patch;
    }

    public void setLocal(Integer local) {
        this.local = local;
    }
}
