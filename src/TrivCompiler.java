import java.io.FileNotFoundException;
import java.util.HashSet;
import java.util.Set;

public class TrivCompiler {

	LexicalAnalyser la;

	TrivCompiler(String filename) throws FileNotFoundException {
		this.la = new LexicalAnalyser(filename) {
			@Override
			Set<String> getKeywords() {
				Set<String> k = new HashSet<>();
				k.add("let");
				k.add("in");
				return k;
			}
		};
	}

	public static void main(String[] args) throws Exception {

		TrivCompiler tc = new TrivCompiler("examples/example1.triv");
		tc.parse();

	}

	public void e0() throws Exception {
		e1();
		while (this.la.have("=")) {
			System.out.println(this.la.getLastSymbol());
			e0();

		}
	}

	public void e1() throws Exception {
		e2();
		while (this.la.have("+")) {
			System.out.println(this.la.getLastSymbol());
			e1();
		}
	}

	public void e2() throws Exception {
		if (this.la.have("numeral")) {
			System.out.println(this.la.getLastSymbol());
		} else if (this.la.have("identifier")) {
			System.out.println(this.la.getLastSymbol());
		} else if (this.la.have("booleanLiteral")) {
			System.out.println(this.la.getLastSymbol());
		}
	}

	public void parse() {
		try {
			expression();

			if (!this.la.endOfInput()) {
				throw new Exception("unexpected symbols after end of program");
			}

			System.out.println("program is correctly formed");
		} catch (Exception e) {
			System.out.println("parse error: " + e.getMessage());
		}
	}

	private void expression() throws Exception {
		if (this.la.have("let")) {
			System.out.println(this.la.getLastSymbol());
			this.la.mustbe("identifier");
			System.out.println(this.la.getLastSymbol());
			this.la.mustbe("=");
			System.out.println(this.la.getLastSymbol());
			expression();
			this.la.mustbe("in");
			System.out.println(this.la.getLastSymbol());
			expression();
		} else {
			e0();
		}
	}
}
