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
		
		if(file.isDirectory()) { // 파일이 디렉토리인지 확인 
			outFileName = args[0]+'\\'+file.getName()+".asm"; // "//"해줘야 슬래시하나 들어감 원래있는 디렉토리에 마지막디렉토리명을가져와서 .asm로만듬 
			System.out.println("Directory case : "+outFileName);
		}
		else { // 디렉토리가 아니면 .vm이라는 소리이고 .vm 떼고 .asm을 붙인다. 
			int extPos = args[0].lastIndexOf(".");
			outFileName = args[0].substring(0, extPos)+".asm";
			System.out.println("file case : "+outFileName);
		}
		
		System.out.println("Target File Name : "+outFileName); 
		CodeWriter myCW = new CodeWriter(outFileName, file.isDirectory()); 
		
		ArrayList<String> vmlist = getInputFiles(args[0]);
		int inFileNum = 0;
		while(inFileNum<vmlist.size()) { // file의 개수만큼 돈다. 
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
	
	private static ArrayList<String> getInputFiles(String string){ // ArrayList 반납 
		ArrayList<String> result = new ArrayList<>();
		File file = new File(string);
		String [] list;
		if(file.isDirectory()) {
			list=file.list(); // list로 어레이를 만들어준다. 
			for(int i=0; i<list.length; i++) {
				int extPos = list[i].indexOf(".vm"); //.vm인것만 찾아내서 list에 추가한다. 
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
