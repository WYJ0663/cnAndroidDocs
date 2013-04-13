/*
 * Copyright (C) 2006 The Android Open Source Project
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

package android.widget;

import android.content.Context;
import android.hardware.SensorManager;
import android.os.Build;
import android.util.FloatMath;
import android.view.ViewConfiguration;
import android.view.animation.AnimationUtils;
import android.view.animation.Interpolator;


/**
 * <p>这个类封装了滚动操作. You can use scrollers ({@link Scroller}
 * or {@link OverScroller}) to collect the data you need to produce a scrolling
 * animation&mdash;for example, in response to a fling gesture. Scrollers track
 * scroll offsets for you over time, but they don't automatically apply those
 * positions to your view. It's your responsibility to get and apply new
 * coordinates at a rate that will make the scrolling animation look smooth.</p>
 *
 * <p>Here is a simple example:</p>
 *
 * <pre> private Scroller mScroller = new Scroller(context);
 * ...
 * public void zoomIn() {
 *     // Revert any animation currently in progress
 *     mScroller.forceFinished(true);
 *     // Start scrolling by providing a starting point and
 *     // the distance to travel
 *     mScroller.startScroll(0, 0, 100, 0);
 *     // Invalidate to request a redraw
 *     invalidate();
 * }</pre>
 *
 * <p>To track the changing positions of the x/y coordinates, use
 * {@link #computeScrollOffset}. The method returns a boolean to indicate
 * whether the scroller is finished. If it isn't, it means that a fling or
 * programmatic pan operation is still in progress. You can use this method to
 * find the current offsets of the x and y coordinates, for example:</p>
 *
 * <pre>if (mScroller.computeScrollOffset()) {
 *     // Get current x and y positions
 *     int currX = mScroller.getCurrX();
 *     int currY = mScroller.getCurrY();
 *    ...
 * }</pre>
 * @author translate by pengyouhong
 * @author convert by cnmahj
 */
public class Scroller  {
    private int mMode;

    private int mStartX;
    private int mStartY;
    private int mFinalX;
    private int mFinalY;

    private int mMinX;
    private int mMaxX;
    private int mMinY;
    private int mMaxY;

    private int mCurrX;
    private int mCurrY;
    private long mStartTime;
    private int mDuration;
    private float mDurationReciprocal;
    private float mDeltaX;
    private float mDeltaY;
    private boolean mFinished;
    private Interpolator mInterpolator;
    private boolean mFlywheel;

    private float mVelocity;
    private float mCurrVelocity;
    private int mDistance;

    private float mFlingFriction = ViewConfiguration.getScrollFriction();

    private static final int DEFAULT_DURATION = 250;
    private static final int SCROLL_MODE = 0;
    private static final int FLING_MODE = 1;

    private static float DECELERATION_RATE = (float) (Math.log(0.78) / Math.log(0.9));
    private static final float INFLEXION = 0.35f; // Tension lines cross at (INFLEXION, 1)
    private static final float START_TENSION = 0.5f;
    private static final float END_TENSION = 1.0f;
    private static final float P1 = START_TENSION * INFLEXION;
    private static final float P2 = 1.0f - END_TENSION * (1.0f - INFLEXION);

    private static final int NB_SAMPLES = 100;
    private static final float[] SPLINE_POSITION = new float[NB_SAMPLES + 1];
    private static final float[] SPLINE_TIME = new float[NB_SAMPLES + 1];

    private float mDeceleration;
    private final float mPpi;

    // A context-specific coefficient adjusted to physical values.
    private float mPhysicalCoeff;

