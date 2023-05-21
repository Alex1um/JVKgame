package GameMap.GameObjects;

import Game.Abilities.*;
import GameMap.GameMap;
import org.jetbrains.annotations.Nullable;

import java.awt.*;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Hashtable;

public abstract class GameObject {

    public float getHealth() {
        return health;
    }

    float health;
    public float getMaxHealth() {
        return maxHealth;
    }
    protected void setHealth(float newHealth) {
        health = newHealth;
    }

    public void damage(float dmg) {
        health -= dmg;
    }
    float maxHealth;

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
        return returnType.equals(Void.TYPE);

    }

    protected GameObject(float maxHealth) {
        this.maxHealth = maxHealth;
        this.health = maxHealth;
        for (Method method : this.getClass().getMethods()) {
            if (isMethodCanBeAbility(method)) {
                if (Abilities == null) {
                    Abilities = new Hashtable<>();
                }
                if (method.isAnnotationPresent(TargetAbilityMethod.class)) {
                    String abilityName = method.getAnnotation(TargetAbilityMethod.class).name();
                    method.setAccessible(true);
                    Abilities.put(abilityName,
                            new TargetAbility(abilityName,
                                    (gameMap, actions, target) -> {
                                        try {
                                            method.invoke(this, gameMap, actions, target);
                                        } catch (IllegalAccessException | InvocationTargetException e) {
                                            throw new RuntimeException(e);
                                        }
                                    }
                            )
                    );
                } else if (method.isAnnotationPresent(BasicAbilityMethod.class)) {
                    String abilityName = method.getAnnotation(BasicAbilityMethod.class).name();
                    method.setAccessible(true);
                    Abilities.put(abilityName,
                            new BasicAbility(abilityName,
                                    (gameMap, actions) -> {
                                        try {
                                            method.invoke(this, gameMap, actions);
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
