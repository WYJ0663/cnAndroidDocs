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
import android.util.AttributeSet;
import android.util.SparseBooleanArray;
import android.view.View;
import android.view.ViewGroup;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityNodeInfo;

import java.util.regex.Pattern;

/**
 * <p>按照行列来组织子视图的布局.表格布局包含一系列的 {@link android.widget.TableRow
 * 表格行}对象，用于定义行（实际上你也可以使用其它子对象，将在后面进行解释）.
 * 表格布局不为它的行、列和单元格显示表格线.每个行可以包含0个以上（包括0）的单元格；
 * 每个单元格可以设置一个{@link android.view.View 视图}对象.与行包含很多单元格一样，
 * 表格包含很多列.表格的单元格可以为空.单元格可以象 HTML 那样跨列.</p>
 *
 * <p>列的宽度由该列所有行中最宽的一个单元格决定.不过表格布局可以通过
 * {@link #setColumnShrinkable(int, boolean) setColumnShrinkable()} 方法或者
 * {@link #setColumnStretchable(int, boolean) setColumnStretchable()} 
 * 方法来标记某些列可以收缩或可以拉伸.
 * 如果标记为可以收缩，列宽可以收缩以使表格适合容器的大小.如果标记为可以拉伸，
 * 列宽可以拉伸以占用多余的空间.表格的总宽度由其父容器决定.
 * 记住列可以同时具有可拉伸和可收缩标记是很重要的.在列可以调整其宽度以占用可用空间，
 * 但不能超过限度时是很有用的.最后，你可以通过调用
 * {@link #setColumnCollapsed(int,boolean) setColumnCollapsed()} 方法来隐藏列.
 * </p>
 *
 * <p>表格布局的子对象不能指定 <code>layout_width</code> 属性.宽度永远是
 * <code>MATCH_PARENT</code>.不过子对象可以定义 <code>layout_height</code>
 * 属性；其默认值是 {@link android.widget.TableLayout.LayoutParams#WRAP_CONTENT}.
 * 如果子对象是 {@link android.widget.TableRow 表格行}，其高度永远是
 * {@link android.widget.TableLayout.LayoutParams#WRAP_CONTENT}.</p>
 *
 * <p>无论是在代码还是在 XML 布局文件中，单元格必须安装索引顺序加入表格行.
 * 列号是从 0 开始的.如果你不为子单元格指定列号，其将自动增值，使用下一个可用列号.
 * 如果你跳过某个列号，他在表格行中作为空可以改对待.参见 ApiDemos
 * 中通过 XML 创建表格的布局示例.</p>
 *
 * @author translate by cnmahj
 */
public class TableLayout extends LinearLayout {
    private int[] mMaxWidths;
    private SparseBooleanArray mStretchableColumns;
    private SparseBooleanArray mShrinkableColumns;
    private SparseBooleanArray mCollapsedColumns;

    private boolean mShrinkAllColumns;
    private boolean mStretchAllColumns;

    private TableLayout.PassThroughHierarchyChangeListener mPassThroughListener;

    private boolean mInitialized;

    /**
     * <p>为给定的上下文创建表格布局.</p>
     *
     * @param context 应用程序上下文.
     */
    public TableLayout(Context context) {
        super(context);
        initTableLayout();
    }

    /**
     * <p>使用指定的属性集合为给定的上下文创建表格布局.</p>
     *
     * @param context 应用程序上下文.
     * @param attrs 属性集合.
     */
    public TableLayout(Context context, AttributeSet attrs) {
        super(context, attrs);

        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.TableLayout);

        String stretchedColumns = a.getString(R.styleable.TableLayout_stretchColumns);
        if (stretchedColumns != null) {
            if (stretchedColumns.charAt(0) == '*') {
                mStretchAllColumns = true;
            } else {
                mStretchableColumns = parseColumns(stretchedColumns);
            }
        }

