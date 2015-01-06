package trivCompiler;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;

public class InputFile {

	private static int INPUT_BUFFER_SIZE = 256;
	private InputStream input;
	private char lastChar;
	private char lastTwoChar;
	private boolean lastCharUnconsumed;
	private boolean lastTwoCharUnconsumed;

	private byte[] inputBuffer;
	private int inputBufferPos;
	private int inputBytesLeftInBuffer;

	/**
	 * Default Constructor for the InputFile
	 * 
	 * @param filename
	 * @throws FileNotFoundException
	 */
	InputFile(String filename) throws FileNotFoundException {
		this.input = new FileInputStream(filename);
		this.inputBuffer = new byte[INPUT_BUFFER_SIZE];
		this.inputBufferPos = INPUT_BUFFER_SIZE;
		this.inputBytesLeftInBuffer = 0;
		this.lastCharUnconsumed = false;
		lastTwoCharUnconsumed = false;
		
		System.out.println("Using file at: " + filename + "\n");
	}

	private char getNextChar() throws Globals.ReadPastEndOfFileException {
		try {
			if (this.inputBufferPos >= INPUT_BUFFER_SIZE) {
				this.inputBytesLeftInBuffer = this.input.read(this.inputBuffer);
				this.inputBufferPos = 0;
			}
			if (this.inputBytesLeftInBuffer <= 0) {
				throw new Exception();
			}
			this.inputBytesLeftInBuffer--;
			byte c = this.inputBuffer[this.inputBufferPos++];
			return (char) c;
		} catch (Exception e) {
			throw new Globals.ReadPastEndOfFileException();
		}
	}

	public static void main(String[] args) throws FileNotFoundException {
		InputFile i = new InputFile("examples/example1.triv");
		boolean first3 = true;
		while (true) {
			try {
				final char nextChar = i.nextChar();
				if (first3 && nextChar == '3') {
					i.unconsumeChar();
					first3 = false;
				}
				System.out.print(nextChar);
			} catch (Globals.ReadPastEndOfFileException e) {
				System.out.println();
				break;
			}
		}
	}

	public char nextChar() throws Globals.ReadPastEndOfFileException {
		if (this.lastCharUnconsumed) {
			this.lastCharUnconsumed = false;
			return this.lastChar;
		}else if(lastTwoCharUnconsumed){
			lastTwoCharUnconsumed = false;
			lastCharUnconsumed = true;
			return lastTwoChar;
		} else {
			final char nextChar = getNextChar();
			lastTwoChar = lastChar;
			this.lastChar = nextChar;
			return nextChar;
		}
	}

	public void unconsumeChar() {
		this.lastCharUnconsumed = true;
	}
	
	public void unconsumeTwoChar(){
		lastTwoCharUnconsumed = true;
	}

}
