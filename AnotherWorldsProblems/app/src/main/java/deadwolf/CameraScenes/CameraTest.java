package deadwolf.CameraScenes;

import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.math.vector.Vector3d;
import com.hypixel.hytale.protocol.ApplyLookType;
import com.hypixel.hytale.protocol.PositionType;
import com.hypixel.hytale.server.core.modules.entity.component.TransformComponent;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;

import deadwolf.MainClass;

import com.hypixel.hytale.protocol.ClientCameraView;
import com.hypixel.hytale.protocol.Direction;
import com.hypixel.hytale.protocol.Position;
import com.hypixel.hytale.protocol.RotationType;
import com.hypixel.hytale.protocol.ServerCameraSettings;
import com.hypixel.hytale.protocol.Vector2f;
import com.hypixel.hytale.protocol.Vector3f;
import com.hypixel.hytale.protocol.packets.camera.SetServerCamera;
// Probably can be deleted completely now
public class CameraTest {
//TODO remove the packet sending logic from this class, only have it in CameraTick
    private void setScene(Ref<EntityStore> playerEntity, World world, String trackID){
        world.execute(() -> {
            TransformComponent playerTransform = playerEntity.getStore().getComponent(playerEntity, TransformComponent.getComponentType());

            ServerCameraSettings cameraSettings = new ServerCameraSettings();
            cameraSettings.isFirstPerson = false;
            cameraSettings.positionType = PositionType.Custom;
            cameraSettings.skipCharacterPhysics = true;
            // Force the camera's rotation to be set by the server.
            cameraSettings.applyLookType = ApplyLookType.Rotation;
            // Notify that we provide a custom rotation in "cameraSettings.rotation"
            cameraSettings.rotationType = RotationType.Custom;
            //Freeze the camera's position and rotation, and disable mouse input.
            cameraSettings.sendMouseMotion = false;
            cameraSettings.allowPitchControls = false;
            cameraSettings.movementMultiplier = new Vector3f(0.0f, 0.0f, 0.0f);
            cameraSettings.lookMultiplier = new Vector2f(0.0f, 0.0f);
            //Direction is in radians
            cameraSettings.rotation = new Direction(0.0f, -1.5707964f, 0.0f);
            cameraSettings.displayCursor = false;
            
            Vector3d cameraPosition = playerTransform.getPosition().add(0, 10, 0);
            cameraSettings.position = new Position(cameraPosition.x, cameraPosition.y, cameraPosition.z);
            
            Store<EntityStore> store = playerEntity.getStore();
            PlayerRef playerRef = store.getComponent(playerEntity, PlayerRef.getComponentType());

            if(store.getComponent(playerEntity, MainClass.instance.getSceneDataComponentType()) != null)
            {
                // If component exists, update it (shouldn't happen, but)
                var comp = store.getComponent(playerEntity, MainClass.instance.getSceneDataComponentType());

                comp.setTrackID(trackID);
            } else {
                // If component doesn't exist, create it and put it in the store
                var sceneComponent = new CameraSceneComponent();

                sceneComponent.setTrackID(trackID);

                store.putComponent(playerEntity, MainClass.instance.getSceneDataComponentType(), sceneComponent);
            }

            playerRef.getPacketHandler().writeNoCache(
                new SetServerCamera(ClientCameraView.Custom, true, cameraSettings)
            );
        });
    }

    private void stopScene(Ref<EntityStore> playerEntity, World world){
        world.execute(() -> {
            Store<EntityStore> store = playerEntity.getStore();
            PlayerRef playerRef = store.getComponent(playerEntity, PlayerRef.getComponentType());
            
            store.tryRemoveComponent(playerEntity, MainClass.instance.getSceneDataComponentType());

            playerRef.getPacketHandler().writeNoCache(
                new SetServerCamera(ClientCameraView.Custom, false, null)
            );
        });
    }
}
