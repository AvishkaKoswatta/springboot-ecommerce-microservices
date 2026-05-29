import React, { useState, useEffect, useCallback, useRef } from 'react'

import { useNavigate, useSearchParams } from 'react-router-dom'

import {

  Search, X, SlidersHorizontal, ChevronDown, ChevronUp,

  Laptop, Shirt, Home, Sparkles, Dumbbell, BookOpen,

  Baby, Apple, Car, Heart, Tag, ShoppingBag,

  ArrowUpDown, Star, Package, Flame, ChevronLeft, ChevronRight,

  LayoutGrid, Zap, Monitor, Smartphone

} from 'lucide-react'

import { productService } from '../../api/productService'

import { categoryService } from '../../api/categoryService'

import { useCart } from '../../context/CartContext'

import ProductCard from '../../components/ProductCard'

import toast from 'react-hot-toast'


// ─── Google Font injection ────────────────────────────────────────────────────

const fontLink = document.createElement('link')

fontLink.rel = 'stylesheet'

fontLink.href = 'https://fonts.googleapis.com/css2?family=DM+Serif+Display:ital@0;1&family=DM+Sans:wght@300;400;500;600&display=swap'

if (!document.head.querySelector('[href*="DM+Serif"]')) document.head.appendChild(fontLink)


// ─── constants ────────────────────────────────────────────────────────────────

const SORT_OPTIONS = [

  { label: 'Newest First',     sortBy: 'createdAt', sortDir: 'desc' },

  { label: 'Oldest First',     sortBy: 'createdAt', sortDir: 'asc'  },

  { label: 'Price: Low → High',sortBy: 'price',     sortDir: 'asc'  },

  { label: 'Price: High → Low',sortBy: 'price',     sortDir: 'desc' },

  { label: 'Name: A → Z',      sortBy: 'name',      sortDir: 'asc'  },

  { label: 'Name: Z → A',      sortBy: 'name',      sortDir: 'desc' },

]


const PRICE_PRESETS = [

  { label: 'Under $25',   min: '',    max: '25'  },

  { label: '$25 – $100',  min: '25',  max: '100' },

  { label: '$100 – $500', min: '100', max: '500' },

  { label: 'Over $500',   min: '500', max: ''    },

]


const CATEGORY_ICONS = {

  electronics: Monitor, laptop: Laptop, mobile: Smartphone,

  fashion: Shirt, cloths: Shirt, clothing: Shirt,

  home: Home, beauty: Sparkles, sports: Dumbbell,

  books: BookOpen, toys: Baby, food: Apple,

  automotive: Car, health: Heart,

}


const getCategoryIcon = (name = '') => {

  const key = name.toLowerCase().replace(/\s+/g, '')

  for (const [k, Icon] of Object.entries(CATEGORY_ICONS)) {

    if (key.includes(k)) return Icon

  }

  return Tag

}


const PAGE_SIZE = 12


const defaultFilters = {

  keyword: '', categoryId: null, categoryIds: null,

  brand: '', minPrice: '', maxPrice: '', sortIdx: 0, page: 0,

}


function activeFilterCount(f) {

  return [f.keyword, f.categoryId, f.brand, f.minPrice, f.maxPrice].filter(Boolean).length

}


// ─── styles ───────────────────────────────────────────────────────────────────

