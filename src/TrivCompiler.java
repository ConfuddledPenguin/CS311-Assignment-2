import java.io.FileNotFoundException;
import java.util.HashSet;
import java.util.Set;

public class TrivCompiler {

	LexicalAnalyser la;
	private enum Type {
		INTEGER{
			@Override
			public String toString(){
				return "numeral";
			}
		},
		BOOLEAN{
			@Override
			public String toString() {
				return "booleanLiteral";
			}
		},
		IDENTIFIER{
			@Override
			public String toString() {
				return "identifier";
			}
		}
	};

	/**
	 * Default constructor for the TrivCompiler
	 * 
	 * @param filename
	 * @throws FileNotFoundException
	 */
	TrivCompiler(String filename) throws FileNotFoundException {
		this.la = new LexicalAnalyser(filename) {
			
			/**
			 * Set the keywords to hunt for
			 */
			@Override
			Set<String> getKeywords() {
				Set<String> k = new HashSet<>();
				k.add("let");
				k.add("in");
				return k;
			}
		};
	}

	/**
	 * It starts here
	 * 
	 * @param args
	 * @throws Exception
	 */
	public static void main(String[] args) throws Exception {

		TrivCompiler tc = new TrivCompiler("examples/example1.triv");
		tc.parse();

	}

	public Type e0() throws Exception {
		Type type = e1();
		while (this.la.have("equals")) {
			if(la.isLastType(type.toString())){
				System.out.println(this.la.getLastSymbol());
				e0();
			}else{
				throw new Exception("Type mismatch error");
			}
		}
		return type;
	}

	public Type e1() throws Exception {
		Type type = e2();
		while (this.la.have("+")) {
			if(la.isLastType(type.toString())){
				System.out.println(this.la.getLastSymbol());
				e1();
			}else{
				throw new Exception("Type mismatch error");
			}
		}
		
		return type;
	}

	public Type e2() throws Exception {
		
		Type type = null;
		
		try{
			if (this.la.have("numeral")) {
				System.out.println(this.la.getLastSymbol());
				type = Type.INTEGER;
			} else if (this.la.have("identifier")) {
				System.out.println(this.la.getLastSymbol());
				type = Type.IDENTIFIER;
			} else if (this.la.have("booleanLiteral")) {
				System.out.println(this.la.getLastSymbol());
				type = Type.BOOLEAN;
			}else{
				throw new Exception("Can't have an empty expression");
			}
		}catch (Exception e){
			throw new Exception("Unrecognised symbol");
		}
		
		return type;
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
