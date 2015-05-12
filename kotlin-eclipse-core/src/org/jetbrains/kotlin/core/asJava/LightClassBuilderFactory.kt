package org.jetbrains.kotlin.core.asJava

import org.jetbrains.kotlin.codegen.ClassBuilderFactory
import org.jetbrains.kotlin.codegen.ClassBuilderMode
import org.jetbrains.kotlin.codegen.ClassBuilder
import org.jetbrains.org.objectweb.asm.ClassWriter
import org.jetbrains.kotlin.codegen.AbstractClassBuilder
import org.jetbrains.kotlin.resolve.jvm.diagnostics.JvmDeclarationOrigin

public class KotlinLightClassBuilderFactory : ClassBuilderFactory {
	override fun getClassBuilderMode(): ClassBuilderMode = ClassBuilderMode.LIGHT_CLASSES

	override fun newClassBuilder(origin: JvmDeclarationOrigin): ClassBuilder {
		return AbstractClassBuilder.Concrete(BinaryClassWriter())
	}	
	
	override fun asText(builder: ClassBuilder): String {
		throw UnsupportedOperationException("BINARIES generator asked for text")
	}
	
	override fun asBytes(builder: ClassBuilder): ByteArray {
		val visitor = builder.getVisitor() as ClassWriter
		return visitor.toByteArray()
	}
}