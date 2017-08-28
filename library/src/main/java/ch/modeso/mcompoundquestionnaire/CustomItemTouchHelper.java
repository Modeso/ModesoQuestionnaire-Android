package ch.modeso.mcompoundquestionnaire;

import android.content.res.Resources;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.os.Build;
import android.support.annotation.Nullable;
import android.support.v4.animation.AnimatorCompatHelper;
import android.support.v4.animation.AnimatorListenerCompat;
import android.support.v4.animation.AnimatorUpdateListenerCompat;
import android.support.v4.animation.ValueAnimatorCompat;
import android.support.v4.view.MotionEventCompat;
import android.support.v4.view.VelocityTrackerCompat;
import android.support.v4.view.ViewCompat;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.support.v7.widget.helper.ItemTouchUIUtil;
import android.util.Log;
import android.view.MotionEvent;
import android.view.VelocityTracker;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.ViewParent;
import android.widget.FrameLayout;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Hazem on 7/24/2017
 */

class CustomItemTouchHelper extends RecyclerView.ItemDecoration
        implements RecyclerView.OnChildAttachStateChangeListener {


    /**
     * Up direction, used for swipe & drag control.
     */
    private static final int UP = 1;

    /**
     * Down direction, used for swipe & drag control.
     */
    private static final int DOWN = 1 << 1;

    /**
     * Left direction, used for swipe & drag control.
     */
    private static final int LEFT = 1 << 2;

    /**
     * Right direction, used for swipe & drag control.
     */
    private static final int RIGHT = 1 << 3;

    // If you change these relative direction values, update Callback#convertToAbsoluteDirection,
    // Callback#convertToRelativeDirection.
    /**
     * Horizontal start direction. Resolved to LEFT or RIGHT depending on RecyclerView's layout
     * direction. Used for swipe & drag control.
     */
    private static final int START = LEFT << 2;

    /**
     * Horizontal end direction. Resolved to LEFT or RIGHT depending on RecyclerView's layout
     * direction. Used for swipe & drag control.
     */
    private static final int END = RIGHT << 2;

    /**
     * CustomItemTouchHelper is in idle state. At this state, either there is no related motion event by
     * the user or latest motion events have not yet triggered a swipe or drag.
     */
    private static final int ACTION_STATE_IDLE = 0;

    /**
     * A View is currently being swiped.
     */
    private static final int ACTION_STATE_SWIPE = 1;

    /**
     * Animation type for views which are swiped successfully.
     */
    private static final int ANIMATION_TYPE_SWIPE_SUCCESS = 1 << 1;

    /**
     * Animation type for views which are not completely swiped thus will animate back to their
     * original position.
     */
    private static final int ANIMATION_TYPE_SWIPE_CANCEL = 1 << 2;

    private static final String TAG = "CustomItemTouchHelper";

    private static final boolean DEBUG = false;

    private static final int ACTIVE_POINTER_ID_NONE = -1;

    private static final int DIRECTION_FLAG_COUNT = 8;

    private static final int ACTION_MODE_IDLE_MASK = (1 << DIRECTION_FLAG_COUNT) - 1;

    private static final int ACTION_MODE_SWIPE_MASK = ACTION_MODE_IDLE_MASK << DIRECTION_FLAG_COUNT;

    private static final int DEFAULT_SWIPE_ANIMATION_DURATION = 500;

    public static final float ANGLE = 45f;

    /**
     * The unit we are using to track velocity
     */
    private static final int PIXELS_PER_SECOND = 1000;

    private static final int RELATIVE_DIR_FLAGS = START | END |
            ((START | END) << DIRECTION_FLAG_COUNT) |
            ((START | END) << (2 * DIRECTION_FLAG_COUNT));

    private static final ItemTouchUIUtil sUICallback;

    private static final int ABS_HORIZONTAL_DIR_FLAGS = LEFT | RIGHT |
            ((LEFT | RIGHT) << DIRECTION_FLAG_COUNT) |
            ((LEFT | RIGHT) << (2 * DIRECTION_FLAG_COUNT));

    static {
        if (Build.VERSION.SDK_INT >= 21) {
            sUICallback = new Lollipop();
        } else {
            sUICallback = new Honeycomb();
        }
    }

    private final DemoAdapter mAdapter;

    /**
     * Views, whose state should be cleared after they are detached from RecyclerView.
     * This is necessary after swipe dismissing an item. We wait until animator finishes its job
     * to clean these views.
     */
    private final List<View> mPendingCleanup = new ArrayList<>();

    /**
     * Re-use array to calculate dx dy for a ViewHolder
     */
    private final float[] mTmpPosition = new float[2];

    /**
     * Currently selected view holder
     */
    private RecyclerView.ViewHolder mSelected = null;

    private static final float SWIPE_THRESHOLD = .5f;

    /**
     * The reference coordinates for the action start. For drag & drop, this is the time long
     * press is completed vs for swipe, this is the initial touch point.
     */
    private float mInitialTouchX;

    private float mInitialTouchY;

    /**
     * Set when CustomItemTouchHelper is assigned to a RecyclerView.
     */
    private float mSwipeEscapeVelocity;

    /**
     * Set when CustomItemTouchHelper is assigned to a RecyclerView.
     */
    private float mMaxSwipeVelocity;

    /**
     * The diff between the last event and initial touch.
     */
    private float mDx;

    private float mDy;

    private int dismissedNo = 0;

    /**
     * The coordinates of the selected view at the time it is selected. We record these values
     * when action starts so that we can consistently position it even if LayoutManager moves the
     * View.
     */
    private float mSelectedStartX;

    private float mSelectedStartY;

    /**
     * The pointer we are tracking.
     */
    private int mActivePointerId = ACTIVE_POINTER_ID_NONE;


    /**
     * Current mode.
     */
    private int mActionState = ACTION_STATE_IDLE;

    private int mSelectedFlags;

    /**
     * When a View is dragged or swiped and needs to go back to where it was, we create a Recover
     * Animation and animate it to its location using this custom Animator, instead of using
     * framework Animators.
     * Using framework animators has the side effect of clashing with ItemAnimator, creating
     * jumpy UIs.
     */
    private List<RecoverAnimation> mRecoverAnimations = new ArrayList<>();

    private int mSlop;

    private RecyclerView mRecyclerView;

    private float mLowerSpace;
    private TileLayoutManager mTileLayoutManager;

    /**
     * Used for detecting fling swipe
     */
    private VelocityTracker mVelocityTracker;

    public int getDismissedNo() {
        return dismissedNo;
    }

    public void setDismissedNo(int dismissedNo) {
        this.dismissedNo = dismissedNo;
    }

    private final RecyclerView.OnItemTouchListener mOnItemTouchListener
            = new RecyclerView.OnItemTouchListener() {
        @Override
        public boolean onInterceptTouchEvent(RecyclerView recyclerView, MotionEvent event) {
            if (DEBUG) {
                Log.d(TAG, "intercept: x:" + event.getX() + ",y:" + event.getY() + ", " + event);
            }
            final int action = MotionEventCompat.getActionMasked(event);
            if (action == MotionEvent.ACTION_DOWN) {
                mActivePointerId = event.getPointerId(0);
                mInitialTouchX = event.getX();
                mInitialTouchY = event.getY();
                obtainVelocityTracker();
                if (mSelected == null) {
                    final RecoverAnimation animation = findAnimation(event);
                    if (animation != null) {
                        mInitialTouchX -= animation.mX;
                        mInitialTouchY -= animation.mY;
                        endRecoverAnimation(animation.mViewHolder, true);
                        if (mPendingCleanup.remove(animation.mViewHolder.itemView)) {
                            clearView(animation.mViewHolder);
                        }
                        select(animation.mViewHolder, animation.mActionState);
                        updateDxDy(event, mSelectedFlags, 0);
                    }
                }
            } else if (action == MotionEvent.ACTION_CANCEL || action == MotionEvent.ACTION_UP) {
                mActivePointerId = ACTIVE_POINTER_ID_NONE;
                select(null, ACTION_STATE_IDLE);
            } else if (mActivePointerId != ACTIVE_POINTER_ID_NONE) {
                // in a non scroll orientation, if distance change is above threshold, we
                // can select the item
                final int index = event.findPointerIndex(mActivePointerId);
                if (DEBUG) {
                    Log.d(TAG, "pointer index " + index);
                }
                if (index >= 0) {
                    checkSelectForSwipe(action, event, index);
                }
            }
            if (mVelocityTracker != null) {
                mVelocityTracker.addMovement(event);
            }
            if (mSelected != null) {
                if (mSelected.getAdapterPosition() != mTileLayoutManager.getCurSelectedPosition()) {
                    return false;
                }
                if (mSelected.itemView instanceof QuestionnaireCardView) {
                    if (((QuestionnaireCardView) mSelected.itemView).getCardStatus() != QuestionnaireCardView.CardStatus.NONE) {
                        return false;
                    }
                }
            }
            return mSelected != null;
        }

        @Override
        public void onTouchEvent(RecyclerView recyclerView, MotionEvent event) {
            if (DEBUG) {
                Log.d(TAG,
                        "on touch: x:" + mInitialTouchX + ",y:" + mInitialTouchY + ", :" + event);
            }
            if (mVelocityTracker != null) {
                mVelocityTracker.addMovement(event);
            }
            if (mActivePointerId == ACTIVE_POINTER_ID_NONE) {
                return;
            }
            final int action = MotionEventCompat.getActionMasked(event);
            final int activePointerIndex = event.findPointerIndex(mActivePointerId);
            if (activePointerIndex >= 0) {
                checkSelectForSwipe(action, event, activePointerIndex);
            }
            RecyclerView.ViewHolder viewHolder = mSelected;
            if (viewHolder == null) {
                return;
            }

            switch (action) {
                case MotionEvent.ACTION_MOVE: {
                    // Find the index of the active pointer and fetch its position
                    if (viewHolder instanceof DemoAdapter.ViewHolder) {
                        if (((DemoAdapter.ViewHolder) viewHolder).getViewDismiss() == DemoAdapter.ViewDismiss.DISMISSED) {
                            return;
                        }
                    }
                    if (activePointerIndex >= 0) {
                        updateDxDy(event, mSelectedFlags, activePointerIndex);
                        mRecyclerView.invalidate();
                    }
                    break;
                }
                case MotionEvent.ACTION_CANCEL:
                    if (mVelocityTracker != null) {
                        mVelocityTracker.clear();
                    }
                    // fall through
                case MotionEvent.ACTION_UP:
                    select(null, ACTION_STATE_IDLE);
                    mActivePointerId = ACTIVE_POINTER_ID_NONE;
                    break;
                case MotionEvent.ACTION_POINTER_UP: {
                    final int pointerIndex = MotionEventCompat.getActionIndex(event);
                    final int pointerId = event.getPointerId(pointerIndex);
                    if (pointerId == mActivePointerId) {
                        // This was our active pointer going up. Choose a new
                        // active pointer and adjust accordingly.
                        final int newPointerIndex = pointerIndex == 0 ? 1 : 0;
                        mActivePointerId = event.getPointerId(newPointerIndex);
                        updateDxDy(event, mSelectedFlags, pointerIndex);
                    }
                    break;
                }
            }
        }

        @Override
        public void onRequestDisallowInterceptTouchEvent(boolean disallowIntercept) {
            if (!disallowIntercept) {
                return;
            }
            select(null, ACTION_STATE_IDLE);
        }
    };

    /**
     * Creates an CustomItemTouchHelper that will work with the given Callback.
     * <p>
     * You can attach CustomItemTouchHelper to a RecyclerView via
     * {@link #attachToRecyclerView(RecyclerView)}. Upon attaching, it will add an item decoration,
     * an onItemTouchListener and a Child attach / detach listener to the RecyclerView.
     */
    CustomItemTouchHelper(DemoAdapter adapter, float lowerSpace, TileLayoutManager tileLayoutManager) {
        mLowerSpace = lowerSpace;
        mAdapter = adapter;
        mTileLayoutManager = tileLayoutManager;
    }

    private static boolean hitTest(View child, float x, float y, float left, float top) {
        return x >= left &&
                x <= left + child.getWidth() &&
                y >= top &&
                y <= top + child.getHeight();
    }

    /**
     * Attaches the CustomItemTouchHelper to the provided RecyclerView. If TouchHelper is already
     * attached to a RecyclerView, it will first detach from the previous one. You can call this
     * method with {@code null} to detach it from the current RecyclerView.
     *
     * @param recyclerView The RecyclerView instance to which you want to add this helper or
     *                     {@code null} if you want to remove CustomItemTouchHelper from the current
     *                     RecyclerView.
     */
    void attachToRecyclerView(@Nullable RecyclerView recyclerView) {
        if (mRecyclerView == recyclerView) {
            return; // nothing to do
        }
        if (mRecyclerView != null) {
            destroyCallbacks();
        }
        mRecyclerView = recyclerView;
        if (mRecyclerView != null) {
            final Resources resources = recyclerView.getResources();
            mSwipeEscapeVelocity = resources
                    .getDimension(R.dimen.item_touch_helper_swipe_escape_velocity);
            mMaxSwipeVelocity = resources
                    .getDimension(R.dimen.item_touch_helper_swipe_escape_max_velocity);
            setupCallbacks();
        }
    }

    private void setupCallbacks() {
        ViewConfiguration vc = ViewConfiguration.get(mRecyclerView.getContext());
        mSlop = vc.getScaledTouchSlop();
        mRecyclerView.addItemDecoration(this);
        mRecyclerView.addOnItemTouchListener(mOnItemTouchListener);
        mRecyclerView.addOnChildAttachStateChangeListener(this);
    }

    private void destroyCallbacks() {
        mRecyclerView.removeItemDecoration(this);
        mRecyclerView.removeOnItemTouchListener(mOnItemTouchListener);
        mRecyclerView.removeOnChildAttachStateChangeListener(this);
        // clean all attached
        final int recoverAnimSize = mRecoverAnimations.size();
        for (int i = recoverAnimSize - 1; i >= 0; i--) {
            final RecoverAnimation recoverAnimation = mRecoverAnimations.get(0);
            clearView(recoverAnimation.mViewHolder);
        }
        mRecoverAnimations.clear();
        releaseVelocityTracker();
    }

    private void getSelectedDxDy(float[] outPosition) {
        if ((mSelectedFlags & (LEFT | RIGHT)) != 0) {
            outPosition[0] = mSelectedStartX + mDx - mSelected.itemView.getLeft();
        } else {
            outPosition[0] = ViewCompat.getTranslationX(mSelected.itemView);
        }
        if ((mSelectedFlags & (UP | DOWN)) != 0) {
            outPosition[1] = mSelectedStartY + mDy - mSelected.itemView.getTop();
        } else {
            outPosition[1] = ViewCompat.getTranslationY(mSelected.itemView);
        }
    }

    @Override
    public void onDrawOver(Canvas c, RecyclerView parent, RecyclerView.State state) {
        float dx = 0, dy = 0;
        if (mSelected != null) {
            getSelectedDxDy(mTmpPosition);
            dx = mTmpPosition[0];
            dy = mTmpPosition[1];
        }

        final int recoverAnimSize = mRecoverAnimations.size();
        for (int i = 0; i < recoverAnimSize; i++) {
            final RecoverAnimation anim = mRecoverAnimations.get(i);
            final int count = c.save();
            onChildDrawOver(c, parent, anim.mViewHolder, anim.mX, anim.mY, anim.mActionState,
                    false);
            c.restoreToCount(count);
        }
        if (mSelected != null) {
            final int count = c.save();
            onChildDrawOver(c, parent, mSelected, dx, dy, mActionState, true);
            c.restoreToCount(count);
        }
        boolean hasRunningAnimation = false;
        for (int i = recoverAnimSize - 1; i >= 0; i--) {
            final RecoverAnimation anim = mRecoverAnimations.get(i);
            if (anim.mEnded && !anim.mIsPendingCleanup) {
                mRecoverAnimations.remove(i);
            } else if (!anim.mEnded) {
                hasRunningAnimation = true;
            }
        }
        if (hasRunningAnimation) {
            parent.invalidate();
        }
    }

    @Override
    public void onDraw(Canvas c, RecyclerView parent, RecyclerView.State state) {
        // we don't know if RV changed something so we should invalidate this index.
        float dy = 0;
        if (mSelected != null) {
            getSelectedDxDy(mTmpPosition);
            dy = mTmpPosition[1];
        }

        final int recoverAnimSize = mRecoverAnimations.size();
        for (int i = 0; i < recoverAnimSize; i++) {
            final RecoverAnimation anim = mRecoverAnimations.get(i);
            anim.update();
            final int count = c.save();
            onChildDraw(parent, anim.mViewHolder, anim.mY);
            c.restoreToCount(count);
        }
        if (mSelected != null) {
            final int count = c.save();
            onChildDraw(parent, mSelected, dy);
            c.restoreToCount(count);
        }
    }

    /**
     * Starts dragging or swiping the given View. Call with null if you want to clear it.
     *
     * @param selected    The ViewHolder to drag or swipe. Can be null if you want to cancel the
     *                    current action
     * @param actionState The type of action
     */
    private void select(RecyclerView.ViewHolder selected, int actionState) {
        if (selected == mSelected && actionState == mActionState) {
            return;
        }
        final int prevActionState = mActionState;
        // prevent duplicate animations
        endRecoverAnimation(selected, true);
        mActionState = actionState;

        int actionStateMask = (1 << (DIRECTION_FLAG_COUNT + DIRECTION_FLAG_COUNT * actionState)) - 1;
        boolean preventLayout = false;

        if (mSelected != null) {
            final RecyclerView.ViewHolder prevSelected = mSelected;
            if (prevSelected.itemView.getParent() != null) {
                final int swipeDir = swipeIfNecessary();
                releaseVelocityTracker();
                // find where we should animate to
                final float targetTranslateX, targetTranslateY;
                int animationType;
                switch (swipeDir) {
                    case LEFT:
                    case RIGHT:
                    case START:
                    case END:
                        targetTranslateY = 0;
                        targetTranslateX = Math.signum(mDx) * mRecyclerView.getWidth();
                        break;
                    case UP:
                    case DOWN:
                        targetTranslateX = 0;
                        targetTranslateY = Math.signum(mDy) * (mRecyclerView.getHeight() - mLowerSpace);
                        break;
                    default:
                        targetTranslateX = 0;
                        targetTranslateY = 0;
                }
                if (swipeDir > 0) {
                    animationType = ANIMATION_TYPE_SWIPE_SUCCESS;
                } else {
                    animationType = ANIMATION_TYPE_SWIPE_CANCEL;
                }
                getSelectedDxDy(mTmpPosition);
                final float currentTranslateX = mTmpPosition[0];
                final float currentTranslateY = mTmpPosition[1];
                final RecoverAnimation rv = new RecoverAnimation(prevSelected, animationType,
                        prevActionState, currentTranslateX, currentTranslateY,
                        targetTranslateX, targetTranslateY) {
                    @Override
                    public void onAnimationEnd(ValueAnimatorCompat animation) {
                        super.onAnimationEnd(animation);
                        if (this.mOverridden) {
                            return;
                        }
                        if (swipeDir <= 0) {
                            // this is a drag or failed swipe. recover immediately
                            clearView(prevSelected);
                            // full cleanup will happen on onDrawOver
                        } else {
                            // wait until remove animation is complete.
                            mPendingCleanup.add(prevSelected.itemView);
                            mIsPendingCleanup = true;
                            if (swipeDir > 0) {
                                // Animation might be ended by other animators during a layout.
                                // We defer callback to avoid editing adapter during a layout.
                                postDispatchSwipe(this);
                            }
                        }
                        // removed from the list after it is drawn for the last time
                    }
                };
                final long duration = getAnimationDuration(mRecyclerView);
                rv.setDuration(duration);
                mRecoverAnimations.add(rv);
                rv.start();
                preventLayout = true;
            } else {
                clearView(prevSelected);
            }
            mSelected = null;
        }
        if (selected != null) {
            mSelectedFlags =
                    (getAbsoluteMovementFlags(mRecyclerView) & actionStateMask)
                            >> (mActionState * DIRECTION_FLAG_COUNT);
            mSelectedStartX = selected.itemView.getLeft();
            mSelectedStartY = selected.itemView.getTop();
            mSelected = selected;

        }
        final ViewParent rvParent = mRecyclerView.getParent();
        if (rvParent != null) {
            rvParent.requestDisallowInterceptTouchEvent(mSelected != null);
        }
        if (!preventLayout) {
            mRecyclerView.getLayoutManager().requestSimpleAnimationsInNextLayout();
        }
        onSelectedChanged(mSelected, mActionState);
        mRecyclerView.invalidate();
    }

    private void postDispatchSwipe(final RecoverAnimation anim) {
        // wait until animations are complete.
        mRecyclerView.post(new Runnable() {
            @Override
            public void run() {
                if (mRecyclerView != null && mRecyclerView.isAttachedToWindow() &&
                        !anim.mOverridden &&
                        anim.mViewHolder.getAdapterPosition() != RecyclerView.NO_POSITION) {
                    final RecyclerView.ItemAnimator animator = mRecyclerView.getItemAnimator();
                    // if animator is running or we have other active recover animations, we try
                    // not to call onSwiped because DefaultItemAnimator is not good at merging
                    // animations. Instead, we wait and batch.
                    if ((animator == null || !animator.isRunning(null))
                            && !hasRunningRecoverAnim()) {
                        onSwiped(anim.mViewHolder);
                    } else {
                        mRecyclerView.post(this);
                    }
                }
            }
        });
    }

    private boolean hasRunningRecoverAnim() {
        final int size = mRecoverAnimations.size();
        for (int i = 0; i < size; i++) {
            if (!mRecoverAnimations.get(i).mEnded) {
                return true;
            }
        }
        return false;
    }

    @Override
    public void onChildViewAttachedToWindow(View view) {
    }

    @Override
    public void onChildViewDetachedFromWindow(View view) {
        final RecyclerView.ViewHolder holder = mRecyclerView.getChildViewHolder(view);
        if (holder == null) {
            return;
        }
        if (mSelected != null && holder == mSelected) {
            select(null, ACTION_STATE_IDLE);
        } else {
            endRecoverAnimation(holder, false); // this may push it into pending cleanup list.
            if (mPendingCleanup.remove(holder.itemView)) {
                clearView(holder);
            }
        }
    }

    /**
     * Returns the animation type or 0 if cannot be found.
     */
    private int endRecoverAnimation(RecyclerView.ViewHolder viewHolder, boolean override) {
        final int recoverAnimSize = mRecoverAnimations.size();
        for (int i = recoverAnimSize - 1; i >= 0; i--) {
            final RecoverAnimation anim = mRecoverAnimations.get(i);
            if (anim.mViewHolder == viewHolder) {
                anim.mOverridden |= override;
                if (!anim.mEnded) {
                    anim.cancel();
                }
                mRecoverAnimations.remove(i);
                return anim.mAnimationType;
            }
        }
        return 0;
    }

    @Override
    public void getItemOffsets(Rect outRect, View view, RecyclerView parent,
                               RecyclerView.State state) {
        outRect.setEmpty();
    }

    private void obtainVelocityTracker() {
        if (mVelocityTracker != null) {
            mVelocityTracker.recycle();
        }
        mVelocityTracker = VelocityTracker.obtain();
    }

    private void releaseVelocityTracker() {
        if (mVelocityTracker != null) {
            mVelocityTracker.recycle();
            mVelocityTracker = null;
        }
    }

    private RecyclerView.ViewHolder findSwipedView(MotionEvent motionEvent) {
        final RecyclerView.LayoutManager lm = mRecyclerView.getLayoutManager();
        if (mActivePointerId == ACTIVE_POINTER_ID_NONE) {
            return null;
        }
        final int pointerIndex = motionEvent.findPointerIndex(mActivePointerId);
        final float dx = motionEvent.getX(pointerIndex) - mInitialTouchX;
        final float dy = motionEvent.getY(pointerIndex) - mInitialTouchY;
        final float absDx = Math.abs(dx);
        final float absDy = Math.abs(dy);

        if (absDx < mSlop && absDy < mSlop) {
            return null;
        }
        if (absDx > absDy && lm.canScrollHorizontally()) {
            return null;
        } else if (absDy > absDx && lm.canScrollVertically()) {
            return null;
        }
        View child = findChildView(motionEvent);
        if (child == null) {
            return null;
        }
        if (child instanceof QuestionnaireCardView) {
            if (((QuestionnaireCardView) child).getCardStatus() == QuestionnaireCardView.CardStatus.NOT_APPLICABLE) {
                return null;
            }
        }
        if (child.getParent() instanceof FrameLayout) {
            return null;
        }
        return mRecyclerView.getChildViewHolder(child);
    }

    /**
     * Checks whether we should select a View for swiping.
     */
    private boolean checkSelectForSwipe(int action, MotionEvent motionEvent, int pointerIndex) {
        if (mSelected != null || action != MotionEvent.ACTION_MOVE) {
            return false;
        }
        if (mRecyclerView.getScrollState() == RecyclerView.SCROLL_STATE_DRAGGING) {
            return false;
        }
        final RecyclerView.ViewHolder vh = findSwipedView(motionEvent);
        if (vh == null) {
            return false;
        }
        final int movementFlags = getAbsoluteMovementFlags(mRecyclerView);

        final int swipeFlags = (movementFlags & ACTION_MODE_SWIPE_MASK)
                >> (DIRECTION_FLAG_COUNT * ACTION_STATE_SWIPE);

        if (swipeFlags == 0) {
            return false;
        }

        // mDx and mDy are only set in allowed directions. We use custom x/y here instead of
        // updateDxDy to avoid swiping if user moves more in the other direction
        final float x = motionEvent.getX(pointerIndex);
        final float y = motionEvent.getY(pointerIndex);

        // Calculate the distance moved
        final float dx = x - mInitialTouchX;
        final float dy = y - mInitialTouchY;
        // swipe target is chose w/o applying flags so it does not really check if swiping in that
        // direction is allowed. This why here, we use mDx mDy to check slope value again.
        final float absDx = Math.abs(dx);
        final float absDy = Math.abs(dy);

        if (absDx < mSlop && absDy < mSlop) {
            return false;
        }
        if (absDx > absDy) {
            if (dx < 0 && (swipeFlags & LEFT) == 0) {
                return false;
            }
            if (dx > 0 && (swipeFlags & RIGHT) == 0) {
                return false;
            }
        } else {
            if (dy < 0 && (swipeFlags & UP) == 0) {
                return false;
            }
            if (dy > 0 && (swipeFlags & DOWN) == 0) {
                return false;
            }
        }
        mDx = mDy = 0f;
        mActivePointerId = motionEvent.getPointerId(0);
        select(vh, ACTION_STATE_SWIPE);
        return true;
    }

    private View findChildView(MotionEvent event) {
        // first check elevated views, if none, then call RV
        final float x = event.getX();
        final float y = event.getY();
        if (mSelected != null) {
            final View selectedView = mSelected.itemView;
            if (hitTest(selectedView, x, y, mSelectedStartX + mDx, mSelectedStartY + mDy)) {
                return selectedView;
            }
        }
        for (int i = mRecoverAnimations.size() - 1; i >= 0; i--) {
            final RecoverAnimation anim = mRecoverAnimations.get(i);
            final View view = anim.mViewHolder.itemView;
            if (hitTest(view, x, y, anim.mX, anim.mY)) {
                return view;
            }
        }
        return mRecyclerView.findChildViewUnder(x, y);
    }

    private RecoverAnimation findAnimation(MotionEvent event) {
        if (mRecoverAnimations.isEmpty()) {
            return null;
        }
        View target = findChildView(event);
        for (int i = mRecoverAnimations.size() - 1; i >= 0; i--) {
            final RecoverAnimation anim = mRecoverAnimations.get(i);
            if (anim.mViewHolder.itemView == target) {
                return anim;
            }
        }
        return null;
    }

    private void updateDxDy(MotionEvent ev, int directionFlags, int pointerIndex) {
        final float x = ev.getX(pointerIndex);
        final float y = ev.getY(pointerIndex);

        // Calculate the distance moved
        mDx = x - mInitialTouchX;
        mDy = y - mInitialTouchY;
        if ((directionFlags & LEFT) == 0) {
            mDx = Math.max(0, mDx);
        }
        if ((directionFlags & RIGHT) == 0) {
            mDx = Math.min(0, mDx);
        }
        if ((directionFlags & UP) == 0) {
            mDy = Math.max(0, mDy);
        }
        if ((directionFlags & DOWN) == 0) {
            mDy = Math.min(0, mDy);
        }
    }

    private int swipeIfNecessary() {
        final int originalMovementFlags = makeMovementFlags(0, DOWN);
        final int absoluteMovementFlags = convertToAbsoluteDirection(
                originalMovementFlags,
                ViewCompat.getLayoutDirection(mRecyclerView));
        final int flags = (absoluteMovementFlags
                & ACTION_MODE_SWIPE_MASK) >> (ACTION_STATE_SWIPE * DIRECTION_FLAG_COUNT);
        if (flags == 0) {
            return 0;
        }
        final int originalFlags = (originalMovementFlags
                & ACTION_MODE_SWIPE_MASK) >> (ACTION_STATE_SWIPE * DIRECTION_FLAG_COUNT);
        int swipeDir;
        if (Math.abs(mDx) > Math.abs(mDy)) {
            if ((swipeDir = checkHorizontalSwipe(flags)) > 0) {
                // if swipe dir is not in original flags, it should be the relative direction
                if ((originalFlags & swipeDir) == 0) {
                    // convert to relative
                    return convertToRelativeDirection(swipeDir,
                            ViewCompat.getLayoutDirection(mRecyclerView));
                }
                return swipeDir;
            }
            if ((swipeDir = checkVerticalSwipe(flags)) > 0) {
                return swipeDir;
            }
        } else {
            if ((swipeDir = checkVerticalSwipe(flags)) > 0) {
                return swipeDir;
            }
            if ((swipeDir = checkHorizontalSwipe(flags)) > 0) {
                // if swipe dir is not in original flags, it should be the relative direction
                if ((originalFlags & swipeDir) == 0) {
                    // convert to relative
                    return convertToRelativeDirection(swipeDir,
                            ViewCompat.getLayoutDirection(mRecyclerView));
                }
                return swipeDir;
            }
        }
        return 0;
    }

    private int checkHorizontalSwipe(int flags) {
        if ((flags & (LEFT | RIGHT)) != 0) {
            final int dirFlag = mDx > 0 ? RIGHT : LEFT;
            if (mVelocityTracker != null && mActivePointerId > -1) {
                mVelocityTracker.computeCurrentVelocity(PIXELS_PER_SECOND, mMaxSwipeVelocity);
                final float xVelocity = VelocityTrackerCompat
                        .getXVelocity(mVelocityTracker, mActivePointerId);
                final float yVelocity = VelocityTrackerCompat
                        .getYVelocity(mVelocityTracker, mActivePointerId);
                final int velDirFlag = xVelocity > 0f ? RIGHT : LEFT;
                final float absXVelocity = Math.abs(xVelocity);
                if ((velDirFlag & flags) != 0 && dirFlag == velDirFlag &&
                        absXVelocity >= mSwipeEscapeVelocity &&
                        absXVelocity > Math.abs(yVelocity)) {
                    return velDirFlag;
                }
            }

            final float threshold = mRecyclerView.getWidth() * SWIPE_THRESHOLD;

            if ((flags & dirFlag) != 0 && Math.abs(mDx) > threshold) {
                return dirFlag;
            }
        }
        return 0;
    }

    private int checkVerticalSwipe(int flags) {
        if ((flags & (UP | DOWN)) != 0) {
            final int dirFlag = mDy > 0 ? DOWN : UP;
            if (mVelocityTracker != null && mActivePointerId > -1) {
                mVelocityTracker.computeCurrentVelocity(PIXELS_PER_SECOND, mMaxSwipeVelocity);
                final float xVelocity = VelocityTrackerCompat
                        .getXVelocity(mVelocityTracker, mActivePointerId);
                final float yVelocity = VelocityTrackerCompat
                        .getYVelocity(mVelocityTracker, mActivePointerId);
                final int velDirFlag = yVelocity > 0f ? DOWN : UP;
                final float absYVelocity = Math.abs(yVelocity);
                if ((velDirFlag & flags) != 0 && velDirFlag == dirFlag &&
                        absYVelocity >= mSwipeEscapeVelocity &&
                        absYVelocity > Math.abs(xVelocity)) {
                    return velDirFlag;
                }
            }

            final float threshold = mRecyclerView.getHeight() * SWIPE_THRESHOLD;
            if ((flags & dirFlag) != 0 && Math.abs(mDy) > threshold) {
                return dirFlag;
            }
        }
        return 0;
    }

    private long getAnimationDuration(RecyclerView recyclerView) {
        final RecyclerView.ItemAnimator itemAnimator = recyclerView.getItemAnimator();
        if (itemAnimator == null) {
            return DEFAULT_SWIPE_ANIMATION_DURATION;
        } else {
            return itemAnimator.getRemoveDuration();
        }
    }

    private static int makeMovementFlags(int dragFlags, int swipeFlags) {
        return makeFlag(ACTION_STATE_IDLE, swipeFlags | dragFlags) |
                makeFlag(ACTION_STATE_SWIPE, swipeFlags);
    }

    private static int makeFlag(int actionState, int directions) {
        return directions << (actionState * DIRECTION_FLAG_COUNT);
    }

    private void onSelectedChanged(RecyclerView.ViewHolder viewHolder, int actionState) {
        if (actionState != ItemTouchHelper.ACTION_STATE_IDLE) {
            if (viewHolder instanceof DemoAdapter.ViewHolder) {
                // Let the view holder know that this item is being moved or dragged
                DemoAdapter.ViewHolder itemViewHolder = (DemoAdapter.ViewHolder) viewHolder;
                itemViewHolder.onItemSelected();
            }
        }
        if (viewHolder != null) {
            sUICallback.onSelected(viewHolder.itemView);
        }
    }

    private void onChildDraw(RecyclerView recyclerView,
                             RecyclerView.ViewHolder viewHolder, float dY) {
        int max = recyclerView.getHeight() - recyclerView.getPaddingTop();
        final float alpha = ANGLE * dY / max;
        viewHolder.itemView.setRotation(alpha);
        viewHolder.itemView.setTranslationY(dY);
        if (viewHolder.itemView instanceof QuestionnaireCardView && !((QuestionnaireCardView) viewHolder.itemView).getMovingHorizontal()) {
            if (mActivePointerId != ACTIVE_POINTER_ID_NONE &&
                    ((QuestionnaireCardView) mSelected.itemView).getCardStatus() == QuestionnaireCardView.CardStatus.NONE) {
                ((QuestionnaireCardView) viewHolder.itemView).onCardMovement(dY / max, false);
                ((QuestionnaireCardView) viewHolder.itemView).setCardMoving(true);

            } else {
                ((QuestionnaireCardView) viewHolder.itemView).setCardMoving(false);
            }
        }
    }

    private void onChildDrawOver(Canvas c, RecyclerView recyclerView,
                                 RecyclerView.ViewHolder viewHolder,
                                 float dX, float dY, int actionState, boolean isCurrentlyActive) {
        sUICallback.onDrawOver(c, recyclerView, viewHolder.itemView, dX, dY, actionState,
                isCurrentlyActive);
    }

    private static int convertToRelativeDirection(int flags, int layoutDirection) {
        int masked = flags & ABS_HORIZONTAL_DIR_FLAGS;
        if (masked == 0) {
            return flags;// does not have any abs flags, good.
        }
        flags &= ~masked; //remove left / right.
        if (layoutDirection == ViewCompat.LAYOUT_DIRECTION_LTR) {
            // no change. just OR with 2 bits shifted mask and return
            flags |= masked << 2; // START is 2 bits after LEFT, END is 2 bits after RIGHT.
            return flags;
        } else {
            // add RIGHT flag as START
            flags |= ((masked << 1) & ~ABS_HORIZONTAL_DIR_FLAGS);
            // first clean RIGHT bit then add LEFT flag as END
            flags |= ((masked << 1) & ABS_HORIZONTAL_DIR_FLAGS) << 2;
        }
        return flags;
    }

    private int convertToAbsoluteDirection(int flags, int layoutDirection) {
        int masked = flags & RELATIVE_DIR_FLAGS;
        if (masked == 0) {
            return flags;// does not have any relative flags, good.
        }
        flags &= ~masked; //remove start / end
        if (layoutDirection == ViewCompat.LAYOUT_DIRECTION_LTR) {
            // no change. just OR with 2 bits shifted mask and return
            flags |= masked >> 2; // START is 2 bits after LEFT, END is 2 bits after RIGHT.
            return flags;
        } else {
            // add START flag as RIGHT
            flags |= ((masked >> 1) & ~RELATIVE_DIR_FLAGS);
            // first clean start bit then add END flag as LEFT
            flags |= ((masked >> 1) & RELATIVE_DIR_FLAGS) >> 2;
        }
        return flags;
    }

    private int getAbsoluteMovementFlags(RecyclerView recyclerView) {
        final int flags = makeMovementFlags(0, DOWN);
        return convertToAbsoluteDirection(flags, ViewCompat.getLayoutDirection(recyclerView));
    }

    private void clearView(RecyclerView.ViewHolder viewHolder) {
        sUICallback.clearView(viewHolder.itemView);

        if (viewHolder instanceof DemoAdapter.ViewHolder) {
            // Tell the view holder it's time to restore the idle state
            DemoAdapter.ViewHolder itemViewHolder = (DemoAdapter.ViewHolder) viewHolder;
            itemViewHolder.onItemClear();
            if (viewHolder.itemView.getParent() != null
                    && viewHolder.itemView.getParent().getParent() != null
                    && viewHolder.itemView.getParent().getParent().getParent() != null
                    && viewHolder.itemView.getParent().getParent().getParent() instanceof MCompoundQuestionnaire) {
                ((MCompoundQuestionnaire) viewHolder.itemView.getParent().getParent().getParent()).setCardMovingHorizontal(false);
            }
        }
    }

    private void onSwiped(final RecyclerView.ViewHolder viewHolder) {
        // Notify the adapter of the dismissal
        mRecoverAnimations.clear();
        if (viewHolder instanceof DemoAdapter.ViewHolder) {
            ((DemoAdapter.ViewHolder) viewHolder).setViewDismiss(DemoAdapter.ViewDismiss.DISMISSED);
        }
        float widthRightPart = mLowerSpace + (float) Math.sqrt(Math.pow((viewHolder.itemView.getMeasuredWidth() - Math.sqrt(2 * Math.pow(mLowerSpace, 2))), 2) / 2);
        float widthLeftPart = mLowerSpace + (float) Math.sqrt(Math.pow((viewHolder.itemView.getMeasuredHeight() - Math.sqrt(2 * Math.pow(mLowerSpace, 2))), 2) / 2);
        final float def = (widthRightPart + widthLeftPart - viewHolder.itemView.getMeasuredWidth()) / 2;
        final float deltaX = viewHolder.itemView.getX() + viewHolder.itemView.getMeasuredWidth() - (widthRightPart - def) - (mLowerSpace * (dismissedNo + 1));
        viewHolder.setIsRecyclable(true);
        if (viewHolder instanceof DemoAdapter.ViewHolder) {
            if (viewHolder.itemView.getParent() != null
                    && viewHolder.itemView.getParent().getParent() != null
                    && viewHolder.itemView.getParent().getParent().getParent() != null
                    && viewHolder.itemView.getParent().getParent().getParent() instanceof MCompoundQuestionnaire) {
                ((MCompoundQuestionnaire) viewHolder.itemView.getParent().getParent().getParent()).setCardMovingHorizontal(true);
            }
            mAdapter.onItemDismiss((DemoAdapter.ViewHolder) viewHolder, deltaX);
        }
        dismissedNo++;
    }

    private class RecoverAnimation implements AnimatorListenerCompat {

        final float mStartDx;

        final float mStartDy;

        final float mTargetX;

        final float mTargetY;

        final RecyclerView.ViewHolder mViewHolder;

        final int mActionState;

        private final ValueAnimatorCompat mValueAnimator;

        final int mAnimationType;

        boolean mIsPendingCleanup;

        float mX;

        float mY;

        // if user starts touching a recovering view, we put it into interaction mode again,
        // instantly.
        boolean mOverridden = false;

        boolean mEnded = false;

        private float mFraction;

        RecoverAnimation(RecyclerView.ViewHolder viewHolder, int animationType,
                         int actionState, float startDx, float startDy, float targetX, float targetY) {
            mActionState = actionState;
            mAnimationType = animationType;
            mViewHolder = viewHolder;
            mStartDx = startDx;
            mStartDy = startDy;
            mTargetX = targetX;
            mTargetY = targetY;
            mValueAnimator = AnimatorCompatHelper.emptyValueAnimator();
            mValueAnimator.addUpdateListener(
                    new AnimatorUpdateListenerCompat() {
                        @Override
                        public void onAnimationUpdate(ValueAnimatorCompat animation) {
                            setFraction(animation.getAnimatedFraction());
                        }
                    });
            mValueAnimator.setTarget(viewHolder.itemView);
            mValueAnimator.addListener(this);
            setFraction(0f);
        }

        void setDuration(long duration) {
            mValueAnimator.setDuration(duration);
        }

        void start() {
            mViewHolder.setIsRecyclable(false);
            mValueAnimator.start();
        }

        void cancel() {
            mValueAnimator.cancel();
        }

        void setFraction(float fraction) {
            mFraction = fraction;
        }

        /**
         * We run updates on onDraw method but use the fraction from animator callback.
         * This way, we can sync translate x/y values w/ the animators to avoid one-off frames.
         */
        void update() {
            if (mStartDx == mTargetX) {
                mX = ViewCompat.getTranslationX(mViewHolder.itemView);
            } else {
                mX = mStartDx + mFraction * (mTargetX - mStartDx);
            }
            if (mStartDy == mTargetY) {
                mY = ViewCompat.getTranslationY(mViewHolder.itemView);
            } else {
                mY = mStartDy + mFraction * (mTargetY - mStartDy);
            }
        }

        @Override
        public void onAnimationStart(ValueAnimatorCompat animation) {

        }

        @Override
        public void onAnimationEnd(ValueAnimatorCompat animation) {
            mEnded = true;
        }

        @Override
        public void onAnimationCancel(ValueAnimatorCompat animation) {
            setFraction(1f); //make sure we recover the view's state.
        }

        @Override
        public void onAnimationRepeat(ValueAnimatorCompat animation) {

        }
    }
}
