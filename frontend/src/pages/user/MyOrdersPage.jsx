import React, { useState, useEffect } from 'react'
import { useNavigate } from 'react-router-dom'
import { orderService } from '../../api/orderService'
import LoadingSpinner from '../../components/LoadingSpinner'
import toast from 'react-hot-toast'
import { Eye, RefreshCw, ShoppingBag, ArrowRight } from 'lucide-react'

const statusConfig = {
  PENDING:    { label: 'Pending',    color: '#C8A96E' },
  CONFIRMED:  { label: 'Confirmed',  color: '#6E9EC8' },
  PROCESSING: { label: 'Processing', color: '#6E9EC8' },
  SHIPPED:    { label: 'Shipped',    color: '#6E9EC8' },
  DELIVERED:  { label: 'Delivered',  color: '#5FA37B' },
  COMPLETED:  { label: 'Completed',  color: '#5FA37B' },
  CANCELLED:  { label: 'Cancelled',  color: '#C87B6E' },
}

const paymentConfig = {
  PENDING:  { label: 'Awaiting Payment', dot: '#C8A96E' },
  PAID:     { label: 'Paid',             dot: '#5FA37B' },
  FAILED:   { label: 'Failed',           dot: '#C87B6E' },
  REFUNDED: { label: 'Refunded',         dot: '#9E9E9E' },
}

