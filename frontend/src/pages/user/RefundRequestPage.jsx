// import React, { useState, useEffect } from 'react'
// import { Container, Row, Col, Form, Button, Card, Alert } from 'react-bootstrap'
// import { useParams, useNavigate } from 'react-router-dom'
// import { orderService } from '../../api/orderService'
// import { refundService } from '../../api/refundService'
// import LoadingSpinner from '../../components/LoadingSpinner'
// import toast from 'react-hot-toast'

// const refundReasons = [
//   { value: 'DAMAGED_PRODUCT', label: 'Damaged Product' },
//   { value: 'WRONG_ITEM', label: 'Wrong Item Received' },
//   { value: 'NOT_DELIVERED', label: 'Not Delivered' },
//   { value: 'CUSTOMER_CANCELLED', label: 'Customer Changed Mind' },
//   { value: 'OTHER', label: 'Other' }
// ]

// export const RefundRequestPage = () => {
//   const { id } = useParams()
//   const navigate = useNavigate()
//   const [order, setOrder] = useState(null)
//   const [isLoading, setIsLoading] = useState(false)
//   const [isSubmitting, setIsSubmitting] = useState(false)
//   const [formData, setFormData] = useState({
//     reasonType: 'DAMAGED_PRODUCT',
//     reasonDetail: '',
//     requestedAmount: ''
//   })
//   const [errors, setErrors] = useState({})

//   useEffect(() => {
//     fetchOrder()
//   }, [id])

//   const fetchOrder = async () => {
//     setIsLoading(true)
//     try {
//       const response = await orderService.getOrderById(id)
//       setOrder(response)
//       setFormData((prev) => ({
//         ...prev,
//         requestedAmount: response.total
//       }))
//     } catch (error) {
//       toast.error('Failed to load order')
//       navigate('/orders')
//     } finally {
//       setIsLoading(false)
//     }
//   }

//   const validateForm = () => {
//     const newErrors = {}

//     if (!formData.reasonType) {
//       newErrors.reasonType = 'Please select a reason'
//     }
//     if (!formData.reasonDetail.trim()) {
//       newErrors.reasonDetail = 'Please provide details'
//     }
//     if (!formData.requestedAmount || parseFloat(formData.requestedAmount) <= 0) {
//       newErrors.requestedAmount = 'Please enter a valid amount'
//     }

//     return newErrors
//   }

//   const handleChange = (e) => {
//     const { name, value } = e.target
//     setFormData((prev) => ({
//       ...prev,
//       [name]: value
//     }))
//   }

//   const handleSubmit = async (e) => {
//     e.preventDefault()
//     const newErrors = validateForm()

//     if (Object.keys(newErrors).length > 0) {
//       setErrors(newErrors)
//       return
//     }

//     setIsSubmitting(true)
//     try {
//       await refundService.requestRefund(id, {
//         reasonType: formData.reasonType,
//         reasonDetail: formData.reasonDetail,
//         requestedAmount: parseFloat(formData.requestedAmount)
//       })

//       toast.success('Refund request submitted successfully!')
//       navigate(`/order/${id}`)
//     } catch (error) {
//       toast.error(error?.message || 'Failed to submit refund request')
//     } finally {
//       setIsSubmitting(false)
//     }
//   }

//   if (isLoading) {
//     return <LoadingSpinner />
//   }

//   if (!order) {
//     return (
//       <Container className="py-5">
//         <div className="empty-state">
//           <h4>Order not found</h4>
//           <Button onClick={() => navigate('/orders')}>Back to Orders</Button>
//         </div>
//       </Container>
//     )
//   }

//   // Check if order is eligible for refund
//   if (!['DELIVERED', 'COMPLETED'].includes(order.status)) {
//     return (
//       <Container className="py-5">
//         <Alert variant="danger">
//           <h4>Refund Not Available</h4>
//           <p>Refunds are only available for delivered or completed orders.</p>
//           <p>Current Order Status: <strong>{order.status}</strong></p>
//           <Button onClick={() => navigate('/orders')}>Back to Orders</Button>
//         </Alert>
//       </Container>
//     )
//   }

//   return (
//     <Container className="py-5">
//       <h2 className="mb-4">Request Refund</h2>

