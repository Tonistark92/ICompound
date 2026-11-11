package com.icompound.quality.lintrules

import com.android.tools.lint.client.api.IssueRegistry
import com.android.tools.lint.detector.api.CURRENT_API
import com.icompound.quality.lintrules.detectors.AndroidLogDetector
import com.icompound.quality.lintrules.detectors.HardcodedColorDetector
import com.icompound.quality.lintrules.detectors.HardcodedSpacingDetector
import com.icompound.quality.lintrules.detectors.HardcodedTextDetector
import com.icompound.quality.lintrules.detectors.HardcodedTypographyDetector
import com.icompound.quality.lintrules.detectors.NoDirectLoggingDetector
import com.icompound.quality.lintrules.detectors.PrintStackTraceDetector

class ICompoundIssueRegistry : IssueRegistry() {
    override val issues =
        listOf(
            // Compose design system
            HardcodedTextDetector.ISSUE,
            HardcodedColorDetector.ISSUE,
            HardcodedTypographyDetector.ISSUE,
            HardcodedSpacingDetector.ISSUE,
            NoDirectLoggingDetector.ISSUE,
            // Code quality
            AndroidLogDetector.ISSUE,
            PrintStackTraceDetector.ISSUE,
        )

    override val api: Int = CURRENT_API
    override val minApi: Int = CURRENT_API
}
