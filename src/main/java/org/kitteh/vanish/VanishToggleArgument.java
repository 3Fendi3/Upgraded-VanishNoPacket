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
package org.kitteh.vanish;

import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.MessageComponentSerializer;
import io.papermc.paper.command.brigadier.argument.CustomArgumentType;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.function.Function;
import net.kyori.adventure.text.Component;
import org.bukkit.entity.Player;
import org.jspecify.annotations.NullMarked;
import org.kitteh.vanish.VanishPerms.TogglePermissions;

@NullMarked
final public class VanishToggleArgument implements
    CustomArgumentType.Converted<VanishPerms.TogglePermissions, String> {


  private static final DynamicCommandExceptionType ERROR_INVALID_TOGGLE = new DynamicCommandExceptionType(
      toggle -> MessageComponentSerializer.message()
          .serialize(Component.text(toggle + " is not a valid toggle!")));

  /**
   * Converts the value from the native type to the custom argument type.
   *
   * @param nativeType native argument provided value
   * @return converted value
   * @throws CommandSyntaxException if an exception occurs while parsing
   * @see #convert(Object, Object)
   */
  @Override
  public TogglePermissions convert(String nativeType) throws CommandSyntaxException {
    try {
      return TogglePermissions.valueOf(nativeType.toUpperCase());
    } catch (IllegalArgumentException ignored) {
      throw ERROR_INVALID_TOGGLE.create(nativeType);
    }
  }

  @Override
  public <S> CompletableFuture<Suggestions> listSuggestions(CommandContext<S> context,
      SuggestionsBuilder builder) {
    for (TogglePermissions permission : TogglePermissions.values()) {
      String name = permission.toString();
      Function<Player, Boolean> permissionsCheck = permission.getCanToggle();

      if (!permissionsCheck.apply((Player) Objects.requireNonNull(((CommandSourceStack) context.getSource()).getExecutor()))) {
        break;
      }

      if (name.startsWith(builder.getRemainingLowerCase())) {
        builder.suggest(permission.toString());
      }
    }

    return builder.buildFuture();
  }

  /**
   * Gets the native type that this argument uses, the type that is sent to the client.
   *
   * @return native argument type
   */
  @Override
  public ArgumentType<String> getNativeType() {
    return StringArgumentType.word();
  }
}
