package com.armadialogcreator.arma.header;

import com.armadialogcreator.util.IndentedStringBuilder;
import com.armadialogcreator.util.SGAS;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.function.Function;

/**
 @author Kayler
 @see HeaderParser
 @since 03/19/2017 */
public class HeaderFile {
	private final String filePath;
	private final List<HeaderAssignment> assignmentsMutable = new LinkedList<>();
	private final HeaderAssignmentList assignments = new HeaderAssignmentList(assignmentsMutable);
	private final List<HeaderClass> classesMutable = new LinkedList<>();
	private final HeaderClassList classes = new HeaderClassList(null, classesMutable);
	private final HashMap<HeaderClass, HeaderClass> inheritanceHelper = new HashMap<>();

	protected HeaderFile(@NotNull String filePath) {
		this.filePath = filePath;
	}

	/** @return the file path that was parsed via {@link HeaderParser#parse()} */
	@NotNull
	public String getPath() {
		return filePath;
	}

	/**
	 List for inserting the root class's {@link HeaderClass#getNestedClasses()}

	 @return a mutable list
	 */
	@NotNull
	protected List<HeaderClass> getClassesMutable() {
		return classesMutable;
	}

	/**
	 List for inserting the root class's {@link HeaderClass#getAssignments()}

	 @return a mutable list
	 */

	@NotNull
	protected List<HeaderAssignment> getAssignmentsMutable() {
		return assignmentsMutable;
	}

	/**
	 Get a list of child assignments for the {@link HeaderFile}. This will not return assignments inside each nested class.

	 @return list of assignments
	 */
	@NotNull
	public HeaderAssignmentList getAssignments() {
		return assignments;
	}

	/**
	 Get a list of child classes for the {@link HeaderFile}. This will not return classes nested inside each class.

	 @return list of classes
	 */
	@NotNull
	public HeaderClassList getClasses() {
		return classes;
	}

	/**
	 An iterative way to traverse downwards the inheritance tree. The algorithm is iterative BFS.
	 This method just iterates through {@link #getClasses()} and invokes {@link HeaderClass#traverseDownwards(Function)}

	 @param classTraverseCallback function invoked on each descendant class. Return true if traversal should continue, false if it should stop
	 */
	public void traverseDownwards(@NotNull Function<HeaderClass, Boolean> classTraverseCallback) {
		for (HeaderClass hc : getClasses()) {
			hc.traverseDownwards(classTraverseCallback);
		}
	}

	/**
	 Get an assignment for a {@link HeaderClass}. This method will also check inherited assignments.
	 <b>NOTE:</b> this method will only work if the {@link HeaderClass} is a member of this file.

	 @param start the {@link HeaderClass} to get an assignment for
	 @param varName the variable name ({@link HeaderAssignment#getVariableName()})
	 @param caseSensitive true if the class names and assignment names should be matched with case sensitivity
	 @return the {@link HeaderAssignment} that was matched, or null if none was matched
	 @throws HeaderClassNotFoundException when the extend class of <code>start</code> doesn't exist
	 */
	@Nullable
	public HeaderAssignment getAssignmentByVarName(@NotNull HeaderClass start, @NotNull String varName, boolean caseSensitive) {
		if (start.getOwnerFile() != this) {
			throw new IllegalArgumentException("start is not a member of this file");
		}
		HeaderClass cursor = start;
		while (true) {
			HeaderAssignment a = cursor.getAssignments().getByVarName(varName, caseSensitive);
			if (a != null) {
				return a;
			}

			HeaderClass extendClass = getInherited(cursor, caseSensitive);

			if (extendClass == null) {
				return null;
			}

			cursor = extendClass;
		}
	}

	/**
	 Get a nested class for a {@link HeaderClass}. This method will also check inherited nested classes.
	 <b>NOTE:</b> this method will only work if the {@link HeaderClass} is a member of this file.

	 @param start the {@link HeaderClass} to get an assignment for
	 @param className the class name ({@link HeaderClass#getClassName()})
	 @param caseSensitive true if the class names and assignment names should be matched with case sensitivity
	 @return the {@link HeaderClass} that was matched, or null if none was matched
	 @throws HeaderClassNotFoundException when the extend class of <code>start</code> doesn't exist
	 */
	@Nullable
	public HeaderClass getNestedClassName(@NotNull HeaderClass start, @NotNull String className, boolean caseSensitive) {
		if (start.getOwnerFile() != this) {
			throw new IllegalArgumentException("start is not a member of this file");
		}
		HeaderClass cursor = start;
		while (true) {
			HeaderClass hc = cursor.getNestedClasses().getByName(className, caseSensitive);
			if (hc != null) {
				return hc;
			}

			HeaderClass extendClass = getInherited(cursor, caseSensitive);

			if (extendClass == null) {
				return null;
			}

			cursor = extendClass;
		}
	}

	/**
	 Get the extend class for the given {@link HeaderClass} instance

	 @param headerClass the class to get extend class for
	 @param caseSensitive true if the case sensitivity of class names matters
	 @return the class, or null if <code>{@link HeaderClass#getExtendClassName()}==null</code>
	 */
	@Nullable
	public HeaderClass getExtendClass(@NotNull HeaderClass headerClass, boolean caseSensitive) {
		if (headerClass.getOwnerFile() != this) {
			throw new IllegalArgumentException("headerClass is not a member of this file");
		}
		return getInherited(headerClass, caseSensitive);
	}

	@Nullable
	private HeaderClass getInherited(@NotNull HeaderClass subClass, boolean caseSensitive) {
		if (subClass.getExtendClassName() == null) {
			return null;
		}
		final String classNameToFind = subClass.getExtendClassName();
		return inheritanceHelper.computeIfAbsent(subClass, (hc) -> {

			//the root class (which the HeaderFile uses for getting its classes and assignmetns)
			//will still be available in the heirarchy, so this will work
			SGAS<HeaderClass> extendClassRef = new SGAS<>();

			hc.traverseUpwards((parent) -> {
				if (parent.classNameEquals(classNameToFind, caseSensitive)) {
					extendClassRef.setValue(parent);
					return false;
				}
				HeaderClass c = parent.getNestedClasses().getByName(classNameToFind, caseSensitive);
				if (c != null) {
					extendClassRef.setValue(c);
					return false;
				}
				return true;
			});

			if (extendClassRef.getValue() == null) {
				throw new HeaderClassNotFoundException("", classNameToFind);
			}

			return extendClassRef.getValue();
		});
	}

	@NotNull
	public String getAsString(@Nullable IndentedStringBuilder indentedBuilder) {
		if (indentedBuilder == null) {
			StringBuilder b = new StringBuilder();
			for (HeaderAssignment a : getAssignments()) {
				b.append(a.getAsString(null));
			}
			for (HeaderClass hc : getClasses()) {
				b.append(hc.getAsString(null));
			}
			return b.toString();
		}
		for (HeaderAssignment a : getAssignments()) {
			a.getAsString(indentedBuilder);
		}
		for (HeaderClass hc : getClasses()) {
			hc.getAsString(indentedBuilder);
		}
		return indentedBuilder.toString();
	}
}
