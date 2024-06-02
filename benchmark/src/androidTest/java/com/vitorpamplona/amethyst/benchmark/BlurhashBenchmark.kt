/**
 * Copyright (c) 2024 Vitor Pamplona
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of
 * this software and associated documentation files (the "Software"), to deal in
 * the Software without restriction, including without limitation the rights to use,
 * copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the
 * Software, and to permit persons to whom the Software is furnished to do so,
 * subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS
 * FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR
 * COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN
 * AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION
 * WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */
package com.vitorpamplona.amethyst.benchmark

import androidx.benchmark.junit4.BenchmarkRule
import androidx.benchmark.junit4.measureRepeated
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.vitorpamplona.amethyst.commons.preview.BlurHashDecoder
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class BlurhashBenchmark {
    @get:Rule
    val benchmarkRule = BenchmarkRule()

    val warmHex = "[45#Y7_2^-xt%OSb%4S0-qt0xbotaRInV|M{RlD~M{M_IVIUNHM{M{M{M{RjNGRkoyj]o[t8tPt8"
    val testHex = "|NHL-]~pabocs+jDM{j?of4T9ZR+WBWZbdR-WCog04ITn\$t6t6t6t6oJoLZ}?bIUWBs:M{WCogRjs:s+o#R+WBoft7axWBx]IV%LogM{t5xaWBay%KRjxus.WCNGWWt7j[j]s+R-S5ofjYV@j[ofD%t8RPoJt7t7R*WCof"

    @Test
    fun testAspectRatio() {
        BlurHashDecoder.aspectRatio(warmHex)

        benchmarkRule.measureRepeated {
            BlurHashDecoder.aspectRatio(testHex)
        }
    }

    @Test
    fun testDecoder() {
        BlurHashDecoder.decodeKeepAspectRatio(warmHex, 50)

        benchmarkRule.measureRepeated {
            BlurHashDecoder.decodeKeepAspectRatio(testHex, 50)
        }
    }
}
