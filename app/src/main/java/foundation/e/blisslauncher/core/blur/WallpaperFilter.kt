package foundation.e.blisslauncher.core.blur

import android.graphics.Bitmap

interface WallpaperFilter<T> {

    fun apply(wallpaper: Bitmap): ApplyTask<T>

    class ApplyTask<T> {

        val emitter = Emitter()

        private var result: T? = null
        private var error: Throwable? = null

        private var callback: ((T?, Throwable?) -> Unit)? = null

        fun setCallback(callback: (T?, Throwable?) -> Unit): ApplyTask<T> {
            result?.let {
                callback(it, null)
                return this
            }
            error?.let {
                callback(null, it)
                return this
            }
            this.callback = callback
            return this
        }

        inner class Emitter {

            fun onSuccess(result: T) {
                callback?.let {
                    it(result, null)
                    return
                }
                this@ApplyTask.result = result
            }

            fun onError(error: Throwable) {
                callback?.let {
                    it(null, error)
                    return
                }
                this@ApplyTask.error = error
            }
        }

        companion object {

            inline fun <T> create(source: (ApplyTask<T>.Emitter) -> Unit): ApplyTask<T> {
                return ApplyTask<T>().also { source(it.emitter) }
            }
        }
    }
}