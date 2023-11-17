package outercloud.simple_bosses;

import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import net.fabricmc.api.ModInitializer;

import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.command.argument.EntityArgumentType;
import net.minecraft.entity.Entity;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.command.CommandManager;
import net.minecraft.text.Text;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import outercloud.simple_bosses.lib.Commands;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.Set;

public class SimpleBosses implements ModInitializer {
    public static final Logger LOGGER = LoggerFactory.getLogger("simple_bosses");

	private static final Random RANDOM = new Random();

	@Override
	public void onInitialize() {
		ServerTickEvents.END_SERVER_TICK.register(this::tick);

		Commands.create("boss", command -> command
				.requires(source -> source.hasPermissionLevel(4))
				.then(CommandManager.literal("create")
						.then(CommandManager.argument("target", EntityArgumentType.entity())
								.then(CommandManager.argument("name", StringArgumentType.word())
										.executes(context -> {
											String name = StringArgumentType.getString(context, "name");
											Entity entity = EntityArgumentType.getEntity(context, "target");

											if(PersistentState.bossExists(name, context.getSource().getServer())) {
												context.getSource().sendError(Text.of("A boss with that name already exists!"));

												return -1;
											}

											PersistentState.setBoss(name, entity, context.getSource().getServer());

											return 1;
										})
								)
						)
				)
				.then(CommandManager.literal("moves")
						.then(CommandManager.argument("boss_name", StringArgumentType.word())
								.then(CommandManager.literal("add")
										.then(CommandManager.argument("move_name", StringArgumentType.word())
												.then(CommandManager.argument("cooldown", IntegerArgumentType.integer(0))
														.then(CommandManager.argument("windup", IntegerArgumentType.integer(0))
																.then(CommandManager.argument("duration", IntegerArgumentType.integer(0))
																		.then(CommandManager.argument("recover", IntegerArgumentType.integer(0))
																				.executes(context -> {
																					String boss = StringArgumentType.getString(context, "boss_name");
																					String move = StringArgumentType.getString(context, "move_name");
																					int cooldown = IntegerArgumentType.getInteger(context, "cooldown");
																					int windup = IntegerArgumentType.getInteger(context, "windup");
																					int duration = IntegerArgumentType.getInteger(context, "duration");
																					int recover = IntegerArgumentType.getInteger(context, "recover");

																					if(!PersistentState.bossExists(boss, context.getSource().getServer())) {
																						context.getSource().sendError(Text.of("No boss with that name exists!"));

																						return -1;
																					}

																					if(PersistentState.moveExists(boss, move, context.getSource().getServer())) {
																						context.getSource().sendError(Text.of("A move with that name already exists on that boss!"));

																						return -1;
																					}

																					PersistentState.addMove(boss, move, cooldown, duration, windup, recover, context.getSource().getServer());

																					return 1;
																				})
																		)
																)
														)
												)
										)
								)
								.then(CommandManager.literal("remove")
										.then(CommandManager.argument("move_name", StringArgumentType.word())
												.executes(context -> {
													String boss = StringArgumentType.getString(context, "boss_name");
													String move = StringArgumentType.getString(context, "move_name");

													if(!PersistentState.bossExists(boss, context.getSource().getServer())) {
														context.getSource().sendError(Text.of("No boss with that name exists!"));

														return -1;
													}

													if(!PersistentState.moveExists(boss, move, context.getSource().getServer())) {
														context.getSource().sendError(Text.of("That boss does not have that move!"));

														return -1;
													}

													PersistentState.removeMove(boss, move, context.getSource().getServer());

													return 1;
												})
										)
								)
								.then(CommandManager.literal("edit")
										.then(CommandManager.argument("move_name", StringArgumentType.word())
												.then(CommandManager.argument("cooldown", IntegerArgumentType.integer(0))
														.then(CommandManager.argument("windup", IntegerArgumentType.integer(0))
																.then(CommandManager.argument("duration", IntegerArgumentType.integer(0))
																		.then(CommandManager.argument("recover", IntegerArgumentType.integer(0))
																				.executes(context -> {
																					String boss = StringArgumentType.getString(context, "boss_name");
																					String move = StringArgumentType.getString(context, "move_name");
																					int cooldown = IntegerArgumentType.getInteger(context, "cooldown");
																					int windup = IntegerArgumentType.getInteger(context, "windup");
																					int duration = IntegerArgumentType.getInteger(context, "duration");
																					int recover = IntegerArgumentType.getInteger(context, "recover");

																					if(!PersistentState.bossExists(boss, context.getSource().getServer())) {
																						context.getSource().sendError(Text.of("No boss with that name exists!"));

																						return -1;
																					}

																					if(!PersistentState.moveExists(boss, move, context.getSource().getServer())) {
																						context.getSource().sendError(Text.of("That boss does not have that move!"));

																						return -1;
																					}

																					PersistentState.removeMove(boss, move, context.getSource().getServer());
																					PersistentState.addMove(boss, move, cooldown, duration, windup, recover, context.getSource().getServer());

																					return 1;
																				})
																		)
																)
														)
												)
										)
								)
								.then(CommandManager.literal("list")
										.executes(context -> {
											String name = StringArgumentType.getString(context, "boss_name");

											if(!PersistentState.bossExists(name, context.getSource().getServer())) {
												context.getSource().sendError(Text.of("No boss with that name exists!"));

												return -1;
											}

											ArrayList<String> moves = PersistentState.getBossMoves(name, context.getSource().getServer());

											if(moves.isEmpty()) {
												context.getSource().sendMessage(Text.of(name + " has no moves."));

												return 1;
											}

											context.getSource().sendMessage(Text.of("Moves of " + name + ":"));

											int index = 1;
											for(String moveName: moves) {
												context.getSource().sendMessage(Text.of(index + ". " + moveName));

												index++;
											}

											return 1;
										})
								)
						)
				)
				.then(CommandManager.literal("list")
						.then(CommandManager.argument("name", StringArgumentType.word()))
						.executes(context -> {
							ArrayList<String> bosses = PersistentState.getBossNames(context.getSource().getServer());

							if(bosses.isEmpty()) {
								context.getSource().sendMessage(Text.of("There are no bosses."));

								return 1;
							}

							context.getSource().sendMessage(Text.of("Bosses:"));

							int index = 1;
							for(String name: bosses) {
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

									if(!PersistentState.bossExists(name, context.getSource().getServer())) {
										context.getSource().sendError(Text.of("No boss with that name exists!"));

										return -1;
									}

									Entity entity = PersistentState.getBoss(name, context.getSource().getServer());

									if(entity != null) {
										String state = PersistentState.getState(name, context.getSource().getServer());
										String move = PersistentState.getMove(name, context.getSource().getServer());

										if(entity.getCommandTags().contains("boss_once_" + state.toLowerCase() + "_" + move)) {
											entity.removeScoreboardTag("boss_once_" + state.toLowerCase() + "_" + move);
										}

										if(entity.getCommandTags().contains("boss_" + state.toLowerCase() + "_" + move)) {
											entity.removeScoreboardTag("boss_" + state.toLowerCase() + "_" + move);
										}
									}

									PersistentState.removeBoss(name, context.getSource().getServer());

									return 1;
								})
						)
				)
				.then(CommandManager.literal("debug")
						.then(CommandManager.argument("boss_name", StringArgumentType.word())
								.then(CommandManager.argument("move_name", StringArgumentType.word())
										.executes(context -> {
											String boss = StringArgumentType.getString(context, "boss_name");
											String move = StringArgumentType.getString(context, "move_name");

											if(!PersistentState.bossExists(boss, context.getSource().getServer())) {
												context.getSource().sendError(Text.of("No boss with that name exists!"));

												return -1;
											}

											if(!PersistentState.moveExists(boss, move, context.getSource().getServer())) {
												context.getSource().sendError(Text.of("That boss does not have that move!"));

												return -1;
											}

											PersistentState.setState(boss, "Windup", context.getSource().getServer());
											PersistentState.setMove(boss, move, context.getSource().getServer());
											PersistentState.setProgress(boss, PersistentState.getWindup(boss, move, context.getSource().getServer()), context.getSource().getServer());

											return 1;
										})
								)
						)
				)
		);
	}

