/**
 * Waylin Wang Petar Georgiev
 * CPE 315 - 05
 * Lab 6
 */

import java.io.File;
import java.io.FileNotFoundException;
import java.util.*;

public class Simulator {
    public static final String registerNames = "$zero $at $v0 $v1 $a0 $a1 $a2 $a3 $t0 $t1 $t2 $t3 $t4 $t5 $t6 $t7 $s0 $s1 $s2 $s3 $s4 $s5 $s6 $s7 $t8 $t9 $k0 $k1 $gp $sp $fp $ra";

    private static String[] memArray = new String[100]; // Memory space.
    // Warning: not dynamically allocated
    private static int data = 65615; // not sure what to do with this
    private static Scanner scanner; // Scanner that holds open bin file
    private static HashMap<String, Integer> registers = new HashMap<String, Integer>();// K = register name in binary, V = contentsheld inside
    private static int programCounter = 4194304; // Which instrution we're running 0x0400000
    private static ArrayList<String> iType = new ArrayList<String>(); // simply
    private static int saveData = 0;

    private static ArrayList<String> jType = new ArrayList<String>(); // simply
    private static int instrCount = 0; // number of instructions run
    private static int memoryRef = 0; // number of memory references
    /**
     * Initializes all instance variables to hold correct register, and
     * instruction data
     */
    public static void simInit() {
        int index = 0;

        /* Fills in the memory space with the instructions */
        while (scanner.hasNext()) {
            memArray[index++] = scanner.next();
        }
        /*
         * fills in the registers with their binary names, and "00000000" as
         * empty
         */
        for (index = 0; index < 32; index++) {
            registers.put(String.format("%5s", Integer.toBinaryString(index))
                    .replace(' ', '0'), 0);
        }


        /* To determine what type an opcode is */
        iType.add("100011"); // lw opcode
        iType.add("001101"); // ori opcode
        iType.add("000100"); // beq opcode
        iType.add("000101"); // bne opcode
        iType.add("101011"); // sw
        iType.add("001111"); // lui opcode
        iType.add("001100"); // andi opcode
        iType.add("000101"); // bne opcode
        iType.add("001001"); // addiu opcode
        iType.add("001000"); // addi opcode
        jType.add("000011"); // jal opcode

        // all R type has 00000 opcode
    }

    /**
     * Opens up the designated file and passes it to a scanner for reading
     *
     * @param filename
     *            Name of the file to be opened
     */
    public static void openFile(String filename) {
        File file = new File(filename);

        try {
            scanner = new Scanner(file);
        } catch (FileNotFoundException e) {
            System.out.println("File not found!!");
        }
    }

    /**
     * Prints out all the register contents
     */
    public static void printRegisters() {
        int i;
        String[] rNames = registerNames.split(" ");
        for (i = 0; i < 32; i++) {
            System.out.println("Register "
                    + i + "(" + rNames[i]+ ")"
                    + " contains: "
                    + registers.get(String.format("%5s",
                    Integer.toBinaryString(i)).replace(' ', '0')));
        }
        System.out.println("");
    }

    /**
     * Moves the register data for the I type instructions
     *
     * @param instr
     *            String of whole line of instruction (32 bits)
     */
    public static void parseITypeInstructions(String instr) {
        String opcode = instr.substring(0, 6);
        String rs = instr.substring(6, 11);
        String rt = instr.substring(11, 16);
        String imm = instr.substring(16, 32);
        int val;
        instrCount++;

        switch (opcode) {
            case "100011":  // lw
                registers.put(rt, data);
                memoryRef++;
                programCounter += 4;
                // System.out.println("Hello");
                break;
            case "001111":  // lui
                val = Integer.parseInt(imm, 2) << 16;
                programCounter += 4;
                registers.put(rt, val);
                break;
            case "001101":  // ori
                System.out.println("RT: " + rt + " RS: " + rs + "IMM: " + imm);
                registers.put(rt,
                      Integer.parseInt(rs, 2) | Integer.parseInt(imm, 2));
                programCounter += 4;
                break;
            case "000100":  // beq
                System.out.println("BEQ: " + registers.get(rs) + ":" + registers.get(rt) + "=" + (registers.get(rs) == (registers.get(rt))));
                if (registers.get(rs) == (registers.get(rt))) {
                    System.out.println("BRANCHING HERE MOTHER FUCKER: " + ((Integer.parseInt(imm, 2) << 1)));
                    programCounter += ((Integer.parseInt(imm, 2) << 1)) * 4;
                } else {
                    programCounter += 4;
                }
                break;
            case "001001":  // addiu
                val = registers.get(rs) + Integer.parseInt(imm, 2);
                registers.put(rt, val);
                programCounter += 4;
                break;
            case "001000":  // addi
                val = registers.get(rs) + Integer.parseInt(imm, 2);
                registers.put(rt, val);
                programCounter += 4;
                break;
            case "001100":  // andi
                registers.put(rt,
                      Integer.parseInt(rs, 2) & Integer.parseInt(imm, 2));
                programCounter += 4;
                break;
            case "000101":  // bne
                if (registers.get(rs) != (registers.get(rt)))
                    if (imm.charAt(0) == '1')
                        programCounter -= ((65535 - Integer.parseInt(imm, 2))) * 4;
                    else programCounter += Integer.parseInt(imm, 2) * 4;
                else
                    programCounter += 4;
                break;
            case "101011":  // sw
                saveData = Integer.parseInt(rt, 2);
                programCounter += 4;
                memoryRef++;
                break;
        }
    }

