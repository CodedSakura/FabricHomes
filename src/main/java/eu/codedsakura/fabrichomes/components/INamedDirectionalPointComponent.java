package eu.codedsakura.fabrichomes.components;

import net.minecraft.server.MinecraftServer;
import net.minecraft.text.MutableText;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.Vec3d;

public interface INamedDirectionalPointComponent extends IDirectionalPointComponent {
    String getName();
}
