// import React, { useState, useEffect } from 'react'
// import {
//   Container,
//   Row,
//   Col,
//   Card,
//   Badge,
//   Button,
//   Table
// } from 'react-bootstrap'
// import { useParams, useNavigate } from 'react-router-dom'
// import { orderService } from '../../api/orderService'
// import LoadingSpinner from '../../components/LoadingSpinner'
// import toast from 'react-hot-toast'

// const statusColors = {
//   PENDING: 'warning',
//   CONFIRMED: 'info',
//   PROCESSING: 'primary',
//   SHIPPED: 'primary',
//   DELIVERED: 'success',
//   COMPLETED: 'success',
//   CANCELLED: 'danger'
// }

// const paymentStatusColors = {
//   PENDING: 'warning',
//   PAID: 'success',
//   FAILED: 'danger',
//   REFUNDED: 'secondary'
// }

// export const OrderDetailsPage = () => {
//   const { id } = useParams()
//   const navigate = useNavigate()

//   const [order, setOrder] = useState(null)
//   const [isLoading, setIsLoading] = useState(false)

//   useEffect(() => {
//     fetchOrder()
//   }, [id])

//   const fetchOrder = async () => {
//     setIsLoading(true)

//     try {
//       const response = await orderService.getOrderById(id)

//       // ✅ safe unwrap
//       const data = response?.data ?? response ?? null

//       setOrder(data)

//     } catch (error) {
//       console.error(error)
//       toast.error('Failed to load order details')
//       navigate('/orders')
//     } finally {
//       setIsLoading(false)
//     }
//   }

//   if (isLoading) return <LoadingSpinner />

//   if (!order) {
//     return (
//       <Container className="py-5 text-center">
//         <h4>Order not found</h4>
//         <Button onClick={() => navigate('/orders')}>
//           Back to Orders
//         </Button>
//       </Container>
//     )
//   }

//   return (
//     <Container className="py-5">
//       <div className="d-flex justify-content-between mb-4">
//         <h2>Order Details</h2>
//         <Button
//           variant="outline-secondary"
//           onClick={() => navigate('/orders')}
//         >
//           Back
//         </Button>
//       </div>

//       <Row>
//         {/* LEFT SIDE */}
//         <Col lg={8}>
//           <Card className="mb-4">
//             <Card.Body>
//               <Row>
//                 <Col md={6}>
//                   <h6>Order Number</h6>
//                   <p className="mb-3">#{order.orderNumber}</p>

//                   <h6>Date</h6>
//                   <p>
//                     {order.createdAt
//                       ? new Date(order.createdAt).toLocaleDateString()
//                       : '-'}
//                   </p>
//                 </Col>

//                 <Col md={6}>
//                   <h6>Status</h6>
//                   <Badge bg={statusColors[order.status] || 'secondary'}>
//                     {order.status}
//                   </Badge>

//                   <h6 className="mt-3">Payment</h6>
//                   <Badge
//                     bg={
//                       paymentStatusColors[order.paymentStatus] ||
//                       'secondary'
//                     }
//                   >
//                     {order.paymentStatus}
//                   </Badge>
//                 </Col>
//               </Row>
//             </Card.Body>
//           </Card>

//           {/* ITEMS */}
//           <Card className="mb-4">
//             <Card.Header>
//               <h5>Order Items</h5>
//             </Card.Header>

//             <Card.Body>
//               <Table>
//                 <thead>
//                   <tr>
//                     <th>Product</th>
//                     <th>Price</th>
//                     <th>Qty</th>
//                     <th>Total</th>
//                   </tr>
//                 </thead>

//                 <tbody>
//                   {order.items?.map((item) => (
//                     <tr key={item.id}>
//                       <td>
//                         <strong>{item.productName}</strong>
//                       </td>

//                       <td>
//                         ${item.unitPrice?.toFixed(2)}
//                       </td>

//                       <td>{item.quantity}</td>

