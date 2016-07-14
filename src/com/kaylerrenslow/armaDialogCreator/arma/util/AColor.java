package com.kaylerrenslow.armaDialogCreator.arma.util;

import com.kaylerrenslow.armaDialogCreator.control.SerializableValue;
import javafx.scene.paint.Color;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.text.DecimalFormat;

/**
 @author Kayler
 Defines a color.
 Created on 05/22/2016. */
public class AColor implements SerializableValue {
	private static DecimalFormat format = new DecimalFormat("#.####");

	/** cache the javaFX color */
	private Color javafxColor;
	private boolean updateJavafxColor = false;

	/** Color array where each value is ranged from 0.0 - 1.0 inclusively. Format=[r,g,b,a] */
	protected double[] color = new double[4];

	/**
	 Creates a color

	 @param r red (range 0-1.0)
	 @param g green (range 0-1.0)
	 @param b blue (range 0-1.0)
	 @param a alpha (range 0-1.0)
	 @throws IllegalArgumentException when r,g,b, or a are less than 0 or greater than 1
	 */
	public AColor(double r, double g, double b, double a) {
		setRed(r);
		setGreen(g);
		setBlue(b);
		setAlpha(a);
	}

	/**
	 Creates a color

	 @param r red (range 0-255)
	 @param g green (range 0-255)
	 @param b blue (range 0-255)
	 @param a alpha (range 0-255)
	 @throws IllegalArgumentException when r,g,b, or a are less than 0 or greater than 255
	 */
	public AColor(int r, int g, int b, int a) {
		final double f = 255.0;
		setRed(r / f);
		setGreen(g / f);
		setBlue(b / f);
		setAlpha(a / f);
	}

	/**
	 Creates a color from a double array of length 4

	 @param c the color array that must have length=4
	 @throws IllegalArgumentException when r,g,b, or a are less than 0 or greater than 1. Also throws it when c.length != 4
	 */
	public AColor(double[] c) {
		setColor(c);
	}

	/** Set the color from a JavaFX Color instance */
	public AColor(Color newValue) {
		this(newValue.getRed(), newValue.getGreen(), newValue.getBlue(), newValue.getOpacity());
		this.javafxColor = newValue;
		updateJavafxColor = false;
	}

	/**
	 Create a new color from String array that is formatted like so: {r,g,b,a} where r,g,b,a are between 0.0 and 1.0 inclusively

	 @throws NumberFormatException     when the string array is not formatted correctly
	 @throws IndexOutOfBoundsException when string array is not of proper size (must be length 4)
	 */
	public AColor(String[] newValue) throws NumberFormatException, IndexOutOfBoundsException {
		this(Double.parseDouble(newValue[0]), Double.parseDouble(newValue[1]), Double.parseDouble(newValue[2]), Double.parseDouble(newValue[3]));
	}

	private void boundCheck(double c) {
		if (c < 0.0 || c > 1.0) {
			throw new IllegalArgumentException("Color value is out of range (must be >=0 and <=1): " + c);
		}
	}

	public double getRed() {
		return color[0];
	}

	/**
	 Set red.

	 @throws IllegalArgumentException when value is less than 0 or greater than 1
	 */
	public void setRed(double r) {
		boundCheck(r);
		color[0] = r;
		updateJavafxColor = true;
	}

	public double getGreen() {
		return color[1];
	}

	/**
	 Set green.

	 @throws IllegalArgumentException when value is less than 0 or greater than 1
	 */
	public void setGreen(double g) {
		boundCheck(g);
		color[1] = g;
		updateJavafxColor = g != color[1];
	}

	public double getBlue() {
		return color[2];
	}

	/**
	 Set blue.

	 @throws IllegalArgumentException when value is less than 0 or greater than 1
	 */
	public void setBlue(double b) {
		boundCheck(b);
		updateJavafxColor = b != color[2];
		color[2] = b;
	}

