/**
 * Waylin Wang, Petar Georgiev
 * CPE 315 - 05 Lab 6
 */

import java.io.File;
import java.io.FileNotFoundException;
import java.util.*;

public class Performance {

    public static ArrayList<String> pipeline = new ArrayList<String>(5);
    public static ArrayList<String> pipedReg = new ArrayList<String>(5);
    private static int clocks = 0;
    public static final String registerNames = "$zero $at $v0 $v1 $a0 $a1 $a2 $a3 $t0 $t1 $t2 $t3 $t4 $t5 $t6 $t7 $s0 $s1 $s2 $s3 $s4 $s5 $s6 $s7 $t8 $t9 $k0 $k1 $gp $sp $fp $ra";

    private static String[] memArray = new String[100];
    private static int data = 65615;
    private static Scanner scanner;
    private static HashMap<String, Integer> registers =
    new HashMap<String, Integer>();
    private static int programCounter = 4194304;
    private static ArrayList<String> iType = new ArrayList<String>();
    private static int saveData = 0;

    private static ArrayList<String> jType = new ArrayList<String>();
    private static int instrCount = 0;
    private static int memoryRef = 0; 

    public static void simInit() {
        int index = 0;
        for (int i = 0; i < 5; i++)
            pipeline.add(null);

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
        // rType.add("100101"); // or
        // rType.add("100100"); // and
        // rType.add("000000"); // sll
        // rType.add("100001"); // addu
    }

    public static void openFile(String filename) {
        File file = new File(filename);

        try {
            scanner = new Scanner(file);
        } catch (FileNotFoundException e) {
            System.out.println("File not found!!");
        }
    }

    public static void printRegisters() {
        int i;
        String[] rNames = registerNames.split(" ");
        for (i = 0; i < 32; i++) {
            System.out.println("Register "
                    + i
                    + "("
                    + rNames[i]
                    + ")"
                    + " contains: "
                    + registers.get(String.format("%5s",
                            Integer.toBinaryString(i)).replace(' ', '0')));
        }
        System.out.println("");
    }

