package outercloud.simple_bosses;

import com.mojang.brigadier.arguments.StringArgumentType;
import net.fabricmc.api.ModInitializer;

import net.minecraft.command.argument.EntityArgumentType;
import net.minecraft.entity.Entity;
import net.minecraft.server.command.CommandManager;
import net.minecraft.text.Text;
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
						.then(CommandManager.argument("target", EntityArgumentType.entity())
								.then(CommandManager.argument("name", StringArgumentType.word())
										.executes(context -> {
											String name = StringArgumentType.getString(context, "name");
											Entity entity = EntityArgumentType.getEntity(context, "target");

											PersistentState.setBoss(name, entity, context.getSource().getServer());

											return 1;
										})
								)
						)
				)
				.then(CommandManager.literal("edit")
						.then(CommandManager.argument("boss_name", StringArgumentType.word())
								.then(CommandManager.literal("moves")
										.then(CommandManager.literal("add")
												.then(CommandManager.argument("move_name", StringArgumentType.word())
														.executes(context -> {
															String boss = StringArgumentType.getString(context, "boss_name");
															String move = StringArgumentType.getString(context, "move_name");

															LOGGER.info(boss);
															LOGGER.info(move);

															PersistentState.addMove(boss, move, context.getSource().getServer());

															return 1;
														})
												)
										)
										.then(CommandManager.literal("remove")
												.then(CommandManager.argument("move_name", StringArgumentType.word())
														.executes(context -> {
															String boss = StringArgumentType.getString(context, "boss_name");
															String move = StringArgumentType.getString(context, "move_name");

															PersistentState.removeMove(boss, move, context.getSource().getServer());

															return 1;
														})
												)
										)
										.then(CommandManager.literal("edit"))
										.then(CommandManager.literal("list")
												.executes(context -> {
													String name = StringArgumentType.getString(context, "boss_name");

													context.getSource().sendMessage(Text.of("Moves of " + name + ":"));

													int index = 1;
													for(String moveName: PersistentState.getBossMoves(name, context.getSource().getServer())) {
														context.getSource().sendMessage(Text.of(index + ". " + moveName));

														index++;
													}

													return 1;
												})
										)
								)
						)
				)
				.then(CommandManager.literal("list")
						.then(CommandManager.argument("name", StringArgumentType.word()))
						.executes(context -> {
							context.getSource().sendMessage(Text.of("Bosses:"));

							int index = 1;
							for(String name: PersistentState.getBossNames(context.getSource().getServer())) {
								context.getSource().sendMessage(Text.of(index + ". " + name));

								index++;
							}

							return 1;
						})
				)
				.then(CommandManager.literal("remove")
						.then(CommandManager.argument("name", StringArgumentType.word())
								.executes(context -> {
									String name = StringArgumentType.getString(context, "name");

									PersistentState.removeBoss(name, context.getSource().getServer());

									return 1;
								})
						)
				)
				.then(CommandManager.literal("debug")
						.then(CommandManager.argument("name", StringArgumentType.word())
								.then(CommandManager.argument("move", StringArgumentType.word()))
						)
				)
		);
	}
}