	public double getAlpha() {
		return color[3];
	}

	/**
	 Set alpha.

	 @throws IllegalArgumentException when value is less than 0 or greater than 1
	 */
	public void setAlpha(double a) {
		boundCheck(a);
		color[3] = a;
		updateJavafxColor = a != color[3];
	}

	public double[] getColors() {
		return color;
	}

	/** Takes out the color values 1 by 1 and injects them into this instances' color array. */
	public void setColor(double[] c) {
		if (c.length != 4) {
			throw new IllegalArgumentException("array length must be 4");
		}
		setRed(c[0]);
		setGreen(c[1]);
		setBlue(c[2]);
		setAlpha(c[3]);
	}

	/** Get the colors as a string array formatted like so: {red, green, blue, alpha} */
	public String[] getAsStringArray() {
		String[] str = new String[4];
		str[0] = format.format(color[0]);
		str[1] = format.format(color[1]);
		str[2] = format.format(color[2]);
		str[3] = format.format(color[3]);
		return str;
	}

	/** Serializes the color array into a String. Example: 0 red, 0.1 green, 0.2 blue, 0.3 alpha becomes "{0.0,0.1,0.2,0.3}" */
	public String toString() {
		return "{" + format.format(color[0]) + "," + format.format(color[1]) + "," + format.format(color[2]) + "," + format.format(color[3]) + "}";
	}

	@Nullable
	/** Takes a serialized color array String and converts it into a double array. Example (both curly braces and square brackets are allowed): "{0.0,0.1,0.2,0.3}"  becomes  0 red, 0.1 green, 0.2 blue, 0.3 alpha
	 @returns null when string is improperly formatted, otherwise will return a double array of dimension 4*/
	public double[] arrayFromText(@NotNull String colorAsArray) {
		try {
			String[] split = colorAsArray.replaceAll("\\[|\\]|\\{|\\}", "").split(",");

			double[] d = new double[4];
			for (int i = 0; i < d.length; i++) {
				d[i] = Double.parseDouble(split[i].trim());
				if (d[i] < 0 || d[i] > 1) {
					return null;
				}
			}
			return d;
		} catch (Exception e) {
			return null;
		}
	}

	/** Convert this color into a JavaFX color */
	public Color toJavaFXColor() {
		if (updateJavafxColor || javafxColor == null) {
			updateJavafxColor = false;
			javafxColor = Color.color(getRed(), getGreen(), getBlue(), getAlpha());
		}
		return javafxColor;
	}

	/**
	 Create a new JavaFX Color from String array that is formatted like so: {r,g,b,a} where r,g,b,a are between 0.0 and 1.0 inclusively

	 @throws NumberFormatException     when the string array is not formatted correctly
	 @throws IndexOutOfBoundsException when string array is not of proper size (must be length 4)
	 */
	public static Color toJavaFXColor(String[] newValue) throws NumberFormatException {
		return Color.color(Double.parseDouble(newValue[0]), Double.parseDouble(newValue[1]), Double.parseDouble(newValue[2]), Double.parseDouble(newValue[3]));
	}


	/**
	 Gets color array (formatted: [r,g,b,a])

	 @param arr stores values in given array (array must be length 4)
	 */
	public static double[] getColorArray(double[] arr, int color) {
		if (arr.length != 4) {
			throw new IllegalArgumentException("arr.length != 4");
		}
		int r = (color) & 0xFF;
		int g = (color >> 8) & 0xFF;
		int b = (color >> 16) & 0xFF;
		int a = (color >> 24) & 0xFF;
		final double f = 255.0;
		arr[0] = r / f;
		arr[1] = g / f;
		arr[2] = b / f;
		arr[3] = a / f;
		return arr;
	}

	/** Returns what {@link #getColorArray(int)} would, with new array of length 4 */
	public static double[] getColorArray(int color) {
		return getColorArray(new double[4], color);
	}
}
