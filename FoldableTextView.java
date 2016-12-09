package com.zzc.expendtextview;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.graphics.Rect;
import android.text.Layout;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.TextPaint;
import android.text.TextUtils;
import android.text.method.LinkMovementMethod;
import android.text.style.ClickableSpan;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

/**
 * Created by ZZC on 2016/12/9.
 * 展开式TextView
 * margin相关属相未用。。。。
 * 将以下属性放在attr.xml中
 * <p/>
 * <declare-styleable name="FoldableTextView">
 * <attr name="gravity_foldText" format="integer"></attr>
 * <attr name="text_fold" format="string"></attr>
 * <attr name="text_unfold" format="string"></attr>
 * <attr name="fold_color" format="string|color"></attr>
 * <attr name="fold_line" format="integer"></attr>
 * <attr name="fold_size" format="dimension"></attr>
 * <attr name="margin_fold_left" format="dimension"></attr>
 * <attr name="margin_fold_right" format="dimension"></attr>
 * <attr name="margin_fold_top" format="dimension"></attr>
 * <attr name="margin_fold_bottom" format="dimension"></attr>
 * <attr name="margins_fold" format="dimension"></attr>
 * </declare-styleable>
 */
public class FoldableTextView extends TextView {
    public static final int COLOR_FOLD = Color.BLUE;
    public static final int LINE_DEFAULT = 3;
    public static final String ELLIPSIS_STRING = "...";
    private TextPaint mPaint;
    private Rect mFoldMargins;
    private String mFoldText, mTextFold, mTextUnfold;
    private int mFoldColor, mFoldSize, mFoldLine;
    private int mMaxLines;
    private boolean mSingleLine;
    private CharSequence mOriginText;
    private boolean mDrawFoldText;
    private boolean mIsFolded = true;

    public FoldableTextView(Context context) {
        this(context, null);
    }

