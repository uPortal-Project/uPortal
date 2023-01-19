package org.apereo.portal.portlets.portletadmin;

import java.text.ParseException;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.TimeZone;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.time.FastDateFormat;
import org.apereo.portal.IUserIdentityStore;
import org.apereo.portal.portlet.dao.IPortletDefinitionDao;
import org.apereo.portal.portlet.om.IPortletDefinition;
import org.apereo.portal.portlet.om.IPortletDefinitionParameter;
import org.apereo.portal.portlet.om.IPortletLifecycleEntry;
import org.apereo.portal.portlet.om.PortletLifecycleState;
import org.apereo.portal.security.IPerson;
import org.apereo.portal.services.PersonService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service("portletMaintenanceScheduler")
public class PortletMaintenanceScheduler {
    protected final Logger logger = LoggerFactory.getLogger(getClass());
    private static final FastDateFormat df = FastDateFormat.getInstance("M/d/yyyy HH:mmZ");
    private static final String UTC_OFFSET = "+0000";

    @Autowired private IPortletDefinitionDao portletDefinitionDao;
    @Autowired private IUserIdentityStore userIdentityStore;
    @Autowired private PersonService personService;

    public boolean updateLifecycleStatus() {
        final Date now = Calendar.getInstance(TimeZone.getTimeZone("UTC")).getTime();

        List<IPortletDefinition> portletDefinitions = portletDefinitionDao.getPortletDefinitions();
        portletDefinitions.forEach(
                portletDef -> {
                    Optional<Date> stopDate = this.retrieveMaintenanceStopDate(portletDef, now);
                    Optional<Date> restartDate =
                            this.retrieveMaintenanceRestartDate(portletDef, now);
                    if (portletDef.getLifecycleState().equals(PortletLifecycleState.PUBLISHED)) {
                        stopDate.ifPresent(
                                sd -> {
                                    if (sd.before(now)) {
                                        Optional<IPerson> person =
                                                retrieveLifecycleStatusUser(portletDef, now);
                                        person.ifPresent(
                                                p -> {
                                                    portletDef.updateLifecycleState(
                                                            PortletLifecycleState.MAINTENANCE,
                                                            p,
                                                            now);
                                                    portletDef.removeParameter(
                                                            PortletLifecycleState
                                                                    .MAINTENANCE_STOP_DATE);
                                                    portletDef.removeParameter(
                                                            PortletLifecycleState
                                                                    .MAINTENANCE_STOP_TIME);
                                                    portletDefinitionDao.mergePortletDefinition(
                                                            portletDef);
                                                    logger.info(
                                                            "portlet ["
                                                                    + portletDef.getDescription()
                                                                    + "] lifecycleState was published, changed to maintenance");
                                                });
                                    }
                                });
                    }
                    ;
                    if (portletDef.getLifecycleState().equals(PortletLifecycleState.MAINTENANCE)) {
                        restartDate.ifPresent(
                                rd -> {
                                    if (rd.before(now)) {
                                        Optional<IPerson> person =
                                                retrieveLifecycleStatusUser(portletDef, now);
                                        person.ifPresent(
                                                p -> {
                                                    portletDef.updateLifecycleState(
                                                            PortletLifecycleState.PUBLISHED,
                                                            p,
                                                            now);
                                                    portletDef.removeParameter(
                                                            PortletLifecycleState
                                                                    .MAINTENANCE_RESTART_DATE);
                                                    portletDef.removeParameter(
                                                            PortletLifecycleState
                                                                    .MAINTENANCE_RESTART_TIME);
                                                    portletDefinitionDao.mergePortletDefinition(
                                                            portletDef);
                                                    logger.info(
                                                            "portlet ["
                                                                    + portletDef.getDescription()
                                                                    + "] lifecycleState was maintenance, changed to published");
                                                });
                                    }
                                });
                    }
                    ;
                });
        return true;
    }

    // TODO lifted directly from PortletDefinitionImpl.getLifecycleState but does
    // not return the state itself; instead it returns the entry so that the userid
    // who created it can be accessed
    public IPortletLifecycleEntry getCurrentLifecycle(IPortletDefinition portletDef, Date now) {

        List<IPortletLifecycleEntry> lifecycleEntries = portletDef.getLifecycle();
        final IPortletLifecycleEntry currentEntry =
                lifecycleEntries.stream()
                        .filter(entry -> entry.getDate().before(now)) // Not entries in the future
                        .reduce(
                                (e1, e2) ->
                                        e1.getDate().after(e2.getDate())
                                                ? e1
                                                : e2) // Only the latest
                        .orElse(null); // Possible if the portlet is not yet fully created
        return currentEntry;
    }

    public Optional<IPerson> retrieveLifecycleStatusUser(IPortletDefinition portletDef, Date now) {
        IPortletLifecycleEntry lifecycleEntry = this.getCurrentLifecycle(portletDef, now);
        int userId = lifecycleEntry.getUserId();
        String userName = userIdentityStore.getPortalUserName(userId);
        IPerson publisher = null;
        if (!StringUtils.isBlank(userName)) {
            publisher = personService.getPerson(userName);
        }
        ;
        if (publisher != null) {
            return Optional.of(publisher);
        }
        logger.warn(
                "Person ["
                        + userName
                        + "] on Portlet ["
                        + portletDef.getName()
                        + "] for lifecycle state ["
                        + lifecycleEntry.getLifecycleState().name()
                        + "] not recognized by personService");
        return Optional.empty();
    }

    public Optional<Date> retrieveMaintenanceStopDate(IPortletDefinition portletDef, Date now) {
        IPortletDefinitionParameter stopDateParam =
                portletDef.getParameter(PortletLifecycleState.MAINTENANCE_STOP_DATE);
        IPortletDefinitionParameter stopTimeParam =
                portletDef.getParameter(PortletLifecycleState.MAINTENANCE_STOP_TIME);
        if (stopDateParam != null && stopTimeParam != null) {
            String stopDateStr =
                    stopDateParam.getValue() + " " + stopTimeParam.getValue() + UTC_OFFSET;
            try {
                Date stopDate = df.parse(stopDateStr);
                return Optional.of(stopDate);
            } catch (ParseException e) {
                logger.error(e.getMessage(), e);
            }
        }
        return Optional.empty();
    }

    public Optional<Date> retrieveMaintenanceRestartDate(IPortletDefinition portletDef, Date now) {
        IPortletDefinitionParameter restartDateParam =
                portletDef.getParameter(PortletLifecycleState.MAINTENANCE_RESTART_DATE);
        IPortletDefinitionParameter restartTimeParam =
                portletDef.getParameter(PortletLifecycleState.MAINTENANCE_RESTART_TIME);
        if (restartDateParam != null && restartTimeParam != null) {
            String restartDateStr =
                    restartDateParam.getValue() + " " + restartTimeParam.getValue() + UTC_OFFSET;
            try {
                Date restartDate = df.parse(restartDateStr);
                return Optional.of(restartDate);
            } catch (ParseException e) {
                logger.error(e.getMessage(), e);
            }
        }
        return Optional.empty();
    }
}
