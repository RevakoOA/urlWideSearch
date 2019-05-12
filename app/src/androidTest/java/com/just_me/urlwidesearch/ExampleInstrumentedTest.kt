package com.just_me.urlwidesearch

import androidx.test.InstrumentationRegistry
import androidx.test.InstrumentationRegistry.getTargetContext
import androidx.test.runner.AndroidJUnit4

import org.junit.Test
import org.junit.runner.RunWith

import org.junit.Assert.*

/**
 * Instrumented test, which will execute on an Android device.
 *
 * See [testing documentation](http://d.android.com/tools/testing).
 */
@RunWith(AndroidJUnit4::class)
class ExampleInstrumentedTest {
    @Test
    fun useAppContext() {
        // Context of the app under test.
        val appContext = getTargetContext()
        assertEquals("com.just_me.urlwidesearch", appContext.packageName)
    }
}
