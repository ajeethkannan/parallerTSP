
import java.util.ArrayList;
import edu.rit.util.Random;

/**
 * Class GeneticAlgorithm implements all the operations of genetic 
 * algorithm. The run() starts manages all the operations. fitnessFunction()
 * and perPopulationFitnessFunction() evaluates the fitness of the 
 * population. selection(), crossover() and mutation() are also implemented.
 * Crossover method used in this class is Ordered Crossover.
 * 
 * @author Satyajeet Shahane, Ajeeth Kannan
 */
public class GeneticAlgorithm {

	private ArrayList<Tour> tours; // list of temporary tours
	private ArrayList<Tour> perIterTours; // list of temporary tours
	
	private Population population; // population in a generation
	private Population perIterPopulation; // temporary population in a generation
	private int mutationPercent; // total percent in mutation
	private int crossoverPercent; // total percent in crossover
	private int popSize; // population size in a generation
	private int N; // number of cites
	private int crossPoint1; // crossover point1 for ordered crossover
	private int crossPoint2; // crossover point2 for ordered crossover
	
	private long randomCount = -2147483648; // seed for random

	/**
	 * Parameterized constructor
	 * 
	 * @param population initial random population
	 * @param size size of the population in a generation
	 * @param N number of cities in a tour
	 */
	public GeneticAlgorithm(Population population, int size, int N) {
		this.population = population;
		this.popSize = size;
		this.N = N;
		perIterTours = new ArrayList<Tour>();
		perIterPopulation = new Population();
	}

	/**
	 * 
	 * run() calls all the operations for genetic algorithm. 
	 * 
	 * @param iterGA number of iterations to run genetic algorithm
	 */
	public void run( int iterGA) {
		for( int j = 0; j < iterGA; j++ ) {
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

		random = new Random(randomCount);

		// run crossover
		for (int i = 0; i < perIterTours.size() / 2; i++) {
			randomVal1 = random.nextInt(perIterTours.size());
			randomVal2 = random.nextInt(perIterTours.size());
			while (perIterTours.get(randomVal1).getTourString().equals(perIterTours.get(randomVal2).getTourString())
					|| checkList.contains(randomVal1 + " " + randomVal2)) {
				randomVal1 = random.nextInt(tours.size());
				randomVal2 = random.nextInt(tours.size());
			}
			checkList.add(randomVal1 + " " + randomVal2);
			child = oderedCrossover(perIterTours.get(randomVal1), perIterTours.get(randomVal2));
			if( !perIterPopulation.contains(child) ) {
				perIterPopulation.addTour(child);
			}
		}
		
		// increment seed value so that the same parent is not selected again
		randomCount++;
		if( randomCount == 2147483647 ) {
			randomCount = -2147483648;
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
		
		randomCount++;
		if( randomCount == 2147483647 ) {
			randomCount = -2147483648;
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
		
		// initialize random
		Random random1 = new Random(randomCount); 
		Random random2 = new Random(randomCount);
		
		int tour; 
		int cPoint1,cPoint2; // cross point
		Tour mutateTour;
		
		mutationPercent = (int) (populationSize * 0.10);
		
		// calls mutation on a random tour in a population
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
		
		// increment the seed value for random
		randomCount++;
		if( randomCount == 2147483647 ) {
			randomCount = -2147483648;
		}
		
	}
	
	// getter for population
	public Population getPopulation() {
		
		return population;
		
	}

}
