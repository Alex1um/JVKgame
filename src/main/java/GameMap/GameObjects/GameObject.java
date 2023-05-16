package GameMap.GameObjects;

import Game.Abilities.Ability;
import Game.Abilities.AbilityMethod;
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
    float maxHealth;

    Point tilePosition;
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
