// import React, { useState, useEffect } from 'react'
// import { Container, Table, Button, Badge, Modal, Form, Pagination } from 'react-bootstrap'
// import { refundService } from '../../api/refundService'
// import LoadingSpinner from '../../components/LoadingSpinner'
// import toast from 'react-hot-toast'

// const refundReasonMap = {
//   DAMAGED_PRODUCT: 'Damaged Product',
//   WRONG_ITEM: 'Wrong Item Received',
//   NOT_DELIVERED: 'Not Delivered',
//   CUSTOMER_CANCELLED: 'Customer Changed Mind',
//   OTHER: 'Other'
// }

// export const AdminRefundsPage = () => {
//   const [refunds, setRefunds] = useState([])
//   const [isLoading, setIsLoading] = useState(false)
//   const [showModal, setShowModal] = useState(false)
//   const [selectedRefund, setSelectedRefund] = useState(null)
//   const [currentPage, setCurrentPage] = useState(0)
//   const [totalPages, setTotalPages] = useState(0)
//   const [pageSize] = useState(10)
//   const [resolution, setResolution] = useState({
//     status: 'APPROVED',
//     approvedAmount: '',
//     adminNotes: ''
//   })

//   useEffect(() => {
//     fetchRefunds(0)
//   }, [])

//   const fetchRefunds = async (page = 0) => {
//     setIsLoading(true)
//     try {
//       const response = await refundService.getPendingRefunds(page, pageSize)
//       setRefunds(response.content || response.data || [])
//       setTotalPages(response.totalPages || 1)
//       setCurrentPage(page)
//     } catch (error) {
//       toast.error('Failed to load refunds')
//     } finally {
//       setIsLoading(false)
//     }
//   }

//   const handlePageChange = (pageNumber) => {
//     fetchRefunds(pageNumber)
//   }

//   const handleResolveClick = (refund) => {
//     setSelectedRefund(refund)
//     setResolution({
//       status: 'APPROVED',
//       approvedAmount: refund.requestedAmount || '',
//       adminNotes: ''
//     })
//     setShowModal(true)
//   }

//   const handleResolveRefund = async () => {
//     if (!selectedRefund) return

//     try {
//       await refundService.resolveRefund(selectedRefund.refundId || selectedRefund.id, {
//         status: resolution.status,
//         approvedAmount: parseFloat(resolution.approvedAmount),
//         adminNotes: resolution.adminNotes
//       })

//       toast.success('Refund resolved successfully!')
//       setShowModal(false)
//       fetchRefunds(currentPage)
//     } catch (error) {
//       toast.error(error?.message || 'Failed to resolve refund')
//     }
//   }

//   if (isLoading && refunds.length === 0) {
//     return <LoadingSpinner />
//   }

//   if (refunds.length === 0) {
//     return (
//       <Container fluid className="py-4">
//         <h2 className="mb-4">Manage Refunds</h2>
//         <div className="alert alert-info">
//           <p className="mb-0">No pending refunds at the moment.</p>
//         </div>
//       </Container>
//     )
//   }

//   const paginationItems = []
//   for (let number = 0; number < totalPages; number++) {
//     paginationItems.push(
//       <Pagination.Item
//         key={number}
//         active={number === currentPage}
//         onClick={() => handlePageChange(number)}
//       >
//         {number + 1}
//       </Pagination.Item>
//     )
//   }

//   return (
//     <Container fluid className="py-4">
//       <h2 className="mb-4">Manage Refunds ({refunds.length} Pending)</h2>

//       <div className="card">
//         <div className="table-responsive">
//           <Table hover className="mb-0">
//             <thead className="bg-light">
//               <tr>
//                 <th>Refund ID</th>
//                 <th>Order ID</th>
//                 <th>Reason</th>
//                 <th>Requested Amount</th>
//                 <th>Status</th>
//                 <th>Date</th>
//                 <th>Actions</th>
//               </tr>
//             </thead>
//             <tbody>
//               {refunds.map((refund) => (
//                 <tr key={refund.refundId || refund.id}>
//                   <td>#{refund.refundId || refund.id}</td>
//                   <td>#{refund.orderId}</td>
//                   <td>{refundReasonMap[refund.reasonType] || refund.reasonType}</td>
//                   <td>${refund.requestedAmount?.toFixed(2)}</td>
//                   <td>
//                     <Badge bg="warning">{refund.status || 'PENDING'}</Badge>
//                   </td>
//                   <td>{new Date(refund.createdAt).toLocaleDateString()}</td>
//                   <td>
//                     <Button
//                       variant="success"
//                       size="sm"
//                       onClick={() => handleResolveClick(refund)}
//                     >
//                       Resolve
//                     </Button>
//                   </td>
//                 </tr>
//               ))}
//             </tbody>
//           </Table>
//         </div>
//       </div>

