package ch.modeso.modesoquestionnaire

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
import android.util.AttributeSet
import android.view.View
import android.view.animation.AccelerateDecelerateInterpolator

/**
 * Created by Hazem on 7/31/2017.
 */
class QuestionnaireIndicator : View {

    var bgColor: Int = ContextCompat.getColor(context, R.color.colorPrimaryDark)
        set(value) {
            field = value
            invalidate()
        }

    val transparentColor: Int = ContextCompat.getColor(context, android.R.color.transparent)

    var upperColor: Int = transparentColor
        set(value) {
            field = value
            invalidate()
        }

    var drawIndicator: Boolean  = true
        get() = field
        set(value) {
            field = value
            invalidate()
        }

    var lowerColor: Int = transparentColor
        set(value) {
            field = value
            invalidate()
        }

    var duration: Float = 1000f
        set(value) {
            field = value
            invalidate()
        }

    var noOfPages: Int = 0
        set(value) {
            field = value
            invalidate()
        }

    var fraction: Float = 2.5f
        set(value) {
            field = value
            invalidate()
        }

    var indicator: Drawable = ContextCompat.getDrawable(context, R.drawable.ic_indicator)
        set(drawable) {
            field = drawable
            invalidate()
        }

    var currentPosition: Int = 0
        set(value) {
            field = value
            animateIndicator()
        }

    private val colorList: MutableList<Int> = mutableListOf()

    var bgHeight: Float = 0f
    var upperLowerHeight: Float = 0f

    val bgPaint = Paint()
    val bgRectF = RectF()
    val itemPaint = Paint()
    val itemRectF = RectF()
    val upperPaint = Paint()
    val upperRectF = RectF()
    val lowerPaint = Paint()
    val lowerRectF = RectF()

    fun colorListAddAll(collection: Collection<Int>) {
        colorList.addAll(collection)
        itemSize = measuredWidth.toFloat() / colorList.size
        leftBound = (itemSize * currentPosition /*+ itemSize / 2*/).toInt()
        rightBound = indicator.intrinsicWidth + leftBound
        invalidate()
    }

    fun changeColorAtPosition(position: Int, color: Int) {
        colorList[position] = color
        invalidate()
    }

    private var leftBound: Int = 0
        set(value) {
            field = value
            invalidate()
        }
    private var itemSize: Float = 0f
        set(value) {
            field = value
            invalidate()
        }
    private var rightBound: Int = 0
        set(value) {
            field = value
            invalidate()
        }

    constructor(context: Context) : this(context, null)

    constructor(context: Context, attrs: AttributeSet?) : this(context, attrs, 0)

    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr) {
        init(attrs, defStyleAttr)
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int, defStyleRes: Int) : super(context, attrs, defStyleAttr, defStyleRes) {
        init(attrs, defStyleAttr, defStyleRes)
    }

    private fun init(attrs: AttributeSet?, defStyleAttr: Int = 0, defStyleRes: Int = 0) {
        val typedArray = context.obtainStyledAttributes(attrs, R.styleable.QuestionnaireIndicator, defStyleAttr, defStyleRes)
        indicator = typedArray.getDrawable(R.styleable.QuestionnaireIndicator_mcqIndicatorIcon) ?: indicator
        fraction = typedArray.getFloat(R.styleable.QuestionnaireIndicator_mcqIndicatorFraction, fraction)
        noOfPages = typedArray.getInt(R.styleable.QuestionnaireIndicator_mcqNumberOfPages, noOfPages)
        upperColor = typedArray.getColor(R.styleable.QuestionnaireIndicator_mcqUpperColor, transparentColor)
        lowerColor = typedArray.getColor(R.styleable.QuestionnaireIndicator_mcqLowerColor, transparentColor)
        bgColor = typedArray.getColor(R.styleable.QuestionnaireIndicator_mcqBackgroundColor, bgColor)

        typedArray.recycle()
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)
        if (Math.abs(fraction) == 1f) {
            bgHeight = measuredHeight.toFloat()
            upperLowerHeight = 0f
        } else {
            bgHeight = measuredHeight.toFloat() / fraction
            upperLowerHeight = (measuredHeight.toFloat() - bgHeight) / 2
        }
        itemSize = measuredWidth.toFloat() / colorList.size
        leftBound = (itemSize * currentPosition /*+ itemSize / 2*/).toInt()
        rightBound = (0 /*indicator.intrinsicWidth */+ leftBound).toInt()
    }

    override fun onDraw(canvas: Canvas?) {
        super.onDraw(canvas)
        if (Math.abs(fraction) == 1f) {
            setBackgroundColor(bgColor)
        } else {
            bgPaint.color = bgColor
            bgRectF.set(0f, upperLowerHeight, measuredWidth.toFloat(), upperLowerHeight + bgHeight)
            canvas?.drawRect(bgRectF, bgPaint)

            upperPaint.color = upperColor
            upperRectF.set(0f, 0f, measuredWidth.toFloat(), upperLowerHeight)
            canvas?.drawRect(upperRectF, upperPaint)

            lowerPaint.color = lowerColor
            lowerRectF.set(0f, upperLowerHeight + bgHeight, measuredWidth.toFloat(), 2 * upperLowerHeight + bgHeight)
            canvas?.drawRect(lowerRectF, lowerPaint)
        }

        if (colorList.isEmpty()) {
            indicator.setBounds(0, 0, indicator.intrinsicWidth, measuredHeight)
            if(drawIndicator)
                indicator.draw(canvas)
        } else {
            for (i in 0..colorList.size - 1) {
                itemPaint.color = colorList[i]
                itemRectF.set(itemSize * i, upperLowerHeight, itemSize * i + itemSize, upperLowerHeight + bgHeight)
                canvas?.drawRect(itemRectF, itemPaint)
            }

            indicator.setBounds(leftBound, 0, rightBound+itemSize.toInt()-indicator.intrinsicWidth, (upperLowerHeight+bgHeight).toInt()/*measuredHeight*/)
            if(drawIndicator)
                indicator.draw(canvas)
        }
    }

    private fun animateIndicator() {
        val animatorSet: AnimatorSet = AnimatorSet()
        animatorSet.playTogether(
                animateLeftIndicator(),
                animateRightIndicator()
        )
        animatorSet.start()
    }

    private fun animateLeftIndicator(): Animator {
        val newValue = (itemSize * currentPosition/* + itemSize / 2*/).toInt()

        val animator = ValueAnimator.ofInt(leftBound, newValue)
        animator.interpolator = AccelerateDecelerateInterpolator()
        animator.duration = duration.toLong()
        animator.addUpdateListener { leftBound = (it.animatedValue as Int) }
        return animator
    }

    private fun animateRightIndicator(): Animator {
        val newValue = indicator.intrinsicWidth + (itemSize * currentPosition /*+ itemSize / 2*/).toInt()
        val animator = ValueAnimator.ofInt(rightBound, newValue)
        animator.interpolator = AccelerateDecelerateInterpolator()
        animator.duration = duration.toLong()
        animator.addUpdateListener { rightBound = (it.animatedValue as Int) }
        return animator
    }
}