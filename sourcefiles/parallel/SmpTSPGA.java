
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import edu.rit.io.InStream;
import edu.rit.io.OutStream;
import edu.rit.pj2.Job;
import edu.rit.pj2.Loop;
import edu.rit.pj2.Task;
import edu.rit.pj2.Tuple;
import edu.rit.pj2.tuple.ObjectTuple;
import edu.rit.util.AList;

/**
 * Class SmpTSPGA is a cluster parallel program to solve traveling sales man problem
 * 
 * @author Satyajeet Shahane, Ajeeth Kannan
 * 
 */
public class SmpTSPGA extends Job {
	
	private Tour initTour;
	private int popSize = 30;
	private int iterGA; 
	private int interMigaration;
	private int intraMigaration;
	private String inputFileName;
	private String stringArray[];
	
	/**
	 * Job main program.
	 */
	public void main(String args[]) throws IOException {
		
		initTour = new Tour();
		
		// Parse command line arguments
		if( args.length != 4 ) {
			throw new ArrayIndexOutOfBoundsException("SeqTSPGA should have 5 arguments");
		}
		
		inputFileName = args[0];
		
		try {
			iterGA = Integer.parseInt(args[1]);
			interMigaration = Integer.parseInt(args[2]);
			intraMigaration = Integer.parseInt(args[3]);
		}catch(NumberFormatException e) {
			throw new NumberFormatException("Argument 2,3,4 should be a number");
		}
		
		addCities();
		
		int workerSize = workers();
		int threadSize = ( workerSize*4 ) - 1;
		
		// put the initial tour into the tuple space
		putTuple( new ObjectTuple<Tour>(initTour) );
		
		// Set up a task group of K worker tasks
		rule().task(workers(), WorkerTask.class).args(Integer.toString(threadSize),Integer.toString(popSize),
				Integer.toString(workerSize),Integer.toString(iterGA),
				Integer.toString(interMigaration),Integer.toString(intraMigaration));
		
		// Set up reduction task
		rule().atFinish().task(ReduceTask.class).runInJobProcess().args(stringArray);
	
	}
	
