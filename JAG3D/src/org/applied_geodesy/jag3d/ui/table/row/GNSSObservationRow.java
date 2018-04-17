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

package org.applied_geodesy.jag3d.ui.table.row;

import java.util.Locale;
import java.util.Scanner;

import org.applied_geodesy.util.FormatterOptions;

import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;

public class GNSSObservationRow extends ObservationRow {
	private ObjectProperty<Double> xApriori = new SimpleObjectProperty<Double>(this, "xApriori", 0.0);
	private ObjectProperty<Double> yApriori = new SimpleObjectProperty<Double>(this, "yApriori", 0.0);
	private ObjectProperty<Double> zApriori = new SimpleObjectProperty<Double>(this, "zApriori", 0.0);
	
	private ObjectProperty<Double> sigmaXapriori = new SimpleObjectProperty<Double>(this, "sigmaXapriori");
	private ObjectProperty<Double> sigmaYapriori = new SimpleObjectProperty<Double>(this, "sigmaYapriori");
	private ObjectProperty<Double> sigmaZapriori = new SimpleObjectProperty<Double>(this, "sigmaZapriori");
	
	private ObjectProperty<Double> xAposteriori = new SimpleObjectProperty<Double>(this, "xAposteriori");
	private ObjectProperty<Double> yAposteriori = new SimpleObjectProperty<Double>(this, "yAposteriori");
	private ObjectProperty<Double> zAposteriori = new SimpleObjectProperty<Double>(this, "zAposteriori");
	
	private ObjectProperty<Double> sigmaXaposteriori = new SimpleObjectProperty<Double>(this, "sigmaXaposteriori");
	private ObjectProperty<Double> sigmaYaposteriori = new SimpleObjectProperty<Double>(this, "sigmaYaposteriori");
	private ObjectProperty<Double> sigmaZaposteriori = new SimpleObjectProperty<Double>(this, "sigmaZaposteriori");

	private ObjectProperty<Double> minimalDetectableBiasX = new SimpleObjectProperty<Double>(this, "minimalDetectableBiasX");
	private ObjectProperty<Double> minimalDetectableBiasY = new SimpleObjectProperty<Double>(this, "minimalDetectableBiasY");
	private ObjectProperty<Double> minimalDetectableBiasZ = new SimpleObjectProperty<Double>(this, "minimalDetectableBiasZ");
	
	private ObjectProperty<Double> residualX = new SimpleObjectProperty<Double>(this, "residualX");
	private ObjectProperty<Double> residualY = new SimpleObjectProperty<Double>(this, "residualY");
	private ObjectProperty<Double> residualZ = new SimpleObjectProperty<Double>(this, "residualZ");
	
	private ObjectProperty<Double> redundancyX = new SimpleObjectProperty<Double>(this, "redundancyX");
	private ObjectProperty<Double> redundancyY = new SimpleObjectProperty<Double>(this, "redundancyY");
	private ObjectProperty<Double> redundancyZ = new SimpleObjectProperty<Double>(this, "redundancyZ");
	
	private ObjectProperty<Double> grossErrorX = new SimpleObjectProperty<Double>(this, "grossErrorX");
	private ObjectProperty<Double> grossErrorY = new SimpleObjectProperty<Double>(this, "grossErrorY");
	private ObjectProperty<Double> grossErrorZ = new SimpleObjectProperty<Double>(this, "grossErrorZ");
	
	private ObjectProperty<Double> influenceOnPointPositionX = new SimpleObjectProperty<Double>(this, "influenceOnPointPositionX");
	private ObjectProperty<Double> influenceOnPointPositionY = new SimpleObjectProperty<Double>(this, "influenceOnPointPositionY");
	private ObjectProperty<Double> influenceOnPointPositionZ = new SimpleObjectProperty<Double>(this, "influenceOnPointPositionZ");

	public ObjectProperty<Double> xAprioriProperty() {
		return this.xApriori;
	}
	
	public Double getXApriori() {
		return this.xAprioriProperty().get();
	}
	
