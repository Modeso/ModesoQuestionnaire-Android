package ch.modeso.mcompoundquestionnaire

import android.content.Context
import android.graphics.PointF
import android.graphics.Rect
import android.support.v7.widget.LinearSmoothScroller
import android.support.v7.widget.OrientationHelper
import android.support.v7.widget.PagerSnapHelper
import android.support.v7.widget.RecyclerView
import android.support.v7.widget.RecyclerView.SCROLL_STATE_IDLE
import android.util.AttributeSet
import android.util.Log
import android.util.SparseArray
import android.view.View
import android.view.ViewGroup

/**
 * Created by Hazem on 7/21/2017
 */
class TileLayoutManager : RecyclerView.LayoutManager(), RecyclerView.SmoothScroller.ScrollVectorProvider {
    private val TAG = "TileLayoutManager"
    internal val LAYOUT_START = -1

    internal val LAYOUT_END = 1

    private var mFirstVisiblePosition = 0
    private var mLastVisiblePos = 0
    private var mInitialSelectedPosition = 0

    internal var mCurSelectedPosition = -1

    internal var mCurSelectedView: View? = null
    /**
     * Scroll state
     */
    private var mState: State = State()

    private val mSnapHelper = PagerSnapHelper()

    private val mInnerScrollListener = InnerScrollListener()

    private var mCallbackInFling = false

    fun getCurSelectedPosition(): Int {
        return mCurSelectedPosition
    }

