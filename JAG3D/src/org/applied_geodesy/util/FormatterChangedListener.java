package org.applied_geodesy.util;


import java.util.EventListener;

public interface FormatterChangedListener extends EventListener {
	/**
	 * Benachrichtigt ein Objekt ueber Aenderungen des Formatters
	 * @param evt
	 */
	public void formatterChanged(FormatterEvent evt);
}