const css = `

  .hp-root * { box-sizing: border-box; }

  .hp-root {

    font-family: 'DM Sans', sans-serif;

    background: #f8f7f4;

    min-height: 100vh;

    color: #1a1a1a;

  }


  /* Hero */

  .hp-hero {

    background: #0f1923;

    padding: 64px 0 56px;

    position: relative;

    overflow: hidden;

  }

  .hp-hero::before {

    content: '';

    position: absolute;

    inset: 0;

    background: radial-gradient(ellipse 80% 60% at 60% 50%, rgba(255,140,0,0.12) 0%, transparent 70%),

                radial-gradient(ellipse 50% 80% at 20% 80%, rgba(0,180,255,0.07) 0%, transparent 60%);

  }

  .hp-hero-inner {

    max-width: 1200px;

    margin: 0 auto;

    padding: 0 24px;

    position: relative;

    z-index: 1;

    display: grid;

    grid-template-columns: 1fr auto;

    gap: 48px;

    align-items: center;

  }

  .hp-hero-eyebrow {

    display: inline-flex;

    align-items: center;

    gap: 6px;

    background: rgba(255,140,0,0.15);

    border: 1px solid rgba(255,140,0,0.3);

    color: #ff8c00;

    font-size: 11px;

    font-weight: 600;

    letter-spacing: 0.1em;

    text-transform: uppercase;

    padding: 5px 12px;

    border-radius: 100px;

    margin-bottom: 16px;

  }

  .hp-hero h1 {

    font-family: 'DM Serif Display', serif;

    font-size: clamp(2rem, 4vw, 3rem);

    color: #fff;

    line-height: 1.15;

    margin: 0 0 12px;

    font-weight: 400;

  }

  .hp-hero h1 em {

    font-style: italic;

    color: #ff8c00;

  }

  .hp-hero-sub {

    color: rgba(255,255,255,0.55);

    font-size: 15px;

    margin: 0 0 28px;

    font-weight: 300;

  }

  .hp-search-wrap {

    display: flex;

    background: rgba(255,255,255,0.08);

    border: 1px solid rgba(255,255,255,0.15);

    border-radius: 12px;

    overflow: hidden;

    backdrop-filter: blur(8px);

    transition: border-color 0.2s;

    max-width: 520px;

  }

  .hp-search-wrap:focus-within {

    border-color: rgba(255,140,0,0.6);

    background: rgba(255,255,255,0.12);

  }

  .hp-search-icon {

    display: flex;

    align-items: center;

    padding: 0 14px;

    color: rgba(255,255,255,0.4);

  }

  .hp-search-input {

    flex: 1;

    background: transparent;

    border: none;

    outline: none;

    color: #fff;

    font-size: 14px;

    font-family: 'DM Sans', sans-serif;

    padding: 14px 0;

  }

  .hp-search-input::placeholder { color: rgba(255,255,255,0.35); }

  .hp-search-clear {

    display: flex;

    align-items: center;

    padding: 0 10px;

    background: transparent;

    border: none;

    cursor: pointer;

    color: rgba(255,255,255,0.4);

  }

  .hp-search-clear:hover { color: rgba(255,255,255,0.8); }

  .hp-search-btn {

    background: #ff8c00;

    border: none;

    color: #fff;

    font-family: 'DM Sans', sans-serif;

    font-size: 13px;

    font-weight: 600;

    padding: 0 22px;

    cursor: pointer;

    transition: background 0.2s;

    letter-spacing: 0.03em;

  }

  .hp-search-btn:hover { background: #e07800; }

  .hp-hero-badge {

    background: rgba(255,255,255,0.05);

    border: 1px solid rgba(255,255,255,0.1);

    border-radius: 16px;

    padding: 24px;

    display: flex;

    flex-direction: column;

    gap: 16px;

    min-width: 160px;

  }

  .hp-hero-stat { text-align: center; }

  .hp-hero-stat-num {

    font-family: 'DM Serif Display', serif;

    font-size: 1.8rem;

    color: #ff8c00;

    line-height: 1;

  }

  .hp-hero-stat-label {

    font-size: 11px;

    color: rgba(255,255,255,0.45);

    text-transform: uppercase;

    letter-spacing: 0.08em;

    margin-top: 4px;

  }

  .hp-hero-divider {

    height: 1px;

    background: rgba(255,255,255,0.08);

  }


  /* Main layout */

  .hp-main {

    max-width: 1200px;

    margin: 0 auto;

    padding: 32px 24px 64px;

    display: grid;

    grid-template-columns: 260px 1fr;

    gap: 32px;

    align-items: start;

  }

  @media (max-width: 900px) {

    .hp-main { grid-template-columns: 1fr; }

    .hp-sidebar { display: none; }

    .hp-hero-inner { grid-template-columns: 1fr; }

    .hp-hero-badge { display: none; }

  }


  /* Sidebar */

  .hp-sidebar {

    position: sticky;

    top: 24px;

    display: flex;

    flex-direction: column;

    gap: 8px;

  }

  .hp-filter-card {

    background: #fff;

    border-radius: 14px;

    border: 1px solid #ebebeb;

    overflow: hidden;

  }

  .hp-filter-header {

    display: flex;

    align-items: center;

    justify-content: space-between;

    padding: 16px 18px;

    cursor: pointer;

    user-select: none;

    border-bottom: 1px solid #f0f0f0;

  }

  .hp-filter-header-title {

    display: flex;

    align-items: center;

    gap: 8px;

    font-size: 13px;

    font-weight: 600;

    color: #1a1a1a;

    letter-spacing: 0.02em;

    text-transform: uppercase;

  }

  .hp-filter-body { padding: 16px 18px; }


  /* Category list in sidebar */

  .hp-cat-item {

    display: flex;

    align-items: center;

    gap: 10px;

    padding: 9px 12px;

    border-radius: 9px;

    cursor: pointer;

    font-size: 13.5px;

    color: #444;

    font-weight: 400;

    transition: all 0.15s;

    margin-bottom: 2px;

  }

  .hp-cat-item:hover { background: #f5f5f5; color: #1a1a1a; }

  .hp-cat-item.active {

    background: #fff5e6;

    color: #cc6f00;

    font-weight: 600;

  }

  .hp-cat-item-icon { opacity: 0.6; flex-shrink: 0; }

  .hp-cat-item.active .hp-cat-item-icon { opacity: 1; }

  .hp-cat-count {

    margin-left: auto;

    font-size: 11px;

    color: #aaa;

    background: #f5f5f5;

    padding: 2px 7px;

    border-radius: 100px;

  }

  .hp-cat-item.active .hp-cat-count {

    background: rgba(255,140,0,0.15);

    color: #cc6f00;

  }


  /* Price inputs */

  .hp-price-row { display: flex; gap: 8px; align-items: center; }

  .hp-price-input {

    flex: 1;

    border: 1px solid #e0e0e0;

    border-radius: 8px;

    padding: 9px 12px;

    font-size: 13px;

    font-family: 'DM Sans', sans-serif;

    outline: none;

    transition: border-color 0.15s;

    background: #fafafa;

    color: #1a1a1a;

  }

  .hp-price-input:focus { border-color: #ff8c00; background: #fff; }

  .hp-price-sep { color: #ccc; font-size: 13px; flex-shrink: 0; }

  .hp-price-presets { display: flex; flex-wrap: wrap; gap: 6px; margin-top: 10px; }

  .hp-price-preset {

    border: 1px solid #e0e0e0;

    background: #fafafa;

    color: #555;

    font-size: 12px;

    font-family: 'DM Sans', sans-serif;

    padding: 5px 10px;

    border-radius: 7px;

    cursor: pointer;

    transition: all 0.15s;

    font-weight: 400;

  }

  .hp-price-preset:hover { border-color: #ff8c00; color: #ff8c00; }

  .hp-price-preset.active { background: #ff8c00; border-color: #ff8c00; color: #fff; font-weight: 600; }


  /* Brand select */

  .hp-select {

    width: 100%;

    border: 1px solid #e0e0e0;

    border-radius: 8px;

    padding: 9px 12px;

    font-size: 13px;

    font-family: 'DM Sans', sans-serif;

    outline: none;

    background: #fafafa;

    color: #1a1a1a;

    cursor: pointer;

    transition: border-color 0.15s;

  }

  .hp-select:focus { border-color: #ff8c00; }


  /* Sidebar apply btn */

  .hp-apply-btn {

    width: 100%;

    background: #0f1923;

    color: #fff;

    border: none;

    border-radius: 9px;

    padding: 12px;

    font-family: 'DM Sans', sans-serif;

    font-size: 13px;

    font-weight: 600;

    cursor: pointer;

    transition: background 0.2s;

    margin-top: 4px;

    letter-spacing: 0.03em;

  }

  .hp-apply-btn:hover { background: #ff8c00; }

  .hp-reset-btn {

    width: 100%;

    background: transparent;

    color: #888;

    border: 1px solid #e8e8e8;

    border-radius: 9px;

    padding: 10px;

    font-family: 'DM Sans', sans-serif;

    font-size: 12px;

    font-weight: 500;

    cursor: pointer;

    transition: all 0.15s;

    margin-top: 6px;

  }

  .hp-reset-btn:hover { border-color: #ccc; color: #333; }


  /* Content area */

  .hp-content {}


  /* Mobile filter bar */

  .hp-mobile-bar {

    display: none;

    gap: 10px;

    margin-bottom: 20px;

    align-items: center;

  }

  @media (max-width: 900px) { .hp-mobile-bar { display: flex; } }

  .hp-mobile-filter-btn {

    display: flex;

    align-items: center;

    gap: 7px;

    background: #fff;

    border: 1px solid #e0e0e0;

    border-radius: 9px;

    padding: 9px 16px;

    font-family: 'DM Sans', sans-serif;

    font-size: 13px;

    font-weight: 500;

    cursor: pointer;

    color: #1a1a1a;

    transition: all 0.15s;

  }

  .hp-mobile-filter-btn:hover { border-color: #ff8c00; color: #ff8c00; }

  .hp-mobile-filter-btn.has-filters { background: #0f1923; color: #fff; border-color: #0f1923; }


  /* Category pills (mobile / top) */

  .hp-cat-pills {

    display: flex;

    gap: 8px;

    flex-wrap: nowrap;

    overflow-x: auto;

    padding-bottom: 4px;

    margin-bottom: 24px;

    scrollbar-width: none;

  }

  .hp-cat-pills::-webkit-scrollbar { display: none; }

  .hp-cat-pill {

    display: inline-flex;

    align-items: center;

    gap: 6px;

    white-space: nowrap;

    border: 1px solid #e0e0e0;

    background: #fff;

    color: #555;

    font-size: 13px;

    font-family: 'DM Sans', sans-serif;

    font-weight: 500;

    padding: 8px 14px;

    border-radius: 100px;

    cursor: pointer;

    transition: all 0.15s;

    flex-shrink: 0;

  }

  .hp-cat-pill:hover { border-color: #ff8c00; color: #ff8c00; }

  .hp-cat-pill.active { background: #ff8c00; border-color: #ff8c00; color: #fff; }


  /* Toolbar */

  .hp-toolbar {

    display: flex;

    align-items: center;

    justify-content: space-between;

    margin-bottom: 20px;

    gap: 12px;

    flex-wrap: wrap;

  }

  .hp-toolbar-left {

    display: flex;

    align-items: baseline;

    gap: 10px;

  }

  .hp-toolbar-title {

    font-family: 'DM Serif Display', serif;

    font-size: 1.4rem;

    color: #1a1a1a;

    font-weight: 400;

  }

  .hp-toolbar-count {

    font-size: 13px;

    color: #999;

    font-weight: 400;

  }

  .hp-sort-select {

    display: flex;

    align-items: center;

    gap: 6px;

    border: 1px solid #e0e0e0;

    border-radius: 9px;

    padding: 8px 12px;

    font-family: 'DM Sans', sans-serif;

    font-size: 13px;

    background: #fff;

    color: #333;

    cursor: pointer;

    outline: none;

    transition: border-color 0.15s;

  }

  .hp-sort-select:focus { border-color: #ff8c00; }


  /* Active chips */

  .hp-chips { display: flex; gap: 8px; flex-wrap: wrap; margin-bottom: 20px; }

  .hp-chip {

    display: inline-flex;

    align-items: center;

    gap: 6px;

    background: #fff5e6;

    border: 1px solid rgba(255,140,0,0.25);

    color: #cc6f00;

    font-size: 12px;

    font-weight: 500;

    padding: 5px 10px 5px 12px;

    border-radius: 100px;

    cursor: pointer;

    transition: all 0.15s;

    font-family: 'DM Sans', sans-serif;

  }

  .hp-chip:hover { background: #ffe8cc; }

  .hp-chip-x { opacity: 0.6; transition: opacity 0.15s; }

  .hp-chip:hover .hp-chip-x { opacity: 1; }

  .hp-chip-clear {

    background: #fff0f0;

    border-color: rgba(220,53,69,0.2);

    color: #c0392b;

  }

  .hp-chip-clear:hover { background: #fde0e0; }


  /* Featured section */

  .hp-featured-label {

    display: flex;

    align-items: center;

    gap: 8px;

    font-size: 12px;

    font-weight: 600;

    letter-spacing: 0.1em;

    text-transform: uppercase;

    color: #ff8c00;

    margin-bottom: 14px;

  }

  .hp-featured-grid {

    display: grid;

    grid-template-columns: repeat(auto-fill, minmax(200px, 1fr));

    gap: 16px;

    margin-bottom: 36px;

  }


  /* Product grid */

  .hp-product-grid {

    display: grid;

    grid-template-columns: repeat(auto-fill, minmax(210px, 1fr));

    gap: 16px;

  }


  /* Loading */

  .hp-loading {

    display: flex;

    flex-direction: column;

    align-items: center;

    justify-content: center;

    padding: 80px 0;

    color: #aaa;

    gap: 14px;

  }

  .hp-spinner {

    width: 36px; height: 36px;

    border: 2.5px solid #ebebeb;

    border-top-color: #ff8c00;

    border-radius: 50%;

    animation: hp-spin 0.7s linear infinite;

  }

  @keyframes hp-spin { to { transform: rotate(360deg); } }


  /* Empty */

  .hp-empty {

    text-align: center;

    padding: 80px 24px;

    color: #aaa;

  }

  .hp-empty-icon { margin-bottom: 16px; opacity: 0.3; }

  .hp-empty h3 { font-family: 'DM Serif Display', serif; font-size: 1.3rem; color: #333; margin: 0 0 8px; font-weight: 400; }

  .hp-empty p { font-size: 14px; margin: 0 0 20px; }

  .hp-empty-btn {

    background: #ff8c00; color: #fff; border: none;

    border-radius: 9px; padding: 10px 24px;

    font-family: 'DM Sans', sans-serif; font-size: 13px; font-weight: 600;

    cursor: pointer; transition: background 0.2s;

  }

  .hp-empty-btn:hover { background: #e07800; }


  /* Pagination */

  .hp-pagination {

    display: flex;

    align-items: center;

    justify-content: center;

    gap: 6px;

    margin-top: 36px;

  }

  .hp-page-btn {

    width: 36px; height: 36px;

    display: flex; align-items: center; justify-content: center;

    border: 1px solid #e0e0e0;

    border-radius: 9px;

    background: #fff;

    color: #555;

    font-family: 'DM Sans', sans-serif;

    font-size: 13px;

    font-weight: 500;

    cursor: pointer;

    transition: all 0.15s;

  }

  .hp-page-btn:hover:not(:disabled) { border-color: #ff8c00; color: #ff8c00; }

  .hp-page-btn.active { background: #ff8c00; border-color: #ff8c00; color: #fff; font-weight: 600; }

  .hp-page-btn:disabled { opacity: 0.35; cursor: default; }

  .hp-page-ellipsis { color: #ccc; font-size: 14px; padding: 0 4px; }


  /* Mobile drawer overlay */

  .hp-drawer-overlay {

    position: fixed; inset: 0;

    background: rgba(0,0,0,0.4);

    z-index: 999;

    opacity: 0;

    pointer-events: none;

    transition: opacity 0.25s;

  }

  .hp-drawer-overlay.open { opacity: 1; pointer-events: all; }

  .hp-drawer {

    position: fixed; top: 0; right: 0; bottom: 0;

    width: 300px;

    background: #fff;

    z-index: 1000;

    transform: translateX(100%);

    transition: transform 0.25s cubic-bezier(0.4,0,0.2,1);

    display: flex;

    flex-direction: column;

    box-shadow: -4px 0 24px rgba(0,0,0,0.12);

  }

  .hp-drawer.open { transform: translateX(0); }

  .hp-drawer-header {

    display: flex;

    align-items: center;

    justify-content: space-between;

    padding: 20px 20px 16px;

    border-bottom: 1px solid #f0f0f0;

  }

  .hp-drawer-title {

    display: flex; align-items: center; gap: 8px;

    font-size: 15px; font-weight: 600; color: #1a1a1a;

  }

  .hp-drawer-close {

    background: #f5f5f5; border: none;

    width: 30px; height: 30px; border-radius: 50%;

    display: flex; align-items: center; justify-content: center;

    cursor: pointer; color: #555;

    transition: background 0.15s;

  }

  .hp-drawer-close:hover { background: #eee; }

  .hp-drawer-body { flex: 1; overflow-y: auto; padding: 20px; }

  .hp-drawer-footer {

    padding: 16px 20px;

    border-top: 1px solid #f0f0f0;

    display: flex; flex-direction: column; gap: 8px;

  }

  .hp-drawer-section { margin-bottom: 24px; }

  .hp-drawer-section-title {

    font-size: 11px; font-weight: 600; letter-spacing: 0.1em;

    text-transform: uppercase; color: #aaa; margin-bottom: 10px;

  }

`


// ─── component ────────────────────────────────────────────────────────────────