//                       <td>
//                         ${(item.unitPrice * item.quantity)?.toFixed(2)}
//                       </td>
//                     </tr>
//                   ))}
//                 </tbody>
//               </Table>
//             </Card.Body>
//           </Card>

//           {/* SHIPPING */}
//           {order.shippingAddress && (
//             <Card>
//               <Card.Header>
//                 <h5>Shipping Address</h5>
//               </Card.Header>

//               <Card.Body>
//                 <p>
//                   {order.shippingAddress.recipientName}
//                 </p>
//                 <p>
//                   {order.shippingAddress.addressLine1}
//                 </p>
//                 <p>
//                   {order.shippingAddress.city},{' '}
//                   {order.shippingAddress.country}
//                 </p>
//                 <p>{order.shippingAddress.phone}</p>
//               </Card.Body>
//             </Card>
//           )}
//         </Col>

//         {/* RIGHT SIDE */}
//         <Col lg={4}>
//           <Card className="sticky-top">
//             <Card.Header>
//               <h5>Summary</h5>
//             </Card.Header>

//             <Card.Body>
//               <div className="d-flex justify-content-between">
//                 <span>Total Items</span>
//                 <span>{order.totalItemCount}</span>
//               </div>

//               <div className="d-flex justify-content-between mt-2">
//                 <span>Total Amount</span>
//                 <strong>
//                   ${order.totalAmount?.toFixed(2)}
//                 </strong>
//               </div>

//               <hr />

//               <div>
//                 <span>Payment Method:</span>
//                 <p>{order.paymentMethod}</p>
//               </div>

//               {order.status === 'DELIVERED' && (
//                 <Button
//                   className="w-100"
//                   variant="warning"
//                   onClick={() =>
//                     navigate(`/refund-request/${order.id}`)
//                   }
//                 >
//                   Request Refund
//                 </Button>
//               )}
//             </Card.Body>
//           </Card>
//         </Col>
//       </Row>
//     </Container>
//   )
// }

// export default OrderDetailsPage


import React, { useState, useEffect } from 'react'
import { useParams, useNavigate } from 'react-router-dom'
import { orderService } from '../../api/orderService'
import { paymentService } from '../../api/paymentService'
import LoadingSpinner from '../../components/LoadingSpinner'
import toast from 'react-hot-toast'
import { ArrowLeft, RefreshCw, CreditCard, MapPin, Package, AlertCircle } from 'lucide-react'

const statusConfig = {
  PENDING:    { label: 'Pending',    color: '#b35509' },
  CONFIRMED:  { label: 'Confirmed',  color: '#2E7D57' },
  PROCESSING: { label: 'Processing', color: '#1A5FAB' },
  SHIPPED:    { label: 'Shipped',    color: '#1A5FAB' },
  DELIVERED:  { label: 'Delivered',  color: '#2E7D57' },
  COMPLETED:  { label: 'Completed',  color: '#2E7D57' },
  CANCELLED:  { label: 'Cancelled',  color: '#C0392B' },
}

const paymentStatusConfig = {
  PENDING:  { label: 'Pending',  color: '#b35509' },
  SUCCESS:  { label: 'Paid',     color: '#2E7D57' },
  PAID:     { label: 'Paid',     color: '#2E7D57' },
  FAILED:   { label: 'Failed',   color: '#C0392B' },
  REFUNDED: { label: 'Refunded', color: '#555' },
  EXPIRED:  { label: 'Expired',  color: '#888' },
}

