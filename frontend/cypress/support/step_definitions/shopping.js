import { Given, When, Then } from '@badeball/cypress-cucumber-preprocessor'

// ─── Background ──────────────────────────────────────────────────────────────

Given('I am on the home page', () => {
  cy.visit('/')
})

// ─── Browsing ─────────────────────────────────────────────────────────────────

Then('I should see a list of products', () => {
  cy.get('[data-testid="product-grid"]').should('be.visible')
  cy.get('[data-testid="product-card"]').should('have.length.greaterThan', 0)
})

Then('each product should show name, price, and {string} button', (btnText) => {
  cy.get('[data-testid="product-card"]').first().within(() => {
    cy.get('[data-testid="product-name"]').should('be.visible')
    cy.get('[data-testid="product-price"]').should('be.visible')
    cy.contains(btnText).should('be.visible')
  })
})

// ─── Cart ─────────────────────────────────────────────────────────────────────

When('I click {string} on the first product', (btnText) => {
  cy.get('[data-testid="product-card"]').first().contains(btnText).click()
})

Then('the cart count should increase by {int}', (count) => {
  cy.get('[data-testid="cart-count"]').should('contain', count.toString())
})

Then('I should see a success notification', () => {
  cy.get('[data-testid="toast-notification"]').should('be.visible')
})

When('I add {int} different products to the cart', (count) => {
  cy.get('[data-testid="add-to-cart-btn"]').each(($btn, index) => {
    if (index < count) cy.wrap($btn).click()
  })
})

Then('the cart count should show {int}', (count) => {
  cy.get('[data-testid="cart-count"]').should('contain', count.toString())
})

Given('I have added a product to the cart', () => {
  cy.get('[data-testid="add-to-cart-btn"]').first().click()
})

When('I click the cart icon', () => {
  cy.get('[data-testid="cart-icon"]').click()
})

Then('I should see the cart page', () => {
  cy.url().should('include', '/cart')
})

Then('the added product should be listed', () => {
  cy.get('[data-testid="cart-items"]').should('have.length.greaterThan', 0)
})

// ─── Search & Filter ──────────────────────────────────────────────────────────

When('I type {string} in the search bar', (keyword) => {
  cy.get('[data-testid="search-input"]').clear().type(keyword)
})

When('I press Enter', () => {
  cy.get('[data-testid="search-input"]').type('{enter}')
})

Then('I should see products matching {string}', (keyword) => {
  cy.get('[data-testid="product-name"]').each(($el) => {
    cy.wrap($el).invoke('text').then((text) => {
      expect(text.toLowerCase()).to.include(keyword.toLowerCase())
    })
  })
})

When('I click on the {string} category', (category) => {
  cy.get('[data-testid="category-filter"]').contains(category).click()
})

Then('I should only see products in the {string} category', (category) => {
  cy.get('[data-testid="product-category"]').each(($el) => {
    expect($el.text()).to.equal(category)
  })
})

// ─── Checkout ─────────────────────────────────────────────────────────────────

Given('I am logged in as a customer', () => {
  cy.request('POST', '/api/v1/users/login', {
    email: 'customer@test.com',
    password: 'Password123',
  }).then(({ body }) => {
    localStorage.setItem('token', body.token)
  })
})

When('I proceed to checkout', () => {
  cy.get('[data-testid="checkout-btn"]').click()
})

When('I fill in shipping address {string}', (address) => {
  cy.get('[data-testid="shipping-address"]').type(address)
})

When('I confirm the order', () => {
  cy.get('[data-testid="confirm-order-btn"]').click()
})

Then('I should see an order confirmation', () => {
  cy.get('[data-testid="order-confirmation"]').should('be.visible')
})

Then('the order status should be {string}', (status) => {
  cy.get('[data-testid="order-status"]').should('contain', status)
})

// ─── Registration ─────────────────────────────────────────────────────────────

Given('I am on the registration page', () => {
  cy.visit('/register')
})

When('I fill in the registration form with:', (dataTable) => {
  const data = dataTable.rowsHash()
  cy.get('[data-testid="register-email"]').type(data.email)
  cy.get('[data-testid="register-password"]').type(data.password)
  cy.get('[data-testid="register-firstName"]').type(data.firstName)
  cy.get('[data-testid="register-lastName"]').type(data.lastName)
})

When('I submit the registration form', () => {
  cy.get('[data-testid="register-submit"]').click()
})

Then('I should be redirected to the home page', () => {
  cy.url().should('eq', Cypress.config('baseUrl') + '/')
})

Then('I should be logged in', () => {
  cy.get('[data-testid="user-menu"]').should('be.visible')
})
