package trivCompiler;

import java.io.FileNotFoundException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class TrivCompiler {

	private class Result {
		
		public final Object value;
		public final Type type;
		
		private Result(Object value, Type type){
			this.value = value;
			this.type = type;
		}
	}
	
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
	
	private Map<String, Type> varsTypes = new HashMap<String, Type>();
	private Map<String, Object> varsValues = new HashMap<String, Object>();
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
		String helpString = "Program usage:\n java -jar TrivCompiler.jar <filepath>";
		
		if(args.length > 0){
			
			if(args.length == 1){
				filename = args[0];
				
				if(filename.equals("help")){
					System.out.println(helpString);
					System.exit(0);
				}
			}else{
				System.err.println("Incorrect ussage: To many arguements");
				System.err.println(helpString);
				System.exit(0);
			}
		}
		
		TrivCompiler tc = new TrivCompiler(filename);
		tc.parse();		
	}

	public Result e0() throws Exception {
		
		Result result = e1();
		Result lastResult;
		
		while (this.la.have("equals")) {
			
			if(la.isLastType(result.type.toString())){ // check types match
				
				System.out.println(this.la.getLastSymbol());
				lastResult = e0();
				
				boolean value = false;
				if(result.type == Type.INTEGER){
					
					int integer = (int) result.value;
					int lastInteger = (int) lastResult.value;
					
					if(integer == lastInteger){
						value = true;
					}
					
				}else if(result.type == Type.BOOLEAN){
					
					boolean bool = (boolean) result.value;
					boolean lastBool = (boolean) lastResult.value;
					
					if( bool == lastBool ){
						value = true;
					}
					
				}
				
				result = new Result(value, Type.BOOLEAN);
				
			}else{
				
				checkForUnexpectedEnding();
				
				throw new Exception("Type mismatch error. Expected type " + result.type + " got type " + 
						la.lastType() + ", on " + la.getLastSymbol());
			}
		}
		return result;
	}

	public Result e1() throws Exception {
		
		Result result = e2();
		
		/*
		 * This var is called lastResult as its value is determined by the call to 
		 * la.getLastSymbol(). In reality this represents the result of what comes after
		 * the "+" symbol and as such is really the next result...
		 */
		Result lastResult;
		while (this.la.have("+")) {
			
			System.out.println(this.la.getLastSymbol());
			lastResult = e1();
		
			if(lastResult.type == Type.BOOLEAN){
				throw new Exception("Cant add boolean values. Expected type integer got type " +
									"boolean");
			}
			
			if(!(lastResult.type == result.type)){
				throw new Exception("Type mismatch error. Expected type " + result.type + " got type " + 
										lastResult.type + ", on " + la.getLastSymbol());
			}
		
			int value = ((Integer)lastResult.value) + ((Integer)result.value);
			
			result = new Result(value, Type.INTEGER);
		}
				
		return result;
	}

	public Result e2() throws Exception {
		
		Type type = null;
		Object value = 0;
		
		if (this.la.have("numeral")) {
	
			//get result vars
			value = Integer.parseInt(la.getLastSymbol());
			type = Type.INTEGER;
			
			
			//finally print
			System.out.println(value);
		} else if (this.la.have("identifier")) {
			
			String symbol = la.getLastSymbol();
			type = varsTypes.get(symbol);
			value = varsValues.get(symbol);
			if(type == null){
				throw new Exception("Undeclared variable: " + symbol);
			}else if(value == null){
				throw new Exception("Value of varible " + symbol + " not found");
			}
			
			
			System.out.println(symbol);
		} else if (this.la.have("booleanLiteral")) {
			
			value = Boolean.parseBoolean(la.getLastSymbol());
			type = Type.BOOLEAN;
			
			System.out.println(value);
		}else{
			throw new Exception("Empty expression found after " + la.getLastSymbol());
		}
		
		return new Result(value, type);
	}

	public void parse() {
		try {
			Result result = expression();

			if (!this.la.endOfInput()) {
				throw new Exception("unexpected symbols after end of program");
			}

			System.out.println("\nprogram is correctly formed");
			System.out.println("program is of type " + result.type + 
							   " and evaluates to " + result.value);
		} catch (Exception e) {
			System.out.println("parse error: " + e.getMessage());
		}
	}

	public Result expression() throws Exception {
		
		Result result = null;
		
		if (this.la.have("let")) {
			
			
			String var = "";
			Type varType = null;
			Object varValue = null;
			
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
			result = expression();
			varType = result.type;
			varValue = result.value;
			
			this.la.mustbe("in");
			System.out.println(this.la.getLastSymbol());
			
			varsTypes.put(var, varType);
			varsValues.put(var, varValue);
			
			result = expression();
		} else if(la.have("if")){
			
			System.out.println(la.getLastSymbol());
			
			mustHaveSymbol("(");
			
			result = expression();
			
			mustHaveSymbol(")");
			
			if( result.type != Type.BOOLEAN){
				throw new Exception("If statement must be followed by a boolean expression");
			}
			
			if(la.have("then")){
				System.out.println(la.getLastSymbol());
				
				Result thenResult = expression();
				
				mustHaveSymbol("else");
				System.out.println(la.getLastSymbol());
				Result elseResult = expression();
				
				if((boolean)result.value){
					result = thenResult;
				}else{
					result = elseResult;
				}
			}else{
				mustHaveSymbol("{");
				Result thenResult = expression();
				mustHaveSymbol("}");
				
				mustHaveSymbol("else");
				System.out.println(la.getLastSymbol());
				mustHaveSymbol("{");
				Result elseResult = expression();
				mustHaveSymbol("}");
				
				if((boolean)result.value){
					result = thenResult;
				}else{
					result = elseResult;
				}
					
			}
			
		} else {
			result = e0();
		}
		
		return result;
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
