package com.armadialogcreator.gui.main.controlPropertiesEditor;

import com.armadialogcreator.control.sv.SVInteger;
import com.armadialogcreator.gui.fxcontrol.inputfield.InputFieldDataChecker;
import com.armadialogcreator.gui.fxcontrol.inputfield.IntegerChecker;
import org.jetbrains.annotations.NotNull;

/**
 Checker for Integers that returns a SerializableValue

 @author Kayler
 @since 05/31/2016. */
public class SVIntegerChecker implements InputFieldDataChecker<SVInteger> {
	private static final IntegerChecker checker = new IntegerChecker();

	@Override
	public String errorMsgOnData(@NotNull String data) {
		return checker.errorMsgOnData(data);
	}

	@Override
	public SVInteger parse(@NotNull String data) {
		Integer i = checker.parse(data);
		if (i == null) {
			throw new IllegalStateException("returned value shouldn't be null");
		}
		return new SVInteger(i);
	}

	@Override
	public String getTypeName() {
		return checker.getTypeName();
	}

	@Override
	public boolean allowEmptyData() {
		return checker.allowEmptyData();
	}
}
