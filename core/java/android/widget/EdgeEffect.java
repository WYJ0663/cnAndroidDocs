/*
 * Copyright (C) 2010 The Android Open Source Project
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

import android.graphics.Rect;
import com.android.internal.R;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.view.animation.AnimationUtils;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.Interpolator;

/**
 * 该类用于绘制用户在两个方向上滚动超出边界时，可滚动小部件边界的图形效果.
 * 
 * <p>EdgeEffect 是种状态.使用 EdgeEffect 的自定义小部件应该为显示该效果的每条边创建一个实例.
 * 使用 {@link #onAbsorb(int)}、{@link #onPull(float)}和{@link #onRelease()} 传入数据，
 * 在小部件重写的 {@link android.view.View#draw(Canvas)} 方法中使用 {@link #draw(Canvas)}
 * 来绘制效果.如果绘制后 {@link #isFinished()} 返回假，说明边缘效果动画还没有完成，
 * 为了使动画继续，小部件应该将其他的绘制内容稍后再处理.</p>
 *
 * <p>绘制时，小部件应该首先绘制自己及子视图的内容，通常是在重写的 <code>draw</code>
 * 方法中执行 <code>super.draw(canvas)</code> 方法.（这就触发 onDraw 事件并绘制必要的子视图.）
 * 边缘效果可能会使用 {@link #draw(Canvas)} 方法绘制在视图内容之上.</p>
 */
public class EdgeEffect {
    @SuppressWarnings("UnusedDeclaration")
    private static final String TAG = "EdgeEffect";

    // Time it will take the effect to fully recede in ms
    private static final int RECEDE_TIME = 1000;

    // Time it will take before a pulled glow begins receding in ms
    private static final int PULL_TIME = 167;

    // Time it will take in ms for a pulled glow to decay to partial strength before release
    private static final int PULL_DECAY_TIME = 1000;

    private static final float MAX_ALPHA = 1.f;
    private static final float HELD_EDGE_SCALE_Y = 0.5f;

    private static final float MAX_GLOW_HEIGHT = 4.f;

    private static final float PULL_GLOW_BEGIN = 1.f;
    private static final float PULL_EDGE_BEGIN = 0.6f;

    // Minimum velocity that will be absorbed
    private static final int MIN_VELOCITY = 100;

    private static final float EPSILON = 0.001f;

    private final Drawable mEdge;
    private final Drawable mGlow;
    private int mWidth;
    private int mHeight;
    private int mX;
    private int mY;
    private static final int MIN_WIDTH = 300;
    private final int mMinWidth;

    private float mEdgeAlpha;
    private float mEdgeScaleY;
    private float mGlowAlpha;
    private float mGlowScaleY;

    private float mEdgeAlphaStart;
    private float mEdgeAlphaFinish;
    private float mEdgeScaleYStart;
    private float mEdgeScaleYFinish;
    private float mGlowAlphaStart;
    private float mGlowAlphaFinish;
    private float mGlowScaleYStart;
    private float mGlowScaleYFinish;

    private long mStartTime;
    private float mDuration;

    private final Interpolator mInterpolator;

    private static final int STATE_IDLE = 0;
    private static final int STATE_PULL = 1;
    private static final int STATE_ABSORB = 2;
    private static final int STATE_RECEDE = 3;
    private static final int STATE_PULL_DECAY = 4;

    // How much dragging should effect the height of the edge image.
    // Number determined by user testing.
    private static final int PULL_DISTANCE_EDGE_FACTOR = 7;

    // How much dragging should effect the height of the glow image.
    // Number determined by user testing.
    private static final int PULL_DISTANCE_GLOW_FACTOR = 7;
    private static final float PULL_DISTANCE_ALPHA_GLOW_FACTOR = 1.1f;

    private static final int VELOCITY_EDGE_FACTOR = 8;
    private static final int VELOCITY_GLOW_FACTOR = 16;

    private int mState = STATE_IDLE;

    private float mPullDistance;
    
    private final Rect mBounds = new Rect();

    private final int mEdgeHeight;
    private final int mGlowHeight;
    private final int mGlowWidth;
    private final int mMaxEffectHeight;

