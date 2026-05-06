import { describe, it, expect, vi } from 'vitest'
import { render, screen, fireEvent } from '@testing-library/react'
import { Provider } from 'react-redux'
import { configureStore } from '@reduxjs/toolkit'
import cartReducer from '../../store/slices/cartSlice'
import ProductCard from '../../components/product/ProductCard'

// ─── TDD: Tests written before/alongside the component ────────────────────────

const makeStore = () => configureStore({ reducer: { cart: cartReducer } })

const mockProduct = {
  id: '123e4567-e89b-12d3-a456-426614174000',
  name: 'Test Laptop',
  description: 'A powerful laptop',
  price: 999.99,
  stockQuantity: 10,
  category: 'Electronics',
  imageUrl: null,
}

const renderProductCard = (product = mockProduct) => {
  const store = makeStore()
  const result = render(
    <Provider store={store}>
      <ProductCard product={product} />
    </Provider>
  )
  return { ...result, store }
}

describe('ProductCard', () => {
  it('renders product name', () => {
    renderProductCard()
    expect(screen.getByTestId('product-name')).toHaveTextContent('Test Laptop')
  })

  it('renders formatted price', () => {
    renderProductCard()
    expect(screen.getByTestId('product-price')).toHaveTextContent('$999.99')
  })

  it('renders category label', () => {
    renderProductCard()
    expect(screen.getByText('Electronics')).toBeInTheDocument()
  })

  it('renders stock quantity', () => {
    renderProductCard()
    expect(screen.getByText('10 in stock')).toBeInTheDocument()
  })

  it('renders Add to Cart button enabled when stock > 0', () => {
    renderProductCard()
    const btn = screen.getByTestId('add-to-cart-btn')
    expect(btn).not.toBeDisabled()
  })

  it('renders Add to Cart button disabled when out of stock', () => {
    renderProductCard({ ...mockProduct, stockQuantity: 0 })
    expect(screen.getByTestId('add-to-cart-btn')).toBeDisabled()
  })

  it('shows Out of stock when stockQuantity is 0', () => {
    renderProductCard({ ...mockProduct, stockQuantity: 0 })
    expect(screen.getByText('Out of stock')).toBeInTheDocument()
  })

  it('dispatches addToCart when Add to Cart is clicked', () => {
    const { store } = renderProductCard()
    fireEvent.click(screen.getByTestId('add-to-cart-btn'))
    const { items } = store.getState().cart
    expect(items).toHaveLength(1)
    expect(items[0].id).toBe(mockProduct.id)
    expect(items[0].quantity).toBe(1)
  })

  it('increments quantity when same product added twice', () => {
    const { store } = renderProductCard()
    fireEvent.click(screen.getByTestId('add-to-cart-btn'))
    fireEvent.click(screen.getByTestId('add-to-cart-btn'))
    const { items } = store.getState().cart
    expect(items).toHaveLength(1)
    expect(items[0].quantity).toBe(2)
  })
})
