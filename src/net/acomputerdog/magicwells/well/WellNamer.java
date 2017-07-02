package net.acomputerdog.magicwells.well;

import net.acomputerdog.magicwells.PluginMagicWells;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class WellNamer {
    private final List<String> syllables;
    private final Random random;

    public WellNamer(PluginMagicWells plugin) {
        this.syllables = new ArrayList<>();
        this.random = new Random();

        try (BufferedReader reader = new BufferedReader(new FileReader(new File(plugin.getDataFolder(), "syllables.lst")))) {
            while (reader.ready()) {
                String line = reader.readLine().trim();
                if (!line.isEmpty() && !line.startsWith("#")) {
                    syllables.add(line);
                }
            }
        } catch (FileNotFoundException e) {
            throw new RuntimeException("Syllables file is missing.", e);
        } catch (IOException e) {
            throw new RuntimeException("Unable to load syllables list.", e);
        }
    }

    public String getRandomName() {
        StringBuilder name = new StringBuilder();
        for (int i = 0; i < random.nextInt(3) + 1; i++) {
            name.append(syllables.get(random.nextInt(syllables.size())));
        }
        return name.toString();
    }
}
