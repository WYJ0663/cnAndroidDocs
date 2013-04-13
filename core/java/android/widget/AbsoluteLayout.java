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
import android.widget.RemoteViews.RemoteView;


/**
 * 让你指定其子元素的精确位置（x/y坐标）的布局.绝对布局缺乏灵活性，
 * 相比其他不指定绝对位置的布局更难于维护。
 *
 * <p><strong>XML 属性</strong></p> <p> 参见 {@link
 * android.R.styleable#ViewGroup ViewGroup 属性} 和 {@link
 * android.R.styleable#View View >属性}。</p>
 * 
 * @deprecated 用 {@link android.widget.FrameLayout}、{@link android.widget.RelativeLayout}
 *             或者自定义布局代替。
 * @author translate by madgoat
 * @author translate by 绵白糖
 * @author review by cnmahj
 * @author convert by cnmahj
 */
@Deprecated
@RemoteView
public class AbsoluteLayout extends ViewGroup {
    public AbsoluteLayout(Context context) {
        super(context);
    }

    public AbsoluteLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public AbsoluteLayout(Context context, AttributeSet attrs,
            int defStyle) {
        super(context, attrs, defStyle);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int count = getChildCount();

        int maxHeight = 0;
        int maxWidth = 0;

        // Find out how big everyone wants to be
        measureChildren(widthMeasureSpec, heightMeasureSpec);

        // Find rightmost and bottom-most child
        for (int i = 0; i < count; i++) {
            View child = getChildAt(i);
            if (child.getVisibility() != GONE) {
                int childRight;
                int childBottom;

                AbsoluteLayout.LayoutParams lp
                        = (AbsoluteLayout.LayoutParams) child.getLayoutParams();

                childRight = lp.x + child.getMeasuredWidth();
                childBottom = lp.y + child.getMeasuredHeight();

                maxWidth = Math.max(maxWidth, childRight);
                maxHeight = Math.max(maxHeight, childBottom);
            }
        }

        // Account for padding too
        maxWidth += mPaddingLeft + mPaddingRight;
        maxHeight += mPaddingTop + mPaddingBottom;

        // Check against minimum height and width
        maxHeight = Math.max(maxHeight, getSuggestedMinimumHeight());
        maxWidth = Math.max(maxWidth, getSuggestedMinimumWidth());
        
        setMeasuredDimension(resolveSizeAndState(maxWidth, widthMeasureSpec, 0),
                resolveSizeAndState(maxHeight, heightMeasureSpec, 0));
    }

    /**
     * 返回一组宽度为 {@link android.view.ViewGroup.LayoutParams#WRAP_CONTENT}、
     * 高度为 {@link android.view.ViewGroup.LayoutParams#WRAP_CONTENT}、
     * 坐标是（0，0）的布局参数。
     */
    @Override
    protected ViewGroup.LayoutParams generateDefaultLayoutParams() {
        return new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT, 0, 0);
    }

    @Override
    protected void onLayout(boolean changed, int l, int t,
            int r, int b) {
        int count = getChildCount();

        for (int i = 0; i < count; i++) {
            View child = getChildAt(i);
            if (child.getVisibility() != GONE) {

                AbsoluteLayout.LayoutParams lp =
                        (AbsoluteLayout.LayoutParams) child.getLayoutParams();

                int childLeft = mPaddingLeft + lp.x;
                int childTop = mPaddingTop + lp.y;
                child.layout(childLeft, childTop,
                        childLeft + child.getMeasuredWidth(),
                        childTop + child.getMeasuredHeight());

            }
        }
    }

    @Override
    public ViewGroup.LayoutParams generateLayoutParams(AttributeSet attrs) {
        return new AbsoluteLayout.LayoutParams(getContext(), attrs);
    }

    // Override to allow type-checking of LayoutParams. 
    @Override
    protected boolean checkLayoutParams(ViewGroup.LayoutParams p) {
        return p instanceof AbsoluteLayout.LayoutParams;
    }

    @Override
    protected ViewGroup.LayoutParams generateLayoutParams(ViewGroup.LayoutParams p) {
        return new LayoutParams(p);
    }

    @Override
    public boolean shouldDelayChildPressedState() {
        return false;
    }

    /**
     * 与 AbsoluteLayout 关联的子元素的布局信息.参见绝对布局属性中该类所支持的子视图属性列表。
     * 该类支持的所有子视图属性列表，参见
     * {@link android.R.styleable#AbsoluteLayout_Layout 绝对布局属性}。
     */
    public static class LayoutParams extends ViewGroup.LayoutParams {
        /**
         * 视图组中子视图的横（X）坐标。
         */
        public int x;
        /**
         * 视图组中子视图的纵（Y）坐标。
         */
        public int y;

        /**
         * 根据指定的宽度、高度和位置创建新的布局参数集合。
         *
         * @param width {@link #MATCH_PARENT}、{@link #WRAP_CONTENT} 
         *              或者已像素为单位的宽度
         * @param height {@link #MATCH_PARENT}、{@link #WRAP_CONTENT}
         *              或者已像素为单位的高度
         * @param x 子视图的横（X）坐标
         * @param y 子视图的纵（Y）坐标
         */
        public LayoutParams(int width, int height, int x, int y) {
            super(width, height);
            this.x = x;
            this.y = y;
        }

        /**
         * 创建新的布局参数集合.其值来自于提供的上下文和属性集合。
         * XML 属性映射关系如下：
         *
         * <ul>
         *   <li><code>layout_x</code>：子视图的横（X）坐标。</li>
         *   <li><code>layout_y</code>：子视图的纵（Y）坐标。</li>
         *   <li>所有来自 {@link android.view.ViewGroup.LayoutParams} 的 XML 属性。</li>
         * </ul>
         *
         * @param c 应用程序上下文
         * @param attrs 用于提取布局属性值的属性集合
         */
        public LayoutParams(Context c, AttributeSet attrs) {
            super(c, attrs);
            TypedArray a = c.obtainStyledAttributes(attrs,
                    com.android.internal.R.styleable.AbsoluteLayout_Layout);
            x = a.getDimensionPixelOffset(
                    com.android.internal.R.styleable.AbsoluteLayout_Layout_layout_x, 0);
            y = a.getDimensionPixelOffset(
                    com.android.internal.R.styleable.AbsoluteLayout_Layout_layout_y, 0);
            a.recycle();
        }

        /**
         * {@inheritDoc}
         */
        public LayoutParams(ViewGroup.LayoutParams source) {
            super(source);
        }

        @Override
        public String debug(String output) {
            return output + "Absolute.LayoutParams={width="
                    + sizeToString(width) + ", height=" + sizeToString(height)
                    + " x=" + x + " y=" + y + "}";
        }
    }
}


