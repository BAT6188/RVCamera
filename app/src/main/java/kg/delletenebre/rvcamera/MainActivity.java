package kg.delletenebre.rvcamera;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.graphics.Point;
import android.graphics.PointF;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.Gravity;
import android.view.SurfaceHolder;
import android.view.View;
import android.view.ViewTreeObserver;
import android.view.Window;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.RadioButton;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.shamanland.fonticon.FontIconTypefaceHolder;
import com.shamanland.fonticon.FontIconView;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import afzkl.development.colorpickerview.dialog.ColorPickerDialog;


public class MainActivity extends Activity {
    private String TAG = this.getClass().getName();

    private boolean isEditLayoutShow;
    private int fullscreenMode = 0;

    private EasycamView camView;
    private SurfaceHolder mHolder;
    private FrameLayout currentLayout;

    private SharedPreferences _settings;

    private GuideLinesView glView;

    private FontIconView btnGLEditSave,
                         btnGLEditReset,
                         btnGLEditChooseColor,
                         btnGLEditUp,
                         btnGLEditRight,
                         btnGLEditDown,
                         btnGLEditLeft;
    private float lineMoveStep;
    private static final int radioButtonMargin = dpToPx(17);


    private List<RadioButton> radioButtonsList = new ArrayList<>();

    protected RelativeLayout layoutGLEdit;
    private Point layoutGLEditSize;

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);

        if(fullscreenMode == 2 && android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.KITKAT) {
            getWindow().getDecorView().setSystemUiVisibility(
                      View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                    | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                    | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                    | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                    | View.SYSTEM_UI_FLAG_FULLSCREEN
                    | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY);
        }

    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        _settings = PreferenceManager.getDefaultSharedPreferences(this);
        fullscreenMode = Integer.parseInt(_settings.getString("app_fullscreen", "2"));
        if (fullscreenMode > 0) {
            requestWindowFeature(Window.FEATURE_NO_TITLE);
            getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        }

        super.onCreate(savedInstanceState);

        FontIconTypefaceHolder.init(getAssets(), "icons.ttf");

        setContentView(R.layout.activity_main);

        App.detectSignalService.setMainActivity(this);

        currentLayout = (FrameLayout) findViewById(R.id.mainLayout);
        camView = (EasycamView) findViewById(R.id.camview);
        mHolder = camView.getHolder();

        SharedPreferences.Editor _settingsEditor = _settings.edit();
        _settingsEditor.putBoolean("pref_key_layout_toasts", false);
        _settingsEditor.commit();

        glView = (GuideLinesView) findViewById(R.id.guide_lines);


        /**** Display: Calibrate guide lines ****/
        layoutGLEdit = (RelativeLayout)findViewById(R.id.guide_lines_edit_layout);
        isEditLayoutShow = getIntent().getBooleanExtra("gl_edit_layout_show", false);
        if ( isEditLayoutShow ) {

            lineMoveStep = 0.005f;

            layoutGLEdit.setVisibility(RelativeLayout.VISIBLE);

            layoutGLEdit.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
                @Override
                public void onGlobalLayout() {
                    layoutGLEdit.getViewTreeObserver().removeGlobalOnLayoutListener(this);

                    int w = layoutGLEdit.getMeasuredWidth(),
                        h = layoutGLEdit.getMeasuredHeight();

                    layoutGLEditSize = new Point(w, h);

                    int current_style_index = Integer.parseInt(_settings.getString("guide_lines_style", "0"));
                    GuideLinesStyle gl_style = new GuideLinesStyle(MainActivity.this);
                    List<GuideLinesStyle.GuideLine> style = gl_style.getStyleFromSettings( current_style_index );

                    String settingsPrefix = gl_style.settingsPrefix + current_style_index;
                    for(int i = 0; i < style.size(); i++) {
                        GuideLinesStyle.GuideLine gline = style.get(i);

                        String settingsPrefixGLine = settingsPrefix + "_l" + i;

                        PointF a = gline.a,
                               b = gline.b;

                        RadioButton rb = new RadioButton(MainActivity.this);
                        rb.setX(Math.round(a.x * w - radioButtonMargin));
                        rb.setY(Math.round(a.y * h - radioButtonMargin));
                        rb.setContentDescription(settingsPrefixGLine + "_a");
                        radioButtonsList.add(rb);

                        rb = new RadioButton(MainActivity.this);
                        rb.setX(Math.round(b.x * w - radioButtonMargin));
                        rb.setY(Math.round(b.y * h - radioButtonMargin));
                        rb.setContentDescription(settingsPrefixGLine + "_b");
                        radioButtonsList.add(rb);
                    }

                    for(int i = 0; i < radioButtonsList.size(); i++) {
                        RadioButton rb = radioButtonsList.get(i);
                        layoutGLEdit.addView(rb);

                        rb.setOnClickListener(new RadioButton.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                RadioButton _this = (RadioButton) v;

                                for(int i = 0; i < radioButtonsList.size(); i++) {
                                    RadioButton rb = radioButtonsList.get(i);
                                    rb.setChecked(false);
                                }

                                _this.setChecked(true);
                            }
                        });
                    }
                }
            });

            btnGLEditSave = (FontIconView)findViewById(R.id.btn_save);
            btnGLEditReset = (FontIconView)findViewById(R.id.btn_reset);
            btnGLEditChooseColor = (FontIconView)findViewById(R.id.btn_color);
            btnGLEditUp = (FontIconView)findViewById(R.id.btn_up);
            btnGLEditDown = (FontIconView)findViewById(R.id.btn_down);
            btnGLEditLeft = (FontIconView)findViewById(R.id.btn_left);
            btnGLEditRight = (FontIconView)findViewById(R.id.btn_right);

            //**** Нажатие кнопки СОХРАНИТЬ
            btnGLEditSave.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    layoutGLEdit.setVisibility(RelativeLayout.GONE);

                    //Toast.makeText(getApplicationContext(), R.string.gl_saved, Toast.LENGTH_LONG).show();
                    finish();
                }
            });

            //**** Нажатие кнопки ВВЕРХ
            btnGLEditUp.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    moveGuideLineByRadioButton(getActiveRadioButton(), "up", false);
                }
            });

            //**** Нажатие (долгое) кнопки ВВЕРХ
            btnGLEditUp.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    moveGuideLineByRadioButton(getActiveRadioButton(), "up", true);
                    return true;
                }
            });

            //**** Нажатие кнопки ВНИЗ
            btnGLEditDown.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    moveGuideLineByRadioButton(getActiveRadioButton(), "down", false);
                }
            });
            //**** Нажатие (долгое) кнопки ВНИЗ
            btnGLEditDown.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    moveGuideLineByRadioButton(getActiveRadioButton(), "down", true);
                    return true;
                }
            });

            //**** Нажатие кнопки ВЛЕВО
            btnGLEditLeft.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    moveGuideLineByRadioButton(getActiveRadioButton(), "left", false);
                }
            });
            //**** Нажатие (долгое) кнопки ВЛЕВО
            btnGLEditLeft.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    moveGuideLineByRadioButton(getActiveRadioButton(), "left", true);
                    return true;
                }
            });

            //**** Нажатие кнопки ВПРАВО
            btnGLEditRight.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    moveGuideLineByRadioButton(getActiveRadioButton(), "right", false);
                }
            });
            //**** Нажатие (долгое) кнопки ВПРАВО
            btnGLEditRight.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    moveGuideLineByRadioButton(getActiveRadioButton(), "right", true);
                    return true;
                }
            });

            btnGLEditChooseColor.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    RadioButton rb = getActiveRadioButton();
                    if ( rb != null ) {
                        String settingKeyTemp = rb.getContentDescription().toString();
                        final String settingKeyColor = settingKeyTemp.substring(0, settingKeyTemp.length()-1) + "color";
                        final String settingKeyEffect = settingKeyTemp.substring(0, settingKeyTemp.length()-1) + "effect";
                        final String settingKeyWidth = settingKeyTemp.substring(0, settingKeyTemp.length()-1) + "width";

                        int initialColor = (int) getCurrentStyleMap().get(settingKeyColor);
                        int initialEffect = (int) getCurrentStyleMap().get(settingKeyEffect);
                        int initialWidth = (int) getCurrentStyleMap().get(settingKeyWidth);

                        final ColorPickerDialog colorDialog = new ColorPickerDialog(MainActivity.this, initialColor, initialEffect, initialWidth);

                        colorDialog.setAlphaSliderVisible(true);

                        colorDialog.setButton(DialogInterface.BUTTON_POSITIVE, getString(android.R.string.ok), new DialogInterface.OnClickListener() {

                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                SharedPreferences.Editor _settingsEditor = _settings.edit();
                                _settingsEditor.putInt(settingKeyColor, colorDialog.getColor());
                                _settingsEditor.putInt(settingKeyEffect, colorDialog.getLineStyle());
                                _settingsEditor.putInt(settingKeyWidth, colorDialog.getLineWidth());
                                _settingsEditor.commit();

                                if (glView != null) {
                                    glView.invalidate();
                                }

                                //Toast.makeText(MainActivity.this, getString(R.string.choosed_color) + ": " + colorToHexString(colorDialog.getColor()), Toast.LENGTH_LONG).show();
                            }
                        });

                        colorDialog.setButton(DialogInterface.BUTTON_NEGATIVE, getString(android.R.string.cancel), new DialogInterface.OnClickListener() {

                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                // Делать нечего
                            }
                        });

                        colorDialog.show();

                    } else {
                        Toast.makeText(MainActivity.this, R.string.choose_line, Toast.LENGTH_SHORT).show();
                    }
                }
            });

            btnGLEditReset.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    new AlertDialog.Builder(MainActivity.this)
                            .setTitle(R.string.title_confirm_reset_guide_line_style)
                            .setMessage(R.string.confirm_reset_guide_line_style)
                            .setCancelable(true)
                            .setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int id) {
                                    GuideLinesStyle gl_style = new GuideLinesStyle(MainActivity.this);
                                    gl_style.resetCurrentStyleToDefaults();
                                    Map currentStyle = gl_style.getStyleMap();

                                    if (glView != null) {
                                        if(radioButtonsList.size() > 0) {
                                            for (int i = 0; i < radioButtonsList.size(); i++) {
                                                RadioButton rb = radioButtonsList.get(i);
                                                PointF point = (PointF) currentStyle.get(rb.getContentDescription().toString());
                                                rb.setX(Math.round(point.x * layoutGLEdit.getMeasuredWidth() - radioButtonMargin));
                                                rb.setY(Math.round(point.y * layoutGLEdit.getMeasuredHeight() - radioButtonMargin));
                                            }
                                        }

                                        glView.invalidate();
                                    }

                                    Toast.makeText(MainActivity.this, R.string.guide_lines_style_was_reset, Toast.LENGTH_SHORT).show();
                                }
                            })
                            .setNegativeButton(R.string.no, null)
                            .show();
                }
            });
            /**** END Display: Calibrate guide lines ****/
        } else {
            // При двойном клике по экрану открываем НАСТРОЙКИ
            glView.setOnClickListener(new DoubleClickListener() {
                @Override
                public void onSingleClick(View v) {}

                @Override
                public void onDoubleClick(View v) {
                    Intent settingsActivity = new Intent(getBaseContext(), SettingsActivity.class);
                    startActivity(settingsActivity);
                }
            });
        }



    }

    @Override
    protected void onPause() {
        super.onPause();

        if ( isEditLayoutShow ) {
            App.activityPaused();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();

        _settings = PreferenceManager.getDefaultSharedPreferences(this);

        if ( glView != null ) {
            glView.invalidate();
        }

        if ( isEditLayoutShow ) {
            App.activityResumed();
        }
    }

    private PointF getCoordinatesForY(PointF a, PointF b, float x) {
        return new PointF( x, getPointY(a, b, x) );
    }

    private PointF getCoordinatesForX(PointF a, PointF b, float y) {
        return new PointF( getPointX(a, b, y), y );
    }

    private float getPointY(PointF a, PointF b, float x) {
        return ( (b.y - a.y) * (x - a.x) / (b.x - a.x) ) + a.y;
    }

    private float getPointX(PointF a, PointF b, float y) {
        return ( (b.x - a.x) * (y - a.y) / (b.y - a.y) ) + a.x;
    }

    private static int dpToPx(int dp) {
        return (int) (dp * Resources.getSystem().getDisplayMetrics().density);
    }

    private Map getCurrentStyleMap() {
        GuideLinesStyle gl_style = new GuideLinesStyle(MainActivity.this);
        return gl_style.getStyleMap();
    }

    private PointF getCoordinatesForRB(RadioButton rb) {
        Map style = getCurrentStyleMap();

        return (PointF) style.get(String.valueOf(rb.getContentDescription()));
    }


    private void saveCoordinatesForRB(CharSequence settingKey, PointF point) {
        SharedPreferences.Editor _settingsEditor = _settings.edit();
        _settingsEditor.putFloat(String.valueOf(settingKey) + "x", point.x);
        _settingsEditor.putFloat(String.valueOf(settingKey) + "y", point.y);
        _settingsEditor.commit();
    }

    private void moveGuideLineByRadioButton(RadioButton rb, String direction, boolean longClick) {
        if (rb != null && layoutGLEditSize != null) {
            float step = longClick ? lineMoveStep * 10 : lineMoveStep;
            PointF point = getCoordinatesForRB(rb);

            if (direction.equals("up")) {
                point.y -= step;

            } else if (direction.equals("down")) {
                point.y += step;

            } else if (direction.equals("left")) {
                point.x -= step;

            } else if (direction.equals("right")) {
                point.x += step;

            }
            point = checkPointForMinMax(point);

            rb.setX(Math.round(point.x * layoutGLEditSize.x - radioButtonMargin));
            rb.setY(Math.round(point.y * layoutGLEditSize.y - radioButtonMargin));

            saveCoordinatesForRB(rb.getContentDescription(), point);

            if (glView != null) {
                glView.invalidate();
            }
        }
    }


    private PointF checkPointForMinMax(PointF point) {
        point.x = (point.x > 1.0f) ? 1.0f : point.x;
        point.y = (point.y > 1.0f) ? 1.0f : point.y;
        point.x = (point.x < 0.0f) ? 0.0f : point.x;
        point.y = (point.y < 0.0f) ? 0.0f : point.y;

        return point;
    }

    private RadioButton getActiveRadioButton() {
        if(radioButtonsList.size() > 0) {
            for (int i = 0; i < radioButtonsList.size(); i++) {
                RadioButton rb = radioButtonsList.get(i);
                if( rb.isChecked() ) {
                    return rb;
                }
            }
        }

        return null;
    }
}
