import React, { useEffect, useState, useCallback } from 'react'
import toast from 'react-hot-toast'
import { reviewService } from '../api/reviewService'
import { orderService } from '../api/orderService'   // adjust path if needed

// ── Font injection ─────────────────────────────────────────────────────────
const _fl = document.createElement('link')
_fl.rel = 'stylesheet'
_fl.href = 'https://fonts.googleapis.com/css2?family=Sora:wght@300;400;500;600&family=DM+Serif+Display:ital@0;1&display=swap'
if (!document.head.querySelector('[href*="Sora"]')) document.head.appendChild(_fl)

// ── CSS ────────────────────────────────────────────────────────────────────
const css = `
  .pr-root * { box-sizing: border-box; }
  .pr-root {
    font-family: 'Sora', sans-serif;
    max-width: 760px;
    color: #1a1a1a;
  }

  .pr-heading { display:flex; align-items:baseline; gap:10px; margin:0 0 24px; }
  .pr-heading-title {
    font-family:'DM Serif Display',serif;
    font-size:1.5rem; font-weight:400; color:#1a1a1a; margin:0;
  }
  .pr-heading-count { font-size:13px; color:#999; }

  .pr-topbar {
    display:flex; align-items:center; justify-content:space-between;
    flex-wrap:wrap; gap:16px; margin-bottom:28px;
  }

  .pr-summary {
    display:flex; align-items:center; gap:28px;
    background:#fff; border:1px solid #ebebeb; border-radius:14px;
    padding:18px 24px; flex:1; min-width:220px;
  }
  .pr-avg-num {
    font-family:'DM Serif Display',serif;
    font-size:2.6rem; color:#1a1a1a; line-height:1;
  }
  .pr-avg-stars { display:flex; gap:3px; margin:5px 0 3px; }
  .pr-avg-label { font-size:11px; color:#aaa; }
  .pr-vdiv { width:1px; height:52px; background:#f0f0f0; flex-shrink:0; }
  .pr-bars { flex:1; display:flex; flex-direction:column; gap:6px; }
  .pr-bar-row { display:flex; align-items:center; gap:8px; }
  .pr-bar-label { font-size:11px; color:#999; width:12px; text-align:right; flex-shrink:0; }
  .pr-bar-track { flex:1; height:5px; background:#f0f0f0; border-radius:100px; overflow:hidden; }
  .pr-bar-fill {
    height:100%; border-radius:100px;
    background:linear-gradient(90deg,#f5a623,#e8640c);
    transition:width .6s cubic-bezier(.4,0,.2,1);
  }
  .pr-bar-pct { font-size:11px; color:#ccc; width:26px; text-align:right; flex-shrink:0; }

  .pr-leave-btn {
    background:#1a1a1a; color:#fff; border:none;
    border-radius:10px; padding:12px 22px;
    font-family:'Sora',sans-serif; font-size:13px; font-weight:600;
    cursor:pointer; letter-spacing:.04em; white-space:nowrap;
    transition:background .2s, transform .1s; flex-shrink:0;
  }
  .pr-leave-btn:hover { background:#e8640c; }
  .pr-leave-btn:active { transform:scale(.97); }

  .pr-list { display:flex; flex-direction:column; gap:12px; }
  .pr-review-card {
    background:#fff; border:1px solid #ebebeb; border-radius:14px;
    padding:20px 22px; transition:box-shadow .18s;
  }
  .pr-review-card:hover { box-shadow:0 4px 20px rgba(0,0,0,.06); }
  .pr-review-top {
    display:flex; align-items:flex-start;
    justify-content:space-between; gap:12px; margin-bottom:10px;
  }
  .pr-reviewer { display:flex; align-items:center; gap:10px; }
  .pr-avatar {
    width:36px; height:36px; border-radius:50%;
    display:flex; align-items:center; justify-content:center;
    font-size:12px; font-weight:600; flex-shrink:0;
  }
  .pr-reviewer-name { font-size:13.5px; font-weight:600; color:#1a1a1a; margin:0 0 2px; }
  .pr-reviewer-date { font-size:11px; color:#bbb; }
  .pr-rating-badge {
    display:flex; align-items:center; gap:4px;
    background:#fffbf0; border:1px solid #fde8b0;
    border-radius:100px; padding:4px 10px; flex-shrink:0;
    font-size:12px; font-weight:600; color:#a06000;
  }
  .pr-review-title { font-size:13.5px; font-weight:600; color:#1a1a1a; margin:8px 0 5px; }
  .pr-review-body { font-size:13px; color:#666; line-height:1.65; margin:0; }

  .pr-stars { display:flex; gap:2px; }
  .pr-star { font-size:12px; }

  .pr-seemore-wrap { display:flex; justify-content:center; margin-top:16px; }
  .pr-seemore-btn {
    background:transparent; border:1px solid #e0e0e0; color:#555;
    border-radius:10px; padding:10px 28px;
    font-family:'Sora',sans-serif; font-size:13px; font-weight:500;
    cursor:pointer; transition:all .18s;
  }
  .pr-seemore-btn:hover { border-color:#1a1a1a; color:#1a1a1a; }

  .pr-empty {
    text-align:center; padding:40px 24px; color:#bbb;
    border:1px dashed #e8e8e8; border-radius:14px;
  }
  .pr-empty-icon { font-size:32px; margin-bottom:10px; opacity:.5; }
  .pr-empty-text { font-size:13.5px; }

  /* ── Modal ── */
  .pr-overlay {
    position:fixed; inset:0; z-index:9000;
    background:rgba(0,0,0,.45);
    display:flex; align-items:center; justify-content:center;
    padding:24px; animation:pr-fade .18s ease;
  }
  @keyframes pr-fade { from{opacity:0} to{opacity:1} }
  .pr-modal {
    background:#fff; border-radius:18px;
    width:100%; max-width:520px; padding:32px 32px 28px;
    position:relative; animation:pr-up .22s ease;
    max-height:90vh; overflow-y:auto;
  }
  @keyframes pr-up {
    from{opacity:0;transform:translateY(16px)}
    to{opacity:1;transform:translateY(0)}
  }
  .pr-modal-close {
    position:absolute; top:18px; right:18px;
    background:#f5f5f5; border:none; width:30px; height:30px;
    border-radius:50%; display:flex; align-items:center; justify-content:center;
    cursor:pointer; font-size:15px; color:#555; transition:background .15s;
  }
  .pr-modal-close:hover { background:#eee; }
  .pr-modal-title {
    font-family:'DM Serif Display',serif;
    font-size:1.4rem; font-weight:400; color:#1a1a1a; margin:0 0 6px;
  }
  .pr-modal-sub { font-size:13px; color:#999; margin:0 0 24px; }

  .pr-star-picker { display:flex; gap:6px; margin-bottom:20px; }
  .pr-star-btn {
    background:none; border:none; cursor:pointer; padding:0;
    font-size:28px; line-height:1;
    filter:grayscale(1) opacity(.3);
    transition:filter .15s, transform .1s;
  }
  .pr-star-btn.on { filter:none; }
  .pr-star-btn:hover { transform:scale(1.18); }

  .pr-field { margin-bottom:12px; }
  .pr-field-label {
    display:block; font-size:11px; font-weight:600;
    letter-spacing:.08em; text-transform:uppercase;
    color:#999; margin-bottom:6px;
  }
  .pr-input {
    width:100%; border:1px solid #e0e0e0; border-radius:10px;
    padding:11px 14px; font-size:13.5px;
    font-family:'Sora',sans-serif; outline:none;
    transition:border-color .18s; background:#fafaf9;
    color:#1a1a1a; resize:none;
  }
  .pr-input::placeholder { color:#bbb; }
  .pr-input:focus { border-color:#e8640c; background:#fff; }
  .pr-textarea { min-height:88px; line-height:1.6; }

  .pr-modal-footer { display:flex; justify-content:flex-end; gap:10px; margin-top:20px; }
  .pr-cancel-btn {
    background:transparent; border:1px solid #e0e0e0; color:#555;
    border-radius:10px; padding:10px 20px;
    font-family:'Sora',sans-serif; font-size:13px; font-weight:500;
    cursor:pointer; transition:all .15s;
  }
  .pr-cancel-btn:hover { border-color:#ccc; color:#1a1a1a; }
  .pr-submit-btn {
    background:#1a1a1a; color:#fff; border:none;
    border-radius:10px; padding:11px 28px;
    font-family:'Sora',sans-serif; font-size:13px; font-weight:600;
    cursor:pointer; letter-spacing:.04em; transition:background .2s, transform .1s;
  }
  .pr-submit-btn:hover { background:#e8640c; }
  .pr-submit-btn:active { transform:scale(.97); }
  .pr-submit-btn:disabled { background:#ccc; cursor:not-allowed; }

  .pr-notice {
    display:flex; align-items:flex-start; gap:10px;
    background:#fafaf0; border:1px solid #f0e8b0;
    border-radius:10px; padding:14px 16px;
    font-size:13px; color:#7a6000; line-height:1.55;
  }
  .pr-notice-icon { font-size:16px; flex-shrink:0; }

  .pr-checking {
    display:flex; align-items:center; gap:10px;
    color:#999; font-size:13px; padding:8px 0;
  }
  .pr-check-spin {
    width:18px; height:18px; border:2px solid #eee;
    border-top-color:#e8640c; border-radius:50%;
    animation:pr-spin .7s linear infinite; flex-shrink:0;
  }
  @keyframes pr-spin { to{transform:rotate(360deg)} }
`

