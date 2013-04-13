/*
 * Copyright (C) 2009 The Android Open Source Project
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

import com.android.internal.R;

import android.content.AsyncQueryHandler;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.res.TypedArray;
import android.database.Cursor;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.provider.ContactsContract.CommonDataKinds.Email;
import android.provider.ContactsContract.Contacts;
import android.provider.ContactsContract.Intents;
import android.provider.ContactsContract.PhoneLookup;
import android.provider.ContactsContract.QuickContact;
import android.provider.ContactsContract.RawContacts;
import android.util.AttributeSet;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityNodeInfo;

/**
 * 该小部件定义了用于显示一个可以显示标准快捷联系人徽章的图片以及点击 图片时的动作.
 * @author translate by 农民伯伯
 * @author review by cnmahj
 * @author convert by cnmahj
 */
public class QuickContactBadge extends ImageView implements OnClickListener {
    private Uri mContactUri;
    private String mContactEmail;
    private String mContactPhone;
    private Drawable mOverlay;
    private QueryHandler mQueryHandler;
    private Drawable mDefaultAvatar;

    protected String[] mExcludeMimes = null;

    static final private int TOKEN_EMAIL_LOOKUP = 0;
    static final private int TOKEN_PHONE_LOOKUP = 1;
    static final private int TOKEN_EMAIL_LOOKUP_AND_TRIGGER = 2;
    static final private int TOKEN_PHONE_LOOKUP_AND_TRIGGER = 3;

    static final String[] EMAIL_LOOKUP_PROJECTION = new String[] {
        RawContacts.CONTACT_ID,
        Contacts.LOOKUP_KEY,
    };
    static final int EMAIL_ID_COLUMN_INDEX = 0;
    static final int EMAIL_LOOKUP_STRING_COLUMN_INDEX = 1;

    static final String[] PHONE_LOOKUP_PROJECTION = new String[] {
        PhoneLookup._ID,
        PhoneLookup.LOOKUP_KEY,
    };
    static final int PHONE_ID_COLUMN_INDEX = 0;
    static final int PHONE_LOOKUP_STRING_COLUMN_INDEX = 1;

    public QuickContactBadge(Context context) {
        this(context, null);
    }

    public QuickContactBadge(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public QuickContactBadge(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);

        TypedArray styledAttributes = mContext.obtainStyledAttributes(R.styleable.Theme);
        mOverlay = styledAttributes.getDrawable(
                com.android.internal.R.styleable.Theme_quickContactBadgeOverlay);
        styledAttributes.recycle();

        mQueryHandler = new QueryHandler(mContext.getContentResolver());
        setOnClickListener(this);
    }

    @Override
    protected void drawableStateChanged() {
        super.drawableStateChanged();
        if (mOverlay != null && mOverlay.isStateful()) {
            mOverlay.setState(getDrawableState());
            invalidate();
        }
    }

    /** 该调用没有影响，因为只有一种 QuickContact 模式. */
    @SuppressWarnings("unused")
    public void setMode(int size) {
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        if (!isEnabled()) {
            // not clickable? don't show triangle
            return;
        }

        if (mOverlay == null || mOverlay.getIntrinsicWidth() == 0 ||
                mOverlay.getIntrinsicHeight() == 0) {
            // nothing to draw
            return;
        }

        mOverlay.setBounds(0, 0, getWidth(), getHeight());

        if (mPaddingTop == 0 && mPaddingLeft == 0) {
            mOverlay.draw(canvas);
        } else {
            int saveCount = canvas.getSaveCount();
            canvas.save();
            canvas.translate(mPaddingLeft, mPaddingTop);
            mOverlay.draw(canvas);
            canvas.restoreToCount(saveCount);
        }
    }

    /** True if a contact, an email address or a phone number has been assigned */
    private boolean isAssigned() {
        return mContactUri != null || mContactEmail != null || mContactPhone != null;
    }

    /**
     * 重置联系人图像为默认状态.
     */
    public void setImageToDefault() {
        if (mDefaultAvatar == null) {
            mDefaultAvatar = getResources().getDrawable(R.drawable.ic_contact_picture);
        }
        setImageDrawable(mDefaultAvatar);
    }

    /**
     * 指定与该 QuickContactBadge 相关联的联系人的URI. 注意，该方法只用于显示
     * QuickContact 窗口，并不会为你绑定联系人图片.
     * 调用 {@link #setImageDrawable(Drawable)} 设置联系人图片。
     *
     * @param contactUri {@link Contacts#CONTENT_URI} 或者
     *            {@link Contacts#CONTENT_LOOKUP_URI} 风格的 URI.
     */
    public void assignContactUri(Uri contactUri) {
        mContactUri = contactUri;
        mContactEmail = null;
        mContactPhone = null;
        onContactUriChanged();
    }

    /**
     * 使用电子邮箱地址来指定联系人.该方法应该只在联系人的 URI 未知时，
     * 作为附加的手段，通过电子邮箱地址来查询联系人的 URI.
     *
     * @param emailAddress 联系人的电子邮件地址.
     * @param lazyLookup 如果该值为真，该查询不立即执行，而是在单击视图时才执行.
     */
    public void assignContactFromEmail(String emailAddress, boolean lazyLookup) {
        mContactEmail = emailAddress;
        if (!lazyLookup) {
            mQueryHandler.startQuery(TOKEN_EMAIL_LOOKUP, null,
                    Uri.withAppendedPath(Email.CONTENT_LOOKUP_URI, Uri.encode(mContactEmail)),
                    EMAIL_LOOKUP_PROJECTION, null, null, null);
        } else {
            mContactUri = null;
            onContactUriChanged();
        }
    }

