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

import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.Map;

import javax.xml.XMLConstants;
import javax.xml.namespace.NamespaceContext;

import org.w3c.dom.Attr;
import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

abstract class XMLNamespaceContext implements NamespaceContext {
	// https://www.ibm.com/developerworks/library/x-nmspccontext/
	private final String defaultPrefix;
	private Map<String, String> prefix2Uri = new HashMap<String, String>();
	private Map<String, LinkedHashSet<String>> uri2Prefix = new HashMap<String, LinkedHashSet<String>>();

	XMLNamespaceContext(Document document) {
		this(document, Boolean.FALSE);
	}
	
	XMLNamespaceContext(Document document, boolean topLevelOnly) {
		this(document, XMLConstants.DEFAULT_NS_PREFIX, XMLConstants.NULL_NS_URI, topLevelOnly);
	}
	
	XMLNamespaceContext(Document document, String defaultPrefix, String defaultURI) {
		this(document, defaultPrefix, defaultURI, Boolean.FALSE);
	}
	
	XMLNamespaceContext(Document document, String defaultPrefixes[], String defaultURIs[]) {
		this(document, defaultPrefixes, defaultURIs, Boolean.FALSE);
	}
	
	XMLNamespaceContext(Document document, String defaultPrefix, String defaultURI, boolean topLevelOnly) {
		this(document, new String[] {defaultPrefix}, new String[] {defaultURI}, Boolean.FALSE);
	}
	
	XMLNamespaceContext(Document document, String defaultPrefixes[], String defaultURIs[], boolean topLevelOnly) {
		this.defaultPrefix = defaultPrefixes == null || defaultPrefixes.length == 0 ? XMLConstants.DEFAULT_NS_PREFIX : defaultPrefixes[0];
		for (int i = 0; i < defaultPrefixes.length; i++) {
			this.putInCache(
					defaultPrefixes[i] == null ? XMLConstants.DEFAULT_NS_PREFIX : defaultPrefixes[i], 
					defaultURIs[i] == null ? XMLConstants.NULL_NS_URI : defaultURIs[i]
			);
		}

		this.examineNode(document.getFirstChild(), topLevelOnly);
//		System.out.println("\nThe list of the cached namespaces:");
//		for (String key : this.prefix2Uri.keySet()) {
//			System.out.println("prefix " + key + ": uri " + this.prefix2Uri.get(key));
//		}
//		System.out.println();
	}

	private void examineNode(Node node, boolean attributesOnly) {
		NamedNodeMap attributes = node.getAttributes();
		for (int i = 0; i < attributes.getLength(); i++) {
			Node attribute = attributes.item(i);
			this.storeAttribute((Attr) attribute);
		}

		if (!attributesOnly) {
			NodeList chields = node.getChildNodes();
			for (int i = 0; i < chields.getLength(); i++) {
				Node chield = chields.item(i);
				if (chield.getNodeType() == Node.ELEMENT_NODE)
					this.examineNode(chield, false);
			}
		}
	}

	private void storeAttribute(Attr attribute) {
		// examine the attributes in namespace xmlns
		if (attribute.getNamespaceURI() != null && attribute.getNamespaceURI().equals(XMLConstants.XMLNS_ATTRIBUTE_NS_URI)) {
			// Default namespace xmlns="uri goes here"
			if (attribute.getNodeName().equals(XMLConstants.XMLNS_ATTRIBUTE)) {
				String prefix = this.getPrefix(attribute);
				this.putInCache(prefix, attribute.getNodeValue());
			} else {
				// Here are the defined prefixes stored
				this.putInCache(attribute.getLocalName(), attribute.getNodeValue());
			}
		}
	}
	
	abstract String getPrefix(Attr attribute);
	
	String getDefaultPrefix() {
		return this.defaultPrefix;
	}

	private void putInCache(String prefix, String uri) {
		this.prefix2Uri.put(prefix, uri);
		if (!this.uri2Prefix.containsKey(uri))
			this.uri2Prefix.put(uri, new LinkedHashSet<String>(5));
		LinkedHashSet<String> ps = this.uri2Prefix.get(uri);
		ps.add(prefix);
	}

	@Override
	public String getNamespaceURI(String prefix) {
		if ((prefix == null || prefix.equals(XMLConstants.DEFAULT_NS_PREFIX)) && this.prefix2Uri.containsKey(this.defaultPrefix))
			return this.prefix2Uri.get(this.defaultPrefix);
		if (this.prefix2Uri.containsKey(prefix))
			return this.prefix2Uri.get(prefix);
		return XMLConstants.NULL_NS_URI;
	}

	@Override
	public String getPrefix(String namespaceURI) {
		for (String prefix : this.uri2Prefix.get(namespaceURI))
			return prefix;
		return null;
	}

	@Override
	public Iterator<String> getPrefixes(String namespaceURI) {
		return this.uri2Prefix.containsKey(namespaceURI) ? this.uri2Prefix.get(namespaceURI).iterator() : Collections.<String>emptyIterator();
	}
}
