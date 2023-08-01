package team.jndk.praktyki.praktyki_spring.model.data.dao;

import team.jndk.praktyki.praktyki_spring.model.data.Channel;

import java.sql.*;
import java.util.List;

public class DBGeneratorDaoImpl implements DataGeneratorDao {
    private static String database_url;

    public DBGeneratorDaoImpl(String url) {
        database_url = url;
    }

    @Override
    public void saveChannels(List<Channel> channels) {

        try (Connection conn = DriverManager.getConnection(database_url);
             PreparedStatement stmt = conn.prepareStatement(
                     "INSERT OR REPLACE INTO Channels (ChannelNames, GoogleChanID) VALUES (?, ?)",
                     Statement.RETURN_GENERATED_KEYS
             )) {


            channels.forEach(channel -> {
                try {
                    stmt.setString(1, channel.getGoogleId());

                    int affectedRows = stmt.executeUpdate();

                    if (affectedRows == 0) {
                        throw new SQLException("Creating channel failed, no rows affected.");
                    }
                    try (ResultSet generatedKeys = stmt.getGeneratedKeys()) {
                        if (generatedKeys.next()) {
                            channel.setId(generatedKeys.getInt(1));
                        } else {
                            throw new SQLException("Creating channel failed, no ID obtained.");
                        }
                    }
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            });

            System.out.println("Channels successfully saved to the database.");

        } catch (SQLException e) {
            e.printStackTrace();
        }
        saveVideos(channels);
    }

    private void saveVideos(List<Channel> channels) {

        try (Connection conn = DriverManager.getConnection(database_url);
             PreparedStatement stmt = conn.prepareStatement(
                     "INSERT INTO Videos (title, GoogleVidID, views, likes, comments, scannedDate, ChanID) VALUES (?, ?, ?, ?, ?, ?, ?)"
             )) {

            // Insert video data into the database
            channels.forEach(channel -> channel.getVideos().forEach(video -> {
                try {
                    stmt.setString(1, video.getTitle());
                    stmt.setString(2, video.getGoogleId());
                    stmt.setInt(3, video.getViews());
                    stmt.setInt(4, video.getLikes());
                    stmt.setInt(5, video.getComments());
                    stmt.setLong(6, video.getScannedDate());
                    stmt.setLong(7, channel.getId());
                    stmt.executeUpdate();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }));

            System.out.println("Videos successfully saved to the database.");

        } catch (SQLException e) {
            e.printStackTrace();
        }

    }
}

