/* On my honor, I have neither given nor received unauthorized aid on this assignment */
/*Rishabh Krishna UFID 20113421*/
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;

public class MIPSsim {
	
	int addLoc=128;
	int simulationKey=128;
	int startOfData=0;
	int endOfData=0;
	boolean isBreakYet = false;
	ArrayList<String> inputFile = new ArrayList<String>();
	ArrayList<Long> inputFileAsNum = new ArrayList<Long>();
	ArrayList<Integer> categoryIndex = new ArrayList<Integer>();
	ArrayList<String> instructions = new ArrayList<String>();
	HashMap<Integer,Integer> category = new HashMap<Integer,Integer>();
	HashMap<Integer,String> categoryOne = new HashMap<Integer,String>();
	HashMap<Integer,String> categoryTwo = new HashMap<Integer,String>();
	HashMap<Integer,String> categoryThree = new HashMap<Integer,String>();
	long[] registers = new long[32];	
	HashMap<Integer,String> processingLocation= new HashMap<Integer,String>();
	ArrayList<String> regPrint = new ArrayList<String>();
	/*Constructor*/
	MIPSsim() {
		Arrays.fill(registers, 0);
		regPrint.add("R00:");
		regPrint.add("R08:");
		regPrint.add("R16:");
		regPrint.add("R24:");
		/*To decide among Category One Two and Three*/
		category.put(0, 1);
		category.put(6, 2);
		category.put(7, 3);
		/*To get OpCodes of Category 1*/
		categoryOne.put(0, "J");
		categoryOne.put(2, "BEQ");
		categoryOne.put(4, "BGTZ");
		categoryOne.put(5, "BREAK");
		categoryOne.put(6, "SW");
		categoryOne.put(7, "LW");
		/*To get OpCodes of Category 2*/
		categoryTwo.put(0,"ADD");
		categoryTwo.put(1,"SUB");
		categoryTwo.put(2,"MUL");
		categoryTwo.put(3,"AND");
		categoryTwo.put(4,"OR");
		categoryTwo.put(5,"XOR");
		categoryTwo.put(6,"NOR");
		/*to get OpCodes of Category 3 */
		categoryThree.put(0,"ADDI");
		categoryThree.put(1,"ANDI");
		categoryThree.put(2,"ORI");
		categoryThree.put(3,"XORI");
	}
	
