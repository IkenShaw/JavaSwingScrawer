package org.iken.crawl;

import org.apache.commons.httpclient.*;
import org.apache.commons.httpclient.methods.GetMethod;
import org.iken.untils.ExcelUtil;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.openqa.selenium.By;
import org.openqa.selenium.Cookie;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;

import java.net.URLEncoder;
import java.util.*;

/**
 * Http请求工具类
 * 请求的是普通的表单数据，返回HTML页面
 *
 * 需要导入commons-codec-1.3.jar
 */
public class WebCrawl {

    private static final String BASE_URL = "https://www.qcc.com/search?key=";
    private static final String HOME_URL = "https://www.qcc.com";

    /**
     * httpClient的get请求方式
     * @throws Exception
     */
    public static void doGet(Set<Cookie> cookieSet) throws Exception {
        // 把Excel读取的数据拿过来
        Map<String, Map<Integer, List<Object>>> data = ExcelUtil.DATA;
        Set<String> sheetSets = data.keySet();
        Iterator<String> sheetIterator = sheetSets.iterator();
        HttpClient client = new HttpClient();
        String sheetName = null;
        Map<Integer, List<Object>> sheetDataMap = null;
        Set<Integer> rowSets = null;
        Iterator<Integer> rowIterator = null;
        Integer rowNum = null;
        List<Object> rowData = null;
        StringBuffer urlBuffer = new StringBuffer();
        GetMethod method = null;
        String regisAddress = null;
        String regisMoney = null;
        // 股东跳转链接
        String ownerLink = null;
        while (sheetIterator.hasNext()) {
            // 循环拿sheet
            sheetName = sheetIterator.next();
            sheetDataMap = data.get(sheetName);
            rowSets = sheetDataMap.keySet();
            rowIterator = rowSets.iterator();
            while (rowIterator.hasNext()) {
                // 重要！！每次查询，都要睡2秒，防止查询过多被拉黑!!!
                Thread.sleep(1000);
                // 每一次拼接查询先将urlBuffer清空
                urlBuffer.delete(0, urlBuffer.length());
                // 拼接基本URL
                urlBuffer.append(BASE_URL);
                // 循环sheet中的行
                rowNum = rowIterator.next();
                // 拿到行数据
                rowData = sheetDataMap.get(rowNum);
                // 取第一列（公司名）拼接url
                urlBuffer.append(URLEncoder.encode((String)rowData.get(0), "UTF-8"));
                if (null == urlBuffer.toString() || !urlBuffer.toString().startsWith("http")) {
                    throw new Exception("请求地址格式不对");
                }
                // 开始发起请求
                method = new GetMethod(urlBuffer.toString());
                // 设置头
                method.addRequestHeader("Content-Type", "text/html; charset=utf-8");
                method.addRequestHeader("Host", "www.qichacha.com");
                method.addRequestHeader("Accept", "text/html, */*; q=0.01");
                method.addRequestHeader("User-Agent", "Mozilla/5.0 (Windows NT 10.0; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/69.0.3497.100 Safari/537.36");
                method.addRequestHeader("Referer", urlBuffer.toString());
                if (null != cookieSet) {
                    System.out.println(cookieSet.toString());
                    method.addRequestHeader("Cookie", cookieSet.toString());
                } else {
                    method.addRequestHeader("Cookie", "QCCSESSID=3lv40h3cchqrvnbatlr59g0li5; zg_did=%7B%22did%22%3A%20%22170d9ab630ef2-0cb6e4c67742278-4c302f7e-144000-170d9ab630f2c2%22%7D; zg_de1d1a35bfa24ce29bbf2c7eb17e6c4f=%7B%22sid%22%3A%201584199852820%2C%22updated%22%3A%201584200709941%2C%22info%22%3A%201584199852827%2C%22superProperty%22%3A%20%22%7B%7D%22%2C%22platform%22%3A%20%22%7B%7D%22%2C%22utm%22%3A%20%22%7B%7D%22%2C%22referrerDomain%22%3A%20%22www.baidu.com%22%2C%22cuid%22%3A%20%2282199a552574f452f85cf9b7c81e3787%22%7D; hasShow=1; _uab_collina=158419985499295683815355; acw_tc=3a4395c915841998572355626e3aa0be2a02aec25e0b9d52e75b6a5038");
                }
                int statusCode = client.executeMethod(method);
                if (statusCode == HttpStatus.SC_OK) {
                    // 返回200才开始提取页面内容
                    String html = method.getResponseBodyAsString();
                    Document document = Jsoup.parse(html);
                    Elements searchResultTable = document.getElementsByAttributeValue("id", "search-result");
                    if (!searchResultTable.isEmpty()){
                        Element firstRowData = searchResultTable.first(); // 表格下的结果集
                        Elements allOfTr = firstRowData.children(); // 结果集下所有行数据tr
                        Element firstTr = allOfTr.first(); // 所有行数据下的第一行数据
                        Elements allOfTd = firstTr.children(); // 第一行数据下的所有TD
                        Element thirdTd = allOfTd.get(2); // 所有td下数据的第3个td
                        Element firstAOfThird = thirdTd.child(0); // 第三个td下的第一个标签:<a>
                        // 取<a>下的所有em
                        StringBuffer temp = new StringBuffer();
                        temp.append(firstAOfThird.getElementsByTag("em").text().replace(" ", ""));
                        // 判断拿到第一条的结果与查询的企业名称是否与一样
                        if (((String)rowData.get(0)).equals(temp.toString())) {
                            // 名称一样后，代表搜索的公司是对的，可以取该td下公司的注册地址   第三个td下的所有p标签，取第3个是注册地址
                            regisAddress = thirdTd.getElementsByTag("p").get(2).text().replace(" ", "");
                            rowData.add(regisAddress);
                        }
                        // 注册资本 xpath: [@id="search-result"]/tr[1]/td[3]/p[1]/span[1]
                        Elements pElements = thirdTd.getElementsByTag("p");
                        regisMoney = pElements.get(0).child(1).text();
                        rowData.add(regisMoney);
                        // 拿股东链接  //*[@id="search-result"]/tr[1]/td[3]/a
                        Elements firstTrChilds = firstTr.children();
                        Element secondOfFirstTrChilds = firstTrChilds.get(2);
                        Elements secondOfFirstTrChildsChilds = secondOfFirstTrChilds.children();
                        ownerLink = secondOfFirstTrChildsChilds.get(0).attr("href");
                        // 开启模拟
                        ChromeOptions options = new ChromeOptions();
                        if (System.getProperty("os.name").startsWith("Mac OS")) {
                            // 苹果
                            System.setProperty("webdriver.chrome.driver", "/Users/hfy/Downloads/chromedriver");
                            System.setProperty("webdriver.chrome.bin", "/Applications/Google Chrome.app");
                            options.addArguments("no-sandbox");
                        } else if (System.getProperty("os.name").startsWith("Windows")) {
                            // windows
                            System.setProperty("webdriver.chrome.driver", "E:/chromedriver.exe");
                            System.setProperty("webdriver.chrome.bin", "C:/Program Files (x86)/Google/Chrome/Application/chrome.exe");
                        }
                        options.addArguments("headless");
                        options.addArguments("User-Agent:Mozilla/5.0 (Windows NT 10.0; Win64; x64; rv:74.0) Gecko/20100101 Firefox/74.0");
                        ChromeDriver chromeDriver = new ChromeDriver(options);
                        options.addArguments("cookie:" + cookieSet.toString());
                        chromeDriver.get(HOME_URL + ownerLink);
                        List<WebElement> ownerTableList = chromeDriver.findElementsByCssSelector("[class='ntable ntable-odd npth nptd']");
                        List<WebElement> ownerNameElementList = ownerTableList.get(0).findElements(By.cssSelector("[class='seo font-14']"));
                        WebElement ownerElement = null;
                        for (int i = 0; i < ownerNameElementList.size(); i++) {
                            rowData.add(ownerNameElementList.get(i).getAttribute("innerHTML"));
                        }
                    }
                }
            }
        }
        // 释放连接
        method.releaseConnection();
    }

    public static void main(String[] args) {
        WebCrawl webCrawl = new WebCrawl();
        try {
            webCrawl.doGet(null);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