	public void setXApriori(final Double xApriori) {
		this.xAprioriProperty().set(xApriori);
	}
	
	public ObjectProperty<Double> yAprioriProperty() {
		return this.yApriori;
	}
	
	public Double getYApriori() {
		return this.yAprioriProperty().get();
	}
	
	public void setYApriori(final Double yApriori) {
		this.yAprioriProperty().set(yApriori);
	}
	
	public ObjectProperty<Double> zAprioriProperty() {
		return this.zApriori;
	}
	
	public Double getZApriori() {
		return this.zAprioriProperty().get();
	}
	
	public void setZApriori(final Double zApriori) {
		this.zAprioriProperty().set(zApriori);
	}
	
	public ObjectProperty<Double> sigmaXaprioriProperty() {
		return this.sigmaXapriori;
	}
	
	public Double getSigmaXapriori() {
		return this.sigmaXaprioriProperty().get();
	}

	public void setSigmaXapriori(final Double sigmaXapriori) {
		this.sigmaXaprioriProperty().set(sigmaXapriori);
	}
	
	public ObjectProperty<Double> sigmaYaprioriProperty() {
		return this.sigmaYapriori;
	}
	
	public Double getSigmaYapriori() {
		return this.sigmaYaprioriProperty().get();
	}

	public void setSigmaYapriori(final Double sigmaYapriori) {
		this.sigmaYaprioriProperty().set(sigmaYapriori);
	}
	
	public ObjectProperty<Double> sigmaZaprioriProperty() {
		return this.sigmaZapriori;
	}
	
	public Double getSigmaZapriori() {
		return this.sigmaZaprioriProperty().get();
	}
	
	public void setSigmaZapriori(final Double sigmaZapriori) {
		this.sigmaZaprioriProperty().set(sigmaZapriori);
	}
	
	public ObjectProperty<Double> xAposterioriProperty() {
		return this.xAposteriori;
	}
	
	public Double getXAposteriori() {
		return this.xAposterioriProperty().get();
	}
	
	public void setXAposteriori(final Double xAposteriori) {
		this.xAposterioriProperty().set(xAposteriori);
	}
	
	public ObjectProperty<Double> yAposterioriProperty() {
		return this.yAposteriori;
	}
	
	public Double getYAposteriori() {
		return this.yAposterioriProperty().get();
	}
	
	public void setYAposteriori(final Double yAposteriori) {
		this.yAposterioriProperty().set(yAposteriori);
	}
	
	public ObjectProperty<Double> zAposterioriProperty() {
		return this.zAposteriori;
	}
	
	public Double getZAposteriori() {
		return this.zAposterioriProperty().get();
	}
	
	public void setZAposteriori(final Double zAposteriori) {
		this.zAposterioriProperty().set(zAposteriori);
	}
	
	public ObjectProperty<Double> sigmaXaposterioriProperty() {
		return this.sigmaXaposteriori;
	}
	
	public Double getSigmaXaposteriori() {
		return this.sigmaXaposterioriProperty().get();
	}
	
	public void setSigmaXaposteriori(final Double sigmaXaposteriori) {
		this.sigmaXaposterioriProperty().set(sigmaXaposteriori);
	}
	
	public ObjectProperty<Double> sigmaYaposterioriProperty() {
		return this.sigmaYaposteriori;
	}
	
	public Double getSigmaYaposteriori() {
		return this.sigmaYaposterioriProperty().get();
	}
	
	public void setSigmaYaposteriori(final Double sigmaYaposteriori) {
		this.sigmaYaposterioriProperty().set(sigmaYaposteriori);
	}
	
	public ObjectProperty<Double> sigmaZaposterioriProperty() {
		return this.sigmaZaposteriori;
	}
	
	public Double getSigmaZaposteriori() {
		return this.sigmaZaposterioriProperty().get();
	}
	
	public void setSigmaZaposteriori(final Double sigmaZaposteriori) {
		this.sigmaZaposterioriProperty().set(sigmaZaposteriori);
	}
	
