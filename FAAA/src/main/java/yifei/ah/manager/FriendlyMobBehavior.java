package yifei.ah.manager;

import net.minecraft.text.Text;

public enum FriendlyMobBehavior {
    IDLE("ah.behavior.idle"),
    FOLLOW("ah.behavior.follow"),
    PATROL("ah.behavior.patrol");

    private final String translationKey;

    FriendlyMobBehavior(String translationKey) {
        this.translationKey = translationKey;
    }

    public Text getDisplayName() {
        return Text.translatable(translationKey);
    }

    public String getTranslationKey() {
        return translationKey;
    }

    public FriendlyMobBehavior next() {
        return values()[(ordinal() + 1) % values().length];
    }
}