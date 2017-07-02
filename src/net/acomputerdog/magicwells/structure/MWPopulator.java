package net.acomputerdog.magicwells.structure;

import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.generator.BlockPopulator;

import java.util.HashSet;
import java.util.Random;
import java.util.Set;

public class MWPopulator extends BlockPopulator {
    private final StructureManager manager;
    private final float wellChance;
    private final Set<Material> avoidBlocks;

    public MWPopulator(StructureManager manager) {
        this.manager = manager;
        this.wellChance = (float) manager.getPlugin().getConfig().getDouble("well_gen_chance", 0.0d);
        if (wellChance == 0.0d) {
            getStructureManager().getPlugin().getLogger().info("Well generation is disabled.");
        }

        this.avoidBlocks = new HashSet<>();
        for (String name : manager.getPlugin().getConfig().getStringList("gen_avoid_blocks")) {
            Material mat = Material.getMaterial(name);
            if (mat == null) {
                manager.getPlugin().getLogger().warning("Unknown material: " + name);
            } else {
                avoidBlocks.add(mat);
            }
        }
    }

    public boolean canReplaceBlock(Block block) {
        return !avoidBlocks.contains(block.getType());
    }

    @Override
    public void populate(World world, Random random, Chunk source) {
        if (random.nextFloat() < wellChance) {

            int x, y, z;
            int numTries = 0;
            do {
                x = random.nextInt(16);
                z = random.nextInt(16);
                y = findGround(source, x, z);

                numTries++;
                if (numTries > 3) {
                    break; // give up
                }
            } while (!tryGenWell(world, source, x, y, z));
        }
    }

    private boolean tryGenWell(World world, Chunk chunk, int x, int y, int z) {
        // make sure that we do not generate through the bottom of the chunk
        if (y < manager.getWellStruct().getHeight()) {
            //getStructureManager().getPlugin().getLogger().info("Source too low, cancelling.");
            return false;
        }

        int endX = x + manager.getWellStruct().getWidth();
        int endZ = z + manager.getWellStruct().getLength();

        //make sure that this well will not generate into a "corner" chunk that may not be loaded
        if (endX > 15 && endZ > 15) {
            return false;
        }

        Location loc = new Location(world, (chunk.getX() * 16) + x, y, (chunk.getZ() * 16) + z);
        manager.getWellStruct().generate(loc);
        return true;
    }

    //make sure that well does not conflict with any important
    private static int findGround(Chunk c, int x, int z) {
        for (int y = 255; y >= 0; y--) {
            if (c.getBlock(x, y, z).getType().isSolid()) {
                return y;
            }
        }
        return -1;
    }

    public StructureManager getStructureManager() {
        return manager;
    }
}
