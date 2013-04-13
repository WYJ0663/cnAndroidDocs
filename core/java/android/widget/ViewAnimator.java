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
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityNodeInfo;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;

/**
 * {@link FrameLayout} 容器类的基类，用于在变换视图时显示动画效果.
 *
 * @attr ref android.R.styleable#ViewAnimator_inAnimation
 * @attr ref android.R.styleable#ViewAnimator_outAnimation
 * @attr ref android.R.styleable#ViewAnimator_animateFirstView
 * @author translate by cnmahj
 */
public class ViewAnimator extends FrameLayout {

    int mWhichChild = 0;
    boolean mFirstTime = true;

    boolean mAnimateFirstTime = true;

    Animation mInAnimation;
    Animation mOutAnimation;

    public ViewAnimator(Context context) {
        super(context);
        initViewAnimator(context, null);
    }

    public ViewAnimator(Context context, AttributeSet attrs) {
        super(context, attrs);

        TypedArray a = context.obtainStyledAttributes(attrs, com.android.internal.R.styleable.ViewAnimator);
        int resource = a.getResourceId(com.android.internal.R.styleable.ViewAnimator_inAnimation, 0);
        if (resource > 0) {
            setInAnimation(context, resource);
        }

        resource = a.getResourceId(com.android.internal.R.styleable.ViewAnimator_outAnimation, 0);
        if (resource > 0) {
            setOutAnimation(context, resource);
        }

        boolean flag = a.getBoolean(com.android.internal.R.styleable.ViewAnimator_animateFirstView, true);
        setAnimateFirstView(flag);

        a.recycle();

        initViewAnimator(context, attrs);
    }

    /**
     * Initialize this {@link ViewAnimator}, possibly setting
     * {@link #setMeasureAllChildren(boolean)} based on {@link FrameLayout} flags.
     */
    private void initViewAnimator(Context context, AttributeSet attrs) {
        if (attrs == null) {
            // For compatibility, always measure children when undefined.
            mMeasureAllChildren = true;
            return;
        }

        // For compatibility, default to measure children, but allow XML
        // attribute to override.
        final TypedArray a = context.obtainStyledAttributes(attrs,
                com.android.internal.R.styleable.FrameLayout);
        final boolean measureAllChildren = a.getBoolean(
                com.android.internal.R.styleable.FrameLayout_measureAllChildren, true);
        setMeasureAllChildren(measureAllChildren);
        a.recycle();
    }

    /**
     * 设置要显示的子视图.
     * 
     * @param whichChild 要显示的子视图的索引.
     */
    @android.view.RemotableViewMethod
    public void setDisplayedChild(int whichChild) {
        mWhichChild = whichChild;
        if (whichChild >= getChildCount()) {
            mWhichChild = 0;
        } else if (whichChild < 0) {
            mWhichChild = getChildCount() - 1;
        }
        boolean hasFocus = getFocusedChild() != null;
        // This will clear old focus if we had it
        showOnly(mWhichChild);
        if (hasFocus) {
            // Try to retake focus if we had it
            requestFocus(FOCUS_FORWARD);
        }
    }

    /**
     * 返回当前显示的子视图的索引.
     */
    public int getDisplayedChild() {
        return mWhichChild;
    }

    /**
     * 显示下一个视图.
     */
    @android.view.RemotableViewMethod
    public void showNext() {
        setDisplayedChild(mWhichChild + 1);
    }

    /**
     * 显示前一个视图.
     */
    @android.view.RemotableViewMethod
    public void showPrevious() {
        setDisplayedChild(mWhichChild - 1);
    }

    /**
     * Shows only the specified child. The other displays Views exit the screen,
     * optionally with the with the {@link #getOutAnimation() out animation} and
     * the specified child enters the screen, optionally with the
     * {@link #getInAnimation() in animation}.
     *
     * @param childIndex The index of the child to be shown.
     * @param animate Whether or not to use the in and out animations, defaults
     *            to true.
     */
    void showOnly(int childIndex, boolean animate) {
        final int count = getChildCount();
        for (int i = 0; i < count; i++) {
            final View child = getChildAt(i);
            if (i == childIndex) {
                if (animate && mInAnimation != null) {
                    child.startAnimation(mInAnimation);
                }
                child.setVisibility(View.VISIBLE);
                mFirstTime = false;
            } else {
                if (animate && mOutAnimation != null && child.getVisibility() == View.VISIBLE) {
                    child.startAnimation(mOutAnimation);
                } else if (child.getAnimation() == mInAnimation)
                    child.clearAnimation();
                child.setVisibility(View.GONE);
            }
        }
    }
    /**
     * Shows only the specified child. The other displays Views exit the screen
     * with the {@link #getOutAnimation() out animation} and the specified child
     * enters the screen with the {@link #getInAnimation() in animation}.
     *
     * @param childIndex The index of the child to be shown.
     */
    void showOnly(int childIndex) {
        final boolean animate = (!mFirstTime || mAnimateFirstTime);
        showOnly(childIndex, animate);
    }

