package cat.udl.easymodel.utils.buffer;

import cat.udl.easymodel.main.SharedData;

public class NotebookMathBuffer {
	private StringBuffer buf;
	private String newLineChar = SharedData.getInstance().getNewLine();
	private String header = "(* Content-type: application/vnd.wolfram.mathematica *)"+newLineChar + 
			newLineChar + 
			"(*** Wolfram Notebook File ***)"+newLineChar + 
			"(* http://www.wolfram.com/nb *)"+newLineChar + 
			newLineChar + 
			"(* CreatedBy='"+SharedData.fullAppName+"' *)"+newLineChar + 
			newLineChar + 
			"(* Internal cache information:"+newLineChar + 
			"NotebookFileLineBreakTest"+newLineChar + 
			"NotebookFileLineBreakTest"+newLineChar + 
			"DialogFrame->Normal*)"+newLineChar + 
			newLineChar + 
			"(* Beginning of Notebook Content *)"+newLineChar + 
			"Notebook[{"+newLineChar;
	private String footer = "},"+newLineChar + 
			"DialogSize->{763, 711},"+newLineChar + 
			"DialogMargins->{{Automatic, 301}, {-10, Automatic}},"+newLineChar + 
			"StyleDefinitions->\"Default.nb\""+newLineChar + 
			"]"+newLineChar + 
			"(* End of Notebook Content *)"+newLineChar;
	
	public NotebookMathBuffer() {
		buf = new StringBuffer();
		buf.append(header);
	}
	
	public void reset() {
		buf.delete(header.length(), buf.length());
	}
	
	public void addCommand(String com) {
		buf.append("Cell[\"\\<"+com.replaceAll("\"", "\\\\\"")+"\\>\", \"Input\"],"+newLineChar);
	}
	public void addSilentCommand(String com) {
		addCommand(com+";");
	}
	
	public void end() {
		buf.delete(buf.lastIndexOf(","), buf.length());
		buf.append(newLineChar);
		buf.append(footer);
	}
	
	public String getString() {
		return buf.toString();
	}
}
