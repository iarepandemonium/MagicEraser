package net.pandette.pojo;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import lombok.Value;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageHistory;
import net.dv8tion.jda.api.entities.channel.Channel;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.pandette.MagicEraser;
import net.pandette.config.ChannelData;
import net.pandette.config.ServerConfig;
import net.pandette.utils.UserMessages;
import net.pandette.utils.Utility;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * This is the main class for message deletion for each individual guild.
 */
@Value
public class GuildMessageDeletion {

    //GSON object for easy json creation.
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

    //ID for the guild
    String id;

    //List of errors that are being sent to the console.
    List<ErrorStatus> errors;

    /**
     * This is the constuctor for the class. This will create the class and also begin the message deletion.
     * It is run in its own independent thread so that one guild will not be affected by an opposing guild that
     * might take up the bots resources and the bot can balance between them as rate limiting allows.
     * @param id Id for the guild
     */
    public GuildMessageDeletion(String id) {
        this.id = id;
        errors = new ArrayList<>();

        new Thread(() -> {
            while (true) {
                try {
                    channelDeletion(Long.parseLong(id));
                } catch (Exception e) {
                    String errorMessage = getErrorMessage(id, e.getClass().getName() + " " + e.getMessage());
                    if (isErrorMessageSpam(Long.parseLong(id), errorMessage)) return;
                    System.out.println(Utility.timestamp() + ": " + errorMessage);
                }
            }
        }).start();
    }

    /**
     * This method is the main entry point for channel deletion. It checks to make sure the guild is actually active.
     * This way if the guild is not active it will not attempt to run it. A guild becomes inactive when the bot is no
     * longer in the server. It attempts to write any debug data necessary to the logs, as well as cycling thru all the
     * channels given to delete messages in said channel.
     * @param guildId Guild ID
     */
    private void channelDeletion(long guildId) {
        if(!isActive()) return;
        Guild guild = MagicEraser.getJda().getGuildById(guildId);

        ServerConfig config = getConfig();
        if (config == null) return;

        attemptToWriteDebugData(guild, config);

        if (config.getChannelData() == null) config.setChannelData(new ArrayList<>());

        config.getChannelData().forEach(
                d -> {
                    TextChannel channel = guild.getChannelById(TextChannel.class, d.getChannelId());
                    if (channel == null) return;

                    try {
                        deleteOldMessages(channel, d);
                    } catch (Exception e) {
                        String errorMessage = getErrorMessage(guild, channel, e.getMessage());
                        if (isErrorMessageSpam(guild.getIdLong(), errorMessage)) return;
                        System.out.println(Utility.timestamp() + ": " + errorMessage);
                    }

                }
        );

    }

