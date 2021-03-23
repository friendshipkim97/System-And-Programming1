package vm;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

public class CodeWriter {
	
	FileOutputStream outFile;
	String vmFileName, currentFunctionName; // for static variable
	int labelCount, returnCount; // for label discrimination
	
	public CodeWriter(String fileName, boolean isDirectory) {
		try {
			System.out.println("codeWriter constructor : "+fileName);
			outFile = new FileOutputStream(fileName); // fileName의 옆에 path가 있어서 잘라주는 작업 필요
			if(isDirectory)
				writeInit();
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public void setFileName(String fileName) {
		File file = new File(fileName);
		vmFileName = file.getName();
		int extPos = vmFileName.indexOf(".vm");
		if(extPos>0)
			vmFileName=vmFileName.substring(0, extPos); // PATH빼고 파일이름만 vmFileName에 저장한다. 
		currentFunctionName = vmFileName; // for the case of no function declaration 
		labelCount=0;
		returnCount=0;
	}
	
	private void writeInit() {  // OS가 지원하는 기능을 약식으로 구현한 것이다. 
		try {
			String code = "// writeInit"+'\n'; outFile.write(code.getBytes());
			code="@256"+'\n'; outFile.write(code.getBytes());
			code="D=A"+'\n'; outFile.write(code.getBytes());
			code="@SP"+'\n'; outFile.write(code.getBytes());
			code="M=D"+'\n'; outFile.write(code.getBytes());
			// call Sys.init 0
			currentFunctionName = "writeInit" ; // actually, never be returned
			writeCall("Sys.init", 0); // main을 찾아서 불러주는 것 
		} catch(IOException e) {
			e.printStackTrace();
		}
		
	}
	
	public void writeArithmetic(String command) {
		
		
		try {
			String code = "// "+command+'\n';
			outFile.write(code.getBytes());
			switch(command) {

			case "add": // add는 사전에 두 개의 operand가 push되어있다는 것을 전제로 한다. 
				code="@SP"+'\n'; outFile.write(code.getBytes());
				code="AM=M-1"+'\n'; outFile.write(code.getBytes());
				code="D=M"+'\n'; outFile.write(code.getBytes());
				code="A=A-1"+'\n'; outFile.write(code.getBytes());
				code="M=D+M"+'\n'; outFile.write(code.getBytes());
				break;
		    case "sub": // sub는 사전에 두 개의 operand가 push되어있다는 것을 전제로 한다. 
			code="@SP"+'\n'; outFile.write(code.getBytes());
			code="AM=M-1"+'\n'; outFile.write(code.getBytes());
			code="D=M"+'\n'; outFile.write(code.getBytes());
			code="A=A-1"+'\n'; outFile.write(code.getBytes());
			code="M=M-D"+'\n'; outFile.write(code.getBytes());
			    break;
		    case "neg":
		    	code="@SP"+'\n'; outFile.write(code.getBytes());
		    	code="AM=M-1"+'\n'; outFile.write(code.getBytes());
		    	code="D=M"+'\n'; outFile.write(code.getBytes());
		    	code="M=-D"+'\n'; outFile.write(code.getBytes());
		    	break;
		    case "eq": // eq가 여러번 나오면 레이블이 어디로갈지 모호해짐 
		    	// R13 <- -1
		    	code="@0"+'\n'; outFile.write(code.getBytes());
		    	code="D=A"+'\n'; outFile.write(code.getBytes());
		    	code="@R13"+'\n'; outFile.write(code.getBytes());
		    	code="M=D-1"+'\n'; outFile.write(code.getBytes());

		    	// M & D=x-y, SP-- (not( SP--)--) 
		    	code="@SP"+'\n'; outFile.write(code.getBytes());
		    	code="AM=M-1"+'\n'; outFile.write(code.getBytes());
		    	code="D=M"+'\n'; outFile.write(code.getBytes());
		    	code="A=A-1"+'\n'; outFile.write(code.getBytes());
		    	code="MD=M-D"+'\n'; outFile.write(code.getBytes());

		    	// if D=0 goto END ( R13=-1 ) // true -1이다.
		    	code="@LABEL"+labelCount+'\n'; outFile.write(code.getBytes()); // 여러번 나오면 문제가 생길 수 있으므로 Label뒤에 숫자를 붙임 
		    	code="D;JEQ"+'\n'; outFile.write(code.getBytes()); // 0이면 점프 

		    	// else R13<-0 // false 는 0이다. 
		    	code="@R13"+'\n'; outFile.write(code.getBytes());
		    	code="M=M+1"+'\n'; outFile.write(code.getBytes());
		    	code="(LABEL"+labelCount+")"+'\n'; outFile.write(code.getBytes());
		    	labelCount++;
		    	code="@R13"+'\n'; outFile.write(code.getBytes());
		    	code="D=M"+'\n'; outFile.write(code.getBytes());

		    	// overwrite D to top of the stack
		    	code="@SP"+'\n'; outFile.write(code.getBytes());
		    	code="A=M-1"+'\n'; outFile.write(code.getBytes());
		    	code="M=D"+'\n'; outFile.write(code.getBytes());
		    	break;
		    case "gt": // eq가 여러번 나오면 레이블이 어디로갈지 모호해짐 
		    	// R13 <- -1
		    	code="@0"+'\n'; outFile.write(code.getBytes());
		    	code="D=A"+'\n'; outFile.write(code.getBytes());
		    	code="@R13"+'\n'; outFile.write(code.getBytes());
		    	code="M=D-1"+'\n'; outFile.write(code.getBytes());

		    	// D=x-y, SP-- (not( SP--)--) 
		    	code="@SP"+'\n'; outFile.write(code.getBytes());
		    	code="AM=M-1"+'\n'; outFile.write(code.getBytes());
		    	code="D=M"+'\n'; outFile.write(code.getBytes());
		    	code="A=A-1"+'\n'; outFile.write(code.getBytes());
		    	code="D=M-D"+'\n'; outFile.write(code.getBytes());

		    	// if D>0 goto LABEL_GT
		    	code="@LABEL"+labelCount+'\n'; outFile.write(code.getBytes());
		    	code="D;JGT"+'\n'; outFile.write(code.getBytes());

		    	// else R13<-0, R13 <- 0
		    	code="@R13"+'\n'; outFile.write(code.getBytes());
		    	code="M=M+1"+'\n'; outFile.write(code.getBytes());
		    	code="(LABEL"+labelCount+")"+'\n'; outFile.write(code.getBytes()); // D>0 이면 여기로 점프 
		    	labelCount++;
		    	code="@R13"+'\n'; outFile.write(code.getBytes());
		    	code="D=M"+'\n'; outFile.write(code.getBytes());

		    	// overwrite D to top of the stack
		    	code="@SP"+'\n'; outFile.write(code.getBytes());
		    	code="A=M-1"+'\n'; outFile.write(code.getBytes());
		    	code="M=D"+'\n'; outFile.write(code.getBytes()); // 데이터 집어넣음 -1일 수, 0일수있음  -1이면 TRUE 0이면 FALSE
		    	break;
		    case "lt": // eq가 여러번 나오면 레이블이 어디로갈지 모호해짐 
		    	// R13 <- -1
		    	code="@0"+'\n'; outFile.write(code.getBytes());
		    	code="D=A"+'\n'; outFile.write(code.getBytes());
		    	code="@R13"+'\n'; outFile.write(code.getBytes());
		    	code="M=D-1"+'\n'; outFile.write(code.getBytes());

		    	// D=x-y, SP-- (not( SP--)--) 
		    	code="@SP"+'\n'; outFile.write(code.getBytes());
		    	code="AM=M-1"+'\n'; outFile.write(code.getBytes());
		    	code="D=M"+'\n'; outFile.write(code.getBytes());
		    	code="A=A-1"+'\n'; outFile.write(code.getBytes());
		    	code="D=M-D"+'\n'; outFile.write(code.getBytes());

		    	// if D<0 goto END
		    	code="@LABEL"+labelCount+'\n'; outFile.write(code.getBytes());
		    	code="D;JLT"+'\n'; outFile.write(code.getBytes());

		    	// else D>=0, END
		    	code="@R13"+'\n'; outFile.write(code.getBytes());
		    	code="M=M+1"+'\n'; outFile.write(code.getBytes());
		    	code="(LABEL"+labelCount+")"+'\n'; outFile.write(code.getBytes());
		    	labelCount++;
		    	code="@R13"+'\n'; outFile.write(code.getBytes());
		    	code="D=M"+'\n'; outFile.write(code.getBytes());

		    	// overwrite D to top of the stack
		    	code="@SP"+'\n'; outFile.write(code.getBytes());
		    	code="A=M-1"+'\n'; outFile.write(code.getBytes());
		    	code="M=D"+'\n'; outFile.write(code.getBytes());
		    	break;
		    	
		    case "and":
		    	code="@SP"+'\n'; outFile.write(code.getBytes());
				code="AM=M-1"+'\n'; outFile.write(code.getBytes());
				code="D=M"+'\n'; outFile.write(code.getBytes());
				code="A=A-1"+'\n'; outFile.write(code.getBytes());
				code="M=D&M"+'\n'; outFile.write(code.getBytes());
				break;
		    case "or":
		    	code="@SP"+'\n'; outFile.write(code.getBytes());
				code="AM=M-1"+'\n'; outFile.write(code.getBytes());
				code="D=M"+'\n'; outFile.write(code.getBytes());
				code="A=A-1"+'\n'; outFile.write(code.getBytes());
				code="M=M|D"+'\n'; outFile.write(code.getBytes());
				break;
		    case "not":
		    	code="@SP"+'\n'; outFile.write(code.getBytes());
				code="AM=M-1"+'\n'; outFile.write(code.getBytes());
				code="D=M"+'\n'; outFile.write(code.getBytes());
				code="M=!D"+'\n'; outFile.write(code.getBytes());
				break;
			}
		}
		 catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public void writePush(VMTranslator.CommandType cType, String segment, int index) {
		String code;
		
		try {
			code = "// "+"push"+" "+segment+" "+index+'\n';
			outFile.write(code.getBytes());
		
		
		switch(segment) {
		
		case "constant":
			code="@"+index+'\n'; outFile.write(code.getBytes());
			code="D=A"+'\n'; outFile.write(code.getBytes());
			code="@SP"+'\n'; outFile.write(code.getBytes());
			code="AM=M+1"+'\n'; outFile.write(code.getBytes());
			code="A=A-1"+'\n'; outFile.write(code.getBytes());
			code="M=D"+'\n'; outFile.write(code.getBytes());
		    break;
		case "local":
			// addr = LCL+i --> D=*addr
			code="@LCL"+'\n'; outFile.write(code.getBytes());
			code="D=M"+'\n'; outFile.write(code.getBytes());
			code="@"+index+'\n'; outFile.write(code.getBytes());
			code="A=A+D"+'\n'; outFile.write(code.getBytes());
			code="D=M"+'\n'; outFile.write(code.getBytes());
			// *SP = *addr, SP++
			code="@SP"+'\n'; outFile.write(code.getBytes());
			code="AM=M+1"+'\n'; outFile.write(code.getBytes());
			code="A=A-1"+'\n'; outFile.write(code.getBytes());
			code="M=D"+'\n'; outFile.write(code.getBytes());
		    break; 
		case "argument":
			// addr = ARG+i --> D=*addr
			code="@ARG"+'\n'; outFile.write(code.getBytes());
			code="D=M"+'\n'; outFile.write(code.getBytes());
			code="@"+index+'\n'; outFile.write(code.getBytes());
			code="A=A+D"+'\n'; outFile.write(code.getBytes());
			code="D=M"+'\n'; outFile.write(code.getBytes());
			// *SP = *addr, SP++
			code="@SP"+'\n'; outFile.write(code.getBytes());
			code="AM=M+1"+'\n'; outFile.write(code.getBytes());
			code="A=A-1"+'\n'; outFile.write(code.getBytes());
			code="M=D"+'\n'; outFile.write(code.getBytes());
		    break;   
		case "this":
			// addr = ARG+i --> D=*addr
			code="@THIS"+'\n'; outFile.write(code.getBytes());
			code="D=M"+'\n'; outFile.write(code.getBytes());
			code="@"+index+'\n'; outFile.write(code.getBytes());
			code="A=A+D"+'\n'; outFile.write(code.getBytes());
			code="D=M"+'\n'; outFile.write(code.getBytes());
			// *SP = *addr, SP++
			code="@SP"+'\n'; outFile.write(code.getBytes());
			code="AM=M+1"+'\n'; outFile.write(code.getBytes());
			code="A=A-1"+'\n'; outFile.write(code.getBytes());
			code="M=D"+'\n'; outFile.write(code.getBytes());
		    break;   
		    
		case "that":
			// addr = ARG+i --> D=*addr
			code="@THAT"+'\n'; outFile.write(code.getBytes());
			code="D=M"+'\n'; outFile.write(code.getBytes());
			code="@"+index+'\n'; outFile.write(code.getBytes());
			code="A=A+D"+'\n'; outFile.write(code.getBytes());
			code="D=M"+'\n'; outFile.write(code.getBytes());
			// *SP = *addr, SP++
			code="@SP"+'\n'; outFile.write(code.getBytes());
			code="AM=M+1"+'\n'; outFile.write(code.getBytes());
			code="A=A-1"+'\n'; outFile.write(code.getBytes());
			code="M=D"+'\n'; outFile.write(code.getBytes());
		    break;   
		case "temp" :
			// temp=5 =>addr=5+i --> D=addr
			int temp=5+index;
			
			code="@"+temp+'\n'; outFile.write(code.getBytes());
			code="D=M"+'\n'; outFile.write(code.getBytes());
			// *SP = *addr, SP++
			code="@SP"+'\n'; outFile.write(code.getBytes());
			code="AM=M+1"+'\n'; outFile.write(code.getBytes());
			code="A=A-1"+'\n'; outFile.write(code.getBytes());
			code="M=D"+'\n'; outFile.write(code.getBytes());
			break;
		case "pointer" : // direct addressing
			// pointer 0 = THIS, 1 = THAT
			if(index==0) {
				code="@THIS"+'\n'; outFile.write(code.getBytes());
				code="D=M"+'\n'; outFile.write(code.getBytes());
			}
			else if(index==1) {
				code="@THAT"+'\n'; outFile.write(code.getBytes());
				code="D=M"+'\n'; outFile.write(code.getBytes());
			}
			else {
				System.out.println("Source Code Error!");
				return;
			}
			// *SP = *addr, SP++
			code="@SP"+'\n'; outFile.write(code.getBytes());
			code="AM=M+1"+'\n'; outFile.write(code.getBytes());
			code="A=A-1"+'\n'; outFile.write(code.getBytes());
			code="M=D"+'\n'; outFile.write(code.getBytes());
            break;
		case "static":
			// treat as a mem. variable
			code="@"+vmFileName+ "."+index+'\n'; outFile.write(code.getBytes()); 
			System.out.println("variable name = "+code);
			code="D=M"+'\n'; outFile.write(code.getBytes());	
			// *SP = *addr, SP++
			code="@SP"+'\n'; outFile.write(code.getBytes());
			code="AM=M+1"+'\n'; outFile.write(code.getBytes());
			code="A=A-1"+'\n'; outFile.write(code.getBytes());
			code="M=D"+'\n'; outFile.write(code.getBytes());
			break;
			
	     default:
	    	 break;
	     }
	} catch (IOException e) {
		// TODO Auto-generated catch block
		e.printStackTrace();
	}
	}
	
	private String getName(String fullName) {
		// extract "test" from "D:\~~~~\test.asm"
		int i;
		String s;
		
		i = fullName.indexOf('.');
		s="";
		int j=i-1;
		while(fullName.charAt(j)!='\\' && j>0) {
			s = fullName.charAt(j)+s;
			j--;
		}
		return s;
	}
	
	public void writePop(VMTranslator.CommandType cType, String segment, int index) {
		String code;
		
		try {
			code = "// "+"pop"+" "+segment+" "+index+'\n';
			outFile.write(code.getBytes());
		
		
		switch(segment) {
		
		case "local":
			// D <- addr=LCL+i --> 주소 임시 저장 R13
			code="@LCL"+'\n'; outFile.write(code.getBytes());
			code="D=M"+'\n'; outFile.write(code.getBytes());
			code="@"+index+'\n'; outFile.write(code.getBytes());
			code="D=D+A"+'\n'; outFile.write(code.getBytes());
			code="@R13"+'\n'; outFile.write(code.getBytes());
			code="M=D"+'\n'; outFile.write(code.getBytes());
			// *SP = *addr, SP--
			code="@SP"+'\n'; outFile.write(code.getBytes());
			code="AM=M-1"+'\n'; outFile.write(code.getBytes());
			code="D=M"+'\n'; outFile.write(code.getBytes());
			code="@R13"+'\n'; outFile.write(code.getBytes());
			code="A=M"+'\n'; outFile.write(code.getBytes());
			code="M=D"+'\n'; outFile.write(code.getBytes());
		    break; 
		case "argument":
			// D <- addr=ARG+i --> 주소 임시 저장 R13
			code="@ARG"+'\n'; outFile.write(code.getBytes());
			code="D=M"+'\n'; outFile.write(code.getBytes());
			code="@"+index+'\n'; outFile.write(code.getBytes());
			code="D=D+A"+'\n'; outFile.write(code.getBytes());
			code="@R13"+'\n'; outFile.write(code.getBytes());
			code="M=D"+'\n'; outFile.write(code.getBytes());
			// *SP = *addr, SP--
			code="@SP"+'\n'; outFile.write(code.getBytes());
			code="AM=M-1"+'\n'; outFile.write(code.getBytes());
			code="D=M"+'\n'; outFile.write(code.getBytes());
			code="@R13"+'\n'; outFile.write(code.getBytes());
			code="A=M"+'\n'; outFile.write(code.getBytes());
			code="M=D"+'\n'; outFile.write(code.getBytes());
		    break; 
		case "this":
			// D <- addr=THIS+i --> 주소 임시 저장 R13
			code="@THIS"+'\n'; outFile.write(code.getBytes());
			code="D=M"+'\n'; outFile.write(code.getBytes());
			code="@"+index+'\n'; outFile.write(code.getBytes());
			code="D=D+A"+'\n'; outFile.write(code.getBytes());
			code="@R13"+'\n'; outFile.write(code.getBytes());
			code="M=D"+'\n'; outFile.write(code.getBytes());
			// *SP = *addr, SP--
			code="@SP"+'\n'; outFile.write(code.getBytes());
			code="AM=M-1"+'\n'; outFile.write(code.getBytes());
			code="D=M"+'\n'; outFile.write(code.getBytes());
			code="@R13"+'\n'; outFile.write(code.getBytes());
			code="A=M"+'\n'; outFile.write(code.getBytes());
			code="M=D"+'\n'; outFile.write(code.getBytes());
		    break;
		case "that":
			// D <- addr=THAT+i --> 주소 임시 저장 R13
			code="@THAT"+'\n'; outFile.write(code.getBytes());
			code="D=M"+'\n'; outFile.write(code.getBytes());
			code="@"+index+'\n'; outFile.write(code.getBytes());
			code="D=D+A"+'\n'; outFile.write(code.getBytes());
			code="@R13"+'\n'; outFile.write(code.getBytes());
			code="M=D"+'\n'; outFile.write(code.getBytes());
			// *SP = *addr, SP--
			code="@SP"+'\n'; outFile.write(code.getBytes());
			code="AM=M-1"+'\n'; outFile.write(code.getBytes());
			code="D=M"+'\n'; outFile.write(code.getBytes());
			code="@R13"+'\n'; outFile.write(code.getBytes());
			code="A=M"+'\n'; outFile.write(code.getBytes());
			code="M=D"+'\n'; outFile.write(code.getBytes());
		    break; 
		case "temp" :
			// temp=5 =>addr=5+i --> addr=*SP--
			code="@SP"+'\n'; outFile.write(code.getBytes());
			code="AM=M-1"+'\n'; outFile.write(code.getBytes());
			code="D=M"+'\n'; outFile.write(code.getBytes());		
			int temp=5+index;
			
			code="@"+temp+'\n'; outFile.write(code.getBytes()); 
			code="M=D"+'\n'; outFile.write(code.getBytes()); // @RAM[temp] = data 
			break;
		case "pointer" : // direct addressing
			code="@SP"+'\n'; outFile.write(code.getBytes());
			code="AM=M-1"+'\n'; outFile.write(code.getBytes());
			code="D=M"+'\n'; outFile.write(code.getBytes());	
			// pointer 0 = THIS, 1 = THAT
			if(index==0) { 
				code="@THIS"+'\n'; outFile.write(code.getBytes());
				code="M=D"+'\n'; outFile.write(code.getBytes());
			}
			else if(index==1) {
				code="@THAT"+'\n'; outFile.write(code.getBytes());
				code="M=D"+'\n'; outFile.write(code.getBytes());
			}
			else {
				System.out.println("Source Code Error!");
				return;
			}
            break;
		case "static":
			// treat as a mem. variable
			code="@SP"+'\n'; outFile.write(code.getBytes());
			code="AM=M-1"+'\n'; outFile.write(code.getBytes());
			code="D=M"+'\n'; outFile.write(code.getBytes());	
			// treat as a mem. variable
			code="@"+vmFileName+ "."+index+'\n'; outFile.write(code.getBytes()); 
			System.out.println("variable name = "+code);
			code="M=D"+'\n'; outFile.write(code.getBytes());
			break;
			
	     default:
	    	 break;
		}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public void writeLabel(String label) {
		String code;
		
		try {
			code = "// label " +label+'\n'; outFile.write(code.getBytes());
			code = "("+currentFunctionName+"$"+label+")"+'\n'; outFile.write(code.getBytes());
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
	public void writeGoto(String label) {
		String code;
		try {
			code = "// "+"goto "+label+'\n';
			outFile.write(code.getBytes());
			
			code="@"+currentFunctionName+"$"+label+'\n'; outFile.write(code.getBytes());
			code= "0;JMP" +'\n'; outFile.write(code.getBytes()); // unconditional jump
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	public void writeIf(String label) {
		String code;
		try {
			code = "// writeIf : if-goto "+label+'\n'; // 조건을 push 해놓고 if-goto를 부르기로 약속 되어있는 것이다. true는 -1로 표현 false는 10진수로 0 2의보수를 이용하기 때문
			outFile.write(code.getBytes());
			code="@SP"+'\n'; outFile.write(code.getBytes());
			code="AM=M-1" +'\n'; outFile.write(code.getBytes());
			code="D=M" +'\n'; outFile.write(code.getBytes());
			code="@0" +'\n'; outFile.write(code.getBytes());
			code="D=D-A" +'\n'; outFile.write(code.getBytes());
			code="@"+currentFunctionName+"$"+label+'\n'; outFile.write(code.getBytes());
			code="D;JNE" +'\n'; outFile.write(code.getBytes()); // D값을 보고 0이 아니면 점프를 해라 0이 FALSE이므로, 위의 레이블로 
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public void writeFunction(String functionName, int numVar) { // numVar은 local variable의 개수이다. 
		String code;
		try {
			code ="// writeFunction : "+ functionName + '\n'; outFile.write(code.getBytes());
			code ="("+functionName+")"+'\n'; outFile.write(code.getBytes());
			code ="// set Local variables to 0 "+'\n'; outFile.write(code.getBytes());
			code ="@0"+'\n'; outFile.write(code.getBytes());
			code ="D=A"+'\n'; outFile.write(code.getBytes());
			for(int i=0; i<numVar; i++) { // 변수의 개수만큼 반복, 0을 변수의 개수만큼 넣어주는 것이다. 
				code = "@SP"+'\n'; outFile.write(code.getBytes());
				code = "AM=M+1"+'\n'; outFile.write(code.getBytes());
				code = "A=A-1"+'\n'; outFile.write(code.getBytes());
				code = "M=D"+'\n'; outFile.write(code.getBytes());
			}
			currentFunctionName = functionName;
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}

	public void writeCall(String functionName, int numArg) {
		String code;
		try {
			code ="// push return-address "+'\n'; outFile.write(code.getBytes());
			String retLabel = currentFunctionName+"$ret."+returnCount;
			returnCount++;
			code = "@"+retLabel+'\n'; outFile.write(code.getBytes());
			code = "D=A"+'\n'; outFile.write(code.getBytes());
			code = "@SP"+'\n'; outFile.write(code.getBytes());
			code = "AM=M+1"+'\n'; outFile.write(code.getBytes());
			code = "A=A-1"+'\n'; outFile.write(code.getBytes());
			code = "M=D"+'\n'; outFile.write(code.getBytes());
			saveFrame("LCL");
			saveFrame("ARG");
			saveFrame("THIS");
			saveFrame("THAT");
			code = "// ARG = SP-n-5 "+ '\n'; outFile.write(code.getBytes()); // ARG가 시작되는 위치 설정 
			code = "@"+numArg+'\n'; outFile.write(code.getBytes());
			code = "D=A"+'\n'; outFile.write(code.getBytes());
			code = "@5"+'\n'; outFile.write(code.getBytes());
			code = "D=D+A"+'\n'; outFile.write(code.getBytes());
			code = "@SP"+'\n'; outFile.write(code.getBytes());
			code = "D=M-D"+'\n'; outFile.write(code.getBytes());
			code = "@ARG"+'\n'; outFile.write(code.getBytes());
			code = "M=D"+'\n'; outFile.write(code.getBytes());
			code = "// LCL = SP "+'\n'; outFile.write(code.getBytes()); // LCL 
			code = "@SP"+'\n'; outFile.write(code.getBytes());
			code = "D=M"+'\n'; outFile.write(code.getBytes());
			code = "@LCL"+'\n'; outFile.write(code.getBytes());
			code = "M=D"+'\n'; outFile.write(code.getBytes());
			code = "// goto f "+'\n'; outFile.write(code.getBytes());
			code = "@"+functionName+'\n'; outFile.write(code.getBytes()); // functionName으로 가는 것
			code = "0; JMP"+'\n'; outFile.write(code.getBytes());  // unconditional jump
			// callStack.push(functionName); xxx
			code = "("+retLabel+")"+'\n'; outFile.write(code.getBytes()); // return label 돌아올 리턴label
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		
	}
	
	private void saveFrame(String string) {
		String code;
		try {
			code = "// push "+string+'\n'; outFile.write(code.getBytes());
			code = "@"+string+'\n'; outFile.write(code.getBytes());
			code = "D=M"+'\n'; outFile.write(code.getBytes());
			code = "@SP"+'\n'; outFile.write(code.getBytes());
			code = "AM=M+1"+'\n'; outFile.write(code.getBytes());
			code = "A=A-1"+'\n'; outFile.write(code.getBytes());
			code = "M=D"+'\n'; outFile.write(code.getBytes());
		}catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public void writeReturn() {
		String code;
		try {
			code = "// R13=LCL, R14=return address"+'\n'; outFile.write(code.getBytes()); // D레지스터를 써야 하므로 R13과 R14에 값을 미리저장해두었다. 
			code = "@LCL"+'\n'; outFile.write(code.getBytes()); // 특정 메모리의 값을 다른 메모리의 값에 저장 
			code = "D=M"+'\n'; outFile.write(code.getBytes());
			code = "@R13"+'\n'; outFile.write(code.getBytes());
			code = "M=D"+'\n'; outFile.write(code.getBytes());
			code = "@5"+'\n'; outFile.write(code.getBytes()); // return address
			code = "A=D-A"+'\n'; outFile.write(code.getBytes());
			code = "D=M"+'\n'; outFile.write(code.getBytes());
			code = "@14"+'\n'; outFile.write(code.getBytes());
			code = "M=D"+'\n'; outFile.write(code.getBytes());
			
			code = "// ARG[0]=return value"+'\n'; outFile.write(code.getBytes()); // return value는 스택의 맨 끝에 있다. 
			code = "@SP"+'\n'; outFile.write(code.getBytes()); // pop D
			code = "AM=M-1"+'\n'; outFile.write(code.getBytes());
			code = "D=M"+'\n'; outFile.write(code.getBytes());
			code = "@ARG"+'\n'; outFile.write(code.getBytes()); // ARG가 가리키는 곳에 D를 써줌 
			code = "A=M"+'\n'; outFile.write(code.getBytes());
			code = "M=D"+'\n'; outFile.write(code.getBytes());
			
			code = "// SP=ARG+1"+'\n'; outFile.write(code.getBytes()); // caller로 갔을 때 스택 포인터를 잡아주는 것 
			code = "@ARG"+'\n'; outFile.write(code.getBytes());
			code = "D=M"+'\n'; outFile.write(code.getBytes());
			code = "D=D+1"+'\n'; outFile.write(code.getBytes());
			code = "@SP"+'\n'; outFile.write(code.getBytes());
			code = "M=D"+'\n'; outFile.write(code.getBytes());
			
			code="// restore Frame "+'\n'; outFile.write(code.getBytes());
			restoreFrame("THAT");
			restoreFrame("THIS");
			restoreFrame("ARG");
			restoreFrame("LCL");
			
			code = "@R14"+'\n'; outFile.write(code.getBytes());
			code = "A=M"+'\n'; outFile.write(code.getBytes()); // indirect Addressing
			code = "0; JMP"+'\n'; outFile.write(code.getBytes()); // unconditional jump
			
		}catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}

	private void restoreFrame(String string) {
		String code;
		try {
			code = "@R13"+'\n'; outFile.write(code.getBytes());
			code = "AM=M-1"+'\n'; outFile.write(code.getBytes());
			code = "D=M"+'\n'; outFile.write(code.getBytes());
			code = "@"+string+'\n'; outFile.write(code.getBytes());
			code = "M=D"+'\n'; outFile.write(code.getBytes());
		}catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
	
}