// ── Helpers ────────────────────────────────────────────────────────────────
const PALETTE = [
  { bg: '#fff3e0', color: '#c04808' },
  { bg: '#e8f4e8', color: '#2e7d32' },
  { bg: '#e8eaf6', color: '#3949ab' },
  { bg: '#fce4ec', color: '#c2185b' },
  { bg: '#e0f7fa', color: '#00838f' },
  { bg: '#f3e5f5', color: '#7b1fa2' },
]

const avatarStyle = (name = '') => PALETTE[(name.charCodeAt(0) || 0) % PALETTE.length]

const initials = (name = '') =>
  name.split(' ').map(w => w[0]).join('').slice(0, 2).toUpperCase() || '?'

// Never expose an email address — strip domain
const safeUsername = (raw = '') => {
  if (!raw) return 'Anonymous'
  if (raw.includes('@')) return raw.split('@')[0]
  return raw
}

const fmtDate = (d) => {
  if (!d) return ''
  try {
    return new Date(d).toLocaleDateString('en-US', { year: 'numeric', month: 'short', day: 'numeric' })
  } catch { return '' }
}

// ── Sub-components ─────────────────────────────────────────────────────────
const Stars = ({ rating, size = 12 }) => (
  <span className="pr-stars" aria-label={`${rating} out of 5`}>
    {[1, 2, 3, 4, 5].map(v => (
      <span key={v} className="pr-star"
        style={{ fontSize: size, color: v <= rating ? '#f5a623' : '#e0e0e0' }}>★</span>
    ))}
  </span>
)

