import React, { useEffect, useState } from 'react'
import {
  Container,
  Row,
  Col,
  Card,
  Button,
  Badge,
  Modal,
  Form
} from 'react-bootstrap'

import {
  Folder,
  Layers,
  Plus,
  Edit3,
  Trash2
} from 'lucide-react'

import toast from 'react-hot-toast'
import LoadingSpinner from '../../components/LoadingSpinner'
import { categoryService } from '../../api/categoryService'

const AdminCategoriesPage = () => {

  const [categories, setCategories] = useState([])
  const [rootCategories, setRootCategories] = useState([])
  const [loading, setLoading] = useState(false)

  const [showModal, setShowModal] = useState(false)
  const [editingCategory, setEditingCategory] = useState(null)

  const [formData, setFormData] = useState({
    name: '',
    description: '',
    imageUrl: '',
    parentId: ''
  })

  useEffect(() => {
    fetchCategories()
    fetchRootCategories()
  }, [])

  const fetchCategories = async () => {
    setLoading(true)
    try {
      const data = await categoryService.getAllCategories()
      setCategories(data.filter(c => c.parentId === null))
    } catch {
      toast.error('Failed categories')
    } finally {
      setLoading(false)
    }
  }

  const fetchRootCategories = async () => {
    try {
      const data = await categoryService.getRootCategories()
      setRootCategories(data)
    } catch {
      toast.error('Failed roots')
    }
  }

  const handleSave = async () => {
    try {
      const payload = {
        ...formData,
        parentId: formData.parentId ? Number(formData.parentId) : null
      }

      if (editingCategory) {
        await categoryService.updateCategory(editingCategory.id, payload)
        toast.success('Updated')
      } else {
        await categoryService.createCategory(payload)
        toast.success('Created')
      }

      setShowModal(false)
      fetchCategories()
      fetchRootCategories()

    } catch {
      toast.error('Failed save')
    }
  }

  const handleDelete = async (id) => {
    if (!window.confirm('Delete category?')) return
    await categoryService.deleteCategory(id)
    toast.success('Deleted')
    fetchCategories()
  }

  if (loading) return <LoadingSpinner />

  return (
    <Container fluid className="py-4" style={{ background: '#f6f7fb', minHeight: '100vh' }}>

      {/* HEADER */}
      <Card className="border-0 shadow-sm mb-3">
        <Card.Body className="d-flex justify-content-between align-items-center">
          <div>
            <h4 className="mb-0 d-flex align-items-center gap-2">
              <Folder size={20} />
              Categories
            </h4>
            <small className="text-muted">Organize your product structure</small>
          </div>

          <Button onClick={() => setShowModal(true)}>
            <Plus size={16} className="me-1" />
            Add Category
          </Button>
        </Card.Body>
      </Card>

      {/* GRID */}
      <Row>
        {categories.map(cat => (
          <Col md={4} key={cat.id} className="mb-3">

            <Card className="border-0 shadow-sm h-100">

              <Card.Body>

                <div className="d-flex justify-content-between align-items-start">

                  <div className="d-flex align-items-center gap-2">
                    <Folder size={18} />
                    <h6 className="mb-0">{cat.name}</h6>
                  </div>

                  <Badge bg={cat.active ? 'success' : 'secondary'}>
                    {cat.active ? 'Active' : 'Inactive'}
                  </Badge>

                </div>

                <p className="text-muted mt-2 mb-2">
                  {cat.description || 'No description'}
                </p>

                <div className="d-flex align-items-center gap-2 text-muted small mb-3">
                  <Layers size={14} />
                  {cat.children?.length || 0} sub categories
                </div>

                <div className="d-flex gap-2">

                  <Button
                    size="sm"
                    variant="outline-primary"
                    onClick={() => {
                      setEditingCategory(cat)
                      setFormData(cat)
                      setShowModal(true)
                    }}
                  >
                    <Edit3 size={14} />
                  </Button>

                  <Button
                    size="sm"
                    variant="outline-danger"
                    onClick={() => handleDelete(cat.id)}
                  >
                    <Trash2 size={14} />
                  </Button>

                </div>

              </Card.Body>

            </Card>

          </Col>
        ))}
      </Row>

      {/* MODAL */}
      <Modal show={showModal} onHide={() => setShowModal(false)}>
        <Modal.Header closeButton>
          <Modal.Title>
            {editingCategory ? 'Edit Category' : 'Add Category'}
          </Modal.Title>
        </Modal.Header>

        <Modal.Body>

          <Form.Control
            className="mb-2"
            placeholder="Name"
            value={formData.name}
            onChange={(e) =>
              setFormData({ ...formData, name: e.target.value })
            }
          />

          <Form.Control
            className="mb-2"
            as="textarea"
            placeholder="Description"
            value={formData.description}
            onChange={(e) =>
              setFormData({ ...formData, description: e.target.value })
            }
          />

          <Form.Select
            value={formData.parentId}
            onChange={(e) =>
              setFormData({ ...formData, parentId: e.target.value })
            }
          >
            <option value="">No Parent</option>
            {rootCategories.map(c => (
              <option key={c.id} value={c.id}>
                {c.name}
              </option>
            ))}
          </Form.Select>

        </Modal.Body>

        <Modal.Footer>
          <Button variant="secondary" onClick={() => setShowModal(false)}>
            Cancel
          </Button>

          <Button onClick={handleSave}>
            Save
          </Button>
        </Modal.Footer>
      </Modal>

    </Container>
  )
}

export default AdminCategoriesPage