	/**
	 * 
	 * Parse the cities from the input file.
	 * 
	 * @throws IOException if error occurs while reading the file
	 */
	private void addCities() throws IOException {
		
		FileReader inputFile;
		BufferedReader bufferedReader = null;
		String line;
		int x,y;
		
		String points[];
		
		try {
		
			inputFile = new FileReader(inputFileName);
			bufferedReader = new BufferedReader(inputFile);
			
			ArrayList<String> pointsList = new ArrayList<String>();
			
			while( (line = bufferedReader.readLine()) != null)
			{
				pointsList.add(line);
			}
			
			stringArray = new String[pointsList.size()];
			City[] city = new City[pointsList.size()];
			
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
		
		}
		catch( IOException e ) {
			throw new IOException("Error while reading");
		}
		finally {
			bufferedReader.close();
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
		
		private int threadSize;
		private int workerSize;
		private int popSize;
		static int taskRank;
		private int beforeTaskRank;
		private int N;
		private int times = 0;
		private int interGA;
		private int interMigaration;
		private int intraMigaration;
		
		private Population population[] = new Population[4];
		
		private GeneticAlgorithm geneticAlgorithm[] = new GeneticAlgorithm[4]; 
		
		private AList<Tour> nodeMigrationSend = new AList<Tour>();
		private ArrayList<Tour> nodeMigrationReceive = new ArrayList<Tour>();
		
		private int migratingRate = 6;
		
		/**
		 * Worker task main program
		 */
		public void main(String[] args) throws Exception {
			
			// reads tuple
			initTour = readTuple(new ObjectTuple<Tour>()).item; 
			
			// parse command line arguments
			threadSize = Integer.parseInt(args[0]);
			popSize = Integer.parseInt(args[1]);
			workerSize = Integer.parseInt(args[2]);
			interGA = Integer.parseInt(args[3]);
			interMigaration = Integer.parseInt(args[4]);
			intraMigaration = Integer.parseInt(args[5]);
			
			N = initTour.getTourSize();
			
			// taskRank rank of this node
			taskRank = taskRank();
			
			// taskRank rank of this neighbor node
			beforeTaskRank = taskRank - 1;
			if( beforeTaskRank == -1 ) {
				beforeTaskRank = workerSize - 1;
			}
			
			// outer loop mentions internode migration
			// inner loop mentions intranode migration
			for(int outerIter = interMigaration; outerIter > 0; outerIter--) {
			
				for( int iter = 0; iter < intraMigaration; iter++ ) {
				
					// run genetic algorithm in all cores independently
					parallelFor(0,3).exec( new Loop() {
				
						int seed;
						int rank;
						int beforeRank;
				
						// initialize the core
						public void start( ) {
					
							// rank of this core
							rank = rank();
							
							// rank of neighbor core
							beforeRank = rank -1;
							if( beforeRank == -1 ) {
								beforeRank = 3;
							}
					
							// seed for this core 
							seed = taskRank * 4 + rank;
						
							 // initialize for first time
							if( times == 0 ) {
							
								population[rank] = new Population( initTour , popSize , seed);
							
								geneticAlgorithm[rank] = new GeneticAlgorithm(population[rank], popSize, N, seed, threadSize);
							
							}
							else {
							
							}
											
						}

						// run threads in each core
						// run genetic algorithm
						public void run(int args) throws Exception {
							
							if( !nodeMigrationReceive.isEmpty() ) {
								geneticAlgorithm[rank].storeNodeMigrate(nodeMigrationReceive);
							}
							
							geneticAlgorithm[rank].run(interGA);
							
							// store best results for migration
							geneticAlgorithm[rank].processMigrate( geneticAlgorithm[beforeRank] );
							
					
						}
					
				
				
					} );
				
					if( times == 0 ) {
						times++;
					}
					

					if( !nodeMigrationReceive.isEmpty() ) {
						nodeMigrationReceive.clear();
					}
			
				}
				
				nodeMigrationSend.clear();
				nodeMigrationReceive.clear();
				
				addToNodeMigrationSend();
				
				if( outerIter == 1 ) {
					
					// final results sent to front end
					Tour bestTour = nodeMigrationSend.get(0);
					bestTour.calculateDistance();
					Double bestDistance = bestTour.getEucDistance();
					putTuple( new EndTuple(bestTour,bestDistance));
					
				}
				else {
					
					// internode migration
					putTuple(1, new TourTuple(nodeMigrationSend,beforeTaskRank,outerIter));
				
					TourTuple templete = new TourTuple();
					TourTuple tourTuple = null;
					templete.taskRank = taskRank;
					templete.step = outerIter;
				
					tourTuple = takeTuple(templete);
					nodeMigrationReceive = tourTuple.storeInNodeMirgationReceive(nodeMigrationReceive);
				}
				
			}
			
		}
		
		/**
		 * get the best tours in a node for internode migration
		 */
		private void addToNodeMigrationSend() {
			
			Population population = new Population();
			
			for( int i = 0; i < geneticAlgorithm.length; i++ ) {
				population.copyPopulation(geneticAlgorithm[i].getPopulation());
			}
			
			population.populationSort();
			
			nodeMigrationSend = population.getTopPopAsAlist(migratingRate);
			
		}
		
	}
	
	/**
	 * Class TourTuple extends Tuple to send best tour in tuple space from a node. 
	 * This is an inter node migration for a node with its neighbor node.
	 * 
	 * @author Satyajeet Shahane, Ajeeth Kannan
	 * 
	 */
	private static class TourTuple extends Tuple {

		AList<Tour> migrateTour = new AList<Tour>();
		int taskRank;
		int step;
		
		// empty constructor
		public TourTuple() {
			
		}
		
		/**
		 * parameterized constructor
		 * 
		 * @param migrateTour AList of best tours
		 * @param taskRank taskRank of the node
		 * @param step iteration number
		 */
		public TourTuple( AList<Tour> migrateTour , int taskRank, int step ) {
			this.migrateTour = migrateTour;
			this.taskRank = taskRank;
			this.step = step;
		}
		
		/**
		 * overrides matchContent() of Tuple
		 * 
		 * @param target to be matched with this tuple
		 */
		public boolean matchContent(Tuple target) {
			TourTuple tourTuple = (TourTuple) target;
			return this.taskRank == tourTuple.taskRank && this.step == tourTuple.step;
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
			migrateTour = (AList<Tour>) inStream.readObject();
			taskRank = inStream.readInt();
			step = inStream.readInt();
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
			outStream.writeObject(migrateTour);
			outStream.writeInt(taskRank);
			outStream.writeInt(step);
		}
		
		/**
		 * 
		 * @param nodeMigrationReceive get migrated tour
		 * @return ArrayList of migrated tour
		 */
		public ArrayList<Tour> storeInNodeMirgationReceive(ArrayList<Tour> nodeMigrationReceive ) {
			
			for( int i = 0; i < migrateTour.size(); i++ ) {
				nodeMigrationReceive.add(migrateTour.get(i));
			}
			
			return nodeMigrationReceive;
			
		}
		
	}
	
	/**
	 * Class EndTuple extends Tuple to send final best tour in tuple space. 
	 * The class also reduces best tour out of all nodes
	 * 
	 * @author Satyajeet Shahane, Ajeeth Kannan
	 * 
	 */
	private static class EndTuple extends Tuple {
		
		public Tour finalTour;
		public Double finalDist;
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
		 * 
		 * If the tour in et is better than this tour, tour in et 
		 * will replace this tour
		 * 
		 * @param et EndTuple results to be compared 
		 */
		public void reduce(EndTuple et) {
			if( finalDist == 0 ) {
				finalTour = et.bestTour;
				finalDist = et.bestDist;
			}
			else {
				if( et.bestDist < finalDist ) {
					finalTour = et.bestTour;
					finalDist = et.bestDist;
				}
			}
		}
		
		/**
		 * displays the result 
		 * 
		 * @param string the tour as a string for output
		 */
		public void display(String[] string) {
			
			int first = 0;
			
			ArrayList<City> individualList  = finalTour.getIndividualList();
			System.out.println(finalTour.getTourString());
			
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
			System.out.printf ("%.5g", finalDist);
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
			endTuple.finalDist = 0.0;
			EndTuple et = null;
			
			// get final tour
			while( ( et = tryToTakeTuple(template) ) != null )
			{
				endTuple.reduce(et);
			}
			
			// display result
			System.out.println("Result:");
			endTuple.display(string);
			
		}
		
	}
	
	// throws error if the format of the input file isn't correct 
	private void notEnoughNumber()
	{
		throw new ArrayIndexOutOfBoundsException("Each line should have exactly 2 numbers");
	}
	
	// throws error if the input file has anything other than number
	private void notNumberUsage()
	{
		throw new NumberFormatException("One of the inputs from the file is not a number");
	}
	
}
