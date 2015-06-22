package org.jetbrains.kotlin.ui.editors.quickassist

import com.intellij.psi.PsiElement
import org.eclipse.jface.text.IDocument
import org.jetbrains.kotlin.psi.JetClassOrObject
import org.jetbrains.kotlin.descriptors.CallableMemberDescriptor
import org.jetbrains.kotlin.descriptors.ClassDescriptor
import org.jetbrains.kotlin.descriptors.DeclarationDescriptor
import org.jetbrains.kotlin.psi.JetElement
import org.jetbrains.kotlin.core.resolve.KotlinAnalyzer
import org.eclipse.jdt.core.JavaCore
import org.jetbrains.kotlin.resolve.BindingContextUtils
import org.jetbrains.kotlin.resolve.BindingContext
import org.jetbrains.kotlin.psi.psiUtil.getElementTextWithContext
import org.jetbrains.kotlin.resolve.OverrideResolver
import com.intellij.psi.util.PsiTreeUtil
import org.eclipse.jface.dialogs.MessageDialog
import org.jetbrains.kotlin.psi.JetFile
import java.util.ArrayList
import org.jetbrains.kotlin.psi.JetPsiFactory
import org.jetbrains.kotlin.psi.JetClassBody
import com.intellij.psi.PsiWhiteSpace
import com.intellij.openapi.util.text.StringUtil
import org.jetbrains.kotlin.descriptors.SimpleFunctionDescriptor
import org.jetbrains.kotlin.descriptors.PropertyDescriptor
import org.jetbrains.kotlin.descriptors.FunctionDescriptor
import org.jetbrains.kotlin.psi.JetNamedFunction
import org.jetbrains.kotlin.descriptors.Modality
import org.jetbrains.kotlin.builtins.KotlinBuiltIns
import org.jetbrains.kotlin.renderer.DescriptorRendererBuilder
import org.jetbrains.kotlin.renderer.DescriptorRenderer
import org.jetbrains.kotlin.renderer.NameShortness
import org.jetbrains.kotlin.idea.util.IdeDescriptorRenderers

public class KotlinImplementMethodsProposal : KotlinQuickAssistProposal() {
    private val OVERRIDE_RENDERER = DescriptorRendererBuilder()
        .setRenderDefaultValues(false)
        .setModifiers(DescriptorRenderer.Modifier.OVERRIDE)
        .setWithDefinedIn(false)
        .setNameShortness(NameShortness.SOURCE_CODE_QUALIFIED)
        .setOverrideRenderingPolicy(DescriptorRenderer.OverrideRenderingPolicy.RENDER_OVERRIDE)
        .setUnitReturnType(false)
        .setTypeNormalizer(IdeDescriptorRenderers.APPROXIMATE_FLEXIBLE_TYPES)
        .build()

	
	override fun apply(document: IDocument, psiElement: PsiElement) {
        val classOrObject = PsiTreeUtil.getParentOfType(psiElement, javaClass<JetClassOrObject>(), false)
        val missingImplementations = collectMethodsToGenerate(classOrObject)
        if (missingImplementations.isEmpty()) {
            return
        }
        
		generateMethods(document, classOrObject, missingImplementations)
	}
	
	override fun getDisplayString(): String = "Implement Members"
	
	override fun isApplicable(psiElement: PsiElement): Boolean {
		val classOrObject = PsiTreeUtil.getParentOfType(psiElement, javaClass<JetClassOrObject>(), false)
		if (classOrObject != null) {
			return collectMethodsToGenerate(classOrObject).isNotEmpty()
		}
		
		return false
	}
	
	public fun generateMethods(document: IDocument, classOrObject: JetClassOrObject, selectedElements: Set<CallableMemberDescriptor>) {
		var body = classOrObject.getBody()
		val editor = getActiveEditor()!!
        if (body == null) {
            val psiFactory = JetPsiFactory(classOrObject)
			classOrObject.add(psiFactory.createWhiteSpace())
			body = classOrObject.add(psiFactory.createEmptyClassBody()) as JetClassBody
        }

        var afterAnchor = findInsertAfterAnchor(body)

        if (afterAnchor == null) return

        var firstGenerated: PsiElement? = null
        for (element in generateOverridingMembers(selectedElements, classOrObject)) {
            val added = body.addAfter(element, afterAnchor)

            if (firstGenerated == null) {
                firstGenerated = added
            }

            afterAnchor = added
        }
		
//		insertAfter(classOrObject, document, editor, body.getText())
	}
	
    private fun generateOverridingMembers(selectedElements: Set<CallableMemberDescriptor>,
                                          classOrObject: JetClassOrObject): List<JetElement> {
        val overridingMembers = ArrayList<JetElement>()
        for (selectedElement in selectedElements) {
            if (selectedElement is SimpleFunctionDescriptor) {
                overridingMembers.add(overrideFunction(classOrObject, selectedElement))
            }
            else if (selectedElement is PropertyDescriptor) {
                overridingMembers.add(overrideProperty(classOrObject, selectedElement))
            }
        }
        return overridingMembers
    }

