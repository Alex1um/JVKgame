package GameMap.GameObjects;

import Game.Abilities.Ability;
import Game.Abilities.AbilityMethod;
import Game.Actions.Action;
import VkRender.Config;
import VkRender.GPUObjects.GameMapVertex;
import VkRender.GPUObjects.HealthBarVertex;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector2f;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;

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
        healthBar[0][0].setHealthPercent(percent);
        healthBar[0][1].setHealthPercent(percent);
        healthBar[1][0].setHealthPercent(percent);
        healthBar[1][1].setHealthPercent(percent);
    }

    protected void setHealthBar() {
        Vector2f tmp = new Vector2f(vertexes[0][0].getPos());
        tmp.y -= Config.healthBarHeight;
        healthBar[0][0].setPos(tmp);
        tmp = new Vector2f(vertexes[1][0].getPos());
        tmp.y -= Config.healthBarHeight;
        healthBar[0][1].setPos(tmp);
        healthBar[1][0].setPos(vertexes[1][0].getPos());
        healthBar[1][1].setPos(vertexes[1][1].getPos());
    }

    @Nullable
    public ArrayList<Ability> getAbilities() {
        return Abilities;
    }

    @Nullable
    ArrayList<Ability> Abilities = null;

    protected GameObject(float maxHealth) {
        this.maxHealth = maxHealth;
        this.health = maxHealth;
        healthBar[0][0] = new HealthBarVertex(new Vector2f(), this.maxHealth);
        healthBar[0][1] = new HealthBarVertex(new Vector2f(), this.maxHealth);
        healthBar[1][0] = new HealthBarVertex(new Vector2f(), this.maxHealth);
        healthBar[1][1] = new HealthBarVertex(new Vector2f(), this.maxHealth);
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
