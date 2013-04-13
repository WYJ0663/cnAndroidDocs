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

import com.android.internal.R;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.ViewDebug;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityNodeInfo;


/**
 * 对 TextView 的扩展，支持 {@link android.widget.Checkable} 接口.
 * 对于将 {@link android.widget.ListView#setChoiceMode(int) setChoiceMode}
 * 设置为 {@link android.widget.ListView#CHOICE_MODE_NONE CHOICE_MODE_NONE}
 * 以外的值的 {@link android.widget.ListView ListView} 是非常有用的.
 *
 * @attr ref android.R.styleable#CheckedTextView_checked
 * @attr ref android.R.styleable#CheckedTextView_checkMark
 * @author translate by 小易
 * @author review by cnmahj
 * @author convert by cnmahj
 */
public class CheckedTextView extends TextView implements Checkable {
    private boolean mChecked;
    private int mCheckMarkResource;
    private Drawable mCheckMarkDrawable;
    private int mBasePadding;
    private int mCheckMarkWidth;
    private boolean mNeedRequestlayout;

    private static final int[] CHECKED_STATE_SET = {
        R.attr.state_checked
    };

    public CheckedTextView(Context context) {
        this(context, null);
    }

    public CheckedTextView(Context context, AttributeSet attrs) {
        this(context, attrs, R.attr.checkedTextViewStyle);
    }

