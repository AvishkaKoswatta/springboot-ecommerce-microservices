import React, { useState, useEffect } from 'react'
import { Container, Table, Badge, InputGroup, Form, Pagination } from 'react-bootstrap'
import { userService } from '../../api/userService'
import LoadingSpinner from '../../components/LoadingSpinner'
import toast from 'react-hot-toast'

export const AdminUsersPage = () => {
  const [users, setUsers] = useState([])
  const [isLoading, setIsLoading] = useState(false)
  const [currentPage, setCurrentPage] = useState(0)
  const [totalPages, setTotalPages] = useState(0)
  const [searchTerm, setSearchTerm] = useState('')
  const [pageSize] = useState(10)

  useEffect(() => {
    fetchUsers(0, '')
  }, [])

  const fetchUsers = async (page = 0, search = '') => {
    setIsLoading(true)
    try {
      const response = await userService.getAllUsers(page, pageSize, search)
      setUsers(response.content || response.data || [])
      setTotalPages(response.totalPages || 1)
      setCurrentPage(page)
    } catch (error) {
      toast.error('Failed to load users')
    } finally {
      setIsLoading(false)
    }
  }

  const handleSearch = (e) => {
    const value = e.target.value
    setSearchTerm(value)
    setCurrentPage(0)
    fetchUsers(0, value)
  }

  const handlePageChange = (pageNumber) => {
    fetchUsers(pageNumber, searchTerm)
  }

  if (isLoading && users.length === 0) {
    return <LoadingSpinner />
  }

  const paginationItems = []
  for (let number = 0; number < totalPages; number++) {
    paginationItems.push(
      <Pagination.Item
        key={number}
        active={number === currentPage}
        onClick={() => handlePageChange(number)}
      >
        {number + 1}
      </Pagination.Item>
    )
  }

  return (
    <Container fluid className="py-4">
      <h2 className="mb-4">Manage Users</h2>

      <InputGroup className="mb-4">
        <Form.Control
          placeholder="Search users by name or email..."
          value={searchTerm}
          onChange={handleSearch}
        />
      </InputGroup>

      <div className="card">
        <div className="table-responsive">
          <Table hover className="mb-0">
            <thead className="bg-light">
              <tr>
                <th>User ID</th>
                <th>Username</th>
                <th>Email</th>
                <th>Roles</th>
                <th>Created Date</th>
              </tr>
            </thead>
            <tbody>
              {users.map((user) => (
                <tr key={user.userId || user.id}>
                  <td>{user.userId || user.id}</td>
                  <td>
                    <strong>{user.username}</strong>
                  </td>
                  <td>{user.email}</td>
                  <td>
                    {user.roles?.map((role) => (
                      <Badge
                        key={role}
                        bg={role === 'ADMIN' ? 'danger' : 'primary'}
                        className="me-1"
                      >
                        {role}
                      </Badge>
                    ))}
                  </td>
                  <td>{new Date(user.createdAt).toLocaleDateString()}</td>
                </tr>
              ))}
            </tbody>
          </Table>
        </div>
      </div>

      {users.length === 0 && (
        <div className="alert alert-info mt-3">
          <p className="mb-0">No users found matching your search.</p>
        </div>
      )}

      {totalPages > 1 && (
        <div className="d-flex justify-content-center mt-4">
          <Pagination>{paginationItems}</Pagination>
        </div>
      )}
    </Container>
  )
}

export default AdminUsersPage