    public static void parseJTypeInstructions(String instr) {
        instrCount++;
        String opcode = instr.substring(0, 6);
        String target = instr.substring(6);
        if (opcode.equals("000011"))
            programCounter = Integer.parseInt(target, 2) << 2;
    }

    public static void parseRTypeInstructions(String instr) {
        instrCount++;
        String opcode = instr.substring(0, 6);
        String funcCode = instr.substring(26); // get Last 6 bits
        String rs = instr.substring(6, 11);
        String rt = instr.substring(11, 16);
        String rd = instr.substring(16, 21);
        String sa = instr.substring(21, 26);
        System.out.println("FUNC CODE: " + funcCode);
        System.out.println("RS: " + rs);
        System.out.println("RT: " + rt);
        System.out.println("RD: " + rd);
        System.out.println("sa: " + sa);
        /*
         * put("and", "100100"); put("or", "100101"); put("add", "100000");
         * put("addu", "100001"); put("sll", "000000"); put("srl", "000010");
         * put("sra", "000011"); put("sltu", "101001"); put("sub", "100010");
         * put("jr", "001000");
         */
        programCounter+=4;
        switch (funcCode) {
            case "100100":  // and
                registers
                      .put(rd, (registers.get(rs) & registers.get(rt)));
                break;
            case "100101":  // or
                registers
                      .put(rd, Integer.parseInt(rs, 2) | Integer.parseInt(rt, 2));
                break;
            case "100000":  // add
                registers
                      .put(rd, Integer.parseInt(rs, 2) + Integer.parseInt(rt, 2));
                break;
            case "100001":  // addu //TODO: UNSIGNED ADD LOL
                registers
                      .put(rd, Integer.parseInt(rs, 2) + Integer.parseInt(rt, 2));
                break;
            case "000000":  // sll
                registers.put(rd,
                      registers.get(rt) << Integer.parseInt(sa, 2));
                break;
            case "000010":  // srl
                registers.put(rd,
                      registers.get(rt) >> Integer.parseInt(sa, 2));
                break;
            case "000011":  // sra TODO: ARITHMETIC V LOGICAL SHIFT
                registers.put(rd, (registers.get(rt) >> Integer.parseInt(sa, 2)));

                break;
            case "101001":  // sltu
                registers
                      .put(rd,
                            (Integer.parseInt(rs, 2) < Integer.parseInt(rt, 2)) ? 1
                                  : 0);
                break;
            case "100010":  // sub
                registers
                      .put(rd, Integer.parseInt(rs, 2) - Integer.parseInt(rt, 2));
                break;
            case "001000":  // jr
                programCounter -= 4;
                programCounter += (Integer.parseInt(rs, 2) / 4);
                break;
        }

    }

    public static void printResults() {
        System.out.println("Instructions run: " + instrCount);
        System.out.println("References to memory " + memoryRef);
    }

    public static int PCtoIndex(int x){
        return ((x - 4194304)/4);
    }

    /**
     * Moves the program by one instuction at a time
     *
     * @return Returns false if a syscall with 10 is in v0, true otherwise
     */
    public static boolean singleStep() {
        String curInstruction = memArray[PCtoIndex(programCounter)];

        String opcode = curInstruction.substring(0, 6);

        if (iType.contains(opcode)) {
            System.out.println("I INSTRUCTION");
            parseITypeInstructions(memArray[PCtoIndex(programCounter)]);
        } else if (opcode.equals("000000")) {
            System.out.println("R INSTRUCTION");
            parseRTypeInstructions(memArray[PCtoIndex(programCounter)]);
        } else if (jType.contains(opcode)) {
            System.out.println("J INSTRUCTION");
            parseJTypeInstructions(memArray[PCtoIndex(programCounter)]);
        } else if (memArray[PCtoIndex(programCounter)] == "00000000000000000000000000001100") {
            System.out.println("syscall");
            if (registers.get("000010") == 10) {
                return false;
            }
        }else{
            System.out.println("INVALID CODE");
        }
        System.out.println("Op Code = " + opcode);
        System.out.println("Program Counter = " + PCtoIndex(programCounter-4));
        printRegisters();
        return true;
    }

    public static void main(String[] args) {
        openFile("Countbits.s");
        simInit();

        boolean lock = true;
        Scanner stdin = new Scanner(System.in);

        while (lock) {

            System.out.println("1) single step, 2) run, or 3) exit?");

            int input = Integer.parseInt(stdin.next());
            switch (input) {
                case 1:
                    lock = singleStep();
                    break;
                case 2:
                    while (singleStep() && memArray[PCtoIndex(programCounter)] != null) {
                    }
                    lock = false;
                    break;
                case 3:
                    lock = false;
                    System.out.println("Good-bye!");
                    break;
                default:
                    lock = false;
                    System.out.println("No such option. Exiting...");
                    break;
            }
        }
        printResults();
        stdin.close();
        scanner.close();
    }

}
