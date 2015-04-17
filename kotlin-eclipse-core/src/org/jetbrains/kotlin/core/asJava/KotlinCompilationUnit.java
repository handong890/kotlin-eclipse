package org.jetbrains.kotlin.core.asJava;

import org.eclipse.jdt.core.WorkingCopyOwner;
import org.eclipse.jdt.internal.core.CompilationUnit;
import org.eclipse.jdt.internal.core.PackageFragment;


public class KotlinCompilationUnit extends CompilationUnit {
    public KotlinCompilationUnit(PackageFragment parent, String name, WorkingCopyOwner owner) {
        super(parent, name, owner);
    }
}
