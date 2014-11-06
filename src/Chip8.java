import java.io.File;

/**
 * Created by Anders on 2014-11-06.
 */
public class Chip8 {
    private int opCode;
    private int index;
    private int pc;
    private int sp;
    private int delayTimer;
    private int soundTimer;
    private int[] mem;
    private int[] reg;
    private int[] gfx;
    private int[] stack;
    private int[] key;

    public void initialize() {
        pc = 0x200;
        sp = 0;
        opCode = 0;
        index = 0;
        delayTimer = 0;
        soundTimer = 0;
        mem = new int[4096];
        reg = new int[16];
        gfx = new int[64 * 32];
        stack = new int[16];
        key = new int[16];
    }

    public void loadGame(File file) {

    }

    public void emulateCycle() {
        // Fetch.
        opCode = mem[pc] << 8 | mem[pc + 1];

        // Decode.
        switch (opCode & 0xF000) {
            case 0x0000:
                switch (opCode & 0x000F) {
                    // 0x00E0: Clears the screen.
                    case 0x0000:
                        // TODO: clear the screen.
                        break;
                    // 0x00EE: Returns from subroutine.
                    case 0x000E:
                        // TODO: return from subroutine.
                        break;
                    default:
                        System.out.printf("Unknown opcode [0x0000]: 0x%d%n", opCode);
                }
                break;

            // 2NNN: Calls subroutine at address NNN.
            case 0x2000:
                stack[sp] = pc;
                sp++;
                pc = opCode & 0x0FFF;
                break;

            case 0x8000:
                int x = opCode & 0x0F00 >> 8;
                int y = opCode & 0x00F0 >> 4;
                switch (opCode & 0x000F) {
                    // 0x8XY4: Adds VY to VX. VF is set to 1 when there's a carry, and to 0 when there isn't.
                    case 0x0004:
                        // If the result overflows, i.e. gets larger than 255, set the carry flag to 1. Otherwise, set
                        // it to 0.
                        reg[0xF] = (reg[y] > (0xFF - reg[x])) ? 1 : 0;
                        reg[x] = (reg[x] + reg[y]) % 255;
                        break;
                }

            // ANNN: Sets the index register to address NNN.
            case 0xA000:
                index = opCode & 0x0FFF;
                pc += 2;
                break;

            default:
                System.out.printf("Unknown opcode: 0x%d%n", opCode);
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

    public static void main(String[] args) {

    }
}
