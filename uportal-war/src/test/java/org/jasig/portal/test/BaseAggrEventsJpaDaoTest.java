package org.jasig.portal.test;

import java.util.LinkedList;
import java.util.List;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.concurrent.Callable;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import org.jasig.portal.events.aggr.DateDimension;
import org.jasig.portal.events.aggr.TimeDimension;
import org.jasig.portal.events.aggr.dao.DateDimensionDao;
import org.jasig.portal.events.aggr.dao.TimeDimensionDao;
import org.jasig.portal.jpa.BaseAggrEventsJpaDao;
import org.jasig.portal.utils.Tuple;
import org.joda.time.DateMidnight;
import org.joda.time.DateTime;
import org.joda.time.LocalTime;
import org.springframework.beans.factory.annotation.Autowired;

import com.google.common.base.Function;

/**
 * Base class for AggrEventsDB unit tests that want TX and entity manager support.
 * 
 * @author Eric Dalquist
 */
public abstract class BaseAggrEventsJpaDaoTest extends BaseJpaDaoTest {
    @Autowired
    private TimeDimensionDao timeDimensionDao;
    @Autowired
    private DateDimensionDao dateDimensionDao;
    private EntityManager entityManager;

    @PersistenceContext(unitName = BaseAggrEventsJpaDao.PERSISTENCE_UNIT_NAME)
    public final void setEntityManager(EntityManager entityManager) {
        this.entityManager = entityManager;
    }
    @Override
    protected final EntityManager getEntityManager() {
        return this.entityManager;
    }
    
    /**
     * Populate date & time dimensions in an interval range executing a callback for each pair
     */
    public final <T> List<T> populateDateTimeDimensions(final DateTime start, final DateTime end,
            final Function<Tuple<DateDimension, TimeDimension>, T> newDimensionHandler) {
        
        return this.executeInTransaction(new Callable<List<T>>() {
            @Override
            public List<T> call() throws Exception {
                final List<T> results = new LinkedList<T>();
                final SortedMap<LocalTime, TimeDimension> times = new TreeMap<LocalTime, TimeDimension>();
                final SortedMap<DateMidnight, DateDimension> dates = new TreeMap<DateMidnight, DateDimension>();
                
                DateTime nextDateTime = start.minuteOfDay().roundFloorCopy();
                while (nextDateTime.isBefore(end)) {
                    
                    //get/create TimeDimension
                    final LocalTime localTime = nextDateTime.toLocalTime();
                    TimeDimension td = times.get(localTime);
                    if (td == null) {
                        td = timeDimensionDao.createTimeDimension(localTime);
                        times.put(localTime, td);
                    }
                    
                    //get/create DateDimension
                    final DateMidnight dateMidnight = nextDateTime.toDateMidnight();
                    DateDimension dd = dates.get(dateMidnight);
                    if (dd == null) {
                        dd = dateDimensionDao.createDateDimension(dateMidnight, 0, null);
                        dates.put(dateMidnight, dd);
                    }
                    
                    //Let callback do work
                    if (newDimensionHandler != null) {
                        final T result = newDimensionHandler.apply(new Tuple<DateDimension, TimeDimension>(dd, td));
                        if (result != null) {
                            results.add(result);
                        }
                    }
                    
                    nextDateTime = nextDateTime.plusMinutes(1);
                }
                
                return results;
            }
        });
    }
}
