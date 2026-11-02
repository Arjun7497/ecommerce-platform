import React from 'react'
import ReactDOM from 'react-dom/client'
import { configureStore } from '@reduxjs/toolkit'
import { Provider } from 'react-redux'
import { Toaster } from 'react-hot-toast'
import App from './App'
import authReducer from './store/slices/authSlice'
import cartReducer from './store/slices/cartSlice'
import './styles.css'

const store = configureStore({
  reducer: {
    auth: authReducer,
    cart: cartReducer,
  },
})

ReactDOM.createRoot(document.getElementById('root')).render(
  <React.StrictMode>
    <Provider store={store}>
      <App />
      <Toaster position="bottom-right" />
    </Provider>
  </React.StrictMode>,
)
