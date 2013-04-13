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

import android.view.View;
import android.view.ViewGroup;

/**
 * 可以实现为 {@link ExpandableListAdapter} 带来 {@link Adapter} 的视图类型机制的附加方法.
 * <p>
 * {@link ExpandableListAdapter} 声明它有一个用于分组项目的视图类型和一个用于子项目的视图类型.
 * 这适用于大多数的 {@link ExpandableListView}，也可以转换为复杂的 {@link ExpandableListView}.
 * </p>
 * 列表包含不同类型的分组视图或子视图，可以使用实现了该接口的适配器.这种方法会重用视图并提供给
 * {@link android.widget.ExpandableListAdapter#getGroupView(int, boolean, View, ViewGroup)}
 * 和
 * {@link android.widget.ExpandableListAdapter#getChildView(int, int, boolean, View, ViewGroup)}
 * 方法，作为适当的分组视图或子视图类型，结果就是更有效的重用之前创建的视图.
 */
public interface HeterogeneousExpandableList {
    /**
     * 取得指定的分组条目的由
     * {@link android.widget.ExpandableListAdapter#getGroupView(int, boolean, View, ViewGroup)}
     * 创建的分组视图的类型.
     * 
     * @param groupPosition 分组条目的位置.
     * @return 代表分组视图类型的整数.如果一个视图可以通过
     *         {@link android.widget.ExpandableListAdapter#getGroupView(int, boolean, View, ViewGroup)}
     *         转换为另一个视图，那么他们可以共享相同的类型.
     *         注意：整数的范围必须在 0 到 {@link #getGroupTypeCount} - 1 之间.
     *         也可以返回 {@link android.widget.Adapter#IGNORE_ITEM_VIEW_TYPE}.
     * @see android.widget.Adapter#IGNORE_ITEM_VIEW_TYPE
     * @see #getGroupTypeCount()
     */
    int getGroupType(int groupPosition);

    /**
     * 取得由
     * {@link android.widget.ExpandableListAdapter#getChildView(int, int, boolean, View, ViewGroup)}
     * 创建的指定的子视图的类型.
     * 
     * @param groupPosition 包含子条目的分组条目的位置.
     * @param childPosition 分组中的子条目的位置.
     * @return 代表子视图类型的整数.如果一个视图可以通过
     *         {@link android.widget.ExpandableListAdapter#getChildView(int, int, boolean, View, ViewGroup)}
     *         转换为另一个视图，那么他们可以共享相同的类型.
     *         注意：整数的范围必须在 0 到 {@link #getChildTypeCount} - 1 之间.
     *         也可以返回 {@link android.widget.Adapter#IGNORE_ITEM_VIEW_TYPE}.
     * @see android.widget.Adapter#IGNORE_ITEM_VIEW_TYPE
     * @see #getChildTypeCount()
     */
    int getChildType(int groupPosition, int childPosition);

    /**
     * <p>
     * 返回由
     * {@link android.widget.ExpandableListAdapter#getChildView(int, int, boolean, View, ViewGroup)}
     * 创建的分组视图类型的个数.每种类型代表可以由
     * {@link android.widget.ExpandableListAdapter#getChildView(int, int, boolean, View, ViewGroup)}
     * 转换的视图的集合.如果适配器对所有的分组元素都返回同一种类型，该方法返回 1.
     * </p>
     * 该方法仅在适配器为 {@link AdapterView}时调用.
     * 
     * @return 返回由该适配器创建的分组视图类型的个数.
     * @see #getChildTypeCount()
     * @see #getGroupType(int)
     */
    int getGroupTypeCount();

    /**
     * <p>
     * 返回由
     * {@link android.widget.ExpandableListAdapter#getChildView(int, int, boolean, View, ViewGroup)}
     * 创建的子视图类型的个数.每种类型代表可以由
     * {@link android.widget.ExpandableListAdapter#getChildView(int, int, boolean, View, ViewGroup)}
     * 转换的任意分组中视图的集合.如果适配器对所有的子元素都返回同一种类型，
     * 该方法返回 1.
     * </p>
     * 该方法仅在适配器为 {@link AdapterView}时调用.
     * 
     * @return 由适配器创建的子视图类型总数.
     * @see #getGroupTypeCount()
     * @see #getChildType(int, int)
     */
    int getChildTypeCount();
}
