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

	public static void main(String[] args) throws IOException{ // argument run-run configuration ���� �����س��� 2���� ��� 
		MyAssembler asm = new MyAssembler();

		asm.init();
		asm.showsymbolTable();

		asm.pass1(args[0]); // ��� 1 .asm
		asm.showsymbolTable();
		asm.pass2(args[0], args[1]); // ���1, ���2  .asm , .hack

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
			Scanner inFile = new Scanner(new File(inputFile));  // ������ �о���̴� ���� 
			FileOutputStream pass1 = new FileOutputStream("File.temp.asm");

			int lineNum=0;
			while(inFile.hasNext()) {
				inLine = inFile.nextLine(); // �� �پ� �д� ���� 
				inLine = removeComment(inLine); // comment ���� 
				inLine=inLine.trim(); // ���� �� ���� ���� ����, ������ ���� ���� 

				if(inLine.length()>0) {
					if(inLine.charAt(0)=='(') {

						String label=getLabel(inLine.substring(1, inLine.length()));
						if(label==null || label==" " || label=="" || symbolTable.containsKey(label)) {
							return;
						}
						symbolTable.put(label, lineNum);
					}
					else { // '('�� �������� �ʴ°� 
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
			Scanner inFile = new Scanner(new File("temp.asm")); // temp.asm�̶�� ������ ���� 
			@SuppressWarnings("resource")
			FileOutputStream code = new FileOutputStream(string2); // ������ ������ �ۼ�  string2�� �����ϴ� ���̴�.

			while (inFile.hasNext()) {
				inLine=inFile.nextLine(); // ������ ������ �� �پ� �о inLine�� �ִ´�.
				inLine=inLine.trim(); // ���� �� ���� ���� ����, ������ ���� ���� 


				if(inLine.charAt(0)=='@') { // �ε�����ġ�� �ش��ϴ� ���ڸ� �����Ѵ�. ó���ڸ������̹Ƿ� 0��°�� '@'���� Ȯ��, Addressing instruction
					int value = getValue(inLine.substring(1, inLine.length())); // substring�� ���ڿ� �߶󳻴� �޼ҵ� 
					String codeALine = "0"+binaryString15(value); // 2���� 0�� 2������ �̷���� 15��Ʈ�� ���ļ� a-instruction ����.
					codeALine += '\n';
					System.out.println(inLine+": code= "+codeALine);
					code.write(codeALine.getBytes()); // String�� ����Ʈ�ڵ�� ���ڵ� ���ִ� �޼ҵ�
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
					cInstHead = cInstHead+cInstComp+cInstDest+cInstJump+'\n'; // �ϳ��� c-instruction���� ����� ���� 
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
		int destcomp = str.indexOf(';'); // indexOf�޼��� ';'�� ��ġ�� ã�Ƽ� ��ȯ. , ã�� ���ϸ� -1�� ��ȯ��.
		if(destcomp==-1)  // ã�� ������ ���
			return null;
		else {
			String jumpString = str.substring(destcomp+1, str.length());
			return jumpString.trim(); // ���� ����, ������ ���� �����ؼ� ��ȯ ex) JGT, JEQ ...
		}
	}

	private String getComp(String str) { // syntax dest = comp ; jump �̹Ƿ� '=', ';' �� ã�ƾ� �� 
		int dest = str.indexOf('=');
		if(dest==-1) // dest==-1�̶�� �� '='�� ã�� ������ ��� 
			dest=0;
		else
			dest++; // '='�� ��ġ �������� 
		int destcomp = str.indexOf(';'); // dest = comp ; ���� ;�� ��ġ ��ȯ 

		if(destcomp==-1) // destcomp==-1�̶�� �� ';'�� ã�� ������ ��� 
			destcomp=str.length(); // ';' ã�� ������ ��� ���ڿ��� ���̸� ��ȯ 
		String compString = str.substring(dest, destcomp);
		return compString.trim();
	}

	private String getDest(String str) {
		int dest = str.indexOf('=');
		if(dest==-1) // dest==-1�̶�� �� '='�� ã�� ������ ��� 
			return null;
		else {
			String destString = str.substring(0, dest);
			return destString;
		}
	}

	private int getValue(String str) {
		int temp = 0;
		if(str.charAt(0)<='9' && str.charAt(0)>='0') { // a-instruction�� @���� ã�� 
			temp = Integer.parseInt(str);
			return temp;
		}
		else { // ���ڰ� �ƴ� ��� 
			if(str!="" && str!=" " && !symbolTable.containsKey(str)) { // ������ �ƴϰų� �̸����ص� symbol�� �ƴ϶�� ���� �ּҸ� �����������.
				symbolTable.put(str,newSymbolValue);
				temp=newSymbolValue; // 16����
				newSymbolValue++; // 16�������� 1������ ������ ������. 
			}
			else { 
				temp = symbolTable.get(str); // �̸� ���ص� symbol�� ��� 
			}
			return temp;
		}
	}

	public  String binaryString15(int temp) { // 2������ �̷���� 15��Ʈ�� ��ģ��.
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
