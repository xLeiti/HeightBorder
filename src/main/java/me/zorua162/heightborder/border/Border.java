package me.zorua162.heightborder.border;


import com.github.yannicklamprecht.worldborder.api.IWorldBorder;
import com.github.yannicklamprecht.worldborder.api.WorldBorderAction;
import com.github.yannicklamprecht.worldborder.api.WorldBorderApi;
import me.zorua162.heightborder.HeightBorder;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.configuration.serialization.ConfigurationSerializable;
import org.bukkit.configuration.serialization.SerializableAs;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDamageEvent;

import java.util.*;
import java.util.logging.Logger;

import static java.lang.Math.abs;

@SerializableAs("Border")
public class Border implements ConfigurationSerializable {
    // Things that need to be stored
    double currentHeight;
    // Height at which border stops moving
    double endHeight;

    double displayBorderDistance = 11.0;
    Boolean displayBorderBasedOnDistance = true;

    // top or bottom, stored as "up" or "bottom" in direction
    String direction;
    // velocity
    double velocity;
    // front left and back right position: pos1 pos2
    Location pos1;
    Location pos2;
    // Particle colour (red default, but this will be changed if it isn't moving)
    Color particleColour = Color.fromRGB(255, 0, 0);
    int numberOfParticles;
    // Particle colours change from one to the other, gives a more varied colour to the world border
    Boolean damagePlayers;
    Boolean breakBlocks;
    Boolean displayBorderParticles;
    Logger log;
    Map<String, Color> colorMap = new HashMap<>();
    // Current tick, when this is equal to damage pause any player outside border is damaged or that of other tasks
    int tickCount = 0;
    // Default pauses between different tasks, this will eventually be moved to config
    int damageWait;
    int breakWait;
    int displayWait;
    int moveWait;
    int previousBreakLayer = 0;
    WarningManager warningManager;

    private Boolean stopped;

    public Border(double currentHeight, double endHeight, String direction, double velocity, Location pos1,
                  Location pos2, Boolean damagePlayers, Boolean breakBlocks, Boolean displayBorderParticles,
                  Integer numberOfParticles, int damageWait, int breakWait, int displayWait, int moveWait,
                  Boolean stopped) {
        this.currentHeight = currentHeight;
        this.endHeight = endHeight;
        this.direction = direction;
        this.velocity = velocity;
        this.pos1 = pos1;
        this.pos2 = pos2;
        this.damagePlayers = damagePlayers;
        this.breakBlocks = breakBlocks;
        this.displayBorderParticles = displayBorderParticles;
        this.numberOfParticles = numberOfParticles;
        this.damageWait = damageWait;
        this.breakWait = breakWait;
        this.displayWait = displayWait;
        this.moveWait = moveWait;
        this.stopped = stopped;
        putMapColours();
    }

    public void setManager(WarningManager warningManager) {
        this.warningManager = warningManager;
    }

    public void putMapColours() {
        colorMap.put("moving", Color.fromRGB(255, 0, 0));
        // colorMap.put("green", Color.fromRGB(0, 255, 0));
        colorMap.put("stopped", Color.fromRGB(0, 0, 255));
    }

    @Override
    public Map<String, Object> serialize() {
        LinkedHashMap<String, Object> result = new LinkedHashMap<>();
        result.put("currentheight", this.getCurrentHeight());
        result.put("endheight", this.getEndHeight());
        result.put("direction", String.valueOf(this.getDirection()));
        result.put("velocity", this.getVelocity());
        result.put("pos1", this.getPos1());
        result.put("pos2", this.getPos2());
        result.put("damagePlayers", this.getDamagePlayers());
        result.put("breakBlocks", this.getBreakBlocks());
        result.put("displayBorderParticles", this.getDisplayBorderParticles());
        result.put("numberOfParticles", this.getNumberOfParticles());
        result.put("damageWait", this.getDamageWait());
        result.put("breakWait", this.getBreakWait());
        result.put("displayWait", this.getDisplayWait());
        result.put("moveWait", this.getMoveWait());
        result.put("stopped", this.getStopped());
        return result;
    }

    private Boolean getStopped() { return stopped; }

    private int getMoveWait() {
        return displayWait;
    }

    private int getDisplayWait() {
        return displayWait;
    }

    private int getBreakWait() {
        return breakWait;
    }

