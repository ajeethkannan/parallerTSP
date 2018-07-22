
import java.util.ArrayList;
import edu.rit.util.Random;

public class GeneticAlgorithm {

	private ArrayList<Tour> tours; // list of temporary tours
	private ArrayList<Tour> perIterTours; // list of temporary tours
	private ArrayList<Tour> processMigrate; // list of tours after migration
	
	private Population population; // population in a generation
	private Population perIterPopulation; // temporary population in a generation
	private int popSize; // population size in a generation
	private int N; // number of cites
	private int crossPoint1; // crossover point2 for ordered crossover
	private int crossPoint2; // crossover point1 for ordered crossover
	private int mutationPercent; // total percent in mutation
	private int crossoverPercent; // total percent in crossover
	private int seed,threadSize; 
	
	private long randomCount;
	
	/**
	 * Parameterized constructor
	 * 
	 * @param population initial random population
	 * @param size size of the population in a generation
	 * @param N number of cities in a tour
	 * @param seed seed for generating random tours
	 * @param threadSize total size of the cores running in all the nodes
	 */
	public GeneticAlgorithm(Population population, int size, int N, int seed, int threadSize) {
		this.population = population;
		this.popSize = size;
		this.N = N;
		this.seed = seed;
		this.threadSize = threadSize;
		this.randomCount = -2147483648 + seed;
		perIterTours = new ArrayList<Tour>();
		processMigrate = new ArrayList<Tour>();
		perIterPopulation = new Population();
	}

	/**
	 * 
	 * run() calls all the operations for genetic algorithm. 
	 * 
	 * @param iterGA number of iterations to run genetic algorithm
	 */
	public void run(int interGA) {
	
		addProcessMigrateTours();
		
		for( int j = 0; j < interGA; j++ ) {
			fitnessFunction();
			selection();
			crossover();
			mutation();
			
			// copying from perIterPopulation to population after each generation
			for( int i = 0; i < popSize; i++ ) {
				if( !perIterPopulation.contains(population.tours.get(i)) ) {
					perIterPopulation.addTour(population.tours.get(i));
				}
				if( perIterPopulation.tours.size() == popSize ) {
					break;
				}
			}
			perPopulationFitnessFunction();
			population.clearTours();
			for( int i = 0; i < popSize; i++ )
			{
				population.addTour(perIterPopulation.tours.get(i));
			}
			
		}
		
		
	}

	/**
	 * fitnessFunction() evaluates the fitness value for each tour and
	 * sorts the tour according to the fitness. 
	 */
	public void fitnessFunction() {

		double tempDistance;
		tours = population.getTours();

		int populationSize = tours.size();

		double maxDistance = 0;

		// Maximum euclidean distance of all the tours in population 
		for (int i = 0; i < populationSize; i++) {

			tours.get(i).calculateDistance();
			
			tempDistance = tours.get(i).getEucDistance();

			if (maxDistance < tempDistance) {
				maxDistance = tempDistance;
			}

		}

		// calculating the fitness value according to the Maximum euclidean distance
		for (int i = 0; i < populationSize; i++) {

			tours.get(i).setFitnessValue(maxDistance - tours.get(i).getEucDistance());

		}

		// sorting the tours in a population
		population = population.populationSort();

	}
	
	/**
	 * fitnessFunction() evaluates the fitness value for each tour in 
	 * perIterPopulation and sorts the tour in perIterPopulation according 
	 * to the fitness. 
	 */
	public void perPopulationFitnessFunction() {

		double tempDistance;
		tours = perIterPopulation.getTours();

		int populationSize = tours.size();
		double maxDistance = 0;

		// Maximum euclidean distance of all the tours in perIterPopulation 
		for (int i = 0; i < populationSize; i++) {
			
			tours.get(i).calculateDistance();

			tempDistance = tours.get(i).getEucDistance();

			if (maxDistance < tempDistance) {
				maxDistance = tempDistance;
			}

		}

		// calculating the fitness value according to the Maximum euclidean distance
		for (int i = 0; i < populationSize; i++) {

			tours.get(i).setFitnessValue(maxDistance - tours.get(i).getEucDistance());

		}

		// sorting the tours in a population
		perIterPopulation = perIterPopulation.populationSort();

	}

