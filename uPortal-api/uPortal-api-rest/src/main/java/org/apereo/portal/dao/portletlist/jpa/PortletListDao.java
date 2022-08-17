package org.apereo.portal.dao.portletlist.jpa;

import com.google.common.base.Function;
import lombok.extern.slf4j.Slf4j;
import org.apereo.portal.dao.portletlist.IPortletList;
import org.apereo.portal.dao.portletlist.IPortletListDao;
import org.apereo.portal.jpa.BasePortalJpaDao;
import org.apereo.portal.security.IPerson;
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

    private ParameterExpression<String> ownerUsernameParameter;

    private ParameterExpression<String> portletListUuidParameter;

    @Override
    public void afterPropertiesSet() throws Exception {
        this.ownerUsernameParameter = this.createParameterExpression(String.class, "OWNER_USERNAME");
        this.portletListUuidParameter = this.createParameterExpression(String.class, "ID");

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
                            .where(cb.equal(root.get("ownerUsername"), ownerUsernameParameter));
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
        String ownerUsername) {
        TypedQuery<PortletList> query =
            this.createCachedQuery(portletListsByUserIdQuery);
        query.setParameter(ownerUsernameParameter, ownerUsername);
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
            log.error("Expected up to 1 portlet list for portlet list uuid [{}], but found [{}].", portletListUuid, lists.size());
            return null;
        }

        return lists.get(0);
    }

    @SuppressWarnings("unused")
    @PortalTransactional
    @Override
    public IPortletList createPortletList(IPortletList toCreate, IPerson requester) {
        log.debug("Persisting portlet list [{}] with owner [{}]", toCreate.getName(), toCreate.getOwnerUsername());
        try {
            toCreate.prepareForPersistence(requester);
            this.getEntityManager().persist(toCreate);
        } catch (Exception e) {
            log.debug("Failed to persist portlet list", e);
            throw e;
        }
        log.debug("Finished persisting portlet list [{}] for owner [{}]. ID = [{}]", toCreate.getName(), toCreate.getOwnerUsername(), toCreate.getId().toString());
        return toCreate;
    }

    @PortalTransactional
    @Override
    public IPortletList updatePortletList(IPortletList toUpdate, IPerson requester) {
        log.debug("Persisting changes for portlet list [{}]", toUpdate.getId());
        try {
            toUpdate.prepareForPersistence(requester);
            log.debug("Portlet List to update: {}", toUpdate);
            this.getEntityManager().merge(toUpdate);
            log.debug("Finished persisting changes for portlet list [{}]", toUpdate.getId());
        } catch (Exception e) {
            log.debug("Failed to persist changes for portlet list", e);
            throw e;
        }
        return toUpdate;
    }

    @PortalTransactional
    @Override
    public boolean removePortletListAsAdmin(String portletListUuid, IPerson requester) {
        IPortletList list = this.getPortletList(portletListUuid);
        if(list == null) {
            log.warn("Admin user [{}] tried to remove a non-existent list [{}]. Failing request.", requester.getUserName(), portletListUuid);
            return false;
        }
        this.getEntityManager().remove(list);
        return true;
    }

    @PortalTransactional
    @Override
    public boolean removePortletListAsOwner(String portletListUuid, IPerson requester) {
        IPortletList list = this.getPortletList(portletListUuid);
        if(list == null) {
            log.warn("Non-admin user [{}] tried to remove a non-existent list [{}]. Failing request.", requester.getUserName(), portletListUuid);
            return false;
        } else if (!list.getOwnerUsername().equals(requester.getUserName())) {
            log.warn("Non-admin user [{}] tried to remove a list they didn't own [{}]. Failing request.", requester.getUserName(), portletListUuid);
            return false;
        }

        log.debug("Non-admin user [{}] requested to remove a list they own [{}]. Allowing request.", requester.getUserName(), portletListUuid);
        this.getEntityManager().remove(list);
        return true;
    }
}
