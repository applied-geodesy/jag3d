/***********************************************************************
* Copyright by Michael Loesler, https://software.applied-geodesy.org   *
*                                                                      *
* This program is free software; you can redistribute it and/or modify *
* it under the terms of the GNU General Public License as published by *
* the Free Software Foundation; either version 3 of the License, or    *
* at your option any later version.                                    *
*                                                                      *
* This program is distributed in the hope that it will be useful,      *
* but WITHOUT ANY WARRANTY; without even the implied warranty of       *
* MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the        *
* GNU General Public License for more details.                         *
*                                                                      *
* You should have received a copy of the GNU General Public License    *
* along with this program; if not, see <http://www.gnu.org/licenses/>  *
* or write to the                                                      *
* Free Software Foundation, Inc.,                                      *
* 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.            *
*                                                                      *
***********************************************************************/

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

	public abstract String getURI();

	public final String getDBDriver() {
		return this.dbDriver;
	}

	public void open() throws ClassNotFoundException, SQLException {
		if (this.conn == null || this.conn.isClosed()) {
			this.conn = this.getConnection();
			this.isOpen = true;
		}
	}
	
	public boolean isOpen() {
		try {
			return this.conn != null && !this.conn.isClosed() && this.isOpen;
		}
		catch (SQLException e ) {
			e.printStackTrace();
		}
		return false;
	}

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
	
	public abstract int getLastInsertId() throws SQLException;

	public PreparedStatement getPreparedStatement(String sql) throws SQLException {
		if (this.isOpen()) {
			return this.conn.prepareStatement(sql);
		}
		return null;
	}

	public void commit() throws SQLException {
		if (this.isOpen()) {
			this.conn.commit();
		}
	}
	
	public void setAutoCommit(boolean autoCommit) throws SQLException {
		if (this.isOpen()) {
			this.conn.setAutoCommit(autoCommit);
		}
	}

	public void rollback() throws SQLException {
		if (this.isOpen()) {
			this.conn.rollback();
		}
	}
	    
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
