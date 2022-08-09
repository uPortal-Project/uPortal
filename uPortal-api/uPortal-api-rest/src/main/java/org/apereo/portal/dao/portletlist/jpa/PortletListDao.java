package org.apereo.portal.dao.portletlist.jpa;

import com.google.common.base.Function;
import lombok.extern.slf4j.Slf4j;
import org.apereo.portal.dao.portletlist.IPortletList;
import org.apereo.portal.dao.portletlist.IPortletListDao;
import org.apereo.portal.jpa.BasePortalJpaDao;
import org.springframework.stereotype.Repository;

import javax.persistence.TypedQuery;
import javax.persistence.criteria.*;
import java.util.*;

@Slf4j
@Repository("portletListDao")
public class PortletListDao extends BasePortalJpaDao implements IPortletListDao {

    private CriteriaQuery<PortletList> portletListsQuery;

    private CriteriaQuery<PortletList> portletListsByUserIdQuery;

    private CriteriaQuery<PortletList> portletListByPortletListUuidQuery;

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

        this.portletListsQuery =
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
                            .select(root);
                        return criteriaQuery;
                    }
                });

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

        this.portletListByPortletListUuidQuery =
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
                            .where(cb.equal(root.get("id"), portletListUuidParameter));
                        return criteriaQuery;
                    }
                });
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
    public List<IPortletList> getPortletLists() {
        TypedQuery<PortletList> query =
            this.createCachedQuery(portletListsQuery);
        List<IPortletList> entities = new ArrayList<>(query.getResultList());
        return entities;
    }

    @PortalTransactionalReadOnly
    @Override
    public IPortletList getPortletList(
        String portletListUuid) {
        TypedQuery<PortletList> query =
            this.createCachedQuery(portletListByPortletListUuidQuery);
        query.setParameter(portletListUuidParameter, portletListUuid);

        List<PortletList> lists = query.getResultList();
        if(lists.size() < 1) {
            return null;
        } else if(lists.size() > 1) {
            log.error("Expected to up to 1 portlet list for portlet list uuid [{}], but found [{}].", portletListUuid, lists.size());
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

    @PortalTransactional
    @Override
    public IPortletList updatePortletList(IPortletList toUpdate, String portletListUuid) {
        log.debug("Persisting changes for portlet list [{}] for user [{}]", toUpdate.getName(), toUpdate.getUserId());
        try {
            IPortletList ref = this.getEntityManager().find(PortletList.class, portletListUuid);
            if (ref == null) {
                log.warn("Unable to find portlet list [{}] to update.", portletListUuid);
                return null;
            }
            ref.overrideItems(toUpdate.getItems());

//            List<PortletListItem> originalItems = toUpdate.getItems();
//            toUpdate.setItems(null);
//            log.debug("Safe updating - removing items on portlet list [{}] for user [{}]", toUpdate.getName(), toUpdate.getUserId());
//            this.getEntityManager().persist(toUpdate);
//            toUpdate.setItems(originalItems);
            //log.debug("Safe updating - updating with current items on portlet list [{}] for user [{}]", toUpdate.getName(), toUpdate.getUserId());
            //this.getEntityManager().merge(toUpdate);
            log.debug("Finished persisting changes for portlet list [{}] for user [{}]. ID = [{}]", toUpdate.getName(), toUpdate.getUserId(), portletListUuid);
        } catch (Exception e) {
            log.debug("Failed to persist changes for portlet list", e);
            throw e;
        }
        return toUpdate;
    }
}
