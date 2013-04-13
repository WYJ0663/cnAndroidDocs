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
import android.database.DataSetObserver;
import android.os.Parcelable;
import android.os.SystemClock;
import android.util.AttributeSet;
import android.util.SparseArray;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.SoundEffectConstants;
import android.view.View;
import android.view.ViewDebug;
import android.view.ViewGroup;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityManager;
import android.view.accessibility.AccessibilityNodeInfo;
import android.view.accessibility.AccessibilityNodeProvider;

/**
 * AdapterView 是内容由 {@link Adapter} 来决定的视图类.
 *
 * <p>
 * 参见 {@link ListView}、{@link GridView}、{@link Spinner} 和
 *      {@link Gallery} 等 AdapterView 的常见子类.
 *
 * <div class="special reference">
 * <h3>开发者指南</h3>
 * <p>关于使用 AdapterView 的更多信息参见 <a href="{@docRoot}guide/topics/ui/binding.html">
 * 绑定数据到 AdapterView</a> 开发者指南.</p></div>
 * @author translate by cnmahj
 */
public abstract class AdapterView<T extends Adapter> extends ViewGroup {

    /**
     * 当适配器禁止条项的视图再利用时，调用 {@link Adapter#getItemViewType(int)} 函数的返回值.
     */
    public static final int ITEM_VIEW_TYPE_IGNORE = -1;

    /**
     * 当条项是列表头或列表尾时，调用 {@link Adapter#getItemViewType(int)} 函数的返回值.
     */
    public static final int ITEM_VIEW_TYPE_HEADER_OR_FOOTER = -2;

    /**
     * The position of the first child displayed
     */
    @ViewDebug.ExportedProperty(category = "scrolling")
    int mFirstPosition = 0;

    /**
     * The offset in pixels from the top of the AdapterView to the top
     * of the view to select during the next layout.
     */
    int mSpecificTop;

    /**
     * Position from which to start looking for mSyncRowId
     */
    int mSyncPosition;

    /**
     * Row id to look for when data has changed
     */
    long mSyncRowId = INVALID_ROW_ID;

    /**
     * Height of the view when mSyncPosition and mSyncRowId where set
     */
    long mSyncHeight;

    /**
     * True if we need to sync to mSyncRowId
     */
    boolean mNeedSync = false;

    /**
     * Indicates whether to sync based on the selection or position. Possible
     * values are {@link #SYNC_SELECTED_POSITION} or
     * {@link #SYNC_FIRST_POSITION}.
     */
    int mSyncMode;

    /**
     * Our height after the last layout
     */
    private int mLayoutHeight;

    /**
     * Sync based on the selected child
     */
    static final int SYNC_SELECTED_POSITION = 0;

    /**
     * Sync based on the first child displayed
     */
    static final int SYNC_FIRST_POSITION = 1;

    /**
     * Maximum amount of time to spend in {@link #findSyncPosition()}
     */
    static final int SYNC_MAX_DURATION_MILLIS = 100;

    /**
     * Indicates that this view is currently being laid out.
     */
    boolean mInLayout = false;

    /**
     * The listener that receives notifications when an item is selected.
     */
    OnItemSelectedListener mOnItemSelectedListener;

    /**
     * The listener that receives notifications when an item is clicked.
     */
    OnItemClickListener mOnItemClickListener;

    /**
     * The listener that receives notifications when an item is long clicked.
     */
    OnItemLongClickListener mOnItemLongClickListener;

    /**
     * True if the data has changed since the last layout
     */
    boolean mDataChanged;

    /**
     * The position within the adapter's data set of the item to select
     * during the next layout.
     */
    @ViewDebug.ExportedProperty(category = "list")
    int mNextSelectedPosition = INVALID_POSITION;

    /**
     * The item id of the item to select during the next layout.
     */
    long mNextSelectedRowId = INVALID_ROW_ID;

    /**
     * The position within the adapter's data set of the currently selected item.
     */
    @ViewDebug.ExportedProperty(category = "list")
    int mSelectedPosition = INVALID_POSITION;

    /**
     * The item id of the currently selected item.
     */
    long mSelectedRowId = INVALID_ROW_ID;

    /**
     * View to show if there are no items to show.
     */
    private View mEmptyView;

