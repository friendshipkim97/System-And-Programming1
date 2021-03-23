package vm;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.Scanner;

public class Parser {
	
	Scanner inFile;
	String currentLine;
	String [] token = {"", "", ""};
	int numberOfToken;
	
	public Parser(String fileName) {
		
			try {
				inFile = new Scanner(new File(fileName));
			} catch (FileNotFoundException e) {		
				e.printStackTrace();
			}
			//System.out.println("읽은거확인");
	}
	
	public boolean hasMoreCommands() {
		  System.out.print("hasMoreCommands-->");
		  while (inFile.hasNext()) {
		   currentLine = inFile.nextLine().trim();
		   int p=0;
		   while (p<currentLine.length()) {
		    if (currentLine.charAt(p)=='/')
		     break;
		    else {
		     System.out.println(currentLine);
		     return true;
		    }
		   }
		  }
		  return false;
		 }
	
	public void advance() { // 읽어논 currentLine을 가지고 
		System.out.println("Advance -->");
		int position=0;
		token[0]="";token[1]="";token[2]="";
		numberOfToken = 0;
		while(numberOfToken<3 && position<currentLine.length()) {
			if(currentLine.charAt(position)!=' ' && currentLine.charAt(position)!='\t') {
			token[numberOfToken] += currentLine.charAt(position);
			position++;
		   }
		else {
			numberOfToken++;
			position++;
		}
	}

	}
	
	public VMTranslator.CommandType commandType(){
		System.out.print("commandType --> ");
		switch(token[0]) {
		
		case "add" :
		case "sub" :
		case "neg" :
		case "eq" :
		case "gt" :
		case "lt" :
		case "and" :
		case "or" :
		case "not" :
			token[1]=token[0];
			return VMTranslator.CommandType.C_ARITHMETIC;
		case "push" :
			return  VMTranslator.CommandType.C_PUSH;
		case "pop" :
			return  VMTranslator.CommandType.C_POP;
		case "label" :
			return  VMTranslator.CommandType.C_LABEL;
		case "goto" :
			return  VMTranslator.CommandType.C_GOTO;
		case "if-goto" :
			return  VMTranslator.CommandType.C_IF;
		case "function" :
			return  VMTranslator.CommandType.C_FUNCTION;
		case "return" :
			return  VMTranslator.CommandType.C_RETURN;
		case "call" :
			return  VMTranslator.CommandType.C_CALL;
		default:
			return null;
			
		}
		
	}
	
	public String arg1() {
		return token[1];
	}
	
	public int arg2() {
		return getValue(token[2]);
	}
	
	private int getValue(String str) { // String으로 들어온 걸 숫자로 바꿔준다. 
		
		int temp = Integer.parseInt(str);
		return temp;
	}

}
