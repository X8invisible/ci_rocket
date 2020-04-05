package coursework;

import model.Fitness;
import model.Individual;
import model.NeuralNetwork;

import java.util.ArrayList;

/**
 * Implements a basic Evolutionary Algorithm to train a Neural Network
 * 
 * You Can Use This Class to implement your EA or implement your own class that extends {@link NeuralNetwork} 
 * 
 */
public class ExampleEvolutionaryAlgorithm extends NeuralNetwork {
	

	/**
	 * The Main Evolutionary Loop
	 */
	@Override
	public void run() {		
		//Initialise a population of Individuals with random weights
		population = initialise();
		System.out.println(Parameters.getNumGenes());
		//Record a copy of the best Individual in the population
		best = getBest(population);
		System.out.println("Best From Initialisation " + best);

		/**
		 * main EA processing loop
		 */		
		
		while (evaluations < Parameters.maxEvaluations) {

			/**
			 * this is a skeleton EA - you need to add the methods.
			 * You can also change the EA if you want 
			 * You must set the best Individual at the end of a run
			 * 
			 */

			Individual parent1 = selectTournament();
			Individual parent2 = selectTournament();

			// Generate a child by crossover.
			//ArrayList<Individual> children = uniformCrossover(parent1, parent2);


			//ArrayList<Individual> children = singlePointCrossover(parent1, parent2);
			ArrayList<Individual> children = multiPointCrossover(parent1, parent2);

			//mutate the offspring
			mutate(children);
			
			// Evaluate the children
			evaluateIndividuals(children);			

			// Replace children in population
			replace(children);

			// check to see if the best has improved
			best = getBest(population);
			
			// Implemented in NN class. 
			outputStats();
			
			//Increment number of completed generations			
		}

		//save the trained network to disk
		saveNeuralNetwork();
	}

	

	/**
	 * Sets the fitness of the individuals passed as parameters (whole population)
	 * 
	 */
	private void evaluateIndividuals(ArrayList<Individual> individuals) {
		for (Individual individual : individuals) {
			individual.fitness = Fitness.evaluate(individual, this);
		}
	}


	/**
	 * Returns a copy of the best individual in the population
	 * 
	 */
	private Individual getBest(ArrayList<Individual> population) {
		best = null;;
		for (Individual individual : population) {
			if (best == null) {
				best = individual.copy();
			} else if (individual.fitness < best.fitness) {
				best = individual.copy();
			}
		}
		return best;
	}

	/**
	 * Generates a randomly initialised population
	 * 
	 */
	private ArrayList<Individual> initialise() {
		population = new ArrayList<>();
		for (int i = 0; i < Parameters.popSize; ++i) {
			//chromosome weights are initialised randomly in the constructor
			Individual individual = new Individual();
			population.add(individual);
		}
		evaluateIndividuals(population);
		return population;
	}

	/**
	 * Selection --
	 * 
	 * NEEDS REPLACED with proper selection this just returns a copy of a random
	 * member of the population
	 *
	 * DONE
	 */
	private Individual selectTournament() {
		ArrayList<Individual> competition = new ArrayList<>();
		for(int i = 0; i< Parameters.tournamentSize; i++){
			competition.add(population.get(Parameters.random.nextInt(population.size())));

		}
		return getBest(competition).copy();
	}

	/**
	 * Crossover / Reproduction
	 *
	 * Uniform Crossover
	 */
	private ArrayList<Individual> uniformCrossover(Individual parent1, Individual parent2) {
		ArrayList<Individual> children = new ArrayList<>();
		Individual child = new Individual();
		for(int i = 0; i < parent1.chromosome.length; i++){
			if(Parameters.random.nextDouble() < 0.5)
				child.chromosome[i] = parent1.chromosome[i];
			else
				child.chromosome[i] = parent2.chromosome[i];
		}
		children.add(child);
		return children;
	}



	private Individual pointCrossover(Individual child, Individual parent, int start, int end){
		for(int i = start; i < end; i++){
			child.chromosome[i] = parent.chromosome[i];
		}

		return child;
	}
	private ArrayList<Individual> singlePointCrossover(Individual parent1, Individual parent2) {
		ArrayList<Individual> children = new ArrayList<>();
		Individual child = new Individual();

		int crossoverPoint = Parameters.random.nextInt(parent1.chromosome.length);

		child = pointCrossover(child, parent1,0,crossoverPoint);
		child = pointCrossover(child,parent2,crossoverPoint,parent2.chromosome.length);
		children.add(child);
		return children;
	}

	private ArrayList<Individual> multiPointCrossover(Individual parent1, Individual parent2){
		ArrayList<Individual> children = new ArrayList<>();
		Individual child = new Individual();
		int nh = Parameters.getNumHidden();
		int point1 = 0;
		int point2 = 0;
		Individual p = parent1;
		for(int i = 0; i < nh; i++){
		//start and end(not inclusive) weights of inputs to hidden
			point1 = i*5;
			point2 = i*5 + 5;
			child = pointCrossover(child,p,point1, point2);
		//bias of hidden
			point1 = nh*5 +i;
			point2 = nh*5 +i+1;
			child = pointCrossover(child, p, point1, point2);
		//start and end(not inclusive) weights of inputs to hidden
			point1 = nh*6 + i*3;
			point2 = nh*6 + i*3 + 4;
			child = pointCrossover(child, p, point1, point2);
			point1 = nh*9 +i;
			point2 = nh*9 +i+1;
			child = pointCrossover(child, p, point1, point2);


			if(p == parent1)
				p = parent2;
			else
				p = parent1;
		}

		children.add(child);
		return children;
	}
	/**
	 * Mutation
	 * 
	 * 
	 */
	private void mutate(ArrayList<Individual> individuals) {		
		for(Individual individual : individuals) {
			for (int i = 0; i < individual.chromosome.length; i++) {
				if (Parameters.random.nextDouble() < Parameters.mutateRate) {
					if (Parameters.random.nextBoolean()) {
						individual.chromosome[i] += (Parameters.mutateChange);
					} else {
						individual.chromosome[i] -= (Parameters.mutateChange);
					}
				}
			}
		}		
	}

	/**
	 * 
	 * Replaces the worst member of the population
	 * if the new child is better
	 * 
	 */
	private void replace(ArrayList<Individual> individuals) {
		for(Individual individual : individuals) {
			int idx = getWorstIndex();
			if(population.get(idx).fitness > individual.fitness){
				population.set(idx, individual);
			}
		}
	}

	

	/**
	 * Returns the index of the worst member of the population
	 * @return
	 */
	private int getWorstIndex() {
		Individual worst = null;
		int idx = -1;
		for (int i = 0; i < population.size(); i++) {
			Individual individual = population.get(i);
			if (worst == null) {
				worst = individual;
				idx = i;
			} else if (individual.fitness > worst.fitness) {
				worst = individual;
				idx = i; 
			}
		}
		return idx;
	}	

	@Override
	public double activationFunction(double x) {
//		if (x < -20.0) {
//			return -1.0;
//		} else if (x > 20.0) {
//			return 1.0;
//		}
//		return Math.tanh(x);
//		if( x >= 0 )
//			return x;
//		else
//			return -1;
		return x;
	}
}
