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
package org.jetbrains.kotlin.core.utils

import org.eclipse.core.resources.IFile
import org.eclipse.core.resources.IFolder
import org.eclipse.core.resources.IResource
import org.eclipse.core.resources.IResourceDelta
import org.eclipse.core.runtime.CoreException
import org.eclipse.jdt.core.IJavaProject
import org.jetbrains.kotlin.core.builder.KotlinPsiManager
import org.jetbrains.kotlin.core.log.KotlinLogger
import kotlin.platform.platformStatic
import org.eclipse.jdt.core.IPackageFragmentRoot
import org.jetbrains.kotlin.core.model.KotlinNature
import org.eclipse.core.resources.ResourcesPlugin
import org.eclipse.jdt.core.JavaCore

fun addFilesToParseFromKotlinProjectsInWorkspace() {
    ResourcesPlugin.getWorkspace().getRoot().getProjects().forEach { 
    	if (KotlinNature.hasKotlinNature(it)) {
    	    addFilesToParse(JavaCore.create(it))
    	}
    }
}

fun addFilesToParse(javaProject: IJavaProject) {
    if (!KotlinNature.hasKotlinNature(javaProject.getProject())) {
    	throw IllegalArgumentException("Project ${javaProject.getElementName()} has no Kotlin Nature")
    }
    
    javaProject.getPackageFragmentRoots().forEach {
        if (it.getKind() == IPackageFragmentRoot.K_SOURCE) {
            scanForKotlinFiles(it.getResource())
        }
    }
}

private fun scanForKotlinFiles(parentResource: IResource) {
    when (parentResource) {
        is IFile -> {
            if (KotlinPsiManager.isKotlinFile(parentResource)) {
                KotlinPsiManager.INSTANCE.updateProjectPsiSources(parentResource, IResourceDelta.ADDED)
            }
        }
        is IFolder -> parentResource.members().forEach { scanForKotlinFiles(it) }
    }
}