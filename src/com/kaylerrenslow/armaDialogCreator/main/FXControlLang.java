package com.kaylerrenslow.armaDialogCreator.main;

/**
 @author Kayler
 Lang class for any custom FX controls
 Created on 07/15/2016. */
public interface FXControlLang {
	interface InputField {
		/** Lang strings for InputFieldDataChecker instances */
		interface DataCheckers {
			String NO_VALUE = "No value entered.";

			interface ArmaString {
				String TYPE_NAME = "String";
				String MISSING_QUOTE = "Missing quote.";
			}

			interface Double {
				String NOT_A_NUMBER = "Not a number.";
				String TYPE_NAME = "Floating Point Number";
			}

			interface Identifier {
				String NOT_IDENTIFIER = "Not an identifier. (Start with: letter, _, $. Then can have: letter, _, $, number)";
				String TYPE_NAME = "Identifier";
			}

			interface Integer {
				String NOT_INTEGER = "Not an integer.";
				String TYPE_NAME = "Integer";
			}

			interface Expression {
				String TYPE_NAME = "Expression";
				String UNKNOWN_ERROR = "Unknown Error";
			}
			
			interface StringChecker {
				String TYPE_NAME = "String";
			}
		}
	}
}