    /**
     * 使用应用程序上下文中的主题构造一个新的 EdgeEffect 对象.
     * @param context 用于为 EdgeEffect 提供主题及资源信息的应用程序上下文
     */
    public EdgeEffect(Context context) {
        final Resources res = context.getResources();
        mEdge = res.getDrawable(R.drawable.overscroll_edge);
        mGlow = res.getDrawable(R.drawable.overscroll_glow);

        mEdgeHeight = mEdge.getIntrinsicHeight();
        mGlowHeight = mGlow.getIntrinsicHeight();
        mGlowWidth = mGlow.getIntrinsicWidth();

        mMaxEffectHeight = (int) (Math.min(
                mGlowHeight * MAX_GLOW_HEIGHT * mGlowHeight / mGlowWidth * 0.6f,
                mGlowHeight * MAX_GLOW_HEIGHT) + 0.5f);

        mMinWidth = (int) (res.getDisplayMetrics().density * MIN_WIDTH + 0.5f);
        mInterpolator = new DecelerateInterpolator();
    }

    /**
     * 以像素为单位设置大小.
     *
     * @param width 以像素为单位设置效果影响范围的宽度.
     * @param height 以像素为单位设置效果影响范围的高度.
     */
    public void setSize(int width, int height) {
        mWidth = width;
        mHeight = height;
    }

    /**
     * Set the position of this edge effect in pixels. This position is
     * only used by {@link #getBounds(boolean)}.
     * 
     * @param x The position of the edge effect on the X axis
     * @param y The position of the edge effect on the Y axis
     */
    void setPosition(int x, int y) {
        mX = x;
        mY = y;
    }

    /**
     * 报告这个 EdgeEffect 动画是否已经完成.如果在调用 {@link #draw(Canvas)} 后，本方法返回假，
     * 小部件应该稍后再绘制其他内容，让该动画继续.
     *
     * @return 如果动画已经完成返回真，否则如果应该继续着返回假.
     */
    public boolean isFinished() {
        return mState == STATE_IDLE;
    }

    /**
     * 立即停止当前动画. 调用该函数后，{@link #isFinished()} 返回真.
     */
    public void finish() {
        mState = STATE_IDLE;
    }

    /**
     * 当用户将内容从边界拉出视图时调用该函数. 该函数更新当前视觉状态，并关联动画。
     * 宿主视图应该在调用该函数后执行 {@link android.view.View#invalidate()}，
     * 来根据设置绘制视图。
     *
     * @param deltaDistance 相对上次调用变更的距离. 值在 0(无变化)到1.0(整个视图)之间，
     *                      负值代表效果的起始边到边界的距离.
     */
    public void onPull(float deltaDistance) {
        final long now = AnimationUtils.currentAnimationTimeMillis();
        if (mState == STATE_PULL_DECAY && now - mStartTime < mDuration) {
            return;
        }
        if (mState != STATE_PULL) {
            mGlowScaleY = PULL_GLOW_BEGIN;
        }
        mState = STATE_PULL;

        mStartTime = now;
        mDuration = PULL_TIME;

        mPullDistance += deltaDistance;
        float distance = Math.abs(mPullDistance);

        mEdgeAlpha = mEdgeAlphaStart = Math.max(PULL_EDGE_BEGIN, Math.min(distance, MAX_ALPHA));
        mEdgeScaleY = mEdgeScaleYStart = Math.max(
                HELD_EDGE_SCALE_Y, Math.min(distance * PULL_DISTANCE_EDGE_FACTOR, 1.f));

        mGlowAlpha = mGlowAlphaStart = Math.min(MAX_ALPHA,
                mGlowAlpha +
                (Math.abs(deltaDistance) * PULL_DISTANCE_ALPHA_GLOW_FACTOR));

        float glowChange = Math.abs(deltaDistance);
        if (deltaDistance > 0 && mPullDistance < 0) {
            glowChange = -glowChange;
        }
        if (mPullDistance == 0) {
            mGlowScaleY = 0;
        }

        // Do not allow glow to get larger than MAX_GLOW_HEIGHT.
        mGlowScaleY = mGlowScaleYStart = Math.min(MAX_GLOW_HEIGHT, Math.max(
                0, mGlowScaleY + glowChange * PULL_DISTANCE_GLOW_FACTOR));

        mEdgeAlphaFinish = mEdgeAlpha;
        mEdgeScaleYFinish = mEdgeScaleY;
        mGlowAlphaFinish = mGlowAlpha;
        mGlowScaleYFinish = mGlowScaleY;
    }

