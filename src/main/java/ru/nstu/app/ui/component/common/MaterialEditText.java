package ru.nstu.app.ui.component.common;

import android.content.Context;
import android.os.Build;
import android.text.InputType;
import android.text.TextWatcher;
import android.util.AttributeSet;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;

import ru.nstu.app.R;
import ru.nstu.app.controller.DisplayController;

public class MaterialEditText extends LinearLayout {
    private EditText editText;
    private View topUnderlineView;
    private View bottomUnderlineView;

    public MaterialEditText(Context context) {
        super(context);
    }

    public MaterialEditText(Context context, AttributeSet attrs) {
        super(context, attrs);

        setOrientation(VERTICAL);

        editText = new EditText(context);
        addView(editText);
        if(Build.VERSION.SDK_INT >= 16) {
            editText.setBackground(null);
        } else {
            editText.setBackgroundDrawable(null);
        }
        editText.setPadding(0, 0, 0, DisplayController.dp(4));
        editText.setOnFocusChangeListener(new OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                topUnderlineView.setBackgroundColor(getContext().getResources().getColor(hasFocus ? R.color.system_blue : R.color.transparent));
                bottomUnderlineView.setBackgroundColor(getContext().getResources().getColor(hasFocus ? R.color.system_blue : R.color.system_grey));
            }
        });

        topUnderlineView = new View(context);
        addView(topUnderlineView);
        LinearLayout.LayoutParams layoutParams = (LinearLayout.LayoutParams)topUnderlineView.getLayoutParams();
        layoutParams.width = LayoutParams.MATCH_PARENT;
        layoutParams.height = 1;
        topUnderlineView.setLayoutParams(layoutParams);
        topUnderlineView.setBackgroundColor(context.getResources().getColor(R.color.transparent));

        bottomUnderlineView = new View(context);
        addView(bottomUnderlineView);
        layoutParams = (LinearLayout.LayoutParams)bottomUnderlineView.getLayoutParams();
        layoutParams.width = LayoutParams.MATCH_PARENT;
        layoutParams.height = 1;
        bottomUnderlineView.setLayoutParams(layoutParams);
        bottomUnderlineView.setBackgroundColor(context.getResources().getColor(R.color.system_grey));
    }

    public void setInputType(int inputType) {
        editText.setInputType(inputType);
    }

    public void setEnabled(boolean enabled) {
        editText.setEnabled(false);
    }

    public void setSelection(int position) {
        editText.setSelection(position);
    }

    public String getText() {
        return editText.getText().toString();
    }

    public void setText(String text) {
        editText.setText(text);
    }

    public void addTextChangedListener(TextWatcher textWatcher) {
        editText.addTextChangedListener(textWatcher);
    }

    public void setHint(String hint) {
        editText.setHint(hint);
    }

    public void error() {
        topUnderlineView.setBackgroundColor(getContext().getResources().getColor(R.color.red));
        bottomUnderlineView.setBackgroundColor(getContext().getResources().getColor(R.color.red));
    }

    public void ok() {
        topUnderlineView.setBackgroundColor(getContext().getResources().getColor(R.color.system_blue));
        bottomUnderlineView.setBackgroundColor(getContext().getResources().getColor(R.color.system_blue));
    }
}
