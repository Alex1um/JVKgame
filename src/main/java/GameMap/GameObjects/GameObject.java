package GameMap.GameObjects;

import Game.Abilities.Ability;
import Game.Abilities.AbilityMethod;
import Game.Abilities.TriConsumer;
import Game.Actions.Action;
import GameMap.GameMap;
import VkRender.Config;
import VkRender.GPUObjects.GameMapVertex;
import VkRender.GPUObjects.HealthBarVertex;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector2f;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Hashtable;

public abstract class GameObject {

    float health;

    public float getMaxHealth() {
        return maxHealth;
    }
    protected void setMaxHealth(float newHealth) {
        health = newHealth;
    }
    float maxHealth;
    public GameMapVertex[][] getVertexes() {
        return vertexes;
    }

    GameMapVertex[][] vertexes = new GameMapVertex[2][2];

    public HealthBarVertex[][] getHealthBar() {
        return healthBar;
    }

    HealthBarVertex[][] healthBar = new HealthBarVertex[2][2];

    protected GameMapVertex getVertex(int x, int y) {
        return vertexes[y][x];
    }

    protected void setVertex(int x, int y, GameMapVertex vertex) {
        vertexes[y][x] = vertex;
    }

    public void setHealth(float newHealth) {
        health = newHealth;
        float percent = health / newHealth;
        float splitter = healthBar[0][0].getPos().x() + (healthBar[1][1].getPos().x() - healthBar[0][0].getPos().x()) * percent;
        healthBar[0][0].setHealthSplitX(splitter);
        healthBar[0][1].setHealthSplitX(splitter);
        healthBar[1][0].setHealthSplitX(splitter);
        healthBar[1][1].setHealthSplitX(splitter);
        healthBar[0][0].setHealthPercent(percent);
        healthBar[0][1].setHealthPercent(percent);
        healthBar[1][0].setHealthPercent(percent);
        healthBar[1][1].setHealthPercent(percent);
    }

    protected void updateHealthBarPos() {
        Vector2f tmp = new Vector2f(vertexes[0][0].getPos());
        tmp.y -= Config.healthBarHeight;
        healthBar[0][0].setPos(tmp);
        Vector2f tmp2 = new Vector2f(vertexes[0][1].getPos());
        tmp2.y -= Config.healthBarHeight;
        healthBar[0][1].setPos(tmp2);
        healthBar[1][0].setPos(vertexes[0][0].getPos());
        healthBar[1][1].setPos(vertexes[0][1].getPos());
    }

    @Nullable
    public Hashtable<String, Ability> getAbilities() {
        return Abilities;
    }

    @Nullable
    Hashtable<String, Ability> Abilities = null;

    private static boolean isMethodCanBeAbility(Method method) {
        Class<?>[] parameterTypes = method.getParameterTypes();
        Class<?> returnType = method.getReturnType();

        if (parameterTypes.length < 2) {
            return false;
        }
        if (!returnType.equals(Void.TYPE)) {
            return false;
        }
        return true;

    }

    protected GameObject(float maxHealth) {
        this.maxHealth = maxHealth;
        this.health = maxHealth;
        healthBar[0][0] = new HealthBarVertex(new Vector2f(), this.maxHealth, 1f);
        healthBar[0][1] = new HealthBarVertex(new Vector2f(), this.maxHealth, 1f);
        healthBar[1][0] = new HealthBarVertex(new Vector2f(), this.maxHealth, 1f);
        healthBar[1][1] = new HealthBarVertex(new Vector2f(), this.maxHealth, 1f);
        for (Method method : this.getClass().getMethods()) {
            if (method.isAnnotationPresent(AbilityMethod.class) && isMethodCanBeAbility(method)) {
                if (Abilities == null) {
                    Abilities = new Hashtable<>();
                }
                String abilityName = method.getAnnotation(AbilityMethod.class).name();
                method.setAccessible(true);
                if (method.getParameterTypes().length == 2) {
                    Abilities.put(abilityName,
                            new Ability(abilityName,
                                    (gameMap, actions, args) -> {
                                        try {
                                            method.invoke(this, gameMap, actions);
                                        } catch (IllegalAccessException | InvocationTargetException e) {
                                            throw new RuntimeException(e);
                                        }
                                    }
                            )
                    );
                } else {
                    Abilities.put(abilityName,
                            new Ability(abilityName,
                                    (gameMap, actions, args) -> {
                                        try {
                                            method.invoke(this, gameMap, actions, args);
                                        } catch (IllegalAccessException | InvocationTargetException e) {
                                            throw new RuntimeException(e);
                                        }
                                    }
                            )
                    );
                }
            }
        }
    }
}
