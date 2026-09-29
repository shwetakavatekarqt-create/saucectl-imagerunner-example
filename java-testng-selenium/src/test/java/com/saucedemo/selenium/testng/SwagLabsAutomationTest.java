package com.saucedemo.selenium.testng;

import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.Select;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.testng.Assert;
import org.testng.ITestResult;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.lang.reflect.Method;
import java.time.Duration;
import java.util.List;

import com.saucelabs.saucebindings.options.SauceOptions;
import com.saucelabs.saucebindings.testng.SauceBaseTest;

/**
 * Automation Test Suite for Swag Labs E-commerce Application.
 * Tests include: Login, Product Listing, Add to Cart, Checkout, and Logout.
 */
public class SwagLabsAutomationTest extends SauceBaseTest {
    
    private static final String BASE_URL = "https://www.saucedemo.com";
    private static final String VALID_USERNAME = "standard_user";
    private static final String VALID_PASSWORD = "secret_sauce";
    private static final String INVALID_USERNAME = "invalid_user";
    private static final String INVALID_PASSWORD = "wrong_password";
    
    private WebDriverWait wait;
    
    @Override
    protected SauceOptions createSauceOptions(Method method) {
        SauceOptions sauceOptions = new SauceOptions();
        sauceOptions.setCapability("browserName", "chrome");
        sauceOptions.setCapability("browserVersion", "latest");
        sauceOptions.setCapability("platformName", "Windows 10");
        return sauceOptions;
    }
    
    @BeforeMethod
    public void setUp(Method method) {
        wait = new WebDriverWait(getDriver(), Duration.ofSeconds(10));
    }
    
    /**
     * Test Case 1: Login with valid credentials
     * Verifies successful login and navigation to products page
     */
    @Test(testName = "TC_001_Login_Valid_Credentials")
    public void testLoginWithValidCredentials() {
        getDriver().navigate().to(BASE_URL);
        
        // Verify login page loads
        Assert.assertEquals("Swag Labs", getDriver().getTitle());
        
        // Enter valid credentials
        WebElement usernameField = wait.until(
            ExpectedConditions.presenceOfElementLocated(By.id("user-name"))
        );
        usernameField.clear();
        usernameField.sendKeys(VALID_USERNAME);
        
        WebElement passwordField = getDriver().findElement(By.id("password"));
        passwordField.clear();
        passwordField.sendKeys(VALID_PASSWORD);
        
        // Click login button
        WebElement loginButton = getDriver().findElement(By.id("login-button"));
        loginButton.click();
        
        // Verify successful login by checking products page
        wait.until(ExpectedConditions.presenceOfElementLocated(By.className("inventory_container")));
        Assert.assertTrue(getDriver().findElement(By.className("inventory_container")).isDisplayed(),
            "Products container should be visible after successful login");
    }
    
    /**
     * Test Case 2: Login with invalid credentials
     * Verifies error message is displayed for invalid login attempt
     */
    @Test(testName = "TC_002_Login_Invalid_Credentials")
    public void testLoginWithInvalidCredentials() {
        getDriver().navigate().to(BASE_URL);
        
        // Enter invalid credentials
        WebElement usernameField = wait.until(
            ExpectedConditions.presenceOfElementLocated(By.id("user-name"))
        );
        usernameField.clear();
        usernameField.sendKeys(INVALID_USERNAME);
        
        WebElement passwordField = getDriver().findElement(By.id("password"));
        passwordField.clear();
        passwordField.sendKeys(INVALID_PASSWORD);
        
        // Click login button
        WebElement loginButton = getDriver().findElement(By.id("login-button"));
        loginButton.click();
        
        // Verify error message
        WebElement errorMessage = wait.until(
            ExpectedConditions.presenceOfElementLocated(By.cssSelector("[data-test='error']"))
        );
        Assert.assertTrue(errorMessage.isDisplayed(), "Error message should be displayed");
        Assert.assertTrue(errorMessage.getText().contains("Epic sadface"),
            "Error message should contain 'Epic sadface'");
    }
    
