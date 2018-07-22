import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;

import edu.rit.io.InStream;
import edu.rit.io.OutStream;
import edu.rit.pj2.Tuple;
import edu.rit.util.AList;

/**
 * The tour class maintains a tour for the given input cities.
 * Combining a list of cities would form a tour. 
 * 
 * A city can be added into a tour using addCity(). Euclidean
 * distance of a tour can be calculated using calculateDistance(). 
 * 
 * This class also extends Tuple. A Tuple class should have readIn()
 * and writeOut() methods.
 * 
 * @author Satyajeet Shahane, Ajeeth Kannan
 */
public class Tour extends Tuple{

	// list of cities.
	private ArrayList<City> individualList = new ArrayList<City>();
	
	// list of cities.
	// AList used while sending through tuple space.
	private AList<City> list = new AList<City>();
	
	private String tourString = ""; // Total tour as a string for output
	
	private double eucDistance; // Euclidean distance of a tour
	private double fitnessVlaue; // Fitness value of a tour
	private double probabilityValue; // Probability value of a tour
	private int samplingValue; // Sampling value of a tour

	// Default constructor
	public Tour() {
	}

	/**
	 * @param tour will be copied to this tour.
	 */
	public Tour(Tour tour) {
		
		// adding each cities into this tour.
		ArrayList<City> tempList = tour.individualList;
		for (int i = 0; i < tempList.size(); i++) {
			addCity(tempList.get(i));
		}
		
		this.eucDistance = tour.eucDistance;
		this.fitnessVlaue = tour.fitnessVlaue;
		this.probabilityValue = tour.probabilityValue;
		this.samplingValue = tour.samplingValue;
	}

	/*
	 * calls Collections.shuffle()
	 */
	public void Shuffle() {
		Collections.shuffle(individualList);
		tourString();
	}

	
	/**
	 * 
	 * add a city into the individualList.
	 * 
	 * @param city
	 */
	public void addCity(City city) {
		individualList.add(city);
		if (tourString.equals(""))
			tourString = city.getX() + " " + city.getY();
		else
			tourString = tourString + "," + city.getX() + " " + city.getY();

	}

	/**
	 * Make the whole tour as a string for output.
	 */
	private void tourString() {
		tourString = "";
		for (int i = 0; i < individualList.size(); i++) {
			City city = individualList.get(i);
			if (tourString.equals(""))
				tourString = city.getX() + " " + city.getY();
			else
				tourString = tourString + "," + city.getX() + " " + city.getY();
		}
	}

	/**
	 * Calculating euclidean distance of a tour.
	 */
	public void calculateDistance() {

		eucDistance = 0;
		
		double distance;
		int x1, y1, x2, y2;
		City city1, city2;
		int xSub, ySub;

		for (int i = 1; i <= individualList.size(); i++) {
			
			if( i == individualList.size() ) {
				city1 = individualList.get(i-1);
				city2 = individualList.get(0);
			}
			else {
				city1 = individualList.get(i - 1);
				city2 = individualList.get(i);
			}

			x1 = city1.getX();
			y1 = city1.getY();

			x2 = city2.getX();
			y2 = city2.getY();

			xSub = x1 - x2;
			ySub = y1 - y2;

			distance = Math.sqrt((xSub * xSub) + (ySub * ySub));

			eucDistance = eucDistance + distance;

		}

	}
	
	/**
	 * swaps the cites in the given two position
	 * 
	 * @param point1 position of a city in individualList
	 * @param point2 position of a city in individualList
	 * @return this instance 
	 */
	public Tour swap(int point1,int point2)
	{
		City temp = individualList.get(point1);
		individualList.set(point1, individualList.get(point2));
		individualList.set(point2, temp);
		tourString();
		return this;
	}
	
	// getter for individualList size
	public int getTourSize() {
		return individualList.size();
	}

	// getter for individualList
	public ArrayList<City> getIndividualList() {
		return individualList;
	}

	// getter for tourString
	public String getTourString() {
		return tourString;
	}
	
	// getter for fitnessVlaue
	public Double getFitnessValue() {
		return fitnessVlaue;
	}

	// getter for probabilityValue
	public double getProbabilityValue() {
		return probabilityValue;
	}

	// getter for samplingValue
	public int getSamplingValue() {
		return samplingValue;
	}
	
	// setter for tourString
	public void setTourStrig(String tourString) {
		this.tourString = tourString;
	}

	// setter for individualList
	public void setIndividualList(ArrayList<City> individualList) {
		this.individualList = individualList;
	}

	// setter for eucDistance 
	public double getEucDistance() {
		return eucDistance;
	}

	// setter for fitnessVlaue 
	public void setFitnessValue(double fitnessValue) {
		this.fitnessVlaue = fitnessValue;
	}

	// setter for probabilityValue 
	public void setProbabilityValue(double probabilityValue) {
		this.probabilityValue = probabilityValue;
	}

	// setter for samplingValue 
	public void setSamplingValue(int samplingValue) {
		this.samplingValue = samplingValue;
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
		try {
			list = (AList<City>) inStream.readObject();
			tourString = inStream.readString();
			eucDistance = inStream.readDouble();
			fitnessVlaue = inStream.readDouble();
			probabilityValue = inStream.readDouble();
			samplingValue = inStream.readInt();
			for( int i = 0; i < list.size(); i++ )
			{
				individualList.add(list.get(i));
			}
			list.clear();
		}
		catch(ClassCastException e) {
			
		}
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
		for( int i = 0; i < individualList.size(); i++ )
		{
			list.addLast( individualList.get(i) );
		}
		outStream.writeObject(list);
		outStream.writeString(tourString);
		outStream.writeDouble(eucDistance);
		outStream.writeDouble(fitnessVlaue);
		outStream.writeDouble(probabilityValue);
		outStream.writeInt(samplingValue);
		list.clear();
	}

}
