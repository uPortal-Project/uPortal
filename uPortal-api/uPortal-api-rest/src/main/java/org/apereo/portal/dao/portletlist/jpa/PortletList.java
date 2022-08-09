package org.apereo.portal.dao.portletlist.jpa;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;
import org.apereo.portal.dao.portletlist.IPortletList;
import org.apereo.portal.dao.portletlist.IPortletListItem;
import org.dom4j.Element;
import org.hibernate.annotations.*;

import javax.persistence.*;
import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.OrderBy;
import javax.persistence.Table;
import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@ToString
@Slf4j
@Entity
@Table(
    name = "UP_PORTLET_LIST",
    uniqueConstraints = { @UniqueConstraint(columnNames = { "user_id", "name" }) })
@Cacheable
@org.hibernate.annotations.Cache(usage = CacheConcurrencyStrategy.NONSTRICT_READ_WRITE)
@SuppressWarnings("unused")
public class PortletList implements IPortletList {

    @Id
    @GeneratedValue(generator = "UUID")
    @GenericGenerator(
        name = "UUID",
        strategy = "org.hibernate.id.UUIDGenerator"
    )
    @Column(name = "id", updatable = false, nullable = false)
    private String id;

    @Column(name = "user_id", updatable = true, nullable = false)
    private String userId;

    @Column(name = "name", updatable = true, nullable = false)
    private String name;

    @OneToMany(
        targetEntity = PortletListItem.class,
        cascade = CascadeType.REMOVE,
        fetch = FetchType.EAGER,
        mappedBy = "portletListItemPK.portletList")
    @org.hibernate.annotations.Cache(usage = CacheConcurrencyStrategy.NONSTRICT_READ_WRITE)
    @Fetch(FetchMode.SELECT) // FM JOIN does BAD things to collections that support duplicates
    @OrderBy("LIST_ORDER ASC")
    private List<PortletListItem> items = new ArrayList<>();

    @Override
    public void toElement(Element parent) {
        if (parent == null) {
            String msg = "Argument 'parent' cannot be null.";
            throw new IllegalArgumentException(msg);
        }

        parent.addElement("id").addText(this.getId().toString());
        parent.addElement("name").addText(this.getName());
        parent.addElement("userid").addText(this.getUserId());
        parent.addElement("items").addText("" + this.getItems().size());

    }

    public void overrideItems(List<PortletListItem> items) {
        this.items.clear();
        int order = 0;
        for(PortletListItem item : items) {
            item.setPortletListItemPK(new PortletListItemPK(this, order++));
            this.items.add(item);
        }
    }
}
