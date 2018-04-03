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
	
	public String getDataBaseFileName() {
		return this.dbFileName;
	}
	
	@Override
	public int getLastInsertId() throws SQLException {
		PreparedStatement statementId = this.getPreparedStatement(HSQLDB.SQL_LAST_ID);
		ResultSet lastId = statementId.executeQuery();
		if (lastId.next() && !lastId.wasNull())
			return lastId.getInt(1);
		return -1;
	}
}