    /**
     * The number of items in the current adapter.
     */
    @ViewDebug.ExportedProperty(category = "list")
    int mItemCount;

    /**
     * The number of items in the adapter before a data changed event occurred.
     */
    int mOldItemCount;

    /**
     * 代表无效的位置.有效值的范围是 0 到当前适配器项目数减 1 .
     */
    public static final int INVALID_POSITION = -1;

    /**
     * 代表空或者无效的行ID.
     */
    public static final long INVALID_ROW_ID = Long.MIN_VALUE;

    /**
     * The last selected position we used when notifying
     */
    int mOldSelectedPosition = INVALID_POSITION;
    
    /**
     * The id of the last selected position we used when notifying
     */
    long mOldSelectedRowId = INVALID_ROW_ID;

    /**
     * Indicates what focusable state is requested when calling setFocusable().
     * In addition to this, this view has other criteria for actually
     * determining the focusable state (such as whether its empty or the text
     * filter is shown).
     *
     * @see #setFocusable(boolean)
     * @see #checkFocus()
     */
    private boolean mDesiredFocusableState;
    private boolean mDesiredFocusableInTouchModeState;

    private SelectionNotifier mSelectionNotifier;
    /**
     * When set to true, calls to requestLayout() will not propagate up the parent hierarchy.
     * This is used to layout the children during a layout pass.
     */
    boolean mBlockLayoutRequests = false;

    public AdapterView(Context context) {
        super(context);
    }

