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
 * Interface that should be implemented on Adapters to enable fast scrolling 
 * in an {@link AbsListView} between sections of the list. A section is a group of list items
 * to jump to that have something in common. For example, they may begin with the
 * same letter or they may be songs from the same artist. 
 */
public interface SectionIndexer {
    /**
     * This provides the list view with an array of section objects. In the simplest
     * case these are Strings, each containing one letter of the alphabet.
     * They could be more complex objects that indicate the grouping for the adapter's
     * consumption. The list view will call toString() on the objects to get the
     * preview letter to display while scrolling.
     * @return the array of objects that indicate the different sections of the list.
     */
    Object[] getSections();
    
    /**
     * 提供列表中指定节的起始索引.
     * @param section 要跳转到的节索引
     * @return 节的开始位置.如果指定节越界，返回的位置必须在列表大小范围内.
     */
    int getPositionForSection(int section);
    
    /**
     * 这是反向映射，取得列表中指定位置对应的节索引.
     * @param position 要查找的列表位置
     * @return 节索引.如果 position 越界，返回的节索引必须在节数组大小范围内.
     */
    int getSectionForPosition(int position);    
}
