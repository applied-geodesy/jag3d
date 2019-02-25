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

package org.applied_geodesy.version.jag3d;

public class DatabaseVersionMismatchException extends Exception {
	private static final long serialVersionUID = 801280202342575901L;

	public DatabaseVersionMismatchException() { 
		super();
	}
	
	public DatabaseVersionMismatchException(String message) { 
		super(message); 
	}
	
	public DatabaseVersionMismatchException(String message, Throwable cause) { 
		super(message, cause);
	}
	
	public DatabaseVersionMismatchException(Throwable cause) { 
		super(cause); 
	}
}