package org.jetbrains.kotlin.ui.navigation;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.internal.compiler.env.IBinaryType;
import org.eclipse.jdt.internal.core.BinaryType;
import org.eclipse.jdt.internal.ui.javaeditor.EditorUtility;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.PartInitException;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.kotlin.core.log.KotlinLogger;

// Seeks for Kotlin editor by IJavaElement
public class KotlinOpenEditor {
	@Nullable
	public static IEditorPart openKotlinEditor(@NotNull BinaryType binaryType, boolean activate) {
		try {
			IBinaryType rawBinaryType = (IBinaryType) ((binaryType).getElementInfo());
			IPath sourceFilePath = new Path(binaryType.getSourceFileName(rawBinaryType));
	        IFile kotlinFile = ResourcesPlugin.getWorkspace().getRoot().getFileForLocation(sourceFilePath);
	        if (kotlinFile.exists()) {
	        	return EditorUtility.openInEditor(kotlinFile, activate);
	        }
		} catch (JavaModelException | PartInitException e) {
			KotlinLogger.logAndThrow(e);
		}
		
		return null;
	}
}
