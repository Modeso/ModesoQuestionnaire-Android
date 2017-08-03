package ch.modeso.mcompoundquestionnaire

import android.animation.Animator
import android.animation.AnimatorSet
import android.animation.ValueAnimator
import android.annotation.TargetApi
import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.RectF
import android.graphics.drawable.Drawable
import android.os.Build
import android.support.v4.content.ContextCompat
import android.text.TextPaint
import android.util.AttributeSet
import android.util.Log
import android.view.MotionEvent
import android.view.View
import android.view.animation.AccelerateDecelerateInterpolator
import android.widget.TextView


/**
 * Created by Hazem on 7/27/2017
 */
class QuestionnaireCardView : View {

    enum class CardStatus {
        NONE,
        ACCEPTED,
        CANCELED,
        NOT_APPLICABLE
    }

    var question: String? = null
        set(value) {
            field = value
            invalidate()
        }

    var textColor: Int = ContextCompat.getColor(context, R.color.colorAccent)
        set(value) {
            field = value
            invalidate()
        }

    var bgDrawable: Drawable = ContextCompat.getDrawable(context, R.drawable.card_bg)
        set(value) {
            field = value
            invalidate()
        }

    var acceptColor: Int = ContextCompat.getColor(context, R.color.colorAccept)
        set(value) {
            field = value
            invalidate()
        }

    var cancelColor: Int = ContextCompat.getColor(context, R.color.colorCancel)
        set(value) {
            field = value
            invalidate()
        }

    var notApplicableColor: Int = ContextCompat.getColor(context, R.color.colorNotApplicable)
        set(value) {
            field = value
            invalidate()
        }

    var acceptDrawable: Drawable = ContextCompat.getDrawable(context, R.drawable.ic_check)
        set(value) {
            field = value
            invalidate()
        }

    var cancelDrawable: Drawable = ContextCompat.getDrawable(context, R.drawable.ic_close)
        set(value) {
            field = value
            invalidate()
        }

    var notApplicableDrawable: Drawable = ContextCompat.getDrawable(context, R.drawable.ic_not_applicable)
        set(value) {
            field = value
            invalidate()
        }

    val acceptCirclePaint = Paint()
    val acceptCircleRectF = RectF()
    val cancelCirclePaint = Paint()
    val cancelCircleRectF = RectF()
    val notApplicableCirclePaint = Paint()
    val notApplicableCircleRectF = RectF()
    val textPaint = TextPaint()

    val density: Float = resources.displayMetrics.density
    val padding: Float
    val textSize: Float
    val textView = TextView(this.context)

    var buttonsRadius: Float = 0f

    var textWidth: Float = 0f
    var textHeight: Float = 0f

    var cancelLeft: Float = 0f
        set(value) {
            field = value
            invalidate()
        }
    var cancelTop: Float = 0f
        set(value) {
            field = value
            invalidate()
        }
    var cancelRight: Float = 0f
        set(value) {
            field = value
            invalidate()
        }
    var cancelBottom: Float = 0f
        set(value) {
            field = value
            invalidate()
        }
    var acceptLeft: Float = 0f
        set(value) {
            field = value
            invalidate()
        }
    var acceptTop: Float = 0f
        set(value) {
            field = value
            invalidate()
        }
    var acceptRight: Float = 0f
        set(value) {
            field = value
            invalidate()
        }
    var acceptBottom: Float = 0f
        set(value) {
            field = value
            invalidate()
        }
    val animationDuration: Long = 1000
    var maxRadius: Float = 0f

    var animatorSet: AnimatorSet = AnimatorSet()

    var cardStatus: CardStatus = CardStatus.NONE

    var cardInteractionCallbacks: CardInteractionCallbacks? =null

    private var lastX: Float = 0f
    private var lastY: Float = 0f

    constructor(context: Context) : this(context, null)

