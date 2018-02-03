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
