package outercloud.simple_bosses;

import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import net.fabricmc.api.ModInitializer;

import net.minecraft.command.argument.EntityArgumentType;
import net.minecraft.server.command.CommandManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import outercloud.simple_bosses.lib.Commands;

public class SimpleBosses implements ModInitializer {
    public static final Logger LOGGER = LoggerFactory.getLogger("simple_bosses");

	@Override
	public void onInitialize() {
		Commands.create("boss", command -> command
				.requires(source -> source.hasPermissionLevel(4))
				.then(CommandManager.literal("create")
						.then(CommandManager.argument("selector", EntityArgumentType.entity())
								.then(CommandManager.argument("name", StringArgumentType.word()))
						)
				)
				.then(CommandManager.literal("edit")
						.then(CommandManager.argument("name", StringArgumentType.word()))
				)
				.then(CommandManager.literal("view")
						.then(CommandManager.argument("name", StringArgumentType.word()))
				)
				.then(CommandManager.literal("remove")
						.then(CommandManager.argument("name", StringArgumentType.word()))
				)
				.then(CommandManager.literal("debug")
						.then(CommandManager.argument("name", StringArgumentType.word())
								.then(CommandManager.argument("move", StringArgumentType.word()))
						)
				)
		);
	}
}