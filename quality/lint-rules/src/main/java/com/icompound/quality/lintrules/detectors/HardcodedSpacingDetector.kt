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
import org.jetbrains.uast.ULiteralExpression

class HardcodedSpacingDetector :
    Detector(),
    SourceCodeScanner {
    private val spacingMethods =
        setOf(
            "padding",
            "spacedBy",
        )

    override fun getApplicableMethodNames(): List<String> = spacingMethods.toList()

    override fun visitMethodCall(
        context: JavaContext,
        node: UCallExpression,
        method: PsiMethod,
    ) {
        val owner = method.containingClass?.qualifiedName ?: return

        val isComposeLayout =
            owner.startsWith("androidx.compose.foundation.layout.")

        if (!isComposeLayout) return

        val hasNumericLiteral =
            node.valueArguments.any { argument ->
                val literal = argument as? ULiteralExpression
                literal?.value is Number
            }

        if (!hasNumericLiteral) return

        context.report(
            ISSUE,
            node,
            context.getLocation(node),
            "Avoid hardcoded spacing values. Use ICompoundTheme.spacing instead.",
        )
    }

    companion object {
        val ISSUE =
            Issue.create(
                id = "ICompoundHardcodedSpacing",
                briefDescription = "Hardcoded Compose spacing",
                explanation =
                    """
                    Use ICompoundTheme.spacing for shared spacing values.

                    This keeps margins, padding, and layout spacing consistent
                    throughout the application.
                    """.trimIndent(),
                category = Category.CUSTOM_LINT_CHECKS,
                priority = 6,
                severity = Severity.WARNING,
                implementation =
                    Implementation(
                        HardcodedSpacingDetector::class.java,
                        Scope.JAVA_FILE_SCOPE,
                    ),
            )
    }
}
