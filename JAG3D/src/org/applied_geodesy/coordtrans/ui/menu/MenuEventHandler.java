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

package org.applied_geodesy.coordtrans.ui.menu;

import java.nio.file.Path;

import org.applied_geodesy.adjustment.transformation.VarianceComponent;
import org.applied_geodesy.adjustment.transformation.VarianceComponentType;
import org.applied_geodesy.coordtrans.ui.CoordTrans;
import org.applied_geodesy.coordtrans.ui.dialog.AboutDialog;
import org.applied_geodesy.coordtrans.ui.dialog.FormatterOptionDialog;
import org.applied_geodesy.coordtrans.ui.dialog.LeastSquaresSettingDialog;
import org.applied_geodesy.coordtrans.ui.dialog.QuantilesDialog;
import org.applied_geodesy.coordtrans.ui.dialog.TestStatisticDialog;
import org.applied_geodesy.coordtrans.ui.dialog.VarianceComponentsDialog;
import org.applied_geodesy.coordtrans.ui.tree.UITreeBuilder;
import org.applied_geodesy.jag3d.ui.menu.PathMenuItem;

import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.control.MenuItem;

class MenuEventHandler implements EventHandler<ActionEvent> {
	private UIMenuBuilder menuBuilder;
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
		MenuItemType menuItemType = null;
		Path path = null;
		
		if (menuItem.getUserData() instanceof MenuItemType) {
			menuItemType = (MenuItemType)menuItem.getUserData();
			path = menuItem instanceof PathMenuItem ? ((PathMenuItem)menuItem).getPath() : null;
		}
		
		if (menuItemType == null)
			return;
		
		switch(menuItemType) {
		case IMPORT_POSITIONS:
			this.menuBuilder.importPositions();
			break;
			
		case TEST_STATISTIC:
			TestStatisticDialog.showAndWait(this.treeBuilder.getTransformationAdjustment().getTestStatisticDefinition());
			break;
			
		case LEAST_SQUARES:
			LeastSquaresSettingDialog.showAndWait(this.treeBuilder.getTransformationAdjustment());
			break;
			
		case QUANTILES:
			QuantilesDialog.showAndWait(this.treeBuilder.getTransformationAdjustment() == null ? null : this.treeBuilder.getTransformationAdjustment().getTestStatisticParameters());
			break;
			
		case VARIANCE_COMPONENT_ESTIMATION:
			VarianceComponentsDialog.showAndWait(this.treeBuilder.getTransformationAdjustment() == null ? null : new VarianceComponent[] {
					this.treeBuilder.getTransformationAdjustment().getVarianceComponent(VarianceComponentType.GLOBAL),
					this.treeBuilder.getTransformationAdjustment().getVarianceComponent(VarianceComponentType.SOURCE),
					this.treeBuilder.getTransformationAdjustment().getVarianceComponent(VarianceComponentType.TARGET)
			});
			break;
			
		case PREFERENCES:
			FormatterOptionDialog.showAndWait();
			break;
			
		case EXIT:
			CoordTrans.close();
			break;
			
		case EXPORT_MATLAB:
			this.menuBuilder.createMatlabFile();
			break;
			
		case REPORT:
			this.menuBuilder.createReport(path);
			break;
			
		case ABOUT:
			AboutDialog.showAndWait();
			break;
		}
	}
}
