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

class AndroidLogDetector : Detector(), SourceCodeScanner {

    override fun getApplicableMethodNames(): List<String> =
        listOf("d", "i", "w", "e", "v", "wtf")

    override fun visitMethodCall(
        context: JavaContext,
        node: UCallExpression,
        method: PsiMethod
    ) {
        val owner = method.containingClass?.qualifiedName ?: return

        if (owner != "android.util.Log") return

        context.report(
            ISSUE,
            node,
            context.getLocation(node),
            "Use the ICompound logging abstraction instead of android.util.Log."
        )
    }

    companion object {
        val ISSUE = Issue.create(
            id = "ICompoundNoAndroidLog",
            briefDescription = "Avoid Android Log",
            explanation = """
                Use the application's logging abstraction instead of
                android.util.Log.

                This allows logging behavior to be controlled consistently
                across debug and release builds.
            """.trimIndent(),
            category = Category.SECURITY,
            priority = 8,
            severity = Severity.ERROR,
            implementation = Implementation(
                AndroidLogDetector::class.java,
                Scope.JAVA_FILE_SCOPE
            )
        )
    }
}
