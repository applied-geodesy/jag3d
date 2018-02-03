package org.applied_geodesy.util.sql;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class HSQLDB extends DataBase{
	private final static String SQL_LAST_ID = "CALL IDENTITY()";
	public static final String JDBCDriver = "org.hsqldb.jdbcDriver";
	private final String dbFileName;
	
	public HSQLDB(String dbFileName) {
		this(dbFileName, "sa", "");
	}

	public HSQLDB(String dbFileName, String username, String password) {
		super(HSQLDB.JDBCDriver, username, password);
		this.dbFileName = dbFileName;
	}
	
	@Override
	public String getURI() {
		return "jdbc:hsqldb:file:" + this.dbFileName + ";shutdown=true";
	}
	
	@Override
	public void close() {
		if (this.isOpen()) {
			try {
				this.getPreparedStatement("CHECKPOINT DEFRAG").execute();
			} 
			catch (SQLException e) {
				e.printStackTrace();
			}
			finally {
				try {
					this.getPreparedStatement("SHUTDOWN COMPACT").execute();
//					this.getPreparedStatement("SHUTDOWN").execute();
				} catch (Exception e) {
					e.printStackTrace();
				}	
			}
		}
		super.close();
	}
	
	/**
	 * Liefert den Pfad zur DB-Datei
	 * @return path
	 */
	public String getDataBaseFileName() {
		return this.dbFileName;
	}
	
	@Override
	public int getLastInsertId() throws SQLException {
		PreparedStatement statementId = this.getPreparedStatement(HSQLDB.SQL_LAST_ID);
		ResultSet lastId = statementId.executeQuery();
		if (!lastId.wasNull() && lastId.next())
			return lastId.getInt(1);
		return -1;
	}
}
