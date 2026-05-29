import React, { useState } from 'react'

const ProductCard = ({ product, onAddToCart, onViewDetails }) => {
  const [hovered, setHovered] = useState(false)
  const [adding, setAdding] = useState(false)

  const inStock = product.stockQuantity > 0

  const handleAdd = async (e) => {
    e.stopPropagation()
    if (!inStock) return
    setAdding(true)
    onAddToCart(product)
    setTimeout(() => setAdding(false), 900)
  }

  return (
    <>
      <style>{`
        .pc-wrap {
          position: relative;
          cursor: pointer;
          background: #fff;
        }

        .pc-img-box {
          position: relative;
          overflow: hidden;
          background: #f2f0ec;
          aspect-ratio: 3/4;
        }

        .pc-img {
          width: 100%;
          height: 100%;
          object-fit: cover;
          transition: transform 0.6s cubic-bezier(0.25,0.46,0.45,0.94);
          display: block;
        }

        .pc-wrap:hover .pc-img {
          transform: scale(1.07);
        }

        .pc-no-img {
          width: 100%;
          height: 100%;
          display: flex;
          align-items: center;
          justify-content: center;
          color: #bbb;
          font-size: 13px;
        }

        .pc-badge-out {
          position: absolute;
          top: 12px;
          left: 12px;
          background: #1a1a1a;
          color: #fff;
          font-size: 10px;
          padding: 4px 10px;
          text-transform: uppercase;
        }

        .pc-quick-add {
          position: absolute;
          bottom: 0;
          left: 0;
          right: 0;
          background: rgba(15,15,15,0.92);
          color: #fff;
          border: none;
          padding: 12px;
          font-size: 11px;
          cursor: pointer;
          transform: translateY(100%);
          transition: transform 0.3s ease;
        }

        .pc-wrap:hover .pc-quick-add {
          transform: translateY(0);
        }

        .pc-quick-add:disabled {
          opacity: 0.5;
          cursor: not-allowed;
        }

        .pc-quick-add.pc-adding {
          background: rgba(40,120,60,0.92);
        }

        .pc-info {
          padding: 14px 2px 20px;
        }

        .pc-category {
          font-size: 10px;
          color: #aaa;
          text-transform: uppercase;
        }

        .pc-name {
          font-size: 16px;
          margin: 5px 0;
        }

        .pc-price-row {
          display: flex;
          align-items: center;
          gap: 10px;
        }

        .pc-price {
          font-size: 15px;
          font-weight: 500;
        }

        .pc-stock-dot {
          width: 6px;
          height: 6px;
          border-radius: 50%;
        }

        .pc-stock-dot.in { background: #3cb97a; }
        .pc-stock-dot.out { background: #ccc; }
      `}</style>

      <div
        className="pc-wrap"
        onMouseEnter={() => setHovered(true)}
        onMouseLeave={() => setHovered(false)}
        onClick={() => onViewDetails(product.id)}
      >
        {/* IMAGE */}
        <div className="pc-img-box">
          <img
            loading="lazy"
            src={
              product.images?.find(i => i.primary)?.url ||
              product.images?.[0]?.url ||
              "https://placehold.co/300x200?text=No+Image"
            }
            alt={product.name}
            className="pc-img"
          />

          {!inStock && <div className="pc-badge-out">Sold Out</div>}

          <button
            className={`pc-quick-add${adding ? ' pc-adding' : ''}`}
            disabled={!inStock}
            onClick={handleAdd}
          >
            {adding ? '✓ Added' : inStock ? 'Quick Add' : 'Out of Stock'}
          </button>
        </div>

        {/* INFO */}
        <div className="pc-info">
          {product.category && (
            <div className="pc-category">{product.category}</div>
          )}

          <div className="pc-name">{product.name}</div>

          <div className="pc-price-row">
            <span className="pc-price">
              ${Number(product.price).toFixed(2)}
            </span>

            <span
              className={`pc-stock-dot ${inStock ? 'in' : 'out'}`}
              title={inStock ? 'In Stock' : 'Out of Stock'}
            />
          </div>
        </div>
      </div>
    </>
  )
}

export default ProductCard