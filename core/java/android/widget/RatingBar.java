/*
 * Copyright (C) 2007 The Android Open Source Project
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
import android.graphics.drawable.shapes.RectShape;
import android.graphics.drawable.shapes.Shape;
import android.util.AttributeSet;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityNodeInfo;

import com.android.internal.R;

/**
 * RatingBar 是基于 SeekBar 和 ProgressBar 的扩展，用星型来显示等级评定.
 * 使用 RatingBar 的默认大小时，用户可以触摸、拖动或使用方向键来设置评分.
 * 它有小 RatingBar 样式（{@link android.R.attr#ratingBarStyleSmall}）
 * 和大的（{@link android.R.attr#ratingBarStyleIndicator}）只用于显示的两种样式.
 * 大的样式不支持用户交互，仅能用于显示.
 * <p>
 * 当使用可以支持用户交互的 RatingBar 时，无论将小部件放在它的左边还是右边都是不合适的.
 * <p>
 * 只有当布局的宽被设置为“<code>wrap content</code>”时，设置的星星数量
 * （通过函数 {@link #setNumStars(int)} 或者在 XML 布局文件中定义）将显示出来
 * （如果宽度设置为其他布局模式，结果不可预知）.
 * <p>
 * 次级进度一般不应该被修改，因为他仅仅是被当作星型部分内部的填充背景.
 * 
 * @attr ref android.R.styleable#RatingBar_numStars
 * @attr ref android.R.styleable#RatingBar_rating
 * @attr ref android.R.styleable#RatingBar_stepSize
 * @attr ref android.R.styleable#RatingBar_isIndicator
 * @author translate by wallace2010
 * @author translate by madgoat
 * @author convert by cnmahj
 */
public class RatingBar extends AbsSeekBar {

    /**
     * 当评分等级改变时通知客户端的回调函数.
     * 这包括用户通过手势、方向键或轨迹球触发的改变，以及编程触发的改变.
     */
    public interface OnRatingBarChangeListener {
        
        /**
         * 通知评分等级已经被修改.
         * 客户端可以使用 fromUser 参数区分用户触发的改变还是编程触发的改变.
         * 当用户拖拽时，将不会连续不断的被调用，仅仅当用户最终离开触摸结束评分时调用.
         * 
         * @param ratingBar 评分修改的 RatingBar.
         * @param rating 当前评分分数.取值范围为0到星型的数量.
         * @param fromUser 如果评分改变是由用户触摸手势、方向键或轨迹球移动触发的，则返回 true.
         */
        void onRatingChanged(RatingBar ratingBar, float rating, boolean fromUser);

    }

    private int mNumStars = 5;

    private int mProgressOnStartTracking;
    
    private OnRatingBarChangeListener mOnRatingBarChangeListener;
    
    public RatingBar(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        
        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.RatingBar,
                defStyle, 0);
        final int numStars = a.getInt(R.styleable.RatingBar_numStars, mNumStars);
        setIsIndicator(a.getBoolean(R.styleable.RatingBar_isIndicator, !mIsUserSeekable));
        final float rating = a.getFloat(R.styleable.RatingBar_rating, -1);
        final float stepSize = a.getFloat(R.styleable.RatingBar_stepSize, -1);
        a.recycle();

        if (numStars > 0 && numStars != mNumStars) {
            setNumStars(numStars);            
        }
        
        if (stepSize >= 0) {
            setStepSize(stepSize);
        } else {
            setStepSize(0.5f);
        }
        
        if (rating >= 0) {
            setRating(rating);
        }
        
