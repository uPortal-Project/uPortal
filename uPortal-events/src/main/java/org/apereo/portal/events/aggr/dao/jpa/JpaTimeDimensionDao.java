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
package org.apereo.portal.events.aggr.dao.jpa;

import com.google.common.base.Function;
import java.util.ArrayList;
import java.util.List;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;
import org.apereo.portal.events.aggr.TimeDimension;
import org.apereo.portal.events.aggr.dao.TimeDimensionDao;
import org.apereo.portal.jpa.BaseAggrEventsJpaDao;
import org.apereo.portal.jpa.OpenEntityManager;
import org.joda.time.LocalTime;
import org.springframework.stereotype.Repository;

@Repository
public class JpaTimeDimensionDao extends BaseAggrEventsJpaDao implements TimeDimensionDao {

    private CriteriaQuery<TimeDimensionImpl> findAllTimeDimensionsQuery;

    @Override
    public void afterPropertiesSet() throws Exception {
        this.findAllTimeDimensionsQuery =
                this.createCriteriaQuery(
                        new Function<CriteriaBuilder, CriteriaQuery<TimeDimensionImpl>>() {
                            @Override
                            public CriteriaQuery<TimeDimensionImpl> apply(CriteriaBuilder cb) {
                                final CriteriaQuery<TimeDimensionImpl> criteriaQuery =
                                        cb.createQuery(TimeDimensionImpl.class);
                                final Root<TimeDimensionImpl> dimensionRoot =
                                        criteriaQuery.from(TimeDimensionImpl.class);
                                criteriaQuery.select(dimensionRoot);
                                criteriaQuery.orderBy(
                                        cb.asc(dimensionRoot.get(TimeDimensionImpl_.time)));

                                return criteriaQuery;
                            }
                        });
    }

    @Override
    @AggrEventsTransactional
    public TimeDimension createTimeDimension(LocalTime time) {
        final TimeDimension timeDimension = new TimeDimensionImpl(time);

        this.getEntityManager().persist(timeDimension);

        return timeDimension;
    }

    @Override
    public List<TimeDimension> getTimeDimensions() {
        final TypedQuery<TimeDimensionImpl> query =
                this.createCachedQuery(this.findAllTimeDimensionsQuery);

        final List<TimeDimensionImpl> portletDefinitions = query.getResultList();
        return new ArrayList<TimeDimension>(portletDefinitions);
    }

    @Override
    public TimeDimension getTimeDimensionById(long key) {
        final TimeDimension timeDimension =
                this.getEntityManager().find(TimeDimensionImpl.class, key);

        return timeDimension;
    }

    @OpenEntityManager(unitName = BaseAggrEventsJpaDao.PERSISTENCE_UNIT_NAME)
    @Override
    public TimeDimension getTimeDimensionByTime(LocalTime localTime) {
        final NaturalIdQuery<TimeDimensionImpl> query =
                this.createNaturalIdQuery(TimeDimensionImpl.class);
        query.using(TimeDimensionImpl_.time, localTime.minuteOfHour().roundFloorCopy());
        return query.load();
    }
}
