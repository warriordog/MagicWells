package net.acomputerdog;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;

public class StructureGenTool {
    public static void main(String[] args) {
        if (args.length != 5) {
            System.out.println("Usage: StructureGenTool <width> <length> <height> <material> <out_file_name>");
        } else {
            try (Writer writer = new FileWriter(new File(args[4]))) {
                int width = Integer.parseInt(args[0]);
                int length = Integer.parseInt(args[1]);
                int height = Integer.parseInt(args[2]);

                writer.write(String.format("width=%d\n", width));
                writer.write(String.format("length=%d\n", length));
                writer.write(String.format("height=%d\n", height));

                for (int x = 0; x < width; x++) {
                    for (int z = 0; z < length; z++) {
                        for (int y = height; y >= 0; y--) {
                            writer.write(String.format("%d,%d,%d,%s\n", x, z, y, args[3]));
                        }
                    }
                }
            } catch (NumberFormatException e) {
                System.err.println("Please use only integers for structure size.");
            } catch (IOException e) {
                System.err.println("IO error!");
                e.printStackTrace();
            }
        }
    }
}
