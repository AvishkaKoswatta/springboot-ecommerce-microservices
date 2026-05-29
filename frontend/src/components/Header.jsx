import React from 'react'
import { Navbar, Nav, Container, Badge, Button } from 'react-bootstrap'
import { Link, useNavigate } from 'react-router-dom'
import { useAuth } from '../context/AuthContext'
import { useCart } from '../context/CartContext'

export const Header = () => {
  const { user, isAuthenticated, isAdmin, logout } = useAuth()
  const { cart } = useCart()
  const navigate = useNavigate()

  const handleLogout = () => {
    logout()
    navigate('/login')
  }

  return (
    <Navbar bg="dark" expand="lg" sticky="top" className="navbar-dark">
      <Container>
        <Navbar.Brand as={Link} to="/" className="fw-bold">
          E-Commerce Store
        </Navbar.Brand>
        <Navbar.Toggle aria-controls="basic-navbar-nav" />
        <Navbar.Collapse id="basic-navbar-nav">
          <Nav className="ms-auto align-items-lg-center">
            {isAuthenticated() ? (
              <>
                <Nav.Link as={Link} to="/">Home</Nav.Link>

                <Nav.Link as={Link} to="/cart" className="position-relative">
                  Cart
                  {cart.length > 0 && (
                    <Badge
                      bg="danger"
                      pill
                      className="position-absolute top-0 start-100 translate-middle"
                    >
                      {cart.length}
                    </Badge>
                  )}
                </Nav.Link>

                <Nav.Link as={Link} to="/orders">My Orders</Nav.Link>

                <Nav.Link as={Link} to="/profile">
                 Profile
                </Nav.Link>

                {/* <Nav.Link as={Link} to="/settings">Settings</Nav.Link> */}

                {isAdmin() && (
                  <Nav.Link as={Link} to="/admin/dashboard">Admin</Nav.Link>
                )}

                <Button
                  variant="outline-light"
                  size="sm"
                  onClick={handleLogout}
                  className="ms-2"
                >
                  Logout
                </Button>
              </>
            ) : (
              <>
                <Nav.Link as={Link} to="/login">Login</Nav.Link>
                <Nav.Link as={Link} to="/register">Register</Nav.Link>
              </>
            )}
          </Nav>
        </Navbar.Collapse>
      </Container>
    </Navbar>
  )
}

export default Header
