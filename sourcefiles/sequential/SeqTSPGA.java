
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import edu.rit.io.InStream;
import edu.rit.io.OutStream;
import edu.rit.pj2.Job;
import edu.rit.pj2.Task;
import edu.rit.pj2.Tuple;
import edu.rit.pj2.tuple.ObjectTuple;

/**
 * Class SmpTSPGA is a sequential program to solve traveling sales man problem
 * 
 * @author Satyajeet Shahane, Ajeeth Kannan
 * 
 */
public class SeqTSPGA extends Job {

	private Tour initTour;
	private String inputFileName;
	private String stringArray[];
	private int iterGA; 
	
	/**
	 * Job main program.
	 */
	public void main(String[] args) throws Exception {
		
		initTour = new Tour();
		
		// Parse command line arguments
		if( args.length != 2 ) {
			throw new ArrayIndexOutOfBoundsException("SeqTSPGA should have 2 arguments");
		}
		
		inputFileName = args[0];
		
		try {
			iterGA = Integer.parseInt(args[1]);
		}
		catch(NumberFormatException e) {
			throw new NumberFormatException("Argument 2 should be a number");
		}
		
		addCities();
		
		// put the initial tour into the tuple space
		putTuple( new ObjectTuple<Tour>(initTour) );
		
		// Set up a task group of K worker tasks
		rule().task(workers(), WorkerTask.class).args(Integer.toString(iterGA));
		
		// Set up reduction task
		rule().atFinish().task(ReduceTask.class).runInJobProcess().args(stringArray);
		
		
	}
	
	/**
	 * 
	 * Parse the cities from the input file.
	 * 
	 * @throws IOException if error occurs while reading the file
	 */
	public void addCities() throws IOException {
		
		File file;
		FileReader inputFile;
		BufferedReader bufferedReader = null;
		String line;
		int x,y;
		
		String points[];
		
		try {
			
			file = new File(inputFileName);
			inputFile = new FileReader(file);
			bufferedReader = new BufferedReader(inputFile);
			
			ArrayList<String> pointsList = new ArrayList<String>();
			
			while( (line = bufferedReader.readLine()) != null) {
				pointsList.add(line);
			}
			
			City[] city = new City[pointsList.size()];
			stringArray = new String[pointsList.size()];
			
			for(int i = 0; i < pointsList.size(); i++)
			{
				
				points = pointsList.get(i).split(" ");
						
				if(points.length != 2)
				{
					notEnoughNumber();
				}
						
				try
				{
					x = Integer.parseInt(points[0]);
					y = Integer.parseInt(points[1]);
					city[i] = new City(x, y);
					initTour.addCity(city[i]);
					stringArray[i] = x + " " + y;
				}	
				catch(NumberFormatException e)
				{
					notNumberUsage();
				}
				catch(ArrayIndexOutOfBoundsException e)
				{
					notEnoughNumber();
				}				
			}
			bufferedReader.close();
		
		}
		catch( IOException e ) {
			throw new IOException("File not found");
		}
		
	}
	
	/**
	 * Class WorkerTask runs the genetic algorithm for the given number of iterations
	 * 
	 * @author Satyajeet Shahane, Ajeeth Kannan
	 * 
	 */
	private static class WorkerTask extends Task {

		private Tour initTour;
		private int popSize;
		private int N;
		private int iterGA;
		private Population population;
		private GeneticAlgorithm geneticAlgorithm;
		
		/**
		 * Worker task main program
		 */
		public void main(String[] args) throws Exception {
			
			// read the tours 
			initTour = readTuple(new ObjectTuple<Tour>()).item; 
			
			// number of generations for genetic algorithm 
			iterGA = Integer.parseInt(args[0]);
			
			popSize = 30;
			N = initTour.getTourSize();
			
			// get initial population
			population = new Population( initTour , popSize );
			
			// initialize genetic algorithm
			geneticAlgorithm = new GeneticAlgorithm(population,popSize,N);
			
			// run genetic algorithm
			geneticAlgorithm.run(iterGA);
			
			// The best result is sent in tuple space
			Tour bestTour = geneticAlgorithm.getPopulation().getTours().get(0);
			bestTour.calculateDistance();
			Double bestDistance = bestTour.getEucDistance();
			putTuple( new EndTuple(bestTour,bestDistance));
			
		}
	}
	
	/**
	 * Class EndTuple extends Tuple to send final best tour in tuple space. 
	 * 
	 * @author Satyajeet Shahane, Ajeeth Kannan
	 * 
	 */
	private static class EndTuple extends Tuple {
		
		public Tour bestTour;
		public Double bestDist;
		
		// default constructor
		public EndTuple() {
		
		}
		
		/**
		 * 
		 * @param bestTour tour that is best of all tours 
		 * @param bestDist the euclidean distance of the best tour
		 */
		public EndTuple( Tour bestTour, Double bestDist ) {
			this.bestTour = bestTour;
			this.bestDist = bestDist;
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
			bestTour = (Tour) inStream.readObject();
			bestDist = inStream.readDouble();
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
			outStream.writeObject(bestTour);
			outStream.writeDouble(bestDist);
		}
		
		/**
		 * displays the result 
		 * 
		 * @param string the tour as a string for output
		 */
		public void display(String[] string) {
			
			int first = 0;
			
			ArrayList<City> individualList  = bestTour.getIndividualList();
			System.out.println(bestTour.getTourString());
			
			for( int i = 0; i < individualList.size(); i++ ) {
				for( int j = 0; j < string.length; j++ ) {
					if( individualList.get(i).pointString().equals(string[j]) ) {
						if( i == 0 ) {
							first = j;
						}
						System.out.print(j);
						System.out.print(" -> ");
						break;
					}
				}
			}
			System.out.print(first);
			System.out.printf ("\n");
			System.out.printf ("%.5g", bestDist);
			System.out.printf ("\n");
			
		}
		
		
	}
	
	/**
	 * Class ReduceTask extends Task to get from tuple space final results.
	 * The final result is displayed in this class  
	 * 
	 * @author Satyajeet Shahane, Ajeeth Kannan
	 * 
	 */
	private static class ReduceTask extends Task { 
		
		/**
		 * Reduce task main program.
		 */
		public void main(String[] args) throws Exception {
			
			String string[] = args; 
			EndTuple template = new EndTuple();
			EndTuple endTuple = new EndTuple();
			EndTuple et = null;
			
			// get final tour
			while( ( et = tryToTakeTuple(template) ) != null )
			{
				endTuple = et;
			}
			
			// display result
			System.out.println("Result:");
			endTuple.display(string);
			
		}
		
	}

	// throws error if the format of the input file isn't correct 
	private void notEnoughNumber() {
		throw new ArrayIndexOutOfBoundsException("Each line should have exactly 2 numbers");
	}

	// throws error if the input file has anything other than number
	private void notNumberUsage() {
		throw new NumberFormatException("One of the inputs from the file is not a number");
	}
	
	// specifies cores required as 1
	protected static int coresRequired() {
		return 1;
    }

}
