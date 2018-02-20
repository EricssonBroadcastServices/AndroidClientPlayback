package net.ericsson.emovs.playback.techs.ExoPlayer;

import android.content.Context;
import android.util.AttributeSet;

import net.ericsson.emovs.utilities.interfaces.IEntitledPlayer;
import net.ericsson.emovs.utilities.interfaces.IPlayer;
import net.ericsson.emovs.utilities.models.EmpProgram;
import net.ericsson.emovs.utilities.time.DateTimeParser;

/**
 * Created by Joao Coelho on 2018-01-24.
 */

public class HookedDurationTextView extends android.support.v7.widget.AppCompatTextView {
    IPlayer player;

    public HookedDurationTextView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public void bindPlayer(IPlayer player) {
        this.player = player;
    }

    @Override
    protected void onTextChanged(CharSequence text, int start, int lengthBefore, int lengthAfter) {
        if (player != null && player instanceof IEntitledPlayer) {
            IEntitledPlayer entitledPlayer = (IEntitledPlayer) player;
            EmpProgram currentProgram = entitledPlayer.getCurrentProgram();
            long[] seekableTimeRange = player.getSeekTimeRange();
            if (currentProgram != null && currentProgram.getDuration() != null && seekableTimeRange != null) {
                long liveDuration = seekableTimeRange[1] - currentProgram.startDateTime.getMillis();
                String newDuration = DateTimeParser.formatDisplayTime(Math.min(currentProgram.getDuration(), liveDuration));
                if (newDuration.equals(text) == false) {
                    setText(newDuration);
                    invalidate();
                }
                else {
                    super.onTextChanged(text, start, lengthBefore, lengthAfter);
                }
                return;
            }
        }
        super.onTextChanged(text, start, lengthBefore, lengthAfter);
    }
}
