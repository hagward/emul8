package hagward.chip8;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileInputStream;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class Chip8 {
    private static final int[] FONT_SET = {
            0xF0, 0x90, 0x90, 0x90, 0xF0, // 0
            0x20, 0x60, 0x20, 0x20, 0x70, // 1
            0xF0, 0x10, 0xF0, 0x80, 0xF0, // 2
            0xF0, 0x10, 0xF0, 0x10, 0xF0, // 3
            0x90, 0x90, 0xF0, 0x10, 0x10, // 4
            0xF0, 0x80, 0xF0, 0x10, 0xF0, // 5
            0xF0, 0x80, 0xF0, 0x90, 0xF0, // 6
            0xF0, 0x10, 0x20, 0x40, 0x40, // 7
            0xF0, 0x90, 0xF0, 0x90, 0xF0, // 8
            0xF0, 0x90, 0xF0, 0x10, 0xF0, // 9
            0xF0, 0x90, 0xF0, 0x90, 0x90, // A
            0xE0, 0x90, 0xE0, 0x90, 0xE0, // B
            0xF0, 0x80, 0x80, 0x80, 0xF0, // C
            0xE0, 0x90, 0x90, 0x90, 0xE0, // D
            0xF0, 0x80, 0xF0, 0x80, 0xF0, // E
            0xF0, 0x80, 0xF0, 0x80, 0x80  // F
    };

    public static final int SCREEN_WIDTH = 64;
    public static final int SCREEN_HEIGHT = 32;
    public static final int SCREEN_MODIFIER = 20;

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

    private int opCode;
    private int index;
    private int pc;
    private int sp;
    private int delayTimer;
    private int soundTimer;
    private int[] mem;
    private int[] reg;
    private int[][] gfx;
    private int[] stack;
    private int[] key;
    private boolean gfxUpdated;

    public Chip8() {
        mem = new int[4096];
        reg = new int[16];
        gfx = new int[SCREEN_HEIGHT][SCREEN_WIDTH];
        stack = new int[16];
        key = new int[16];
        reset();
    }

    public void reset() {
        pc = 0x200;
        sp = 0;
        opCode = 0;
        index = 0;
        delayTimer = 0;
        soundTimer = 0;
        gfxUpdated = true;
        Arrays.fill(mem, 0);
        Arrays.fill(reg, 0);
        Arrays.fill(stack, 0);
        Arrays.fill(key, 0);
        for (int[] row : gfx) {
            Arrays.fill(row, 0);
        }

        // Load font set.
        System.arraycopy(FONT_SET, 0, mem, 0, 80);
    }

    public int loadRom(File file) {
        try {
            FileInputStream in = new FileInputStream(file);
            int nextByte;
            int i = 0x200;
            while ((nextByte = in.read()) != -1) {
                mem[i++] = nextByte;
            }
            in.close();
            return i - 0x200;
        } catch (Exception e) {
            System.out.println("Error loading rom: " + e.getMessage());
            return -1;
        }
    }

    public void drawToImage(BufferedImage image) {
        Graphics2D g = image.createGraphics();
        g.setColor(Color.darkGray);
        g.fillRect(0, 0, SCREEN_WIDTH * SCREEN_MODIFIER, SCREEN_HEIGHT * SCREEN_MODIFIER);

        for (int y = 0; y < SCREEN_HEIGHT; y++) {
            for (int x = 0; x < SCREEN_WIDTH; x++) {
                if (gfx[y][x] != 0) {
                    g.setColor(Color.green);
                    g.fillRect(x * SCREEN_MODIFIER, y * SCREEN_MODIFIER, SCREEN_MODIFIER, SCREEN_MODIFIER);
                }
            }
        }

        g.dispose();
        gfxUpdated = false;
    }

    public void setKey(char keyChar, boolean pressed) {
        Integer keyIndex = keyMap.get(keyChar);
        if (keyIndex != null) {
            key[keyIndex] = (pressed) ? 1 : 0;
        }
    }

    public boolean isGfxUpdated() {
        return gfxUpdated;
    }

    public void emulateCycle() {
        // Fetch.
        opCode = (mem[pc] << 8) | mem[pc + 1];

        int x = (opCode & 0x0F00) >> 8;
        int y = (opCode & 0x00F0) >> 4;

        // Decode.
        switch (opCode & 0xF000) {
            case 0x0000:
                switch (opCode & 0x00FF) {
                    // 00E0: Clears the screen.
                    case 0x00E0:
                        for (int[] row : gfx) {
                            Arrays.fill(row, 0);
                        }
                        gfxUpdated = true;
                        pc += 2;
                        break;

                    // 00EE: Returns from subroutine.
                    case 0x00EE:
                        sp--;
                        pc = stack[sp];
                        pc += 2;
                        break;

                    default:
                        System.out.printf("Unknown opcode [0x0000]: 0x%x%n", opCode);
                        pc += 2;
                }
                break;

            // 1NNN: Jumps to address NNN.
            case 0x1000:
                pc = opCode & 0x0FFF;
                break;

            // 2NNN: Calls subroutine at address NNN.
            case 0x2000:
                stack[sp] = pc;
                sp++;
                pc = opCode & 0x0FFF;
                break;

            // 3XNN: Skips the next instruction if VX equals NN.
            case 0x3000:
                pc += (reg[x] == (opCode & 0x00FF)) ? 4 : 2;
                break;

            // 4XNN: Skips the next instruction if VX doesn't equal NN.
            case 0x4000:
                pc += (reg[x] != (opCode & 0x00FF)) ? 4 : 2;
                break;

            // 5XY0: Skips the next instruction if VX equals VY.
            case 0x5000:
                pc += (reg[x] == reg[y]) ? 4 : 2;
                break;

            // 6XNN: Sets VX to NN.
            case 0x6000:
                reg[x] = opCode & 0x00FF;
                pc += 2;
                break;

            // 7XNN: Adds NN to VX.
            case 0x7000:
                reg[x] = (reg[x] + (opCode & 0x00FF)) % (1 << 8);
                pc += 2;
                break;

            case 0x8000:
                switch (opCode & 0x000F) {
                    // 8XY0: Sets VX to the value of VY.
                    case 0x0000:
                        reg[x] = reg[y];
                        pc += 2;
                        break;

                    // 8XY1: Sets VX to VX OR VY.
                    case 0x0001:
                        reg[x] |= reg[y];
                        pc += 2;
                        break;

                    // 8XY2: Sets VX to VX AND VY.
                    case 0x0002:
                        reg[x] &= reg[y];
                        pc += 2;
                        break;

                    // 8XY3: Sets VX to VX XOR VY.
                    case 0x0003:
                        reg[x] ^= reg[y];
                        pc += 2;
                        break;

                    // 8XY4: Adds VY to VX. VF is set to 1 when there's a carry, and to 0 when there isn't.
                    case 0x0004:
                        reg[0xF] = (reg[y] > (0xFF - reg[x])) ? 1 : 0;
                        reg[x] = (reg[x] + reg[y]) % (1 << 8);
                        pc += 2;
                        break;

                    // 8XY5: VY is subtracted from VX. VF is set to 0 when there's a borrow, and 1 when there isn't.
                    case 0x0005:
                        reg[0xF] = (reg[y] > reg[x]) ? 0 : 1;
                        reg[x] = (reg[x] - reg[y]) % (1 << 8);
                        if (reg[x] < 0) {
                            reg[x] += (1 << 8);
                        }
                        pc += 2;
                        break;

                    // 8XY6: Shifts VX right by one. VF is set to the value of the least significant bit of VX before
                    // the shift.
                    case 0x0006:
                        reg[0xF] = reg[x] & 1;
                        reg[x] >>= 1;
                        pc += 2;
                        break;

                    // 8XY7: Sets VX to VY minus VX. VF is set to 0 when there's a borrow, and 1 when there isn't.
                    case 0x0007:
                        reg[0xF] = (reg[x] > reg[y]) ? 0 : 1;
                        reg[x] = (reg[y] - reg[x]) % (1 << 8);
                        if (reg[x] < 0) {
                            reg[x] += (1 << 8);
                        }
                        pc += 2;
                        break;

                    // 8XYE: Shifts VX left by one. VF is set to the value of the most significant bit of VX before the
                    // shift
                    case 0x000E:
                        reg[0xF] = reg[x] >> 7;
                        reg[x] <<= 1;
                        pc += 2;
                        break;

                    default:
                        System.out.printf("Unknown opcode [0x8000]: 0x%x%n", opCode);
                }
                break;

            // 9XY0: Skips the next instruction if VX doesn't equal VY.
            case 0x9000:
                pc += (reg[x] != reg[y]) ? 4 : 2;
                break;

            // ANNN: Sets the index register to address NNN.
            case 0xA000:
                index = opCode & 0x0FFF;
                pc += 2;
                break;

            // BNNN: Jumps to the address NNN plus V0.
            case 0xB000:
                pc = (opCode & 0x0FFF) + reg[0];
                break;

            // CXNN: Sets VX to a random number and NN.
            case 0xC000:
                reg[x] = ((int) (Math.random() * 0xFF)) & (opCode & 0x00FF);
                pc += 2;
                break;

            // DXYN: Sprites stored in memory at location in index register (I), maximum 8 bits wide. Wraps around the
            // screen. If when drawn, clears a pixel, register VF is set to 1 otherwise it is zero. All drawing is XOR
            // drawing (i.e. it toggles the screen pixels).
            case 0xD000: {
                int height = opCode & 0x000F;
                int pixels;

                reg[0xF] = 0;
                for (int i = 0; i < height; i++) {
                    // Fetch a row of pixels.
                    pixels = mem[index + i];
                    // Scan through each of the eight bits in the row.
                    for (int j = 0; j < 8; j++) {
                        int currX = reg[x] + j;
                        int currY = reg[y] + i;

                        // Wrap around the screen if outside bounds.
                        if (currX >= SCREEN_WIDTH) {
                            currX -= SCREEN_WIDTH;
                        }
                        if (currY >= SCREEN_HEIGHT) {
                            currY -= SCREEN_HEIGHT;
                        }

                        if ((pixels & (128 >> j)) != 0) {
                            if (gfx[currY][currX] == 1) {
                                reg[0xF] = 1;
                            }
                            gfx[currY][currX] ^= 1;
                        }
                    }
                }

                gfxUpdated = true;
                pc += 2;
            }
            break;

            case 0xE000:
                switch (opCode & 0x00FF) {
                    // EX9E: Skips the next instruction if the key stored in VX is pressed.
                    case 0x009E:
                        pc += (key[reg[x]] != 0) ? 4 : 2;
                        break;

                    // EXA1: Skips the next instruction if the key stored in VX isn't pressed.
                    case 0x00A1:
                        pc += (key[reg[x]] == 0) ? 4 : 2;
                        break;

                    default:
                        System.out.printf("Unknown opcode [0xE000]: 0x%x%n", opCode);
                }
                break;

            case 0xF000:
                switch (opCode & 0x00FF) {
                    // FX07: Sets VX to the value of the delay timer.
                    case 0x0007:
                        reg[x] = delayTimer;
                        pc += 2;
                        break;

                    // FX0A: A key press is awaited, and then stored in VX.
                    case 0x000A: {
                        boolean keyPress = false;

                        for (int i = 0; i < 16; i++) {
                            if (key[i] != 0) {
                                reg[x] = i;
                                keyPress = true;
                            }
                        }

                        if (!keyPress) {
                            return;
                        }

                        pc += 2;
                    }
                    break;

                    // FX15: Sets the delay timer to VX.
                    case 0x0015:
                        delayTimer = reg[x];
                        pc += 2;
                        break;

                    // FX18: Sets the sound timer to VX.
                    case 0x0018:
                        soundTimer = reg[x];
                        pc += 2;
                        break;

                    // FX1E: Adds VX to I.
                    case 0x001E:
                        reg[0xF] = (index + reg[x] > 0xFFF) ? 1 : 0;
                        index = (index + reg[x]) % (1 << 16);
                        pc += 2;
                        break;

                    // FX29: Sets I to the location of the sprite for the character in VX. Characters 0-F
                    // (in hexadecimal) are represented by a 4x5 font.
                    case 0x0029:
                        index = reg[x] * 5;
                        pc += 2;
                        break;

                    // FX33: Stores the Binary-coded decimal representation of VX, with the most significant of three
                    // digits at the address in I, the middle digit at I plus 1, and the least significant digit at I
                    // plus 2. (In other words, take the decimal representation of VX, place the hundreds digit in
                    // memory at location in I, the tens digit at location I+1, and the ones digit at location I+2.)
                    case 0x0033:
                        mem[index] = reg[x] / 100;
                        mem[index + 1] = (reg[x] / 10) % 10;
                        mem[index + 2] = (reg[x] % 100) % 10;
                        pc += 2;
                        break;

                    // FX55: Stores V0 to VX in memory starting at address I.
                    case 0x0055:
                        System.arraycopy(reg, 0, mem, index, x);

                        // When the operation is done, for some reason, index is set to index + x + 1.
                        index += x + 1;
                        pc += 2;
                        break;

                    // FX65: Fills V0 to VX with values from memory starting at address I.
                    case 0x0065:
                        System.arraycopy(mem, index, reg, 0, x);

                        index += x + 1;
                        pc += 2;
                        break;

                    default:
                        System.out.printf("Unknown opcode [0xF000]: 0x%x%n", opCode);
                        pc += 2;
                }
                break;

            default:
                System.out.printf("Unknown opcode: 0x%x%n", opCode);
                pc += 2;
        }

        // Update timers.
        if (delayTimer > 0) {
            delayTimer--;
        }

        if (soundTimer > 0) {
            if (soundTimer == 1) {
                System.out.println("BEEP!");
            }
            soundTimer--;
        }
    }
}
