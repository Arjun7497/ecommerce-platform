import axios from 'axios'

const createClient = (baseURL) => {
  const client = axios.create({ baseURL })

  client.interceptors.request.use((config) => {
    const token = localStorage.getItem('token')
    if (token) config.headers.Authorization = `Bearer ${token}`
    return config
  })

  client.interceptors.response.use(
    (res) => res.data,
    (err) => Promise.reject(err.response?.data ?? err)
  )

  return client
}

export const userApi = createClient('/api/v1/users')
export const productApi = createClient('/api/v1/products')
export const orderApi = createClient('/api/v1/orders')

// ─── Auth ────────────────────────────────────────────────────────────────────
export const authService = {
  register: (data) => userApi.post('/register', data),
  login: (data) => userApi.post('/login', data),
  getProfile: (id) => userApi.get(`/${id}`),
}

// ─── Products ─────────────────────────────────────────────────────────────────
export const productService = {
  getAll: (params) => productApi.get('', { params }),
  getById: (id) => productApi.get(`/${id}`),
  getByCategory: (category, params) => productApi.get(`/category/${category}`, { params }),
  search: (keyword, params) => productApi.get('/search', { params: { keyword, ...params } }),
  create: (data) => productApi.post('', data),
  update: (id, data) => productApi.put(`/${id}`, data),
  delete: (id) => productApi.delete(`/${id}`),
}

// ─── Orders ──────────────────────────────────────────────────────────────────
export const orderService = {
  create: (data) => orderApi.post('', data),
  getById: (id) => orderApi.get(`/${id}`),
  getMyOrders: (userId, params) => orderApi.get(`/user/${userId}`, { params }),
  updateStatus: (id, status) => orderApi.patch(`/${id}/status`, { status }),
}
