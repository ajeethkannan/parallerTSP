import java.io.IOException;
import edu.rit.io.InStream;
import edu.rit.io.OutStream;
import edu.rit.io.Streamable;

/**
 * Class City defines a city using its x and y co-ordinates
 * 
 * The class implements Streamable from pj2. So, class has also 
 * exported readIn() and writeOut() from the Interface Streamable.
 * 
 * @author Satyajeet Shahane, Ajeeth Kannan
 */

public class City implements Streamable{
	
	// Hidden data members.

	private int x;
	private int y;

	// default constructor
	
	public City() {
		
	}
	
	/**
	 * 
	 * parameterized constructor
	 * 
	 * @param x x coordinate of a point
	 * @param y y coordinate of a point
	 */
	
	public City(int x, int y) {
		this.x = x;
		this.y = y;
	}
	
	//getter for x
	public int getX() {
		return x;
	}
	
	//getter for y
	public int getY() {
		return y;
	}

	//setter for x
	public void setX(int x) {
		this.x = x;
	}

	//setter for y
	public void setY(int y) {
		this.y = y;
	}

	/**
	 * 
	 * returns point as a string
	 * 
	 * @return string which returns the point
	 */
	public String pointString() {
		return x + " " + y ;
	}

	/**
	 * readIn() method declared in Interface edu.rit.io.Streamable
	 *
	 * Gets value for the hidden data members.
	 * 
	 * @exception IOException is called to indicate that an object
	 * could not be or should not be read.
	 */
	public void readIn(InStream inStream) throws IOException {
		x = inStream.readInt();
		y = inStream.readInt();
	}

	/**
	 * writeOut() method declared in Interface edu.rit.io.Streamable
	 * 
	 * hidden data members are sent through tuple.
	 * 
	 * exception IOException is called to indicate that an object
	 * could not be or should not be written.
	 */
	public void writeOut(OutStream outStream) throws IOException {
		outStream.writeInt(x);
		outStream.writeInt(y);
	}

}
