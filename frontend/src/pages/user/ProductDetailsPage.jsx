import React, { useState, useEffect } from 'react'
import { useParams, useNavigate } from 'react-router-dom'
import { productService } from '../../api/productService'
import { useCart } from '../../context/CartContext'
import LoadingSpinner from '../../components/LoadingSpinner'
import ProductReviews from '../../components/ProductReviews'
import toast from 'react-hot-toast'

// ── Font injection ─────────────────────────────────────────────────────────
const _fl = document.createElement('link')
_fl.rel = 'stylesheet'
_fl.href = 'https://fonts.googleapis.com/css2?family=DM+Serif+Display:ital@0;1&family=DM+Sans:wght@300;400;500;600&display=swap'
if (!document.head.querySelector('[href*="DM+Serif"]')) document.head.appendChild(_fl)

// ── Styles ─────────────────────────────────────────────────────────────────
const styles = `
  .pdp-root * { box-sizing: border-box; }
  .pdp-root {
    --bg:       #faf9f7;
    --surface:  #ffffff;
    --ink:      #1a1916;
    --muted:    #7a7672;
    --border:   #e8e4de;
    --accent:   #2e6b45;
    --accent-lt:#eaf3ed;
    --danger:   #b94040;
    --star:     #c8932a;
    --shadow:   0 4px 32px rgba(26,25,22,.07);
    --r:        12px;
    background: var(--bg);
    min-height: 100vh;
    font-family: 'DM Sans', sans-serif;
    color: var(--ink);
  }

  .pdp-container { max-width: 1140px; margin: 0 auto; padding: 0 24px; }

  /* ── breadcrumb ── */
  .pdp-breadcrumb {
    display: flex; align-items: center; gap: 6px;
    font-size: 12px; color: var(--muted);
    letter-spacing: .06em; text-transform: uppercase;
    padding: 28px 0 0;
  }
  .pdp-breadcrumb button {
    background: none; border: none; padding: 0;
    color: var(--muted); cursor: pointer;
    font-family: 'DM Sans', sans-serif; font-size: 12px;
    letter-spacing: .06em; text-transform: uppercase;
    transition: color .2s;
  }
  .pdp-breadcrumb button:hover { color: var(--ink); }
  .pdp-breadcrumb span { opacity: .4; }
  .pdp-breadcrumb .pdp-bc-current { opacity: 1; color: var(--ink); }

  /* ── two-column grid ── */
  .pdp-grid {
    display: grid; grid-template-columns: 1fr 1fr;
    gap: 56px; padding: 32px 0 80px; align-items: start;
  }
  @media (max-width: 860px) {
    .pdp-grid { grid-template-columns: 1fr; gap: 32px; }
  }

  /* ── image panel ── */
  .pdp-img-wrap {
    position: sticky; top: 24px;
    border-radius: var(--r); overflow: hidden;
    background: var(--surface); box-shadow: var(--shadow);
    aspect-ratio: 1 / 1;
  }
  .pdp-img-wrap img {
    width: 100%; height: 100%; object-fit: cover; display: block;
    transition: transform .6s ease;
  }
  .pdp-img-wrap:hover img { transform: scale(1.03); }
  .pdp-img-placeholder {
    width: 100%; height: 100%; display: flex;
    align-items: center; justify-content: center;
    background: #f5f2ee; color: var(--muted); font-size: 48px;
  }

  /* ── info panel ── */
  .pdp-category {
    font-size: 11px; letter-spacing: .12em; text-transform: uppercase;
    color: var(--accent); font-weight: 500; margin: 0 0 10px;
  }
  .pdp-name {
    font-family: 'DM Serif Display', serif;
    font-size: clamp(1.9rem, 4vw, 2.7rem);
    font-weight: 400; line-height: 1.15;
    margin: 0 0 18px; letter-spacing: -.01em;
  }

  /* rating row */
  .pdp-rating-row { display:flex; align-items:center; gap:10px; margin-bottom:20px; }
  .pdp-stars { color: var(--star); font-size: 15px; letter-spacing: 1px; }
  .pdp-rating-num { font-weight: 500; font-size: 14px; }
  .pdp-rating-count { font-size: 13px; color: var(--muted); }
  .pdp-rating-div { width: 1px; height: 14px; background: var(--border); }

  /* price */
  .pdp-price {
    font-family: 'DM Serif Display', serif;
    font-size: 2rem; font-weight: 400;
    color: var(--ink); margin: 0 0 14px;
  }

  /* stock badge */
  .pdp-badge {
    display: inline-flex; align-items: center; gap: 6px;
    padding: 4px 12px; border-radius: 99px;
    font-size: 12px; font-weight: 500; letter-spacing: .04em;
    margin-bottom: 24px;
  }
  .pdp-badge.in  { background: var(--accent-lt); color: var(--accent); }
  .pdp-badge.out { background: #fdeaea; color: var(--danger); }
  .pdp-badge-dot { width: 6px; height: 6px; border-radius: 50%; flex-shrink: 0; }
  .pdp-badge.in  .pdp-badge-dot { background: var(--accent); }
  .pdp-badge.out .pdp-badge-dot { background: var(--danger); }

  /* description */
  .pdp-desc {
    font-size: 14px; line-height: 1.75; color: var(--muted);
    margin: 0 0 28px; border-top: 1px solid var(--border); padding-top: 22px;
  }

  /* tags */
  .pdp-tags { display: flex; flex-wrap: wrap; gap: 8px; margin-bottom: 28px; }
  .pdp-tag {
    font-size: 11px; letter-spacing: .07em; text-transform: uppercase;
    padding: 4px 10px; border-radius: 4px;
    border: 1px solid var(--border); color: var(--muted);
  }

  /* quantity */
  .pdp-qty-label {
    font-size: 11px; letter-spacing: .08em; text-transform: uppercase;
    font-weight: 500; color: var(--muted); margin: 0 0 8px;
  }
  .pdp-qty-row { display: flex; align-items: center; margin-bottom: 24px; }
  .pdp-qty-btn {
    width: 36px; height: 36px; border: 1px solid var(--border);
    background: var(--surface); color: var(--ink);
    font-size: 18px; cursor: pointer; display: flex;
    align-items: center; justify-content: center; transition: background .15s;
  }
  .pdp-qty-btn:first-child { border-radius: 8px 0 0 8px; }
  .pdp-qty-btn:last-child  { border-radius: 0 8px 8px 0; }
  .pdp-qty-btn:hover { background: var(--bg); }
  .pdp-qty-num {
    width: 52px; height: 36px;
    border: 1px solid var(--border); border-left: none; border-right: none;
    text-align: center; font-family: 'DM Sans', sans-serif;
    font-size: 14px; background: var(--surface); color: var(--ink);
  }
  .pdp-qty-num:focus { outline: none; }

  /* CTA buttons */
  .pdp-btn-primary {
    width: 100%; padding: 15px;
    background: var(--ink); color: #fff;
    border: none; border-radius: var(--r);
    font-family: 'DM Sans', sans-serif; font-size: 14px;
    font-weight: 500; letter-spacing: .06em; text-transform: uppercase;
    cursor: pointer; transition: opacity .2s, transform .15s;
    margin-bottom: 10px;
  }
  .pdp-btn-primary:hover:not(:disabled) { opacity: .85; transform: translateY(-1px); }
  .pdp-btn-primary:disabled { background: #ccc; cursor: not-allowed; transform: none; }
  .pdp-btn-secondary {
    width: 100%; padding: 14px;
    background: transparent; color: var(--ink);
    border: 1px solid var(--border); border-radius: var(--r);
    font-family: 'DM Sans', sans-serif; font-size: 14px;
    cursor: pointer; transition: border-color .2s, background .2s;
  }
  .pdp-btn-secondary:hover { border-color: var(--ink); background: var(--bg); }

  /* meta */
  .pdp-meta {
    margin-top: 28px; border-top: 1px solid var(--border); padding-top: 20px;
  }
  .pdp-meta-title {
    font-size: 11px; letter-spacing: .1em; text-transform: uppercase;
    color: var(--muted); font-weight: 500; margin: 0 0 14px;
  }
  .pdp-meta-grid { display: grid; grid-template-columns: 1fr 1fr; gap: 10px 20px; }
  .pdp-meta-key { color: var(--muted); font-size: 11px; margin-bottom: 2px; }
  .pdp-meta-val { font-weight: 500; font-size: 13px; }

  /* ── reviews section ── */
  .pdp-reviews-section {
    border-top: 1px solid var(--border);
    padding: 52px 0 80px;
  }

  /* ── not found / loading ── */
  .pdp-center {
    display: flex; flex-direction: column;
    align-items: center; justify-content: center;
    min-height: 60vh; gap: 20px; text-align: center;
  }
  .pdp-center h4 {
    font-family: 'DM Serif Display', serif;
    font-size: 1.8rem; font-weight: 400; margin: 0;
  }
  .pdp-center p { color: var(--muted); font-size: 14px; margin: 0; }
`

