package kevkevin.wsdt.tagueberstehen.classes;


import java.util.Random;

/** With this factory universal random interface, and only one object is created once :) */
public class RandomFactory {
    private static final Random random = new Random();

    public static int getRandNo_int(int min, int max) {
        return random.nextInt(max-min +1)+min; //r.nextInt(max - min + 1) + min;
    }
}
