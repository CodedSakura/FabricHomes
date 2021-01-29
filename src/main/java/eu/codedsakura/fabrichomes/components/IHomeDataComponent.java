package eu.codedsakura.fabrichomes.components;

import dev.onyxstudios.cca.api.v3.component.ComponentV3;

import java.util.List;

public interface IHomeDataComponent extends ComponentV3 {
    List<HomeComponent> getHomes();
    int getMaxHomes();

    boolean addHome(HomeComponent home);
    boolean removeHome(String name);
}
