package org.apereo.portal.dao.portletlist.jpa;

import com.fasterxml.jackson.annotation.JsonIgnore;
import javax.persistence.*;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;
import org.apereo.portal.dao.portletlist.IPortletListItem;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.hibernate.annotations.GenericGenerator;

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
            // Only allow sets of lists
            @UniqueConstraint(columnNames = {"LIST_ID", "LIST_ORDER", "ENTITY_ID"}),
            // Only allow sets of portlets in the list
            @UniqueConstraint(columnNames = {"LIST_ID", "ENTITY_ID"}),
        })
@Cacheable
@org.hibernate.annotations.Cache(usage = CacheConcurrencyStrategy.NONSTRICT_READ_WRITE)
@SuppressWarnings("unused")
public class PortletListItem implements IPortletListItem {

    @JsonIgnore
    @Id
    @GeneratedValue(generator = "UUID")
    @GenericGenerator(name = "UUID", strategy = "org.hibernate.id.UUIDGenerator")
    @Column(name = "ID", updatable = false, nullable = false)
    private String id;

    @JsonIgnore
    @ManyToOne(optional = false)
    @JoinColumn(name = "LIST_ID", referencedColumnName = "ID")
    private PortletList portletList;

    @JsonIgnore
    @Column(name = "LIST_ORDER", unique = false, nullable = false, updatable = true)
    private int listOrder;

    // This is generally the portlet fname, but could be adjusted in the future
    @Column(name = "ENTITY_ID", updatable = true, nullable = false)
    private String entityId;

    public PortletListItem() {
        // No-arg constructor for JSON mapping
    }

    public PortletListItem(String entityId) {
        this.entityId = entityId;
    }

    public String toString() {
        return "PortletListItem: id=["
                + id
                + "], portlet-list=["
                + (portletList == null ? "NULL" : portletList.getId())
                + "], order=["
                + listOrder
                + "], entity-id=["
                + entityId
                + "]";
    }
}
