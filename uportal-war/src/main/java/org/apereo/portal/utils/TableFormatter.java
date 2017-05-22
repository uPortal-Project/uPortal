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
package org.apereo.portal.utils;

import java.util.ArrayList;
import java.util.Formatter;
import java.util.LinkedList;
import java.util.List;
import org.jvnet.jaxb2_commons.lang.Validate;

/**
 */
public class TableFormatter {
    public static final class TableEntry<T> {
        private final T value;
        private final String flags;
        private final String conversion;

        public TableEntry(T value, String conversion) {
            this(value, "", conversion);
        }

        public TableEntry(T value, String flags, String conversion) {
            Validate.notNull(flags);
            Validate.notNull(conversion);

            this.value = value;
            this.flags = flags;
            this.conversion = conversion;
        }

        protected String getFormatString() {
            return this.getFormatString(1);
        }

        protected String getFormatString(int padding) {
            return "%" + this.flags + padding + this.conversion;
        }

        /** @return the value */
        public T getValue() {
            return value;
        }
        /** @return the flags */
        public String getFlags() {
            return flags;
        }
        /** @return the conversion */
        public String getConversion() {
            return conversion;
        }

        /* (non-Javadoc)
         * @see java.lang.Object#toString()
         */
        @Override
        public String toString() {
            return "TableEntry [flags="
                    + flags
                    + ", conversion="
                    + conversion
                    + ", value="
                    + value
                    + "]";
        }
    }

    private final StringBuilder scratchBuilder;
    private final Formatter scratchFormatter;

    private final List<Integer> columnWidths;
    private final List<TableEntry<?>> headerRow;
    private final List<List<TableEntry<?>>> rows = new LinkedList<List<TableEntry<?>>>();

    public TableFormatter(TableEntry<?> heading, TableEntry<?>... headings) {
        this(null, heading, headings);
    }

    public TableFormatter(Formatter f, TableEntry<?> firstHeading, TableEntry<?>... headings) {
        Validate.notNull(firstHeading);

        //Setup scratch objects used for formatting data
        this.scratchBuilder = new StringBuilder();
        this.scratchFormatter = new Formatter(scratchBuilder, f != null ? f.locale() : null);

        this.columnWidths = new ArrayList<Integer>(1 + headings.length);
        this.headerRow = new ArrayList<TableEntry<?>>(1 + headings.length);

        this.updateColumnSize(0, firstHeading);
        this.headerRow.add(firstHeading);

        for (final TableEntry<?> heading : headings) {
            this.updateColumnSize(this.headerRow.size(), heading);
            this.headerRow.add(heading);
        }
    }

    public void addRow(TableEntry<?> firstValue, TableEntry<?>... values) {
        Validate.notNull(firstValue);
        if (1 + values.length != this.columnWidths.size()) {
            throw new IllegalArgumentException(
                    "Inconsistent column count. Expected "
                            + this.columnWidths.size()
                            + " but was "
                            + (1 + values.length));
        }

        final List<TableEntry<?>> row = new ArrayList<TableEntry<?>>(this.columnWidths.size());

        updateColumnSize(row.size(), firstValue);
        row.add(firstValue);

        for (final TableEntry<?> value : values) {
            updateColumnSize(row.size(), value);
            row.add(value);
        }

        this.rows.add(row);
    }

    private void updateColumnSize(int index, TableEntry<?> value) {
        final int valueLength = estimateEntryLength(value);
        if (this.columnWidths.size() == index) {
            this.columnWidths.add(valueLength);
        } else {
            this.columnWidths.set(index, Math.max(this.columnWidths.get(index), valueLength));
        }
    }

    public void format(Formatter formatter) {
        //Write out table header
        this.formatRow(formatter, this.headerRow);

        //Write out separator row
        this.clearScratchBuilder();
        this.scratchBuilder.append('-');
        for (int column = 0; column < this.columnWidths.size(); column++) {
            if (column > 0) {
                this.scratchBuilder.append("-+-");
            }

            final int width = this.columnWidths.get(column);
            for (int i = 0; i < width; i++) {
                this.scratchBuilder.append('-');
            }
        }
        this.scratchBuilder.append("-%n");
        formatter.format(this.scratchBuilder.toString());

        //Write out rows
        for (final List<TableEntry<?>> row : this.rows) {
            this.formatRow(formatter, row);
        }
    }

    private void formatRow(Formatter formatter, final List<TableEntry<?>> row) {
        this.clearScratchBuilder();
        this.scratchBuilder.append(' ');
        final Object[] formatArgs = new Object[this.columnWidths.size()];
        for (int column = 0; column < this.columnWidths.size(); column++) {
            if (column > 0) {
                this.scratchBuilder.append(" | ");
            }

            final int width = this.columnWidths.get(column);
            final TableEntry<?> header = row.get(column);

            formatArgs[column] = header.getValue();
            this.scratchBuilder.append(header.getFormatString(width));
        }
        this.scratchBuilder.append(" %n");
        formatter.format(this.scratchBuilder.toString(), formatArgs);
    }

    private <T> int estimateEntryLength(TableEntry<T> entry) {
        clearScratchBuilder();

        this.scratchFormatter.format(entry.getFormatString(), entry.getValue());

        return this.scratchBuilder.length();
    }

    private void clearScratchBuilder() {
        if (this.scratchBuilder.length() > 0) {
            this.scratchBuilder.delete(0, this.scratchBuilder.length());
        }
    }
}
