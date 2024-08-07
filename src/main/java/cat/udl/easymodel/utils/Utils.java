package cat.udl.easymodel.utils;

import java.util.ArrayList;
import java.util.Set;
import java.util.regex.Pattern;

import cat.udl.easymodel.main.SharedData;

public class Utils {
	private Utils() {

	}

	public static Integer getPosition(Set<String> searchSet, String searchStr) {
		int count=0;
		for (String it : searchSet) {
			if (it.equals(searchStr))
				return count;
			count++;
		}
		return null;
	}
	
	public static Integer boolToInt(Boolean bool) {
		if (bool == null)
			return null;
		return (bool) ? 1 : 0;
	}

	public static Boolean intToBool(Integer in) {
		if (in == null)
			return null;
		return (in == 0) ? false : true;
	}

	public static String evalMathExpr(final String str) {
		return new Object() {
			int pos = -1, ch;

			void nextChar() {
				ch = (++pos < str.length()) ? str.charAt(pos) : -1;
			}

			boolean eat(int charToEat) {
				while (ch == ' ')
					nextChar();
				if (ch == charToEat) {
					nextChar();
					return true;
				}
				return false;
			}

			String parse() {
				nextChar();
				double x = parseExpression();
				if (pos < str.length())
					throw new RuntimeException("Unexpected: " + (char) ch);
				return Utils.doubleToString(x);
			}

			// Grammar:
			// expression = term | expression `+` term | expression `-` term
			// term = factor | term `*` factor | term `/` factor
			// factor = `+` factor | `-` factor | `(` expression `)`
			// | number | functionName factor | factor `^` factor

			double parseExpression() {
				double x = parseTerm();
				for (;;) {
					if (eat('+'))
						x += parseTerm(); // addition
					else if (eat('-'))
						x -= parseTerm(); // subtraction
					else
						return x;
				}
			}

			double parseTerm() {
				double x = parseFactor();
				for (;;) {
					if (eat('*'))
						x *= parseFactor(); // multiplication
					else if (eat('/'))
						x /= parseFactor(); // division
					else
						return x;
				}
			}

			double parseFactor() {
				if (eat('+'))
					return parseFactor(); // unary plus
				if (eat('-'))
					return -parseFactor(); // unary minus

				double x;
				int startPos = this.pos;
				if (eat('(')) { // parentheses
					x = parseExpression();
					eat(')');
				} else if ((ch >= '0' && ch <= '9') || ch == '.') { // numbers
					while ((ch >= '0' && ch <= '9') || ch == '.')
						nextChar();
					x = Double.parseDouble(str.substring(startPos, this.pos));
				} else if (ch >= 'a' && ch <= 'z') { // functions
					while (ch >= 'a' && ch <= 'z')
						nextChar();
					String func = str.substring(startPos, this.pos);
					x = parseFactor();
					if (func.equals("sqrt"))
						x = Math.sqrt(x);
					else if (func.equals("sin"))
						x = Math.sin(Math.toRadians(x));
					else if (func.equals("cos"))
						x = Math.cos(Math.toRadians(x));
					else if (func.equals("tan"))
						x = Math.tan(Math.toRadians(x));
					else
						throw new RuntimeException("Unknown function: " + func);
				} else {
					throw new RuntimeException("Unexpected: " + (char) ch);
				}

				if (eat('^'))
					x = Math.pow(x, parseFactor()); // exponentiation

				return x;
			}
		}.parse();
	}

	public static String doubleToString(Double d) {
		return SharedData.getInstance().doubleToString(d);
	}

	public static Integer getNumOfDecimalDigits(String num) {
		String[] parts = num.split("\\.");
		if (parts.length == 2)
			return parts[1].length();
		else
			return 0;
	}
	public static String curateForURLFilename(String filename) {
		Pattern pattern = Pattern.compile("[^a-zA-Z0-9._-]");
		String curatedText = pattern.matcher(filename).replaceAll("-");
		if (curatedText.isEmpty())
			curatedText= "unknown";
		return curatedText;
	}

	public static <T> boolean compareArrayLists(ArrayList<T> list1, ArrayList<T> list2) {
		if (list1.size() != list2.size()) {
			return false;
		}
		for (T element : list1) {
			if (!list2.contains(element)) {
				return false;
			}
		}
		for (T element : list2) {
			if (!list1.contains(element)) {
				return false;
			}
		}
		return true;
	}
}
