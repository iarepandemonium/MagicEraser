package net.pandette.pojo;

import lombok.AllArgsConstructor;
import lombok.Value;

import java.util.Objects;

/**
 * This error message is used to see how often a unique error message is sent, the primary goal is to prevent
 * the same error message from spamming the logs when a guild has not fixed the issue. Error messages should only
 * hit the logs every 5 minutes if they are the same message.
 */
@Value
@AllArgsConstructor
public class ErrorStatus {

    //Guild
    String guild;

    //Message
    String error;

    //Time it was last received which will be compared to current time.
    long timeReceived;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ErrorStatus error1 = (ErrorStatus) o;
        return guild.equals(error1.guild) && error.equals(error1.error);
    }

    @Override
    public int hashCode() {
        return Objects.hash(guild, error);
    }
}
