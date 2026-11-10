package com.icompound.quality.lint_rules
//
//import com.android.tools.lint.checks.infrastructure.TestFiles
//import com.android.tools.lint.checks.infrastructure.TestLintTask.lint
//import com.icompound.quality.lint_rules.detectors.NoDirectLoggingDetector
//import org.junit.Test
//
//class NoDirectLoggingDetectorTest {
//
//    private val logStub = TestFiles.java(
//        """
//        package android.util;
//        public class Log { public static int d(String t, String m) { return 0; } }
//        """
//    ).indented()
//
//    @Test
//    fun `flags Log d`() {
//        lint()
//            .files(
//                logStub,
//                TestFiles.kotlin(
//                    """
//                    package com.icompound.sample
//                    import android.util.Log
//                    fun f() { Log.d("T", "m") }
//                    """
//                ).indented()
//            )
//            .issues(NoDirectLoggingDetector.ISSUE)
//            .run()
//            .expectErrorCount(1)
//    }
//
//    @Test
//    fun `flags kotlin println`() {
//        lint()
//            .files(TestFiles.kotlin("package a\nfun f() { println(\"x\") }").indented())
//            .issues(NoDirectLoggingDetector.ISSUE)
//            .run()
//            .expectErrorCount(1)
//    }
//}