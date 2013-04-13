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

import android.database.Cursor;
import android.database.DataSetObserver;
import android.util.SparseIntArray;

/**
 * <p>
 * 实现了 SectionIndexer 接口的适配器的辅助类.如果适配器使用简单的基于字母的排序方式，
 * 该类提供了使用二进制检索来对项目数巨大的列表进行快速索引的方法. 
 * 该类缓存二进制检索时取得的索引信息，并在游标发生改变时使缓存失效.
 * <p/>
 * 如果游标发生变更，你的适配器负责调用 {@link #setCursor} 来更新游标.
 * {@link #getPositionForSection} 方法进行二进制搜索来对给定节（字母）进行索引.
 * @author translate by cnmahj
 */
public class AlphabetIndexer extends DataSetObserver implements SectionIndexer {

    /**
     * 列表视图适配器使用的游标。
     */
    protected Cursor mDataCursor;

    /**
     * 该列表排序用的游标列的索引。
     */
    protected int mColumnIndex;

    /**
     * 生成索引节的字符串。
     */
    protected CharSequence mAlphabet;

    /**
     * Cached length of the alphabet array.
     */
    private int mAlphabetLength;

    /**
     * This contains a cache of the computed indices so far. It will get reset whenever
     * the dataset changes or the cursor changes.
     */
    private SparseIntArray mAlphaMap;

    /**
     * Use a collator to compare strings in a localized manner.
     */
    private java.text.Collator mCollator;

    /**
     * The section array converted from the alphabet string.
     */
    private String[] mAlphabetArray;

    /**
     * 索引器的构造函数.
     * @param cursor 包含数据集的游标
     * @param sortedColumnIndex 按字母索引的游标中的列号
     * @param alphabet 包含字母的字符串，首字符为空格.例如，使用字符串
     *        " ABCDEFGHIJKLMNOPQRSTUVWXYZ" 用于英文的索引.字符必须为大写，
     *        并按照 ASCII或 UNICODE排序. 基本上这些字符会显示为预览字母.
     */
    public AlphabetIndexer(Cursor cursor, int sortedColumnIndex, CharSequence alphabet) {
        mDataCursor = cursor;
        mColumnIndex = sortedColumnIndex;
        mAlphabet = alphabet;
        mAlphabetLength = alphabet.length();
        mAlphabetArray = new String[mAlphabetLength];
        for (int i = 0; i < mAlphabetLength; i++) {
            mAlphabetArray[i] = Character.toString(mAlphabet.charAt(i));
        }
        mAlphaMap = new SparseIntArray(mAlphabetLength);
        if (cursor != null) {
            cursor.registerDataSetObserver(this);
        }
        // Get a Collator for the current locale for string comparisons.
        mCollator = java.text.Collator.getInstance();
        mCollator.setStrength(java.text.Collator.PRIMARY);
    }

    /**
     * 返回构造函数中指定的由字母构成的区段数组。
     * @return 区段数组
     */
    public Object[] getSections() {
        return mAlphabetArray;
    }

    /**
     * 设置作为数据集的新游标并重置索引缓存。
     * @param cursor 作为数据集的新游标
     */
    public void setCursor(Cursor cursor) {
        if (mDataCursor != null) {
            mDataCursor.unregisterDataSetObserver(this);
        }
        mDataCursor = cursor;
        if (cursor != null) {
            mDataCursor.registerDataSetObserver(this);
        }
        mAlphaMap.clear();
    }

    /**
     * 比较单词的首字母的默认实现。
     */
    protected int compare(String word, String letter) {
        final String firstLetter;
        if (word.length() == 0) {
            firstLetter = " ";
        } else {
            firstLetter = word.substring(0, 1);
        }

        return mCollator.compare(firstLetter, letter);
    }

