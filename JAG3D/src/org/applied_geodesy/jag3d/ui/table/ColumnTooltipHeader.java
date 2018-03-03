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

package org.applied_geodesy.jag3d.ui.table;

import org.applied_geodesy.util.FormatterChangedListener;
import org.applied_geodesy.util.FormatterEvent;
import org.applied_geodesy.util.FormatterEventType;
import org.applied_geodesy.util.FormatterOptions;
import org.applied_geodesy.util.unit.Unit;

import javafx.beans.binding.Bindings;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.scene.control.Label;
import javafx.scene.control.Tooltip;

public class ColumnTooltipHeader implements FormatterChangedListener {
	private ObjectProperty<String> header      = new SimpleObjectProperty<String>();
	private ObjectProperty<String> labelText   = new SimpleObjectProperty<String>();
	private ObjectProperty<String> unitText    = new SimpleObjectProperty<String>();
	private ObjectProperty<String> tooltipText = new SimpleObjectProperty<String>();

	private final CellValueType type;
	private Label label     = new Label();
	private Tooltip tooltip = new Tooltip();
	private boolean displayUnit = false;
	ColumnTooltipHeader(CellValueType type, String label, String tooltip) {
		this(type, label, tooltip, null);
	}
	
	ColumnTooltipHeader(CellValueType type, String label, String tooltip, Unit unit) {
		FormatterOptions.getInstance().addFormatterChangedListener(this);
		this.type = type;
		this.displayUnit = unit != null;
		
		this.header.bind(Bindings.concat(this.labelText).concat(this.unitText));
		this.label.textProperty().bind(this.header);
		this.tooltip.textProperty().bind(this.tooltipText);
		
		this.labelText.set(label);
		this.unitText.set(this.displayUnit ? " [" + unit.getAbbreviation() + "]" : "");
		this.tooltipText.set(tooltip);
	}
	
	@Override
	public void formatterChanged(FormatterEvent evt) {
		if (this.displayUnit && evt.getEventType() == FormatterEventType.UNIT_CHANGED && this.type == evt.getCellType()) {
			Unit unit = evt.getNewUnit();
			this.unitText.set("[" + unit.getAbbreviation() + "]");
		}
	}

	Label getLabel() {
		return this.label;
	}
	
	Tooltip getTooltip() {
		return this.tooltip;
	}
}
