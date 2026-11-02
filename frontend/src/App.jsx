import { useState } from 'react'
import { useSelector } from 'react-redux'
import ProductList from './components/product/ProductList'
import { selectCartCount } from './store/slices/cartSlice'

export default function App() {
  const [searchKeyword, setSearchKeyword] = useState('')
  const cartCount = useSelector(selectCartCount)

  return (
    <div className="app-shell">
      <header className="site-header">
        <a className="site-brand" href="/" aria-label="E-Commerce home">
          <span className="brand-mark" aria-hidden="true">E</span>
          <span>E-Commerce</span>
        </a>
        <div className="cart-summary" aria-label={`${cartCount} items in cart`}>
          <span aria-hidden="true">Cart</span>
          <span className="cart-count">{cartCount}</span>
        </div>
      </header>

      <main className="catalog">
        <section className="catalog-heading">
          <div>
            <p className="eyebrow">Discover something great</p>
            <h1>Shop the collection</h1>
            <p className="catalog-description">Browse our latest products and find your next favorite.</p>
          </div>
          <label className="search-field">
            <span className="visually-hidden">Search products</span>
            <input
              type="search"
              placeholder="Search products"
              value={searchKeyword}
              onChange={(event) => setSearchKeyword(event.target.value)}
            />
          </label>
        </section>
        <ProductList searchKeyword={searchKeyword.trim()} />
      </main>
    </div>
  )
}
