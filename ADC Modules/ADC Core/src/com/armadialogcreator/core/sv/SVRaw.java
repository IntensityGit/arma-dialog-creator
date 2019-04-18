package com.armadialogcreator.core.sv;

import com.armadialogcreator.core.PropertyType;
import com.armadialogcreator.expression.Env;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 A Raw value is literally anything. A Raw value should be thought of as a "substitute" for
 a different value.
 <p>
 A Raw value can't be converted (via {@link SerializableValue#convert(Env, SerializableValue, PropertyType)})
 into any {@link PropertyType}, except it's {@link #getSubstituteType()} (if specified).
 */
public final class SVRaw extends SerializableValue {

	public static final StringArrayConverter<SVRaw> CONVERTER = new StringArrayConverter<>() {
		@Override
		public SVRaw convert(@NotNull Env env, @NotNull String[] values) {
			return new SVRaw(values[0], null);
		}
	};
	private final String s;
	private final PropertyType substituteType;

	/** If s==null, "" (empty string) will be used */
	public SVRaw(@Nullable String s, @Nullable PropertyType substituteType) {
		this.s = s == null ? "" : s;
		this.substituteType = substituteType;
	}

	@NotNull
	public String getString() {
		return s;
	}

	@Override
	public SerializableValue deepCopy() {
		return new SVRaw(s, substituteType);
	}

	/**
	 @return the {@link PropertyType} that the value can convert into, or null if a substitute type wasn't provided
	 */
	@Nullable
	public PropertyType getSubstituteType() {
		return substituteType;
	}

	@NotNull
	@Override
	public PropertyType getPropertyType() {
		return PropertyType.Raw;
	}

	@Override
	public String toString() {
		return s;
	}

	@Override
	public boolean equals(Object o) {
		if (o == this) {
			return true;
		}
		if (o instanceof SVRaw) {
			SVRaw other = (SVRaw) o;
			return this.s.equals(other.s);
		}
		return false;
	}

	@NotNull
	@Override
	public String[] getAsStringArray() {
		return new String[]{s};
	}

	/**
	 @return null if {@link #getSubstituteType()} is null,
	 or new instance of {@link SerializableValue} that will have a {@link SerializableValue#getPropertyType()}
	 equal to {@link #getSubstituteType()}
	 @throws Exception when the new value couldn't be created
	 */
	@Nullable
	public SerializableValue newSubstituteTypeValue(@NotNull Env env) throws Exception {
		if (substituteType == null) {
			return null;
		}
		return substituteType.getConverter().convert(env, getAsStringArray());
	}

}
