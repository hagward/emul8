package hagward.chip8.emu;

import hagward.chip8.gui.Display;

public class Cpu {

    private boolean isRunning;
    private boolean drawFlag;

    private int pc;
    private int sp;
    private int opCode;
    private int regDelay;
    private int regSound;
    private int regI;
    private int[] regV;
    private int[] stack;

    private final CpuLoop cpuProcess;
    private final Memory memory;
    private final Video video;
    private final Keyboard keyboard;

    public Cpu(Memory memory, Video video, Keyboard keyboard) {
        this.memory = memory;
        this.video = video;
        this.keyboard = keyboard;
        cpuProcess = new CpuLoop(this);
        isRunning = false;
        reset();
    }

    public void reset() {
        pc = 0x200;
        sp = 0;
        opCode = 0;
        regDelay = 0;
        regSound = 0;
        regI = 0;
        regV = new int[16];
        stack = new int[16];
        drawFlag = true;
    }

    public void start() {
        if (isRunning) {
            return;
        }

        isRunning = true;
        new Thread(cpuProcess).start();
    }

    public void stop() {
        isRunning = false;
    }

    public boolean isRunning() {
        return isRunning;
    }

    public void emulateCycle() {
        // Fetch.
        opCode = (memory.getByte(pc++) << 8) | memory.getByte(pc++);

        int x = (opCode & 0x0F00) >> 8;
        int y = (opCode & 0x00F0) >> 4;

        // Decode.
        switch (opCode & 0xF000) {
        case 0x0000:
            switch (opCode & 0x00FF) {
            // 00E0: Clears the screen.
            case 0x00E0:
                video.clear();
                drawFlag = true;
                break;

            // 00EE: Returns from subroutine.
            case 0x00EE:
                sp--;
                pc = stack[sp];
                break;

            default:
                System.out.printf("Unknown opcode [0x0000]: 0x%x%n", opCode);
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
            if (regV[x] == (opCode & 0x00FF)) {
                pc += 2;
            }
            break;

        // 4XNN: Skips the next instruction if VX doesn't equal NN.
        case 0x4000:
            if (regV[x] != (opCode & 0x00FF)) {
                pc += 2;
            }
            break;

        // 5XY0: Skips the next instruction if VX equals VY.
        case 0x5000:
            if (regV[x] == regV[y]) {
                pc += 2;
            }
            break;

        // 6XNN: Sets VX to NN.
        case 0x6000:
            regV[x] = opCode & 0x00FF;
            break;

        // 7XNN: Adds NN to VX.
        case 0x7000:
            regV[x] = (regV[x] + (opCode & 0x00FF)) % (1 << 8);
            break;

        case 0x8000:
            switch (opCode & 0x000F) {
            // 8XY0: Sets VX to the value of VY.
            case 0x0000:
                regV[x] = regV[y];
                break;

            // 8XY1: Sets VX to VX OR VY.
            case 0x0001:
                regV[x] |= regV[y];
                break;

            // 8XY2: Sets VX to VX AND VY.
            case 0x0002:
                regV[x] &= regV[y];
                break;

            // 8XY3: Sets VX to VX XOR VY.
            case 0x0003:
                regV[x] ^= regV[y];
                break;

            // 8XY4: Adds VY to VX. VF is set to 1 when there's a carry, and to
            // 0 when there isn't.
            case 0x0004:
                regV[0xF] = (regV[y] > (0xFF - regV[x])) ? 1 : 0;
                regV[x] = (regV[x] + regV[y]) % (1 << 8);
                break;

            // 8XY5: VY is subtracted from VX. VF is set to 0 when there's a
            // borrow, and 1 when there isn't.
            case 0x0005:
                regV[0xF] = (regV[y] > regV[x]) ? 0 : 1;
                regV[x] = (regV[x] - regV[y]) % (1 << 8);
                if (regV[x] < 0) {
                    regV[x] += (1 << 8);
                }
                break;

            // 8XY6: Shifts VX right by one. VF is set to the value of the least
            // significant bit of VX before
            // the shift.
            case 0x0006:
                regV[0xF] = regV[x] & 1;
                regV[x] >>= 1;
                break;

            // 8XY7: Sets VX to VY minus VX. VF is set to 0 when there's a
            // borrow, and 1 when there isn't.
            case 0x0007:
                regV[0xF] = (regV[x] > regV[y]) ? 0 : 1;
                regV[x] = (regV[y] - regV[x]) % (1 << 8);
                if (regV[x] < 0) {
                    regV[x] += (1 << 8);
                }
                break;

            // 8XYE: Shifts VX left by one. VF is set to the value of the most
            // significant bit of VX before the
            // shift
            case 0x000E:
                regV[0xF] = regV[x] >> 7;
                regV[x] <<= 1;
                break;

            default:
                System.out.printf("Unknown opcode [0x8000]: 0x%x%n", opCode);
            }
            break;

        // 9XY0: Skips the next instruction if VX doesn't equal VY.
        case 0x9000:
            if (regV[x] != regV[y]) {
                pc += 2;
            }
            break;

        // ANNN: Sets the index register to address NNN.
        case 0xA000:
            regI = opCode & 0x0FFF;
            break;

        // BNNN: Jumps to the address NNN plus V0.
        case 0xB000:
            pc = (opCode & 0x0FFF) + regV[0];
            break;

        // CXNN: Sets VX to a random number and NN.
        case 0xC000:
            regV[x] = ((int) (Math.random() * 0xFF)) & (opCode & 0x00FF);
            break;

        // DXYN: Sprites stored in memory at location in index register (I),
        // maximum 8 bits wide. Wraps around the
        // screen. If when drawn, clears a pixel, register VF is set to 1
        // otherwise it is zero. All drawing is XOR
        // drawing (i.e. it toggles the screen pixels).
        case 0xD000: {
            int height = opCode & 0x000F;
            int pixels;

            regV[0xF] = 0;
            for (int i = 0; i < height; i++) {
                // Fetch a row of pixels.
                pixels = memory.getByte(regI + i);
                // Scan through each of the eight bits in the row.
                for (int j = 0; j < 8; j++) {
                    int currX = regV[x] + j;
                    int currY = regV[y] + i;

                    // Wrap around the screen if outside bounds.
                    if (currX >= Display.DISPLAY_WIDTH) {
                        currX -= Display.DISPLAY_WIDTH;
                    }
                    if (currY >= Display.DISPLAY_HEIGHT) {
                        currY -= Display.DISPLAY_HEIGHT;
                    }

                    if ((pixels & (128 >> j)) != 0) {
                        boolean newPixelState = video.togglePixel(currX, currY);
                        if (!newPixelState) {
                            regV[0xF] = 1;
                        }
                    }
                }
            }

            drawFlag = true;
        }
            break;

        case 0xE000:
            switch (opCode & 0x00FF) {
            // EX9E: Skips the next instruction if the key stored in VX is
            // pressed.
            case 0x009E:
                if (keyboard.isPressed(regV[x])) {
                    pc += 2;
                }
                break;

            // EXA1: Skips the next instruction if the key stored in VX isn't
            // pressed.
            case 0x00A1:
                if (!keyboard.isPressed(regV[x])) {
                    pc += 2;
                }
                break;

            default:
                System.out.printf("Unknown opcode [0xE000]: 0x%x%n", opCode);
            }
            break;

        case 0xF000:
            switch (opCode & 0x00FF) {
            // FX07: Sets VX to the value of the delay timer.
            case 0x0007:
                regV[x] = regDelay;
                break;

            // FX0A: A key press is awaited, and then stored in VX.
            case 0x000A: {
                boolean keyPress = false;

                for (int i = 0; i < 16; i++) {
                    if (keyboard.isPressed(i)) {
                        regV[x] = i;
                        keyPress = true;
                        break;
                    }
                }

                if (!keyPress) {
                    return;
                }
            }
                break;

            // FX15: Sets the delay timer to VX.
            case 0x0015:
                regDelay = regV[x];
                break;

            // FX18: Sets the sound timer to VX.
            case 0x0018:
                regSound = regV[x];
                break;

            // FX1E: Adds VX to I.
            case 0x001E:
                regV[0xF] = (regI + regV[x] > 0xFFF) ? 1 : 0;
                regI = (regI + regV[x]) % (1 << 16);
                break;

            // FX29: Sets I to the location of the sprite for the character in
            // VX. Characters 0-F
            // (in hexadecimal) are represented by a 4x5 font.
            case 0x0029:
                regI = regV[x] * 5;
                break;

            // FX33: Stores the Binary-coded decimal representation of VX, with
            // the most significant of three
            // digits at the address in I, the middle digit at I plus 1, and the
            // least significant digit at I
            // plus 2. (In other words, take the decimal representation of VX,
            // place the hundreds digit in
            // memory at location in I, the tens digit at location I+1, and the
            // ones digit at location I+2.)
            case 0x0033:
                memory.setByte(regI, regV[x] / 100);
                memory.setByte(regI + 1, (regV[x] / 10) % 100);
                memory.setByte(regI + 2, regV[x] % 100);
                break;

            // FX55: Stores V0 to VX in memory starting at address I.
            case 0x0055:
                for (int i = 0; i < x; i++) {
                    memory.setByte(regI + i, regV[i]);
                }
                // When the operation is done, index should be set to index + x
                // + 1.
                regI += x + 1;
                break;

            // FX65: Fills V0 to VX with values from memory starting at address
            // I.
            case 0x0065:
                for (int i = 0; i < x; i++) {
                    regV[i] = memory.getByte(regI + i);
                }
                regI += x + 1;
                break;

            default:
                System.out.printf("Unknown opcode [0xF000]: 0x%x%n", opCode);
            }
            break;

        default:
            System.out.printf("Unknown opcode: 0x%x%n", opCode);
        }

        // Update timers.
        if (regDelay > 0) {
            regDelay--;
        }
        if (regSound > 0) {
            if (regSound == 1) {
                System.out.println("BEEP!");
            }
            regSound--;
        }

        if (drawFlag) {
            video.flushVideo();
            drawFlag = false;
        }
    }

}
