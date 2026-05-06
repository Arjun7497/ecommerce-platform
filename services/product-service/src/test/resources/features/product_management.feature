Feature: Product Management
  As a store admin
  I want to manage products in the catalog
  So that customers can browse and purchase items

  Background:
    Given the product service is running

  Scenario: Successfully create a new product
    When I create a product with the following details:
      | name          | Gaming Laptop          |
      | description   | High-performance gaming laptop |
      | price         | 1299.99                |
      | stockQuantity | 25                     |
      | category      | Electronics            |
    Then the response status should be 201
    And the response should contain product name "Gaming Laptop"
    And the response should contain price "1299.99"
    And the product should exist in the catalog

  Scenario: Create product fails with negative price
    When I create a product with the following details:
      | name          | Broken Item |
      | price         | -10.00      |
      | stockQuantity | 5           |
      | category      | Misc        |
    Then the response status should be 400

  Scenario: Retrieve product by ID
    Given a product exists with name "Wireless Mouse"
    When I retrieve the product by its ID
    Then the response status should be 200
    And the response should contain product name "Wireless Mouse"

  Scenario: Search products by keyword
    Given the following products exist in the catalog:
      | name          | category    | price  |
      | MacBook Pro   | Electronics | 1999.99 |
      | MacBook Air   | Electronics | 1299.99 |
      | Dell XPS      | Electronics | 1499.99 |
    When I search for products with keyword "MacBook"
    Then the response status should be 200
    And the results should contain 2 products

  Scenario: Soft delete a product
    Given a product exists with name "Discontinued Item"
    When I delete the product
    Then the response status should be 204
    And the product should not appear in the active catalog

  Scenario: Update product details
    Given a product exists with name "Old Name"
    When I update the product with name "Updated Name" and price "299.99"
    Then the response status should be 200
    And the response should contain product name "Updated Name"
