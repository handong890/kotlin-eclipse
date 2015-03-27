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

import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.ui.editors.text.EditorsUI;
import org.eclipse.ui.texteditor.AbstractDecoratedTextEditorPreferenceConstants;
import org.jetbrains.kotlin.testframework.editor.KotlinEditorTestCase;
import org.jetbrains.kotlin.testframework.editor.KotlinEditorWithAfterFileTestCase;
import org.jetbrains.kotlin.testframework.utils.EditorTestUtils;
import org.junit.After;
import org.junit.Before;

public abstract class KotlinAutoIndenterTestCase extends KotlinEditorWithAfterFileTestCase {
    private int initialSpacesCount;
    private Separator initialSeparator;
    
    @Before
    public void configure() {
        initialSeparator = EditorsUI.getPreferenceStore().getBoolean(AbstractDecoratedTextEditorPreferenceConstants.EDITOR_SPACES_FOR_TABS) ? Separator.SPACE : Separator.TAB;
        initialSpacesCount = EditorsUI.getPreferenceStore().getInt(AbstractDecoratedTextEditorPreferenceConstants.EDITOR_TAB_WIDTH);
    }
    
    @Override
    @After
    public void afterTest() {
        super.afterTest();
        setStorePreference(Separator.SPACE == initialSeparator, initialSpacesCount);
    }
    
    @Override
	protected void performTest(String fileText, String expectedFileText) {
    	assert fileText.contains(KotlinEditorTestCase.CARET_TAG);
    	
    	testEditor.typeEnter();
    	EditorTestUtils.assertByEditor(testEditor.getEditor(), expectedFileText);
    }
    
    protected IPreferenceStore getStore() {
        return EditorsUI.getPreferenceStore();
    }
    
    protected void setStorePreference(boolean isSpacesForTabs, int tabWidth) {
        getStore().setValue(AbstractDecoratedTextEditorPreferenceConstants.EDITOR_SPACES_FOR_TABS, isSpacesForTabs);
        getStore().setValue(AbstractDecoratedTextEditorPreferenceConstants.EDITOR_TAB_WIDTH, tabWidth);
    }
}