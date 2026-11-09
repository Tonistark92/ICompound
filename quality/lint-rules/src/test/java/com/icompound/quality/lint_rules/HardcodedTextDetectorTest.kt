package com.icompound.quality.lint_rules
//
//import com.android.tools.lint.checks.infrastructure.LintDetectorTest
//import com.icompound.quality.lint_rules.detectors.HardcodedTextDetector
//import org.junit.Test
//
//class HardcodedTextDetectorTest : LintDetectorTest() {
//
//    override fun getDetector() = HardcodedTextDetector()
//
//    override fun getIssues() = listOf(
//        HardcodedTextDetector.ISSUE
//    )
//
//    @Test
//    fun `hardcoded Text string is reported`() {
//        lint()
//            .files(
//                kotlin(
//                    """
//                    package com.example
//
//                    import androidx.compose.material3.Text
//
//                    fun Welcome() {
//                        Text("Welcome")
//                    }
//                    """
//                )
//            )
//            .run()
//            .expectErrorCount(1)
//    }
//
//    @Test
//    fun `stringResource is allowed`() {
//        lint()
//            .files(
//                kotlin(
//                    """
//                    package com.example
//
//                    import androidx.compose.material3.Text
//                    import androidx.compose.ui.res.stringResource
//                    import com.example.R
//
//                    fun Welcome() {
//                        Text(stringResource(R.string.welcome))
//                    }
//                    """
//                )
//            )
//            .run()
//            .expectClean()
//    }
//}