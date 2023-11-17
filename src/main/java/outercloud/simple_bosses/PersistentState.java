package outercloud.simple_bosses;

import net.minecraft.entity.Entity;
import net.minecraft.nbt.*;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.world.PersistentStateManager;
import net.minecraft.world.World;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.UUID;

public class PersistentState extends net.minecraft.world.PersistentState {
    private HashMap<String, UUID> bosses = new HashMap<>();
    private HashMap<String, String> bossStates = new HashMap<>();
    private HashMap<String, String> bossCurrentMoves = new HashMap<>();
    private HashMap<String, Integer> bossStateProgresses = new HashMap<>();
    private HashMap<String, ArrayList<String>> moves = new HashMap<>();
    private HashMap<String, ArrayList<Integer>> moveCooldowns = new HashMap<>();
    private HashMap<String, ArrayList<Integer>> moveDurations = new HashMap<>();
    private HashMap<String, ArrayList<Integer>> moveWindups = new HashMap<>();
    private HashMap<String, ArrayList<Integer>> moveRecovers = new HashMap<>();

    @Override
    public NbtCompound writeNbt(NbtCompound nbt) {
        NbtCompound bossesData = new NbtCompound();
        NbtCompound bossStatesData = new NbtCompound();
        NbtCompound bossCurrentMovesData = new NbtCompound();
        NbtCompound bossStateProgressesData = new NbtCompound();
        NbtCompound movesData = new NbtCompound();
        NbtCompound movesCooldownsData = new NbtCompound();
        NbtCompound movesDurationsData = new NbtCompound();
        NbtCompound movesWindupsData = new NbtCompound();
        NbtCompound movesRecoversData = new NbtCompound();

        for(String boss : bosses.keySet()) {
            bossesData.putString(boss, bosses.get(boss).toString());
            bossStatesData.putString(boss, bossStates.get(boss));
            bossCurrentMovesData.putString(boss, bossCurrentMoves.get(boss));
            bossStateProgressesData.putInt(boss, bossStateProgresses.get(boss));

            NbtList moveData = new NbtList();

            for(String move: moves.get(boss)) {
                moveData.add(NbtString.of(move));
            }

            movesData.put(boss, moveData);
            movesCooldownsData.putIntArray(boss, moveCooldowns.get(boss));
            movesDurationsData.putIntArray(boss, moveDurations.get(boss));
            movesWindupsData.putIntArray(boss, moveWindups.get(boss));
            movesRecoversData.putIntArray(boss, moveRecovers.get(boss));
        }

        nbt.put("bosses", bossesData);
        nbt.put("bossesStates", bossesData);
        nbt.put("bossesCurrentMoves", bossesData);
        nbt.put("bossesStateProgresses", bossesData);
        nbt.put("moves", bossesData);
        nbt.put("moveCooldowns", bossesData);
        nbt.put("moveDurations", bossesData);
        nbt.put("moveWindups", bossesData);
        nbt.put("moveRecovers", bossesData);

        return nbt;
    }

    public static PersistentState createFromNbt(NbtCompound nbt) {
        PersistentState state = new PersistentState();

        for(String boss: nbt.getCompound("bosses").getKeys()) {
            state.bosses.put(boss, UUID.fromString(nbt.getCompound("bosses").getString(boss)));
            state.bossStates.put(boss, nbt.getCompound("bossStates").getString(boss));
            state.bossCurrentMoves.put(boss, nbt.getCompound("bossCurrentMoves").getString(boss));
            state.bossStateProgresses.put(boss, nbt.getCompound("bossStateProgresses").getInt(boss));
            state.moves.put(boss, new ArrayList<>(Arrays.asList((String[])nbt.getCompound("moves").getList("boss", NbtElement.STRING_TYPE).toArray())));
            state.moveCooldowns.put(boss, new ArrayList<>(Arrays.asList((Integer[])nbt.getCompound("moveCooldowns").getList("boss", NbtElement.INT_TYPE).toArray())));
            state.moveDurations.put(boss, new ArrayList<>(Arrays.asList((Integer[])nbt.getCompound("moveDurations").getList("boss", NbtElement.INT_TYPE).toArray())));
            state.moveWindups.put(boss, new ArrayList<>(Arrays.asList((Integer[])nbt.getCompound("moveWindups").getList("boss", NbtElement.INT_TYPE).toArray())));
            state.moveRecovers.put(boss, new ArrayList<>(Arrays.asList((Integer[])nbt.getCompound("moveRecovers").getList("boss", NbtElement.INT_TYPE).toArray())));
        }

        return state;
    }

