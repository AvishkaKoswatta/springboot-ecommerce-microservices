# Quick Start Guide - E-Commerce Frontend

## 🚀 Get Up and Running in 5 Minutes

### Step 1: Install Dependencies (Already Done ✅)

```bash
npm install
```

### Step 2: Configure Your Environment

Create a `.env.local` file in the root directory:

```
VITE_API_URL=http://localhost:8080/api
```

Update the URL based on your backend service location:

- **Local Development**: `http://localhost:8080/api`
- **Remote Server**: `http://your-server.com/api`

### Step 3: Start Development Server

```bash
npm run dev
```

The application will automatically open at:

```
http://localhost:5173
```

### Step 4: Login with Demo Credentials

**Regular User:**

```
Email: user@example.com
Password: password
```

**Admin User:**

```
Email: admin@example.com
Password: password
```

---

## 📚 Available Commands

```bash
# Start development server with hot reload
npm run dev

# Build for production
npm run build

# Preview production build
npm run preview

# Run linter (if configured)
npm run lint
```

---

## 🔧 Backend Integration Checklist

Before running the frontend, ensure your backend microservices are running:

- [ ] User Service running on port 8080
- [ ] Product Service running on port 8081
- [ ] Order Service running on port 8082
- [ ] Refund Service running on port 8083

Or ensure all services are accessible via a single API Gateway at:

```
http://localhost:8080/api
```

---

## 📱 Features Quick Reference

### For Users:

1. **Register/Login** - Create account or sign in
2. **Browse Products** - View products with search & pagination
3. **View Details** - Check detailed product information
4. **Add to Cart** - Build your shopping cart
5. **Checkout** - Complete purchase with payment method
6. **Track Orders** - View order status and details
7. **Request Refunds** - For delivered orders
8. **View Profile** - Account information

### For Admins:

1. **Dashboard** - Overview statistics
2. **Manage Products** - CRUD operations
3. **Manage Orders** - View and update statuses
4. **Manage Refunds** - Approve/reject requests
5. **Manage Users** - Search and view user info

---

## 🐛 Troubleshooting

### Port Already in Use

If port 5173 is busy:

```bash
# Vite will automatically use the next available port
npm run dev
```

### API Connection Issues

1. **Check backend is running**

   ```bash
   curl http://localhost:8080/api/products
   ```

2. **Update API URL in `.env.local`**

   ```
   VITE_API_URL=http://your-api-url/api
   ```

3. **Check CORS headers** in backend configuration

### 401 Unauthorized Errors

- Clear browser localStorage: `localStorage.clear()`
- Re-login with valid credentials
- Verify backend returns JWT token

### Build Errors

```bash
# Clear node_modules and reinstall
rm -r node_modules
npm install
npm run build
```

---

## 📁 Project Structure Overview

```
src/
├── api/              # API service layer with Axios
├── components/       # Reusable React components
├── context/          # Context API (Auth, Cart)
├── layouts/          # App layouts (User, Admin)
├── pages/            # Page components
│   ├── auth/         # Login & Register
│   ├── user/         # User pages
│   └── admin/        # Admin pages
├── App.jsx           # Main routing
├── main.jsx          # Entry point
└── index.css         # Global styles
```

---

## 🎯 Development Tips

### Hot Module Replacement (HMR)

Changes to files are automatically reflected in the browser without page reload.

### Component Reusability

- `ProductCard` - Display product in list/grid
- `LoadingSpinner` - Show loading state
- `ProtectedRoute` - Protect routes with auth check

### Context Hooks

```javascript
// Use authentication
const { user, login, logout } = useAuth();

// Use shopping cart
const { cart, addToCart, removeFromCart } = useCart();
```

### Making API Calls

```javascript
import { productService } from "../../api/productService";

// Get all products
const response = await productService.getAllProducts(page, size, search);

// Interceptor automatically adds JWT token
```

---

## 📦 Production Deployment

### Build for Production

```bash
npm run build
```

### Deploy Built Files

The `dist/` folder contains production-ready files. Deploy to:

- **Vercel**: Push to git or drag `dist` folder
- **Netlify**: Connect repo or drag `dist` folder
- **AWS S3**: Upload `dist` contents to S3 bucket
- **GitHub Pages**: Configure for static site hosting

### Environment Setup

Update `.env.local` with production API URL:

```
VITE_API_URL=https://your-production-api.com/api
```

---

## 🎨 Customization

### Change Colors

Edit `src/index.css` CSS variables:

```css
:root {
  --bs-primary: #007bff;
  --bs-secondary: #6c757d;
  --bs-success: #28a745;
  /* ... more colors */
}
```

### Add New Pages

1. Create file in `src/pages/category/NewPage.jsx`
2. Add route in `src/App.jsx`
3. Add navigation link in header/sidebar

### Modify API Endpoints

Update service files in `src/api/`:

- `authService.js`
- `productService.js`
- `orderService.js`
- `refundService.js`
- `userService.js`

---

## 📞 Support

For issues or questions:

1. Check `README.md` for detailed documentation
2. Review `IMPLEMENTATION_SUMMARY.md` for architecture
3. Check component JSDoc comments
4. Verify backend API responses

---

## ✨ Next Steps

1. ✅ Start development server
2. ✅ Test with demo credentials
3. ✅ Integrate with your backend
4. ✅ Customize colors and styling
5. ✅ Add additional features
6. ✅ Deploy to production

---

**Happy Coding! 🎉**
