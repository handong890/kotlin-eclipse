package org.jetbrains.kotlin.ui.navigation

import org.jetbrains.kotlin.psi.JetVisitorVoid
import com.intellij.psi.PsiElement
import org.eclipse.jdt.core.IJavaElement
import org.jetbrains.kotlin.psi.JetElement
import org.eclipse.jdt.core.IType
import org.jetbrains.kotlin.psi.JetVisitor
import org.jetbrains.kotlin.psi.JetClass
import org.eclipse.jdt.core.IMethod
import org.jetbrains.kotlin.psi.JetNamedFunction
import org.jetbrains.kotlin.psi.JetFile
import kotlin.MutableList
import java.util.ArrayList

fun visitFile(element: IJavaElement, jetFile: JetFile): List<JetElement> {
	val referenceElement: IJavaElement
	if (element is IMethod && element.isConstructor()) {
		referenceElement = element.getDeclaringType()
	} else {
		referenceElement = element
	}
	
	val result = ArrayList<JetElement>()
	val visitor = makeVisitor(referenceElement, result)
	if (visitor != null) {
		jetFile.acceptChildren(visitor)
	}
	
	return result
}

fun makeVisitor(element: IJavaElement, result: MutableList<JetElement>): JetVisitorVoid? {
	return when (element) {
		is IType -> object : JetAllVisitor() {
			override fun visitClass(jetClass: JetClass) {
				val fqName = jetClass.getFqName()
				if (fqName != null) {
					if (fqName.asString().equals(element.getFullyQualifiedName('.'))) {
						result.add(jetClass)
					}
				}
				jetClass.acceptChildren(this)
			}
		}
		is IMethod -> object : JetAllVisitor() {
			override fun visitNamedFunction(function: JetNamedFunction) {
				if (function.getName().equals(element.getElementName())) {
					result.add(function)
				}
				function.acceptChildren(this)
			}
		}
		else -> null
	}
}

open class JetAllVisitor() : JetVisitorVoid() {
	override fun visitElement(element: PsiElement) {
		element.acceptChildren(this)
	}
}