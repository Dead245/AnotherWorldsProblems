package deadwolf.Questing;

import java.util.UUID;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.component.query.Query;
import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.entity.UUIDComponent;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.modules.entity.damage.Damage;
import com.hypixel.hytale.server.core.modules.entity.damage.Damage.Source;
import com.hypixel.hytale.server.core.modules.entity.damage.DeathComponent;
import com.hypixel.hytale.server.core.modules.entity.damage.DeathSystems;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.Universe;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.hypixel.hytale.server.npc.entities.NPCEntity;

import deadwolf.MainClass;

public class PlayerKillTracker extends DeathSystems.OnDeathSystem{

    @Override
    @Nullable
    public Query<EntityStore> getQuery() {
        //Check for an NPC's death
        return Query.and(NPCEntity.getComponentType());
    }

    @Override
    public void onComponentAdded(@Nonnull Ref<EntityStore> victimRef, @Nonnull DeathComponent deathComp,
            @Nonnull Store<EntityStore> store, @Nonnull CommandBuffer<EntityStore> cmdBuffer) {
        // When something dies, a death component is added to them, this is what we are checking.
        Damage deathInfo = deathComp.getDeathInfo();
        if (deathInfo == null) return; //Should never be null, but just in case.
        
        NPCEntity victim = store.getComponent(victimRef, NPCEntity.getComponentType());
        
        String victimId = victim.getNPCTypeId();

        Source source = deathInfo.getSource();
        if (source instanceof Damage.EntitySource entitySource) {
            //Damage.EntitySource entitySource = (Damage.EntitySource) source;
            Ref<EntityStore> killerRef = entitySource.getRef();
            
            Player playerComp = killerRef.getStore().getComponent(killerRef, Player.getComponentType());
            if (playerComp == null) return; //If the killer isn't a player, we don't care about it for quest progression.

            ProgressionComponent progressionComp = killerRef.getStore().getComponent(killerRef, MainClass.instance.getProgressionComponentType());
            //Increment Kill count for the quest progression or check victim ID for quest progression
            UUID playerUUID = store.getComponent(killerRef, UUIDComponent.getComponentType()).getUuid();
            PlayerRef playerRef = Universe.get().getPlayer(playerUUID);
            QuestHud qstHud = new QuestHud(playerRef);

            //This part is hard coded due to only 2 quests needing this logic, but ideally you would want to have something different (and better) for this in a larger scale quest system.
            if (progressionComp.getProgressionID().equals("help_the_villagers")) {
                if (progressionComp.getKillCount() < 8) {
                    progressionComp.incrementKillCount();
                    if (progressionComp.getKillCount() < 8) {
                        qstHud.updateQuestDisplay("Help the Villagers", "Defeat 8 nearby monsters (" + progressionComp.getKillCount() + "/8)");
                        progressionComp.setQuestInfo("Help the Villagers", "Defeat 8 nearby monsters (" + progressionComp.getKillCount() + "/8)");
                    } else { //For when they transition from 7 to 8 kills, we want to make sure the quest updates to the next step and not just say 8/8 kills.
                        progressionComp.setProgressionID("helped_the_villagers");
                        qstHud.updateQuestDisplay("Return to Ferris", "Speak of what you've slain.");
                        progressionComp.setQuestInfo("Return to Ferris", "Speak of what you've slain.");
                    }
                } else { //Just in case this is ran when the player already has 8 kills for some reason, we want to make sure they can still progress.
                    progressionComp.setProgressionID("helped_the_villagers");
                    qstHud.updateQuestDisplay("Return to Ferris", "Speak of what you've slain.");
                    progressionComp.setQuestInfo("Return to Ferris", "Speak of what you've slain.");
                }
            } else if (progressionComp.getProgressionID().equals("defeat_the_boss") && victimId.equals("Spawn_Void")) {
                progressionComp.setProgressionID("boss_defeated");
                qstHud.updateQuestDisplay("Return to Ferris", "Tell them of your victory.");
                progressionComp.setQuestInfo("Return to Ferris", "Tell them of your victory.");
            }

        }
    }
    
}
