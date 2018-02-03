package org.applied_geodesy.util.sql;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public abstract class DataBase {
	private final String dbDriver, username, password;
	private Connection conn = null;
	private boolean isOpen = false;

	public DataBase(String dbDriver, String username, String password) {
		this.dbDriver = dbDriver;
		this.username = username;
		this.password = password;
	}
	
	/**
	 * Liefert die Adresse zur DB
	 * @return
	 */
	public abstract String getURI();
	  
	/**
	 * Gibt den DB-Treiber zurueck
	 * @return
	 */
	public final String getDBDriver() {
		return this.dbDriver;
	}

	/**
	 * Oeffnet eine Datenbankverbindung
	 * @throws ClassNotFoundException
	 * @throws SQLException
	 */
	public void open() throws ClassNotFoundException, SQLException {
		if (this.conn == null || this.conn.isClosed()) {
			this.conn = this.getConnection();
			this.isOpen = true;
		}
	}
	
	/**
	 * Liefert true, wenn eine Verbindung zum DBS besteht
	 * @return isOpen
	 */
	public boolean isOpen() {
		try {
			return this.conn != null && !this.conn.isClosed() && this.isOpen;
		}catch (SQLException e ) {
			e.printStackTrace();
		}
		return false;
	}
	    
	/**
	 * Schliesst eine Datanbankverbindung
	 */
	public void close() {
		if (this.conn == null)
			return;
		try {
			if (this.conn.isClosed())
				return;
			this.conn.close();
		}
		catch (SQLException e ) {
			e.printStackTrace();
		}
	    finally {
	    	this.conn = null;
	    	this.isOpen = false;
	    }
	}
	
	/**
	 * Liefert im Erfolgsfall die beim letzten INSERT 
	 * automatisch vergebene ID des Datensatzes 
	 * ansonsten -1
	 * @return id
	 * @throws SQLException
	 */
	public abstract int getLastInsertId() throws SQLException;

	/**
	 * Liefert eine vorbereitete Anweisung fuer ein DB Abfrage
	 * @param sql
	 * @return statement
	 * @throws SQLException
	 */
	public PreparedStatement getPreparedStatement(String sql) throws SQLException {
		if (this.isOpen()) {
			return this.conn.prepareStatement(sql);
		}
		return null;
	}
	
	/**
	 * Schreibt die Daten engueltig in die DB (INSERT/UPDATE)
	 * @throws SQLException
	 */
	public void commit() throws SQLException {
		if (this.isOpen()) {
			this.conn.commit();
		}
	}
	
	/**
	 * Legt fest, ob jedes Statement einzeln abgearbeitet wird oder eine Reihe von
	 * Statements als Transaktion zusammengefasst werden.
	 * 
	 * @param autoCommit
	 * @throws SQLException
	 */
	public void setAutoCommit(boolean autoCommit) throws SQLException {
		if (this.isOpen()) {
			this.conn.setAutoCommit(autoCommit);
		}
	}
	
	/**
	 * Fuehrt einen Rollback der Transaktion aus.
	 * 
	 * @throws SQLException 
	 */
	public void rollback() throws SQLException {
		if (this.isOpen()) {
			this.conn.rollback();
		}
	}
	    
	/**
	 * Stellt eine Verbindung zur DB her
	 * @return conn
	 * @throws ClassNotFoundException
	 * @throws SQLException
	 */
	private Connection getConnection() throws ClassNotFoundException, SQLException {
		Class.forName( this.getDBDriver() );
		Connection con = DriverManager.getConnection(
				this.getURI(),
				this.username,
				this.password
		);
		return con;
	}
}
