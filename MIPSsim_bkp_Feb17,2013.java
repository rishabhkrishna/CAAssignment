import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;


public class MIPSsim {
	
	boolean isBreakYet = false;
	ArrayList<String> inputFile = new ArrayList<String>();
	ArrayList<Long> inputFileAsNum = new ArrayList<Long>();
	ArrayList<Integer> categoryIndex = new ArrayList<Integer>();
	ArrayList<String> instructions = new ArrayList<String>();
	HashMap<Integer,Integer> category = new HashMap<Integer,Integer>();
	HashMap<Integer,String> categoryOne = new HashMap<Integer,String>();
	HashMap<Integer,String> categoryTwo = new HashMap<Integer,String>();
	HashMap<Integer,String> categoryThree = new HashMap<Integer,String>();
	
	/*Constructor*/
	MIPSsim() {
		
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
		
		//to get OpCodes of Category 2
		categoryTwo.put(0,"ADD");
		categoryTwo.put(1,"SUB");
		categoryTwo.put(2,"MUL");
		categoryTwo.put(3,"AND");
		categoryTwo.put(4,"OR");
		categoryTwo.put(5,"XOR");
		categoryTwo.put(6,"NOR");
		
		//to get OpCodes of Category 3
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
			int addLoc=128;
			for(int i=0;i<inputFile.size();i++,addLoc+=4) {
				bufferedWriter.write(inputFile.get(i)+"\t"+addLoc+"\t"+instructions.get(i));
				bufferedWriter.newLine();
			}
			//end writing
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
						strBldr = decodeCategory1(inputFileAsNum.get(i));	
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
	
	public String decodeCategory1(Long insNumber) {
		String strBldr = new String();
		int opcode = (int) ((insNumber>>26) & 7);
		if(categoryOne.get(opcode).equalsIgnoreCase("BREAK")) {
			isBreakYet = true;
			strBldr=categoryOne.get(opcode);
		}
		else {
			switch (categoryOne.get(opcode)) {
			case "J":
				strBldr=caseJ(insNumber);
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

	private String caseJ(Long insNumber) {
		String strJ = new String();
		int addr = (int) (insNumber & 0b11111111111111111111111111);
		addr<<=2;
		strJ="J #"+addr;
		return strJ;
	}

	public String decodeCategory2(Long insNumber) {
		String strBldr = new String();
		int rs = (int) ((insNumber>>24) & 31);
		int rt = (int) ((insNumber>>19) & 31);
		int rd = (int) ((insNumber>>11) & 31);
		int opcode = (int) ((insNumber>>16) & 7);
		strBldr=categoryTwo.get(opcode)+" R"+rd+", R"+rt+", R"+rs;
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

}
