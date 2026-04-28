package deadwolf.CameraScenes;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;



import com.hypixel.hytale.component.ArchetypeChunk;
import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.component.ComponentType;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.component.query.Query;
import com.hypixel.hytale.component.system.tick.EntityTickingSystem;
import com.hypixel.hytale.math.vector.Vector3d;
import com.hypixel.hytale.math.vector.Vector3i;
import com.hypixel.hytale.protocol.ApplyLookType;
import com.hypixel.hytale.protocol.ClientCameraView;
import com.hypixel.hytale.protocol.Direction;
import com.hypixel.hytale.protocol.Position;
import com.hypixel.hytale.protocol.PositionType;
import com.hypixel.hytale.protocol.RotationType;
import com.hypixel.hytale.protocol.ServerCameraSettings;
import com.hypixel.hytale.protocol.Vector2f;
import com.hypixel.hytale.protocol.Vector3f;
import com.hypixel.hytale.protocol.packets.camera.SetServerCamera;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;

import deadwolf.MainClass;
import deadwolf.CameraScenes.CameraTracks.CameraNode;

//Thank you Hytalemodding.dev for the tutorials on how Components and systems work!
public class CameraTick extends EntityTickingSystem<EntityStore>{

    private final ComponentType<EntityStore, CameraSceneComponent> cameraComponentType;

    public CameraTick(ComponentType<EntityStore, CameraSceneComponent> cameraComponentType) {
        this.cameraComponentType = cameraComponentType;
    }

    @Override
    @Nullable
    public Query<EntityStore> getQuery() {
        //Need to give a query to determine which entities need to have their cutscenes progressed.
        return Query.and(this.cameraComponentType);
    }

    @Override
    public void tick(float deltaTime, int entityIndex, @Nonnull ArchetypeChunk<EntityStore> archetypeChunk, @Nonnull Store<EntityStore> store,
            @Nonnull CommandBuffer<EntityStore> cmdBuffer) {   
        CameraSceneComponent sceneComponent = archetypeChunk.getComponent(entityIndex, cameraComponentType);
        Ref<EntityStore> ref = archetypeChunk.getReferenceTo(entityIndex);
        
        PlayerRef playerRef = archetypeChunk.getComponent(entityIndex, PlayerRef.getComponentType());

        var scene = CameraTracks.getCameraScene(sceneComponent.getTrackID());
        var events = CameraTracks.getSceneEvents(sceneComponent.getTrackID());
  
        if (scene == null) {
            MainClass.LOGGER.atInfo().log("------ Scene '" + sceneComponent.getTrackID() + "'' is null, aborting tick()");
            return;
        }

        float speed = scene.speed;
        int currNode = sceneComponent.getCurrentNodeIndex();
        ServerCameraSettings cameraSettings = new ServerCameraSettings();

        //Start the scene if not already started
        if(currNode == -1 && sceneComponent.getProgress() == 0){
            MainClass.LOGGER.atInfo().log("### Starting new camera scene ###");
            //Z and X are swapped in Posiition vs Vector3d, so swap them when creating the new Position
            Position newNodePos = new Position(scene.nodes[0].getPosition().z, scene.nodes[0].getPosition().y, scene.nodes[0].getPosition().x);
            Direction newNodeRot = new Direction((float) scene.nodes[0].getRotation().x, (float) scene.nodes[0].getRotation().y, (float) scene.nodes[0].getRotation().z);

            int[] originPos = sceneComponent.getOriginPostion();
            
            Vector3i newPos = new Vector3i(originPos[0], originPos[1], originPos[2]);
            updateSettings(cameraSettings, newNodePos, newNodeRot, newPos, sceneComponent.getOriginRotation());
            float nodeStartTime;
            if (currNode == -1){
                nodeStartTime = 0;
            } else {
                nodeStartTime = scene.nodes[currNode].getTime();
            }

            float nodeTargetTime = scene.nodes[currNode + 1].getTime();

            //Calculate lerp speeds
            float timeBetweenNodes = nodeTargetTime - nodeStartTime;
            float actualTime = timeBetweenNodes / speed;

            float ticks = actualTime * 30; 
            float lerpSpeed;
            if (ticks <= 1){
                lerpSpeed = 1;
            } else {
                lerpSpeed = 1 - (float) Math.pow(0.5, 1 / ticks);
            }
            cameraSettings.positionLerpSpeed = lerpSpeed;
            cameraSettings.rotationLerpSpeed = lerpSpeed * 2f;

            MainClass.LOGGER.atInfo().log("###### Sending first camera packet for index 0...");
            playerRef.getPacketHandler().writeNoCache(
                new SetServerCamera(ClientCameraView.Custom, true, cameraSettings)
            );
        }


        if (currNode >= scene.nodes.length - 1){
             MainClass.LOGGER.atInfo().log("------ At end of Scene nodes for '" + sceneComponent.getTrackID() + "', triggering endScene()");
            endScene(cmdBuffer, ref, playerRef);
            return;
        }

        //Update progress
        float oldProgress = sceneComponent.getProgress();
        float progress = sceneComponent.getProgress() + (deltaTime * speed);
        sceneComponent.setProgress(progress);
        //Check if any events should be called
        if (events != null){
            for(var event : events){
                //Potential bug: If progression doesn't progress enough, an event might trigger multiple times.
                if (event.getTime() >= oldProgress && event.getTime() <= progress){
                    MainClass.LOGGER.atInfo().log("------ Triggering event '" + event.getName() + "' for scene '" + sceneComponent.getTrackID() + "'");
                    
                    boolean triggered = CameraTracks.triggerEvent(event, store.getExternalData().getWorld(), store, playerRef);
                    if (!triggered){
                        MainClass.LOGGER.atWarning().log("Event '" + event.getName() + "' failed to trigger!");
                    }
                }
            }
        }
    
        //Check if camera is at end of scene before calculating anything else
        if(sceneComponent.getProgress() >= scene.nodes[scene.nodes.length - 1].getTime()){
            endScene(cmdBuffer, ref, playerRef);
            return;
        }

        float nodeStartTime;
        if (currNode == -1){
            nodeStartTime = 0;
        } else {
            nodeStartTime = scene.nodes[currNode].getTime();
        }

        float nodeTargetTime = scene.nodes[currNode + 1].getTime();

        if (progress < nodeTargetTime){
            return;
        }

        //Calculate lerp speeds
        float timeBetweenNodes = nodeTargetTime - nodeStartTime;
        float actualTime = timeBetweenNodes / speed;

        float ticks = actualTime * 30; 
        float lerpSpeed;
        if (ticks <= 1){
            lerpSpeed = 1;
        } else {
            lerpSpeed = 1 - (float) Math.pow(0.4f, 1 / ticks);
        }
        cameraSettings.positionLerpSpeed = lerpSpeed;
        cameraSettings.rotationLerpSpeed = lerpSpeed * 2f;

        //Advance to the next node
        int newNodeIndex = currNode + 1;
        sceneComponent.setCurrentNodeIndex(newNodeIndex);
        MainClass.LOGGER.atInfo().log("------ Advancing to next node, (Progress: "+progress+")");
        MainClass.LOGGER.atInfo().log("------ Going to node index "+ newNodeIndex + " with a time of " + scene.nodes[newNodeIndex].getTime());
        CameraNode nextNode = scene.nodes[newNodeIndex + 1];
        //Z and X are swapped in Posiition vs Vector3d, so swap them when creating the new Position
        Position newNodePos = new Position(nextNode.getPosition().z, nextNode.getPosition().y, nextNode.getPosition().x);
        //Yaw - Pitch - Roll order for rotations
        Direction newNodeRot = new Direction((float) nextNode.getRotation().x, (float) nextNode.getRotation().y, (float) nextNode.getRotation().z);

        int[] originPos = sceneComponent.getOriginPostion();
        Vector3i newPos = new Vector3i(originPos[0], originPos[1], originPos[2]);
        updateSettings(cameraSettings, newNodePos, newNodeRot, newPos, sceneComponent.getOriginRotation());

        //Update the camera by sending a packet to the player
        playerRef.getPacketHandler().writeNoCache(
            new SetServerCamera(ClientCameraView.Custom, true, cameraSettings)
        );
    }

