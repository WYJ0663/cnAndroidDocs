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

import android.view.View;
import android.view.ViewGroup;

/**
 * 扩展自 {@link Adapter} 的适配器，是 {@link android.widget.Spinner}
 * 与数据之间的一座桥梁.Spinner 适配器允许定义两个不同的视图：
 * 一个是在 Spinner 上显示数据，另一个是按下 Spinner 时在下拉列表里显示数据.
 * @author translate by 德罗德（Android中文翻译组）
 * @author convert by cnmahj
 */
public interface SpinnerAdapter extends Adapter {
    /**
     * <p>获得一个用于在下列菜单中显示数据集中指定位置的数据的 {@link android.view.View}.</p>
     *
     * @param position      要取得的条目的视图索引
     * @param convertView   重新利用的旧视图（如果可能的话）.
     *        注意：在使用之前应该确保这个视图不是空的并且类型合适.
     *        如果该视图无法转换为可以显示正确数据的视图，该方法会创建新试图.
     * @param parent 视图最终将依附的父对象
     * @return 一个对应指定位置的数据的 {@link android.view.View}.
     */
    public View getDropDownView(int position, View convertView, ViewGroup parent);
}