    static {
        float x_min = 0.0f;
        float y_min = 0.0f;
        for (int i = 0; i < NB_SAMPLES; i++) {
            final float alpha = (float) i / NB_SAMPLES;

            float x_max = 1.0f;
            float x, tx, coef;
            while (true) {
                x = x_min + (x_max - x_min) / 2.0f;
                coef = 3.0f * x * (1.0f - x);
                tx = coef * ((1.0f - x) * P1 + x * P2) + x * x * x;
                if (Math.abs(tx - alpha) < 1E-5) break;
                if (tx > alpha) x_max = x;
                else x_min = x;
            }
            SPLINE_POSITION[i] = coef * ((1.0f - x) * START_TENSION + x) + x * x * x;

            float y_max = 1.0f;
            float y, dy;
            while (true) {
                y = y_min + (y_max - y_min) / 2.0f;
                coef = 3.0f * y * (1.0f - y);
                dy = coef * ((1.0f - y) * START_TENSION + y) + y * y * y;
                if (Math.abs(dy - alpha) < 1E-5) break;
                if (dy > alpha) y_max = y;
                else y_min = y;
            }
            SPLINE_TIME[i] = coef * ((1.0f - y) * P1 + y * P2) + y * y * y;
        }
        SPLINE_POSITION[NB_SAMPLES] = SPLINE_TIME[NB_SAMPLES] = 1.0f;

        // This controls the viscous fluid effect (how much of it)
        sViscousFluidScale = 8.0f;
        // must be set to 1.0 (used in viscousFluid())
        sViscousFluidNormalize = 1.0f;
        sViscousFluidNormalize = 1.0f / viscousFluid(1.0f);

    }

    private static float sViscousFluidScale;
    private static float sViscousFluidNormalize;

    /**
     * 使用缺省的持续持续时间和动画插入器（interpolator）创建 Scroller.
     */
    public Scroller(Context context) {
        this(context, null);
    }

    /**
     * 根据指定的动画插入器（interpolator）创建 Scroller，如果指定的动画插入器为空，
     * 则会使用缺省的动画插入器（粘滞viscous）创建.
     * 飞轮（Flywheel）动作效果只适用于 Honeycomb 及更新的系统.
     */
    public Scroller(Context context, Interpolator interpolator) {
        this(context, interpolator,
                context.getApplicationInfo().targetSdkVersion >= Build.VERSION_CODES.HONEYCOMB);
    }

    /**
     * 使用指定的插补器创建 Scroller.如果插补器为空，使用默认（粘滞）插补器.
     * 并可指定快速滑动时，是否支持“飞轮”行为.
     */
    public Scroller(Context context, Interpolator interpolator, boolean flywheel) {
        mFinished = true;
        mInterpolator = interpolator;
        mPpi = context.getResources().getDisplayMetrics().density * 160.0f;
        mDeceleration = computeDeceleration(ViewConfiguration.getScrollFriction());
        mFlywheel = flywheel;

        mPhysicalCoeff = computeDeceleration(0.84f); // look and feel tuning
    }

    /**
     * 应用到快速滑动的摩擦系数.默认值为 {@link ViewConfiguration#getScrollFriction}.
     * 
     * @param friction 代表与维度无关的摩擦系数.
     */
    public final void setFriction(float friction) {
        mDeceleration = computeDeceleration(friction);
        mFlingFriction = friction;
    }
    
    private float computeDeceleration(float friction) {
        return SensorManager.GRAVITY_EARTH   // g (m/s^2)
                      * 39.37f               // inch/meter
                      * mPpi                 // pixels per inch
                      * friction;
    }

    /**
     * 
     * 返回 scroller 是否已完成滚动.
     * 
     * @return 已完成滚动返回真，否则返回假.
     */
    public final boolean isFinished() {
        return mFinished;
    }
    
    /**
     * 强制设置终止状态为特定值.
     *  
     * @param finished 新的终止状态.
     */
    public final void forceFinished(boolean finished) {
        mFinished = finished;
    }
    
    /**
     * 返回滚动事件持续的时间，以毫秒为单位.
     * 
     * @return 以毫秒为单位的持续的时间.
     */
    public final int getDuration() {
        return mDuration;
    }
    
