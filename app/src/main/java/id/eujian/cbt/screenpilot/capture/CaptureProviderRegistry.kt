package id.eujian.cbt.screenpilot.capture

interface CaptureProviderRegistration : AutoCloseable {
    override fun close()
}

object CaptureProviderRegistry {
    private val lock = Any()
    private val registrations = LinkedHashMap<Long, CaptureProvider>()
    private var nextRegistrationId = 0L

    fun register(provider: CaptureProvider): CaptureProviderRegistration {
        val registrationId = synchronized(lock) {
            val id = nextRegistrationId++
            registrations[id] = provider
            id
        }

        return RegistryRegistration(registrationId)
    }

    fun get(): CaptureProvider? = synchronized(lock) {
        registrations.entries.lastOrNull()?.value
    }

    internal fun clearAllForTests() {
        synchronized(lock) {
            registrations.clear()
        }
    }

    private fun unregister(registrationId: Long) {
        synchronized(lock) {
            registrations.remove(registrationId)
        }
    }

    private class RegistryRegistration(
        private val registrationId: Long
    ) : CaptureProviderRegistration {
        override fun close() {
            unregister(registrationId)
        }
    }
}
