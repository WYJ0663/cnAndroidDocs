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
 *
 * 绑定由两个阶段组成.首先，如果{@link android.widget.SimpleCursorAdapter.ViewBinder}
 * 可用，则执行
 * {@link ViewBinder#setViewValue(android.view.View, android.database.Cursor, int)}.
 * 如果返回在为真，代表发生了绑定.如果返回值为假，并且要绑定的是文本视图，则执行
 * {@link #setViewText(TextView, String)} 方法.如果返回值为假，并且要绑定的是
 * ImageView，则执行{@link #setViewImage(ImageView, String)}.如果没有适当的绑定发生，
 * 将抛出{@link IllegalStateException}.
 * 
 * 如果该适配器与过滤功能同时使用，比如在
 * {@link android.widget.AutoCompleteTextView}中，可以使用
 * {@link android.widget.SimpleCursorAdapter.CursorToStringConverter}和
 * {@link android.widget.FilterQueryProvider}接口，用于控制过滤过程.
 * 详细信息可以参考
 * {@link #convertToString(android.database.Cursor)}和
 * {@link #runQueryOnBackgroundThread(CharSequence)}.
 */
public class SimpleCursorAdapter extends ResourceCursorAdapter {
    /**
     * A list of columns containing the data to bind to the UI.
     * This field should be made private, so it is hidden from the SDK.
     * {@hide}
     */
    protected int[] mFrom;
    /**
     * A list of View ids representing the views to which the data must be bound.
     * This field should be made private, so it is hidden from the SDK.
     * {@hide}
     */
    protected int[] mTo;

    private int mStringConversionColumn = -1;
    private CursorToStringConverter mCursorToStringConverter;
    private ViewBinder mViewBinder;

    String[] mOriginalFrom;

    /**
     * 构造函数，启用自动再检索功能.
     *
     * @deprecated 该选项已经废止，因为他会在应用程序的 UI 线程中执行
     * 游标的检索，会导致响应变慢甚至发生应用程序无响应错误.
     * 请使用带有 {@link android.content.CursorLoader}
     * 的 {@link android.app.LoaderManager} 来代替.
     * 
     * @param context 与运行中的 SimpleListItemFactory 关联的 ListView 的上下文
     * @param layout 为该列表定义了视图的布局文件标识.布局文件应该至少包括“to”中定义的视图
     * @param c 数据库游标.如果游标不可用，可设为空.
     * @param from 代表要绑定到UI的数据的列名列表.如果游标不可用，可设为空.
     * @param to 用于显示“from”参数的列的视图.应该都是 TextViews.
     *           视图与from参数按位置一一对应.如果游标不可用，可设为空.
     */
    @Deprecated
    public SimpleCursorAdapter(Context context, int layout, Cursor c, String[] from, int[] to) {
        super(context, layout, c);
        mTo = to;
        mOriginalFrom = from;
        findColumns(c, from);
    }

    /**
     * 标准构造函数.
     * 
     * @param context 与运行中的 SimpleListItemFactory 关联的 ListView 的上下文
     * @param layout 为该列表定义了视图的布局文件标识.布局文件应该至少包括“to”中定义的视图
     * @param c 数据库游标.如果游标不可用，可设为空.
     * @param from 代表要绑定到UI的数据的列名列表.如果游标不可用，可设为空.
     * @param to 用于显示“from”参数的列的视图.应该都是 TextViews.
     *           视图与from参数按位置一一对应.如果游标不可用，可设为空.
     * @param flags 用于决定适配器行为的标志位.与 {@link CursorAdapter#CursorAdapter(Context, Cursor, int)} 相同.
     */
    public SimpleCursorAdapter(Context context, int layout, Cursor c, String[] from,
            int[] to, int flags) {
        super(context, layout, c, flags);
        mTo = to;
        mOriginalFrom = from;
        findColumns(c, from);
    }

    /**
     * 与视图绑定有两个阶段.第一阶段：如果SimpleCursorAdapter.ViewBinder可用，将会调用setViewValue(android.view.View, android.database.Cursor, int)方法.该方法返回true就说明绑定成功，否则返回false ，这就到了第二阶段，SimpleCursorAdapter内部开始自行绑定，过程是这样的，若绑定到TextView上，调用setViewText();若绑定到ImageView上，调用setViewImage();如果视图不是TextView或ImageView则抛出IllegalStateException异常.
     * 将传入构造函数“to”参数的所有字段名和他们对应的“from”参数的绑定在一起.
     *
     * 绑定由两个阶段组成.首先，如果{@link android.widget.SimpleCursorAdapter.ViewBinder}
     * 可用，则执行
     * {@link ViewBinder#setViewValue(android.view.View, android.database.Cursor, int)}.
     * 如果返回在为真，代表发生了绑定.如果返回值为假，并且要绑定的是文本视图，则执行
     * {@link #setViewText(TextView, String)} 方法.如果返回值为假，并且要绑定的是
     * ImageView，则执行{@link #setViewImage(ImageView, String)}.如果没有适当的绑定发生，
     * 将抛出{@link IllegalStateException}.
     *
     * @throws IllegalStateException 如果无法绑定.
     * 
     * @see android.widget.CursorAdapter#bindView(android.view.View,
     *      android.content.Context, android.database.Cursor)
     * @see #getViewBinder()
     * @see #setViewBinder(android.widget.SimpleCursorAdapter.ViewBinder)
     * @see #setViewImage(ImageView, String)
     * @see #setViewText(TextView, String)
     */
    @Override
    public void bindView(View view, Context context, Cursor cursor) {
        final ViewBinder binder = mViewBinder;
        final int count = mTo.length;
        final int[] from = mFrom;
        final int[] to = mTo;

        for (int i = 0; i < count; i++) {
            final View v = view.findViewById(to[i]);
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
                        throw new IllegalStateException(v.getClass().getName() + " is not a " +
                                " view that can be bounds by this SimpleCursorAdapter");
                    }
                }
            }
        }
    }

    /**
     * 返回用于绑定数据到视图的{@link ViewBinder}.
     *
     * @return ViewBinder 或者当绑定器不存在时返回空.
     *
     * @see #bindView(android.view.View, android.content.Context, android.database.Cursor)
     * @see #setViewBinder(android.widget.SimpleCursorAdapter.ViewBinder)
     */
    public ViewBinder getViewBinder() {
        return mViewBinder;
    }

    /**
     * 设置用于绑定数据到视图的绑定器.
     *
     * @param viewBinder 用于将数据绑定到视图的绑定器，若要移除既存绑定器可以设为空.
     *
     * @see #bindView(android.view.View, android.content.Context, android.database.Cursor)
     * @see #getViewBinder()
     */
    public void setViewBinder(ViewBinder viewBinder) {
        mViewBinder = viewBinder;
    }

    /**
     * 由bindView()调用，当没有既存的 ViewBinder 或者 ViewBinder
     * 不能处理到ImageView的绑定时，为ImageView设置图像.
     *
     * 默认情况，字符串值作为图像资源对待.如果该值不能作为图像资源，
     * 将其作为图像URI对待.
     *
     * 为需要过滤从数据库中取得的字符串的适配器重写而准备的函数.
     *
     * @param v 要设置图像的ImageView.
     * @param value 从游标取得的值.
     */
    public void setViewImage(ImageView v, String value) {
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
     * 返回用于取得代表游标的字符串的列索引.
     *
     * @return 当前游标的有效的列索引或者-1.
     *
     * @see android.widget.CursorAdapter#convertToString(android.database.Cursor)
     * @see #setStringConversionColumn(int) 
     * @see #setCursorToStringConverter(android.widget.SimpleCursorAdapter.CursorToStringConverter)
     * @see #getCursorToStringConverter()
     */
    public int getStringConversionColumn() {
        return mStringConversionColumn;
    }

    /**
     * 定义代表游标的字符串的列索引.该列用于在当前 CursorToStringConverter 为空时，
     * 将游标转换为字符串时使用.
     *
     * @param stringConversionColumn 使用默认转换机制时，当前游标的有效的列索引或者-1.
     *
     * @see android.widget.CursorAdapter#convertToString(android.database.Cursor)
     * @see #getStringConversionColumn()
     * @see #setCursorToStringConverter(android.widget.SimpleCursorAdapter.CursorToStringConverter)
     * @see #getCursorToStringConverter()
     */
    public void setStringConversionColumn(int stringConversionColumn) {
        mStringConversionColumn = stringConversionColumn;
    }

    /**
     * 返回用于将过滤游标转换为字符串的转换器.
     *
     * @return 如果转换器不存在返回空，否则返回
     *         {@link android.widget.SimpleCursorAdapter.CursorToStringConverter}
     *         的实例.
     *
     * @see #setCursorToStringConverter(android.widget.SimpleCursorAdapter.CursorToStringConverter)
     * @see #getStringConversionColumn()
     * @see #setStringConversionColumn(int)
     * @see android.widget.CursorAdapter#convertToString(android.database.Cursor)
     */
    public CursorToStringConverter getCursorToStringConverter() {
        return mCursorToStringConverter;
    }

    /**
     * 设置用于将过滤游标转换为字符串的转换器.
     *
     * @param cursorToStringConverter 将游标转为字符串的转换器，要删除时可设为空.
     *
     * @see #setCursorToStringConverter(android.widget.SimpleCursorAdapter.CursorToStringConverter) 
     * @see #getStringConversionColumn()
     * @see #setStringConversionColumn(int)
     * @see android.widget.CursorAdapter#convertToString(android.database.Cursor)
     */
    public void setCursorToStringConverter(CursorToStringConverter cursorToStringConverter) {
        mCursorToStringConverter = cursorToStringConverter;
    }

    /**
     * 返回由当前 CursorToStringConverter 指定的代表游标的 CharSequence.
     * 如果没有指定 CursorToStringConverter，则用字符串转换列代表.
     * 如果转换列为 -1，对于空游标返回空字符串，其他返回 Cursor.toString().
     *
     * @param cursor 转换为 CharSequence 的游标.
     *
     * @return 代表游标的非空 CharSequence.
     */
    @Override
    public CharSequence convertToString(Cursor cursor) {
        if (mCursorToStringConverter != null) {
            return mCursorToStringConverter.convertToString(cursor);
        } else if (mStringConversionColumn > -1) {
            return cursor.getString(mStringConversionColumn);
        }

        return super.convertToString(cursor);
    }

    /**
     * Create a map from an array of strings to an array of column-id integers in cursor c.
     * If c is null, the array will be discarded.
     *
     * @param c the cursor to find the columns from
     * @param from the Strings naming the columns of interest
     */
    private void findColumns(Cursor c, String[] from) {
        if (c != null) {
            int i;
            int count = from.length;
            if (mFrom == null || mFrom.length != count) {
                mFrom = new int[count];
            }
            for (i = 0; i < count; i++) {
                mFrom[i] = c.getColumnIndexOrThrow(from[i]);
            }
        } else {
            mFrom = null;
        }
    }

    @Override
    public Cursor swapCursor(Cursor c) {
        // super.swapCursor() will notify observers before we have
        // a valid mapping, make sure we have a mapping before this
        // happens
        findColumns(c, mOriginalFrom);
        return super.swapCursor(c);
    }
    
    /**
     * 同时改变游标及绑定到视图的游标列.
     *  
     * @param c 数据库游标.如果游标不存在可以为空.
     * @param from 代表要绑定到UI的数据的列名列表.如果游标不存在可以为空.
     * @param to 用于显示“from”参数中数据的视图.应该都是文本视图.
     *           视图的位置与数据的位置一一对应.如果游标不存在可以为空.
     */
    public void changeCursorAndColumns(Cursor c, String[] from, int[] to) {
        mOriginalFrom = from;
        mTo = to;
        // super.changeCursor() will notify observers before we have
        // a valid mapping, make sure we have a mapping before this
        // happens
        findColumns(c, mOriginalFrom);
        super.changeCursor(c);
    }

    /**
     * 该接口可用于 SimpleCursorAdapter 的外部客户端将游标绑定到视图.
     * 
     * 对于 SimpleCursorAdapter 不直接支持或要改变 SimpleCursorAdapter
     * 对游标的绑定方式时，你应该使用该类将游标的值绑定到视图.
     *
     * @see SimpleCursorAdapter#bindView(android.view.View, android.content.Context, android.database.Cursor)
     * @see SimpleCursorAdapter#setViewImage(ImageView, String) 
     * @see SimpleCursorAdapter#setViewText(TextView, String)
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

    /**
     * 该接口允许 SimpleCursorAdapter 的外部客户端定义游标如何转换为字符串.
     *
     * @see android.widget.CursorAdapter#convertToString(android.database.Cursor)
     */
    public static interface CursorToStringConverter {
        /**
         * 返回代表指定游标的CharSequence.
         *
         * @param cursor 要求返回 CharSequence 的游标.
         *
         * @return 代表指定游标的非空 CharSequence.
         */
        CharSequence convertToString(Cursor cursor);
    }

}