    /**
     * 返回当前滚动 X 方向的偏移.
     * 
     * @return 距离原点 X 轴方向的绝对值.
     */
    public final int getCurrX() {
        return mCurrX;
    }
    
    /**
     * 返回当前滚动 Y 方向的偏移.
     * 
     * @return 距离原点 Y 轴方向的绝对值.
     */
    public final int getCurrY() {
        return mCurrY;
    }
    
    /**
     * 返回当前速度.
     *
     * @return 与维度无关的原始速度.结果可能为负值.
     */
    public float getCurrVelocity() {
        return mMode == FLING_MODE ?
                mCurrVelocity : mVelocity - mDeceleration * timePassed() / 2000.0f;
    }

    /**
     * 返回滚动起始点的X方向的偏移.
     *  
     * @return 起始点在X方向距离原点的绝对距离.
     */
    public final int getStartX() {
        return mStartX;
    }
    
    /**
     * 返回滚动起始点的Y方向的偏移.
     * 
     * @return 起始点在Y方向距离原点的绝对距离.
     */
    public final int getStartY() {
        return mStartY;
    }
    
    /**
     * 返回滚动结束位置.仅针对“fling”滚动有效.
     * 
     * @return 最终位置X方向距离原点的绝对距离.
     */
    public final int getFinalX() {
        return mFinalX;
    }
    
    /**
     * 返回滚动结束位置.仅针对“fling”滚动有效.
     * 
     * @return 最终位置Y方向距离原点的绝对距离.
     */
    public final int getFinalY() {
        return mFinalY;
    }

    /**
     * 当想要知道新的位置时，调用此函数.如果返回真，表示动画还没有结束.
     */ 
    public boolean computeScrollOffset() {
        if (mFinished) {
            return false;
        }

        int timePassed = (int)(AnimationUtils.currentAnimationTimeMillis() - mStartTime);
    
        if (timePassed < mDuration) {
            switch (mMode) {
            case SCROLL_MODE:
                float x = timePassed * mDurationReciprocal;
    
                if (mInterpolator == null)
                    x = viscousFluid(x); 
                else
                    x = mInterpolator.getInterpolation(x);
    
                mCurrX = mStartX + Math.round(x * mDeltaX);
                mCurrY = mStartY + Math.round(x * mDeltaY);
                break;
            case FLING_MODE:
                final float t = (float) timePassed / mDuration;
                final int index = (int) (NB_SAMPLES * t);
                float distanceCoef = 1.f;
                float velocityCoef = 0.f;
                if (index < NB_SAMPLES) {
                    final float t_inf = (float) index / NB_SAMPLES;
                    final float t_sup = (float) (index + 1) / NB_SAMPLES;
                    final float d_inf = SPLINE_POSITION[index];
                    final float d_sup = SPLINE_POSITION[index + 1];
                    velocityCoef = (d_sup - d_inf) / (t_sup - t_inf);
                    distanceCoef = d_inf + (t - t_inf) * velocityCoef;
                }

                mCurrVelocity = velocityCoef * mDistance / mDuration * 1000.0f;
                
                mCurrX = mStartX + Math.round(distanceCoef * (mFinalX - mStartX));
                // Pin to mMinX <= mCurrX <= mMaxX
                mCurrX = Math.min(mCurrX, mMaxX);
                mCurrX = Math.max(mCurrX, mMinX);
                
                mCurrY = mStartY + Math.round(distanceCoef * (mFinalY - mStartY));
                // Pin to mMinY <= mCurrY <= mMaxY
                mCurrY = Math.min(mCurrY, mMaxY);
                mCurrY = Math.max(mCurrY, mMinY);

                if (mCurrX == mFinalX && mCurrY == mFinalY) {
                    mFinished = true;
                }

                break;
            }
        }
        else {
            mCurrX = mFinalX;
            mCurrY = mFinalY;
            mFinished = true;
        }
        return true;
    }
    