    public static void parseITypeInstructions(String instr) {
        String opcode = instr.substring(0, 6);
        String rs = instr.substring(6, 11);
        String rt = instr.substring(11, 16);
        String imm = instr.substring(16, 32);
        int val;
        instrCount++;

        if(pipedReg.contains(rt)) {
            System.out.println("Possible data hazard");
        }
        pipedReg.add(rs);

        switch (opcode) {
            case "100011":  // lw
                registers.put(rt, data);
                memoryRef++;
                programCounter += 4;
                pipeline.set(0, "lw");
                break;
            case "001111":  // lui
                val = Integer.parseInt(imm, 2) << 16;
                programCounter += 4;
                registers.put(rt, val);
                break;
            case "001101":  // ori
                registers.put(rt,
                      Integer.parseInt(rs, 2) | Integer.parseInt(imm, 2));
                pipeline.set(0, "ori");
                programCounter += 4;
                break;
            case "000100":  // beq
                pipeline.set(0, "beq");
                if (registers.get(rs) == (registers.get(rt))) {
                    programCounter += ((Integer.parseInt(imm, 2) << 1)) * 4;
                } else {
                    programCounter += 4;
                }
                break;
            case "001001":  // addiu
                pipeline.set(0, "addiu");
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
                pipeline.set(0, "bne");
                if (registers.get(rs) != (registers.get(rt)))
                    if (imm.charAt(0) == '1')
                        programCounter -= ((65535 - Integer.parseInt(imm, 2))) * 4;
                    else
                        programCounter += Integer.parseInt(imm, 2) * 4;
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
        if (opcode.equals("000011")) {
            programCounter = Integer.parseInt(target, 2) << 2;
            pipeline.set(0, "jal");
        }
    }

    public static void parseRTypeInstructions(String instr) {
        instrCount++;
        String opcode = instr.substring(0, 6);
        String funcCode = instr.substring(26); // get Last 6 bits
        String rs = instr.substring(6, 11);
        String rt = instr.substring(11, 16);
        String rd = instr.substring(16, 21);
        String sa = instr.substring(21, 26);

        if(pipedReg.contains(rs) || pipedReg.contains(rt))
            System.out.println("Possible data hazard");

        pipedReg.add(rd);

        /*
         * put("and", "100100"); put("or", "100101"); put("add", "100000");
         * put("addu", "100001"); put("sll", "000000"); put("srl", "000010");
         * put("sra", "000011"); put("sltu", "101001"); put("sub", "100010");
         * put("jr", "001000");
         */
        programCounter += 4;
        switch (funcCode) {
            case "100100":  // and
                registers.put(rd, (registers.get(rs) & registers.get(rt)));
                pipeline.set(0, "and");
                break;
            case "100101":  // or
                registers
                      .put(rd, Integer.parseInt(rs, 2) | Integer.parseInt(rt, 2));
                pipeline.set(0, "or");
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

                if (sa.equals("00000"))
                    pipeline.set(0, "bubble");
                else
                    pipeline.set(0, "sll");

                registers.put(rd, registers.get(rt) << Integer.parseInt(sa, 2));
                break;
            case "000010":  // srl
                registers.put(rd, registers.get(rt) >> Integer.parseInt(sa, 2));
                break;
            case "000011":  // sra
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
                pipeline.set(0, "jr");
                programCounter -= 4;
                programCounter += (Integer.parseInt(rs, 2) / 4);
                break;
        }

    }

    public static void printResults() {
        System.out.println("Instructions run: " + instrCount);
        System.out.println("References to memory " + memoryRef);
    }

    public static int PCtoIndex(int x) {
        return ((x - 4194304) / 4);
    }

    public static boolean singleStep() {
        String curInstruction = memArray[PCtoIndex(programCounter)];

        String opcode = curInstruction.substring(0, 6);
        /* MOVE THE PIPELINE */
        for (int i = pipeline.size() - 1; i > 0; i--) {
            pipeline.set(i, pipeline.get(i - 1));
        }

        if (iType.contains(opcode)) {
            parseITypeInstructions(memArray[PCtoIndex(programCounter)]);
        } else if (opcode.equals("000000")) {
            parseRTypeInstructions(memArray[PCtoIndex(programCounter)]);
        } else if (jType.contains(opcode)) {
            parseJTypeInstructions(memArray[PCtoIndex(programCounter)]);
        } else if (memArray[PCtoIndex(programCounter)] == "00000000000000000000000000001100") {
            pipeline.set(0, "syscall");
            if (registers.get("000010") == 10)
                return false;

        } else {
            System.out.println("INVALID CODE");
        }

        if(pipedReg.size() >= 5) {
            pipedReg.remove(0);
        }

        /* Print pipeline */
        for (String x : pipeline)
            System.out.print(x + " -> ");

        System.out.println("");

        if (pipeline.get(2) != null)
            if (pipeline.get(2).equals("beq") || pipeline.get(2).equals("bne")
                    || pipeline.get(2).equals("jr") || pipeline.get(2).equals("jal") ){
                flush();
                System.out.println("Branch Hazard");
            }
        System.out.println();

        return true;
    }

    public static void flush() {
        for (int i = pipeline.size() - 1; i >= 0; i--)
            pipeline.set(i, null);
    }

    public static void main(String[] args) {
        openFile("Countbits.s");
        simInit();

        boolean lock = true;
        Scanner stdin = new Scanner(System.in);

        while (lock) {

            int input = 2;
            switch (input) {
                case 1:
                    lock = singleStep();
                    break;
                case 2:
                    while (singleStep()
                          && memArray[PCtoIndex(programCounter)] != null)
                        ;
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
        System.out.println(clocks);
    }

}
