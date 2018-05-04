package net.ericsson.emovs.playback.techs.ExoPlayer;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import com.google.android.exoplayer2.ui.DefaultTimeBar;

import net.ericsson.emovs.utilities.interfaces.IEntitledPlayer;
import net.ericsson.emovs.utilities.interfaces.IPlayer;
import net.ericsson.emovs.utilities.models.EmpProgram;

/**
 * Created by Joao Coelho on 2018-01-24.
 */

public class HookedDefaultTimeBar extends DefaultTimeBar {
    IPlayer player;
    View liveLine;

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
        double MAX_WEIGHT = 10000.0;
        if (player instanceof IEntitledPlayer) {
            IEntitledPlayer entitledPlayer = (IEntitledPlayer) player;
            EmpProgram currentProgram = entitledPlayer.getCurrentProgram();
            long[] seekableTimeRange = player.getSeekTimeRange();
            if (currentProgram != null && currentProgram.getDuration() != null && seekableTimeRange != null) {
                long liveDuration = seekableTimeRange[1] - currentProgram.startDateTime.getMillis();
                long programDuration = currentProgram.getDuration();
                super.setDuration(Math.min(liveDuration, programDuration));

                if (currentProgram.liveNow()) {
                    long w = Math.round(liveDuration * MAX_WEIGHT / programDuration);
                    if (w < MAX_WEIGHT * 0.07) {
                        w = (long) (MAX_WEIGHT * 0.07);
                    }
                    if (w >= MAX_WEIGHT) {
                        this.liveLine.setVisibility(View.GONE);
                    }
                    else {
                        setWeight(w, (long) MAX_WEIGHT);
                    }
                }
                else {
                    this.liveLine.setVisibility(View.GONE);
                }
                return;
            }
        }
        super.setDuration(duration);
    }

    @Override
    public void setEnabled(boolean enabled) {
        super.setEnabled(enabled);
    }

    public void setWeight(long w, long max) {
        // Set weight for seekbar
        ViewGroup.LayoutParams p = getLayoutParams();
        if (p instanceof LinearLayout.LayoutParams) {
            LinearLayout.LayoutParams linearParams = (LinearLayout.LayoutParams) p;
            linearParams.weight = w;
        }
        this.setLayoutParams(p);

        if (this.liveLine != null) {
            // Set weight for live line
            p = this.liveLine.getLayoutParams();
            if (p instanceof LinearLayout.LayoutParams) {
                LinearLayout.LayoutParams linearParams = (LinearLayout.LayoutParams) p;
                linearParams.weight = max - w;
            }
            this.liveLine.setLayoutParams(p);
            this.liveLine.setVisibility(View.VISIBLE);
        }
    }
    public void bindLiveLine(View liveLine) {
        this.liveLine = liveLine;
    }
}
