package eu.codedsakura.fabrichomes;

import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import eu.codedsakura.fabrichomes.components.DeathComponent;
import eu.codedsakura.fabrichomes.components.HomeComponent;
import eu.codedsakura.mods.ConfigUtils;
import eu.codedsakura.mods.TeleportUtils;
import eu.codedsakura.mods.TextUtils;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v1.CommandRegistrationCallback;
import net.fabricmc.fabric.api.entity.event.v1.ServerPlayerEvents;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.command.argument.EntityArgumentType;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.*;
import net.minecraft.util.Formatting;
import net.minecraft.util.registry.Registry;
import net.minecraft.util.registry.RegistryKey;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.time.Instant;
import java.util.*;
import java.util.concurrent.CompletableFuture;

import static eu.codedsakura.fabrichomes.components.PlayerComponentInitializer.DEATH_DATA;
import static eu.codedsakura.fabrichomes.components.PlayerComponentInitializer.HOME_DATA;
import static net.minecraft.server.command.CommandManager.argument;
import static net.minecraft.server.command.CommandManager.literal;

public class FabricHomes implements ModInitializer {
    public static final Logger logger = LogManager.getLogger("FabricHomes");
    private static final String CONFIG_NAME = "FabricHomes.properties";

    private final HashMap<UUID, Long> recentRequests = new HashMap<>();
    private ConfigUtils config;

    @Override
    public void onInitialize() {
        logger.info("Initializing...");

        config = new ConfigUtils(FabricLoader.getInstance().getConfigDir().resolve(CONFIG_NAME).toFile(), logger, Arrays.asList(new ConfigUtils.IConfigValue[] {
                new ConfigUtils.IntegerConfigValue("stand-still", 3, new ConfigUtils.IntegerConfigValue.IntLimits(0),
                        new ConfigUtils.Command("Stand-Still time is %s seconds", "Stand-Still time set to %s seconds")),
                new ConfigUtils.IntegerConfigValue("cooldown", 30, new ConfigUtils.IntegerConfigValue.IntLimits(0),
                        new ConfigUtils.Command("Cooldown is %s seconds", "Cooldown set to %s seconds")),
                new ConfigUtils.BooleanConfigValue("bossbar", true,
                        new ConfigUtils.Command("Boss-Bar on: %s", "Boss-Bar is now: %s")),
                new ConfigUtils.IntegerConfigValue("max-homes", 2, new ConfigUtils.IntegerConfigValue.IntLimits(0),
                        new ConfigUtils.Command("Max available homes is %s", "Max homes set to %s"))
        }));

        CommandRegistrationCallback.EVENT.register((dispatcher, dedicated) -> {
            dispatcher.register(literal("home")
                    .executes(ctx -> homeInit(ctx, null))
                    .then(argument("name", StringArgumentType.greedyString()).suggests(this::getHomeSuggestions)
                            .executes(ctx -> homeInit(ctx, StringArgumentType.getString(ctx, "name")))));

            dispatcher.register(literal("sethome")
                    .executes(ctx -> homeSet(ctx, null))
                    .then(argument("name", StringArgumentType.greedyString())
                            .executes(ctx -> homeSet(ctx, StringArgumentType.getString(ctx, "name")))));

            dispatcher.register(literal("delhome")
                            .then(argument("name", StringArgumentType.greedyString()).suggests(this::getHomeSuggestions)
                                    .executes(ctx -> homeDel(ctx, StringArgumentType.getString(ctx, "name")))));

            dispatcher.register(literal("homes")
                    .executes(this::homeList)
                    .then(literal("list")
                            .executes(this::homeList)
                            .then(argument("player", EntityArgumentType.player()).requires(source -> source.hasPermissionLevel(2))
                                    .executes(ctx -> homeList(ctx, EntityArgumentType.getPlayer(ctx, "player")))))
                    .then(literal("gui").requires(req -> false)
                            .executes(ctx -> 0)) // TODO
                    .then(literal("delete")
                            .then(argument("name", StringArgumentType.greedyString()).suggests(this::getHomeSuggestions)
                                    .executes(ctx -> homeDel(ctx, StringArgumentType.getString(ctx, "name")))))
                    .then(config.generateCommand("config", source -> source.hasPermissionLevel(2))));

            // lastdeath command added by PurpleCho
            // can teleport on /lastdeath
            //or view death coords with /lastdeath info
            dispatcher.register(literal("lastdeath")
                    .executes(this::deathInit)
                    .then(literal("info")
                        .executes(this::deathInfo)));
        });

        //When a player dies, store the coords and cause in their NBT data
        ServerPlayerEvents.ALLOW_DEATH.register(((player, damageSource, damageAmount) -> deathStore(player, damageSource)));
    }

