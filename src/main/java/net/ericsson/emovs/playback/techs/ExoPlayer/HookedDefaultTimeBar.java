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

    public HookedDefaultTimeBar(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public void bindPlayer(IPlayer player) {
        this.player = player;
    }

    @Override
    public void setPosition(long position) {
        if (player instanceof IEntitledPlayer) {
            IEntitledPlayer entitledPlayer = (IEntitledPlayer) player;
            EmpProgram currentProgram = entitledPlayer.getCurrentProgram();
            if (currentProgram != null && currentProgram.getDuration() != null) {
                long duration = currentProgram.getDuration();
                long newPosition = Math.min(duration, Math.max(0, player.getPlayheadTime() - currentProgram.startDateTime.getMillis()));
                super.setPosition(newPosition);
                return;
            }
        }
        super.setPosition(position);
    }

    @Override
    public void setBufferedPosition(long bufferedPosition) {
        if (player instanceof IEntitledPlayer) {
            IEntitledPlayer entitledPlayer = (IEntitledPlayer) player;
            EmpProgram currentProgram = entitledPlayer.getCurrentProgram();
            long[] bufferedTimeRange = player.getBufferedTimeRange();
            if (currentProgram != null && currentProgram.getDuration() != null && bufferedTimeRange != null) {
                long duration = currentProgram.getDuration();
                long newPosition = Math.min(duration, Math.max(0, bufferedTimeRange[1] - currentProgram.startDateTime.getMillis()));
                super.setBufferedPosition(newPosition);
                return;
            }
        }
        super.setBufferedPosition(bufferedPosition);
    }

    @Override
    public void setDuration(long duration) {
        if (player instanceof IEntitledPlayer) {
            IEntitledPlayer entitledPlayer = (IEntitledPlayer) player;
            EmpProgram currentProgram = entitledPlayer.getCurrentProgram();
            long[] seekableTimeRange = player.getSeekTimeRange();
            if (currentProgram != null && currentProgram.getDuration() != null && seekableTimeRange != null) {
                long liveDuration = seekableTimeRange[1] - currentProgram.startDateTime.getMillis();
                long programDuration = currentProgram.getDuration();
                super.setDuration(Math.min(liveDuration, programDuration));
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
