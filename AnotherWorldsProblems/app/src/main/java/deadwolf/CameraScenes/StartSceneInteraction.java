package deadwolf.CameraScenes;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.math.util.ChunkUtil;
import com.hypixel.hytale.math.vector.Vector3i;
import com.hypixel.hytale.protocol.InteractionType;
import com.hypixel.hytale.server.core.entity.InteractionContext;
import com.hypixel.hytale.server.core.inventory.ItemStack;
import com.hypixel.hytale.server.core.modules.interaction.interaction.CooldownHandler;
import com.hypixel.hytale.server.core.modules.interaction.interaction.config.client.SimpleBlockInteraction;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.chunk.BlockComponentChunk;
import com.hypixel.hytale.server.core.universe.world.storage.ChunkStore;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;

import deadwolf.MainClass;
import deadwolf.Questing.ProgressionComponent;

public class StartSceneInteraction extends SimpleBlockInteraction {

    public static final BuilderCodec<StartSceneInteraction> CODEC = BuilderCodec.builder(
        StartSceneInteraction.class, StartSceneInteraction::new, SimpleBlockInteraction.CODEC
    ).build();

    @Override
    protected void interactWithBlock(@Nonnull World world, @Nonnull CommandBuffer<EntityStore> cmdBuffer,
            @Nonnull InteractionType intType, @Nonnull InteractionContext intCtx, @Nullable ItemStack itmStack,
            @Nonnull Vector3i targetBlock, @Nonnull CooldownHandler cooldownHandler) {

        Ref<EntityStore> playerRef = intCtx.getEntity();
        
        ChunkStore chunkStore = world.getChunkStore();

        long chunkIndex = ChunkUtil.indexChunkFromBlock(targetBlock.x, targetBlock.z);
        BlockComponentChunk blockComponentChunk = chunkStore.getChunkComponent(chunkIndex, BlockComponentChunk.getComponentType());
        int blockIndex = ChunkUtil.indexBlockInColumn(targetBlock.x, targetBlock.y, targetBlock.z);
        Ref<ChunkStore> blockRef = blockComponentChunk.getEntityReference(blockIndex);

        MainClass.LOGGER.atInfo().log("Trying to get TriggerData for block at " + targetBlock.toString());
        TriggerDataComponent triggerData = chunkStore.getStore().getComponent(blockRef, MainClass.instance.getTriggerDataComponentType());

        if (triggerData == null) {
            return;
        }
        
        //Check if the player meets the quest progression requirement for the track, if not, do nothing
        String questCondition = CameraTracks.getQuestConditionForTrack(triggerData.getTrackID());
        if (questCondition == null) {
            return;
        }
        ProgressionComponent progressionComponent = playerRef.getStore().getComponent(playerRef, MainClass.instance.getProgressionComponentType());
        if (progressionComponent == null || !progressionComponent.getProgressionID().equals(questCondition)) {
            return;
        }


        MainClass.LOGGER.atInfo().log("Trying to update/put CameraScene on player...");
        //Add the component to the player.
        if(cmdBuffer.getComponent(playerRef, MainClass.instance.getSceneDataComponentType()) != null)
            {
                // If component exists, update it (shouldn't happen, but)
                var comp = cmdBuffer.getComponent(playerRef, MainClass.instance.getSceneDataComponentType());

                comp.setTrackID(triggerData.getTrackID());

                int[] blockPos = {targetBlock.x, targetBlock.y, targetBlock.z};
                comp.setOriginPosition(blockPos);
                comp.setOriginRotation(world.getBlockRotationIndex(targetBlock.x, targetBlock.y, targetBlock.z));
            } else {
                // If component doesn't exist, create it and put it in the store
                var sceneComponent = new CameraSceneComponent();

                sceneComponent.setTrackID(triggerData.getTrackID());
                int[] blockPos = {targetBlock.x, targetBlock.y, targetBlock.z};
                sceneComponent.setOriginPosition(blockPos);
                sceneComponent.setOriginRotation(world.getBlockRotationIndex(targetBlock.x, targetBlock.y, targetBlock.z));

                cmdBuffer.putComponent(playerRef, MainClass.instance.getSceneDataComponentType(), sceneComponent);
            }
    }

    @Override
    protected void simulateInteractWithBlock(@Nonnull InteractionType arg0, @Nonnull InteractionContext arg1,
            @Nullable ItemStack arg2, @Nonnull World arg3, @Nonnull Vector3i arg4) {
        // Unneeded to simulate interaction for a cutscene trigger
        throw new UnsupportedOperationException("Unimplemented method 'simulateInteractWithBlock'");
    }
    
}
