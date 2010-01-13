package org.jasig.portal.portlets.sqlquery;

public class SqlQueryConfigForm {

	private String sqlQuery;
	
	private String dataSource;
	
	private String viewName;

	public String getSqlQuery() {
		return this.sqlQuery;
	}

	public void setSqlQuery(String sqlQuery) {
		this.sqlQuery = sqlQuery;
	}

	public String getDataSource() {
		return this.dataSource;
	}

	public void setDataSource(String dataSource) {
		this.dataSource = dataSource;
	}

	public String getViewName() {
		return this.viewName;
	}

	public void setViewName(String viewName) {
		this.viewName = viewName;
	}
	
	
	
}