    private int getDamageWait() {
        return damageWait;
    }


    private Boolean getDisplayBorderParticles() {
        return displayBorderParticles;
    }

    private Boolean getBreakBlocks() {
        return breakBlocks;
    }

    private Boolean getDamagePlayers() {
        return damagePlayers;
    }

    private Integer getNumberOfParticles() {
        return numberOfParticles;
    }

    public double getEndHeight() {
        return endHeight;
    }

    public double getCurrentHeight() {
        return currentHeight;
    }

    private Location getPos2() {
        return this.pos2;
    }

    private Location getPos1() {
        return this.pos1;
    }

    public String getDirection() {
        return this.direction;
    }

    private Double getVelocity() {
        return this.velocity;
    }

    public static Border deserialize(Map<String, Object> args) {
        // Some default deserialize values so that errors aren't thrown, could cause issues in future.
        double currentHeight = 256;
        double endHeight = 128;
        String direction = "down";
        double velocity = 0;
        Location pos1 = null;
        Location pos2 = null;
        Boolean damagePlayers = null;
        Boolean breakBlocks = null;
        Boolean displayBorderParticles = null;
        int damageWait = 20;
        int breakWait = 20;
        int displayWait = 10;
        int moveWait = 10;
        Boolean stopped = null;

        int numberOfParticles = 100;

        if (args.containsKey("currentheight")) {
            currentHeight = (Double) args.get("currentheight");
        }

        if (args.containsKey("endheight")) {
            endHeight = (Double) args.get("endheight");
        }

        if(args.containsKey("direction")) {
            direction = ((String)args.get("direction"));
        }

        if(args.containsKey("velocity")) {
            velocity = (Double) args.get("velocity");
        }

        if(args.containsKey("pos1")) {
            pos1 = ((Location)args.get("pos1"));
        }

        if(args.containsKey("pos2")) {
            pos2 = ((Location)args.get("pos2"));
        }

        if(args.containsKey("damagePlayers")) {
            damagePlayers = ((Boolean)args.get("damagePlayers"));
        }

        if(args.containsKey("breakBlocks")) {
            breakBlocks = ((Boolean)args.get("breakBlocks"));
        }

        if(args.containsKey("displayBorderParticles")) {
            displayBorderParticles = ((Boolean)args.get("displayBorderParticles"));
        }

        if(args.containsKey("numberOfParticles")) {
            numberOfParticles = ((Integer)args.get("numberOfParticles"));
        }

        if(args.containsKey("damageWait")) {
            damageWait = ((Integer)args.get("damageWait"));
        }

        if(args.containsKey("breakWait")) {
            breakWait = ((Integer)args.get("breakWait"));
        }

        if(args.containsKey("displayWait")) {
            displayWait = ((Integer)args.get("displayWait"));
        }

        if(args.containsKey("moveWait")) {
            moveWait = ((Integer)args.get("moveWait"));
        }
        if (args.containsKey("stopped")) {
            stopped = ((boolean) args.get("stopped"));
        }
        return new Border(currentHeight, endHeight, direction, velocity, pos1, pos2, damagePlayers, breakBlocks,
                displayBorderParticles, numberOfParticles, damageWait, breakWait, displayWait, moveWait, stopped);
    }

    public String getListInfo() {
        return " - Y = " + currentHeight +
                "\n - End height = " + endHeight +
                "\n - Direction = " + direction +
                "\n - Velocity = " + velocity +
                "\n - Pos1 = " + pos1.toString() +
                "\n - Pos2 = " + pos2.toString() +
                "\n - Particle colour = " + particleColour.toString() +
                "\n - Damage players = " + damagePlayers +
                "\n - Break blocks = " + breakBlocks +
                "\n - Display border particles = " + displayBorderParticles +
                "\n - Number of particles = " + numberOfParticles +
                "\n - Damage pause = " + damageWait +
                "\n - Break pause = " + breakWait +
                "\n - Display pause = " + displayWait +
                "\n - move pause = " + moveWait +
                "\n - stopped = " + stopped;
    }

    private int getMax(int n1, int n2) {
        return Math.max(n1, n2);
    }

    private int getMin(int n1, int n2) {
        return Math.min(n1, n2);
    }