    private boolean checkCooldown(ServerPlayerEntity tFrom) {
        if (recentRequests.containsKey(tFrom.getUuid())) {
            long diff = Instant.now().getEpochSecond() - recentRequests.get(tFrom.getUuid());
            if (diff < (int) config.getValue("cooldown")) {
                tFrom.sendMessage(new TranslatableText("You cannot make teleport home for %s more seconds!", String.valueOf((int) config.getValue("cooldown") - diff)).formatted(Formatting.RED), false);
                return true;
            }
        }
        return false;
    }

    private CompletableFuture<Suggestions> getHomeSuggestions(CommandContext<ServerCommandSource> context, SuggestionsBuilder builder) throws CommandSyntaxException {
        String start = builder.getRemaining().toLowerCase();
        HOME_DATA.get(context.getSource().getPlayer()).getHomes().stream()
                .map(HomeComponent::getName)
                .sorted(String::compareToIgnoreCase)
                .filter(v -> v.toLowerCase().startsWith(start))
                .forEach(builder::suggest);
        return builder.buildFuture();
    }

    int homeInit(CommandContext<ServerCommandSource> ctx, String name) throws CommandSyntaxException {
        ServerPlayerEntity player = ctx.getSource().getPlayer();
        if (name == null) name = "main";

        String finalName = name;
        Optional<HomeComponent> home = HOME_DATA.get(player).getHomes()
                .stream().filter(v -> v.getName().equals(finalName)).findFirst();

        if (!home.isPresent()) {
            ctx.getSource().sendFeedback(new LiteralText("This home does not exist").formatted(Formatting.RED), false);
            return 0;
        }

        if (checkCooldown(player)) return 1;

        TeleportUtils.genericTeleport((boolean) config.getValue("bossbar"), (int) config.getValue("stand-still"), player, () -> {
            player.teleport(
                    ctx.getSource().getMinecraftServer().getWorld(RegistryKey.of(Registry.WORLD_KEY, home.get().getDimID())),
                    home.get().getX(), home.get().getY(), home.get().getZ(),
                    home.get().getYaw(), home.get().getPitch());
            recentRequests.put(player.getUuid(), Instant.now().getEpochSecond());
        });

        return 1;
    }

