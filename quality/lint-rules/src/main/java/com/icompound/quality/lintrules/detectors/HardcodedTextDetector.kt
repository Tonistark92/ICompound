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

class HardcodedTextDetector :
    Detector(),
    SourceCodeScanner {
    override fun getApplicableMethodNames(): List<String> = listOf("Text", "BasicText")

    override fun visitMethodCall(
        context: JavaContext,
        node: UCallExpression,
        method: PsiMethod,
    ) {
        val packageName =
            method.containingClass
                ?.qualifiedName
                ?.substringBeforeLast(".")
                ?: return

        // Only inspect actual Compose text APIs.
        val isComposeText =
            packageName == "androidx.compose.material" ||
                packageName == "androidx.compose.material3" ||
                packageName == "androidx.compose.foundation.text"

        if (!isComposeText) return

        // The first parameter of Text and BasicText is the text value.
        val textArgument = node.valueArguments.firstOrNull() ?: return

        // Only flag a direct string literal.
        // stringResource(R.string.some_text) is not a literal.
        val literal =
            textArgument as? ULiteralExpression
                ?: return

        if (literal.value !is String) return

        context.report(
            ISSUE,
            textArgument,
            context.getLocation(textArgument),
            "Do not hardcode user-facing text in Compose. " +
                "Move it to strings.xml and use stringResource().",
        )
    }

    companion object {
        val ISSUE: Issue =
            Issue.create(
                id = "ICompoundHardcodedText",
                briefDescription = "Hardcoded text in Compose",
                explanation =
                    """
                    User-facing text must be defined in Android string resources.

                    Use stringResource(R.string.your_string) instead of a string
                    literal in Text or BasicText. This makes the application
                    localizable and keeps UI text consistent.
                    """.trimIndent(),
                category = Category.I18N,
                priority = 8,
                severity = Severity.ERROR,
                implementation =
                    Implementation(
                        HardcodedTextDetector::class.java,
                        Scope.JAVA_FILE_SCOPE,
                    ),
            )
    }
}
