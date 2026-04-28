package deadwolf.Questing;

import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.component.Component;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;


public class ProgressionComponent implements Component<EntityStore> {
    
    private String progressionID;
    private String questTitle;
    private String questDescription;
    private int killCount;

    //REMEMBER: IDs must start with a capital letter (eg. "ProgressionID"), otherwise the Codec won't work and the component won't be saved/loaded properly!
    public static final BuilderCodec<ProgressionComponent> CODEC = BuilderCodec.builder(ProgressionComponent.class, ProgressionComponent::new)
    .append(new KeyedCodec<>("ProgressionID", Codec.STRING),
        (data, value) -> data.progressionID = value,
        data -> data.progressionID).add()
    .append(new KeyedCodec<>("QuestTitle", Codec.STRING),
        (data, value) -> data.questTitle = value,
        data -> data.questTitle).add()
    .append(new KeyedCodec<>("QuestDescription", Codec.STRING),
        (data, value) -> data.questDescription = value,
        data -> data.questDescription).add()
    .append(new KeyedCodec<>("KillCount", Codec.INTEGER),
        (data, value) -> data.killCount = value,
        data -> data.killCount).add()
    .build();

    public String getProgressionID() {
        return progressionID;
    }

    public void setProgressionID(String progressionID) {
        this.progressionID = progressionID;
    }

    public String getQuestTitle() {
        return questTitle;
    }

    public void setQuestInfo(String questTitle, String questDescription) {
        this.questTitle = questTitle;
        this.questDescription = questDescription;
    }

    public String getQuestDescription() {
        return questDescription;
    }

    public int getKillCount() {
        return killCount;
    }

    public void incrementKillCount() {
        this.killCount++;
    }

    public void setKillCount(int killCount) {
        this.killCount = killCount;
    }

    public ProgressionComponent() {
        //Default constructor
        this.progressionID = "";
    }



    public ProgressionComponent(ProgressionComponent clone) {
        this.progressionID = clone.progressionID;
    }

    //Required function for Component
    @Override
    public Component<EntityStore> clone() {
        return new ProgressionComponent(this);
    }
}
