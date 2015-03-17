package org.jetbrains.kotlin.ui.editors.highlighting

import org.eclipse.jface.text.rules.IToken
import org.eclipse.jface.text.TextAttribute
import org.eclipse.jdt.ui.text.IColorManager
import org.eclipse.swt.SWT
import com.intellij.psi.impl.source.tree.LeafPsiElement
import org.jetbrains.kotlin.lexer.JetTokens
import com.intellij.psi.PsiElement
import kotlin.platform.platformStatic
import org.eclipse.jface.preference.PreferenceConverter
import org.eclipse.jface.preference.IPreferenceStore
import org.eclipse.jdt.ui.PreferenceConstants

class KotlinTokenSettings(val preferenceStore: IPreferenceStore, val colorManager: IColorManager)

abstract class KotlinToken(
		val settings: KotlinTokenSettings, 
		val colorKey: String = PreferenceConstants.EDITOR_JAVA_DEFAULT_COLOR,
		val style: Int = SWT.NORMAL,
		val isUndefined: Boolean = false,
		val isWhitespace: Boolean = false,
		val isEOF: Boolean = false,
		val isOther: Boolean = true) : IToken {
			
	override fun isUndefined(): Boolean = isUndefined
	
	override fun isWhitespace(): Boolean = isWhitespace
	
	override fun isEOF(): Boolean = isEOF
	
	override fun isOther(): Boolean = isOther
	
	override fun getData(): Any {
		val color = settings.colorManager.getColor(PreferenceConverter.getColor(settings.preferenceStore, colorKey))
		return TextAttribute(color, null, style)
	}
	
	companion object {
		platformStatic fun create(leafElement: PsiElement, settings: KotlinTokenSettings): KotlinToken {
			if (leafElement is LeafPsiElement) {
				val elementType = leafElement.getElementType()
				return when {
					JetTokens.KEYWORDS.contains(elementType), 
						JetTokens.SOFT_KEYWORDS.contains(elementType), 
						JetTokens.MODIFIER_KEYWORDS.contains(elementType) -> KotlinKeyword(settings)
					
					JetTokens.IDENTIFIER.equals(elementType) -> KotlinIdentifier(settings)
					
					JetTokens.STRINGS.contains(elementType),
						JetTokens.OPEN_QUOTE.equals(elementType),
						JetTokens.CLOSING_QUOTE.equals(elementType) -> KotlinString(settings)
					
					JetTokens.WHITESPACES.contains(elementType) -> KotlinWhiteSpace(settings)
					
					JetTokens.COMMENTS.contains(elementType) -> KotlinComment(settings)
					
					else -> KotlinUndefinedToken(settings)
				}
			}
			
			return KotlinUndefinedToken(settings)
		}
	}
}

class KotlinKeyword(settings: KotlinTokenSettings) : KotlinToken(settings, PreferenceConstants.EDITOR_JAVA_KEYWORD_COLOR, style = SWT.BOLD, isOther = true)

class KotlinIdentifier(settings: KotlinTokenSettings) : KotlinToken(settings, PreferenceConstants.EDITOR_JAVA_DEFAULT_COLOR, isOther = true)

class KotlinString(settings: KotlinTokenSettings) : KotlinToken(settings, PreferenceConstants.EDITOR_STRING_COLOR, isOther = true)

class KotlinComment(settings: KotlinTokenSettings) : KotlinToken(settings, PreferenceConstants.EDITOR_SINGLE_LINE_COMMENT_COLOR, isOther = true)

class EOFToken(settings: KotlinTokenSettings) : KotlinToken(settings, isEOF = true) 

class KotlinWhiteSpace(settings: KotlinTokenSettings) : KotlinToken(settings, isWhitespace = true)

class KotlinUndefinedToken(settings: KotlinTokenSettings) : KotlinToken(settings, isUndefined = true) 