    @Override
    public void addView(View child, int index, ViewGroup.LayoutParams params) {
        super.addView(child, index, params);
        if (getChildCount() == 1) {
            child.setVisibility(View.VISIBLE);
        } else {
            child.setVisibility(View.GONE);
        }
        if (index >= 0 && mWhichChild >= index) {
            // Added item above current one, increment the index of the displayed child
            setDisplayedChild(mWhichChild + 1);
        }
    }

    @Override
    public void removeAllViews() {
        super.removeAllViews();
        mWhichChild = 0;
        mFirstTime = true;
    }

    @Override
    public void removeView(View view) {
        final int index = indexOfChild(view);
        if (index >= 0) {
            removeViewAt(index);
        }
    }

    @Override
    public void removeViewAt(int index) {
        super.removeViewAt(index);
        final int childCount = getChildCount();
        if (childCount == 0) {
            mWhichChild = 0;
            mFirstTime = true;
        } else if (mWhichChild >= childCount) {
            // Displayed is above child count, so float down to top of stack
            setDisplayedChild(childCount - 1);
        } else if (mWhichChild == index) {
            // Displayed was removed, so show the new child living in its place
            setDisplayedChild(mWhichChild);
        }
    }

    public void removeViewInLayout(View view) {
        removeView(view);
    }

    public void removeViews(int start, int count) {
        super.removeViews(start, count);
        if (getChildCount() == 0) {
            mWhichChild = 0;
            mFirstTime = true;
        } else if (mWhichChild >= start && mWhichChild < start + count) {
            // Try showing new displayed child, wrapping if needed
            setDisplayedChild(mWhichChild);
        }
    }

    public void removeViewsInLayout(int start, int count) {
        removeViews(start, count);
    }

    /**
     * 返回当前显示的子视图.
     *
     * @return 当前显示的子视图.
     *
     * @see #getDisplayedChild()
     */
    public View getCurrentView() {
        return getChildAt(mWhichChild);
    }

    /**
     * 返回用于绘制视图进入屏幕时动画的动画对象.
     *
     * @return 动画对象；未设定时返回空.
     *
     * @see #setInAnimation(android.view.animation.Animation)
     * @see #setInAnimation(android.content.Context, int)
     */
    public Animation getInAnimation() {
        return mInAnimation;
    }

    /**
     * 设置用于绘制视图进入屏幕时动画的动画对象.
     *
     * @param inAnimation 视图进入屏幕时显示动画用的动画对象.
     *
     * @see #getInAnimation()
     * @see #setInAnimation(android.content.Context, int)
     */
    public void setInAnimation(Animation inAnimation) {
        mInAnimation = inAnimation;
    }

    /**
     * 返回用于绘制视图退出屏幕时动画的动画对象.
     *
     * @return 动画对象；未设定时返回空.
     *
     * @see #setOutAnimation(android.view.animation.Animation)
     * @see #setOutAnimation(android.content.Context, int)
     */
    public Animation getOutAnimation() {
        return mOutAnimation;
    }

    /**
     * 设置用于绘制视图退出屏幕时动画的动画对象.
     *
     * @param outAnimation 视图退出屏幕时显示动画用的动画对象.
     *
     * @see #getOutAnimation()
     * @see #setOutAnimation(android.content.Context, int)
     */
    public void setOutAnimation(Animation outAnimation) {
        mOutAnimation = outAnimation;
    }

    /**
     * 设置用于绘制视图进入屏幕时动画的动画对象.
     *
     * @param context 应用程序上下文.
     * @param resourceID 动画对象的资源 ID.
     *
     * @see #getInAnimation()
     * @see #setInAnimation(android.view.animation.Animation)
     */
    public void setInAnimation(Context context, int resourceID) {
        setInAnimation(AnimationUtils.loadAnimation(context, resourceID));
    }

    /**
     * 设置用于绘制视图退出屏幕时动画的动画对象.
     *
     * @param context 应用程序上下文.
     * @param resourceID 动画对象的资源 ID.
     *
     * @see #getOutAnimation()
     * @see #setOutAnimation(android.view.animation.Animation)
     */
    public void setOutAnimation(Context context, int resourceID) {
        setOutAnimation(AnimationUtils.loadAnimation(context, resourceID));
    }

    /**
     * Returns whether the current View should be animated the first time the ViewAnimator
     * is displayed.
     *
     * @return true if the current View will be animated the first time it is displayed,
     * false otherwise.
     *
     * @see #setAnimateFirstView(boolean)
     */
    public boolean getAnimateFirstView() {
        return mAnimateFirstTime;
    }

    /**
     * 设置视图动画对象首次显示时，是否为当前视图显示动画.
     *
     * @param animate 视图动画对象首次显示时为当前视图显示动画时为真；否则为假.
     */
    public void setAnimateFirstView(boolean animate) {
        mAnimateFirstTime = animate;
    }

    @Override
    public int getBaseline() {
        return (getCurrentView() != null) ? getCurrentView().getBaseline() : super.getBaseline();
    }

    @Override
    public void onInitializeAccessibilityEvent(AccessibilityEvent event) {
        super.onInitializeAccessibilityEvent(event);
        event.setClassName(ViewAnimator.class.getName());
    }

    @Override
    public void onInitializeAccessibilityNodeInfo(AccessibilityNodeInfo info) {
        super.onInitializeAccessibilityNodeInfo(info);
        info.setClassName(ViewAnimator.class.getName());
    }
}
