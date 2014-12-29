package trivCompiler;

import java.io.FileNotFoundException;
import java.util.Set;

/**
 * @author Richard Connor
 * 
 *         a relatively generic lexical analyser which can be used for many
 *         languages
 *
 */
public abstract class LexicalAnalyser {

	private static boolean isDigit(char nextChar) {
		return nextChar >= '0' && nextChar <= '9';
	}

	private static boolean isLetter(char nextChar) {
		return (nextChar >= 'a' && nextChar <= 'z')
				|| (nextChar >= 'A' && nextChar <= 'Z');
	}

	private static boolean isWordFollowChar(char nextChar) {
		if (isLetter(nextChar) || isDigit(nextChar)) {
			return true;
		} else {
			switch (nextChar) {
			case '_':
				/*
				 * different styles of identifiers are allowed in various
				 * languages
				 */
				// case '-':
				// case '.':
				return true;
			default:
				return false;
			}
		}
	}

	private Set<String> keywords;
	private boolean atEndOfInput;
	private StringBuffer lastSymbol;

	private String lastSymbolBuffer;
	private String currentSymbol;

	private String lastLexeme;

	private Exception lastError;

	InputFile input;

	/**
	 * Default Constructor for the LexicalAnalyser
	 * 
	 * @param filename
	 * @throws FileNotFoundException
	 */
	LexicalAnalyser(String filename) throws FileNotFoundException {
		
		this.input = new InputFile(filename); //get new file
		this.keywords = getKeywords(); //get the keywords
		this.atEndOfInput = false;
		this.lastSymbol = new StringBuffer("");
		try {
			this.lastLexeme = getNextSymbol();
			this.lastError = null;
		} catch (Globals.LexicalError e) {
			this.lastError = e;
		}
	}

	public boolean endOfInput() {
		strip();
		return this.atEndOfInput;
	}

	public String getLastSymbol() {
		return this.lastSymbolBuffer;
	}
	
	public String getCurrentSymbol(){
		return currentSymbol;
	}
	
	public boolean isLastType(String target){

		
		if(target.equals(lastLexeme)){
			return true;
		}
		return false;
	}
	
	public String lastType(){
		
		return lastLexeme;
	}

	public boolean have(String target) throws Exception {
		if (this.lastError != null) {
			throw this.lastError;
		}
		
		if (this.lastLexeme.equals(target)) {
			try {
				this.lastLexeme = getNextSymbol();
				this.lastError = null;
			} catch (Globals.LexicalError e) {
				this.lastError = e;
			}
			return true;
		} else {
			return false;
		}
	}
	
	public void mustbe(String target) throws Exception {
		if (this.lastError != null) {
			throw this.lastError;
		}
		if (this.lastLexeme.equals(target)) {
			try {
				this.lastLexeme = getNextSymbol();
				this.lastError = null;
			} catch (Globals.LexicalError e) {
				this.lastError = e;
			}
		} else {
			throw new Globals.UnexpectedSymbolException();
		}
	}

	private void complete(char firstChar) {
		switch (firstChar) {

		case '!':
		case '<':
		case '>': {
			try {
				char secondChar = this.input.nextChar();

				if (secondChar == '=') {
					this.lastSymbol.append(secondChar);
				} else {
					this.input.unconsumeChar();
				}
				break;
			} catch (Globals.ReadPastEndOfFileException e) {
				this.atEndOfInput = true;
			}

		}
		}
	}

	/**
	 * 
	 * 
	 * @return
	 * @throws Globals.LexicalError
	 */
	private String getNextSymbol() throws Globals.LexicalError {

		String res;
		this.lastSymbolBuffer = this.lastSymbol.toString();
		this.lastSymbol = new StringBuffer();

		/*
		 * the call to endOfInput causes a call to strip to strip white space
		 */
		if (endOfInput()) {
			res = "eoi";
			this.lastSymbol.append("eoi");
		} else {
			if (alphaNumeric()) {
				String s = this.lastSymbol.toString();
				if (this.keywords.contains(s)) {
					res = s;
				} else if (s.equals("true") || s.equals("false")) {
					res = "booleanLiteral";
				} else if (s.equals("(") || s.equals(")") || s.equals("[") ||
							s.equals("]") || s.equals("{") || s.equals("}")){
					res = "bracket";
				} else {
					res = "identifier";
				}
			} else if (numeral()) {
				res = "numeral";
			} else if (equals()){
				res = "equals";
			} else if (puncutator()) {
				res = this.lastSymbol.toString();
			} else {
				// need to advance input here or else might not terminate
				try {
					this.input.nextChar();
				} catch (Globals.ReadPastEndOfFileException e) {
					// dummy block, going to throw an exception anyway
				}
				// should throw a lexical error
				throw new Globals.LexicalError();
			}
		}

		currentSymbol = lastSymbol.toString();
		return res;
	}

