package deadwolf.Questing;

import javax.annotation.Nullable;

import com.hypixel.hytale.server.npc.asset.builder.BuilderDescriptorState;
import com.hypixel.hytale.server.npc.asset.builder.BuilderSupport;
import com.hypixel.hytale.server.npc.corecomponents.builders.BuilderActionBase;
import com.hypixel.hytale.server.npc.instructions.Action;

public class BuilderActionQuestNPCInteract extends BuilderActionBase {

    @Override
    @Nullable
    public Action build(BuilderSupport support) {
        //Call the Action constructor here with parameters
        return new ActionQuestNPC(this, support);
    }

    @Override
    @Nullable
    public BuilderDescriptorState getBuilderDescriptorState() {
        return BuilderDescriptorState.Stable;
    }

    @Override
    @Nullable
    public String getLongDescription() {
        return this.getShortDescription();
    }

    @Override
    @Nullable
    public String getShortDescription() {
        return "Triggers a quest NPC action";
    }

    
    
}
