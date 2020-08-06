package com.test.jdsearchtest.util;

import com.test.jdsearchtest.pojo.Content;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

public class HtmlParseUtil {
    /**
     * @param keyword 可以灵活的爬取指定的关键字
     * @return
     */
    public static List<Content> parseHtml(String keyword){
        //要爬取的网址
        String url="https://search.jd.com/Search?keyword="+keyword;
        //该对象就是js中的 Document对象
        Document document = null;
        try {
            document = Jsoup.parse(new URL(url), 3000);
        } catch (IOException e) {
            e.printStackTrace();
        }
        //获取id为J_goodsList的元素
        Element element = document.getElementById("J_goodsList");
        //获取所有的 li标签
        Elements elements = element.getElementsByTag("li");
        //将爬取到的数据添加容器中
        List<Content> goodsList = new ArrayList<>();
        for (Element li : elements) {
            //获取li下的第一个img标签及src属性值
            String img = li.getElementsByTag("img").eq(0).attr("src");
            //获取class为 p-price的标签的文本内容 如果li中只有一个class为 p-price的标签可以不用 .eq()
            String price = li.getElementsByClass("p-price").eq(0).text();
            //获取class为 p-name标签的文本内容
            String name = li.getElementsByClass("p-name").eq(0).text();
            goodsList.add(new Content(name,price,img));
        }
        return goodsList;
    }
}