    /**
     * 使用电话号码来指定联系人.该方法应该只在联系人的 URI 未知时，
     * 作为附加的手段，通过电话号码来查询联系人的 URI.
     * 
     * @param phoneNumber 联系人的电话号码.
     * @param lazyLookup 如果该值为真，该查询不立即执行，而是在单击视图时才执行.
     */
    public void assignContactFromPhone(String phoneNumber, boolean lazyLookup) {
        mContactPhone = phoneNumber;
        if (!lazyLookup) {
            mQueryHandler.startQuery(TOKEN_PHONE_LOOKUP, null,
                    Uri.withAppendedPath(PhoneLookup.CONTENT_FILTER_URI, mContactPhone),
                    PHONE_LOOKUP_PROJECTION, null, null, null);
        } else {
            mContactUri = null;
            onContactUriChanged();
        }
    }

    private void onContactUriChanged() {
        setEnabled(isAssigned());
    }

    @Override
    public void onClick(View v) {
        if (mContactUri != null) {
            QuickContact.showQuickContact(getContext(), QuickContactBadge.this, mContactUri,
                    QuickContact.MODE_LARGE, mExcludeMimes);
        } else if (mContactEmail != null) {
            mQueryHandler.startQuery(TOKEN_EMAIL_LOOKUP_AND_TRIGGER, mContactEmail,
                    Uri.withAppendedPath(Email.CONTENT_LOOKUP_URI, Uri.encode(mContactEmail)),
                    EMAIL_LOOKUP_PROJECTION, null, null, null);
        } else if (mContactPhone != null) {
            mQueryHandler.startQuery(TOKEN_PHONE_LOOKUP_AND_TRIGGER, mContactPhone,
                    Uri.withAppendedPath(PhoneLookup.CONTENT_FILTER_URI, mContactPhone),
                    PHONE_LOOKUP_PROJECTION, null, null, null);
        } else {
            // If a contact hasn't been assigned, don't react to click.
            return;
        }
    }

    @Override
    public void onInitializeAccessibilityEvent(AccessibilityEvent event) {
        super.onInitializeAccessibilityEvent(event);
        event.setClassName(QuickContactBadge.class.getName());
    }

    @Override
    public void onInitializeAccessibilityNodeInfo(AccessibilityNodeInfo info) {
        super.onInitializeAccessibilityNodeInfo(info);
        info.setClassName(QuickContactBadge.class.getName());
    }

    /**
     * 设置一组要排除不显示的MIMI类型列表.例如，可以隐藏Contacts.CONTENT_ITEM_TYPE类型的图标.
     * 设置排除在外的、不显示的 MIME 类型一览.例如，
     * {@link Contacts#CONTENT_ITEM_TYPE} 用于隐藏个人资料图标.
     */
    public void setExcludeMimes(String[] excludeMimes) {
        mExcludeMimes = excludeMimes;
    }

    private class QueryHandler extends AsyncQueryHandler {

        public QueryHandler(ContentResolver cr) {
            super(cr);
        }

        @Override
        protected void onQueryComplete(int token, Object cookie, Cursor cursor) {
            Uri lookupUri = null;
            Uri createUri = null;
            boolean trigger = false;

            try {
                switch(token) {
                    case TOKEN_PHONE_LOOKUP_AND_TRIGGER:
                        trigger = true;
                        createUri = Uri.fromParts("tel", (String)cookie, null);

                        //$FALL-THROUGH$
                    case TOKEN_PHONE_LOOKUP: {
                        if (cursor != null && cursor.moveToFirst()) {
                            long contactId = cursor.getLong(PHONE_ID_COLUMN_INDEX);
                            String lookupKey = cursor.getString(PHONE_LOOKUP_STRING_COLUMN_INDEX);
                            lookupUri = Contacts.getLookupUri(contactId, lookupKey);
                        }

                        break;
                    }
                    case TOKEN_EMAIL_LOOKUP_AND_TRIGGER:
                        trigger = true;
                        createUri = Uri.fromParts("mailto", (String)cookie, null);

                        //$FALL-THROUGH$
                    case TOKEN_EMAIL_LOOKUP: {
                        if (cursor != null && cursor.moveToFirst()) {
                            long contactId = cursor.getLong(EMAIL_ID_COLUMN_INDEX);
                            String lookupKey = cursor.getString(EMAIL_LOOKUP_STRING_COLUMN_INDEX);
                            lookupUri = Contacts.getLookupUri(contactId, lookupKey);
                        }
                        break;
                    }
                }
            } finally {
                if (cursor != null) {
                    cursor.close();
                }
            }

            mContactUri = lookupUri;
            onContactUriChanged();

            if (trigger && lookupUri != null) {
                // Found contact, so trigger QuickContact
                QuickContact.showQuickContact(getContext(), QuickContactBadge.this, lookupUri,
                        QuickContact.MODE_LARGE, mExcludeMimes);
            } else if (createUri != null) {
                // Prompt user to add this person to contacts
                final Intent intent = new Intent(Intents.SHOW_OR_CREATE_CONTACT, createUri);
                getContext().startActivity(intent);
            }
        }
    }
}