	private boolean haveComment() {
		boolean res = false;
		try {
			char p1 = this.input.nextChar();

			if (p1 == '#') {
				char pNext = this.input.nextChar();
				while (pNext != '\n' && pNext != '\r') {
					pNext = this.input.nextChar();
				}
				this.input.unconsumeChar();
				res = true;
			} else {
				this.input.unconsumeChar();
			}
		} catch (Globals.ReadPastEndOfFileException e) {
			this.atEndOfInput = true;
		}
		return res;
	}

	private boolean haveSpaceChar() {
		char c;
		try {
			c = this.input.nextChar();

			boolean res = (c == ' ' || c == '\t' || c == '\n' || c == '\r');

			if (!res) {
				this.input.unconsumeChar();
			}
			return res;
		} catch (Globals.ReadPastEndOfFileException e) {
			this.atEndOfInput = true;
			return false;
		}
	}

	private boolean numeral() {
		boolean res = false;
		try {
			char nextChar = this.input.nextChar();

			if (isDigit(nextChar)) {
				res = true;
				this.lastSymbol.append(nextChar);

				boolean finished = false;
				while (!finished) {
					nextChar = this.input.nextChar();

					if (isDigit(nextChar)) {
						this.lastSymbol.append(nextChar);
					} else {
						this.input.unconsumeChar();
						finished = true;
					}
				}
			} else {
				this.input.unconsumeChar();
			}

		} catch (Globals.ReadPastEndOfFileException e) {
			this.atEndOfInput = true;
		}
		return res;
	}
	
	private boolean equals(){
		
		boolean res = false;
		
		try {
			char nextChar = this.input.nextChar();

			if (nextChar == '=') {
				
				char secondChar = this.input.nextChar();

				if (secondChar == '=') {
					this.lastSymbol.append("==");
					res = true;
				} else {
					this.input.unconsumeTwoChar();
				}
				
			} else {
				this.input.unconsumeChar();
			}

		} catch (Globals.ReadPastEndOfFileException e) {
			this.atEndOfInput = true;
		}
		
		return res;
		
	}

	private boolean puncutator() {
		boolean res = false;
		try {
			char nextChar = this.input.nextChar();

			switch (nextChar) {
			/*
			 * core TRIV has only a few allowed punctuator symbols, but no harm
			 * in including them here
			 */
			case ';':
			case ',':
			case '(':
			case ')':
			case '[':
			case ']':
			case '+':
			case '-':
			case '=':
			case '*':
			case '/':
			case '&':
			case '^':
			case '.':
			case '{':
			case '}':
				res = true;
				this.lastSymbol.append(nextChar);
				break;

			/*
			 * these characters can either occur by themselves, or as part of
			 * compound symbols
			 */
			case '<':
			case '>':
			case '!':
				res = true;
				this.lastSymbol.append(nextChar);
				complete(nextChar);
				break;
			}
			if (!res) {
				this.input.unconsumeChar();
			}
		} catch (Globals.ReadPastEndOfFileException e) {
			this.atEndOfInput = true;
		}
		return res;
	}

	/**
	 * strips white space from the front of the current position
	 * 
	 * @throws Exception
	 * 
	 */
	private void strip() {
		while (this.haveSpaceChar() || haveComment())
		// || (elisionEnabled && haveHaggisElision()))
		{
			//
		}
	}

	/**
	 * any sequence of alphanumeric characters starting with a letter, ie
	 * 
	 * [a-zA-Z][a-zA-Z0-9]*
	 * 
	 * if one is found, this returns true and updates lastSymbol to the actual
	 * value
	 * 
	 * @return
	 */
	private boolean alphaNumeric() {
		boolean res = false;
		try {
			char nextChar = this.input.nextChar();

			if (isLetter(nextChar)) {
				res = true;
				this.lastSymbol.append(nextChar);

				boolean finished = false;
				while (!finished) {
					nextChar = this.input.nextChar();

					if (isWordFollowChar(nextChar)) {
						this.lastSymbol.append(nextChar);
					} else {
						this.input.unconsumeChar();
						finished = true;
					}
				}
			} else {
				this.input.unconsumeChar();
			}
		} catch (Globals.ReadPastEndOfFileException e) {
			this.atEndOfInput = true;
		}
		return res;
	}

	abstract Set<String> getKeywords();

}
