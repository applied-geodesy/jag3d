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

  /**********************************************************************
  *                        JobXMLNamespaceContext                        *
  ************************************************************************
  * Copyright (C) by Michael Loesler, http://derletztekick.com           *
  *                                                                      *
  * This program is free software; you can redistribute it and/or modify *
  * it under the terms of the GNU General Public License as published by *
  * the Free Software Foundation; either version 3 of the License, or    *
  * (at your option) any later version.                                  *
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
   **********************************************************************/

package org.applied_geodesy.util.io.xml;

import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.Map;

import javax.xml.namespace.NamespaceContext;

/**
 * @author Michael Loesler
 */
public class JobXMLNamespaceContext implements NamespaceContext {
	Map<String, String> prefix2ns = new HashMap<String, String>(15);
	Map<String, LinkedHashSet<String>> ns2prefix = new HashMap<String, LinkedHashSet<String>>(3);
	
	public JobXMLNamespaceContext() {
		this.add("",   "http://www.trimble.com/schema/JobXML/5_6");
	}
	
	private void add(String prefix, String ns) {
		this.prefix2ns.put(prefix, ns);
		if (!this.ns2prefix.containsKey(ns))
			this.ns2prefix.put(ns, new LinkedHashSet<String>(5));
		LinkedHashSet<String> ps = this.ns2prefix.get(ns);
		ps.add(prefix);
		
	}

	@Override
	public String getNamespaceURI(String ns) {
		return this.prefix2ns.get(ns);
	}

	@Override
	public String getPrefix(String ns) {
		for (String prefix : this.ns2prefix.get(ns))
			return prefix;
		return null;
	}

	@Override
	public Iterator<String> getPrefixes(String ns) {
		return this.ns2prefix.containsKey(ns) ? this.ns2prefix.get(ns).iterator() : Collections.<String>emptyIterator();
	}
}