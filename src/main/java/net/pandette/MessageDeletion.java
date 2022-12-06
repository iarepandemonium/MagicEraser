package net.pandette;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageHistory;
import net.dv8tion.jda.api.entities.channel.Channel;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.pandette.config.ChannelData;
import net.pandette.config.ServerConfig;
import net.pandette.utils.Utility;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

public class MessageDeletion implements Runnable {

    private static final Gson gson = new GsonBuilder().setPrettyPrinting().create();


    public MessageDeletion() {
    }

    @Override
    public void run() {
        try {
            File configs = new File("configs");
            if (!configs.exists()) configs.mkdirs();
            if (!configs.isDirectory()) return;

            for (File f : Objects.requireNonNull(configs.listFiles())) {
                if (!f.getName().endsWith(".json")) continue;
                String guild = f.getName().replace(".json", "");
                try {
                    new Thread(() -> {
                        while (true) {
                            loopChannels(Long.parseLong(guild));
                        }
                    }).start();
                } catch (Exception e) {
                    e.printStackTrace();
                }

            }
        } catch (Exception e) {
            e.printStackTrace();
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
            try {
                deleteMessages(guild, d.getChannelId(), d);
            } catch (Exception perm) {
                Guild g = MagicEraser.getJda().getGuildById(guild);
                Channel c = null;
                for (Channel ch : g.getChannels(true)) {
                    if (ch.getIdLong() == d.getChannelId()) {
                        c = ch;
                        break;
                    }
                }

                String name = "";
                if (c != null) {
                    name = c.getName();
                }
                System.out.printf(
                        "The Guild %s [%s] in %s [%d] has encountered the following error: %s%n",
                        g.getName(),
                        guild,
                        name,
                        d.getChannelId(),
                        perm.getMessage());
            }
        }
    }

    public void deleteMessages(long guild, long channel, ChannelData c) {
        Guild g = MagicEraser.getJda().getGuildById(guild);

        if (g == null) return;

        TextChannel chan = g.getChannelById(TextChannel.class, channel);
        if (chan == null) return;


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
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }

        if (c.getMessageCount() == -1) return;

        int deleteTooMuch = notPinnedNotOld.size() - c.getMessageCount();

        for (int i = 0; i < deleteTooMuch; i++) {
            notPinnedNotOld.get(i).delete().queue();
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }
    }

}



