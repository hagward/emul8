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
        int x = opCode & 0x0F00 >> 8;
        int y = opCode & 0x00F0 >> 4;

        // Fetch.
        opCode = mem[pc] << 8 | mem[pc + 1];

        // Decode.
        switch (opCode & 0xF000) {
            case 0x0000:
                switch (opCode & 0x00FF) {
                    // 00E0: Clears the screen.
                    case 0x00E0:
                        // TODO: clear the screen.
                        break;
                    // 00EE: Returns from subroutine.
                    case 0x00EE:
                        // TODO: return from subroutine.
                        break;
                    // 0NNN: Calls RCA 1802 program at address NNN.
                    default:
                        // TODO: complete.
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
                pc += (reg[x] == (opCode & 0x00FF)) ? 2 : 1;
                break;

            // 4XNN: Skips the next instruction if VX doesn't equal NN.
            case 0x4000:
                pc += (reg[x] == (opCode & 0x00FF)) ? 1 : 2;
                break;

            // 5XY0: Skips the next instruction if VX equals VY.
            case 0x5000:
                pc += (reg[x] == reg[y]) ? 2 : 1;
                break;

            // 6XNN: Sets VX to NN.
            case 0x6000:
                reg[x] = opCode & 0x00FF;
                break;

            // 7XNN: Adds NN to VX.
            case 0x7000:
                // TODO: is the modulo needed?
                reg[x] = (reg[x] + (opCode & 0x00FF)) % 255;
                break;

            case 0x8000:
                switch (opCode & 0x000F) {
                    // 8XY0: Sets VX to the value of VY.
                    case 0x0000:
                        reg[x] = reg[y];
                        break;

                    // 8XY1: Sets VX to VX OR VY.
                    case 0x0001:
                        reg[x] |= reg[y];
                        break;

                    // 8XY2: Sets VX to VX AND VY.
                    case 0x0002:
                        reg[x] &= reg[y];
                        break;

                    // 8XY3: Sets VX to VX XOR VY.
                    case 0x0003:
                        reg[x] ^= reg[y];
                        break;

                    // 8XY4: Adds VY to VX. VF is set to 1 when there's a carry, and to 0 when there isn't.
                    case 0x0004:
                        reg[0xF] = (reg[y] > (0xFF - reg[x])) ? 1 : 0;
                        reg[x] = (reg[x] + reg[y]) % 255;
                        pc += 2;
                        break;

                    // 8XY5: VY is subtracted from VX. VF is set to 0 when there's a borrow, and 1 when there isn't.
                    case 0x0005:
                        reg[0xF] = (reg[y] > reg[x]) ? 0 : 1;
                        // TODO: check that his doesn't become negative.
                        reg[x] = (reg[x] - reg[y]) % 255;
                        pc += 2;
                        break;

                    // 8XY6: Shifts VX right by one. VF is set to the value of the least significant bit of VX before
                    // the shift.
                    case 0x0006:
                        reg[0xF] = reg[x] & 1;
                        reg[x] >>= 1;
                        break;

                    // 8XY7: Sets VX to VY minus VX. VF is set to 0 when there's a borrow, and 1 when there isn't.
                    case 0x0007:
                        reg[0xF] = (reg[x] > reg[y]) ? 0 : 1;
                        // TODO: check that this doesn't become negative.
                        reg[x] = (reg[y] - reg[x]) % 255;
                        break;

                    // 8XYE: Shifts VX left by one. VF is set to the value of the most significant bit of VX before the
                    // shift
                    case 0x000E:
                        // TODO: really 7 steps?
                        reg[0xF] = (reg[x] & 128) >> 7;
                        reg[x] <<= 1;
                        break;
                }

            // 9XY0: Skips the next instruction if VX doesn't equal VY.
            case 0x9000:
                if (reg[x] != reg[y]) {
                    pc += 2;
                }
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
                reg[x] = (int) (Math.random() * 10) << 8;
                reg[x] |= opCode & 0x00FF;
                break;

            // DXYN: Sprites stored in memory at location in index register (I), maximum 8bits wide. Wraps around the
            // screen. If when drawn, clears a pixel, register VF is set to 1 otherwise it is zero. All drawing is XOR
            // drawing (i.e. it toggles the screen pixels).
            case 0xD000:
                // TODO: complete this case.
                break;

            case 0xE000:
                switch (opCode & 0x00FF) {
                    // EX9E: Skips the next instruction if the key stored in VX is pressed.
                    case 0x009E:
                        // TODO: complete.
                        break;
                    // EXA1: Skips the next instruction if the key stored in VX isn't pressed.
                    case 0x00A1:
                        // TODO: complete.
                        break;
                    default:
                        System.out.printf("Unknown opcode [0xE000]: 0x%x%n", opCode);
                }

            case 0xF000:
                switch (opCode & 0x00FF) {
                    // FX07: Sets VX to the value of the delay timer.
                    case 0x0007:
                        reg[x] = delayTimer;
                        break;
                    // FX0A: A key press is awaited, and then stored in VX.
                    case 0x000A:
                            // TODO: complete.
                        break;
                    // FX15: Sets the delay timer to VX.
                    case 0x0015:
                        delayTimer = reg[x];
                        break;
                    // FX18: Sets the sound timer to VX.
                    case 0x0018:
                        soundTimer = reg[x];
                        break;
                    // FX1E: Adds VX to I.
                    case 0x001E:
                        index = (index + reg[x]) % 255;
                        break;
                    // FX29: Sets I to the location of the sprite for the character in VX. Characters 0-F
                    // (in hexadecimal) are represented by a 4x5 font.
                    case 0x0029:
                        // TODO: complete.
                        break;
                    // FX33: Stores the Binary-coded decimal representation of VX, with the most significant of three
                    // digits at the address in I, the middle digit at I plus 1, and the least significant digit at I
                    // plus 2. (In other words, take the decimal representation of VX, place the hundreds digit in
                    // memory at location in I, the tens digit at location I+1, and the ones digit at location I+2.)
                    case 0x0033:
                        mem[index] = reg[x] / 100;
                        mem[index + 1] = (reg[x] / 10) % 10;
                        mem[index + 2] = reg[x] % 10;
                        pc += 2;
                        break;
                    // FX55: Stores V0 to VX in memory starting at address I.
                    case 0x0055:
                        for (int i = 0; i < x; i++) {
                            mem[index + i] = reg[i];
                        }
                        break;
                    // FX65: Fills V0 to VX with values from memory starting at address I.
                    case 0x0065:
                        for (int i = 0; i < x; i++) {
                            reg[i] = mem[index + i];
                        }
                        break;
                    default:
                        System.out.printf("Unknown opcode [0xF000]: 0x%x%n", opCode);
                }

            default:
                System.out.printf("Unknown opcode: 0x%x%n", opCode);
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
