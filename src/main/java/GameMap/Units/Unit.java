package GameMap.Units;

public class Unit {

    public int getHp() {
        return hp;
    }

    public void setHp(int hp) {
        this.hp = hp;
    }

    public void addHp(int dhp) {
        this.hp += hp;
    }

    public int getMaxHp() {
        return maxHp;
    }

    public void setMaxHp(int maxHp) {
        this.maxHp = maxHp;
    }

    public int getTextureId() {
        return textureId;
    }

    public static String getName() {
        return name;
    }

    static String name;
    int hp;
    int maxHp;

    int textureId;


}
