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
 * 此接口定义了一个使视图具有选中状态的扩展.
 * @author translate by CN七号
 * @author convert by cnmahj
 */
public interface Checkable {
    
    /**
     * 设置当前视图控件的选中状态.
     * 
     * @param checked 新的选中状态
     */
    void setChecked(boolean checked);
        
    /**
     * @return 当前视图是否出于选中状态.
     */
    boolean isChecked();
    
    /**
     * 反转当前视图控件的选中状态.
     */
    void toggle();
}
