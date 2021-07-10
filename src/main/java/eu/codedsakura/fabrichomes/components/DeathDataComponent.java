package eu.codedsakura.fabrichomes.components;

import net.minecraft.nbt.NbtCompound;
import net.minecraft.util.Identifier;


public class DeathDataComponent implements IDeathDataComponent {
    private DeathComponent death = new DeathComponent(0,0,0,0,0, new Identifier("minecraft:none"),"none");

    @Override
    public void readFromNbt(NbtCompound tag) {
        NbtCompound deathTag = tag.getCompound("death");
        death = DeathComponent.readFromNbt(deathTag);
    }

    @Override
    public void writeToNbt(NbtCompound tag) {
        NbtCompound deathTag = new NbtCompound();
        death.writeToNbt(deathTag);
        tag.put("death", deathTag);
    }

    @Override public DeathComponent getDeath() { return death; }

    @Override
    public void setDeath(DeathComponent deathComponent) {
        death = deathComponent;
    }
}
