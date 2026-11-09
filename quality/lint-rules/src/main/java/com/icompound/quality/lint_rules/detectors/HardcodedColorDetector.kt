package com.icompound.quality.lint_rules.detectors

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

class HardcodedColorDetector : Detector(), SourceCodeScanner {

    override fun getApplicableMethodNames(): List<String> =
        listOf("Color")

    override fun visitMethodCall(
        context: JavaContext,
        node: UCallExpression,
        method: PsiMethod
    ) {
        val owner = method.containingClass?.qualifiedName ?: return

        if (owner != "androidx.compose.ui.graphics.Color") return

        context.report(
            ISSUE,
            node,
            context.getLocation(node),
            "Avoid hardcoded Compose colors. Use ICompoundTheme.colors instead."
        )
    }

    companion object {
        val ISSUE = Issue.create(
            id = "ICompoundHardcodedColor",
            briefDescription = "Hardcoded Compose color",
            explanation = """
                Feature UI should use the centralized ICompound design system
                instead of constructing colors directly.

                Use ICompoundTheme.colors to support consistent theming
                and light/dark mode.
            """.trimIndent(),
            category = Category.CUSTOM_LINT_CHECKS,
            priority = 8,
            severity = Severity.ERROR,
            implementation = Implementation(
                HardcodedColorDetector::class.java,
                Scope.JAVA_FILE_SCOPE
            )
        )
    }
}
