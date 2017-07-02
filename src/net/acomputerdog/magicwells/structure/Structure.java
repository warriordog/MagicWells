package net.acomputerdog.magicwells.structure;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.*;

/**
 * An optimized class for storing, generating, and comparing a structure
 */
public class Structure implements Iterable<StructBlock> {
    private final MWPopulator populator;

    // the components of the structure stored in XZY order
    private final StructBlock[] components;
    private final int width; //x
    private final int height; //y
    private final int length; //z

    public Structure(MWPopulator populator, BufferedReader reader) throws IOException {
        this.populator = populator;

        List<StructBlock> blocks = new ArrayList<>();
        Map<String, String> props = new HashMap<>();
        while (reader.ready()) {
            String line = reader.readLine().trim();
            if (!line.isEmpty() && !line.startsWith("#")) {
                int eq = line.indexOf('=');
                // read a property
                if (eq >= 0 && line.length() - eq >= 2) {
                    String key = line.substring(0, eq);
                    String val = line.substring(eq + 1);
                    props.put(key, val);
                    // read a block
                } else {
                    String[] parts = line.split(",");
                    if (parts.length == 4) {
                        try {
                            int x = Integer.parseInt(parts[0]);
                            int y = Integer.parseInt(parts[1]);
                            int z = Integer.parseInt(parts[2]);
                            Material mat = Material.getMaterial(parts[3]);

                            if (mat != null) {
                                // add the block
                                blocks.add(new StructBlock(mat, x, y, z));
                            } else {
                                throw new IllegalArgumentException("Unknown material: " + line);
                            }
                        } catch (NumberFormatException e) {
                            throw new IllegalArgumentException("Malformed coordinate: " + line);
                        }
                    } else {
                        throw new IllegalArgumentException("Malformed line: " + line);
                    }
                }
            }
        }
        this.components = blocks.toArray(new StructBlock[blocks.size()]);

        try {
            width = Integer.parseInt(props.get("width"));
            height = Integer.parseInt(props.get("height"));
            length = Integer.parseInt(props.get("length"));
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Malformed dimension.");
        }
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
