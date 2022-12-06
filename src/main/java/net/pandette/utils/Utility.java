package net.pandette.utils;

import net.pandette.config.ChannelData;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.text.DateFormat;
import java.util.Calendar;
import java.util.List;
import java.util.TimeZone;

/**
 * This is a utility class. Only public static methods should remain here that have no association with other classes.
 */
public class Utility {

    /**
     * Reads a file.
     * @param path Path to file
     * @return File contents as a string
     * @throws IOException *sadface* Failed to read it.
     */
    public static String readFile(String path) throws IOException {
        byte[] encoded = Files.readAllBytes(Paths.get(path));
        return new String(encoded, Charset.defaultCharset());
    }

    /**
     * Writes to a file
     * @param path The path to write it to
     * @param json The json that will be written to the file, this turns it into a string to fille the contents.
     * @throws IOException *sadface* failed to write it.
     */
    public static void writeFile(String path, String json) throws IOException {
        BufferedWriter br = new BufferedWriter(new FileWriter(path, false));
        br.write(json);
        br.close();
    }

    /**
     * Gets the channel data for a specific channel based on the channel id.
     * @param dataList List of Channel Datas
     * @param id ID for the channel
     * @return Channel Data for the id or null if not found.
     */
    public static ChannelData getChannelData(List<ChannelData> dataList, long id) {
        for (ChannelData data : dataList) {
            if (data.getChannelId() == id) return data;
        }
        return null;
    }

    /**
     * Creates a time stamp in EST timezone so that its easier to tell when an error message was sent because I am
     * from EST and lazy.
     * @return Timestamp
     */
    public static String timestamp() {
        Calendar c = Calendar.getInstance(TimeZone.getTimeZone("America/New_York"));
        DateFormat f = DateFormat.getDateTimeInstance(DateFormat.MEDIUM, DateFormat.MEDIUM);
        return f.format(c.getTime());
    }

}
