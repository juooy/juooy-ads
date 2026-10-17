package kr.co.juooy.ads

/**
 * Holds onReady callbacks until SDK initialization settles, then fires each exactly once.
 *
 * initializeWith* may be called by several screens while init is still in flight
 * (e.g. a base Activity and its subclass). Callbacks registered before [open] are queued
 * instead of dropped. Not thread-safe: call from the main thread only.
 */
internal class ReadyGate {

    private var isOpen = false
    private val pending = mutableListOf<() -> Unit>()

    fun await(callback: (() -> Unit)?) {
        if (callback == null) return
        if (isOpen) callback() else pending += callback
    }

    /** Idempotent: a late success after a timeout does not fire callbacks twice. */
    fun open() {
        if (isOpen) return
        isOpen = true
        val callbacks = pending.toList()
        pending.clear()
        callbacks.forEach { it() }
    }
}
