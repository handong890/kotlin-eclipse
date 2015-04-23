package org.jetbrains.kotlin.aspects.navigation;

import org.eclipse.ui.IEditorPart;
import org.eclipse.jdt.core.IClassFile;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.internal.ui.javaeditor.EditorUtility;
import org.eclipse.jdt.internal.core.BinaryType;
import org.jetbrains.kotlin.core.resolve.lang.java.structure.EclipseJavaElementUtil;
import org.jetbrains.kotlin.ui.navigation.KotlinOpenEditor;

@SuppressWarnings("restriction")
public aspect KotlinOpenEditorAspect {
	pointcut openInEditor(Object inputElement, boolean activate) :
		args(inputElement, activate)
		&& execution(IEditorPart EditorUtility.openInEditor(Object, boolean));
	
	IEditorPart around(Object inputElement, boolean activate) : openInEditor(inputElement, activate) {
		if (inputElement instanceof IJavaElement) {
			IJavaElement javaElement = (IJavaElement) inputElement;
			IClassFile classFile = (IClassFile) javaElement.getAncestor(IJavaElement.CLASS_FILE);
			if (classFile != null) {
				BinaryType binaryType = (BinaryType) classFile.getType();
				if (EclipseJavaElementUtil.isKotlinLightClass((BinaryType) binaryType)) {
					return KotlinOpenEditor.openKotlinEditor(binaryType, activate);
				}	
			}
		}
		
		return proceed(inputElement, activate);
	}
}
