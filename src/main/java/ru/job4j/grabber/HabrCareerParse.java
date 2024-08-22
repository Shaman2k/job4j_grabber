package ru.job4j.grabber;

import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import ru.job4j.grabber.model.Post;
import ru.job4j.grabber.utils.DateTimeParser;
import ru.job4j.grabber.utils.HabrCareerDateTimeParser;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class HabrCareerParse implements Parse {
    private final DateTimeParser dateTimeParser;
    private static final String SOURCE_LINK = "https://career.habr.com";
    public static final String PREFIX = "/vacancies?page=";
    public static final String SUFFIX = "&q=Java%20developer&type=all";
    public static final int NUMBER_OF_PAGES = 1;

    public HabrCareerParse(DateTimeParser dateTimeParser) {
        this.dateTimeParser = dateTimeParser;
    }

    private static String retrieveDescription(String link) {
        Connection connection = Jsoup.connect(link);
        try {
            Document document = connection.get();
            Element element = document.select(".vacancy-description__text").first();
            return element.text();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static void main(String[] args) {
        HabrCareerDateTimeParser dateTimeParser = new HabrCareerDateTimeParser();
        HabrCareerParse habrCareerParse = new HabrCareerParse(dateTimeParser);
        List<Post> posts = new ArrayList<>();
        for (int pageNumber = 1; pageNumber <= NUMBER_OF_PAGES; pageNumber++) {
            String fullLink = "%s%s%d%s".formatted(SOURCE_LINK, PREFIX, pageNumber, SUFFIX);
            posts.addAll(habrCareerParse.list(fullLink));
        }
        posts.forEach(System.out::println);
    }

    public List<Post> parse() {
        List<Post> posts = new ArrayList<>();
        for (int pageNumber = 1; pageNumber <= NUMBER_OF_PAGES; pageNumber++) {
            String fullLink = "%s%s%d%s".formatted(SOURCE_LINK, PREFIX, pageNumber, SUFFIX);
            posts.addAll(list(fullLink));
        }
        return posts;
    }

    @Override
    public List<Post> list(String link) {
        Connection connection = Jsoup.connect(link);
        List<Post> result = new ArrayList<>();
        try {
            Document document = connection.get();
            Elements rows = document.select(".vacancy-card__inner");
            rows.forEach(row -> {
                Element titleElement = row.select(".vacancy-card__title").first();
                Element linkElement = titleElement.child(0);
                Element dateElement = row.select(".vacancy-card__date")
                        .first()
                        .child(0);
                String postLink = String.format("%s%s", SOURCE_LINK, linkElement.attr("href"));

                Post post = new Post();
                post.setCreated(dateTimeParser.parse(dateElement.attr("datetime")));
                post.setTitle(titleElement.text());
                post.setLink(postLink);
                post.setDescription(retrieveDescription(postLink));
                result.add(post);
            });

        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return result;
    }
}