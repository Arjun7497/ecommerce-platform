import { useDispatch } from 'react-redux'
import { addToCart } from '../../store/slices/cartSlice'
import toast from 'react-hot-toast'

export default function ProductCard({ product }) {
  const dispatch = useDispatch()

  const handleAddToCart = () => {
    dispatch(addToCart(product))
    toast.success(`${product.name} added to cart`)
  }

  return (
    <article data-testid="product-card" className="product-card">
      {product.imageUrl && (
        <img
          src={product.imageUrl}
          alt={product.name}
          className="product-card__image"
        />
      )}
      <div>
        <span className="product-card__category">
          {product.category}
        </span>
        <h2 data-testid="product-name">
          {product.name}
        </h2>
        <p className="product-card__description">{product.description}</p>
      </div>
      <div className="product-card__details">
        <span className="product-card__price" data-testid="product-price">
          ${product.price.toFixed(2)}
        </span>
        <span className="product-card__stock">
          {product.stockQuantity > 0 ? `${product.stockQuantity} in stock` : 'Out of stock'}
        </span>
      </div>
      <button
        onClick={handleAddToCart}
        disabled={product.stockQuantity === 0}
        data-testid="add-to-cart-btn"
        className="product-card__button"
      >
        Add to Cart
      </button>
    </article>
  )
}