    public AdapterView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public AdapterView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);

        // If not explicitly specified this view is important for accessibility.
        if (getImportantForAccessibility() == IMPORTANT_FOR_ACCESSIBILITY_AUTO) {
            setImportantForAccessibility(IMPORTANT_FOR_ACCESSIBILITY_YES);
        }
    }

    /**
     * 定义了当单击 AdapterView 中的项目时调用的回调函数的接口.
     */
    public interface OnItemClickListener {

        /**
         * 按下 AdapterView 中的条目时，调用该回调方法.
         * <p>
         * 实现的函数中可以调用 getItemAtPosition(position) 方法来访问按下条目的数据.
         *
         * @param parent 发生单击事件的 AdapterView.
         * @param view AdapterView 中发生单击事件的视图（由适配器提供的视图）.
         * @param position 适配器中视图的位置.
         * @param id 单击的条目 ID.
         */
        void onItemClick(AdapterView<?> parent, View view, int position, long id);
    }

    /**
     * 注册单击 AdapterView 中的条目时执行的回调函数.
     *
     * @param listener 执行的回调函数.
     */
    public void setOnItemClickListener(OnItemClickListener listener) {
        mOnItemClickListener = listener;
    }

    /**
     * @return 点击 AdapterView 中的条目时执行的回调函数；没有设置时返回空.
     */
    public final OnItemClickListener getOnItemClickListener() {
        return mOnItemClickListener;
    }

    /**
     * 如果定义了 OnItemClickListener 则调用它.
     *
     * @param view AdapterView 中被点击的视图.
     * @param position 视图在适配器中的索引.
     * @param id 点击的条目的行 ID.
     * @return 如果成功调用了定义的 OnItemClickListener 则返回真；否则返回假.
     */
    public boolean performItemClick(View view, int position, long id) {
        if (mOnItemClickListener != null) {
            playSoundEffect(SoundEffectConstants.CLICK);
            if (view != null) {
                view.sendAccessibilityEvent(AccessibilityEvent.TYPE_VIEW_CLICKED);
            }
            mOnItemClickListener.onItemClick(this, view, position, id);
            return true;
        }

        return false;
    }

    /**
     * 定义了当长按视图中的项目时调用的回调函数的接口.
     */
    public interface OnItemLongClickListener {
        /**
         * 当按下视图中的项目并保持按下状态（长按）时执行的回调函数.
         *
         * 实现时如果需要访问与选中条目关联的数据，可以调用 getItemAtPosition(position).
         *
         * @param parent 发生点击事件的 AbsListView.
         * @param view AbsListView 中被点击的视图.
         * @param position 视图在一览中的位置（索引）.
         * @param id 被点击条目的行 ID.
         *
         * @return 如果回调函数处理了长按事件，返回真；否则返回假.
         */
        boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id);
    }


    /**
     * 注册长按 AdapterView 中的条目时执行的回调函数.
     *
     * @param listener 事件发生时运行的回调函数.
     */
    public void setOnItemLongClickListener(OnItemLongClickListener listener) {
        if (!isLongClickable()) {
            setLongClickable(true);
        }
        mOnItemLongClickListener = listener;
    }

    /**
     * @return 取得长按 AdapterView 中的条目时执行的回调函数的监听器；未设置则返回空.
     */
    public final OnItemLongClickListener getOnItemLongClickListener() {
        return mOnItemLongClickListener;
    }

    /**
     * 定义了当选中视图中的项目时调用的回调函数的接口.
     */
    public interface OnItemSelectedListener {
        /**
         * <p>当该选中该视图的条目时执行的回调函数.该回调函数仅当新选中条目位置与
         * 之前选中的条目位置不同或没有选中条目时执行.</p>
         *
         * 实现时如果需要访问与选中条目关联的数据，可以调用 getItemAtPosition(position).
         *
         * @param parent 发生选中事件的 AbsListView.
         * @param view AbsListView 中被选中的视图.
         * @param position 视图在一览中的位置（索引）.
         * @param id 被点击条目的行 ID.
         */
        void onItemSelected(AdapterView<?> parent, View view, int position, long id);

        /**
         * 当视图中的处于选中状态的条目全部消失时执行的回调函数.
         * 启动触控功能或适配器为空都可能导致选中条目消失.
         *
         * @param parent 没有任何选中条目的 AdapterView.
         */
        void onNothingSelected(AdapterView<?> parent);
    }


    /**
     * 注册选中 AdapterView 中的条目时执行的回调函数.
     *
     * @param listener 事件发生时运行的回调函数.
     */
    public void setOnItemSelectedListener(OnItemSelectedListener listener) {
        mOnItemSelectedListener = listener;
    }

    public final OnItemSelectedListener getOnItemSelectedListener() {
        return mOnItemSelectedListener;
    }

    /**
     * 当显示 AdapterView 的上下文菜单时，为
     * {@link android.view.View.OnCreateContextMenuListener#onCreateContextMenu(ContextMenu, View, ContextMenuInfo) }
     * 回调函数提供的额外的菜单信息.
     */
    public static class AdapterContextMenuInfo implements ContextMenu.ContextMenuInfo {

        public AdapterContextMenuInfo(View targetView, int position, long id) {
            this.targetView = targetView;
            this.position = position;
            this.id = id;
        }

        /**
         * 用于显示上下文菜单的子视图.也是 AdapterView 的子视图之一.
         */
        public View targetView;

        /**
         * 用于显示上下文菜单的子视图在适配器中的位置.
         */
        public int position;

        /**
         * 用于显示上下文菜单的子视图的行 ID.
         */
        public long id;
    }

    /**
     * 返回当前与该小部件关联的适配器.
     *
     * @return 用于提供视图内容的适配器.
     */
    public abstract T getAdapter();

    /**
     * 设置用于为该小部件的视图提供用于显示的数据的适配器.
     *
     * @param adapter 用于创建视图内容的适配器.
     */
    public abstract void setAdapter(T adapter);

    /**
     * 该类不支持该方法，如果调用将抛出 UnsupportedOperationException 异常.
     *
     * @param child 忽略.
     *
     * @throws UnsupportedOperationException 调用该方法时
     */
    @Override
    public void addView(View child) {
        throw new UnsupportedOperationException("addView(View) is not supported in AdapterView");
    }

    /**
     * 该类不支持该方法，如果调用将抛出 UnsupportedOperationException 异常.
     *
     * @param child 忽略.
     * @param index 忽略.
     *
     * @throws UnsupportedOperationException 调用该方法时
     */
    @Override
    public void addView(View child, int index) {
        throw new UnsupportedOperationException("addView(View, int) is not supported in AdapterView");
    }

    /**
     * 该类不支持该方法，如果调用将抛出 UnsupportedOperationException 异常.
     *
     * @param child 忽略.
     * @param params 忽略.
     *
     * @throws UnsupportedOperationException 调用该方法时
     */
    @Override
    public void addView(View child, LayoutParams params) {
        throw new UnsupportedOperationException("addView(View, LayoutParams) "
                + "is not supported in AdapterView");
    }

    /**
     * 该类不支持该方法，如果调用将抛出 UnsupportedOperationException 异常.
     *
     * @param child 忽略.
     * @param index 忽略.
     * @param params 忽略.
     *
     * @throws UnsupportedOperationException 调用该方法时
     */
    @Override
    public void addView(View child, int index, LayoutParams params) {
        throw new UnsupportedOperationException("addView(View, int, LayoutParams) "
                + "is not supported in AdapterView");
    }

    /**
     * 该类不支持该方法，如果调用将抛出 UnsupportedOperationException 异常.
     *
     * @param child 忽略.
     *
     * @throws UnsupportedOperationException 调用该方法时
     */
    @Override
    public void removeView(View child) {
        throw new UnsupportedOperationException("removeView(View) is not supported in AdapterView");
    }

    /**
     * 该类不支持该方法，如果调用将抛出 UnsupportedOperationException 异常.
     *
     * @param index 忽略.
     *
     * @throws UnsupportedOperationException 调用该方法时
     */
    @Override
    public void removeViewAt(int index) {
        throw new UnsupportedOperationException("removeViewAt(int) is not supported in AdapterView");
    }

    /**
     * 该类不支持该方法，如果调用将抛出 UnsupportedOperationException 异常.
     *
     * @throws UnsupportedOperationException 调用该方法时
     */
    @Override
    public void removeAllViews() {
        throw new UnsupportedOperationException("removeAllViews() is not supported in AdapterView");
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        mLayoutHeight = getHeight();
    }

    /**
     * 返回当前选中项目在适配器数据中的位置.
     *
     * @return 返回从零开始的位置（索引）信息，没有选择条目时返回 {@link #INVALID_POSITION}.
     */
    @ViewDebug.CapturedViewProperty
    public int getSelectedItemPosition() {
        return mNextSelectedPosition;
    }

    /**
     * @return 当前选中条目相应的 ID；无选中条目则返回 {@link #INVALID_ROW_ID}.
     */
    @ViewDebug.CapturedViewProperty
    public long getSelectedItemId() {
        return mNextSelectedRowId;
    }

    /**
     * @return 当前选中条目对应的视图；无选中条目时返回空.
     */
    public abstract View getSelectedView();

    /**
     * @return 当前选中条目对应的数据；无选中条目时返回空.
     */
    public Object getSelectedItem() {
        T adapter = getAdapter();
        int selection = getSelectedItemPosition();
        if (adapter != null && adapter.getCount() > 0 && selection >= 0) {
            return adapter.getItem(selection);
        } else {
            return null;
        }
    }

    /**
     * @return 与 AdapterView 相关联的适配器的条目数量.（该值是数据条目的数量，
     *         可能大于可见的视图的数量.）
     */
    @ViewDebug.CapturedViewProperty
    public int getCount() {
        return mItemCount;
    }

    /**
     * 取得适配器项目对应的视图或其子视图在适配器的数据中所处的位置.
     *
     * @param view 适配器条目或其后代的视图.调用时该项目在 AdapterView 中必须可见.
     * @return 视图在适配器数据集中的位置；如果视图不在数据列表中或当前不可见，则返回
     *         {@link #INVALID_POSITION}.
     */
    public int getPositionForView(View view) {
        View listItem = view;
        try {
            View v;
            while (!(v = (View) listItem.getParent()).equals(this)) {
                listItem = v;
            }
        } catch (ClassCastException e) {
            // We made it up to the window without find this list view
            return INVALID_POSITION;
        }

        // Search the children for the list item
        final int childCount = getChildCount();
        for (int i = 0; i < childCount; i++) {
            if (getChildAt(i).equals(listItem)) {
                return mFirstPosition + i;
            }
        }

        // Child not found!
        return INVALID_POSITION;
    }

    /**
     * 返回显示在屏幕上的第一个元素在适配器中所处的位置.
     *
     * @return 在适配器数据集中的位置.
     */
    public int getFirstVisiblePosition() {
        return mFirstPosition;
    }

    /**
     * 返回显示在屏幕上的最后一个元素在适配器中所处的位置.
     *
     * @return 在适配器数据集中的位置.
     */
    public int getLastVisiblePosition() {
        return mFirstPosition + getChildCount() - 1;
    }

    /**
     * 设置当前选择条目.为了支持无障碍功能，重写该方法的子类必须首先调用父类的该方法.
     *
     * @param position 选择的数据条目的索引（从零开始）.
     */
    public abstract void setSelection(int position);

    /**
     * 设置适配器内容为空时显示的视图.
     */
    @android.view.RemotableViewMethod
    public void setEmptyView(View emptyView) {
        mEmptyView = emptyView;

        // If not explicitly specified this view is important for accessibility.
        if (emptyView != null
                && emptyView.getImportantForAccessibility() == IMPORTANT_FOR_ACCESSIBILITY_AUTO) {
            emptyView.setImportantForAccessibility(IMPORTANT_FOR_ACCESSIBILITY_YES);
        }

        final T adapter = getAdapter();
        final boolean empty = ((adapter == null) || adapter.isEmpty());
        updateEmptyStatus(empty);
    }

    /**
     * 当前适配器无内容时，AdapterView 会显示特殊的空视图.
     * 空视图用于告诉用户，该 AdapterView 没有数据.
     *
     * @return 适配器为空时显示的视图.
     */
    public View getEmptyView() {
        return mEmptyView;
    }

    /**
     * 指示视图是否处于过滤状态。过滤状态在用户在键盘上输入信息时启动. 
     *
     * @return 如果视图处于过滤状态，返回真；否则返回假。
     */
    boolean isInFilterMode() {
        return false;
    }

    @Override
    public void setFocusable(boolean focusable) {
        final T adapter = getAdapter();
        final boolean empty = adapter == null || adapter.getCount() == 0;

        mDesiredFocusableState = focusable;
        if (!focusable) {
            mDesiredFocusableInTouchModeState = false;
        }

        super.setFocusable(focusable && (!empty || isInFilterMode()));
    }

    @Override
    public void setFocusableInTouchMode(boolean focusable) {
        final T adapter = getAdapter();
        final boolean empty = adapter == null || adapter.getCount() == 0;

        mDesiredFocusableInTouchModeState = focusable;
        if (focusable) {
            mDesiredFocusableState = true;
        }

        super.setFocusableInTouchMode(focusable && (!empty || isInFilterMode()));
    }

    void checkFocus() {
        final T adapter = getAdapter();
        final boolean empty = adapter == null || adapter.getCount() == 0;
        final boolean focusable = !empty || isInFilterMode();
        // The order in which we set focusable in touch mode/focusable may matter
        // for the client, see View.setFocusableInTouchMode() comments for more
        // details
        super.setFocusableInTouchMode(focusable && mDesiredFocusableInTouchModeState);
        super.setFocusable(focusable && mDesiredFocusableState);
        if (mEmptyView != null) {
            updateEmptyStatus((adapter == null) || adapter.isEmpty());
        }
    }

    /**
     * Update the status of the list based on the empty parameter.  If empty is true and
     * we have an empty view, display it.  In all the other cases, make sure that the listview
     * is VISIBLE and that the empty view is GONE (if it's not null).
     */
    private void updateEmptyStatus(boolean empty) {
        if (isInFilterMode()) {
            empty = false;
        }

        if (empty) {
            if (mEmptyView != null) {
                mEmptyView.setVisibility(View.VISIBLE);
                setVisibility(View.GONE);
            } else {
                // If the caller just removed our empty view, make sure the list view is visible
                setVisibility(View.VISIBLE);
            }

            // We are now GONE, so pending layouts will not be dispatched.
            // Force one here to make sure that the state of the list matches
            // the state of the adapter.
            if (mDataChanged) {           
                this.onLayout(false, mLeft, mTop, mRight, mBottom); 
            }
        } else {
            if (mEmptyView != null) mEmptyView.setVisibility(View.GONE);
            setVisibility(View.VISIBLE);
        }
    }

    /**
     * 取得列表中指定位置的数据.
     *
     * @param position 要取得数据的位置.
     * @return 列表中指定位置的数据.
     */
    public Object getItemAtPosition(int position) {
        T adapter = getAdapter();
        return (adapter == null || position < 0) ? null : adapter.getItem(position);
    }

    public long getItemIdAtPosition(int position) {
        T adapter = getAdapter();
        return (adapter == null || position < 0) ? INVALID_ROW_ID : adapter.getItemId(position);
    }

    @Override
    public void setOnClickListener(OnClickListener l) {
        throw new RuntimeException("Don't call setOnClickListener for an AdapterView. "
                + "You probably want setOnItemClickListener instead");
    }

    /**
     * 为了防止适配器生成的视图被冻结而重写.
     */
    @Override
    protected void dispatchSaveInstanceState(SparseArray<Parcelable> container) {
        dispatchFreezeSelfOnly(container);
    }

    /**
     * 为了防止适配器生成的视图被解冻而重写.
     */
    @Override
    protected void dispatchRestoreInstanceState(SparseArray<Parcelable> container) {
        dispatchThawSelfOnly(container);
    }

    class AdapterDataSetObserver extends DataSetObserver {

        private Parcelable mInstanceState = null;

        @Override
        public void onChanged() {
            mDataChanged = true;
            mOldItemCount = mItemCount;
            mItemCount = getAdapter().getCount();

            // Detect the case where a cursor that was previously invalidated has
            // been repopulated with new data.
            if (AdapterView.this.getAdapter().hasStableIds() && mInstanceState != null
                    && mOldItemCount == 0 && mItemCount > 0) {
                AdapterView.this.onRestoreInstanceState(mInstanceState);
                mInstanceState = null;
            } else {
                rememberSyncState();
            }
            checkFocus();
            requestLayout();
        }

        @Override
        public void onInvalidated() {
            mDataChanged = true;

            if (AdapterView.this.getAdapter().hasStableIds()) {
                // Remember the current state for the case where our hosting activity is being
                // stopped and later restarted
                mInstanceState = AdapterView.this.onSaveInstanceState();
            }

            // Data is invalid so we should reset our state
            mOldItemCount = mItemCount;
            mItemCount = 0;
            mSelectedPosition = INVALID_POSITION;
            mSelectedRowId = INVALID_ROW_ID;
            mNextSelectedPosition = INVALID_POSITION;
            mNextSelectedRowId = INVALID_ROW_ID;
            mNeedSync = false;

            checkFocus();
            requestLayout();
        }

        public void clearSavedState() {
            mInstanceState = null;
        }
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        removeCallbacks(mSelectionNotifier);
    }

    private class SelectionNotifier implements Runnable {
        public void run() {
            if (mDataChanged) {
                // Data has changed between when this SelectionNotifier
                // was posted and now. We need to wait until the AdapterView
                // has been synched to the new data.
                if (getAdapter() != null) {
                    post(this);
                }
            } else {
                fireOnSelected();
                performAccessibilityActionsOnSelected();
            }
        }
    }

    void selectionChanged() {
        if (mOnItemSelectedListener != null
                || AccessibilityManager.getInstance(mContext).isEnabled()) {
            if (mInLayout || mBlockLayoutRequests) {
                // If we are in a layout traversal, defer notification
                // by posting. This ensures that the view tree is
                // in a consistent state and is able to accomodate
                // new layout or invalidate requests.
                if (mSelectionNotifier == null) {
                    mSelectionNotifier = new SelectionNotifier();
                }
                post(mSelectionNotifier);
            } else {
                fireOnSelected();
                performAccessibilityActionsOnSelected();
            }
        }
    }

    private void fireOnSelected() {
        if (mOnItemSelectedListener == null) {
            return;
        }
        final int selection = getSelectedItemPosition();
        if (selection >= 0) {
            View v = getSelectedView();
            mOnItemSelectedListener.onItemSelected(this, v, selection,
                    getAdapter().getItemId(selection));
        } else {
            mOnItemSelectedListener.onNothingSelected(this);
        }
    }

    private void performAccessibilityActionsOnSelected() {
        if (!AccessibilityManager.getInstance(mContext).isEnabled()) {
            return;
        }
        final int position = getSelectedItemPosition();
        if (position >= 0) {
            // we fire selection events here not in View
            sendAccessibilityEvent(AccessibilityEvent.TYPE_VIEW_SELECTED);
        }
    }

    @Override
    public boolean dispatchPopulateAccessibilityEvent(AccessibilityEvent event) {
        View selectedView = getSelectedView();
        if (selectedView != null && selectedView.getVisibility() == VISIBLE
                && selectedView.dispatchPopulateAccessibilityEvent(event)) {
            return true;
        }
        return false;
    }

    @Override
    public boolean onRequestSendAccessibilityEvent(View child, AccessibilityEvent event) {
        if (super.onRequestSendAccessibilityEvent(child, event)) {
            // Add a record for ourselves as well.
            AccessibilityEvent record = AccessibilityEvent.obtain();
            onInitializeAccessibilityEvent(record);
            // Populate with the text of the requesting child.
            child.dispatchPopulateAccessibilityEvent(record);
            event.appendRecord(record);
            return true;
        }
        return false;
    }

    @Override
    public void onInitializeAccessibilityNodeInfo(AccessibilityNodeInfo info) {
        super.onInitializeAccessibilityNodeInfo(info);
        info.setClassName(AdapterView.class.getName());
        info.setScrollable(isScrollableForAccessibility());
        View selectedView = getSelectedView();
        if (selectedView != null) {
            info.setEnabled(selectedView.isEnabled());
        }
    }

    @Override
    public void onInitializeAccessibilityEvent(AccessibilityEvent event) {
        super.onInitializeAccessibilityEvent(event);
        event.setClassName(AdapterView.class.getName());
        event.setScrollable(isScrollableForAccessibility());
        View selectedView = getSelectedView();
        if (selectedView != null) {
            event.setEnabled(selectedView.isEnabled());
        }
        event.setCurrentItemIndex(getSelectedItemPosition());
        event.setFromIndex(getFirstVisiblePosition());
        event.setToIndex(getLastVisiblePosition());
        event.setItemCount(getCount());
    }

    private boolean isScrollableForAccessibility() {
        T adapter = getAdapter();
        if (adapter != null) {
            final int itemCount = adapter.getCount();
            return itemCount > 0
                && (getFirstVisiblePosition() > 0 || getLastVisiblePosition() < itemCount - 1);
        }
        return false;
    }

    @Override
    protected boolean canAnimate() {
        return super.canAnimate() && mItemCount > 0;
    }

    void handleDataChanged() {
        final int count = mItemCount;
        boolean found = false;

        if (count > 0) {

            int newPos;

            // Find the row we are supposed to sync to
            if (mNeedSync) {
                // Update this first, since setNextSelectedPositionInt inspects
                // it
                mNeedSync = false;

                // See if we can find a position in the new data with the same
                // id as the old selection
                newPos = findSyncPosition();
                if (newPos >= 0) {
                    // Verify that new selection is selectable
                    int selectablePos = lookForSelectablePosition(newPos, true);
                    if (selectablePos == newPos) {
                        // Same row id is selected
                        setNextSelectedPositionInt(newPos);
                        found = true;
                    }
                }
            }
            if (!found) {
                // Try to use the same position if we can't find matching data
                newPos = getSelectedItemPosition();

                // Pin position to the available range
                if (newPos >= count) {
                    newPos = count - 1;
                }
                if (newPos < 0) {
                    newPos = 0;
                }

                // Make sure we select something selectable -- first look down
                int selectablePos = lookForSelectablePosition(newPos, true);
                if (selectablePos < 0) {
                    // Looking down didn't work -- try looking up
                    selectablePos = lookForSelectablePosition(newPos, false);
                }
                if (selectablePos >= 0) {
                    setNextSelectedPositionInt(selectablePos);
                    checkSelectionChanged();
                    found = true;
                }
            }
        }
        if (!found) {
            // Nothing is selected
            mSelectedPosition = INVALID_POSITION;
            mSelectedRowId = INVALID_ROW_ID;
            mNextSelectedPosition = INVALID_POSITION;
            mNextSelectedRowId = INVALID_ROW_ID;
            mNeedSync = false;
            checkSelectionChanged();
        }

        //TODO: Hmm, we do not know the old state so this is sub-optimal
        notifyAccessibilityStateChanged();
    }

    void checkSelectionChanged() {
        if ((mSelectedPosition != mOldSelectedPosition) || (mSelectedRowId != mOldSelectedRowId)) {
            selectionChanged();
            mOldSelectedPosition = mSelectedPosition;
            mOldSelectedRowId = mSelectedRowId;
        }
    }

    /**
     * Searches the adapter for a position matching mSyncRowId. The search starts at mSyncPosition
     * and then alternates between moving up and moving down until 1) we find the right position, or
     * 2) we run out of time, or 3) we have looked at every position
     *
     * @return Position of the row that matches mSyncRowId, or {@link #INVALID_POSITION} if it can't
     *         be found
     */
    int findSyncPosition() {
        int count = mItemCount;

        if (count == 0) {
            return INVALID_POSITION;
        }

        long idToMatch = mSyncRowId;
        int seed = mSyncPosition;

        // If there isn't a selection don't hunt for it
        if (idToMatch == INVALID_ROW_ID) {
            return INVALID_POSITION;
        }

        // Pin seed to reasonable values
        seed = Math.max(0, seed);
        seed = Math.min(count - 1, seed);

        long endTime = SystemClock.uptimeMillis() + SYNC_MAX_DURATION_MILLIS;

        long rowId;

        // first position scanned so far
        int first = seed;

        // last position scanned so far
        int last = seed;

        // True if we should move down on the next iteration
        boolean next = false;

        // True when we have looked at the first item in the data
        boolean hitFirst;

        // True when we have looked at the last item in the data
        boolean hitLast;

        // Get the item ID locally (instead of getItemIdAtPosition), so
        // we need the adapter
        T adapter = getAdapter();
        if (adapter == null) {
            return INVALID_POSITION;
        }

        while (SystemClock.uptimeMillis() <= endTime) {
            rowId = adapter.getItemId(seed);
            if (rowId == idToMatch) {
                // Found it!
                return seed;
            }

            hitLast = last == count - 1;
            hitFirst = first == 0;

            if (hitLast && hitFirst) {
                // Looked at everything
                break;
            }

            if (hitFirst || (next && !hitLast)) {
                // Either we hit the top, or we are trying to move down
                last++;
                seed = last;
                // Try going up next time
                next = false;
            } else if (hitLast || (!next && !hitFirst)) {
                // Either we hit the bottom, or we are trying to move up
                first--;
                seed = first;
                // Try going down next time
                next = true;
            }

        }

        return INVALID_POSITION;
    }

    /**
     * Find a position that can be selected (i.e., is not a separator).
     *
     * @param position The starting position to look at.
     * @param lookDown Whether to look down for other positions.
     * @return The next selectable position starting at position and then searching either up or
     *         down. Returns {@link #INVALID_POSITION} if nothing can be found.
     */
    int lookForSelectablePosition(int position, boolean lookDown) {
        return position;
    }

    /**
     * Utility to keep mSelectedPosition and mSelectedRowId in sync
     * @param position Our current position
     */
    void setSelectedPositionInt(int position) {
        mSelectedPosition = position;
        mSelectedRowId = getItemIdAtPosition(position);
    }

    /**
     * Utility to keep mNextSelectedPosition and mNextSelectedRowId in sync
     * @param position Intended value for mSelectedPosition the next time we go
     * through layout
     */
    void setNextSelectedPositionInt(int position) {
        mNextSelectedPosition = position;
        mNextSelectedRowId = getItemIdAtPosition(position);
        // If we are trying to sync to the selection, update that too
        if (mNeedSync && mSyncMode == SYNC_SELECTED_POSITION && position >= 0) {
            mSyncPosition = position;
            mSyncRowId = mNextSelectedRowId;
        }
    }

    /**
     * Remember enough information to restore the screen state when the data has
     * changed.
     *
     */
    void rememberSyncState() {
        if (getChildCount() > 0) {
            mNeedSync = true;
            mSyncHeight = mLayoutHeight;
            if (mSelectedPosition >= 0) {
                // Sync the selection state
                View v = getChildAt(mSelectedPosition - mFirstPosition);
                mSyncRowId = mNextSelectedRowId;
                mSyncPosition = mNextSelectedPosition;
                if (v != null) {
                    mSpecificTop = v.getTop();
                }
                mSyncMode = SYNC_SELECTED_POSITION;
            } else {
                // Sync the based on the offset of the first view
                View v = getChildAt(0);
                T adapter = getAdapter();
                if (mFirstPosition >= 0 && mFirstPosition < adapter.getCount()) {
                    mSyncRowId = adapter.getItemId(mFirstPosition);
                } else {
                    mSyncRowId = NO_ID;
                }
                mSyncPosition = mFirstPosition;
                if (v != null) {
                    mSpecificTop = v.getTop();
                }
                mSyncMode = SYNC_FIRST_POSITION;
            }
        }
    }
}