	public ObjectProperty<Double> minimalDetectableBiasXProperty() {
		return this.minimalDetectableBiasX;
	}
	
	public Double getMinimalDetectableBiasX() {
		return this.minimalDetectableBiasXProperty().get();
	}
	
	public void setMinimalDetectableBiasX(final Double minimalDetectableBiasX) {
		this.minimalDetectableBiasXProperty().set(minimalDetectableBiasX);
	}
	
	public ObjectProperty<Double> minimalDetectableBiasYProperty() {
		return this.minimalDetectableBiasY;
	}
	
	public Double getMinimalDetectableBiasY() {
		return this.minimalDetectableBiasYProperty().get();
	}
	
	public void setMinimalDetectableBiasY(final Double minimalDetectableBiasY) {
		this.minimalDetectableBiasYProperty().set(minimalDetectableBiasY);
	}
	
	public ObjectProperty<Double> minimalDetectableBiasZProperty() {
		return this.minimalDetectableBiasZ;
	}
	
	public Double getMinimalDetectableBiasZ() {
		return this.minimalDetectableBiasZProperty().get();
	}
	
	public void setMinimalDetectableBiasZ(final Double minimalDetectableBiasZ) {
		this.minimalDetectableBiasZProperty().set(minimalDetectableBiasZ);
	}
	
	public final ObjectProperty<Double> residualXProperty() {
		return this.residualX;
	}

	public final Double getResidualX() {
		return this.residualXProperty().get();
	}

	public final void setResidualX(final Double residualX) {
		this.residualXProperty().set(residualX);
	}

	public final ObjectProperty<Double> residualYProperty() {
		return this.residualY;
	}

	public final Double getResidualY() {
		return this.residualYProperty().get();
	}

	public final void setResidualY(final Double residualY) {
		this.residualYProperty().set(residualY);
	}

	public final ObjectProperty<Double> residualZProperty() {
		return this.residualZ;
	}

	public final Double getResidualZ() {
		return this.residualZProperty().get();
	}

	public final void setResidualZ(final Double residualZ) {
		this.residualZProperty().set(residualZ);
	}
	
	public ObjectProperty<Double> redundancyXProperty() {
		return this.redundancyX;
	}
	
	public Double getRedundancyX() {
		return this.redundancyXProperty().get();
	}
	
	public void setRedundancyX(final Double redundancyX) {
		this.redundancyXProperty().set(redundancyX);
	}
	
	public ObjectProperty<Double> redundancyYProperty() {
		return this.redundancyY;
	}
	
	public Double getRedundancyY() {
		return this.redundancyYProperty().get();
	}
	
	public void setRedundancyY(final Double redundancyY) {
		this.redundancyYProperty().set(redundancyY);
	}
	
	public ObjectProperty<Double> redundancyZProperty() {
		return this.redundancyZ;
	}
	
	public Double getRedundancyZ() {
		return this.redundancyZProperty().get();
	}
	
	public void setRedundancyZ(final Double redundancyZ) {
		this.redundancyZProperty().set(redundancyZ);
	}
	
	public ObjectProperty<Double> grossErrorXProperty() {
		return this.grossErrorX;
	}
	
	public Double getGrossErrorX() {
		return this.grossErrorXProperty().get();
	}
	
	public void setGrossErrorX(final Double nablaX) {
		this.grossErrorXProperty().set(nablaX);
	}
	
	public ObjectProperty<Double> grossErrorYProperty() {
		return this.grossErrorY;
	}
	
	public Double getGrossErrorY() {
		return this.grossErrorYProperty().get();
	}
	
	public void setGrossErrorY(final Double nablaY) {
		this.grossErrorYProperty().set(nablaY);
	}
	
	public ObjectProperty<Double> grossErrorZProperty() {
		return this.grossErrorZ;
	}
	
	public Double getGrossErrorZ() {
		return this.grossErrorZProperty().get();
	}
	
	public void setGrossErrorZ(final Double nablaZ) {
		this.grossErrorZProperty().set(nablaZ);
	}
	
	public ObjectProperty<Double> influenceOnPointPositionXProperty() {
		return this.influenceOnPointPositionX;
	}
	
