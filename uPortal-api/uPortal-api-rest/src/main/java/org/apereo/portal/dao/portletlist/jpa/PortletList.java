package org.apereo.portal.dao.portletlist.jpa;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;
import org.apereo.portal.dao.portletlist.IPortletList;
import org.apereo.portal.rest.utils.InputValidator;
import org.apereo.portal.security.IPerson;
import org.dom4j.Element;
import org.hibernate.annotations.*;
import org.springframework.util.StringUtils;

import javax.persistence.*;
import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.OrderBy;
import javax.persistence.Table;
import javax.portlet.Portlet;
import java.sql.Timestamp;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

@Getter
@Setter
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@Slf4j
@Entity
@Table(
    name = "UP_PORTLET_LIST",
    uniqueConstraints = { @UniqueConstraint(columnNames = { "OWNER_USERNAME", "NAME" }) })
@Cacheable
@org.hibernate.annotations.Cache(usage = CacheConcurrencyStrategy.NONSTRICT_READ_WRITE)
@SuppressWarnings("unused")
public class PortletList implements IPortletList {
    private static final ZoneId tz = ZoneId.systemDefault();
    private static final String AUDIT_DATE_FORMAT = "yyyy-MM-dd hh:mm:ss Z";

    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    @Id
    @GeneratedValue(generator = "UUID")
    @GenericGenerator(
        name = "UUID",
        strategy = "org.hibernate.id.UUIDGenerator"
    )
    @Column(name = "ID", updatable = false, nullable = false)
    private String id;

    @Column(name = "OWNER_USERNAME", updatable = true, nullable = false)
    private String ownerUsername;

    @Column(name = "NAME", updatable = true, nullable = false)
    private String name;

    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    @Column(name = "CREATED_BY", updatable = false, nullable = false)
    private String createdBy;

    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    @JsonFormat
        (shape = JsonFormat.Shape.STRING, pattern = AUDIT_DATE_FORMAT)
    @Column(name = "CREATED_ON", updatable = false, nullable = false)
    private Timestamp createdOn;

    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    @Column(name = "UPDATED_BY", updatable = true, nullable = false)
    private String updatedBy;

    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    @JsonFormat
        (shape = JsonFormat.Shape.STRING, pattern = AUDIT_DATE_FORMAT)
    @Column(name = "UPDATED_ON", updatable = true, nullable = false)
    private Timestamp updatedOn;

    @OneToMany(
        targetEntity = PortletListItem.class,
        cascade = CascadeType.ALL,
        fetch = FetchType.EAGER,
        mappedBy = "portletList",
        orphanRemoval = true)
    @org.hibernate.annotations.Cache(usage = CacheConcurrencyStrategy.NONSTRICT_READ_WRITE)
    @Fetch(FetchMode.SELECT) // FM JOIN does BAD things to collections that support duplicates
    @OrderBy("LIST_ORDER ASC")
    private List<PortletListItem> items;

    @Override
    public void toElement(Element parent) {
        if (parent == null) {
            String msg = "Argument 'parent' cannot be null.";
            throw new IllegalArgumentException(msg);
        }

        parent.addElement("id").addText(this.getId().toString());
        parent.addElement("name").addText(this.getName());
        parent.addElement("ownerUsername").addText(this.getOwnerUsername());
        parent.addElement("items").addText("" + this.getItems().size());

    }

    public void clearAndSetItems(List<PortletListItem> items) {
        if(this.items == null) {
            this.items = new ArrayList<>();
        }

        // Index all current items
        HashMap<String, PortletListItem> existingItems = new HashMap<>();
        for(PortletListItem existingItem : this.items) {
            existingItems.put(existingItem.getEntityId(), existingItem);
        }

        this.items.clear();

        for(PortletListItem item : items) {
            PortletListItem existingItem = existingItems.get(item.getEntityId());
            if(existingItem != null) {
                // If any item specific attributes are configured, specifically copy them over here.
                // Order will be set in prepareForPersistence()
                existingItem.setListOrder(-1);
                this.items.add(existingItem);
            } else {
                this.items.add(item);
            }
        }
    }

    /**
     * Final step before letting the object be persisted or merged via the entity manager.
     *
     * Validation is in part, a fail-safe to ensure SQL injections are checked.
     * @param requester
     */
    public void prepareForPersistence(IPerson requester) {
        if(!StringUtils.isEmpty(this.name)) {
            InputValidator.validateAsWordCharacters(this.name, "name");
        }

        if(!StringUtils.isEmpty(this.ownerUsername)) {
            InputValidator.validateAsWordCharacters(this.ownerUsername, "ownerUsername");
        }

        if(this.items != null) {
            int order = 0;
            for (PortletListItem item : this.items) {
                InputValidator.validateAsWordCharacters(item.getEntityId(), "items > entityId");
                item.setPortletList(this);
                item.setListOrder(order++);
            }
        }

        // Set / Update audit fields
        if(this.createdOn == null) {
            this.createdOn = this.updatedOn = Timestamp.valueOf(LocalDateTime.now(tz));
            this.createdBy = this.updatedBy = requester.getUserName();
        } else {
            this.updatedOn = Timestamp.valueOf(LocalDateTime.now(tz));
            this.updatedBy = requester.getUserName();
        }
    }

    public String toString() {
        StringBuffer sb = new StringBuffer();
        sb.append("PortletList id=[");
        sb.append(id);
        sb.append("], name=[");
        sb.append(name);
        sb.append("], owner=[");
        sb.append(ownerUsername);
        sb.append("], items=: ");
        if(this.items.size() < 1) {
            sb.append("[Currently no items]");
        } else {
            final int size = items.size();
            for (int i = 0; i < size; i++) {
                PortletListItem item = items.get(i);
                sb.append(item);
                if(i < size - 1) {
                    sb.append("; ");
                }
            }
        }
        return sb.toString();
    }
}
