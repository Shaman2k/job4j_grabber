package ru.job4j.grabber;

import ru.job4j.grabber.model.Post;
import ru.job4j.grabber.utils.HabrCareerDateTimeParser;

import java.io.InputStream;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

public class PsqlStore implements Store {
    private final Connection connection;

    public PsqlStore(Properties config) throws SQLException {
        try {
            Class.forName(config.getProperty("jdbc.driver"));
        } catch (Exception e) {
            throw new IllegalStateException(e);
        }
        connection = DriverManager.getConnection(
                config.getProperty("url"),
                config.getProperty("username"),
                config.getProperty("password")
        );
    }

    @Override
    public void save(Post post) {
        try (PreparedStatement statement = connection.prepareStatement(
                "INSERT INTO posts (title, link, description, created_date) VALUES (?, ?, ?, ?) "
                        + "ON CONFLICT(link) DO UPDATE "
                        + "SET title= EXCLUDED.title, "
                        + "link = EXCLUDED.link, "
                        + "description = EXCLUDED.description, "
                        + "created_date = EXCLUDED.created_date"
        )) {
            statement.setString(1, post.getTitle());
            statement.setString(2, post.getLink());
            statement.setString(3, post.getDescription());
            statement.setTimestamp(4, Timestamp.valueOf(post.getCreated()));
            statement.execute();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Override
    public List<Post> getAll() {
        List<Post> result = new ArrayList<>();
        try (PreparedStatement statement = connection.prepareStatement(
                "SELECT  * FROM posts;")) {
            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    result.add(getPost(resultSet));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return result;
    }

    @Override
    public Post findById(int id) {
        Post result = null;
        try (PreparedStatement statement = connection.prepareStatement(
                "SELECT * FROM posts WHERE id = ?;")) {
            statement.setInt(1, id);
            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    result = getPost(resultSet);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return result;
    }

    private Post getPost(ResultSet resultSet) throws SQLException {
        return new Post(
                resultSet.getInt("id"),
                resultSet.getString("title"),
                resultSet.getString("link"),
                resultSet.getString("description"),
                resultSet.getTimestamp("created_date").toLocalDateTime()
        );
    }

    public static void main(String[] args) throws SQLException {
        Properties config = new Properties();
        try (InputStream input = PsqlStore.class.getClassLoader()
                .getResourceAsStream("rabbit.properties")) {
            config.load(input);
        } catch (Exception e) {
            throw new IllegalStateException(e);
        }
        PsqlStore store = new PsqlStore(config);
        HabrCareerParse parser = new HabrCareerParse(new HabrCareerDateTimeParser());

        List<Post> list = parser.parse();
        list.forEach(System.out::println);
        list.forEach(store::save);
        System.out.println();
        Post post = store.findById(5);
        System.out.println(post);
        post.setTitle("JavaScript Developer");
        store.save(post);
        System.out.println(store.findById(5));

        System.out.println();
        store.getAll().forEach(System.out::println);
    }
}
