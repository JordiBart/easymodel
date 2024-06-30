package cat.udl.easymodel.thread.visitcounter;

import java.io.BufferedWriter;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.LinkedHashMap;

import cat.udl.easymodel.main.SharedData;
import cat.udl.easymodel.utils.CException;

public class VisitCounterRunnable implements Runnable {
	private static final String visitCounterFilePath = SharedData.visitCounterFilePath;
	private String visitorIp=null;
	private LinkedHashMap<String, VisitNode> visitsByIpMap=null;
	private Integer totalCounter = 0;

	public VisitCounterRunnable() {
		readVisitCounterFile();
	}

	public void setVisitorIp(String visitorIp) {
		this.visitorIp = visitorIp;
		totalCounter++;
	}

	public Integer getTotalCounter() {
		return totalCounter;
	}
	
	@Override
	public void run() {
		if (visitorIp == null)
			return;
		// update map
		VisitNode visitNode = visitsByIpMap.get(visitorIp);
		if (visitNode == null)
			visitsByIpMap.put(visitorIp, new VisitNode(visitorIp));
		else
			visitNode.inc();
		// save files
		writeFile();
		logVisits();
		
		System.out.println(LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")) + " | "
				+ visitorIp + " | Total/Unique: " + totalCounter + "/" + visitsByIpMap.keySet().size());
	}

	public void printVisits() {
		String sep = "-------------------------------------";
		System.out.println(sep);
		for (VisitNode v : visitsByIpMap.values()) {
			System.out.println(v.ip);
			System.out.println(v.counter);
			System.out.println(v.lastVisit);
			System.out.println(v.geoJson);
			System.out.println(sep);
		}
	}

	public void logVisits() {
		try {
			BufferedWriter logFile = new BufferedWriter(new FileWriter(SharedData.appDir + "/visits.log"));
			String sep = "-------------------------------------";
			String newLine = "\r\n";
			logFile.write("VISITORS LOG" + newLine);
			int total = 0;
			for (VisitNode v : visitsByIpMap.values()) {
				total += v.counter;
				logFile.write(sep + newLine);
				logFile.write("IP " + v.ip + newLine);
				logFile.write("Visits " + v.counter + newLine);
				logFile.write("Last visit " + v.lastVisit.toString() + newLine);
				logFile.write("Geo info " + v.geoJson + newLine);
			}
			logFile.write(sep + newLine);
			logFile.write("Total/Unique " + total + "/" + visitsByIpMap.size() + newLine);
			logFile.close();
		} catch (IOException e) {
			System.err.println("Visits log file error");
//			e.printStackTrace();
		}
	}

	@SuppressWarnings("unchecked")
	public void readVisitCounterFile() {
		// read from file
		try {
			FileInputStream fileIn = new FileInputStream(visitCounterFilePath);
			ObjectInputStream in = new ObjectInputStream(fileIn);
			Object readObject = in.readObject();
			in.close();
			fileIn.close();
			if (readObject instanceof LinkedHashMap<?, ?>)
				visitsByIpMap = (LinkedHashMap<String, VisitNode>) readObject;
			else
				throw new CException("Corrupted file?");
//			System.out.println("Visit counter file read");
		} catch (CException | IOException | ClassNotFoundException e) {
			System.err.println("ERROR CAN'T READ " + visitCounterFilePath);
//			e.printStackTrace();
			visitsByIpMap = new LinkedHashMap<String, VisitNode>();
		} finally {
			totalCounter=0;
			for (VisitNode counterByIp : visitsByIpMap.values())
				totalCounter += counterByIp.counter;
		}
	}

	public void writeFile() {
		// save map to file
		try {
			FileOutputStream fileOut = new FileOutputStream(visitCounterFilePath);
			ObjectOutputStream out = new ObjectOutputStream(fileOut);
			out.writeObject(visitsByIpMap);
			out.close();
			fileOut.close();
		} catch (IOException i) {
			System.err.println("ERROR: CAN'T WRITE " + visitCounterFilePath);
//					i.printStackTrace();
		}
	}
}
