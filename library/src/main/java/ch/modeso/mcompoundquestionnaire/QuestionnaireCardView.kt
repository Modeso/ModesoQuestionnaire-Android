package ch.modeso.mcompoundquestionnaire

import android.animation.Animator
import android.animation.AnimatorSet
import android.animation.ValueAnimator
import android.annotation.TargetApi
import android.content.Context
/* ktlint-disable no-wildcard-imports */
import android.graphics.*
import android.graphics.drawable.Drawable
import android.os.Build
import android.support.v4.content.ContextCompat
import android.text.TextPaint
import android.util.AttributeSet
import android.util.Log
import android.view.Gravity
import android.view.MotionEvent
import android.view.MotionEvent.INVALID_POINTER_ID
import android.view.View
import android.view.animation.AccelerateDecelerateInterpolator
import android.widget.FrameLayout
import android.widget.TextView
import android.support.v4.graphics.drawable.DrawableCompat

/**
 * Created by Hazem on 7/27/2017
 */
class QuestionnaireCardView : View {
    companion object {
        val TAG:String = QuestionnaireCardView.javaClass.simpleName
    }

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

    var textSecondColor: Int = ContextCompat.getColor(context, R.color.colorAccent)
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

    var acceptDrawable: Drawable = Utils.getVectorDrawable(context, R.drawable.ic_check)//.getDrawable(context, R.drawable.ic_check)
        set(value) {
            field = value
            invalidate()
        }

    var cancelDrawable: Drawable = Utils.getVectorDrawable(context, R.drawable.ic_close)
        set(value) {
            field = value
            invalidate()
        }

    var notApplicableDrawable: Drawable = Utils.getVectorDrawable(context, R.drawable.ic_not_applicable)
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

    var cancelDrawableLeft: Int = 0
        set(value) {
            field = value
            invalidate()
        }
    var cancelDrawableTop: Int = 0
        set(value) {
            field = value
            invalidate()
        }
    var cancelDrawableRight: Int = 0
        set(value) {
            field = value
            invalidate()
        }
    var cancelDrawableBottom: Int = 0
        set(value) {
            field = value
            invalidate()
        }
    var acceptDrawableLeft: Int = 0
        set(value) {
            field = value
            invalidate()
        }
    var acceptDrawableTop: Int = 0
        set(value) {
            field = value
            invalidate()
        }
    var acceptDrawableRight: Int = 0
        set(value) {
            field = value
            invalidate()
        }
    var acceptDrawableBottom: Int = 0
        set(value) {
            field = value
            invalidate()
        }

    var cancelDrawableAlpha: Int = 255
        set(value) {
            field = value
            invalidate()
        }

    var acceptDrawableAlpha: Int = 255
        set(value) {
            field = value
            invalidate()
        }

    var animationDuration: Float = 1000f
    var maxRadius: Float = 0f

    var animatorSet: AnimatorSet = AnimatorSet()

    var cardStatus: CardStatus = CardStatus.NONE

    var cardInteractionCallbacks: CardInteractionCallbacks? = null

    var originalX = 0f
    var originalY = 0f
    var originalWidth = 0f
    var originalHeight = 0f
    var rotationAngle = 0f
    var initialY = 0f

