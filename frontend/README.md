# E-Commerce Frontend Application

A complete React + Vite frontend for a microservices-based e-commerce system with JWT authentication, role-based access control, and separate user and admin interfaces.

## Features

### User Features

- **Authentication**: Register, login, logout with JWT token storage
- **Product Browsing**: View products with pagination and search
- **Product Details**: Detailed view of individual products
- **Shopping Cart**: Add/remove items, update quantities, persistent storage
- **Checkout**: Order placement with payment method selection
- **Order Management**: View personal orders, track status
- **Order Details**: View complete order information and status
- **Refund Requests**: Request refunds for delivered orders
- **User Profile**: View account information and roles

### Admin Features

- **Dashboard**: Overview statistics (orders, products, refunds, users)
- **Product Management**: Create, update, delete products with full CRUD
- **Order Management**: View all orders and update order statuses
- **Refund Management**: Approve/reject refund requests with amount customization
- **User Management**: View all users with search and role information

## Tech Stack

- **React 18** - UI library
- **Vite** - Build tool and dev server
- **React Router DOM** - Client-side routing
- **Axios** - HTTP client with JWT interceptor
- **Bootstrap 5** - UI framework
- **React Hot Toast** - Toast notifications
- **Context API** - State management (Authentication, Cart)

## Project Structure

```
src/
  ├── api/
  │   ├── index.js              # Axios configuration with JWT interceptor
  │   ├── authService.js        # Auth API endpoints
  │   ├── productService.js     # Product API endpoints
  │   ├── orderService.js       # Order API endpoints
  │   ├── refundService.js      # Refund API endpoints
  │   └── userService.js        # User API endpoints
  ├── components/
  │   ├── ProtectedRoute.jsx    # Route protection component
  │   ├── LoadingSpinner.jsx    # Loading indicator
  │   ├── ProductCard.jsx       # Product card component
  │   ├── Header.jsx            # User navigation header
  │   └── AdminSidebar.jsx      # Admin sidebar navigation
  ├── context/
  │   ├── AuthContext.jsx       # Authentication context
  │   └── CartContext.jsx       # Shopping cart context
  ├── layouts/
  │   ├── UserLayout.jsx        # User app layout
  │   └── AdminLayout.jsx       # Admin app layout
  ├── pages/
  │   ├── auth/
  │   │   ├── LoginPage.jsx
  │   │   └── RegisterPage.jsx
  │   ├── user/
  │   │   ├── HomePage.jsx
  │   │   ├── ProductDetailsPage.jsx
  │   │   ├── CartPage.jsx
  │   │   ├── CheckoutPage.jsx
  │   │   ├── MyOrdersPage.jsx
  │   │   ├── OrderDetailsPage.jsx
  │   │   ├── RefundRequestPage.jsx
  │   │   └── ProfilePage.jsx
  │   └── admin/
  │       ├── AdminDashboardPage.jsx
  │       ├── AdminProductsPage.jsx
  │       ├── AdminOrdersPage.jsx
  │       ├── AdminRefundsPage.jsx
  │       └── AdminUsersPage.jsx
  ├── App.jsx                   # Main app with routing
  ├── main.jsx                  # Entry point
  └── index.css                 # Global styles
```

## Installation & Setup

### Prerequisites

- Node.js (v14 or higher)
- npm or yarn

### 1. Install Dependencies

```bash
npm install
```

### 2. Configure API URL

Create a `.env.local` file in the root directory:

```
VITE_API_URL=http://localhost:8080/api
```

Update the API URL based on your backend service location. The default expects microservices running on `http://localhost:8080`.

### 3. Start Development Server

```bash
npm run dev
```

The application will open at `http://localhost:5173`

## Available Scripts

```bash
# Development server (with hot reload)
npm run dev

# Build for production
npm run build

# Preview production build locally
npm run preview

# Run linter (if configured)
npm run lint
```

## API Endpoints Configuration

The application communicates with the following microservices:

### User Service

- `POST /auth/register` - User registration
- `POST /auth/login` - User login
- `GET /users/profile` - Get user profile
- `GET /users/admin/all` - Get all users (admin)

### Product Service

- `GET /products` - Get all products (paginated)
- `GET /products/{id}` - Get product details
- `POST /products` - Create product (admin)
- `PUT /products/{id}` - Update product (admin)
- `DELETE /products/{id}` - Delete product (admin)

### Order Service

- `POST /orders` - Create order
- `GET /orders/my-orders` - Get user's orders
- `GET /orders/{id}` - Get order details
- `GET /orders/admin/all` - Get all orders (admin)
- `PATCH /orders/{id}/status` - Update order status (admin)

