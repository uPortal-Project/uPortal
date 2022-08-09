package org.apereo.portal.dao.portletlist.jpa;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;
import org.apereo.portal.dao.portletlist.IPortletListItem;
import org.hibernate.annotations.CacheConcurrencyStrategy;

import javax.persistence.*;

@Getter
@Setter
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@ToString
@Slf4j
@Entity
@Table(
    // This is ONLY to be used as part of a portlet list, so not specifying a PK
    name = "UP_PORTLET_LIST_ITEM",
    uniqueConstraints = {
        // These are sets of lists
        @UniqueConstraint(columnNames = { "LIST_ID", "ENTITY_ID" }),
    })
@Cacheable
@org.hibernate.annotations.Cache(usage = CacheConcurrencyStrategy.NONSTRICT_READ_WRITE)
@SuppressWarnings("unused")
public class PortletListItem implements IPortletListItem {

    @JsonIgnore
    @EmbeddedId
    private PortletListItemPK portletListItemPK;

    // This is generally the portlet fname, but could be adjusted in the future
    @Column(name = "ENTITY_ID", updatable = true, nullable = false)
    private String entityId;

    public PortletListItem() {
        // No-arg constructor for JSON mapping
    }

    public PortletListItem(String entityId) {
        this.entityId = entityId;
    }
}
