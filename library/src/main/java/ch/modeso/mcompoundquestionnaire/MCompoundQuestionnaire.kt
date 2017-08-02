package ch.modeso.mcompoundquestionnaire

import android.annotation.TargetApi
import android.content.Context
import android.graphics.drawable.Drawable
import android.os.Build
import android.support.v4.content.ContextCompat
import android.support.v7.widget.RecyclerView
import android.util.AttributeSet
import android.view.Gravity
import android.view.View
import android.widget.LinearLayout

/**
 * Created by Hazem on 7/28/2017.
 */
class MCompoundQuestionnaire : LinearLayout, CardInteractionCallbacks {

    var indicatorBackgroundColor: Int = ContextCompat.getColor(context, R.color.colorPrimaryDark)
        set(value) {
            field = value
            questionnaireIndicator?.bgColor = value
        }

    var indicatorUpperColor: Int = ContextCompat.getColor(context, android.R.color.transparent)
        set(value) {
            field = value
            questionnaireIndicator?.upperColor = value
        }

    var indicatorLowerColor: Int = ContextCompat.getColor(context, android.R.color.transparent)
        set(value) {
            field = value
            questionnaireIndicator?.lowerColor = value
        }

    var indicatorIcon: Drawable = ContextCompat.getDrawable(context, R.drawable.ic_indicator)
        set(drawable) {
            field = drawable
            questionnaireIndicator?.indicator = drawable
        }

    var indicatorFraction: Float = 2.5f
        set(value) {
            field = value
            invalidate()
            questionnaireIndicator?.fraction = value
        }

    var cardTextColor: Int = ContextCompat.getColor(context, R.color.colorAccent)
        set(value) {
            field = value
            demoAdapter?.cardTextColor = value
        }
    var acceptColor: Int = ContextCompat.getColor(context, R.color.colorAccept)
        set(value) {
            field = value
            demoAdapter?.acceptColor = value
        }

    var cancelColor: Int = ContextCompat.getColor(context, R.color.colorCancel)
        set(value) {
            field = value
            demoAdapter?.cancelColor = value
        }

    var cardBackgroundDrawable: Drawable = ContextCompat.getDrawable(context, R.drawable.card_bg)
        set(value) {
            field = value
            demoAdapter?.cardBackgroundDrawable = value
        }

    var notApplicableColor: Int = ContextCompat.getColor(context, R.color.colorNotApplicable)
        set(value) {
            field = value
            demoAdapter?.notApplicableColor = value
        }

    var acceptDrawable: Drawable = ContextCompat.getDrawable(context, R.drawable.ic_check)
        set(value) {
            field = value
            demoAdapter?.acceptDrawable = value
        }

    var cancelDrawable: Drawable = ContextCompat.getDrawable(context, R.drawable.ic_close)
        set(value) {
            field = value
            demoAdapter?.cancelDrawable = value
        }

    var notApplicableDrawable: Drawable = ContextCompat.getDrawable(context, R.drawable.ic_not_applicable)
        set(value) {
            field = value
            demoAdapter?.notApplicableDrawable = value
        }

    var questionnaireIndicator: QuestionnaireIndicator? = null
    var recyclerView: RecyclerView? = null
    var demoAdapter: DemoAdapter? = null
    val tileManager = TileLayoutManager()
    private var items: MutableList<BaseModel> = mutableListOf()

    private val progressBarSize = 24
    private val density = context.resources.displayMetrics.density

    private val topPadding = (30 * density).toInt()
    private val bottomView = (30 * density) + 50

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
        val typedArray = context.obtainStyledAttributes(attrs, R.styleable.MCompoundQuestionnaire, defStyleAttr, defStyleRes)
        indicatorIcon = typedArray.getDrawable(R.styleable.MCompoundQuestionnaire_mcqIndicatorDrawableIcon) ?: indicatorIcon
        indicatorFraction = typedArray.getFloat(R.styleable.MCompoundQuestionnaire_mcqIndicatorSizeFraction, indicatorFraction)
        indicatorUpperColor = typedArray.getColor(R.styleable.MCompoundQuestionnaire_mcqIndicatorUpperColor, indicatorUpperColor)
        indicatorLowerColor = typedArray.getColor(R.styleable.MCompoundQuestionnaire_mcqIndicatorLowerColor, indicatorLowerColor)
        indicatorBackgroundColor = typedArray.getColor(R.styleable.MCompoundQuestionnaire_mcqIndicatorBackgroundColor, indicatorBackgroundColor)
        cardTextColor = typedArray.getColor(R.styleable.MCompoundQuestionnaire_mcqCardTextColor, cardTextColor)
        acceptColor = typedArray.getColor(R.styleable.MCompoundQuestionnaire_mcqAcceptColor, acceptColor)
        cancelColor = typedArray.getColor(R.styleable.MCompoundQuestionnaire_mcqCancelColor, cancelColor)
        notApplicableColor = typedArray.getColor(R.styleable.MCompoundQuestionnaire_mcqNotApplicableColor, notApplicableColor)
        cardBackgroundDrawable = typedArray.getDrawable(R.styleable.MCompoundQuestionnaire_mcqCardBackgroundDrawable) ?: cardBackgroundDrawable
        acceptDrawable = typedArray.getDrawable(R.styleable.MCompoundQuestionnaire_mcqAcceptDrawable) ?: acceptDrawable
        cancelDrawable = typedArray.getDrawable(R.styleable.MCompoundQuestionnaire_mcqCancelDrawable) ?: cancelDrawable
        notApplicableDrawable = typedArray.getDrawable(R.styleable.MCompoundQuestionnaire_mcqNotApplicableDrawable) ?: notApplicableDrawable
        typedArray.recycle()

