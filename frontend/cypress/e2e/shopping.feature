Feature: Shopping Experience
  As a customer
  I want to browse products and place orders
  So that I can purchase items online

  Background:
    Given I am on the home page

  Scenario: Browse products on the home page
    Then I should see a list of products
    And each product should show name, price, and "Add to Cart" button

  Scenario: Add product to cart
    When I click "Add to Cart" on the first product
    Then the cart count should increase by 1
    And I should see a success notification

  Scenario: Add multiple products to cart
    When I add 3 different products to the cart
    Then the cart count should show 3

  Scenario: View cart
    Given I have added a product to the cart
    When I click the cart icon
    Then I should see the cart page
    And the added product should be listed

  Scenario: Search for products
    When I type "laptop" in the search bar
    And I press Enter
    Then I should see products matching "laptop"

  Scenario: Filter products by category
    When I click on the "Electronics" category
    Then I should only see products in the "Electronics" category

  Scenario: Complete checkout flow
    Given I am logged in as a customer
    And I have added a product to the cart
    When I proceed to checkout
    And I fill in shipping address "123 Test Street, City, 12345"
    And I confirm the order
    Then I should see an order confirmation
    And the order status should be "PENDING"

  Scenario: User registration
    Given I am on the registration page
    When I fill in the registration form with:
      | email     | newuser@test.com |
      | password  | SecurePass123    |
      | firstName | New              |
      | lastName  | User             |
    And I submit the registration form
    Then I should be redirected to the home page
    And I should be logged in
