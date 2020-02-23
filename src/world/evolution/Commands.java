package world.evolution;

public interface Commands {
	//NUTRITION
	byte PHOTOSYNTHESIS = 32; // Get some energy depending on brightness level. If there is no light, the bot will loose some energy
	byte EAT = 34; // - Try to eat the thing, th bot is looking at. If not possible - make conditional redirect.
	byte CONSUME_MINERALS = 30; // - Consume some minrals from the environment, depending on mineral level.
	byte EAT_MINERALS = 33; // - convert some minerals to energy.  Produces waste.

	//REPRODUCTION
	byte DIVIDE = 16; // - make a free and equal clone; The child cell gets minerals/2 minerals and energy/2 energy. parent cell gets minerals - minerals/2 minerals and energy - energy/2 energy;
	byte SPAWN = 22; // - spawn a kid with minimal amount of resources.
	byte GROW = 24; // - spawn an attached colony cell. thus growing into a colony.

	//SENSORS
	byte SURROUNDED = 46; //- do something if bot is surrounded
	byte CHECK_ENERGY = 47; //- checks energy level; The next byt*15 determines the threshold. +2 if below threshold, +3 if above or = threshold;
	byte CHECK_LIGHT = 48; //- Check light in the tile where the cell is.
	byte CHECK_MINERALS = 40; //- like check energy, but with minerals;
	byte LOOK_AROUND = 49; //- look around. Act when 1st not empty cell found or all cells are empty.

	//MOTION
	byte MOVE = 1; //- move int the direction, the cell is looking.
	byte TURN_HEAD = 23; //- change cell direction;
	byte TAXIS = 25;

	//COLONY INTERACTIONS
	byte SHARE = 63;
	byte GIVE = 62;
	byte CHECK_COLONY = 61; // 0-16 as binary signal from colony mates and their position
	byte DO_LIKE_ME = 59; // - tell colony mates to repeat an action.

	//EXTRA


	//Directions. To turn, move, divide, etc
	int UP_LEFT = 0;
	int UP = 1;
	int UP_RIGHT = 2;
	int RIGHT = 3;
	int LEFT = 5;
	int DOWN_RIGHT = 6;
	int DOWN = 7;
	int DOWN_LEFT = 8;

}
