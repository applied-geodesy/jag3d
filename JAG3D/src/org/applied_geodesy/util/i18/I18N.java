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

package org.applied_geodesy.util.i18;


import java.util.Locale;
import java.util.ResourceBundle;

public class I18N {
	private static I18N i18n = new I18N();
	private ResourceBundle bundle;
	private Locale currentLocale = Locale.ENGLISH;
	private String baseName = null;
	
	private I18N() {}
	
	public static I18N getInstance() {
		return i18n;
	}
	
	public static I18N getInstance(String baseName) {
		i18n.setBaseName(baseName);
		return i18n;
	}
	
	public static I18N getInstance(Locale locale, String baseName) {
		i18n.setLocale(locale);
		i18n.setBaseName(baseName);
		return i18n;
	}
	
	public void setLocale(Locale locale) {
		this.currentLocale = locale;
		this.loadBundle();
	}
	
	public void setBaseName(String baseName) {
		this.baseName = baseName;
		this.loadBundle();
	}
	
	public void loadBundle() {
		if (this.currentLocale != null && this.baseName != null) {
			try {
				ResourceBundle.clearCache();
				this.bundle = ResourceBundle.getBundle(this.baseName, this.currentLocale);
			}
			catch (Exception e) {
				e.printStackTrace();
			}
		}
	}
	
	public String getString(String key, String defaultValue) {
		if (key == null || this.bundle == null || !this.bundle.containsKey(key))
			System.err.println(this.getClass().getSimpleName() + " WARNING: Missing entry in lang file.\r\n" + key + " = " + defaultValue);
		return (key == null || this.bundle == null || !this.bundle.containsKey(key)) ? defaultValue : this.bundle.getString(key);
	}
}
