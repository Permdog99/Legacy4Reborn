package wily.legacy.client;

import wily.legacy.client.screen.Assort;

import java.util.List;

public interface LegacyClientWorldSettings {
    long getDisplaySeed();
    void setDisplaySeed(long s);
    boolean trustPlayers();
    void setTrustPlayers(boolean trust);
    boolean isDifficultyLocked();
    void setDifficultyLocked(boolean locked);
    void setAllowCommands(boolean allow);
    void setSelectedResourceAssort(Assort assort);
    Assort getSelectedResourceAssort();
}
