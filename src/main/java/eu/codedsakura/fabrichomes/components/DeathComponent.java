package eu.codedsakura.fabrichomes.components;

import net.minecraft.nbt.NbtCompound;
import net.minecraft.server.MinecraftServer;
import net.minecraft.text.MutableText;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.Vec3d;

import static eu.codedsakura.mods.TextUtils.valueRepr;

public class DeathComponent implements IDirectionalPointComponent {
    private double x, y, z;
    private float pitch, yaw;
    private Identifier dim;
    private String damageSource;

    public DeathComponent(double x, double y, double z, float pitch, float yaw, Identifier dim, String damageSource) {
        this.x = x;
        this.y = y;
        this.z = z;
        this.pitch = pitch;
        this.yaw = yaw;
        this.dim = dim;
        this.damageSource = damageSource;
    }

    public DeathComponent(Vec3d pos, float pitch, float yaw, Identifier dim, String damageSource) {
        this.x = pos.x;
        this.y = pos.y;
        this.z = pos.z;
        this.pitch = pitch;
        this.yaw = yaw;
        this.dim = dim;
        this.damageSource = damageSource;
    }

    public static DeathComponent readFromNbt(NbtCompound tag) {
        return new DeathComponent(
            tag.getDouble("x"),
            tag.getDouble("y"),
            tag.getDouble("z"),
            tag.getFloat("pitch"),
            tag.getFloat("yaw"),
            Identifier.tryParse(tag.getString("dim")),
            tag.getString("source")
        );
    }

    public void writeToNbt(NbtCompound tag) {
        tag.putDouble("x", x);
        tag.putDouble("y", y);
        tag.putDouble("z", z);
        tag.putFloat("pitch", pitch);
        tag.putFloat("yaw", yaw);
        tag.putString("dim", dim.toString());
        tag.putString("source", damageSource);
    }

    @Override public double getX()  { return x; }
    @Override public double getY()  { return y; }
    @Override public double getZ()  { return z; }
    @Override public float getPitch()  { return pitch; }
    @Override public float getYaw()    { return yaw;   }
    @Override public Vec3d getCoords()  { return new Vec3d(x, y, z); }
    @Override public Identifier getDimID() { return dim; }

    @Override
    public MutableText toText(MinecraftServer server) {
        return new TranslatableText("%s; %s; %s\n%s; %s\n%s\n%s",
                valueRepr("X", x), valueRepr("Y", y), valueRepr("Z", z),
                valueRepr("Yaw", yaw), valueRepr("Pitch", pitch),
                valueRepr("In", dim.toString()),
                valueRepr("Cause", damageSource));
    }
}
