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

public class HookedPositionTextView extends android.support.v7.widget.AppCompatTextView {
    IPlayer player;

    public HookedPositionTextView(Context context, AttributeSet attrs) {
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
            if (currentProgram != null && currentProgram.getDuration() != null ) {
                long programDuration = currentProgram.getDuration();
                long positionInTheProgram = Math.max(0, Math.min(programDuration, player.getPlayheadTime() - currentProgram.startDateTime.getMillis()));
                String newPosition = DateTimeParser.formatDisplayTime(positionInTheProgram);
                if (newPosition.equals(text) == false) {
                    setText(newPosition);
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