const styles = `
  @import url('https://fonts.googleapis.com/css2?family=DM+Serif+Display:ital@0;1&family=DM+Sans:wght@300;400;500;600&display=swap');

  .orders-root {
    background: #F9F7F4;
    min-height: 100vh;
    font-family: 'DM Sans', sans-serif;
    color: #1A1814;
  }

  .orders-container {
    max-width: 860px;
    margin: 0 auto;
    padding: 72px 24px 96px;
  }

  .orders-eyebrow {
    font-family: 'DM Sans', sans-serif;
    font-weight: 300;
    font-size: 11px;
    letter-spacing: 0.22em;
    text-transform: uppercase;
    color: #9A9590;
    margin-bottom: 10px;
  }

  .orders-title {
    font-family: 'DM Sans', sans-serif;
    font-weight: 300;
    font-size: 48px;
    letter-spacing: -0.01em;
    line-height: 1;
    color: #1A1814;
    margin: 0 0 6px;
  }

  .orders-count {
    font-size: 12px;
    color: #B5B0A8;
    letter-spacing: 0.06em;
    font-weight: 300;
  }

  .orders-divider {
    height: 1px;
    background: linear-gradient(to right, #D4CFC8, transparent);
    margin: 36px 0;
  }

  /* Order row */
  .order-row {
    display: grid;
    grid-template-columns: 1fr auto auto auto;
    align-items: center;
    gap: 32px;
    padding: 26px 0;
    border-bottom: 1px solid #ECEAE6;
    transition: opacity 0.2s ease;
    position: relative;
  }

  .order-row:first-child {
    border-top: 1px solid #ECEAE6;
  }

  .order-row:hover {
    opacity: 0.8;
  }

  .order-number {
    font-family: 'DM Sans', sans-serif;
    font-weight: 500;
    font-size: 18px;
    color: #1A1814;
    margin-bottom: 5px;
    letter-spacing: 0.01em;
  }

  .order-meta {
    font-size: 11.5px;
    color: #A8A39B;
    font-weight: 300;
    letter-spacing: 0.04em;
    display: flex;
    gap: 14px;
  }

  .order-meta span::before {
    content: '';
    display: inline-block;
    width: 3px;
    height: 3px;
    border-radius: 50%;
    background: #C8C3BB;
    margin-right: 6px;
    vertical-align: middle;
  }

  .order-meta span:first-child::before {
    display: none;
  }

  /* Status pill */
  .status-pill {
    display: inline-flex;
    align-items: center;
    gap: 6px;
    padding: 4px 10px;
    border-radius: 20px;
    font-size: 11px;
    font-weight: 400;
    letter-spacing: 0.08em;
    text-transform: uppercase;
    border: 1px solid currentColor;
    white-space: nowrap;
  }

  .status-dot {
    width: 5px;
    height: 5px;
    border-radius: 50%;
    background: currentColor;
    flex-shrink: 0;
  }

  /* Amount */
  .order-amount {
    font-family: 'DM Sans', sans-serif;
    font-weight: 500;
    font-size: 22px;
    color: #1A1814;
    white-space: nowrap;
    letter-spacing: -0.01em;
  }

  /* Actions */
  .order-actions {
    display: flex;
    align-items: center;
    gap: 8px;
  }

  .btn-icon {
    display: inline-flex;
    align-items: center;
    justify-content: center;
    width: 34px;
    height: 34px;
    border-radius: 50%;
    border: 1px solid #D4CFC8;
    background: transparent;
    color: #6B6660;
    cursor: pointer;
    transition: all 0.18s ease;
  }

  .btn-icon:hover {
    background: #1A1814;
    border-color: #1A1814;
    color: #F9F7F4;
  }

  .btn-refund {
    display: inline-flex;
    align-items: center;
    gap: 5px;
    padding: 6px 13px;
    font-size: 11px;
    letter-spacing: 0.1em;
    text-transform: uppercase;
    border: 1px solid #C8A96E;
    background: transparent;
    color: #C8A96E;
    border-radius: 2px;
    cursor: pointer;
    font-family: 'Jost', sans-serif;
    font-weight: 400;
    transition: all 0.18s ease;
    white-space: nowrap;
  }

  .btn-refund:hover {
    background: #C8A96E;
    color: #fff;
  }

  /* Empty state */
  .empty-state {
    text-align: center;
    padding: 100px 24px;
    display: flex;
    flex-direction: column;
    align-items: center;
    gap: 16px;
  }

  .empty-icon {
    width: 56px;
    height: 56px;
    border: 1px solid #D4CFC8;
    border-radius: 50%;
    display: flex;
    align-items: center;
    justify-content: center;
    color: #C0BAB2;
    margin-bottom: 8px;
  }

  .empty-title {
    font-family: 'Cormorant Garamond', serif;
    font-size: 30px;
    font-weight: 300;
    color: #1A1814;
    margin: 0;
  }

  .empty-sub {
    font-size: 13px;
    color: #B5B0A8;
    font-weight: 300;
    margin: 0;
  }

  .btn-shop {
    display: inline-flex;
    align-items: center;
    gap: 8px;
    margin-top: 8px;
    padding: 10px 24px;
    font-size: 11.5px;
    letter-spacing: 0.14em;
    text-transform: uppercase;
    background: #1A1814;
    color: #F9F7F4;
    border: none;
    border-radius: 2px;
    cursor: pointer;
    font-family: 'Jost', sans-serif;
    font-weight: 400;
    transition: opacity 0.18s ease;
  }

  .btn-shop:hover { opacity: 0.75; }

  /* Pagination */
  .pagination {
    display: flex;
    justify-content: center;
    gap: 4px;
    margin-top: 52px;
  }

  .page-btn {
    width: 34px;
    height: 34px;
    display: inline-flex;
    align-items: center;
    justify-content: center;
    font-size: 12px;
    font-weight: 400;
    letter-spacing: 0.04em;
    border: 1px solid transparent;
    background: transparent;
    color: #9A9590;
    border-radius: 2px;
    cursor: pointer;
    font-family: 'Jost', sans-serif;
    transition: all 0.16s ease;
  }

  .page-btn:hover { color: #1A1814; border-color: #D4CFC8; }

  .page-btn.active {
    background: #1A1814;
    color: #F9F7F4;
    border-color: #1A1814;
  }

  @media (max-width: 640px) {
    .order-row {
      grid-template-columns: 1fr auto;
      gap: 16px;
    }

    .order-statuses, .order-amount-col { display: none; }

    .orders-title { font-size: 36px; }
  }
`