	public Double getInfluenceOnPointPositionX() {
		return this.influenceOnPointPositionXProperty().get();
	}
	
	public void setInfluenceOnPointPositionX(final Double influenceOnPointPositionX) {
		this.influenceOnPointPositionXProperty().set(influenceOnPointPositionX);
	}
	
	public ObjectProperty<Double> influenceOnPointPositionYProperty() {
		return this.influenceOnPointPositionY;
	}
	
	public Double getInfluenceOnPointPositionY() {
		return this.influenceOnPointPositionYProperty().get();
	}
	
	public void setInfluenceOnPointPositionY(final Double influenceOnPointPositionY) {
		this.influenceOnPointPositionYProperty().set(influenceOnPointPositionY);
	}
	
	public ObjectProperty<Double> influenceOnPointPositionZProperty() {
		return this.influenceOnPointPositionZ;
	}
	
	public Double getInfluenceOnPointPositionZ() {
		return this.influenceOnPointPositionZProperty().get();
	}
	
	public void setInfluenceOnPointPositionZ(final Double influenceOnPointPositionZ) {
		this.influenceOnPointPositionZProperty().set(influenceOnPointPositionZ);
	}

	public static GNSSObservationRow cloneRowApriori(GNSSObservationRow row) {
		GNSSObservationRow clone = new GNSSObservationRow();
		
		clone.setId(-1);
		clone.setGroupId(row.getGroupId());
		clone.setEnable(row.isEnable());
		
		clone.setStartPointName(row.getStartPointName());
		clone.setEndPointName(row.getEndPointName());
		
		clone.setXApriori(row.getXApriori());
		clone.setYApriori(row.getYApriori());
		clone.setZApriori(row.getZApriori());
		
		clone.setSigmaXapriori(row.getSigmaXapriori());
		clone.setSigmaYapriori(row.getSigmaYapriori());
		clone.setSigmaZapriori(row.getSigmaZapriori());

		return clone;
	}

	public static GNSSObservationRow scan(String str, int dim) {
		if (dim == 1)
			return GNSSObservationRow.scan1D(str);
		else if (dim == 2)
			return GNSSObservationRow.scan2D(str);
		else if (dim == 3)
			return GNSSObservationRow.scan3D(str);
		return null;
	}

	private static GNSSObservationRow scan1D(String str) {
		FormatterOptions options = FormatterOptions.getInstance();
		Scanner scanner = new Scanner( str.trim() );
		try {
			scanner.useLocale( Locale.ENGLISH );
			String startPointName = new String(), 
					endPointName  = new String(); 
			double z = 0, sigmaZ = 0;
			GNSSObservationRow row = new GNSSObservationRow();
			
			// station
			if (!scanner.hasNext())
				return null;
			startPointName = scanner.next();

			// target
			if (!scanner.hasNext())
				return null;
			endPointName = scanner.next();	

			if (startPointName.equals(endPointName))
				return null;
			
			row.setStartPointName(startPointName);
			row.setEndPointName(endPointName);

			// Z 		
			if (!scanner.hasNextDouble())
				return null;
			z = options.convertLengthToModel(scanner.nextDouble());

			// sigma Z
			if (!scanner.hasNextDouble()) {
				row.setZApriori(z);
				return row;
			}
			sigmaZ = options.convertLengthToModel(scanner.nextDouble());

			row.setZApriori(z);
			row.setSigmaZapriori(sigmaZ);
			
			return row;
		}
		finally {
			scanner.close();
		}
	}