const styles = `
  @import url('https://fonts.googleapis.com/css2?family=DM+Sans:ital,opsz,wght@0,9..40,300;0,9..40,400;0,9..40,500;0,9..40,600;1,9..40,400&display=swap');

  *, *::before, *::after { box-sizing: border-box; margin: 0; padding: 0; }

  .od-root {
    background: #F5F5F5;
    min-height: 100vh;
    font-family: 'DM Sans', sans-serif;
    color: #111;
  }

  .od-wrap {
    max-width: 1040px;
    margin: 0 auto;
    padding: 52px 24px 96px;
  }

  /* Back button */
  .od-back {
    display: inline-flex;
    align-items: center;
    gap: 7px;
    font-size: 13px;
    font-weight: 500;
    color: #555;
    background: none;
    border: none;
    cursor: pointer;
    padding: 0;
    margin-bottom: 32px;
    letter-spacing: 0.01em;
    transition: color 0.15s;
  }
  .od-back:hover { color: #b35509; }

  /* Page title row */
  .od-header {
    display: flex;
    align-items: baseline;
    gap: 14px;
    margin-bottom: 8px;
  }

  .od-title {
    font-size: 28px;
    font-weight: 600;
    letter-spacing: -0.02em;
    color: #111;
  }

  .od-order-num {
    font-size: 14px;
    font-weight: 400;
    color: #888;
    letter-spacing: 0.02em;
  }

  .od-date {
    font-size: 13px;
    color: #AAA;
    margin-bottom: 32px;
  }

  /* Pending payment banner */
  .od-banner {
    display: flex;
    align-items: center;
    justify-content: space-between;
    background: #FFF8F2;
    border: 1px solid #b35509;
    border-left: 4px solid #b35509;
    border-radius: 6px;
    padding: 14px 18px;
    margin-bottom: 28px;
    gap: 12px;
  }

  .od-banner-text {
    display: flex;
    align-items: center;
    gap: 9px;
    font-size: 13.5px;
    font-weight: 500;
    color: #b35509;
  }

  .od-banner-btn {
    flex-shrink: 0;
    padding: 7px 18px;
    font-size: 13px;
    font-weight: 600;
    font-family: 'DM Sans', sans-serif;
    background: #b35509;
    color: #fff;
    border: none;
    border-radius: 5px;
    cursor: pointer;
    transition: opacity 0.15s;
    letter-spacing: 0.01em;
  }
  .od-banner-btn:hover { opacity: 0.85; }
  .od-banner-btn:disabled { opacity: 0.55; cursor: not-allowed; }

  /* Layout */
  .od-layout {
    display: grid;
    grid-template-columns: 1fr 300px;
    gap: 24px;
    align-items: start;
  }

  @media (max-width: 760px) {
    .od-layout { grid-template-columns: 1fr; }
    .od-sidebar { position: static !important; }
  }

  /* Cards */
  .od-card {
    background: #fff;
    border-radius: 10px;
    border: 1px solid #E8E8E8;
    overflow: hidden;
    margin-bottom: 20px;
  }

  .od-card-header {
    padding: 16px 22px;
    border-bottom: 1px solid #F0F0F0;
    display: flex;
    align-items: center;
    gap: 9px;
  }

  .od-card-header-icon {
    color: #b35509;
  }

  .od-card-title {
    font-size: 13px;
    font-weight: 600;
    text-transform: uppercase;
    letter-spacing: 0.09em;
    color: #111;
  }

  .od-card-body {
    padding: 22px;
  }

  /* Status grid */
  .od-status-grid {
    display: grid;
    grid-template-columns: 1fr 1fr;
    gap: 20px;
  }

  .od-field-label {
    font-size: 11px;
    font-weight: 600;
    text-transform: uppercase;
    letter-spacing: 0.1em;
    color: #AAA;
    margin-bottom: 7px;
  }

  .od-field-value {
    font-size: 15px;
    font-weight: 500;
    color: #111;
  }

  .od-status-badge {
    display: inline-flex;
    align-items: center;
    gap: 6px;
    padding: 4px 10px;
    border-radius: 20px;
    font-size: 12px;
    font-weight: 500;
    letter-spacing: 0.04em;
    border: 1.5px solid;
  }

  .od-status-dot {
    width: 6px;
    height: 6px;
    border-radius: 50%;
    background: currentColor;
    flex-shrink: 0;
  }

  /* Items table */
  .od-table {
    width: 100%;
    border-collapse: collapse;
  }

  .od-table thead th {
    font-size: 11px;
    font-weight: 600;
    text-transform: uppercase;
    letter-spacing: 0.1em;
    color: #AAA;
    padding: 0 0 14px;
    text-align: left;
    border-bottom: 1px solid #F0F0F0;
  }

  .od-table thead th:last-child,
  .od-table tbody td:last-child { text-align: right; }

  .od-table tbody tr { border-bottom: 1px solid #F7F7F7; }
  .od-table tbody tr:last-child { border-bottom: none; }

  .od-table tbody td {
    padding: 14px 0;
    font-size: 14px;
    color: #333;
    vertical-align: middle;
  }

  .od-item-name {
    font-weight: 500;
    color: #111;
    font-size: 14.5px;
  }

  .od-item-unit {
    font-size: 12px;
    color: #AAA;
    margin-top: 2px;
  }

  .od-qty-badge {
    display: inline-flex;
    align-items: center;
    justify-content: center;
    min-width: 28px;
    height: 24px;
    padding: 0 6px;
    background: #F5F5F5;
    border-radius: 4px;
    font-size: 13px;
    font-weight: 500;
    color: #333;
  }

  .od-item-total {
    font-weight: 600;
    font-size: 14.5px;
    color: #111;
  }

  /* Shipping */
  .od-address-line {
    font-size: 14.5px;
    color: #333;
    line-height: 1.75;
  }

  .od-address-name {
    font-weight: 600;
    color: #111;
    font-size: 15px;
  }

  /* Sidebar */
  .od-sidebar {
    position: sticky;
    top: 24px;
  }

  .od-summary-row {
    display: flex;
    align-items: center;
    justify-content: space-between;
    padding: 9px 0;
  }

  .od-summary-row + .od-summary-row {
    border-top: 1px solid #F5F5F5;
  }

  .od-summary-label {
    font-size: 13px;
    color: #777;
  }

  .od-summary-value {
    font-size: 13.5px;
    font-weight: 500;
    color: #111;
  }

  .od-summary-divider {
    height: 1px;
    background: #EBEBEB;
    margin: 14px 0;
  }

  .od-total-row {
    display: flex;
    align-items: center;
    justify-content: space-between;
  }

  .od-total-label {
    font-size: 14px;
    font-weight: 600;
    color: #111;
  }

  .od-total-amount {
    font-size: 22px;
    font-weight: 600;
    color: #111;
    letter-spacing: -0.03em;
  }

  .od-payment-method {
    margin-top: 18px;
    padding-top: 16px;
    border-top: 1px solid #F0F0F0;
  }

  .od-payment-ref {
    margin-top: 10px;
    padding: 10px 12px;
    background: #F9F9F9;
    border-radius: 6px;
    font-size: 11.5px;
    color: #888;
    word-break: break-all;
    line-height: 1.6;
  }

  /* CTA buttons */
  .od-btn-primary {
    width: 100%;
    padding: 12px;
    font-size: 14px;
    font-weight: 600;
    font-family: 'DM Sans', sans-serif;
    background: #b35509;
    color: #fff;
    border: none;
    border-radius: 7px;
    cursor: pointer;
    margin-top: 16px;
    transition: opacity 0.15s;
    letter-spacing: 0.01em;
  }
  .od-btn-primary:hover { opacity: 0.87; }
  .od-btn-primary:disabled { opacity: 0.5; cursor: not-allowed; }

  .od-btn-outline {
    width: 100%;
    padding: 11px;
    font-size: 13.5px;
    font-weight: 500;
    font-family: 'DM Sans', sans-serif;
    background: transparent;
    color: #b35509;
    border: 1.5px solid #b35509;
    border-radius: 7px;
    cursor: pointer;
    margin-top: 10px;
    display: flex;
    align-items: center;
    justify-content: center;
    gap: 7px;
    transition: all 0.15s;
  }
  .od-btn-outline:hover { background: #b35509; color: #fff; }

  /* Not found */
  .od-notfound {
    text-align: center;
    padding: 100px 24px;
  }
  .od-notfound h4 {
    font-size: 22px;
    font-weight: 600;
    margin-bottom: 12px;
  }
`

