package com.ashwinrao.cyclescout

import android.content.Context
import android.util.TypedValue

fun dpToPx(context: Context, dp: Float) =
    TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp, context.resources.displayMetrics).toInt()