package org.jasig.portal.portlets.statistics;

import org.codehaus.jackson.map.annotate.JsonSerialize;

import com.google.visualization.datasource.datatable.DataTable;

/**
 * JsonDataTable extends Google's DataTable class so that we can use a 
 * custom Json serializer.
 * 
 * @author Jen Bourey, jennifer.bourey@gmail.com
 */
@JsonSerialize(using=GoogleDataTableSerializer.class)
public class JsonDataTable extends DataTable {

}
