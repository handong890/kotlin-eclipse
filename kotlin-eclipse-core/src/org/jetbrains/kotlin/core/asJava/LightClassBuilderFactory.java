package org.jetbrains.kotlin.core.asJava;

import org.eclipse.core.runtime.Path;
import org.eclipse.jdt.core.IJavaProject;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.kotlin.codegen.AbstractClassBuilder;
import org.jetbrains.kotlin.codegen.ClassBuilder;
import org.jetbrains.kotlin.codegen.ClassBuilderFactory;
import org.jetbrains.kotlin.codegen.ClassBuilderMode;
import org.jetbrains.kotlin.resolve.jvm.diagnostics.JvmDeclarationOrigin;
import org.jetbrains.org.objectweb.asm.ClassWriter;

public class LightClassBuilderFactory implements ClassBuilderFactory {
    
    private final IJavaProject javaProject;
    
    public LightClassBuilderFactory(IJavaProject javaProject) {
        this.javaProject = javaProject;
    }

    @Override
    @NotNull
    public ClassBuilderMode getClassBuilderMode() {
        return ClassBuilderMode.LIGHT_CLASSES;
    }

    @Override
    @NotNull
    public ClassBuilder newClassBuilder(@NotNull JvmDeclarationOrigin origin) {
        return new AbstractClassBuilder.Concrete(new BinaryClassWriter()) {
            @Override
            public void visitSource(@NotNull String name, @Nullable String debug) {
                String relativeSourceFileName = new Path(name).makeRelativeTo(javaProject.getProject().getLocation()).toOSString();
                super.visitSource(relativeSourceFileName, debug);
            }
        };
    }

    @Override
    public String asText(ClassBuilder builder) {
        throw new UnsupportedOperationException("BINARIES generator asked for text");
    }

    @Override
    public byte[] asBytes(ClassBuilder builder) {
        ClassWriter visitor = (ClassWriter) builder.getVisitor();
        return visitor.toByteArray();
    }
    
}