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

package org.applied_geodesy.util.io.xml;

import org.w3c.dom.Attr;
import org.w3c.dom.Document;

/**
 * @author Michael Loesler
 */
class HeXMLNamespaceContext extends XMLNamespaceContext {
	HeXMLNamespaceContext(Document document) {
		super(
				document,
				new String[] {
						"landxml",
						"hexml"
				},
				new String[] {
						"http://www.landxml.org/schema/LandXML-1.2",
						"http://xml.hexagon.com/schema/HeXML-1.1"
				}
		);
	}
	
	@Override
	String getPrefix(Attr attribute) {
		if (attribute.getNodeValue().toLowerCase().contains("landxml.org/schema/landxml"))
			return "landxml";
		if (attribute.getNodeValue().toLowerCase().contains("xml.hexagon.com/schema/hexml"))
			return "hexml";
		return this.getDefaultPrefix();
	}
}