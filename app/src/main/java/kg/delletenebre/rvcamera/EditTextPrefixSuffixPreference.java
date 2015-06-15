package kg.delletenebre.rvcamera;

import android.content.Context;
import android.content.res.Resources;
import android.preference.EditTextPreference;
import android.util.AttributeSet;


public class EditTextPrefixSuffixPreference extends EditTextPreference {
    private String  prefix = "",
                    suffix = "";

    public EditTextPrefixSuffixPreference(Context context) { super(context); }
    public EditTextPrefixSuffixPreference(Context context, AttributeSet attrs) {
        super(context, attrs);
        for (int i=0;i<attrs.getAttributeCount();i++) {
            String attr = attrs.getAttributeName(i);
            String val  = attrs.getAttributeValue(i);

            if (attr.equalsIgnoreCase("prefix")) {
                prefix = getAttributeStringValue(context, val, "");
            } else if (attr.equalsIgnoreCase("suffix")) {
                suffix = getAttributeStringValue(context, val, "");
            }

        }
    }

    @Override
    public void setText(String value) {
        super.setText(value);

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