    private List<Integer> getBorders(){
        List<Integer> outList;
        // Get positions that make up corner of the border to be shown
        int x1 = pos1.getBlockX();
        int x2 = pos2.getBlockX();
        int z1 = pos1.getBlockZ();
        int z2 = pos2.getBlockZ();
        // set up for for loop
        int startx = getMin(x1, x2);
        int endx = getMax(x1, x2);
        int startz = getMin(z1, z2);
        int endz = getMax(z1, z2);
        outList = Arrays.asList(startx, endx, startz, endz);
        return outList;
    }

    public void displayBorder() {
        // Display the border as particles in the world
        // get start and end position for the for nested 2d for loops that loop over the border
        List<Integer> borders = getBorders();
        int startx = borders.get(0);
        int endx = borders.get(1);
        int startz = borders.get(2);
        int endz = borders.get(3);

        if (!displayBorderParticles) {
            return;
        }

        // do not display if lower then y = -200, as this would be unecessary
        if (currentHeight < -200) {
            return;
        }

        World world = pos1.getWorld();
        // Only this set number of particles is created to reduce client lag
        // Scale number of particles to the required size

        // Calculate step size and if it would be less than 1 then set it to 1
        int stepx = getStep(startx, endx);
        int stepz = getStep(startz, endz);
        //
        for (int x=startx; x < endx + stepx; x = x + stepx) {
           for (int z=startz; z < endz + stepz; z = z + stepz) {
               Location currentLoc = new Location(world, x, currentHeight, z);
               // Only the same colour is used, because in testing this significantly reduced client side lag
               // Particle.DustTransition dustOptions = new Particle.DustTransition(Color.fromRGB(255, 0, 0), Color.fromRGB(255, 0, 0), 10.0F);
               // Particle.DustOptions dustOptions = new Particle.DustOptions(Color.fromRGB(255, 0, 0), 10.0F);
               Particle.DustOptions dustOptions = new Particle.DustOptions(particleColour, 10.0F);
               world.spawnParticle(Particle.REDSTONE , currentLoc, 1, 0, 0, 0,
                       1, dustOptions, true);

           }
        }
    }