    private ServerCameraSettings updateSettings(ServerCameraSettings cameraSettings, Position newPos, Direction newDir, Vector3i originPos, int originRot){
        originRot = originRot % 4;

        //Set values that do not change between nodes
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
        cameraSettings.displayCursor = false;

        switch (originRot) {
            case 0:
                break;
            case 1:
                //90
                newPos = new Position(newPos.z,newPos.y,-newPos.x);
                break;
            case 2:
                //180
                newPos = new Position(-newPos.x,newPos.y,-newPos.z);
                break;
            case 3:
                //270
                newPos = new Position(-newPos.z,newPos.y,newPos.x);
                break;

            default:
                //"originRot % 4 is not 0-3" ERROR
                break;
        }

        Vector3i intPos = new Vector3i((int)newPos.x,(int)newPos.y,(int)newPos.z);
        Vector3i tempPos = originPos.add(intPos);
        Vector3d resultPos = new Vector3d(tempPos.x,tempPos.y,tempPos.z);

        cameraSettings.position = new Position(resultPos.x, resultPos.y, resultPos.z);
        float yawOffset = originRot * ((float)Math.PI / 2f);
        cameraSettings.rotation = new Direction(newDir.yaw + yawOffset,newDir.pitch,newDir.roll);

        return cameraSettings;
    }

    private void endScene(CommandBuffer<EntityStore> cmdBuffer, Ref<EntityStore> ref, PlayerRef playerRef){
        if (ref == null){
            MainClass.LOGGER.atSevere().log("'ref' is somehow null in CameraTick System's endScene()");
        } else {
            cmdBuffer.removeComponent(ref, MainClass.instance.getSceneDataComponentType());
        }


        playerRef.getPacketHandler().writeNoCache(
            new SetServerCamera(ClientCameraView.Custom, false, null)
        );

    }
}
