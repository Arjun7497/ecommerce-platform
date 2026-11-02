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
      <div data-testid="loading-spinner" className="catalog-state">
        <div className="loading-indicator" />
      </div>
    )
  }

  if (error) {
    return (
      <div data-testid="error-message" className="catalog-state catalog-state--error">
        {error}
      </div>
    )
  }

  if (products.length === 0) {
    return (
      <div data-testid="empty-state" className="catalog-state">
        No products found.
      </div>
    )
  }

  return (
    <div>
      <div data-testid="product-grid" className="product-grid">
        {products.map((product) => (
          <ProductCard key={product.id} product={product} />
        ))}
      </div>
      {totalPages > 1 && (
        <div className="pagination">
          <button
            onClick={() => setPage((p) => Math.max(0, p - 1))}
            disabled={page === 0}
          >
            Previous
          </button>
          <span>Page {page + 1} of {totalPages}</span>
          <button
            onClick={() => setPage((p) => Math.min(totalPages - 1, p + 1))}
            disabled={page === totalPages - 1}
          >
            Next
          </button>
        </div>
      )}
    </div>
  )
}
