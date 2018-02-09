package net.ericsson.emovs.playback.ui.adapters;

import android.content.Context;
import android.graphics.Typeface;
import android.text.SpannableString;
import android.text.style.StyleSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;

import net.ericsson.emovs.playback.R;
import net.ericsson.emovs.playback.ui.activities.SimplePlaybackActivity;
import net.ericsson.emovs.playback.ui.views.EMPPlayerView;

import java.util.List;
import java.util.Locale;

/**
 * Created by Joao Coelho on 2017-11-27.
 */

public class LanguageAdapter extends ArrayAdapter<String> {
    public enum TrackType {
        AUDIO,
        SUBS
    };

    private static final String[] languages = Locale.getISOLanguages();
    private String[] pickerLangCodes;
    private final Spinner holder;
    private TrackType trackType;

    public LanguageAdapter(Spinner holder, SimplePlaybackActivity context, TrackType trackType, List<String> objects) {
        super(context, 0, objects);
        this.trackType = trackType;
        this.holder = holder;
        bindSpinnerSpecificActions();
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
        if (trackType == TrackType.SUBS) {
            return pickerLangCodes.length + 1;
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
        if (position >= pickerLangCodes.length) {
            SpannableString spanString = new SpannableString("Disable");
            spanString.setSpan(new StyleSpan(Typeface.ITALIC), 0, spanString.length(), 0);
            langNameView.setText(spanString);
        }
        else {
            if (pickerLangCodes[position] != null) {
                Locale loc = new Locale(pickerLangCodes[position]);
                langNameView.setText(loc.getDisplayLanguage().substring(0, 1).toUpperCase() + loc.getDisplayLanguage().substring(1));
            }
        }


        if(hideIcon) {
            ImageView langIconView = view.findViewById(R.id.lang_icon);
            langIconView.setVisibility(View.GONE);
            if (holder.getSelectedItemPosition() == position) {
                view.setBackgroundColor(view.getResources().getColor(R.color.green));
            }
        }
        else {
            ImageView langIconView = view.findViewById(R.id.lang_icon);
            if (trackType == TrackType.AUDIO) {
                langIconView.setImageDrawable(view.getResources().getDrawable(R.drawable.ic_audiotrack_white_24dp));
            } else if(trackType == TrackType.SUBS) {
                langIconView.setImageDrawable(view.getResources().getDrawable(R.drawable.ic_subtitles_white_24dp));
            }
            langNameView.setVisibility(View.GONE);
        }

        return view;
    }

    private void bindSpinnerSpecificActions() {
        holder.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                for (final EMPPlayerView pView : ((SimplePlaybackActivity) getContext()).getPlayerViews()) {
                    if(pView == null || pView.getPlayer() == null) {
                        continue;
                    }
                    if (trackType == TrackType.AUDIO) {
                        pView.getPlayer().selectAudioLanguage(getLangCode(i));
                    } else if(trackType == TrackType.SUBS) {
                        if (i >= pickerLangCodes.length) {
                            pView.getPlayer().selectTextLanguage(null);
                        }
                        else {
                            pView.getPlayer().selectTextLanguage(getLangCode(i));
                        }
                    }
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });
        holder.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                holder.setDropDownVerticalOffset(
                        holder.getDropDownVerticalOffset() + holder.getHeight());
                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.JELLY_BEAN) {
                    holder.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                } else {
                    holder.getViewTreeObserver().removeGlobalOnLayoutListener(this);
                }
            }
        });
    }
}