    public FoldableTextView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public FoldableTextView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        mPaint = getPaint();
        initField();
        setParamsFromXml(context, attrs);
    }

    private void initField() {
        mFoldMargins = new Rect();
        setMovementMethod(LinkMovementMethod.getInstance());
    }

    private void setParamsFromXml(Context context, AttributeSet attrs) {
        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.FoldableTextView);
        int indexCount = a.getIndexCount();
        int foldColor = COLOR_FOLD;
        int foldSize = dp2px(12);
        String textFold = "", textUnfold = "";
        int marginLeft = 0, marginRight = 0, marginTop = 0, marginBottom = 0, margins = 0;
        boolean singleLine = false;
        int maxLines = -1, foldLine = LINE_DEFAULT;
        for (int i = 0; i < indexCount; i++) {
            int attr = a.getIndex(i);
            switch (attr) {
                case android.R.styleable.TextView_maxLines:
                    maxLines = a.getInteger(attr, -1);
                    break;
                case android.R.styleable.TextView_singleLine:
                    singleLine = a.getBoolean(attr, false);
                    break;
                case R.styleable.FoldableTextView_fold_color:
                    foldColor = a.getColor(attr, foldColor);
                    break;
                case R.styleable.FoldableTextView_fold_size:
                    foldSize = a.getDimensionPixelSize(attr, foldSize);
                    break;
                case R.styleable.FoldableTextView_gravity_foldText:
                    break;
                case R.styleable.FoldableTextView_fold_line:
                    foldLine = a.getInteger(attr, LINE_DEFAULT);
                case R.styleable.FoldableTextView_text_fold:
                    textFold = a.getString(attr);
                    break;
                case R.styleable.FoldableTextView_text_unfold:
                    textUnfold = a.getString(attr);
                    break;
                case R.styleable.FoldableTextView_margin_fold_left:
                    marginLeft = a.getDimensionPixelSize(attr, margins);
                    break;
                case R.styleable.FoldableTextView_margin_fold_right:
                    marginRight = a.getDimensionPixelSize(attr, margins);
                    break;
                case R.styleable.FoldableTextView_margin_fold_top:
                    marginTop = a.getDimensionPixelSize(attr, margins);
                    break;
                case R.styleable.FoldableTextView_margin_fold_bottom:
                    marginBottom = a.getDimensionPixelSize(attr, margins);
                    break;
                case R.styleable.FoldableTextView_margins_fold:
                    margins = a.getDimensionPixelSize(attr, 0);
                    if (margins != 0) {
                        marginLeft = margins;
                        marginRight = margins;
                        marginTop = margins;
                        marginBottom = margins;
                    }
                    break;
            }
        }
        a.recycle();
        mFoldMargins.set(marginLeft, marginTop, marginRight, marginBottom);
        mFoldText = mTextFold = textFold;
        mTextUnfold = textUnfold;
        mFoldColor = foldColor;
        mFoldSize = foldSize;
        mSingleLine = singleLine;
        mMaxLines = maxLines;
        mFoldLine = foldLine;
        Log.d("xb", mTextFold + "\n" + mTextUnfold + "\n" + mFoldColor + "\n" + mFoldSize + "\n" + mSingleLine + "\n" + mMaxLines + "\n" + mFoldLine + "\n" + mFoldMargins);
    }

    @Override
    public void setText(CharSequence text, final BufferType type) {
        mOriginText = text;
        super.setText(text, type);
        Layout layout = getLayout();
        if (layout == null) {
            post(new Runnable() {
                @Override
                public void run() {
                    Layout layout1 = getLayout();
                    if (layout1 != null) {
                        setEllipesize(layout1, type);
                    }
                    mDrawFoldText = false;
                }
            });
        } else {
            setEllipesize(layout, type);
        }
    }

    private void setEllipesize(Layout layout, BufferType type) {
        int lineCount = layout.getLineCount();
        if (lineCount > mFoldLine) {
            mDrawFoldText = true;
            float v = mPaint.measureText(mFoldText, 0, mFoldText.length());
            int measuredWidth = getMeasuredWidth();
            int width = getWidth();
            float dWidth = mPaint.measureText("...", 0, "...".length());
            int paddingLeft = getPaddingLeft();
            int paddingRight = getPaddingRight();
            int lineWidth = measuredWidth - paddingLeft - paddingRight;
            CharSequence charSequence = "";
            StringBuilder sb = new StringBuilder();
            if (mIsFolded) {
                for (int i = 1; i <= mFoldLine; i++) {
                    charSequence = mOriginText.subSequence(charSequence.length() != 0 ? charSequence.length() : 0, mOriginText.length());
                    charSequence = TextUtils.ellipsize(charSequence, getPaint(), i == mFoldLine ? lineWidth - v : lineWidth, TextUtils.TruncateAt.END);
                    String normal = ELLIPSIS_STRING;
                    boolean contains = charSequence.toString().contains(normal);
                    if (i != mFoldLine && contains) {
                        charSequence = charSequence.toString().substring(0, charSequence.length() - 3);
//                        sb.append(mOriginText.subSequence(sb.length() - 1, sb.length() + 3));
                    }
                    sb.append(charSequence);
                }
            } else {
                sb.append(mOriginText);
            }
            SpannableString ss = new SpannableString(sb + mFoldText);
            ss.setSpan(new FoldClickableSpan(), sb.length(), ss.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            superText(ss, type);
            return;
        }
    }

    private void superText(CharSequence text, BufferType type) {
        super.setText(text, type);
    }

    @Override
    public CharSequence getText() {
        if (mDrawFoldText) {
            return mOriginText;
        } else {
            return super.getText();
        }
    }

    class FoldClickableSpan extends ClickableSpan {
        @Override
        public void updateDrawState(TextPaint ds) {
//            ds.setUnderlineText(true);
            ds.setColor(mFoldColor);
            ds.setTextSize(mFoldSize);
        }

        @Override
        public void onClick(View widget) {
            foldOrUnfold();
        }
    }

    private void foldOrUnfold() {
        if (mIsFolded) {
            mFoldText = "《收起";
        } else {
            mFoldText = mTextUnfold;
        }
        mIsFolded = !mIsFolded;
        setText(mOriginText);
    }

    private int dp2px(int dp) {
        return (int) (getResources().getDisplayMetrics().density * dp + 0.5f);
    }

    private int px2dp(int px) {
        return (int) (px / getResources().getDisplayMetrics().density);
    }
}
