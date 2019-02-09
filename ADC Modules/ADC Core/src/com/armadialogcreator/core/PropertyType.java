package com.armadialogcreator.core;

import com.armadialogcreator.core.old.ControlPropertyLookup;
import com.armadialogcreator.core.sv.*;
import com.armadialogcreator.lang.Lang;
import org.jetbrains.annotations.NotNull;

import java.util.LinkedList;

/**
 @author Kayler
 @since 07/15/2016. */
public enum PropertyType {
	/** Is a integer value. Current implementation is a 32 bit integer (java int) */
	Int(0, SVExpression.CONVERTER, getString("PropertyType.int")),
	/** Is a floating point value. The current implementation uses 64 bit floating point (java double) */
	Float(1, SVExpression.CONVERTER, getString("PropertyType.float")),
	/** Unique property type to {@link ControlPropertyLookup#STYLE} */
	ControlStyle(2, SVControlStyleGroup.CONVERTER, getString("PropertyType.control_style")),
	/** Denotes an image path inside a String */
	Image(3, SVImage.CONVERTER, getString("PropertyType.image"), true),
	/** Is a boolean (0 for false, 1 for true) */
	Boolean(4, SVBoolean.CONVERTER, getString("PropertyType.boolean")),
	/** Is a String */
	String(5, SVString.CONVERTER, getString("PropertyType.string"), true),
	/** Generic array property type */
	Array(6, SVArray.CONVERTER, getString("PropertyType.array"), 2, PropertyTypeHelper.EMPTY),
	/** Color array string ({r,g,b,a} where r,g,b,a are from 0 to 1 inclusively) */
	Color(7, SVColorArray.CONVERTER, getString("PropertyType.color"), 4, PropertyTypeHelper.EMPTY),
	/** Is an array that is formatted to fit a sound and its params */
	Sound(8, SVSound.CONVERTER, getString("PropertyType.sound"), 3, 0),
	/** Is font name */
	Font(9, SVFont.CONVERTER, getString("PropertyType.font"), true),
	/** Denotes a file name inside a String */
	FileName(10, SVFileName.CONVERTER, getString("PropertyType.file_name"), true),
	/** Color is set to a hex string like #ffffff or #ffffffff */
	HexColorString(11, SVHexColor.CONVERTER, getString("PropertyType.hex_color_string"), true),
	/** example: #(argb,8,8,3)color(1,1,1,1) however there is more than one way to set texture */
	Texture(12, SVString.CONVERTER, getString("PropertyType.texture"), true),
	/** SQF code String */
	SQF(13, SVString.CONVERTER, getString("PropertyType.sqf"), true),
	/** Raw value */
	Raw(14, SVRaw.CONVERTER, getString("PropertyType.raw"));

	private final int propertyValuesSize;
	private final String displayName;
	private final StringArrayConverter<? extends SerializableValue> converter;
	private final int id;
	private int[] quoteIndexes;

	PropertyType(int id, StringArrayConverter<? extends SerializableValue> converter, String displayName) {
		this(id, converter, displayName, 1);
	}

	PropertyType(int id, StringArrayConverter<? extends SerializableValue> converter, String displayName, boolean exportHasQuotes) {
		if (PropertyTypeHelper.usedIds.contains(id)) {
			throw new IllegalStateException("used id:" + id);
		}
		PropertyTypeHelper.usedIds.add(id);
		this.id = id;
		this.converter = converter;
		this.propertyValuesSize = 1;
		this.displayName = displayName;
		if (exportHasQuotes) {
			this.quoteIndexes = PropertyTypeHelper.FIRST_INDEX;
		} else {
			this.quoteIndexes = PropertyTypeHelper.EMPTY;
		}
	}

	PropertyType(int id, StringArrayConverter<? extends SerializableValue> converter, String displayName, int propertyValueSize, int... quoteIndexes) {
		if (PropertyTypeHelper.usedIds.contains(id)) {
			throw new IllegalStateException("used id:" + id);
		}
		PropertyTypeHelper.usedIds.add(id);
		this.id = id;
		if (propertyValueSize <= 0) {
			throw new IllegalArgumentException("Number of values must be >= 1");
		}
		this.converter = converter;
		this.displayName = displayName;
		propertyValuesSize = propertyValueSize;
		this.quoteIndexes = quoteIndexes;
	}

	@Override
	public String toString() {
		return getDisplayName();
	}

	/** @throws IllegalArgumentException when id couldn't be matched */
	@NotNull
	public static PropertyType findById(int id) {
		for (PropertyType propertyType : values()) {
			if (propertyType.getId() == id) {
				return propertyType;
			}
		}
		throw new IllegalArgumentException("id " + id + " couldn't be matched");
	}

	private static String getString(String s) {
		return Lang.LookupBundle().getString(s);
	}

	/** Number of values used to represent the data */
	public int getPropertyValuesSize() {
		return propertyValuesSize;
	}

	/**
	 Get an array of indexes describing which value in {@link #getPropertyValuesSize()} should be exported with quotes.
	 Examples:
	 <ul>
	 <li>{@link #getPropertyValuesSize()} == 2, this method returns {1}, so export should be {value, "value"}</li>
	 <li>{@link #getPropertyValuesSize()} == 1, this method returns {}, so export should be {value}</li>
	 <li>{@link #getPropertyValuesSize()} == 3, this method returns {0,2}, so export should be {"value", value, "value"}</li>
	 </ul>
	 */
	@NotNull
	public int[] getIndexesWithQuotes() {
		return quoteIndexes;
	}

	@NotNull
	public String getDisplayName() {
		return displayName;
	}

	@NotNull
	public StringArrayConverter<? extends SerializableValue> getConverter() {
		return converter;
	}

	/** Unique id. Used when saving for file. Do not change when assigned. */
	public int getId() {
		return id;
	}

	private static class PropertyTypeHelper {
		static final LinkedList<Integer> usedIds = new LinkedList<>();
		static final int[] EMPTY = {};
		public static final int[] FIRST_INDEX = {0};
	}
}
