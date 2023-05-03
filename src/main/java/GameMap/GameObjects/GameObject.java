package GameMap.GameObjects;

import Game.Abilities.Ability;
import Game.Abilities.AbilityMethod;
import Game.Actions.Action;
import VkRender.GPUObjects.GameMapVertex;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;

public abstract class GameObject {
    public GameMapVertex[][] getVertexes() {
        return vertexes;
    }

    GameMapVertex[][] vertexes = new GameMapVertex[2][2];

    protected GameMapVertex getVertex(int x, int y) {
        return vertexes[y][x];
    }

    protected void setVertex(int x, int y, GameMapVertex vertex) {
        vertexes[y][x] = vertex;
    }

    @Nullable
    public ArrayList<Ability> getAbilities() {
        return Abilities;
    }

    @Nullable
    ArrayList<Ability> Abilities = null;

    protected GameObject() {
        for (Method method : this.getClass().getDeclaredMethods()) {
            if (method.isAnnotationPresent(AbilityMethod.class)) {
                if (Abilities == null) {
                    Abilities = new ArrayList<Ability>();
                }
                String abilityName = method.getAnnotation(AbilityMethod.class).name();
                method.setAccessible(true);
                Abilities.add(
                        new Ability(
                                abilityName,
                                new Action(
                                        (gameMap, actions) -> {
                                            try {
                                                method.invoke(this, gameMap, actions);
                                            } catch (IllegalAccessException | InvocationTargetException e) {
                                                throw new RuntimeException(e);
                                            }
                                        }
                                )
                        )
                );
            }
        }
    }
}
