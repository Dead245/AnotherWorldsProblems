package deadwolf;

import javax.annotation.Nonnull;

import com.hypixel.hytale.Main;
import com.hypixel.hytale.builtin.blockspawner.state.BlockSpawner;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.math.util.ChunkUtil;
import com.hypixel.hytale.math.vector.Vector3i;
import com.hypixel.hytale.protocol.GameMode;
import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.command.system.CommandContext;
import com.hypixel.hytale.server.core.command.system.arguments.system.OptionalArg;
import com.hypixel.hytale.server.core.command.system.arguments.system.RequiredArg;
import com.hypixel.hytale.server.core.command.system.arguments.types.ArgTypes;
import com.hypixel.hytale.server.core.command.system.arguments.types.RelativeIntPosition;
import com.hypixel.hytale.server.core.command.system.basecommands.AbstractWorldCommand;
import com.hypixel.hytale.server.core.command.system.exceptions.GeneralCommandException;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.chunk.WorldChunk;
import com.hypixel.hytale.server.core.universe.world.storage.ChunkStore;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.hypixel.hytale.server.core.util.TargetUtil;

import deadwolf.CameraScenes.TriggerDataComponent;

public class SetTrackCommand extends AbstractWorldCommand {
    // A lot of this was pulled from BlockSpawnerGetCommand and edited
    //Ended up not using it since the prefab didnt save the change
    @Nonnull
    private static final Message MESSAGE_GENERAL_BLOCK_TARGET_NOT_IN_RANGE = Message.translation("server.general.blockTargetNotInRange");
    @Nonnull
    private static final Message MESSAGE_COMMANDS_ERRORS_PROVIDE_POSITION = Message.translation("server.commands.errors.providePosition");
    @Nonnull
    private static final Message MESSAGE_COMMANDS_ERRORS_PLAYER_NOT_IN_WORLD = Message.translation("server.commands.errors.playerNotInWorld");
    @Nonnull
    private final RequiredArg<String> trackIDArg = this.withRequiredArg(
      "trackID", "Track ID of the cutscene", ArgTypes.STRING
    );
    @Nonnull
    private final OptionalArg<RelativeIntPosition> positionArg = this.withOptionalArg(
      "position", "Position of the block", ArgTypes.RELATIVE_BLOCK_POSITION
    );

    public SetTrackCommand() {
        super("track", "A command to set the track of a block with the TriggerData component.");
        this.setPermissionGroup(GameMode.Creative);
    }

    @Override
    protected void execute(@Nonnull CommandContext cmdCtx, @Nonnull World world, @Nonnull Store<EntityStore> store) {
        Vector3i position;
        if (this.positionArg.provided(cmdCtx)) {
            RelativeIntPosition relativePosition = this.positionArg.get(cmdCtx);
            position = relativePosition.getBlockPosition(cmdCtx, store);
        } else {
            if (!cmdCtx.isPlayer()) {
                throw new GeneralCommandException(MESSAGE_COMMANDS_ERRORS_PROVIDE_POSITION);
            }

            Ref<EntityStore> ref = cmdCtx.senderAsPlayerRef();
            if (ref == null || !ref.isValid()) {
                throw new GeneralCommandException(MESSAGE_COMMANDS_ERRORS_PLAYER_NOT_IN_WORLD);
            }

            Vector3i targetBlock = TargetUtil.getTargetBlock(ref, 10.0, store);
            if (targetBlock == null) {
                throw new GeneralCommandException(MESSAGE_GENERAL_BLOCK_TARGET_NOT_IN_RANGE);
            }

            position = targetBlock;
        }

        ChunkStore chunkStore = world.getChunkStore();
        long chunkIndex = ChunkUtil.indexChunkFromBlock(position.x, position.z);
        Ref<ChunkStore> chunkRef = chunkStore.getChunkReference(chunkIndex);
        if (chunkRef != null && chunkRef.isValid()) {
            WorldChunk worldChunkComponent = chunkStore.getStore().getComponent(chunkRef, WorldChunk.getComponentType());

            assert worldChunkComponent != null;

            Ref<ChunkStore> blockRef = worldChunkComponent.getBlockComponentEntity(position.x, position.y, position.z);
            if (blockRef != null && blockRef.isValid()) {
                TriggerDataComponent triggerData = chunkStore.getStore().getComponent(blockRef, MainClass.instance.getTriggerDataComponentType());

                if (triggerData == null) {
                cmdCtx.sendMessage(Message.raw("TriggerData component not found on block at " + position.toString()));
                } else {
                    triggerData.setTrackID(this.trackIDArg.get(cmdCtx));
                    cmdCtx.sendMessage(Message.raw("Set track ID of block at " + position.toString() + " to " + this.trackIDArg.get(cmdCtx)));
                }
            } else {
                cmdCtx.sendMessage(Message.translation("server.general.containerNotFound").param("block", position.toString()));
            }
        } else {
            cmdCtx.sendMessage(Message.translation("server.general.containerNotFound").param("block", position.toString()));
        }
    }
}
