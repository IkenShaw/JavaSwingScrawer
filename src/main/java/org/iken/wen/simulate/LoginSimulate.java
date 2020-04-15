package org.iken.wen.simulate;

import org.openqa.selenium.Cookie;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.firefox.FirefoxBinary;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxOptions;

import java.util.Set;

public class LoginSimulate {

    public static Set<Cookie> login(String userName, String password) {
        Set<Cookie> cookieSet = null;
        // selenium + firefoxDriver 模拟登录 无界面运行
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
        try {
            chromeDriver.get("https://www.qichacha.com/user_login");
            WebElement normalLoginButton = chromeDriver.findElementByXPath("//a[contains(.,'密码登录')]");
            normalLoginButton.click();
            chromeDriver.findElementByXPath("//input[contains(@name,'nameNormal')]").sendKeys("18620673827");
            chromeDriver.findElementByXPath("//input[contains(@name,'pwdNormal')]").sendKeys("kevin920408");
            String setscroll = "document.documentElement.scrollTop=" + 500;
            ((JavascriptExecutor)chromeDriver).executeScript(setscroll);
            WebElement buttonElement = chromeDriver.findElementByXPath("(//button[@type='submit'][contains(.,'立即登录')])[1]");
            buttonElement.click();
            Thread.sleep(5000); // 睡5秒，等登录结束
            // 拿到了cookies
            cookieSet = chromeDriver.manage().getCookies();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            chromeDriver.quit();
        }
        // 返回cookie
        return cookieSet;
    }
}
