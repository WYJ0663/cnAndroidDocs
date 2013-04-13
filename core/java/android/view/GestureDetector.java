/*
 * Copyright (C) 2008 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package android.view;

import android.content.Context;
import android.os.Build;
import android.os.Handler;
import android.os.Message;

/**
 * 根据 {@link MotionEvent}事件检测各种手势. {@link OnGestureListener}
 * 回调函数用于通知用户发生的手势动作.该类仅处理 {@link MotionEvent}
 * 事件中的触摸事件（不处理轨迹球事件）.
 *
 * 使用该类的方法如下：
 * <ul>
 *  <li>为你的{@link View 视图}创建{@code GestureDetector}的实例；
 *  <li>保证在{@link View#onTouchEvent(MotionEvent)}方法中调用了该类的
 *      {@link #onTouchEvent(MotionEvent)}方法.当事件发生时调用该回调函数.
 * </ul>
 */
public class GestureDetector {
    /**
     * 用于通知手势发生事件的监听器. 如果你想要监听所有的手势，可以实现该接口.
     * 如果只想监听一部分手势，扩展{@link SimpleOnGestureListener}
     * 类可能更简单一些.
     */
    public interface OnGestureListener {

        /**
         * 当轻触手势按下 {@link MotionEvent} 时发生的事件.
         * 每当按下时，立即触发该事件.优先于其它事件.
         *
         * @param e 按下动作时间.
         */
        boolean onDown(MotionEvent e);

        /**
         * 用户执行按下 {@link MotionEvent} 但没有执行移动或抬起动作时的事件.
         * 该事件一般用于为用户提供视觉反馈，比如高亮显示操作的元素，
         * 以通知用户其动作已经被识别.
         *
         * @param e 按下动作事件.
         */
        void onShowPress(MotionEvent e);

        /**
         * 当轻触手势抬起 {@link MotionEvent} 时发生的事件.
         *
         * @param e 结束轻触手势的抬起动作事件.
         * @return 若已处理，返回真；否则返回假.
         */
        boolean onSingleTapUp(MotionEvent e);

        /**
         * 包含开始滚动时的按下 {@link MotionEvent} 和当前移动 {@link MotionEvent}
         * 的滚动事件.为了方便提供了X轴和Y轴上的滚动距离.
         *
         * @param e1 开始滚动时的按下动作事件.
         * @param e2 触发当前滚动的移动动作事件.
         * @param distanceX 上次执行onScroll事件后沿x轴方向的移动量.
         *              不是{@code e1} 和 {@code e2}之间的距离.
         * @param distanceY 上次执行onScroll事件后沿y轴方向的移动量.
         *              不是{@code e1} 和 {@code e2}之间的距离.
         * @return 若事件已处理返回真；否则返回假.
         */
        boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY);

        /**
         * 触发长按时的通知事件，包含最初按下时的 {@link MotionEvent}.
         *
         * @param e 开始长按时的按下动作事件.
         */
        void onLongPress(MotionEvent e);

