/* zandgall

 ## Util
 # A set of global utility functions

 : MADE IN NEOVIM */

package com.zandgall.plorpland.util;

public class Util {
	
	/**
	 * Calculate the minimum distance from angle A to B, and whether it's to the
	 * left or right
	 * 
	 * @param a Angle 1 in radians
	 * @param b Angle 2 in radians
	 * @return Signed distance from a to b
	 */
	public static double signedAngularDistance(double a, double b) {
		double phi = (a - b) % Math.TAU;
		if (phi < -Math.PI)
			return phi + Math.TAU;
		else if (phi > Math.PI)
			return phi - Math.TAU;
		return phi;
	}


}