//       {totalPages > 1 && (
//         <div className="d-flex justify-content-center mt-4">
//           <Pagination>{paginationItems}</Pagination>
//         </div>
//       )}

//       {/* Refund Resolution Modal */}
//       <Modal show={showModal} onHide={() => setShowModal(false)} size="lg">
//         <Modal.Header closeButton>
//           <Modal.Title>Resolve Refund Request</Modal.Title>
//         </Modal.Header>
//         <Modal.Body>
//           {selectedRefund && (
//             <Form>
//               <Form.Group className="mb-3">
//                 <Form.Label>Refund ID</Form.Label>
//                 <Form.Control
//                   type="text"
//                   value={`#${selectedRefund.refundId || selectedRefund.id}`}
//                   disabled
//                 />
//               </Form.Group>

//               <Form.Group className="mb-3">
//                 <Form.Label>Order ID</Form.Label>
//                 <Form.Control
//                   type="text"
//                   value={`#${selectedRefund.orderId}`}
//                   disabled
//                 />
//               </Form.Group>

//               <Form.Group className="mb-3">
//                 <Form.Label>Reason</Form.Label>
//                 <Form.Control
//                   type="text"
//                   value={refundReasonMap[selectedRefund.reasonType] || selectedRefund.reasonType}
//                   disabled
//                 />
//               </Form.Group>

//               <Form.Group className="mb-3">
//                 <Form.Label>Reason Details</Form.Label>
//                 <Form.Control
//                   as="textarea"
//                   rows={3}
//                   value={selectedRefund.reasonDetail}
//                   disabled
//                 />
//               </Form.Group>

//               <Form.Group className="mb-3">
//                 <Form.Label>Requested Amount</Form.Label>
//                 <Form.Control
//                   type="number"
//                   step="0.01"
//                   value={selectedRefund.requestedAmount}
//                   disabled
//                 />
//               </Form.Group>

//               <Form.Group className="mb-3">
//                 <Form.Label>Decision</Form.Label>
//                 <Form.Select
//                   value={resolution.status}
//                   onChange={(e) =>
//                     setResolution({ ...resolution, status: e.target.value })
//                   }
//                 >
//                   <option value="APPROVED">Approve</option>
//                   <option value="REJECTED">Reject</option>
//                 </Form.Select>
//               </Form.Group>

//               <Form.Group className="mb-3">
//                 <Form.Label>Approved Amount</Form.Label>
//                 <Form.Control
//                   type="number"
//                   step="0.01"
//                   value={resolution.approvedAmount}
//                   onChange={(e) =>
//                     setResolution({ ...resolution, approvedAmount: e.target.value })
//                   }
//                   disabled={resolution.status === 'REJECTED'}
//                 />
//               </Form.Group>

//               <Form.Group className="mb-3">
//                 <Form.Label>Admin Notes</Form.Label>
//                 <Form.Control
//                   as="textarea"
//                   rows={3}
//                   value={resolution.adminNotes}
//                   onChange={(e) =>
//                     setResolution({ ...resolution, adminNotes: e.target.value })
//                   }
//                   placeholder="Add notes for the customer..."
//                 />
//               </Form.Group>
//             </Form>
//           )}
//         </Modal.Body>
//         <Modal.Footer>
//           <Button variant="secondary" onClick={() => setShowModal(false)}>
//             Close
//           </Button>
//           <Button variant="primary" onClick={handleResolveRefund}>
//             Submit Decision
//           </Button>
//         </Modal.Footer>
//       </Modal>
//     </Container>
//   )
// }

// export default AdminRefundsPage

import React, { useState, useEffect } from 'react'
import {
  Container,
  Card,
  Button,
  Badge,
  Modal,
  Pagination,
  Row,
  Col
} from 'react-bootstrap'

import {
  RefreshCw,
  CreditCard,
  AlertTriangle,
  CheckCircle,
  XCircle,
  DollarSign,
  FileText
} from 'lucide-react'

import { refundService } from '../../api/refundService'
import LoadingSpinner from '../../components/LoadingSpinner'
import toast from 'react-hot-toast'

