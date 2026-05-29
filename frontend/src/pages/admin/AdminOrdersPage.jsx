import React, { useState, useEffect } from 'react'
import {
  Container,
  Card,
  Badge,
  Button,
  Form,
  Row,
  Col,
  Pagination
} from 'react-bootstrap'

import {
  Package,
  Clock,
  CheckCircle,
  Truck,
  XCircle,
  Settings,
  ShoppingCart,
  DollarSign
} from 'lucide-react'

import { orderService } from '../../api/orderService'
import LoadingSpinner from '../../components/LoadingSpinner'
import toast from 'react-hot-toast'

const orderStatuses = [
  'PENDING',
  'CONFIRMED',
  'PROCESSING',
  'SHIPPED',
  'DELIVERED',
  'COMPLETED',
  'CANCELLED'
]

const statusColors = {
  PENDING: 'warning',
  CONFIRMED: 'info',
  PROCESSING: 'primary',
  SHIPPED: 'primary',
  DELIVERED: 'success',
  COMPLETED: 'success',
  CANCELLED: 'danger'
}

const getStatusIcon = (status) => {
  switch (status) {
    case 'PENDING': return <Clock size={18} />
    case 'CONFIRMED': return <CheckCircle size={18} />
    case 'PROCESSING': return <Settings size={18} />
    case 'SHIPPED': return <Truck size={18} />
    case 'DELIVERED': return <Package size={18} />
    case 'COMPLETED': return <CheckCircle size={18} />
    case 'CANCELLED': return <XCircle size={18} />
    default: return <ShoppingCart size={18} />
  }
}

