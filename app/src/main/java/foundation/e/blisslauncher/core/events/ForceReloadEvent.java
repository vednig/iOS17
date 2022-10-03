package foundation.e.blisslauncher.core.events;

public class ForceReloadEvent extends Event {
    public static final int TYPE = 801;

    public ForceReloadEvent() {
        super(TYPE);
    }
}
