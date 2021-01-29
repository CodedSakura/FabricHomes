package eu.codedsakura.fabrichomes.components;

import dev.onyxstudios.cca.api.v3.component.ComponentV3;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.MutableText;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.Vec3d;

public interface INamedDirectionalPointComponent extends ComponentV3 {
    double getX();
    double getY();
    double geyZ();
    float getPitch();
    float getYaw();
    String getName();
    Vec3d getCoords();
    Identifier getDimID();
    MutableText toText(MinecraftServer server);
}
