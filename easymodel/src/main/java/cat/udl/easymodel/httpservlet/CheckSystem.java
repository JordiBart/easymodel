package cat.udl.easymodel.httpservlet;

import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import cat.udl.easymodel.main.SharedData;
import cat.udl.easymodel.mathlink.MathLinkOp;

@WebServlet(urlPatterns = { "/checksystem" })
public class CheckSystem extends HttpServlet {
	public void init() throws ServletException {
	}

	public void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		response.setContentType("text/plain");
		PrintWriter out = response.getWriter();
		out.print(check());
	}
	
	public void destroy() {
	}
	
	private String check() {
		String message = "";
		int fails = 0;
		try {
			// check mysql
			SharedData.getInstance().getDbManager().open();
		} catch (Throwable t) {
			fails++;
			message += t.getMessage()+"\n";
			t.printStackTrace();
		}
		try {
			// check mathlink + virtual display
			MathLinkOp ml = new MathLinkOp(null);
			ml.openMathLink();
			try {
				ml.evaluateToImage("Rasterize[Plot[x, {x, 0, 1}]]");
			} catch (Throwable t) {
				fails++;
				message += t.getMessage()+"\n";
				t.printStackTrace();	
			}
			ml.closeMathLink();
		} catch (Throwable t) {
			fails++;
			message += t.getMessage()+"\n";
			t.printStackTrace();
		}
		if (fails==0)
			message = "OK";
		return message;
	}
}