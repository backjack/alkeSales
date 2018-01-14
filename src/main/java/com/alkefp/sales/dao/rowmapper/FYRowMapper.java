package com.alkefp.sales.dao.rowmapper;

import java.sql.ResultSet;
import java.sql.SQLException;

import org.springframework.jdbc.core.RowMapper;

import com.alkefp.sales.beans.Pair;

public class FYRowMapper implements RowMapper<Pair<Integer,String>> {

	@Override
	public Pair<Integer,String> mapRow(ResultSet rs, int arg1) throws SQLException {
		// TODO Auto-generated method stub
		
		Pair<Integer,String> pair = new Pair<Integer,String>();
		pair.setKey(rs.getInt("fyYear"));
		pair.setValue(rs.getString("fyYearDesc"));
		return pair;
		
	}

	

}
