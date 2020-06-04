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

package org.applied_geodesy.adjustment.geometry;

public class Quaternion  {
	private double q[] = new double[4];
	public Quaternion() { }
	
	public Quaternion(double n[], double alpha){
		// Einheitsvektor aus Achsvektor erzeugen
		double n0[] = this.normalise(n);
		double c = Math.cos(0.5*alpha);
		double s = Math.sin(0.5*alpha);
		// q = [s, v] = [cos(alpha/2), n0*sin(alpha/2)]
		this.q[0] = c;
		this.q[1] = s*n0[0];
		this.q[2] = s*n0[1];
		this.q[3] = s*n0[2];
	}
	
	public Quaternion(double v[]){
		this(v.length == 4 ? v[0] : 0.0, v.length == 4 ? v[1] : v[0], v.length == 4 ? v[2] : v[1], v.length == 4 ? v[3] : v[2]);
	}
	
	public Quaternion(double R[][]){
		this(toQuaternion(R));
	}
	
	public Quaternion(Quaternion q){
		this(q.getQ0(), q.getQ1(), q.getQ2(), q.getQ3());
	}
	
	public Quaternion(double q0, double q1, double q2, double q3){
		this.q[0] = q0;
		this.q[1] = q1;
		this.q[2] = q2;
		this.q[3] = q3;
	}
	
	public double getArg(int i){
		return this.q[i];
	}
	
	public double getQ0(){
		return this.q[0];
	}
	
	public double getQ1(){
		return this.q[1];
	}
	
	public double getQ2(){
		return this.q[2];
	}
	
	public double getQ3(){
		return this.q[3];
	}
	
	public double[] toArray() {
		return this.q;
	}
	
	/**
	 * Rotiert ein Quaternion q
	 * @param q
	 * @return qR
	 */
	public Quaternion rotate(Quaternion q) {
		Quaternion invQ = this.inv();
		Quaternion tmpQ = this.times(q);
		Quaternion qR = tmpQ.times(invQ);
		return qR;
	}
	
	/**
	 * Rotiert einen 3D-Vektor um q
	 * @param p
	 * @return qR
	 */
	public Quaternion rotate(double p[]) {
		return this.rotate(new Quaternion(p));
	}
	
	/**
	 * Liefert das Inverse Quaternion
	 * @return inv(q)
	 */
	public Quaternion inv() {
		Quaternion cQ = this.conj();
		double abs2 = Math.pow(this.abs(),2);
		return new Quaternion(cQ.getArg(0)/abs2, cQ.getArg(1)/abs2, cQ.getArg(2)/abs2, cQ.getArg(3)/abs2);
	}

	/**
	 * Liefert das konjugiert Quaternion q
	 * [s, v] = [s, -v]
	 * @return cQ
	 */
	public Quaternion conj() {
		double qc[] = new double[this.q.length];
		for (int i=1; i<this.q.length; i++)
			qc[i] = -this.q[i];
		qc[0] = this.q[0];
		return new Quaternion(qc[0], qc[1], qc[2], qc[3]);
	}
	
	/**
	 * Betrag des Quaternion = Vektornorm
	 * a = sqrt(s*s + x*x + y*y + z*z)
	 * @return norm(q)
	 */
	public double abs() {
		return this.norm(this.q);
	}

	/**
	 * Multipliziert zwei Quaternione und gibt das Produkt zurueck
	 * q = [s, v]
	 * q*q' = [s*s' - v*v', v x v' + s*v' + s'*v]
	 * @param q
	 * @return q1*q2
	 */
	public Quaternion times(Quaternion q) {
		double mulQ[] = new double[4];

		mulQ[0] = this.q[0]*q.getArg(0) - this.q[1]*q.getArg(1) - this.q[2]*q.getArg(2) - this.q[3]*q.getArg(3);

		mulQ[1] = this.q[0]*q.getArg(1) + q.getArg(0)*this.q[1] + this.q[2]*q.getArg(3) - this.q[3]*q.getArg(2);
		mulQ[2] = this.q[0]*q.getArg(2) + q.getArg(0)*this.q[2] - this.q[1]*q.getArg(3) + this.q[3]*q.getArg(1);
		mulQ[3] = this.q[0]*q.getArg(3) + q.getArg(0)*this.q[3] + this.q[1]*q.getArg(2) - this.q[2]*q.getArg(1);
		return new Quaternion(mulQ[0], mulQ[1], mulQ[2], mulQ[3]);
	}
	
	/**
	 * Liefert die zu q aequivaltente Rotationsmatrix R
	 * @return R
	 */
	public double[][] toRotationMatrix() {
		return toRotationMatrix(this);
	}
	
