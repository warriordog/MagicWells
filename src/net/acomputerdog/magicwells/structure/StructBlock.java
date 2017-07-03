package net.acomputerdog.magicwells.structure;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;

class StructBlock {
    // temporary shared location instance used to avoid allocating too many objects
    private static final Location tempLocation = new Location(null, 0, 0, 0);

    private final Material block;
    private final int xOff;
    private final int yOff;
    private final int zOff;

    public StructBlock(Material block, int xOff, int yOff, int zOff) {
        this.block = block;
        this.xOff = xOff;
        this.yOff = yOff;
        this.zOff = zOff;
    }

    public Material getBlock() {
        return block;
    }

    public int getXOff() {
        return xOff;
    }

    public int getYOff() {
        return yOff;
    }

    public int getZOff() {
        return zOff;
    }

    /**
     * Checks if a location relative to a base position is equal to the position of this component
     *
     * @param root The location to use as a base
     * @param loc  The real location
     * @return return true if the location matches, false otherwise
     */
    public boolean locationMatches(Location root, Location loc) {
        int blockX = calcOff(root.getBlockX(), loc.getBlockX());
        if (blockX != xOff) {
            return false;
        }

        int blockY = calcOff(root.getBlockY(), loc.getBlockY());
        if (blockY != yOff) {
            return false;
        }

        int blockZ = calcOff(root.getBlockZ(), loc.getBlockZ());
        if (blockZ != zOff) {
            return false;
        }

        return true;
    }

    /**
     * Checks if a block matches this component as a part of the structure at the specified location
     *
     * @param root  The location of the structure to use as a base
     * @param block The block to check
     * @return return true if the block matches, false otherwise
     */
    public boolean blockMatches(Location root, Block block) {
        if (block == null) {
            return false;
        }
        if (block.getType() != this.block) {
            return false;
        }

        block.getLocation(tempLocation);

        int blockX = calcOff(root.getBlockX(), tempLocation.getBlockX());
        if (blockX != xOff) {
            return false;
        }

        int blockY = calcOff(root.getBlockY(), tempLocation.getBlockY());
        if (blockY != yOff) {
            return false;
        }

        int blockZ = calcOff(root.getBlockZ(), tempLocation.getBlockZ());
        if (blockZ != zOff) {
            return false;
        }

        return true;
    }

    /**
     * Calculates the local offset of a position from a root position
     */
    private static int calcOff(int root, int loc) {
        return loc - root;
    }
}