export const MyOrdersPage = () => {
  const [orders, setOrders] = useState([])
  const [isLoading, setIsLoading] = useState(false)
  const [currentPage, setCurrentPage] = useState(0)
  const [totalPages, setTotalPages] = useState(0)
  const [pageSize] = useState(10)

  const navigate = useNavigate()

  useEffect(() => { fetchOrders(0) }, [])

  const fetchOrders = async (page = 0) => {
    setIsLoading(true)
    try {
      const response = await orderService.getMyOrders(page, pageSize)
      const data = response?.data ?? response ?? {}
      setOrders(Array.isArray(data.content) ? data.content : [])
      setTotalPages(data.totalPages || 0)
      setCurrentPage(page)
    } catch {
      toast.error('Failed to load orders')
      setOrders([])
    } finally {
      setIsLoading(false)
    }
  }

  if (isLoading && orders.length === 0) return <LoadingSpinner />

  return (
    <>
      <style>{styles}</style>
      <div className="orders-root">
        <div className="orders-container">

          {/* Header */}
          <div>
            <p className="orders-eyebrow">Account</p>
            <h1 className="orders-title">Order History</h1>
            {orders.length > 0 && (
              <p className="orders-count">{orders.length} order{orders.length !== 1 ? 's' : ''}</p>
            )}
          </div>

          <div className="orders-divider" />

          {/* Empty state */}
          {!isLoading && orders.length === 0 && (
            <div className="empty-state">
              <div className="empty-icon">
                <ShoppingBag size={22} />
              </div>
              <h4 className="empty-title">Nothing here yet</h4>
              <p className="empty-sub">Your completed orders will appear here</p>
              <button className="btn-shop" onClick={() => navigate('/')}>
                Continue Shopping <ArrowRight size={13} />
              </button>
            </div>
          )}

          {/* Orders */}
          {orders.map((order) => {
            const status = statusConfig[order.status] || { label: order.status, color: '#9E9E9E' }
            const payment = paymentConfig[order.paymentStatus] || { label: order.paymentStatus, dot: '#9E9E9E' }
console.log(payment)
            return (
              <div className="order-row" key={order.id}>

                {/* Left: number + meta */}
                <div>
                  <div className="order-number">
                    {order.orderNumber}
                  </div>
                  <div className="order-meta">
                    <span>
                      {order.createdAt
                        ? new Date(order.createdAt).toLocaleDateString('en-US', {
                            year: 'numeric', month: 'long', day: 'numeric'
                          })
                        : '—'}
                    </span>
                    <span>{order.totalItemCount} {order.totalItemCount === 1 ? 'item' : 'items'}</span>
                  </div>
                </div>

                {/* Statuses */}
                <div className="order-statuses" style={{ display: 'flex', flexDirection: 'column', gap: 6, alignItems: 'flex-end' }}>
                  <span className="status-pill" style={{ color: status.color }}>
                    <span className="status-dot" />
                    {status.label}
                  </span>
                  <span style={{ fontSize: 11, color: payment.dot, letterSpacing: '0.06em', display: 'flex', alignItems: 'center', gap: 5 }}>
                    <span style={{ width: 5, height: 5, borderRadius: '50%', background: payment.dot, display: 'inline-block', flexShrink: 0 }} />
                    {payment.label}
                  </span>
                </div>

                {/* Amount */}
                <div className="order-amount-col" style={{ textAlign: 'right' }}>
                  <div className="order-amount">
                    ${order.totalAmount?.toFixed(2)}
                  </div>
                </div>

                {/* Actions */}
                <div className="order-actions">
                  <button
                    className="btn-icon"
                    title="View order"
                    onClick={() => navigate(`/order/${order.id}`)}
                  >
                    <Eye size={14} />
                  </button>

                  {order.status === 'DELIVERED' && (
                    <button
                      className="btn-refund"
                      onClick={() => navigate(`/refund-request/${order.id}`)}
                    >
                      <RefreshCw size={11} />
                      Refund
                    </button>
                  )}
                </div>

              </div>
            )
          })}

          {/* Pagination */}
          {totalPages > 1 && (
            <div className="pagination">
              {Array.from({ length: totalPages }, (_, i) => (
                <button
                  key={i}
                  className={`page-btn${i === currentPage ? ' active' : ''}`}
                  onClick={() => fetchOrders(i)}
                >
                  {i + 1}
                </button>
              ))}
            </div>
          )}

        </div>
      </div>
    </>
  )
}

export default MyOrdersPage