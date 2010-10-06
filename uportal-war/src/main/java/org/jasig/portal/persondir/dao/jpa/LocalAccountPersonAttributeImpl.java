package org.jasig.portal.persondir.dao.jpa;

import java.io.Serializable;
import java.util.List;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.Table;

import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;
import org.hibernate.annotations.Cascade;
import org.hibernate.annotations.CollectionOfElements;
import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.IndexColumn;
import org.hibernate.annotations.Parameter;
import org.hibernate.annotations.Type;

@Entity
@Table(name = "UP_PERSON_ATTR")
@GenericGenerator(name = "UP_PERSON_ATTR_GEN", strategy = "native", parameters = {
        @Parameter(name = "sequence", value = "UP_PERSON_ATTR_SEQ"),
        @Parameter(name = "table", value = "UP_JPA_UNIQUE_KEY"),
        @Parameter(name = "column", value = "NEXT_UP_PERSON_ATTR_HI") })
public class LocalAccountPersonAttributeImpl implements Serializable {
    
    @Id
    @GeneratedValue(generator = "UP_PERSON_ATTR_GEN")
    private int id;
    
    @Column(name = "ATTR_NAME", nullable = false)
    private String name;
    
    @CollectionOfElements(fetch = FetchType.EAGER)
    @JoinTable(
        name = "UP_PERSON_ATTR_VALUES",
        joinColumns = @JoinColumn(name = "ATTR_ID")
    )
    @IndexColumn(name = "VALUE_ORDER")
    @Type(type = "nullSafeText")
    @Column(name = "ATTR_VALUE")
    @Cascade( { org.hibernate.annotations.CascadeType.DELETE_ORPHAN, org.hibernate.annotations.CascadeType.ALL })
    private List<String> values = null;

    @SuppressWarnings("unused")
    private LocalAccountPersonAttributeImpl() { 
    }
    
    public LocalAccountPersonAttributeImpl(String name, List<String> values) {
        this.name = name;
        this.values = values;
    }
    
    public List<String> getValues() {
        return values;
    }

    public void setValues(List<String> values) {
        this.values = values;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
    
    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((name == null) ? 0 : name.hashCode());
        result = prime * result + ((values == null) ? 0 : values.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (!(obj instanceof LocalAccountPersonAttributeImpl)) {
            return false;
        }
        LocalAccountPersonAttributeImpl other = (LocalAccountPersonAttributeImpl) obj;
        if (name == null) {
            if (other.name != null) {
                return false;
            }
        }
        else if (!name.equals(other.name)) {
            return false;
        }
        if (values == null) {
            if (other.values != null) {
                return false;
            }
        }
        else if (!values.equals(other.values)) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this, ToStringStyle.SHORT_PREFIX_STYLE)
        .append("id", this.id)
        .append("name", this.name)
        .append("values", this.values)
        .toString();
    }

}
