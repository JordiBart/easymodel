package cat.udl.easymodel.httpservlet;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import cat.udl.easymodel.mathlink.MathLinkOp;
import com.wolfram.jlink.MathLinkException;

@WebServlet(urlPatterns = { "/numberlinks" })
public class ScriptNumberLinks extends HttpServlet {
	public void init() throws ServletException {
	}

	public void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		if (true)
			return;
		response.setContentType("text/plain");
		PrintWriter out = response.getWriter();
		ArrayList<MathLinkOp> mlArray = new ArrayList<>();
		while (true) {
			if (mlArray.size() >= 20)
				break;
			MathLinkOp ml = null;
			try {
				ml = new MathLinkOp();
			} catch (MathLinkException e) {
			}
			if (ml != null)
				mlArray.add(ml);
			else
				break;
		}
		out.print("max number of simultaneous mathlinks="+mlArray.size()+"\n");
		for (MathLinkOp ml : mlArray)
			ml.closeMathLink();
	}

	public void destroy() {
	}
}
