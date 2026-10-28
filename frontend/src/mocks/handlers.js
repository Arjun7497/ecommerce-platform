import { http, HttpResponse } from 'msw'

const mockProducts = [
  { id: '1', name: 'Laptop', price: 999.99, stockQuantity: 5, category: 'Electronics', description: 'A laptop', active: true },
  { id: '2', name: 'Mouse', price: 29.99, stockQuantity: 20, category: 'Electronics', description: 'A mouse', active: true },
]

const mockUser = { id: 'u1', email: 'test@example.com', firstName: 'Test', lastName: 'User', role: 'CUSTOMER' }

export const handlers = [
  // Products
  http.get('/api/v1/products', () =>
    HttpResponse.json({ content: mockProducts, totalPages: 1, totalElements: 2 })
  ),
  http.get('/api/v1/products/:id', ({ params }) =>
    HttpResponse.json(mockProducts.find((p) => p.id === params.id) ?? null)
  ),
  http.get('/api/v1/products/category/:category', () =>
    HttpResponse.json({ content: mockProducts, totalPages: 1, totalElements: 2 })
  ),
  http.get('/api/v1/products/search', () =>
    HttpResponse.json({ content: mockProducts, totalPages: 1, totalElements: 2 })
  ),

  // Auth
  http.post('/api/v1/users/register', () =>
    HttpResponse.json(mockUser, { status: 201 })
  ),
  http.post('/api/v1/users/login', () =>
    HttpResponse.json({ token: 'mock-jwt-token', tokenType: 'Bearer', user: mockUser })
  ),

  // Orders
  http.post('/api/v1/orders', () =>
    HttpResponse.json({
      id: 'o1',
      userId: 'u1',
      items: [],
      status: 'PENDING',
      totalAmount: 999.99,
      shippingAddress: '123 Main St',
    }, { status: 201 })
  ),
]
