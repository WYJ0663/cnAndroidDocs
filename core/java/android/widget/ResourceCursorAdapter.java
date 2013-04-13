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
import android.database.Cursor;
import android.view.View;
import android.view.ViewGroup;
import android.view.LayoutInflater;


/**
 * 根据XML文件的定义创建视图的简单适配器.
 * 你可以 指定定义了视图外观的XML文件.
 */
public abstract class ResourceCursorAdapter extends CursorAdapter {
    private int mLayout;

    private int mDropDownLayout;
    
    private LayoutInflater mInflater;
    
    /**
     * 构造函数，启用自动再检索功能.
     *
     * @deprecated 该选项已经废止，因为他会在应用程序的 UI 线程中执行
     * 游标的检索，会导致响应变慢甚至发生应用程序无响应错误.
     * 请使用带有 {@link android.content.CursorLoader}
     * 的 {@link android.app.LoaderManager} 来代替.
     * 
     * @param context 与正在运行的 SimpleListItemFactory 关联的列表视图的上下文.
     * @param layout 为该列表条目定义视图的布局文件资源标识.除非你之后重载它们，
     *               否则会同时生成列表条目视图和下拉视图.
     * @param c 用于取得数据的游标
     */
    @Deprecated
    public ResourceCursorAdapter(Context context, int layout, Cursor c) {
        super(context, c);
        mLayout = mDropDownLayout = layout;
        mInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }
    
    /**
     * 与 {@link CursorAdapter#CursorAdapter(Context, Cursor, boolean)} 的默认行为相同的构造函数；
     * 不推荐使用该函数，请使用 {@link #ResourceCursorAdapter(Context, int, Cursor, int)} 代替.
     * 使用该构造函数时，总会设置{@link #FLAG_REGISTER_CONTENT_OBSERVER}标志位.
     * 
     * @param context 与正在运行的 SimpleListItemFactory 关联的列表视图的上下文.
     * @param layout 为该列表条目定义视图的布局文件资源标识.除非你之后重载它们，
     *               否则会同时生成列表条目视图和下拉视图.
     * @param c 用于取得数据的游标
     * @param autoRequery 如果此参数为真，当适配器的数据发生变化的时，
     *                    适配器会调用游标的 requery()方法，保持显示最新数据。
     *                    Using true here is discouraged.
     */
    public ResourceCursorAdapter(Context context, int layout, Cursor c, boolean autoRequery) {
        super(context, c, autoRequery);
        mLayout = mDropDownLayout = layout;
        mInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    /**
     * 标准构造函数.
     * 
     * @param context 与运行中的 SimpleListItemFactory 关联的 ListView 的上下文
     * @param layout 为该列表定义了视图的布局文件标识.布局文件应该至少包括“to”中定义的视图
     * @param c 数据库游标.如果游标不可用，可设为空.
     * @param flags 用于决定适配器行为的标志位.与 {@link CursorAdapter#CursorAdapter(Context, Cursor, int)} 相同.
     */
    public ResourceCursorAdapter(Context context, int layout, Cursor c, int flags) {
        super(context, c, flags);
        mLayout = mDropDownLayout = layout;
        mInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    /**
     * 根据指定的 xml 文件创建视图
     * 
     * @see android.widget.CursorAdapter#newView(android.content.Context,
     *      android.database.Cursor, ViewGroup)
     */
    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        return mInflater.inflate(mLayout, parent, false);
    }

    @Override
    public View newDropDownView(Context context, Cursor cursor, ViewGroup parent) {
        return mInflater.inflate(mDropDownLayout, parent, false);
    }

    /**
     * <p>设置列表条目视图的布局资源.</p>
     *
     * @param layout 用于创建列表条目视图的布局资源.
     */
    public void setViewResource(int layout) {
        mLayout = layout;
    }
    
    /**
     * <p>设置下拉视图的布局资源.</p>
     *
     * @param dropDownLayout 用于创建下拉视图的布局资源.
     */
    public void setDropDownViewResource(int dropDownLayout) {
        mDropDownLayout = dropDownLayout;
    }
}
