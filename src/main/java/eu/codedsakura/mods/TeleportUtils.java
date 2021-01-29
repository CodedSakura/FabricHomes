package eu.codedsakura.mods;

import net.minecraft.entity.boss.BossBar;
import net.minecraft.entity.boss.CommandBossBar;
import net.minecraft.network.packet.s2c.play.TitleS2CPacket;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.LiteralText;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.Vec3d;

import java.util.Timer;
import java.util.TimerTask;

public class TeleportUtils {
    public static void genericTeleport(boolean bossBar, double standStillTime, ServerPlayerEntity who, Action onCounterDone) {
        MinecraftServer server = who.server;
        Timer timer = new Timer();
        final double[] counter = {standStillTime};
        final Vec3d[] lastPos = {who.getPos()};
        CommandBossBar standStillBar = null;
        if (bossBar) {
            standStillBar = server.getBossBarManager().add(new Identifier("standstill"), LiteralText.EMPTY);
            standStillBar.addPlayer(who);
            standStillBar.setColor(BossBar.Color.PINK);
        }
        who.networkHandler.sendPacket(new TitleS2CPacket(0, 10, 5));
        CommandBossBar finalStandStillBar = standStillBar;
        timer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                if (counter[0] == 0) {
                    if (bossBar) {
                        finalStandStillBar.removePlayer(who);
                        server.getBossBarManager().remove(finalStandStillBar);
                    } else {
                        who.sendMessage(new LiteralText("Teleporting!").formatted(Formatting.LIGHT_PURPLE), true);
                    }
                    new Timer().schedule(new TimerTask() {
                        @Override
                        public void run() {
                            who.networkHandler.sendPacket(new TitleS2CPacket(TitleS2CPacket.Action.RESET, null));
                        }
                    }, 500);
                    timer.cancel();
                    onCounterDone.run();
                    return;
                }

                Vec3d currPos = who.getPos();
                if (lastPos[0].equals(currPos)) {
                    counter[0] -= .25;
                } else {
                    lastPos[0] = currPos;
                    counter[0] = standStillTime;
                }

                if (bossBar) {
                    finalStandStillBar.setPercent((float) (counter[0] / standStillTime));
                } else {
                    who.sendMessage(new LiteralText("Stand still for ").formatted(Formatting.LIGHT_PURPLE)
                            .append(new LiteralText(Integer.toString((int) Math.floor(counter[0] + 1))).formatted(Formatting.GOLD))
                            .append(new LiteralText(" more seconds!").formatted(Formatting.LIGHT_PURPLE)), true);
                }
                who.networkHandler.sendPacket(new TitleS2CPacket(TitleS2CPacket.Action.SUBTITLE,
                        new LiteralText("Please stand still...").formatted(Formatting.RED, Formatting.ITALIC)));
                who.networkHandler.sendPacket(new TitleS2CPacket(TitleS2CPacket.Action.TITLE,
                        new LiteralText("Teleporting!").formatted(Formatting.LIGHT_PURPLE, Formatting.BOLD)));
            }
        }, 0, 250);
    }

    public interface Action {
        void run();
    }
}