    public CheckedTextView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);

        TypedArray a = context.obtainStyledAttributes(attrs,
                R.styleable.CheckedTextView, defStyle, 0);

        Drawable d = a.getDrawable(R.styleable.CheckedTextView_checkMark);
        if (d != null) {
            setCheckMarkDrawable(d);
        }

        boolean checked = a.getBoolean(R.styleable.CheckedTextView_checked, false);
        setChecked(checked);

        a.recycle();
    }

    public void toggle() {
        setChecked(!mChecked);
    }

    @ViewDebug.ExportedProperty
    public boolean isChecked() {
        return mChecked;
    }

    /**
     * <p>改变文本视图的选中状态.</p>	
     *
     * @param checked 选中文本返回 true，未选中返回 false.
     */
    public void setChecked(boolean checked) {
        if (mChecked != checked) {
            mChecked = checked;
            refreshDrawableState();
            notifyAccessibilityStateChanged();
        }
    }


    /**
     * 将指定的资源 ID 对应的可绘制对象设置为选中标记.
     * 当 {@link #isChecked()} 为 true 时绘制该标记.
     * 
     * @param resid 作为选中标记的可绘制对象的资源 ID.
     *
     * @see #setCheckMarkDrawable(Drawable)
     * @see #getCheckMarkDrawable()
     *
     * @attr ref android.R.styleable#CheckedTextView_checkMark
     */
    public void setCheckMarkDrawable(int resid) {
        if (resid != 0 && resid == mCheckMarkResource) {
            return;
        }

        mCheckMarkResource = resid;

        Drawable d = null;
        if (mCheckMarkResource != 0) {
            d = getResources().getDrawable(mCheckMarkResource);
        }
        setCheckMarkDrawable(d);
    }

    /**
     * 将指定的可绘制对象设置为选中标记.
     * 当 {@link #isChecked()} 为 true 时绘制该标记.
     * @param d 作为选中标记的可绘制对象.
     *
     * @see #setCheckMarkDrawable(int)
     * @see #getCheckMarkDrawable()
     *
     * @attr ref android.R.styleable#CheckedTextView_checkMark
     */
    public void setCheckMarkDrawable(Drawable d) {
        if (mCheckMarkDrawable != null) {
            mCheckMarkDrawable.setCallback(null);
            unscheduleDrawable(mCheckMarkDrawable);
        }
        mNeedRequestlayout = (d != mCheckMarkDrawable);
        if (d != null) {
            d.setCallback(this);
            d.setVisible(getVisibility() == VISIBLE, false);
            d.setState(CHECKED_STATE_SET);
            setMinHeight(d.getIntrinsicHeight());

            mCheckMarkWidth = d.getIntrinsicWidth();
            d.setState(getDrawableState());
        } else {
            mCheckMarkWidth = 0;
        }
        mCheckMarkDrawable = d;
        // Do padding resolution. This will call internalSetPadding() and do a requestLayout() if needed.
        resolvePadding();
    }

    /**
     * Gets the checkmark drawable
     *
     * @return The drawable use to represent the checkmark, if any.
     *
     * @see #setCheckMarkDrawable(Drawable)
     * @see #setCheckMarkDrawable(int)
     *
     * @attr ref android.R.styleable#CheckedTextView_checkMark
     */
    public Drawable getCheckMarkDrawable() {
        return mCheckMarkDrawable;
    }

    /**
     * @hide
     */
    @Override
    protected void internalSetPadding(int left, int top, int right, int bottom) {
        super.internalSetPadding(left, top, right, bottom);
        setBasePadding(isLayoutRtl());
    }

    @Override
    public void onRtlPropertiesChanged(int layoutDirection) {
        super.onRtlPropertiesChanged(layoutDirection);
        updatePadding();
    }

    private void updatePadding() {
        resetPaddingToInitialValues();
        int newPadding = (mCheckMarkDrawable != null) ?
                mCheckMarkWidth + mBasePadding : mBasePadding;
        if (isLayoutRtl()) {
            mNeedRequestlayout |= (mPaddingLeft != newPadding);
            mPaddingLeft = newPadding;
        } else {
            mNeedRequestlayout |= (mPaddingRight != newPadding);
            mPaddingRight = newPadding;
        }
        if (mNeedRequestlayout) {
            requestLayout();
            mNeedRequestlayout = false;
        }
    }

    private void setBasePadding(boolean isLayoutRtl) {
        if (isLayoutRtl) {
            mBasePadding = mPaddingLeft;
        } else {
            mBasePadding = mPaddingRight;
        }
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        final Drawable checkMarkDrawable = mCheckMarkDrawable;
        if (checkMarkDrawable != null) {
            final int verticalGravity = getGravity() & Gravity.VERTICAL_GRAVITY_MASK;
            final int height = checkMarkDrawable.getIntrinsicHeight();

            int y = 0;

            switch (verticalGravity) {
                case Gravity.BOTTOM:
                    y = getHeight() - height;
                    break;
                case Gravity.CENTER_VERTICAL:
                    y = (getHeight() - height) / 2;
                    break;
            }
            
            final boolean isLayoutRtl = isLayoutRtl();
            final int width = getWidth();
            final int top = y;
            final int bottom = top + height;
            final int left;
            final int right;
            if (isLayoutRtl) {
                left = mBasePadding;
                right = left + mCheckMarkWidth;
            } else {
                right = width - mBasePadding;
                left = right - mCheckMarkWidth;
            }
            checkMarkDrawable.setBounds( left, top, right, bottom);
            checkMarkDrawable.draw(canvas);
        }
    }
    
    @Override
    protected int[] onCreateDrawableState(int extraSpace) {
        final int[] drawableState = super.onCreateDrawableState(extraSpace + 1);
        if (isChecked()) {
            mergeDrawableStates(drawableState, CHECKED_STATE_SET);
        }
        return drawableState;
    }

    @Override
    protected void drawableStateChanged() {
        super.drawableStateChanged();
        
        if (mCheckMarkDrawable != null) {
            int[] myDrawableState = getDrawableState();
            
            // Set the state of the Drawable
            mCheckMarkDrawable.setState(myDrawableState);
            
            invalidate();
        }
    }

    @Override
    public void onInitializeAccessibilityEvent(AccessibilityEvent event) {
        super.onInitializeAccessibilityEvent(event);
        event.setClassName(CheckedTextView.class.getName());
        event.setChecked(mChecked);
    }

    @Override
    public void onInitializeAccessibilityNodeInfo(AccessibilityNodeInfo info) {
        super.onInitializeAccessibilityNodeInfo(info);
        info.setClassName(CheckedTextView.class.getName());
        info.setCheckable(true);
        info.setChecked(mChecked);
    }
}
