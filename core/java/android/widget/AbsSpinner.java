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

import com.android.internal.R;

import android.content.Context;
import android.content.res.TypedArray;
import android.database.DataSetObserver;
import android.graphics.Rect;
import android.os.Parcel;
import android.os.Parcelable;
import android.util.AttributeSet;
import android.util.SparseArray;
import android.view.View;
import android.view.ViewGroup;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityNodeInfo;

/**
 * spinner小部件的抽象基类.SDK 用户可能不需要使用该类.
 * 
 * @attr ref android.R.styleable#AbsSpinner_entries
 * @author translate by 思考的狼
 * @author translate by cnmahj
 * @author convert by cnmahj
 */
public abstract class AbsSpinner extends AdapterView<SpinnerAdapter> {
    SpinnerAdapter mAdapter;

    int mHeightMeasureSpec;
    int mWidthMeasureSpec;

    int mSelectionLeftPadding = 0;
    int mSelectionTopPadding = 0;
    int mSelectionRightPadding = 0;
    int mSelectionBottomPadding = 0;
    final Rect mSpinnerPadding = new Rect();

    final RecycleBin mRecycler = new RecycleBin();
    private DataSetObserver mDataSetObserver;

    /** Temporary frame to hold a child View's frame rectangle */
    private Rect mTouchFrame;

    public AbsSpinner(Context context) {
        super(context);
        initAbsSpinner();
    }

    public AbsSpinner(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public AbsSpinner(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        initAbsSpinner();

        TypedArray a = context.obtainStyledAttributes(attrs,
                com.android.internal.R.styleable.AbsSpinner, defStyle, 0);

        CharSequence[] entries = a.getTextArray(R.styleable.AbsSpinner_entries);
        if (entries != null) {
            ArrayAdapter<CharSequence> adapter =
                    new ArrayAdapter<CharSequence>(context,
                            R.layout.simple_spinner_item, entries);
            adapter.setDropDownViewResource(R.layout.simple_spinner_dropdown_item);
            setAdapter(adapter);
        }

        a.recycle();
    }

    /**
     * Common code for different constructor flavors
     */
    private void initAbsSpinner() {
        setFocusable(true);
        setWillNotDraw(false);
    }

    /**
     * 该适配器为 Spinner 提供后端数据.
     * 他还提供了基于选中条目的相对位置来改变 spinner 条目的方法.
     * @param adapter 用于 Spinner 的 SpinnerAdapter.
     */
    @Override
    public void setAdapter(SpinnerAdapter adapter) {
        if (null != mAdapter) {
            mAdapter.unregisterDataSetObserver(mDataSetObserver);
            resetList();
        }
        
        mAdapter = adapter;
        
        mOldSelectedPosition = INVALID_POSITION;
        mOldSelectedRowId = INVALID_ROW_ID;
        
        if (mAdapter != null) {
            mOldItemCount = mItemCount;
            mItemCount = mAdapter.getCount();
            checkFocus();

            mDataSetObserver = new AdapterDataSetObserver();
            mAdapter.registerDataSetObserver(mDataSetObserver);

            int position = mItemCount > 0 ? 0 : INVALID_POSITION;

            setSelectedPositionInt(position);
            setNextSelectedPositionInt(position);
            
            if (mItemCount == 0) {
                // Nothing selected
                checkSelectionChanged();
            }
            
        } else {
            checkFocus();            
            resetList();
            // Nothing selected
            checkSelectionChanged();
        }

        requestLayout();
    }

    /**
     * Clear out all children from the list
     */
    void resetList() {
        mDataChanged = false;
        mNeedSync = false;
        
        removeAllViewsInLayout();
        mOldSelectedPosition = INVALID_POSITION;
        mOldSelectedRowId = INVALID_ROW_ID;
        
        setSelectedPositionInt(INVALID_POSITION);
        setNextSelectedPositionInt(INVALID_POSITION);
        invalidate();
    }

    /** 
     * @see android.view.View#measure(int, int)
     * 
     * 计算 Spinner 的尺寸.宽度来自于 widthMeasureSpec 属性，当 Spinnner
     * 没有设置宽度时，返回 UNSPECIFIED；高度是在选中条目高度加上内边距.
     */
    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int widthMode = MeasureSpec.getMode(widthMeasureSpec);
        int widthSize;
        int heightSize;

        mSpinnerPadding.left = mPaddingLeft > mSelectionLeftPadding ? mPaddingLeft
                : mSelectionLeftPadding;
        mSpinnerPadding.top = mPaddingTop > mSelectionTopPadding ? mPaddingTop
                : mSelectionTopPadding;
        mSpinnerPadding.right = mPaddingRight > mSelectionRightPadding ? mPaddingRight
                : mSelectionRightPadding;
        mSpinnerPadding.bottom = mPaddingBottom > mSelectionBottomPadding ? mPaddingBottom
                : mSelectionBottomPadding;

        if (mDataChanged) {
            handleDataChanged();
        }
        
        int preferredHeight = 0;
        int preferredWidth = 0;
        boolean needsMeasuring = true;
        
        int selectedPosition = getSelectedItemPosition();
        if (selectedPosition >= 0 && mAdapter != null && selectedPosition < mAdapter.getCount()) {
            // Try looking in the recycler. (Maybe we were measured once already)
            View view = mRecycler.get(selectedPosition);
            if (view == null) {
                // Make a new one
                view = mAdapter.getView(selectedPosition, null, this);

                if (view.getImportantForAccessibility() == IMPORTANT_FOR_ACCESSIBILITY_AUTO) {
                    view.setImportantForAccessibility(IMPORTANT_FOR_ACCESSIBILITY_YES);
                }
            }

            if (view != null) {
                // Put in recycler for re-measuring and/or layout
                mRecycler.put(selectedPosition, view);
            }

            if (view != null) {
                if (view.getLayoutParams() == null) {
                    mBlockLayoutRequests = true;
                    view.setLayoutParams(generateDefaultLayoutParams());
                    mBlockLayoutRequests = false;
                }
                measureChild(view, widthMeasureSpec, heightMeasureSpec);
                
                preferredHeight = getChildHeight(view) + mSpinnerPadding.top + mSpinnerPadding.bottom;
                preferredWidth = getChildWidth(view) + mSpinnerPadding.left + mSpinnerPadding.right;
                
                needsMeasuring = false;
            }
        }
        
        if (needsMeasuring) {
            // No views -- just use padding
            preferredHeight = mSpinnerPadding.top + mSpinnerPadding.bottom;
            if (widthMode == MeasureSpec.UNSPECIFIED) {
                preferredWidth = mSpinnerPadding.left + mSpinnerPadding.right;
            }
        }

        preferredHeight = Math.max(preferredHeight, getSuggestedMinimumHeight());
        preferredWidth = Math.max(preferredWidth, getSuggestedMinimumWidth());

        heightSize = resolveSizeAndState(preferredHeight, heightMeasureSpec, 0);
        widthSize = resolveSizeAndState(preferredWidth, widthMeasureSpec, 0);

        setMeasuredDimension(widthSize, heightSize);
        mHeightMeasureSpec = heightMeasureSpec;
        mWidthMeasureSpec = widthMeasureSpec;
    }

