package net.ericsson.emovs.playback.techs.ExoPlayer;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.ImageButton;

import com.google.android.exoplayer2.ui.DefaultTimeBar;

import net.ericsson.emovs.playback.R;
import net.ericsson.emovs.utilities.interfaces.IPlayer;

/**
 * Created by Joao Coelho on 2018-01-24.
 */

public class HookedImageButton extends android.support.v7.widget.AppCompatImageButton {
    IPlayer player;

    public HookedImageButton(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public void bindPlayer(IPlayer player) {
        this.player = player;
    }

    @Override
    public void setEnabled(boolean enabled) {
        if (this.player != null) {
            if ((this.getId() == R.id.exo_ffwd && !this.player.canSeekForward()) ||
                (this.getId() == R.id.exo_next && !this.player.canSeekForward()) ||
                (this.getId() == R.id.exo_rew && !this.player.canSeekBack()) ||
                (this.getId() == R.id.exo_prev && !this.player.canSeekBack()) ||
                (this.getId() == R.id.exo_pause && !this.player.canPause())) {
                super.setEnabled(false);
                setImageAlpha(77);
            }
        }
        super.setEnabled(enabled);
    }
}
