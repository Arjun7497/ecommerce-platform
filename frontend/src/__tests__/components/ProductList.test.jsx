import { describe, it, expect } from 'vitest'
import { render, screen, waitFor } from '@testing-library/react'
import { Provider } from 'react-redux'
import { configureStore } from '@reduxjs/toolkit'
import { http, HttpResponse } from 'msw'
import { server } from '../../mocks/server'
import cartReducer from '../../store/slices/cartSlice'
import ProductList from '../../components/product/ProductList'

const makeStore = () => configureStore({ reducer: { cart: cartReducer } })

const renderProductList = (props = {}) =>
  render(
    <Provider store={makeStore()}>
      <ProductList {...props} />
    </Provider>
  )

const mockProductPage = {
  content: [
    { id: '1', name: 'Laptop', price: 999.99, stockQuantity: 5, category: 'Electronics', description: '' },
    { id: '2', name: 'Mouse', price: 29.99, stockQuantity: 20, category: 'Electronics', description: '' },
  ],
  totalPages: 1,
  totalElements: 2,
}

describe('ProductList', () => {
  it('shows loading spinner initially', () => {
    renderProductList()
    expect(screen.getByTestId('loading-spinner')).toBeInTheDocument()
  })

  it('renders products after successful fetch', async () => {
    server.use(
      http.get('/api/v1/products', () => HttpResponse.json(mockProductPage))
    )
    renderProductList()
    await waitFor(() => {
      expect(screen.getByTestId('product-grid')).toBeInTheDocument()
    })
    expect(screen.getAllByTestId('product-card')).toHaveLength(2)
    expect(screen.getByText('Laptop')).toBeInTheDocument()
    expect(screen.getByText('Mouse')).toBeInTheDocument()
  })

  it('shows empty state when no products returned', async () => {
    server.use(
      http.get('/api/v1/products', () =>
        HttpResponse.json({ content: [], totalPages: 0, totalElements: 0 })
      )
    )
    renderProductList()
    await waitFor(() => {
      expect(screen.getByTestId('empty-state')).toBeInTheDocument()
    })
  })

  it('shows error message when fetch fails', async () => {
    server.use(
      http.get('/api/v1/products', () => HttpResponse.error())
    )
    renderProductList()
    await waitFor(() => {
      expect(screen.getByTestId('error-message')).toBeInTheDocument()
    })
  })

  it('uses category endpoint when category prop provided', async () => {
    let capturedUrl = ''
    server.use(
      http.get('/api/v1/products/category/Electronics', ({ request }) => {
        capturedUrl = request.url
        return HttpResponse.json(mockProductPage)
      })
    )
    renderProductList({ category: 'Electronics' })
    await waitFor(() => expect(screen.getByTestId('product-grid')).toBeInTheDocument())
    expect(capturedUrl).toContain('/category/Electronics')
  })
})
