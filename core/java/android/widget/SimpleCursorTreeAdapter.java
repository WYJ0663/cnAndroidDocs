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
import android.database.Cursor;
import android.net.Uri;
import android.view.View;

/**
 * 用于将游标列映射到XML文件中定义的文本视图或图像视图的简单适配器.
 * 你可以指定有哪些列，由那些视图显示这些列的内容，并通过XML定义这些视图的呈现.
 * 可以为分组和子视图分别指定XML文件.
 *
 * 绑定由两个阶段组成.首先，如果
 * {@link android.widget.SimpleCursorTreeAdapter.ViewBinder}可用，则执行
 * {@link ViewBinder#setViewValue(android.view.View, android.database.Cursor, int)}.
 * 如果返回在为真，代表发生了绑定.如果返回值为假，并且要绑定的是文本视图，则执行
 * {@link #setViewText(TextView, String)} 方法.如果返回值为假，并且要绑定的是
 * ImageView，则执行{@link #setViewImage(ImageView, String)}.如果没有适当的绑定发生，
 * 将抛出{@link IllegalStateException}.
 */
public abstract class SimpleCursorTreeAdapter extends ResourceCursorTreeAdapter {
    
    /** The name of the columns that contain the data to display for a group. */
    private String[] mGroupFromNames;
    
    /** The indices of columns that contain data to display for a group. */
    private int[] mGroupFrom;
    /**
     * The View IDs that will display a group's data fetched from the
     * corresponding column.
     */
    private int[] mGroupTo;

    /** The name of the columns that contain the data to display for a child. */
    private String[] mChildFromNames;
    
    /** The indices of columns that contain data to display for a child. */
    private int[] mChildFrom;
    /**
     * The View IDs that will display a child's data fetched from the
     * corresponding column.
     */
    private int[] mChildTo;
    
    /**
     * View binder, if supplied
     */
    private ViewBinder mViewBinder;

    /**
     * 构造函数
     * 
     * @param context 运行时，与 {@link SimpleCursorTreeAdapter}
     *            关联的 {@link ExpandableListView} 的上下文.
     * @param cursor 数据库游标
     * @param collapsedGroupLayout 定义组收起时视图的XML资源布局.
     *            该布局文件应当至少包括所有在groupTo中所定义的视图.
     * @param expandedGroupLayout 定义组展开时视图的XML资源布局.
     *            该布局文件应当至少包括所有在groupTo中所定义的视图.
     * @param groupFrom 在组中显示的数据的列名.
     * @param groupTo 用于显示“from”参数中的列内容的分组视图（来自分组布局）.
     *            这些视图应该都是文本视图或图像视图.视图与列一一对应.
     * @param childLayout 布局资源文件标识，其定义的是子视图的布局样式
     *            (不包括最后一个子视图)，内部至少要包含参数 “childTo”中指定的视图ID.
     * @param lastChildLayout 布局资源文件标识，其定义的是最后一个子视图的布局文件.
     *            该布局文件至少要包含参数“childTo”中定义的视图ID.
     * @param childFrom 用于子视图显示的数据的列名列表.
     * @param childTo 用于显示“from”参数中指定列的子视图（来自子布局文件）.
     *            这些视图应该都为文本视图或图像视图.视图与给定参数列的顺序是一一对应的.
     */
    public SimpleCursorTreeAdapter(Context context, Cursor cursor, int collapsedGroupLayout,
            int expandedGroupLayout, String[] groupFrom, int[] groupTo, int childLayout,
            int lastChildLayout, String[] childFrom, int[] childTo) {
        super(context, cursor, collapsedGroupLayout, expandedGroupLayout, childLayout,
                lastChildLayout);
        init(groupFrom, groupTo, childFrom, childTo);
    }

    /**
     * 构造函数
     * 
     * @param context 运行时，与 {@link SimpleCursorTreeAdapter}
     *            关联的 {@link ExpandableListView} 的上下文.
     * @param cursor 数据库游标
     * @param collapsedGroupLayout 定义组收起时视图的XML资源布局.
     *            该布局文件应当至少包括所有在groupTo中所定义的视图.
     * @param expandedGroupLayout 定义组展开时视图的XML资源布局.
     *            该布局文件应当至少包括所有在groupTo中所定义的视图.
     * @param groupFrom 在组中显示的数据的列名.
     * @param groupTo 用于显示“from”参数中的列内容的分组视图（来自分组布局）.
     *            这些视图应该都是文本视图或图像视图.视图与列一一对应.
     * @param childLayout 布局资源文件标识ID，其定义的是子视图的布局样式
     *            (不包括最后一个子视图)，内部至少要包含参数 “childTo”中指定的视图ID.
     * @param childFrom 用于子视图显示的数据的列名列表.
     * @param childTo 用于显示“from”参数中指定列的子视图（来自子布局文件）.
     *            这些视图应该都为文本视图或图像视图.视图与给定参数列的顺序是一一对应的.
     */
    public SimpleCursorTreeAdapter(Context context, Cursor cursor, int collapsedGroupLayout,
            int expandedGroupLayout, String[] groupFrom, int[] groupTo,
            int childLayout, String[] childFrom, int[] childTo) {
        super(context, cursor, collapsedGroupLayout, expandedGroupLayout, childLayout);
        init(groupFrom, groupTo, childFrom, childTo);
    }