    /**
     * Test Case 3: Product Listing Validation
     * Verifies products are displayed correctly on the products page
     */
    @Test(testName = "TC_003_Product_Listing_Validation")
    public void testProductListingValidation() {
        loginWithValidCredentials();
        
        // Verify products container is visible
        WebElement productsContainer = wait.until(
            ExpectedConditions.presenceOfElementLocated(By.className("inventory_container"))
        );
        Assert.assertTrue(productsContainer.isDisplayed(), "Products container should be visible");
        
        // Verify product items are displayed
        List<WebElement> productItems = getDriver().findElements(By.className("inventory_item"));
        Assert.assertTrue(productItems.size() > 0, "Product items should be displayed");
        
        // Verify each product has required elements
        for (WebElement product : productItems) {
            WebElement productName = product.findElement(By.className("inventory_item_name"));
            WebElement productPrice = product.findElement(By.className("inventory_item_price"));
            WebElement addButton = product.findElement(By.xpath(".//button[contains(@class, 'add-to-cart')]"));
            
            Assert.assertTrue(productName.isDisplayed(), "Product name should be visible");
            Assert.assertTrue(productPrice.isDisplayed(), "Product price should be visible");
            Assert.assertTrue(addButton.isDisplayed(), "Add to cart button should be visible");
        }
    }
    
    /**
     * Test Case 4: Add Product(s) to Cart
     * Verifies that products can be added to the cart successfully
     */
    @Test(testName = "TC_004_Add_Products_To_Cart")
    public void testAddProductsToCart() {
        loginWithValidCredentials();
        
        // Get initial cart count
        WebElement cartBadge = getDriver().findElement(By.className("shopping_cart_badge"));
        int initialCartCount = Integer.parseInt(cartBadge.getText());
        Assert.assertEquals(0, initialCartCount, "Cart should be empty initially");
        
        // Add first product to cart
        List<WebElement> addButtons = getDriver().findElements(
            By.xpath("//button[contains(@class, 'add-to-cart')]")
        );
        addButtons.get(0).click();
        
        // Verify cart badge updated
        WebElement updatedBadge = wait.until(
            ExpectedConditions.textToBePresentInElement(cartBadge, "1")
        );
        Assert.assertEquals("1", updatedBadge.getText(), "Cart badge should show 1");
        
        // Add second product to cart
        addButtons.get(1).click();
        
        // Verify cart badge updated to 2
        WebElement finalBadge = wait.until(
            ExpectedConditions.textToBePresentInElement(cartBadge, "2")
        );
        Assert.assertEquals("2", finalBadge.getText(), "Cart badge should show 2");
    }
    
    /**
     * Test Case 5: Cart Content Validation
     * Verifies that cart displays correct products and prices
     */
    @Test(testName = "TC_005_Cart_Content_Validation")
    public void testCartContentValidation() {
        loginWithValidCredentials();
        
        // Add two products
        List<WebElement> productItems = getDriver().findElements(By.className("inventory_item"));
        String firstProductName = productItems.get(0).findElement(By.className("inventory_item_name")).getText();
        String firstProductPrice = productItems.get(0).findElement(By.className("inventory_item_price")).getText();
        
        String secondProductName = productItems.get(1).findElement(By.className("inventory_item_name")).getText();
        String secondProductPrice = productItems.get(1).findElement(By.className("inventory_item_price")).getText();
        
        // Click add to cart for first product
        productItems.get(0).findElement(By.xpath(".//button[contains(@class, 'add-to-cart')]")).click();
        
        // Click add to cart for second product
        productItems.get(1).findElement(By.xpath(".//button[contains(@class, 'add-to-cart')]")).click();
        
        // Click cart icon to view cart
        WebElement cartLink = getDriver().findElement(By.className("shopping_cart_link"));
        cartLink.click();
        
        // Verify cart page loads
        wait.until(ExpectedConditions.presenceOfElementLocated(By.className("cart_contents_container")));
        
        // Verify products in cart
        List<WebElement> cartItems = getDriver().findElements(By.className("cart_item"));
        Assert.assertEquals(2, cartItems.size(), "Cart should contain 2 items");
        
        // Verify first product details
        Assert.assertTrue(cartItems.get(0).getText().contains(firstProductName),
            "First product name should match");
        Assert.assertTrue(cartItems.get(0).getText().contains(firstProductPrice),
            "First product price should match");
        
        // Verify second product details
        Assert.assertTrue(cartItems.get(1).getText().contains(secondProductName),
            "Second product name should match");
        Assert.assertTrue(cartItems.get(1).getText().contains(secondProductPrice),
            "Second product price should match");
    }
    