	/**
	 * selection() does the selection operation of genetic algorithm.
	 * The method finds probability value and sampling value for 
	 * all the tours in population.
	 */
	public void selection() {

		tours = population.getTours();
		Tour tempTourObj;
		double probability;
		int samplingValue;

		int populationSize = tours.size();
		double totalFitnessVal = 0;

		for (int i = 0; i < populationSize; i++) {
			totalFitnessVal = totalFitnessVal + tours.get(i).getFitnessValue();
		}

		// setting probability value and sampling value for each tour
		for (int i = 0; i < populationSize; i++) {
			tempTourObj = tours.get(i);
			probability = tempTourObj.getFitnessValue() / totalFitnessVal;
			samplingValue = ((int) (probability * populationSize)) + 1;
			tempTourObj.setProbabilityValue(probability);
			tempTourObj.setSamplingValue(samplingValue);
		}

	}
	
	/**
	 * crossover() does the crossover operation of genetic algorithm. Population of
	 * this generation would be copied to perIterPopulation. Initially best 20
	 * percent of the population will be directly selected for next generation. 
	 * Two parents will be randomly selected in perIterPopulation and undergo
	 * ordered crossover. 
	 */
	public void crossover() {

		tours = population.getTours();
		int populationSize = tours.size();
		Tour tempTourObj,child;
		int randomVal1,randomVal2;
		Random random;
		ArrayList<String> checkList = new ArrayList<String>();
		crossoverPercent = (int) (populationSize * 0.80);

		perIterTours.clear();
		perIterPopulation.clearTours();

		// copy 20 percent population to perItertours
		for (int i = 0; i < populationSize; i++) {
			tempTourObj = tours.get(i);
			for (int j = 0; j < tempTourObj.getSamplingValue(); j++) {
				perIterTours.add(new Tour(tempTourObj));
			}
		}

		// copy all the population to perIterPopulation
		for (int i = 0; i < populationSize - crossoverPercent; i++) {
			perIterPopulation.addTour(tours.get(i));
		}

		// run crossover
		random = new Random(randomCount);

		for (int i = 0; i < perIterTours.size() / 2; i++) {
			randomVal1 = random.nextInt(perIterTours.size());
			randomVal2 = random.nextInt(perIterTours.size());
			while (perIterTours.get(randomVal1).getTourString().equals(perIterTours.get(randomVal2).getTourString())
					|| checkList.contains(randomVal1 + " " + randomVal2)) {
				randomVal1 = random.nextInt(tours.size());
				randomVal2 = random.nextInt(tours.size());
				if( randomCount == 2147483647 ) {
					randomCount = -2147483648 + seed;
				}
				randomVal1 = random.nextInt(perIterTours.size());
				randomVal2 = random.nextInt(perIterTours.size());
			}
			checkList.add(randomVal1 + " " + randomVal2);
			child = oderedCrossover(perIterTours.get(randomVal1), perIterTours.get(randomVal2));
			if( !perIterPopulation.contains(child) ) {
				perIterPopulation.addTour(child);
			}
			randomCount = randomCount + threadSize;
			
		}
		
		
	}

