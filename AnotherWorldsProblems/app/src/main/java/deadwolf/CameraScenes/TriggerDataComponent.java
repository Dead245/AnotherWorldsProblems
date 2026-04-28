package deadwolf.CameraScenes;

import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.component.Component;
import com.hypixel.hytale.server.core.universe.world.storage.ChunkStore;
//Used for Camera system
public class TriggerDataComponent implements Component<ChunkStore>{
    private String trackID;
    
    //REMEMBER: IDs must start with a capital letter (eg. "TrackID"), otherwise the Codec won't work and the component won't be saved/loaded properly!
    public static final BuilderCodec<TriggerDataComponent> CODEC = BuilderCodec.builder(TriggerDataComponent.class, TriggerDataComponent::new)
    .append(new KeyedCodec<>("Track ID", Codec.STRING),
        (data, value) -> data.trackID = value,
        data -> data.trackID).add()
    .build();

    public TriggerDataComponent() {
        this.trackID = null;
    }

    //for clone function
    public TriggerDataComponent(TriggerDataComponent clone) {
        this.trackID = clone.trackID;
    }

    public String getTrackID() {
        return trackID;
    }

    public void setTrackID(String trackID) {
        this.trackID = trackID;
    }

    //Required function for Component
    @Override
    public Component<ChunkStore> clone() {
        return new TriggerDataComponent(this);
    }
}
