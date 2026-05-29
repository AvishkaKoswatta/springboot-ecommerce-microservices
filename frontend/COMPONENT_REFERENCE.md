# Component & File Reference Guide

## 📦 API Services (`src/api/`)

### `index.js` - Axios Configuration

- Centralized Axios instance
- JWT token interceptor
- Automatic token inclusion in headers
- 401 error handling with logout
- Base URL configuration from environment

### `authService.js` - Authentication API

- `register()` - User registration
- `login()` - User login with email/password
- `getProfile()` - Get current user profile
- `getAllUsers()` - Get all users (admin)

### `productService.js` - Product Management

- `getAllProducts()` - Get paginated products with search
- `getProductById()` - Get single product details
- `createProduct()` - Create new product (admin)
- `updateProduct()` - Update existing product (admin)
- `deleteProduct()` - Delete product (admin)

### `orderService.js` - Order Management

- `createOrder()` - Place new order
- `getMyOrders()` - Get user's orders
- `getOrderById()` - Get order details
- `getAllOrders()` - Get all orders (admin)
- `updateOrderStatus()` - Update order status (admin)

### `refundService.js` - Refund Management

- `requestRefund()` - Create refund request
- `getPendingRefunds()` - Get pending refunds (admin)
- `resolveRefund()` - Approve/reject refund (admin)

### `userService.js` - User Data

- `getAllUsers()` - Get paginated users with search
- `getProfile()` - Get current user profile

---

## 🧩 Components (`src/components/`)

### `ProtectedRoute.jsx`

**Purpose**: Route protection with role-based access control
**Props**:

- `children` - Component to render if authorized
- `requiredRole` - Optional role requirement (e.g., "ADMIN")

**Features**:

- Checks authentication status
- Redirects to login if not authenticated
- Validates user role if required
- Redirects to home if role doesn't match

### `LoadingSpinner.jsx`

**Purpose**: Display loading indicator while fetching data
**Props**:

- `size` - Spinner size ("sm", "lg", default: "sm")
- `text` - Loading text to display

**Features**:

- Centered spinner
- Customizable text
- Responsive sizing

### `ProductCard.jsx`

**Purpose**: Reusable product display component
**Props**:

- `product` - Product object with all data
- `onAddToCart` - Callback when add to cart is clicked
- `onViewDetails` - Callback when view details is clicked

**Features**:

- Product image, name, price, description
- Add to cart button
- View details button
- Card hover effects
- Responsive grid layout

### `Header.jsx`

**Purpose**: Navigation header for user application
**Features**:

- Navbar with logo
- Navigation links (Home, Cart, Orders, Profile)
- Cart item count badge
- User greeting
- Logout button
- Responsive mobile menu
- Show/hide nav based on auth status

### `AdminSidebar.jsx`

**Purpose**: Navigation sidebar for admin dashboard
**Features**:

- Admin panel branding
- Navigation links to all admin sections
- Link to go back to user store
- Logout button
- Responsive design

---

## 📄 Context Providers (`src/context/`)

### `AuthContext.jsx`

**Purpose**: Centralized authentication state management

**State**:

- `user` - Current user object with username, email, roles
- `accessToken` - JWT authentication token
- `isLoading` - Loading state indicator
- `error` - Error messages

**Methods**:

- `login(userData, token)` - Store user and token
- `logout()` - Clear user and token
- `isAdmin()` - Check if user has ADMIN role
- `isAuthenticated()` - Check if user is logged in

**Storage**: Uses localStorage for persistence

### `CartContext.jsx`

**Purpose**: Shopping cart state management

**State**:

- `cart` - Array of cart items with quantity
- `total` - Total cart amount

**Methods**:

- `addToCart(product)` - Add product to cart
- `removeFromCart(productId)` - Remove product from cart
- `updateQuantity(productId, quantity)` - Update item quantity
- `clearCart()` - Empty entire cart
- `getCartTotal()` - Get formatted total

**Storage**: Uses localStorage for persistence

---

## 🎨 Layouts (`src/layouts/`)

### `UserLayout.jsx`

**Purpose**: Main layout for user application
**Components**:

- Header navigation
- Page content area (via Outlet)
- Footer
- Toast notifications

