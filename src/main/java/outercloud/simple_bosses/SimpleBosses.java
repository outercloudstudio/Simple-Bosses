package outercloud.simple_bosses;

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
												.executes(context -> {
													String boss = StringArgumentType.getString(context, "boss_name");
													String move = StringArgumentType.getString(context, "move_name");

													PersistentState.addMove(boss, move, 20, 0, 0, 0, context.getSource().getServer());

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

	private void tick(MinecraftServer server) {
		for(String name : PersistentState.getBossNames(server)) {
			Entity boss = PersistentState.getBoss(name, server);

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

				LOGGER.info("Switched from Cooldown to Windup on " + name);
			} else if(state == "Windup") {
				String move = PersistentState.getMove(name, server);

				PersistentState.setState(name, "Move", server);
				PersistentState.setProgress(name, PersistentState.getDuration(name, move, server), server);

				LOGGER.info("Switched from Windup to Move on " + name);
			} else if(state == "Move") {
				String move = PersistentState.getMove(name, server);

				PersistentState.setState(name, "Recover", server);
				PersistentState.setProgress(name, PersistentState.getRecover(name, move, server), server);

				boss.addCommandTag("boss_move_" + move);

				LOGGER.info("Switched from Move to Recover on " + name);
			} else if(state == "Recover") {
				String move = PersistentState.getMove(name, server);

				PersistentState.setState(name, "Cooldown", server);
				PersistentState.setProgress(name, PersistentState.getCooldown(name, move, server), server);

				boss.removeScoreboardTag("boss_move_" + move);

				LOGGER.info("Switched from Recover to Cooldown on " + name);
			}
		}
	}
}