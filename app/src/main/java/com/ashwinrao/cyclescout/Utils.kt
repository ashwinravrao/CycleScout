package com.ashwinrao.cyclescout

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.drawable.VectorDrawable
import android.os.Handler
import android.os.SystemClock
import android.util.TypedValue
import android.view.animation.Interpolator
import androidx.interpolator.view.animation.LinearOutSlowInInterpolator
import com.google.android.gms.maps.model.Marker
import kotlin.math.max


fun dpToPx(context: Context, dp: Float) =
    TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp, context.resources.displayMetrics).toInt()

// Adapted from https://stackoverflow.com/questions/13189054/implement-falling-pin-animation-on-google-maps-android
fun animateDropPin(marker: Marker, duration: Long) {
    val handler = Handler()
    val start = SystemClock.uptimeMillis()
    val interpolator: Interpolator = LinearOutSlowInInterpolator()
    handler.post(object : Runnable {
        override fun run() {
            val elapsed = SystemClock.uptimeMillis() - start
            val t = max(1 - interpolator.getInterpolation(elapsed.toFloat() / duration), 0f)
            marker.setAnchor(0.5f, 1.0f + 14 * t)
            if (t > 0.0) {
                // Post again 15ms later.
                handler.postDelayed(this, 15)
            } else {
                marker.showInfoWindow()
            }
        }
    })
}

// Adapted from https://stackoverflow.com/questions/32716570/decoding-svg-image-to-bitmap
fun bitmapFromDrawable(vectorDrawable: VectorDrawable): Bitmap? {
    val bitmap = Bitmap.createBitmap(
        vectorDrawable.intrinsicWidth,
        vectorDrawable.intrinsicHeight, Bitmap.Config.ARGB_8888
    )
    val canvas = Canvas(bitmap)
    vectorDrawable.setBounds(0, 0, canvas.getWidth(), canvas.getHeight())
    vectorDrawable.draw(canvas)
    return bitmap
}