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

package org.applied_geodesy.jag3d.ui.resultpane;

import org.applied_geodesy.util.i18.I18N;

import javafx.scene.Node;
import javafx.scene.control.ListCell;

public class GlobalResultTypeListCell extends ListCell<Node> {
	private I18N i18n = I18N.getInstance();
	
	protected void updateItem(Node item, boolean empty){
		super.updateItem(item, empty);
	
		this.setGraphic(null);
		this.setText(null);
		
		if(item != null && !empty && item.getUserData() instanceof GlobalResultType) {
			this.setText(this.getText((GlobalResultType)item.getUserData()));
		}
	}
	
	private String getText(GlobalResultType type) {
		switch(type) {
		case TEST_STATISTIC:
			return i18n.getString("GlobalResultTableListCell.test_statistic.label", "Test statistics");
		case VARIANCE_COMPONENT:
			return i18n.getString("GlobalResultTableListCell.variance_component.label", "Variance components estimation");
		case PRINCIPAL_COMPONENT:
			return i18n.getString("GlobalResultTableListCell.principal_component.label", "Principal component analysis");
		}
		return null;
	}
}