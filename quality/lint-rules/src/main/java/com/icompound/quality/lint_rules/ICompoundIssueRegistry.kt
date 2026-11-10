package com.icompound.quality.lint_rules

import com.android.tools.lint.client.api.IssueRegistry
import com.android.tools.lint.detector.api.CURRENT_API
import com.icompound.quality.lint_rules.detectors.AndroidLogDetector
import com.icompound.quality.lint_rules.detectors.ForbiddenApiDetector
import com.icompound.quality.lint_rules.detectors.HardcodedColorDetector
import com.icompound.quality.lint_rules.detectors.HardcodedSpacingDetector
import com.icompound.quality.lint_rules.detectors.HardcodedTextDetector
import com.icompound.quality.lint_rules.detectors.HardcodedTypographyDetector
import com.icompound.quality.lint_rules.detectors.NoDirectLoggingDetector
import com.icompound.quality.lint_rules.detectors.PrintStackTraceDetector

class ICompoundIssueRegistry : IssueRegistry() {

    override val issues = listOf(
        // Compose design system
        HardcodedTextDetector.ISSUE,
        HardcodedColorDetector.ISSUE,
        HardcodedTypographyDetector.ISSUE,
        HardcodedSpacingDetector.ISSUE,
        NoDirectLoggingDetector.ISSUE,
        // Code quality
        AndroidLogDetector.ISSUE,
        PrintStackTraceDetector.ISSUE
    )

    override val api: Int = CURRENT_API
    override val minApi: Int = CURRENT_API
}