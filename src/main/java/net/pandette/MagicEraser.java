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
import net.pandette.utils.Utility;

import java.io.File;
import java.io.IOException;

public class MagicEraser extends ListenerAdapter {

    private static final String BOT_CONFIGURATION_FILE_NAME = "BotConfiguration.json";

    private static final Gson gson = new GsonBuilder().setPrettyPrinting().create();

    @Getter
    private static JDA jda;

    @Getter
    private static BotConfiguration configuration;

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
            new MessageDeletion().run();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    private static JDA createJDA() {
        jda = JDABuilder
                .createDefault(configuration.getBotToken())
                .setEnabledIntents(GatewayIntent.GUILD_MESSAGES, GatewayIntent.GUILD_VOICE_STATES,
                        GatewayIntent.GUILD_EMOJIS_AND_STICKERS, GatewayIntent.SCHEDULED_EVENTS)
                .setChunkingFilter(ChunkingFilter.ALL)
                .build();
        //jda.addEventListener(new DiscordListener());
        return jda;

    }
}
