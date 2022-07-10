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

package org.applied_geodesy.adjustment.cmd;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.logging.LogManager;

import org.applied_geodesy.adjustment.EstimationStateType;
import org.applied_geodesy.adjustment.network.NetworkAdjustment;
import org.applied_geodesy.adjustment.network.sql.SQLAdjustmentManager;
import org.applied_geodesy.util.sql.HSQLDB;

public class OpenAdjustmentCMD {
	private boolean displayState;
	private HSQLDB dataBase;
	private AdjustmentStateListener adjustmentStateListener = new AdjustmentStateListener();
	
	private class AdjustmentStateListener implements PropertyChangeListener {
		@Override
		public void propertyChange(PropertyChangeEvent evt) {
			String name = evt.getPropertyName();

			EstimationStateType state = EstimationStateType.valueOf(name);
			if (state == null)
				return;

			Object oldValue = evt.getOldValue();
			Object newValue = evt.getNewValue();
			
			if (displayState)
				System.out.println("Current state: " + name + " (" + newValue + "/" + oldValue + ")");
		}
	}
	
	public OpenAdjustmentCMD(String dataBaseName) {
		this(dataBaseName, true);
	}
	
	public OpenAdjustmentCMD(String dataBaseName, boolean displayState) {
		this.dataBase = new HSQLDB(dataBaseName);
		this.displayState = displayState;
	}
	
	public int process() throws Exception {
		EstimationStateType returnType = EstimationStateType.NOT_INITIALISED;
		
		boolean isOpen = false;
		try {
			isOpen = this.dataBase.isOpen();
			if (!isOpen)
				this.dataBase.open();

			SQLAdjustmentManager adjustmentManager = new SQLAdjustmentManager(this.dataBase);
			NetworkAdjustment adjustment = adjustmentManager.getNetworkAdjustment();

			adjustment.addPropertyChangeListener(this.adjustmentStateListener);
			returnType = adjustment.estimateModel();
			this.destroyNetworkAdjustment(adjustment);

			adjustmentManager.saveResults();
			adjustmentManager.clear();
		}
		finally {
			if (this.dataBase != null && !isOpen)
				this.dataBase.close();
		}
		return returnType.getId();
	}

	private void destroyNetworkAdjustment(NetworkAdjustment adjustment) {
		if (adjustment != null) {
			adjustment.removePropertyChangeListener(this.adjustmentStateListener);
			adjustment.clearMatrices();
			adjustment = null;
		}
	}
	
	public static void main(String[] args)  {
		try {
			System.setProperty("com.github.fommil.netlib.BLAS",   "com.github.fommil.netlib.F2jBLAS");
			System.setProperty("com.github.fommil.netlib.LAPACK", "com.github.fommil.netlib.F2jLAPACK");
			System.setProperty("com.github.fommil.netlib.ARPACK", "com.github.fommil.netlib.F2jARPACK");

			System.setProperty("hsqldb.reconfig_logging", "false");

			LogManager.getLogManager().reset();
		}
		catch (Exception e) {
			e.printStackTrace();
		}

		int status = -1;

		if (args.length == 0)
			throw new IllegalArgumentException("Error, no database specified!");

		String dataBaseName = args[0];
		boolean displayState = args.length > 1 && args[1].equalsIgnoreCase("TRUE") ? Boolean.TRUE : Boolean.FALSE; 
		try {
			if (!Files.isRegularFile(Paths.get(dataBaseName + ".script")) || 
					!Files.isRegularFile(Paths.get(dataBaseName + ".properties")) ||
					!Files.isRegularFile(Paths.get(dataBaseName + ".data")))
				throw new IOException("Error, related database files (e.g. script, properties or data) not found! " + dataBaseName);

			OpenAdjustmentCMD openAdjustment = new OpenAdjustmentCMD(dataBaseName, displayState);
			status = openAdjustment.process();
		}
		catch (Exception e) {
			e.printStackTrace();
		}
		System.exit(status);
	}
}
