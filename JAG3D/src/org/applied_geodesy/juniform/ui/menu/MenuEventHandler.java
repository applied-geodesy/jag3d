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

package org.applied_geodesy.juniform.ui.menu;

import java.io.File;

import org.applied_geodesy.adjustment.geometry.Feature;
import org.applied_geodesy.adjustment.geometry.FeatureType;
import org.applied_geodesy.adjustment.geometry.GeometricPrimitive;
import org.applied_geodesy.adjustment.geometry.curve.CircleFeature;
import org.applied_geodesy.adjustment.geometry.curve.EllipseFeature;
import org.applied_geodesy.adjustment.geometry.curve.LineFeature;
import org.applied_geodesy.adjustment.geometry.curve.ModifiableCurveFeature;
import org.applied_geodesy.adjustment.geometry.curve.QuadraticCurveFeature;
import org.applied_geodesy.adjustment.geometry.surface.CircularConeFeature;
import org.applied_geodesy.adjustment.geometry.surface.CircularCylinderFeature;
import org.applied_geodesy.adjustment.geometry.surface.CircularParaboloidFeature;
import org.applied_geodesy.adjustment.geometry.surface.ConeFeature;
import org.applied_geodesy.adjustment.geometry.surface.CylinderFeature;
import org.applied_geodesy.adjustment.geometry.surface.EllipsoidFeature;
import org.applied_geodesy.adjustment.geometry.surface.ModifiableSurfaceFeature;
import org.applied_geodesy.adjustment.geometry.surface.ParaboloidFeature;
import org.applied_geodesy.adjustment.geometry.surface.PlaneFeature;
import org.applied_geodesy.adjustment.geometry.surface.QuadraticSurfaceFeature;
import org.applied_geodesy.adjustment.geometry.surface.SpatialCircleFeature;
import org.applied_geodesy.adjustment.geometry.surface.SpatialEllipseFeature;
import org.applied_geodesy.adjustment.geometry.surface.SpatialLineFeature;
import org.applied_geodesy.adjustment.geometry.surface.SphereFeature;
import org.applied_geodesy.juniform.io.FeaturePointFileReader;
import org.applied_geodesy.juniform.ui.JUniForm;
import org.applied_geodesy.juniform.ui.dialog.AboutDialog;
import org.applied_geodesy.juniform.ui.dialog.FeatureDialog;
import org.applied_geodesy.juniform.ui.dialog.FormatterOptionDialog;
import org.applied_geodesy.juniform.ui.dialog.LeastSquaresSettingDialog;
import org.applied_geodesy.juniform.ui.dialog.QuantilesDialog;
import org.applied_geodesy.juniform.ui.dialog.RestrictionDialog;
import org.applied_geodesy.juniform.ui.dialog.TestStatisticDialog;
import org.applied_geodesy.juniform.ui.dialog.UnknownParameterDialog;
import org.applied_geodesy.juniform.ui.dialog.VarianceComponentsDialog;
import org.applied_geodesy.juniform.ui.menu.UIMenuBuilder.DisableStateType;
import org.applied_geodesy.juniform.ui.tree.UITreeBuilder;
import org.applied_geodesy.juniform.ui.i18n.I18N;

import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.control.MenuItem;

class MenuEventHandler implements EventHandler<ActionEvent> {
	private UIMenuBuilder menuBuilder;
	private I18N i18n = I18N.getInstance();
	private UITreeBuilder treeBuilder = UITreeBuilder.getInstance();

	MenuEventHandler(UIMenuBuilder menuBuilder) {
		this.menuBuilder = menuBuilder;
	}

	@Override
	public void handle(ActionEvent event) {
		if (event.getSource() instanceof MenuItem) {
			MenuItem menuItem = (MenuItem)event.getSource();
			handleAction(menuItem);
		}
	}
	