    /**
     * 释放拉动对象时调用. 该操作会启动“衰变”效果。调用该方法后，宿主视图应该调用
     * {@link android.view.View#invalidate()} 以根据结果绘制视图。
     */
    public void onRelease() {
        mPullDistance = 0;

        if (mState != STATE_PULL && mState != STATE_PULL_DECAY) {
            return;
        }

        mState = STATE_RECEDE;
        mEdgeAlphaStart = mEdgeAlpha;
        mEdgeScaleYStart = mEdgeScaleY;
        mGlowAlphaStart = mGlowAlpha;
        mGlowScaleYStart = mGlowScaleY;

        mEdgeAlphaFinish = 0.f;
        mEdgeScaleYFinish = 0.f;
        mGlowAlphaFinish = 0.f;
        mGlowScaleYFinish = 0.f;

        mStartTime = AnimationUtils.currentAnimationTimeMillis();
        mDuration = RECEDE_TIME;
    }

    /**
     * 当效果抵消给定速度的影响时调用. 在快速滑动到达滚动边界时使用。
     *
     * <p>当使用 {@link android.widget.Scroller} 或 {@link android.widget.OverScroller} 时，
     * 方法 <code>getCurrVelocity</code> 可以提供用在这里的合理的近似值。</p>
     *
     * @param velocity 速度的影响，每秒的像素数
     */
    public void onAbsorb(int velocity) {
        mState = STATE_ABSORB;
        velocity = Math.max(MIN_VELOCITY, Math.abs(velocity));

        mStartTime = AnimationUtils.currentAnimationTimeMillis();
        mDuration = 0.1f + (velocity * 0.03f);

        // The edge should always be at least partially visible, regardless
        // of velocity.
        mEdgeAlphaStart = 0.f;
        mEdgeScaleY = mEdgeScaleYStart = 0.f;
        // The glow depends more on the velocity, and therefore starts out
        // nearly invisible.
        mGlowAlphaStart = 0.5f;
        mGlowScaleYStart = 0.f;

        // Factor the velocity by 8. Testing on device shows this works best to
        // reflect the strength of the user's scrolling.
        mEdgeAlphaFinish = Math.max(0, Math.min(velocity * VELOCITY_EDGE_FACTOR, 1));
        // Edge should never get larger than the size of its asset.
        mEdgeScaleYFinish = Math.max(
                HELD_EDGE_SCALE_Y, Math.min(velocity * VELOCITY_EDGE_FACTOR, 1.f));

        // Growth for the size of the glow should be quadratic to properly
        // respond
        // to a user's scrolling speed. The faster the scrolling speed, the more
        // intense the effect should be for both the size and the saturation.
        mGlowScaleYFinish = Math.min(0.025f + (velocity * (velocity / 100) * 0.00015f), 1.75f);
        // Alpha should change for the glow as well as size.
        mGlowAlphaFinish = Math.max(
                mGlowAlphaStart, Math.min(velocity * VELOCITY_GLOW_FACTOR * .00001f, MAX_ALPHA));
    }