    /**
     * Test Case 6: Checkout Flow (Happy Path)
     * Verifies the complete checkout process from cart to order confirmation
     */
    @Test(testName = "TC_006_Checkout_Happy_Path")
    public void testCheckoutFlow() {
        loginWithValidCredentials();
        
        // Add product to cart
        List<WebElement> addButtons = getDriver().findElements(
            By.xpath("//button[contains(@class, 'add-to-cart')]")
        );
        addButtons.get(0).click();
        
        // Click cart icon
        WebElement cartLink = getDriver().findElement(By.className("shopping_cart_link"));
        cartLink.click();
        
        // Verify cart page
        wait.until(ExpectedConditions.presenceOfElementLocated(By.className("cart_contents_container")));
        
        // Click checkout button
        WebElement checkoutButton = getDriver().findElement(By.id("checkout"));
        checkoutButton.click();
        
        // Fill checkout information form
        wait.until(ExpectedConditions.presenceOfElementLocated(By.id("first-name")));
        
        WebElement firstNameField = getDriver().findElement(By.id("first-name"));
        WebElement lastNameField = getDriver().findElement(By.id("last-name"));
        WebElement zipCodeField = getDriver().findElement(By.id("postal-code"));
        
        firstNameField.sendKeys("John");
        lastNameField.sendKeys("Doe");
        zipCodeField.sendKeys("12345");
        
        // Click continue button
        WebElement continueButton = getDriver().findElement(By.id("continue"));
        continueButton.click();
        
        // Verify checkout overview page
        wait.until(ExpectedConditions.presenceOfElementLocated(By.className("checkout_summary_container")));
        
        // Click finish button
        WebElement finishButton = getDriver().findElement(By.id("finish"));
        finishButton.click();
        
        // Verify order confirmation page
        wait.until(ExpectedConditions.presenceOfElementLocated(By.className("complete-container")));
        WebElement completeMessage = getDriver().findElement(By.className("complete-text"));
        Assert.assertTrue(completeMessage.isDisplayed(), "Order confirmation should be displayed");
        Assert.assertTrue(completeMessage.getText().contains("Thank you"),
            "Confirmation message should contain 'Thank you'");
    }
    
    /**
     * Test Case 7: Logout
     * Verifies that user can logout successfully
     */
    @Test(testName = "TC_007_Logout")
    public void testLogout() {
        loginWithValidCredentials();
        
        // Click menu button
        WebElement menuButton = getDriver().findElement(By.id("react-burger-menu-btn"));
        menuButton.click();
        
        // Wait for logout button to be visible
        WebElement logoutButton = wait.until(
            ExpectedConditions.elementToBeClickable(By.id("logout_sidebar_link"))
        );
        logoutButton.click();
        
        // Verify login page is displayed
        wait.until(ExpectedConditions.presenceOfElementLocated(By.id("login-button")));
        WebElement loginButton = getDriver().findElement(By.id("login-button"));
        Assert.assertTrue(loginButton.isDisplayed(), "Login button should be displayed after logout");
    }
    
    // Helper method to login with valid credentials
    private void loginWithValidCredentials() {
        getDriver().navigate().to(BASE_URL);
        
        WebElement usernameField = wait.until(
            ExpectedConditions.presenceOfElementLocated(By.id("user-name"))
        );
        usernameField.clear();
        usernameField.sendKeys(VALID_USERNAME);
        
        WebElement passwordField = getDriver().findElement(By.id("password"));
        passwordField.clear();
        passwordField.sendKeys(VALID_PASSWORD);
        
        WebElement loginButton = getDriver().findElement(By.id("login-button"));
        loginButton.click();
        
        wait.until(ExpectedConditions.presenceOfElementLocated(By.className("inventory_container")));
    }
}