const RatingBar = ({ star, count, total }) => {
  const pct = total > 0 ? Math.round((count / total) * 100) : 0
  return (
    <div className="pr-bar-row">
      <span className="pr-bar-label">{star}</span>
      <div className="pr-bar-track"><div className="pr-bar-fill" style={{ width: `${pct}%` }} /></div>
      <span className="pr-bar-pct">{pct}%</span>
    </div>
  )
}

// ── Review Modal ───────────────────────────────────────────────────────────
const ReviewModal = ({ productId, onClose, onSuccess }) => {
  const [step, setStep]             = useState('checking') // checking | blocked | form
  const [hoveredStar, setHovered]   = useState(null)
  const [submitting, setSubmitting] = useState(false)
  const [formData, setFormData]     = useState({ rating: 5, title: '', body: '' })

  useEffect(() => {
    const check = async () => {
      try {
        const orders = await orderService.getMyOrders()
        const hasCompleted = orders.some(
          o =>
            o.status === 'COMPLETED' &&
            (o.items || o.orderItems || []).some(
              item => String(item.productId ?? item.product?.id) === String(productId)
            )
        )
        setStep(hasCompleted ? 'form' : 'blocked')
      } catch {
        // If order service is unavailable, allow submission
        setStep('form')
      }
    }
    check()
  }, [productId])

  const handleSubmit = async () => {
    if (!formData.title.trim() || !formData.body.trim()) {
      toast.error('Please add a title and review text')
      return
    }
    setSubmitting(true)
    try {
      await reviewService.createReview(productId, formData)
      toast.success('Review submitted — thank you!')
      onSuccess?.()
      onClose()
    } catch (err) {
      toast.error(err.message || 'Failed to submit review')
    } finally {
      setSubmitting(false)
    }
  }

  const displayRating = hoveredStar ?? formData.rating

  return (
    <div className="pr-overlay" onClick={e => e.target === e.currentTarget && onClose()}>
      <div className="pr-modal" role="dialog" aria-modal="true">
        <button className="pr-modal-close" onClick={onClose} aria-label="Close">✕</button>

        {step === 'checking' && (
          <>
            <p className="pr-modal-title">Leave a Review</p>
            <div className="pr-checking">
              <div className="pr-check-spin" />
              Verifying your purchase…
            </div>
          </>
        )}

        {step === 'blocked' && (
          <>
            <p className="pr-modal-title">Purchase Required</p>
            <p className="pr-modal-sub">Reviews are only available for verified buyers.</p>
            <div className="pr-notice">
              <span className="pr-notice-icon">ℹ</span>
              You can leave a review once you have a completed order containing this product.
            </div>
            <div className="pr-modal-footer">
              <button className="pr-cancel-btn" onClick={onClose}>Close</button>
            </div>
          </>
        )}

        {step === 'form' && (
          <>
            <p className="pr-modal-title">Leave a Review</p>
            <p className="pr-modal-sub">Share your honest experience with this product.</p>

            <div className="pr-star-picker" role="group" aria-label="Select rating">
              {[1, 2, 3, 4, 5].map(v => (
                <button
                  key={v}
                  className={`pr-star-btn ${v <= displayRating ? 'on' : ''}`}
                  onMouseEnter={() => setHovered(v)}
                  onMouseLeave={() => setHovered(null)}
                  onClick={() => setFormData(f => ({ ...f, rating: v }))}
                  aria-label={`${v} star${v !== 1 ? 's' : ''}`}
                >
                  ★
                </button>
              ))}
            </div>

            <div className="pr-field">
              <label className="pr-field-label">Review title</label>
              <input
                className="pr-input"
                placeholder="Summarise your experience"
                value={formData.title}
                onChange={e => setFormData(f => ({ ...f, title: e.target.value }))}
                maxLength={120}
              />
            </div>

            <div className="pr-field">
              <label className="pr-field-label">Your review</label>
              <textarea
                className="pr-input pr-textarea"
                placeholder="What did you like or dislike? How was the quality?"
                value={formData.body}
                onChange={e => setFormData(f => ({ ...f, body: e.target.value }))}
                maxLength={1200}
              />
            </div>

            <div className="pr-modal-footer">
              <button className="pr-cancel-btn" onClick={onClose}>Cancel</button>
              <button className="pr-submit-btn" onClick={handleSubmit} disabled={submitting}>
                {submitting ? 'Submitting…' : 'Submit Review'}
              </button>
            </div>
          </>
        )}
      </div>
    </div>
  )
}