    /**
     * 以提供的起始点和将要滑动的距离开始滚动.滚动会使用缺省值 250ms 作为持续时间.
     * 
     * @param startX 水平方向滚动的偏移值，以像素为单位.负值表示向左滚动.
     * @param startY 垂直方向滚动的偏移值，以像素为单位.负值表示向上滚动.
     * @param dx 水平方向滑动的距离，负值表示向左滚动.
     * @param dy 垂直方向滑动的距离，负值表示向上滚动.
     */
    public void startScroll(int startX, int startY, int dx, int dy) {
        startScroll(startX, startY, dx, dy, DEFAULT_DURATION);
    }

    /**
     * 以提供的起始点、要滑动的距离和持续时间开始滚动.
     * 
     * @param startX 水平方向滚动的偏移值，以像素为单位.负值表示向左滚动.
     * @param startY 垂直方向滚动的偏移值，以像素为单位.负值表示向上滚动.
     * @param dx 水平方向滑动的距离，负值表示向左滚动.
     * @param dy 垂直方向滑动的距离，负值表示向上滚动.
     * @param duration 以毫秒为单位的滚动持续时间.
     */
    public void startScroll(int startX, int startY, int dx, int dy, int duration) {
        mMode = SCROLL_MODE;
        mFinished = false;
        mDuration = duration;
        mStartTime = AnimationUtils.currentAnimationTimeMillis();
        mStartX = startX;
        mStartY = startY;
        mFinalX = startX + dx;
        mFinalY = startY + dy;
        mDeltaX = dx;
        mDeltaY = dy;
        mDurationReciprocal = 1.0f / (float) mDuration;
    }

    /**
     * 开始基于 fling 手势的滚动.滚动的距离取决于 fling 的初速度.
     * 
     * @param startX 滚动起始点X坐标.
     * @param startY 滚动起始点Y坐标
     * @param velocityX 当滑动屏幕时X方向初速度，以每秒像素数计算.
     * @param velocityY 当滑动屏幕时Y方向初速度，以每秒像素数计算
     * @param minX X方向的最小值，scroller的滚动不会低于该值.
     * @param maxX X方向的最大值，scroller的滚动不会高于该值.
     * @param minY Y方向的最小值，scroller的滚动不会低于该值.
     * @param maxY Y方向的最大值，scroller的滚动不会高于该值.
     */
    public void fling(int startX, int startY, int velocityX, int velocityY,
            int minX, int maxX, int minY, int maxY) {
        // Continue a scroll or fling in progress
        if (mFlywheel && !mFinished) {
            float oldVel = getCurrVelocity();

            float dx = (float) (mFinalX - mStartX);
            float dy = (float) (mFinalY - mStartY);
            float hyp = FloatMath.sqrt(dx * dx + dy * dy);

            float ndx = dx / hyp;
            float ndy = dy / hyp;

            float oldVelocityX = ndx * oldVel;
            float oldVelocityY = ndy * oldVel;
            if (Math.signum(velocityX) == Math.signum(oldVelocityX) &&
                    Math.signum(velocityY) == Math.signum(oldVelocityY)) {
                velocityX += oldVelocityX;
                velocityY += oldVelocityY;
            }
        }

        mMode = FLING_MODE;
        mFinished = false;

        float velocity = FloatMath.sqrt(velocityX * velocityX + velocityY * velocityY);
     
        mVelocity = velocity;
        mDuration = getSplineFlingDuration(velocity);
        mStartTime = AnimationUtils.currentAnimationTimeMillis();
        mStartX = startX;
        mStartY = startY;

        float coeffX = velocity == 0 ? 1.0f : velocityX / velocity;
        float coeffY = velocity == 0 ? 1.0f : velocityY / velocity;

        double totalDistance = getSplineFlingDistance(velocity);
        mDistance = (int) (totalDistance * Math.signum(velocity));
        
        mMinX = minX;
        mMaxX = maxX;
        mMinY = minY;
        mMaxY = maxY;

        mFinalX = startX + (int) Math.round(totalDistance * coeffX);
        // Pin to mMinX <= mFinalX <= mMaxX
        mFinalX = Math.min(mFinalX, mMaxX);
        mFinalX = Math.max(mFinalX, mMinX);
        
        mFinalY = startY + (int) Math.round(totalDistance * coeffY);
        // Pin to mMinY <= mFinalY <= mMaxY
        mFinalY = Math.min(mFinalY, mMaxY);
        mFinalY = Math.max(mFinalY, mMinY);
    }
    
