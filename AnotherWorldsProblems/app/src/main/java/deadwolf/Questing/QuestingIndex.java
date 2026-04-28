package deadwolf.Questing;

import java.util.concurrent.CompletableFuture;

import javax.annotation.Nonnull;

import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.universe.PlayerRef;

import deadwolf.MainClass;

public class QuestingIndex {

    //Determines questing order
    private static final String[] questOrder = new String[] {
        "intro",
        "help_the_villagers",
        "helped_the_villagers",
        "defeat_the_boss",
        "boss_defeated",
        "completed_all_quests"
    };

    //Handle quest progression based on quest ID when interacting with the NPC
    public static String handleProgression(@Nonnull ProgressionComponent progressionComp, @Nonnull Player player, @Nonnull PlayerRef playerRef) {
        QuestHud qstHud = new QuestHud(playerRef);
        String progressionID = progressionComp.getProgressionID();

        switch(progressionID) {
            case "":
            	player.sendMessage(Message.raw("???: Hey! Your one of those 'Avatars', right?"));
            	return questOrder[0];
            case "intro":
                player.sendMessage(Message.raw("Ferris: Name's Ferris. Can you help me out?\nWell let's see if I can trust you first...\nCould you defeat some of monsters that have been bothering the village?\nAll the other villagers have left due to them."));
                //Reveal quest HUD
                CompletableFuture.runAsync(() -> {
                    player.getHudManager().setCustomHud(playerRef, qstHud);
                    progressionComp.setQuestInfo("Help the Villagers", "Defeat the nearby monsters");
                    qstHud.updateQuestDisplay("Help the Villagers", "Defeat 8 nearby monsters (0/8)");
                }, player.getWorld());
                return questOrder[1];
            case "help_the_villagers":
                player.sendMessage(Message.raw("Ferris: I don't care what you attack!\nYou can look around for some equipment nearby."));
                return questOrder[1];
            case "helped_the_villagers":
                //ID is entered from the PlayerKillTracker
                player.sendMessage(Message.raw("Ferris: Oh you actually did it?...\nThen, can you defeat a nuisance for me?"));
                //Update quest HUD
                progressionComp.setQuestInfo("Defeat the Nuisance", "Go Southeast to x:500 z:500");
                qstHud.updateQuestDisplay("Defeat the Nuisance", "Go Southeast to x:500 z:500");
                return questOrder[3];
            case "defeat_the_boss":
                player.sendMessage(Message.raw("Ferris: Go defeat that nuisance! I'd do it but uhhh...\nI don't want to leave the village unguarded, you know?"));
                return questOrder[3];
            case "boss_defeated":
                player.sendMessage(Message.raw("Ferris: Oh wow you actually did it!\nUh...\nGreat job!"));
                //TODO trigger last cutscene
                progressionComp.setQuestInfo("Objectives Completed", "Thanks for playing!");
                qstHud.updateQuestDisplay("Objectives Completed", "Thanks for playing!");
                return questOrder[5];
            case "completed_all_quests":
                player.sendMessage(Message.raw("Ferris: I don't have anything else to ask of you! Thank you!...\nYou can leave now."));
                return questOrder[5];
            default:
                MainClass.LOGGER.atWarning().log("----- " + progressionID + " is an invalid Quest Progression ID -----");
        }

        return null;
    }
}
