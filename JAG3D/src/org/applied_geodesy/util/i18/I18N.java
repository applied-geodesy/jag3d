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
//		if (key == null || this.bundle == null || !this.bundle.containsKey(key))
//			System.err.println(this.getClass().getSimpleName() + " WARNUNG: keinen Wert fuer Schluessel " + key + " gefunden. Verwende Default-Value: " + defaultValue);
		return (key == null || this.bundle == null || !this.bundle.containsKey(key)) ? defaultValue : this.bundle.getString(key);
	}
}
