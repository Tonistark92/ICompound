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
import org.jetbrains.uast.UClass
import org.jetbrains.uast.getParentOfType

class NoDirectLoggingDetector :
    Detector(),
    SourceCodeScanner {
    override fun getApplicableMethodNames(): List<String> =
        listOf(
            "d",
            "e",
            "i",
            "v",
            "w",
            "wtf",
            "println",
            "print",
            "printStackTrace",
        )

    override fun visitMethodCall(
        context: JavaContext,
        node: UCallExpression,
        method: PsiMethod,
    ) {
        val owner = method.containingClass?.qualifiedName ?: return
        val methodName = method.name

        // AppLogger is the only approved logging entry point (including its nested classes/lambdas).
        val inAppLogger =
            generateSequence(node.getParentOfType<UClass>(true)) {
                it.getParentOfType<UClass>(true)
            }.any { it.qualifiedName == APP_LOGGER }
        if (inAppLogger) return

        val isForbidden =
            when (owner) {
                "android.util.Log" -> methodName in LOG_METHODS
                "timber.log.Timber" -> methodName in LOG_METHODS
                "java.io.PrintStream" -> methodName == "println" || methodName == "print"
                "kotlin.io.ConsoleKt" -> methodName == "println" || methodName == "print"
                "java.lang.Throwable" -> methodName == "printStackTrace"
                else -> false
            }

        if (isForbidden) {
            context.report(
                issue = ISSUE,
                location = context.getLocation(node),
                message = MESSAGE,
            )
        }
    }

    companion object {
        private const val APP_LOGGER = "com.icompound.core.util.AppLogger"
        private const val MESSAGE = "Use AppLogger instead of direct logging or debugging APIs."
        private val LOG_METHODS = setOf("d", "e", "i", "v", "w", "wtf")

        val ISSUE: Issue =
            Issue.create(
                id = "NoDirectLogging",
                briefDescription = "Direct logging is forbidden",
                explanation =
                    """
                    ICompound uses AppLogger as its centralized logging API.
                    Direct Android Log, Timber, System.out, System.err,
                    and printStackTrace calls are not allowed in production.
                    """.trimIndent(),
                category = Category.CORRECTNESS,
                priority = 8,
                severity = Severity.ERROR,
                implementation =
                    Implementation(
                        NoDirectLoggingDetector::class.java,
                        Scope.JAVA_FILE_SCOPE,
                    ),
            )
    }
}
