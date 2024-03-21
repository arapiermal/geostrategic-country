package com.erimali.cntrygame;

import java.util.function.Function;

import javafx.geometry.Point2D;

class Robinson {
	private final double mapWidth;
	private final double mapHeight;
	private final double earthRadius;
	private final double fudgeX;
	private final double fudgeY;
	private final double[] AA;
	private final double[] BB;

	public Robinson(double mapWidth, double mapHeight, double fudgeX, double fudgeY) {
		this.mapWidth = mapWidth;
		this.mapHeight = mapHeight;
		this.earthRadius = (mapWidth / 2.666269758) / 2;
		this.fudgeX = fudgeX;
		this.fudgeY = fudgeY;
		this.AA = new double[] { 0.8487, 0.84751182, 0.84479598, 0.840213, 0.83359314, 0.8257851, 0.814752, 0.80006949,
				0.78216192, 0.76060494, 0.73658673, 0.7086645, 0.67777182, 0.64475739, 0.60987582, 0.57134484,
				0.52729731, 0.48562614, 0.45167814 };
		this.BB = new double[] { 0, 0.0838426, 0.1676852, 0.2515278, 0.3353704, 0.419213, 0.5030556, 0.5868982,
				0.67182264, 0.75336633, 0.83518048, 0.91537187, 0.99339958, 1.06872269, 1.14066505, 1.20841528,
				1.27035062, 1.31998003, 1.3523 };
	}

	public Point projectToCSS(double lat, double lng) {
		Point point = project(lat, lng);
		point.x = (point.x + (mapWidth / 2));
		point.y = ((mapHeight / 2) - point.y);
		return point;
	}

	double roundToNearest(double roundTo, double value) {
		return Math.floor(value / roundTo) * roundTo; // rounds down
	}

	int getSign(double value) {
		return value < 0 ? -1 : 1;
	}

	public Point project(double lat, double lng) {
		int lngSign = getSign(lng), latSign = getSign(lat); // deals with negatives
		lng = Math.abs(lng);
		lat = Math.abs(lat);
		double radian = 0.017453293; // pi/180
		double low = roundToNearest(5, lat - 0.0000000001); // want exact numbers to round down
		low = (lat == 0) ? 0 : low; // except when at 0
		double high = low + 5;

		// indices used for interpolation
		double lowIndex = low / 5;
		double highIndex = high / 5;
		double ratio = (lat - low) / 5;

		// interpolation in one dimension
		double adjAA = ((AA[(int) highIndex] - AA[(int) lowIndex]) * ratio) + AA[(int) lowIndex];
		double adjBB = ((BB[(int) highIndex] - BB[(int) lowIndex]) * ratio) + BB[(int) lowIndex];

		// create point from robinson function
		Point point = new Point();
		point.x = ((adjAA * lng * radian * lngSign * earthRadius) + fudgeX);
		point.y = ((adjBB * latSign * earthRadius) + fudgeY);

		return point;
	}

	private static class Point {
		double x;
		double y;
	}
}

public class WMCoordinate {

	protected static double width = WorldMap.mapWidth;
	protected static double height = WorldMap.mapHeight;

	public static Point2D calc(double lat, double lng) {
		double i = 5;
		Function<Double, Integer> n = (e) -> e < 0 ? -1 : 1;
		int r = n.apply(lng); // x has this
		int a = n.apply(lat); // y has this
		double s = Math.abs(lng);
		double l = Math.abs(lat);
		double t = l - 1e-10;
		double c = Math.floor(t / i) * i;
		c = (l == 0) ? 0 : c;
		double m = (l == 0) ? 0 : c + 5;

		double u = c / 5;
		double p = m / 5;
		double d = (l - c) / 5;

		double[] f = { 0.8487, 0.84751182, 0.84479598, 0.840213, 0.83359314, 0.8257851, 0.814752, 0.80006949,
				0.78216192, 0.76060494, 0.73658673, 0.7086645, 0.67777182, 0.64475739, 0.60987582, 0.57134484,
				0.52729731, 0.48562614, 0.45167814 };

		double[] h = { 0, 0.0838426, 0.1676852, 0.2515278, 0.3353704, 0.419213, 0.5030556, 0.5868982, 0.67182264,
				0.75336633, 0.83518048, 0.91537187, 0.99339958, 1.06872269, 1.14066505, 1.20841528, 1.27035062,
				1.31998003, 1.3523 };
		//ADDED
		double earthRadius = (width / 2.666269758) / 2;
		double x = ((f[(int) p] - f[(int) u]) * d + f[(int) u]) * s * 0.017453293 * r * earthRadius;
		double y = ((h[(int) p] - h[(int) u]) * d + h[(int) u]) * a * earthRadius;

		return toMapPoint(x, y);
	}

	public static Point2D toMapPoint(double X, double Y) {
		double x = (X + (width / 2));
		double y = ((height / 2) - Y);
		//double x = (Math.PI + X) * (width / (2 * Math.PI));
		//double y = (Math.PI - Y) * (height/(2*Math.PI));
		return new Point2D(x, y);
	}


	// lat -> from -90 to 90
	// lng -> from -180 to 180

	public static void main(String... args) {

		Point2D paris = WMCoordinate.calc(48.8566, 2.3522);
		Point2D tirana = WMCoordinate.calc(41.3275, 19.8187);
		Point2D tokyo = WMCoordinate.calc(35.666666670, 139.750000000);
		TESTING.print(paris, tirana, tokyo);
		Point2D h01 = WMCoordinate.calc(90, 0);
		Point2D h02 = WMCoordinate.calc(-90, 0);
		TESTING.print(h01, h02);

		Point2D h1 = WMCoordinate.calc(0, -180);
		Point2D h2 = WMCoordinate.calc(0, 180);
		TESTING.print(h1, h2);
	}

	public static Point2D mapEasy(double lat, double lng) {
		double x = width / 2 + (lng / 180) * (width / 2);
		double y = height / 2 - (lat / 90) * (height / 2);
		return new Point2D(x, y);
	}
}
