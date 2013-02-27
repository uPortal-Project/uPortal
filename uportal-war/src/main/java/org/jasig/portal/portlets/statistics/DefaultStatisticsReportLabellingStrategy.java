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

import java.util.ArrayList;
import java.util.List;

import com.google.visualization.datasource.datatable.ColumnDescription;
import com.google.visualization.datasource.datatable.value.ValueType;
import org.springframework.stereotype.Component;

/**
 * Create statistics report title and column headers using a dashed separator
 * with of items in title and column header being the order passed in.
 *
 * @author James Wennmacher, jameswennmacher@gmail.com
 */

@Component
public class DefaultStatisticsReportLabellingStrategy
    implements ReportTitleAndColumnDescriptionStrategy {

    String displaySeparator = " - ";

    public void setDisplaySeparator(String displaySeparator) {
        this.displaySeparator = displaySeparator;
    }

    /**
     * Create report title.  Criteria that have a single value selected are put
     * into the title. If only 1 of each item chosen, the 1st item will be
     * a column header and the remaining items will be in the title header.
     * Format and possible options for title are:
     * <ul>
     * <li>itemX</li>
     * <li>itemX - itemY [ - itemZ...]</li>
     * <li>item2 [ - item 3...] if all items have items.count = 1</li>
     * </ul>
     * @return report title
     */
    @Override
    public String getReportTitleAugmentation(TitleAndCount[] items) {
        // If only 1 item, don't augment the title
        if (items.length == 1) {
            return null;
        }
        String title = null;
        int singleValues = 0;
        for (TitleAndCount item : items) {
            if (item.getCriteriaValuesSelected() == 1) {
                singleValues++;
                title = title == null ? item.getCriteriaItem()
                        : title + displaySeparator + item.getCriteriaItem();
            }
        }
        // If all items have 1 value selected, return only items 2 and beyond.
        // Item 1 will be a column separator.
        if (singleValues == items.length) {
            return title.substring(title.indexOf(displaySeparator) + displaySeparator.length());
        }
        return title;
    }

    /**
     * Create column descriptions for the portlet report.  The column descriptions
     * are essentially the opposite of the title description changes.
     * Those items that have size > 1 (more than one value selected in the
     * report criteria) are displayed in the column description.
     * If all items have only 1 value selected, the first item will be the column
     * description.
     *
     * @param items ordered array of items in the report.  NOTE:  item ordering
     *              must be the same as with getReportTitleAugmentation
     * @param showAll true to include all item descriptions in the column headings.
     *                Useful if report is being written to CSV, XML, HTML, etc.
     *                for importing into another tool
     * @param form statistics report form
     * @return List of column descriptions for the report
     */
    @Override
    public List<ColumnDescription> getColumnDescriptions(TitleAndCount[] items, boolean showAll,
                                                         BaseReportForm form) {
        String description = null;
        int multipleValues = 0;
        for (TitleAndCount item : items) {
            if (item.getCriteriaValuesSelected() > 1 || showAll) {
                multipleValues++;
                description = description == null ? item.getCriteriaItem()
                        : description + displaySeparator + item.getCriteriaItem();
            }
        }
        // If all items have 1 value selected or if there is only 1 item, make the
        // first item the column descriptor.
        if (multipleValues == 0 || items.length == 1) {
            description = items[0].getCriteriaItem();
        }

        final List<ColumnDescription> columnDescriptions = new ArrayList<ColumnDescription>();
        columnDescriptions.add(new ColumnDescription(description, ValueType.NUMBER, description));
        return columnDescriptions;
    }
}