	private static GNSSObservationRow scan2D(String str) {
		FormatterOptions options = FormatterOptions.getInstance();
		Scanner scanner = new Scanner( str.trim() );
		try {
			scanner.useLocale( Locale.ENGLISH );
			String startPointName = new String(), 
					endPointName  = new String(); 
			double x = 0, y = 0, sigmaX = 0, sigmaY = 0;
			GNSSObservationRow row = new GNSSObservationRow();
			
			// station
			if (!scanner.hasNext())
				return null;
			startPointName = scanner.next();

			// target
			if (!scanner.hasNext())
				return null;
			endPointName = scanner.next();	

			if (startPointName.equals(endPointName))
				return null;
			
			row.setStartPointName(startPointName);
			row.setEndPointName(endPointName);

			// Y 		
			if (!scanner.hasNextDouble())
				return null;
			y = options.convertLengthToModel(scanner.nextDouble());

			// X
			if (!scanner.hasNextDouble())
				return null;
			x = options.convertLengthToModel(scanner.nextDouble());

			// sigma Y (and X)
			if (!scanner.hasNextDouble()) {
				row.setXApriori(x);
				row.setYApriori(y);
				return row;
			}
			sigmaX = sigmaY = options.convertLengthToModel(scanner.nextDouble());

			// sigma Y
			if (!scanner.hasNextDouble()) {
				row.setXApriori(x);
				row.setYApriori(y);
				row.setSigmaXapriori(sigmaX);
				row.setSigmaYapriori(sigmaY);
				return row;

			}
			sigmaX = options.convertLengthToModel(scanner.nextDouble());

			row.setXApriori(x);
			row.setYApriori(y);
			row.setSigmaXapriori(sigmaX);
			row.setSigmaYapriori(sigmaY);
			return row;
		}
		finally {
			scanner.close();
		}
	}

	private static GNSSObservationRow scan3D(String str) {
		FormatterOptions options = FormatterOptions.getInstance();
		Scanner scanner = new Scanner( str.trim() );
		try {
			scanner.useLocale( Locale.ENGLISH );
			String startPointName = new String(), 
					endPointName  = new String(); 
			double x = 0, y = 0, z = 0, sigmaX = 0, sigmaY = 0, sigmaZ = 0;
			GNSSObservationRow row = new GNSSObservationRow();
			
			// station
			if (!scanner.hasNext())
				return null;
			startPointName = scanner.next();

			// target
			if (!scanner.hasNext())
				return null;
			endPointName = scanner.next();	

			if (startPointName.equals(endPointName))
				return null;
			
			row.setStartPointName(startPointName);
			row.setEndPointName(endPointName);

			// Y 		
			if (!scanner.hasNextDouble())
				return null;
			y = options.convertLengthToModel(scanner.nextDouble());

			// X
			if (!scanner.hasNextDouble())
				return null;
			x = options.convertLengthToModel(scanner.nextDouble());

			// Z
			if (!scanner.hasNextDouble())
				return null;
			z = options.convertLengthToModel(scanner.nextDouble());

			// sigma Y (and X,Z)
			if (!scanner.hasNextDouble()) {
				row.setXApriori(x);
				row.setYApriori(y);
				row.setZApriori(z);
				return row;
			}
			sigmaY = sigmaX = sigmaZ = options.convertLengthToModel(scanner.nextDouble());

			// sigma X (or Z)
			if (!scanner.hasNextDouble()) {
				row.setXApriori(x);
				row.setYApriori(y);
				row.setZApriori(z);
				row.setSigmaXapriori(sigmaX);
				row.setSigmaYapriori(sigmaY);
				row.setSigmaZapriori(sigmaZ);
				return row;
			}

			sigmaX = sigmaZ = options.convertLengthToModel(scanner.nextDouble());

			// sigma Y (or Z)
			if (!scanner.hasNextDouble()) {
				row.setXApriori(x);
				row.setYApriori(y);
				row.setZApriori(z);
				row.setSigmaXapriori(sigmaY);
				row.setSigmaYapriori(sigmaY);
				row.setSigmaZapriori(sigmaZ);
				return row;
			}

			sigmaZ = options.convertLengthToModel(scanner.nextDouble());

			row.setXApriori(x);
			row.setYApriori(y);
			row.setZApriori(z);
			row.setSigmaXapriori(sigmaX);
			row.setSigmaYapriori(sigmaY);
			row.setSigmaZapriori(sigmaZ);
			return row;
		}
		finally {
			scanner.close();
		}
	}
}
