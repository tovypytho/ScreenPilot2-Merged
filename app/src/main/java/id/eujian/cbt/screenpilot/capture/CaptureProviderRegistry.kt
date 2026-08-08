package id.eujian.cbt.screenpilot.capture

object CaptureProviderRegistry {
    @Volatile
    private var provider: CaptureProvider? = null

    fun set(provider: CaptureProvider?) {
        this.provider = provider
    }

    fun get(): CaptureProvider? = provider

    fun clear() {
        provider = null
    }
}
