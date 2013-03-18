/**
 * Licensed to Jasig under one or more contributor license
 * agreements. See the NOTICE file distributed with this work
 * for additional information regarding copyright ownership.
 * Jasig licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file
 * except in compliance with the License. You may obtain a
 * copy of the License at:
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on
 * an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.jasig.portal.portlets.statistics;

import org.jasig.portal.portlets.statistics.ReportTitleAndColumnDescriptionStrategy.TitleAndCount;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

/**
 * @author James Wennmacher (jameswennmacher@gmail.com)
 * @version $Revision$
 */
public class DefaultStatisticsReportLabellingStrategyTest {

    DefaultStatisticsReportLabellingStrategy object;
    TitleAndCount bob1;
    TitleAndCount bob2;
    TitleAndCount sam1;
    TitleAndCount sam2;
    TitleAndCount pete1;
    TitleAndCount pete2;
    BaseReportForm form;

    @Before
    public void setup() {
        object = new DefaultStatisticsReportLabellingStrategy();
        bob1 = new TitleAndCount("bob",1);
        bob2 = new TitleAndCount("bob",2);
        sam1 = new TitleAndCount("sam",1);
        sam2 = new TitleAndCount("sam",2);
        pete1 = new TitleAndCount("pete",1);
        pete2 = new TitleAndCount("pete",2);
        form = new LoginReportForm();
    }

    @Test
    public void testSingleValue() {
        TitleAndCount[] items = new TitleAndCount[] { bob1 };
        assertEquals(null, object.getReportTitleAugmentation(items));
        assertEquals("bob", object.getColumnDescriptions(items, false, form).get(0).getLabel());
    }

    @Test
    public void testTwoSingle() {
        TitleAndCount[] items = new TitleAndCount[] { bob1, sam1 };
        assertEquals(sam1.getCriteriaItem(), object.getReportTitleAugmentation(items));
        assertEquals(bob1.getCriteriaItem(), object.getColumnDescriptions(items, false, form).get(0).getLabel());
    }

    @Test
    public void testAllMultipleValues() {
        TitleAndCount[] items = new TitleAndCount[] { bob2, sam2 };
        assertEquals(null, object.getReportTitleAugmentation(items));
        assertEquals("bob - sam", object.getColumnDescriptions(items, false, form).get(0).getLabel());
    }

    @Test
    public void testSingleMultiple() {
        TitleAndCount[] items = new TitleAndCount[] { bob1, sam2 };
        assertEquals(bob1.getCriteriaItem(), object.getReportTitleAugmentation(items));
        assertEquals(sam2.getCriteriaItem(), object.getColumnDescriptions(items, false, form).get(0).getLabel());
    }

    @Test
    public void testSingleMultipleShowAll() {
        TitleAndCount[] items = new TitleAndCount[] { bob1, sam2 };
        assertEquals(bob1.getCriteriaItem(), object.getReportTitleAugmentation(items));
        assertEquals("bob - sam", object.getColumnDescriptions(items, true, form).get(0).getLabel());
    }

    @Test
    public void testMultipleSingle() {
        TitleAndCount[] items = new TitleAndCount[] { bob2, sam1 };
        assertEquals(sam1.getCriteriaItem(), object.getReportTitleAugmentation(items));
        assertEquals(bob2.getCriteriaItem(), object.getColumnDescriptions(items, false, form).get(0).getLabel());
    }

    @Test
    public void testThreeSingle() {
        TitleAndCount[] items = new TitleAndCount[] { bob1, sam1, pete1};
        assertEquals("sam - pete", object.getReportTitleAugmentation(items));
        assertEquals(bob1.getCriteriaItem(), object.getColumnDescriptions(items, false, form).get(0).getLabel());
    }

}
