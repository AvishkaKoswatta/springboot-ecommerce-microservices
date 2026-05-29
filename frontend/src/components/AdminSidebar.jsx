import React from 'react'
import { Nav, Button } from 'react-bootstrap'
import { Link, useNavigate, useLocation } from 'react-router-dom'
import { useAuth } from '../context/AuthContext'

import {
  LayoutDashboard,
  Package,
  Tag,
  Folder,
  ClipboardList,
  RefreshCw,
  Users,
  Store,
  LogOut,
  Star
} from 'lucide-react'

export const AdminSidebar = () => {
  const { logout, user } = useAuth()
  const navigate = useNavigate()
  const location = useLocation()

  const handleLogout = () => {
    logout()
    navigate('/login')
  }

  const isActive = (path) => location.pathname === path

  return (
    <div
      style={{
        width: 260,
        height: '100vh',
        background: '#111827',
        color: 'white',
        display: 'flex',
        flexDirection: 'column'
      }}
    >

      {/* HEADER */}
      <div className="p-3 border-bottom border-secondary">
        <h5 className="mb-1 fw-bold">Admin Panel</h5>
        <small className="text-muted">
          {user?.username}
        </small>
      </div>

      {/* NAV */}
      <Nav className="flex-column flex-grow-1 p-2">

        <SidebarLink
          to="/admin/dashboard"
          icon={<LayoutDashboard size={18} />}
          label="Dashboard"
          active={isActive('/admin/dashboard')}
        />

        <SidebarLink
          to="/admin/products"
          icon={<Package size={18} />}
          label="Products"
          active={isActive('/admin/products')}
        />

        <SidebarLink
          to="/admin/reviews"
          icon={<Star size={18} />}
          label="Reviews"
          active={isActive('/admin/reviews')}
        />

        <SidebarLink
          to="/admin/categories"
          icon={<Tag size={18} />}
          label="Categories"
          active={isActive('/admin/categories')}
        />

        <SidebarLink
          to="/admin/orders"
          icon={<ClipboardList size={18} />}
          label="Orders"
          active={isActive('/admin/orders')}
        />

        <SidebarLink
          to="/admin/refunds"
          icon={<RefreshCw size={18} />}
          label="Refunds"
          active={isActive('/admin/refunds')}
        />

        <SidebarLink
          to="/admin/users"
          icon={<Users size={18} />}
          label="Users"
          active={isActive('/admin/users')}
        />

        <SidebarLink
          to="/"
          icon={<Store size={18} />}
          label="Store"
          active={false}
        />

      </Nav>

      {/* FOOTER */}
      <div className="p-3 border-top border-secondary">
        <Button
          variant="outline-light"
          className="w-100 d-flex align-items-center justify-content-center gap-2"
          onClick={handleLogout}
        >
          <LogOut size={16} />
          Logout
        </Button>
      </div>

      {/* STYLE */}
      <style>{`
        .sidebar-link {
          display: flex;
          align-items: center;
          gap: 10px;
          padding: 10px 12px;
          border-radius: 8px;
          color: #cbd5e1;
          text-decoration: none;
          margin-bottom: 4px;
          transition: 0.2s;
        }

        .sidebar-link:hover {
          background: rgba(255,255,255,0.08);
          color: white;
        }

        .sidebar-link.active {
          background: #2563eb;
          color: white;
        }
      `}</style>
    </div>
  )
}

/* reusable link component */
const SidebarLink = ({ to, icon, label, active }) => (
  <Link to={to} className={`sidebar-link ${active ? 'active' : ''}`}>
    {icon}
    {label}
  </Link>
)

export default AdminSidebar