package org.jetbrains.kotlin.core.asJava;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.internal.ui.javaeditor.CompilationUnitDocumentProvider;

public class KotlinDocumentProvider extends CompilationUnitDocumentProvider {

    @Override
    public void connect(Object element) throws CoreException {
        super.connect(element);
    }
}
