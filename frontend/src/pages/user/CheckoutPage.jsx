// import React, { useState } from 'react'
// import { Container, Row, Col, Form, Button, Card } from 'react-bootstrap'
// import { useNavigate } from 'react-router-dom'
// import { useCart } from '../../context/CartContext'
// import { orderService } from '../../api/orderService'
// import toast from 'react-hot-toast'

// export const CheckoutPage = () => {
//   const { cart, getCartTotal, clearCart } = useCart()
//   const navigate = useNavigate()
//   const [isLoading, setIsLoading] = useState(false)
//   const [paymentMethod, setPaymentMethod] = useState('CREDIT_CARD')

//   const [shippingAddress, setShippingAddress] = useState({
//     recipientName: '',
//     phone: '',
//     addressLine1: '',
//     addressLine2: '',
//     city: '',
//     state: '',
//     postalCode: '',
//     country: 'Sri Lanka'
//   })

//   if (cart.length === 0) {
//     return (
//       <Container className="py-5">
//         <h4>Your cart is empty</h4>
//         <Button onClick={() => navigate('/')}>Back to Shopping</Button>
//       </Container>
//     )
//   }

//   const handleChange = (e) => {
//     setShippingAddress({
//       ...shippingAddress,
//       [e.target.name]: e.target.value
//     })
//   }

//   const handlePlaceOrder = async () => {
//     setIsLoading(true)

//     try {
//       const orderPayload = {
//         paymentMethod,
//         customerNotes: "-",
//         items: cart.map(item => ({
//           productId: item.productId || item.id,
//           quantity: item.quantity
//         })),
//         shippingAddress
//       }

//       console.log("ORDER PAYLOAD:", orderPayload)

//       const response = await orderService.createOrder(orderPayload)

//       toast.success('Order placed successfully!')
//       clearCart()

//       const orderId = response?.data?.data?.id || response?.data?.id
//       navigate(`/order-confirmation/${orderId}`)

//     } catch (error) {
//       console.error(error)
//       toast.error(error?.message || 'Failed to place order')
//     } finally {
//       setIsLoading(false)
//     }
//   }

//   return (
//     <Container className="py-5">
//       <h2 className="mb-4">Checkout</h2>

//       <Row>
//         {/* SHIPPING FORM */}
//         <Col lg={8}>
//           <Card className="mb-4">
//             <Card.Header>Shipping Address</Card.Header>
//             <Card.Body>
//               <Row>
//                 <Col md={6}>
//                   <Form.Control
//                     name="recipientName"
//                     placeholder="Full Name"
//                     value={shippingAddress.recipientName}
//                     onChange={handleChange}
//                     className="mb-3"
//                   />
//                 </Col>

//                 <Col md={6}>
//                   <Form.Control
//                     name="phone"
//                     placeholder="Phone"
//                     value={shippingAddress.phone}
//                     onChange={handleChange}
//                     className="mb-3"
//                   />
//                 </Col>
//               </Row>

//               <Form.Control
//                 name="addressLine1"
//                 placeholder="Address Line 1"
//                 value={shippingAddress.addressLine1}
//                 onChange={handleChange}
//                 className="mb-3"
//               />

//               <Form.Control
//                 name="addressLine2"
//                 placeholder="Address Line 2"
//                 value={shippingAddress.addressLine2}
//                 onChange={handleChange}
//                 className="mb-3"
//               />

//               <Row>
//                 <Col md={6}>
//                   <Form.Control
//                     name="city"
//                     placeholder="City"
//                     value={shippingAddress.city}
//                     onChange={handleChange}
//                     className="mb-3"
//                   />
//                 </Col>

//                 <Col md={6}>
//                   <Form.Control
//                     name="state"
//                     placeholder="State"
//                     value={shippingAddress.state}
//                     onChange={handleChange}
//                     className="mb-3"
//                   />
//                 </Col>
//               </Row>

//               <Row>
//                 <Col md={6}>
//                   <Form.Control
//                     name="postalCode"
//                     placeholder="Postal Code"
//                     value={shippingAddress.postalCode}
//                     onChange={handleChange}
//                     className="mb-3"
//                   />
//                 </Col>

//                 <Col md={6}>
//                   <Form.Control
//                     name="country"
//                     value={shippingAddress.country}
//                     onChange={handleChange}
//                     className="mb-3"
//                   />
//                 </Col>
//               </Row>
//             </Card.Body>
//           </Card>

