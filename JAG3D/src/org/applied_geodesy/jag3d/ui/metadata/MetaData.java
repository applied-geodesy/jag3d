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

package org.applied_geodesy.jag3d.ui.metadata;

import java.time.LocalDate;

import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

public class MetaData {
	private StringProperty name = new SimpleStringProperty();
	private StringProperty operator = new SimpleStringProperty();
	private StringProperty customerId = new SimpleStringProperty();
	private StringProperty projectId = new SimpleStringProperty();
	private StringProperty description = new SimpleStringProperty();
	private ObjectProperty<LocalDate> date = new SimpleObjectProperty<LocalDate>(LocalDate.now());
	public StringProperty nameProperty() {
		return this.name;
	}
	
	public String getName() {
		return this.nameProperty().get();
	}
	
	public void setName(final String name) {
		this.nameProperty().set(name);
	}
	
	public StringProperty operatorProperty() {
		return this.operator;
	}
	
	public String getOperator() {
		return this.operatorProperty().get();
	}
	
	public void setOperator(final String operator) {
		this.operatorProperty().set(operator);
	}
	
	public StringProperty customerIdProperty() {
		return this.customerId;
	}
	
	public String getCustomerId() {
		return this.customerIdProperty().get();
	}
	
	public void setCustomerId(final String customerId) {
		this.customerIdProperty().set(customerId);
	}
	
	public StringProperty projectIdProperty() {
		return this.projectId;
	}
	
	public String getProjectId() {
		return this.projectIdProperty().get();
	}
	
	public void setProjectId(final String projectId) {
		this.projectIdProperty().set(projectId);
	}
	
	public StringProperty descriptionProperty() {
		return this.description;
	}
	
	public String getDescription() {
		return this.descriptionProperty().get();
	}
	
	public void setDescription(final String description) {
		this.descriptionProperty().set(description);
	}
	
	public ObjectProperty<LocalDate> dateProperty() {
		return this.date;
	}
	
	public LocalDate getDate() {
		return this.dateProperty().get();
	}
	
	public void setDate(final LocalDate date) {
		this.dateProperty().set(date);
	}
}
