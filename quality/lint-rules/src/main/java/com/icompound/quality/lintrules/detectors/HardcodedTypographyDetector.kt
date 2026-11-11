package com.icompound.quality.lintrules.detectors

import com.android.tools.lint.detector.api.Category
import com.android.tools.lint.detector.api.Detector
import com.android.tools.lint.detector.api.Implementation
import com.android.tools.lint.detector.api.Issue
import com.android.tools.lint.detector.api.JavaContext
import com.android.tools.lint.detector.api.Scope
import com.android.tools.lint.detector.api.Severity
import com.android.tools.lint.detector.api.SourceCodeScanner
import com.intellij.psi.PsiMethod
import org.jetbrains.uast.UCallExpression

class HardcodedTypographyDetector :
    Detector(),
    SourceCodeScanner {
    override fun getApplicableMethodNames(): List<String> = listOf("TextStyle")

    override fun visitMethodCall(
        context: JavaContext,
        node: UCallExpression,
        method: PsiMethod,
    ) {
        val owner = method.containingClass?.qualifiedName ?: return

        if (owner != "androidx.compose.ui.text.TextStyle") return

        context.report(
            ISSUE,
            node,
            context.getLocation(node),
            "Avoid constructing TextStyle directly. Use ICompoundTheme.typography.",
        )
    }

    companion object {
        val ISSUE =
            Issue.create(
                id = "ICompoundHardcodedTypography",
                briefDescription = "Hardcoded typography",
                explanation =
                    """
                    Use ICompoundTheme.typography for consistent text styling
                    throughout the application.

                    Avoid defining independent TextStyle instances in feature UI.
                    """.trimIndent(),
                category = Category.CUSTOM_LINT_CHECKS,
                priority = 8,
                severity = Severity.ERROR,
                implementation =
                    Implementation(
                        HardcodedTypographyDetector::class.java,
                        Scope.JAVA_FILE_SCOPE,
                    ),
            )
    }
}