export const AdminRefundsPage = () => {

  const [refunds, setRefunds] = useState([])
  const [isLoading, setIsLoading] = useState(false)
  const [showModal, setShowModal] = useState(false)
  const [selectedRefund, setSelectedRefund] = useState(null)

  const [currentPage, setCurrentPage] = useState(0)
  const [totalPages, setTotalPages] = useState(0)
  const [pageSize] = useState(10)

  useEffect(() => {
    fetchRefunds(0)
  }, [])

  const fetchRefunds = async (page = 0) => {
    setIsLoading(true)

    try {
      const response = await refundService.getAllRefunds(page, pageSize)

      setRefunds(response.content || [])
      setTotalPages(response.totalPages || 1)
      setCurrentPage(page)

    } catch {
      toast.error('Failed to load refunds')
    } finally {
      setIsLoading(false)
    }
  }

  const handleProcessClick = (refund) => {
    setSelectedRefund(refund)
    setShowModal(true)
  }

  const handleProcessRefund = async () => {
    try {
      await refundService.processRefund(selectedRefund.refundReference)
      toast.success('Refund processed')
      setShowModal(false)
      fetchRefunds(currentPage)
    } catch {
      toast.error('Failed to process refund')
    }
  }

  const getStatusIcon = (status) => {
    switch (status) {
      case 'COMPLETED':
        return <CheckCircle size={16} />
      case 'FAILED':
        return <XCircle size={16} />
      default:
        return <AlertTriangle size={16} />
    }
  }

  const getStatusColor = (status) => {
    switch (status) {
      case 'COMPLETED': return 'success'
      case 'FAILED': return 'danger'
      default: return 'warning'
    }
  }

  if (isLoading && refunds.length === 0) return <LoadingSpinner />

  return (
    <Container fluid className="py-4" style={{ background: '#f6f7fb', minHeight: '100vh' }}>

      {/* HEADER */}
      <div className="mb-4">
        <h3 className="mb-1">
          <RefreshCw className="me-2" size={22} />
          Refund Management
        </h3>
        <small className="text-muted">
          Process and monitor customer refund requests
        </small>
      </div>

      {/* REFUND CARDS */}
      <Row>
        {refunds.map(refund => (
          <Col lg={6} key={refund.id} className="mb-3">

            <Card className="border-0 shadow-sm h-100 hover-card">

              <Card.Body>

                {/* HEADER */}
                <div className="d-flex justify-content-between align-items-center mb-2">

                  <div className="d-flex align-items-center gap-2">
                    <CreditCard size={18} />
                    <div>
                      <h6 className="mb-0">
                        Refund #{refund.id}
                      </h6>
                      <small className="text-muted">
                        {new Date(refund.createdAt).toLocaleDateString()}
                      </small>
                    </div>
                  </div>

                  <Badge bg={getStatusColor(refund.status)}
                    className="d-flex align-items-center gap-1">
                    {getStatusIcon(refund.status)}
                    {refund.status}
                  </Badge>

                </div>

                <hr />

                {/* DETAILS */}
                <Row className="mb-2">

                  <Col>
                    <small className="text-muted">Reference</small>
                    <div className="fw-semibold">
                      {refund.refundReference}
                    </div>
                  </Col>

                  <Col>
                    <small className="text-muted">Amount</small>
                    <div className="fw-bold d-flex align-items-center gap-1">
                      <DollarSign size={14} />
                      {refund.amount?.toFixed(2)}
                    </div>
                  </Col>

                  <Col>
                    <small className="text-muted">Payment</small>
                    <div>
                      #{refund.paymentId}
                    </div>
                  </Col>

                </Row>

                {/* REASON */}
                <div className="mb-3">
                  <small className="text-muted">Reason</small>
                  <div className="text-dark">
                    <FileText size={14} className="me-1" />
                    {refund.reason || 'No reason provided'}
                  </div>
                </div>

                {/* ACTION */}
                <div className="d-flex justify-content-end">

                  <Button
                    size="sm"
                    variant="outline-success"
                    disabled={refund.status === 'COMPLETED'}
                    onClick={() => handleProcessClick(refund)}
                  >
                    Process Refund
                  </Button>

                </div>

              </Card.Body>

            </Card>

          </Col>
        ))}
      </Row>

      {/* PAGINATION */}
      {totalPages > 1 && (
        <div className="d-flex justify-content-center mt-4">
          <Pagination>
            {Array.from({ length: totalPages }, (_, i) => (
              <Pagination.Item
                key={i}
                active={i === currentPage}
                onClick={() => fetchRefunds(i)}
              >
                {i + 1}
              </Pagination.Item>
            ))}
          </Pagination>
        </div>
      )}

      {/* MODAL */}
      <Modal show={showModal} onHide={() => setShowModal(false)} centered>

        <Modal.Header closeButton>
          <Modal.Title>Process Refund</Modal.Title>
        </Modal.Header>

        <Modal.Body>
          {selectedRefund && (
            <div>
              <p><b>Reference:</b> {selectedRefund.refundReference}</p>
              <p><b>Amount:</b> ${selectedRefund.amount}</p>
              <p><b>Reason:</b> {selectedRefund.reason || '—'}</p>
            </div>
          )}
        </Modal.Body>

        <Modal.Footer>
          <Button variant="secondary" onClick={() => setShowModal(false)}>
            Cancel
          </Button>

          <Button variant="success" onClick={handleProcessRefund}>
            Confirm Process
          </Button>
        </Modal.Footer>

      </Modal>

    </Container>
  )
}

export default AdminRefundsPage