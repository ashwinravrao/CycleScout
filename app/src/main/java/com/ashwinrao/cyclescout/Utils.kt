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
import com.ashwinrao.cyclescout.data.remote.response.NearbySearch
import com.google.android.gms.maps.model.Marker
import kotlin.math.max

const val RESULT_EXTRA = "result_extra"
const val START_LATITUDE = 41.88744282963304
const val START_LONGITUDE = -87.65274711534346
const val METER_TO_MILE_CONVERSION_FACTOR = 1609.344

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

/**
 * Generates the image URL that Glide uses to asynchronously fetch and bind to a particular ImageView object.
 */
fun buildImageUrl(context: Context, result: NearbySearch.Result, shopPhotoMaxWidth: Int = 100) =
    String.format(
        context.getString(R.string.places_photo_url),
        shopPhotoMaxWidth,
        result.photos[0].photoReference,
        context.getString(R.string.places_key)
    )