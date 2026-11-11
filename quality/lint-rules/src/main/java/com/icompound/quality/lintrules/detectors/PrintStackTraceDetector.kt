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

class PrintStackTraceDetector :
    Detector(),
    SourceCodeScanner {
    override fun getApplicableMethodNames(): List<String> = listOf("printStackTrace")

    override fun visitMethodCall(
        context: JavaContext,
        node: UCallExpression,
        method: PsiMethod,
    ) {
        val owner = method.containingClass?.qualifiedName ?: return

        if (!owner.startsWith("java.lang.") &&
            !owner.endsWith("Exception") &&
            !owner.endsWith("Throwable")
        ) {
            return
        }

        context.report(
            ISSUE,
            node,
            context.getLocation(node),
            "Avoid printStackTrace(). Use the ICompound logger instead.",
        )
    }

    companion object {
        val ISSUE =
            Issue.create(
                id = "ICompoundNoPrintStackTrace",
                briefDescription = "Avoid printStackTrace",
                explanation =
                    """
                    Do not print exception stack traces directly.

                    Use the application logging abstraction to ensure exceptions
                    are reported according to the project's logging policy.
                    """.trimIndent(),
                category = Category.CORRECTNESS,
                priority = 8,
                severity = Severity.ERROR,
                implementation =
                    Implementation(
                        PrintStackTraceDetector::class.java,
                        Scope.JAVA_FILE_SCOPE,
                    ),
            )
    }
}
