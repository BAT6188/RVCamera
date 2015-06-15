package afzkl.development.colorpickerview;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.widget.EditText;

public class ARGBEditText extends EditText {

    private String mPrefix = "ARGB: #";
    private Rect mPrefixRect = new Rect();

    public ARGBEditText(Context context, AttributeSet attrs) {
        super(context, attrs);
        TypedArray a = getContext().obtainStyledAttributes(attrs, R.styleable.ARGBEditText, 0, 0);
        try {
            String xmlProvidedPrefix = a.getString(R.styleable.ARGBEditText_prefix);
            if ( xmlProvidedPrefix != null ) {
                mPrefix = xmlProvidedPrefix;
            }
        } finally {
            a.recycle();
        }
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        getPaint().getTextBounds(mPrefix, 0, mPrefix.length(), mPrefixRect);
        //mPrefixRect.right += getPaint().measureText(""); // add some offset

        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        canvas.drawText(mPrefix, super.getCompoundPaddingLeft(), getBaseline(), getPaint());
    }

    @Override
    public int getCompoundPaddingLeft() {
        return super.getCompoundPaddingLeft() + mPrefixRect.width();
    }

    public void setColor(int color) {
        this.setText( colorToHexString(color) );
    }

    private String colorToHexString(int color) {
        return String.format("%06X", 0xFFFFFFFF & color);
    }
}
