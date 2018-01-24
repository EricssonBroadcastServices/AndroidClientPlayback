package net.ericsson.emovs.playback.techs.ExoPlayer;

import android.content.Context;
import android.util.AttributeSet;

import com.google.android.exoplayer2.ui.DefaultTimeBar;

import net.ericsson.emovs.utilities.interfaces.IPlayer;

/**
 * Created by Joao Coelho on 2018-01-24.
 */

public class HookedDefaultTimeBar extends DefaultTimeBar {
    IPlayer player;

    public HookedDefaultTimeBar(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public void bindPlayer(IPlayer player) {
        this.player = player;
    }

    @Override
    public void setPosition(long position) {
        super.setPosition(position);
    }

    @Override
    public void setBufferedPosition(long bufferedPosition) {
        super.setBufferedPosition(bufferedPosition);
    }

    @Override
    public void setDuration(long duration) {
        super.setDuration(duration);
    }

    @Override
    public void setEnabled(boolean enabled) {
        super.setEnabled(enabled);
    }
}
