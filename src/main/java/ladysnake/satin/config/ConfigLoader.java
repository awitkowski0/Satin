/*
 * Satin
 * Copyright (C) 2019-2020 Ladysnake
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program; If not, see <https://www.gnu.org/licenses>.
 */
package ladysnake.satin.config;

import com.google.gson.FieldNamingPolicy;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.fabricmc.loader.api.FabricLoader;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Objects;
import java.util.function.Supplier;

public class ConfigLoader {
    private static final Gson GSON = new GsonBuilder().setFieldNamingPolicy(FieldNamingPolicy.LOWER_CASE_WITH_UNDERSCORES).setPrettyPrinting().create();

    /**
     * Loads a config into a Plain Old Java Object from a file.
     *
     * @param configFile    a file in the config folder
     * @param configClass   the class of the POJO config
     * @param defaultValues a factory for a config object with default values
     * @param <T>           type of the POJO config
     * @return the generated config
     * @throws IOException if the config file cannot be read or created
     */
    public static <T extends Versionable> T load(String configFile, Class<T> configClass, Supplier<T> defaultValues) throws IOException {
        final Path config = FabricLoader.getInstance().getConfigDirectory().toPath().resolve(configFile);
        final T features;
        boolean needsUpdate;
        // Read the config from the file
        if (Files.exists(config)) {
            // The config exists, read from it and check if it is up to date
            try (final Reader reader = Files.newBufferedReader(config)) {
                features = Objects.requireNonNull(GSON.fromJson(reader, configClass), "invalid config: " + reader.toString());
            }
            needsUpdate = features.isUpToDate();
        } else {
            // First boot, use default values and write the file
            Files.createFile(config);
            features = defaultValues.get();
            needsUpdate = true;
        }
        // Write the config to the file if needed
        if (needsUpdate) {
            try (final Writer writer = Files.newBufferedWriter(config)) {
                writer.append(GSON.toJson(features));
            }
            features.update();
        }
        return features;
    }
}
