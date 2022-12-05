package net.pandette;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageHistory;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.pandette.config.ChannelData;
import net.pandette.config.ServerConfig;
import net.pandette.utils.Utility;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class MessageDeletion implements Runnable {

    private static final Gson gson = new GsonBuilder().setPrettyPrinting().create();


    public MessageDeletion() {
    }

    @Override
    public void run() {
        while (true) {
            try {
                File configs = new File("configs");
                if (!configs.exists()) configs.mkdirs();
                for (File f : configs.listFiles()) {
                    if (!f.getName().endsWith(".json")) continue;
                    String guild = f.getName().replace(".json", "");
                    try {
                        loopChannels(Long.parseLong(guild));
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    Thread.sleep(1000);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }


    }

    public void loopChannels(long guild) {
        File configs = new File("configs");
        if (!configs.exists()) configs.mkdirs();
        String filename = "configs/" + guild + ".json";
        File f = new File(filename);
        ServerConfig config;
        if (!f.exists()) config = new ServerConfig();
        else {
            try {
                config = gson.fromJson(Utility.readFile(filename), ServerConfig.class);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }

        for (ChannelData d : config.getChannelData()) {
            deleteMessages(guild, d.getChannelId(), d);
        }
    }

    public void deleteMessages(long guild, long channel, ChannelData c) {
        Guild g = MagicEraser.getJda().getGuildById(guild);

        if (g == null) return;

        TextChannel chan = g.getChannelById(TextChannel.class, channel);
        if (chan == null) return;


        new Thread(() -> {
            MessageHistory hist = chan.getHistoryFromBeginning(100).complete();
            List<Message> notPinnedNotOld = new ArrayList<>();
            long now = System.currentTimeMillis();

            List<Message> list = new ArrayList<>(hist.getRetrievedHistory());
            Collections.reverse(list);
            for (Message m : list) {
                if (m.isPinned()) continue;

                long timeMillis = m.getTimeCreated().toInstant().toEpochMilli();

                if (c.getTime() == -1 || now <= c.getTime() + timeMillis) {
                    notPinnedNotOld.add(m);
                    continue;
                }

                m.delete().queue();
            }

            if (c.getMessageCount() == -1) return;

            int deleteTooMuch = notPinnedNotOld.size() - c.getMessageCount();

            for (int i = 0; i < deleteTooMuch; i++) {
                notPinnedNotOld.get(i).delete().queue();
            }
        }).start();

    }


}