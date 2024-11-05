package cat.udl.easymodel.httpservlet;

import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.io.PrintWriter;

import cat.udl.easymodel.main.SharedData;
import cat.udl.easymodel.utils.P;

@WebServlet(urlPatterns = { "/fixreactions" })
public class ScriptCurateDBReactions extends HttpServlet {
	private static final long serialVersionUID = 1L;

	public void init() throws ServletException {
	}

	public void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		if (true) //disable
			return;
		SharedData sharedData = SharedData.getInstance();
		if (!sharedData.isDebug())
			return;
		response.setContentType("text/plain"); // text/html
		PrintWriter out = response.getWriter();
		try {
			Connection con = sharedData.getDbManager().getCon();
			PreparedStatement ps, ps2;
			ps = con.prepareStatement("SELECT id,reaction FROM reaction");
			int c=1;
			ResultSet rs = ps.executeQuery();
			while (rs.next()) {
				String r = rs.getString("reaction");
				boolean dirty=false;
				String newR;
				if (r.matches("^.*(?=[^\\s]);(?=[^\\s]).*$" )) {
					newR = r.replaceAll("(?=[^\\s]);(?=[^\\s])", "; ");
					P.p("OLD: " + r + "\nNEW: " + newR);
					r = newR;
					dirty=true;
				}
				if (r.matches("^.*(?=\\D|^)\\s*1\\s*\\*\\s*.*$")) {
					newR = r.replaceAll("(?=\\D|^)\\s*1\\s*\\*\\s*", " ");
					newR = newR.trim();
					P.p("OLD: " + r + "\nNEW: " + newR);
					r = newR;
					dirty=true;
				}
				if (dirty) {
					c = 1;
					ps2 = con.prepareStatement("UPDATE reaction SET reaction=? WHERE id=?");
					ps2.setString(c++, r);
					ps2.setInt(c++, rs.getInt("id"));
					ps2.executeUpdate();
					ps2.close();
				}
			}
			rs.close();
			ps.close();
			out.print("DONE");
		} catch (Exception e) {
			out.print("ERROR");
			e.printStackTrace();
		}
	}

	public void destroy() {
	}
}
