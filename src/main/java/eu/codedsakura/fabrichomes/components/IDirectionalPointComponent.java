package eu.codedsakura.fabrichomes.components;

import net.minecraft.server.MinecraftServer;
import net.minecraft.text.MutableText;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.Vec3d;

public interface IDirectionalPointComponent {
    double getX();
    double getY();
    double getZ();
    float getPitch();
    float getYaw();
    Vec3d getCoords();
    Identifier getDimID();
    MutableText toText(MinecraftServer server);
}
