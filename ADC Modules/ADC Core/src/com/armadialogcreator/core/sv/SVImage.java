package com.armadialogcreator.core.sv;

import com.armadialogcreator.core.PropertyType;
import com.armadialogcreator.core.old.FilePathUser;
import com.armadialogcreator.expression.Env;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;

/**
 A SerializableValue implementation for storing an image file

 @author Kayler
 @since 07/16/2016. */
public class SVImage extends SerializableValue implements FilePathUser {

	public static final StringArrayConverter<SVImage> CONVERTER = new StringArrayConverter<SVImage>() {
		@Override
		public SVImage convert(@NotNull Env env, @NotNull String[] values) throws Exception {
			return new SVImage(new File(values[0]), null);
		}
	};

	private File imageFile;
	private File nonPaaImageFile;

	public SVImage(@NotNull File imageFile, @Nullable File nonPaaImageFile) {
		this.nonPaaImageFile = nonPaaImageFile;
		this.imageFile = imageFile;
	}

	/** @return the file that is displayed to the user (may be a .paa image file) */
	@NotNull
	public File getImageFile() {
		return imageFile;
	}

	/** @return a file that is guaranteed not to be a .paa image file */
	@Nullable
	public File getNonPaaImageFile() {
		return nonPaaImageFile;
	}

	@Override
	public String toString() {
		return imageFile.getPath();
	}

	@NotNull
	@Override
	public String[] getAsStringArray() {
		return new String[]{toString()};
	}

	@NotNull
	@Override
	public SerializableValue deepCopy() {
		return new SVImage(imageFile, nonPaaImageFile);
	}

	@NotNull
	@Override
	public PropertyType getPropertyType() {
		return PropertyType.Image;
	}

	@Override
	public boolean equals(Object o) {
		if (o == this) {
			return true;
		}
		if (o instanceof SVImage) {
			SVImage other = (SVImage) o;
			return this.imageFile.equals(other.imageFile);
		}
		return false;
	}

	@NotNull
	@Override
	public int[] getIndicesThatUseFilePaths() {
		return new int[]{0};
	}
}