    public void displayBorderBasedOnDistance(Player player) {
        // Display the border as particles in the world
        // get start and end position for the for nested 2d for loops that loop over the border
        List<Integer> borders = getBorders();

        Double size = player.getWorld().getWorldBorder().getSize()/2;

        //just to make sure it doesn't scan the entire world in case there isn't a worldborder setup.
        if(size > 200)
            size = 200.0;

        int startx = (int) Math.floor(-size) -1;
        int endx = (int) Math.ceil(size);
        int startz = (int) Math.floor(-size) -1;
        int endz = (int) Math.ceil(size);

        if (!displayBorderParticles) {
            return;
        }

        if(!player.getWorld().equals(pos1.getWorld())){
            return;
        }


        // do not display if lower then y = -200, as this would be unecessary
        if (currentHeight < -200) {
            return;
        }

        double offset = 0;
        if(direction.equals("down")){
            offset = 0.25;
        }else if(direction.equals("up")){
            offset = -0.25;
        }

        World world = pos1.getWorld();
        // Only this set number of particles is created to reduce client lag
        // Scale number of particles to the required size

        // Calculate step size and if it would be less than 1 then set it to 1
        int stepx = 1;
        int stepz = 1;
        double distance;
        //
        for (int x=startx; x < endx + stepx; x = x + stepx) {
            for (int z=startz; z < endz + stepz; z = z + stepz) {

                //We don't need to render particles which we cant see
                //further optimisation can be done with client side mods
                if(world.getBlockAt(x, (int) Math.ceil(currentHeight), z).getType().isSolid()&&world.getBlockAt(x, (int) Math.floor(currentHeight), z).getType().isSolid()){
                    continue;
                }
                //Drawing the dense border when the player is close
                distance = Math.sqrt(Math.pow(x-player.getLocation().getX(),2)+Math.pow(z-player.getLocation().getZ(),2)+Math.pow(currentHeight-player.getLocation().getY(),2));
                if(distance<displayBorderDistance){
                    Location currentLoc = new Location(world, x+0.5, currentHeight+offset, z+0.5);
                    // Only the same colour is used, because in testing this significantly reduced client side lag
                    // Particle.DustTransition dustOptions = new Particle.DustTransition(Color.fromRGB(255, 0, 0), Color.fromRGB(255, 0, 0), 10.0F);
                    // Particle.DustOptions dustOptions = new Particle.DustOptions(Color.fromRGB(255, 0, 0), 10.0F);
                    Particle.DustOptions dustOptions = new Particle.DustOptions(particleColour, 20.0F);
                    player.spawnParticle(Particle.REDSTONE , currentLoc, 1, 0, 0, 0,
                            1, dustOptions);

                //Drawing a rough grid when player is further away
                /*}else if((distance >=displayBorderDistance) && (distance < 2.5*displayBorderDistance)){
                    if(x % 10 == 0 || z % 10 == 0){
                        Location currentLoc = new Location(world, x+0.5, currentHeight, z+0.5);
                        // Only the same colour is used, because in testing this significantly reduced client side lag
                        // Particle.DustTransition dustOptions = new Particle.DustTransition(Color.fromRGB(255, 0, 0), Color.fromRGB(255, 0, 0), 10.0F);
                        // Particle.DustOptions dustOptions = new Particle.DustOptions(Color.fromRGB(255, 0, 0), 10.0F);
                        Particle.DustOptions dustOptions = new Particle.DustOptions(particleColour, 10.0F);
                        player.spawnParticle(Particle.REDSTONE , currentLoc, 1, 0, 0, 0,
                                1, dustOptions);
                    }*/
                }else if((distance >=displayBorderDistance) && (distance < 100)){
                    if((((x+10) % 20 == 0)&&(z % 2 == 0)) || (((z+10) % 20 == 0)&&(x % 2 == 0))){
                        Location currentLoc = new Location(world, x+0.5, currentHeight+offset, z+0.5);
                        // Only the same colour is used, because in testing this significantly reduced client side lag
                        // Particle.DustTransition dustOptions = new Particle.DustTransition(Color.fromRGB(255, 0, 0), Color.fromRGB(255, 0, 0), 10.0F);
                        // Particle.DustOptions dustOptions = new Particle.DustOptions(Color.fromRGB(255, 0, 0), 10.0F);
                        Particle.DustOptions dustOptions = new Particle.DustOptions(particleColour, 20.0F);
                        player.spawnParticle(Particle.REDSTONE , currentLoc, 1, 0, 0, 0,
                                1, dustOptions);
                    }
                }

            }
        }
    }

    private int getStep(int startx, int endx) {
        double checkStepX = (endx - startx) / Math.sqrt(numberOfParticles);
        if (checkStepX < 1) {
            return 1;
        } else {
            return (int) checkStepX;
        }
    }

    public void moveBorder() {
        // Velocity is given in blocks/minute
        // this function is called every tick
        // conversion of block/min to amount/tick
        // ticks in a second = 20
        // so it should move the number of blocks/s/20 every tick or blocks/min/20/60
        // however movement might be skipped due to moveWait, so increase change by number of ticks paused
        double heightChange = (velocity/20/60)* moveWait;
        if (direction.equals("down")){
            // Check if reached final height
            if (currentHeight <= endHeight) {
                // final height reached
                currentHeight = endHeight;
                particleColour = colorMap.get("stopped");
                return;
            }
            particleColour = colorMap.get("moving");
            currentHeight = currentHeight - heightChange;
        } else {
            if (currentHeight >= endHeight) {
                // final height reached
                currentHeight = endHeight;
                particleColour = colorMap.get("stopped");
                return;
            }
            currentHeight = currentHeight + heightChange;
        }

        // Set particle colour to blue if the border isn't moving
        if (velocity == 0) {
            particleColour = colorMap.get("stopped");
        } else {
            particleColour = colorMap.get("moving");
        }
    }

    public Boolean checkOutsideBorder(Player player) {
        // Check if the player should be damaged or not
        if (direction.equals("down")) {
            //Eyelocation because sneaking and crawling affects the distance
            return player.getEyeLocation().getY() + 0.25 > currentHeight;
        } else {
            //Playerfeet
            return player.getLocation().getY()  < currentHeight;

        }
    }

        public void doDamage() {
        // check if border should do damage
        if (!damagePlayers) {
            return;
        }
        // get players in border's world and damage if outside of it
        List<Player> players = pos1.getWorld().getPlayers();
        for (Player player: players) {
            if (checkOutsideBorder(player)) {
                //having random knockback is annyoing, hence no damage source
                player.damage(1);
            }
        }
    }

