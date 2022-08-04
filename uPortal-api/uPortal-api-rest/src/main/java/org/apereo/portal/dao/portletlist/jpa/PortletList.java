package org.apereo.portal.dao.portletlist.jpa;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;
import org.apereo.portal.dao.portletlist.IPortletList;
import org.dom4j.Element;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.hibernate.annotations.GenericGenerator;
//import org.hibernate.annotations.NaturalIdCache;
import javax.persistence.*;
import java.util.UUID;

@Getter
@Setter
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@ToString
@Slf4j
@Entity
@Table(
    name = "UP_PORTLET_LIST",
    uniqueConstraints = { @UniqueConstraint(columnNames = { "user_id", "name" }) })
//@NaturalIdCache(
//    region =
//        "org.apereo.portal.groups.pags.dao.jpa.PersonAttributesGroupDefinitionImpl-NaturalId")
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

    @Override
    public void toElement(Element parent) {
        if (parent == null) {
            String msg = "Argument 'parent' cannot be null.";
            throw new IllegalArgumentException(msg);
        }

        parent.addElement("id").addText(this.getId().toString());
        parent.addElement("name").addText(this.getName());
        parent.addElement("userid").addText(this.getUserId());
//                    if (!members.isEmpty()) {
//                        org.dom4j.Element elementMembers = DocumentHelper.createElement(new QName("members"));
//                        for (IPersonAttributesGroupDefinition member : members) {
//                            elementMembers.addElement("member-name").addText(member.getName());
//                        }
//                        parent.add(elementMembers);
//                    }
    }
}
