import React, { useState, useEffect, useRef } from 'react'
import {
  Container,
  Card,
  Button,
  Badge,
  Modal,
  Form,
  Row,
  Col,
  InputGroup
} from 'react-bootstrap'

import {
  Plus,
  Search,
  Edit3,
  Trash2,
  Package,
  Tag,
  DollarSign,
  Layers
} from 'lucide-react'

import { productService } from '../../api/productService'
import { categoryService } from '../../api/categoryService'
import LoadingSpinner from '../../components/LoadingSpinner'
import toast from 'react-hot-toast'

export const AdminProductsPage = () => {

  const [products, setProducts] = useState([])
  const [categories, setCategories] = useState([])
  const [isLoading, setIsLoading] = useState(false)

  const [showModal, setShowModal] = useState(false)
  const [editingProduct, setEditingProduct] = useState(null)

  const [searchTerm, setSearchTerm] = useState('')
  const [imageFile, setImageFile] = useState(null)

  const [formData, setFormData] = useState({
    name: '',
    description: '',
    price: '',
    brand: '',
    stockQuantity: '',
    categoryId: '',
    status: ''
  })

  const statuses = ["DRAFT", "ACTIVE", "ARCHIVED", "INACTIVE", "OUT_OF_STOCK"]

  const isFirstRender = useRef(true)

  useEffect(() => {
    fetchProducts()
    fetchCategories()
  }, [])

  useEffect(() => {
    if (isFirstRender.current) {
      isFirstRender.current = false
      return
    }
    const timer = setTimeout(() => {
      fetchProducts(searchTerm)
    }, 500)
    return () => clearTimeout(timer)
  }, [searchTerm])

  const fetchProducts = async (search = '') => {
    setIsLoading(true)
    try {
      const res = await productService.getAllProducts(0, 50, search)
      const data = res?.data ?? res ?? {}
      setProducts(data.content || [])
    } catch {
      toast.error('Failed to load products')
    } finally {
      setIsLoading(false)
    }
  }

  const fetchCategories = async () => {
    try {
      const res = await categoryService.getRootCategories()
      setCategories(res || [])
    } catch {
      toast.error('Failed categories')
    }
  }

  const handleSaveProduct = async () => {
    try {
      let uploadedUrl = null

      if (imageFile) {
        const res = await productService.uploadImage(imageFile)
        console.log('Upload response:', res)       // see what came back
        console.log('uploadedUrl:', res?.url)
        uploadedUrl = res?.url
      }



      const payload = {
        ...formData,
        brand: formData.brand,
        price: Number(formData.price),
        stockQuantity: Number(formData.stockQuantity),
        categoryId: Number(formData.categoryId),
        images: uploadedUrl
          ? [{ url: uploadedUrl, primary: true }]
          : []
      }

      if (editingProduct) {
        await productService.updateProduct(editingProduct.id, payload)
        toast.success('Updated')
      } else {
        await productService.createProduct(payload)
        toast.success('Created')
      }

      setShowModal(false)
      fetchProducts(searchTerm)

    } catch {
      toast.error('Failed save')
    }
  }

  const handleDelete = async (id) => {
    if (!window.confirm('Delete product?')) return
    await productService.deleteProduct(id)
    toast.success('Deleted')
    fetchProducts(searchTerm)
  }

  const getStatusColor = (status) => {
    switch (status) {
      case 'ACTIVE': return 'success'
      case 'DRAFT': return 'warning'
      case 'INACTIVE': return 'secondary'
      case 'OUT_OF_STOCK': return 'danger'
      default: return 'info'
    }
  }

  return (
    <Container fluid className="py-4" style={{ background: '#f6f7fb', minHeight: '100vh' }}>

      {/* HEADER */}
      <Card className="border-0 shadow-sm mb-3">
        <Card.Body>

          <div className="d-flex justify-content-between align-items-center">
            <div>
              <h4 className="mb-0 d-flex align-items-center gap-2">
                <Package size={22} />
                Product Management
              </h4>
              <small className="text-muted">Manage inventory easily</small>
            </div>

            <Button onClick={() => setShowModal(true)}>
              <Plus size={16} className="me-1" />
              Add Product
            </Button>
          </div>

          <InputGroup className="mt-3">
            <InputGroup.Text>
              <Search size={16} />
            </InputGroup.Text>
            <Form.Control
              placeholder="Search products..."
              onChange={(e) => setSearchTerm(e.target.value)}
            />
          </InputGroup>

        </Card.Body>
      </Card>

      {/* GRID */}
      {isLoading
        ? <LoadingSpinner />
        : (
          <Row>
            {products.map(p => (
              <Col md={3} key={p.id} className="mb-4">

                <Card className="border-0 shadow-sm h-100 product-card">

                  {/* IMAGE */}
                  <div style={{ height: 180, overflow: 'hidden' }}>
                    <img
                        loading="lazy"
                        src={
                          p.images?.find(i => i.primary)?.url ||
                          p.images?.[0]?.url ||
                          "https://placehold.co/300x200?text=No+Image"
                        }
                        alt={p.name}
                        style={{
                          width: '100%',
                          height: '100%',
                          objectFit: 'cover'
                        }}
                      />
                  </div>

                  <Card.Body>

                    <div className="d-flex justify-content-between">
                      <h6 className="mb-1">{p.name}</h6>
                      <Badge bg={getStatusColor(p.status)}>
                        {p.status}
                      </Badge>
                    </div>

                    <div className="text-muted small mb-2">
                      <Tag size={14} className="me-1" />
                      {p.categoryName || 'No category'}
                    </div>

                    <div className="fw-bold mb-2">
                      <DollarSign size={14} /> {p.price}
                    </div>

                    <div className="text-muted small mb-3">
                      Stock: {p.stockQuantity}
                    </div>

                    <div className="d-flex gap-2">
                      <Button
                        size="sm"
                        variant="outline-primary"
                        onClick={() => {
                          setEditingProduct(p)

                          setFormData({
                            name: p.name || '',
                            description: p.description || '',
                            price: p.price || '',
                            stockQuantity: p.stockQuantity || '',
                            categoryId: p.categoryId || '',
                            status: p.status || '',
                            brand: p.brand || ''
                          })

                          setShowModal(true)
                        }}
                      >
                        <Edit3 size={14} />
                      </Button>

                      <Button
                        size="sm"
                        variant="outline-danger"
                        onClick={() => handleDelete(p.id)}
                      >
                        <Trash2 size={14} />
                      </Button>
                    </div>

                  </Card.Body>

                </Card>

              </Col>
            ))}
          </Row>
        )
      }

      {/* MODAL */}
      <Modal show={showModal} onHide={() => setShowModal(false)}>
        <Modal.Header closeButton>
          <Modal.Title>
            {editingProduct ? 'Edit Product' : 'Add Product'}
          </Modal.Title>
        </Modal.Header>

        <Modal.Body>

          <Form.Group className="mb-2">
            <Form.Label>Name</Form.Label>
            <Form.Control
              placeholder="Product name"
              value={formData.name}
              onChange={(e) => setFormData({ ...formData, name: e.target.value })}
            />
          </Form.Group>

          <Form.Group className="mb-2">
            <Form.Label>Description</Form.Label>
            <Form.Control
              as="textarea"
              placeholder="Description"
              value={formData.description}
              onChange={(e) => setFormData({ ...formData, description: e.target.value })}
            />
          </Form.Group>

          <Form.Group className="mb-2">
            <Form.Label>Brand</Form.Label>
            <Form.Control
              type="text"
              placeholder="e.g. Samsung, Nike"
              value={formData.brand}
              onChange={(e) =>
                setFormData({ ...formData, brand: e.target.value })
              }
            />
          </Form.Group>

          <Row>
            <Col>
              <Form.Group className="mb-2">
                <Form.Label>Price</Form.Label>
                <Form.Control
                  type="number"
                  placeholder="0.00"
                  value={formData.price}
                  onChange={(e) => setFormData({ ...formData, price: e.target.value })}
                />
              </Form.Group>
            </Col>
            <Col>
              <Form.Group className="mb-2">
                <Form.Label>Stock</Form.Label>
                <Form.Control
                  type="number"
                  placeholder="0"
                  value={formData.stockQuantity}
                  onChange={(e) => setFormData({ ...formData, stockQuantity: e.target.value })}
                />
              </Form.Group>
            </Col>
          </Row>

          <Form.Group className="mb-2">
            <Form.Label>Category</Form.Label>
            <Form.Select
              value={formData.categoryId}
              onChange={(e) => setFormData({ ...formData, categoryId: e.target.value })}
            >
              <option value="">Select category</option>
              {categories.map(c => (
                <option key={c.id} value={c.id}>{c.name}</option>
              ))}
            </Form.Select>
          </Form.Group>

          <Form.Group className="mb-2">
            <Form.Label>Status</Form.Label>
            <Form.Select
              value={formData.status}
              onChange={(e) => setFormData({ ...formData, status: e.target.value })}
            >
              <option value="">Select status</option>
              {statuses.map(s => (
                <option key={s} value={s}>{s}</option>
              ))}
            </Form.Select>
          </Form.Group>

          <Form.Group className="mb-2">
            <Form.Label>Product Image</Form.Label>
            <Form.Control
              type="file"
              accept="image/*"
              onChange={(e) => setImageFile(e.target.files[0])}
            />
          </Form.Group>

        </Modal.Body>

        <Modal.Footer>
          <Button variant="secondary" onClick={() => setShowModal(false)}>
            Cancel
          </Button>
          <Button onClick={handleSaveProduct}>
            Save
          </Button>
        </Modal.Footer>

      </Modal>

    </Container>
  )
}

export default AdminProductsPage