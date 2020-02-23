package world.evolution;

public class Species {
	private final byte[]genome;
	private final int hash;
	private static final String UNKNOWN = "unknown species";

	public void setName(String name) {
		this.name = name;
	}

	private int population;
	private String name;
	public final static int GENOME_SIZE = 64;
    public final static int MUTATION_FACTOR = 8;

	public Species(byte[] genome) {
		this.genome = genome;
		hash = genome.hashCode();
		population = 0;
		SpeciesTree.INSTANCE.add(this);
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

	public String getName() {
		return name == null? String.format("%s [%d]", UNKNOWN, hash) : name;
	}
}
