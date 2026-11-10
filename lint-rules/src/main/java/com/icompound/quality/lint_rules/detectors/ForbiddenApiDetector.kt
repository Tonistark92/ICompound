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

class ForbiddenApiDetector : Detector(), SourceCodeScanner {

    override fun getApplicableMethodNames(): List<String> = listOf(
        "d", "i", "w", "e", "v", "wtf",
        "printStackTrace"
    )

    override fun visitMethodCall(
        context: JavaContext,
        node: UCallExpression,
        method: PsiMethod
    ) {
        val className = method.containingClass?.qualifiedName ?: return

        when {
            className == "android.util.Log" -> {
                context.report(
                    NO_ANDROID_LOG,
                    node,
                    context.getLocation(node),
                    "Use the ICompound logger instead of Android Log."
                )
            }

            className == "java.lang.Throwable" &&
                    method.name == "printStackTrace" -> {
                context.report(
                    NO_PRINT_STACK_TRACE,
                    node,
                    context.getLocation(node),
                    "Do not print stack traces directly. Use the project logger."
                )
            }
        }
    }

    companion object {

        val NO_ANDROID_LOG = Issue.create(
            id = "ICompoundNoAndroidLog",
            briefDescription = "Avoid Android Log",
            explanation = """
                Use the ICompound logging abstraction instead of android.util.Log.
                This allows logging behavior to be controlled consistently
                across debug and release builds.
            """.trimIndent(),
            category = Category.CORRECTNESS,
            priority = 8,
            severity = Severity.ERROR,
            implementation = Implementation(
                ForbiddenApiDetector::class.java,
                Scope.JAVA_FILE_SCOPE
            )
        )

        val NO_PRINT_STACK_TRACE = Issue.create(
            id = "ICompoundNoPrintStackTrace",
            briefDescription = "Avoid printStackTrace",
            explanation = """
                Do not print exceptions directly. Use the project logger
                so exception reporting follows the application's logging policy.
            """.trimIndent(),
            category = Category.CORRECTNESS,
            priority = 8,
            severity = Severity.ERROR,
            implementation = Implementation(
                ForbiddenApiDetector::class.java,
                Scope.JAVA_FILE_SCOPE
            )
        )
    }
}