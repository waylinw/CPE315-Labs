import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Scanner;

public class Assembler {

    public static void main(String[] args) {
        String fileName = "test.s";

        int lineNumber = 1;

        //Read the entire file line by line into an arraylist
        ArrayList<String> lines = readFile(fileName);

        //Strip all the comments in the lines, not useful
        lines = removeComments(lines);

        //Put all labels into a hashmap for constants, jumps, and branches to find
        for (String b : lines) {
            getLabels(b, lineNumber);
            lineNumber++;
        }

        //Remove all the labels from the line file so we are only left with instructions
        lines = removeLabels(lines);

        lineNumber = 1;

        //Transcode assembly to binaries
        for (String s : lines)
            transcode(s, lineNumber++);
    }

    /**
     * Translates assembly file to binary lines
     * @param line
     * @param lineNumber
     */
    public static void transcode(String line, int lineNumber) {
        //Spliting the line up by spaces so that we can get the 0th element and that'll tell us the instruction type
        String[] temp = line.split(" ");
        String instruction = "";

        int displacement;

        //Uses the .contains method from hashmap to simplify life
        //Then passes it to its specific instruction type function for transcoding

        if (rInstructions.contains(temp[0])) {
            System.out.println(transCodeR(line));
        }
        else if (iInstructions.contains(temp[0])) {
            System.out.println(transCodeI(line, lineNumber));

        }
        else if (jInstructions.contains(temp[0])) {
            instruction += jInstructionMap.get(temp[0]);

            if(labels.containsKey(temp[1])) {
                if(labels.get(temp[1]).contains("0x")) {
                    instruction += hexToBin(temp[1], 26);
                }
                else {
                    displacement = (Integer.parseInt(labels.get(temp[1])) - (lineNumber + 1));
                    instruction += decToBin(displacement, 26);
                }
            }
            System.out.println(instruction);
        }
        else if (temp[0].contains("syscall"))  {
            System.out.println("00000000000000000000000000000110");
        }
    }

    /**
     * Transcode function for I type instruction
     * @param line
     * @param lineNumber
     * @return
     */
    public static String transCodeI(String line, int lineNumber){
        String[] temp = line.split(" ");
        String coded = iInstructionMap.get(temp[0]);
        String s = "00000";
        String t = "00000";
        String imm = "0000000000000000";
        int displacement;
        String[] split;

        if (temp[0].equals("andi") || temp[0].equals("ori") || temp[0].equals("addi") || temp[0].equals("addiu") || temp[0].equals("sltiu")){
            t = registers.get(temp[1]);
            if (temp.length == 4){
                s = registers.get(temp[2]);
                if (temp[3].contains("0x"))
                    imm = hexToBin(temp[3].substring(2), 16);
                else
                    imm = decToBin(Integer.parseInt(temp[3].replaceAll(" ", "")), 16);
            }
            else {
                s = registers.get(temp[2].substring(temp[2].indexOf("(")+1, temp[2].indexOf(")")));
                imm = hexToBin(temp[2].substring(2, temp[2].indexOf("(")),16);
            }
        }
        else if (temp[0].equals("beq") || temp[0].equals("bne")){
            s = registers.get(temp[1]);
            t = registers.get(temp[2]);

            if (labels.containsKey(temp[3])) {
                if (labels.get(temp[3]).contains("0x")) {
                    imm = hexToBin(temp[1], 16);
                }
                else {
                    displacement = (Integer.parseInt(labels.get(temp[3])) - (lineNumber + 1));
                    imm = decToBin(displacement, 16);
                }
            }
        }
        else if(temp[0].equals("lw") || temp[0]. equals("sw")){
            t = registers.get(temp[1]);
            if (temp[2].contains("0x")){
                s = registers.get(temp[2].substring(temp[2].indexOf("(")+1, temp[2].indexOf(")")));
                imm = hexToBin(temp[2].substring(2,temp[2].indexOf("(")), 16);
            }
            if (labels.containsKey(temp[2])) {
                if (labels.get(temp[2]).contains("0x")) {
                    imm = hexToBin(temp[2], 16);
                }
                else {
                    displacement = (Integer.parseInt(labels.get(temp[2])) - (lineNumber + 1));
                    imm = decToBin(displacement, 16);
                }
            }
            try {
                if (temp[2].contains("(")) {
                    imm = numToBin(labels.get(temp[2].substring(0, temp[2].indexOf("("))), 16);
                }
            }
            catch (Exception e) {
            }
        }
        else if (temp[0].equals("lui")){
            t = registers.get(temp[1]);
            if (temp[2].contains("(")){
                imm = decToBin((Integer.parseInt(temp[2].substring(2,temp[2].indexOf("(")),16) + registerValues.get(temp[2].substring(temp[2].indexOf("(")+1, temp[2].indexOf(")")))),16);
            }
            else
                imm = numToBin(temp[2], 16);
        }

        return coded + s + t+ imm;
    }

