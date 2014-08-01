package org.jasig.portal.events.tincan;

import java.util.List;

import javax.annotation.Resource;

import org.jasig.portal.events.PortalEvent;
import org.jasig.portal.events.aggr.BasePortalEventAggregator;
import org.jasig.portal.events.aggr.SimplePortalEventAggregator;
import org.jasig.portal.events.aggr.session.EventSession;
import org.jasig.portal.events.tincan.om.LrsStatement;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;


/**
 * Event aggregator that processes events and converts them to
 * xAPI events.
 *
 * @author Josh Helmer, jhelmer@unicon.net
 */
public class TinCanPortalEventAggregator extends
        BasePortalEventAggregator<PortalEvent> implements
        SimplePortalEventAggregator<PortalEvent> {
    private static final Logger log = LoggerFactory.getLogger(TinCanPortalEventAggregator.class);
    private ITinCanEventScheduler tinCanEventScheduler;
    private List<IPortalEventToLrsStatementConverter> statementFactories;
    private boolean tinCanAPIEnabled = false;


    @Resource(name = "tinCanEventConverters")
    public void setStatementFactories(final List<IPortalEventToLrsStatementConverter> statementFactories) {
        this.statementFactories = statementFactories;
    }


    @Autowired
    @Qualifier("tinCanEventScheduler")
    public void setTinCanEventScheduler(final ITinCanEventScheduler tinCanEventScheduler) {
        this.tinCanEventScheduler = tinCanEventScheduler;
    }

    @Value("${org.jasig.portal.tincan-api.enabled:false}")
    public void setTinCanAPIEnabled(final boolean enabled) {
        this.tinCanAPIEnabled = enabled;
    }


    @Override
    public boolean supports(Class<? extends PortalEvent> type) {
        return true;
    }


    @Override
    public void aggregateEvent(PortalEvent e, EventSession eventSession) {
        if (!tinCanAPIEnabled) {
            return;
        }

        try {
            for (IPortalEventToLrsStatementConverter factory : statementFactories) {
                if (factory.supports(e)) {
                    LrsStatement lrsStatement = factory.toLrsStatement(e);
                    if (lrsStatement != null) {
                        tinCanEventScheduler.scheduleEvent(lrsStatement);
                        // for now, assume a max of 1 handler per event.
                        break;
                    }
                }
            }
        } catch (Exception ex) {
            log.error("Error sending xAPI event", ex);
        }
    }
}