	//function to read the files
	public ArrayList<String> readInputFile(String args)
	{
		try (BufferedReader buffRead = new BufferedReader(new FileReader(args)))
		{
			String strCurrentLine;
			while ((strCurrentLine = buffRead.readLine()) != null) {
				inputFile.add(strCurrentLine);
				//System.out.println(strCurrentLine);
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		return inputFile;
	}
	
	//write to File
	public void writetoDisassembly() {
		try {
			File file = new File("disassembly.txt");
			// if file doesn't exists, then create it
			if (!file.exists()) {
				file.createNewFile();
			}
 			FileWriter fileWriter = new FileWriter(file.getAbsoluteFile());
			BufferedWriter bufferedWriter = new BufferedWriter(fileWriter);
			//start writing
			for(int i=0;i<inputFile.size();i++,addLoc+=4) {
				bufferedWriter.write(inputFile.get(i)+"\t"+addLoc+"\t"+instructions.get(i)+"\n");
				processingLocation.put(addLoc, instructions.get(i));
				//bufferedWriter.newLine();
			}
			//end writing
			endOfData=addLoc-4;
			//System.out.println("Start of Data :"+startOfData+" and End of Data:"+endOfData);
			bufferedWriter.close();
		} 
		catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	//dummy function to print
	public void printInputFile(ArrayList<String> inputFile) {
		/*for(String str : inputFile)
			System.out.println(str);*/
		for(int i=0; i<inputFile.size();i++)
			System.out.println("hi" + inputFile.get(i));
	}
	
	//function to convert the read file to Numbers
	public void convertToNumbers() {
		for(String str : inputFile)
			inputFileAsNum.add(Long.parseLong(str, 2));
	}

	//decide which category from the first 3 bits
	public void getCategory() {
		int ftBits;
		//int ii=0;
		for(Long i : inputFileAsNum) {
			
			ftBits= (int) ((i>>29) & 7);
			categoryIndex.add(category.get(ftBits));
			/*System.out.println(categoryIndex.get(ii));
			ii++;*/
			
		}
	}
	
	//based on category, call the respective functions
	public void disassemble() {
		for(int i=0; i<categoryIndex.size();i++) {
			String strBldr = new String();
			if(isBreakYet==false) {
					switch (categoryIndex.get(i)) {
					case 1://go to solve category one
						strBldr = decodeCategory1(inputFileAsNum.get(i),i);	
						instructions.add(strBldr);
						break;
					case 2://go to solve category two
						strBldr = decodeCategory2(inputFileAsNum.get(i));	
						instructions.add(strBldr);
						break;
					case 3://go to solve category three
						strBldr = decodeCategory3(inputFileAsNum.get(i));	
						instructions.add(strBldr);
						break;
					default:
						break;
					}
			}
			else {
				Long insNumber = inputFileAsNum.get(i);
				int signBit= (int) ((insNumber>>31) & 1);
				if(signBit == 1) {
					//System.out.println("number "+insNumber);
					strBldr = Long.toHexString(insNumber);
					int x =Long.valueOf(strBldr,16).intValue();
					strBldr=Integer.toString(x);
					instructions.add(strBldr);
				}
				else {
					strBldr=insNumber.toString();
					instructions.add(strBldr);
				}
			}
		}
	}
	
	public String decodeCategory1(Long insNumber, int i) {
		String strBldr = new String();
		int opcode = (int) ((insNumber>>26) & 7);
		if(categoryOne.get(opcode).equalsIgnoreCase("BREAK")) {
			isBreakYet = true;
			startOfData=addLoc + (i+1)*4;
			strBldr=categoryOne.get(opcode);
		}
		else {
			switch (categoryOne.get(opcode)) {
			case "J":
				strBldr=caseJ(insNumber, i);
				break;
			case "BEQ":
				strBldr=caseBEQ(insNumber);
				break;
			case "BGTZ":
				strBldr=caseBGTZ(insNumber);
				break;
			case "SW":
				strBldr=caseSW(insNumber);
				break;
			case "LW":
				strBldr=caseLW(insNumber);
				break;
			default:
				break;
			}
		}
		return strBldr;
	}
	
	private String caseLW(Long insNumber) {
		String strLW = new String();
		int base = (int) ((insNumber>>21) & 31);
		int rt = (int) ((insNumber>>16) & 31);
		int offset = (int) ((insNumber) & 0b1111111111111111);
		strLW="LW R"+rt+", "+offset+"(R"+base+")";
		return strLW;
	}

	private String caseSW(Long insNumber) {
		String strSW = new String();
		int base = (int) ((insNumber>>21) & 31);
		int rt = (int) ((insNumber>>16) & 31);
		int offset = (int) ((insNumber) & 0b1111111111111111);
		strSW="SW R"+rt+", "+offset+"(R"+base+")";
		return strSW;
	}

	private String caseBGTZ(Long insNumber) {
		String strBGTZ = new String();
		int rs = (int) ((insNumber>>21) & 31);
		int offset = (int) ((insNumber) & 0b1111111111111111);
		offset<<=2;
		strBGTZ="BGTZ R"+rs+", #"+offset;
		return strBGTZ;
	}

	private String caseBEQ(Long insNumber) {
		String strBEQ = new String();
		int rs = (int) ((insNumber>>21) & 31);
		int rt = (int) ((insNumber>>16) & 31);
		int offset = (int) ((insNumber) & 0b1111111111111111);
		offset<<=2;
		strBEQ="BEQ R"+rs+", R"+rt+", #"+offset;
		return strBEQ;
	}

	private String caseJ(Long insNumber, int i) {
		String strJ = new String();
		int nextInstr = addLoc + (i+1)*4;
		nextInstr=(nextInstr & 0b11111111111111111111111111111111);
		//System.out.println("after getting bitwise andto 32 bits "+nextInstr);
		nextInstr=(nextInstr & 0b11110000000000000000000000000000);
		//System.out.println("after right shift so that i get 4 MSB "+nextInstr);
		int addr = (int) (insNumber & 0b11111111111111111111111111);
		addr<<=2;
		int finalLoc = addr | nextInstr;
		strJ="J #"+finalLoc;
		return strJ;
	}

	public String decodeCategory2(Long insNumber) {
		String strBldr = new String();
		int rs = (int) ((insNumber>>24) & 31);
		int rt = (int) ((insNumber>>19) & 31);
		int rd = (int) ((insNumber>>11) & 31);
		int opcode = (int) ((insNumber>>16) & 7);
		strBldr=categoryTwo.get(opcode)+" R"+rd+", R"+rs+", R"+rt;
		return strBldr;
	}
	
	public String decodeCategory3(Long insNumber) {
		String strBldr = new String();
		int rs = (int) ((insNumber>>24) & 31);
		int rt = (int) ((insNumber>>19) & 31);
		int immVal = (int) (insNumber & 65535);
		int opcode = (int) ((insNumber>>16) & 7);
		strBldr=categoryThree.get(opcode)+" R"+rt+", R"+rs+", #"+immVal;
		return strBldr;
	}
	
	//main function
	public static void main(String[] args) {
		MIPSsim obj = new MIPSsim();
		/*To read the lines from the input file*/
		obj.readInputFile(args[0]);
		/*To convert to numeric values*/
		obj.convertToNumbers();
		/*To get first three bits to decide the category*/
		obj.getCategory();
		/*To finally work out the details of disassembly*/
		obj.disassemble();
		/*To write to the file*/
		obj.writetoDisassembly();
		
		obj.simulation();
		/*   To Test Input File Printing   */
		/*obj.printInputFile(inputFile);
		for(Long i : obj.inputFileAsNum)
			System.out.println(i);
		*/
		/*To print everything*/
		/*
		for(int i=0; i<obj.inputFileAsNum.size(); i++)
			System.out.println(obj.inputFile.get(i)+ "\t" + obj.inputFileAsNum.get(i)+"\t"+obj.categoryIndex.get(i)+"\t"+ obj.instructions.get(i));
		*/
	}

	/*ALL the function Calls for Simulation*/
	public boolean simulation() {
		int cycleNumber=1;
		int nextAddrLoc=0;
		try {
			File file = new File("simulation.txt");
			// if file doesn't exists, then create it
			if (!file.exists()) {
				file.createNewFile();
			}
 			FileWriter fileWriter = new FileWriter(file.getAbsoluteFile());
			BufferedWriter bufferedWriter = new BufferedWriter(fileWriter);
			while(! (processingLocation.get(simulationKey).equalsIgnoreCase("BREAK"))) {
				String decodedInstruction = processingLocation.get(simulationKey);
				//System.out.println("decoded instruction in simulation "+decodedInstruction);
				String[] toOperate = decodedInstruction.split(" ");
				switch (toOperate[0]) {
					case "J":
						//System.out.println("J");
						nextAddrLoc=impJ(decodedInstruction);
						break;
					case "BEQ":
						//System.out.println("BEQ");
						nextAddrLoc=impBEQ(simulationKey,decodedInstruction);
						//System.out.println("BEQ Next addr :" + nextAddrLoc);
						break;
					case "BGTZ":
						//System.out.println("BGTZ");
						nextAddrLoc=impBGTZ(simulationKey,decodedInstruction);
						//System.out.println("BGTZ Next addr :" + nextAddrLoc);
						break;
					case "SW":
						//System.out.println("SW");
						nextAddrLoc=impSW(simulationKey,decodedInstruction);
						break;
					case "LW":
						//System.out.println("LW");
						nextAddrLoc=impLW(simulationKey,decodedInstruction);
						break;
					case "ADD":
						//System.out.println("ADD");
						nextAddrLoc=impADD(simulationKey,decodedInstruction);
						break;
					case "SUB":
						//System.out.println("SUB");
						nextAddrLoc=impSUB(simulationKey,decodedInstruction);
						break;
					case "MUL":
						//System.out.println("MUL");
						nextAddrLoc=impMUL(simulationKey,decodedInstruction);
						break;
					case "AND":
						//System.out.println("AND");
						nextAddrLoc=impAND(simulationKey,decodedInstruction);
						break;
					case "OR":
						//System.out.println("OR");
						nextAddrLoc=impOR(simulationKey, decodedInstruction);
						break;
					case "XOR":
						//System.out.println("XOR");
						nextAddrLoc=impXOR(simulationKey,decodedInstruction);
						break;
					case "NOR":
						//System.out.println("NOR");
						nextAddrLoc=impNOR(simulationKey,decodedInstruction);
						break;
					case "ADDI":
						//System.out.println("ADDI");
						nextAddrLoc=impADDI(simulationKey,decodedInstruction);
						break;
					case "ANDI":
						//System.out.println("ANDI");
						nextAddrLoc=impANDI(simulationKey,decodedInstruction);
						break;
					case "ORI":
						//System.out.println("ORI");
						nextAddrLoc=impORI(simulationKey,decodedInstruction);
						break;
					case "XORI":
						//System.out.println("XORI");
						nextAddrLoc=impXORI(simulationKey,decodedInstruction);
						break;
			
					default:
						break;
				}
				//printTheRegisters();
				//printTheAddressValues();
				//start writing
				bufferedWriter.write("--------------------"+"\n");
				bufferedWriter.write("Cycle:"+cycleNumber+"\t"+simulationKey+"\t"+decodedInstruction+"\n");
				bufferedWriter.write("\n");
				bufferedWriter.write("Registers\n");
				for(int i=0;i<4;i++) {
					StringBuilder strBuilder = new StringBuilder();
					for(int j=i*8;j<(i+1)*8;j++) {
						strBuilder.append("\t");
						strBuilder.append(registers[j]);
					}
					bufferedWriter.write(regPrint.get(i)+strBuilder+"\n");
				}
				bufferedWriter.write("\n");
				bufferedWriter.write("Data\n");
				for(int i=startOfData;i<endOfData;) {
					StringBuilder strBuilder = new StringBuilder();
					strBuilder.append(i+":");
					int counter=8;
					while(counter>0 && i<=endOfData) {
						if(processingLocation.containsKey(i)) {
							strBuilder.append("\t"+processingLocation.get(i));
							i+=4;
							counter--;
						}
						else
							break;
					}
					bufferedWriter.write(strBuilder+"\n");
				}
				bufferedWriter.write("\n");
				//end writing
				simulationKey=nextAddrLoc;
				cycleNumber++;
			}
		//When Break Encountered
			bufferedWriter.write("--------------------"+"\n");
			bufferedWriter.write("Cycle:"+cycleNumber+"\t"+simulationKey+"\t"+processingLocation.get(simulationKey)+"\n");
			bufferedWriter.write("\n");
			bufferedWriter.write("Registers\n");
			for(int i=0;i<4;i++) {
				StringBuilder strBuilder = new StringBuilder();
				for(int j=i*8;j<(i+1)*8;j++) {
					strBuilder.append("\t");
					strBuilder.append(registers[j]);
				}
				bufferedWriter.write(regPrint.get(i)+strBuilder+"\n");
			}
			bufferedWriter.write("\n");
			bufferedWriter.write("Data\n");
			for(int i=startOfData;i<endOfData;) {
				StringBuilder strBuilder = new StringBuilder();
				strBuilder.append(i+":");
				int counter=8;
				while(counter>0 && i<=endOfData) {
					if(processingLocation.containsKey(i)) {
						strBuilder.append("\t"+processingLocation.get(i));
						i+=4;
						counter--;
					}
					else
						break;
				}
				bufferedWriter.write(strBuilder+"\n");
			}
			//bufferedWriter.write("\n");
			bufferedWriter.close();
		}
		catch (IOException e) {
			e.printStackTrace();
		}
		return true;
	}
	
	public int impJ(String decodedInstruction) {
		int nextAddr=0;
		String onlyInstruction = decodedInstruction.substring(3, 6);
		//System.out.println("Jump to "+onlyInstruction);
		nextAddr=Integer.parseInt(onlyInstruction);
		return nextAddr;
	}
	//BEQ R1, R2, #36
	public int impBEQ(int currentAddr, String decodedInstruction) {
		int nextAddr=0;
		int reg1=Integer.parseInt(decodedInstruction.substring(5, 6));
		int reg2=Integer.parseInt(decodedInstruction.substring(9, 10));
		int jumpTo=Integer.parseInt(decodedInstruction.substring(13));
		//System.out.println("reg 1= "+ reg1+ " reg2= "+reg2+" jump to "+jumpTo);
		if(registers[reg1]==registers[reg2]) {
			nextAddr = currentAddr + jumpTo + 4;
		}
		else {
			nextAddr=currentAddr+4;
		}
		return nextAddr;
	}
	//BGTZ R5, #4
	public int impBGTZ(int currentAddr, String decodedInstruction) {
		int nextAddr=0;
		int reg1=Integer.parseInt(decodedInstruction.substring(6, 7));
		int jumpTo=Integer.parseInt(decodedInstruction.substring(10));
		//System.out.println("reg 1= "+ reg1+" jump to "+jumpTo);
		if(registers[reg1]>0) {
			nextAddr = currentAddr + jumpTo + 4;
		}
		else {
			nextAddr=currentAddr+4;
		}
		return nextAddr;
	}
	//SW R5, 216(R6)
	//SW rt, offset(base)
	//memory[base+offset] <- rt
	public int impSW(int currentAddr, String decodedInstruction) {
		int nextAddr=0;
		String[] offsetBaseHelper = decodedInstruction.split("\\(");
		//System.out.println("THE first part is->"+offsetBaseHelper[0]+"<-");//take 7 as start
		//System.out.println("THE second part is->"+offsetBaseHelper[1]+"<-");//1,2
		int rt=Integer.parseInt(decodedInstruction.substring(4,5));
		//int offset=Integer.parseInt(decodedInstruction.substring(7,10));
		int offset=Integer.parseInt(offsetBaseHelper[0].substring(7));
		//int base=Integer.parseInt(decodedInstruction.substring(12,13));
		int base=Integer.parseInt(offsetBaseHelper[1].substring(1,2));
		processingLocation.put(offset+ (int) registers[base], String.valueOf(registers[rt]));
		nextAddr=currentAddr+4;
		return nextAddr;
	}
	//LW R4, 200(R6)
	//LW rt, offset(base)
	//rt <- memory[base+offset]
	public int impLW(int currentAddr, String decodedInstruction) {
		int nextAddr=0;
		String[] offsetBaseHelper = decodedInstruction.split("\\(");
		int rt=Integer.parseInt(decodedInstruction.substring(4,5));
		int offset=Integer.parseInt(offsetBaseHelper[0].substring(7));
		int base=Integer.parseInt(offsetBaseHelper[1].substring(1,2));
		//int offset=Integer.parseInt(decodedInstruction.substring(7,10));
		//int base=Integer.parseInt(decodedInstruction.substring(12,13));
		registers[rt]=Long.parseLong(processingLocation.get(offset+ (int) registers[base]));
		nextAddr=currentAddr+4;
		return nextAddr;
	}
	//ADD R1, R0, R0
	public int impADD(int currentAddr, String decodedInstruction) {
		int nextAddr=0;
		int rd=Integer.parseInt(decodedInstruction.substring(5, 6));
		int rs=Integer.parseInt(decodedInstruction.substring(9, 10));
		int rt=Integer.parseInt(decodedInstruction.substring(13));
		registers[rd]=registers[rs]+registers[rt];
		nextAddr=currentAddr+4;
		return nextAddr;
	}
	//SUB R1, R0, R0
	public int impSUB(int currentAddr, String decodedInstruction) {
		int nextAddr=0;
		int rd=Integer.parseInt(decodedInstruction.substring(5, 6));
		int rs=Integer.parseInt(decodedInstruction.substring(9, 10));
		int rt=Integer.parseInt(decodedInstruction.substring(13));
		registers[rd]=registers[rs]-registers[rt];
		nextAddr=currentAddr+4;
		return nextAddr;
	}
	//MUL R5, R3, R4
	public int impMUL(int currentAddr, String decodedInstruction) {
		int nextAddr=0;
		int rd=Integer.parseInt(decodedInstruction.substring(5, 6));
		int rs=Integer.parseInt(decodedInstruction.substring(9, 10));
		int rt=Integer.parseInt(decodedInstruction.substring(13));
		registers[rd]=registers[rs] * registers[rt];
		nextAddr=currentAddr+4;
		return nextAddr;
	}
	//AND R5, R3, R4
	public int impAND(int currentAddr, String decodedInstruction) {
		int nextAddr=0;
		int rd=Integer.parseInt(decodedInstruction.substring(5, 6));
		int rs=Integer.parseInt(decodedInstruction.substring(9, 10));
		int rt=Integer.parseInt(decodedInstruction.substring(13));
		registers[rd]=registers[rs] & registers[rt];
		nextAddr=currentAddr+4;
		return nextAddr;
	}
	
	public int impOR(int currentAddr, String decodedInstruction) {
		int nextAddr=0;
		int rd=Integer.parseInt(decodedInstruction.substring(5, 6));
		int rs=Integer.parseInt(decodedInstruction.substring(9, 10));
		int rt=Integer.parseInt(decodedInstruction.substring(13));
		registers[rd]=registers[rs] | registers[rt];
		nextAddr=currentAddr+4;
		return nextAddr;
	}
	
	public int impXOR(int currentAddr, String decodedInstruction) {
		int nextAddr=0;
		int rd=Integer.parseInt(decodedInstruction.substring(5, 6));
		int rs=Integer.parseInt(decodedInstruction.substring(9, 10));
		int rt=Integer.parseInt(decodedInstruction.substring(13));
		registers[rd]=registers[rs] ^ registers[rt];
		nextAddr=currentAddr+4;
		return nextAddr;
	}
	
	public int impNOR(int currentAddr, String decodedInstruction) {
		int nextAddr=0;
		int rd=Integer.parseInt(decodedInstruction.substring(5, 6));
		int rs=Integer.parseInt(decodedInstruction.substring(9, 10));
		int rt=Integer.parseInt(decodedInstruction.substring(13));
		registers[rd]=~(registers[rs] | registers[rt]);
		nextAddr=currentAddr+4;
		return nextAddr;
	}
	//ADDI R2, R0, #3
	public int impADDI(int currentAddr, String decodedInstruction) {
		int nextAddr=0;
		int rt=Integer.parseInt(decodedInstruction.substring(6, 7));
		int rs=Integer.parseInt(decodedInstruction.substring(10, 11));
		int immediate=Integer.parseInt(decodedInstruction.substring(14));
		registers[rt]=registers[rs] + immediate;
		nextAddr=currentAddr+4;
		return nextAddr;
	}
	//ANDI R5, R5, #12
	public int impANDI(int currentAddr, String decodedInstruction) {
		int nextAddr=0;
		int rt=Integer.parseInt(decodedInstruction.substring(6, 7));
		int rs=Integer.parseInt(decodedInstruction.substring(10, 11));
		int immediate=Integer.parseInt(decodedInstruction.substring(14));
		registers[rt]=registers[rs] & immediate;
		nextAddr=currentAddr+4;
		return nextAddr;
	}
	//ORI R5, R5, #12
	public int impORI(int currentAddr, String decodedInstruction) {
		int nextAddr=0;
		int rt=Integer.parseInt(decodedInstruction.substring(5, 6));
		int rs=Integer.parseInt(decodedInstruction.substring(9, 10));
		int immediate=Integer.parseInt(decodedInstruction.substring(13));
		registers[rt]=registers[rs] | immediate;
		nextAddr=currentAddr+4;
		return nextAddr;
	}
	//XORI R5, R5, #12
	public int impXORI(int currentAddr, String decodedInstruction) {
		int nextAddr=0;
		int rt=Integer.parseInt(decodedInstruction.substring(6, 7));
		int rs=Integer.parseInt(decodedInstruction.substring(10, 11));
		int immediate=Integer.parseInt(decodedInstruction.substring(14));
		registers[rt]=registers[rs] ^ immediate;
		nextAddr=currentAddr+4;
		return nextAddr;
	}
	
	//print the register values
	public void printTheRegisters() {
		for(int i=0; i<32;i++) {
			System.out.print("R"+i+"="+registers[i]+"\t");
		}
		System.out.println("");
	}
	//print the hash table values
	public void printTheAddressValues() {
		for(int i=184; i<=244;i+=4) {
			System.out.print(i+"="+processingLocation.get(i)+"\t");
		}
		System.out.println("");
	}
}
