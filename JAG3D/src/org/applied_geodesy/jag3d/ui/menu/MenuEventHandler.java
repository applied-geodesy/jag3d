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

package org.applied_geodesy.jag3d.ui.menu;

import java.io.File;

import org.applied_geodesy.jag3d.sql.SQLManager;
import org.applied_geodesy.jag3d.ui.JAG3D;
import org.applied_geodesy.jag3d.ui.dialog.AboutDialog;
import org.applied_geodesy.jag3d.ui.dialog.ApproximationValuesDialog;
import org.applied_geodesy.jag3d.ui.dialog.AverageDialog;
import org.applied_geodesy.jag3d.ui.dialog.CongruentPointDialog;
import org.applied_geodesy.jag3d.ui.dialog.FormatterOptionDialog;
import org.applied_geodesy.jag3d.ui.dialog.LeastSquaresSettingDialog;
import org.applied_geodesy.jag3d.ui.dialog.ProjectionDialog;
import org.applied_geodesy.jag3d.ui.dialog.RankDefectDialog;
import org.applied_geodesy.jag3d.ui.dialog.TableRowHighlightDialog;
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
		if (event.getSource() instanceof MenuItem) {
			MenuItem menuItem = (MenuItem)event.getSource();
			if (menuItem.getUserData() instanceof MenuItemType) {
				MenuItemType menuItemType = (MenuItemType)menuItem.getUserData();
				File file = menuItem instanceof FileMenuItem ? ((FileMenuItem)menuItem).getFile() : null;
				handleAction(menuItemType, file);
			}
		}
	}
	
	private void handleAction(MenuItemType menuItemType, File file) {
		switch(menuItemType) {
		case EXIT:
			JAG3D.close();
			break;
			
		case CLOSE:
			SQLManager.closeProject();
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
			
		case COPY:
			this.menuBuilder.copyProject();
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
			
		case CHECK_UPDATES:
			this.menuBuilder.checkUpdates();
			break;
			
		case IMPORT_FLAT_DIRECTION:
		case IMPORT_FLAT_GNSS1D:
		case IMPORT_FLAT_GNSS2D:
		case IMPORT_FLAT_GNSS3D:
		case IMPORT_FLAT_HORIZONTAL_DISTANCE:
		case IMPORT_FLAT_LEVELING:
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
			
		case IMPORT_FLAT_REFERENCE_POINT_1D:
		case IMPORT_FLAT_REFERENCE_POINT_2D:
		case IMPORT_FLAT_REFERENCE_POINT_3D:
		case IMPORT_FLAT_STOCHASTIC_POINT_1D:
		case IMPORT_FLAT_STOCHASTIC_POINT_2D:
		case IMPORT_FLAT_STOCHASTIC_POINT_3D:
		case IMPORT_FLAT_DATUM_POINT_1D:
		case IMPORT_FLAT_DATUM_POINT_2D:
		case IMPORT_FLAT_DATUM_POINT_3D:
		case IMPORT_FLAT_NEW_POINT_1D:
		case IMPORT_FLAT_NEW_POINT_2D:
		case IMPORT_FLAT_NEW_POINT_3D:
			
		case IMPORT_FLAT_CONGRUENCE_ANALYSIS_PAIR_1D:
		case IMPORT_FLAT_CONGRUENCE_ANALYSIS_PAIR_2D:
		case IMPORT_FLAT_CONGRUENCE_ANALYSIS_PAIR_3D:
			
		case IMPORT_COLUMN_BASED_FILES:
			this.menuBuilder.importFile(menuItemType);
			break;
			
		case HIGHLIGHT_TABLE_ROWS:
			TableRowHighlightDialog.showAndWait();
			break;
			
		case MODULE_COORDTRANS:
		case MODULE_FORMFITTINGTOOLBOX:
		case MODULE_GEOTRA:
			this.menuBuilder.showSwingApplication(menuItemType);
			break;

		case REPORT:
			this.menuBuilder.createReport(file);
			break;
			
		case RECENTLY_USED:
			this.menuBuilder.openProject(file);
			break;
		}
	}
}