export const OrderDetailsPage = () => {
  const { id } = useParams()
  const navigate = useNavigate()

  const [order, setOrder] = useState(null)
  const [isLoading, setIsLoading] = useState(false)
  const [isPayingNow, setIsPayingNow] = useState(false)

  useEffect(() => { fetchOrder() }, [id])

  const fetchOrder = async () => {
    setIsLoading(true)
    try {
      const response = await orderService.getOrderById(id)
      setOrder(response?.data ?? response ?? null)
    } catch (error) {
      toast.error('Failed to load order details')
      navigate('/orders')
    } finally {
      setIsLoading(false)
    }
  }

  const handlePayNow = async () => {
    setIsPayingNow(true)
    try {
      const payRes = await paymentService.initiatePayment({
        orderId: order.id,
        orderNumber: order.orderNumber,
        amount: order.totalAmount,
        currency: 'USD',
        paymentMethod: order.paymentMethod,
      })
      const paymentReference = payRes?.data?.paymentReference ?? payRes?.paymentReference
      navigate(`/payment/${order.id}`, { state: { order, paymentReference } })
    } catch {
      toast.error('Could not re-initiate payment. Please try again.')
    } finally {
      setIsPayingNow(false)
    }
  }

  if (isLoading) return <LoadingSpinner />

  if (!order) {
    return (
      <>
        <style>{styles}</style>
        <div className="od-root">
          <div className="od-wrap">
            <div className="od-notfound">
              <h4>Order not found</h4>
              <button className="od-btn-primary" style={{ width: 'auto', padding: '10px 24px' }} onClick={() => navigate('/orders')}>
                Back to Orders
              </button>
            </div>
          </div>
        </div>
      </>
    )
  }

  const isPendingOnlinePayment =
    order.status === 'PENDING' && order.paymentMethod !== 'CASH_ON_DELIVERY'

  const status = statusConfig[order.status] || { label: order.status, color: '#888' }
  const payment = paymentStatusConfig[order.paymentStatus] || { label: order.paymentStatus, color: '#888' }

  return (
    <>
      <style>{styles}</style>
      <div className="od-root">
        <div className="od-wrap">

          {/* Back */}
          <button className="od-back" onClick={() => navigate('/orders')}>
            <ArrowLeft size={15} /> Back to Orders
          </button>

          {/* Title */}
          <div className="od-header">
            <h1 className="od-title">Order Details</h1>
            <span className="od-order-num">#{order.orderNumber}</span>
          </div>
          <p className="od-date">
            {order.createdAt
              ? new Date(order.createdAt).toLocaleDateString('en-US', { year: 'numeric', month: 'long', day: 'numeric' })
              : '—'}
          </p>

          {/* Pending banner */}
          {isPendingOnlinePayment && (
            <div className="od-banner">
              <div className="od-banner-text">
                <AlertCircle size={16} />
                Payment is pending for this order
              </div>
              <button className="od-banner-btn" onClick={handlePayNow} disabled={isPayingNow}>
                {isPayingNow ? 'Redirecting…' : 'Pay Now'}
              </button>
            </div>
          )}

          {/* Layout */}
          <div className="od-layout">

            {/* LEFT */}
            <div>

              {/* Status card */}
              <div className="od-card">
                <div className="od-card-header">
                  <Package size={15} className="od-card-header-icon" />
                  <span className="od-card-title">Order Status</span>
                </div>
                <div className="od-card-body">
                  <div className="od-status-grid">
                    <div>
                      <div className="od-field-label">Order Status</div>
                      <span
                        className="od-status-badge"
                        style={{ color: status.color, borderColor: status.color + '44', background: status.color + '10' }}
                      >
                        <span className="od-status-dot" />
                        {status.label}
                      </span>
                    </div>
                    <div>
                      <div className="od-field-label">Payment Status</div>
                      <span
                        className="od-status-badge"
                        style={{ color: payment.color, borderColor: payment.color + '44', background: payment.color + '10' }}
                      >
                        <span className="od-status-dot" />
                        {payment.label}
                      </span>
                    </div>
                  </div>
                </div>
              </div>

              {/* Items */}
              <div className="od-card">
                <div className="od-card-header">
                  <Package size={15} className="od-card-header-icon" />
                  <span className="od-card-title">Items</span>
                </div>
                <div className="od-card-body">
                  <table className="od-table">
                    <thead>
                      <tr>
                        <th>Product</th>
                        <th>Unit Price</th>
                        <th style={{ textAlign: 'center' }}>Qty</th>
                        <th>Total</th>
                      </tr>
                    </thead>
                    <tbody>
                      {order.items?.map((item) => (
                        <tr key={item.id}>
                          <td>
                            <div className="od-item-name">{item.productName}</div>
                          </td>
                          <td>
                            <span style={{ fontSize: 14, color: '#555' }}>
                              ${item.unitPrice?.toFixed(2)}
                            </span>
                          </td>
                          <td style={{ textAlign: 'center' }}>
                            <span className="od-qty-badge">{item.quantity}</span>
                          </td>
                          <td>
                            <span className="od-item-total">
                              ${(item.unitPrice * item.quantity)?.toFixed(2)}
                            </span>
                          </td>
                        </tr>
                      ))}
                    </tbody>
                  </table>
                </div>
              </div>

              {/* Shipping */}
              {order.shippingAddress && (
                <div className="od-card">
                  <div className="od-card-header">
                    <MapPin size={15} className="od-card-header-icon" />
                    <span className="od-card-title">Shipping Address</span>
                  </div>
                  <div className="od-card-body">
                    <div className="od-address-name">{order.shippingAddress.recipientName}</div>
                    <div className="od-address-line">{order.shippingAddress.addressLine1}</div>
                    <div className="od-address-line">
                      {order.shippingAddress.city}, {order.shippingAddress.country}
                    </div>
                    <div className="od-address-line" style={{ color: '#888' }}>
                      {order.shippingAddress.phone}
                    </div>
                  </div>
                </div>
              )}
            </div>

            {/* SIDEBAR */}
            <div className="od-sidebar">
              <div className="od-card">
                <div className="od-card-header">
                  <CreditCard size={15} className="od-card-header-icon" />
                  <span className="od-card-title">Summary</span>
                </div>
                <div className="od-card-body">

                  <div className="od-summary-row">
                    <span className="od-summary-label">Items</span>
                    <span className="od-summary-value">{order.totalItemCount}</span>
                  </div>

                  <div className="od-summary-divider" />

                  <div className="od-total-row">
                    <span className="od-total-label">Total</span>
                    <span className="od-total-amount">${order.totalAmount?.toFixed(2)}</span>
                  </div>

                  {/* Payment method */}
{/*                   <div className="od-payment-method"> */}
{/*                     <div className="od-field-label">Payment Method</div> */}
{/*                     <div style={{ fontSize: 14, fontWeight: 500, color: '#111', marginTop: 5 }}> */}
{/*                       {order.paymentMethod?.replace(/_/g, ' ')} */}
{/*                     </div> */}
{/*                   </div> */}

                  {order.paymentReference && (
                    <div className="od-payment-ref">
                      Ref: {order.paymentReference}
                    </div>
                  )}

                  {/* CTAs */}
                  {isPendingOnlinePayment && (
                    <button className="od-btn-primary" onClick={handlePayNow} disabled={isPayingNow}>
                      {isPayingNow ? 'Redirecting…' : 'Complete Payment'}
                    </button>
                  )}

                  {order.status === 'DELIVERED' && (
                    <button
                      className="od-btn-outline"
                      onClick={() => navigate(`/refund-request/${order.id}`)}
                    >
                      <RefreshCw size={14} />
                      Request Refund
                    </button>
                  )}

                </div>
              </div>
            </div>

          </div>
        </div>
      </div>
    </>
  )
}

export default OrderDetailsPage