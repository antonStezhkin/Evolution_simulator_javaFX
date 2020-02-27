package world.evolution;

import java.util.Random;

public class Species {
	private final byte[]genome;
	private final int hash;
	private final int geneticDistance;
	private static final String UNKNOWN = "unknown";
	private static int idCounter = 0;
	public final boolean acidResistant;
	int id = 0;

	public void setName(String name) {
		this.name = name;
	}

	private int population;
	private String name;
	public final static int GENOME_SIZE = 64;
    public final static int MUTATION_FACTOR = 10;

	public Species(byte[] genome) {
		this.genome = genome;
		hash = genome.hashCode();
		population = 0;
		geneticDistance = 0;
		SpeciesTree.INSTANCE.add(this);
		id = ++idCounter;
		boolean r = false;
		for(int i =0; i<genome.length; i++){
			if(genome[i] == Commands.RESIST){ r = true; break;}
		}
		acidResistant = r;
	}

	private Species(byte[] genome, int geneticDistance) {
		this.genome = genome;
		hash = genome.hashCode();
		population = 0;
		this.geneticDistance = geneticDistance;
		SpeciesTree.INSTANCE.add(this);
		id = ++idCounter;
		boolean r = false;
		for(int i =0; i<genome.length; i++){
			if(genome[i] == Commands.RESIST){ r = true; break;}
		}
		acidResistant = r;
	}

	public int getHash() {
		return hash;
	}

	public int getPopulation() {
		return population;
	}

	public void increasePopulation(){
		population++;
	}
	public void decreasePopulation(){
		population--;
		if(population < 1) extinct();
	}

	private void extinct(){
		SpeciesTree.INSTANCE.remove(hash);
	}

	public byte[] getGenome() {
		return genome;
	}

	public Species mutate(){
		int random = new Random().nextInt(64*64);
		byte[] mutated =genome.clone();
		byte mutation = (byte) (random / Species.GENOME_SIZE);
		int mutationIndex = random % Species.GENOME_SIZE;
		mutated[mutationIndex] = mutation;
		return new Species(mutated, geneticDistance+1);
	}

	public String getName() {
		return name == null? String.format("%s [%d] distance: %d, population: %d", UNKNOWN, id, geneticDistance, population) : String.format("%s, distance: %d, population: %d", name, geneticDistance, population);
	}
}
