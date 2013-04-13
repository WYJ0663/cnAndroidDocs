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

/**
 * <p>定义一个可过滤的行为.一个可过滤的类可以通过一个过滤器筛选它的数据.
 * 过滤类通常由{@link android.widget.Adapter 适配器}实现.</p>
 *
 * @see android.widget.Filter
 * @author translate by 德罗德
 * @author convert by cnmahj
 */
public interface Filterable {
    /**
     * <p>返回可以根据过滤模式限制数据的过滤器.</p>
     *
     * <p>该方法一般由 {@link android.widget.Adapter} 实现.</p>
     *
     * @return 用于限制数据的过滤器
     */
    Filter getFilter();
}
