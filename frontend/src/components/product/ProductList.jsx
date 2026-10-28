import { useState, useEffect } from 'react'
import { productService } from '../../services/api'
import ProductCard from './ProductCard'

export default function ProductList({ category, searchKeyword }) {
  const [products, setProducts] = useState([])
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState(null)
  const [page, setPage] = useState(0)
  const [totalPages, setTotalPages] = useState(0)

  useEffect(() => {
    setPage(0)
  }, [category, searchKeyword])

  useEffect(() => {
    const fetchProducts = async () => {
      setLoading(true)
      setError(null)
      try {
        let data
        if (searchKeyword) {
          data = await productService.search(searchKeyword, { page, size: 12 })
        } else if (category) {
          data = await productService.getByCategory(category, { page, size: 12 })
        } else {
          data = await productService.getAll({ page, size: 12 })
        }
        setProducts(data.content)
        setTotalPages(data.totalPages)
      } catch {
        setError('Failed to load products. Please try again.')
      } finally {
        setLoading(false)
      }
    }
    fetchProducts()
  }, [category, searchKeyword, page])

  if (loading) {
    return (
      <div data-testid="loading-spinner" className="flex justify-center items-center h-64">
        <div className="animate-spin rounded-full h-12 w-12 border-b-2 border-blue-600" />
      </div>
    )
  }

  if (error) {
    return (
      <div data-testid="error-message" className="text-center text-red-500 p-8">
        {error}
      </div>
    )
  }

  if (products.length === 0) {
    return (
      <div data-testid="empty-state" className="text-center text-gray-500 p-8">
        No products found.
      </div>
    )
  }

  return (
    <div>
      <div
        data-testid="product-grid"
        className="grid grid-cols-1 sm:grid-cols-2 md:grid-cols-3 lg:grid-cols-4 gap-6"
      >
        {products.map((product) => (
          <ProductCard key={product.id} product={product} />
        ))}
      </div>
      {totalPages > 1 && (
        <div className="flex justify-center gap-2 mt-8">
          <button
            onClick={() => setPage((p) => Math.max(0, p - 1))}
            disabled={page === 0}
            className="px-4 py-2 border rounded disabled:opacity-50"
          >
            Previous
          </button>
          <span className="px-4 py-2">Page {page + 1} of {totalPages}</span>
          <button
            onClick={() => setPage((p) => Math.min(totalPages - 1, p + 1))}
            disabled={page === totalPages - 1}
            className="px-4 py-2 border rounded disabled:opacity-50"
          >
            Next
          </button>
        </div>
      )}
    </div>
  )
}
