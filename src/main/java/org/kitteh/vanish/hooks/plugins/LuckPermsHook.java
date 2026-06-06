/*
 * VanishNoPacket
 * Copyright (C) 2011-2022 Matt Baxter
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */
package org.kitteh.vanish.hooks.plugins;

import net.luckperms.api.LuckPerms;
import net.luckperms.api.context.ContextCalculator;
import net.luckperms.api.context.ContextConsumer;
import net.luckperms.api.context.ContextSet;
import net.luckperms.api.context.ImmutableContextSet;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.kitteh.vanish.VanishPlugin;
import org.kitteh.vanish.hooks.Hook;

public class LuckPermsHook extends Hook {

  boolean enabled = false;

  private static class VanishContextCalculator implements ContextCalculator<Player> {

    private final VanishPlugin plugin;

    public VanishContextCalculator(VanishPlugin plugin) {
      this.plugin = plugin;
    }

    @Override
    public void calculate(@NonNull Player target, @NonNull ContextConsumer consumer) {
      consumer.accept("vanished", this.plugin.getManager().isVanished(target) ? "true" : "false");
    }

    @Override
    public @NonNull ContextSet estimatePotentialContexts() {
      return ImmutableContextSet.builder().add("vanished", "true").add("vanished", "false").build();
    }
  }

  public LuckPermsHook(@NonNull VanishPlugin plugin) {
    super(plugin);
  }

  @Override
  public void onEnable() {
    RegisteredServiceProvider<LuckPerms> provider = Bukkit.getServicesManager()
        .getRegistration(LuckPerms.class);

    if (provider == null) {
      this.plugin.getLogger().info("You wanted Luckperms support. I could not find LuckPerms.");
      this.enabled = false;
      return;
    }

    LuckPerms luckperms = provider.getProvider();
    this.plugin.getLogger().info("Now hooking into Luckperms");
    this.enabled = true;
    luckperms.getContextManager().registerCalculator(new VanishContextCalculator(this.plugin));
  }
}
