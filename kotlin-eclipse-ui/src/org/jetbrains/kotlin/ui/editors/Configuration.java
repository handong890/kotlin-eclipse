package org.jetbrains.kotlin.ui.editors;

import org.eclipse.jdt.internal.ui.text.JavaPartitionScanner;
import org.eclipse.jdt.ui.text.IJavaPartitions;
import org.eclipse.jface.text.IAutoEditStrategy;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.ITextDoubleClickStrategy;
import org.eclipse.jface.text.TextAttribute;
import org.eclipse.jface.text.contentassist.ContentAssistant;
import org.eclipse.jface.text.contentassist.IContentAssistant;
import org.eclipse.jface.text.presentation.IPresentationReconciler;
import org.eclipse.jface.text.presentation.PresentationReconciler;
import org.eclipse.jface.text.rules.DefaultDamagerRepairer;
import org.eclipse.jface.text.rules.Token;
import org.eclipse.jface.text.source.ISourceViewer;
import org.eclipse.jface.text.source.SourceViewerConfiguration;
import org.jetbrains.kotlin.ui.editors.codeassist.CompletionProcessor;

@SuppressWarnings("restriction")
public class Configuration extends SourceViewerConfiguration {
    private DoubleClickStrategy doubleClickStrategy;
    private Scanner scanner;
    private final ColorManager colorManager;

    public Configuration(ColorManager colorManager) {
        this.colorManager = colorManager;
    }
    
    @Override
    public String[] getConfiguredContentTypes(ISourceViewer sourceViewer) {
        return new String[] {
            IDocument.DEFAULT_CONTENT_TYPE,
            IJavaPartitions.JAVA_DOC,
            IJavaPartitions.JAVA_MULTI_LINE_COMMENT,
            IJavaPartitions.JAVA_SINGLE_LINE_COMMENT,
            IJavaPartitions.JAVA_STRING,
            IJavaPartitions.JAVA_CHARACTER
        };
    }

    @Override
    public ITextDoubleClickStrategy getDoubleClickStrategy(ISourceViewer sourceViewer, String contentType) {
        if (doubleClickStrategy == null)
            doubleClickStrategy = new DoubleClickStrategy();
        return doubleClickStrategy;
    }

    protected Scanner getScanner() {
        if (scanner == null) {
            scanner = new Scanner(colorManager);
            scanner.setDefaultReturnToken(new Token(new TextAttribute(colorManager.getColor(IColorConstants.DEFAULT))));
        }
        return scanner;
    }
    
    @Override
    public String[] getDefaultPrefixes(ISourceViewer sourceViewer, String contentType) {
        return new String[] { "//", "" };
    }
    
    @Override
    public IAutoEditStrategy[] getAutoEditStrategies(ISourceViewer sourceViewer, String contentType) {
        return new IAutoEditStrategy[] { new KotlinAutoIndentStrategy() };
    }
    
    @Override
    public String getConfiguredDocumentPartitioning(ISourceViewer sourceViewer) {
        return JavaPartitionScanner.JAVA_PARTITIONING;
    }
    
    @Override
    public IPresentationReconciler getPresentationReconciler(ISourceViewer sourceViewer) {
        PresentationReconciler reconciler = (PresentationReconciler) super.getPresentationReconciler(sourceViewer);
        
        DefaultDamagerRepairer dr;

        dr = new DefaultDamagerRepairer(getScanner());
        reconciler.setDamager(dr, IDocument.DEFAULT_CONTENT_TYPE);
        reconciler.setRepairer(dr, IDocument.DEFAULT_CONTENT_TYPE);
        
        reconciler.setDamager(dr, IJavaPartitions.JAVA_STRING);
        reconciler.setRepairer(dr, IJavaPartitions.JAVA_STRING);
        
        NonRuleBasedDamagerRepairer ndr = new NonRuleBasedDamagerRepairer(new TextAttribute(
                colorManager.getColor(IColorConstants.COMMENT)));
        reconciler.setDamager(ndr, IJavaPartitions.JAVA_MULTI_LINE_COMMENT);
        reconciler.setRepairer(ndr, IJavaPartitions.JAVA_MULTI_LINE_COMMENT);
        
        reconciler.setDamager(ndr, IJavaPartitions.JAVA_SINGLE_LINE_COMMENT);
        reconciler.setRepairer(ndr, IJavaPartitions.JAVA_SINGLE_LINE_COMMENT);
        
        reconciler.setDamager(ndr, IJavaPartitions.JAVA_DOC);
        reconciler.setRepairer(ndr, IJavaPartitions.JAVA_DOC);
        
        return reconciler;
    }

    @Override
    public IContentAssistant getContentAssistant(ISourceViewer sourceViewer) {
         ContentAssistant assistant= new ContentAssistant();
         assistant.setContentAssistProcessor(new CompletionProcessor(), IDocument.DEFAULT_CONTENT_TYPE);
         assistant.enableAutoActivation(true);
         assistant.setAutoActivationDelay(500);
         assistant.setProposalPopupOrientation(IContentAssistant.PROPOSAL_OVERLAY);
         assistant.setContextInformationPopupOrientation(IContentAssistant.CONTEXT_INFO_ABOVE);

        return assistant;
    }
}