// ── Main component ─────────────────────────────────────────────────────────
const PREVIEW_COUNT = 2

const ProductReviews = ({ productId, onSummaryLoaded }) => {
  const [reviews,   setReviews]   = useState([])
  const [summary,   setSummary]   = useState({})
  const [expanded,  setExpanded]  = useState(false)
  const [showModal, setShowModal] = useState(false)

  const loadReviews = useCallback(async () => {
    try {
      const data = await reviewService.getReviews(productId)
      setReviews(data.content || [])
    } catch { toast.error('Failed to load reviews') }
  }, [productId])

  const loadSummary = useCallback(async () => {
    try {
      const data = await reviewService.getReviewSummary(productId)
      setSummary(data)
      onSummaryLoaded?.(data)
    } catch { /* silent */ }
  }, [productId, onSummaryLoaded])

  useEffect(() => { loadReviews(); loadSummary() }, [loadReviews, loadSummary])

  const totalReviews   = summary.totalReviews || 0
  const avgRating      = summary.averageRating ? Number(summary.averageRating).toFixed(1) : null
  const distribution   = summary.ratingDistribution || {}
  const visibleReviews = expanded ? reviews : reviews.slice(0, PREVIEW_COUNT)
  const hasMore        = reviews.length > PREVIEW_COUNT

  return (
    <>
      <style>{css}</style>
      <div className="pr-root">

        <div className="pr-heading">
          <h2 className="pr-heading-title">Customer Reviews</h2>
          {totalReviews > 0 && (
            <span className="pr-heading-count">
              {totalReviews.toLocaleString()} review{totalReviews !== 1 ? 's' : ''}
            </span>
          )}
        </div>

        <div className="pr-topbar">
          {totalReviews > 0 && avgRating && (
            <div className="pr-summary">
              <div>
                <div className="pr-avg-num">{avgRating}</div>
                <div className="pr-avg-stars">
                  <Stars rating={Math.round(Number(summary.averageRating || 0))} size={15}/>
                </div>
                <div className="pr-avg-label">out of 5</div>
              </div>
              <div className="pr-vdiv"/>
              <div className="pr-bars">
                {[5, 4, 3, 2, 1].map(s => (
                  <RatingBar key={s} star={s} count={distribution[s] || 0} total={totalReviews}/>
                ))}
              </div>
            </div>
          )}
          <button className="pr-leave-btn" onClick={() => setShowModal(true)}>
            Leave a Review
          </button>
        </div>

        {reviews.length === 0 ? (
          <div className="pr-empty">
            <div className="pr-empty-icon">✦</div>
            <p className="pr-empty-text">No reviews yet — be the first to share your experience.</p>
          </div>
        ) : (
          <>
            <div className="pr-list">
              {visibleReviews.map(r => {
                const palette = avatarStyle(r.username || '')
                const name    = safeUsername(r.username)
                return (
                  <div className="pr-review-card" key={r.id}>
                    <div className="pr-review-top">
                      <div className="pr-reviewer">
                        <div className="pr-avatar"
                          style={{ background: palette.bg, color: palette.color }}
                          aria-hidden="true">
                          {initials(name)}
                        </div>
                        <div>
                          <p className="pr-reviewer-name">{name}</p>
                          <p className="pr-reviewer-date">{fmtDate(r.createdAt)}</p>
                        </div>
                      </div>
                      <div className="pr-rating-badge">
                        <span style={{ color: '#f5a623', fontSize: 11 }}>★</span>
                        {r.rating}/5
                      </div>
                    </div>
                    <Stars rating={r.rating} size={12}/>
                    {r.title && <p className="pr-review-title">{r.title}</p>}
                    <p className="pr-review-body">{r.body}</p>
                  </div>
                )
              })}
            </div>

            {hasMore && (
              <div className="pr-seemore-wrap">
                <button className="pr-seemore-btn" onClick={() => setExpanded(e => !e)}>
                  {expanded ? 'Show fewer reviews' : `See all ${reviews.length} reviews`}
                </button>
              </div>
            )}
          </>
        )}
      </div>

      {showModal && (
        <ReviewModal
          productId={productId}
          onClose={() => setShowModal(false)}
          onSuccess={() => { loadReviews(); loadSummary() }}
        />
      )}
    </>
  )
}

export default ProductReviews