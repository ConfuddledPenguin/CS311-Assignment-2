import java.io.FileNotFoundException;
import java.security.acl.LastOwnerException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class TrivCompiler {

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
		}
	};
	
	private Map<String, Type> vars = new HashMap<String, Type>();
	LexicalAnalyser la;
	

	/**
	 * Default constructor for the TrivCompiler
	 * 
	 * @param filename
	 * @throws FileNotFoundException
	 */
	TrivCompiler(String filename) throws FileNotFoundException {
		
		try{
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
		} catch (FileNotFoundException e){
			System.err.println("Error: Invalid file path");
			System.err.println(e.getMessage());
			System.exit(0);
		}
	}

	/**
	 * It starts here
	 * 
	 * @param args
	 * @throws Exception
	 */
	public static void main(String[] args) throws Exception {

		String filename = "examples/example.triv";
		
		if(args.length > 0){
			
			if(args.length == 1){
				filename = args[0];
			}else{
				System.err.println("Incorrect ussage: To many arguements");
				System.err.println("Args should be of the form: filepath");
				System.exit(0);
			}
		}
		
		TrivCompiler tc = new TrivCompiler(filename);
		tc.parse();		
	}

	public Type e0() throws Exception {
		Type type = e1();
		while (this.la.have("equals")) {
			if(la.isLastType(type.toString())){
				System.out.println(this.la.getLastSymbol());
				e0();
			}else{
				throw new Exception("Type mismatch error. Expected type " + type + " got type " + la.lastType());
			}
		}
		return type;
	}

	public Type e1() throws Exception {
		Type type = e2();
		Type lastType;
		while (this.la.have("+")) {
			
			System.out.println(this.la.getLastSymbol());
			lastType = e1();
		
			if(!(lastType == type)){
				throw new Exception("Type mismatch error. Expected type " + type + " got type " + 
										lastType + ", on " + la.getLastSymbol());
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
				
				String symbol = la.getLastSymbol();
				type = vars.get(symbol);
				System.out.println(symbol);
			} else if (this.la.have("booleanLiteral")) {
				System.out.println(this.la.getLastSymbol());
				type = Type.BOOLEAN;
			}else{
				throw new Exception("Can't have an empty expression");
			}
		}catch (Exception e){
			System.out.println(la.lastType());
			e.printStackTrace(System.out);
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
			//TODO
//			e.printStackTrace();
			System.out.println("parse error: " + e.getMessage());
		}
		//TODO remove
//		System.out.println(vars);
	}

	private Type expression() throws Exception {
		
		Type type = null;
		
		if (this.la.have("let")) {
			
			
			String var = "";
			Type varType = null;
			
			System.out.println(this.la.getLastSymbol());
			
			this.la.mustbe("identifier");
			var = la.getLastSymbol();
			System.out.println(var);
			
			this.la.mustbe("=");
			System.out.println(this.la.getLastSymbol());
			varType = expression();
			
			this.la.mustbe("in");
			System.out.println(this.la.getLastSymbol());
			
			vars.put(var, varType);
			
			expression();
		} else {
			type = e0();
		}
		
		return type;
	}
}
