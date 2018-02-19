package net.ericsson.emovs.playback.techs.ExoPlayer;

import android.content.Context;
import android.util.AttributeSet;

import com.google.android.exoplayer2.ui.DefaultTimeBar;

import net.ericsson.emovs.utilities.interfaces.IEntitledPlayer;
import net.ericsson.emovs.utilities.interfaces.IPlayer;
import net.ericsson.emovs.utilities.models.EmpProgram;

/**
 * Created by Joao Coelho on 2018-01-24.
 */

public class HookedDefaultTimeBar extends DefaultTimeBar {
    IPlayer player;

    // TODO: update timeline when program changes
    // TODO: hook timeline duration and current position
    
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
        if (player instanceof IEntitledPlayer) {
            IEntitledPlayer entitledPlayer = (IEntitledPlayer) player;
            EmpProgram currentProgram = entitledPlayer.getCurrentProgram();
            if (currentProgram != null && currentProgram.endDateTime != null && currentProgram.startDateTime != null) {
                long programDuration = currentProgram.endDateTime.getMillis() - currentProgram.startDateTime.getMillis();
                super.setDuration(programDuration);
                return;
            }
        }
        super.setDuration(duration);
    }

    @Override
    public void setEnabled(boolean enabled) {
        super.setEnabled(enabled);
    }
}