    private double getSplineDeceleration(float velocity) {
        return Math.log(INFLEXION * Math.abs(velocity) / (mFlingFriction * mPhysicalCoeff));
    }

    private int getSplineFlingDuration(float velocity) {
        final double l = getSplineDeceleration(velocity);
        final double decelMinusOne = DECELERATION_RATE - 1.0;
        return (int) (1000.0 * Math.exp(l / decelMinusOne));
    }

    private double getSplineFlingDistance(float velocity) {
        final double l = getSplineDeceleration(velocity);
        final double decelMinusOne = DECELERATION_RATE - 1.0;
        return mFlingFriction * mPhysicalCoeff * Math.exp(DECELERATION_RATE / decelMinusOne * l);
    }

    static float viscousFluid(float x)
    {
        x *= sViscousFluidScale;
        if (x < 1.0f) {
            x -= (1.0f - (float)Math.exp(-x));
        } else {
            float start = 0.36787944117f;   // 1/e == exp(-1)
            x = 1.0f - (float)Math.exp(1.0f - x);
            x = start + x * (1.0f - start);
        }
        x *= sViscousFluidNormalize;
        return x;
    }
    
    /**
     * 停止动画.
     * 与 {@link #forceFinished(boolean)} 不同，该方法终止动画并滚动到最终的X、Y位置.
     *
     * @see #forceFinished(boolean)
     */
    public void abortAnimation() {
        mCurrX = mFinalX;
        mCurrY = mFinalY;
        mFinished = true;
    }
    
    /**
     * 延长滚动动画时间.此函数允许与 {@link #setFinalX(int)} 和  {@link #setFinalY(int)}
     * 一起使用，延长滚动动画的持续时间和滚动距离.
     *
     * @param extend 延长的以毫秒为单位的时间.
     * @see #setFinalX(int)
     * @see #setFinalY(int)
     */
    public void extendDuration(int extend) {
        int passed = timePassed();
        mDuration = passed + extend;
        mDurationReciprocal = 1.0f / mDuration;
        mFinished = false;
    }

    /**
     * 返回自滚动开始经过的时间.
     *
     * @return 经过时间以毫秒为单位.
     */
    public int timePassed() {
        return (int)(AnimationUtils.currentAnimationTimeMillis() - mStartTime);
    }

    /**
     * 设置 scroller 的 X 方向终止位置.
     *
     * @param newX 新位置在 X 方向距离原点的绝对偏移量.
     * @see #extendDuration(int)
     * @see #setFinalY(int)
     */
    public void setFinalX(int newX) {
        mFinalX = newX;
        mDeltaX = mFinalX - mStartX;
        mFinished = false;
    }

    /**
     * 设置 scroller 的 Y 方向终止位置.
     *
     * @param newY 新位置在 Y 方向距离原点的绝对偏移量.
     * @see #extendDuration(int)
     * @see #setFinalX(int)
     */
    public void setFinalY(int newY) {
        mFinalY = newY;
        mDeltaY = mFinalY - mStartY;
        mFinished = false;
    }

    /**
     * @hide
     */
    public boolean isScrollingInDirection(float xvel, float yvel) {
        return !mFinished && Math.signum(xvel) == Math.signum(mFinalX - mStartX) &&
                Math.signum(yvel) == Math.signum(mFinalY - mStartY);
    }
}
