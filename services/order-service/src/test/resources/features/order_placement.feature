Feature: Order Placement
  As a registered customer
  I want to place orders for products
  So that I can purchase items from the e-commerce platform

  Background:
    Given the order service is running

  Scenario: Successfully place an order
    Given a product "Wireless Headphones" is available with stock 20 and price 89.99
    When a customer places an order for 2 units of "Wireless Headphones" to "456 Oak Avenue"
    Then the response status should be 201
    And the order status should be "PENDING"
    And the order total should be "179.98"

  Scenario: Order fails when stock is insufficient
    Given a product "Limited Edition Watch" is available with stock 1 and price 299.99
    When a customer places an order for 5 units of "Limited Edition Watch" to "789 Pine Road"
    Then the response status should be 409
    And the error message should mention "Insufficient stock"

  Scenario: Order fails when items list is empty
    When a customer places an order with no items to "123 Elm Street"
    Then the response status should be 400

  Scenario: Retrieve order by ID
    Given an order has been placed by a customer
    When the customer retrieves the order by ID
    Then the response status should be 200
    And the order details should be returned

  Scenario: Update order status
    Given an order has been placed with status "PENDING"
    When an admin updates the order status to "CONFIRMED"
    Then the response status should be 200
    And the order status should be "CONFIRMED"
