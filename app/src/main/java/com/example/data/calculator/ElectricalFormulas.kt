package com.example.data.calculator

import com.example.data.model.CalculationResult
import com.example.data.model.CalculationStep
import java.util.Locale
import kotlin.math.PI
import kotlin.math.acos
import kotlin.math.asin
import kotlin.math.atan
import kotlin.math.cos
import kotlin.math.ln
import kotlin.math.pow
import kotlin.math.sin
import kotlin.math.sqrt
import kotlin.math.tan

object ElectricalFormulas {

    fun fmt(value: Double, decimals: Int = 2): String {
        return if (value.isNaN() || value.isInfinite()) {
            "0.00"
        } else {
            String.format(Locale.US, "%.${decimals}f", value)
        }
    }

    // AWG to mm² conversion lookup table & equation
    fun awgToMm2(awg: Double): Double {
        // d = 0.127 * 92^((36-n)/39) in mm, A = pi*(d/2)^2
        val d = 0.127 * 92.0.pow((36.0 - awg) / 39.0)
        return (PI * (d / 2.0).pow(2.0))
    }

    fun mm2ToAwg(area: Double): Double {
        // n = -39 * log92(d / 0.127) + 36 where d = 2 * sqrt(area / pi)
        val d = 2.0 * sqrt(area / PI)
        return 36.0 - 39.0 * (ln(d / 0.127) / ln(92.0))
    }

    // SWG conversion table lookup
    val swgTable = mapOf(
        0 to 8.229, 1 to 7.620, 2 to 7.010, 3 to 6.401, 4 to 5.893,
        5 to 5.385, 6 to 4.877, 7 to 4.470, 8 to 4.064, 9 to 3.658,
        10 to 3.251, 11 to 2.946, 12 to 2.642, 13 to 2.337, 14 to 2.032,
        15 to 1.829, 16 to 1.626, 17 to 1.422, 18 to 1.219, 19 to 1.016,
        20 to 0.914, 21 to 0.813, 22 to 0.711, 23 to 0.610, 24 to 0.559,
        25 to 0.508, 26 to 0.457, 27 to 0.417, 28 to 0.376, 29 to 0.345,
        30 to 0.315, 32 to 0.274, 34 to 0.234, 36 to 0.193, 38 to 0.152, 40 to 0.122
    )

    // Standard cable sizes in mm²
    val standardCableSizes = listOf(
        1.5, 2.5, 4.0, 6.0, 10.0, 16.0, 25.0, 35.0, 50.0, 70.0,
        95.0, 120.0, 150.0, 185.0, 240.0, 300.0, 400.0, 500.0, 630.0
    )

    // Standard breaker sizes (Amperes)
    val standardBreakerRatings = listOf(
        6, 10, 16, 20, 25, 32, 40, 50, 63, 80, 100, 125, 160, 200, 250, 315, 400, 500, 630, 800, 1000, 1250, 1600, 2000, 2500, 3200, 4000
    )

    fun selectBreaker(current: Double): Int {
        val designCurrent = current * 1.25
        return standardBreakerRatings.firstOrNull { it >= designCurrent } ?: ((designCurrent / 100).toInt() + 1) * 100
    }

    fun selectCable(designCurrent: Double, isCopper: Boolean = true): Double {
        // Approximate standard air/conduit ampacity for copper/aluminum (IEC 60364-5-52)
        val ampacityCu = mapOf(
            1.5 to 19.5, 2.5 to 27.0, 4.0 to 36.0, 6.0 to 46.0, 10.0 to 63.0,
            16.0 to 85.0, 25.0 to 112.0, 35.0 to 138.0, 50.0 to 168.0, 70.0 to 213.0,
            95.0 to 258.0, 120.0 to 299.0, 150.0 to 344.0, 185.0 to 392.0, 240.0 to 461.0,
            300.0 to 530.0, 400.0 to 618.0, 500.0 to 710.0, 630.0 to 810.0
        )
        val factor = if (isCopper) 1.0 else 0.78
        for ((size, amp) in ampacityCu) {
            if (amp * factor >= designCurrent) return size
        }
        return 630.0
    }
}