        String shrinkedColumns = a.getString(R.styleable.TableLayout_shrinkColumns);
        if (shrinkedColumns != null) {
            if (shrinkedColumns.charAt(0) == '*') {
                mShrinkAllColumns = true;
            } else {
                mShrinkableColumns = parseColumns(shrinkedColumns);
            }
        }

        String collapsedColumns = a.getString(R.styleable.TableLayout_collapseColumns);
        if (collapsedColumns != null) {
            mCollapsedColumns = parseColumns(collapsedColumns);
        }

        a.recycle();
        initTableLayout();
    }

    /**
     * <p>Parses a sequence of columns ids defined in a CharSequence with the
     * following pattern (regex): \d+(\s*,\s*\d+)*</p>
     *
     * <p>Examples: "1" or "13, 7, 6" or "".</p>
     *
     * <p>The result of the parsing is stored in a sparse boolean array. The
     * parsed column ids are used as the keys of the sparse array. The values
     * are always true.</p>
     *
     * @param sequence a sequence of column ids, can be empty but not null
     * @return a sparse array of boolean mapping column indexes to the columns
     *         collapse state
     */
    private static SparseBooleanArray parseColumns(String sequence) {
        SparseBooleanArray columns = new SparseBooleanArray();
        Pattern pattern = Pattern.compile("\\s*,\\s*");
        String[] columnDefs = pattern.split(sequence);

        for (String columnIdentifier : columnDefs) {
            try {
                int columnIndex = Integer.parseInt(columnIdentifier);
                // only valid, i.e. positive, columns indexes are handled
                if (columnIndex >= 0) {
                    // putting true in this sparse array indicates that the
                    // column index was defined in the XML file
                    columns.put(columnIndex, true);
                }
            } catch (NumberFormatException e) {
                // we just ignore columns that don't exist
            }
        }

        return columns;
    }

    /**
     * <p>Performs initialization common to prorgrammatic use and XML use of
     * this widget.</p>
     */
    private void initTableLayout() {
        if (mCollapsedColumns == null) {
            mCollapsedColumns = new SparseBooleanArray();
        }
        if (mStretchableColumns == null) {
            mStretchableColumns = new SparseBooleanArray();
        }
        if (mShrinkableColumns == null) {
            mShrinkableColumns = new SparseBooleanArray();
        }

        // TableLayouts are always in vertical orientation; keep this tracked
        // for shared LinearLayout code.
        setOrientation(VERTICAL);

        mPassThroughListener = new PassThroughHierarchyChangeListener();
        // make sure to call the parent class method to avoid potential
        // infinite loops
        super.setOnHierarchyChangeListener(mPassThroughListener);

        mInitialized = true;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setOnHierarchyChangeListener(
            OnHierarchyChangeListener listener) {
        // the user listener is delegated to our pass-through listener
        mPassThroughListener.mOnHierarchyChangeListener = listener;
    }

    private void requestRowsLayout() {
        if (mInitialized) {
            final int count = getChildCount();
            for (int i = 0; i < count; i++) {
                getChildAt(i).requestLayout();
            }
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void requestLayout() {
        if (mInitialized) {
            int count = getChildCount();
            for (int i = 0; i < count; i++) {
                getChildAt(i).forceLayout();
            }
        }

        super.requestLayout();
    }

    /**
     * <p>指示，是否所有的列都是可收缩的.</p>
     *
     * @return 如果所有列都可收缩，返回真；否则返回假.
     *
     * @attr ref android.R.styleable#TableLayout_shrinkColumns
     */
    public boolean isShrinkAllColumns() {
        return mShrinkAllColumns;
    }

    /**
     * <p>标记所有列为可收缩的便利的方法.</p>
     *
     * @param shrinkAllColumns 如果标记所有列为可收缩时为真.
     *
     * @attr ref android.R.styleable#TableLayout_shrinkColumns
     */
    public void setShrinkAllColumns(boolean shrinkAllColumns) {
        mShrinkAllColumns = shrinkAllColumns;
    }

    /**
     * <p>指示，是否所有的列都是可拉伸的.</p>
     *
     * @return 如果所有列都可拉伸，返回真；否则返回假.
     *
     * @attr ref android.R.styleable#TableLayout_stretchColumns
     */
    public boolean isStretchAllColumns() {
        return mStretchAllColumns;
    }

    /**
     * <p>标记所有列为可拉伸的便利的方法.</p>
     *
     * @param stretchAllColumns 如果标记所有列为可拉伸时为真.
     *
     * @attr ref android.R.styleable#TableLayout_stretchColumns
     */
    public void setStretchAllColumns(boolean stretchAllColumns) {
        mStretchAllColumns = stretchAllColumns;
    }

    /**
     * <p>折叠或回复给定列.折叠时，列从屏幕上消失，其空间由其它列占用.
     * 当列属于 {@link android.widget.TableRow} 时才可以进行折叠/回复操作.</p>
     *
     * <p>调用该方法会请求布局操作.</p>
     *
     * @param columnIndex 列索引.
     * @param isCollapsed 折叠时为真；否则为假.
     *
     * @attr ref android.R.styleable#TableLayout_collapseColumns
     */
    public void setColumnCollapsed(int columnIndex, boolean isCollapsed) {
        // update the collapse status of the column
        mCollapsedColumns.put(columnIndex, isCollapsed);

        int count = getChildCount();
        for (int i = 0; i < count; i++) {
            final View view = getChildAt(i);
            if (view instanceof TableRow) {
                ((TableRow) view).setColumnCollapsed(columnIndex, isCollapsed);
            }
        }

        requestRowsLayout();
    }

    /**
     * <p>返回指定列的折叠状态.</p>
     *
     * @param columnIndex 列索引.
     * @return 折叠时为真；否则为假.
     */
    public boolean isColumnCollapsed(int columnIndex) {
        return mCollapsedColumns.get(columnIndex);
    }

    /**
     * <p>设置指定列是否可拉伸.可拉伸时，列会尽可能多的占用行中的可用空间.</p>
     *
     * <p>调用该方法会请求布局操作.</p>
     *
     * @param columnIndex 列的索引.
     * @param isStretchable 如果列可以拉伸，设为真；否则设为假.默认是假.
     *
     * @attr ref android.R.styleable#TableLayout_stretchColumns
     */
    public void setColumnStretchable(int columnIndex, boolean isStretchable) {
        mStretchableColumns.put(columnIndex, isStretchable);
        requestRowsLayout();
    }

    /**
     * <p>返回指定的列是否可拉伸.</p>
     *
     * @param columnIndex 列的索引.
     * @return 如果列可以拉伸，返回真；否则返回假.
     */
    public boolean isColumnStretchable(int columnIndex) {
        return mStretchAllColumns || mStretchableColumns.get(columnIndex);
    }

    /**
     * <p>设置指定列是否可收缩.当行太宽时，表格可以收缩该列以提供更多空间.</p>
     *
     * <p>调用该方法会请求布局操作.</p>
     *
     * @param columnIndex 列的索引.
     * @param isShrinkable 如果列可以拉伸，设为真；否则设为假.默认是假.
     *
     * @attr ref android.R.styleable#TableLayout_shrinkColumns
     */
    public void setColumnShrinkable(int columnIndex, boolean isShrinkable) {
        mShrinkableColumns.put(columnIndex, isShrinkable);
        requestRowsLayout();
    }

    /**
     * <p>返回指定的列是否可收缩.</p>
     *
     * @param columnIndex 列的索引.
     * @return 如果列可以收缩，返回真；否则返回假.
     */
    public boolean isColumnShrinkable(int columnIndex) {
        return mShrinkAllColumns || mShrinkableColumns.get(columnIndex);
    }

    /**
     * <p>Applies the columns collapse status to a new row added to this
     * table. This method is invoked by PassThroughHierarchyChangeListener
     * upon child insertion.</p>
     *
     * <p>This method only applies to {@link android.widget.TableRow}
     * instances.</p>
     *
     * @param child the newly added child
     */
    private void trackCollapsedColumns(View child) {
        if (child instanceof TableRow) {
            final TableRow row = (TableRow) child;
            final SparseBooleanArray collapsedColumns = mCollapsedColumns;
            final int count = collapsedColumns.size();
            for (int i = 0; i < count; i++) {
                int columnIndex = collapsedColumns.keyAt(i);
                boolean isCollapsed = collapsedColumns.valueAt(i);
                // the collapse status is set only when the column should be
                // collapsed; otherwise, this might affect the default
                // visibility of the row's children
                if (isCollapsed) {
                    row.setColumnCollapsed(columnIndex, isCollapsed);
                }
            }
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void addView(View child) {
        super.addView(child);
        requestRowsLayout();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void addView(View child, int index) {
        super.addView(child, index);
        requestRowsLayout();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void addView(View child, ViewGroup.LayoutParams params) {
        super.addView(child, params);
        requestRowsLayout();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void addView(View child, int index, ViewGroup.LayoutParams params) {
        super.addView(child, index, params);
        requestRowsLayout();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        // enforce vertical layout
        measureVertical(widthMeasureSpec, heightMeasureSpec);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        // enforce vertical layout
        layoutVertical();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    void measureChildBeforeLayout(View child, int childIndex,
            int widthMeasureSpec, int totalWidth,
            int heightMeasureSpec, int totalHeight) {
        // when the measured child is a table row, we force the width of its
        // children with the widths computed in findLargestCells()
        if (child instanceof TableRow) {
            ((TableRow) child).setColumnsWidthConstraints(mMaxWidths);
        }

        super.measureChildBeforeLayout(child, childIndex,
                widthMeasureSpec, totalWidth, heightMeasureSpec, totalHeight);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    void measureVertical(int widthMeasureSpec, int heightMeasureSpec) {
        findLargestCells(widthMeasureSpec);
        shrinkAndStretchColumns(widthMeasureSpec);

        super.measureVertical(widthMeasureSpec, heightMeasureSpec);
    }

    /**
     * <p>Finds the largest cell in each column. For each column, the width of
     * the largest cell is applied to all the other cells.</p>
     *
     * @param widthMeasureSpec the measure constraint imposed by our parent
     */
    private void findLargestCells(int widthMeasureSpec) {
        boolean firstRow = true;

        // find the maximum width for each column
        // the total number of columns is dynamically changed if we find
        // wider rows as we go through the children
        // the array is reused for each layout operation; the array can grow
        // but never shrinks. Unused extra cells in the array are just ignored
        // this behavior avoids to unnecessary grow the array after the first
        // layout operation
        final int count = getChildCount();
        for (int i = 0; i < count; i++) {
            final View child = getChildAt(i);
            if (child.getVisibility() == GONE) {
                continue;
            }

            if (child instanceof TableRow) {
                final TableRow row = (TableRow) child;
                // forces the row's height
                final ViewGroup.LayoutParams layoutParams = row.getLayoutParams();
                layoutParams.height = LayoutParams.WRAP_CONTENT;

                final int[] widths = row.getColumnsWidths(widthMeasureSpec);
                final int newLength = widths.length;
                // this is the first row, we just need to copy the values
                if (firstRow) {
                    if (mMaxWidths == null || mMaxWidths.length != newLength) {
                        mMaxWidths = new int[newLength];
                    }
                    System.arraycopy(widths, 0, mMaxWidths, 0, newLength);
                    firstRow = false;
                } else {
                    int length = mMaxWidths.length;
                    final int difference = newLength - length;
                    // the current row is wider than the previous rows, so
                    // we just grow the array and copy the values
                    if (difference > 0) {
                        final int[] oldMaxWidths = mMaxWidths;
                        mMaxWidths = new int[newLength];
                        System.arraycopy(oldMaxWidths, 0, mMaxWidths, 0,
                                oldMaxWidths.length);
                        System.arraycopy(widths, oldMaxWidths.length,
                                mMaxWidths, oldMaxWidths.length, difference);
                    }

                    // the row is narrower or of the same width as the previous
                    // rows, so we find the maximum width for each column
                    // if the row is narrower than the previous ones,
                    // difference will be negative
                    final int[] maxWidths = mMaxWidths;
                    length = Math.min(length, newLength);
                    for (int j = 0; j < length; j++) {
                        maxWidths[j] = Math.max(maxWidths[j], widths[j]);
                    }
                }
            }
        }
    }

    /**
     * <p>Shrinks the columns if their total width is greater than the
     * width allocated by widthMeasureSpec. When the total width is less
     * than the allocated width, this method attempts to stretch columns
     * to fill the remaining space.</p>
     *
     * @param widthMeasureSpec the width measure specification as indicated
     *                         by this widget's parent
     */
    private void shrinkAndStretchColumns(int widthMeasureSpec) {
        // when we have no row, mMaxWidths is not initialized and the loop
        // below could cause a NPE
        if (mMaxWidths == null) {
            return;
        }

        // should we honor AT_MOST, EXACTLY and UNSPECIFIED?
        int totalWidth = 0;
        for (int width : mMaxWidths) {
            totalWidth += width;
        }

        int size = MeasureSpec.getSize(widthMeasureSpec) - mPaddingLeft - mPaddingRight;

        if ((totalWidth > size) && (mShrinkAllColumns || mShrinkableColumns.size() > 0)) {
            // oops, the largest columns are wider than the row itself
            // fairly redistribute the row's width among the columns
            mutateColumnsWidth(mShrinkableColumns, mShrinkAllColumns, size, totalWidth);
        } else if ((totalWidth < size) && (mStretchAllColumns || mStretchableColumns.size() > 0)) {
            // if we have some space left, we distribute it among the
            // expandable columns
            mutateColumnsWidth(mStretchableColumns, mStretchAllColumns, size, totalWidth);
        }
    }

    private void mutateColumnsWidth(SparseBooleanArray columns,
            boolean allColumns, int size, int totalWidth) {
        int skipped = 0;
        final int[] maxWidths = mMaxWidths;
        final int length = maxWidths.length;
        final int count = allColumns ? length : columns.size();
        final int totalExtraSpace = size - totalWidth;
        int extraSpace = totalExtraSpace / count;

        // Column's widths are changed: force child table rows to re-measure.
        // (done by super.measureVertical after shrinkAndStretchColumns.)
        final int nbChildren = getChildCount();
        for (int i = 0; i < nbChildren; i++) {
            View child = getChildAt(i);
            if (child instanceof TableRow) {
                child.forceLayout();
            }
        }

        if (!allColumns) {
            for (int i = 0; i < count; i++) {
                int column = columns.keyAt(i);
                if (columns.valueAt(i)) {
                    if (column < length) {
                        maxWidths[column] += extraSpace;
                    } else {
                        skipped++;
                    }
                }
            }
        } else {
            for (int i = 0; i < count; i++) {
                maxWidths[i] += extraSpace;
            }

            // we don't skip any column so we can return right away
            return;
        }

        if (skipped > 0 && skipped < count) {
            // reclaim any extra space we left to columns that don't exist
            extraSpace = skipped * extraSpace / (count - skipped);
            for (int i = 0; i < count; i++) {
                int column = columns.keyAt(i);
                if (columns.valueAt(i) && column < length) {
                    if (extraSpace > maxWidths[column]) {
                        maxWidths[column] = 0;
                    } else {
                        maxWidths[column] += extraSpace;
                    }
                }
            }
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public LayoutParams generateLayoutParams(AttributeSet attrs) {
        return new TableLayout.LayoutParams(getContext(), attrs);
    }

    /**
     * 返回宽度为 {@link android.view.ViewGroup.LayoutParams#MATCH_PARENT}，
     * 高度为 {@link android.view.ViewGroup.LayoutParams#WRAP_CONTENT}
     * 的布局参数集合.
     */
    @Override
    protected LinearLayout.LayoutParams generateDefaultLayoutParams() {
        return new LayoutParams();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected boolean checkLayoutParams(ViewGroup.LayoutParams p) {
        return p instanceof TableLayout.LayoutParams;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected LinearLayout.LayoutParams generateLayoutParams(ViewGroup.LayoutParams p) {
        return new LayoutParams(p);
    }

    @Override
    public void onInitializeAccessibilityEvent(AccessibilityEvent event) {
        super.onInitializeAccessibilityEvent(event);
        event.setClassName(TableLayout.class.getName());
    }

    @Override
    public void onInitializeAccessibilityNodeInfo(AccessibilityNodeInfo info) {
        super.onInitializeAccessibilityNodeInfo(info);
        info.setClassName(TableLayout.class.getName());
    }

    /**
     * <p>该类强制将子视图的宽度设为 {@link #MATCH_PARENT}，
     * 将没有设置高度的子视图的高度设为 {@link #WRAP_CONTENT}.</p>
     */
    @SuppressWarnings({"UnusedDeclaration"})
    public static class LayoutParams extends LinearLayout.LayoutParams {
        /**
         * {@inheritDoc}
         */
        public LayoutParams(Context c, AttributeSet attrs) {
            super(c, attrs);
        }

        /**
         * {@inheritDoc}
         */
        public LayoutParams(int w, int h) {
            super(MATCH_PARENT, h);
        }

        /**
         * {@inheritDoc}
         */
        public LayoutParams(int w, int h, float initWeight) {
            super(MATCH_PARENT, h, initWeight);
        }

        /**
         * <p>将子视图的宽度设为 {@link android.view.ViewGroup.LayoutParams}，
         * 高度设为 {@link android.view.ViewGroup.LayoutParams#WRAP_CONTENT}.</p>
         */
        public LayoutParams() {
            super(MATCH_PARENT, WRAP_CONTENT);
        }

        /**
         * {@inheritDoc}
         */
        public LayoutParams(ViewGroup.LayoutParams p) {
            super(p);
        }

        /**
         * {@inheritDoc}
         */
        public LayoutParams(MarginLayoutParams source) {
            super(source);
        }

        /**
         * <p>如果没有指定布局高度，则修改行的宽度为
         * {@link android.view.ViewGroup.LayoutParams#MATCH_PARENT}；
         * 高度为 {@link android.view.ViewGroup.LayoutParams#WRAP_CONTENT}.</p>
         *
         * @param a 特定风格的属性集合.
         * @param widthAttr 查询宽度属性用的索引.
         * @param heightAttr 查询高度属性用的索引.
         */
        @Override
        protected void setBaseAttributes(TypedArray a,
                int widthAttr, int heightAttr) {
            this.width = MATCH_PARENT;
            if (a.hasValue(heightAttr)) {
                this.height = a.getLayoutDimension(heightAttr, "layout_height");
            } else {
                this.height = WRAP_CONTENT;
            }
        }
    }

    /**
     * <p>A pass-through listener acts upon the events and dispatches them
     * to another listener. This allows the table layout to set its own internal
     * hierarchy change listener without preventing the user to setup his.</p>
     */
    private class PassThroughHierarchyChangeListener implements
            OnHierarchyChangeListener {
        private OnHierarchyChangeListener mOnHierarchyChangeListener;

        /**
         * {@inheritDoc}
         */
        public void onChildViewAdded(View parent, View child) {
            trackCollapsedColumns(child);

            if (mOnHierarchyChangeListener != null) {
                mOnHierarchyChangeListener.onChildViewAdded(parent, child);
            }
        }

        /**
         * {@inheritDoc}
         */
        public void onChildViewRemoved(View parent, View child) {
            if (mOnHierarchyChangeListener != null) {
                mOnHierarchyChangeListener.onChildViewRemoved(parent, child);
            }
        }
    }
}