### Refund Service

- `POST /refunds/order/{orderId}` - Request refund
- `GET /refunds/pending` - Get pending refunds (admin)
- `PATCH /refunds/{id}/resolve` - Resolve refund (admin)

## Authentication Flow

1. **Registration**: New user creates account via `/register`
2. **Login**: User logs in via `/login` with email and password
3. **Token Storage**: JWT token and user data stored in localStorage
4. **Axios Interceptor**: Automatically adds Bearer token to all API requests
5. **Role-Based Redirect**:
   - Admin users → `/admin/dashboard`
   - Normal users → `/`
6. **Protected Routes**: Routes check authentication and roles
7. **401 Handling**: Automatic redirect to login on expired/invalid token

## Cart Management

- **Local Storage**: Cart persists across browser sessions
- **Context API**: Global cart state accessible from any component
- **Cart Operations**: Add, remove, update quantity, clear cart
- **Total Calculation**: Automatic total calculation on cart updates

## Features Deep Dive

### Product Browsing & Search

- Display products in responsive grid layout
- Pagination support for large product lists
- Real-time search filtering
- Product details modal/page

### Shopping Cart

- Add/remove items
- Update item quantities
- Persist cart in localStorage
- View cart total with calculations

### Checkout

- Billing information form
- Payment method selection (Credit Card, Net Banking, UPI)
- Order summary display
- Order placement with cart clearing

### Order Management

- List all personal orders
- Filter by status
- View detailed order information
- Track shipment status
- Request refunds for delivered orders

### Admin Dashboard

- Quick statistics overview
- Fast access to management sections
- Order status management
- Product CRUD operations
- Refund approval/rejection workflow
- User directory

### Refund System

- Request refunds for eligible orders (DELIVERED/COMPLETED)
- Select reason type from predefined options
- Provide detailed reason
- Specify refund amount
- Admin approves/rejects with notes
- Admin can modify approved amount

## Form Validation

All forms include:

- Required field validation
- Email format validation
- Password strength validation
- Amount validation
- Error message display

## Error Handling

- API error messages displayed in toast notifications
- 401 Unauthorized → Auto-redirect to login
- Failed requests show user-friendly error messages
- Loading states during API calls

## Styling & UI

- **Bootstrap 5** for responsive design
- **Custom CSS** in `src/index.css` for additional styling
- **Responsive Grid** layouts for all pages
- **Cards & Tables** for data display
- **Badges** for status indicators
- **Loading Spinners** for async operations
- **Toast Notifications** for user feedback

## Demo Credentials

For testing with login:

```
User Account:
  Email: user@example.com
  Password: password

Admin Account:
  Email: admin@example.com
  Password: password
```

## Building for Production

```bash
npm run build
```

This creates an optimized build in the `dist/` directory ready for deployment.

## Deployment

The built files can be deployed to:

- Vercel
- Netlify
- AWS S3 + CloudFront
- GitHub Pages
- Any static hosting service

## Environment Variables

Create `.env.local` file:

```
VITE_API_URL=http://localhost:8080/api
```

## Backend Integration Notes

- Ensure CORS is enabled on backend services
- Backend should return JWT tokens in `accessToken` field
- User object should include `userId`, `username`, `email`, `roles` fields
- Order and Product responses should include proper field mappings
- All dates should be in ISO 8601 format

## Browser Support

- Chrome (latest)
- Firefox (latest)
- Safari (latest)
- Edge (latest)

## Performance Optimization

- Code splitting with lazy loading routes
- Image optimization with placeholder support
- Pagination for large datasets
- Toast notifications instead of modal alerts
- Bootstrap CDN for faster CSS delivery

## Common Issues & Solutions

### 401 Unauthorized Errors

- Check if backend service is running
- Verify API URL in `.env.local`
- Check if token is stored in localStorage
- Clear localStorage and re-login

### CORS Errors

- Ensure backend has CORS enabled
- Check allowed origins in backend config
- Verify API URL is correct

### Products Not Loading

- Check Product Service endpoint
- Verify pagination parameters
- Check network tab in browser DevTools

## Future Enhancements

- Product reviews and ratings
- Wishlist functionality
- Advanced search filters
- Multiple payment gateway integration
- Email notifications
- Order tracking with map
- Inventory management
- Analytics dashboard for admins

## Contributing

When adding new features:

1. Create feature branch
2. Follow existing code structure
3. Add comments for complex logic
4. Test thoroughly
5. Submit pull request

## License

This project is part of an e-commerce microservices system.

## Support

For issues or questions, refer to backend service documentation or contact the development team.
