package org.applied_geodesy.jag3d.ui.menu;

import org.applied_geodesy.jag3d.ui.JavaGraticule3D;
import org.applied_geodesy.jag3d.ui.dialog.AboutDialog;
import org.applied_geodesy.jag3d.ui.dialog.ApproximationValuesDialog;
import org.applied_geodesy.jag3d.ui.dialog.AverageDialog;
import org.applied_geodesy.jag3d.ui.dialog.CongruentPointDialog;
import org.applied_geodesy.jag3d.ui.dialog.FormatterOptionDialog;
import org.applied_geodesy.jag3d.ui.dialog.LeastSquaresSettingDialog;
import org.applied_geodesy.jag3d.ui.dialog.ProjectionDialog;
import org.applied_geodesy.jag3d.ui.dialog.RankDefectDialog;
import org.applied_geodesy.jag3d.ui.dialog.TestStatisticDialog;

import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.control.MenuItem;

public class MenuEventHandler implements EventHandler<ActionEvent> {
	private UIMenuBuilder menuBuilder;
	MenuEventHandler(UIMenuBuilder menuBuilder) {
		this.menuBuilder = menuBuilder;
	}

	@Override
	public void handle(ActionEvent event) {
		if (event.getSource() instanceof MenuItem && ((MenuItem)event.getSource()).getUserData() instanceof MenuItemType) {
			MenuItemType menuItemType = (MenuItemType)((MenuItem)event.getSource()).getUserData();
			handleAction(menuItemType);
		}
	}
	
	private void handleAction(MenuItemType menuItemType) {
		switch(menuItemType) {
		case EXIT:
			JavaGraticule3D.close();
			break;
		case ABOUT:
			AboutDialog.showAndWait();
			break;
		case NEW:
			this.menuBuilder.newProject();
			break;
		case OPEN:
			this.menuBuilder.openProject();
			break;
		case PREFERENCES:
			FormatterOptionDialog.showAndWait();
			break;
		case LEAST_SQUARES:
			LeastSquaresSettingDialog.showAndWait();
			break;
		case TEST_STATISTIC:
			TestStatisticDialog.showAndWait();
			break;
		case RANK_DEFECT:
			RankDefectDialog.showAndWait();
			break;
		case HORIZONTAL_PROJECTION:
			ProjectionDialog.showAndWait();
			break;
		case CONGRUENT_POINT:
			CongruentPointDialog.showAndWait();
			break;
		case APROXIMATE_VALUES:
			ApproximationValuesDialog.showAndWait();
			break;
		case AVERAGE:
			AverageDialog.showAndWait();
			break;
		case IMPORT_FLAT_DIRECTION:
		case IMPORT_FLAT_GNSS1D:
		case IMPORT_FLAT_GNSS2D:
		case IMPORT_FLAT_GNSS3D:
		case IMPORT_FLAT_HORIZONTAL_DISTANCE:
		case IMPORT_FLAT_LEVELLING:
		case IMPORT_FLAT_SLOPE_DISTANCE:
		case IMPORT_FLAT_ZENITH_ANGLE:
		case IMPORT_BEO:
		case IMPORT_GSI1D:
		case IMPORT_GSI2D:
		case IMPORT_GSI2DH:
		case IMPORT_GSI3D:
		case IMPORT_LAND_XML2D:
		case IMPORT_LAND_XML3D:
		case IMPORT_M5:
		case IMPORT_JOB_XML2D:
		case IMPORT_JOB_XML2DH:
		case IMPORT_JOB_XML3D:
		case IMPORT_DL100:
		case IMPORT_Z:
			this.menuBuilder.importFile(menuItemType);
			break;
		}
	}
}

