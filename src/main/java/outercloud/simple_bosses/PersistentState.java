package outercloud.simple_bosses;

import net.minecraft.entity.Entity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.world.PersistentStateManager;
import net.minecraft.world.World;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

public class PersistentState extends net.minecraft.world.PersistentState {
    private HashMap<String, UUID> bosses = new HashMap<>();
    private HashMap<String, List<String>> moves = new HashMap<>();

    @Override
    public NbtCompound writeNbt(NbtCompound nbt) {
        return nbt;
    }

    public static PersistentState createFromNbt(NbtCompound nbt) {
        PersistentState state = new PersistentState();

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
    }

    public static void removeBoss(String name, MinecraftServer server) {
        PersistentState instance = get(server);
        instance.bosses.remove(name);
        instance.moves.remove(name);
    }

    public static Entity getBoss(String name, MinecraftServer server) {
        UUID id = get(server).bosses.get(name);

        for(ServerWorld world: server.getWorlds()) {
            Entity entity = world.getEntity(id);

            if(entity != null) return entity;
        }

        return  null;
    }

    public static void addMove(String boss, String name, MinecraftServer server) {
        PersistentState instance = get(server);
        instance.moves.get(boss).add(name);
    }

    public static void removeMove(String boss, String name, MinecraftServer server) {
        PersistentState instance = get(server);
        instance.moves.get(boss).remove(name);
    }

    public static List<String> getBossNames(MinecraftServer server) {
        return get(server).bosses.keySet().stream().toList();
    }

    public static List<String> getBossMoves(String name, MinecraftServer server) {
        return get(server).moves.get(name);
    }
}
