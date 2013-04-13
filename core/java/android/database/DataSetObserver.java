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

package android.database;

/**
 * 数据集变更或无效时调用的回调函数. 监视的典型的数据集是 {@link Cursor} 或
 * {@link android.widget.Adapter}.加入 DataSetObservable 的对象必须实现该接口.
 */
public abstract class DataSetObserver {
    /**
     * 整个数据集变更时调用该方法，类似于在 {@link Cursor} 上调用 {@link Cursor#requery()}.
     */
    public void onChanged() {
        // Do nothing
    }

    /**
     * 整个数据集无效时调用该方法，类似于在 {@link Cursor} 上调用了 {@link Cursor#deactivate()}
     * 或者 {@link Cursor#close()}.
     */
    public void onInvalidated() {
        // Do nothing
    }
}
