package org.jetbrains.kotlin.ui.editors;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.internal.core.ClassFile;
import org.eclipse.jdt.internal.core.ClassFileWorkingCopy;
import org.eclipse.jdt.internal.core.DefaultWorkingCopyOwner;
import org.eclipse.jdt.internal.ui.javaeditor.CompilationUnitDocumentProvider;
import org.jetbrains.kotlin.core.builder.KotlinPsiManager;
import org.jetbrains.kotlin.core.model.KotlinJavaManager;
import org.jetbrains.kotlin.psi.JetClass;
import org.jetbrains.kotlin.psi.JetDeclaration;
import org.jetbrains.kotlin.psi.JetFile;

public class KotlinDocumentProvider extends CompilationUnitDocumentProvider {
    @Override
    public void connect(Object element) throws CoreException {
        super.connect(element);
    }

    @Override
    protected ICompilationUnit createCompilationUnit(IFile file) {
        JetFile jetFile = KotlinPsiManager.INSTANCE.getParsedFile(file);
        IType eclipseType = null;
        IJavaProject javaProject = JavaCore.create(file.getProject());
        for (JetDeclaration jetDeclaration : jetFile.getDeclarations()) {
            if (jetDeclaration instanceof JetClass) {
                eclipseType = KotlinJavaManager.INSTANCE.findEclipseType((JetClass) jetDeclaration, javaProject);
                break;
            }
        }
        
        if (eclipseType == null) {
            eclipseType = KotlinJavaManager.INSTANCE.findEclipseType(jetFile.getPackageFqName(), javaProject);
        }
        
        if (eclipseType != null) {
            ClassFileWorkingCopy compilationUnit = new ClassFileWorkingCopy((ClassFile) eclipseType.getParent(), DefaultWorkingCopyOwner.PRIMARY);
            return compilationUnit;
        }
        
        return super.createCompilationUnit(file);
    }
}
