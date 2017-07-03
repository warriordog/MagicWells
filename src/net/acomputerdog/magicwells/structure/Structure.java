package net.acomputerdog.magicwells.structure;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * An optimized class for storing, generating, and comparing a structure
 */
public class Structure implements Iterable<StructBlock> {
    private final MWPopulator populator;

    // the components of the structure stored in X+ Z+ Y- order
    private final StructBlock[] components;

    // structure size and position
    private final int width; //x
    private final int height; //y
    private final int length; //z
    private final int offset; //distance to move upwards before generating

    //trigger block
    private final Material triggerBlock; //trigger block for this structure
    private final int triggerOffX, triggerOffY, triggerOffZ;

    // bounding box
    private final int bbX1, bbY1, bbZ1, bbX2, bbY2, bbZ2;

    // shared Location instance
    private final Location tempLocation = new Location(null, 0, 0, 0);

    //This is a huge constructor, but it has to be like this because fields are final and cannot
    //  be set from other methods
    public Structure(MWPopulator populator, BufferedReader reader) throws IOException {
        this.populator = populator;

        // Read in the dimensions
        if (!reader.ready()) {
            throw new IllegalArgumentException("Input file is empty.");
        }
        String first = reader.readLine();
        String[] firstParts = first.split(",");
        if (firstParts.length != 4) {
            throw new IllegalArgumentException("Input file does not include dimensions.");
        }
        try {
            this.width = Integer.parseInt(firstParts[0]);
            this.length = Integer.parseInt(firstParts[1]);
            this.height = Integer.parseInt(firstParts[2]);
            this.offset = Integer.parseInt(firstParts[3]);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Input file has non-integer dimensions.");
        }

        // read in the trigger block material
        if (!reader.ready()) {
            throw new IllegalArgumentException("Input file is cut short.");
        }
        String second = reader.readLine().trim();
        Material triggerMat = Material.getMaterial(second);
        if (triggerMat == null) {
            throw new IllegalArgumentException("Trigger block is unknown");
        }
        this.triggerBlock = triggerMat;

        // read in the bounding box
        if (!reader.ready()) {
            throw new IllegalArgumentException("Input file is cut short.");
        }
        String third = reader.readLine();
        String[] thirdParts = third.split(",");
        if (thirdParts.length != 6) {
            throw new IllegalArgumentException("Input file does not include bounding box.");
        }
        try {
            this.bbX1 = Integer.parseInt(thirdParts[0]);
            this.bbY1 = height - Integer.parseInt(thirdParts[1]) - offset;
            this.bbZ1 = Integer.parseInt(thirdParts[2]);
            this.bbX2 = Integer.parseInt(thirdParts[3]);
            this.bbY2 = height - Integer.parseInt(thirdParts[4]) - offset;
            this.bbZ2 = Integer.parseInt(thirdParts[5]);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Input file has non-integer bounding box.");
        }

        // Read in the blocks
        // YZX order for efficiency
        String[][][] blocks = new String[height][length][width];
        int y = 0; // read in normal order so it gets flipped below
        int z = 0;

        // read layers in Y Z X order
        while (reader.ready()) {
            // don't trim here, some lines may have spaces
            String line = reader.readLine();
            if (!line.isEmpty() && !line.startsWith("#")) {
                // must be at the top, because last layer will end at -1
                if (y >= height) {
                    throw new IllegalArgumentException("Input file has too many layers.");
                }

                String[] lineParts = line.split(",");
                if (lineParts.length != width) {
                    System.out.printf("Wrong length: %d", lineParts.length);
                    throw new IllegalArgumentException("Layer strip is wrong length: " + line);
                }

                // copy part of line to out array
                System.arraycopy(lineParts, 0, blocks[y][z], 0, width);

                // move to next layer strip
                z++;

                // if this is the last strip, then go to next layer
                if (z >= length) {
                    z = 0;
                    y++;
                }
            }
        }

        // Convert 3D array to 1D "packed" array that skips empty spaces
        // can't assign a final in a loop
        int tOffX = 0;
        int tOffY = 0;
        int tOffZ = 0;
        boolean triggerFound = false;

        List<StructBlock> structBlocks = new ArrayList<>();
        for (int aY = height - 1; aY >= 0; aY--) {
            for (int aZ = 0; aZ < length; aZ++) {
                for (int aX = 0; aX < width; aX++) {
                    String blockName = blocks[aY][aZ][aX].trim();
                    // skip empty spaces
                    if (!blockName.isEmpty() && !".".equals(blockName)) {
                        Material mat = Material.getMaterial(blockName);
                        if (mat == null) {
                            throw new IllegalArgumentException("Unknown material: " + blockName);
                        }
                        if (mat == triggerBlock) {
                            if (!triggerFound) {
                                tOffX = aX;
                                tOffY = aY;
                                tOffZ = aZ;
                                triggerFound = true;
                            } else {
                                throw new IllegalArgumentException("Input structure has multiple trigger blocks.");
                            }
                        }
                        structBlocks.add(new StructBlock(mat, aX, aY, aZ));
                    }
                }
            }
        }
        this.components = structBlocks.toArray(new StructBlock[structBlocks.size()]);

        // make sure that the trigger block showed up somewhere in the file
        if (!triggerFound) {
            throw new IllegalArgumentException("Input structure has not trigger block.");
        }
        this.triggerOffX = tOffX;
        this.triggerOffY = tOffY;
        this.triggerOffZ = tOffZ;
    }

    public void generate(Location l) {
        if (l.getWorld() == null) {
            throw new IllegalArgumentException("Location must include a world to generate in.");
        }

        World w = l.getWorld();
        for (StructBlock sb : components) {
            // set the block
            Block block = w.getBlockAt(l.getBlockX() + sb.getXOff(), l.getBlockY() - sb.getYOff() + offset, l.getBlockZ() + sb.getZOff());
            if (populator.canReplaceBlock(block)) {
                block.setType(sb.getBlock());
            }
        }
    }

    @Override
    public Iterator<StructBlock> iterator() {
        return new Iterator<StructBlock>() {
            private int i = 0;

            @Override
            public boolean hasNext() {
                return i < components.length;
            }

            @Override
            public StructBlock next() {
                StructBlock next = components[i];
                i++;
                return next;
            }
        };
    }

    public int getOffset() {
        return offset;
    }

    public Material getTriggerBlock() {
        return triggerBlock;
    }

    public int getTriggerOffX() {
        return triggerOffX;
    }

    public int getTriggerOffY() {
        return triggerOffY + offset;
    }

    public int getTriggerOffZ() {
        return triggerOffZ;
    }

    public Location getBB1() {
        return new Location(null, bbX1, bbY1, bbZ1);
    }

    public Location getBB2() {
        return new Location(null, bbX2, bbY2, bbZ2);
    }

    public StructBlock[] getComponents() {
        return components;
    }

    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }

    public int getLength() {
        return length;
    }

    /*
        Calculates the offset of "loc" from "root" and stores it in "out"
     */
    private static void calcLocalOffset(Location root, Location loc, Location out) {
        out.setWorld(root.getWorld());
        out.setX(loc.getBlockX() - root.getBlockX());
        out.setY(loc.getBlockY() - root.getBlockY());
        out.setZ(loc.getBlockZ() - root.getBlockZ());
    }
}
