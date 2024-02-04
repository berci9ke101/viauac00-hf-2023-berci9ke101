package hu.kszi2.android.schpincer.data

import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex

internal object PreferenceAccessor {
    private var mutex: Mutex = Mutex()

    @OptIn(DelicateCoroutinesApi::class)
    fun lockPreferences(lambda: () -> Unit) {
        GlobalScope.launch {
            mutex.lock()
            try {
                lambda.invoke()
            } finally {
                mutex.unlock()
            }
        }
    }
}