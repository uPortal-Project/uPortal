package org.jasig.portal.i18n.dao.jpa;

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
import org.hibernate.annotations.Index;
import org.jasig.portal.i18n.Message;

@Entity
@Table(name = "UP_MESSAGE")
@SequenceGenerator(name = "UP_MESSAGE_GEN", sequenceName = "UP_MESSAGE_SEQ", allocationSize = 5)
@TableGenerator(name = "UP_MESSAGE_GEN", pkColumnValue = "UP_MESSAGE", allocationSize = 5)
@Cacheable
@Cache(usage = CacheConcurrencyStrategy.NONSTRICT_READ_WRITE)
public class MessageImpl implements Message, Serializable {
    
    private static final long serialVersionUID = 1L;
    
    @Id
    @GeneratedValue(generator = "UP_MESSAGE_GEN")
    @Column(name = "ID")
    private final long id = -1;
    
    @Column(name = "CODE", length = 128, nullable = false, unique = false)
    private String code;
    
    @Column(name = "VALUE", length = 1024, nullable = false, unique = false)
    private String value;
    
    @Column(name = "LOCALE", length = 64, nullable = false, unique = false)
    @Index(name = "UP_MESSAGE_CODE_LOCALE_IDX", columnNames = { "CODE", "LOCALE" })
    private String locale;
    
    public MessageImpl() {
    }
    
    public MessageImpl(String code, String locale, String value) {
        this.code = code;
        this.locale = locale;
        this.value = value;
    }
    
    @Override
    public String getCode() {
        return code;
    }
    
    @Override
    public void setCode(String code) {
        this.code = code;
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
    public String getLocale() {
        return locale;
    }
    
    @Override
    public void setLocale(String locale) {
        this.locale = locale;
    }
    
    /*
     * (non-Javadoc)
     * 
     * @see java.lang.Object#hashCode()
     */
    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((code == null) ? 0 : code.hashCode());
        return result;
    }
    
    /*
     * (non-Javadoc)
     * 
     * @see java.lang.Object#equals(java.lang.Object)
     */
    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        MessageImpl other = (MessageImpl) obj;
        if (code == null) {
            if (other.code != null)
                return false;
        } else if (!code.equals(other.code))
            return false;
        return true;
    }
    
    @Override
    public String toString() {
        return "MultilingualMessage [code=" + getCode() + ", value=" + value + "]";
    }
}
