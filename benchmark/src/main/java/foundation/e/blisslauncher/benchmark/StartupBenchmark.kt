package foundation.e.blisslauncher.benchmark

import androidx.benchmark.macro.CompilationMode
import androidx.benchmark.macro.FrameTimingMetric
import androidx.benchmark.macro.StartupMode
import androidx.benchmark.macro.StartupTimingMetric
import androidx.benchmark.macro.junit4.MacrobenchmarkRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class StartupBenchmark {
    @get:Rule val benchmarkRule = MacrobenchmarkRule()

    @Test fun startupCompilationModeNone() = startup(CompilationMode.None())

    @Test fun startupCompilationBaselineProfile() = startup(CompilationMode.Partial())

    private fun startup(mode: CompilationMode) {
        benchmarkRule.measureRepeated(
            packageName = "foundation.e.blisslauncher",
            iterations = 5,
            startupMode = StartupMode.COLD,
            compilationMode = mode,
            metrics = listOf(StartupTimingMetric(), FrameTimingMetric()),
            setupBlock = { pressHome() }
        ) {
            startActivityAndWait()
        }
    }
}
