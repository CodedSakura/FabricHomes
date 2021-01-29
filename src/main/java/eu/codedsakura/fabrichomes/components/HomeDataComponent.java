package eu.codedsakura.fabrichomes.components;

import net.fabricmc.fabric.api.util.NbtType;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;

import java.util.ArrayList;
import java.util.List;

public class HomeDataComponent implements IHomeDataComponent {
    private final List<HomeComponent> homes = new ArrayList<>();
    private int maxHomes;

    @Override
    public void readFromNbt(CompoundTag tag) {
        homes.clear();
        tag.getList("homes", NbtType.COMPOUND).forEach(v -> homes.add((HomeComponent) v));
        maxHomes = tag.getInt("maxHomes");
    }

    @Override
    public void writeToNbt(CompoundTag tag) {
        ListTag homeTag = new ListTag();
        homes.forEach(v -> {
            CompoundTag ct = new CompoundTag();
            v.writeToNbt(ct);
            homeTag.add(ct);
        });
        tag.put("homes", homeTag);
        tag.putInt("maxHomes", maxHomes);
    }

    @Override public List<HomeComponent> getHomes() { return homes; }
    @Override public int getMaxHomes() { return maxHomes; }

    @Override
    public boolean addHome(HomeComponent home) {
        if (homes.stream().anyMatch(v -> v.getName().equalsIgnoreCase(home.getName()))) return false;
        return homes.add(home);
    }

    @Override
    public boolean removeHome(String name) {
        if (homes.stream().noneMatch(v -> v.getName().equalsIgnoreCase(name))) return false;
        return homes.removeIf(v -> v.getName().equalsIgnoreCase(name));
    }
}
