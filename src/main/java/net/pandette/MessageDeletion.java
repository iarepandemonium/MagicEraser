package net.pandette;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import lombok.AllArgsConstructor;
import lombok.Value;
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
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.TimeZone;
import java.util.concurrent.TimeUnit;

public class MessageDeletion implements Runnable {

    private static final Gson gson = new GsonBuilder().setPrettyPrinting().create();
    public static final Set<String> guilds = new HashSet<>();
    private static final List<Error> errors = new ArrayList<>();


    @AllArgsConstructor
    @Value
    record Error(String guild, String error, long timeReceived) {
        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            Error error1 = (Error) o;
            return guild.equals(error1.guild) && error.equals(error1.error);
        }

        @Override
        public int hashCode() {
            return Objects.hash(guild, error);
        }
    }


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
                guilds.add(guild);
                try {
                    runGuildThread(guild);
                } catch (Exception e) {
                    e.printStackTrace();
                }

            }
        } catch (Exception e) {
            e.printStackTrace();
        }


    }

    public static void runGuildThread(String guild) {
        new Thread(() -> {
            while (true) {
                loopChannels(Long.parseLong(guild));
            }
        }).start();
    }

    public static void loopChannels(long guild) {
        File configs = new File("configs");
        if (!configs.exists()) configs.mkdirs();
        String filename = "configs/" + guild + ".json";
        File f = new File(filename);
        ServerConfig config;
        if (!f.exists()) return;
        else {
            try {
                config = gson.fromJson(Utility.readFile(filename), ServerConfig.class);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
        Guild g = MagicEraser.getJda().getGuildById(guild);
        if (config.getServername() == null || !config.getServername().equals(g.getName())) {
            config.setServername(g.getName());
            try {
                Utility.writeFile(filename, gson.toJson(config));
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }

        for (ChannelData d : config.getChannelData()) {
            try {
                deleteMessages(guild, d.getChannelId(), d);
            } catch (Exception perm) {
                Channel c = null;
                try {
                    for (Channel ch : g.getChannels(true)) {
                        if (ch.getIdLong() == d.getChannelId()) {
                            c = ch;
                            break;
                        }
                    }
                } catch (Exception e) {
                    String error = String.format("The Guild %s [%s] in [%d] has encountered the following error: %s%n",

                            g.getName(),
                            guild,
                            d.getChannelId(), e.getMessage());

                    if (checkErrorSpam(guild, error)) return;

                    System.out.printf(currentTimestamp() + ": " + error);
                }

                String name = "";
                if (c != null) {
                    name = c.getName();
                }
                String error = String.format("The Guild %s [%s] in %s [%d] has encountered the following error: %s%n",
                        g.getName(),
                        guild,
                        name,
                        d.getChannelId(),
                        perm.getMessage());
                if (checkErrorSpam(guild, error)) return;
                System.out.printf(currentTimestamp() + ": " + error);
            }
        }
    }

    private static boolean checkErrorSpam(long guild, String error) {
        Error err = new Error(String.valueOf(guild), error, System.currentTimeMillis());
        if (errors.contains(err)) {
            Error curr = errors.get(errors.indexOf(err));
            if (curr.timeReceived() + TimeUnit.MINUTES.toMillis(5) < System.currentTimeMillis()) {
                errors.remove(curr);
            } else {
                return true;
            }
        }

        errors.add(err);
        return false;
    }

    public static String currentTimestamp() {
        Calendar c = Calendar.getInstance(TimeZone.getTimeZone("America/New_York"));
        DateFormat f = DateFormat.getDateTimeInstance(DateFormat.MEDIUM, DateFormat.MEDIUM);
        return f.format(c.getTime());
    }

    public static void deleteMessages(long guild, long channel, ChannelData c) {
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