**Responsive**: Mobile, tablet, desktop optimized

### `AdminLayout.jsx`

**Purpose**: Main layout for admin dashboard
**Components**:

- Sidebar navigation
- Main content area (via Outlet)
- Protected route checking

**Features**:

- Admin-only access
- Sticky sidebar
- Responsive layout

---

## 📖 Pages - Authentication (`src/pages/auth/`)

### `LoginPage.jsx`

**Route**: `/login`
**Features**:

- Email and password input
- Form validation
- Error messages
- Loading state
- Link to register
- Demo credentials display
- Role-based redirect after login
- Toast notifications

**Validation**:

- Email format
- Password required

### `RegisterPage.jsx`

**Route**: `/register`
**Features**:

- Username, email, password inputs
- Password confirmation
- Form validation
- Error display
- Loading state
- Link to login
- Toast notifications

**Validation**:

- Username required
- Email format
- Password minimum length
- Password confirmation match

---

## 👤 Pages - User (`src/pages/user/`)

### `HomePage.jsx`

**Route**: `/`
**Features**:

- Product grid with pagination
- Real-time search bar
- Product cards with images and prices
- Pagination controls
- Add to cart functionality
- View details navigation
- Empty state message

**Pagination**: 12 products per page

### `ProductDetailsPage.jsx`

**Route**: `/product/:id`
**Features**:

- Large product image
- Product name, price, description
- Stock availability badge
- Quantity input with validation
- Add to cart button
- Specifications display
- Product metadata
- Link back to products

### `CartPage.jsx`

**Route**: `/cart`
**Features**:

- Display all cart items
- Item images, prices, quantities
- Quantity update input
- Remove item button
- Order summary section
- Total calculation
- Proceed to checkout button
- Continue shopping button
- Empty cart message

### `CheckoutPage.jsx`

**Route**: `/checkout`
**Features**:

- Billing information form
- Payment method selection (3 methods)
- Order summary with itemization
- Total calculation
- Place order button
- Back to cart option
- Order confirmation redirect

**Payment Methods**:

- CREDIT_CARD
- NET_BANKING
- UPI

### `MyOrdersPage.jsx`

**Route**: `/orders`
**Features**:

- Orders table with pagination
- Order ID, date, status, total
- Status badges with color coding
- Payment status badges
- View order button
- Refund request button (for delivered)
- Pagination controls
- Empty state message

### `OrderDetailsPage.jsx`

**Route**: `/order/:id`
**Features**:

- Order header (ID, date, status)
- Order items table with product info
- Shipping information
- Payment information
- Order summary sidebar
- Total calculation
- Payment method display
- Refund request button

### `RefundRequestPage.jsx`

**Route**: `/refund-request/:id`
**Features**:

- Refund eligibility check
- Reason selection dropdown (5 types)
- Detailed reason textarea
- Refund amount input (max: order total)
- Order information display
- Submit and cancel buttons
- Validation

**Refund Reasons**:

- DAMAGED_PRODUCT
- WRONG_ITEM
- NOT_DELIVERED
- CUSTOMER_CANCELLED
- OTHER

### `ProfilePage.jsx`

**Route**: `/profile`
**Features**:

- User account information
- Email display
- Username display
- User ID
- Roles display with badges
- Member since date
- Account summary stats
- Quick action links

---

## ⚙️ Pages - Admin (`src/pages/admin/`)

### `AdminDashboardPage.jsx`

**Route**: `/admin/dashboard`
**Features**:

- Total orders card
- Total products card
- Pending refunds card
- Total users card
- Quick action buttons
- Icon and color-coded cards
- Loading states for stats

### `AdminProductsPage.jsx`

**Route**: `/admin/products`
**Features**:

- Product table with pagination
- Search functionality
- Create product button
- Edit product modal
- Delete product button
- Stock status badges
- Pagination

**CRUD Operations**:

- Create: New product form in modal
- Read: List all products with search
- Update: Edit modal with pre-filled data
- Delete: Confirmation dialog

### `AdminOrdersPage.jsx`

**Route**: `/admin/orders`
**Features**:

- Orders table with pagination
- Status filter dropdown
- Order information display
- Inline status update
- Status dropdown on selected order
- Save/cancel buttons
- Pagination

