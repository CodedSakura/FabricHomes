package eu.codedsakura.fabrichomes.components;

import dev.onyxstudios.cca.api.v3.component.ComponentV3;

public interface IDeathDataComponent extends ComponentV3 {
    DeathComponent getDeath();
    void setDeath(DeathComponent deathComponent);
}
