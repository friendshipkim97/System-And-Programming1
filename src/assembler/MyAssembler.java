package assembler;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Scanner;
import java.util.Set;

public class MyAssembler {
	static HashMap<String, Integer> symbolTable;
	static int newSymbolValue;
	static HashMap<String, String> compTable;
	static HashMap<String, String> destTable;
	static HashMap<String, String> jumpTable;

	public static void main(String[] args) throws IOException{ // argument run-run configuration 에서 설정해놓은 2개의 경로 
		MyAssembler asm = new MyAssembler();

		asm.init();
		asm.showsymbolTable();

		asm.pass1(args[0]); // 경로 1 .asm
		asm.showsymbolTable();
		asm.pass2(args[0], args[1]); // 경로1, 경로2  .asm , .hack

	}

	private void init() {
		symbolTable = new HashMap<>();
		newSymbolValue = 16;

		// symbol table
		symbolTable.put("R0", 0);
		symbolTable.put("R1", 1);
		symbolTable.put("R2", 2);
		symbolTable.put("R3", 3);
		symbolTable.put("R4", 4);
		symbolTable.put("R5", 5);
		symbolTable.put("R6", 6);
		symbolTable.put("R7", 7);
		symbolTable.put("R8", 8);
		symbolTable.put("R9", 9);
		symbolTable.put("R10", 10);
		symbolTable.put("R11", 11);
		symbolTable.put("R12", 12);
		symbolTable.put("R13", 13);
		symbolTable.put("R14", 14);
		symbolTable.put("R15", 15);
		symbolTable.put("SCREEN", 16384);
		symbolTable.put("KBD", 24576);
		symbolTable.put("SP", 0);
		symbolTable.put("LCL", 1);
		symbolTable.put("ARG", 2);
		symbolTable.put("THIS", 3);
		symbolTable.put("THAT", 4);

		compTable = new HashMap<>();

		// computation, a=0
		compTable.put("0", "0101010");
		compTable.put("1", "0111111");
		compTable.put("-1", "0111010");
		compTable.put("D", "0001100");
		compTable.put("A", "0110000");
		compTable.put("!D", "0001101");
		compTable.put("!A", "0110001");
		compTable.put("-D", "0001111");
		compTable.put("-A", "0110011");
		compTable.put("D+1", "0011111");
		compTable.put("A+1", "0110111");
		compTable.put("D-1", "0001110");
		compTable.put("A-1", "0110010");
		compTable.put("D+A", "0000010");
		compTable.put("D-A", "0010011");
		compTable.put("A-D", "0000111");
		compTable.put("D&A", "0000000");
		compTable.put("D|A", "0010101");

		// comp, a=1
		compTable.put("M", "1110000");
		compTable.put("!M", "1110001");
		compTable.put("-M", "1110011");
		compTable.put("M+1", "1110111");
		compTable.put("M-1", "1110010");
		compTable.put("D+M", "1000010");
		compTable.put("D-M", "1010011");
		compTable.put("M-D", "1000111");
		compTable.put("D&M", "1000000");
		compTable.put("D|M", "1010101");

		destTable = new HashMap<>();

		// destination
		destTable.put("DESTNULL", "000");
		destTable.put("M", "001");
		destTable.put("D", "010");
		destTable.put("MD", "011");
		destTable.put("A", "100");
		destTable.put("AM", "101");
		destTable.put("AD", "110");
		destTable.put("AMD", "111");

		jumpTable = new HashMap<>();

		// jump
		jumpTable.put("JUMPNULL", "000");
		jumpTable.put("JGT", "001");
		jumpTable.put("JEQ", "010");
		jumpTable.put("JGE", "011");
		jumpTable.put("JLT", "100");
		jumpTable.put("JNE", "101");
		jumpTable.put("JLE", "110");
		jumpTable.put("JMP", "111");

	}

