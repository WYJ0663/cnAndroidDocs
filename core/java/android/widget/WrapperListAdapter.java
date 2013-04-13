/*
 * Copyright (C) 2008 The Android Open Source Project
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
 * 封装了另一个列表适配器的列表适配器.调用 {@link #getWrappedAdapter()}
 * 可以取得封装在其中的适配器.
 *
 * @see ListView
 * @author translate by cnmahj
 */
public interface WrapperListAdapter extends ListAdapter {
    /**
     * 返回封装在列表适配器中的适配器.
     *
     * @return 封装在该适配器中的 {@link android.widget.ListAdapter}.
     */
    public ListAdapter getWrappedAdapter();
}
