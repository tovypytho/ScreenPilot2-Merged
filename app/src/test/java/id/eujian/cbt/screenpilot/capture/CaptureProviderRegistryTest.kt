package id.eujian.cbt.screenpilot.capture

import org.junit.After
import org.junit.Assert.assertNull
import org.junit.Assert.assertSame
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class CaptureProviderRegistryTest {

    @Before
    fun setUp() {
        CaptureProviderRegistry.clearAllForTests()
    }

    @After
    fun tearDown() {
        CaptureProviderRegistry.clearAllForTests()
    }

    @Test
    fun `register selects provider`() {
        val provider = TestProvider("A")
        val registration = CaptureProviderRegistry.register(provider)

        assertSame(provider, CaptureProviderRegistry.get())

        registration.close()
        assertNull(CaptureProviderRegistry.get())
    }

    @Test
    fun `newest registration is selected`() {
        val providerA = TestProvider("A")
        val providerB = TestProvider("B")

        val registrationA = CaptureProviderRegistry.register(providerA)
        val registrationB = CaptureProviderRegistry.register(providerB)

        assertSame(providerB, CaptureProviderRegistry.get())

        registrationB.close()
        registrationA.close()
    }

    @Test
    fun `closing stale registration leaves newer provider selected`() {
        val providerA = TestProvider("A")
        val providerB = TestProvider("B")

        val registrationA = CaptureProviderRegistry.register(providerA)
        val registrationB = CaptureProviderRegistry.register(providerB)

        registrationA.close()

        assertSame(providerB, CaptureProviderRegistry.get())

        registrationB.close()
    }

    @Test
    fun `closing current registration restores previous live provider`() {
        val providerA = TestProvider("A")
        val providerB = TestProvider("B")

        val registrationA = CaptureProviderRegistry.register(providerA)
        val registrationB = CaptureProviderRegistry.register(providerB)

        registrationB.close()

        assertSame(providerA, CaptureProviderRegistry.get())

        registrationA.close()
    }

    @Test
    fun `closing all registrations leaves registry empty`() {
        val registrationA = CaptureProviderRegistry.register(TestProvider("A"))
        val registrationB = CaptureProviderRegistry.register(TestProvider("B"))

        registrationA.close()
        registrationB.close()

        assertNull(CaptureProviderRegistry.get())
    }

    @Test
    fun `closing same registration twice is idempotent`() {
        val provider = TestProvider("A")
        val registration = CaptureProviderRegistry.register(provider)

        registration.close()
        registration.close()

        assertNull(CaptureProviderRegistry.get())
    }

    @Test
    fun `closing middle registration does not change newest provider`() {
        val providerA = TestProvider("A")
        val providerB = TestProvider("B")
        val providerC = TestProvider("C")

        val registrationA = CaptureProviderRegistry.register(providerA)
        val registrationB = CaptureProviderRegistry.register(providerB)
        val registrationC = CaptureProviderRegistry.register(providerC)

        registrationB.close()

        assertSame(providerC, CaptureProviderRegistry.get())

        registrationC.close()
        assertSame(providerA, CaptureProviderRegistry.get())

        registrationA.close()
    }

    @Test
    fun `closing newest restores newest remaining live registration`() {
        val providerA = TestProvider("A")
        val providerB = TestProvider("B")
        val providerC = TestProvider("C")

        val registrationA = CaptureProviderRegistry.register(providerA)
        val registrationB = CaptureProviderRegistry.register(providerB)
        val registrationC = CaptureProviderRegistry.register(providerC)

        registrationC.close()

        assertSame(providerB, CaptureProviderRegistry.get())

        registrationB.close()
        assertSame(providerA, CaptureProviderRegistry.get())

        registrationA.close()
    }

    @Test
    fun `arbitrary stale closes preserve newest live registration`() {
        val providerA = TestProvider("A")
        val providerB = TestProvider("B")
        val providerC = TestProvider("C")
        val providerD = TestProvider("D")

        val registrationA = CaptureProviderRegistry.register(providerA)
        val registrationB = CaptureProviderRegistry.register(providerB)
        val registrationC = CaptureProviderRegistry.register(providerC)
        val registrationD = CaptureProviderRegistry.register(providerD)

        registrationB.close()
        registrationA.close()

        assertSame(providerD, CaptureProviderRegistry.get())

        registrationD.close()
        assertSame(providerC, CaptureProviderRegistry.get())

        registrationC.close()
        assertNull(CaptureProviderRegistry.get())
    }

    private class TestProvider(
        private val name: String
    ) : CaptureProvider {
        override suspend fun capture(): CaptureResult {
            return CaptureResult.Error("unused:$name")
        }
    }
}
