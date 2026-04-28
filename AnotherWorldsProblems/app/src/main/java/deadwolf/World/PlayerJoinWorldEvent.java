package deadwolf.World;

import java.util.UUID;
import java.util.concurrent.CompletableFuture;

import com.hypixel.hytale.builtin.instances.InstancesPlugin;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.entity.UUIDComponent;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.event.events.player.PlayerReadyEvent;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.Universe;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;

import deadwolf.MainClass;
import deadwolf.Questing.ProgressionComponent;
import deadwolf.Questing.QuestHud;

public class PlayerJoinWorldEvent {

    public static void onPlayerReady(PlayerReadyEvent event){

        World currentWorld = event.getPlayer().getWorld();
        var store = currentWorld.getEntityStore().getStore();

        //Move player to modded instance if they end up in the default world
        if(currentWorld.getName().equalsIgnoreCase("default")){
            InstancesPlugin instances = InstancesPlugin.get();

            UUID playerUUID = store.getComponent(event.getPlayer().getReference(), UUIDComponent.getComponentType()).getUuid();
            PlayerRef playerRef = Universe.get().getPlayer(playerUUID);

            CompletableFuture<World> worldFuture = instances.spawnInstance(
                "WorldStruct",
                currentWorld,
                playerRef.getTransform());


            InstancesPlugin.teleportPlayerToLoadingInstance(
                playerRef.getReference(), 
                playerRef.getReference().getStore(),
                worldFuture,
                null);
        }

        //Turn on player Quest HUD if progression is not "intro", null, or ""
        Ref<EntityStore> playerReference = event.getPlayerRef();
        ProgressionComponent progressionComponent = store.ensureAndGetComponent(playerReference, MainClass.instance.getProgressionComponentType());

        if (progressionComponent.getProgressionID() != null) {
            UUID playerUUID = store.getComponent(event.getPlayer().getReference(), UUIDComponent.getComponentType()).getUuid();
            PlayerRef playerRef = Universe.get().getPlayer(playerUUID);
            Player player = event.getPlayer();

            QuestHud qstHud = new QuestHud(playerRef);
            CompletableFuture.runAsync(() -> {
                player.getHudManager().setCustomHud(playerRef, qstHud);
                qstHud.updateQuestDisplay(progressionComponent.getQuestTitle(), progressionComponent.getQuestDescription());
            }, player.getWorld());
        }


    }
}
