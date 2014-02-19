package org.jasig.portal.rest.models;

import java.util.List;

public class DataTablesResponse {
	
    private String sEcho;
    private Integer iTotalRecords;
    private Integer iTotalDisplayRecords;
    private List<List<? extends Object>> aaData;
    
    public DataTablesResponse(int rows) {
        setSize(rows);
    }

    public DataTablesResponse(int rows, String sEcho) {
        setSize(rows);
        this.sEcho = sEcho;
    }

    private void setSize(int rows) {
        this.iTotalDisplayRecords = rows;
        this.iTotalRecords = rows;
    }

	public String getsEcho() {
		return sEcho;
	}

	public void setsEcho(String sEcho) {
		this.sEcho = sEcho;
	}

	public Integer getiTotalRecords() {
		return iTotalRecords;
	}

	public void setiTotalRecords(Integer iTotalRecords) {
		this.iTotalRecords = iTotalRecords;
	}

	public Integer getiTotalDisplayRecords() {
		return iTotalDisplayRecords;
	}

	public void setiTotalDisplayRecords(Integer iTotalDisplayRecords) {
		this.iTotalDisplayRecords = iTotalDisplayRecords;
	}

	public List<List<? extends Object>> getAaData() {
		return aaData;
	}

	public void setAaData(List<List<? extends Object>> aaData) {
		this.aaData = aaData;
	}
}