        orientation = LinearLayout.VERTICAL
        gravity = Gravity.CENTER_HORIZONTAL
        if (questionnaireIndicator == null) {
            initProgressBar()
        }
        if (recyclerView == null) {
            initRecyclerView()
        }
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)
    }

    private fun initProgressBar() {
        questionnaireIndicator = QuestionnaireIndicator(context)
        questionnaireIndicator?.layoutParams = LayoutParams(LayoutParams.MATCH_PARENT, (progressBarSize * density).toInt())
        questionnaireIndicator?.fraction = indicatorFraction
        questionnaireIndicator?.lowerColor = indicatorLowerColor
        questionnaireIndicator?.upperColor = indicatorUpperColor
        questionnaireIndicator?.bgColor = indicatorBackgroundColor
        questionnaireIndicator?.indicator = indicatorIcon
        questionnaireIndicator?.colorListAddAll(items.map { getCardColor(it.status) })
        addView(questionnaireIndicator)
    }

    private fun initRecyclerView() {
        recyclerView = RecyclerView(context)
        recyclerView?.layoutParams = LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT)
        recyclerView?.setPadding(0, topPadding, 0, 0)
        demoAdapter = DemoAdapter(context, this, items, cardTextColor, acceptColor, cancelColor, notApplicableColor, cardBackgroundDrawable, acceptDrawable, cancelDrawable, notApplicableDrawable)
        tileManager.attach(recyclerView, 0)
        demoAdapter?.setOnItemClickListener(object : DemoAdapter.OnItemClickListener{
            override fun onItemClick(view: View, position: Int) {
                recyclerView?.smoothScrollToPosition(position)
            }

        })
        tileManager.setOnItemSelectedListener(object : TileLayoutManager.OnItemSelectedListener {
            override fun onItemSelected(recyclerView: RecyclerView, item: View, position: Int) {
                questionnaireIndicator?.currentPosition = position
            }

        })
        val itemTouchHelper = CustomItemTouchHelper(demoAdapter, bottomView)
        itemTouchHelper.attachToRecyclerView(recyclerView)
        recyclerView?.adapter = demoAdapter
        addView(recyclerView)
    }

    override fun itemAcceptClick(itemPosition: Int) {
        questionnaireIndicator?.changeColorAtPosition(itemPosition, acceptColor)
        if (demoAdapter != null) {
            demoAdapter!!.items[itemPosition].status = QuestionnaireCardView.CardStatus.ACCEPTED
        }
    }

    override fun itemCancelClick(itemPosition: Int) {
        questionnaireIndicator?.changeColorAtPosition(itemPosition, cancelColor)
        if (demoAdapter != null) {
            demoAdapter!!.items[itemPosition].status = QuestionnaireCardView.CardStatus.CANCELED
        }
    }

    override fun itemNone(itemPosition: Int) {
        questionnaireIndicator?.changeColorAtPosition(itemPosition, indicatorBackgroundColor)
        if (demoAdapter != null) {
            demoAdapter!!.items[itemPosition].status = QuestionnaireCardView.CardStatus.NONE
        }
    }

    fun updateList(itemsList: MutableList<BaseModel>) {
        items = itemsList
        demoAdapter?.items = items
        demoAdapter?.notifyDataSetChanged()
        questionnaireIndicator?.colorListAddAll(items.map { getCardColor(it.status) })
    }

    private fun getCardColor(cardStatus: QuestionnaireCardView.CardStatus): Int {
        when (cardStatus) {
            QuestionnaireCardView.CardStatus.NONE -> return indicatorBackgroundColor
            QuestionnaireCardView.CardStatus.ACCEPTED -> return acceptColor
            QuestionnaireCardView.CardStatus.CANCELED -> return cancelColor
            QuestionnaireCardView.CardStatus.NOT_APPLICABLE -> return notApplicableColor
        }
    }
}