// ── Stars helper ───────────────────────────────────────────────────────────
const Stars = ({ rating, size = 14 }) => {
  const full  = Math.floor(rating || 0)
  const half  = (rating || 0) % 1 >= 0.5
  const empty = 5 - full - (half ? 1 : 0)
  return (
    <span className="pdp-stars" style={{ fontSize: size }}>
      {'★'.repeat(full)}
      {half ? '⯨' : ''}
      {'☆'.repeat(Math.max(0, empty))}
    </span>
  )
}

// ── Page ───────────────────────────────────────────────────────────────────
export const ProductDetailsPage = () => {
  const { id }        = useParams()
  const navigate      = useNavigate()
  const { addToCart } = useCart()

  const [product,       setProduct]       = useState(null)
  const [isLoading,     setIsLoading]     = useState(true)
  const [quantity,      setQuantity]      = useState(1)
  const [reviewSummary, setReviewSummary] = useState({ averageRating: 0, totalReviews: 0 })

  useEffect(() => {
    if (!id) return
    loadProduct()
  }, [id]) // eslint-disable-line

  const loadProduct = async () => {
    setIsLoading(true)
    try {
      const [prod] = await Promise.all([
        productService.getProductById(id),
      ])
      setProduct(prod)
    } catch {
      toast.error('Failed to load product details')
      navigate('/')
    } finally {
      setIsLoading(false)
    }
  }

  // ReviewSummary is passed up from ProductReviews via a callback
  const handleSummaryLoaded = (summary) => {
    if (summary) setReviewSummary(summary)
  }

  const handleAddToCart = () => {
    if (!product) return
    addToCart({ ...product, quantity })
    toast.success(`${quantity} × ${product.name} added to cart`)
    setQuantity(1)
  }

  const tags = product?.tags
    ? (Array.isArray(product.tags)
        ? product.tags
        : product.tags.split(',').map(t => t.trim()).filter(Boolean))
    : []

  // ── Loading ──
  if (isLoading) {
    return (
      <div className="pdp-root">
        <style>{styles}</style>
        <LoadingSpinner />
      </div>
    )
  }

  // ── Not found ──
  if (!product) {
    return (
      <div className="pdp-root">
        <style>{styles}</style>
        <div className="pdp-container pdp-center">
          <h4>Product not found</h4>
          <p>The item you're looking for doesn't exist or has been removed.</p>
          <button className="pdp-btn-primary" style={{ width: 'auto', padding: '12px 32px' }}
            onClick={() => navigate('/')}>
            Back to Shop
          </button>
        </div>
      </div>
    )
  }

  const imgSrc = product.images?.length > 0 ? product.images[0].url : null

  return (
    <div className="pdp-root">
      <style>{styles}</style>

      <div className="pdp-container">

        {/* ── Breadcrumb ── */}
        <nav className="pdp-breadcrumb" aria-label="Breadcrumb">
          <button onClick={() => navigate('/')}>Home</button>
          <span>/</span>
          {product.categoryName && (
            <>
              <span>{product.categoryName}</span>
              <span>/</span>
            </>
          )}
          <span className="pdp-bc-current">{product.name}</span>
        </nav>

        {/* ── Two-column grid ── */}
        <div className="pdp-grid">

          {/* Left: Image */}
          <div className="pdp-img-wrap">
            {imgSrc
              ? <img src={imgSrc} alt={product.name} />
              : <div className="pdp-img-placeholder">🛍</div>
            }
          </div>

          {/* Right: Details */}
          <div>
            {product.categoryName && (
              <p className="pdp-category">{product.categoryName}</p>
            )}

            <h1 className="pdp-name">{product.name}</h1>

            {/* Rating row */}
            <div className="pdp-rating-row">
              <Stars rating={reviewSummary.averageRating} size={15} />
              <span className="pdp-rating-num">
                {Number(reviewSummary.averageRating || 0).toFixed(1)}
              </span>
              <div className="pdp-rating-div" />
              <span className="pdp-rating-count">
                {reviewSummary.totalReviews || 0} review{reviewSummary.totalReviews !== 1 ? 's' : ''}
              </span>
            </div>

            {/* Price */}
            <div className="pdp-price">${Number(product.price || 0).toFixed(2)}</div>

            {/* Stock badge */}
            <div className={`pdp-badge ${product.inStock ? 'in' : 'out'}`}>
              <span className="pdp-badge-dot" />
              {product.inStock ? 'In Stock' : 'Out of Stock'}
            </div>

            {/* Description */}
            {product.description && (
              <p className="pdp-desc">{product.description}</p>
            )}

            {/* Tags */}
            {tags.length > 0 && (
              <div className="pdp-tags">
                {tags.map((t, i) => <span key={i} className="pdp-tag">{t}</span>)}
              </div>
            )}

            {/* Quantity */}
            <p className="pdp-qty-label">Quantity</p>
            <div className="pdp-qty-row">
              <button className="pdp-qty-btn"
                onClick={() => setQuantity(q => Math.max(1, q - 1))}>−</button>
              <input
                type="number" min="1" className="pdp-qty-num"
                value={quantity}
                onChange={e => setQuantity(Math.max(1, parseInt(e.target.value) || 1))}
              />
              <button className="pdp-qty-btn"
                onClick={() => setQuantity(q => q + 1)}>+</button>
            </div>

            {/* CTA */}
            <button
              className="pdp-btn-primary"
              onClick={handleAddToCart}
              disabled={!product.inStock}
            >
              {product.inStock ? 'Add to Cart' : 'Out of Stock'}
            </button>
            <button className="pdp-btn-secondary" onClick={() => navigate('/')}>
              Continue Shopping
            </button>

            {/* Meta */}
            <div className="pdp-meta">
              <p className="pdp-meta-title">Product Details</p>
              <div className="pdp-meta-grid">
                {[
                  ['SKU',       product.sku                ?? 'N/A'],
                  ['Brand',     product.brand              ?? 'N/A'],
//                   ['Available', product.availableQuantity  ?? 'N/A'],
//                   ['ID',        product.id],
                ].map(([k, v]) => (
                  <div key={k}>
                    <div className="pdp-meta-key">{k}</div>
                    <div className="pdp-meta-val">{v}</div>
                  </div>
                ))}
              </div>
            </div>
          </div>
        </div>
      </div>

      {/* ── Full-width Reviews ── */}
      <div className="pdp-reviews-section">
        <div className="pdp-container">
          <ProductReviews
            productId={product.id}
            onSummaryLoaded={handleSummaryLoaded}
          />
        </div>
      </div>

    </div>
  )
}

export default ProductDetailsPage