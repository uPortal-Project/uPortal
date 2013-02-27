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

import java.util.List;

import com.google.visualization.datasource.datatable.ColumnDescription;

/**
 * Interface to define the strategies used to determine the text of the statistics
 * report titles and column headings.  Various implementations will generate the
 * text as they see fit based upon available data.
 *
 * @author James Wennmacher, jameswennmacher@gmail.com
 */
public interface ReportTitleAndColumnDescriptionStrategy {

    /**
     * Create a title that augments the report's standard title.  The items that have a single
     * multi-valued criteria are typically added to the report title so they are
     * not listed redundantly in each column. You typically wouldn't have a report
     * with columns Weather Portlet (ALL), Weather Portlet (RENDER), Weather Portlet
     * (ACTION).  You'd want the title to include 'Weather Portlet' and the columns
     * to be ALL, RENDER, ACTION.
     *
     * Implementing classes can format the data
     *
     * @param items Array of report items in order of importance.  The item the
     *              report is for is typically the first in the array, and the
     *              other items in order of 'preference' to being in the title
     *              (last item least likely to be in the title).
     * @return String to add to the report's standard title, typically including
     *         report item descriptions that would be duplicated in each column
     */
    String getReportTitleAugmentation(TitleAndCount[] items);

    /**
     * Create column descriptions for the portlet report.  The column descriptions
     * are typically the opposite of the title description changes.
     * If an item appears in the title it typically does not appear in the column
     * heading as well.
     *
     * Output is dependent upon the implementing class.
     *
     * @param items ordered array of items in the report.  NOTE:  item ordering
     *              must be the same as with getReportTitleAugmentation
     * @param showAll true to include all item descriptions in the column headings.
     *                Useful if report is being written to CSV, XML, HTML, etc.
     *                for importing into another tool
     * @param form statistics report form
     * @return List of column descriptions for the report
     */
    List<ColumnDescription> getColumnDescriptions(TitleAndCount[] items, boolean showAll,
                                                  BaseReportForm form);

        public class TitleAndCount {
        private String criteriaItem;
        private int criteriaValuesSelected;

        public TitleAndCount(String criteriaItem, int criteriaValuesSelected) {
            this.criteriaItem = criteriaItem;
            this.criteriaValuesSelected = criteriaValuesSelected;
        }

        public String getCriteriaItem() {
            return criteriaItem;
        }

        public int getCriteriaValuesSelected() {
            return criteriaValuesSelected;
        }
    }
}