        // A touch inside a star fill up to that fractional area (slightly more
        // than 1 so boundaries round up).
        mTouchProgressOffset = 1.1f;
    }

    public RatingBar(Context context, AttributeSet attrs) {
        this(context, attrs, com.android.internal.R.attr.ratingBarStyle);
    }

    public RatingBar(Context context) {
        this(context, null);
    }
    
    /**
     * 设置当评分等级发生改变时回调的监听器.
     * 
     * @param listener 监听器.
     */
    public void setOnRatingBarChangeListener(OnRatingBarChangeListener listener) {
        mOnRatingBarChangeListener = listener;
    }
    
    /**
     * @return 监听评分改变事件的监听器（可能为空）.
     */
    public OnRatingBarChangeListener getOnRatingBarChangeListener() {
        return mOnRatingBarChangeListener;
    }

    /**
     * 设置当前的评分条是否仅仅是个指示器（这样用户就不能进行修改操作了）
     * 
     * @param isIndicator 是否是一个指示器.
     *
     * @attr ref android.R.styleable#RatingBar_isIndicator
     */
    public void setIsIndicator(boolean isIndicator) {
        mIsUserSeekable = !isIndicator;
        setFocusable(!isIndicator);
    }
    
    /**
     * @return 判断当前的评分条是否仅仅是一个指示器（注：即能否被修改）.
     *
     * @attr ref android.R.styleable#RatingBar_isIndicator
     */
    public boolean isIndicator() {
        return !mIsUserSeekable;
    }
    
    /**
     * 设置显示的星型的数量.为了能够正常显示它们，建议将当前小部件的布局宽度设置为
     * “<code>wrap content</code>”.
     * 
     * @param numStars 星型的数量.
     */
    public void setNumStars(final int numStars) {
        if (numStars <= 0) {
            return;
        }
        
        mNumStars = numStars;
        
        // This causes the width to change, so re-layout
        requestLayout();
    }

    /**
     * 返回显示的星型数量.
     * @return 显示的星型数量.
     */
    public int getNumStars() {
        return mNumStars;
    }
    
    /**
     * 设置分数（星型的数量）.
     * 
     * @param rating 设置的分数.
     */
    public void setRating(float rating) {
        setProgress(Math.round(rating * getProgressPerStar()));
    }

    /**
     * 获取当前的评分（填充的星型的数量）.
     * 
     * @return 当前的评分.
     */
    public float getRating() {
        return getProgress() / getProgressPerStar();        
    }

    /**
     * 设置当前评分条的步长（粒度）.
     * 
     * @param stepSize 评分条的步进.例如：如果想要半个星星的粒度，则它的值为 0.5.
     */
    public void setStepSize(float stepSize) {
        if (stepSize <= 0) {
            return;
        }
        
        final float newMax = mNumStars / stepSize;
        final int newProgress = (int) (newMax / getMax() * getProgress());
        setMax((int) newMax);
        setProgress(newProgress);
    }

    /**
     * 获取评分条的步长.
     * 
     * @return 步长.
     */
    public float getStepSize() {
        return (float) getNumStars() / getMax();
    }
    
    /**
     * @return The amount of progress that fits into a star
     */
    private float getProgressPerStar() {
        if (mNumStars > 0) {
            return 1f * getMax() / mNumStars;
        } else {
            return 1;
        }
    }

    @Override
    Shape getDrawableShape() {
        // TODO: Once ProgressBar's TODOs are fixed, this won't be needed
        return new RectShape();
    }

    @Override
    void onProgressRefresh(float scale, boolean fromUser) {
        super.onProgressRefresh(scale, fromUser);

        // Keep secondary progress in sync with primary
        updateSecondaryProgress(getProgress());
        
        if (!fromUser) {
            // Callback for non-user rating changes
            dispatchRatingChange(false);
        }
    }

    /**
     * The secondary progress is used to differentiate the background of a
     * partially filled star. This method keeps the secondary progress in sync
     * with the progress.
     * 
     * @param progress The primary progress level.
     */
    private void updateSecondaryProgress(int progress) {
        final float ratio = getProgressPerStar();
        if (ratio > 0) {
            final float progressInStars = progress / ratio;
            final int secondaryProgress = (int) (Math.ceil(progressInStars) * ratio);
            setSecondaryProgress(secondaryProgress);
        }
    }
    
    @Override
    protected synchronized void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        
        if (mSampleTile != null) {
            // TODO: Once ProgressBar's TODOs are gone, this can be done more
            // cleanly than mSampleTile
            final int width = mSampleTile.getWidth() * mNumStars;
            setMeasuredDimension(resolveSizeAndState(width, widthMeasureSpec, 0),
                    getMeasuredHeight());
        }
    }

    @Override
    void onStartTrackingTouch() {
        mProgressOnStartTracking = getProgress();
        
        super.onStartTrackingTouch();
    }

    @Override
    void onStopTrackingTouch() {
        super.onStopTrackingTouch();

        if (getProgress() != mProgressOnStartTracking) {
            dispatchRatingChange(true);
        }
    }

    @Override
    void onKeyChange() {
        super.onKeyChange();
        dispatchRatingChange(true);
    }

    void dispatchRatingChange(boolean fromUser) {
        if (mOnRatingBarChangeListener != null) {
            mOnRatingBarChangeListener.onRatingChanged(this, getRating(),
                    fromUser);
        }
    }

    @Override
    public synchronized void setMax(int max) {
        // Disallow max progress = 0
        if (max <= 0) {
            return;
        }
        
        super.setMax(max);
    }
    
    @Override
    public void onInitializeAccessibilityEvent(AccessibilityEvent event) {
        super.onInitializeAccessibilityEvent(event);
        event.setClassName(RatingBar.class.getName());
    }

    @Override
    public void onInitializeAccessibilityNodeInfo(AccessibilityNodeInfo info) {
        super.onInitializeAccessibilityNodeInfo(info);
        info.setClassName(RatingBar.class.getName());
    }
}