	/**
	 * The method follows oderedCrossover algorithm. Gets two parents  
	 * to perform oderedCrossover from crossover() to get a new offspring.
	 * 
	 * @param parent1 parent1 to crossover
	 * @param parent2 parent2 to crossover
	 * @return new child
	 */
	private Tour oderedCrossover(Tour parent1, Tour parent2) {
		int startPoint,endPoint;
		boolean isThere = false;
		City city[] = new City[N];
		Tour child = new Tour();
		ArrayList<City> individualList1 = parent1.getIndividualList();
		ArrayList<City> individualList2 = parent2.getIndividualList();
		int j,pos;
		Random random = new Random(randomCount);
		
		crossPoint1 = random.nextInt(individualList1.size());
		crossPoint2 = random.nextInt(individualList2.size());
		
		while( crossPoint1 >= crossPoint2 )
		{
			crossPoint1 = random.nextInt(individualList1.size());
			crossPoint2 = random.nextInt(individualList2.size());
		}
				
		for( int i = crossPoint1; i < crossPoint2; i++)
		{
			city[i] = new City(individualList1.get(i).getX(),individualList1.get(i).getY());
		}
		
		for( int l = 0; l < 2 ; l++ ) {
			
			if( l == 0 ) {
				startPoint = crossPoint2;
				endPoint = parent1.getTourSize();
			}
			else {
				startPoint = 0;
				endPoint = crossPoint1;
			}
		 
		
			for( int i = startPoint; i < endPoint; i++ )
			{
				j = crossPoint2;
				do
				{
					isThere = false;
					for( int k = 0; k < city.length; k++ )
					{
						if( city[k] == null  ) {
						}
						else {
							if(city[k].pointString().equals(individualList2.get(j).pointString())) {
								isThere = true;
							}
						}
					}
					pos = j;
					j++;
					if( j == parent2.getTourSize() ) {
						j = 0;
					}	 
				}while(isThere == true);
				city[i] = individualList2.get(pos);
			}
		}
		
		for( int i = 0; i < city.length; i++ )
		{
			child.addCity(city[i]);
		}
		
		child.calculateDistance();
		
		randomCount = randomCount + threadSize;
		if( randomCount == 2147483647 ) {
			randomCount = -2147483648 + seed;
		}
		
		return child;
		
	}

	/**
	 * mutation() does the mutation operation of genetic algorithm.
	 * The cross points for mutation is selected randomly. Position 
	 * of two cites in a tour will be swapped to mutate. Seeds are 
	 * incremented all the mutation. So that cross points are not 
	 * same while calling mutation next time.
	 */
	public void mutation() {
		
		tours = population.getTours();
		int populationSize = tours.size();
		int tourSize = tours.get(0).getTourSize();
		Random random1 = new Random(randomCount); 
		Random random2 = new Random(randomCount); 
		int tour,cPoint1,cPoint2;
		Tour mutateTour;
		
		mutationPercent = (int) (populationSize * 0.10);
		
		for( int i = 0; i<mutationPercent; i++ )
		{
			tour = random1.nextInt(populationSize);
			cPoint1 = random2.nextInt(tourSize);
			cPoint2 = random2.nextInt(tourSize);
			mutateTour = new Tour(tours.get(tour));
			mutateTour = mutateTour.swap(cPoint1, cPoint2);
			if( !perIterPopulation.contains(mutateTour) )
			{
				perIterPopulation.addTour(mutateTour);
			}
		}
		
		randomCount = randomCount + threadSize;
		if( randomCount == 2147483647 ) {
			randomCount = -2147483648 + seed;
		}
		
	}
	
	/**
	 * addProcessMigrateTours() adds the migrated tours to this population
	 * before starting the first iteration.
	 */
	private void addProcessMigrateTours() {
		int addAt = population.tours.size()-1;
		for( int i = 0; i < processMigrate.size(); i++ ) {
			if( !population.contains(processMigrate.get(i)) ) {
				population.tours.set(addAt, processMigrate.get(i));
				addAt -- ;
			}
		}
		processMigrate.clear();
	}
	
	/**
	 * 
	 * Adds intranode migrated tours to this population
	 * @param geneticAlgorithm geneticAlgorithm population which has migrated results
	 */
	public void processMigrate(GeneticAlgorithm geneticAlgorithm) {
		for( int i = 0; i < 6; i++ ) {
			Tour tempTour = new Tour(this.population.tours.get(i));
			geneticAlgorithm.processMigrate.add(tempTour);
		}
	}
	
	/**
	 * The best tours after a certain number of iterations will
	 * be stored in this population as a migration from other 
	 * population.
	 * 
	 * @param nodeMigrationReceive 
	 */
	public void storeNodeMigrate(ArrayList<Tour> nodeMigrationReceive) {
		int addAt = population.tours.size()-1;
		for( int i = 0; i < nodeMigrationReceive.size(); i++ ) {
			if( !population.contains(nodeMigrationReceive.get(i)) ) {
				Tour tour = new Tour(nodeMigrationReceive.get(i));
				population.tours.set(addAt, tour);
				addAt -- ;
			}
		}
		fitnessFunction();
		population = population.populationSort();
		
	}
	
	// getter for population
	public Population getPopulation() {
		
		return population;
		
	}

}
