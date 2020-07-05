package com.tc.jd.utils;


import com.tc.jd.pojo.Content;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

@Component
public class HtmlParseUtil {

    public static List<Content> parseHtmlByKey(String keyword,String page) throws IOException {
        String url="https://search.jd.com/Search?keyword="+keyword+"&page="+page;
        Document document = Jsoup.parse(new URL(url), 30000);
        Element element = document.getElementById("J_goodsList");
        Elements elements = element.getElementsByTag("li");
        List<Content> list = new ArrayList<>();
        for (Element ele:elements) {
            String img = ele.getElementsByTag("img").eq(0).attr("src");
            String price = ele.getElementsByClass("p-price").eq(0).text();
            String title = ele.getElementsByClass("p-name p-name-type-2").eq(0).text();
            Content content = new Content();
            content.setImg(img);
            content.setPrice(price);
            content.setTitle(title);
            list.add(content);
            System.out.println(content.toString());
        }
        return list;
    }

//    public static void main(String[] args) throws IOException {
//        parseHtmlByKey("java");
//    }
}
