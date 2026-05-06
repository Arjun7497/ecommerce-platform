import { describe, it, expect } from 'vitest'
import { renderHook, act } from '@testing-library/react'
import { Provider } from 'react-redux'
import { configureStore } from '@reduxjs/toolkit'
import cartReducer, {
  selectCartItems,
  selectCartTotal,
  selectCartCount,
  addToCart,
  removeFromCart,
  updateQuantity,
  clearCart,
} from '../../store/slices/cartSlice'

// TDD: Testing Redux slice logic via hook-like pattern

const makeStore = () => configureStore({ reducer: { cart: cartReducer } })

const product1 = { id: 'p1', name: 'Laptop', price: 999.99 }
const product2 = { id: 'p2', name: 'Mouse', price: 29.99 }

describe('Cart Redux Slice', () => {
  describe('addToCart', () => {
    it('adds new item with quantity 1', () => {
      const store = makeStore()
      store.dispatch(addToCart(product1))
      expect(selectCartItems(store.getState())).toHaveLength(1)
      expect(selectCartItems(store.getState())[0].quantity).toBe(1)
    })

    it('increments quantity for duplicate item', () => {
      const store = makeStore()
      store.dispatch(addToCart(product1))
      store.dispatch(addToCart(product1))
      const items = selectCartItems(store.getState())
      expect(items).toHaveLength(1)
      expect(items[0].quantity).toBe(2)
    })

    it('adds multiple distinct items', () => {
      const store = makeStore()
      store.dispatch(addToCart(product1))
      store.dispatch(addToCart(product2))
      expect(selectCartItems(store.getState())).toHaveLength(2)
    })
  })

  describe('removeFromCart', () => {
    it('removes item from cart', () => {
      const store = makeStore()
      store.dispatch(addToCart(product1))
      store.dispatch(addToCart(product2))
      store.dispatch(removeFromCart('p1'))
      const items = selectCartItems(store.getState())
      expect(items).toHaveLength(1)
      expect(items[0].id).toBe('p2')
    })
  })

  describe('updateQuantity', () => {
    it('updates item quantity', () => {
      const store = makeStore()
      store.dispatch(addToCart(product1))
      store.dispatch(updateQuantity({ id: 'p1', quantity: 5 }))
      expect(selectCartItems(store.getState())[0].quantity).toBe(5)
    })

    it('enforces minimum quantity of 1', () => {
      const store = makeStore()
      store.dispatch(addToCart(product1))
      store.dispatch(updateQuantity({ id: 'p1', quantity: 0 }))
      expect(selectCartItems(store.getState())[0].quantity).toBe(1)
    })
  })

  describe('clearCart', () => {
    it('removes all items', () => {
      const store = makeStore()
      store.dispatch(addToCart(product1))
      store.dispatch(addToCart(product2))
      store.dispatch(clearCart())
      expect(selectCartItems(store.getState())).toHaveLength(0)
    })
  })

  describe('selectCartTotal', () => {
    it('calculates total correctly', () => {
      const store = makeStore()
      store.dispatch(addToCart(product1)) // 999.99
      store.dispatch(addToCart(product1)) // 999.99 x2
      store.dispatch(addToCart(product2)) // 29.99
      const total = selectCartTotal(store.getState())
      expect(total).toBeCloseTo(2029.97, 2)
    })
  })

  describe('selectCartCount', () => {
    it('returns total item count', () => {
      const store = makeStore()
      store.dispatch(addToCart(product1))
      store.dispatch(addToCart(product1))
      store.dispatch(addToCart(product2))
      expect(selectCartCount(store.getState())).toBe(3)
    })
  })
})
