package eu.codedsakura.fabrichomes.components;

import dev.onyxstudios.cca.api.v3.component.ComponentKey;
import dev.onyxstudios.cca.api.v3.component.ComponentRegistryV3;
import dev.onyxstudios.cca.api.v3.entity.EntityComponentFactoryRegistry;
import dev.onyxstudios.cca.api.v3.entity.EntityComponentInitializer;
import dev.onyxstudios.cca.api.v3.entity.RespawnCopyStrategy;
import net.minecraft.util.Identifier;

public class PlayerComponentInitializer implements EntityComponentInitializer {
    public static final ComponentKey<IHomeDataComponent> HOME_DATA =
        ComponentRegistryV3.INSTANCE.getOrCreate(new Identifier("fabrichomes", "homes"), IHomeDataComponent.class);

    public  static ComponentKey<IDeathDataComponent> DEATH_DATA =
            ComponentRegistryV3.INSTANCE.getOrCreate(new Identifier("fabrichomes", "death"), IDeathDataComponent.class);

    @Override
    public void registerEntityComponentFactories(EntityComponentFactoryRegistry registry) {
        registry.registerForPlayers(HOME_DATA, playerEntity -> new HomeDataComponent(), RespawnCopyStrategy.ALWAYS_COPY.ALWAYS_COPY);
        registry.registerForPlayers(DEATH_DATA, playerEntity -> new DeathDataComponent(), RespawnCopyStrategy.ALWAYS_COPY.ALWAYS_COPY);
    }
}