    /**
     * 构造函数
     * 
     * @param context 运行时，与 {@link SimpleCursorTreeAdapter}
     *            关联的 {@link ExpandableListView} 的上下文.
     * @param cursor 数据库游标
     * @param groupLayout 定义了分组视图的资源文件的标识.布局文件至少要包含groupTo中定义的视图.
     * @param groupFrom 在组中显示的数据的列名.
     * @param groupTo 用于显示“from”参数中的列内容的分组视图（来自分组布局）.
     * @param childLayout 布局资源文件标识ID，其定义的是子视图的布局样式
     *            (不包括最后一个子视图)，内部至少要包含参数 “childTo”中指定的视图ID.
     * @param childFrom 用于子视图显示的数据的列名列表.
     * @param childTo 用于显示“from”参数中指定列的子视图（来自子布局文件）.
     *            这些视图应该都为文本视图或图像视图.视图与给定参数列的顺序是一一对应的.
     */
    public SimpleCursorTreeAdapter(Context context, Cursor cursor, int groupLayout,
            String[] groupFrom, int[] groupTo, int childLayout, String[] childFrom,
            int[] childTo) {
        super(context, cursor, groupLayout, childLayout);
        init(groupFrom, groupTo, childFrom, childTo);
    }

    private void init(String[] groupFromNames, int[] groupTo, String[] childFromNames,
            int[] childTo) {
        
        mGroupFromNames = groupFromNames;
        mGroupTo = groupTo;
        
        mChildFromNames = childFromNames;
        mChildTo = childTo;
    }
    
    /**
     * 返回用于绑定数据到视图的{@link ViewBinder}.
     *
     * @return ViewBinder 或者当绑定器不存在时返回空.
     *
     * @see #setViewBinder(android.widget.SimpleCursorTreeAdapter.ViewBinder)
     */
    public ViewBinder getViewBinder() {
        return mViewBinder;
    }

    /**
     * 设置用于绑定数据到视图的绑定器.
     *
     * @param viewBinder 用于将数据绑定到视图的绑定器，若要移除既存绑定器可以设为空.
     *
     * @see #getViewBinder()
     */
    public void setViewBinder(ViewBinder viewBinder) {
        mViewBinder = viewBinder;
    }

    private void bindView(View view, Context context, Cursor cursor, int[] from, int[] to) {
        final ViewBinder binder = mViewBinder;
        
        for (int i = 0; i < to.length; i++) {
            View v = view.findViewById(to[i]);
            if (v != null) {
                boolean bound = false;
                if (binder != null) {
                    bound = binder.setViewValue(v, cursor, from[i]);
                }
                
                if (!bound) {
                    String text = cursor.getString(from[i]);
                    if (text == null) {
                        text = "";
                    }
                    if (v instanceof TextView) {
                        setViewText((TextView) v, text);
                    } else if (v instanceof ImageView) {
                        setViewImage((ImageView) v, text);
                    } else {
                        throw new IllegalStateException("SimpleCursorTreeAdapter can bind values" +
                                " only to TextView and ImageView!");
                    }
                }
            }
        }
    }
    
    private void initFromColumns(Cursor cursor, String[] fromColumnNames, int[] fromColumns) {
        for (int i = fromColumnNames.length - 1; i >= 0; i--) {
            fromColumns[i] = cursor.getColumnIndexOrThrow(fromColumnNames[i]);
        }
    }
    
    @Override
    protected void bindChildView(View view, Context context, Cursor cursor, boolean isLastChild) {
        if (mChildFrom == null) {
            mChildFrom = new int[mChildFromNames.length];
            initFromColumns(cursor, mChildFromNames, mChildFrom);
        }
        
        bindView(view, context, cursor, mChildFrom, mChildTo);
    }

    @Override
    protected void bindGroupView(View view, Context context, Cursor cursor, boolean isExpanded) {
        if (mGroupFrom == null) {
            mGroupFrom = new int[mGroupFromNames.length];
            initFromColumns(cursor, mGroupFromNames, mGroupFrom);
        }
        
        bindView(view, context, cursor, mGroupFrom, mGroupTo);
    }

    /**
     * 由bindView()调用，为ImageView设置图像.默认情况，字符串值作为图像URI对待.
     * 为需要过滤从数据库中取得的字符串的适配器重写而准备的函数.
     *
     * @param v 要设置图像的ImageView.
     * @param value 从游标取得的值.
     */
    protected void setViewImage(ImageView v, String value) {
        try {
            v.setImageResource(Integer.parseInt(value));
        } catch (NumberFormatException nfe) {
            v.setImageURI(Uri.parse(value));
        }
    }

    /**
     * 由bindView()调用，当没有既存的 ViewBinder 或者 ViewBinder
     * 不能处理到文本视图的绑定时，为文本视图设置文本.
     *
     * 为需要过滤从数据库中取得的字符串的适配器重写而准备的函数.
     * 
     * @param v 要设置文本的文本视图.
     * @param text 要设置到文本视图中的文本.
     */
    public void setViewText(TextView v, String text) {
        v.setText(text);
    }

    /**
     * 该接口可用于 SimpleCursorTreeAdapter 的外部客户端将游标绑定到视图.
     * 
     * 对于 SimpleCursorTreeAdapter 不直接支持或要改变 SimpleCursorTreeAdapter
     * 对游标的绑定方式时，你应该使用该类将游标的值绑定到视图.
     *
     * @see SimpleCursorTreeAdapter#setViewImage(ImageView, String) 
     * @see SimpleCursorTreeAdapter#setViewText(TextView, String)
     */
    public static interface ViewBinder {
        /**
         * 绑定指定位置的游标列到指定视图.
         *
         * 若该 ViewBinder 处理了绑定，该方法应该返回真.若该方法返回假，
         * SimpleCursorAdapter 将尝试去执行绑定操作.
         *
         * @param view 要绑定数据的视图.
         * @param cursor 用于取得数据的游标.
         * @param columnIndex 要取得数据对应的游标中的列.
         *
         * @return 若数据已绑定到视图返回真，否则返回假.
         */
        boolean setViewValue(View view, Cursor cursor, int columnIndex);
    }
}