export const AdminOrdersPage = () => {

  const [orders, setOrders] = useState([])
  const [isLoading, setIsLoading] = useState(false)
  const [currentPage, setCurrentPage] = useState(0)
  const [totalPages, setTotalPages] = useState(0)
  const [statusFilter, setStatusFilter] = useState('')
  const [pageSize] = useState(10)

  const [editingOrderId, setEditingOrderId] = useState(null)
  const [newStatus, setNewStatus] = useState('')

  const [searchOrderId, setSearchOrderId] = useState('')
  const [isSearching, setIsSearching] = useState(false)

  useEffect(() => {
    fetchOrders(0, '')
  }, [])

  const fetchOrders = async (page = 0, status = '') => {
    setIsLoading(true)

    try {
      const response = await orderService.getAllOrders(page, pageSize, status)
      const data = response?.data ?? response ?? {}

      setOrders(data.content || [])
      setTotalPages(data.totalPages || 1)
      setCurrentPage(page)

    } catch {
      toast.error('Failed to load orders')
    } finally {
      setIsLoading(false)
    }
  }

  const handleUpdateStatus = async (orderId, status) => {
    try {
      await orderService.updateOrderStatus(orderId, status)
      toast.success('Order updated')
      setEditingOrderId(null)
      fetchOrders(currentPage, statusFilter)
    } catch {
      toast.error('Update failed')
    }
  }

const handleSearchById = async () => {
  if (!searchOrderId) {
    fetchOrders(0, statusFilter)
    return
  }

  setIsLoading(true)
  setIsSearching(true)

  try {
    const response = await orderService.getOrderById(searchOrderId)
    const order = response?.data ?? response

    if (order && order.id) {
      setOrders([order])
      setTotalPages(1)
      setCurrentPage(0)
    } else {
      setOrders([])
      toast.error('Order not found')
    }

  } catch {
    toast.error('Order not found')
    setOrders([])
  } finally {
    setIsLoading(false)
    setIsSearching(false)
  }
}

const clearSearch = () => {
  setSearchOrderId('')
  fetchOrders(0, statusFilter)
}

  if (isLoading && orders.length === 0) return <LoadingSpinner />

  return (
    <Container fluid className="py-4" style={{ background: '#f6f7fb', minHeight: '100vh' }}>

      {/* HEADER */}
      <div className="mb-4">
        <h3 className="mb-1">
          <Package className="me-2" size={22} />
          Order Management
        </h3>
        <small className="text-muted">
          Track and manage customer orders
        </small>
      </div>

 <Row className="mb-3">

  {/* LEFT: SEARCH */}
  <Col lg={6}>
    <Card className="border-0 shadow-sm h-100">
      <Card.Body>

        <Row className="g-2 align-items-center">

          <Col md={8}>
            <Form.Control
              placeholder="Search by Order ID..."
              value={searchOrderId}
              onChange={(e) => setSearchOrderId(e.target.value)}
            />
          </Col>

          <Col md={2}>
            <Button
              className="w-110"
              onClick={handleSearchById}
            >
              Search
            </Button>
          </Col>

          <Col md={2}>
            <Button
              variant="secondary"
              className="w-100"
              onClick={clearSearch}
            >
              Clear
            </Button>
          </Col>

        </Row>

      </Card.Body>
    </Card>
  </Col>

  {/* RIGHT: FILTER */}
  <Col lg={6}>
    <Card className="border-0 shadow-sm h-100">
      <Card.Body>

        <Form.Select
          value={statusFilter}
          onChange={(e) => {
            setStatusFilter(e.target.value)
            fetchOrders(0, e.target.value)
          }}
        >
          <option value="">All Orders</option>
          {orderStatuses.map(s => (
            <option key={s} value={s}>{s}</option>
          ))}
        </Form.Select>

      </Card.Body>
    </Card>
  </Col>

</Row>


      {/* ORDER CARDS */}
      <Row>
        {orders.map(order => (
          <Col lg={6} key={order.id || order.orderId} className="mb-3">

            <Card className="border-0 shadow-sm h-100 hover-card">

              <Card.Body>
 {/* {console.log("ORDER:", order)} */}
                {/* HEADER */}
                <div className="d-flex justify-content-between align-items-center mb-2">

                  <div className="d-flex align-items-center gap-2">
                    <ShoppingCart size={18} />
                    <div>
                      <h6 className="mb-0">
                        Order #{order.orderId || order.id}
                      </h6>
                      <small className="text-muted">
                        {new Date(order.createdAt).toLocaleDateString()}
                      </small>
                    </div>
                  </div>

                  <Badge bg={statusColors[order.status]} className="d-flex align-items-center gap-1">
                    {getStatusIcon(order.status)}
                    {order.status}
                  </Badge>

                </div>

                <hr />

                {/* INFO */}
  {/* INFO */}
<Row className="mb-2">
  <Col>
    <small className="text-muted">Items</small>
    <div className="fw-bold">
      {order.totalItemCount || 0}
    </div>
  </Col>

  <Col>
    <small className="text-muted">Total</small>
    <div className="fw-bold d-flex align-items-center gap-1">
      <DollarSign size={14} />
      {Number(order.totalAmount || 0).toFixed(2)}
    </div>
  </Col>

  <Col>
    <small className="text-muted">Order Number</small>
    <div className="fw-bold">
      {order.orderNumber || 'N/A'}
    </div>
  </Col>
</Row>

                {/* ACTIONS */}
                <div className="d-flex justify-content-between align-items-center mt-3">

                  {editingOrderId === order.id ? (
                    <div className="d-flex gap-2 w-100">

                      <Form.Select
                        size="sm"
                        value={newStatus}
                        onChange={(e) => setNewStatus(e.target.value)}
                      >
                        {orderStatuses.map(s => (
                          <option key={s}>{s}</option>
                        ))}
                      </Form.Select>

                      <Button
                        size="sm"
                        variant="success"
                        onClick={() => handleUpdateStatus(order.id, newStatus)}
                      >
                        Save
                      </Button>

                      <Button
                        size="sm"
                        variant="secondary"
                        onClick={() => setEditingOrderId(null)}
                      >
                        Cancel
                      </Button>

                    </div>
                  ) : (
                    <>
                      <Button
                        size="sm"
                        variant="outline-primary"
                        onClick={() => {
                          setEditingOrderId(order.id)
                          setNewStatus(order.status)
                        }}
                      >
                        <Settings size={14} className="me-1" />
                        Update
                      </Button>

                      <small className="text-muted">
                        Manage status
                      </small>
                    </>
                  )}

                </div>

              </Card.Body>
            </Card>

          </Col>
        ))}
      </Row>

      {/* PAGINATION */}
      {!searchOrderId && totalPages > 1 && (
        <div className="d-flex justify-content-center mt-4">
          <Pagination>
            {Array.from({ length: totalPages }, (_, i) => (
              <Pagination.Item
                key={i}
                active={i === currentPage}
                onClick={() => fetchOrders(i, statusFilter)}
              >
                {i + 1}
              </Pagination.Item>
            ))}
          </Pagination>
        </div>
      )}

    </Container>
  )
}

export default AdminOrdersPage