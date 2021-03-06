package world.evolution;

public interface Commands {
	//NUTRITION
	byte PHOTOSYNTHESIS = 32; // Get some energy depending on brightness level. If there is no light, the bot will loose some energy
	byte EAT = 34; // - Try to eat the thing, th bot is looking at. If not possible - make conditional redirect.
	byte PUMP_MINERALS = 30; // - Consume some minrals from the environment, depending on mineral level.
	byte EAT_MINERALS = 33; // - convert some minerals to energy.  Produces waste.
	byte EAT_POOP = 35;

	//REPRODUCTION
	byte DIVIDE = 16; // - make a free and equal clone; The child cell gets minerals/2 minerals and energy/2 energy. parent cell gets minerals - minerals/2 minerals and energy - energy/2 energy;
	byte SPAWN = 22; // - spawn a kid with genetically determined amount of resources.
	byte GROW = 24; // - spawn an attached colony cell. thus growing into a colony.
	byte NO_DIVISION = 25;// - no division on this turn even if enough organic and minerals

	//SENSORS
	byte SURROUNDED = 46; //- do something if bot is surrounded
	byte CHECK_ENERGY = 47; //- checks energy level; The next byt*15 determines the threshold. +2 if below threshold, +3 if above or = threshold;
	byte CHECK_LIGHT = 48; //- Check light in the tile where the cell is.
	byte CHECK_MINERALS = 40; //- like check energy, but with minerals;
	byte CHECK_RELATION = 45;
	byte LOOK_AROUND = 49; //- look around. Act when 1st not empty cell found or all cells are empty.

	//MOTION
	byte MOVE = 1; //- move int the direction, the cell is looking.
	byte TURN_HEAD = 3; //- change cell direction;
	byte TAXIS = 5;
	byte GAS = 6;
	byte SINK = 7;

	//COLONY INTERACTIONS
	byte SHARE = 63;
	byte GIVE = 62;
	byte CHECK_COLONY = 61; // 0-16 as binary signal from colony mates and their position
	byte DO_LIKE_ME = 60; // - tell colony mates to repeat an action.

	//EXTRA
	byte ACID = 50;
	byte DESTROY_CORPSE = 51;
	byte POISON = 52;
	byte SLIME = 53;
	byte RELEASE_MINERALS = 54;
	byte RESIST = 56;


	//Directions. To turn, move, divide, etc
	int UP_LEFT = 0;
	int UP = 1;
	int UP_RIGHT = 2;
	int RIGHT = 3;
	int LEFT = 5;
	int DOWN_RIGHT = 6;
	int DOWN = 7;
	int DOWN_LEFT = 8;

	//results
	int SUCCESS = 1;
	int INVALID_PARAM = 2;
	int WORLD_BOTTOM = 3;
	int STARVING = 4;
	int CLONE = 5;
	int RELATIVE = 6;
	int ALIEN = 7;
	int WORLD_TOP = 8;
}