export const HomePage = () => {

  const navigate = useNavigate()

  const [searchParams, setSearchParams] = useSearchParams()


  const [filters, setFilters] = useState(() => ({

    ...defaultFilters,

    keyword:  searchParams.get('q')        || '',

    brand:    searchParams.get('brand')    || '',

    minPrice: searchParams.get('minPrice') || '',

    maxPrice: searchParams.get('maxPrice') || '',

  }))

  const [draft, setDraft] = useState(filters)


  const [products,   setProducts]   = useState([])

  const [featured,   setFeatured]   = useState([])

  const [brands,     setBrands]     = useState([])

  const [categories, setCategories] = useState([])

  const [totalPages, setTotalPages] = useState(0)

  const [totalItems, setTotalItems] = useState(0)

  const [isLoading,  setIsLoading]  = useState(false)

  const [showDrawer, setShowDrawer] = useState(false)

  const [openSections, setOpenSections] = useState({ category: true, price: true, brand: true })


  const { addToCart } = useCart()

  const debounceRef   = useRef(null)


  useEffect(() => {

    productService.getFeaturedProducts().then(setFeatured).catch(() => {})

    productService.getAllBrands().then(setBrands).catch(() => {})

    categoryService.getRootCategories().then(setCategories).catch(() => {})

  }, [])


  useEffect(() => {

    const params = {}

    if (filters.keyword)    params.q          = filters.keyword

    if (filters.brand)      params.brand      = filters.brand

    if (filters.minPrice)   params.minPrice   = filters.minPrice

    if (filters.maxPrice)   params.maxPrice   = filters.maxPrice

    if (filters.categoryId) params.categoryId = filters.categoryId

    setSearchParams(params, { replace: true })

  }, [filters]) // eslint-disable-line


  const fetchProducts = useCallback(async (f) => {

    setIsLoading(true)

    try {

      const sort = SORT_OPTIONS[f.sortIdx] || SORT_OPTIONS[0]

      const resp = await productService.searchProducts({

        query:       f.keyword    || undefined,

        categoryIds: f.categoryIds?.length ? f.categoryIds : undefined,

        brand:       f.brand      || undefined,

        minPrice:    f.minPrice !== '' ? Number(f.minPrice) : undefined,

        maxPrice:    f.maxPrice !== '' ? Number(f.maxPrice) : undefined,

        page:        f.page,

        size:        PAGE_SIZE,

        sortBy:      sort.sortBy,

        sortDir:     sort.sortDir,

      })

      setProducts(resp?.content || [])

      setTotalPages(resp?.totalPages || 1)

      setTotalItems(resp?.totalElements || 0)

    } catch {

      toast.error('Failed to load products')

    } finally {

      setIsLoading(false)

    }

  }, [])


  useEffect(() => { fetchProducts(filters) }, [filters]) // eslint-disable-line


  const handleKeywordChange = (e) => {

    const kw = e.target.value

    setDraft(d => ({ ...d, keyword: kw }))

    clearTimeout(debounceRef.current)

    debounceRef.current = setTimeout(() =>

      setFilters(f => ({ ...f, keyword: kw, page: 0 })), 350)

  }


  const handleCategoryClick = (catId) => {

    if (filters.categoryId === catId) {

      setFilters(f => ({ ...f, categoryId: null, categoryIds: null, page: 0 }))

      setDraft(d  => ({ ...d, categoryId: null, categoryIds: null }))

    } else {

      const cat = categories.find(c => c.id === catId)

      const ids = [catId, ...(cat?.children || []).map(ch => ch.id)]

      const next = { ...defaultFilters, categoryId: catId, categoryIds: ids }

      setDraft(next)

      setFilters(next)

    }

  }


  const handleDraftCategoryClick = (catId) => {

    if (draft.categoryId === catId) {

      setDraft(d => ({ ...d, categoryId: null, categoryIds: null }))

    } else {

      const cat = categories.find(c => c.id === catId)

      const ids = [catId, ...(cat?.children || []).map(ch => ch.id)]

      setDraft(d => ({ ...d, categoryId: catId, categoryIds: ids }))

    }

  }


  const handleSortChange = (e) => {

    const idx = Number(e.target.value)

    setFilters(f => ({ ...f, sortIdx: idx, page: 0 }))

    setDraft(d => ({ ...d, sortIdx: idx }))

  }


  const handlePageChange = (page) => {

    setFilters(f => ({ ...f, page }))

    window.scrollTo({ top: 0, behavior: 'smooth' })

  }


  const applyDraft   = () => { setFilters({ ...draft, page: 0 }); setShowDrawer(false) }

  const resetFilters = () => { setDraft(defaultFilters); setFilters(defaultFilters); setShowDrawer(false) }

  const removeFilter = (key) => {

    const reset = { [key]: defaultFilters[key], page: 0 }

    if (key === 'categoryId') reset.categoryIds = null

    setFilters(f => ({ ...f, ...reset }))

    setDraft(d  => ({ ...d, ...reset }))

  }


  const toggleSection = (key) =>

    setOpenSections(s => ({ ...s, [key]: !s[key] }))


  // Apply sidebar draft immediately (desktop)

  const applySidebarFilter = (patch) => {

    const next = { ...filters, ...patch, page: 0 }

    setFilters(next)

    setDraft(next)

  }


  const handleAddToCart = (product) => {

    addToCart(product)

    toast.success(`${product.name} added to cart`)

  }


  const filterCount      = activeFilterCount(filters)

  const selectedCategory = categories.find(c => c.id === filters.categoryId)


  const chips = []

  if (filters.keyword)    chips.push({ key: 'keyword',    label: filters.keyword })

  if (filters.categoryId) chips.push({ key: 'categoryId', label: selectedCategory?.name || 'Category' })

  if (filters.brand)      chips.push({ key: 'brand',      label: filters.brand })

  if (filters.minPrice)   chips.push({ key: 'minPrice',   label: `From $${filters.minPrice}` })

  if (filters.maxPrice)   chips.push({ key: 'maxPrice',   label: `Up to $${filters.maxPrice}` })


  // Pagination

  const paginationItems = []

  const buildPages = () => {

    if (totalPages <= 7) {

      for (let i = 0; i < totalPages; i++) paginationItems.push(i)

    } else {

      const cur = filters.page

      const set = new Set([0, totalPages - 1, cur, cur - 1, cur + 1].filter(p => p >= 0 && p < totalPages))

      Array.from(set).sort((a, b) => a - b).forEach((p, i, arr) => {

        if (i > 0 && p - arr[i - 1] > 1) paginationItems.push('...')

        paginationItems.push(p)

      })

    }

  }

  buildPages()


  // ── Sidebar filter panel (reused in desktop + drawer) ─────────────────────

  const FilterPanel = ({ isDraft = false }) => {

    const vals = isDraft ? draft : filters

    const set  = isDraft

      ? (patch) => setDraft(d => ({ ...d, ...patch }))

      : (patch) => applySidebarFilter(patch)

    const onCatClick = isDraft ? handleDraftCategoryClick : handleCategoryClick


    return (

      <>

        {/* Category */}

        <div className="hp-filter-card">

          <div className="hp-filter-header" onClick={() => toggleSection('category')}>

            <span className="hp-filter-header-title">

              <LayoutGrid size={14} />

              Category

            </span>

            {openSections.category ? <ChevronUp size={14} color="#aaa" /> : <ChevronDown size={14} color="#aaa" />}

          </div>

          {openSections.category && (

            <div className="hp-filter-body" style={{ paddingTop: 8 }}>

              <div

                className={`hp-cat-item ${!vals.categoryId ? 'active' : ''}`}

                onClick={() => set({ categoryId: null, categoryIds: null })}

              >

                <ShoppingBag size={15} className="hp-cat-item-icon" />

                All Products

                <span className="hp-cat-count">{totalItems}</span>

              </div>

              {categories.map(cat => {

                const Icon = getCategoryIcon(cat.name)

                const total = cat.productCount + (cat.children || []).reduce((s, c) => s + c.productCount, 0)

                return (

                  <div

                    key={cat.id}

                    className={`hp-cat-item ${vals.categoryId === cat.id ? 'active' : ''}`}

                    onClick={() => onCatClick(cat.id)}

                  >

                    <Icon size={15} className="hp-cat-item-icon" />

                    {cat.name}

                    <span className="hp-cat-count">{total}</span>

                  </div>

                )

              })}

            </div>

          )}

        </div>


        {/* Price */}

        <div className="hp-filter-card">

          <div className="hp-filter-header" onClick={() => toggleSection('price')}>

            <span className="hp-filter-header-title">

              <Tag size={14} />

              Price Range

            </span>

            {openSections.price ? <ChevronUp size={14} color="#aaa" /> : <ChevronDown size={14} color="#aaa" />}

          </div>

          {openSections.price && (

            <div className="hp-filter-body">

              <div className="hp-price-row">

                <input

                  className="hp-price-input"

                  type="number" min={0} placeholder="Min"

                  value={vals.minPrice}

                  onChange={e => set({ minPrice: e.target.value })}

                />

                <span className="hp-price-sep">—</span>

                <input

                  className="hp-price-input"

                  type="number" min={0} placeholder="Max"

                  value={vals.maxPrice}

                  onChange={e => set({ maxPrice: e.target.value })}

                />

              </div>

              <div className="hp-price-presets">

                {PRICE_PRESETS.map(p => (

                  <button

                    key={p.label}

                    className={`hp-price-preset ${vals.minPrice === p.min && vals.maxPrice === p.max ? 'active' : ''}`}

                    onClick={() => set({ minPrice: p.min, maxPrice: p.max })}

                  >

                    {p.label}

                  </button>

                ))}

              </div>

            </div>

          )}

        </div>


        {/* Brand */}

        {brands.length > 0 && (

          <div className="hp-filter-card">

            <div className="hp-filter-header" onClick={() => toggleSection('brand')}>

              <span className="hp-filter-header-title">

                <Star size={14} />

                Brand

              </span>

              {openSections.brand ? <ChevronUp size={14} color="#aaa" /> : <ChevronDown size={14} color="#aaa" />}

            </div>

            {openSections.brand && (

              <div className="hp-filter-body">

                <select

                  className="hp-select"

                  value={vals.brand}

                  onChange={e => set({ brand: e.target.value })}

                >

                  <option value="">All brands</option>

                  {brands.map(b => <option key={b} value={b}>{b}</option>)}

                </select>

              </div>

            )}

          </div>

        )}

      </>

    )

  }


  // ──────────────────────────────────────────────────────────────────────────

  return (

    <>

      <style>{css}</style>

      <div className="hp-root">


        {/* ── HERO ── */}

        <div className="hp-hero">

          <div className="hp-hero-inner">

            <div>

              <div className="hp-hero-eyebrow">

                <Zap size={11} />

                Best prices, every day

              </div>

              <h1>Discover <em>everything</em><br />you need, online.</h1>

              <p className="hp-hero-sub">

                Thousands of products across hundreds of categories,<br />

                delivered fast to your door.

              </p>

              <div className="hp-search-wrap">

                <span className="hp-search-icon"><Search size={16} /></span>

                <input

                  className="hp-search-input"

                  placeholder="Search products, brands, categories…"

                  value={draft.keyword}

                  onChange={handleKeywordChange}

                  onKeyDown={e => e.key === 'Enter' &&

                    setFilters(f => ({ ...f, keyword: draft.keyword, page: 0 }))}

                />

                {draft.keyword && (

                  <button className="hp-search-clear" onClick={() => {

                    setDraft(d => ({ ...d, keyword: '' }))

                    setFilters(f => ({ ...f, keyword: '', page: 0 }))

                  }}>

                    <X size={14} />

                  </button>

                )}

                <button className="hp-search-btn"

                  onClick={() => setFilters(f => ({ ...f, keyword: draft.keyword, page: 0 }))}>

                  Search

                </button>

              </div>

            </div>


            <div className="hp-hero-badge">

              <div className="hp-hero-stat">

                <div className="hp-hero-stat-num">{totalItems || '—'}</div>

                <div className="hp-hero-stat-label">Products</div>

              </div>

              <div className="hp-hero-divider" />

              <div className="hp-hero-stat">

                <div className="hp-hero-stat-num">{categories.length}</div>

                <div className="hp-hero-stat-label">Categories</div>

              </div>

              <div className="hp-hero-divider" />

              <div className="hp-hero-stat">

                <div className="hp-hero-stat-num">{brands.length}</div>

                <div className="hp-hero-stat-label">Brands</div>

              </div>

            </div>

          </div>

        </div>


        {/* ── MAIN ── */}

        <div className="hp-main">


          {/* ── DESKTOP SIDEBAR ── */}

          <aside className="hp-sidebar">

            <FilterPanel isDraft={false} />

            {filterCount > 0 && (

              <button className="hp-reset-btn" onClick={resetFilters}>

                Clear all filters

              </button>

            )}

          </aside>


          {/* ── CONTENT ── */}

          <div className="hp-content">


            {/* Category pills — desktop hidden (sidebar handles it), mobile visible */}

            <div className="hp-cat-pills" style={{ display: 'none' /* shown via mobile bar */ }} />


            {/* Mobile toolbar */}

            <div className="hp-mobile-bar">

              <button

                className={`hp-mobile-filter-btn ${filterCount > 0 ? 'has-filters' : ''}`}

                onClick={() => { setDraft(filters); setShowDrawer(true) }}

              >

                <SlidersHorizontal size={15} />

                Filters {filterCount > 0 && `(${filterCount})`}

              </button>


              {/* Mobile category pills */}

              {categories.map(cat => {

                const Icon = getCategoryIcon(cat.name)

                return (

                  <button

                    key={cat.id}

                    className={`hp-cat-pill ${filters.categoryId === cat.id ? 'active' : ''}`}

                    onClick={() => handleCategoryClick(cat.id)}

                  >

                    <Icon size={13} />

                    {cat.name}

                  </button>

                )

              })}

            </div>


            {/* Featured */}

            {featured.length > 0 && filterCount === 0 && (

              <div style={{ marginBottom: 32 }}>

                <div className="hp-featured-label">

                  <Flame size={14} />

                  Featured picks

                </div>

                <div className="hp-featured-grid">

                  {featured.map(p => (

                    <ProductCard

                      key={p.id}

                      product={p}

                      onAddToCart={handleAddToCart}

                      onViewDetails={(id) => navigate(`/product/${id}`)}

                    />

                  ))}

                </div>

              </div>

            )}


            {/* Toolbar */}

            <div className="hp-toolbar">

              <div className="hp-toolbar-left">

                <span className="hp-toolbar-title">

                  {selectedCategory ? selectedCategory.name : 'All Products'}

                </span>

                {totalItems > 0 && (

                  <span className="hp-toolbar-count">{totalItems.toLocaleString()} items</span>

                )}

              </div>


              <div style={{ display: 'flex', alignItems: 'center', gap: 8 }}>

                <ArrowUpDown size={14} color="#aaa" />

                <select className="hp-sort-select" value={filters.sortIdx} onChange={handleSortChange}>

                  {SORT_OPTIONS.map((o, i) => (

                    <option key={i} value={i}>{o.label}</option>

                  ))}

                </select>

              </div>

            </div>


            {/* Chips */}

            {chips.length > 0 && (

              <div className="hp-chips">

                {chips.map(c => (

                  <button key={c.key} className="hp-chip" onClick={() => removeFilter(c.key)}>

                    {c.label}

                    <X size={12} className="hp-chip-x" />

                  </button>

                ))}

                <button className="hp-chip hp-chip-clear" onClick={resetFilters}>

                  Clear all

                  <X size={12} className="hp-chip-x" />

                </button>

              </div>

            )}


            {/* Grid */}

            {isLoading ? (

              <div className="hp-loading">

                <div className="hp-spinner" />

                <span style={{ fontSize: 13 }}>Loading products…</span>

              </div>

            ) : products.length === 0 ? (

              <div className="hp-empty">

                <div className="hp-empty-icon"><Package size={48} /></div>

                <h3>No products found</h3>

                <p>Try adjusting your search or removing some filters</p>

                <button className="hp-empty-btn" onClick={resetFilters}>Clear filters</button>

              </div>

            ) : (

              <div className="hp-product-grid">

                {products.map(product => (

                  <ProductCard

                    key={product.id}

                    product={product}

                    onAddToCart={handleAddToCart}

                    onViewDetails={(id) => navigate(`/product/${id}`)}

                  />

                ))}

              </div>

            )}


            {/* Pagination */}

            {totalPages > 1 && (

              <div className="hp-pagination">

                <button

                  className="hp-page-btn"

                  disabled={filters.page === 0}

                  onClick={() => handlePageChange(filters.page - 1)}

                >

                  <ChevronLeft size={15} />

                </button>


                {paginationItems.map((item, i) =>

                  item === '...' ? (

                    <span key={`e${i}`} className="hp-page-ellipsis">…</span>

                  ) : (

                    <button

                      key={item}

                      className={`hp-page-btn ${item === filters.page ? 'active' : ''}`}

                      onClick={() => handlePageChange(item)}

                    >

                      {item + 1}

                    </button>

                  )

                )}


                <button

                  className="hp-page-btn"

                  disabled={filters.page >= totalPages - 1}

                  onClick={() => handlePageChange(filters.page + 1)}

                >

                  <ChevronRight size={15} />

                </button>

              </div>

            )}

          </div>

        </div>


        {/* ── MOBILE DRAWER ── */}

        <div className={`hp-drawer-overlay ${showDrawer ? 'open' : ''}`} onClick={() => setShowDrawer(false)} />

        <div className={`hp-drawer ${showDrawer ? 'open' : ''}`}>

          <div className="hp-drawer-header">

            <div className="hp-drawer-title">

              <SlidersHorizontal size={16} />

              Filters

            </div>

            <button className="hp-drawer-close" onClick={() => setShowDrawer(false)}>

              <X size={15} />

            </button>

          </div>

          <div className="hp-drawer-body">

            <FilterPanel isDraft={true} />

          </div>

          <div className="hp-drawer-footer">

            <button className="hp-apply-btn" onClick={applyDraft}>Apply Filters</button>

            <button className="hp-reset-btn" onClick={resetFilters}>Reset all</button>

          </div>

        </div>


      </div>

    </>

  )

}


