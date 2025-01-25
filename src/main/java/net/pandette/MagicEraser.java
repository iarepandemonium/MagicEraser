package net.pandette;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import lombok.Getter;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.requests.GatewayIntent;
import net.dv8tion.jda.api.utils.ChunkingFilter;
import net.pandette.config.BotConfiguration;
import net.pandette.pojo.GuildMessageDeletion;
import net.pandette.utils.Utility;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class MagicEraser extends ListenerAdapter {

    private static final String BOT_CONFIGURATION_FILE_NAME = "BotConfiguration.json";

    private static final Gson gson = new GsonBuilder().setPrettyPrinting().create();

    public static final Map<String, GuildMessageDeletion> GUILD_DELETION = new HashMap<>();

    @Getter
    private static JDA jda;

    @Getter
    private static BotConfiguration configuration;

    /**
     * Entry point for the entire bot. This creates the bot, checks the config, and starts up the listener and the
     * message deletions.
     * @param args No args necessary this bot has no external args.
     */
    public static void main(String[] args) {
        System.out.println("Magic Eraser is starting up!");

        File configFile = new File(BOT_CONFIGURATION_FILE_NAME);
        if (!configFile.exists()) {
            System.out.println("The configuration file is missing, please create it: " + BOT_CONFIGURATION_FILE_NAME);
            System.exit(1);
        }

        try {
            configuration = gson.fromJson(Utility.readFile(BOT_CONFIGURATION_FILE_NAME), BotConfiguration.class);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        if (configuration.getBotToken() == null) {
            System.out.println("The Bot Token is not present in the configuration file! Exiting.");
            System.exit(1);
        }

        jda = createJDA();


        try {
            jda.awaitReady();
            jda.addEventListener(new EraserCommand());
            try {
                File configs = new File("configs");
                if (!configs.exists()) configs.mkdirs();
                if (!configs.isDirectory()) return;

                for (File f : Objects.requireNonNull(configs.listFiles())) {
                    if (!f.getName().endsWith(".json")) continue;
                    String guild = f.getName().replace(".json", "");

                    GuildMessageDeletion deletion = new GuildMessageDeletion(guild);
                    GUILD_DELETION.put(guild, deletion);
                }
            } catch (Exception e) {
                System.out.println("Fatale Error in attempting to read through configuration directory.");
            }
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Creates the JDA api with the intents we use.
     * Magic Eraser does not require any *Privileged Gateway Intents* which at the time of writing this bot is:
     * Presence Intent
     * Server Member Intent
     * Message Content Intent
     *
     * This bot does not read the content of the messages it deletes, it merely deletes them without regard for what
     * content they possess.
     */
    private static JDA createJDA() {
        jda = JDABuilder
                .createDefault(configuration.getBotToken())
                .setEnabledIntents(GatewayIntent.GUILD_MESSAGES, GatewayIntent.GUILD_VOICE_STATES,
                        GatewayIntent.GUILD_EXPRESSIONS, GatewayIntent.SCHEDULED_EVENTS)
                .setChunkingFilter(ChunkingFilter.ALL)
                .build();
        //jda.addEventListener(new DiscordListener());
        return jda;

    }
}