    private var lastX: Float = 0f
    private var lastY: Float = 0f
    var movingHorizontal = false
    private var lastFraction: Float = 0f
    var cardMoving = false
        set(value) {
            field = value
            if (parent != null && parent.parent != null && parent.parent.parent != null && parent.parent.parent is MCompoundQuestionnaire) {
                (parent.parent.parent as MCompoundQuestionnaire).cardMoving = value
            }
            invalidate()
        }
    private var notApplicableRadius = buttonsRadius + maxRadius
    private var notApplicableCenterX = 0f
    private var notApplicableCenterY = 0f
    private var mActivePointerId = INVALID_POINTER_ID
    private var mLastTouchX = 0f
    private var mLastTouchY = 0f
    private var mPosX = 0f
    private var mPosY = 0f

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
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)
        textView.layoutParams = this.layoutParams
        originalWidth = measuredWidth.toFloat()
        originalHeight = measuredHeight.toFloat()
        buttonsRadius = (measuredWidth - 3 * padding) / 4
        textWidth = measuredWidth - 2 * padding
        textHeight = measuredHeight - 3 * padding - 2 * buttonsRadius
        val x0: Float = 2 * padding + 3 * buttonsRadius
        val y0: Float = textHeight + 2 * padding + buttonsRadius
        maxRadius = Math.sqrt(Math.pow((x + measuredWidth - x0).toDouble(), 2.0) + Math.pow((y - y0-buttonsRadius).toDouble(), 2.0)).toFloat() + 5 * density

        cancelLeft = padding
        cancelTop = textHeight + 2 * padding
        cancelRight = padding + 2 * buttonsRadius
        cancelBottom = textHeight + 2 * padding + 2 * buttonsRadius
        acceptLeft = 2 * padding + 2 * buttonsRadius
        acceptTop = textHeight + 2 * padding
        acceptRight = 2 * padding + 4 * buttonsRadius
        acceptBottom = textHeight + 2 * padding + 2 * buttonsRadius

        cancelDrawableLeft = (padding + 2 * buttonsRadius / 3).toInt()
        cancelDrawableTop = (textHeight + 2 * padding + 2 * buttonsRadius / 3).toInt()
        cancelDrawableRight = (padding + 4 * buttonsRadius / 3).toInt()
        cancelDrawableBottom = (textHeight + 2 * padding + 4 * buttonsRadius / 3).toInt()
        acceptDrawableLeft = (2 * padding + 2 * buttonsRadius + 2 * buttonsRadius / 3).toInt()
        acceptDrawableTop = (textHeight + 2 * padding + 2 * buttonsRadius / 3).toInt()
        acceptDrawableRight = (2 * padding + 2 * buttonsRadius + 4 * buttonsRadius / 3).toInt()
        acceptDrawableBottom = (textHeight + 2 * padding + 4 * buttonsRadius / 3).toInt()

        notApplicableCenterY = textHeight + 2 * padding
        notApplicableCenterX = measuredWidth / 2f
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

    fun resetSizes() {
        cancelLeft = padding
        cancelTop = textHeight + 2 * padding
        cancelRight = padding + 2 * buttonsRadius
        cancelBottom = textHeight + 2 * padding + 2 * buttonsRadius
        acceptLeft = 2 * padding + 2 * buttonsRadius
        acceptTop = textHeight + 2 * padding
        acceptRight = 2 * padding + 4 * buttonsRadius
        acceptBottom = textHeight + 2 * padding + 2 * buttonsRadius
    }

    fun onCardMovement(fraction: Float, pulling: Boolean) {
        if (parent != null && parent.parent != null && parent.parent.parent != null && parent.parent.parent is MCompoundQuestionnaire) {
            (parent.parent.parent as MCompoundQuestionnaire).onCardMoving(fraction, y, measuredHeight, pulling)
        }
        cardMoving = fraction > 0f && !movingHorizontal
        if (fraction > 0.07f) {
            notApplicableRadius = buttonsRadius + maxRadius * (fraction - 0.07f)
        } else {
            notApplicableRadius = buttonsRadius
        }
        lastFraction = fraction
        invalidate()
    }

    override fun onDraw(canvas: Canvas?) {
        // background
        background = bgDrawable

        if (cardMoving) {
            if (lastFraction <= 0.07f) {
                val buttonFraction = lastFraction / 0.07f
                val newCancelLeft = cancelLeft + (notApplicableCenterX - (cancelLeft + buttonsRadius)) * buttonFraction
                val newCancelTop = cancelTop + (notApplicableCenterY - (cancelTop + buttonsRadius)) * buttonFraction
                val newCancelRight = cancelRight + (notApplicableCenterX - (cancelRight - buttonsRadius)) * buttonFraction
                val newCancelBottom = cancelBottom + (notApplicableCenterY - (cancelBottom - buttonsRadius)) * buttonFraction
                val newAcceptLeft = acceptLeft + (notApplicableCenterX - (acceptLeft + buttonsRadius)) * buttonFraction
                val newAcceptTop = acceptTop + (notApplicableCenterY - (acceptTop + buttonsRadius)) * buttonFraction
                val newAcceptRight = acceptRight + (notApplicableCenterX - (acceptRight - buttonsRadius)) * buttonFraction
                val newAcceptBottom = acceptBottom + (notApplicableCenterY - (acceptBottom - buttonsRadius)) * buttonFraction
                val newCancelDrawableLeft = (cancelDrawableLeft + (notApplicableCenterX - (cancelDrawableLeft + cancelDrawableRight) / 2) * buttonFraction).toInt()
                val newCancelDrawableTop = (cancelDrawableTop + (notApplicableCenterY - (cancelDrawableTop + cancelDrawableBottom) / 2) * buttonFraction).toInt()
                val newCancelDrawableRight = (cancelDrawableRight + (notApplicableCenterX - (cancelDrawableLeft + cancelDrawableRight) / 2) * buttonFraction).toInt()
                val newCancelDrawableBottom = (cancelDrawableBottom + (notApplicableCenterY - (cancelDrawableTop + cancelDrawableBottom) / 2) * buttonFraction).toInt()
                val newAcceptDrawableLeft = (acceptDrawableLeft + (notApplicableCenterX - (acceptDrawableLeft + acceptDrawableRight) / 2) * buttonFraction).toInt()
                val newAcceptDrawableTop = (acceptDrawableTop + (notApplicableCenterY - (cancelDrawableTop + acceptDrawableBottom) / 2) * buttonFraction).toInt()
                val newAcceptDrawableRight = (acceptDrawableRight + (notApplicableCenterX - (acceptDrawableLeft + acceptDrawableRight) / 2) * buttonFraction).toInt()
                val newAcceptDrawableBottom = (acceptDrawableBottom + (notApplicableCenterY - (cancelDrawableTop + acceptDrawableBottom) / 2) * buttonFraction).toInt()

                val alpha = (255 * (1 - buttonFraction)).toInt()

                cancelCirclePaint.color = Color.argb(alpha, Color.red(cancelColor), Color.green(cancelColor), Color.blue(cancelColor))
                cancelCircleRectF.set(newCancelLeft, newCancelTop, newCancelRight, newCancelBottom)
                canvas?.drawOval(cancelCircleRectF, cancelCirclePaint)

                acceptCirclePaint.color = Color.argb(alpha, Color.red(acceptColor), Color.green(acceptColor), Color.blue(acceptColor))
                acceptCircleRectF.set(newAcceptLeft, newAcceptTop, newAcceptRight, newAcceptBottom)
                canvas?.drawOval(acceptCircleRectF, acceptCirclePaint)

                cancelDrawable.bounds?.set(
                        newCancelDrawableLeft,
                        newCancelDrawableTop,
                        newCancelDrawableRight,
                        newCancelDrawableBottom
                )
                cancelDrawable.alpha = cancelDrawableAlpha
                cancelDrawable.draw(canvas)

                acceptDrawable.bounds?.set(
                        newAcceptDrawableLeft,
                        newAcceptDrawableTop,
                        newAcceptDrawableRight,
                        newAcceptDrawableBottom
                )
                acceptDrawable.alpha = acceptDrawableAlpha
                Log.d("acceptDrawable", "acceptDrawable.bounds :${acceptDrawable.bounds}")
                acceptDrawable.draw(canvas)

            } else {
                notApplicableCirclePaint.color = notApplicableColor
                notApplicableCircleRectF.set(notApplicableCenterX - notApplicableRadius
                        , notApplicableCenterY - notApplicableRadius, notApplicableCenterX + notApplicableRadius
                        , notApplicableCenterY + notApplicableRadius)
                canvas?.drawOval(notApplicableCircleRectF, notApplicableCirclePaint)
                notApplicableDrawable.bounds.set(
                        (notApplicableCenterX - 2 * buttonsRadius / 3).toInt(),
                        (notApplicableCenterY - 2 * buttonsRadius / 3).toInt(),
                        (notApplicableCenterX + 2 * buttonsRadius / 3).toInt(),
                        (notApplicableCenterY + 2 * buttonsRadius / 3).toInt()
                )
                notApplicableDrawable.draw(canvas)

            }
            Log.d("acceptDrawable", "acceptDrawable.bounds :${acceptDrawable.bounds}")
        } else {

            if (cardStatus == CardStatus.ACCEPTED) {
                acceptCirclePaint.color = acceptColor
                acceptCircleRectF.set(acceptLeft, acceptTop, acceptRight, acceptBottom)
                canvas?.drawOval(acceptCircleRectF, acceptCirclePaint)

                cancelCirclePaint.color = cancelColor
                cancelCircleRectF.set(cancelLeft, cancelTop, cancelRight, cancelBottom)
                canvas?.drawOval(cancelCircleRectF, cancelCirclePaint)
            } else {
                //buttons bg
                cancelCirclePaint.color = cancelColor
                cancelCircleRectF.set(cancelLeft, cancelTop, cancelRight, cancelBottom)
                canvas?.drawOval(cancelCircleRectF, cancelCirclePaint)

                acceptCirclePaint.color = acceptColor
                acceptCircleRectF.set(acceptLeft, acceptTop, acceptRight, acceptBottom)
                canvas?.drawOval(acceptCircleRectF, acceptCirclePaint)
            }

            //buttons icon
            cancelDrawable.bounds?.set(
                    cancelDrawableLeft,
                    cancelDrawableTop,
                    cancelDrawableRight,
                    cancelDrawableBottom
            )
            cancelDrawable.alpha = cancelDrawableAlpha
            cancelDrawable.draw(canvas)

            acceptDrawable.bounds?.set(
                    acceptDrawableLeft,
                    acceptDrawableTop,
                    acceptDrawableRight,
                    acceptDrawableBottom
            )
            acceptDrawable.alpha = acceptDrawableAlpha
            Log.d("acceptDrawable", "acceptDrawable.bounds :${acceptDrawable.bounds}")
            acceptDrawable.draw(canvas)

        }

        //text
        if (question != null) {
            textView.isDrawingCacheEnabled = true
            if (cardStatus == CardStatus.NONE) {
                textView.setTextColor(textColor)
            } else {
                textView.setTextColor(textSecondColor)
            }
            textView.textSize = textSize
            textView.gravity = Gravity.START
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
        if (cardStatus == CardStatus.NOT_APPLICABLE) {
            when (event?.actionMasked) {
                MotionEvent.ACTION_DOWN -> {
                    val pointerIndex = event.actionIndex
                    val x = event.rawX
                    val y = event.rawY
                    // Remember where we started (for dragging)
                    mLastTouchX = x
                    mLastTouchY = y
                    mPosX = getX()
                    mPosY = getY()
                    // Save the ID of this pointer (for dragging)
                    mActivePointerId = event.getPointerId(pointerIndex)
                }

                MotionEvent.ACTION_MOVE -> {
                    // Find the index of the active pointer and fetch its position
                    val pointerIndex = event.findPointerIndex(mActivePointerId)
                    val x = event.rawX
                    val y = event.rawY
                    // Calculate the distance moved
                    val dx = x - mLastTouchX
                    val dy = y - mLastTouchY
                    mPosX += dx
                    mPosY += dy
                    this.x = mPosX
                    this.y = mPosY
                    onCardMovement((mPosY - originalY) / initialY, true)
                    cardMoving = true
                    this.rotation = rotationAngle * (mPosY - originalY) / initialY
                    alpha = 0.5F
                    invalidate()

                    // Remember this touch position for the next move event
                    mLastTouchX = x
                    mLastTouchY = y
                }

                MotionEvent.ACTION_UP -> {
                    cardMoving = false
                    alpha = 1F
                    if (isDroppedOnList(mPosX, mPosY)) {
                        if (parent is FrameLayout) {
                            if (parent.parent is FrameLayout) {
                                var viewParent = parent.parent.parent
                                if (viewParent is MCompoundQuestionnaire) {
                                    viewParent.onItemUnDismiss(this)
                                }
                            }
                        }
                    } else {
                        if (parent is FrameLayout) {
                            if (parent.parent is FrameLayout) {
                                if (parent.parent.parent is MCompoundQuestionnaire) {
                                    (parent.parent.parent as MCompoundQuestionnaire).redrawDismissedChild(initialY, rotationAngle)
                                }
                            }
                        }
                    }
                    mActivePointerId = INVALID_POINTER_ID
                }

                MotionEvent.ACTION_CANCEL -> {
                    mActivePointerId = INVALID_POINTER_ID
                }

                MotionEvent.ACTION_POINTER_UP -> {
                    val pointerIndex = event.actionIndex
                    val pointerId = event.getPointerId(pointerIndex)

                    if (pointerId == mActivePointerId) {
                        // This was our active pointer going up. Choose a new
                        // active pointer and adjust accordingly.
                        val newPointerIndex = if (pointerIndex == 0) 1 else 0
                        mLastTouchX = event.rawX
                        mLastTouchY = event.rawY
                        mActivePointerId = event.getPointerId(newPointerIndex)
                    }
                }
            }
        } else {
            if (event != null) {
                if (event.actionMasked == MotionEvent.ACTION_DOWN) {
                    lastX = event.x
                    lastY = event.y
                } else if (event.actionMasked == MotionEvent.ACTION_UP) {
                    if (inCancelCircle(event.x, event.y)) {
                        Log.d(TAG,"Cancel Btn Clicked....")
                        animateCancelButton((animationDuration - (animationDuration / 3)).toLong())
                    } else if (inAcceptCircle(event.x, event.y)) {
                        Log.d(TAG,"Accept Btn Clicked....")
                        animateAcceptButton((animationDuration - (animationDuration / 3)).toLong())
                    }
                    lastX = 0f
                    lastY = 0f
                }
            }
        }
        return super.onTouchEvent(event)
    }

    private fun isDroppedOnList(x: Float, y: Float): Boolean {
        return (x >= (originalX - originalWidth / 3) && x <= (originalX + 2 * originalWidth / 3) && y >= (originalY - measuredHeight / 3) && y <= (originalY + 2 * measuredHeight / 3))
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
                    cancelLeftAnimation(true, false, false, duration),
                    cancelTopAnimation(true, false, false, duration),
                    cancelRightAnimation(true, false, false, duration),
                    cancelBottomAnimation(true, false, false, duration),
                    acceptLeftAnimation(false, false, false, duration),
                    acceptTopAnimation(false, false, false, duration),
                    acceptRightAnimation(false, false, false, duration),
                    acceptBottomAnimation(false, false, false, duration),
                    cancelDrawableLeftAnimation(false, duration),
                    cancelDrawableTopAnimation(false, duration),
                    cancelDrawableRightAnimation(false, duration),
                    cancelDrawableBottomAnimation(false, duration),
                    acceptDrawableLeftAnimation(false, duration),
                    acceptDrawableTopAnimation(false, duration),
                    acceptDrawableRightAnimation(false, duration),
                    acceptDrawableBottomAnimation(false, duration),
                    acceptDrawableAlphaAnimation(false, duration),
                    cancelDrawableAlphaAnimation(false, duration)
            )
            animatorSet.start()
        } else if (cardStatus == CardStatus.ACCEPTED) {
            animatorSet.playTogether(
                    cancelLeftAnimation(true, true, false, duration),
                    cancelTopAnimation(true, true, false, duration),
                    cancelRightAnimation(true, true, false, duration),
                    cancelBottomAnimation(true, true, false, duration),
                    acceptLeftAnimation(false, true, false, duration),
                    acceptTopAnimation(false, true, false, duration),
                    acceptRightAnimation(false, true, false, duration),
                    acceptBottomAnimation(false, true, false, duration),
                    cancelDrawableLeftAnimation(true, duration),
                    cancelDrawableTopAnimation(true, duration),
                    cancelDrawableRightAnimation(true, duration),
                    cancelDrawableBottomAnimation(true, duration),
                    acceptDrawableLeftAnimation(true, duration),
                    acceptDrawableTopAnimation(true, duration),
                    acceptDrawableRightAnimation(true, duration),
                    acceptDrawableBottomAnimation(true, duration),
                    acceptDrawableAlphaAnimation(true, duration),
                    cancelDrawableAlphaAnimation(true, duration)
            )
            cardStatus = CardStatus.NONE
            animatorSet.start()
            cardInteractionCallbacks?.onItemNone(tag as String)
            return
        } else if (cardStatus == CardStatus.CANCELED) {
            animatorSet.playTogether(
                    cancelLeftAnimation(false, true, true, duration),
                    cancelTopAnimation(false, true, true, duration),
                    cancelRightAnimation(false, true, true, duration),
                    cancelBottomAnimation(false, true, true, duration),
                    acceptLeftAnimation(true, true, true, duration),
                    acceptTopAnimation(true, true, true, duration),
                    acceptRightAnimation(true, true, true, duration),
                    acceptBottomAnimation(true, true, true, duration),
                    cancelDrawableLeftAnimation(true, duration),
                    cancelDrawableTopAnimation(true, duration),
                    cancelDrawableRightAnimation(true, duration),
                    cancelDrawableBottomAnimation(true, duration),
                    acceptDrawableLeftAnimation(true, duration),
                    acceptDrawableTopAnimation(true, duration),
                    acceptDrawableRightAnimation(true, duration),
                    acceptDrawableBottomAnimation(true, duration),
                    acceptDrawableAlphaAnimation(true, duration),
                    cancelDrawableAlphaAnimation(true, duration)
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
                            cancelLeftAnimation(true, false, true, duration),
                            cancelTopAnimation(true, false, true, duration),
                            cancelRightAnimation(true, false, true, duration),
                            cancelBottomAnimation(true, false, true, duration),
                            acceptLeftAnimation(false, false, false, duration),
                            acceptTopAnimation(false, false, false, duration),
                            acceptRightAnimation(false, false, false, duration),
                            acceptBottomAnimation(false, false, false, duration),
                            cancelDrawableLeftAnimation(true, duration),
                            cancelDrawableTopAnimation(true, duration),
                            cancelDrawableRightAnimation(true, duration),
                            cancelDrawableBottomAnimation(true, duration),
                            acceptDrawableLeftAnimation(false, duration),
                            acceptDrawableTopAnimation(false, duration),
                            acceptDrawableRightAnimation(false, duration),
                            acceptDrawableBottomAnimation(false, duration),
                            acceptDrawableAlphaAnimation(false, duration),
                            cancelDrawableAlphaAnimation(true, duration)
                    )
                    cardStatus = CardStatus.ACCEPTED
                    animatorSet.start()
                    cardInteractionCallbacks?.onItemAcceptClick(tag as String)
                }

            })
            animatorSet.start()
        } else {
            animatorSet.playTogether(
                    cancelLeftAnimation(true, false, false, duration),
                    cancelTopAnimation(true, false, false, duration),
                    cancelRightAnimation(true, false, false, duration),
                    cancelBottomAnimation(true, false, false, duration),
                    acceptLeftAnimation(false, false, false, duration),
                    acceptTopAnimation(false, false, false, duration),
                    acceptRightAnimation(false, false, false, duration),
                    acceptBottomAnimation(false, false, false, duration),
                    cancelDrawableLeftAnimation(true, duration),
                    cancelDrawableTopAnimation(true, duration),
                    cancelDrawableRightAnimation(true, duration),
                    cancelDrawableBottomAnimation(true, duration),
                    acceptDrawableLeftAnimation(false, duration),
                    acceptDrawableTopAnimation(false, duration),
                    acceptDrawableRightAnimation(false, duration),
                    acceptDrawableBottomAnimation(false, duration),
                    acceptDrawableAlphaAnimation(false, duration),
                    cancelDrawableAlphaAnimation(true, duration)
            )
            cardStatus = CardStatus.ACCEPTED
            animatorSet.start()
            cardInteractionCallbacks?.onItemAcceptClick(tag as String)
        }
    }

    private fun animateCancelButton(duration: Long) {
        if (animatorSet.isRunning) {
            return
        }
        animatorSet = AnimatorSet()
        if (duration == 0L) {
            animatorSet.playTogether(
                    cancelLeftAnimation(false, false, false, duration),
                    cancelTopAnimation(false, false, false, duration),
                    cancelRightAnimation(false, false, false, duration),
                    cancelBottomAnimation(false, false, false, duration),
                    acceptLeftAnimation(true, false, false, duration),
                    acceptTopAnimation(true, false, false, duration),
                    acceptRightAnimation(true, false, false, duration),
                    acceptBottomAnimation(true, false, false, duration),
                    cancelDrawableLeftAnimation(false, duration),
                    cancelDrawableTopAnimation(false, duration),
                    cancelDrawableRightAnimation(false, duration),
                    cancelDrawableBottomAnimation(false, duration),
                    acceptDrawableLeftAnimation(false, duration),
                    acceptDrawableTopAnimation(false, duration),
                    acceptDrawableRightAnimation(false, duration),
                    acceptDrawableBottomAnimation(false, duration),
                    acceptDrawableAlphaAnimation(false, duration),
                    cancelDrawableAlphaAnimation(false, duration)
            )
            animatorSet.start()
        } else if (cardStatus == CardStatus.CANCELED) {
            animatorSet.playTogether(
                    cancelLeftAnimation(false, true, false, duration),
                    cancelTopAnimation(false, true, false, duration),
                    cancelRightAnimation(false, true, false, duration),
                    cancelBottomAnimation(false, true, false, duration),
                    acceptLeftAnimation(true, true, false, duration),
                    acceptTopAnimation(true, true, false, duration),
                    acceptRightAnimation(true, true, false, duration),
                    acceptBottomAnimation(true, true, false, duration),
                    cancelDrawableLeftAnimation(true, duration),
                    cancelDrawableTopAnimation(true, duration),
                    cancelDrawableRightAnimation(true, duration),
                    cancelDrawableBottomAnimation(true, duration),
                    acceptDrawableLeftAnimation(true, duration),
                    acceptDrawableTopAnimation(true, duration),
                    acceptDrawableRightAnimation(true, duration),
                    acceptDrawableBottomAnimation(true, duration),
                    acceptDrawableAlphaAnimation(true, duration),
                    cancelDrawableAlphaAnimation(true, duration)
            )
            cardStatus = CardStatus.NONE
            animatorSet.start()
            cardInteractionCallbacks?.onItemNone(tag as String)
            return
        } else if (cardStatus == CardStatus.ACCEPTED) {
            animatorSet.playTogether(
                    cancelLeftAnimation(true, true, true, duration),
                    cancelTopAnimation(true, true, true, duration),
                    cancelRightAnimation(true, true, true, duration),
                    cancelBottomAnimation(true, true, true, duration),
                    acceptLeftAnimation(false, true, true, duration),
                    acceptTopAnimation(false, true, true, duration),
                    acceptRightAnimation(false, true, true, duration),
                    acceptBottomAnimation(false, true, true, duration),
                    cancelDrawableLeftAnimation(true, duration),
                    cancelDrawableTopAnimation(true, duration),
                    cancelDrawableRightAnimation(true, duration),
                    cancelDrawableBottomAnimation(true, duration),
                    acceptDrawableLeftAnimation(true, duration),
                    acceptDrawableTopAnimation(true, duration),
                    acceptDrawableRightAnimation(true, duration),
                    acceptDrawableBottomAnimation(true, duration),
                    acceptDrawableAlphaAnimation(true, duration),
                    cancelDrawableAlphaAnimation(true, duration)
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
                            cancelLeftAnimation(false, false, false, duration),
                            cancelTopAnimation(false, false, false, duration),
                            cancelRightAnimation(false, false, false, duration),
                            cancelBottomAnimation(false, false, false, duration),
                            acceptLeftAnimation(true, false, true, duration),
                            acceptTopAnimation(true, false, true, duration),
                            acceptRightAnimation(true, false, true, duration),
                            acceptBottomAnimation(true, false, true, duration),
                            cancelDrawableLeftAnimation(false, duration),
                            cancelDrawableTopAnimation(false, duration),
                            cancelDrawableRightAnimation(false, duration),
                            cancelDrawableBottomAnimation(false, duration),
                            acceptDrawableLeftAnimation(true, duration),
                            acceptDrawableTopAnimation(true, duration),
                            acceptDrawableRightAnimation(true, duration),
                            acceptDrawableBottomAnimation(true, duration),
                            acceptDrawableAlphaAnimation(true, duration),
                            cancelDrawableAlphaAnimation(false, duration)
                    )
                    cardStatus = CardStatus.CANCELED
                    animatorSet.start()
                    cardInteractionCallbacks?.onItemCancelClick(tag as String)
                }

            })
            animatorSet.start()
        } else {
            animatorSet.playTogether(
                    cancelLeftAnimation(false, false, false, duration),
                    cancelTopAnimation(false, false, false, duration),
                    cancelRightAnimation(false, false, false, duration),
                    cancelBottomAnimation(false, false, false, duration),
                    acceptLeftAnimation(true, false, false, duration),
                    acceptTopAnimation(true, false, false, duration),
                    acceptRightAnimation(true, false, false, duration),
                    acceptBottomAnimation(true, false, false, duration),
                    cancelDrawableLeftAnimation(false, duration),
                    cancelDrawableTopAnimation(false, duration),
                    cancelDrawableRightAnimation(false, duration),
                    cancelDrawableBottomAnimation(false, duration),
                    acceptDrawableLeftAnimation(true, duration),
                    acceptDrawableTopAnimation(true, duration),
                    acceptDrawableRightAnimation(true, duration),
                    acceptDrawableBottomAnimation(true, duration),
                    acceptDrawableAlphaAnimation(true, duration),
                    cancelDrawableAlphaAnimation(false, duration)
            )
            cardStatus = CardStatus.CANCELED
            animatorSet.start()
            cardInteractionCallbacks?.onItemCancelClick(tag as String)
        }
    }

    private fun cancelLeftAnimation(shrink: Boolean, reverse: Boolean, toZero: Boolean, duration: Long): Animator {
        val newValue = if (toZero) padding + buttonsRadius else if (shrink) {
            if (reverse) {
                padding
            } else {
                padding + buttonsRadius
            }
        } else {
            if (reverse) {
                padding
            } else {
                padding - maxRadius
            }
        }
        val animator = ValueAnimator.ofFloat(cancelLeft, newValue)
        animator.interpolator = AccelerateDecelerateInterpolator()
        animator.duration = duration
        animator.addUpdateListener { cancelLeft = (it.animatedValue as Float) }
        return animator
    }

    private fun cancelTopAnimation(shrink: Boolean, reverse: Boolean, toZero: Boolean, duration: Long): Animator {
        val newValue = if (toZero) textHeight + 2 * padding + buttonsRadius else if (shrink) {
            if (reverse) {
                textHeight + 2 * padding
            } else {
                textHeight + 2 * padding + buttonsRadius
            }
        } else {
            if (reverse) {
                textHeight + 2 * padding
            } else {
                textHeight + 2 * padding - maxRadius
            }
        }
        val animator = ValueAnimator.ofFloat(cancelTop, newValue)
        animator.interpolator = AccelerateDecelerateInterpolator()
        animator.duration = duration
        animator.addUpdateListener { cancelTop = (it.animatedValue as Float) }
        return animator
    }

    private fun cancelRightAnimation(shrink: Boolean, reverse: Boolean, toZero: Boolean, duration: Long): Animator {
        val newValue = if (toZero) padding + buttonsRadius else if (shrink) {
            if (reverse) {
                padding + 2 * buttonsRadius
            } else {
                padding + buttonsRadius
            }
        } else {
            if (reverse) {
                padding + 2 * buttonsRadius
            } else {
                padding + 2 * buttonsRadius + maxRadius
            }
        }
        val animator = ValueAnimator.ofFloat(cancelRight, newValue)
        animator.interpolator = AccelerateDecelerateInterpolator()
        animator.duration = duration
        animator.addUpdateListener { cancelRight = (it.animatedValue as Float) }
        return animator
    }

    private fun cancelBottomAnimation(shrink: Boolean, reverse: Boolean, toZero: Boolean, duration: Long): Animator {
        val newValue = if (toZero) textHeight + 2 * padding + buttonsRadius else if (shrink) {
            if (reverse) {
                textHeight + 2 * padding + 2 * buttonsRadius
            } else {
                textHeight + 2 * padding + buttonsRadius
            }
        } else {
            if (reverse) {
                textHeight + 2 * padding + 2 * buttonsRadius
            } else {
                textHeight + 2 * padding + 2 * buttonsRadius + maxRadius
            }
        }
        val animator = ValueAnimator.ofFloat(cancelBottom, newValue)
        animator.interpolator = AccelerateDecelerateInterpolator()
        animator.duration = duration
        animator.addUpdateListener { cancelBottom = (it.animatedValue as Float) }
        return animator
    }

    private fun acceptLeftAnimation(shrink: Boolean, reverse: Boolean, toZero: Boolean, duration: Long): Animator {
        val newValue = if (toZero) 2 * padding + 3 * buttonsRadius else if (shrink) {
            if (reverse) {
                2 * padding + 2 * buttonsRadius
            } else {
                2 * padding + 3 * buttonsRadius
            }
        } else {
            if (reverse) {
                2 * padding + 2 * buttonsRadius
            } else {
                2 * padding + 2 * buttonsRadius - maxRadius
            }
        }
        val animator = ValueAnimator.ofFloat(acceptLeft, newValue)
        animator.interpolator = AccelerateDecelerateInterpolator()
        animator.duration = duration
        animator.addUpdateListener { acceptLeft = (it.animatedValue as Float) }
        return animator
    }

    private fun acceptTopAnimation(shrink: Boolean, reverse: Boolean, toZero: Boolean, duration: Long): Animator {
        val newValue = if (toZero) textHeight + 2 * padding + buttonsRadius else if (shrink) {
            if (reverse) {
                textHeight + 2 * padding
            } else {
                textHeight + 2 * padding + buttonsRadius
            }
        } else {
            if (reverse) {
                textHeight + 2 * padding
            } else {
                textHeight + 2 * padding - maxRadius
            }
        }
        val animator = ValueAnimator.ofFloat(acceptTop, newValue)
        animator.interpolator = AccelerateDecelerateInterpolator()
        animator.duration = duration
        animator.addUpdateListener { acceptTop = (it.animatedValue as Float) }
        return animator
    }

    private fun acceptRightAnimation(shrink: Boolean, reverse: Boolean, toZero: Boolean, duration: Long): Animator {
        val newValue = if (toZero) 2 * padding + 3 * buttonsRadius else if (shrink) {
            if (reverse) {
                2 * padding + 4 * buttonsRadius
            } else {
                2 * padding + 3 * buttonsRadius
            }
        } else {
            if (reverse) {
                2 * padding + 4 * buttonsRadius
            } else {
                2 * padding + 4 * buttonsRadius + maxRadius
            }
        }
        val animator = ValueAnimator.ofFloat(acceptRight, newValue)
        animator.interpolator = AccelerateDecelerateInterpolator()
        animator.duration = duration
        animator.addUpdateListener { acceptRight = (it.animatedValue as Float) }
        return animator
    }

    private fun acceptBottomAnimation(shrink: Boolean, reverse: Boolean, toZero: Boolean, duration: Long): Animator {
        val newValue = if (toZero) textHeight + 2 * padding + buttonsRadius else if (shrink) {
            if (reverse) {
                textHeight + 2 * padding + 2 * buttonsRadius
            } else {
                textHeight + 2 * padding + buttonsRadius
            }
        } else {
            if (reverse) {
                textHeight + 2 * padding + 2 * buttonsRadius
            } else {
                textHeight + 2 * padding + 2 * buttonsRadius + maxRadius
            }
        }
        val animator = ValueAnimator.ofFloat(acceptBottom, newValue)
        animator.interpolator = AccelerateDecelerateInterpolator()
        animator.duration = duration
        animator.addUpdateListener { acceptBottom = (it.animatedValue as Float) }
        return animator
    }

    private fun cancelDrawableLeftAnimation(reverse: Boolean, duration: Long): Animator {
        val newValue =
                if (reverse) {
                    (padding + 2 * buttonsRadius / 3).toInt()
                } else {
                    cancelDrawableLeft - (buttonsRadius * 0.2).toInt()
                }
        val animator = ValueAnimator.ofInt(cancelDrawableLeft, newValue)
        animator.interpolator = AccelerateDecelerateInterpolator()
        animator.duration = duration
        animator.addUpdateListener { cancelDrawableLeft = (it.animatedValue as Int) }
        return animator
    }

    private fun cancelDrawableTopAnimation(reverse: Boolean, duration: Long): Animator {
        val newValue =
                if (reverse) {
                    (textHeight + 2 * padding + 2 * buttonsRadius / 3).toInt()
                } else {
                    cancelDrawableTop - (buttonsRadius * 0.2).toInt()
                }
        val animator = ValueAnimator.ofInt(cancelDrawableTop, newValue)
        animator.interpolator = AccelerateDecelerateInterpolator()
        animator.duration = duration
        animator.addUpdateListener { cancelDrawableTop = (it.animatedValue as Int) }
        return animator
    }

    private fun cancelDrawableRightAnimation(reverse: Boolean, duration: Long): Animator {
        val newValue =
                if (reverse) {
                    (padding + 4 * buttonsRadius / 3).toInt()
                } else {
                    cancelDrawableRight + (buttonsRadius * 0.2).toInt()
                }
        val animator = ValueAnimator.ofInt(cancelDrawableRight, newValue)
        animator.interpolator = AccelerateDecelerateInterpolator()
        animator.duration = duration
        animator.addUpdateListener { cancelDrawableRight = (it.animatedValue as Int) }
        return animator
    }

    private fun cancelDrawableBottomAnimation(reverse: Boolean, duration: Long): Animator {
        val newValue =
                if (reverse) {
                    (textHeight + 2 * padding + 4 * buttonsRadius / 3).toInt()
                } else {
                    cancelDrawableBottom + (buttonsRadius * 0.2).toInt()
                }
        val animator = ValueAnimator.ofInt(cancelDrawableBottom, newValue)
        animator.interpolator = AccelerateDecelerateInterpolator()
        animator.duration = duration
        animator.addUpdateListener { cancelDrawableBottom = (it.animatedValue as Int) }
        return animator
    }

    private fun acceptDrawableLeftAnimation(reverse: Boolean, duration: Long): Animator {
        val newValue =
                if (reverse) {
                    (2 * padding + 2 * buttonsRadius + 2 * buttonsRadius / 3).toInt()
                } else {
                    acceptDrawableLeft - (buttonsRadius * 0.2).toInt()
                }
        val animator = ValueAnimator.ofInt(acceptDrawableLeft, newValue)
        animator.interpolator = AccelerateDecelerateInterpolator()
        animator.duration = duration
        animator.addUpdateListener { acceptDrawableLeft = (it.animatedValue as Int) }
        return animator
    }

    private fun acceptDrawableTopAnimation(reverse: Boolean, duration: Long): Animator {
        val newValue =
                if (reverse) {
                    (textHeight + 2 * padding + 2 * buttonsRadius / 3).toInt()
                } else {
                    acceptDrawableTop - (buttonsRadius * 0.2).toInt()
                }
        val animator = ValueAnimator.ofInt(acceptDrawableTop, newValue)
        animator.interpolator = AccelerateDecelerateInterpolator()
        animator.duration = duration
        animator.addUpdateListener { acceptDrawableTop = (it.animatedValue as Int) }
        return animator
    }

    private fun acceptDrawableRightAnimation(reverse: Boolean, duration: Long): Animator {
        val newValue =
                if (reverse) {
                    (2 * padding + 2 * buttonsRadius + 4 * buttonsRadius / 3).toInt()
                } else {
                    acceptDrawableRight + (buttonsRadius * 0.2).toInt()
                }
        val animator = ValueAnimator.ofInt(acceptDrawableRight, newValue)
        animator.interpolator = AccelerateDecelerateInterpolator()
        animator.duration = duration
        animator.addUpdateListener { acceptDrawableRight = (it.animatedValue as Int) }
        return animator
    }

    private fun acceptDrawableBottomAnimation(reverse: Boolean, duration: Long): Animator {
        val newValue =
                if (reverse) {
                    (textHeight + 2 * padding + 4 * buttonsRadius / 3).toInt()
                } else {
                    acceptDrawableBottom + (buttonsRadius * 0.2).toInt()
                }
        val animator = ValueAnimator.ofInt(acceptDrawableBottom, newValue)
        animator.interpolator = AccelerateDecelerateInterpolator()
        animator.duration = duration
        animator.addUpdateListener { acceptDrawableBottom = (it.animatedValue as Int) }
        return animator
    }

    private fun acceptDrawableAlphaAnimation(reverse: Boolean, duration: Long): Animator {
        val newValue =
                if (reverse) {
                    255
                } else {
                    (255 * 0.7).toInt()
                }
        val animator = ValueAnimator.ofInt(acceptDrawableAlpha, newValue)
        animator.interpolator = AccelerateDecelerateInterpolator()
        animator.duration = duration
        animator.addUpdateListener { acceptDrawableAlpha = (it.animatedValue as Int) }
        return animator
    }

    private fun cancelDrawableAlphaAnimation(reverse: Boolean, duration: Long): Animator {
        val newValue =
                if (reverse) {
                    255
                } else {
                    (255 * 0.7).toInt()
                }
        val animator = ValueAnimator.ofInt(cancelDrawableAlpha, newValue)
        animator.interpolator = AccelerateDecelerateInterpolator()
        animator.duration = duration
        animator.addUpdateListener { cancelDrawableAlpha = (it.animatedValue as Int) }
        return animator
    }
    fun getBitmapFromVectorDrawable(context: Context, drawableId: Int): Bitmap {
        var drawable = ContextCompat.getDrawable(context, drawableId)
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
            drawable = DrawableCompat.wrap(drawable).mutate()
        }

        val bitmap = Bitmap.createBitmap(drawable.intrinsicWidth,
                drawable.intrinsicHeight, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        drawable.setBounds(0, 0, canvas.width, canvas.height)
        drawable.draw(canvas)

        return bitmap
    }

}