    /**
     * This currently just writes the server name to the config because its hard to help debug if we cannot check
     * individual configs.
     * @param guild Guild
     * @param config Configuration
     */
    private void attemptToWriteDebugData(Guild guild, ServerConfig config){
        if (config.getDateCreated() == null) config.setDateCreated(System.currentTimeMillis());

        if (config.getServername() != null && config.getServername().equals(guild.getName())) return;

        config.setServername(guild.getName());
        try {
            Utility.writeFile(configLocation(), GSON.toJson(config));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * This creates an error message with only the guild id.
     * @param guild guild id
     * @param error Error message to make
     * @return Formatted error message
     */
    private String getErrorMessage(String guild, String error) {
        return String.format(UserMessages.ERROR_MESSAGE_GUILD_ID_ONLY,
                guild,
                error);
    }

    /**
     * This creates an error message with the guild name, and the guild id.
     * @param guild Guild
     * @param error Error message to make
     * @return Formatted error message
     */
    private String getErrorMessage(Guild guild, String error) {
        return String.format(UserMessages.ERROR_MESSAGE_NO_CHANNEL,
                guild.getName(),
                guild.getId(),
                error);
    }

    /**
     * This creates an error message with the guild name, the guild id, the channel name, and the
     * channel id.
     * @param guild Guild
     * @param channel Channel
     * @param error Error message to make
     * @return Formatted Error Message
     */
    private String getErrorMessage(Guild guild, Channel channel, String error) {
        return String.format(UserMessages.ERROR_MESSAGE_CHANNEL,
                guild.getName(),
                guild.getId(),
                channel.getName(),
                channel.getIdLong(),
                error);
    }

    /**
     * Checks if the bot is still in the guild
     * @return True if it is, false if it is not.
     */
    private boolean isActive() {
        return MagicEraser.getJda().getGuildById(id) != null;
    }

    /**
     * Gets the file location of the config for this guild
     * @return Location of the configuration file.
     */
    private String configLocation(){
        return "configs/" + id + ".json";
    }

    /**
     * Gets the server config if it exists.
     * @return Server Config if it exists, null if it does not.
     */
    private ServerConfig getConfig() {
        File configs = new File("configs");
        if (!configs.exists()) configs.mkdirs();
        File f = new File(configLocation());
        ServerConfig config;
        if (!f.exists()) throw new RuntimeException("Configuration File is missing for " + id + ".");
        else {
            try {
                config = GSON.fromJson(Utility.readFile(configLocation()), ServerConfig.class);
            } catch (IOException e) {
                throw new RuntimeException("Configuration File is missing for " + id + ": " + e.getMessage());
            }
        }
        return config;
    }

    /**
     * Checks a specific error message to see if it is considered a spam message (Same message within 5 minutes,
     * if it is, it will return true,
     * if it is not, it will return false.
     * @param guild Guild for the message
     * @param error Error message being sent
     * @return True for spam, false for not spam.
     */
    private boolean isErrorMessageSpam(long guild, String error) {
        long now = System.currentTimeMillis();
        ErrorStatus freshError = new ErrorStatus(String.valueOf(guild), error, now);
        if (errors.contains(freshError)) {
            ErrorStatus previousErrorStatus = errors.get(errors.indexOf(freshError));
            long expirationTime = previousErrorStatus.getTimeReceived() + TimeUnit.MINUTES.toMillis(5);
            if (expirationTime < now) errors.remove(previousErrorStatus);
            else return true;
        }

        errors.add(freshError);
        return false;
    }

    /**
     * Method that deletes old messages in a channel based on time or message count.
     * @param channel Channel to delete old messages
     * @param data Channel data
     */
    private void deleteOldMessages(TextChannel channel, ChannelData data) {
        List<Message> nonPinNonOldMessages = new ArrayList<>();
        MessageHistory channelHistory = channel.getHistoryFromBeginning(100).complete();

        List<Message> oldestToNewest = new ArrayList<>(channelHistory.getRetrievedHistory());
        //Original list comes out newest to oldest.
        Collections.reverse(oldestToNewest);

        deleteTimeBased(oldestToNewest, nonPinNonOldMessages, data);
        deleteMessageCount(nonPinNonOldMessages, data);


    }

    /**
     * Deletes old messages based on time, if the time is -1 then it will simply add it to the nonpinnonold list.
     * If the message has expired, it will delete it.
     * @param oldestToNewest Messages ordered oldest to newest
     * @param nonPinNonOldMessages Messages that are not pinned and not old
     * @param data Channel data.
     */
    private void deleteTimeBased(List<Message> oldestToNewest, List<Message> nonPinNonOldMessages, ChannelData data) {
        long now = System.currentTimeMillis();

        for (Message message : oldestToNewest) {
            if (message.isPinned()) continue;

            long messageCreation = message.getTimeCreated().toInstant().toEpochMilli();
            long expirationTime = data.getTime();
            if (expirationTime == -1 || now <= messageCreation + expirationTime) {
                nonPinNonOldMessages.add(message);
                continue;
            }
            message.delete().queue();
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                //Do nothing if interrupted.
            }
        }
    }

    /**
     * Delete messages based on message count, if count is -1 then it will not delete based on count.
     * @param nonPinNonOldMessages List of non pinned and non old messages
     * @param data Channel data
     */
    private void deleteMessageCount(List<Message> nonPinNonOldMessages, ChannelData data) {
        int messageCount = data.getMessageCount();
        if (messageCount == -1) return;

        int currentMessages = nonPinNonOldMessages.size();

        int deleteTooMuch = currentMessages - messageCount;

        for (int messageIndex = 0; messageIndex < deleteTooMuch; messageIndex++) {
            nonPinNonOldMessages.get(messageIndex).delete().queue();
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                //Do nothing if interrupted.
            }
        }
    }
}