    /**
     * Transcoding for R type instructions
     * @param line
     * @return String representation of the binary representation of instruction
     */
    public static String transCodeR(String line){
        String[] temp = line.split(" ");
        String coded = "000000";
        String s = "00000";
        String t = "00000";
        String d = "00000";
        String h = "00000";

        if(temp[0].equals("jr"))
            s = registers.get(temp[1]);
        else if(temp[0].equals("srl") || temp[0].equals("sra") || temp[0].equals("sll")){
            d = registers.get(temp[1]);
            t = registers.get(temp[2]);
            h = numToBin(temp[3],5); //TODO hex or bin to binary
        }else{
            d = registers.get(temp[1]);
            s = registers.get(temp[2]);
            t = registers.get(temp[3]);
        }
        return coded + s+ t + d+ h + rInstructionMap.get(temp[0]);
    }

    /**
     * Function strips label names since variable names are not
     * added to the compiled code
     * @param lines ArrayList of the entire file line by line
     * @return ArrayList of code only, without any label name
     */
    private static ArrayList<String> removeLabels(ArrayList<String> lines) {
        ArrayList<String> temp = new ArrayList<String>();

        for(String x: lines){
            if(x.contains(":"))
                temp.add(x.substring(x.indexOf(":")+1).trim());
            else
                temp.add(x);

        }

        return temp;
    }

    /**
     * Put any label into a hashmap for easier storage
     * @param line The line of code
     * @param linenum The line number of the code for the address
     */
    public static void getLabels(String line, int linenum) {
        String word[];
        String split[];

        if (line.contains(":")) {
            word = line.split(":");
            split = line.split(" ");

            if (line.contains(".word") || line.contains(".byte")) {
                labels.put(word[word.length - 2].trim(), split[2]);
            } else {
                labels.put(word[0], Integer.toString(linenum));
            }
        }
    }

    /**
     * Read in the file and return every line of the file in arraylist.
     * @param fileName
     * @return
     */
    public static ArrayList<String> readFile(String fileName) {
        ArrayList<String> lines = new ArrayList<String>();
        File f = new File(fileName);
        Scanner s = null;
        try {
            s = new Scanner(f);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            System.exit(1);
        }
        while (s.hasNextLine()) {
            lines.add(s.nextLine().trim());
        }
        return lines;
    }

    /**
     * Strips comments tag on the file on the file
     * @param lines
     * @return ArrayList with all comment tags removed
     */
    private static ArrayList<String> removeComments(ArrayList<String> lines) {
        ArrayList<String> temp = new ArrayList<String>();
        for (String s : lines) {
            if (s.indexOf('#') != -1) {
                temp.add((String) s.subSequence(0, s.indexOf('#')));
            } else {
                temp.add(s);
            }
        }
        return format(temp);
    }

    /**
     * Remove , tabs and double spaces so we can parse correctly
     * @param lines
     * @return ArrayList with all punctuations/spaces removed
     */
    private static ArrayList<String> format(ArrayList<String> lines) {
        ArrayList<String> temp = new ArrayList<String>();
        for (String s : lines) {
            s = s.replaceAll(",", " ");
            s = s.replaceAll("\t", " ");
            s = s.replaceAll("  ", " ");
            if(!s.equals(""))
                temp.add(s);
        }
        return temp;
    }

