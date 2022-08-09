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
package org.apereo.portal.dao.portletlist.jpa;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Getter;
import lombok.Setter;
import org.apereo.portal.dao.portletlist.IPortletList;
import org.hibernate.annotations.Cascade;

import javax.persistence.*;
import java.io.Serializable;

@Getter
@Setter
@SecondaryTable(name = "UP_PORTLET_LIST")
@Embeddable
@JsonIgnoreProperties({ "portletList" })
public class PortletListItemPK implements Serializable {

    @ManyToOne(optional = false)
    @JoinColumn(
        name = "LIST_ID",
        referencedColumnName = "ID")
    protected PortletList portletList;

    @Column(
        name = "LIST_ORDER",
        unique = false,
        nullable = false,
        insertable = true,
        updatable = true)
    protected int listOrder;

    /** Empty constructor is needed for Serializable */
    public PortletListItemPK() {};

    public PortletListItemPK(PortletList portletList, int listOrder) {
        this.portletList = portletList;
        this.listOrder = listOrder;
    }

//    @Override
//    public boolean equals(Object obj) {
//        if (obj == null || !(obj instanceof org.apereo.portal.portlet.dao.jpa.MarketplaceRatingPK)) {
//            return false;
//        } else if (obj == this) {
//            return true;
//        }
//        org.apereo.portal.portlet.dao.jpa.MarketplaceRatingPK tempRating = (org.apereo.portal.portlet.dao.jpa.MarketplaceRatingPK) obj;
//        return new EqualsBuilder()
//            .append(userName, tempRating.userName)
//            .append(portletDefinition, tempRating.portletDefinition)
//            .isEquals();
//    }
//
//    @Override
//    public int hashCode() {
//        return new HashCodeBuilder(17, 31)
//            . // two randomly chosen prime numbers
//                append(userName)
//            .append(portletDefinition)
//            .toHashCode();
//    }
//
//    @Override
//    public String toString() {
//        StringBuilder builder = new StringBuilder();
//        builder.append("User: ");
//        builder.append(this.userName);
//        builder.append("\n");
//        builder.append("Portlet: ");
//        builder.append(this.portletDefinition);
//        return builder.toString();
//    }
}
