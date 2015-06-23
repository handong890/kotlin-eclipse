/*******************************************************************************
 * Copyright 2000-2015 JetBrains s.r.o.
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
package org.jetbrains.kotlin.ui.builder;

import org.eclipse.jdt.core.ElementChangedEvent;
import org.eclipse.jdt.core.IElementChangedListener;
import org.eclipse.jdt.core.IJavaElementDelta;
import org.eclipse.jdt.core.IJavaProject;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.kotlin.core.model.KotlinEnvironment;
import org.jetbrains.kotlin.core.model.KotlinNature;

public class KotlinJavaElementListener implements IElementChangedListener {
    @Override
    public void elementChanged(ElementChangedEvent event) {
        updateEnvironmentIfClasspathChanged(event.getDelta());
    }
    
    public void updateEnvironmentIfClasspathChanged(@NotNull IJavaElementDelta delta) {
        for (IJavaElementDelta affectedChild : delta.getAffectedChildren()) {
            updateEnvironmentIfClasspathChanged(affectedChild);
        }
        
        IJavaProject javaProject = delta.getElement().getJavaProject();
        if (javaProject != null && javaProject.exists() && KotlinNature.hasKotlinNature(javaProject.getProject())) {
            if ((delta.getFlags() & IJavaElementDelta.F_CLASSPATH_CHANGED) != 0) {
                KotlinEnvironment.updateKotlinEnvironment(javaProject);
            } 
        }
    }
}