	private void tick(MinecraftServer server) {
		for(String name : PersistentState.getBossNames(server)) {
			Entity boss = PersistentState.getBoss(name, server);
			Set<String> tags = boss.getCommandTags();
			String move = PersistentState.getMove(name, server);

			if(tags.contains("boss_once_windup_" + move)) boss.removeScoreboardTag("boss_once_windup_" + move);
			if(tags.contains("boss_once_move_" + move)) boss.removeScoreboardTag("boss_once_move_" + move);
			if(tags.contains("boss_once_recover_" + move)) boss.removeScoreboardTag("boss_once_recover_" + move);

			int progress = PersistentState.getProgress(name, server);

			PersistentState.setProgress(name, progress - 1, server);

			if(progress > 0) continue;

			String state = PersistentState.getState(name, server);

			if(state == "Cooldown") {
				ArrayList<String> moves = PersistentState.getBossMoves(name, server);

				if(moves.size() == 0) continue;

				String newMove = moves.get(RANDOM.nextInt(moves.size()));

				PersistentState.setState(name, "Windup", server);
				PersistentState.setMove(name, newMove, server);
				PersistentState.setProgress(name, PersistentState.getWindup(name, newMove, server), server);

				boss.addCommandTag("boss_windup_" + newMove);
				boss.addCommandTag("boss_once_windup_" + newMove);
			}

			if(state == "Windup") {
				PersistentState.setState(name, "Move", server);
				PersistentState.setProgress(name, PersistentState.getDuration(name, move, server), server);

				boss.removeScoreboardTag("boss_windup_" + move);
				boss.addCommandTag("boss_move_" + move);
				boss.addCommandTag("boss_once_move_" + move);

				LOGGER.info("boss_once_move_" + move);
			}

			if(state == "Move") {
				PersistentState.setState(name, "Recover", server);
				PersistentState.setProgress(name, PersistentState.getRecover(name, move, server), server);

				boss.removeScoreboardTag("boss_move_" + move);
				boss.addCommandTag("boss_recover_" + move);
				boss.addCommandTag("boss_once_recover_" + move);
			}

			if(state == "Recover") {
				PersistentState.setState(name, "Cooldown", server);
				PersistentState.setProgress(name, PersistentState.getCooldown(name, move, server), server);

				boss.removeScoreboardTag("boss_recover_" + move);
			}
		}
	}
}