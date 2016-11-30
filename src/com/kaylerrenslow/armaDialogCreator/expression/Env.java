package com.kaylerrenslow.armaDialogCreator.expression;

import org.jetbrains.annotations.Nullable;

/**
 Environment for evaluating identifiers for {@link ExpressionInterpreter}

 @author Kayler
 @since 07/14/2016. */
public interface Env {
	/** Returns the value for the given identifier. If returns null, means identifier couldn't be resolved to a value. */
	@Nullable
	Value getValue(String identifier);
}
