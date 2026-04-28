package deadwolf.CameraScenes;

import java.util.HashMap;
import java.util.Map;

import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.math.vector.Vector3d;
import com.hypixel.hytale.math.vector.Vector3f;
import com.hypixel.hytale.protocol.SoundCategory;
import com.hypixel.hytale.server.core.asset.type.soundevent.config.SoundEvent;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.SoundUtil;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.npc.NPCPlugin;

import deadwolf.MainClass;

public class CameraTracks {
    
    private CameraTracks(){}

    private static final Map<String, CameraScene> cameraScenes = new HashMap<>();
    private static final Map<String, SceneEvent[]> sceneEvents = new HashMap<>();

    static {
        //POSTIONS ARE THE OFFSET FROM THE TRIGGERING ENTITY, NOT WORLD POSITIONS, SO THEY CAN BE REUSED FOR MULTIPLE SCENES
        //IT ALSO ROTATES PROPERLY IF THE TRIGGER BOX IS ROTATED
        
        //Intro track
        cameraScenes.put("intro", new CameraScene(new CameraNode[] {
            new CameraNode(new Vector3d(-2, 2, 0), new Vector3d(Math.toRadians(180), 0, 0), 2),
            new CameraNode(new Vector3d(12.5, 2, -3.5), new Vector3d(Math.toRadians(90), 0, 0), 4),
            new CameraNode(new Vector3d(12.5, 1.5, -8.5), new Vector3d(Math.toRadians(90), 0, 0), 6),
            new CameraNode(new Vector3d(12.5, 1.5, -8.5), new Vector3d(Math.toRadians(90), 0, 0), 7)
        },
        1f,"" // "" is the progression state the player spawns in with
        ));
        sceneEvents.put("intro", new SceneEvent[] {
            new SceneEvent(EventType.SPAWN_ENTITY, "QuestFeran", 0)
            .with("position", new Vector3d(-5, 121, 25)),
            new SceneEvent(EventType.PLAY_SOUND, "SFX_Creative_Play_Seletion_Drag", 4)
            .with("position", new Vector3d(-17, 121, 14)),
        });

        cameraScenes.put("boss", new CameraScene(new CameraNode[] {
            new CameraNode(new Vector3d(1, 2, 0), new Vector3d(Math.toRadians(180), 0, 0), 2),
            new CameraNode(new Vector3d(10, 2, -3.5), new Vector3d(Math.toRadians(90), 0, 0), 4),
            new CameraNode(new Vector3d(10, 2, -3.5), new Vector3d(Math.toRadians(90), 0, 0), 6)
        },
        1f, "defeat_the_boss"
        ));
        sceneEvents.put("boss", new SceneEvent[] {
            new SceneEvent(EventType.SPAWN_ENTITY, "Spawn_Void", 1)
            .with("position", new Vector3d(505, 129, 513)),
        });
    }

    public static CameraScene getCameraScene(String trackID) {
        return cameraScenes.get(trackID);
    }

    public static SceneEvent[] getSceneEvents(String trackID) {
        return sceneEvents.get(trackID);
    }

    public static String getQuestConditionForTrack(String trackID){
        CameraScene scene = cameraScenes.get(trackID);
        if (scene != null){
            return scene.questCondition;
        }
        return null;
    }

    public static class CameraScene{
        public final CameraNode[] nodes;
        public final float speed;
        public final String questCondition;
        public CameraScene(CameraNode[] nodes, float speed, String questCondition){
            this.nodes = nodes;
            this.speed = speed;
            this.questCondition = questCondition;
        }
    }

    public static class CameraNode {
        private final Vector3d position;
        private final Vector3d rotation;
        private final float time;

        public CameraNode(Vector3d position, Vector3d rotation, float time) {
            this.position = position;
            this.rotation = rotation;
            this.time = time;
        }

        public Vector3d getPosition() {
            return position;
        }

        public Vector3d getRotation() {
            return rotation;
        }

        public float getTime() {
            return time;
        }

        public String toString() {
            return "CameraNode{position=" + position + ", rotation=" + rotation + ", time=" + time + "}";
        }
    }

    public static class SceneEvent{
        private final EventType type;
        private final String name;
        private final float time;
        private final Map<String, Object> data;

        public SceneEvent(EventType type, String name, float time){
            this.type = type;
            this.name = name;
            this.time = time;
            this.data = new HashMap<>();
        }

        public EventType getType() {
            return type;
        }

        public String getName() {
            return name;
        }

        public float getTime() {
            return time;
        }

        public SceneEvent with(String key, Object value) {
            data.put(key, value);
            return this;
        }

        @SuppressWarnings("unchecked")
        public <T> T getData(String key, Class<T> type) {
            Object value = data.get(key);
            if (value == null) {
                MainClass.LOGGER.atWarning().log("No data found for key: " + key);
                return null;
            } else if (!type.isInstance(value)) {
                MainClass.LOGGER.atWarning().log("Data for key: " + key + " is not of type: " + type.getName());
                return null;
            }
            return (T)value;
        }
    }

    public enum EventType {
        BEACON,
        PLAY_SOUND,
        SPAWN_ENTITY,
        
    }

    public static boolean triggerEvent(SceneEvent event, World world, Store store, PlayerRef playerRef){
        switch (event.getType()) {
            case BEACON:
                //Wanted to trigger NPC beacons from here based on position and range, but it looks like I can't without some weird workaround?
                //So for now, this does not work
                MainClass.LOGGER.atInfo().log("------ Beacon event not implemented");
                return false;
            case PLAY_SOUND:
                world.execute(() -> {
                    int index = SoundEvent.getAssetMap().getIndex(event.getName());
                    //Might need to swap the SoundCategory to something in the event data if we want different types of sounds?
                    SoundUtil.playSoundEvent2dToPlayer(playerRef, index, SoundCategory.UI, 1.0f, 12.0f);
                });
                return true;
            case SPAWN_ENTITY:
                world.execute (() -> {
                    NPCPlugin.get().spawnNPC(store,event.getName(),null,event.getData("position", Vector3d.class), new Vector3f(0, 0, 0));
                });
                return true;
            default:
                MainClass.LOGGER.atWarning().log("Unhandled event type: " + event.getType());
        }
        return false;
    }
}
