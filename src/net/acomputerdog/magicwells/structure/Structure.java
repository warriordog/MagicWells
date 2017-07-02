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
    private final int width; //x
    private final int height; //y
    private final int length; //z

    public Structure(MWPopulator populator, BufferedReader reader) throws IOException {
        this.populator = populator;

        // ready() call reads in the line that is read as "first"
        if (!reader.ready()) {
            throw new IllegalArgumentException("Input file is empty.");
        }
        String first = reader.readLine();
        String[] firstParts = first.split(",");
        if (firstParts.length != 3) {
            throw new IllegalArgumentException("Input file does not include dimensions.");
        }

        try {
            this.width = Integer.parseInt(firstParts[0]);
            this.length = Integer.parseInt(firstParts[1]);
            this.height = Integer.parseInt(firstParts[2]);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Input file has non-integer dimensions.");
        }

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
                        structBlocks.add(new StructBlock(mat, aX, aY, aZ));
                    }
                }
            }
        }
        this.components = structBlocks.toArray(new StructBlock[structBlocks.size()]);
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

    public void generate(Location l) {
        if (l.getWorld() == null) {
            throw new IllegalArgumentException("Location must include a world to generate in.");
        }

        World w = l.getWorld();
        for (StructBlock sb : components) {
            // set the block
            Block block = w.getBlockAt(l.getBlockX() + sb.getXOff(), l.getBlockY() - sb.getYOff(), l.getBlockZ() + sb.getZOff());
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
}
