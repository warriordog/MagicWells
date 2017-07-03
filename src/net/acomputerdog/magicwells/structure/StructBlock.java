package net.acomputerdog.magicwells.structure;

import org.bukkit.Material;

class StructBlock {
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
}