	/**
	 * Liefert die zur Rotationsmatrix R aequivaltente Quaternion q
	 * Kuipers pp. 168
	 * @param R
	 * @return q
	 */
	public static Quaternion toQuaternion(double R[][]) {
		double m11 = R[0][0];
		double m22 = R[1][1];
		double m33 = R[2][2];
		
		double m23 = R[1][2];
		double m32 = R[2][1];
		
		double m31 = R[2][0];
		double m13 = R[0][2];
		
		double m12 = R[0][1];
		double m21 = R[1][0];
		
		double q0 = 0.5 * Math.sqrt(Math.abs(m11 + m22 + m33 + 1.0));
//		double q1 = (m23 - m32) / 4.0 / q0;
//		double q2 = (m31 - m13) / 4.0 / q0;
//		double q3 = (m12 - m21) / 4.0 / q0;
		
		double q1 = (m32 - m23) / 4.0 / q0;
		double q2 = (m13 - m31) / 4.0 / q0;
		double q3 = (m21 - m12) / 4.0 / q0;

		return new Quaternion(q0, q1, q2, q3);
	}
	
	/**
	 * Liefert die zu q aequivaltente Rotationsmatrix R
	 * Kuipers pp. 168
	 * @param q
	 * @return R
	 */
	public static double[][] toRotationMatrix(Quaternion q) {
		double R[][] = new double[3][3];
		double q0 = q.getQ0();
		double q1 = q.getQ1();
		double q2 = q.getQ2();
		double q3 = q.getQ3();

//		R[0] = new double[] {
//				2.0*q0*q0 - 1.0 + 2.0*q1*q1,  2.0*(q1*q2 + q0*q3),  2.0*(q1*q3 - q0*q2)	
//		};
//		
//		R[1] = new double[] {
//				2.0*(q1*q2 - q0*q3),  2.0*q0*q0 - 1.0 + 2.0*q2*q2,  2.0*(q2*q3 + q0*q1)
//		};
//		
//		R[2] = new double[] {
//				2.0*(q1*q3 + q0*q2),  2.0*(q2*q3 - q0*q1),  2.0*q0*q0 - 1.0 + 2.0*q3*q3
//		};

		R[0] = new double[] {
				2.0*q0*q0 - 1.0 + 2.0*q1*q1,  2.0*(q1*q2 - q0*q3),  2.0*(q1*q3 + q0*q2)	
		};
		
		R[1] = new double[] {
				2.0*(q1*q2 + q0*q3),  2.0*q0*q0 - 1.0 + 2.0*q2*q2,  2.0*(q2*q3 - q0*q1)
		};
		
		R[2] = new double[] {
				2.0*(q1*q3 - q0*q2),  2.0*(q2*q3 + q0*q1),  2.0*q0*q0 - 1.0 + 2.0*q3*q3
		};

		return R;
	}
	
	/**
	 * Vektornome
	 * @param n
	 * @return norm
	 */
	private double norm(double n[]) {
		double norm = 0;
		for (int i=0; i<n.length; i++)
			norm = norm + n[i] * n[i];
		return Math.sqrt(norm);
	}
	
	/**
	 * Normiert einen Vektor n
	 * @param n
	 * @return n0
	 */
	private double[] normalise(double n[]) {
		double norm = this.norm(n);
		if (norm<=0)
			throw new IllegalArgumentException("Error, norm of axis vector is equal or less then zero!");
	
		for (int i=0; i<n.length; i++)
			n[i] = n[i]/norm;
	
		return n;
	}
	
	@Override
	public String toString(){
		return new String("s = "+this.q[0]+"  q = ["+this.q[1]+", "+this.q[2]+" ,"+this.q[3]+"]");
	}
	
	public double[] getRotationAxis() {
		double n[] = new double[3];
		double q0 = this.q[0];
		double s = Math.sin(Math.acos(q0));
		if (s == 0)
			return n;
		for (int i=0; i<n.length; i++)
			n[i] = this.q[i+1]/s;
		return this.normalise(n);
	}
	
	public double[] getEulerAngles() {
		double q0 = this.getQ0();
		double q1 = this.getQ1();
		double q2 = this.getQ2();
		double q3 = this.getQ3();
		
		double r13 = 2.0*(q1*q3 + q0*q2);
		double r23 = 2.0*(q2*q3 - q0*q1);
		double r33 = 2.0*q0*q0 - 1.0 + 2.0*q3*q3;
		
		double r12 = 2.0*(q1*q2 - q0*q3);
		double r11 = 2.0*q0*q0 - 1.0 + 2.0*q1*q1;
		
		double rx = Math.atan2( r23, r33);
		double ry = Math.atan2(-r13, Math.hypot(r23, r33));
		double rz = Math.atan2( r12, r11);
		
		return new double[]{
				rx, ry,rz
		};
	}
}