export default HomePage


//
// import React, { useState, useEffect, useCallback, useRef } from 'react'
// import { useNavigate, useSearchParams } from 'react-router-dom'
// import {
//   Search, X, SlidersHorizontal, ChevronDown, ChevronUp,
//   Laptop, Shirt, Home, Sparkles, Dumbbell, BookOpen,
//   Baby, Apple, Car, Heart, Tag, ShoppingBag,
//   ArrowUpDown, Star, Package, Flame, ChevronLeft, ChevronRight,
//   LayoutGrid, Zap, Monitor, Smartphone
// } from 'lucide-react'
// import { productService } from '../../api/productService'
// import { categoryService } from '../../api/categoryService'
// import { useCart } from '../../context/CartContext'
// import ProductCard from '../../components/ProductCard'
// import toast from 'react-hot-toast'
//
// const fontLink = document.createElement('link')
// fontLink.rel = 'stylesheet'
// fontLink.href = 'https://fonts.googleapis.com/css2?family=DM+Serif+Display:ital@0;1&family=DM+Sans:wght@300;400;500;600&display=swap'
// if (!document.head.querySelector('[href*="DM+Serif"]')) document.head.appendChild(fontLink)
//
// const SORT_OPTIONS = [
//   { label: 'Newest First',      sortBy: 'createdAt', sortDir: 'desc' },
//   { label: 'Oldest First',      sortBy: 'createdAt', sortDir: 'asc'  },
//   { label: 'Price: Low → High', sortBy: 'price',     sortDir: 'asc'  },
//   { label: 'Price: High → Low', sortBy: 'price',     sortDir: 'desc' },
//   { label: 'Name: A → Z',       sortBy: 'name',      sortDir: 'asc'  },
//   { label: 'Name: Z → A',       sortBy: 'name',      sortDir: 'desc' },
// ]
//
// const PRICE_PRESETS = [
//   { label: 'Under $25',   min: '',    max: '25'  },
//   { label: '$25 – $100',  min: '25',  max: '100' },
//   { label: '$100 – $500', min: '100', max: '500' },
//   { label: 'Over $500',   min: '500', max: ''    },
// ]
//
// const CATEGORY_ICONS = {
//   electronics: Monitor, laptop: Laptop, mobile: Smartphone,
//   fashion: Shirt, cloths: Shirt, clothing: Shirt,
//   home: Home, beauty: Sparkles, sports: Dumbbell,
//   books: BookOpen, toys: Baby, food: Apple,
//   automotive: Car, health: Heart,
// }
//
// const getCategoryIcon = (name = '') => {
//   const key = name.toLowerCase().replace(/\s+/g, '')
//   for (const [k, Icon] of Object.entries(CATEGORY_ICONS)) {
//     if (key.includes(k)) return Icon
//   }
//   return Tag
// }
//
// const PAGE_SIZE = 12
//
// const defaultFilters = {
//   keyword: '', categoryId: null, categoryIds: null,
//   brand: '', minPrice: '', maxPrice: '', sortIdx: 0, page: 0,
// }
//
// function activeFilterCount(f) {
//   return [f.keyword, f.categoryId, f.brand, f.minPrice, f.maxPrice].filter(Boolean).length
// }
//
// // ─── 3-D Cartoon Shop SVG Illustration ────────────────────────────────────
// const CartoonShop3D = () => (
//   <svg
//     viewBox="0 0 420 340"
//     fill="none"
//     xmlns="http://www.w3.org/2000/svg"
//     style={{ width: '100%', maxWidth: 420, height: 'auto', filter: 'drop-shadow(0 24px 40px rgba(0,0,0,0.35))' }}
//     aria-hidden="true"
//   >
//     <defs>
//       {/* Gradients */}
//       <linearGradient id="sky" x1="0" y1="0" x2="0" y2="1">
//         <stop offset="0%" stopColor="#1a2744"/>
//         <stop offset="100%" stopColor="#0f1923"/>
//       </linearGradient>
//       <linearGradient id="roof" x1="0" y1="0" x2="0" y2="1">
//         <stop offset="0%" stopColor="#ff6b35"/>
//         <stop offset="100%" stopColor="#d4451a"/>
//       </linearGradient>
//       <linearGradient id="roofSide" x1="0" y1="0" x2="1" y2="0">
//         <stop offset="0%" stopColor="#b83a14"/>
//         <stop offset="100%" stopColor="#8c2a0d"/>
//       </linearGradient>
//       <linearGradient id="wall" x1="0" y1="0" x2="0" y2="1">
//         <stop offset="0%" stopColor="#fdf6ec"/>
//         <stop offset="100%" stopColor="#f2e8d4"/>
//       </linearGradient>
//       <linearGradient id="wallSide" x1="0" y1="0" x2="1" y2="0">
//         <stop offset="0%" stopColor="#e0d3bb"/>
//         <stop offset="100%" stopColor="#c8bc9e"/>
//       </linearGradient>
//       <linearGradient id="floor" x1="0" y1="0" x2="0" y2="1">
//         <stop offset="0%" stopColor="#3a2c1e"/>
//         <stop offset="100%" stopColor="#241c12"/>
//       </linearGradient>
//       <linearGradient id="floorSide" x1="0" y1="0" x2="1" y2="0">
//         <stop offset="0%" stopColor="#1e1510"/>
//         <stop offset="100%" stopColor="#150e08"/>
//       </linearGradient>
//       <linearGradient id="door" x1="0" y1="0" x2="0" y2="1">
//         <stop offset="0%" stopColor="#5c3d1e"/>
//         <stop offset="100%" stopColor="#3a2610"/>
//       </linearGradient>
//       <linearGradient id="window" x1="0" y1="0" x2="1" y2="1">
//         <stop offset="0%" stopColor="#a8d8f0"/>
//         <stop offset="100%" stopColor="#7ab8e0"/>
//       </linearGradient>
//       <linearGradient id="awning" x1="0" y1="0" x2="0" y2="1">
//         <stop offset="0%" stopColor="#ff8c00"/>
//         <stop offset="100%" stopColor="#e07000"/>
//       </linearGradient>
//       <linearGradient id="bag" x1="0" y1="0" x2="1" y2="1">
//         <stop offset="0%" stopColor="#ffcc44"/>
//         <stop offset="100%" stopColor="#f5a800"/>
//       </linearGradient>
//       <linearGradient id="box" x1="0" y1="0" x2="1" y2="1">
//         <stop offset="0%" stopColor="#7ecef5"/>
//         <stop offset="100%" stopColor="#4ab0e8"/>
//       </linearGradient>
//       <linearGradient id="heart" x1="0" y1="0" x2="1" y2="1">
//         <stop offset="0%" stopColor="#ff6b9d"/>
//         <stop offset="100%" stopColor="#e83c7a"/>
//       </linearGradient>
//       <linearGradient id="star" x1="0" y1="0" x2="1" y2="1">
//         <stop offset="0%" stopColor="#ffe066"/>
//         <stop offset="100%" stopColor="#ffc200"/>
//       </linearGradient>
//       <radialGradient id="glow" cx="50%" cy="50%" r="50%">
//         <stop offset="0%" stopColor="#ff8c00" stopOpacity="0.18"/>
//         <stop offset="100%" stopColor="#ff8c00" stopOpacity="0"/>
//       </radialGradient>
//       <filter id="softShadow" x="-20%" y="-20%" width="140%" height="140%">
//         <feDropShadow dx="2" dy="4" stdDeviation="4" floodColor="#000" floodOpacity="0.18"/>
//       </filter>
//     </defs>
//
//     {/* ── Glow halo behind shop ── */}
//     <ellipse cx="210" cy="200" rx="180" ry="100" fill="url(#glow)"/>
//
//     {/* ── Ground shadow ── */}
//     <ellipse cx="210" cy="300" rx="140" ry="14" fill="#000" fillOpacity="0.22"/>
//
//     {/* ══════════════ BUILDING ══════════════ */}
//
//     {/* Floor platform – side */}
//     <polygon points="70,292 210,302 210,310 70,300" fill="url(#floorSide)"/>
//     {/* Floor platform – top face */}
//     <polygon points="70,285 280,285 280,292 70,292" fill="url(#floor)"/>
//     {/* Floor platform – front face */}
//     <rect x="70" y="292" width="210" height="10" fill="#2a1e0f" rx="0"/>
//
//     {/* Wall – side (right) */}
//     <polygon points="280,110 320,92 320,290 280,290" fill="url(#wallSide)" filter="url(#softShadow)"/>
//     {/* Wall – front */}
//     <rect x="70" y="110" width="210" height="180" fill="url(#wall)" filter="url(#softShadow)"/>
//
//     {/* Roof – side (right) */}
//     <polygon points="280,50 320,30 320,92 280,110" fill="url(#roofSide)"/>
//     {/* Roof – front face (isometric) */}
//     <polygon points="50,110 70,110 280,110 300,50 280,50 70,50" fill="url(#roof)"/>
//     {/* Roof ridge detail */}
//     <line x1="70" y1="50" x2="280" y2="50" stroke="#c24010" strokeWidth="2.5"/>
//
//     {/* Roof top horizontal bar */}
//     <rect x="60" y="46" width="228" height="10" rx="5" fill="#ff6b35"/>
//     {/* Chimney */}
//     <rect x="240" y="24" width="22" height="32" rx="4" fill="#e0cbb0"/>
//     <rect x="237" y="20" width="28" height="8" rx="3" fill="#c8b898"/>
//
//     {/* ══ AWNING ══ */}
//     <polygon points="80,158 270,158 280,178 70,178" fill="url(#awning)"/>
//     {/* Awning stripes */}
//     {[0,1,2,3,4,5].map(i => (
//       <line key={i}
//         x1={100 + i * 30} y1="158"
//         x2={95 + i * 30} y2="178"
//         stroke="#fff" strokeWidth="6" strokeOpacity="0.18"
//       />
//     ))}
//     {/* Awning scallop */}
//     {[0,1,2,3,4,5,6].map(i => (
//       <ellipse key={i} cx={86 + i * 28} cy="178" rx="14" ry="8" fill="#e07000"/>
//     ))}
//
//     {/* ══ SIGN ══ */}
//     <rect x="100" y="56" width="140" height="38" rx="8" fill="#fff" fillOpacity="0.9" filter="url(#softShadow)"/>
//     <rect x="103" y="59" width="134" height="32" rx="6" fill="#fff5e6"/>
//     <text x="170" y="81" textAnchor="middle" fontFamily="'DM Serif Display', serif" fontSize="14" fontWeight="700" fill="#cc6600" letterSpacing="1">✦ SHOP ✦</text>
//
//     {/* ══ WINDOW LEFT ══ */}
//     <rect x="85" y="190" width="70" height="62" rx="6" fill="url(#window)" filter="url(#softShadow)"/>
//     <rect x="88" y="193" width="64" height="56" rx="4" fill="#c8e8f8" fillOpacity="0.5"/>
//     {/* window cross */}
//     <line x1="120" y1="193" x2="120" y2="249" stroke="#a0c8e0" strokeWidth="2"/>
//     <line x1="85" y1="222" x2="155" y2="222" stroke="#a0c8e0" strokeWidth="2"/>
//     {/* window item: tiny t-shirt */}
//     <rect x="104" y="200" width="32" height="20" rx="3" fill="#ff6b9d" fillOpacity="0.85"/>
//     <polygon points="108,200 112,195 120,198 128,195 132,200" fill="#ff6b9d" fillOpacity="0.85"/>
//
//     {/* ══ DOOR ══ */}
//     <rect x="158" y="210" width="54" height="75" rx="6" fill="url(#door)" filter="url(#softShadow)"/>
//     <rect x="161" y="213" width="48" height="69" rx="4" fill="#4a3018"/>
//     {/* door arch */}
//     <path d="M161 231 A24 20 0 0 1 209 231" fill="#5c3d1e"/>
//     {/* door knob */}
//     <circle cx="202" cy="252" r="4" fill="#e8a835"/>
//     <circle cx="202" cy="252" r="2.5" fill="#ffd060"/>
//     {/* door steps */}
//     <rect x="152" y="283" width="66" height="6" rx="2" fill="#8c7050"/>
//     <rect x="156" y="279" width="58" height="6" rx="2" fill="#a08060"/>
//
//     {/* ══ WINDOW RIGHT ══ */}
//     <rect x="215" y="190" width="56" height="62" rx="6" fill="url(#window)" filter="url(#softShadow)"/>
//     <rect x="218" y="193" width="50" height="56" rx="4" fill="#c8e8f8" fillOpacity="0.5"/>
//     <line x1="243" y1="193" x2="243" y2="249" stroke="#a0c8e0" strokeWidth="2"/>
//     <line x1="215" y1="222" x2="271" y2="222" stroke="#a0c8e0" strokeWidth="2"/>
//     {/* window item: tiny star tag */}
//     <polygon points="243,200 246,208 255,208 248,213 250,222 243,217 236,222 238,213 231,208 240,208"
//       fill="#ffe066" fillOpacity="0.9"/>
//
//     {/* ══ BUSHES ══ */}
//     <ellipse cx="90" cy="286" rx="22" ry="14" fill="#3a7d44"/>
//     <ellipse cx="76" cy="280" rx="16" ry="12" fill="#4caf50"/>
//     <ellipse cx="100" cy="278" rx="18" ry="13" fill="#45a049"/>
//     <ellipse cx="315" cy="284" rx="20" ry="13" fill="#3a7d44"/>
//     <ellipse cx="328" cy="279" rx="15" ry="11" fill="#4caf50"/>
//
//     {/* ══ LAMP POST ══ */}
//     <rect x="44" y="200" width="5" height="88" rx="2" fill="#8c7050"/>
//     <ellipse cx="46" cy="200" rx="12" ry="6" fill="#ffd060" fillOpacity="0.3"/>
//     <rect x="37" y="195" width="18" height="12" rx="4" fill="#d4a830"/>
//     <circle cx="46" cy="195" r="7" fill="#ffe066" fillOpacity="0.9"/>
//     <circle cx="46" cy="195" r="4" fill="#fff9c0"/>
//
//     {/* ══════════════ FLOATING ITEMS ══════════════ */}
//
//     {/* Floating shopping bag — top right */}
//     <g style={{ animation: 'floatA 3.2s ease-in-out infinite' }}>
//       <rect x="330" y="60" width="44" height="48" rx="8" fill="url(#bag)" filter="url(#softShadow)"/>
//       <path d="M341 68 Q341 56 354 56 Q367 56 367 68" stroke="#e09000" strokeWidth="3" fill="none" strokeLinecap="round"/>
//       <rect x="336" y="80" width="32" height="2" rx="1" fill="#e09000" fillOpacity="0.5"/>
//       <rect x="336" y="88" width="24" height="2" rx="1" fill="#e09000" fillOpacity="0.4"/>
//       <text x="354" y="100" textAnchor="middle" fontSize="12" fill="#cc6600">🛍</text>
//     </g>
//
//     {/* Floating box — left */}
//     <g style={{ animation: 'floatB 2.8s ease-in-out infinite' }}>
//       <rect x="22" y="120" width="38" height="34" rx="6" fill="url(#box)" filter="url(#softShadow)"/>
//       <polygon points="22,120 41,112 60,120 41,128" fill="#9de0ff"/>
//       <line x1="41" y1="120" x2="41" y2="154" stroke="#3a9fd4" strokeWidth="1.5"/>
//       <line x1="22" y1="120" x2="41" y2="128" stroke="#3a9fd4" strokeWidth="1"/>
//       <line x1="60" y1="120" x2="41" y2="128" stroke="#3a9fd4" strokeWidth="1"/>
//     </g>
//
//     {/* Floating heart — far right */}
//     <g style={{ animation: 'floatC 3.6s ease-in-out infinite' }}>
//       <path d="M370 150 C370 140 358 132 354 142 C350 132 338 140 338 150 C338 162 354 172 354 172 C354 172 370 162 370 150Z"
//         fill="url(#heart)" filter="url(#softShadow)"/>
//     </g>
//
//     {/* Floating star — top left */}
//     <g style={{ animation: 'floatD 4s ease-in-out infinite' }}>
//       <polygon
//         points="32,68 35,78 46,78 37,84 40,95 32,88 24,95 27,84 18,78 29,78"
//         fill="url(#star)" filter="url(#softShadow)"
//       />
//     </g>
//
//     {/* Small sparkle dots */}
//     {[
//       {cx:360, cy:108, r:3.5, fill:'#ffe066', anim:'floatA'},
//       {cx:18,  cy:168, r:2.5, fill:'#ff6b9d', anim:'floatB'},
//       {cx:390, cy:200, r:2,   fill:'#7ecef5', anim:'floatC'},
//       {cx:8,   cy:230, r:3,   fill:'#ff8c00', anim:'floatD'},
//       {cx:400, cy:70,  r:2,   fill:'#fff',    anim:'floatC'},
//     ].map((s,i) => (
//       <circle key={i} cx={s.cx} cy={s.cy} r={s.r} fill={s.fill}
//         style={{ animation: `${s.anim} ${2.4+i*0.3}s ease-in-out infinite` }}/>
//     ))}
//
//     {/* CSS keyframes injected via style tag inside SVG */}
//     <style>{`
//       @keyframes floatA {
//         0%,100% { transform: translateY(0px) rotate(-2deg); }
//         50%      { transform: translateY(-12px) rotate(2deg); }
//       }
//       @keyframes floatB {
//         0%,100% { transform: translateY(0px) rotate(1deg); }
//         50%      { transform: translateY(-10px) rotate(-3deg); }
//       }
//       @keyframes floatC {
//         0%,100% { transform: translateY(0px) scale(1); }
//         50%      { transform: translateY(-14px) scale(1.08); }
//       }
//       @keyframes floatD {
//         0%,100% { transform: translateY(0px) rotate(-5deg); }
//         50%      { transform: translateY(-9px) rotate(5deg); }
//       }
//     `}</style>
//   </svg>
// )
//
// const css = `
//   .hp-root * { box-sizing: border-box; }
//   .hp-root {
//     font-family: 'DM Sans', sans-serif;
//     background: #f8f7f4;
//     min-height: 100vh;
//     color: #1a1a1a;
//   }
//
//   /* ══ HERO ══ */
//   .hp-hero {
//     background: #0d1520;
//     padding: 60px 0 52px;
//     position: relative;
//     overflow: hidden;
//   }
//   .hp-hero::before {
//     content: '';
//     position: absolute; inset: 0;
//     background:
//       radial-gradient(ellipse 70% 70% at 75% 55%, rgba(255,140,0,0.13) 0%, transparent 65%),
//       radial-gradient(ellipse 50% 60% at 15% 80%, rgba(100,180,255,0.08) 0%, transparent 60%),
//       radial-gradient(ellipse 40% 40% at 50% 0%,  rgba(255,80,120,0.06) 0%, transparent 70%);
//   }
//   /* subtle grid texture */
//   .hp-hero::after {
//     content: '';
//     position: absolute; inset: 0;
//     background-image: linear-gradient(rgba(255,255,255,0.03) 1px, transparent 1px),
//                       linear-gradient(90deg, rgba(255,255,255,0.03) 1px, transparent 1px);
//     background-size: 40px 40px;
//     pointer-events: none;
//   }
//   .hp-hero-inner {
//     max-width: 1200px;
//     margin: 0 auto;
//     padding: 0 24px;
//     position: relative;
//     z-index: 1;
//     display: grid;
//     grid-template-columns: 1fr 460px;
//     gap: 32px;
//     align-items: center;
//   }
//   @media (max-width: 860px) {
//     .hp-hero-inner { grid-template-columns: 1fr; }
//     .hp-hero-illustration { display: none; }
//   }
//   .hp-hero-illustration {
//     display: flex;
//     align-items: center;
//     justify-content: center;
//     position: relative;
//   }
//   /* rotating ring behind illustration */
//   .hp-hero-illustration::before {
//     content: '';
//     position: absolute;
//     width: 340px; height: 340px;
//     border-radius: 50%;
//     border: 1.5px dashed rgba(255,140,0,0.2);
//     animation: spin 28s linear infinite;
//   }
//   .hp-hero-illustration::after {
//     content: '';
//     position: absolute;
//     width: 260px; height: 260px;
//     border-radius: 50%;
//     border: 1px dashed rgba(100,180,255,0.15);
//     animation: spin 18s linear infinite reverse;
//   }
//   @keyframes spin { to { transform: rotate(360deg); } }
//
//   .hp-hero-eyebrow {
//     display: inline-flex; align-items: center; gap: 6px;
//     background: rgba(255,140,0,0.14);
//     border: 1px solid rgba(255,140,0,0.3);
//     color: #ff8c00; font-size: 11px; font-weight: 600;
//     letter-spacing: 0.1em; text-transform: uppercase;
//     padding: 5px 13px; border-radius: 100px; margin-bottom: 18px;
//     animation: fadeSlideUp 0.6s ease both;
//   }
//   .hp-hero h1 {
//     font-family: 'DM Serif Display', serif;
//     font-size: clamp(2rem, 4vw, 3rem);
//     color: #fff; line-height: 1.15;
//     margin: 0 0 14px; font-weight: 400;
//     animation: fadeSlideUp 0.6s 0.1s ease both;
//   }
//   .hp-hero h1 em { font-style: italic; color: #ff8c00; }
//   .hp-hero-sub {
//     color: rgba(255,255,255,0.52); font-size: 15px;
//     margin: 0 0 28px; font-weight: 300; line-height: 1.6;
//     animation: fadeSlideUp 0.6s 0.2s ease both;
//   }
//   .hp-hero-stats {
//     display: flex; gap: 24px; margin-bottom: 28px;
//     animation: fadeSlideUp 0.6s 0.25s ease both;
//   }
//   .hp-hero-stat-item { text-align: left; }
//   .hp-hero-stat-num {
//     font-family: 'DM Serif Display', serif;
//     font-size: 1.6rem; color: #ff8c00; line-height: 1;
//   }
//   .hp-hero-stat-label {
//     font-size: 11px; color: rgba(255,255,255,0.38);
//     text-transform: uppercase; letter-spacing: 0.08em; margin-top: 2px;
//   }
//   .hp-hero-stat-divider { width: 1px; background: rgba(255,255,255,0.1); }
//
//   .hp-search-wrap {
//     display: flex; max-width: 500px;
//     background: rgba(255,255,255,0.08);
//     border: 1px solid rgba(255,255,255,0.14);
//     border-radius: 12px; overflow: hidden;
//     backdrop-filter: blur(8px);
//     transition: border-color 0.2s;
//     animation: fadeSlideUp 0.6s 0.3s ease both;
//   }
//   .hp-search-wrap:focus-within {
//     border-color: rgba(255,140,0,0.55);
//     background: rgba(255,255,255,0.12);
//   }
//   .hp-search-icon { display: flex; align-items: center; padding: 0 14px; color: rgba(255,255,255,0.38); }
//   .hp-search-input {
//     flex: 1; background: transparent; border: none; outline: none;
//     color: #fff; font-size: 14px; font-family: 'DM Sans', sans-serif;
//     padding: 14px 0;
//   }
//   .hp-search-input::placeholder { color: rgba(255,255,255,0.32); }
//   .hp-search-clear {
//     display: flex; align-items: center; padding: 0 10px;
//     background: transparent; border: none; cursor: pointer;
//     color: rgba(255,255,255,0.38); transition: color 0.15s;
//   }
//   .hp-search-clear:hover { color: rgba(255,255,255,0.8); }
//   .hp-search-btn {
//     background: #ff8c00; border: none; color: #fff;
//     font-family: 'DM Sans', sans-serif; font-size: 13px; font-weight: 600;
//     padding: 0 22px; cursor: pointer; transition: background 0.2s;
//     letter-spacing: 0.03em;
//   }
//   .hp-search-btn:hover { background: #e07800; }
//
//   @keyframes fadeSlideUp {
//     from { opacity: 0; transform: translateY(18px); }
//     to   { opacity: 1; transform: translateY(0);    }
//   }
//
//   /* ══ MAIN ══ */
//   .hp-main {
//     max-width: 1200px; margin: 0 auto; padding: 32px 24px 64px;
//     display: grid; grid-template-columns: 260px 1fr;
//     gap: 32px; align-items: start;
//   }
//   @media (max-width: 900px) {
//     .hp-main { grid-template-columns: 1fr; }
//     .hp-sidebar { display: none; }
//   }
//
//   /* Sidebar */
//   .hp-sidebar { position: sticky; top: 24px; display: flex; flex-direction: column; gap: 8px; }
//   .hp-filter-card { background: #fff; border-radius: 14px; border: 1px solid #ebebeb; overflow: hidden; }
//   .hp-filter-header {
//     display: flex; align-items: center; justify-content: space-between;
//     padding: 16px 18px; cursor: pointer; user-select: none;
//     border-bottom: 1px solid #f0f0f0;
//   }
//   .hp-filter-header-title {
//     display: flex; align-items: center; gap: 8px;
//     font-size: 13px; font-weight: 600; color: #1a1a1a;
//     letter-spacing: 0.02em; text-transform: uppercase;
//   }
//   .hp-filter-body { padding: 16px 18px; }
//
//   .hp-cat-item {
//     display: flex; align-items: center; gap: 10px;
//     padding: 9px 12px; border-radius: 9px; cursor: pointer;
//     font-size: 13.5px; color: #444; font-weight: 400;
//     transition: all 0.15s; margin-bottom: 2px;
//   }
//   .hp-cat-item:hover { background: #f5f5f5; color: #1a1a1a; }
//   .hp-cat-item.active { background: #fff5e6; color: #cc6f00; font-weight: 600; }
//   .hp-cat-item-icon { opacity: 0.6; flex-shrink: 0; }
//   .hp-cat-item.active .hp-cat-item-icon { opacity: 1; }
//   .hp-cat-count {
//     margin-left: auto; font-size: 11px; color: #aaa;
//     background: #f5f5f5; padding: 2px 7px; border-radius: 100px;
//   }
//   .hp-cat-item.active .hp-cat-count { background: rgba(255,140,0,0.15); color: #cc6f00; }
//
//   .hp-price-row { display: flex; gap: 8px; align-items: center; }
//   .hp-price-input {
//     flex: 1; border: 1px solid #e0e0e0; border-radius: 8px;
//     padding: 9px 12px; font-size: 13px; font-family: 'DM Sans', sans-serif;
//     outline: none; transition: border-color 0.15s; background: #fafafa; color: #1a1a1a;
//   }
//   .hp-price-input:focus { border-color: #ff8c00; background: #fff; }
//   .hp-price-sep { color: #ccc; font-size: 13px; flex-shrink: 0; }
//   .hp-price-presets { display: flex; flex-wrap: wrap; gap: 6px; margin-top: 10px; }
//   .hp-price-preset {
//     border: 1px solid #e0e0e0; background: #fafafa; color: #555;
//     font-size: 12px; font-family: 'DM Sans', sans-serif;
//     padding: 5px 10px; border-radius: 7px; cursor: pointer; transition: all 0.15s;
//   }
//   .hp-price-preset:hover { border-color: #ff8c00; color: #ff8c00; }
//   .hp-price-preset.active { background: #ff8c00; border-color: #ff8c00; color: #fff; font-weight: 600; }
//
//   .hp-select {
//     width: 100%; border: 1px solid #e0e0e0; border-radius: 8px;
//     padding: 9px 12px; font-size: 13px; font-family: 'DM Sans', sans-serif;
//     outline: none; background: #fafafa; color: #1a1a1a; cursor: pointer; transition: border-color 0.15s;
//   }
//   .hp-select:focus { border-color: #ff8c00; }
//
//   .hp-apply-btn {
//     width: 100%; background: #0f1923; color: #fff; border: none;
//     border-radius: 9px; padding: 12px; font-family: 'DM Sans', sans-serif;
//     font-size: 13px; font-weight: 600; cursor: pointer; transition: background 0.2s;
//     margin-top: 4px; letter-spacing: 0.03em;
//   }
//   .hp-apply-btn:hover { background: #ff8c00; }
//   .hp-reset-btn {
//     width: 100%; background: transparent; color: #888;
//     border: 1px solid #e8e8e8; border-radius: 9px; padding: 10px;
//     font-family: 'DM Sans', sans-serif; font-size: 12px; font-weight: 500;
//     cursor: pointer; transition: all 0.15s; margin-top: 6px;
//   }
//   .hp-reset-btn:hover { border-color: #ccc; color: #333; }
//
//   .hp-content {}
//
//   .hp-mobile-bar { display: none; gap: 10px; margin-bottom: 20px; align-items: center; }
//   @media (max-width: 900px) { .hp-mobile-bar { display: flex; } }
//   .hp-mobile-filter-btn {
//     display: flex; align-items: center; gap: 7px;
//     background: #fff; border: 1px solid #e0e0e0; border-radius: 9px;
//     padding: 9px 16px; font-family: 'DM Sans', sans-serif; font-size: 13px;
//     font-weight: 500; cursor: pointer; color: #1a1a1a; transition: all 0.15s;
//   }
//   .hp-mobile-filter-btn:hover { border-color: #ff8c00; color: #ff8c00; }
//   .hp-mobile-filter-btn.has-filters { background: #0f1923; color: #fff; border-color: #0f1923; }
//
//   .hp-cat-pill {
//     display: inline-flex; align-items: center; gap: 6px; white-space: nowrap;
//     border: 1px solid #e0e0e0; background: #fff; color: #555;
//     font-size: 13px; font-family: 'DM Sans', sans-serif; font-weight: 500;
//     padding: 8px 14px; border-radius: 100px; cursor: pointer; transition: all 0.15s; flex-shrink: 0;
//   }
//   .hp-cat-pill:hover { border-color: #ff8c00; color: #ff8c00; }
//   .hp-cat-pill.active { background: #ff8c00; border-color: #ff8c00; color: #fff; }
//
//   .hp-toolbar {
//     display: flex; align-items: center; justify-content: space-between;
//     margin-bottom: 20px; gap: 12px; flex-wrap: wrap;
//   }
//   .hp-toolbar-left { display: flex; align-items: baseline; gap: 10px; }
//   .hp-toolbar-title { font-family: 'DM Serif Display', serif; font-size: 1.4rem; color: #1a1a1a; font-weight: 400; }
//   .hp-toolbar-count { font-size: 13px; color: #999; font-weight: 400; }
//   .hp-sort-select {
//     display: flex; align-items: center; gap: 6px;
//     border: 1px solid #e0e0e0; border-radius: 9px; padding: 8px 12px;
//     font-family: 'DM Sans', sans-serif; font-size: 13px;
//     background: #fff; color: #333; cursor: pointer; outline: none; transition: border-color 0.15s;
//   }
//   .hp-sort-select:focus { border-color: #ff8c00; }
//
//   .hp-chips { display: flex; gap: 8px; flex-wrap: wrap; margin-bottom: 20px; }
//   .hp-chip {
//     display: inline-flex; align-items: center; gap: 6px;
//     background: #fff5e6; border: 1px solid rgba(255,140,0,0.25); color: #cc6f00;
//     font-size: 12px; font-weight: 500; padding: 5px 10px 5px 12px; border-radius: 100px;
//     cursor: pointer; transition: all 0.15s; font-family: 'DM Sans', sans-serif;
//   }
//   .hp-chip:hover { background: #ffe8cc; }
//   .hp-chip-x { opacity: 0.6; transition: opacity 0.15s; }
//   .hp-chip:hover .hp-chip-x { opacity: 1; }
//   .hp-chip-clear { background: #fff0f0; border-color: rgba(220,53,69,0.2); color: #c0392b; }
//   .hp-chip-clear:hover { background: #fde0e0; }
//
//   .hp-featured-label {
//     display: flex; align-items: center; gap: 8px;
//     font-size: 12px; font-weight: 600; letter-spacing: 0.1em;
//     text-transform: uppercase; color: #ff8c00; margin-bottom: 14px;
//   }
//   .hp-featured-grid {
//     display: grid; grid-template-columns: repeat(auto-fill, minmax(200px, 1fr));
//     gap: 16px; margin-bottom: 36px;
//   }
//   .hp-product-grid {
//     display: grid; grid-template-columns: repeat(auto-fill, minmax(210px, 1fr)); gap: 16px;
//   }
//
//   .hp-loading { display: flex; flex-direction: column; align-items: center; justify-content: center; padding: 80px 0; color: #aaa; gap: 14px; }
//   .hp-spinner { width: 36px; height: 36px; border: 2.5px solid #ebebeb; border-top-color: #ff8c00; border-radius: 50%; animation: hp-spin 0.7s linear infinite; }
//   @keyframes hp-spin { to { transform: rotate(360deg); } }
//
//   .hp-empty { text-align: center; padding: 80px 24px; color: #aaa; }
//   .hp-empty-icon { margin-bottom: 16px; opacity: 0.3; }
//   .hp-empty h3 { font-family: 'DM Serif Display', serif; font-size: 1.3rem; color: #333; margin: 0 0 8px; font-weight: 400; }
//   .hp-empty p { font-size: 14px; margin: 0 0 20px; }
//   .hp-empty-btn { background: #ff8c00; color: #fff; border: none; border-radius: 9px; padding: 10px 24px; font-family: 'DM Sans', sans-serif; font-size: 13px; font-weight: 600; cursor: pointer; transition: background 0.2s; }
//   .hp-empty-btn:hover { background: #e07800; }
//
//   .hp-pagination { display: flex; align-items: center; justify-content: center; gap: 6px; margin-top: 36px; }
//   .hp-page-btn { width: 36px; height: 36px; display: flex; align-items: center; justify-content: center; border: 1px solid #e0e0e0; border-radius: 9px; background: #fff; color: #555; font-family: 'DM Sans', sans-serif; font-size: 13px; font-weight: 500; cursor: pointer; transition: all 0.15s; }
//   .hp-page-btn:hover:not(:disabled) { border-color: #ff8c00; color: #ff8c00; }
//   .hp-page-btn.active { background: #ff8c00; border-color: #ff8c00; color: #fff; font-weight: 600; }
//   .hp-page-btn:disabled { opacity: 0.35; cursor: default; }
//   .hp-page-ellipsis { color: #ccc; font-size: 14px; padding: 0 4px; }
//
//   .hp-drawer-overlay { position: fixed; inset: 0; background: rgba(0,0,0,0.4); z-index: 999; opacity: 0; pointer-events: none; transition: opacity 0.25s; }
//   .hp-drawer-overlay.open { opacity: 1; pointer-events: all; }
//   .hp-drawer { position: fixed; top: 0; right: 0; bottom: 0; width: 300px; background: #fff; z-index: 1000; transform: translateX(100%); transition: transform 0.25s cubic-bezier(0.4,0,0.2,1); display: flex; flex-direction: column; box-shadow: -4px 0 24px rgba(0,0,0,0.12); }
//   .hp-drawer.open { transform: translateX(0); }
//   .hp-drawer-header { display: flex; align-items: center; justify-content: space-between; padding: 20px 20px 16px; border-bottom: 1px solid #f0f0f0; }
//   .hp-drawer-title { display: flex; align-items: center; gap: 8px; font-size: 15px; font-weight: 600; color: #1a1a1a; }
//   .hp-drawer-close { background: #f5f5f5; border: none; width: 30px; height: 30px; border-radius: 50%; display: flex; align-items: center; justify-content: center; cursor: pointer; color: #555; transition: background 0.15s; }
//   .hp-drawer-close:hover { background: #eee; }
//   .hp-drawer-body { flex: 1; overflow-y: auto; padding: 20px; }
//   .hp-drawer-footer { padding: 16px 20px; border-top: 1px solid #f0f0f0; display: flex; flex-direction: column; gap: 8px; }
//   .hp-drawer-section { margin-bottom: 24px; }
//   .hp-drawer-section-title { font-size: 11px; font-weight: 600; letter-spacing: 0.1em; text-transform: uppercase; color: #aaa; margin-bottom: 10px; }
// `
//
// export const HomePage = () => {
//   const navigate = useNavigate()
//   const [searchParams, setSearchParams] = useSearchParams()
//
//   const [filters, setFilters] = useState(() => ({
//     ...defaultFilters,
//     keyword:  searchParams.get('q')        || '',
//     brand:    searchParams.get('brand')    || '',
//     minPrice: searchParams.get('minPrice') || '',
//     maxPrice: searchParams.get('maxPrice') || '',
//   }))
//   const [draft, setDraft] = useState(filters)
//
//   const [products,   setProducts]   = useState([])
//   const [featured,   setFeatured]   = useState([])
//   const [brands,     setBrands]     = useState([])
//   const [categories, setCategories] = useState([])
//   const [totalPages, setTotalPages] = useState(0)
//   const [totalItems, setTotalItems] = useState(0)
//   const [isLoading,  setIsLoading]  = useState(false)
//   const [showDrawer, setShowDrawer] = useState(false)
//   const [openSections, setOpenSections] = useState({ category: true, price: true, brand: true })
//
//   const { addToCart } = useCart()
//   const debounceRef   = useRef(null)
//
//   useEffect(() => {
//     productService.getFeaturedProducts().then(setFeatured).catch(() => {})
//     productService.getAllBrands().then(setBrands).catch(() => {})
//     categoryService.getRootCategories().then(setCategories).catch(() => {})
//   }, [])
//
//   useEffect(() => {
//     const params = {}
//     if (filters.keyword)    params.q          = filters.keyword
//     if (filters.brand)      params.brand      = filters.brand
//     if (filters.minPrice)   params.minPrice   = filters.minPrice
//     if (filters.maxPrice)   params.maxPrice   = filters.maxPrice
//     if (filters.categoryId) params.categoryId = filters.categoryId
//     setSearchParams(params, { replace: true })
//   }, [filters]) // eslint-disable-line
//
//   const fetchProducts = useCallback(async (f) => {
//     setIsLoading(true)
//     try {
//       const sort = SORT_OPTIONS[f.sortIdx] || SORT_OPTIONS[0]
//       const resp = await productService.searchProducts({
//         query:       f.keyword    || undefined,
//         categoryIds: f.categoryIds?.length ? f.categoryIds : undefined,
//         brand:       f.brand      || undefined,
//         minPrice:    f.minPrice !== '' ? Number(f.minPrice) : undefined,
//         maxPrice:    f.maxPrice !== '' ? Number(f.maxPrice) : undefined,
//         page:        f.page,
//         size:        PAGE_SIZE,
//         sortBy:      sort.sortBy,
//         sortDir:     sort.sortDir,
//       })
//       setProducts(resp?.content || [])
//       setTotalPages(resp?.totalPages || 1)
//       setTotalItems(resp?.totalElements || 0)
//     } catch {
//       toast.error('Failed to load products')
//     } finally {
//       setIsLoading(false)
//     }
//   }, [])
//
//   useEffect(() => { fetchProducts(filters) }, [filters]) // eslint-disable-line
//
//   const handleKeywordChange = (e) => {
//     const kw = e.target.value
//     setDraft(d => ({ ...d, keyword: kw }))
//     clearTimeout(debounceRef.current)
//     debounceRef.current = setTimeout(() =>
//       setFilters(f => ({ ...f, keyword: kw, page: 0 })), 350)
//   }
//
//   const handleCategoryClick = (catId) => {
//     if (filters.categoryId === catId) {
//       setFilters(f => ({ ...f, categoryId: null, categoryIds: null, page: 0 }))
//       setDraft(d  => ({ ...d, categoryId: null, categoryIds: null }))
//     } else {
//       const cat = categories.find(c => c.id === catId)
//       const ids = [catId, ...(cat?.children || []).map(ch => ch.id)]
//       const next = { ...defaultFilters, categoryId: catId, categoryIds: ids }
//       setDraft(next); setFilters(next)
//     }
//   }
//
//   const handleDraftCategoryClick = (catId) => {
//     if (draft.categoryId === catId) {
//       setDraft(d => ({ ...d, categoryId: null, categoryIds: null }))
//     } else {
//       const cat = categories.find(c => c.id === catId)
//       const ids = [catId, ...(cat?.children || []).map(ch => ch.id)]
//       setDraft(d => ({ ...d, categoryId: catId, categoryIds: ids }))
//     }
//   }
//
//   const handleSortChange = (e) => {
//     const idx = Number(e.target.value)
//     setFilters(f => ({ ...f, sortIdx: idx, page: 0 }))
//     setDraft(d  => ({ ...d, sortIdx: idx }))
//   }
//
//   const handlePageChange = (page) => {
//     setFilters(f => ({ ...f, page }))
//     window.scrollTo({ top: 0, behavior: 'smooth' })
//   }
//
//   const applyDraft   = () => { setFilters({ ...draft, page: 0 }); setShowDrawer(false) }
//   const resetFilters = () => { setDraft(defaultFilters); setFilters(defaultFilters); setShowDrawer(false) }
//   const removeFilter = (key) => {
//     const reset = { [key]: defaultFilters[key], page: 0 }
//     if (key === 'categoryId') reset.categoryIds = null
//     setFilters(f => ({ ...f, ...reset }))
//     setDraft(d  => ({ ...d, ...reset }))
//   }
//
//   const toggleSection = (key) => setOpenSections(s => ({ ...s, [key]: !s[key] }))
//
//   const applySidebarFilter = (patch) => {
//     const next = { ...filters, ...patch, page: 0 }
//     setFilters(next); setDraft(next)
//   }
//
//   const handleAddToCart = (product) => {
//     addToCart(product)
//     toast.success(`${product.name} added to cart`)
//   }
//
//   const filterCount      = activeFilterCount(filters)
//   const selectedCategory = categories.find(c => c.id === filters.categoryId)
//
//   const chips = []
//   if (filters.keyword)    chips.push({ key: 'keyword',    label: filters.keyword })
//   if (filters.categoryId) chips.push({ key: 'categoryId', label: selectedCategory?.name || 'Category' })
//   if (filters.brand)      chips.push({ key: 'brand',      label: filters.brand })
//   if (filters.minPrice)   chips.push({ key: 'minPrice',   label: `From $${filters.minPrice}` })
//   if (filters.maxPrice)   chips.push({ key: 'maxPrice',   label: `Up to $${filters.maxPrice}` })
//
//   const paginationItems = []
//   const buildPages = () => {
//     if (totalPages <= 7) {
//       for (let i = 0; i < totalPages; i++) paginationItems.push(i)
//     } else {
//       const cur = filters.page
//       const set = new Set([0, totalPages - 1, cur, cur - 1, cur + 1].filter(p => p >= 0 && p < totalPages))
//       Array.from(set).sort((a, b) => a - b).forEach((p, i, arr) => {
//         if (i > 0 && p - arr[i - 1] > 1) paginationItems.push('...')
//         paginationItems.push(p)
//       })
//     }
//   }
//   buildPages()
//
//   const FilterPanel = ({ isDraft = false }) => {
//     const vals = isDraft ? draft : filters
//     const set  = isDraft
//       ? (patch) => setDraft(d => ({ ...d, ...patch }))
//       : (patch) => applySidebarFilter(patch)
//     const onCatClick = isDraft ? handleDraftCategoryClick : handleCategoryClick
//
//     return (
//       <>
//         <div className="hp-filter-card">
//           <div className="hp-filter-header" onClick={() => toggleSection('category')}>
//             <span className="hp-filter-header-title"><LayoutGrid size={14} />Category</span>
//             {openSections.category ? <ChevronUp size={14} color="#aaa"/> : <ChevronDown size={14} color="#aaa"/>}
//           </div>
//           {openSections.category && (
//             <div className="hp-filter-body" style={{ paddingTop: 8 }}>
//               <div className={`hp-cat-item ${!vals.categoryId ? 'active' : ''}`}
//                 onClick={() => set({ categoryId: null, categoryIds: null })}>
//                 <ShoppingBag size={15} className="hp-cat-item-icon"/>
//                 All Products
//                 <span className="hp-cat-count">{totalItems}</span>
//               </div>
//               {categories.map(cat => {
//                 const Icon  = getCategoryIcon(cat.name)
//                 const total = cat.productCount + (cat.children || []).reduce((s, c) => s + c.productCount, 0)
//                 return (
//                   <div key={cat.id}
//                     className={`hp-cat-item ${vals.categoryId === cat.id ? 'active' : ''}`}
//                     onClick={() => onCatClick(cat.id)}>
//                     <Icon size={15} className="hp-cat-item-icon"/>
//                     {cat.name}
//                     <span className="hp-cat-count">{total}</span>
//                   </div>
//                 )
//               })}
//             </div>
//           )}
//         </div>
//
//         <div className="hp-filter-card">
//           <div className="hp-filter-header" onClick={() => toggleSection('price')}>
//             <span className="hp-filter-header-title"><Tag size={14}/>Price Range</span>
//             {openSections.price ? <ChevronUp size={14} color="#aaa"/> : <ChevronDown size={14} color="#aaa"/>}
//           </div>
//           {openSections.price && (
//             <div className="hp-filter-body">
//               <div className="hp-price-row">
//                 <input className="hp-price-input" type="number" min={0} placeholder="Min"
//                   value={vals.minPrice} onChange={e => set({ minPrice: e.target.value })}/>
//                 <span className="hp-price-sep">—</span>
//                 <input className="hp-price-input" type="number" min={0} placeholder="Max"
//                   value={vals.maxPrice} onChange={e => set({ maxPrice: e.target.value })}/>
//               </div>
//               <div className="hp-price-presets">
//                 {PRICE_PRESETS.map(p => (
//                   <button key={p.label}
//                     className={`hp-price-preset ${vals.minPrice === p.min && vals.maxPrice === p.max ? 'active' : ''}`}
//                     onClick={() => set({ minPrice: p.min, maxPrice: p.max })}>
//                     {p.label}
//                   </button>
//                 ))}
//               </div>
//             </div>
//           )}
//         </div>
//
//         {brands.length > 0 && (
//           <div className="hp-filter-card">
//             <div className="hp-filter-header" onClick={() => toggleSection('brand')}>
//               <span className="hp-filter-header-title"><Star size={14}/>Brand</span>
//               {openSections.brand ? <ChevronUp size={14} color="#aaa"/> : <ChevronDown size={14} color="#aaa"/>}
//             </div>
//             {openSections.brand && (
//               <div className="hp-filter-body">
//                 <select className="hp-select" value={vals.brand} onChange={e => set({ brand: e.target.value })}>
//                   <option value="">All brands</option>
//                   {brands.map(b => <option key={b} value={b}>{b}</option>)}
//                 </select>
//               </div>
//             )}
//           </div>
//         )}
//       </>
//     )
//   }
//
//   return (
//     <>
//       <style>{css}</style>
//       <div className="hp-root">
//
//         {/* ══════════════ HERO ══════════════ */}
//         <div className="hp-hero">
//           <div className="hp-hero-inner">
//
//             {/* Left: copy + search */}
//             <div>
//               <div className="hp-hero-eyebrow">
//                 <Zap size={11}/>
//                 Best prices, every day
//               </div>
//               <h1>Discover <em>everything</em><br/>you need, online.</h1>
//               <p className="hp-hero-sub">
//                 Thousands of products across hundreds of categories,<br/>
//                 delivered fast to your door.
//               </p>
//
//               {/* Inline stats (replaced the card) */}
//               <div className="hp-hero-stats">
//                 <div className="hp-hero-stat-item">
//                   <div className="hp-hero-stat-num">{totalItems || '—'}</div>
//                   <div className="hp-hero-stat-label">Products</div>
//                 </div>
//                 <div className="hp-hero-stat-divider"/>
//                 <div className="hp-hero-stat-item">
//                   <div className="hp-hero-stat-num">{categories.length}</div>
//                   <div className="hp-hero-stat-label">Categories</div>
//                 </div>
//                 <div className="hp-hero-stat-divider"/>
//                 <div className="hp-hero-stat-item">
//                   <div className="hp-hero-stat-num">{brands.length}</div>
//                   <div className="hp-hero-stat-label">Brands</div>
//                 </div>
//               </div>
//
//               <div className="hp-search-wrap">
//                 <span className="hp-search-icon"><Search size={16}/></span>
//                 <input
//                   className="hp-search-input"
//                   placeholder="Search products, brands, categories…"
//                   value={draft.keyword}
//                   onChange={handleKeywordChange}
//                   onKeyDown={e => e.key === 'Enter' &&
//                     setFilters(f => ({ ...f, keyword: draft.keyword, page: 0 }))}
//                 />
//                 {draft.keyword && (
//                   <button className="hp-search-clear" onClick={() => {
//                     setDraft(d => ({ ...d, keyword: '' }))
//                     setFilters(f => ({ ...f, keyword: '', page: 0 }))
//                   }}><X size={14}/></button>
//                 )}
//                 <button className="hp-search-btn"
//                   onClick={() => setFilters(f => ({ ...f, keyword: draft.keyword, page: 0 }))}>
//                   Search
//                 </button>
//               </div>
//             </div>
//
//             {/* Right: 3-D cartoon shop illustration */}
//             <div className="hp-hero-illustration">
//               <CartoonShop3D />
//             </div>
//
//           </div>
//         </div>
//
//         {/* ══════════════ MAIN ══════════════ */}
//         <div className="hp-main">
//
//           <aside className="hp-sidebar">
//             <FilterPanel isDraft={false}/>
//             {filterCount > 0 && (
//               <button className="hp-reset-btn" onClick={resetFilters}>Clear all filters</button>
//             )}
//           </aside>
//
//           <div className="hp-content">
//
//             <div className="hp-mobile-bar">
//               <button
//                 className={`hp-mobile-filter-btn ${filterCount > 0 ? 'has-filters' : ''}`}
//                 onClick={() => { setDraft(filters); setShowDrawer(true) }}>
//                 <SlidersHorizontal size={15}/>
//                 Filters {filterCount > 0 && `(${filterCount})`}
//               </button>
//               {categories.map(cat => {
//                 const Icon = getCategoryIcon(cat.name)
//                 return (
//                   <button key={cat.id}
//                     className={`hp-cat-pill ${filters.categoryId === cat.id ? 'active' : ''}`}
//                     onClick={() => handleCategoryClick(cat.id)}>
//                     <Icon size={13}/>{cat.name}
//                   </button>
//                 )
//               })}
//             </div>
//
//             {featured.length > 0 && filterCount === 0 && (
//               <div style={{ marginBottom: 32 }}>
//                 <div className="hp-featured-label"><Flame size={14}/>Featured picks</div>
//                 <div className="hp-featured-grid">
//                   {featured.map(p => (
//                     <ProductCard key={p.id} product={p}
//                       onAddToCart={handleAddToCart}
//                       onViewDetails={(id) => navigate(`/product/${id}`)}/>
//                   ))}
//                 </div>
//               </div>
//             )}
//
//             <div className="hp-toolbar">
//               <div className="hp-toolbar-left">
//                 <span className="hp-toolbar-title">
//                   {selectedCategory ? selectedCategory.name : 'All Products'}
//                 </span>
//                 {totalItems > 0 && (
//                   <span className="hp-toolbar-count">{totalItems.toLocaleString()} items</span>
//                 )}
//               </div>
//               <div style={{ display: 'flex', alignItems: 'center', gap: 8 }}>
//                 <ArrowUpDown size={14} color="#aaa"/>
//                 <select className="hp-sort-select" value={filters.sortIdx} onChange={handleSortChange}>
//                   {SORT_OPTIONS.map((o, i) => (
//                     <option key={i} value={i}>{o.label}</option>
//                   ))}
//                 </select>
//               </div>
//             </div>
//
//             {chips.length > 0 && (
//               <div className="hp-chips">
//                 {chips.map(c => (
//                   <button key={c.key} className="hp-chip" onClick={() => removeFilter(c.key)}>
//                     {c.label}<X size={12} className="hp-chip-x"/>
//                   </button>
//                 ))}
//                 <button className="hp-chip hp-chip-clear" onClick={resetFilters}>
//                   Clear all<X size={12} className="hp-chip-x"/>
//                 </button>
//               </div>
//             )}
//
//             {isLoading ? (
//               <div className="hp-loading">
//                 <div className="hp-spinner"/>
//                 <span style={{ fontSize: 13 }}>Loading products…</span>
//               </div>
//             ) : products.length === 0 ? (
//               <div className="hp-empty">
//                 <div className="hp-empty-icon"><Package size={48}/></div>
//                 <h3>No products found</h3>
//                 <p>Try adjusting your search or removing some filters</p>
//                 <button className="hp-empty-btn" onClick={resetFilters}>Clear filters</button>
//               </div>
//             ) : (
//               <div className="hp-product-grid">
//                 {products.map(product => (
//                   <ProductCard key={product.id} product={product}
//                     onAddToCart={handleAddToCart}
//                     onViewDetails={(id) => navigate(`/product/${id}`)}/>
//                 ))}
//               </div>
//             )}
//
//             {totalPages > 1 && (
//               <div className="hp-pagination">
//                 <button className="hp-page-btn" disabled={filters.page === 0}
//                   onClick={() => handlePageChange(filters.page - 1)}>
//                   <ChevronLeft size={15}/>
//                 </button>
//                 {paginationItems.map((item, i) =>
//                   item === '...' ? (
//                     <span key={`e${i}`} className="hp-page-ellipsis">…</span>
//                   ) : (
//                     <button key={item}
//                       className={`hp-page-btn ${item === filters.page ? 'active' : ''}`}
//                       onClick={() => handlePageChange(item)}>
//                       {item + 1}
//                     </button>
//                   )
//                 )}
//                 <button className="hp-page-btn" disabled={filters.page >= totalPages - 1}
//                   onClick={() => handlePageChange(filters.page + 1)}>
//                   <ChevronRight size={15}/>
//                 </button>
//               </div>
//             )}
//
//           </div>
//         </div>
//
//         {/* ══ MOBILE DRAWER ══ */}
//         <div className={`hp-drawer-overlay ${showDrawer ? 'open' : ''}`} onClick={() => setShowDrawer(false)}/>
//         <div className={`hp-drawer ${showDrawer ? 'open' : ''}`}>
//           <div className="hp-drawer-header">
//             <div className="hp-drawer-title"><SlidersHorizontal size={16}/>Filters</div>
//             <button className="hp-drawer-close" onClick={() => setShowDrawer(false)}><X size={15}/></button>
//           </div>
//           <div className="hp-drawer-body"><FilterPanel isDraft={true}/></div>
//           <div className="hp-drawer-footer">
//             <button className="hp-apply-btn" onClick={applyDraft}>Apply Filters</button>
//             <button className="hp-reset-btn" onClick={resetFilters}>Reset all</button>
//           </div>
//         </div>
//
//       </div>
//     </>
//   )
// }
//
// export default HomePage