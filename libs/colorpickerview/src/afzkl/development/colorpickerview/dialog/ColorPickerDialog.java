/*
 * Copyright (C) 2010 Daniel Nilsson
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

package afzkl.development.colorpickerview.dialog;

import afzkl.development.colorpickerview.ARGBEditText;
import afzkl.development.colorpickerview.R;
import afzkl.development.colorpickerview.view.ColorPanelView;
import afzkl.development.colorpickerview.view.ColorPickerView;
import afzkl.development.colorpickerview.view.ColorPickerView.OnColorChangedListener;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Color;
import android.graphics.PixelFormat;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ColorPickerDialog extends AlertDialog implements
		ColorPickerView.OnColorChangedListener {

	private ColorPickerView mColorPicker;

	private ColorPanelView mOldColor;
	private ColorPanelView mNewColor;
    private ARGBEditText mEditText;
    private EditText mEditTextWidth;
	private Button mLineStyleButton;

	private OnColorChangedListener mListener;

	public ColorPickerDialog(Context context, int initialColor) {
		this(context, initialColor, -1, 1, null);

		init(initialColor, -1, 1);
	}

    public ColorPickerDialog(Context context, int initialColor, int initialLineStyle) {
        this(context, initialColor, initialLineStyle, 1, null);

        init(initialColor, initialLineStyle, 1);
    }

    public ColorPickerDialog(Context context, int initialColor, int initialLineStyle, int initialWidth) {
        this(context, initialColor, initialLineStyle, initialWidth, null);

        init(initialColor, initialLineStyle, initialWidth);
    }
	
	public ColorPickerDialog(Context context, int initialColor, int initialLineStyle, int initialWidth, OnColorChangedListener listener) {
		super(context);
		mListener = listener;
		init(initialColor, initialLineStyle, initialWidth);
	}

	private void init(int color, int lineStyle, int initialWidth) {
		// To fight color branding.
		getWindow().setFormat(PixelFormat.RGBA_8888);
		setUp(color, lineStyle, initialWidth);
	}

	private void setUp(int color, int lineStyle, int lineWidth) {
		boolean isLandscapeLayout = false;
		
		LayoutInflater inflater = (LayoutInflater) getContext()
				.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		final View layout = inflater.inflate(R.layout.dialog_color_picker, null);

		setView(layout);

		setTitle(R.string.dialog_title);
		// setIcon(android.R.drawable.ic_dialog_info);

		LinearLayout landscapeLayout = (LinearLayout) layout.findViewById(R.id.dialog_color_picker_extra_layout_landscape);

		if(landscapeLayout != null) {
			isLandscapeLayout = true;
		}
		
				
		mColorPicker = (ColorPickerView) layout.findViewById(R.id.color_picker_view);
		mOldColor = (ColorPanelView) layout.findViewById(R.id.color_panel_old);
		mNewColor = (ColorPanelView) layout.findViewById(R.id.color_panel_new);
        mEditText = (ARGBEditText)   layout.findViewById(R.id.color_panel_hex);
        mEditTextWidth = (EditText)  layout.findViewById(R.id.width_edittext);

		if(!isLandscapeLayout) {
			((LinearLayout) mOldColor.getParent()).setPadding(
                    Math.round(mColorPicker.getDrawingOffset()), 0,
                    Math.round(mColorPicker.getDrawingOffset()), 0);

		} else {
			landscapeLayout.setPadding(0, 0, Math.round(mColorPicker.getDrawingOffset()), 0);
			setTitle(null);
		}

		mColorPicker.setOnColorChangedListener(this);

		mOldColor.setColor(color);
		mColorPicker.setColor(color, true);

        mColorPicker.setLineStyle(lineStyle);
        mColorPicker.setLineWidth(lineWidth);

        mEditTextWidth.setText(String.valueOf(lineWidth));
        mEditTextWidth.addTextChangedListener(new TextWatcher() {

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

                String value = s.toString();
                if(value == null || value.equals("")) {
                    value = "1";
                }
                mColorPicker.setLineWidth(Integer.parseInt(value));
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });

        mEditText.setColor(color);
        mEditText.addTextChangedListener(new TextWatcher() {

			@Override
			public void beforeTextChanged(CharSequence s, int start,
										  int count, int after) {
			}

			@Override
			public void onTextChanged(CharSequence s, int start,
									  int before, int count) {
				String hexValue = s.toString();

				if (hexValue.matches("(?i:[0-9a-f]{8})")) {
					onColorChanged(Color.parseColor("#" + hexValue), false);
				}
			}

			@Override
			public void afterTextChanged(Editable s) {
			}
		});


		mLineStyleButton = (Button) layout.findViewById(R.id.line_style_button);
		mLineStyleButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				final CharSequence[] items = {
                    getContext().getString(R.string.line_style_solid),
                    getContext().getString(R.string.line_style_dashed),
                    getContext().getString(R.string.line_style_dotted),
                    getContext().getString(R.string.line_style_dash_dotted)
				};

				AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
				builder.setTitle(R.string.line_style_title);
				builder.setItems(items, new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int item) {
                        Log.d("@@@@@@@@@@@@@@@@@@@@@", "BUTTTON CLICKED " + item);
                        mColorPicker.setLineStyle(item);
					}
				});
				AlertDialog alert = builder.create();
				alert.show();
			}
		});
	}

	@Override
	public void onColorChanged(int color) {
		mNewColor.setColor(color);
        mEditText.setColor(color);

		if (mListener != null) {
			mListener.onColorChanged(color);
		}

	}

    public void onColorChanged(int color, boolean changeTextInEditText) {
        mNewColor.setColor(color);
        mColorPicker.setColor(color);

        if( changeTextInEditText ) {
            mEditText.setColor(color);
        }

        if (mListener != null) {
            mListener.onColorChanged(color);
        }
    }


	public void setAlphaSliderVisible(boolean visible) {
		mColorPicker.setAlphaSliderVisible(visible);
	}

	public int getColor() {
		return mColorPicker.getColor();
	}

    public int getLineStyle() {
        return mColorPicker.getLineStyle();
    }

    public int getLineWidth() {
        return mColorPicker.getLineWidth();
    }

    private String colorToHexString(int color) {
        return String.format("%06X", 0xFFFFFFFF & color);
    }

}
