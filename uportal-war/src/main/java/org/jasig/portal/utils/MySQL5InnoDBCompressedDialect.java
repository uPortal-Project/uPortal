package org.jasig.portal.utils;

import org.hibernate.dialect.MySQL5InnoDBDialect;

/**
 * Uses the COMPRESSED row format in an InnoDB engine, needed for long index support with UTF-8 
 * 
 * @author Raymond Bourges
 */
public class MySQL5InnoDBCompressedDialect extends MySQL5InnoDBDialect {

	public String getTableTypeString() {
		return " ENGINE=InnoDB ROW_FORMAT=COMPRESSED";
	}
	
}