    public void breakBlocks() {
        if (!breakBlocks) {
            return;
        }
        World world = pos1.getWorld();
        List<Integer> borders = getBorders();
        int startx = borders.get(0);
        int endx = borders.get(1);
        int startz = borders.get(2);
        int endz = borders.get(3);
        for (int x=startx-1; x<=endx; x++){
            for (int z=startz-1; z<=endz; z++) {
                Block block = world.getBlockAt(x, (int) currentHeight, z);
                block.setType(Material.AIR);
            }
        }

        // irrespective of direction, all the blocks at the borders current y level should be broken by the time
        // time it has passed through them
        // Calculate time to pass through a block
        // velocity is in blocks per minute
        // velocity/60 = bps
        if ((int) currentHeight != previousBreakLayer) {
            previousBreakLayer = (int) currentHeight;
        }
    }

    public void setCurrentHeight(double value) {
        currentHeight = value;
    }

    public void setEndHeight(double value) {
        endHeight = value;
    }

    public void setDirection(String value) {
        direction = value;
    }

    public void setVelocity(double value) {
        velocity = value;
    }

    public void setPos(String pos, String value) {
        switch (pos) {
            case "pos1x":
                pos1.setX(Double.parseDouble(value));
            case "pos1z":
                pos1.setZ(Double.parseDouble(value));
            case "pos2x":
                pos2.setX(Double.parseDouble(value));
            case "pos2z":
                pos2.setZ(Double.parseDouble(value));
        }
    }

    public void setDamageWait(String value) {
        damageWait = Integer.parseInt(value);
    }

    public void setNumberOfParticles(String value) {
        numberOfParticles = Integer.parseInt(value);
    }

    public void setDisplayBorderParticles(String value) {
        if (value.equals("true")) {
            displayBorderParticles = true;
            return;
        }
        displayBorderParticles = false;
    }

    public void setBreakBlocks(String value) {
        if (value.equals("true")) {
            breakBlocks = true;
            return;
        }
        breakBlocks = false;
    }

    public void setDamagePlayers(String value) {
        if (value.equals("true")) {
            damagePlayers = true;
            return;
        }
        damagePlayers = false;
    }

    public void runTasks(HeightBorder plugin) {
        tickCount++;

        // Don't do anything to the border if
        if (stopped) {
            return;
        }

        // Call border methods, but only on the period defined by each method's pause variable
        if ((tickCount % damageWait) == 0) {
            doDamage();
        }
        if ((tickCount % breakWait) == 0) {
            breakBlocks();
        }
        if ((tickCount % moveWait) == 0) {
            moveBorder();
        }
        if ((tickCount % displayWait) == 0) {
            if(displayBorderBasedOnDistance){
                for(Player player : plugin.getServer().getOnlinePlayers()){
                    displayBorderBasedOnDistance(player);
                }
            }
            else{
                displayBorder();
            }
        }
        checkWarning(plugin);
    }

    public void setBreakWait(String value) {
        breakWait = Integer.parseInt(value);
    }

    public void setDisplayWait(String value) {
        displayWait = Integer.parseInt(value);
    }

    public void setMoveWait(String value) {
        moveWait = Integer.parseInt(value);
    }

    public void checkWarning(HeightBorder plugin) {
        // show the red "border near" warning to all players
        // Currently show warning no matter the border type
        List<Player> players = pos1.getWorld().getPlayers();
        for (Player player: players) {

            if (direction.equals("down")) {
                if (player.getLocation().getY() -2 > currentHeight) {
                    warningManager.setReddenPlayersScreen(plugin, player, this);
                } else {
                   warningManager.setUnReddenPlayersScreen(plugin, player, this);
                }
            } else {
                if (player.getLocation().getY() - 1 < currentHeight) {
                    warningManager.setReddenPlayersScreen(plugin, player, this);
                } else {
                    warningManager.setUnReddenPlayersScreen(plugin, player, this);
                }
            }
        }
    }

    public Integer getDistance(Player player) {
        // Return a string containing info about where the border is in comparison to the player
        double heightDifference = player.getLocation().getY() - currentHeight;
        int roundHeightDifference = (int) heightDifference;
        return roundHeightDifference;
    }

    public void startBorder() {
        stopped = false;
    }
}
