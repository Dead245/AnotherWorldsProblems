package deadwolf.CameraScenes;

import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.component.Component;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;

public class CameraSceneComponent implements Component<EntityStore> {
    private String trackID;
    private int currentNodeIndex = 0;
    private float progress = 0;
    private int[] originPosition;
    private int originRotation;
    
    //REMEMBER: IDs must start with a capital letter (eg. "TrackID"), otherwise the Codec won't work and the component won't be saved/loaded properly!
    public static final BuilderCodec<CameraSceneComponent> CODEC = BuilderCodec.builder(CameraSceneComponent.class, CameraSceneComponent::new)
    .append(new KeyedCodec<>("TrackID", Codec.STRING),
        (data, value) -> data.trackID = value,
        data -> data.trackID).add()
    .append(new KeyedCodec<>("CurrentNodeIndex", Codec.INTEGER),
        (data, value) -> data.currentNodeIndex = value,
        data -> data.currentNodeIndex).add()
    .append(new KeyedCodec<>("Progress", Codec.FLOAT),
        (data, value) -> data.progress = value,
        data -> data.progress).add()
    .append(new KeyedCodec<>("OriginPosition", Codec.INT_ARRAY),
        (data, value) -> data.originPosition = value,
        data -> data.originPosition).add()
    .append(new KeyedCodec<>("OriginRotation", Codec.INTEGER),
        (data, value) -> data.originRotation = value,
        data -> data.originRotation).add()
    .build();

    public CameraSceneComponent() {
        this.trackID = "";
        this.currentNodeIndex = -1;
        this.progress = 0;
        this.originPosition = new int[3];
        this.originRotation = 0;
    }

    //For clone function
    public CameraSceneComponent(CameraSceneComponent clone) {
        this.trackID = clone.trackID;
        this.currentNodeIndex = clone.currentNodeIndex;
        this.progress = clone.progress;
        this.originPosition = clone.originPosition;
        this.originRotation = clone.originRotation;
    }

    public String getTrackID() {
        return trackID;
    }

    public void setTrackID(String trackID) {
        this.trackID = trackID;
    }

    public int getCurrentNodeIndex() {
        return currentNodeIndex;
    }

    public void setCurrentNodeIndex(int currentNodeIndex) {
        this.currentNodeIndex = currentNodeIndex;
    }

    public float getProgress() {
        return progress;
    }

    public void setProgress(float progress) {
        this.progress = progress;
    }

    public int[] getOriginPostion() {
        return originPosition;
    }

    public void setOriginPosition(int[] originPostion) {
        this.originPosition = originPostion;
    }

    public int getOriginRotation() {
        return originRotation;
    }

    public void setOriginRotation(int originRotation) {
        this.originRotation = originRotation;
    }

    //Required function for Component
    @Override
    public Component<EntityStore> clone() {
        return new CameraSceneComponent(this);
    }
}
