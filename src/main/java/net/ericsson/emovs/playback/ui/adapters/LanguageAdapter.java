package net.ericsson.emovs.playback.ui.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
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

    public LanguageAdapter(Context context, int resource, List<String> objects) {
        super(context, resource, objects);
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
        return getLanguageView(position, convertView, parent);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        return getLanguageView(position, convertView, parent);
    }

    public View getLanguageView(int position, View convertView, ViewGroup parent) {
        LayoutInflater inflater = (LayoutInflater) this.getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View view = inflater.inflate(R.layout.menu_item_language, parent, false);

        TextView langNameView = (TextView) view.findViewById(R.id.lang_label);
        Locale loc = new Locale(pickerLangCodes[position]);
        langNameView.setText(loc.getDisplayLanguage().substring(0, 1).toUpperCase() + loc.getDisplayLanguage().substring(1));

        return view;
    }
}