    /**
     * 执行二进制检索或查找索引来找出匹配给定节首字母的第一行数据.
     * @param sectionIndex 要检索的节索引
     * @return 第一个匹配的行索引，或下一个最接近的字母的行索引.例如，当检索“T”，
     * 而“T”不存在时，会返回“U”或其后面存在的字母的索引.如果“T”之后没有任何数据，
     * 则返回列表的大小.
     */
    public int getPositionForSection(int sectionIndex) {
        final SparseIntArray alphaMap = mAlphaMap;
        final Cursor cursor = mDataCursor;

        if (cursor == null || mAlphabet == null) {
            return 0;
        }

        // Check bounds
        if (sectionIndex <= 0) {
            return 0;
        }
        if (sectionIndex >= mAlphabetLength) {
            sectionIndex = mAlphabetLength - 1;
        }

        int savedCursorPos = cursor.getPosition();

        int count = cursor.getCount();
        int start = 0;
        int end = count;
        int pos;

        char letter = mAlphabet.charAt(sectionIndex);
        String targetLetter = Character.toString(letter);
        int key = letter;
        // Check map
        if (Integer.MIN_VALUE != (pos = alphaMap.get(key, Integer.MIN_VALUE))) {
            // Is it approximate? Using negative value to indicate that it's
            // an approximation and positive value when it is the accurate
            // position.
            if (pos < 0) {
                pos = -pos;
                end = pos;
            } else {
                // Not approximate, this is the confirmed start of section, return it
                return pos;
            }
        }

        // Do we have the position of the previous section?
        if (sectionIndex > 0) {
            int prevLetter =
                    mAlphabet.charAt(sectionIndex - 1);
            int prevLetterPos = alphaMap.get(prevLetter, Integer.MIN_VALUE);
            if (prevLetterPos != Integer.MIN_VALUE) {
                start = Math.abs(prevLetterPos);
            }
        }

        // Now that we have a possibly optimized start and end, let's binary search

        pos = (end + start) / 2;

        while (pos < end) {
            // Get letter at pos
            cursor.moveToPosition(pos);
            String curName = cursor.getString(mColumnIndex);
            if (curName == null) {
                if (pos == 0) {
                    break;
                } else {
                    pos--;
                    continue;
                }
            }
            int diff = compare(curName, targetLetter);
            if (diff != 0) {
                // TODO: Commenting out approximation code because it doesn't work for certain
                // lists with custom comparators
                // Enter approximation in hash if a better solution doesn't exist
                // String startingLetter = Character.toString(getFirstLetter(curName));
                // int startingLetterKey = startingLetter.charAt(0);
                // int curPos = alphaMap.get(startingLetterKey, Integer.MIN_VALUE);
                // if (curPos == Integer.MIN_VALUE || Math.abs(curPos) > pos) {
                //     Negative pos indicates that it is an approximation
                //     alphaMap.put(startingLetterKey, -pos);
                // }
                // if (mCollator.compare(startingLetter, targetLetter) < 0) {
                if (diff < 0) {
                    start = pos + 1;
                    if (start >= count) {
                        pos = count;
                        break;
                    }
                } else {
                    end = pos;
                }
            } else {
                // They're the same, but that doesn't mean it's the start
                if (start == pos) {
                    // This is it
                    break;
                } else {
                    // Need to go further lower to find the starting row
                    end = pos;
                }
            }
            pos = (start + end) / 2;
        }
        alphaMap.put(key, pos);
        cursor.moveToPosition(savedCursorPos);
        return pos;
    }

    /**
     * Returns the section index for a given position in the list by querying the item
     * and comparing it with all items in the section array.
     */
    public int getSectionForPosition(int position) {
        int savedCursorPos = mDataCursor.getPosition();
        mDataCursor.moveToPosition(position);
        String curName = mDataCursor.getString(mColumnIndex);
        mDataCursor.moveToPosition(savedCursorPos);
        // Linear search, as there are only a few items in the section index
        // Could speed this up later if it actually gets used.
        for (int i = 0; i < mAlphabetLength; i++) {
            char letter = mAlphabet.charAt(i);
            String targetLetter = Character.toString(letter);
            if (compare(curName, targetLetter) == 0) {
                return i;
            }
        }
        return 0; // Don't recognize the letter - falls under zero'th section
    }

    /*
     * @hide
     */
    @Override
    public void onChanged() {
        super.onChanged();
        mAlphaMap.clear();
    }

    /*
     * @hide
     */
    @Override
    public void onInvalidated() {
        super.onInvalidated();
        mAlphaMap.clear();
    }
}

