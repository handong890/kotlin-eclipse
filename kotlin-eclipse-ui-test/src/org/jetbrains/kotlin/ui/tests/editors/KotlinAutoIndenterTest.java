/*******************************************************************************
 * Copyright 2000-2014 JetBrains s.r.o.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 *******************************************************************************/
package org.jetbrains.kotlin.ui.tests.editors;

import org.junit.Test;

public class KotlinAutoIndenterTest extends KotlinAutoIndenterTestCase {
	@Override
	protected String getTestDataRelativePath() {
		return "indentation";
	}
	
	@Test
	public void afterCommentBeforeProperty() {
		doAutoTest();
	}
	
	public void afterElse() {
		doAutoTest();
	}
	
	public void afterElseStatement() {
		doAutoTest();
	}
	
	@Test
	public void afterThen() {
		doAutoTest();
	}
	
	@Test
	public void afterGetter() {
		doAutoTest();
	}
	
	@Test
	public void afterSetter() {
		doAutoTest();
	}
	
	@Test
	public void inSetter() {
		doAutoTest();
	}
	
	@Test
	public void afterKDoc() {
		doAutoTest();
	}
	
	@Test
	public void afterKDocBeforeFun() {
		doAutoTest();
	}
	
	@Test
	public void beforeFunInClass() {
		doAutoTest();
	}
	
	@Test
	public void beforeKDoc() {
		doAutoTest();
	}
	
	@Test
	public void inBlockAfterCloseBrace() {
		doAutoTest();
	}
	
	@Test
	public void inBlockBeforeBlock() {
		doAutoTest();
	}
	
	@Test
	public void inKDocBasic() {
		doAutoTest();
	}
	
	@Test
	public void afterBraceBeforeNewLine() {
		doAutoTest();
	}
	
	@Test
	public void afterIfWithoutBraces() {
		doAutoTest();
	}
	
	@Test
	public void afterOpenBrace() {
		doAutoTest();
	}
	
	@Test
	public void afterWhileWithoutBraces() {
		doAutoTest();
	}
	
	@Test
	public void basicIndentation() {
		doAutoTest();
	}
	
	@Test
	public void beforeCloseBrace() {
		doAutoTest();
	}
	
	@Test
	public void beforeCloseBraceWithIndent() {
		doAutoTest();
	}
	
	@Test
	public void beforeFunctionStart() {
		doAutoTest();
	}
	
	@Test
	public void betweenBracesOnOneLine() {
		doAutoTest();
	}
	
	@Test
	public void lineBreakSaveIndent() {
		doAutoTest();
	}
	
	@Test
	public void nestedOperatorsWithBraces() {
		doAutoTest();
	}
	
	@Test
	public void nestedOperatorsWithoutBraces() {
		doAutoTest();
	}
	
	@Test
	public void withBreakLineAfterIf() {
		doAutoTest();
	}
}
