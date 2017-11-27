package net.ericsson.emovs.playback.ui.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;

import net.ericsson.emovs.playback.R;

import java.util.List;
import java.util.Locale;

/**
 * Created by Joao Coelho on 2017-11-27.
 */

public class LanguageAdapter extends ArrayAdapter<String> {
    private static final String[] languages = Locale.getISOLanguages();
    private String[] pickerLangCodes;
    private int iconResId;
    private Spinner holder;

    public LanguageAdapter(Spinner holder, Context context, int iconResId, List<String> objects) {
        super(context, iconResId, objects);
        this.iconResId = iconResId;
        this.holder = holder;
    }

    public void setLanguages(String[] langs) {
        pickerLangCodes = langs;
        notifyDataSetChanged();
    }

    public String getLangCode(int position) {
        return pickerLangCodes[position];
    }

    @Override
    public int getCount() {
        if (pickerLangCodes == null) {
            return 0;
        }
        return pickerLangCodes.length;
    }

    @Override
    public View getDropDownView(int position, View convertView, ViewGroup parent) {
        return getLanguageView(position, convertView, parent, true);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        return getLanguageView(position, convertView, parent, false);
    }

    public View getLanguageView(int position, View convertView, ViewGroup parent, boolean hideIcon) {
        LayoutInflater inflater = (LayoutInflater) this.getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View view = inflater.inflate(R.layout.menu_item_language, parent, false);

        TextView langNameView = (TextView) view.findViewById(R.id.lang_label);
        Locale loc = new Locale(pickerLangCodes[position]);
        langNameView.setText(loc.getDisplayLanguage().substring(0, 1).toUpperCase() + loc.getDisplayLanguage().substring(1));

        if(hideIcon) {
            ImageView langIconView = view.findViewById(R.id.lang_icon);
            langIconView.setVisibility(View.GONE);
            if (holder.getSelectedItemPosition() == position) {
                view.setBackgroundColor(view.getResources().getColor(R.color.green));
            }
        }
        else {
            ImageView langIconView = view.findViewById(R.id.lang_icon);
            langIconView.setImageDrawable(view.getResources().getDrawable(this.iconResId));
            langNameView.setVisibility(View.GONE);
        }

        return view;
    }
}
