import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Random;

import edu.rit.util.AList;


/**
 * Class Population has list of tours in it. It calls Collections.sort which is 
 * implemented in SortTours to sort the tours according to the fitness value. 
 * The class has getTopPopAsAlist() to return best tours in a population. 
 * 
 * @author Satyajeet Shahane, Ajeeth Kannan
 */
public class Population {

	protected ArrayList<Tour> tours = new ArrayList<Tour>();
	protected Tour initTour;

	// default constructor
	public Population() {
	}

	/**
	 * Parameterized constructor
	 * 
	 * copies the tours into this population from 
	 * the given population.
	 * 
	 * @param population tours to be copied from
	 */
	public Population(Population population) {
		for (int i = 0; i < population.tours.size(); i++) {
			this.tours.add(new Tour(population.tours.get(i)));
		}
	}

	/**
	 *  Parameterized constructor
	 *  
	 *  Initial tour will be shuffled to form a population
	 *  for first generation. 
	 *  
	 * @param initTour initial tour generated
	 * @param size size of a tour
	 */
	public Population(Tour initTour, int size, int seed) {
		
		this.initTour = new Tour(initTour);
		Random random = new Random(seed);

		do {
			this.initTour.Shuffle(random );
			if (tours.size() == 0) {
				Tour tour = new Tour(this.initTour);
				tour.calculateDistance();
				tours.add(tour);
			} else {
				if (!contains()) {
					Tour tour = new Tour(this.initTour);
					tour.calculateDistance();
					tours.add(tour);
				}
			}
		} while (tours.size() != size);
	}
	
	/**
	 * The method is called only by the constructor and checks 
	 * if the tour is contained already. 
	 * 
	 * @return true if the tour already exist
	 */
	public void copyPopulation(Population population) {
		for (int i = 0; i < population.tours.size(); i++) {
			this.tours.add(new Tour(population.tours.get(i)));
		}
	}
	
	/**
	 * Get the top tours in the population as a AList.
	 * 
	 * @param migratingRate number of best tours need to sent 
	 * @return AList of tours
	 */
	public AList<Tour> getTopPopAsAlist(int migratingRate) {
		AList<Tour> migratingList = new AList<Tour>();
		
		for( int i = 0; i < migratingRate; i++ ) {
			Tour tour = new Tour(tours.get(i));
			migratingList.addLast(tour);
		}
		
		return migratingList;
	}

	/**
	 * The method is called only by the constructor and checks 
	 * if the tour is contained already. 
	 * 
	 * @return true if the tour already exist
	 */
	private boolean contains() {
		boolean isContained = false;
		for (int i = 0; i < tours.size(); i++) {
			if (tours.get(i).getTourString().equals(initTour.getTourString())) {
				isContained = true;
			}
		}
		return isContained;
	}
	
	/**
	 * checks if the tour is contained already. 
	 * 
	 * @param tour tour to be checked
	 * @return true if the tour already exist
	 */
	public boolean contains(Tour tour) {
		boolean isContained = false;
		for (int i = 0; i < this.tours.size(); i++) {
			if (this.tours.get(i).getTourString().equals(tour.getTourString())) {
				isContained = true;
			}
		}
		return isContained;
	}
	
	/**
	 * sorts the population by calling the method in SortTours
	 * @return this instance
	 */
	public Population populationSort() {
		Collections.sort(tours, new SortTours());
		return this;
	}

	/**
	 * adds the tour to the population
	 * 
	 * @param tour tour to be added
	 */
	public void addTour(Tour tour) {
		tours.add(tour);
	}

	/**
	 * clears the tour
	 */
	public void clearTours() {
		tours.clear();
	}
	
	// size of the tours
	public int getToursSize() {
		return tours.size();
	}

	// getter for tours
	public ArrayList<Tour> getTours() {
		return tours;
	}
	
	// setter for tours
	public void setTours(ArrayList<Tour> tours) {
		this.tours = tours;
	}
	
}

/**
 * 
 * Class SortTours implements Comparator<Tour>. The compare method
 * in the class compares fitnessValue of the two tours
 * 
 * @author Satyajeet Shahane, Ajeeth Kannan
 */
class SortTours implements Comparator<Tour> {

	/**
	 * returns 1 if fitness value of tour2 is greater than tour2
	 * returns 0 if fitness value of tour2 is equal to tour2
	 * returns -1 if fitness value of tour2 is smaller than tour2
	 * 
	 * @param1 tour1 tour to be compared
	 * @param2 tour2 tour to be compared
	 * @return return value
	 */
	public int compare(Tour tour1, Tour tour2) {
		return tour2.getFitnessValue().compareTo(tour1.getFitnessValue());
	}

}