//       <Row>
//         <Col lg={8}>
//           <Card className="mb-4">
//             <Card.Header>
//               <h5 className="mb-0">Refund Request Form</h5>
//             </Card.Header>
//             <Card.Body>
//               <Form onSubmit={handleSubmit}>
//                 <Form.Group className="mb-3">
//                   <Form.Label>Reason for Refund</Form.Label>
//                   <Form.Select
//                     name="reasonType"
//                     value={formData.reasonType}
//                     onChange={handleChange}
//                     isInvalid={!!errors.reasonType}
//                   >
//                     {refundReasons.map((reason) => (
//                       <option key={reason.value} value={reason.value}>
//                         {reason.label}
//                       </option>
//                     ))}
//                   </Form.Select>
//                   <Form.Control.Feedback type="invalid">
//                     {errors.reasonType}
//                   </Form.Control.Feedback>
//                 </Form.Group>

//                 <Form.Group className="mb-3">
//                   <Form.Label>Details</Form.Label>
//                   <Form.Control
//                     as="textarea"
//                     rows={4}
//                     name="reasonDetail"
//                     value={formData.reasonDetail}
//                     onChange={handleChange}
//                     isInvalid={!!errors.reasonDetail}
//                     placeholder="Describe the issue in detail..."
//                   />
//                   <Form.Control.Feedback type="invalid">
//                     {errors.reasonDetail}
//                   </Form.Control.Feedback>
//                 </Form.Group>

//                 <Form.Group className="mb-3">
//                   <Form.Label>Refund Amount</Form.Label>
//                   <Form.Control
//                     type="number"
//                     step="0.01"
//                     name="requestedAmount"
//                     value={formData.requestedAmount}
//                     onChange={handleChange}
//                     isInvalid={!!errors.requestedAmount}
//                   />
//                   <Form.Text className="text-muted">
//                     Max amount available: ${order.total?.toFixed(2)}
//                   </Form.Text>
//                   <Form.Control.Feedback type="invalid">
//                     {errors.requestedAmount}
//                   </Form.Control.Feedback>
//                 </Form.Group>

//                 <div className="d-grid gap-2">
//                   <Button
//                     variant="primary"
//                     type="submit"
//                     disabled={isSubmitting}
//                   >
//                     {isSubmitting ? 'Submitting...' : 'Submit Refund Request'}
//                   </Button>
//                   <Button
//                     variant="outline-secondary"
//                     onClick={() => navigate(`/order/${id}`)}
//                   >
//                     Cancel
//                   </Button>
//                 </div>
//               </Form>
//             </Card.Body>
//           </Card>
//         </Col>

//         <Col lg={4}>
//           <Card>
//             <Card.Header>
//               <h5 className="mb-0">Order Information</h5>
//             </Card.Header>
//             <Card.Body>
//               <p>
//                 <strong>Order ID:</strong> #{order.orderId || order.id}
//               </p>
//               <p>
//                 <strong>Order Date:</strong>{' '}
//                 {new Date(order.createdAt).toLocaleDateString()}
//               </p>
//               <p>
//                 <strong>Order Total:</strong> ${order.total?.toFixed(2)}
//               </p>
//               <p>
//                 <strong>Items:</strong> {order.items?.length || 0}
//               </p>
//               <hr />
//               <p className="text-muted mb-0">
//                 Your refund request will be reviewed by our team. We'll notify you via email
//                 once a decision has been made.
//               </p>
//             </Card.Body>
//           </Card>
//         </Col>
//       </Row>
//     </Container>
//   )
// }

// export default RefundRequestPage


import React, { useState, useEffect } from 'react'
import { Container, Row, Col, Form, Button, Card, Alert } from 'react-bootstrap'
import { useParams, useNavigate } from 'react-router-dom'
import { orderService } from '../../api/orderService'
import { refundService } from '../../api/refundService'
import LoadingSpinner from '../../components/LoadingSpinner'
import toast from 'react-hot-toast'