    /**
     * Convert decimal to binary
     * @param x integer to convert to binary
     * @param numChars number of bits to output
     * @return string representation of the binary output
     */
    public static String decToBin(int x, int numChars) {
        String format = "%" + numChars + "s";
        String temp = String.format(format, Integer.toBinaryString(x)).replace(' ', '0');
        if(temp.length() > 16){
            return temp.substring(temp.length()-numChars);
        }
        return temp;
    }

    /**
     * Convert hex to binary
     * @param x string representation of a hex number
     * @param numChars number of bits to output
     * @return string representation of the binary output
     */
    public static String hexToBin(String x, int numChars) {
        int hex = Integer.parseInt(x, 16);
        return decToBin(hex, numChars);
    }

    /**
     * Convert a number to binary
     * @param x string rep of the number
     * @param numChars number of characters to output
     * @return string representation of the binary output
     */
    public static String numToBin(String x, int numChars){
        if(x.contains("0x")){
            return decToBin(Integer.parseInt(x.substring(2), 16),numChars);
        }else{
            return decToBin(Integer.parseInt(x),numChars);
        }
    }

    /* Instructions are 32 bits */
    public static final String registerNames = "$zero $at $v0 $v1 $a0 $a1 $a2 $a3 $t0 $t1 $t2 $t3 $t4 $t5 $t6 $t7 $s0 $s1 $s2 $s3 $s4 $s5 $s6 $s7 $t8 $t9 $k0 $k1 $gp $sp $fp $ra";
    public static final HashMap<String, String> registers = new HashMap<String, String>();
    static {
        String[] temp = registerNames.split(" ");
        int count = 0;
        for (String s : temp) {
            registers.put(s,
                    String.format("%5s", Integer.toBinaryString(count++))
                            .replace(' ', '0'));
        }
    }
    /*
     * R type instructions OpCode(6) Rs(5) Rt(5) Rd(5) Sa(5) Func(6)
     */
    public static final ArrayList<String> rInstructions = new ArrayList<String>(
            Arrays.asList("and", "or", "add", "addu", "sll", "srl", "sra",
                    "sltu", "sub", "jr"));
    public static final String rOpCode = "000000";
    public static final HashMap<String, String> rInstructionMap = new HashMap<String, String>() {
        {
            put("and", "100100");
            put("or", "100101");
            put("add", "100000");
            put("addu", "100001");
            put("sll", "000000");
            put("srl", "000010");
            put("sra", "000011");
            put("sltu", "101001");
            put("sub", "100010");
            put("jr", "001000");
        }
    };

    /*
     * I type instructions (All opcodes except 000000, 00001x, and 0100xx)
     * OpCode(6) Rs(5) Rt(5) Immediate(16)
     */
    public static final ArrayList<String> iInstructions = new ArrayList<String>(
            Arrays.asList("andi", "ori", "addi", "addiu", "sltiu", "beq",
                    "bne", "lw", "sw", "lui"));
    public static final HashMap<String, String> iInstructionMap = new HashMap<String, String>() {
        {
            put("andi", "001100");
            put("ori", "001101");
            put("addi", "001000");
            put("addiu", "001001");
            put("sltiu", "001001");
            put("beq", "000100");
            put("bne", "000101");
            put("lw", "100011");
            put("sw", "101011");
            put("lui", "001111");
        }
    };

    /*
     * J-Type Instructions (Opcode 00001x) OpCode(6) Target(26)
     */
    public static final ArrayList<String> jInstructions = new ArrayList<String>(
            Arrays.asList("j", "jal"));
    public static final HashMap<String, String> jInstructionMap = new HashMap<String, String>() {
        {
            put("j", "000010");
            put("jal", "000011");
        }
    };

    //Hashmap for storing all the labels and their line number
    public static HashMap<String, String> labels = new HashMap<String, String>();

    //Hashmap for the values at registers
    public static HashMap<String, Integer> registerValues = new HashMap<String, Integer>();
    static{
        {
            String[] temp = registerNames.split(" ");
            for (String s : temp) {
                registerValues.put(s, 0);
            }
        }
    }

}