    /**
     * 在给定画布上绘图. 假设画布已经旋转完毕，并设置了大小。效果会全宽绘制，从X=0 到 X=width，
     * 高度则从 Y=0 到 某个小于 1.0 的倍率。
     *
     * @param canvas 用于绘制的画布
     * @return 如果动画在该帧结束后要继续绘制则返回真。
     */
    public boolean draw(Canvas canvas) {
        update();

        mGlow.setAlpha((int) (Math.max(0, Math.min(mGlowAlpha, 1)) * 255));

        int glowBottom = (int) Math.min(
                mGlowHeight * mGlowScaleY * mGlowHeight / mGlowWidth * 0.6f,
                mGlowHeight * MAX_GLOW_HEIGHT);
        if (mWidth < mMinWidth) {
            // Center the glow and clip it.
            int glowLeft = (mWidth - mMinWidth)/2;
            mGlow.setBounds(glowLeft, 0, mWidth - glowLeft, glowBottom);
        } else {
            // Stretch the glow to fit.
            mGlow.setBounds(0, 0, mWidth, glowBottom);
        }

        mGlow.draw(canvas);

        mEdge.setAlpha((int) (Math.max(0, Math.min(mEdgeAlpha, 1)) * 255));

        int edgeBottom = (int) (mEdgeHeight * mEdgeScaleY);
        if (mWidth < mMinWidth) {
            // Center the edge and clip it.
            int edgeLeft = (mWidth - mMinWidth)/2;
            mEdge.setBounds(edgeLeft, 0, mWidth - edgeLeft, edgeBottom);
        } else {
            // Stretch the edge to fit.
            mEdge.setBounds(0, 0, mWidth, edgeBottom);
        }
        mEdge.draw(canvas);

        if (mState == STATE_RECEDE && glowBottom == 0 && edgeBottom == 0) {
            mState = STATE_IDLE;
        }

        return mState != STATE_IDLE;
    }

    /**
     * Returns the bounds of the edge effect.
     * 
     * @hide
     */
    public Rect getBounds(boolean reverse) {
        mBounds.set(0, 0, mWidth, mMaxEffectHeight);
        mBounds.offset(mX, mY - (reverse ? mMaxEffectHeight : 0));

        return mBounds;
    }

    private void update() {
        final long time = AnimationUtils.currentAnimationTimeMillis();
        final float t = Math.min((time - mStartTime) / mDuration, 1.f);

        final float interp = mInterpolator.getInterpolation(t);

        mEdgeAlpha = mEdgeAlphaStart + (mEdgeAlphaFinish - mEdgeAlphaStart) * interp;
        mEdgeScaleY = mEdgeScaleYStart + (mEdgeScaleYFinish - mEdgeScaleYStart) * interp;
        mGlowAlpha = mGlowAlphaStart + (mGlowAlphaFinish - mGlowAlphaStart) * interp;
        mGlowScaleY = mGlowScaleYStart + (mGlowScaleYFinish - mGlowScaleYStart) * interp;

        if (t >= 1.f - EPSILON) {
            switch (mState) {
                case STATE_ABSORB:
                    mState = STATE_RECEDE;
                    mStartTime = AnimationUtils.currentAnimationTimeMillis();
                    mDuration = RECEDE_TIME;

                    mEdgeAlphaStart = mEdgeAlpha;
                    mEdgeScaleYStart = mEdgeScaleY;
                    mGlowAlphaStart = mGlowAlpha;
                    mGlowScaleYStart = mGlowScaleY;

                    // After absorb, the glow and edge should fade to nothing.
                    mEdgeAlphaFinish = 0.f;
                    mEdgeScaleYFinish = 0.f;
                    mGlowAlphaFinish = 0.f;
                    mGlowScaleYFinish = 0.f;
                    break;
                case STATE_PULL:
                    mState = STATE_PULL_DECAY;
                    mStartTime = AnimationUtils.currentAnimationTimeMillis();
                    mDuration = PULL_DECAY_TIME;

                    mEdgeAlphaStart = mEdgeAlpha;
                    mEdgeScaleYStart = mEdgeScaleY;
                    mGlowAlphaStart = mGlowAlpha;
                    mGlowScaleYStart = mGlowScaleY;

                    // After pull, the glow and edge should fade to nothing.
                    mEdgeAlphaFinish = 0.f;
                    mEdgeScaleYFinish = 0.f;
                    mGlowAlphaFinish = 0.f;
                    mGlowScaleYFinish = 0.f;
                    break;
                case STATE_PULL_DECAY:
                    // When receding, we want edge to decrease more slowly
                    // than the glow.
                    float factor = mGlowScaleYFinish != 0 ? 1
                            / (mGlowScaleYFinish * mGlowScaleYFinish)
                            : Float.MAX_VALUE;
                    mEdgeScaleY = mEdgeScaleYStart +
                        (mEdgeScaleYFinish - mEdgeScaleYStart) *
                            interp * factor;
                    mState = STATE_RECEDE;
                    break;
                case STATE_RECEDE:
                    mState = STATE_IDLE;
                    break;
            }
        }
    }
}
