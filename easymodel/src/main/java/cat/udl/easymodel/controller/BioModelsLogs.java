package cat.udl.easymodel.controller;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;

import cat.udl.easymodel.main.SharedData;

public class BioModelsLogs {

	public BufferedWriter loadLogFile;
	public BufferedWriter errorLogFile;
//	public BufferedWriter reportLogFile;
	public BufferedWriter simLogFile;
	public boolean isLastSimError = false;
	
	public BioModelsLogs() {
		try {
			loadLogFile = new BufferedWriter(new FileWriter(SharedData.appDir+"/load_ok.txt"));
			errorLogFile = new BufferedWriter(new FileWriter(SharedData.appDir+"/load_error.txt"));
//			reportLogFile = new BufferedWriter(new FileWriter(SharedData.appDir+"/report.txt"));
			simLogFile = new BufferedWriter(new FileWriter(SharedData.appDir+"/simulation.txt"));
			
//			writer.write(str);
//		    writer.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public void close() {
		try {
			loadLogFile.close();
			errorLogFile.close();
//			reportLogFile.close();
			simLogFile.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
