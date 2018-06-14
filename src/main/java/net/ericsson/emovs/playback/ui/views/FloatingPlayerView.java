package net.ericsson.emovs.playback.ui.views;

import android.content.Context;
import android.support.v4.view.MotionEventCompat;
import android.support.v4.view.ViewCompat;
import android.support.v4.widget.ViewDragHelper;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;

import net.ericsson.emovs.playback.PlaybackProperties;
import net.ericsson.emovs.playback.R;
import net.ericsson.emovs.utilities.interfaces.IPlayable;

public class FloatingPlayerView extends ViewGroup {

    private final ViewDragHelper mViewDragHelper;

//    private View mHeaderView;
    private EMPPlayerView mHeaderView;
    private View mDescriptionView;

    private float mInitialMotionX;
    private float mInitialMotionY;

    private int mDragRange;
    private int mTop;
    private float mDragOffset;

    public FloatingPlayerView(Context context) {
        this(context, null);
    }

    public FloatingPlayerView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public FloatingPlayerView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        mViewDragHelper = ViewDragHelper.create(this, 1f, new DragHelperCallback());
    }


    public void play(IPlayable playable) {
//        EMPPlayerView playerView = (EMPPlayerView) findViewById(R.id.empplayer_layout);
//
//        if (playerView != null) {
//            playerView.getPlayer().play(playable, PlaybackProperties.DEFAULT);
//        }

        if (mHeaderView != null) {
            mHeaderView.getPlayer().play(playable, PlaybackProperties.DEFAULT);
        }
    }

    public void stop() {
        if (mHeaderView != null) {
            mHeaderView.getPlayer().stop();
        }
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
//        if (!isPlayerInjected) {
//            createInnerView();
//        }
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
//        EMPPlayerView playerView = (EMPPlayerView) findViewById(R.id.empplayer_layout);
//        if (playerView != null) {
//            playerView.getPlayer().release();
//        }

        if (mHeaderView != null) {
            mHeaderView.getPlayer().release();
        }
    }

    @Override
    protected void onFinishInflate() {
        mHeaderView = findViewById(R.id.v_header);
        mDescriptionView = findViewById(R.id.v_description);

        super.onFinishInflate();
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        mDragRange = getHeight() - mHeaderView.getHeight();

        mHeaderView.layout(0, mTop, r, mTop + mHeaderView.getMeasuredHeight());

        mDescriptionView.layout(0, mTop + mHeaderView.getMeasuredHeight(), r, mTop  + b);
    }

    @Override
    public void computeScroll() {
        if (mViewDragHelper.continueSettling(true)) {
            ViewCompat.postInvalidateOnAnimation(this);
        }
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        final int action = MotionEventCompat.getActionMasked(ev);

        if (( action != MotionEvent.ACTION_DOWN)) {
            mViewDragHelper.cancel();
            return super.onInterceptTouchEvent(ev);
        }

        if (action == MotionEvent.ACTION_CANCEL || action == MotionEvent.ACTION_UP) {
            mViewDragHelper.cancel();
            return false;
        }

        final float x = ev.getX();
        final float y = ev.getY();
        boolean interceptTap = false;

        switch (action) {
            case MotionEvent.ACTION_DOWN: {
                mInitialMotionX = x;
                mInitialMotionY = y;
                interceptTap = mViewDragHelper.isViewUnder(mHeaderView, (int) x, (int) y);
                break;
            }

            case MotionEvent.ACTION_MOVE: {
                final float adx = Math.abs(x - mInitialMotionX);
                final float ady = Math.abs(y - mInitialMotionY);
                final int slop = mViewDragHelper.getTouchSlop();

                /*useless*/
                if (ady > slop && adx > ady) {
                    mViewDragHelper.cancel();
                    return false;
                }
            }
        }

        return mViewDragHelper.shouldInterceptTouchEvent(ev) || interceptTap;
    }

    @Override
    public boolean onTouchEvent(MotionEvent ev) {
        mViewDragHelper.processTouchEvent(ev);

        final int action = ev.getAction();
        final float x = ev.getX();
        final float y = ev.getY();

        boolean isHeaderViewUnder = mViewDragHelper.isViewUnder(mHeaderView, (int) x, (int) y);

        switch (action & MotionEventCompat.ACTION_MASK) {
            case MotionEvent.ACTION_DOWN: {
                mInitialMotionX = x;
                mInitialMotionY = y;

                break;
            }

            case MotionEvent.ACTION_UP: {
                final float dx = x - mInitialMotionX;
                final float dy = y - mInitialMotionY;
                final int slop = mViewDragHelper.getTouchSlop();

                if (dx * dx + dy * dy < slop * slop && isHeaderViewUnder) {
                    if (mDragOffset == 0) {
                        smoothSlideTo(1f);
                    } else {
                        smoothSlideTo(0f);
                    }
                }

                break;
            }
        }

        return isHeaderViewUnder
                && isViewHit(mHeaderView, (int) x, (int) y) || isViewHit(mDescriptionView, (int) x, (int) y);
    }


    private boolean isViewHit(View view, int x, int y) {
        int[] viewLocation = new int[2];
        int[] parentLocation = new int[2];
        view.getLocationOnScreen(viewLocation);
        this.getLocationOnScreen(parentLocation);

        int screenX = parentLocation[0] + x;
        int screenY = parentLocation[1] + y;

        return screenX >= viewLocation[0] && screenX < viewLocation[0] + view.getWidth() &&
                screenY >= viewLocation[1] && screenY < viewLocation[1] + view.getHeight();
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        measureChildren(widthMeasureSpec, heightMeasureSpec);

        int maxWidth = MeasureSpec.getSize(widthMeasureSpec);
        int maxHeight = MeasureSpec.getSize(heightMeasureSpec);

        setMeasuredDimension(
                resolveSizeAndState(maxWidth, widthMeasureSpec, 0),
                resolveSizeAndState(maxHeight, heightMeasureSpec, 0));
    }


    private class DragHelperCallback extends ViewDragHelper.Callback {
        @Override
        public boolean tryCaptureView(View child, int pointerId) {
            return (child == mHeaderView);
        }

        @Override
        public void onViewPositionChanged(View changedView, int left, int top, int dx, int dy) {
            mTop = top;

            mDragOffset = (float) top / mDragRange;

            mHeaderView.setPivotX(mHeaderView.getWidth());
            mHeaderView.setPivotY(mHeaderView.getHeight());
            mHeaderView.setScaleX(1 - mDragOffset / 2);
            mHeaderView.setScaleY(1 - mDragOffset / 2);

            mDescriptionView.setAlpha(1 - mDragOffset);

            requestLayout();
        }

        @Override
        public void onViewReleased(View releasedChild, float xvel, float yvel) {
            int top = getPaddingTop();

            if (yvel > 0 || (yvel == 0 && mDragOffset > 0.5f)) {
                top += mDragRange;
            }

            mViewDragHelper.settleCapturedViewAt(releasedChild.getLeft(), top);

            invalidate();
        }

        @Override
        public int getViewVerticalDragRange(View child) {
            return mDragRange;
        }

        @Override
        public int clampViewPositionVertical(View child, int top, int dy) {
            final int topBound = getPaddingTop();
            final int bottomBound =
                    getHeight() - mHeaderView.getHeight() - mHeaderView.getPaddingBottom();

            return Math.min(Math.max(top, topBound), bottomBound);
        }
    }

    public void maximize() {
        smoothSlideTo(0f);
    }

    public void minimize() {
        smoothSlideTo(1f);
    }

    boolean smoothSlideTo(float slideOffset) {
        final int topBound = getPaddingTop();
        int y = (int) (topBound + slideOffset * mDragRange);

        if (mViewDragHelper.smoothSlideViewTo(mHeaderView, mHeaderView.getLeft(), y)) {
            ViewCompat.postInvalidateOnAnimation(this);
            return true;
        }

        return false;
    }
}
