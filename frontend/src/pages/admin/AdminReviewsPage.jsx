import React, { useEffect, useState } from 'react'
import {
  Container,
  Table,
  Button,
  Badge,
  Tabs,
  Tab,
  Form,
  Spinner,
  Row,
  Col
} from 'react-bootstrap'

import toast from 'react-hot-toast'
import { reviewService } from '../../api/reviewService'

const AdminReviewsPage = () => {

  const [pending, setPending] = useState([])
  const [allReviews, setAllReviews] = useState([])

  const [productId, setProductId] = useState('')
  const [loading, setLoading] = useState(false)

  // ─────────────────────────────
  // Pending reviews
  // ─────────────────────────────

  const loadPending = async () => {
    try {
      setLoading(true)

      const data =
        await reviewService.getPendingReviews()

      setPending(data?.content || [])

    } catch (err) {
      console.log(err)
      toast.error('Failed loading pending reviews')
    } finally {
      setLoading(false)
    }
  }

  // ─────────────────────────────
  // Search reviews
  // ─────────────────────────────

  const loadAll = async () => {

    if (!productId) {
      toast.error('Enter Product ID')
      return
    }

    try {

      setLoading(true)

      const data =
        await reviewService.getAllReviews(productId)

      console.log(data)

      setAllReviews(data?.content || [])

    } catch (err) {

      console.log(err)

      toast.error(
        'Failed loading reviews'
      )

    } finally {
      setLoading(false)
    }
  }

  useEffect(() => {
    loadPending()
  }, [])

  // ─────────────────────────────
  // Update status
  // ─────────────────────────────

  const updateStatus = async (
    id,
    status
  ) => {

    try {

      await reviewService.updateReviewStatus(
        id,
        status
      )

      toast.success(
        `Review ${status}`
      )

      loadPending()

      if (productId) {
        loadAll()
      }

    } catch {

      toast.error(
        'Update failed'
      )

    }
  }

  // ─────────────────────────────
  // Delete
  // ─────────────────────────────

  const deleteReview = async (
    id
  ) => {

    try {

      await reviewService.deleteReview(id)

      toast.success(
        'Deleted'
      )

      loadPending()

      if (productId) {
        loadAll()
      }

    } catch {

      toast.error(
        'Delete failed'
      )

    }
  }

  // ─────────────────────────────
  // Table UI
  // ─────────────────────────────

  const renderTable = (
    data
  ) => (

    <Table bordered hover>

      <thead>

      <tr>
        <th>ID</th>
        <th>User</th>
        <th>Rating</th>
        <th>Title</th>
        <th>Status</th>
        <th>Actions</th>
      </tr>

      </thead>

      <tbody>

      {data.length===0?

      (
        <tr>

          <td
            colSpan='6'
            className='text-center'
          >

            No reviews found

          </td>

        </tr>
      )

      :

      (

      data.map(r=>(

      <tr key={r.id}>

        <td>{r.id}</td>

        <td>
          {r.username}
        </td>

        <td>
          ⭐ {r.rating}
        </td>

        <td>
          {r.title}
        </td>

        <td>

          <Badge
          bg={
            r.status==='APPROVED'
            ?'success'
            :r.status==='REJECTED'
            ?'danger'
            :'warning'
          }
          >

            {r.status}

          </Badge>

        </td>

        <td>

        <Button
        size='sm'
        className='me-2'
        onClick={()=>
          updateStatus(
            r.id,
            'APPROVED'
          )
        }
        >
          Approve
        </Button>

        <Button
        size='sm'
        variant='warning'
        className='me-2'
        onClick={()=>
          updateStatus(
            r.id,
            'REJECTED'
          )
        }
        >
          Reject
        </Button>

        <Button
        size='sm'
        variant='danger'
        onClick={()=>
          deleteReview(
            r.id
          )
        }
        >
          Delete
        </Button>

        </td>

      </tr>

      ))

      )}

      </tbody>

    </Table>

  )

  return (

    <Container className='mt-4'>

      <h3>
        Admin Review Dashboard
      </h3>

      {loading && (

      <div className='text-center my-3'>

        <Spinner
        animation='border'
        />

      </div>

      )}

      <Tabs
      defaultActiveKey='pending'
      className='mb-3'
      >

        {/* Pending */}

        <Tab
        eventKey='pending'
        title='Pending Reviews'
        >

          {renderTable(
            pending
          )}

        </Tab>


        {/* Search */}

        <Tab
        eventKey='search'
        title='Search Reviews'
        >

        <Row className='mb-3'>

          <Col md={8}>

          <Form.Control
          type='number'
          placeholder='Enter Product ID'
          value={productId}
          onChange={(e)=>
            setProductId(
              e.target.value
            )
          }
          />

          </Col>

          <Col md={4}>

          <Button
          onClick={loadAll}
          >
            Search
          </Button>

          </Col>

        </Row>

        {renderTable(
          allReviews
        )}

        </Tab>

      </Tabs>

    </Container>

  )

}

export default AdminReviewsPage