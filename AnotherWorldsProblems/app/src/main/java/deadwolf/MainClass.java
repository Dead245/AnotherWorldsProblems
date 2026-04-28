package deadwolf;

import com.hypixel.hytale.component.ComponentType;
import com.hypixel.hytale.logger.HytaleLogger;
import com.hypixel.hytale.server.core.HytaleServerConfig;
import com.hypixel.hytale.server.core.event.events.player.PlayerReadyEvent;
import com.hypixel.hytale.server.core.modules.interaction.interaction.config.Interaction;
import com.hypixel.hytale.server.core.plugin.JavaPlugin;
import com.hypixel.hytale.server.core.plugin.JavaPluginInit;
import com.hypixel.hytale.server.core.universe.world.storage.ChunkStore;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.hypixel.hytale.server.npc.NPCPlugin;

import deadwolf.CameraScenes.CameraSceneComponent;
import deadwolf.CameraScenes.CameraTick;
import deadwolf.CameraScenes.StartSceneInteraction;
import deadwolf.CameraScenes.TriggerDataComponent;
import deadwolf.Questing.BuilderActionQuestNPCInteract;
import deadwolf.Questing.ProgressionComponent;
import deadwolf.Questing.PlayerKillTracker;
import deadwolf.World.PlayerJoinWorldEvent;

import com.hypixel.hytale.server.core.HytaleServerConfig.Defaults;


public class MainClass extends JavaPlugin{
    public static final HytaleLogger LOGGER = HytaleLogger.forEnclosingClass();
    private ComponentType<EntityStore, CameraSceneComponent> sceneDataComponentType;
    private ComponentType<ChunkStore, TriggerDataComponent> triggerDataComponentType;
    private ComponentType<EntityStore, ProgressionComponent> progressionComponentType;
    public static MainClass instance;

    public MainClass(JavaPluginInit init) {
        super(init);
        LOGGER.atInfo().log("Plugin %s version %s initialized.", this.getName(),this.getManifest().getVersion().toString());
        instance = this;
    }
    
    @Override
    protected void setup(){
        this.getCommandRegistry().registerCommand(new SetTrackCommand());

        //Camera Scene Related
        this.sceneDataComponentType = this.getEntityStoreRegistry().registerComponent(CameraSceneComponent.class, CameraSceneComponent::new);
        this.triggerDataComponentType = this.getChunkStoreRegistry().registerComponent(TriggerDataComponent.class, "TriggerData", TriggerDataComponent.CODEC);

        this.getCodecRegistry(Interaction.CODEC).register("TriggerScene", StartSceneInteraction.class, StartSceneInteraction.CODEC);

        this.getEntityStoreRegistry().registerSystem(new CameraTick(this.sceneDataComponentType));

        //Quest Related
        NPCPlugin.get().registerCoreComponentType("GetQuest", BuilderActionQuestNPCInteract::new);
        this.progressionComponentType = this.getEntityStoreRegistry().registerComponent(ProgressionComponent.class, "ProgressionData",ProgressionComponent.CODEC);
        this.getEntityStoreRegistry().registerSystem(new PlayerKillTracker());

        //World Join Event
        this.getEventRegistry().registerGlobal(PlayerReadyEvent.class, PlayerJoinWorldEvent::onPlayerReady);

        HytaleServerConfig serverConfig = HytaleServerConfig.load();
        Defaults serverDefaults = serverConfig.getDefaults();
        serverDefaults.setWorld("NewWorld");
        HytaleServerConfig.save(serverConfig);
    }

    public ComponentType<EntityStore, CameraSceneComponent> getSceneDataComponentType() {
        return this.sceneDataComponentType;
    }

    public ComponentType<ChunkStore, TriggerDataComponent> getTriggerDataComponentType() {
        return this.triggerDataComponentType;
    }

    public ComponentType<EntityStore, ProgressionComponent> getProgressionComponentType() {
        return this.progressionComponentType;
    }
}
