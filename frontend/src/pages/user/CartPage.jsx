import React from 'react'
import {
  Container,
  Row,
  Col,
  Card,
  Button
} from 'react-bootstrap'
import { Link, useNavigate } from 'react-router-dom'
import { useCart } from '../../context/CartContext'

import {
  ShoppingCart,
  Trash2,
  Plus,
  Minus,
  ArrowRight,
  ShoppingBag,
  Truck,
  CreditCard
} from 'lucide-react'

export const CartPage = () => {
  const {
    cart,
    removeFromCart,
    updateQuantity,
    getCartTotal
  } = useCart()

  const navigate = useNavigate()

  if (cart.length === 0) {
    return (
      <Container className="py-5">
        <div
          className="text-center p-5 rounded shadow-sm bg-white"
        >
          <ShoppingCart size={80} className="mb-3 text-primary" />

          <h3>Your Cart is Empty</h3>

          <p className="text-muted mb-4">
            Looks like you haven't added anything yet
          </p>

          <Button
            size="lg"
            onClick={() => navigate('/')}
          >
            <ShoppingBag size={18} className="me-2" />
            Continue Shopping
          </Button>
        </div>
      </Container>
    )
  }

  return (
    <div
      style={{
        background: '#f5f7fb',
        minHeight: '100vh'
      }}
    >
      <Container className="py-5">

        {/* Header */}

        <div className="mb-4">
          <h2 className="fw-bold d-flex align-items-center gap-2">
            <ShoppingCart />
            Shopping Cart
          </h2>

          <small className="text-muted">
            {cart.length} items in your cart
          </small>
        </div>

        <Row>

          {/* LEFT SIDE */}

          <Col lg={8}>

            {cart.map((item) => (
              <Card
                key={item.productId}
                className="mb-3 border-0 shadow-sm"
              >
                <Card.Body>

                  <Row className="align-items-center">

                    {/* Image */}

                    <Col md={2}>
                     <img
  src={
    item.images?.length > 0
      ? `http://localhost:8079${item.images[0].url}`
      : 'https://via.placeholder.com/150'
  }
  alt={item.name}
  className="img-fluid rounded"
  style={{
    height: 90,
    width: 90,
    objectFit: 'cover'
  }}
/>
                    </Col>

                    {/* Product info */}

                    <Col md={4}>
                      <Link
                        to={`/product/${item.productId}`}
                        className="text-decoration-none"
                      >
                        <h6 className="fw-bold mb-1">
                          {item.name}
                        </h6>
                      </Link>

                      {/* <small className="text-muted">
                        Product ID: #{item.productId}
                      </small> */}
                    </Col>

                    {/* Quantity */}

                    <Col md={3}>

                      <div
                        className="d-flex align-items-center justify-content-center gap-2"
                      >

                        <Button
                          variant="light"
                          onClick={() =>
                            updateQuantity(
                              item.productId,
                              Math.max(
                                item.quantity - 1,
                                1
                              )
                            )
                          }
                        >
                          <Minus size={15} />
                        </Button>

                        <span
                          className="fw-bold px-3"
                        >
                          {item.quantity}
                        </span>

                        <Button
                          variant="light"
                          onClick={() =>
                            updateQuantity(
                              item.productId,
                              item.quantity + 1
                            )
                          }
                        >
                          <Plus size={15} />
                        </Button>

                      </div>

                    </Col>

                    {/* Price */}

                    <Col md={2}>
                      <div className="fw-bold fs-7">
                        $
                        {(
                          item.price *
                          item.quantity
                        ).toFixed(2)}
                      </div>

                      <small className="text-muted">
                        ${item.price} each
                      </small>
                    </Col>

                    {/* Delete */}

                    <Col
                      md={1}
                      className="text-end"
                    >
                      <Button
                        variant="light"
                        onClick={() =>
                          removeFromCart(
                            item.id
                          )
                        }
                      >
                        <Trash2
                          size={18}
                          className="text-danger"
                        />
                      </Button>
                    </Col>

                  </Row>

                </Card.Body>
              </Card>
            ))}

          </Col>

          {/* RIGHT SIDE */}

          <Col lg={4}>

            <Card
              className="border-0 shadow-sm sticky-top"
              style={{ top: 20 }}
            >
              <Card.Body>

                <h5 className="fw-bold mb-4">
                  Order Summary
                </h5>

                <div className="d-flex justify-content-between mb-3">
                  <span>Subtotal</span>
                  <strong>
                    ${getCartTotal()}
                  </strong>
                </div>

                <div className="d-flex justify-content-between mb-3">
                  <span className="d-flex gap-2">
                    <Truck size={18} />
                    Shipping
                  </span>

                  <span className="text-success">
                    Free
                  </span>
                </div>

                <div className="d-flex justify-content-between mb-3">
                  <span>Tax</span>
                  <span>$0.00</span>
                </div>

                <hr />

                <div className="d-flex justify-content-between mb-4">
                  <h5>Total</h5>

                  <h4 className="fw-bold text-primary">
                    ${getCartTotal()}
                  </h4>
                </div>

                <Button
                  className="w-100 mb-3 d-flex align-items-center justify-content-center gap-2"
                  size="lg"
                  onClick={() =>
                    navigate('/checkout')
                  }
                >
                  <CreditCard size={18} />
                  Checkout
                  <ArrowRight size={18} />
                </Button>

                <Button
                  variant="outline-dark"
                  className="w-100"
                  onClick={() =>
                    navigate('/')
                  }
                >
                  Continue Shopping
                </Button>

              </Card.Body>
            </Card>

          </Col>

        </Row>
      </Container>
    </div>
  )
}

export default CartPage