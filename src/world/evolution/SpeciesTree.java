package world.evolution;

import java.util.HashMap;

public enum SpeciesTree {
	INSTANCE;
	private HashMap<Integer, Species> speciesHashMap = new HashMap<>();
	public void add(Species species){
		int key = species.getHash();
		if(!speciesHashMap.containsKey(key)) return;
		speciesHashMap.put(key, species);
	}
	public void remove(int key){
		speciesHashMap.remove(key);
	}

}