export const RefundRequestPage = () => {
  const { id } = useParams()
  const navigate = useNavigate()

  const [order, setOrder] = useState(null)
  const [paymentReference, setPaymentReference] = useState(null)

  const [isLoading, setIsLoading] = useState(false)
  const [isSubmitting, setIsSubmitting] = useState(false)

  const [formData, setFormData] = useState({
    amount: '',
    reason: ''
  })

  const [errors, setErrors] = useState({})

  useEffect(() => {
    fetchOrder()
  }, [id])

 const fetchOrder = async () => {
  setIsLoading(true)

  try {
    const response = await orderService.getOrderById(id)

    console.log("FULL RESPONSE:", response)

    // ✅ correct order object
    const orderData = response?.data

    console.log("ORDER DATA:", orderData)

    setOrder(orderData)

    // ❗ FIX: paymentReference MUST come from backend
    setPaymentReference(orderData.paymentReference)

    setFormData({
      amount: orderData.totalAmount || orderData.total || '',
      reason: ''
    })

  } catch (error) {
    console.error(error)
    toast.error('Failed to load order')
    navigate('/orders')
  } finally {
    setIsLoading(false)
  }
}

  const validateForm = () => {
    const newErrors = {}

    if (!formData.amount || parseFloat(formData.amount) <= 0) {
      newErrors.amount = 'Enter valid refund amount'
    }

    if (!formData.reason || formData.reason.trim().length < 5) {
      newErrors.reason = 'Reason must be at least 5 characters'
    }

    return newErrors
  }
console.log("PAYMENT REFERENCE:", paymentReference)
  const handleChange = (e) => {
    setFormData({
      ...formData,
      [e.target.name]: e.target.value
    })
  }

  const handleSubmit = async (e) => {
    e.preventDefault()

    const newErrors = validateForm()
    if (Object.keys(newErrors).length > 0) {
      setErrors(newErrors)
      return
    }

    setIsSubmitting(true)

    try {
      await refundService.requestRefund({
        paymentReference: paymentReference,
        amount: parseFloat(formData.amount),
        reason: formData.reason
      })

      toast.success('Refund request submitted successfully!')
      navigate(`/order/${id}`)

    } catch (error) {
      toast.error(error?.message || 'Failed to submit refund request')
    } finally {
      setIsSubmitting(false)
    }
  }

  if (isLoading) return <LoadingSpinner />

  if (!order) {
    return (
      <Container className="py-5">
        <Alert variant="danger">
          Order not found
        </Alert>
      </Container>
    )
  }
console.log("CHECKING STATUS:", JSON.stringify(order.status))


  if (!['DELIVERED', 'COMPLETED'].includes(order.status)) {
    return (
      <Container className="py-5">
        <Alert variant="danger">
          Refunds are only available for delivered or completed orders.
        </Alert>
      </Container>
    )
  }

  return (
    <Container className="py-5">
      <h2 className="mb-4">Request Refund</h2>

      <Row>
        <Col lg={8}>
          <Card>
            <Card.Body>
              <Form onSubmit={handleSubmit}>

                <Form.Group className="mb-3">
                  <Form.Label>Refund Amount</Form.Label>
                  <Form.Control
                    type="number"
                    name="amount"
                    value={formData.amount}
                    onChange={handleChange}
                    isInvalid={!!errors.amount}
                  />
                  <Form.Control.Feedback type="invalid">
                    {errors.amount}
                  </Form.Control.Feedback>
                </Form.Group>

                <Form.Group className="mb-3">
                  <Form.Label>Reason</Form.Label>
                  <Form.Control
                    as="textarea"
                    rows={4}
                    name="reason"
                    value={formData.reason}
                    onChange={handleChange}
                    isInvalid={!!errors.reason}
                    placeholder="Explain your refund reason..."
                  />
                  <Form.Control.Feedback type="invalid">
                    {errors.reason}
                  </Form.Control.Feedback>
                </Form.Group>

                <Button type="submit" disabled={isSubmitting}>
                  {isSubmitting ? 'Submitting...' : 'Submit Refund'}
                </Button>

              </Form>
            </Card.Body>
          </Card>
        </Col>

        <Col lg={4}>
          <Card>
            <Card.Body>
              <p><strong>Order:</strong> #{order.id}</p>
              <p><strong>Total:</strong> ${order.total}</p>
              <p><strong>Status:</strong> {order.status}</p>
            </Card.Body>
          </Card>
        </Col>
      </Row>
    </Container>
  )
}

export default RefundRequestPage