	void handleAction(MenuItem menuItem) {
		Feature feature = null;
		MenuItemType menuItemType = null;
		File file = null;
		
		if (menuItem.getUserData() instanceof GeometricPrimitive) {
			this.menuBuilder.importInitialGuess((GeometricPrimitive)menuItem.getUserData());
			return;
		}
		else if (menuItem.getUserData() instanceof MenuItemType) {
			menuItemType = (MenuItemType)menuItem.getUserData();
			file = menuItem instanceof FileMenuItem ? ((FileMenuItem)menuItem).getFile() : null;
		}
		
		if (menuItemType == null)
			return;
		
		switch(menuItemType) {
		case IMPORT_CURVE_POINTS:
			this.menuBuilder.importFile(new FeaturePointFileReader(FeatureType.CURVE), FeaturePointFileReader.getExtensionFilters(), i18n.getString("UIMenuBuilder.filechooser.import.point.curve.title", "Import curve points from flat files"));
			break;

		case IMPORT_SURFACE_POINTS:
			this.menuBuilder.importFile(new FeaturePointFileReader(FeatureType.SURFACE), FeaturePointFileReader.getExtensionFilters(), i18n.getString("UIMenuBuilder.filechooser.import.point.surface.title", "Import surface points from flat files"));
			break;
			
		case FEATURE_PROPERTIES:
			FeatureDialog.showAndWait(this.treeBuilder.getFeatureAdjustment().getFeature());
			break;
			
		case PARAMETER_PROPERTIES:
			UnknownParameterDialog.showAndWait(this.treeBuilder.getFeatureAdjustment().getFeature());
			break;
			
		case RESTRICTION_PROPERTIES:
			RestrictionDialog.showAndWait(this.treeBuilder.getFeatureAdjustment().getFeature(), false);
			break;
			
		case POSTPROCESSING_PROPERTIES:
			RestrictionDialog.showAndWait(this.treeBuilder.getFeatureAdjustment().getFeature(), true);
			break;
			
		case TEST_STATISTIC:
			TestStatisticDialog.showAndWait(this.treeBuilder.getFeatureAdjustment().getTestStatisticDefinition());
			break;
			
		case LEAST_SQUARES:
			LeastSquaresSettingDialog.showAndWait(this.treeBuilder.getFeatureAdjustment());
			break;
			
		case PREFERENCES:
			FormatterOptionDialog.showAndWait();
			break;
			
		case QUANTILES:
			QuantilesDialog.showAndWait(this.treeBuilder.getFeatureAdjustment() == null ? null : this.treeBuilder.getFeatureAdjustment().getTestStatisticParameters());
			break;
			
		case VARIANCE_COMPONENT_ESTIMATION:
			VarianceComponentsDialog.showAndWait(this.treeBuilder.getFeatureAdjustment() == null ? null : this.treeBuilder.getFeatureAdjustment().getVarianceComponentOfUnitWeight());
			break;
			
		case EXIT:
			JUniForm.close();
			break;
			
		case LINE:
			feature = new LineFeature();
			break;
			
		case CIRCLE:			
			feature = new CircleFeature();
			break;
			
		case ELLIPSE:
			feature = new EllipseFeature();
			break;
			
		case QUADRATIC_CURVE:
			feature = new QuadraticCurveFeature();
			break;
			
		case PLANE:		
			feature = new PlaneFeature();
			break;
			
		case SPHERE:
			feature = new SphereFeature();
			break;
			
		case ELLIPSOID:
			feature = new EllipsoidFeature();
			break;
			
		case SPATIAL_CIRCLE:
			feature = new SpatialCircleFeature();
			break;
			
		case SPATIAL_ELLIPSE:
			feature = new SpatialEllipseFeature();
			break;
		
		case SPATIAL_LINE:
			feature = new SpatialLineFeature();
			break;
			
		case CIRCULAR_CYLINDER:
			feature = new CircularCylinderFeature();
			break;
			
		case CYLINDER:
			feature = new CylinderFeature();
			break;
			
		case CIRCULAR_CONE:
			feature = new CircularConeFeature();
			break;
			
		case CONE:
			feature = new ConeFeature();
			break;
			
		case CIRCULAR_PARABOLOID:
			feature = new CircularParaboloidFeature();
			break;
			
		case PARABOLOID:
			feature = new ParaboloidFeature();
			break;
			
		case QUADRATIC_SURFACE:
			feature = new QuadraticSurfaceFeature();
			break;
			
		case MODIFIABLE_CURVE:
			feature = new ModifiableCurveFeature();
			break;
			
		case MODIFIABLE_SURFACE:
			feature = new ModifiableSurfaceFeature();
			break;
			
		case REPORT:
			this.menuBuilder.createReport(file);
			break;
			
		case ABOUT:
			AboutDialog.showAndWait();
			break;
			
		}
		
		if (feature != null) {
			JUniForm.setTitle(menuItem.getText());
			this.menuBuilder.disableMenu(DisableStateType.FEATURE);
			this.treeBuilder.getFeatureAdjustment().setFeature(feature);
			this.treeBuilder.handleTreeSelections();
		}
		else if (this.treeBuilder.getFeatureAdjustment().getFeature() == null)
			JUniForm.setTitle(null);
	}
}
