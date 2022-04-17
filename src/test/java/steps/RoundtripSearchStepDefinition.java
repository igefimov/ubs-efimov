package steps;

import exceptions.UnsupportedOsException;

import io.cucumber.java.After;
import io.cucumber.java.Before;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;

import org.openqa.selenium.By;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxOptions;
import org.openqa.selenium.io.FileHandler;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import org.junit.Assert;

import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;
import java.text.ParseException;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.util.Locale;
import java.util.logging.Logger;
import java.util.regex.Pattern;

import java.text.SimpleDateFormat;
import java.util.Date;

import static org.openqa.selenium.support.ui.ExpectedConditions.visibilityOfElementLocated;

/**
 * RoundtripSearchStepDefinition class defines Step Definition for the appropriate feature file
 */
public class RoundtripSearchStepDefinition {
    private WebDriver driver;
    private WebDriverWait wait;
    private static String url;
    private static final String WEB_DRIVER_PATH = "src/test/resources/geckodriver_mac/geckodriver";
    private static final String COOKIES_CLASS = "iInN-decline";
    private static final String PROGRESS_BAR_CLASS = "Common-Results-ProgressBar";
    private static final String SEARCH_RESULT_CLASS = "Flights-Results-FlightResultItem";
    private static final String PRICE_CLASS = "price-text";
    private static final String NOT_MATCHING_FLIGHTS_FOUND_CLASS = "col-illustration";
    private static final String NO_FLIGHTS_FOUND_CLASS = "Flights-Results-NoFlightResults";
    private static final Logger LOG = Logger.getLogger(RoundtripSearchStepDefinition.class.getName());


    /**
     * Initialize webdriver for Firefox browser in the headless mode
     *
     * @throws UnsupportedOsException Currently supported macOS only
     */
    @Before
    public void setUp() throws UnsupportedOsException {
        String OS = System.getProperty("os.name").toUpperCase(Locale.ROOT);
        LOG.info(OS);
        if (OS.startsWith("MAC")) {
            System.setProperty("webdriver.gecko.driver",
                    Paths.get(WEB_DRIVER_PATH).toString());
        } else {
            throw new UnsupportedOsException("Unsupported OS.\n" +
                    "Currently we support only macOS");
        }
        LOG.info("Set Firefox Headless mode as TRUE");
        FirefoxOptions options = new FirefoxOptions();
        options.setHeadless(true);
        System.setProperty(FirefoxDriver.SystemProperty.BROWSER_LOGFILE, "/dev/null");

        LOG.info("Starting browser");
        driver = new FirefoxDriver(options);
        driver.manage().window().maximize();
        wait = new WebDriverWait(driver, Duration.ofSeconds(60));
    }

    /**
     * Take screnshot and quit the browser
     *
     * @throws IOException for the non-existing path to screenshots
     */
    @After
    public void tearDown() throws IOException {
        String screenshot = "build/cucumber-report/" + Instant.now().toString() + ".png";
        File source = ((FirefoxDriver) driver).getFullPageScreenshotAs(OutputType.FILE);
        FileHandler.copy(source, new File(screenshot));
        driver.quit();
    }

    /**
     * Validate input parameters provided by customer in the Example section
     *
     * @param originAirport      represents code of origin airport
     * @param destinationAirport represents code of destination airport
     * @param departureDate      represents departure date in format YYYY-MM-DD
     * @param returnDate         represents return date in format YYYY-MM-DD
     * @param maxPrice           represents maximum price that customer is willing to pay
     * @throws ParseException for the incorrect date format
     */
    @Given("Valid parameters are provided {string} {string} {string} {string} {int}")
    public void validParametersAreProvided(String originAirport,
                                           String destinationAirport,
                                           String departureDate,
                                           String returnDate,
                                           Integer maxPrice) throws ParseException {

        Assert.assertTrue("Use valid airport code", Pattern.matches("[A-Z]{3}", originAirport));
        Assert.assertTrue("Use valid airport code", Pattern.matches("[A-Z]{3}", destinationAirport));
        Assert.assertNotEquals("Origin and destination airports can't be the same", originAirport, destinationAirport);

        Assert.assertTrue("Valid Date format: YYYY-MM-DD", Pattern.matches("[0-9]{4}-[0-9]{2}-[0-9]{2}", departureDate));
        Assert.assertTrue("Valid Date format: YYYY-MM-DD", Pattern.matches("[0-9]{4}-[0-9]{2}-[0-9]{2}", returnDate));

        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        Date dDate = sdf.parse(departureDate);
        Date rDate = sdf.parse(returnDate);
        Assert.assertTrue("Can't return before departure", rDate.after(dDate) || rDate.equals(dDate));

        Date today = sdf.parse(String.valueOf(LocalDate.now()));
        Assert.assertTrue("You can't travel in the past", dDate.after(today) || dDate.equals(today));

        Date limit = sdf.parse(String.valueOf(LocalDate.now().plusYears(1)));
        Assert.assertTrue("your trip must start and finish within 1 year of today", rDate.before(limit));

        Assert.assertTrue("Only digits are allowed", Pattern.matches("[0-9]+", maxPrice.toString()));

        url = "https://www.kayak.ch/flights/" + originAirport + "-" + destinationAirport +
                "/" + departureDate + "/" + returnDate +
                "?sort=price_b&fs=price=-" + maxPrice;

//      Example URL for the last test to pass. No matching flights should be displayed
//      url = "https://www.kayak.ch/flights/PRG-KIV/2022-05-17/2022-05-24?sort=bestflight_a&fs=takeoff=0200,0545__0500,0700";
    }

    /**
     * Open web site, decline cooking and wait for all the results to be loaded
     */
    @When("User navigates to the web site")
    public void userNavigatesToTheWebSite() {
        LOG.info("Navigate to " + url);
        driver.navigate().to(url);
        wait.until(visibilityOfElementLocated(By.className(COOKIES_CLASS)));
        LOG.info("Decline cookies");
        driver.findElement(By.className(COOKIES_CLASS)).click();
        LOG.info("Waiting for results");
        wait.until(ExpectedConditions.invisibilityOfElementLocated(By.className(PROGRESS_BAR_CLASS)));
    }

    /**
     * Verify that price of flights displays by search engine is always lower that on entered by customer
     * There are 2 corner cases that should display no results:
     * - there is no connection below the requested price - POSSIBLE BUG IN KAYAK WEB SITE
     * - there is no connection between 2 cities
     *
     * @param maxPrice is set by customer
     */
    @Then("Roundtrip flights below price {int} are displayed")
    public void roundtripFlightsBelowPriceMaxPriceAreDisplayed(Integer maxPrice) {
        if (driver.findElements(By.className(SEARCH_RESULT_CLASS)).size() != 0) {
            String actualPrice = driver.findElement(By.className(SEARCH_RESULT_CLASS)).findElement(By.className(PRICE_CLASS)).getText();
            int numberOnly = Integer.parseInt(actualPrice.replaceAll("[^0-9]", ""));
            LOG.info("User requested price to be less than " + maxPrice);
            LOG.info("Search engine maximum price is " + numberOnly);
            Assert.assertTrue(String.format("Found price should be below expected: %d < %d", numberOnly, maxPrice),
                    numberOnly < maxPrice);
        } else {
            wait.until(ExpectedConditions.or(
                    visibilityOfElementLocated(By.className(NOT_MATCHING_FLIGHTS_FOUND_CLASS)),
                    visibilityOfElementLocated(By.className(NO_FLIGHTS_FOUND_CLASS))
            ));
            LOG.info("No matching flights found");
        }
    }
}
