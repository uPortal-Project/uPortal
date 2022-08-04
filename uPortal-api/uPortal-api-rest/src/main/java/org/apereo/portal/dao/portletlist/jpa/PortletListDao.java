package org.apereo.portal.dao.portletlist.jpa;

import com.google.common.base.Function;
import lombok.extern.slf4j.Slf4j;
import org.apereo.portal.dao.portletlist.IPortletList;
import org.apereo.portal.dao.portletlist.IPortletListDao;
import org.apereo.portal.groups.pags.dao.IPersonAttributesGroupDefinition;
import org.apereo.portal.groups.pags.dao.jpa.PersonAttributesGroupDefinitionImpl;
import org.apereo.portal.groups.pags.dao.jpa.PersonAttributesGroupDefinitionImpl_;
import org.apereo.portal.jpa.BasePortalJpaDao;
import org.dom4j.Element;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Repository;

import javax.persistence.TypedQuery;
import javax.persistence.criteria.*;
import javax.portlet.Portlet;
import java.util.*;

@Slf4j
@Repository("portletListDao")
public class PortletListDao extends BasePortalJpaDao implements IPortletListDao {

    private CriteriaQuery<PortletList> portletListsByUserIdQuery;

    private CriteriaQuery<PortletList> portletListByUserIdAndPortletListUuidQuery;

    private ParameterExpression<String> userIdParameter;

    private ParameterExpression<String> portletListUuidParameter;

    @Override
    public void afterPropertiesSet() throws Exception {
        this.userIdParameter = this.createParameterExpression(String.class, "user_id");
        this.portletListUuidParameter = this.createParameterExpression(String.class, "id");

//        this.findAllDefinitionsQuery =
//            this.createCriteriaQuery(
//                new Function<
//                    CriteriaBuilder,
//                    CriteriaQuery<PersonAttributesGroupDefinitionImpl>>() {
//                    @Override
//                    public CriteriaQuery<PersonAttributesGroupDefinitionImpl> apply(
//                        CriteriaBuilder cb) {
//                        final CriteriaQuery<PersonAttributesGroupDefinitionImpl>
//                            criteriaQuery =
//                            cb.createQuery(
//                                PersonAttributesGroupDefinitionImpl.class);
//                        criteriaQuery.from(PersonAttributesGroupDefinitionImpl.class);
//                        return criteriaQuery;
//                    }
//                });

        this.portletListsByUserIdQuery =
            this.createCriteriaQuery(
                new Function<
                    CriteriaBuilder,
                    CriteriaQuery<PortletList>>() {
                    @Override
                    public CriteriaQuery<PortletList> apply(
                        CriteriaBuilder cb) {
                        final CriteriaQuery<PortletList>
                            criteriaQuery =
                            cb.createQuery(
                                PortletList.class);
                        Root<PortletList> root =
                            criteriaQuery.from(
                                PortletList.class);
                        criteriaQuery
                            .select(root)
                            .where(cb.equal(root.get("userId"), userIdParameter));
                        return criteriaQuery;
                    }
                });

        this.portletListByUserIdAndPortletListUuidQuery =
            this.createCriteriaQuery(
                new Function<
                    CriteriaBuilder,
                    CriteriaQuery<PortletList>>() {
                    @Override
                    public CriteriaQuery<PortletList> apply(
                        CriteriaBuilder cb) {
                        final CriteriaQuery<PortletList>
                            criteriaQuery =
                            cb.createQuery(
                                PortletList.class);
                        Root<PortletList> root =
                            criteriaQuery.from(
                                PortletList.class);
                        criteriaQuery
                            .select(root)
                            .where(cb.and(cb.equal(root.get("userId"), userIdParameter),
                                cb.equal(root.get("id"), portletListUuidParameter)));
                        return criteriaQuery;
                    }
                });
//
//        this.parentGroupDefinitionsQuery =
//            this.createCriteriaQuery(
//                new Function<
//                    CriteriaBuilder,
//                    CriteriaQuery<PersonAttributesGroupDefinitionImpl>>() {
//                    @Override
//                    public CriteriaQuery<PersonAttributesGroupDefinitionImpl> apply(
//                        CriteriaBuilder cb) {
//                        final CriteriaQuery<PersonAttributesGroupDefinitionImpl>
//                            criteriaQuery =
//                            cb.createQuery(
//                                PersonAttributesGroupDefinitionImpl.class);
//                        Root<PersonAttributesGroupDefinitionImpl> root =
//                            criteriaQuery.from(
//                                PersonAttributesGroupDefinitionImpl.class);
//                        Join<
//                            PersonAttributesGroupDefinitionImpl,
//                            PersonAttributesGroupDefinitionImpl>
//                            members =
//                            root.join(
//                                PersonAttributesGroupDefinitionImpl_
//                                    .members);
//                        criteriaQuery.where(
//                            cb.equal(
//                                members.get(
//                                    PersonAttributesGroupDefinitionImpl_.name),
//                                nameParameter));
//                        return criteriaQuery;
//                    }
//                });
    }

    @PortalTransactionalReadOnly
    @Override
    public List<IPortletList> getPortletLists(
        String userId) {
        TypedQuery<PortletList> query =
            this.createCachedQuery(portletListsByUserIdQuery);
        query.setParameter(userIdParameter, userId);
        List<IPortletList> entities = new ArrayList<>(query.getResultList());
        return entities;
    }

    @PortalTransactionalReadOnly
    @Override
    public IPortletList getPortletList(
        String userId,
        String portletListUuid) {
        TypedQuery<PortletList> query =
            this.createCachedQuery(portletListByUserIdAndPortletListUuidQuery);
        query.setParameter(userIdParameter, userId);
        query.setParameter(portletListUuidParameter, portletListUuid);

        List<PortletList> lists = query.getResultList();
        if(lists.size() < 1) {
            return null;
        } else if(lists.size() > 1) {
            log.error("Expected to up to 1 portlet list for user [{}] and portlet list uuid [{}], but found [{}].", userId, portletListUuid, lists.size());
            return null;
        }

        return lists.get(0);
    }

    @SuppressWarnings("unused")
    @PortalTransactional
    @Override
    public IPortletList createPortletList(IPortletList toCreate) {
        log.debug("Persisting portlet list [{}] for user [{}]", toCreate.getName(), toCreate.getUserId());
        try {
            this.getEntityManager().persist(toCreate);
        } catch (Exception e) {
            log.debug("Failed to persist portlet list", e);
            throw e;
        }
        log.debug("Finished persisting portlet list [{}] for user [{}]. ID = [{}]", toCreate.getName(), toCreate.getUserId(), toCreate.getId().toString());
        return toCreate;
    }
}
