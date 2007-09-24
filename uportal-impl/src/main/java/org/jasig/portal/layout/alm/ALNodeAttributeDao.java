package org.jasig.portal.layout.alm;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.HashMap;
import java.util.Map;

import javax.sql.DataSource;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowCallbackHandler;

/**
 * Data access object for getting the attributes of a ALM fragment node.
 */
public class ALNodeAttributeDao {

	private JdbcTemplate attributeReadTemplate;
    
    private JdbcTemplate attributeInsertTemplate;
    
    private JdbcTemplate attributeUpdateTemplate;
	
	public ALNodeAttributeDao(DataSource dataSource) {
		this.attributeReadTemplate = new JdbcTemplate(dataSource);
        
        
        this.attributeInsertTemplate = new JdbcTemplate(dataSource);
        this.attributeInsertTemplate.setMaxRows(1);
        
        this.attributeUpdateTemplate = new JdbcTemplate(dataSource);
        this.attributeUpdateTemplate.setMaxRows(1);
	}
	
	
    /**
     * Returns a Map from String attribute names to String attribute values.
     * @param fragmentId
     * @param nodeId
     * @return
     */
    public Map attributesForFragmentNode(int fragmentId, int nodeId) {
        
        final Map map = new HashMap();
        
        String sql = "select param_name, param_value from up_fragment_param where " +
        		"fragment_id = ? and node_id = ?";
        final Object[] params = new Object[] { new Integer(fragmentId), new Integer(nodeId) };
        
        attributeReadTemplate.query(sql, params, new RowCallbackHandler() {
        	public void processRow(ResultSet rs) throws SQLException {
        		String paramName = rs.getString("param_name");
        		String paramValue = rs.getString("param_value");
        		map.put(paramName, paramValue);
        	}
        });

        return map;
    }
    
    public int insertFragmentNodeAttribute(int fragmentId, int nodeId, String attributeName, String attributeValue) {
        String sql = "insert into up_fragment_param (fragment_id, node_id, param_name, param_value) values (?, ?, ?, ?)";
        Object[] params = new Object[] {new Integer(fragmentId), new Integer(nodeId), attributeName, attributeValue};
        
        int[] types = new int[] { Types.INTEGER, Types.INTEGER, Types.VARCHAR, Types.VARCHAR };
        
        return attributeInsertTemplate.update(sql, params, types);

    }
    
    
    public int setFragmentNodeAttribute(int fragmentId, int nodeId, String attributeName, String attributeValue) {
        String sql = "update up_fragment_param set param_value = ? where fragment_id = ? and node_id = ? and param_name = ?";
        Object[] params = new Object[] { attributeValue, new Integer(fragmentId), new Integer(nodeId), attributeName};
        
        int[] types = new int[] {Types.VARCHAR, Types.INTEGER, Types.INTEGER, Types.VARCHAR};
        
        return attributeUpdateTemplate.update(sql, params, types);

    }
    
}