    override fun generateDefaultLayoutParams(): RecyclerView.LayoutParams {
        return TileLayoutManager.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.MATCH_PARENT)
    }

    override fun generateLayoutParams(c: Context, attrs: AttributeSet): RecyclerView.LayoutParams {
        return LayoutParams(c, attrs)
    }

    override fun generateLayoutParams(lp: ViewGroup.LayoutParams): RecyclerView.LayoutParams {
        if (lp is ViewGroup.MarginLayoutParams) {
            return LayoutParams(lp)
        } else {
            return LayoutParams(lp)
        }
    }

    override fun checkLayoutParams(lp: RecyclerView.LayoutParams?): Boolean {
        return lp is LayoutParams
    }

    override fun onLayoutChildren(recycler: RecyclerView.Recycler, state: RecyclerView.State?) {
        if (BuildConfig.DEBUG) {
            Log.d(TAG, "onLayoutChildren() called with: state = [$state]")
        }
        if (itemCount == 0) {
            reset()
            detachAndScrapAttachedViews(recycler)
            return
        }
        if (state!!.isPreLayout) {
            return
        }
        if (state.itemCount != 0 && !state.didStructureChange()) {
            if (BuildConfig.DEBUG) {
                Log.d(TAG, "onLayoutChildren: ignore extra layout step")
            }
            return
        }
        if (childCount == 0 || state.didStructureChange()) {
            reset()
        }
        mInitialSelectedPosition = Math.min(Math.max(0, mInitialSelectedPosition), itemCount - 1)
        detachAndScrapAttachedViews(recycler)
        firstFillCover(recycler, 0)
    }

    private fun reset() {
        if (BuildConfig.DEBUG) {
            Log.d(TAG, "reset: ")
        }
        mState.mItemsFrames.clear()
        //when data set update keep the last selected position
        if (mCurSelectedPosition != -1) {
            mInitialSelectedPosition = mCurSelectedPosition
        }
        mInitialSelectedPosition = Math.min(Math.max(0, mInitialSelectedPosition), itemCount - 1)
        mFirstVisiblePosition = mInitialSelectedPosition
        mLastVisiblePos = mInitialSelectedPosition
        mCurSelectedPosition = -1
        if (mCurSelectedView != null) {
            mCurSelectedView!!.isSelected = false
            mCurSelectedView = null
        }
    }

    private fun firstFillCover(recycler: RecyclerView.Recycler, scrollDelta: Int) {
        firstFillWithHorizontal(recycler)

        if (BuildConfig.DEBUG) {
            Log.d(TAG, "firstFillCover finish:first: $mFirstVisiblePosition,last:$mLastVisiblePos")
        }

        var child: View
        for (i in 0..childCount - 1) {
            child = getChildAt(i)
            transformItem(child, calculateToCenterFraction(child, scrollDelta.toFloat()))
        }
        mInnerScrollListener.onScrolled(mRecyclerView, 0, 0)
    }

    fun transformItem(item: View, fraction: Float) {
        item.pivotX = item.width / 2f
        item.pivotY = item.height / 2.0f
        val scale = 1 - 0.2f * Math.abs(fraction)
        item.scaleX = scale
        item.scaleY = scale
    }

    /**
     * Layout the item view witch position specified by [TileLayoutManager.mInitialSelectedPosition] first and then layout the other

     * @param recycler
     * *
     */
    private fun firstFillWithHorizontal(recycler: RecyclerView.Recycler) {
        detachAndScrapAttachedViews(recycler)
        val leftEdge = getOrientationHelper().startAfterPadding
        val rightEdge = getOrientationHelper().endAfterPadding
        val startPosition = mInitialSelectedPosition
        val scrapWidth: Int
        val scrapHeight: Int
        val scrapRect = Rect()
        val topOffset: Int = paddingTop
        //layout the init position view
        val scrap = recycler.getViewForPosition(mInitialSelectedPosition)
        addView(scrap, 0)
        measureChildWithMargins(scrap, 0, 0)
        scrapWidth = getDecoratedMeasuredWidth(scrap)
        scrapHeight = getDecoratedMeasuredHeight(scrap)
        val left = (paddingLeft + (getHorizontalSpace() - scrapWidth) / 2f).toInt()
        scrapRect.set(left, topOffset, left + scrapWidth, topOffset + scrapHeight)
        layoutDecorated(scrap, scrapRect.left, scrapRect.top, scrapRect.right, scrapRect.bottom)
        if (mState.mItemsFrames.get(startPosition) == null) {
            mState.mItemsFrames.put(startPosition, scrapRect)
        } else {
            mState.mItemsFrames.get(startPosition).set(scrapRect)
        }
        mLastVisiblePos = startPosition
        mFirstVisiblePosition = mLastVisiblePos
        val leftStartOffset = getDecoratedLeft(scrap)
        val rightStartOffset = getDecoratedRight(scrap)
        //fill left of center
        fillLeft(recycler, mInitialSelectedPosition - 1, leftStartOffset, leftEdge)
        //fill right of center
        fillRight(recycler, mInitialSelectedPosition + 1, rightStartOffset, rightEdge)
    }

    override fun onItemsRemoved(recyclerView: RecyclerView?, positionStart: Int, itemCount: Int) {
        super.onItemsRemoved(recyclerView, positionStart, itemCount)
    }

    /**
     * Fill left of the center view

     * @param recycler
     * *
     * @param startPosition start position to fill left
     * *
     * @param startOffset layout start offset
     * *
     * @param leftEdge
     */
    private fun fillLeft(recycler: RecyclerView.Recycler, startPosition: Int, startOffset: Int, leftEdge: Int) {
        var Offset = startOffset
        var scrap: View
        var topOffset: Int
        var scrapWidth: Int
        var scrapHeight: Int
        val scrapRect = Rect()
        var i = startPosition
        while (i >= 0 && Offset > leftEdge) {
            scrap = recycler.getViewForPosition(i)
            addView(scrap, 0)
            measureChildWithMargins(scrap, 0, 0)
            scrapWidth = getDecoratedMeasuredWidth(scrap)
            scrapHeight = getDecoratedMeasuredHeight(scrap)
            topOffset = paddingTop
            scrapRect.set(Offset - scrapWidth, topOffset, Offset, topOffset + scrapHeight)
            layoutDecorated(scrap, scrapRect.left, scrapRect.top, scrapRect.right, scrapRect.bottom)
            Offset = scrapRect.left
            mFirstVisiblePosition = i
            if (mState.mItemsFrames.get(i) == null) {
                mState.mItemsFrames.put(i, scrapRect)
            } else {
                mState.mItemsFrames.get(i).set(scrapRect)
            }
            i--
        }
    }

    /**
     * Fill right of the center view

     * @param recycler
     * *
     * @param startPosition start position to fill right
     * *
     * @param startOffset layout start offset
     * *
     * @param rightEdge
     */
    private fun fillRight(recycler: RecyclerView.Recycler, startPosition: Int, startOffset: Int, rightEdge: Int) {
        var Offset = startOffset
        var scrap: View
        var topOffset: Int
        var scrapWidth: Int
        var scrapHeight: Int
        val scrapRect = Rect()
        var i = startPosition
        while (i < itemCount && Offset < rightEdge) {
            scrap = recycler.getViewForPosition(i)
            addView(scrap)
            measureChildWithMargins(scrap, 0, 0)
            scrapWidth = getDecoratedMeasuredWidth(scrap)
            scrapHeight = getDecoratedMeasuredHeight(scrap)
//            topOffset = (paddingTop + (height - scrapHeight) / 2.0f).toInt()
            topOffset = paddingTop
            scrapRect.set(Offset, topOffset, Offset + scrapWidth, topOffset + scrapHeight)
            layoutDecorated(scrap, scrapRect.left, scrapRect.top, scrapRect.right, scrapRect.bottom)
            Offset = scrapRect.right
            mLastVisiblePos = i
            if (mState.mItemsFrames.get(i) == null) {
                mState.mItemsFrames.put(i, scrapRect)
            } else {
                mState.mItemsFrames.get(i).set(scrapRect)
            }
            i++
        }
    }

    private fun fillCover(recycler: RecyclerView.Recycler, scrollDelta: Int) {
        if (itemCount == 0) {
            return
        }
        fillWithHorizontal(recycler, scrollDelta)
        var child: View
        for (i in 0..childCount - 1) {
            child = getChildAt(i)
            transformItem(child, calculateToCenterFraction(child, scrollDelta.toFloat()))
        }
    }

    private fun calculateToCenterFraction(child: View, pendingOffset: Float): Float {
        val distance = calculateDistanceCenter(child, pendingOffset)
        val childLength = child.width

        if (BuildConfig.DEBUG) {
            Log.d(TAG, "calculateToCenterFraction: distance:$distance,childLength:$childLength")
        }
        return Math.max(-1f, Math.min(1f, distance * 1f / childLength))
    }

    /**
     * @param child
     * *
     * @param pendingOffset child view will scroll by
     * *
     * @return
     */
    private fun calculateDistanceCenter(child: View, pendingOffset: Float): Int {
        val orientationHelper = getOrientationHelper()
        val parentCenter = (orientationHelper.endAfterPadding - orientationHelper.startAfterPadding) / 2 + orientationHelper.startAfterPadding
        return (child.width / 2 - pendingOffset + child.left - parentCenter).toInt()

    }

    /**
     * @param recycler
     * *
     */
    private fun fillWithHorizontal(recycler: RecyclerView.Recycler, dx: Int) {
        val leftEdge = getOrientationHelper().startAfterPadding
        val rightEdge = getOrientationHelper().endAfterPadding
        if (BuildConfig.DEBUG) {
            Log.v(TAG, "fillWithHorizontal() called with: dx = [$dx],leftEdge:$leftEdge,rightEdge:$rightEdge")
        }
        //1.remove and recycle the view that disappear in screen
        var child: View
        if (childCount > 0) {
            if (dx >= 0) {
                //remove and recycle the left off screen view
                var fixIndex = 0
                for (i in 0..childCount - 1) {
                    child = getChildAt(i + fixIndex)
                    if (getDecoratedRight(child) - dx < leftEdge) {
                        removeAndRecycleView(child, recycler)
                        mFirstVisiblePosition++
                        fixIndex--
                        if (BuildConfig.DEBUG) {
                            Log.v(TAG, "fillWithHorizontal:removeAndRecycleView:" + getPosition(child) + " mFirstVisiblePosition change to:" + mFirstVisiblePosition)
                        }
                    } else {
                        break
                    }
                }
            } else { //dx<0
                //remove and recycle the right off screen view
                for (i in childCount - 1 downTo 0) {
                    child = getChildAt(i)
                    if (getDecoratedLeft(child) - dx > rightEdge) {
                        removeAndRecycleView(child, recycler)
                        mLastVisiblePos--
                        if (BuildConfig.DEBUG) {
                            Log.v(TAG, "fillWithHorizontal:removeAndRecycleView:" + getPosition(child) + "mLastVisiblePos change to:" + mLastVisiblePos)
                        }
                    }
                }
            }

        }
        //2.Add or reattach item view to fill screen
        var startPosition = mFirstVisiblePosition
        var startOffset = -1
        var scrapWidth: Int
        var scrapHeight: Int
        var scrapRect: Rect?
        var topOffset: Int
        var scrap: View
        if (dx >= 0) {
            if (childCount != 0) {
                val lastView = getChildAt(childCount - 1)
                startPosition = getPosition(lastView) + 1 //start layout from next position item
                startOffset = getDecoratedRight(lastView)
                if (BuildConfig.DEBUG) {
                    Log.d(TAG, "fillWithHorizontal:to right startPosition:$startPosition,startOffset:$startOffset,rightEdge:$rightEdge")
                }
            }
            var i = startPosition
            while (i < itemCount && startOffset < rightEdge + dx) {
                scrapRect = mState.mItemsFrames.get(i)
                scrap = recycler.getViewForPosition(i)
                addView(scrap)
                if (scrapRect == null) {
                    scrapRect = Rect()
                    mState.mItemsFrames.put(i, scrapRect)
                }
                measureChildWithMargins(scrap, 0, 0)
                scrapWidth = getDecoratedMeasuredWidth(scrap)
                scrapHeight = getDecoratedMeasuredHeight(scrap)
                topOffset = paddingTop
                if (startOffset == -1 && startPosition == 0) {
                    // layout the first position item in center
                    val left = (paddingLeft + (getHorizontalSpace() - scrapWidth) / 2f).toInt()
                    scrapRect.set(left, topOffset, left + scrapWidth, topOffset + scrapHeight)
                } else {
                    scrapRect.set(startOffset, topOffset, startOffset + scrapWidth, topOffset + scrapHeight)
                }
                layoutDecorated(scrap, scrapRect.left, scrapRect.top, scrapRect.right, scrapRect.bottom)
                startOffset = scrapRect.right
                mLastVisiblePos = i
                if (BuildConfig.DEBUG) {
                    Log.d(TAG, "fillWithHorizontal,layout:mLastVisiblePos: " + mLastVisiblePos)
                }
                i++
            }
        } else {
            //dx<0
            if (childCount > 0) {
                val firstView = getChildAt(0)
                startPosition = getPosition(firstView) - 1 //start layout from previous position item
                startOffset = getDecoratedLeft(firstView)
                if (BuildConfig.DEBUG) {
                    Log.d(TAG, "fillWithHorizontal:to left startPosition:$startPosition,startOffset:$startOffset,leftEdge:$leftEdge,child count:$childCount")
                }
            }
            var i = startPosition
            while (i >= 0 && startOffset > leftEdge + dx) {
                scrapRect = mState.mItemsFrames.get(i)
                scrap = recycler.getViewForPosition(i)
                addView(scrap, 0)
                if (scrapRect == null) {
                    scrapRect = Rect()
                    mState.mItemsFrames.put(i, scrapRect)
                }
                measureChildWithMargins(scrap, 0, 0)
                scrapWidth = getDecoratedMeasuredWidth(scrap)
                scrapHeight = getDecoratedMeasuredHeight(scrap)
                topOffset = paddingTop
                scrapRect.set(startOffset - scrapWidth, topOffset, startOffset, topOffset + scrapHeight)
                layoutDecorated(scrap, scrapRect.left, scrapRect.top, scrapRect.right, scrapRect.bottom)
                startOffset = scrapRect.left
                mFirstVisiblePosition = i
                i--
            }
        }
    }

    private fun getHorizontalSpace(): Int {
        return width - paddingRight - paddingLeft
    }

    private fun calculateScrollDirectionForPosition(position: Int): Int {
        if (childCount == 0) {
            return LAYOUT_START
        }
        val firstChildPos = mFirstVisiblePosition
        return if (position < firstChildPos) LAYOUT_START else LAYOUT_END
    }

    override fun computeScrollVectorForPosition(targetPosition: Int): PointF? {
        val direction = calculateScrollDirectionForPosition(targetPosition)
        val outVector = PointF()
        if (direction == 0) {
            return null
        }
        outVector.x = direction.toFloat()
        outVector.y = 0f
        return outVector
    }

    /**
     * @author chensuilun
     */
    internal inner class State {
        /**
         * Record all item view 's last position after last layout
         */
        var mItemsFrames: SparseArray<Rect> = SparseArray()

        /**
         * RecycleView 's current scroll distance since first layout
         */
        var mScrollDelta: Int = 0
    }

    override fun scrollHorizontallyBy(dx: Int, recycler: RecyclerView.Recycler, state: RecyclerView.State): Int {
        // When dx is positive，finger fling from right to left(←)，scrollX+
        if (childCount == 0 || dx == 0) {
            return 0
        }
        var delta = -dx
        val parentCenter = (getOrientationHelper().endAfterPadding - getOrientationHelper().startAfterPadding) / 2 + getOrientationHelper().startAfterPadding
        val child: View
        if (dx > 0) {
            //If we've reached the last item, enforce limits
            if (getPosition(getChildAt(childCount - 1)) == itemCount - 1) {
                child = getChildAt(childCount - 1)
                delta = -Math.max(0, Math.min(dx, (child.right - child.left) / 2 + child.left - parentCenter))
            }
        } else {
            //If we've reached the first item, enforce limits
            if (mFirstVisiblePosition == 0) {
                child = getChildAt(0)
                delta = -Math.min(0, Math.max(dx, (child.right - child.left) / 2 + child.left - parentCenter))
            }
        }
        if (BuildConfig.DEBUG) {
            Log.d(TAG, "scrollHorizontallyBy: dx:$dx,fixed:$delta")
        }
        mState.mScrollDelta = -delta
        fillCover(recycler, -delta)
        offsetChildrenHorizontal(delta)
        return -delta
    }

    fun getOrientationHelper(): OrientationHelper {
        return OrientationHelper.createHorizontalHelper(this)
    }

    /**
     * @author chensuilun
     */
    class LayoutParams : RecyclerView.LayoutParams {

        constructor(c: Context, attrs: AttributeSet) : super(c, attrs)

        constructor(width: Int, height: Int) : super(width, height)

        constructor(source: ViewGroup.MarginLayoutParams) : super(source)

        constructor(source: ViewGroup.LayoutParams) : super(source)

        constructor(source: RecyclerView.LayoutParams) : super(source)
    }

    /**
     * Listen for changes to the selected item

     * @author chensuilun
     */
    interface OnItemSelectedListener {
        /**
         * @param recyclerView The RecyclerView which item view belong to.
         * *
         * @param item The current selected view
         * *
         * @param position The current selected view's position
         */
        fun onItemSelected(recyclerView: RecyclerView, item: View, position: Int)
    }

    private var mOnItemSelectedListener: OnItemSelectedListener? = null

    fun setOnItemSelectedListener(onItemSelectedListener: OnItemSelectedListener) {
        mOnItemSelectedListener = onItemSelectedListener
    }

    /**
     * @param recyclerView
     * *
     * @param selectedPosition
     */
    fun attach(recyclerView: RecyclerView?, selectedPosition: Int) {
        if (recyclerView == null) {
            throw IllegalArgumentException("The attach RecycleView must not null!!")
        }
        mRecyclerView = recyclerView
        mInitialSelectedPosition = Math.max(0, selectedPosition)
        recyclerView.layoutManager = this
        mSnapHelper.attachToRecyclerView(recyclerView)
        recyclerView.addOnScrollListener(mInnerScrollListener)
    }

    internal lateinit var mRecyclerView: RecyclerView

    fun setCallbackInFling(callbackInFling: Boolean) {
        mCallbackInFling = callbackInFling
    }

    /**
     * Inner Listener to listen for changes to the selected item
     */
    private inner class InnerScrollListener : RecyclerView.OnScrollListener() {
        internal var mState: Int = 0
        internal var mCallbackOnIdle: Boolean = false

        override fun onScrolled(recyclerView: RecyclerView?, dx: Int, dy: Int) {
            super.onScrolled(recyclerView, dx, dy)
            val snap = mSnapHelper.findSnapView(recyclerView!!.layoutManager)
            if (snap != null) {
                val selectedPosition = recyclerView.layoutManager.getPosition(snap)
                if (selectedPosition != mCurSelectedPosition) {
                    if (mCurSelectedView != null) {
                        mCurSelectedView!!.isSelected = false
                    }
                    mCurSelectedView = snap
                    mCurSelectedView!!.isSelected = true
                    mCurSelectedPosition = selectedPosition
                    if (!mCallbackInFling && mState != SCROLL_STATE_IDLE) {
                        if (BuildConfig.DEBUG) {
                            Log.v(TAG, "ignore selection change callback when fling ")
                        }
                        mCallbackOnIdle = true
                        return
                    }
                    if (mOnItemSelectedListener != null) {
                        mOnItemSelectedListener!!.onItemSelected(recyclerView, snap, mCurSelectedPosition)
                    }
                }
            }
            if (BuildConfig.DEBUG) {
                Log.v(TAG, "onScrolled: dx:$dx,dy:$dy")
            }
        }

        override fun onScrollStateChanged(recyclerView: RecyclerView?, newState: Int) {
            super.onScrollStateChanged(recyclerView, newState)
            mState = newState
            if (BuildConfig.DEBUG) {
                Log.v(TAG, "onScrollStateChanged: " + newState)
            }
            if (mState == SCROLL_STATE_IDLE) {
                val snap = mSnapHelper.findSnapView(recyclerView!!.layoutManager)
                if (snap != null) {
                    val selectedPosition = recyclerView.layoutManager.getPosition(snap)
                    if (selectedPosition != mCurSelectedPosition) {
                        if (mCurSelectedView != null) {
                            mCurSelectedView!!.isSelected = false
                        }
                        mCurSelectedView = snap
                        mCurSelectedView!!.isSelected = true
                        mCurSelectedPosition = selectedPosition
                        if (mOnItemSelectedListener != null) {
                            mOnItemSelectedListener!!.onItemSelected(recyclerView, snap, mCurSelectedPosition)
                        }
                    } else if (!mCallbackInFling && mOnItemSelectedListener != null && mCallbackOnIdle) {
                        mCallbackOnIdle = false
                        mOnItemSelectedListener!!.onItemSelected(recyclerView, snap, mCurSelectedPosition)
                    }
                } else {
                    Log.e(TAG, "onScrollStateChanged: snap null")
                }
            }
        }
    }

    override fun canScrollHorizontally(): Boolean {
        return true
    }

    override fun smoothScrollToPosition(recyclerView: RecyclerView?, state: RecyclerView.State?, position: Int) {
        val linearSmoothScroller = TileSmoothScroller(recyclerView!!.context)
        linearSmoothScroller.targetPosition = position
        startSmoothScroll(linearSmoothScroller)
    }

    /**
     * Implement to support [TileLayoutManager.smoothScrollToPosition]
     */
    private inner class TileSmoothScroller(context: Context) : LinearSmoothScroller(context) {

        /**
         * Calculates the horizontal scroll amount necessary to make the given view in center of the RecycleView

         * @param view The view which we want to make in center of the RecycleView
         * *
         * @return The horizontal scroll amount necessary to make the view in center of the RecycleView
         */
        fun calculateDxToMakeCentral(view: View): Int {
            val layoutManager = layoutManager
            if (layoutManager == null || !layoutManager.canScrollHorizontally()) {
                return 0
            }
            val params = view.layoutParams as RecyclerView.LayoutParams
            val left = layoutManager.getDecoratedLeft(view) - params.leftMargin
            val right = layoutManager.getDecoratedRight(view) + params.rightMargin
            val start = layoutManager.paddingLeft
            val end = layoutManager.width - layoutManager.paddingRight
            val childCenter = left + ((right - left) / 2.0f).toInt()
            val containerCenter = ((end - start) / 2f).toInt()
            return containerCenter - childCenter
        }

        /**
         * Calculates the vertical scroll amount necessary to make the given view in center of the RecycleView

         * @param view The view which we want to make in center of the RecycleView
         * *
         * @return The vertical scroll amount necessary to make the view in center of the RecycleView
         */
        fun calculateDyToMakeCentral(view: View): Int {
            val layoutManager = layoutManager
            if (layoutManager == null || !layoutManager.canScrollVertically()) {
                return 0
            }
            val params = view.layoutParams as RecyclerView.LayoutParams
            val top = layoutManager.getDecoratedTop(view) - params.topMargin
            val bottom = layoutManager.getDecoratedBottom(view) + params.bottomMargin
            val start = layoutManager.paddingTop
            val end = layoutManager.height - layoutManager.paddingBottom
            val childCenter = top + ((bottom - top) / 2.0f).toInt()
            val containerCenter = ((end - start) / 2f).toInt()
            return containerCenter - childCenter
        }

        override fun onTargetFound(targetView: View, state: RecyclerView.State?, action: RecyclerView.SmoothScroller.Action) {
            val dx = calculateDxToMakeCentral(targetView)
            val dy = calculateDyToMakeCentral(targetView)
            val distance = Math.sqrt((dx * dx + dy * dy).toDouble()).toInt()
            val time = calculateTimeForDeceleration(distance)
            if (time > 0) {
                action.update(-dx, -dy, time, mDecelerateInterpolator)
            }
        }
    }
}