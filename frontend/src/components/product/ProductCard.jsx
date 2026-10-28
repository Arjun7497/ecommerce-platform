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
    <div data-testid="product-card" className="border rounded-lg p-4 flex flex-col gap-3 shadow-sm hover:shadow-md transition-shadow">
      {product.imageUrl && (
        <img
          src={product.imageUrl}
          alt={product.name}
          className="w-full h-48 object-cover rounded"
        />
      )}
      <div className="flex-1">
        <span className="text-xs text-blue-600 font-medium uppercase tracking-wide">
          {product.category}
        </span>
        <h3 className="text-lg font-semibold mt-1" data-testid="product-name">
          {product.name}
        </h3>
        <p className="text-gray-500 text-sm mt-1 line-clamp-2">{product.description}</p>
      </div>
      <div className="flex items-center justify-between mt-2">
        <span className="text-xl font-bold text-gray-900" data-testid="product-price">
          ${product.price.toFixed(2)}
        </span>
        <span className="text-sm text-gray-400">
          {product.stockQuantity > 0 ? `${product.stockQuantity} in stock` : 'Out of stock'}
        </span>
      </div>
      <button
        onClick={handleAddToCart}
        disabled={product.stockQuantity === 0}
        data-testid="add-to-cart-btn"
        className="w-full bg-blue-600 text-white py-2 rounded-lg hover:bg-blue-700 disabled:opacity-50 disabled:cursor-not-allowed transition-colors"
      >
        Add to Cart
      </button>
    </div>
  )
}
