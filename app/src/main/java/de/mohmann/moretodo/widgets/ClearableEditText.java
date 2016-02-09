package de.mohmann.moretodo.widgets;

import android.content.Context;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.AppCompatEditText;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

import de.mohmann.moretodo.R;
import de.mohmann.moretodo.util.Utils;

/**
 * Created by mohmann on 2/8/16.
 */
public class ClearableEditText extends AppCompatEditText implements TextWatcher,
        View.OnTouchListener {

    private Drawable mClearIcon;
    private OnClearTextListener mClearTextListener;
    private OnTouchListener mTouchListener;

    public ClearableEditText(Context context) {
        super(context);
        init();
    }

    public ClearableEditText(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public ClearableEditText(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init();
    }

    private void init() {
        mClearIcon = getCompoundDrawables()[2];

        if (mClearIcon == null) {
            mClearIcon = ContextCompat.getDrawable(getContext(), R.drawable.ic_clear_black_18dp);
        }

        mClearIcon = mClearIcon.mutate();
        mClearIcon.setBounds(0, 0, mClearIcon.getIntrinsicWidth(), mClearIcon.getIntrinsicHeight());
        mClearIcon.setAlpha(80);

        setClearIconVisible(false);
        addTextChangedListener(this);
        super.setOnTouchListener(this);
    }

    protected void setClearIconVisible(boolean visible) {
        Drawable[] drawables = getCompoundDrawables();
        boolean wasVisible = (drawables[2] != null);
        if (visible != wasVisible) {
            Drawable icon = visible ? mClearIcon : null;
            setCompoundDrawables(drawables[0], drawables[1], icon, drawables[3]);
        }
    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        if (getCompoundDrawables()[2] != null) {
            int right = getWidth() - getPaddingRight();
            int left = right - mClearIcon.getIntrinsicWidth();
            int bottom = getHeight() - getPaddingBottom();
            int top = bottom - mClearIcon.getIntrinsicHeight();
            boolean hitClear = new Rect(left, top, right, bottom)
                    .contains((int) event.getX(), (int) event.getY());

            if (hitClear) {
                if (event.getAction() == MotionEvent.ACTION_UP) {
                    setText("");
                    if (mClearTextListener != null) {
                        mClearTextListener.onClearText(this);
                    }
                }
                return true;
            }
        }
        return (mTouchListener != null && mTouchListener.onTouch(v, event));
    }

    @Override
    public void beforeTextChanged(CharSequence s, int start, int count, int after) {
        // pass
    }

    @Override
    public void onTextChanged(CharSequence s, int start, int before, int count) {
        setClearIconVisible(Utils.isNotEmpty(s));
    }

    @Override
    public void afterTextChanged(Editable s) {
        // pass
    }

    @Override
    public void setOnTouchListener(OnTouchListener listener) {
        mTouchListener = listener;
    }

    public void setOnClearTextListener(OnClearTextListener listener) {
        mClearTextListener = listener;
    }

    public interface OnClearTextListener {
        void onClearText(ClearableEditText view);
    }

}
