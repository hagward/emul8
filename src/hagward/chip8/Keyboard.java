package hagward.chip8;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class Keyboard {

    private static final Map<Character, Integer> keyMap;
    static {
        Map<Character, Integer> m = new HashMap<>();
        m.put('1', 1);
        m.put('2', 2);
        m.put('3', 3);
        m.put('4', 12);

        m.put('q', 4);
        m.put('w', 5);
        m.put('e', 6);
        m.put('r', 13);

        m.put('a', 7);
        m.put('s', 8);
        m.put('d', 9);
        m.put('f', 14);

        m.put('z', 10);
        m.put('x', 0);
        m.put('c', 11);
        m.put('v', 15);
        keyMap = Collections.unmodifiableMap(m);
    }

    private boolean[] keys;

    public Keyboard() {
        keys = new boolean[16];
    }

    public void reset() {
        Arrays.fill(keys, false);
    }

    public boolean isPressed(int key) {
        return keys[key];
    }

    public void setKey(char keyChar, boolean pressed) {
        Integer key = keyMap.get(keyChar);
        if (key != null) {
            keys[key] = pressed;
        }
    }

}