    int homeSet(CommandContext<ServerCommandSource> ctx, String name) throws CommandSyntaxException {
        if (name == null) name = "main";

        if (HOME_DATA.get(ctx.getSource().getPlayer()).getHomes().size() >= (int) config.getValue("max-homes")) {
            ctx.getSource().sendFeedback(new LiteralText("Home limit reached!").formatted(Formatting.RED), false);
            return 1;
        }

        if (HOME_DATA.get(ctx.getSource().getPlayer()).addHome(new HomeComponent(
                ctx.getSource().getPosition(),
                ctx.getSource().getPlayer().getPitch(),
                ctx.getSource().getPlayer().getYaw(),
                ctx.getSource().getWorld().getRegistryKey().getValue(),
                name))) {

            String finalName = name;
            Optional<HomeComponent> home = HOME_DATA.get(ctx.getSource().getPlayer()).getHomes()
                    .stream().filter(v -> v.getName().equals(finalName)).findFirst();

            if (!home.isPresent()) {
                ctx.getSource().sendFeedback(new LiteralText("Something went wrong adding the home!").formatted(Formatting.RED), true);
                return 1;
            }

            ctx.getSource().sendFeedback(new TranslatableText("Home %s added successfully!",
                    new LiteralText(name).styled(s -> s.withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, home.get().toText(ctx.getSource().getMinecraftServer())))
                            .withColor(Formatting.GOLD))).formatted(Formatting.LIGHT_PURPLE), false);
        } else {
            ctx.getSource().sendFeedback(new LiteralText("Couldn't add the home (probably already exists)!").formatted(Formatting.RED), false);
            return 1;
        }
        return 1;
    }

    int homeDel(CommandContext<ServerCommandSource> ctx, String name) throws CommandSyntaxException {
        if (HOME_DATA.get(ctx.getSource().getPlayer()).removeHome(name)) {
            Optional<HomeComponent> home = HOME_DATA.get(ctx.getSource().getPlayer()).getHomes()
                    .stream().filter(v -> v.getName().equals(name)).findFirst();

            if (home.isPresent()) {
                ctx.getSource().sendFeedback(new LiteralText("Something went wrong removing the home!").formatted(Formatting.RED), true);
                return 1;
            }

            ctx.getSource().sendFeedback(new TranslatableText("Home %s deleted successfully!",
                    new LiteralText(name).formatted(Formatting.GOLD)).formatted(Formatting.LIGHT_PURPLE), false);
        } else {
            ctx.getSource().sendFeedback(new LiteralText("Couldn't remove the home!").formatted(Formatting.RED), false);
            return 1;
        }
        return 1;
    }


    int homeList(CommandContext<ServerCommandSource> ctx) throws CommandSyntaxException {
        return homeList(ctx, ctx.getSource().getPlayer());
    }
    int homeList(CommandContext<ServerCommandSource> ctx, ServerPlayerEntity player) {
        List<HomeComponent> homes = HOME_DATA.get(player).getHomes();
        List<Text> list = new ArrayList<>();
        homes.stream().sorted((h1, h2) -> h1.getName().compareToIgnoreCase(h2.getName())).forEach(h ->
                list.add(new LiteralText(h.getName()).styled(s ->
                        s.withClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/home " + h.getName()))
                                .withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT,
                                        LiteralText.EMPTY.copy().append(new LiteralText("Click to teleport.\n").formatted(Formatting.ITALIC))
                                                .append(h.toText(ctx.getSource().getMinecraftServer()))))
                                .withColor(Formatting.GOLD))));
        ctx.getSource().sendFeedback(new TranslatableText("%s/%s:\n", homes.size(), config.getValue("max-homes")).append(TextUtils.join(list, new LiteralText(", "))), false);
        return 1;
    }

boolean deathStore(ServerPlayerEntity player, DamageSource damageSource) {
        DeathComponent death = new DeathComponent(
                player.getPos(),
                player.getPitch(),
                player.getYaw(),
                player.getServerWorld().getRegistryKey().getValue(),
                damageSource.getName());
        DEATH_DATA.get(player).setDeath(death);
        return true;
    }

    int deathInfo(CommandContext<ServerCommandSource> ctx) throws CommandSyntaxException {
        ctx.getSource().sendFeedback(DEATH_DATA.get(ctx.getSource().getPlayer()).getDeath().toText(ctx.getSource().getMinecraftServer()), false);
        return 1;
    }

    int deathInit(CommandContext<ServerCommandSource> ctx) throws CommandSyntaxException {
        ServerPlayerEntity player = ctx.getSource().getPlayer();
        DeathComponent death = DEATH_DATA.get(player).getDeath();

        if (checkCooldown(player)) return 1;

        TeleportUtils.genericTeleport((boolean) config.getValue("bossbar"), (int) config.getValue("stand-still"), player, () -> {
            player.teleport(
                    ctx.getSource().getMinecraftServer().getWorld(RegistryKey.of(Registry.WORLD_KEY, death.getDimID())),
                    death.getX(), death.getY(), death.getZ(),
                    death.getYaw(), death.getPitch());
            recentRequests.put(player.getUuid(), Instant.now().getEpochSecond());
        });

        return 1;
    }
}
