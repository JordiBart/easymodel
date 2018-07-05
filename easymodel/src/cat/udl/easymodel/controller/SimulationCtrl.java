package cat.udl.easymodel.controller;

import com.wolfram.jlink.MathLinkException;

import cat.udl.easymodel.utils.CException;

public interface SimulationCtrl {
	void simulate() throws CException, MathLinkException, Exception;
}
