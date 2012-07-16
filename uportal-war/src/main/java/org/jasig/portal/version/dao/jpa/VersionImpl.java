package org.jasig.portal.version.dao.jpa;

import java.io.Serializable;

import javax.persistence.Cacheable;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;
import javax.persistence.TableGenerator;

import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.hibernate.annotations.NaturalId;
import org.hibernate.annotations.NaturalIdCache;
import org.hibernate.annotations.Type;
import org.jasig.portal.version.om.Version;

@Entity
@Table(name = "UP_VERSION")
@SequenceGenerator(
        name="UP_VERSION_GEN",
        sequenceName="UP_VERSION_SEQ",
        allocationSize=1
    )
@TableGenerator(
        name="UP_VERSION_GEN",
        pkColumnValue="UP_VERSION",
        allocationSize=1
    )
@NaturalIdCache
@Cacheable
@Cache(usage = CacheConcurrencyStrategy.NONSTRICT_READ_WRITE)
class VersionImpl implements Version, Serializable {
    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(generator = "UP_VERSION_GEN")
    @Column(name = "OWNER_ID")
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
    
    public void setMajor(int major) {
        this.major = major;
    }

    public void setMinor(int minor) {
        this.minor = minor;
    }

    public void setPatch(int patch) {
        this.patch = patch;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + major;
        result = prime * result + minor;
        result = prime * result + patch;
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        VersionImpl other = (VersionImpl) obj;
        if (major != other.major)
            return false;
        if (minor != other.minor)
            return false;
        if (patch != other.patch)
            return false;
        return true;
    }

    @Override
    public String toString() {
        return product + " " + major + "." + minor + "." + patch;
    }
}
