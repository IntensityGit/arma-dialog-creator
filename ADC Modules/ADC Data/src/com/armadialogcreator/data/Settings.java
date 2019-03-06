package com.armadialogcreator.data;

import com.armadialogcreator.application.Configurable;
import com.armadialogcreator.core.sv.*;
import com.armadialogcreator.expression.SimpleEnv;
import com.armadialogcreator.util.AColor;
import com.armadialogcreator.util.ColorUtil;
import com.armadialogcreator.util.KeyValueString;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

/**
 @author K
 @since 02/15/2019 */
public class Settings {

	protected final Map<String, Setting> map = new HashMap<>();

	@NotNull
	public Map<String, Setting> getMap() {
		return map;
	}

	protected final void setFromConfigurable(@NotNull Configurable c) {
		System.out.println(Configurable.toFormattedString(c, 1));
		System.out.println("Settings.setFromConfigurable ");
		for (Configurable nested : c.getNestedConfigurables()) {
			for (KeyValueString attr : nested.getConfigurableAttributes()) {
				if (!attr.getKey().equals("name")) {
					continue;
				}
				Setting setting = map.get(attr.getValue());
				if (setting == null) {
					continue;
				}
				setting.setFromConfigurable(nested.getNestedConfigurables().iterator().next());
			}
		}
	}

	@NotNull
	protected final Configurable copyToConfigurable() {
		Configurable c = new Configurable.Simple("settings");
		map.forEach((key, value) -> {
			Configurable setting = new Configurable.Simple("s");
			setting.addAttribute("name", key);
			if (value.get() == null && value.getDefaultValue() == null) {
				return;
			}
			setting.addNestedConfigurable(value.exportToConfigurable());
			c.addNestedConfigurable(setting);
		});
		return c;
	}

	public static class FileSetting extends Setting<File> {

		public FileSetting(@Nullable File initial) {
			super(initial);
		}

		public FileSetting() {
		}

		@NotNull
		@Override
		public Configurable exportToConfigurable() {
			if (v == null) {
				if (defaultValue == null) {
					throw new IllegalStateException();
				}
				return new Configurable.Simple("file", defaultValue.getAbsolutePath());
			}
			return new Configurable.Simple("file", v.getAbsolutePath());
		}

		@Override
		public void setFromConfigurable(@NotNull Configurable c) {

		}
	}

	public static class StringSetting extends SVSetting<SVString> {

		public StringSetting(@Nullable String initial) {
			super(new SVString(initial));
		}

		public StringSetting() {
		}
	}

	public static class BooleanSetting extends SVSetting<SVBoolean> {

		public BooleanSetting(boolean initial) {
			super(SVBoolean.get(initial));
		}

		public BooleanSetting() {
		}

		public boolean isTrue() {
			SVBoolean b = super.get();
			return b != null && b.isTrue();
		}

		/** Take the current vale and set it to the opposite (true becomes false, false becomes true) */
		public void not() {
			set(isTrue() ? SVBoolean.FALSE : SVBoolean.TRUE);
		}
	}

	public static class IntegerSetting extends SVSetting<SVInteger> {

		public IntegerSetting(@Nullable SVInteger initial) {
			super(initial);
		}

		public IntegerSetting() {
		}

	}

	public static class ColorSetting extends Setting<AColor> {
		public ColorSetting(int r, int g, int b, int a) {
			set(r, g, b, a);
		}

		public ColorSetting(int argb) {
			this(ColorUtil.ri(argb), ColorUtil.gi(argb), ColorUtil.bi(argb), ColorUtil.ai(argb));
		}

		public ColorSetting() {
		}

		public int argb() {
			AColor color = get();
			if (color == null) {
				return 0;
			}
			return color.toARGB();
		}

		@Override
		public void set(@Nullable AColor color) {
			super.set(color == null ? new SVColorInt(0) : color);
		}

		@NotNull
		@Override
		public Configurable exportToConfigurable() {
			return new Configurable.Simple("argb", argb() + "");
		}

		@Override
		public void setFromConfigurable(@NotNull Configurable c) {
			set(new SVColorInt(Integer.parseInt(c.getConfigurableBody())));
		}

		/** Sets the integer value based upon ints ranged 0-255 */
		public void set(int r, int g, int b, int a) {
			set(new SVColorIntArray(r, g, b, a));
		}

		/** Sets the integer value based upon floats ranged 0-1.0 */
		public void set(double r, double g, double b, double a) {
			set(new SVColorArray(r, g, b, a));
		}

		public double r() {
			return ColorUtil.rf(argb());
		}

		public double g() {
			return ColorUtil.gf(argb());
		}

		public double b() {
			return ColorUtil.bf(argb());
		}

		public double a() {
			return ColorUtil.af(argb());
		}
	}

	public static abstract class SVSetting<V extends SerializableValue> extends Setting<V> {

		public SVSetting(@Nullable V initial) {
			super(initial);
		}

		public SVSetting() {
		}

		@NotNull
		@Override
		public Configurable exportToConfigurable() {
			if (v == null) {
				if (defaultValue == null) {
					throw new IllegalStateException();
				}
				return new SerializableValueConfigurable(defaultValue);
			}
			return new SerializableValueConfigurable(v);
		}

		@Override
		public void setFromConfigurable(@NotNull Configurable c) {
			this.v = (V) SerializableValueConfigurable.createFromConfigurable(c, new SimpleEnv());
		}
	}

	public static abstract class Setting<V> {
		protected V v;
		protected V defaultValue;

		public Setting(@Nullable V initial) {
			this.v = initial;
		}

		public Setting() {
		}

		@Nullable
		public V get() {
			return v;
		}

		public void set(@Nullable V v) {
			this.v = v;
		}

		@Nullable
		public V getDefaultValue() {
			return defaultValue;
		}

		public void setDefaultValue(@Nullable V defaultValue) {
			this.defaultValue = defaultValue;
		}

		@NotNull
		public abstract Configurable exportToConfigurable();

		public abstract void setFromConfigurable(@NotNull Configurable c);
	}
}
