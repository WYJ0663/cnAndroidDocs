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

package android.os;

/**
 * 用于实例可以写入以及从 {@link Parcel} 中恢复的类的接口. 实现 Parcelable
 * 接口的类必须包含一个实现了 {@link Parcelable.Creator Parcelable.Creator}
 * 接口的名为 <code>CREATOR</code> 的静态字段.
 * 
 * <p>Parcelable 典型的实现方法如下：</p>
 * 
 * <pre>
 * public class MyParcelable implements Parcelable {
 *     private int mData;
 *
 *     public int describeContents() {
 *         return 0;
 *     }
 *
 *     public void writeToParcel(Parcel out, int flags) {
 *         out.writeInt(mData);
 *     }
 *
 *     public static final Parcelable.Creator&lt;MyParcelable&gt; CREATOR
 *             = new Parcelable.Creator&lt;MyParcelable&gt;() {
 *         public MyParcelable createFromParcel(Parcel in) {
 *             return new MyParcelable(in);
 *         }
 *
 *         public MyParcelable[] newArray(int size) {
 *             return new MyParcelable[size];
 *         }
 *     };
 *     
 *     private MyParcelable(Parcel in) {
 *         mData = in.readInt();
 *     }
 * }</pre>
 */
public interface Parcelable {
    /**
     * 用于 {@link #writeToParcel} 的标志位：写入的对象是象
     * "<code>Parcelable someFunction()</code>"、
     * "<code>void someFunction(out Parcelable)</code>" 或
     * "<code>void someFunction(inout Parcelable)</code>" 这样的函数的返回值.
     * 一些实现可能想在这个时候释放资源.
     */
    public static final int PARCELABLE_WRITE_RETURN_VALUE = 0x0001;
    
    /**
     * 用于 {@link #describeContents} 的位掩码：每一位代表编组时包含特殊意义的一种对象.
     */
    public static final int CONTENTS_FILE_DESCRIPTOR = 0x0001;
    
    /**
     * 描述包含在可包装对象的编组形式中的各种特殊对象.
     *  
     * @return 代表由可包装对象编组的特殊对象类型集合的位掩码.
     */
    public int describeContents();
    
    /**
     * 展开该对象到包裹对象中.
     * 
     * @param dest 该对象要写入的包裹对象.
     * @param flags 关于对象如何写入的附件标志.可以为 0 或 {@link #PARCELABLE_WRITE_RETURN_VALUE}.
     */
    public void writeToParcel(Parcel dest, int flags);

    /**
     * 子类必须实现该接口，并定义为公有的 CREATOR 字段，用于从包裹对象中实例化你的可包装类.
     */
    public interface Creator<T> {
        /**
         * 创建可包装类的新实例，从给定的包裹中实例化之前使用
         * {@link Parcelable#writeToParcel Parcelable.writeToParcel()}
         * 方法写入的对象.
         * 
         * @param source 用于读取对象数据的包裹对象.
         * @return 返回可包装类的新实例.
         */
        public T createFromParcel(Parcel source);
        
        /**
         * 创建新的可包装类的数组.
         * 
         * @param size 数组大小
         * @return 返回可包装对象类的数组，每个元素均已初始化为 null.
         */
        public T[] newArray(int size);
    }

    /**
     * 专业化的 {@link Creator}，允许你接收内部创建的 ClassLoader 对象.
     */
    public interface ClassLoaderCreator<T> extends Creator<T> {
        /**
         * 创建 Parcelable 类的新实例，使用给定的 ClassLoader 来实例化
         * 之前通过 {@link Parcelable#writeToParcel Parcelable.writeToParcel()}
         * 写入指定包裹中的类.
         *
         * @param source 用于读取对象数据的包裹对象
         * @param loader 用于创建该对象的 ClassLoader.
         * @return 返回可包装类的新实例.
         */
        public T createFromParcel(Parcel source, ClassLoader loader);
    }
}