        /**
         * 包含初始按下 {@link MotionEvent} 和抬起 {@link MotionEvent} 的快速滑动事件的通知.
         * 提供x、y两个方向的速度，以每秒像素数为单位.
         *
         * @param e1 开始快速滑动时的按下事件.
         * @param e2 触发当前快速滑动的移动动作事件.
         * @param velocityX 在x轴方向测定的以每秒像素数为单位的滑动速度.
         * @param velocityY 在y轴方向测定的以每秒像素数为单位的滑动速度.
         * @return 若事件已处理返回真；否则返回假.
         */
        boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY);
    }

    /**
     * 用于通知发生了双击或确定的单击事件的监听器.
     */
    public interface OnDoubleTapListener {
        /**
         * 发生确定的单击时执行.
         * <p>
         * 与{@link OnGestureListener#onSingleTapUp(MotionEvent)}不同，
         * 该事件在探测器确定用户单击后没有发生导致双击事件的第二次单击时发生.
         *
         * @param e 单击手势的按下动作事件.
         * @return 事件已处理返回真，否则返回假.
         */
        boolean onSingleTapConfirmed(MotionEvent e);
 
        /**
         * 双击发生时的通知.
         *
         * @param e 双击手势的第一次按下动作事件.
         * @return 事件已处理返回真，否则返回假.
         */
        boolean onDoubleTap(MotionEvent e);

        /**
         * 双击手势过程中发生的事件，包括按下、移动和抬起事件.
         *
         * @param e 双击手势过程中发生的事件.
         * @return 事件已处理返回真，否则返回假.
         */
        boolean onDoubleTapEvent(MotionEvent e);
    }

    /**
     * 便于只实现一部分手势时继承的类. 该类实现了{@link OnGestureListener}
     * 和{@link OnDoubleTapListener}中的所有方法，但没有任何处理，
     * 只是简单的返回{@code false 假}.
     */
    public static class SimpleOnGestureListener implements OnGestureListener, OnDoubleTapListener {
        public boolean onSingleTapUp(MotionEvent e) {
            return false;
        }

        public void onLongPress(MotionEvent e) {
        }

        public boolean onScroll(MotionEvent e1, MotionEvent e2,
                float distanceX, float distanceY) {
            return false;
        }

        public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX,
                float velocityY) {
            return false;
        }

        public void onShowPress(MotionEvent e) {
        }

        public boolean onDown(MotionEvent e) {
            return false;
        }

        public boolean onDoubleTap(MotionEvent e) {
            return false;
        }

        public boolean onDoubleTapEvent(MotionEvent e) {
            return false;
        }

        public boolean onSingleTapConfirmed(MotionEvent e) {
            return false;
        }
    }

    private int mTouchSlopSquare;
    private int mDoubleTapTouchSlopSquare;
    private int mDoubleTapSlopSquare;
    private int mMinimumFlingVelocity;
    private int mMaximumFlingVelocity;

    private static final int LONGPRESS_TIMEOUT = ViewConfiguration.getLongPressTimeout();
    private static final int TAP_TIMEOUT = ViewConfiguration.getTapTimeout();
    private static final int DOUBLE_TAP_TIMEOUT = ViewConfiguration.getDoubleTapTimeout();

    // constants for Message.what used by GestureHandler below
    private static final int SHOW_PRESS = 1;
    private static final int LONG_PRESS = 2;
    private static final int TAP = 3;

    private final Handler mHandler;
    private final OnGestureListener mListener;
    private OnDoubleTapListener mDoubleTapListener;

    private boolean mStillDown;
    private boolean mInLongPress;
    private boolean mAlwaysInTapRegion;
    private boolean mAlwaysInBiggerTapRegion;

    private MotionEvent mCurrentDownEvent;
    private MotionEvent mPreviousUpEvent;

    /**
     * True when the user is still touching for the second tap (down, move, and
     * up events). Can only be true if there is a double tap listener attached.
     */
    private boolean mIsDoubleTapping;

    private float mLastFocusX;
    private float mLastFocusY;
    private float mDownFocusX;
    private float mDownFocusY;

    private boolean mIsLongpressEnabled;

    /**
     * Determines speed during touch scrolling
     */
    private VelocityTracker mVelocityTracker;

    /**
     * Consistency verifier for debugging purposes.
     */
    private final InputEventConsistencyVerifier mInputEventConsistencyVerifier =
            InputEventConsistencyVerifier.isInstrumentationEnabled() ?
                    new InputEventConsistencyVerifier(this, 0) : null;

    private class GestureHandler extends Handler {
        GestureHandler() {
            super();
        }

        GestureHandler(Handler handler) {
            super(handler.getLooper());
        }

        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
            case SHOW_PRESS:
                mListener.onShowPress(mCurrentDownEvent);
                break;
                
            case LONG_PRESS:
                dispatchLongPress();
                break;
                
            case TAP:
                // If the user's finger is still down, do not count it as a tap
                if (mDoubleTapListener != null && !mStillDown) {
                    mDoubleTapListener.onSingleTapConfirmed(mCurrentDownEvent);
                }
                break;

            default:
                throw new RuntimeException("Unknown message " + msg); //never
            }
        }
    }

    /**
     * 根据提供的监听器创建GestureDetector. 该函数用于非UI线程
     * （它指定了句柄）.
     * 
     * @param listener 实现了所有回调函数的监听器，不能为空.
     * @param handler 使用的句柄.
     *
     * @throws NullPointerException 当{@code listener}或
     * {@code handler}为空时.
     *
     * @deprecated 用{@link #GestureDetector(android.content.Context,
     *      android.view.GestureDetector.OnGestureListener, android.os.Handler)}代替.
     */
    @Deprecated
    public GestureDetector(OnGestureListener listener, Handler handler) {
        this(null, listener, handler);
    }

    /**
     * 根据提供的监听器创建GestureDetector. 该函数用于UI线程
     * （一般的情况）.
     * @see android.os.Handler#Handler()
     * 
     * @param listener 实现了所有回调函数的监听器，不能为空.
     * 
     * @throws NullPointerException 当{@code listener}为空时.
     *
     * @deprecated 用{@link #GestureDetector(android.content.Context,
     *      android.view.GestureDetector.OnGestureListener)}代替.
     */
    @Deprecated
    public GestureDetector(OnGestureListener listener) {
        this(null, listener, null);
    }

    /**
     * 根据提供的监听器创建GestureDetector. 该函数用于UI线程
     * （一般的情况）.
     * @see android.os.Handler#Handler()
     *
     * @param context 应用程序上下文.
     * @param listener 实现了所有回调函数的监听器，不能为空.
     *
     * @throws NullPointerException 当{@code listener}为空时.
     */
    public GestureDetector(Context context, OnGestureListener listener) {
        this(context, listener, null);
    }

    /**
     * 根据提供的监听器创建GestureDetector. 该函数用于UI线程
     * （一般的情况）.
     * @see android.os.Handler#Handler()
     *
     * @param context 应用程序上下文.
     * @param listener 实现了所有回调函数的监听器，不能为空.
     * @param handler 使用的句柄. 
     *
     * @throws NullPointerException 当{@code listener}为空时.
     */
    public GestureDetector(Context context, OnGestureListener listener, Handler handler) {
        if (handler != null) {
            mHandler = new GestureHandler(handler);
        } else {
            mHandler = new GestureHandler();
        }
        mListener = listener;
        if (listener instanceof OnDoubleTapListener) {
            setOnDoubleTapListener((OnDoubleTapListener) listener);
        }
        init(context);
    }
    
    /**
     * 根据提供的监听器创建GestureDetector. 该函数用于UI线程
     * （一般的情况）.
     * @see android.os.Handler#Handler()
     *
     * @param context 应用程序上下文.
     * @param listener 实现了所有回调函数的监听器，不能为空.
     * @param handler 使用的句柄. 
     *
     * @throws NullPointerException 当{@code listener}为空时.
     */
    public GestureDetector(Context context, OnGestureListener listener, Handler handler,
            boolean unused) {
        this(context, listener, handler);
    }

    private void init(Context context) {
        if (mListener == null) {
            throw new NullPointerException("OnGestureListener must not be null");
        }
        mIsLongpressEnabled = true;

        // Fallback to support pre-donuts releases
        int touchSlop, doubleTapSlop, doubleTapTouchSlop;
        if (context == null) {
            //noinspection deprecation
            touchSlop = ViewConfiguration.getTouchSlop();
            doubleTapTouchSlop = touchSlop; // Hack rather than adding a hiden method for this
            doubleTapSlop = ViewConfiguration.getDoubleTapSlop();
            //noinspection deprecation
            mMinimumFlingVelocity = ViewConfiguration.getMinimumFlingVelocity();
            mMaximumFlingVelocity = ViewConfiguration.getMaximumFlingVelocity();
        } else {
            final ViewConfiguration configuration = ViewConfiguration.get(context);
            touchSlop = configuration.getScaledTouchSlop();
            doubleTapTouchSlop = configuration.getScaledDoubleTapTouchSlop();
            doubleTapSlop = configuration.getScaledDoubleTapSlop();
            mMinimumFlingVelocity = configuration.getScaledMinimumFlingVelocity();
            mMaximumFlingVelocity = configuration.getScaledMaximumFlingVelocity();
        }
        mTouchSlopSquare = touchSlop * touchSlop;
        mDoubleTapTouchSlopSquare = doubleTapTouchSlop * doubleTapTouchSlop;
        mDoubleTapSlopSquare = doubleTapSlop * doubleTapSlop;
    }

    /**
     * 设置双击及其相关手势的监听器.
     * 
     * @param onDoubleTapListener 用于执行双击手势时所有回调函数的监听器，
     *        为空时停止监听双击手势.
     */
    public void setOnDoubleTapListener(OnDoubleTapListener onDoubleTapListener) {
        mDoubleTapListener = onDoubleTapListener;
    }

    /**
     * 设置是否允许长按.如果允许长按，当用户按下并保持按下状态时，
     * 将收到一个长按事件，同时不再接收其它事件；如果禁用长按，
     * 当用户按下并保持按下状态然后再移动手指时，将会接收到滚动事件.
     * 长按默认为允许.
     *
     * @param isLongpressEnabled 是否允许长按.
     */
    public void setIsLongpressEnabled(boolean isLongpressEnabled) {
        mIsLongpressEnabled = isLongpressEnabled;
    }

    /**
     * @return 如果允许长按，返回真；否则返回假.
     */
    public boolean isLongpressEnabled() {
        return mIsLongpressEnabled;
    }

    /**
     * 分析给定的动作事件，如果满足条件，就触发{@link OnGestureListener}
     * 中提供的回调函数.
     *
     * @param ev 当前动作事件.
     * @return 如果{@link OnGestureListener}处理了事件，返回真；否则返回假.
     */
    public boolean onTouchEvent(MotionEvent ev) {
        if (mInputEventConsistencyVerifier != null) {
            mInputEventConsistencyVerifier.onTouchEvent(ev, 0);
        }

        final int action = ev.getAction();

        if (mVelocityTracker == null) {
            mVelocityTracker = VelocityTracker.obtain();
        }
        mVelocityTracker.addMovement(ev);

        final boolean pointerUp =
                (action & MotionEvent.ACTION_MASK) == MotionEvent.ACTION_POINTER_UP;
        final int skipIndex = pointerUp ? ev.getActionIndex() : -1;

        // Determine focal point
        float sumX = 0, sumY = 0;
        final int count = ev.getPointerCount();
        for (int i = 0; i < count; i++) {
            if (skipIndex == i) continue;
            sumX += ev.getX(i);
            sumY += ev.getY(i);
        }
        final int div = pointerUp ? count - 1 : count;
        final float focusX = sumX / div;
        final float focusY = sumY / div;

        boolean handled = false;

        switch (action & MotionEvent.ACTION_MASK) {
        case MotionEvent.ACTION_POINTER_DOWN:
            mDownFocusX = mLastFocusX = focusX;
            mDownFocusY = mLastFocusY = focusY;
            // Cancel long press and taps
            cancelTaps();
            break;

        case MotionEvent.ACTION_POINTER_UP:
            mDownFocusX = mLastFocusX = focusX;
            mDownFocusY = mLastFocusY = focusY;

            // Check the dot product of current velocities.
            // If the pointer that left was opposing another velocity vector, clear.
            mVelocityTracker.computeCurrentVelocity(1000, mMaximumFlingVelocity);
            final int upIndex = ev.getActionIndex();
            final int id1 = ev.getPointerId(upIndex);
            final float x1 = mVelocityTracker.getXVelocity(id1);
            final float y1 = mVelocityTracker.getYVelocity(id1);
            for (int i = 0; i < count; i++) {
                if (i == upIndex) continue;

                final int id2 = ev.getPointerId(i);
                final float x = x1 * mVelocityTracker.getXVelocity(id2);
                final float y = y1 * mVelocityTracker.getYVelocity(id2);

                final float dot = x + y;
                if (dot < 0) {
                    mVelocityTracker.clear();
                    break;
                }
            }
            break;

        case MotionEvent.ACTION_DOWN:
            if (mDoubleTapListener != null) {
                boolean hadTapMessage = mHandler.hasMessages(TAP);
                if (hadTapMessage) mHandler.removeMessages(TAP);
                if ((mCurrentDownEvent != null) && (mPreviousUpEvent != null) && hadTapMessage &&
                        isConsideredDoubleTap(mCurrentDownEvent, mPreviousUpEvent, ev)) {
                    // This is a second tap
                    mIsDoubleTapping = true;
                    // Give a callback with the first tap of the double-tap
                    handled |= mDoubleTapListener.onDoubleTap(mCurrentDownEvent);
                    // Give a callback with down event of the double-tap
                    handled |= mDoubleTapListener.onDoubleTapEvent(ev);
                } else {
                    // This is a first tap
                    mHandler.sendEmptyMessageDelayed(TAP, DOUBLE_TAP_TIMEOUT);
                }
            }

            mDownFocusX = mLastFocusX = focusX;
            mDownFocusY = mLastFocusY = focusY;
            if (mCurrentDownEvent != null) {
                mCurrentDownEvent.recycle();
            }
            mCurrentDownEvent = MotionEvent.obtain(ev);
            mAlwaysInTapRegion = true;
            mAlwaysInBiggerTapRegion = true;
            mStillDown = true;
            mInLongPress = false;
            
            if (mIsLongpressEnabled) {
                mHandler.removeMessages(LONG_PRESS);
                mHandler.sendEmptyMessageAtTime(LONG_PRESS, mCurrentDownEvent.getDownTime()
                        + TAP_TIMEOUT + LONGPRESS_TIMEOUT);
            }
            mHandler.sendEmptyMessageAtTime(SHOW_PRESS, mCurrentDownEvent.getDownTime() + TAP_TIMEOUT);
            handled |= mListener.onDown(ev);
            break;

        case MotionEvent.ACTION_MOVE:
            if (mInLongPress) {
                break;
            }
            final float scrollX = mLastFocusX - focusX;
            final float scrollY = mLastFocusY - focusY;
            if (mIsDoubleTapping) {
                // Give the move events of the double-tap
                handled |= mDoubleTapListener.onDoubleTapEvent(ev);
            } else if (mAlwaysInTapRegion) {
                final int deltaX = (int) (focusX - mDownFocusX);
                final int deltaY = (int) (focusY - mDownFocusY);
                int distance = (deltaX * deltaX) + (deltaY * deltaY);
                if (distance > mTouchSlopSquare) {
                    handled = mListener.onScroll(mCurrentDownEvent, ev, scrollX, scrollY);
                    mLastFocusX = focusX;
                    mLastFocusY = focusY;
                    mAlwaysInTapRegion = false;
                    mHandler.removeMessages(TAP);
                    mHandler.removeMessages(SHOW_PRESS);
                    mHandler.removeMessages(LONG_PRESS);
                }
                if (distance > mDoubleTapTouchSlopSquare) {
                    mAlwaysInBiggerTapRegion = false;
                }
            } else if ((Math.abs(scrollX) >= 1) || (Math.abs(scrollY) >= 1)) {
                handled = mListener.onScroll(mCurrentDownEvent, ev, scrollX, scrollY);
                mLastFocusX = focusX;
                mLastFocusY = focusY;
            }
            break;

        case MotionEvent.ACTION_UP:
            mStillDown = false;
            MotionEvent currentUpEvent = MotionEvent.obtain(ev);
            if (mIsDoubleTapping) {
                // Finally, give the up event of the double-tap
                handled |= mDoubleTapListener.onDoubleTapEvent(ev);
            } else if (mInLongPress) {
                mHandler.removeMessages(TAP);
                mInLongPress = false;
            } else if (mAlwaysInTapRegion) {
                handled = mListener.onSingleTapUp(ev);
            } else {

                // A fling must travel the minimum tap distance
                final VelocityTracker velocityTracker = mVelocityTracker;
                final int pointerId = ev.getPointerId(0);
                velocityTracker.computeCurrentVelocity(1000, mMaximumFlingVelocity);
                final float velocityY = velocityTracker.getYVelocity(pointerId);
                final float velocityX = velocityTracker.getXVelocity(pointerId);

                if ((Math.abs(velocityY) > mMinimumFlingVelocity)
                        || (Math.abs(velocityX) > mMinimumFlingVelocity)){
                    handled = mListener.onFling(mCurrentDownEvent, ev, velocityX, velocityY);
                }
            }
            if (mPreviousUpEvent != null) {
                mPreviousUpEvent.recycle();
            }
            // Hold the event we obtained above - listeners may have changed the original.
            mPreviousUpEvent = currentUpEvent;
            if (mVelocityTracker != null) {
                // This may have been cleared when we called out to the
                // application above.
                mVelocityTracker.recycle();
                mVelocityTracker = null;
            }
            mIsDoubleTapping = false;
            mHandler.removeMessages(SHOW_PRESS);
            mHandler.removeMessages(LONG_PRESS);
            break;

        case MotionEvent.ACTION_CANCEL:
            cancel();
            break;
        }

        if (!handled && mInputEventConsistencyVerifier != null) {
            mInputEventConsistencyVerifier.onUnhandledEvent(ev, 0);
        }
        return handled;
    }

    private void cancel() {
        mHandler.removeMessages(SHOW_PRESS);
        mHandler.removeMessages(LONG_PRESS);
        mHandler.removeMessages(TAP);
        mVelocityTracker.recycle();
        mVelocityTracker = null;
        mIsDoubleTapping = false;
        mStillDown = false;
        mAlwaysInTapRegion = false;
        mAlwaysInBiggerTapRegion = false;
        if (mInLongPress) {
            mInLongPress = false;
        }
    }

    private void cancelTaps() {
        mHandler.removeMessages(SHOW_PRESS);
        mHandler.removeMessages(LONG_PRESS);
        mHandler.removeMessages(TAP);
        mIsDoubleTapping = false;
        mAlwaysInTapRegion = false;
        mAlwaysInBiggerTapRegion = false;
        if (mInLongPress) {
            mInLongPress = false;
        }
    }

    private boolean isConsideredDoubleTap(MotionEvent firstDown, MotionEvent firstUp,
            MotionEvent secondDown) {
        if (!mAlwaysInBiggerTapRegion) {
            return false;
        }

        if (secondDown.getEventTime() - firstUp.getEventTime() > DOUBLE_TAP_TIMEOUT) {
            return false;
        }

        int deltaX = (int) firstDown.getX() - (int) secondDown.getX();
        int deltaY = (int) firstDown.getY() - (int) secondDown.getY();
        return (deltaX * deltaX + deltaY * deltaY < mDoubleTapSlopSquare);
    }

    private void dispatchLongPress() {
        mHandler.removeMessages(TAP);
        mInLongPress = true;
        mListener.onLongPress(mCurrentDownEvent);
    }
}
