package org.jetbrains.kotlin.testframework.editor;

import java.io.File;
import java.io.IOException;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.ui.PlatformUI;
import org.jetbrains.kotlin.testframework.utils.KotlinTestUtils;
import org.jetbrains.kotlin.testframework.utils.TestJavaProject;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;

import com.intellij.openapi.util.io.FileUtil;

public class KotlinProjectTestCase {
	public static final String CARET_TAG = "<caret>";
	public static final String ERROR_TAG_OPEN = "<error>";
    public static final String ERROR_TAG_CLOSE = "</error>";
    public static final String WARNING_TAG_OPEN = "<warning>";
    public static final String WARNING_TAG_CLOSE = "</warning>";
	
    private static TestJavaProject testJavaProject;
    
    @Before
    public void beforeTest() {
        KotlinTestUtils.refreshWorkspace();
    }
    
    @After
    public void afterTest() {
        PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage().closeAllEditors(false);
        
        if (testJavaProject != null) {
            testJavaProject.clean();
        }
    }
    
    @AfterClass
    public static void afterAllTests() {
        if (testJavaProject != null) {
            testJavaProject.clean();
            testJavaProject.setDefaultSettings();
        }
    }
    
    public static String getText(String testPath) {
        return getText(new File(testPath));
    }
    
    public static String getText(File file) {
        try {
            return String.valueOf(FileUtil.loadFile(file));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
    
    protected void configureProject() {
        configureProject(TextEditorTest.TEST_PROJECT_NAME);
    }
    
    protected void configureProject(String projectName) {
        configureProject(projectName, null);
    }
    
    protected void configureProject(String projectName, String location) {
        testJavaProject = new TestJavaProject(projectName, location);
    }
    
    protected void configureProjectWithStdLib() {
        configureProjectWithStdLib(TextEditorTest.TEST_PROJECT_NAME);
    }
    
    protected void configureProjectWithStdLib(String projectName) {
        configureProjectWithStdLib(projectName, null);
    }
    
    protected void configureProjectWithStdLib(String projectName, String location) {
        configureProject(projectName, location);
        testJavaProject.addKotlinRuntime();
    }
    
    public IFile createSourceFile(String pkg, String fileName, String content) {
        try {
            return testJavaProject.createSourceFile(pkg, fileName, removeTags(content));
        } catch (CoreException e) {
            throw new RuntimeException(e);
        }
    }
    
    public void createSourceFile(String fileName, String content) {
        createSourceFile(TextEditorTest.TEST_PACKAGE_NAME, fileName, content);
    }
    
    protected TextEditorTest configureEditor(String fileName, String content) {
        return configureEditor(fileName, content, TextEditorTest.TEST_PACKAGE_NAME);
    }
    
    protected TextEditorTest configureEditor(String fileName, String content, String packageName) {
    	TextEditorTest testEditor = new TextEditorTest(testJavaProject);
    	testEditor.createEditor(fileName, content, packageName);
    	
    	return testEditor;
    }
    
    protected TestJavaProject getTestProject() {
        return testJavaProject;
    }
    
    public static String resolveTestTags(String text) {
        return text.replaceAll(ERROR_TAG_OPEN, "")
                .replaceAll(ERROR_TAG_CLOSE, "")
                .replaceAll(WARNING_TAG_OPEN, "")
                .replaceAll(WARNING_TAG_CLOSE, "");
    }
    
    public static String removeTags(String text) {
        return resolveTestTags(text).replaceAll(CARET_TAG, "");
    }
}
