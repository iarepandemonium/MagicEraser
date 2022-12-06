package net.pandette.config;

import com.google.gson.annotations.SerializedName;
import lombok.Data;

import java.util.List;

/**
 * This class is a code representation of the json file that stores the bot configuration. This must be placed
 * in the top level directory where the bot is being run from.
 */
@Data
public class BotConfiguration {

    /**
     * The Bot token which is provided by the discord api.
     */
    @SerializedName("bot-token")
    private String botToken;

    /**
     * This indicates if this is in production or not.
     */
    @SerializedName("production")
    private Boolean production;

    /**
     * A list of admins if you will enable admin override for something for the bot.
     */
    @SerializedName("admin")
    private List<String> admins;

}
