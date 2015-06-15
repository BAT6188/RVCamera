package kg.delletenebre.rvcamera;

import android.content.Context;
import android.content.res.Resources;
import android.preference.EditTextPreference;
import android.util.AttributeSet;


public class EditNumberPreference extends EditTextPreference {
    private String  prefix = "",
                    suffix = "";
    private int min = 0,
                max = 0;

    public EditNumberPreference(Context context) { super(context); }
    public EditNumberPreference(Context context, AttributeSet attrs) {
        super(context, attrs);
        for (int i=0;i<attrs.getAttributeCount();i++) {
            String attr = attrs.getAttributeName(i);
            String val  = attrs.getAttributeValue(i);

            if (attr.equalsIgnoreCase("prefix")) {
                prefix = getAttributeStringValue(context, val, "");
            } else if (attr.equalsIgnoreCase("suffix")) {
                suffix = getAttributeStringValue(context, val, "");
            } else if (attr.equalsIgnoreCase("min")) {
                min = Integer.valueOf(val);
            } else if (attr.equalsIgnoreCase("max")) {
                max = Integer.valueOf(val);
            }

        }
    }

    @Override
    public void setText(String value) {
        int val = min;

        if(value != null && !value.equals("")) {
            val = Integer.valueOf(value);
            if (min < max) {
                if (val < min) {
                    val = min;
                } else if (val > max) {
                    val = max;
                }
            }
        }

        super.setText(String.valueOf(val));

        setSummary(prefix + getText() + suffix);
    }

    private String getAttributeStringValue(Context context, String name, String defaultValue) {
        Resources res = context.getResources();

        if( name.length() > 1 && name.charAt(0) == '@' &&  name.contains("@string/") ) {
            final int id = res.getIdentifier(context.getPackageName() + ":" + name.substring(1), null, null);
            name = res.getString(id);
        } else {
            name = defaultValue;
        }

        return name;
    }

}
