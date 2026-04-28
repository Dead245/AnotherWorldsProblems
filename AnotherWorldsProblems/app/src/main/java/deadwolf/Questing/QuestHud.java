package deadwolf.Questing;

import javax.annotation.Nonnull;

import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.entity.entities.player.hud.CustomUIHud;
import com.hypixel.hytale.server.core.ui.builder.UICommandBuilder;
import com.hypixel.hytale.server.core.universe.PlayerRef;

public class QuestHud extends CustomUIHud {

    public QuestHud(PlayerRef playerReference) {
        super(playerReference);
    }

    @Override
    protected void build(@Nonnull UICommandBuilder uiCmdBuilder) {
        uiCmdBuilder.append("Hud/QuestHud.ui");
        //Default text
        uiCmdBuilder.set("#QuestTitle.TextSpans", Message.raw("Quest Title"));
        uiCmdBuilder.set("#QuestText.TextSpans", Message.raw("Quest Description"));
    }

    public void updateQuestDisplay(@Nonnull String title, @Nonnull String text){
        UICommandBuilder uiCmdBuilder = new UICommandBuilder();

        uiCmdBuilder.set("#QuestTitle.TextSpans", Message.raw(title));
        uiCmdBuilder.set("#QuestText.TextSpans", Message.raw(text));
        update(false, uiCmdBuilder);
    }
    
}
