package org.jetbrains.kotlin.core.asJava;

import org.eclipse.contribution.jdt.cuprovider.ICompilationUnitProvider;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.ITypeRoot;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.WorkingCopyOwner;
import org.eclipse.jdt.internal.core.CompilationUnit;
import org.eclipse.jdt.internal.core.PackageFragment;
import org.jetbrains.kotlin.core.builder.KotlinPsiManager;
import org.jetbrains.kotlin.core.model.KotlinJavaManager;
import org.jetbrains.kotlin.psi.JetClass;
import org.jetbrains.kotlin.psi.JetFile;

public class KotlinCompilationUnitProvider implements ICompilationUnitProvider {
    
    public KotlinCompilationUnitProvider() {
        // TODO Auto-generated constructor stub
    }

    @Override
    public CompilationUnit create(PackageFragment parent, String name, WorkingCopyOwner owner) {
        JetFile jetFile = KotlinPsiManager.getKotlinFileIfExist("C:/Users/Mikhail.Zarechenskiy/runtime-EclipseApplicationwithEquinoxWeaving/Ttemp/src/testing/debug/view/Some.kt");
        if (jetFile != null) {
            if (!jetFile.getDeclarations().isEmpty()) {
                JetClass jetClass = (JetClass) jetFile.getDeclarations().get(0);
                IProject project = ResourcesPlugin.getWorkspace().getRoot().getProject("Ttemp");
                IType type = KotlinJavaManager.INSTANCE.findEclipseType(jetClass, JavaCore.create(project));
                if (type != null) {
                    ITypeRoot eclipseType = (ITypeRoot) type.getParent();
                    if (eclipseType != null) {
                        ICompilationUnit compilationUnit;
                        try {
                            compilationUnit = eclipseType.getWorkingCopy(null, null);
                            compilationUnit.getAllTypes();
                            compilationUnit.getParent();
                        } catch (JavaModelException e) {
                            e.printStackTrace();
                        }
                    }
                }
            }
        }
        return new KotlinCompilationUnit(parent, name, owner);
    }
    
}
