package eu.codedsakura.fabrichomes.components;

import eu.codedsakura.mods.TextUtils;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.MutableText;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.Vec3d;

import static eu.codedsakura.mods.TextUtils.valueRepr;

public class HomeComponent implements INamedDirectionalPointComponent {
    private double x, y, z;
    private float pitch, yaw;
    private String name;
    private Identifier dim;

    public HomeComponent(double x, double y, double z, float pitch, float yaw, Identifier dim, String name) {
        this.x = x;
        this.y = y;
        this.z = z;
        this.pitch = pitch;
        this.yaw = yaw;
        this.name = name;
        this.dim = dim;
    }

    public HomeComponent(Vec3d pos, float pitch, float yaw, Identifier dim, String name) {
        this.x = pos.x;
        this.y = pos.y;
        this.z = pos.z;
        this.pitch = pitch;
        this.yaw = yaw;
        this.name = name;
        this.dim = dim;
    }

    @Override
    public void readFromNbt(CompoundTag tag) {
        x = tag.getDouble("x");
        y = tag.getDouble("y");
        z = tag.getDouble("z");
        pitch = tag.getFloat("pitch");
        yaw = tag.getFloat("yaw");
        name = tag.getString("name");
        dim = Identifier.tryParse(tag.getString("dim"));
    }

    @Override
    public void writeToNbt(CompoundTag tag) {
        tag.putDouble("x", x);
        tag.putDouble("y", y);
        tag.putDouble("z", z);
        tag.putFloat("pitch", pitch);
        tag.putFloat("yaw", yaw);
        tag.putString("name", name);
        tag.putString("dim", dim.toString());
    }

    @Override public double getX()  { return x; }
    @Override public double getY()  { return y; }
    @Override public double geyZ()  { return z; }
    @Override public float getPitch()  { return pitch; }
    @Override public float getYaw()    { return yaw;   }
    @Override public String getName()   { return name;  }
    @Override public Vec3d getCoords()  { return new Vec3d(x, y, z); }
    @Override public Identifier getDimID() { return dim; }

    @Override
    public MutableText toText(MinecraftServer server) {
        return new TranslatableText("%s\n%s; %s; %s\n%s; %s\n%s",
                valueRepr("Name", name),
                valueRepr("X", x), valueRepr("Y", y), valueRepr("Z", z),
                valueRepr("Yaw", yaw), valueRepr("Pitch", pitch),
                valueRepr("In", dim.toString()));
    }
}
