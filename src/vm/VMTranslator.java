package vm;

import java.io.File;
import java.util.ArrayList;

public class VMTranslator {
	
	static enum CommandType{
		C_ARITHMETIC, C_PUSH, C_POP, C_LABEL, C_GOTO, C_IF, C_FUNCTION, C_RETURN, C_CALL;
	}
	
	static String currentCommand;
	
	public static void main(String[] args) { 
		File file = new File(args[0]);
		String inFileName, outFileName;
		
		if(file.isDirectory()) { // ������ ���丮���� Ȯ�� 
			outFileName = args[0]+'\\'+file.getName()+".asm"; // "//"����� �������ϳ� �� �����ִ� ���丮�� ���������丮���������ͼ� .asm�θ��� 
			System.out.println("Directory case : "+outFileName);
		}
		else { // ���丮�� �ƴϸ� .vm�̶�� �Ҹ��̰� .vm ���� .asm�� ���δ�. 
			int extPos = args[0].lastIndexOf(".");
			outFileName = args[0].substring(0, extPos)+".asm";
			System.out.println("file case : "+outFileName);
		}
		
		System.out.println("Target File Name : "+outFileName); 
		CodeWriter myCW = new CodeWriter(outFileName, file.isDirectory()); 
		
		ArrayList<String> vmlist = getInputFiles(args[0]);
		int inFileNum = 0;
		while(inFileNum<vmlist.size()) { // file�� ������ŭ ����. 
			inFileName=vmlist.get(inFileNum);
			inFileNum++;
			System.out.println("Source File Name : " +inFileName);
			myCW.setFileName(inFileName);
			Parser myP = new Parser(inFileName);
			while(myP.hasMoreCommands()) {
				myP.advance();
				CommandType myType = myP.commandType();
				if(myType==null) {
					System.out.println("Wrong command is given..  ");
					break;
				}
	
			
			switch(myType) {
			
			case C_ARITHMETIC:
				myCW.writeArithmetic(myP.arg1());
				break;
			case C_PUSH:
				myCW.writePush(myType,myP.arg1(),myP.arg2());
				break;
			case C_POP:
				myCW.writePop(myType,myP.arg1(),myP.arg2());
				break;
			case C_LABEL:
				myCW.writeLabel(myP.arg1());
				break;
			case C_GOTO:
				myCW.writeGoto(myP.arg1());
				break;
			case C_IF:
				myCW.writeIf(myP.arg1());
				break;
			case C_FUNCTION:
				myCW.writeFunction(myP.arg1(), myP.arg2());
				break;
			case C_CALL:
				myCW.writeCall(myP.arg1(), myP.arg2());
				break;
			case C_RETURN:
				myCW.writeReturn();
				break;
			
			default: // includes comment line with ret.val=null
		        break;

			}
		}
		}
		//myP.inFile.close();	
	}
	
	private static ArrayList<String> getInputFiles(String string){ // ArrayList �ݳ� 
		ArrayList<String> result = new ArrayList<>();
		File file = new File(string);
		String [] list;
		if(file.isDirectory()) {
			list=file.list(); // list�� ��̸� ������ش�. 
			for(int i=0; i<list.length; i++) {
				int extPos = list[i].indexOf(".vm"); //.vm�ΰ͸� ã�Ƴ��� list�� �߰��Ѵ�. 
				if(extPos>0) {
					list[i] = file.getPath()+'\\'+list[i];
					System.out.println("getInput..Source File Name : "+list[i]);
					result.add(list[i]);
				}
			}
		}
		else {
			result.add(string);
		}
		return result;
	}

}
