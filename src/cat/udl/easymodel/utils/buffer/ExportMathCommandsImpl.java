package cat.udl.easymodel.utils.buffer;

import cat.udl.easymodel.main.SharedData;

public class ExportMathCommandsImpl implements ExportMathCommands {
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
			"WindowFrame->Normal*)"+newLineChar + 
			newLineChar + 
			"(* Beginning of Notebook Content *)"+newLineChar + 
			"Notebook[{"+newLineChar;
	private String footer = "},"+newLineChar + 
			"WindowSize->{763, 711},"+newLineChar + 
			"WindowMargins->{{Automatic, 301}, {-10, Automatic}},"+newLineChar + 
			"StyleDefinitions->\"Default.nb\""+newLineChar + 
			"]"+newLineChar + 
			"(* End of Notebook Content *)"+newLineChar;
	
	public ExportMathCommandsImpl() {
		buf = new StringBuffer();
		buf.append(header);
	}
	
	@Override
	public void reset() {
		buf.delete(header.length(), buf.length());
	}
	
	@Override
	public void addCommand(String com) {
		buf.append("Cell[\""+com.replaceAll("\"", "\\\\\"")+"\", \"Input\"],"+newLineChar);
	}
	
	@Override
	public void end() {
		buf.delete(buf.lastIndexOf(","), buf.length());
		buf.append(newLineChar);
		buf.append(footer);
	}
	
	@Override
	public String getString() {
		return buf.toString();
	}
}