    int getChildHeight(View child) {
        return child.getMeasuredHeight();
    }
    
    int getChildWidth(View child) {
        return child.getMeasuredWidth();
    }
    
    @Override
    protected ViewGroup.LayoutParams generateDefaultLayoutParams() {
        return new ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT);
    }
    
    void recycleAllViews() {
        final int childCount = getChildCount();
        final AbsSpinner.RecycleBin recycleBin = mRecycler;
        final int position = mFirstPosition;

        // All views go in recycler
        for (int i = 0; i < childCount; i++) {
            View v = getChildAt(i);
            int index = position + i;
            recycleBin.put(index, v);
        }  
    }

    /**
     * 直接跳到数据适配器中指定条目.
     */
    public void setSelection(int position, boolean animate) {
        // Animate only if requested position is already on screen somewhere
        boolean shouldAnimate = animate && mFirstPosition <= position &&
                position <= mFirstPosition + getChildCount() - 1;
        setSelectionInt(position, shouldAnimate);
    }

    @Override
    public void setSelection(int position) {
        setNextSelectedPositionInt(position);
        requestLayout();
        invalidate();
    }
    

    /**
     * Makes the item at the supplied position selected.
     * 
     * @param position Position to select
     * @param animate Should the transition be animated
     * 
     */
    void setSelectionInt(int position, boolean animate) {
        if (position != mOldSelectedPosition) {
            mBlockLayoutRequests = true;
            int delta  = position - mSelectedPosition;
            setNextSelectedPositionInt(position);
            layout(delta, animate);
            mBlockLayoutRequests = false;
        }
    }

    abstract void layout(int delta, boolean animate);

    @Override
    public View getSelectedView() {
        if (mItemCount > 0 && mSelectedPosition >= 0) {
            return getChildAt(mSelectedPosition - mFirstPosition);
        } else {
            return null;
        }
    }
   
    /**
     * 重写以防止调整自身布局时出现的无用布局请求.
     * 
     * @see android.view.View#requestLayout()
     */
    @Override
    public void requestLayout() {
        if (!mBlockLayoutRequests) {
            super.requestLayout();
        }
    }

    @Override
    public SpinnerAdapter getAdapter() {
        return mAdapter;
    }

    @Override
    public int getCount() {
        return mItemCount;
    }

    /**
     * 将一个坐标点变换为列表的索引.
     * 
     * @param x 本地坐标系的 X.
     * @param y 本地坐标系的 Y.
     * @return 指定点所在条目的索引.如果这个点不在任何条目上则返回 {@link #INVALID_POSITION}.
     */
    public int pointToPosition(int x, int y) {
        Rect frame = mTouchFrame;
        if (frame == null) {
            mTouchFrame = new Rect();
            frame = mTouchFrame;
        }

        final int count = getChildCount();
        for (int i = count - 1; i >= 0; i--) {
            View child = getChildAt(i);
            if (child.getVisibility() == View.VISIBLE) {
                child.getHitRect(frame);
                if (frame.contains(x, y)) {
                    return mFirstPosition + i;
                }
            }
        } 
        return INVALID_POSITION;
    }
    
    static class SavedState extends BaseSavedState {
        long selectedId;
        int position;

        /**
         * Constructor called from {@link AbsSpinner#onSaveInstanceState()}
         */
        SavedState(Parcelable superState) {
            super(superState);
        }
        
        /**
         * Constructor called from {@link #CREATOR}
         */
        private SavedState(Parcel in) {
            super(in);
            selectedId = in.readLong();
            position = in.readInt();
        }

        @Override
        public void writeToParcel(Parcel out, int flags) {
            super.writeToParcel(out, flags);
            out.writeLong(selectedId);
            out.writeInt(position);
        }

        @Override
        public String toString() {
            return "AbsSpinner.SavedState{"
                    + Integer.toHexString(System.identityHashCode(this))
                    + " selectedId=" + selectedId
                    + " position=" + position + "}";
        }

        public static final Parcelable.Creator<SavedState> CREATOR
                = new Parcelable.Creator<SavedState>() {
            public SavedState createFromParcel(Parcel in) {
                return new SavedState(in);
            }

            public SavedState[] newArray(int size) {
                return new SavedState[size];
            }
        };
    }

    @Override
    public Parcelable onSaveInstanceState() {
        Parcelable superState = super.onSaveInstanceState();
        SavedState ss = new SavedState(superState);
        ss.selectedId = getSelectedItemId();
        if (ss.selectedId >= 0) {
            ss.position = getSelectedItemPosition();
        } else {
            ss.position = INVALID_POSITION;
        }
        return ss;
    }

    @Override
    public void onRestoreInstanceState(Parcelable state) {
        SavedState ss = (SavedState) state;
  
        super.onRestoreInstanceState(ss.getSuperState());

        if (ss.selectedId >= 0) {
            mDataChanged = true;
            mNeedSync = true;
            mSyncRowId = ss.selectedId;
            mSyncPosition = ss.position;
            mSyncMode = SYNC_SELECTED_POSITION;
            requestLayout();
        }
    }

    class RecycleBin {
        private final SparseArray<View> mScrapHeap = new SparseArray<View>();

        public void put(int position, View v) {
            mScrapHeap.put(position, v);
        }
        
        View get(int position) {
            // System.out.print("Looking for " + position);
            View result = mScrapHeap.get(position);
            if (result != null) {
                // System.out.println(" HIT");
                mScrapHeap.delete(position);
            } else {
                // System.out.println(" MISS");
            }
            return result;
        }

        void clear() {
            final SparseArray<View> scrapHeap = mScrapHeap;
            final int count = scrapHeap.size();
            for (int i = 0; i < count; i++) {
                final View view = scrapHeap.valueAt(i);
                if (view != null) {
                    removeDetachedView(view, true);
                }
            }
            scrapHeap.clear();
        }
    }

    @Override
    public void onInitializeAccessibilityEvent(AccessibilityEvent event) {
        super.onInitializeAccessibilityEvent(event);
        event.setClassName(AbsSpinner.class.getName());
    }

    @Override
    public void onInitializeAccessibilityNodeInfo(AccessibilityNodeInfo info) {
        super.onInitializeAccessibilityNodeInfo(info);
        info.setClassName(AbsSpinner.class.getName());
    }
}