**Statuses Available**: 7 order statuses

### `AdminRefundsPage.jsx`

**Route**: `/admin/refunds`
**Features**:

- Pending refunds table
- Resolve button for each refund
- Refund resolution modal
- Status selection (Approve/Reject)
- Approved amount field
- Admin notes textarea
- Refund reason display
- Pagination

**Modal Fields**:

- Refund ID (read-only)
- Order ID (read-only)
- Reason (read-only)
- Details (read-only)
- Requested amount (read-only)
- Decision (dropdown)
- Approved amount (conditional)
- Admin notes (textarea)

### `AdminUsersPage.jsx`

**Route**: `/admin/users`
**Features**:

- Users table with pagination
- Search by name/email
- User information display
- Role badges
- Creation date display
- Pagination controls

---

## 🎯 Main App (`src/App.jsx`)

**Purpose**: Main router configuration and app setup

**Route Structure**:

```
/ (root)
├── /login (public)
├── /register (public)
├── / (protected - user layout)
│   ├── /product/:id
│   ├── /cart
│   ├── /checkout
│   ├── /orders
│   ├── /order/:id
│   ├── /refund-request/:id
│   └── /profile
└── /admin/* (protected - admin layout)
    ├── /dashboard
    ├── /products
    ├── /orders
    ├── /refunds
    └── /users
```

**Features**:

- Route protection
- Role-based access control
- Layout wrapping
- Toast notification system
- Context providers setup

---

## 🎨 Styling (`src/index.css`)

**Global Styles**:

- Font family setup
- Box-sizing reset
- Body background color
- Container styling

**Component Styles**:

- Card hover effects
- Button styling
- Sidebar navigation
- Product card layout
- Cart items
- Order card styling
- Status badges
- Form controls
- Loading spinners
- Empty states
- Tables

**Responsive Design**:

- Mobile-first approach
- Media queries for breakpoints
- Sidebar mobile toggle
- Grid adjustments

---

## 🚀 Entry Point (`src/main.jsx`)

**Purpose**: Application bootstrap
**Sets up**:

- React root rendering
- Bootstrap CSS import
- Global CSS import
- App component mounting

---

## 📋 Configuration Files

### `package.json`

- Project metadata
- Dependencies listing
- NPM scripts (dev, build, preview, lint)
- Package versions

### `vite.config.js`

- Vite configuration
- React plugin setup
- Dev server port (5173)
- Auto-open on start

### `index.html`

- HTML template
- Root div for React
- Script module entry

### `.gitignore`

- Node modules
- Build output
- Environment files
- OS files

### `.env.example`

- Template for environment variables
- API URL configuration

---

## 📚 File Count Summary

- **API Services**: 6 files (1 config + 5 services)
- **Components**: 5 reusable components
- **Context Providers**: 2 context files
- **Layouts**: 2 layout files
- **Pages**: 15 page components (2 auth + 8 user + 5 admin)
- **Configuration**: 6 config files
- **Styling**: 1 CSS file + Bootstrap

**Total: 50+ files organized in clean structure**

---

## 🔄 Data Flow

### Authentication Flow

1. User registers → `RegisterPage` → `authService.register()`
2. User logs in → `LoginPage` → `authService.login()`
3. Token stored in `AuthContext` and `localStorage`
4. `ProtectedRoute` checks auth status
5. Logout clears `AuthContext` and `localStorage`

### Cart Flow

1. User adds product → `ProductCard` → `useCart().addToCart()`
2. Cart stored in `CartContext` and `localStorage`
3. `CartPage` displays and manages cart items
4. User proceeds to checkout → `CheckoutPage`
5. Order placed → cart cleared

### Order Flow

1. Checkout form submitted → `orderService.createOrder()`
2. Order created in backend
3. User redirected to order confirmation
4. User can view orders in `MyOrdersPage`
5. Admin can view/manage all orders

### Admin Flow

1. Admin logs in (role: ADMIN)
2. Redirected to `AdminDashboardPage`
3. Access to all admin sections via sidebar
4. Manage products, orders, refunds, users

---

This comprehensive guide should help understand the complete application structure and how all components work together!
