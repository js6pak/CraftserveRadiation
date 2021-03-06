/*
 * Copyright 2019 Aleksander Jagiełło <themolkapl@gmail.com>
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package pl.craftserve.radiation;

import org.bukkit.NamespacedKey;
import org.bukkit.Server;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.logging.Level;

public final class RadiationPlugin extends JavaPlugin {
    private final List<Radiation> radiations = new ArrayList<>();

    private LugolsIodinePotion potion;
    private LugolsIodineEffect effect;
    private LugolsIodineDisplay display;

    private CraftserveListener craftserveListener;

    @Override
    public void onEnable() {
        Server server = this.getServer();
        this.saveDefaultConfig();

        //
        // Loading configuration
        //

        FileConfiguration config = this.getConfig();

        int potionDuration = config.getInt("potion-duration", 10); // in minutes
        if (potionDuration <= 0) {
            this.getLogger().log(Level.SEVERE, "\"potion-duration\" option must be positive.");
            this.setEnabled(false);
            return;
        }

        String regionName = config.getString("region-name", "km_safe_from_radiation");

        List<String> worldNames = config.getStringList("world-names");
        if (worldNames.isEmpty()) {
            this.getLogger().log(Level.SEVERE, "No world names defined. Loading in the overworld...");
            worldNames.add(server.getWorlds().get(0).getName()); // overworld is always at index 0
        }

        //
        // Enabling
        //

        worldNames.forEach(worldName -> this.radiations.add(new Radiation(this, worldName, regionName)));

        this.potion = new LugolsIodinePotion(this, "Płyn Lugola", potionDuration);
        this.effect = new LugolsIodineEffect(this);
        this.display = new LugolsIodineDisplay(this);

        this.radiations.forEach(Radiation::enable);

        this.potion.enable();
        this.effect.enable();
        this.display.enable();

        this.craftserveListener = new CraftserveListener(this);
        this.craftserveListener.enable();
    }

    @Override
    public void onDisable() {
        if (this.craftserveListener != null) {
            this.craftserveListener.disable();
        }

        if (this.display != null) {
            this.display.disable();
        }

        if (this.effect != null) {
            this.effect.disable();
        }

        if (this.potion != null) {
            this.potion.disable();
        }

        this.radiations.forEach(Radiation::disable);
        this.radiations.clear();
    }

    public LugolsIodineEffect getEffect() {
        return this.effect;
    }

    public NamespacedKey createKey(String key) {
        return new NamespacedKey(this, Objects.requireNonNull(key, "key"));
    }
}
