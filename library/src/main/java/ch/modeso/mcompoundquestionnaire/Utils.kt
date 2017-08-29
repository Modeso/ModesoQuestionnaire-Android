package ch.modeso.mcompoundquestionnaire

import android.content.Context
import android.graphics.drawable.Drawable
import android.os.Build
import android.support.annotation.DrawableRes
import android.support.annotation.NonNull
import android.support.graphics.drawable.VectorDrawableCompat
import android.support.v4.content.ContextCompat
import android.support.v4.graphics.drawable.DrawableCompat

/**
 * Created by user on 29/08/2017.
 */
class Utils {
    companion object {
        fun getVectorDrawable(@NonNull context: Context, @DrawableRes drawable: Int ): Drawable {
            var vectorDrawable: Drawable
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                vectorDrawable = ContextCompat.getDrawable(context, drawable)
            } else {
                vectorDrawable = VectorDrawableCompat.create(context.getResources(), drawable, null) as Drawable
                if (vectorDrawable != null) {
                    vectorDrawable = DrawableCompat.wrap(vectorDrawable)
                }
            }
            return vectorDrawable
        }
    }

}