//           {/* PAYMENT */}
//           <Card>
//             <Card.Header>Payment Method</Card.Header>
//             <Card.Body>
//               {['CREDIT_CARD', 'NET_BANKING', 'UPI', 'CASH_ON_DELIVERY'].map((m) => (
//                 <Form.Check
//                   key={m}
//                   type="radio"
//                   name="payment"
//                   label={m}
//                   value={m}
//                   checked={paymentMethod === m}
//                   onChange={(e) => setPaymentMethod(e.target.value)}
//                 />
//               ))}
//             </Card.Body>
//           </Card>
//         </Col>

//         {/* ORDER SUMMARY */}
//         <Col lg={4}>
//           <Card>
//             <Card.Header>Order Summary</Card.Header>
//             <Card.Body>

//               {cart.map(item => (
//                 <div key={item.productId || item.id} className="d-flex justify-content-between mb-2">
//                   <span>{item.name} x {item.quantity}</span>
//                   <span>${(item.price * item.quantity).toFixed(2)}</span>
//                 </div>
//               ))}

//               <hr />

//               <div className="d-flex justify-content-between">
//                 <strong>Total:</strong>
//                 <strong>${getCartTotal()}</strong>
//               </div>

//               <Button
//                 className="w-100 mt-3"
//                 onClick={handlePlaceOrder}
//                 disabled={isLoading}
//               >
//                 {isLoading ? 'Placing Order...' : 'Place Order'}
//               </Button>

//               <Button
//                 variant="outline-secondary"
//                 className="w-100 mt-2"
//                 onClick={() => navigate('/cart')}
//               >
//                 Back
//               </Button>

//             </Card.Body>
//           </Card>
//         </Col>
//       </Row>
//     </Container>
//   )
// }

// export default CheckoutPage
import React, { useState } from 'react'
import { Container, Row, Col, Form, Button, Card } from 'react-bootstrap'
import { useNavigate } from 'react-router-dom'
import { useCart } from '../../context/CartContext'
import { orderService } from '../../api/orderService'
import { paymentService } from '../../api/paymentService'
import { useAuth } from '../../context/AuthContext'
import toast from 'react-hot-toast'

const PAYMENT_METHODS = [
  { value: 'CREDIT_CARD', label: '💳 Credit / Debit Card' },
  { value: 'NET_BANKING', label: '🏦 Net Banking' },
  { value: 'UPI', label: '📱 UPI' },
  { value: 'CASH_ON_DELIVERY', label: '💵 Cash on Delivery' },
]

