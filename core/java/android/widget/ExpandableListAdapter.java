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

import android.database.DataSetObserver;
import android.view.View;
import android.view.ViewGroup;

/**
 * 这个适配器在 {@link ExpandableListView} 和底层数据之间起到了一个衔接的作用.
 * 该接口的实现类提供了访问子元素（以组的形式将它们分类）的数据；
 * 同时，也提供了为子元素和组创建相应的{@link View 视图}的功能.
 */
public interface ExpandableListAdapter {
    /**
     * @see Adapter#registerDataSetObserver(DataSetObserver)
     */
    void registerDataSetObserver(DataSetObserver observer);

    /**
     * @see Adapter#unregisterDataSetObserver(DataSetObserver)
     */
    void unregisterDataSetObserver(DataSetObserver observer);

    /**
     * 取得分组数.
     * 
     * @return 分组数.
     */
    int getGroupCount();

    /**
     * 取得指定分组的子元素数.
     * 
     * @param groupPosition 要取得子元素个数的分组位置.
     * @return 指定分组的子元素个数.
     */
    int getChildrenCount(int groupPosition);

    /**
     * 取得与给定分组关联的数据.
     * 
     * @param groupPosition 分组的位置.
     * @return 指定分组的数据.
     */
    Object getGroup(int groupPosition);
    
    /**
     * 取得与指定分组、指定子项目关联的数据.
     * 
     * @param groupPosition 包含子视图的分组的位置.
     * @param childPosition 指定的分组中的子视图的位置.
     * @return 与子视图关联的数据.
     */
    Object getChild(int groupPosition, int childPosition);

    /**
     * 取得指定分组的ID.该组ID必须在组中是唯一的.组合的ID
     * （参见{@link #getCombinedGroupId(long)}）
     * 必须不同于其他所有ID（分组及子项目的ID）.
     * 
     * @param groupPosition 要取得ID的分组位置.
     * @return 与分组关联的ID.
     */
    long getGroupId(int groupPosition);

    /**
     * 取得给定分组中给定子视图的ID. 该组ID必须在组中是唯一的.组合的ID
     * （参见{@link #getCombinedGroupId(long)}）
     * 必须不同于其他所有ID（分组及子项目的ID）.
     * 
     * @param groupPosition 包含子视图的分组的位置.
     * @param childPosition 要取得ID的指定的分组中的子视图的位置.
     * @return 与子视图关联的ID.
     */
    long getChildId(int groupPosition, int childPosition);

    /**
     * 是否指定分组视图及其子视图的ID对应的后台数据改变也会保持该ID.
     * 
     * @return 是否相同的ID总是指向同一个对象.
     * @see Adapter#hasStableIds()
     */
    boolean hasStableIds();

    /**
     * 取得用于显示给定分组的视图. 这个方法仅返回分组的视图对象，
     * 要想获取子元素的视图对象，就需要调用
     * {@link #getChildView(int, int, boolean, View, ViewGroup)}.
     * 
     * @param groupPosition 决定返回哪个视图的组位置 .
     * @param isExpanded 该组是展开状态还是收起状态 .
     * @param convertView 如果可能，重用旧的视图对象.
     *            使用前你应该保证视图对象为非空，并且是否是合适的类型.
     *            如果该对象不能转换为可以正确显示数据的视图，该方法就创建新视图.
     *            不保证使用先前由
     *            {@link #getGroupView(int, boolean, View, ViewGroup)}创建的视图.
     * @param parent 该视图最终从属的父视图.
     * @return 指定位置相应的组视图.
     */
    View getGroupView(int groupPosition, boolean isExpanded, View convertView, ViewGroup parent);

    /**
     * 取得显示给定分组给定子位置的数据用的视图.
     * 
     * @param groupPosition 包含要取得子视图的分组位置.
     * @param childPosition 分组中子视图（要返回的视图）的位置.
     * @param isLastChild 该视图是否为组中的最后一个视图.
     * @param convertView 如果可能，重用旧的视图对象.
     *            使用前你应该保证视图对象为非空，并且是否是合适的类型.
     *            如果该对象不能转换为可以正确显示数据的视图，该方法就创建新视图.
     *            不保证使用先前由
     *            {@link #getChildView(int, int, boolean, View, ViewGroup)}
     *            创建的视图.
     * @param parent 该视图最终从属的父视图.
     * @return 指定位置相应的子视图.
     */
    View getChildView(int groupPosition, int childPosition, boolean isLastChild,
            View convertView, ViewGroup parent);

    /**
     * 指定位置的子视图是否可选择.
     * 
     * @param groupPosition 包含要取得子视图的分组位置.
     * @param childPosition 分组中子视图的位置.
     * @return 是否子视图可选择.
     */
    boolean isChildSelectable(int groupPosition, int childPosition);

    /**
     * @see ListAdapter#areAllItemsEnabled()
     */
    boolean areAllItemsEnabled();
    
    /**
     * @see ListAdapter#isEmpty()
     */
    boolean isEmpty();

    /**
     * 分组展开时调用.
     * 
     * @param groupPosition 展开的分组.
     */
    void onGroupExpanded(int groupPosition);
    
    /**
     * 分组收起时调用.
     * 
     * @param groupPosition 收起的分组.
     */
    void onGroupCollapsed(int groupPosition);
    
    /**
     * 取得一览中可以唯一识别子条目的 ID（包括分组ID和子条目ID）.可扩展列表要求每个条目
     * （分组条目和子条目）具有一个可以唯一识别列表中子条目和分组条目的ID.
     * 该方法根据给定子条目ID和分组条目ID返回唯一识别ID.另外，如果
     * {@link #hasStableIds()} 为真，该函数返回的ID必须是固定不变的.
     * 
     * @param groupId 包含子条目ID的分组条目ID.
     * @param childId 子条目的ID.
     * @return 返回可以在所有分组条目和子条目中唯一识别该子条目的ID
     * （可能是固定不变的）.
     */
    long getCombinedChildId(long groupId, long childId);

    /**
     * 取得一览中可以唯一识别子条目的 ID（包括分组ID和子条目ID）.可扩展列表要求每个条目
     * （分组条目和子条目）具有一个可以唯一识别列表中子条目和分组条目的ID.
     * 该方法根据给定子条目ID和分组条目ID返回唯一识别ID.另外，如果
     * {@link #hasStableIds()} 为真，该函数返回的ID必须是固定不变的.
     * 
     * @param groupId 分组条目ID.
     * @return 返回可以在所有分组条目和子条目中唯一识别该分组条目的ID
     * （可能是固定不变的）.
     */
    long getCombinedGroupId(long groupId);
}