    private fun overrideFunction(classOrObject: JetClassOrObject, descriptor: FunctionDescriptor): JetNamedFunction {
        val newDescriptor = descriptor.copy(descriptor.getContainingDeclaration(), Modality.OPEN, descriptor.getVisibility(),
                                            descriptor.getKind(), /* copyOverrides = */ true)
        newDescriptor.addOverriddenDescriptor(descriptor)

        val returnType = descriptor.getReturnType()
        val returnsNotUnit = returnType != null && !KotlinBuiltIns.isUnit(returnType)
        val isAbstract = descriptor.getModality() == Modality.ABSTRACT

        val delegation = generateUnsupportedOrSuperCall(classOrObject, descriptor)

        val body = "{" + (if (returnsNotUnit && !isAbstract) "return " else "") + delegation + "}"

        return JetPsiFactory(classOrObject.getProject()).createFunction(OVERRIDE_RENDERER.render(newDescriptor) + body)
    }
	
    private fun overrideProperty(classOrObject: JetClassOrObject, descriptor: PropertyDescriptor): JetElement {
        val newDescriptor = descriptor.copy(descriptor.getContainingDeclaration(), Modality.OPEN, descriptor.getVisibility(),
                                            descriptor.getKind(), /* copyOverrides = */ true) as PropertyDescriptor
        newDescriptor.addOverriddenDescriptor(descriptor)

        val body = StringBuilder()
        body.append("\nget()")
        body.append(" = ")
        body.append(generateUnsupportedOrSuperCall(classOrObject, descriptor))
        if (descriptor.isVar()) {
            body.append("\nset(value) {}")
        }
        return JetPsiFactory(classOrObject.getProject()).createProperty(OVERRIDE_RENDERER.render(newDescriptor) + body)
    }

	
    private fun generateUnsupportedOrSuperCall(classOrObject: JetClassOrObject, descriptor: CallableMemberDescriptor): String {
        val isAbstract = descriptor.getModality() == Modality.ABSTRACT
        if (isAbstract) {
            return "throw UnsupportedOperationException()"
        }
        else {
            val builder = StringBuilder()
            builder.append("super")
            if (classOrObject.getDelegationSpecifiers().size() > 1) {
                builder.append("<").append(descriptor.getContainingDeclaration().escapedName()).append(">")
            }
            builder.append(".").append(descriptor.escapedName())

            if (descriptor is FunctionDescriptor) {
                val paramTexts = descriptor.getValueParameters().map {
                    val renderedName = it.escapedName()
                    if (it.getVarargElementType() != null) "*$renderedName" else renderedName
                }
                paramTexts.joinTo(builder, prefix="(", postfix=")")
            }

            return builder.toString()
        }
    }

	
    private fun findInsertAfterAnchor(body: JetClassBody): PsiElement? {
        val afterAnchor = body.getLBrace()
        if (afterAnchor == null) return null

        val offset = getCaretOffset(getActiveEditor()!!)
        val offsetCursorElement = PsiTreeUtil.findFirstParent(body.getContainingFile().findElementAt(offset)) {
            it.getParent() == body
        }

        if (offsetCursorElement is PsiWhiteSpace) {
            return removeAfterOffset(offset, offsetCursorElement)
        }

        if (offsetCursorElement != null && offsetCursorElement != body.getRBrace()) {
            return offsetCursorElement
        }

        return afterAnchor
    }
	
    private fun removeAfterOffset(offset: Int, whiteSpace: PsiWhiteSpace): PsiElement {
        val spaceNode = whiteSpace.getNode()
        if (spaceNode.getTextRange().contains(offset)) {
            var beforeWhiteSpaceText = spaceNode.getText().substring(0, offset - spaceNode.getStartOffset())
            if (!StringUtil.containsLineBreak(beforeWhiteSpaceText)) {
                // Prevent insertion on same line
                beforeWhiteSpaceText += "\n"
            }

            val factory = JetPsiFactory(whiteSpace.getProject())

            val insertAfter = whiteSpace.getPrevSibling()
            whiteSpace.delete()

            val beforeSpace = factory.createWhiteSpace(beforeWhiteSpaceText)
            insertAfter.getParent().addAfter(beforeSpace, insertAfter)

            return insertAfter.getNextSibling()
        }

        return whiteSpace
    }


	
	public fun collectMethodsToGenerate(classOrObject: JetClassOrObject): Set<CallableMemberDescriptor> {
        val descriptor = classOrObject.resolveToDescriptor()
        if (descriptor is ClassDescriptor) {
            return OverrideResolver.getMissingImplementations(descriptor)
        }
        return emptySet()
    }
	
	private fun JetElement.resolveToDescriptor(): DeclarationDescriptor {
		val jetFile = this.getContainingJetFile()
		val project = getActiveFile()!!.getProject()
		val analysisResult = KotlinAnalyzer.analyzeFile(JavaCore.create(project), jetFile)
		return BindingContextUtils.getNotNull(
				analysisResult.bindingContext, 
				BindingContext.DECLARATION_TO_DESCRIPTOR,
				this,
				"Descriptor wasn't found for declaration " + this.toString() + "\n" +
				this.getElementTextWithContext())
	}
	
	fun DeclarationDescriptor.escapedName() = DescriptorRenderer.COMPACT.renderName(getName())
}