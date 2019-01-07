package com.armadialogcreator.data.olddata;

import com.armadialogcreator.util.DataContext;
import com.armadialogcreator.util.ValueConverter;
import org.jetbrains.annotations.NotNull;

/**
 Trivial implementation for a boolean {@link ValueConverter}
 @author Kayler
 @since 07/31/2016. */
public class BooleanConverter implements ValueConverter<Boolean> {
	public static final BooleanConverter INSTANCE = new BooleanConverter();
	
	@Override
	public Boolean convert(DataContext context, @NotNull String... values) throws Exception {
		return values[0].equalsIgnoreCase("true");
	}
}
