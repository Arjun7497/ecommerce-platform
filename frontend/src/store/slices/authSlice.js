import { createSlice, createAsyncThunk } from '@reduxjs/toolkit'
import { authService } from '../../services/api'

export const login = createAsyncThunk('auth/login', async (credentials, { rejectWithValue }) => {
  try {
    const data = await authService.login(credentials)
    localStorage.setItem('token', data.token)
    return data
  } catch (err) {
    return rejectWithValue(err)
  }
})

export const register = createAsyncThunk('auth/register', async (userData, { rejectWithValue }) => {
  try {
    return await authService.register(userData)
  } catch (err) {
    return rejectWithValue(err)
  }
})

const authSlice = createSlice({
  name: 'auth',
  initialState: {
    user: null,
    token: localStorage.getItem('token'),
    loading: false,
    error: null,
  },
  reducers: {
    logout(state) {
      state.user = null
      state.token = null
      localStorage.removeItem('token')
    },
    clearError(state) {
      state.error = null
    },
  },
  extraReducers: (builder) => {
    builder
      .addCase(login.pending, (state) => { state.loading = true; state.error = null })
      .addCase(login.fulfilled, (state, action) => {
        state.loading = false
        state.token = action.payload.token
        state.user = action.payload.user
      })
      .addCase(login.rejected, (state, action) => {
        state.loading = false
        state.error = action.payload?.detail ?? 'Login failed'
      })
      .addCase(register.pending, (state) => { state.loading = true; state.error = null })
      .addCase(register.fulfilled, (state) => { state.loading = false })
      .addCase(register.rejected, (state, action) => {
        state.loading = false
        state.error = action.payload?.detail ?? 'Registration failed'
      })
  },
})

export const { logout, clearError } = authSlice.actions
export const selectUser = (state) => state.auth.user
export const selectIsAuthenticated = (state) => !!state.auth.token
export const selectAuthLoading = (state) => state.auth.loading
export const selectAuthError = (state) => state.auth.error

export default authSlice.reducer