    constructor(context: Context, attrs: AttributeSet?) : this(context, attrs, 0)

    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr) {

    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int, defStyleRes: Int) : super(context, attrs, defStyleAttr, defStyleRes) {

    }

    init {
        padding = density * 40
        textSize = 18f
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        Log.d("Test", "onMeasure")
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)
        textView.layoutParams = this.layoutParams
        buttonsRadius = (measuredWidth - 3 * padding) / 4
        textWidth = measuredWidth - 2 * padding
        textHeight = measuredHeight - 3 * padding - 2 * buttonsRadius
        val x0: Float = 2 * padding + 3 * buttonsRadius
        val y0: Float = textHeight + 2 * padding + buttonsRadius
        maxRadius = Math.sqrt(Math.pow((x + measuredWidth - x0).toDouble(), 2.0) + Math.pow((y - y0).toDouble(), 2.0)).toFloat() + 5 * density

        cancelLeft = padding
        cancelTop = textHeight + 2 * padding
        cancelRight = padding + 2 * buttonsRadius
        cancelBottom = textHeight + 2 * padding + 2 * buttonsRadius
        acceptLeft = 2 * padding + 2 * buttonsRadius
        acceptTop = textHeight + 2 * padding
        acceptRight = 2 * padding + 4 * buttonsRadius
        acceptBottom = textHeight + 2 * padding + 2 * buttonsRadius

        if (cardStatus == CardStatus.ACCEPTED) {
            cancelLeft += buttonsRadius
            cancelTop += buttonsRadius
            cancelRight -= buttonsRadius
            cancelBottom -= buttonsRadius
            acceptLeft -= maxRadius
            acceptTop -= maxRadius
            acceptRight += maxRadius
            acceptBottom += maxRadius
        } else if (cardStatus == CardStatus.CANCELED) {
            cancelLeft -= maxRadius
            cancelTop -= maxRadius
            cancelRight += maxRadius
            cancelBottom += maxRadius
            acceptLeft += buttonsRadius
            acceptTop += buttonsRadius
            acceptRight -= buttonsRadius
            acceptBottom -= buttonsRadius
        }
    }

    override fun onDraw(canvas: Canvas?) {
        Log.d("Test", "onDraw")
        // background
        background = bgDrawable


        //buttons bg
        cancelCirclePaint.color = cancelColor
        cancelCircleRectF.set(cancelLeft, cancelTop, cancelRight, cancelBottom)
        canvas?.drawOval(cancelCircleRectF, cancelCirclePaint)

        acceptCirclePaint.color = acceptColor
        acceptCircleRectF.set(acceptLeft, acceptTop, acceptRight, acceptBottom)
        canvas?.drawOval(acceptCircleRectF, acceptCirclePaint)


        //buttons icon
        cancelDrawable.bounds?.set(
                (padding + 2 * buttonsRadius / 3).toInt(),
                (textHeight + 2 * padding + 2 * buttonsRadius / 3).toInt(),
                (padding + 4 * buttonsRadius / 3).toInt(),
                (textHeight + 2 * padding + 4 * buttonsRadius / 3).toInt()
        )
        cancelDrawable.draw(canvas)

        acceptDrawable.bounds?.set(
                (2 * padding + 2 * buttonsRadius + 2 * buttonsRadius / 3).toInt(),
                (textHeight + 2 * padding + 2 * buttonsRadius / 3).toInt(),
                (2 * padding + 2 * buttonsRadius + 4 * buttonsRadius / 3).toInt(),
                (textHeight + 2 * padding + 4 * buttonsRadius / 3).toInt()
        )
        acceptDrawable.draw(canvas)

        //text
        if (question != null) {
            textView.isDrawingCacheEnabled = true
            textView.setTextColor(textColor)
            textView.textSize = textSize
            textView.measure(View.MeasureSpec.makeMeasureSpec(textWidth.toInt(), View.MeasureSpec.EXACTLY),
                    View.MeasureSpec.makeMeasureSpec(textHeight.toInt(), View.MeasureSpec.EXACTLY))
            textView.layout(0, 0, textView.measuredWidth, textView.measuredHeight)
            textView.text = question
            if (textView.drawingCache != null) {
                canvas?.drawBitmap(textView.drawingCache, padding, padding, textPaint)
            }
            textView.isDrawingCacheEnabled = false
        }
    }

    override fun onTouchEvent(event: MotionEvent?): Boolean {
        if (event != null) {
            if (event.actionMasked == MotionEvent.ACTION_DOWN) {
                lastX = event.x
                lastY = event.y
            } else if (event.actionMasked == MotionEvent.ACTION_UP) {
                if (inCancelCircle(event.x, event.y)) {
                    animateCancelButton(animationDuration - (animationDuration / 3))
                } else if (inAcceptCircle(event.x, event.y)) {
                    animateAcceptButton(animationDuration - (animationDuration / 3))
                }
                lastX = 0f
                lastY = 0f
            }
        }
        return super.onTouchEvent(event)
    }

    private fun inCancelCircle(x: Float, y: Float): Boolean {
        val x0: Float = padding + buttonsRadius
        val y0: Float = textHeight + 2 * padding + buttonsRadius
        val distance: Float = Math.sqrt(Math.pow((x - x0).toDouble(), 2.0) + Math.pow((y - y0).toDouble(), 2.0)).toFloat()
        val distance2: Float = Math.sqrt(Math.pow((lastX - x0).toDouble(), 2.0) + Math.pow((lastY - y0).toDouble(), 2.0)).toFloat()
        if (distance <= buttonsRadius && distance2 <= buttonsRadius) {
            return true
        }
        return false
    }

    private fun inAcceptCircle(x: Float, y: Float): Boolean {
        val x0: Float = 2 * padding + 3 * buttonsRadius
        val y0: Float = textHeight + 2 * padding + buttonsRadius
        val distance: Float = Math.sqrt(Math.pow((x - x0).toDouble(), 2.0) + Math.pow((y - y0).toDouble(), 2.0)).toFloat()
        val distance2: Float = Math.sqrt(Math.pow((lastX - x0).toDouble(), 2.0) + Math.pow((lastY - y0).toDouble(), 2.0)).toFloat()
        if (distance <= buttonsRadius && distance2 <= buttonsRadius) {
            return true
        }
        return false
    }

    private fun animateAcceptButton(duration: Long) {
        if (animatorSet.isRunning) {
            return
        }
        animatorSet = AnimatorSet()
        if (duration == 0L) {
            animatorSet.playTogether(
                    cancelLeftAnimation(true, false, duration),
                    cancelTopAnimation(true, false, duration),
                    cancelRightAnimation(true, false, duration),
                    cancelBottomAnimation(true, false, duration),
                    acceptLeftAnimation(false, false, duration),
                    acceptTopAnimation(false, false, duration),
                    acceptRightAnimation(false, false, duration),
                    acceptBottomAnimation(false, false, duration)
            )
            animatorSet.start()
        } else if (cardStatus == CardStatus.ACCEPTED) {
            animatorSet.playTogether(
                    cancelLeftAnimation(true, true, duration),
                    cancelTopAnimation(true, true, duration),
                    cancelRightAnimation(true, true, duration),
                    cancelBottomAnimation(true, true, duration),
                    acceptLeftAnimation(false, true, duration),
                    acceptTopAnimation(false, true, duration),
                    acceptRightAnimation(false, true, duration),
                    acceptBottomAnimation(false, true, duration)
            )
            cardStatus = CardStatus.NONE
            animatorSet.start()
            cardInteractionCallbacks?.itemNone(tag as Int)
            return
        } else if (cardStatus == CardStatus.CANCELED) {
            animatorSet.playTogether(
                    cancelLeftAnimation(false, true, duration),
                    cancelTopAnimation(false, true, duration),
                    cancelRightAnimation(false, true, duration),
                    cancelBottomAnimation(false, true, duration),
                    acceptLeftAnimation(true, true, duration),
                    acceptTopAnimation(true, true, duration),
                    acceptRightAnimation(true, true, duration),
                    acceptBottomAnimation(true, true, duration)
            )
            animatorSet.addListener(object : Animator.AnimatorListener {
                override fun onAnimationRepeat(animation: Animator?) {
                }

                override fun onAnimationCancel(animation: Animator?) {
                }

                override fun onAnimationStart(animation: Animator?) {
                }

                override fun onAnimationEnd(animation: Animator?) {
                    animatorSet = AnimatorSet()
                    animatorSet.playTogether(
                            cancelLeftAnimation(true, false, duration),
                            cancelTopAnimation(true, false, duration),
                            cancelRightAnimation(true, false, duration),
                            cancelBottomAnimation(true, false, duration),
                            acceptLeftAnimation(false, false, duration),
                            acceptTopAnimation(false, false, duration),
                            acceptRightAnimation(false, false, duration),
                            acceptBottomAnimation(false, false, duration)
                    )
                    cardStatus = CardStatus.ACCEPTED
                    animatorSet.start()
                    cardInteractionCallbacks?.itemAcceptClick(tag as Int)
                }

            })
            animatorSet.start()
        } else {
            animatorSet.playTogether(
                    cancelLeftAnimation(true, false, duration),
                    cancelTopAnimation(true, false, duration),
                    cancelRightAnimation(true, false, duration),
                    cancelBottomAnimation(true, false, duration),
                    acceptLeftAnimation(false, false, duration),
                    acceptTopAnimation(false, false, duration),
                    acceptRightAnimation(false, false, duration),
                    acceptBottomAnimation(false, false, duration)
            )
            cardStatus = CardStatus.ACCEPTED
            animatorSet.start()
            cardInteractionCallbacks?.itemAcceptClick(tag as Int)
        }
    }

    private fun animateCancelButton(duration: Long) {
        if (animatorSet.isRunning) {
            return
        }
        animatorSet = AnimatorSet()
        if (duration == 0L) {
            animatorSet.playTogether(
                    cancelLeftAnimation(false, false, duration),
                    cancelTopAnimation(false, false, duration),
                    cancelRightAnimation(false, false, duration),
                    cancelBottomAnimation(false, false, duration),
                    acceptLeftAnimation(true, false, duration),
                    acceptTopAnimation(true, false, duration),
                    acceptRightAnimation(true, false, duration),
                    acceptBottomAnimation(true, false, duration)
            )
            animatorSet.start()
        } else if (cardStatus == CardStatus.CANCELED) {
            animatorSet.playTogether(
                    cancelLeftAnimation(false, true, duration),
                    cancelTopAnimation(false, true, duration),
                    cancelRightAnimation(false, true, duration),
                    cancelBottomAnimation(false, true, duration),
                    acceptLeftAnimation(true, true, duration),
                    acceptTopAnimation(true, true, duration),
                    acceptRightAnimation(true, true, duration),
                    acceptBottomAnimation(true, true, duration)
            )
            cardStatus = CardStatus.NONE
            animatorSet.start()
            cardInteractionCallbacks?.itemNone(tag as Int)
            return
        } else if (cardStatus == CardStatus.ACCEPTED) {
            animatorSet.playTogether(
                    cancelLeftAnimation(true, true, duration),
                    cancelTopAnimation(true, true, duration),
                    cancelRightAnimation(true, true, duration),
                    cancelBottomAnimation(true, true, duration),
                    acceptLeftAnimation(false, true, duration),
                    acceptTopAnimation(false, true, duration),
                    acceptRightAnimation(false, true, duration),
                    acceptBottomAnimation(false, true, duration)
            )
            animatorSet.addListener(object : Animator.AnimatorListener {
                override fun onAnimationRepeat(animation: Animator?) {
                }

                override fun onAnimationCancel(animation: Animator?) {
                }

                override fun onAnimationStart(animation: Animator?) {
                }

                override fun onAnimationEnd(animation: Animator?) {
                    animatorSet = AnimatorSet()
                    animatorSet.playTogether(
                            cancelLeftAnimation(false, false, duration),
                            cancelTopAnimation(false, false, duration),
                            cancelRightAnimation(false, false, duration),
                            cancelBottomAnimation(false, false, duration),
                            acceptLeftAnimation(true, false, duration),
                            acceptTopAnimation(true, false, duration),
                            acceptRightAnimation(true, false, duration),
                            acceptBottomAnimation(true, false, duration)
                    )
                    cardStatus = CardStatus.CANCELED
                    animatorSet.start()
                    cardInteractionCallbacks?.itemCancelClick(tag as Int)
                }

            })
            animatorSet.start()
        } else {
            animatorSet.playTogether(
                    cancelLeftAnimation(false, false, duration),
                    cancelTopAnimation(false, false, duration),
                    cancelRightAnimation(false, false, duration),
                    cancelBottomAnimation(false, false, duration),
                    acceptLeftAnimation(true, false, duration),
                    acceptTopAnimation(true, false, duration),
                    acceptRightAnimation(true, false, duration),
                    acceptBottomAnimation(true, false, duration)
            )
            cardStatus = CardStatus.CANCELED
            animatorSet.start()
            cardInteractionCallbacks?.itemCancelClick(tag as Int)
        }
    }

    private fun cancelLeftAnimation(shrink: Boolean, reverse: Boolean, duration: Long): Animator {
        val newValue = if (shrink) {
            if (reverse) {
                cancelLeft - buttonsRadius
            } else {
                cancelLeft + buttonsRadius
            }
        } else {
            if (reverse) {
                cancelLeft + maxRadius
            } else {
                cancelLeft - maxRadius
            }
        }
        val animator = ValueAnimator.ofFloat(cancelLeft, newValue)
        animator.interpolator = AccelerateDecelerateInterpolator()
        animator.duration = duration
        animator.addUpdateListener { cancelLeft = (it.animatedValue as Float) }
        return animator
    }

    private fun cancelTopAnimation(shrink: Boolean, reverse: Boolean, duration: Long): Animator {
        val newValue = if (shrink) {
            if (reverse) {
                cancelTop - buttonsRadius
            } else {
                cancelTop + buttonsRadius
            }
        } else {
            if (reverse) {
                cancelTop + maxRadius
            } else {
                cancelTop - maxRadius
            }
        }
        val animator = ValueAnimator.ofFloat(cancelTop, newValue)
        animator.interpolator = AccelerateDecelerateInterpolator()
        animator.duration = duration
        animator.addUpdateListener { cancelTop = (it.animatedValue as Float) }
        return animator
    }

    private fun cancelRightAnimation(shrink: Boolean, reverse: Boolean, duration: Long): Animator {
        val newValue = if (shrink) {
            if (reverse) {
                cancelRight + buttonsRadius
            } else {
                cancelRight - buttonsRadius
            }
        } else {
            if (reverse) {
                cancelRight - maxRadius
            } else {
                cancelRight + maxRadius
            }
        }
        val animator = ValueAnimator.ofFloat(cancelRight, newValue)
        animator.interpolator = AccelerateDecelerateInterpolator()
        animator.duration = duration
        animator.addUpdateListener { cancelRight = (it.animatedValue as Float) }
        return animator
    }

    private fun cancelBottomAnimation(shrink: Boolean, reverse: Boolean, duration: Long): Animator {
        val newValue = if (shrink) {
            if (reverse) {
                cancelBottom + buttonsRadius
            } else {
                cancelBottom - buttonsRadius
            }
        } else {
            if (reverse) {
                cancelBottom - maxRadius
            } else {
                cancelBottom + maxRadius
            }
        }
        val animator = ValueAnimator.ofFloat(cancelBottom, newValue)
        animator.interpolator = AccelerateDecelerateInterpolator()
        animator.duration = duration
        animator.addUpdateListener { cancelBottom = (it.animatedValue as Float) }
        return animator
    }

    private fun acceptLeftAnimation(shrink: Boolean, reverse: Boolean, duration: Long): Animator {
        val newValue = if (shrink) {
            if (reverse) {
                acceptLeft - buttonsRadius
            } else {
                acceptLeft + buttonsRadius
            }
        } else {
            if (reverse) {
                acceptLeft + maxRadius
            } else {
                acceptLeft - maxRadius
            }
        }
        val animator = ValueAnimator.ofFloat(acceptLeft, newValue)
        animator.interpolator = AccelerateDecelerateInterpolator()
        animator.duration = duration
        animator.addUpdateListener { acceptLeft = (it.animatedValue as Float) }
        return animator
    }

    private fun acceptTopAnimation(shrink: Boolean, reverse: Boolean, duration: Long): Animator {
        val newValue = if (shrink) {
            if (reverse) {
                acceptTop - buttonsRadius
            } else {
                acceptTop + buttonsRadius
            }
        } else {
            if (reverse) {
                acceptTop + maxRadius
            } else {
                acceptTop - maxRadius
            }
        }
        val animator = ValueAnimator.ofFloat(acceptTop, newValue)
        animator.interpolator = AccelerateDecelerateInterpolator()
        animator.duration = duration
        animator.addUpdateListener { acceptTop = (it.animatedValue as Float) }
        return animator
    }

    private fun acceptRightAnimation(shrink: Boolean, reverse: Boolean, duration: Long): Animator {
        val newValue = if (shrink) {
            if (reverse) {
                acceptRight + buttonsRadius
            } else {
                acceptRight - buttonsRadius
            }
        } else {
            if (reverse) {
                acceptRight - maxRadius
            } else {
                acceptRight + maxRadius
            }
        }
        val animator = ValueAnimator.ofFloat(acceptRight, newValue)
        animator.interpolator = AccelerateDecelerateInterpolator()
        animator.duration = duration
        animator.addUpdateListener { acceptRight = (it.animatedValue as Float) }
        return animator
    }

    private fun acceptBottomAnimation(shrink: Boolean, reverse: Boolean, duration: Long): Animator {
        val newValue = if (shrink) {
            if (reverse) {
                acceptBottom + buttonsRadius
            } else {
                acceptBottom - buttonsRadius
            }
        } else {
            if (reverse) {
                acceptBottom - maxRadius
            } else {
                acceptBottom + maxRadius
            }
        }
        val animator = ValueAnimator.ofFloat(acceptBottom, newValue)
        animator.interpolator = AccelerateDecelerateInterpolator()
        animator.duration = duration
        animator.addUpdateListener { acceptBottom = (it.animatedValue as Float) }
        return animator
    }

}