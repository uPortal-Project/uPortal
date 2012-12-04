package org.jasig.portal.utils;

import org.hibernate.dialect.MySQL5InnoDBDialect;

public class MySQL5SpecificDBDialect extends MySQL5InnoDBDialect {

	public String getTableTypeString() {
		return " ENGINE=InnoDB ROW_FORMAT=COMPRESSED";
	}

	
}
