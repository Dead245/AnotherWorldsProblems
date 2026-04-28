package deadwolf.Questing;

import javax.annotation.Nonnull;

import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.hypixel.hytale.server.npc.asset.builder.BuilderSupport;
import com.hypixel.hytale.server.npc.corecomponents.ActionBase;
import com.hypixel.hytale.server.npc.corecomponents.builders.BuilderActionBase;
import com.hypixel.hytale.server.npc.role.Role;
import com.hypixel.hytale.server.npc.sensorinfo.InfoProvider;

import deadwolf.MainClass;

public class ActionQuestNPC extends ActionBase{

    public ActionQuestNPC(BuilderActionBase builderActionBase, @Nonnull BuilderSupport support) {
        super(builderActionBase);
        //TODO Auto-generated constructor stub
        
    }
    
    @Override
    public boolean execute(@Nonnull Ref<EntityStore> ref, @Nonnull Role role, InfoProvider sensorInfo, double dt, @Nonnull Store<EntityStore> store) {
        super.execute(ref, role, sensorInfo, dt, store);
        //This is where the actual logic for the Action takes place

        //Doing a bare basic quest system here, is not a proper questing system

        //Get the player's current progression, give them the progression component if they don't already have it
        Ref<EntityStore> playerReference = role.getStateSupport().getInteractionIterationTarget();
        ProgressionComponent progressionComponent = store.ensureAndGetComponent(playerReference, MainClass.instance.getProgressionComponentType());
        PlayerRef playerRef = store.getComponent(playerReference, PlayerRef.getComponentType());
        
        Player playerComponent = store.getComponent(playerReference, Player.getComponentType());

        //Checks what progressionID is, does something based on it, then advances it to the next progression stage
        progressionComponent.setProgressionID(QuestingIndex.handleProgression(progressionComponent, playerComponent, playerRef));

        return true;
    }

}
