package trivCompiler;

import java.io.FileNotFoundException;
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
		},
		IDENTIFIER{
			@Override
			public String toString() {
				return "identifier";
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
	public TrivCompiler(String filename) throws FileNotFoundException {
		
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
					k.add("if");
					k.add("then");
					k.add("else");
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

		String filename = "examples/example1.triv";
		
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
				type = Type.BOOLEAN;
			}else{
				
				checkForUnexpectedEnding();
				
				throw new Exception("Type mismatch error. Expected type " + type + " got type " + 
						la.lastType() + ", on " + la.getLastSymbol());
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
		
		if (this.la.have("numeral")) {
			System.out.println(this.la.getLastSymbol());
			type = Type.INTEGER;
		} else if (this.la.have("identifier")) {
			
			String symbol = la.getLastSymbol();
			type = vars.get(symbol);
			if(type == null){
				throw new Exception("Undeclared variable: " + symbol);
			}
			System.out.println(symbol);
		} else if (this.la.have("booleanLiteral")) {
			System.out.println(this.la.getLastSymbol());
			type = Type.BOOLEAN;
		}else{
			throw new Exception("Empty expression found after " + la.getLastSymbol());
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
	}

	public Type expression() throws Exception {
		
		Type type = null;
		
		if (this.la.have("let")) {
			
			
			String var = "";
			Type varType = null;
			
			System.out.println(this.la.getLastSymbol());
			
			try{
				this.la.mustbe("identifier");
			}catch(Globals.UnexpectedSymbolException e){
				throw new Exception("Missing variable declaration");
			}
			var = la.getLastSymbol();
			System.out.println(var);
			
			this.la.mustbe("=");
			System.out.println(this.la.getLastSymbol());
			varType = expression();
			
			this.la.mustbe("in");
			System.out.println(this.la.getLastSymbol());
			
			vars.put(var, varType);
			
			expression();
		} else if(la.have("if")){
			
			System.out.println(la.getLastSymbol());
			
			mustHaveSymbol("(");
			
			type = expression();
			
			mustHaveSymbol(")");
			
			if( type != Type.BOOLEAN){
				throw new Exception("If statement must be followed by a boolean expression");
			}
			
			if(la.have("then")){
				System.out.println(la.getLastSymbol());
				expression();
				
				if(la.have("else")){
					System.out.println(la.getLastSymbol());
					expression();
				}
			}else{
				mustHaveSymbol("{");
				expression();
				mustHaveSymbol("}");
				
				if(la.have("else")){
					System.out.println(la.getLastSymbol());
					mustHaveSymbol("{");
					expression();
					mustHaveSymbol("}");
				}
			}
			
		} else {
			type = e0();
		}
		
		return type;
	}
	
	/**
	 * Wonders if the next symbol is as specified
	 * 
	 * @param c
	 * @throws Exception
	 */
	private void mustHaveSymbol(String str) throws Exception{
		
		if(str == null){
			throw new Exception("Cant expect a null. The Dev has screwed up");
		}
		
		if(la.have(str)){
			System.out.println(la.getLastSymbol());
		}else{
			throw new Exception("Expected '" + str + "' after '" + la.getLastSymbol() + "' found '" + la.getCurrentSymbol() + "'");
		}
	}
	
	private void checkForUnexpectedEnding() throws Exception {
		
		if(la.lastType().equals("eoi")){
			throw new Exception("Program unexpectantly ended after " + la.getLastSymbol());
		}
	}
}