    public static PersistentState get(MinecraftServer server) {
        PersistentStateManager persistentStateManager = server.getWorld(World.OVERWORLD).getPersistentStateManager();

        PersistentState state = persistentStateManager.getOrCreate(PersistentState::createFromNbt, PersistentState::new, "simple_bosses");

        state.markDirty();

        return state;
    }

    public static void setBoss(String name, Entity entity, MinecraftServer server) {
        PersistentState instance = get(server);
        instance.bosses.put(name, entity.getUuid());
        instance.moves.put(name, new ArrayList<>());
        instance.moveCooldowns.put(name, new ArrayList<>());
        instance.moveDurations.put(name, new ArrayList<>());
        instance.moveRecovers.put(name, new ArrayList<>());
        instance.moveWindups.put(name, new ArrayList<>());
        instance.bossStates.put(name, "Cooldown");
        instance.bossStateProgresses.put(name, 0);
        instance.bossCurrentMoves.put(name, "None");
    }

    public static boolean bossExists(String name, MinecraftServer server) {
        PersistentState instance = get(server);

        return instance.bosses.keySet().contains(name);
    }

    public static void removeBoss(String name, MinecraftServer server) {
        PersistentState instance = get(server);

        instance.bosses.remove(name);
        instance.bossStates.remove(name);
        instance.bossStateProgresses.remove(name);
        instance.bossCurrentMoves.remove(name);
        instance.moves.remove(name);
        instance.moveCooldowns.remove(name);
        instance.moveDurations.remove(name);
        instance.moveWindups.remove(name);
        instance.moveRecovers.remove(name);
    }

    public static Entity getBoss(String name, MinecraftServer server) {
        UUID id = get(server).bosses.get(name);

        for(ServerWorld world: server.getWorlds()) {
            Entity entity = world.getEntity(id);

            if(entity != null) return entity;
        }

        return  null;
    }

    public static String getState(String name, MinecraftServer server) {
        PersistentState instance = get(server);

        return instance.bossStates.get(name);
    }

    public static void setState(String name, String state, MinecraftServer server) {
        PersistentState instance = get(server);

        instance.bossStates.put(name, state);
    }

    public static int getProgress(String name, MinecraftServer server) {
        PersistentState instance = get(server);

        return instance.bossStateProgresses.get(name);
    }

    public static void setProgress(String name, int progress, MinecraftServer server) {
        PersistentState instance = get(server);

        instance.bossStateProgresses.put(name, progress);
    }

    public static void addMove(String boss, String name, int cooldown, int duration, int windup, int recover, MinecraftServer server) {
        PersistentState instance = get(server);
        instance.moves.get(boss).add(name);
        instance.moveCooldowns.get(boss).add(cooldown);
        instance.moveDurations.get(boss).add(duration);
        instance.moveWindups.get(boss).add(windup);
        instance.moveRecovers.get(boss).add(recover);
    }

    public static boolean moveExists(String boss, String name, MinecraftServer server) {
        PersistentState instance = get(server);

        return  instance.moves.get(boss).contains(name);
    }

    public static void removeMove(String boss, String name, MinecraftServer server) {
        PersistentState instance = get(server);

        int index = instance.moves.get(boss).indexOf(name);

        instance.moves.get(boss).remove(index);
        instance.moveCooldowns.get(boss).remove(index);
        instance.moveDurations.get(boss).remove(index);
        instance.moveWindups.get(boss).remove(index);
        instance.moveRecovers.get(boss).remove(index);
    }

    public static String getMove(String name, MinecraftServer server) {
        PersistentState instance = get(server);

        return instance.bossCurrentMoves.get(name);
    }

    public static void setMove(String name, String move, MinecraftServer server) {
        PersistentState instance = get(server);

        instance.bossCurrentMoves.put(name, move);
    }

    public static int getDuration(String name, String move, MinecraftServer server) {
        PersistentState instance = get(server);

        return instance.moveDurations.get(name).get(instance.moves.get(name).indexOf(move));
    }

    public static int getWindup(String name, String move, MinecraftServer server) {
        PersistentState instance = get(server);

        return instance.moveWindups.get(name).get(instance.moves.get(name).indexOf(move));
    }

    public static int getRecover(String name, String move, MinecraftServer server) {
        PersistentState instance = get(server);

        return instance.moveRecovers.get(name).get(instance.moves.get(name).indexOf(move));
    }

    public static int getCooldown(String name, String move, MinecraftServer server) {
        PersistentState instance = get(server);

        return instance.moveCooldowns.get(name).get(instance.moves.get(name).indexOf(move));
    }

    public static ArrayList<String> getBossNames(MinecraftServer server) {
        return new ArrayList<>(get(server).bosses.keySet());
    }

    public static ArrayList<String> getBossMoves(String name, MinecraftServer server) {
        return new ArrayList<>(get(server).moves.get(name));
    }
}