export const CheckoutPage = () => {
  const { cart, getCartTotal, clearCart } = useCart()
  const navigate = useNavigate()
  const { user } = useAuth()

  const [isLoading, setIsLoading] = useState(false)
  const [paymentMethod, setPaymentMethod] = useState('CREDIT_CARD')

  const [shippingAddress, setShippingAddress] = useState({
    recipientName: '',
    phone: '',
    addressLine1: '',
    addressLine2: '',
    city: '',
    state: '',
    postalCode: '',
    country: 'Sri Lanka',
  })

  if (cart.length === 0) {
    return (
      <Container className="py-5">
        <h4>Your cart is empty</h4>
        <Button onClick={() => navigate('/')}>Back to Shopping</Button>
      </Container>
    )
  }

  const handleChange = (e) => {
    setShippingAddress({
      ...shippingAddress,
      [e.target.name]: e.target.value,
    })
  }

  const handlePlaceOrder = async () => {
    const required = ['recipientName', 'phone', 'addressLine1', 'city', 'postalCode']

    for (const field of required) {
      if (!shippingAddress[field]?.trim()) {
        toast.error(`Please fill in ${field}`)
        return
      }
    }

    setIsLoading(true)

    try {
      // 1. CREATE ORDER
      const orderPayload = {
        paymentMethod,
        customerNotes: '-',
        items: cart.map((item) => ({
          productId: item.productId || item.id,
          quantity: item.quantity,
        })),
        shippingAddress,
      }

      const orderRes = await orderService.createOrder(orderPayload)
      const order = orderRes?.data?.data ?? orderRes?.data ?? orderRes

      clearCart()

      // COD FLOW → NO PAYMENT
      if (paymentMethod === 'CASH_ON_DELIVERY') {
        toast.success('Order placed successfully!')
        navigate(`/order/${order.id}`)
        return
      }

      // 2. INITIATE PAYMENT (ONLY FOR REFERENCE)
      let paymentReference = null

      try {
        const payRes = await paymentService.initiatePayment({
          orderId: order.id,
          orderNumber: order.orderNumber,
          amount: order.totalAmount,
          currency: 'USD',
          paymentMethod: paymentMethod,
          userEmail: user?.email || order.userEmail,
        })

        paymentReference =
          payRes?.data?.paymentReference ||
          payRes?.paymentReference ||
          payRes?.data?.data?.paymentReference ||
          null
      } catch (err) {
        console.warn('Payment initiation failed:', err)
        toast.error('Payment initialization failed, retry on payment page')
      }

      toast.success('Order created successfully!')

      

      // 3. GO TO PAYMENT PAGE
      navigate(`/payment/${order.id}`, {
        state: {
          order,
          paymentReference,
          paymentMethod,
          amount: order.totalAmount,
          currency: 'USD',
          userEmail: user?.email || order.userEmail,
        },
      })

    } catch (error) {
      console.error(error)
      toast.error(error?.message || 'Failed to place order')
    } finally {
      setIsLoading(false)
    }
  }

  return (
    <Container className="py-5">
      <h2 className="mb-4">Checkout</h2>

      <Row>
        {/* SHIPPING */}
        <Col lg={8}>
          <Card className="mb-4">
            <Card.Header><strong>Shipping Address</strong></Card.Header>
            <Card.Body>

              <Row>
                <Col md={6}>
                  <Form.Control
                    name="recipientName"
                    placeholder="Full Name *"
                    value={shippingAddress.recipientName}
                    onChange={handleChange}
                    className="mb-3"
                  />
                </Col>
                <Col md={6}>
                  <Form.Control
                    name="phone"
                    placeholder="Phone *"
                    value={shippingAddress.phone}
                    onChange={handleChange}
                    className="mb-3"
                  />
                </Col>
              </Row>

              <Form.Control
                name="addressLine1"
                placeholder="Address Line 1 *"
                value={shippingAddress.addressLine1}
                onChange={handleChange}
                className="mb-3"
              />

              <Form.Control
                name="addressLine2"
                placeholder="Address Line 2"
                value={shippingAddress.addressLine2}
                onChange={handleChange}
                className="mb-3"
              />

              <Row>
                <Col md={6}>
                  <Form.Control
                    name="city"
                    placeholder="City *"
                    value={shippingAddress.city}
                    onChange={handleChange}
                    className="mb-3"
                  />
                </Col>
                <Col md={6}>
                  <Form.Control
                    name="state"
                    placeholder="State"
                    value={shippingAddress.state}
                    onChange={handleChange}
                    className="mb-3"
                  />
                </Col>
              </Row>

              <Row>
                <Col md={6}>
                  <Form.Control
                    name="postalCode"
                    placeholder="Postal Code *"
                    value={shippingAddress.postalCode}
                    onChange={handleChange}
                    className="mb-3"
                  />
                </Col>
                <Col md={6}>
                  <Form.Control
                    name="country"
                    value={shippingAddress.country}
                    onChange={handleChange}
                    className="mb-3"
                  />
                </Col>
              </Row>

            </Card.Body>
          </Card>

          {/* PAYMENT METHOD */}
          <Card>
            <Card.Header><strong>Payment Method</strong></Card.Header>
            <Card.Body>
              {PAYMENT_METHODS.map((m) => (
                <Form.Check
                  key={m.value}
                  type="radio"
                  name="payment"
                  label={m.label}
                  value={m.value}
                  checked={paymentMethod === m.value}
                  onChange={(e) => setPaymentMethod(e.target.value)}
                  className="mb-2"
                />
              ))}
            </Card.Body>
          </Card>
        </Col>

        {/* SUMMARY */}
        <Col lg={4}>
          <Card>
            <Card.Header><strong>Order Summary</strong></Card.Header>
            <Card.Body>

              {cart.map((item) => (
                <div
                  key={item.productId || item.id}
                  className="d-flex justify-content-between mb-2"
                >
                  <span>{item.name} × {item.quantity}</span>
                  <span>${(item.price * item.quantity).toFixed(2)}</span>
                </div>
              ))}

              <hr />

              <div className="d-flex justify-content-between">
                <strong>Total:</strong>
                <strong>${getCartTotal()}</strong>
              </div>

              <Button
                className="w-100 mt-3"
                onClick={handlePlaceOrder}
                disabled={isLoading}
              >
                {isLoading
                  ? 'Processing...'
                  : paymentMethod === 'CASH_ON_DELIVERY'
                  ? 'Place Order'
                  : 'Place Order & Pay'}
              </Button>

              <Button
                variant="outline-secondary"
                className="w-100 mt-2"
                onClick={() => navigate('/cart')}
              >
                Back to Cart
              </Button>

            </Card.Body>
          </Card>
        </Col>

      </Row>
    </Container>
  )
}

export default CheckoutPage