import React from 'react'
import { Spinner } from 'react-bootstrap'

export const LoadingSpinner = ({ size = 'sm', text = 'Loading...' }) => {
  return (
    <div className="text-center py-5">
      <Spinner animation="border" role="status" size={size} className="loading-spinner" />
      <span className="ms-2">{text}</span>
    </div>
  )
}

export default LoadingSpinner
