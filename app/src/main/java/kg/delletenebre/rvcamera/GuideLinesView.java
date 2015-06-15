package kg.delletenebre.rvcamera;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.DashPathEffect;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PointF;
import android.graphics.RectF;
import android.os.Build;
import android.preference.PreferenceManager;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;

import java.util.List;

public final class GuideLinesView extends View {
    private static final String TAG = GuideLinesView.class.getSimpleName();

    Context _context;
    SharedPreferences _settings;


    // drawing tools
    Canvas canvas;
    private int screenScale;
    private Bitmap background, defaultBitmap; // holds the cached static part
    private RectF defaultRectf, glRectf;
    private Paint glPaint;
    private Path  glPath;


    // **** Attributes ****
    private int glColor;

    public GuideLinesView(Context context, AttributeSet attrs) {
        super(context, attrs);

        _context = context;
        _settings = PreferenceManager.getDefaultSharedPreferences(getContext());

        initDrawingTools();
    }



    private void initDrawingTools() {
        defaultRectf = new RectF(0,0,1,1);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
            setLayerType(LAYER_TYPE_SOFTWARE, null);
        }

        glRectf = new RectF();
        glPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        glPaint.setStyle(Paint.Style.STROKE);
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        screenScale = Math.min(getWidth(), getHeight());

        defaultBitmap = Bitmap.createBitmap(screenScale, screenScale, Bitmap.Config.ARGB_8888);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        if( _settings.getBoolean("guide_lines_show", true) ) {
            GuideLinesStyle gl_style = new GuideLinesStyle(getContext());
            List<GuideLinesStyle.GuideLine> style = gl_style.getStyleFromSettings(Integer.parseInt(_settings.getString("guide_lines_style", "0")));//get current guide lines style

            int w = getWidth(),
                h = getHeight();

            glRectf.set(0,0, w,h);

            for(int i = 0; i < style.size(); i++) {
                GuideLinesStyle.GuideLine gline = style.get(i);

                int color = gline.color,
                    width = gline.width,
                    effect = gline.effect;

                PointF a = gline.a,
                       b = gline.b;

                glPath = new Path();

                glPaint.setStrokeWidth(width);
                glPaint.setColor(color);

                glPaint.setPathEffect(getPathEffect(effect, width));

                glPath.moveTo(a.x * w, a.y * h);
                glPath.lineTo(b.x * w, b.y * h);

                canvas.drawPath(glPath, glPaint);
            }
        }
    }

    private DashPathEffect getPathEffect(int index, int width) {
        DashPathEffect effect;

        switch(index) {
            case 1:
                effect = new DashPathEffect(new float[] { dpToPx(width * 5), dpToPx(width * 3) }, 0);//dashed
                break;
            case 2:
                effect = new DashPathEffect(new float[] { dpToPx(width), dpToPx(width * 2) }, 0);//dotted
                break;
            case 3:
                effect = new DashPathEffect(new float[] { dpToPx(width * 5), dpToPx(width * 3), dpToPx(width), dpToPx(width * 3) }, 0);//dash-dotted
                break;
            default:
                effect = null;
                break;
        }

        return effect;
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

    public static int dpToPx(int dp) {
        return (int) (dp * Resources.getSystem().getDisplayMetrics().density);
    }

}

