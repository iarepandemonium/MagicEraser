package net.pandette;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.dv8tion.jda.api.entities.channel.unions.GuildChannelUnion;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.pandette.config.ChannelData;
import net.pandette.config.ServerConfig;
import net.pandette.utils.Utility;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.concurrent.TimeUnit;

public class EraserCommand extends ListenerAdapter {

    private static final Gson gson = new GsonBuilder().setPrettyPrinting().create();

    public EraserCommand() {
        MagicEraser.getJda().upsertCommand("eraser", "Allows you to setup the Magic Eraser to delete from channel based on the settings you input.")
                .addOption(OptionType.CHANNEL, "channel", "Name of the channel you wish to have erase", true)
                .addOption(OptionType.BOOLEAN, "remove", "If option is selected all other " +
                        "settings will be ignore and Eraser will ignore this channel.", false)
                .addOption(OptionType.INTEGER, "message-count", "The number of messages before Magic Eraser will start deleting.", false)
                .addOption(OptionType.STRING, "time", "The time you want to set before Magic Eraser will start deleting", false)
                .queue();
    }

    @Override
    public void onSlashCommandInteraction(@NotNull SlashCommandInteractionEvent event) {
        onCommand(event);
    }

    public void onCommand(SlashCommandInteractionEvent event) {
        if (!event.getName().equalsIgnoreCase("eraser")) {
            System.out.println("????");
            return;
        }

        File configs = new File("configs");
        if (!configs.exists()) configs.mkdirs();
        String filename = "configs/" + event.getGuild().getId() + ".json";
        File f = new File(filename);
        ServerConfig config;
        if (!f.exists()) config = new ServerConfig();
        else {
            try {
                config = gson.fromJson(Utility.readFile(filename), ServerConfig.class);
            } catch (IOException e) {
                event.reply("There was a problem saving the configuration file!").queue();
                throw new RuntimeException(e);
            }
        }

        if (config.getChannelData() == null) config.setChannelData(new ArrayList<>());

        GuildChannelUnion channel = event.getOption("channel").getAsChannel();
        OptionMapping remove = event.getOption("remove");
        ChannelData data = Utility.getChannelData(config.getChannelData(), channel.getIdLong());


        if (remove != null && !remove.getAsBoolean()) {
            if (data == null) {
                event.reply("That channel doesn't have any eraser data or has already had it's data removed!").queue();
                return;
            }

            config.getChannelData().remove(data);
            try {
                Utility.writeFile(filename, gson.toJson(config));
            } catch (IOException e) {
                event.reply("There was a problem saving the configuration file!").queue();
                throw new RuntimeException(e);
            }
            event.reply("Successfully removed channel data for " + channel.getName()).queue();
            return;
        }

        if (data == null) {
            data = new ChannelData();
        }


        data.setChannelId(channel.getIdLong());

        OptionMapping messageCount = event.getOption("message-count");
        OptionMapping time = event.getOption("time");
        if (messageCount == null && time == null) {
            event.reply("When adding or modifying a channel eraser data you must include time or message count.").queue();
            return;
        }

        String add = "";
        if (messageCount != null) {
            int mess = messageCount.getAsInt();
            if (mess <= 0) mess = -1;
            if (mess >= 20) {
                mess = 20;
                add = " Message Count was set to 20, because Magic Eraser doesn't permit more than 20 as a configuration.";
            }
            data.setMessageCount(mess);
            add += " Message Count Deletion is " + mess + " if time has not removed it first.";
        } else {
            data.setMessageCount(-1);
        }


        if (time != null) {
            String t = time.getAsString();
            if (t.equals("-1")) {
                data.setTime(-1);
            } else {

                int timeCount;
                try {
                    timeCount = Integer.parseInt(t.substring(0, t.length() - 1));
                } catch (Exception e) {
                    event.reply("Could not parse integer from time count. It must be a number with [s, m, d]. ie: 20s, 10m, 4d.").queue();
                    return;
                }

                long val;
                String qualifier = t.substring(t.length() - 1).toLowerCase();
                switch (qualifier) {
                    case "s" -> val = TimeUnit.SECONDS.toMillis(timeCount);
                    case "m" -> val = TimeUnit.MINUTES.toMillis(timeCount);
                    case "d" -> val = TimeUnit.DAYS.toMillis(timeCount);
                    default -> {
                        event.reply("Could not parse time unit. It must be a number with [s, m, d]. ie: 20s, 10m, 4d.").queue();
                        return;
                    }
                }
                add += " Time Deletion is " + t + ".";
                data.setTime(val);
            }
        } else {
            data.setTime(-1);
        }

        if (!config.getChannelData().contains(data)) {
            config.getChannelData().add(data);
        }

        try {
            Utility.writeFile(filename, gson.toJson(config));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        event.reply("Successfully saved config!" + add).queue();
    }



}
