package ch.modeso.modesoquestionnaire

import android.content.Context
import android.graphics.drawable.Drawable
import android.support.v4.animation.AnimatorCompatHelper
import android.support.v7.widget.RecyclerView
import android.view.View
import android.view.ViewGroup
import android.view.animation.DecelerateInterpolator
import android.widget.FrameLayout

class DemoAdapter(val context: Context, private val callbacks: CardInteractionCallbacks, val otherViewsHeight: Float, var items: MutableList<BaseModel> = mutableListOf(),
                  cardTextColor: Int, acceptColor: Int, cancelColor: Int, notApplicableColor: Int, cardBackgroundDrawable: Drawable,
                  acceptDrawable: Drawable, cancelDrawable: Drawable, notApplicableDrawable: Drawable, val bottomFrame: FrameLayout)
    : RecyclerView.Adapter<DemoAdapter.ViewHolder>(), View.OnClickListener {

    var cardTextColor = cardTextColor
        set(value) {
            field = value
            this.notifyDataSetChanged()
        }

    var acceptColor = acceptColor
        set(value) {
            field = value
            this.notifyDataSetChanged()
        }

    var cancelColor = cancelColor
        set(value) {
            field = value
            this.notifyDataSetChanged()
        }

    var notApplicableColor = notApplicableColor
        set(value) {
            field = value
            this.notifyDataSetChanged()
        }

    var cardBackgroundDrawable = cardBackgroundDrawable
        set(value) {
            field = value
            this.notifyDataSetChanged()
        }

    var acceptDrawable = acceptDrawable
        set(value) {
            field = value
            this.notifyDataSetChanged()
        }

    var cancelDrawable = cancelDrawable
        set(value) {
            field = value
            this.notifyDataSetChanged()
        }

    var notApplicableDrawable = notApplicableDrawable
        set(value) {
            field = value
            this.notifyDataSetChanged()
        }

    private val mDensity: Int = context.resources.displayMetrics.density.toInt()
    private var mOnItemClickListener: OnItemClickListener? = null

    fun setOnItemClickListener(onItemClickListener: OnItemClickListener) {
        this.mOnItemClickListener = onItemClickListener
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val v = QuestionnaireCardView(context)
        v.cardInteractionCallbacks = callbacks
        v.setOnClickListener(this)
        v.textColor = cardTextColor
        v.acceptColor = acceptColor
        v.cancelColor = cancelColor
        v.notApplicableColor = notApplicableColor
        v.acceptDrawable = acceptDrawable
        v.cancelDrawable = cancelDrawable
        v.notApplicableDrawable = notApplicableDrawable
        v.bgDrawable = cardBackgroundDrawable
        val height = (parent.measuredHeight - otherViewsHeight).toInt()
        val width = parent.measuredWidth - (100 * mDensity)
        v.layoutParams = RecyclerView.LayoutParams(width, height)
        return ViewHolder(v)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = items[position]
        if (holder.itemView is QuestionnaireCardView) {
            holder.itemView.cardStatus = item.status
            holder.itemView.textColor = cardTextColor
            holder.itemView.acceptColor = acceptColor
            holder.itemView.cancelColor = cancelColor
            holder.itemView.notApplicableColor = notApplicableColor
            holder.itemView.acceptDrawable = acceptDrawable
            holder.itemView.cancelDrawable = cancelDrawable
            holder.itemView.notApplicableDrawable = notApplicableDrawable
            holder.itemView.bgDrawable = cardBackgroundDrawable
            holder.itemView.question = item.question
        }
        holder.itemView.tag = item.id
    }

    override fun getItemCount(): Int {
        return items.size
    }

    override fun onClick(v: View) {
        if (mOnItemClickListener != null) {
            val item = items.find { it.id.contentEquals(v.tag as String) }
            if (item != null) {
                val index = items.indexOf(item)
                if (index > -1) {
                    mOnItemClickListener!!.onItemClick(v, index)
                }
            }
        }
    }

    fun onItemDismiss(viewHolder: ViewHolder, deltaX: Float) {
        val position = viewHolder.adapterPosition
        val targetY = viewHolder.itemView.y - viewHolder.itemView.translationY
        callbacks.itemDismiss(items[position].id)
        items.removeAt(position)
        notifyItemRemoved(position)
        val view = viewHolder.itemView
        if (view.parent is ViewGroup) {
            (view.parent as ViewGroup).removeView(view)
            viewHolder.setIsRecyclable(true)
        }
        if (view is QuestionnaireCardView) {
            view.cardStatus = QuestionnaireCardView.CardStatus.NOT_APPLICABLE
            view.originalY = targetY
            view.originalX = view.x
            view.initialY = view.y
            view.rotationAngle = view.rotation
            view.movingHorizontal = true
            view.cardMoving = false
        }
        bottomFrame.addView(view)
        val animatorCompat = AnimatorCompatHelper.emptyValueAnimator()
        animatorCompat.setDuration(1000)
        val interpolator = DecelerateInterpolator()
        val left = view.left
        animatorCompat.addUpdateListener { animation ->
            val fraction = interpolator.getInterpolation(animation.animatedFraction)
            val interpolatedValue = 0 - deltaX * fraction
            view.x = (left + interpolatedValue)
        }
        animatorCompat.start()
    }

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var viewDismiss = ViewDismiss.SHOWN

        fun onItemClear() {

        }

        fun onItemSelected() {

        }
    }

    interface OnItemClickListener {

        fun onItemClick(view: View, position: Int)

    }

    enum class ViewDismiss {
        SHOWN,
        DISMISSED
    }
}
