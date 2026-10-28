Feature: User Registration and Authentication
  As a new visitor
  I want to register an account
  So that I can access the e-commerce platform

  Background:
    Given the user service is running

  Scenario: Successful user registration
    Given no user exists with email "bdd@example.com"
    When I register with the following details:
      | email          | bdd@example.com |
      | password       | SecurePass123   |
      | firstName      | Alice           |
      | lastName       | Smith           |
    Then the response status should be 201
    And the response should contain email "bdd@example.com"
    And the response should contain firstName "Alice"
    And the user should be saved in the database

  Scenario: Registration fails with duplicate email
    Given a user already exists with email "existing@example.com"
    When I register with the following details:
      | email          | existing@example.com |
      | password       | AnotherPass123       |
      | firstName      | Bob                  |
      | lastName       | Jones                |
    Then the response status should be 409
    And the response should contain error about duplicate email

  Scenario: Registration fails with invalid email
    When I register with the following details:
      | email          | not-an-email    |
      | password       | SecurePass123   |
      | firstName      | Charlie         |
      | lastName       | Brown           |
    Then the response status should be 400
    And the response should contain validation error for "email"

  Scenario: Registration fails with short password
    When I register with the following details:
      | email          | short@example.com |
      | password       | short             |
      | firstName      | Dave              |
      | lastName       | Wilson            |
    Then the response status should be 400
    And the response should contain validation error for "password"

  Scenario: Successful login after registration
    Given a user is registered with email "login@example.com" and password "MyPassword123"
    When I login with email "login@example.com" and password "MyPassword123"
    Then the response status should be 200
    And the response should contain a JWT token
    And the token type should be "Bearer"

  Scenario: Login fails with wrong password
    Given a user is registered with email "secure@example.com" and password "CorrectPass123"
    When I login with email "secure@example.com" and password "WrongPass123"
    Then the response status should be 404
