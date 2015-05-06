package org.jetbrains.kotlin.ui.navigation;

import java.io.File;
import java.util.List;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.internal.ui.javaeditor.EditorUtility;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.window.Window;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.dialogs.ListDialog;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.kotlin.core.builder.KotlinPsiManager;
import org.jetbrains.kotlin.core.filesystem.KotlinLightClassManager;
import org.jetbrains.kotlin.core.log.KotlinLogger;
import org.jetbrains.kotlin.eclipse.ui.utils.EditorUtil;
import org.jetbrains.kotlin.eclipse.ui.utils.LineEndUtil;
import org.jetbrains.kotlin.psi.JetDeclaration;
import org.jetbrains.kotlin.psi.JetElement;
import org.jetbrains.kotlin.psi.JetFile;
import org.jetbrains.kotlin.ui.editors.KotlinEditor;

// Seeks Kotlin editor by IJavaElement
public class KotlinOpenEditor {
	@Nullable
	public static IEditorPart openKotlinEditor(@NotNull IJavaElement element, boolean activate) {
	    File lightClass = element.getResource().getFullPath().toFile();
	    List<JetFile> sourceFiles = KotlinLightClassManager.INSTANCE.getSourceFiles(lightClass);
	    JetFile referenceFile = null;
	    for (JetFile sourceFile : sourceFiles) {
	        JetElement referenceElement = findKotlinElement(element, sourceFile);
	        if (referenceElement != null) {
	            referenceFile = sourceFile;
	            break;
	        }
	    }
	    
	    if (referenceFile == null) {
	        return null;
	    }
	    
	    IPath sourceFilePath = new Path(referenceFile.getVirtualFile().getPath());
	    IFile kotlinFile = ResourcesPlugin.getWorkspace().getRoot().getFileForLocation(sourceFilePath);
	    
	    try {
	        if (kotlinFile.exists()) {
	            return EditorUtility.openInEditor(kotlinFile, activate);
	        }
	    } catch (PartInitException e) {
	        KotlinLogger.logAndThrow(e);
	    }
	    
	    return null;
	}
	
	public static void revealKotlinElement(@NotNull KotlinEditor kotlinEditor, @NotNull IJavaElement javaElement) {
	    IFile file = EditorUtil.getFile(kotlinEditor);
	    if (file != null) {
	        JetFile jetFile = KotlinPsiManager.INSTANCE.getParsedFile(file);
	        JetElement jetElement = findKotlinElement(javaElement, jetFile);
	        if (jetElement != null) {
	            int offset = LineEndUtil.convertLfToDocumentOffset(jetFile.getText(), jetElement.getTextOffset(), EditorUtil.getDocument(file));
	            kotlinEditor.selectAndReveal(offset, 0);
	        }
	    }
	}

	@Nullable
	private static JetElement findKotlinElement(@NotNull final IJavaElement javaElement, @NotNull JetFile jetFile) {
	    List<JetElement> result = NavigationPackage.findKotlinDeclarations(javaElement, jetFile);
	    if (result.size() == 1) {
	        return result.get(0);
	    } else if (result.size() > 1) {
	        ListDialog dialog = new ListDialog(PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell());
	        dialog.setBlockOnOpen(true);
	        dialog.setMessage("Select a Kotlin element to navigate");
	        dialog.setTitle("Choose Kotlin element");
	        dialog.setContentProvider(new ArrayContentProvider());
	        dialog.setLabelProvider(new LabelProvider() {
                @Override
                public String getText(Object element) {
                    if (element instanceof JetDeclaration) {
                        return element.toString();
                    } 
                    return super.getText(element);
                }
	        });
	        
	        dialog.setInput(result);
	        
	        if (dialog.open() == Window.CANCEL) {
	            return null;
	        }
	        
	        Object[] results = dialog.getResult();
	        return results != null ? (JetElement) results[0] : null;
	    }
	    
	    return null;
	}
}