	private void pass1(String inputFile) throws IOException {
		String inLine;

		try {
			Scanner inFile = new Scanner(new File(inputFile));  // 파일을 읽어들이는 과정 
			FileOutputStream pass1 = new FileOutputStream("File.temp.asm");

			int lineNum=0;
			while(inFile.hasNext()) {
				inLine = inFile.nextLine(); // 한 줄씩 읽는 과정 
				inLine = removeComment(inLine); // comment 제거 
				inLine=inLine.trim(); // 읽은 한 줄의 왼쪽 공백, 오른쪽 공백 제거 

				if(inLine.length()>0) {
					if(inLine.charAt(0)=='(') {

						String label=getLabel(inLine.substring(1, inLine.length()));
						if(label==null || label==" " || label=="" || symbolTable.containsKey(label)) {
							return;
						}
						symbolTable.put(label, lineNum);
					}
					else { // '('올 포함하지 않는것 
						inLine += '\n';
						pass1.write(inLine.getBytes());
						lineNum++;
					}
				}
			}

			inFile.close();
			pass1.close();

		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	private void pass2(String string, String string2) throws IOException {
		String inLine;

		try {
			@SuppressWarnings("resource")
			Scanner inFile = new Scanner(new File("temp.asm")); // temp.asm이라는 파일을 읽음 
			@SuppressWarnings("resource")
			FileOutputStream code = new FileOutputStream(string2); // 파일의 내용을 작성  string2에 저장하는 것이다.

			while (inFile.hasNext()) {
				inLine=inFile.nextLine(); // 파일의 내용을 한 줄씩 읽어서 inLine에 넣는다.
				inLine=inLine.trim(); // 읽은 한 줄의 왼쪽 공백, 오른쪽 공백 제거 


				if(inLine.charAt(0)=='@') { // 인덱스위치에 해당하는 문자를 추출한다. 처음자리부터이므로 0번째가 '@'인지 확인, Addressing instruction
					int value = getValue(inLine.substring(1, inLine.length())); // substring은 문자열 잘라내는 메소드 
					String codeALine = "0"+binaryString15(value); // 2진수 0과 2진수로 이루어진 15비트를 합쳐서 a-instruction 만듬.
					codeALine += '\n';
					System.out.println(inLine+": code= "+codeALine);
					code.write(codeALine.getBytes()); // String을 바이트코드로 인코딩 해주는 메소드
				}
				else { // Compute instruction

					String cInstHead = "111";
					String cInstDest, cInstComp, cInstJump;

					String dest=getDest(inLine);
					if(dest==null) 
						dest="DESTNULL";
					cInstDest = destTable.get(dest);
					System.out.println(inLine+":"+dest+" "+cInstDest);

					String comp = getComp(inLine);
					cInstComp = compTable.get(comp);
					System.out.println(inLine+":"+comp+" "+cInstComp);

					String jump = getJump(inLine);
					if(jump==null) 
						jump="JUMPNULL";
					cInstJump = jumpTable.get(jump);
					System.out.println(inLine+":"+jump+" "+cInstJump);

					if(cInstDest==null || cInstComp==null || cInstJump==null) {
						System.out.println("Error: C-instruction cannot be mapped");
						return;
					}
					cInstHead = cInstHead+cInstComp+cInstDest+cInstJump+'\n'; // 하나의 c-instruction으로 만드는 과정 
					System.out.println(inLine+": code= "+cInstHead);
					code.write(cInstHead.getBytes());
				}

			}
			inFile.close();
			code.close();
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}


	private String getJump(String str) {
		int destcomp = str.indexOf(';'); // indexOf메서든 ';'의 위치를 찾아서 반환. , 찾지 못하면 -1을 반환함.
		if(destcomp==-1)  // 찾지 못했을 경우
			return null;
		else {
			String jumpString = str.substring(destcomp+1, str.length());
			return jumpString.trim(); // 왼쪽 공백, 오른쪽 공백 제거해서 반환 ex) JGT, JEQ ...
		}
	}

	private String getComp(String str) { // syntax dest = comp ; jump 이므로 '=', ';' 다 찾아야 함 
		int dest = str.indexOf('=');
		if(dest==-1) // dest==-1이라는 건 '='을 찾지 못했을 경우 
			dest=0;
		else
			dest++; // '='의 위치 다음부터 
		int destcomp = str.indexOf(';'); // dest = comp ; 에서 ;의 위치 반환 

		if(destcomp==-1) // destcomp==-1이라는 건 ';'을 찾지 못했을 경우 
			destcomp=str.length(); // ';' 찾지 못했을 경우 문자열의 길이를 반환 
		String compString = str.substring(dest, destcomp);
		return compString.trim();
	}

	private String getDest(String str) {
		int dest = str.indexOf('=');
		if(dest==-1) // dest==-1이라는 건 '='을 찾지 못했을 경우 
			return null;
		else {
			String destString = str.substring(0, dest);
			return destString;
		}
	}

	private int getValue(String str) {
		int temp = 0;
		if(str.charAt(0)<='9' && str.charAt(0)>='0') { // a-instruction의 @이후 찾기 
			temp = Integer.parseInt(str);
			return temp;
		}
		else { // 숫자가 아닐 경우 
			if(str!="" && str!=" " && !symbolTable.containsKey(str)) { // 공백이 아니거나 미리정해둔 symbol이 아니라면 새로 주소를 지정해줘야함.
				symbolTable.put(str,newSymbolValue);
				temp=newSymbolValue; // 16번지
				newSymbolValue++; // 16번지에서 1번지씩 증가해 나간다. 
			}
			else { 
				temp = symbolTable.get(str); // 미리 정해둔 symbol인 경우 
			}
			return temp;
		}
	}

	public  String binaryString15(int temp) { // 2진수로 이루어진 15비트를 합친다.
		String str = Integer.toBinaryString(temp);
		while(str.length()!=15) {
			str ='0'+str;
		}

		return str;
	}

	private static String getLabel(String str) {
		if(str.length()==0)
			return null;
		int temp = str.indexOf(')');
		String result = str.substring(0, temp);
		return result;	

	}


	private String removeComment(String str) {
		if(str.length()==0)
			return str;
		if(str.indexOf("//")==-1) {
			return str;
		}
		else if(str.indexOf("//")==0){
			return "";
		}
		else {
			int a = str.indexOf("//");
			String temp = str.substring(0, str.indexOf("//"));
			return temp;
		}
	}

	public void showsymbolTable() {
		Set<String> key = symbolTable.keySet();
		System.out.println("<< Symbol Table >>");
		Iterator<String> keys = symbolTable.keySet().iterator();
		while(keys.hasNext()) {
			String k = keys.next();
			System.out.println(k+"  "+symbolTable.get(k));
		}
	}

}
